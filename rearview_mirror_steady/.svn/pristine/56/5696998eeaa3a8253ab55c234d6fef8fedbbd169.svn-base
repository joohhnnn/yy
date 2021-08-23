package com.txznet.music.model.logic.queue;

import com.txznet.audio.player.entity.Audio;
import com.txznet.audio.player.queue.PlayQueue;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.music.Constant;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.PlayQueueHelper;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 音乐专辑下的音频选择器
 *
 * @author zackzhou
 * @date 2018/12/18,15:20
 */

public class MusicQueueItemPicker extends AbstractQueueItemPicker {

    public MusicQueueItemPicker(Album album) {
        super(album);
    }

    @Override
    public void pickPrevItem(PlayQueue queue, Audio oriAudio, Callback callback) {
        super.pickPrevItem(queue, oriAudio, callback);
        if (queue.isEmpty()) {
            return;
        }
        // 列表循环模式，第一首触发上一首重新开始播放
        if (PlayQueue.SHUFFLE_MODE_ALL != queue.getShuffleMode()) {
            if (queue.getCurrentPosition() == 0) {
                callback.onPickResult(queue.getFirstItem());
                return;
            }
        }
        // 离线情况下
        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            // 如果是历史音乐或者收藏音乐场景或者在线专辑
            if (PlayScene.HISTORY_MUSIC == PlayHelper.get().getCurrPlayScene()
                    || PlayScene.FAVOUR_MUSIC == PlayHelper.get().getCurrPlayScene()
                    || (PlayScene.ALBUM == PlayHelper.get().getCurrPlayScene() && PlayHelper.get().getCurrAlbum() == null)) {
                List<Audio> subList = new ArrayList<>(queue.getSize());
                // 反向查找
                try {
                    List<Audio> audioList;
                    if (PlayQueue.SHUFFLE_MODE_ALL == queue.getShuffleMode()) {
                        audioList = queue.getRandomQueue();
                    } else {
                        audioList = queue.getQueue();
                    }
                    Audio audio = queue.getCurrentItem();
                    int index = audioList.indexOf(audio);
                    subList.addAll(audioList.subList(0, index));
                    subList.addAll(0, audioList.subList(index + 1, queue.getSize()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Collections.reverse(subList); // 反转列表
                if (!subList.isEmpty()) {
                    Audio willPlay = null;
                    for (Audio audio : subList) {
                        if (AudioUtils.isLocalSong(audio.sid)) {
                            willPlay = audio;
                            break;
                        } else {
                            File tmdFile = AudioUtils.getAudioTMDFile(audio);
                            if (tmdFile != null && tmdFile.exists()) {
                                willPlay = audio;
                                break;
                            }
                        }
                    }
                    if (willPlay != null) {
                        callback.onPickResult(willPlay);
                        if (willPlay != oriAudio) {
                            ToastUtils.showShortOnUI(Constant.TIP_MEDIA_WILL_SKIP);
                        }
                        return;
                    }
                }
                // 找不到的情况，重新播放
                if (queue.getCurrentItem() != null) {
                    callback.onPickResult(queue.getCurrentItem());
                    if (queue.getSize() > 1) {
                        ToastUtils.showShortOnUI(Constant.TIP_MEDIA_WILL_SKIP);
                    }
                    return;
                }
            }
        }
        callback.onPickResult(oriAudio);
    }

    @Override
    public void pickNextItem(PlayQueue queue, Audio oriAudio, boolean fromUser, Callback callback) {
        super.pickNextItem(queue, oriAudio, fromUser, callback);
        if (queue.isEmpty()) {
            return;
        }
        // 列表循环模式
        if (PlayQueue.SHUFFLE_MODE_ALL != queue.getShuffleMode()) {
            // 当前播放的是最后一首
            if (queue.getCurrentPosition() == queue.getSize() - 1) {
                if (PlayScene.ALBUM == PlayHelper.get().getCurrPlayScene()) {
                    List<Audio> queueList = queue.getQueue();
                    Audio lastAudio = null;
                    for (int index = queueList.size() - 1; index >= 0; index--) {
                        Audio item = queueList.get(index);
                        Boolean isFromSoundChoice = item.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE);
                        if (isFromSoundChoice == null || !isFromSoundChoice) {
                            lastAudio = item;
                            break;
                        }
                    }
                    if (lastAudio == null) {
                        lastAudio = queue.getLastItem();
                    }
                    Disposable disposable = TXZMusicDataSource.get().listAudios(album, AudioConverts.convert2Audio(lastAudio), "0-" + lastAudio.sid + "-" + lastAudio.id + "-" + lastAudio.albumId, Configuration.DefVal.PAGE_COUNT_MUSIC)
                            .subscribeOn(Schedulers.io()).observeOn(Schedulers.single())
                            .subscribe(audioV5List -> {
                                // 如果是每日推荐20首，全量覆盖
                                if (AlbumUtils.isRecommend(album) && audioV5List.size() > 0) {
                                    // 播放新拉取的第一个
                                    List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
                                    queue.setQueue(audioList);
                                    callback.onPickResult(audioList.get(0));
                                } else {
                                    // 去重
                                    PlayQueueHelper.get().distinctQueueV5(queue.getQueue(), audioV5List);
                                    // 没有下一页，播放第一个
                                    if (audioV5List.isEmpty()) {
                                        callback.onPickResult(queue.getItem(0));
                                    } else {
                                        // 播放新拉取的第一个
                                        List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
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
        }
        // 离线情况下
        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            // 如果是历史音乐或者收藏音乐场景或者在线专辑
            if (PlayScene.HISTORY_MUSIC == PlayHelper.get().getCurrPlayScene()
                    || PlayScene.LOCAL_MUSIC == PlayHelper.get().getCurrPlayScene()
                    || PlayScene.FAVOUR_MUSIC == PlayHelper.get().getCurrPlayScene()
                    || (PlayScene.ALBUM == PlayHelper.get().getCurrPlayScene() && PlayHelper.get().getCurrAlbum() == null)) { // 虚构的ALBUM
                List<Audio> subList = new ArrayList<>(queue.getSize());
                try {
                    List<Audio> audioList;
                    if (PlayQueue.SHUFFLE_MODE_ALL == queue.getShuffleMode()) {
                        audioList = queue.getRandomQueue();
                    } else {
                        audioList = queue.getQueue();
                    }
                    Audio audio = queue.getCurrentItem();
                    int index = audioList.indexOf(audio);
                    subList.addAll(audioList.subList(index + 1, queue.getSize()));
                    subList.addAll(audioList.subList(0, index));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!subList.isEmpty()) {
                    Audio willPlay = null;
                    for (Audio audio : subList) {
                        if (AudioUtils.isLocalSong(audio.sid)) {
                            willPlay = audio;
                            break;
                        } else {
                            File tmdFile = AudioUtils.getAudioTMDFile(audio);
                            if (tmdFile != null && tmdFile.exists()) {
                                willPlay = audio;
                                break;
                            }
                        }
                    }
                    if (willPlay != null) {
                        callback.onPickResult(willPlay);
                        if (willPlay != oriAudio) {
                            ToastUtils.showShortOnUI(Constant.TIP_MEDIA_WILL_SKIP);
                        }
                        return;
                    }
                }
                // 找不到的情况，重新播放
                if (queue.getCurrentItem() != null) {
                    callback.onPickResult(queue.getCurrentItem());
                    if (queue.getSize() > 1) {
                        ToastUtils.showShortOnUI(Constant.TIP_MEDIA_WILL_SKIP);
                    }
                    return;
                }
            }
        }
        callback.onPickResult(oriAudio);
    }
}
