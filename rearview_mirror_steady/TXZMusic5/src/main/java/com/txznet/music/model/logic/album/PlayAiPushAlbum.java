package com.txznet.music.model.logic.album;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.source.AiPushDataSource;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.PlayQueueHelper;
import com.txznet.music.helper.ProxyHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.PlayInfoEvent;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.DisposableManager;
import com.txznet.rxflux.Operation;

import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 播放其他电台的处理逻辑
 *
 * @author zackzhou
 * @date 2018/12/17,10:42
 */

public class PlayAiPushAlbum extends AbstractPlayAlbum {
    public PlayAiPushAlbum(Album album) {
        super(album);
    }

    public void onPlayAlbumWithAudio(Operation operation, Audio audio) {
        // 防止加载更多互串
        DisposableManager.get().remove("loadMore");
        AudioV5 currAudio = PlayHelper.get().getCurrAudio();
        if (currAudio != null) {
            ProxyHelper.releaseProxyRequest(currAudio.sid, currAudio.id);

            ReportEvent.reportAudioPlayEnd(currAudio,
                    PlayInfoEvent.MANUAL_TYPE_MANUAL,
                    AudioUtils.isLocalSong(currAudio.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE, PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(),
                    Operation.SOUND == operation ? PlayInfoEvent.EXIT_TYPE_SOUND : Operation.MANUAL == operation ? PlayInfoEvent.EXIT_TYPE_MANUAL : PlayInfoEvent.EXIT_TYPE_OTHER, false);
        }

        if (PlayScene.AI_RADIO != PlayHelper.get().getCurrPlayScene()) {
            // 清空当前播放内容
            PlayHelper.get().clearNotAlbum();
            PlayHelper.get().setPlayScene(PlayScene.AI_RADIO);
            PlayHelper.get().setCurrAlbum(album);
        }
        PlayHelper.get().useRadioPlayMode();
        if (audio != null) {
            // 保存到DataSource
            if (AiPushDataSource.get().size() > 0) {
                AiPushDataSource.get().pushFirst(Collections.singletonList(audio));
            } else {
                AiPushDataSource.get().push(Collections.singletonList(audio));
            }
            Audio poll = AiPushDataSource.get().poll();
            int pos = AudioPlayer.getDefault().getQueue().getCurrentPosition();
            if (pos >= 0) {
                AudioPlayer.getDefault().getQueue().remove(poll);
                AudioPlayer.getDefault().getQueue().addToQueue(poll, pos + 1);
            }
            PlayHelper.get().play(poll);
        }
        Disposable disposable = TXZMusicDataSource.get().listAudios(album, null, null, Configuration.DefVal.PAGE_COUNT_AI)
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.single())
                .subscribe(audioV5List -> {
                    List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
                    PlayQueueHelper.get().distinctQueue(AudioPlayer.getDefault().getQueue().getQueue(), audioList);
                    // FIXME: 2019/4/13 已经处于AI电台下声控插入，不能清除当前内容
                    if (audioList.isEmpty() && AudioPlayer.getDefault().getQueue().getSize() == 0) {
                        notifyAlbumEmpty(operation);
                        return;
                    }
                    // 保存到DataSource
                    AiPushDataSource.get().clear();
                    AiPushDataSource.get().push(audioList);
                }, throwable -> {
                    notifyRequestError(operation);
                    check2DoRetry(operation, throwable);
                });
        DisposableManager.get().add("onPlayAlbum", disposable);
    }

    @Override
    public void onPlayAlbumInner(Operation operation) {
        Disposable disposable = TXZMusicDataSource.get().listAudios(album, null, null, Configuration.DefVal.PAGE_COUNT_AI)
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.single())
                .subscribe(audioV5List -> {
                    if (audioV5List == null || audioV5List.isEmpty()) {
                        notifyAlbumEmpty(operation);
                    } else {
                        List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
                        // 保存到DataSource
                        AiPushDataSource.get().clear();
                        AiPushDataSource.get().push(audioList);

                        PlayHelper.get().useRadioPlayMode();
                        PlayHelper.get().play(AiPushDataSource.get().poll());
                    }
                }, throwable -> {
                    notifyRequestError(operation);
                    check2DoRetry(operation, throwable);
                });
        DisposableManager.get().add("onPlayAlbum", disposable);
    }
}
