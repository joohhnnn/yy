package com.txznet.music.model.logic.queue;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.audio.player.queue.PlayQueue;
import com.txznet.music.Constant;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.source.AiPushDataSource;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.PlayQueueHelper;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.TtsHelper;
import com.txznet.rxflux.Operation;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * AI电台专辑下的音频选择器
 *
 * @author zackzhou
 * @date 2018/12/18,15:24
 */

public class AiPushQueueItemPicker extends AbstractQueueItemPicker {
    private boolean need2Next;

    public AiPushQueueItemPicker(Album album) {
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
            if (AudioUtils.isSong(queue.getFirstItem().sid)) {
                callback.onPickResult(queue.getFirstItem());
            } else {
                notifyAlreadyFirst(PlayHelper.get().getLastSwitchAudioOperation());
            }
            return;
        }
        // 上一首不判断断点逻辑
        callback.onPickResult(oriAudio);
    }

    @Override
    public void pickNextItem(PlayQueue queue, Audio oriAudio, boolean fromUser, Callback callback) {
        super.pickNextItem(queue, oriAudio, fromUser, callback);
        if (queue.isEmpty()) {
            return;
        }
        if (oriAudio != null) {
            callback.onPickResult(oriAudio);
        } else {
            Audio audio = AiPushDataSource.get().poll();
            if (audio != null) {
                callback.onPickResult(audio);
            } else {
                need2Next = true;
            }
            // 缓冲数据小于两条时，尝试补充
            if (AiPushDataSource.get().size() < Configuration.DefVal.AI_RADIO_NEED_TO_REQ_COUNT) {
                Operation operation = PlayHelper.get().getLastSwitchAudioOperation();
                Audio reqAudio = AiPushDataSource.get().getLast();
                if (reqAudio == null) {
                    reqAudio = queue.getCurrentItem();
                    Boolean isFromSoundChoice = reqAudio.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE);
                    if (isFromSoundChoice != null && isFromSoundChoice) {
                        List<Audio> queueList = queue.getQueue();
                        for (int index = queueList.size() - 1; index >= 0; index--) {
                            Audio item = queueList.get(index);
                            isFromSoundChoice = item.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE);
                            if (isFromSoundChoice == null || !isFromSoundChoice) {
                                reqAudio = item;
                                break;
                            }
                        }
                    }
                }
                String audioId = "0-" + reqAudio.sid + "-" + reqAudio.id + "-" + reqAudio.albumId;
                Disposable disposable = TXZMusicDataSource.get().listAudios(album, AudioConverts.convert2Audio(audio), audioId, Configuration.DefVal.PAGE_COUNT_AI)
                        .subscribeOn(Schedulers.io()).observeOn(Schedulers.single())
                        .subscribe(audioV5List -> {
                            if (audioV5List == null || audioV5List.isEmpty()) {
                            } else {
                                List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
                                PlayQueueHelper.get().distinctQueue(AudioPlayer.getDefault().getQueue().getQueue(), audioList);
                                if (audioList.isEmpty()) {
                                    // 已经没有了，重新播放当前音频
                                    Audio currentItem = AudioPlayer.getDefault().getQueue().getCurrentItem();
                                    if (currentItem != null) {
//                                        callback.onPickResult(currentItem);
                                    }
                                } else {
                                    AiPushDataSource.get().push(audioList);
                                    if (need2Next && PlayScene.AI_RADIO == PlayHelper.get().getCurrPlayScene()) {
                                        Audio pollItem = AiPushDataSource.get().poll();
                                        AudioPlayer.getDefault().getQueue().addToQueue(pollItem);
                                        callback.onPickResult(pollItem);
                                    }
                                }
                            }
                        }, throwable -> {
                            throwable.printStackTrace();
                            notifyRequestError(operation);
                        });
                DisposableManager.get().add("pickerInvoke", disposable);
            }
        }
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
