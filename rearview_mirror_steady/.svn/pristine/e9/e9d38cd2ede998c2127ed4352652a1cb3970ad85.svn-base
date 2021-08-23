package com.txznet.music.model;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.BreakpointHelper;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.PlayQueueHelper;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.PlaySceneUtils;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 播放队列业务逻辑
 *
 * @author zackzhou
 * @date 2018/12/13,16:04
 */

public class PlayerQueueModel extends RxWorkflow {

    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_PLAYER_QUEUE_GET:
                Disposable disposable = Observable.create(emitter -> {
                    List<AudioV5> audioV5List = AudioConverts.convert2List(AudioPlayer.getDefault().getQueue().getQueue(), AudioConverts::convert2Audio);
                    if (!PlaySceneUtils.isMusicScene()) {
                        for (AudioV5 audioV5 : audioV5List) {
                            Breakpoint breakpoint = BreakpointHelper.findBreakpointByAudio(audioV5.sid, audioV5.id);
                            if (breakpoint != null) {
                                audioV5.hasPlay = true;
                                if (breakpoint.position == 0 && breakpoint.playEndCount > 0) {
                                    audioV5.progress = 100;
                                } else {
                                    if (breakpoint.duration == 0) {
                                        audioV5.progress = 0;
                                    } else {
                                        audioV5.progress = (int) (breakpoint.position * 1f / breakpoint.duration * 100 + 0.5f);
                                    }
                                }
                            }
                        }
                    }
                    emitter.onNext(audioV5List);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(audioV5List -> {
                    action.data.put(Constant.PlayQueueConstant.KEY_AUDIO_LIST, audioV5List);
                    postRxData(action);
                });
                addRxAction(action, disposable);
                break;
            case ActionType.ACTION_PLAYER_QUEUE_LOAD_MORE:
                boolean isUp = (boolean) action.data.get(Constant.PlayQueueConstant.KEY_IS_UP);
                loadMore(action, isUp);
                break;
            default:
        }
    }

    private void loadMore(RxAction action, boolean isUp) {
        Album album = PlayHelper.get().getCurrAlbum();
        if (album == null) {
            postRxError(action, null);
            return;
        }
        Audio item = null;
        if (isUp) {
            item = AudioPlayer.getDefault().getQueue().getFirstItem();
        } else {
            List<Audio> queueList = AudioPlayer.getDefault().getQueue().getQueue();
            for (int index = queueList.size() - 1; index >= 0; index--) {
                Audio audio = queueList.get(index);
                Boolean isFromSoundChoice = audio.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE);
                if (isFromSoundChoice == null || !isFromSoundChoice) {
                    item = audio;
                    break;
                }
            }
            if (item == null) {
                item = AudioPlayer.getDefault().getQueue().getLastItem();
            }
        }
        if (item == null) {
            postRxError(action, null);
            return;
        }
        String audioId;
        if (isUp) {
            audioId = String.format("%s-%s-%s-%s", 1, item.sid, item.id, item.albumId);
        } else {
            audioId = String.format("%s-%s-%s-%s", 0, item.sid, item.id, item.albumId);
        }
        int pagesize;
        if (AlbumUtils.isMusic(album)) {
            pagesize = Configuration.DefVal.PAGE_COUNT_MUSIC;
        } else {
            pagesize = Configuration.DefVal.PAGE_COUNT;
        }
        Disposable disposable = TXZMusicDataSource.get().listAudios(album, AudioConverts.convert2Audio(item), audioId, pagesize)
                .subscribeOn(Schedulers.io())
                .map(audioV5s -> AudioConverts.convert2List(audioV5s, AudioConverts::convert2MediaAudio))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(audioList -> {
                    if (!isUp && AlbumUtils.isRecommend(album)) {
                        AudioPlayer.getDefault().getQueue().setQueue(audioList);
                        action.data.put(Constant.PlayQueueConstant.KEY_AUDIO_LIST, audioList);
                    } else {
                        PlayQueueHelper.get().distinctQueue(AudioPlayer.getDefault().getQueue().getQueue(), audioList);
                        if (!audioList.isEmpty()) {
                            if (isUp) {
                                AudioPlayer.getDefault().getQueue().addToQueue(audioList, 0);
                            } else {
                                AudioPlayer.getDefault().getQueue().addToQueue(audioList);
                            }
                        }
                        action.data.put(Constant.PlayQueueConstant.KEY_AUDIO_LIST, audioList);
                    }
                    postRxData(action);
                }, throwable -> {
                    postRxError(action, throwable);
                });
        DisposableManager.get().add("loadMore", disposable);
    }
}
