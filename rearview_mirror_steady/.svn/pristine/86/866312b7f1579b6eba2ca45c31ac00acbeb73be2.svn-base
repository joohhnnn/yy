package com.txznet.music.model;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.IMediaPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.BreakpointDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.BreakpointHelper;
import com.txznet.music.helper.FakeReqHelper;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.PreLoadHelper;
import com.txznet.music.helper.ProxyHelper;
import com.txznet.music.helper.SyncCoreData;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.PlayInfoEvent;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.music.util.PlaySceneUtils;
import com.txznet.music.util.TtsHelper;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import java.util.List;

/**
 * 播放器业务
 *
 * @author zackzhou
 * @date 2018/12/3,14:07
 */

public class PlayerModel extends RxWorkflow {
    private static final String TAG = Constant.LOG_TAG_LOGIC + ":Model";

    public PlayerModel() {
    }

    @Override
    public void onAction(RxAction action) {
        AudioV5 audioV5;
        int index;
        switch (action.type) {
            case ActionType.ACTION_PLAYER_SET_QUEUE: // 设置队列
                PlayScene scene = (PlayScene) action.data.get(Constant.PlayConstant.KEY_SCENE);
                List<? extends AudioV5> audioList = (List<? extends AudioV5>) action.data.get(Constant.PlayConstant.KEY_QUEUE);
                PlayHelper.get().setQueue(scene, AudioConverts.convert2List(audioList, AudioConverts::convert2MediaAudio));
                break;
            case ActionType.ACTION_PLAYER_PLAY_ITEM: // 播放指定音频
                Operation operation = action.operation;
                reportCurrPlayEnd(operation);
                audioV5 = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                Object o = action.data.get(Constant.PlayConstant.KEY_MODE_IMMEDIATELY);
                boolean isImmediately = false;
                if (o != null) {
                    isImmediately = (boolean) o;
                }
                Audio audio = AudioConverts.convert2MediaAudio(audioV5);
                if (Operation.MANUAL == action.operation) {
                    PlayHelper.get().markLastClickAudio(audio);
                }
                if (isImmediately) {
                    PlayHelper.get().playImmediately(audio);
                } else {
                    PlayHelper.get().play(audio);
                }
                break;
            case ActionType.ACTION_PLAYER_PLAY_MUSIC: // 播放音乐
                doPlayMusic(action);
                break;
            case ActionType.ACTION_PLAYER_PLAY_RADIO: // 播放电台
                doPlayRadio(action);
                break;
            case ActionType.ACTION_PLAYER_START: // 开始播放
                if (PlayHelper.get().getCurrAlbum() != null) {
                    if (PlayHelper.get().getCurrAlbum().isPlayEnd || PlayHelper.get().getCurrAudio() == null) {
                        // 当前专辑播放完毕，重头开始播放
                        Logger.d(TAG, "album is playEnd or unload, replay now");
                        PlayHelper.get().playAlbum(action.operation, PlayHelper.get().getCurrPlayScene(), PlayHelper.get().getCurrAlbum());
                        PlayHelper.get().getCurrAlbum().isPlayEnd = false;
                        return;
                    }
                }
                AudioPlayer.getDefault().start();
                break;
            case ActionType.ACTION_PLAYER_PLAY_ALBUM: // 播放专辑
                doPlayAlbum(action);
                break;
            case ActionType.ACTION_PLAYER_PAUSE: // 暂停
                AudioPlayer.getDefault().pause();
                break;
            case ActionType.ACTION_PLAYER_STOP: // 暂停
                AudioPlayer.getDefault().stop();
                break;
            case ActionType.ACTION_PLAYER_PLAY_OR_PAUSE: // 播放或暂停
                TtsHelper.cancel();
                if (PlayHelper.get().getCurrAlbum() != null) {
                    if (PlayHelper.get().getCurrAlbum().isPlayEnd || PlayHelper.get().getCurrAudio() == null) {
                        // 当前专辑播放完毕，重头开始播放
                        Logger.d(TAG, "album is playEnd or unload, replay now");
                        PlayHelper.get().playAlbum(action.operation, PlayHelper.get().getCurrPlayScene(), PlayHelper.get().getCurrAlbum());
                        PlayHelper.get().getCurrAlbum().isPlayEnd = false;
                        return;
                    }
                }
                if (PlayHelper.get().getCurrAudio() == null) {
                    PlayHelper.get().play(Operation.MANUAL);
                } else {
                    if (IMediaPlayer.STATE_ON_ERROR == PlayHelper.get().getCurrPlayState()) {
                        AudioPlayer.getDefault().play(AudioPlayer.getDefault().getCurrentAudio());
                    } else {
                        AudioPlayer.getDefault().playOrPause();
                    }
                }
                break;
            case ActionType.ACTION_PLAYER_PLAY_PREV: // 上一首
                PlayHelper.get().setLastSwitchAudioOperation(action.operation);
                AudioPlayer.getDefault().prev();
                break;
            case ActionType.ACTION_PLAYER_PLAY_NEXT: // 下一首
                PlayHelper.get().setLastSwitchAudioOperation(action.operation);
                AudioPlayer.getDefault().next(true);
                break;
            case ActionType.ACTION_PLAYER_SEEK_TO: // 跳转
                if (PlayHelper.get().getCurrAlbum() != null) {
                    PlayHelper.get().getCurrAlbum().isPlayEnd = false;
                }
                audioV5 = PlayHelper.get().getCurrAudio();
                if (audioV5 != null) {
                    long duration = AudioPlayer.getDefault().getDuration();
                    if (duration == 0) {
                        return;
                    }
                    long position = 0;
                    Long positionL = (Long) action.data.get(Constant.PlayConstant.KEY_POSITION);
                    if (positionL != null) {
                        position = positionL;
                    } else {
                        Float percent = (Float) action.data.get(Constant.PlayConstant.KEY_PERCENT);
                        if (percent != null) {
                            position = (long) (percent * duration);
                        }
                    }
                    ReportEvent.reportPlayerSeekTo(audioV5, PlayHelper.get().getLastPosition(), position, PlayHelper.get().getLastDuration());
                    doSeekTo(position);
                    // 非音乐音频，进行seekTo操作，立即更新断点
                    if (!AudioUtils.isSong(audioV5.sid)) {
                        final long f_pos = position;
                        final String albumId;
                        if (PlayHelper.get().getCurrAlbum() != null) {
                            albumId = PlayHelper.get().getCurrAlbum().sid + "-" + PlayHelper.get().getCurrAlbum().id;
                        } else {
                            albumId = null;
                        }
                        AppLogic.runOnBackGround(() -> {
                            BreakpointDao breakpointDao = DBUtils.getDatabase(GlobalContext.get()).getBreakpointDao();
                            Breakpoint breakpoint = breakpointDao.findByAudio(audioV5.id, audioV5.sid);
                            audioV5.hasPlay = true;
                            if (breakpoint == null) {
                                breakpoint = new Breakpoint();
                                breakpoint.sid = audioV5.sid;
                                breakpoint.id = audioV5.id;
                                breakpoint.albumId = albumId;
                                breakpoint.duration = audioV5.duration;
                            }
                            breakpoint.position = f_pos;
                            audioV5.progress = (int) (breakpoint.position * 1f / breakpoint.duration * 100 + 0.5f);
                            breakpointDao.saveOrUpdate(breakpoint);
                            Logger.d("Music:DB:", "save breakpoint " + audioV5.name + " " + breakpoint.position + "/" + breakpoint.duration + ", end=" + breakpoint.playEndCount);
                            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_BREAK_POINT_UPDATE)
                                    .bundle(Constant.BreakpointConstant.KEY_AUDIO, audioV5)
                                    .bundle(Constant.BreakpointConstant.KEY_POSITION, breakpoint.position)
                                    .bundle(Constant.BreakpointConstant.KEY_DURATION, breakpoint.duration)
                                    .bundle(Constant.BreakpointConstant.KEY_PLAY_END, false)
                                    .build());
                        });
                    }
                }
                break;
            case ActionType.ACTION_PLAYER_ON_INFO_CHANGE: // 播放内容改变
                audioV5 = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                //同步给Core,当前播放的信息
                SyncCoreData.syncCurMusicModel(audioV5);
                if (audioV5 != null
                        && audioV5.id != mLastBreakpointTtsAudioId
                        && audioV5.sid != mLastBreakpointTtsAudioSid) {
                    mLastBreakpointTtsAudioSid = -1;
                    mLastBreakpointTtsAudioId = -1;
                }
                break;
            case ActionType.ACTION_PLAYER_ON_STATE_CHANGE: // 播放状态改变
                int playState = (int) action.data.get(Constant.PlayConstant.KEY_PLAY_STATE);
                if (IMediaPlayer.STATE_ON_STOPPED == playState) { // 结束
                    audioV5 = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                    if (audioV5 != null) {
                        BreakpointHelper.saveBreakpoint(PlayHelper.get().getCurrAlbum(), audioV5, 0, audioV5.duration, true);
                    }
                } else if (IMediaPlayer.STATE_ON_PREPARED == playState) { // 装载完毕
                    AppLogic.runOnBackGround(() -> {
                        AudioV5 currAudio = PlayHelper.get().getCurrAudio();
                        PlayScene currScene = PlayHelper.get().getCurrPlayScene();
                        if (currAudio == null) {
                            return;
                        }
                        FakeReqHelper.get().doFakeReq(currAudio.sid, currAudio.id); // 假请求
                        if (!PlaySceneUtils.isMusicScene()) {
                            // 新闻类型专辑不断点
                            if (PlayHelper.get().getCurrAlbum() != null && AlbumUtils.isNews(PlayHelper.get().getCurrAlbum())) {
                                invokeStartFocusSafety();
                                return;
                            }

                            /*
                             * 查询断点信息，从上次断点处前5s开始播放
                             */
                            Breakpoint breakpoint = DBUtils.getDatabase(GlobalContext.get()).getBreakpointDao().findByAudio(currAudio.id, currAudio.sid);
                            long position = 0;
                            if (breakpoint != null) {
                                position = breakpoint.position - 5000;
                            }
                            boolean isSame = false;
                            if (currAudio.sid == mLastBreakpointTtsAudioSid
                                    && currAudio.id == mLastBreakpointTtsAudioId
                                    && currScene == mLastPlayScene) {
                                isSame = true;
                            }
                            mLastBreakpointTtsAudioSid = currAudio.sid;
                            mLastBreakpointTtsAudioId = currAudio.id;
                            mLastPlayScene = currScene;
                            if (position > 1000 * 10) {
                                final int lastSid = currAudio.sid;
                                final long lastId = currAudio.id;

                                if (!isSame) {
                                    TtsHelper.isNotifySeek = true;
                                    mBreakpointTts = TtsHelper.speakResource("RS_VOICE_MUSIC_BREAKPOINT_TIPS", Constant.RS_VOICE_MUSIC_BREAKPOINT_TIPS, new TtsUtil.ITtsCallback() {
                                        @Override
                                        public void onEnd() {
                                            TtsHelper.isNotifySeek = false;
                                            if (PlayHelper.get().getCurrAudio() != null
                                                    && PlayHelper.get().getCurrAudio().sid == lastSid
                                                    && PlayHelper.get().getCurrAudio().id == lastId) {
                                                doSeekTo(breakpoint.position - 5000);
                                            }
                                        }
                                    });
                                } else {
                                    doSeekTo(breakpoint.position - 5000);
                                }
                                return;
                            } else if (breakpoint != null && breakpoint.playEndCount > 0) {
                                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_BREAK_POINT_UPDATE)
                                        .bundle(Constant.BreakpointConstant.KEY_AUDIO, PlayHelper.get().getCurrAudio())
                                        .bundle(Constant.BreakpointConstant.KEY_POSITION, position)
                                        .bundle(Constant.BreakpointConstant.KEY_DURATION, breakpoint.duration)
                                        .bundle(Constant.BreakpointConstant.KEY_PLAY_END, false)
                                        .build());
                            }
                        }
                        invokeStartFocusSafety();
                    });
                }
                break;
            case ActionType.ACTION_PLAYER_ON_COMPLETION: // 播放完毕
                audioV5 = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                // 播放完毕保存断点信息
                BreakpointHelper.saveBreakpoint(PlayHelper.get().getCurrAlbum(), audioV5, 0, audioV5.duration, true);
                break;
            case ActionType.ACTION_PLAYER_ON_PROGRESS_CHANGE: //  播放进度改变
                audioV5 = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                long position = (long) action.data.get(Constant.PlayConstant.KEY_POSITION);
                long duration = (long) action.data.get(Constant.PlayConstant.KEY_DURATION);
                long playSec = position / 1000; // 播放秒数

                // 播放进度记录
                if (audioV5 != null && duration != 0) {
                    audioV5.progress = (int) (position * 1f / duration * 100 + 0.5f);
                }

                if (position <= duration && playSec >= 5 && playSec % 5 == 0 && PlayHelper.get().isPlaying()) {
                    BreakpointHelper.saveBreakpoint(PlayHelper.get().getCurrAlbum(), audioV5, position, duration, false);
                }
                if (PlaySceneUtils.isMusicScene()) {
                    // 音乐内容，已播放时长>10s时，
                    if (position > 10 * 1000) {
                        PreLoadHelper.forceNeedMoreData();
                    }
                } else {
                    if (position != 0 && duration != 0 && position * 1f / duration > 0.2f) {
                        PreLoadHelper.forceNeedMoreData();
                    }
                }
                // 还有1min音频即将播放完毕
                if (duration != 0 && position != 0 && duration - position < 1000 * 60) {
                    PreLoadHelper.preloadNext();
                }
                break;
            case ActionType.ACTION_PLAYER_ON_SEEK_COMPLETE: // 跳转完成
                invokeStartFocusSafety();
                break;
            case ActionType.ACTION_PLAYER_PLAY_LOCAL: // 播放本地
                reportCurrPlayEnd(action.operation);
                List<LocalAudio> localAudioList = (List<LocalAudio>) action.data.get(Constant.PlayConstant.KEY_AUDIO_LIST);
                if (localAudioList != null) {
                    index = (int) action.data.get(Constant.PlayConstant.KEY_POSITION);
                    PlayHelper.get().play(PlayScene.LOCAL_MUSIC, localAudioList, index);
                } else {
                    AudioV5 localAudio = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                    if (localAudio == null) {
                        Boolean needResume = (Boolean) action.data.get(Constant.PlayConstant.KEY_RESUME_LAST_PLAY);
                        PlayHelper.get().playLocal(action.operation, needResume != null && needResume, true);
                    } else {
                        PlayHelper.get().playLocal(localAudio);
                    }
                }
                break;
            case ActionType.ACTION_PLAYER_PLAY_SUBSCRIBE: // 播放订阅
                PlayHelper.get().playSubscribe();
                break;
            case ActionType.ACTION_PLAYER_PLAY_FAVOUR: // 播放收藏
                reportCurrPlayEnd(action.operation);
                List<FavourAudio> favourAudioList = (List<FavourAudio>) action.data.get(Constant.PlayConstant.KEY_AUDIO_LIST);
                if (favourAudioList != null) {
                    index = (int) action.data.get(Constant.PlayConstant.KEY_POSITION);
                    PlayHelper.get().play(PlayScene.FAVOUR_MUSIC, favourAudioList, index);
                } else {
                    AudioV5 favourAudio = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                    if (favourAudio == null) {
                        PlayHelper.get().playFavour();
                    } else {
                        PlayHelper.get().playFavour(favourAudio);
                    }
                }
                break;
            case ActionType.ACTION_PLAYER_PLAY_HISTORY_MUSIC: // 播放历史音乐
                reportCurrPlayEnd(action.operation);
                List<HistoryAudio> historyAudioList = (List<HistoryAudio>) action.data.get(Constant.PlayConstant.KEY_AUDIO_LIST);
                if (historyAudioList != null) {
                    index = (int) action.data.get(Constant.PlayConstant.KEY_POSITION);
                    PlayHelper.get().play(PlayScene.HISTORY_MUSIC, historyAudioList, index);
                } else {
                    AudioV5 historyAudio = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                    if (historyAudio == null) {
                        PlayHelper.get().playHistoryMusic();
                    } else {
                        PlayHelper.get().playHistoryMusic(historyAudio);
                    }
                }
                break;
            case ActionType.ACTION_PLAYER_PLAY_HISTORY_ALBUM: // 播放历史节目
                PlayHelper.get().playHistoryAlbum();
                break;
            case ActionType.ACTION_PLAYER_PLAY_WX_PUSH: // 播放微信推送
                reportCurrPlayEnd(action.operation);
                List<PushItem> pushList = (List<PushItem>) action.data.get(Constant.PlayConstant.KEY_AUDIO_LIST);
                if (pushList != null) {
                    index = (int) action.data.get(Constant.PlayConstant.KEY_POSITION);
                    PlayHelper.get().play(PlayScene.WECHAT_PUSH, pushList, index);
                } else {
                    AudioV5 pushAudio = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                    if (pushAudio == null) {
                        PlayHelper.get().playWxPush();
                    } else {
                        PlayHelper.get().playWxPush(pushAudio);
                    }
                }
                break;
            case ActionType.ACTION_PLAYER_PLAY_AI: // 播放AI电台
                PlayHelper.get().playAi(action.operation);
                break;
            case ActionType.ACTION_PLAYER_GET_PLAY_INFO: // 同步播放状态
                action.data.put(Constant.PlayConstant.KEY_AUDIO, PlayHelper.get().getCurrAudio());
                action.data.put(Constant.PlayConstant.KEY_ALBUM, PlayHelper.get().getCurrAlbum());
                action.data.put(Constant.PlayConstant.KEY_POSITION, PlayHelper.get().getLastPosition());
                action.data.put(Constant.PlayConstant.KEY_DURATION, PlayHelper.get().getLastDuration());
                action.data.put(Constant.PlayConstant.KEY_PLAY_STATE, PlayHelper.get().getCurrPlayState());
                action.data.put(Constant.PlayConstant.KEY_PLAY_BUFFER, PlayHelper.get().getLastLocalBuffer());
                postRxData(action);
                break;
            case ActionType.ACTION_LOCAL_DELETE: // 删除本地，级联删除播放列表
                // 收藏音乐、历史、本地会受到影响
                List<LocalAudio> bRemove = (List<LocalAudio>) action.data.get(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO);
                PlayHelper.get().removeLocalAudioCascade(bRemove, false);
                break;
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_MUSIC: // 删除历史音乐
                // FIXME: 2019/3/20 历史音乐删除时，播放列表不需要同步更新
