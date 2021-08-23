package com.txznet.music.model.logic.queue;

import com.txznet.audio.player.entity.Audio;
import com.txznet.audio.player.queue.PlayQueue;
import com.txznet.music.Constant;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.PlayQueueHelper;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.TtsHelper;
import com.txznet.rxflux.Operation;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 其他电台专辑下的音频选择器
 *
 * @author zackzhou
 * @date 2018/12/18,15:24
 */

public class OtherQueueItemPicker extends AbstractQueueItemPicker {
    public OtherQueueItemPicker(Album album) {
        super(album);
    }

    @Override
    public void pickPrevItem(PlayQueue queue, Audio oriAudio, Callback callback) {
        super.pickPrevItem(queue, oriAudio, callback);
        if (queue.isEmpty()) {
            return;
        }
        // 已经是第一个的处理逻辑
        if (queue.getCurrentPosition() == 0) {
            // 尝试拉取上一页数据
            Audio firstItem = queue.getFirstItem();
            Disposable disposable = TXZMusicDataSource.get().listAudios(album, AudioConverts.convert2Audio(firstItem), "1-" + firstItem.sid + "-" + firstItem.id, Configuration.DefVal.PAGE_COUNT)
                    .subscribeOn(Schedulers.io()).observeOn(Schedulers.single())
                    .subscribe(audioV5List -> {
                        if (audioV5List.isEmpty()) {
                            notifyAlreadyFirst(PlayHelper.get().getLastSwitchAudioOperation());
                        } else {
                            List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
                            PlayQueueHelper.get().distinctQueue(queue.getQueue(), audioList);
                            queue.addToQueue(audioList, 0);
                            callback.onPickResult(audioList.get(audioList.size() - 1));
                        }
                    }, throwable -> {
                        throwable.printStackTrace();
                        notifyRequestError(PlayHelper.get().getLastSwitchAudioOperation());
                    });
            DisposableManager.get().add("pickerInvoke", disposable);
            return;
        }

        callback.onPickResult(oriAudio);
    }

    @Override
    public void pickNextItem(PlayQueue queue, Audio oriAudio, boolean fromUser, Callback callback) {
        super.pickNextItem(queue, oriAudio, fromUser, callback);
        if (queue.isEmpty()) {
            return;
        }
        // 当前播放的是最后一首
        if (queue.getCurrentPosition() == queue.getSize() - 1) {
            // 专辑场景，拉取下一批
            Audio lastAudio = queue.getLastItem();
            if (lastAudio != null) {
                Disposable disposable = TXZMusicDataSource.get().listAudios(album, AudioConverts.convert2Audio(lastAudio), "0-" + lastAudio.sid + "-" + lastAudio.id + "-" + lastAudio.albumId, Configuration.DefVal.PAGE_COUNT)
                        .subscribeOn(Schedulers.io()).observeOn(Schedulers.single())
                        .subscribe(audioV5List -> {
                            // 没有下一页
                            if (audioV5List.isEmpty()) {
                                onAlbumEnd(PlayHelper.get().getLastSwitchAudioOperation(), fromUser);
                            } else {
                                // 播放新拉取的第一个
                                List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
                                PlayQueueHelper.get().distinctQueue(queue.getQueue(), audioList);
                                // 没有下一页
                                if (audioList.isEmpty()) {
                                    onAlbumEnd(PlayHelper.get().getLastSwitchAudioOperation(), fromUser);
                                } else {
                                    queue.addToQueue(audioList);
                                    callback.onPickResult(audioList.get(0));
                                }
                            }
                        }, throwable -> {
                            throwable.printStackTrace();
                            notifyRequestError(PlayHelper.get().getLastSwitchAudioOperation());
                        });
                DisposableManager.get().add("pickerInvoke", disposable);
                return;
            }
        }
        callback.onPickResult(oriAudio);
    }

    // 专辑播放完毕
    private void onAlbumEnd(Operation operation, boolean fromUser) {
        if (fromUser) {
            if (Operation.SOUND == operation) {
                TtsHelper.speakResource("RS_VOICE_ALREADY_LAST", Constant.RS_VOICE_ALREADY_LAST);
            } else {
                ToastUtils.showShortOnUI(Constant.RS_VOICE_ALREADY_LAST);
            }
        } else {
            TtsHelper.speakResource("RS_VOICE_SPEAK_PLAY_END", Constant.RS_VOICE_SPEAK_PLAY_END);
            Logger.d(TAG, "onAlbumEnd album=" + album.name);
            album.isPlayEnd = true;
        }
    }
}