//                List<HistoryAudio> bRemoveHistory = (List<HistoryAudio>) action.data.get(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIOS);
//                PlayHelper.get().removeHistoryAudioCascade(bRemoveHistory);
                break;
            case ActionType.ACTION_MEDIA_SCANNER_STARTED: // TF卡拔出/插入
                // 刷新播放列表
                PlayHelper.get().refreshQueueIfNotExists();
                break;
            case ActionType.ACTION_PLAYER_PLAY: // 播放
                PlayHelper.get().play(action.operation);
                break;
            case ActionType.ACTION_PLAYER_ON_ALBUM_CHANGE:
                Boolean fromAi = (Boolean) action.data.get(Constant.PlayConstant.KEY_FROM_AI);
                if (fromAi == null || !fromAi) {
                    TtsUtil.cancelSpeak(mBreakpointTts);
                }
                break;
            case ActionType.ACTION_PLAYER_SET_VOL:
                float leftVol = (float) action.data.get(Constant.PlayConstant.KEY_LEFT_VOLUME);
                float rightVol = (float) action.data.get(Constant.PlayConstant.KEY_RIGHT_VOLUME);
                AudioPlayer.getDefault().setVolume(leftVol, rightVol);
                break;
            default:
                break;
        }
    }

    private void reportCurrPlayEnd(Operation operation) {
        AudioV5 currAudio = PlayHelper.get().getCurrAudio();
        if (currAudio != null) {
            ProxyHelper.releaseProxyRequest(currAudio.sid, currAudio.id);
            ReportEvent.reportAudioPlayEnd(currAudio,
                    PlayInfoEvent.MANUAL_TYPE_MANUAL,
                    AudioUtils.isLocalSong(currAudio.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE, PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(),
                    Operation.SOUND == operation ? PlayInfoEvent.EXIT_TYPE_SOUND : Operation.MANUAL == operation ? PlayInfoEvent.EXIT_TYPE_MANUAL : PlayInfoEvent.EXIT_TYPE_OTHER, false);
        }
    }

    private int mBreakpointTts;
    private int mLastBreakpointTtsAudioSid;
    private long mLastBreakpointTtsAudioId;
    private PlayScene mLastPlayScene;

    // 开始播放
    private void invokeStartFocusSafety() {
        if (AudioPlayer.getDefault().getAudioFocusController().isFocusInThere()) {
            Logger.w(TAG, "invokeStartFocusSafety begin start");
            AudioPlayer.getDefault().start();
        } else {
            Logger.w(TAG, "invokeStartFocusSafety failed");
            AudioPlayer.getDefault().pause();
        }
    }

    // 执行跳转
    private void doSeekTo(long position) {
        // 修复
        fixNoPlayAfterSeek();
        AudioPlayer.getDefault().seekTo(position);
    }

    /**
     * FIXME 已知问题，seekto后，无法继续播放，因为代理根据这个变量来进行阻塞线程，ffmpeg一致再等待服务端的返回，导致拖动有问题
     */
    private void fixNoPlayAfterSeek() {
        PreLoadHelper.forceNeedMoreData();
    }

    // 播放音乐
    private void doPlayMusic(RxAction oriAction) {
        PlayHelper.get().playMusic(oriAction.operation);
    }

    // 播放电台
    private void doPlayRadio(RxAction oriAction) {
        PlayHelper.get().playRadio(oriAction.operation);
    }

    // 执行播放专辑
    private void doPlayAlbum(RxAction oriAction) {
        PlayScene scene = (PlayScene) oriAction.data.get(Constant.PlayConstant.KEY_SCENE);
        Album album = (Album) oriAction.data.get(Constant.PlayConstant.KEY_ALBUM);
        PlayHelper.get().playAlbum(oriAction.operation, scene, album);
    }
}
