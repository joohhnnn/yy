package com.txznet.music.helper;

import android.media.AudioManager;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.IMediaPlayer;
import com.txznet.audio.player.core.ijk.IjkMediaPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.audio.player.playback.MediaPlaybackController;
import com.txznet.audio.player.queue.PlayQueue;
import com.txznet.audio.player.util.PlayStateUtil;
import com.txznet.comm.err.Error;
import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.data.entity.Nothing;
import com.txznet.music.data.http.NetRequestManager;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.model.logic.PlayLogicFactory;
import com.txznet.music.power.PowerManager;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.FileConfigUtil;
import com.txznet.music.util.Logger;
import com.txznet.reserve.service.ReserveServiceSameProcess0;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.music.util.FileConfigUtil.KEY_MUSIC_RESUME_PLAY_AFTER_WAKEUP;

/**
 * 播放器配置
 *
 * @author zackzhou
 * @date 2019/1/3,15:53
 */

public class PlayerConfigHelper {
    public static final String TAG = Constant.LOG_TAG_LOGIC + ":Config";

    private static PlayerConfigHelper sInstance = new PlayerConfigHelper();

    private PlayerConfigHelper() {
    }

    public static PlayerConfigHelper get() {
        return sInstance;
    }

    /**
     * 初始化播放器
     */
    public void initPlayer() {
        Logger.d(TAG, "initPlayer");
        AudioPlayer.setServiceClass(ReserveServiceSameProcess0.class);
        AudioPlayer.getDefault().setConfig(new AudioPlayer.Config.Builder().autoPlay(false).setPlayerImplClass(IjkMediaPlayer.class).build());
        initAudioFocusHandler();
        initPlayerListener();
        initPlayUrlProvider();
        initPlaybackConfig();
        initPlayQueueListener();
        initPlayQueueInterceptor();

    }

    // 恢复媒体按键
    public void resumeMediaPlayback() {
        MediaPlaybackController.get().enable();
    }

    // 媒体焦点管理
    private void initAudioFocusHandler() {
        AudioPlayer.getDefault().setAudioFocusHandler((player, focusChange) -> {
            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_AUDIO_FOCUS_CHANGE)
                    .bundle("focusChange", focusChange).build());
            int lostFlag;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN: // 重新获得焦点
                    PlayerActionCreator.get().setVol(Operation.FOCUS, 1, 1);
                    Logger.d(TAG, String.format("check play, isPlaying=%s, mPausedByTransientLossOfFocus=%s", PlayHelper.get().isPlaying(), PlayHelper.get().mPausedByTransientLossOfFocus));
                    if (!PlayHelper.get().isPlaying() && PlayHelper.get().mPausedByTransientLossOfFocus) {
                        PlayerActionCreator.get().start(Operation.FOCUS);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS: // 焦点丢失
                    lostFlag = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_LOSS, 1);
                    if (PlayHelper.get().isPlayingUnStrict()) {
                        PlayHelper.get().mPausedByTransientLossOfFocus = true;
                        Logger.d(TAG, "set mPausedByTransientLossOfFocus=" + false + ", state=" + PlayStateUtil.convert2Str(PlayHelper.get().getCurrPlayState()));
                    }
                    switch (lostFlag) {
                        case 0: // 不处理
                            break;
                        case 1: // 默认,表示停止,释放音频焦点
                            if (SharedPreferencesUtils.isReleaseAudioFocus()) {
                                AudioPlayer.getDefault().getAudioFocusController().abandonAudioFocus();
                                PlayerActionCreator.get().pause(Operation.FOCUS);
                            }
                            break;
                        case 2: // 表示暂停,不释放音频焦点
                            PlayerActionCreator.get().pause(Operation.FOCUS);
                            break;
                        default:
                            break;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: //  短暂焦点丢失
                    PlayHelper.get().mPausedByTransientLossOfFocus = PlayHelper.get().isPlayingUnStrict();
                    Logger.d(TAG, "set mPausedByTransientLossOfFocus=" + PlayHelper.get().mPausedByTransientLossOfFocus + ", state=" + PlayStateUtil.convert2Str(PlayHelper.get().getCurrPlayState()));
                    if (PlayHelper.get().isPlayingUnStrict()) {
                        PlayerActionCreator.get().pause(Operation.FOCUS);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // 降半播放
                    PlayHelper.get().mPausedByTransientLossOfFocus = PlayHelper.get().isPlayingUnStrict();
                    Logger.d(TAG, "set mPausedByTransientLossOfFocus=" + PlayHelper.get().mPausedByTransientLossOfFocus + ", state=" + PlayStateUtil.convert2Str(PlayHelper.get().getCurrPlayState()));
                    Audio currAudio = AudioPlayer.getDefault().getCurrentAudio();
                    if (currAudio != null) {
                        float lostFactor = (float) TXZFileConfigUtil.getDoubleSingleConfig(TXZFileConfigUtil.KEY_MUSIC_LOSS_TRANSIENT_FACTOR, 0.5f);
                        lostFactor = Math.min(Math.max(0f, lostFactor), 1.0f);//[0.0-- 1.0]
                        // 0(表示不处理),1(表示降低音量, 音乐默认),2(表示暂停，电台默认)
                        if (AudioUtils.isSong(currAudio.sid)) {
                            lostFlag = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_LOSS_TRANSIENT_M, 1);
                        } else {
                            lostFlag = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_LOSS_TRANSIENT_R, 2);
                        }
                        switch (lostFlag) {
                            case 0: // 不处理
                                break;
                            case 1: // 降低音量
                                PlayerActionCreator.get().setVol(Operation.FOCUS, lostFactor, lostFactor);
                                break;
                            case 2: // 暂停
                                PlayerActionCreator.get().pause(Operation.FOCUS);
                                break;
                            default:
                                break;
                        }
                    }
                    break;
                default:
                    break;
            }
        });
    }

    // 状态监听
    private void initPlayerListener() {
        AudioPlayer.getDefault().setAudioPlayerStateChangeListener(new AudioPlayer.AudioPlayerStateChangeListener() {
            private AudioV5 mLastAudioNotNull;

            @Override
            public void onAudioChanged(Audio audio, boolean willEnd) {
                AudioV5 audioV5 = AudioConverts.convert2Audio(audio);
                PlayHelper.get().mCurrAudio = audioV5;
                if (audioV5 != null) {
                    mLastAudioNotNull = audioV5;
                }
                Disposable disposable = Observable.create(emitter -> {
                    if (PlayHelper.get().mCurrAudio != null) {
                        PlayHelper.get().mCurrAudio.isFavour = FavourHelper.isFavour(PlayHelper.get().mCurrAudio.id, PlayHelper.get().mCurrAudio.sid);
                        if (AlbumUtils.isAiRadio(PlayHelper.get().mCurrAlbum)) {
                            Album audioAlbum = new Album();
                            audioAlbum.id = PlayHelper.get().mCurrAudio.albumId;
                            audioAlbum.sid = PlayHelper.get().mCurrAudio.albumSid;
                            PlayHelper.get().mCurrAlbum.isSubscribe = SubscribeHelper.isSubscribe(audioAlbum) != null;
                            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_ALBUM_CHANGE)
                                    .bundle(Constant.PlayConstant.KEY_ALBUM, PlayHelper.get().mCurrAlbum)
                                    .bundle(Constant.PlayConstant.KEY_FROM_AI, true)
                                    .build());
                        }
                    }
                    emitter.onNext(Nothing.NONE);
                    emitter.onComplete();
                }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {
                    PlayHelper.get().mDuration = 0;
                    PlayHelper.get().mPosition = 0;
                    Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_INFO_CHANGE)
                            .bundle(Constant.PlayConstant.KEY_AUDIO, PlayHelper.get().mCurrAudio)
                            .bundle(Constant.PlayConstant.KEY_PLAY_WILL_BE_END, willEnd).build());
                    Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_PROGRESS_CHANGE)
                            .bundle(Constant.PlayConstant.KEY_AUDIO, PlayHelper.get().mCurrAudio)
                            .bundle(Constant.PlayConstant.KEY_POSITION, 0L)
                            .bundle(Constant.PlayConstant.KEY_DURATION, 0L).build());
                    Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PROXY_BUFFERING_UPDATE).bundle(Constant.PlayConstant.KEY_PLAY_BUFFER, new ArrayList<>()).build());
                });
                DisposableManager.get().add("onAudioChanged", disposable);
                // 保存数据库
                PlayListDataHelper.updatePlayListAudioAsync(audio);
            }

            @Override
            public void onQueuePlayEnd() {
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_QUEUE_ON_PLAY_END).build());
            }

            @Override
            public void onPlayStateChanged(int state) {
                PlayHelper.get().mCurrPlayState = state;

                if (!PowerManager.getInstance().isSleeping() && FileConfigUtil.getBooleanConfig(KEY_MUSIC_RESUME_PLAY_AFTER_WAKEUP, false)) {
                    if (PlayHelper.get().isPlayingUnStrict()) {
                        if (!SharedPreferencesUtils.getIsPlay()) {
                            Logger.d(Constant.LOG_TAG_POWER, "isPlay:true");
                            SharedPreferencesUtils.setIsPlay(true);
                        }
                    } else {
                        if (SharedPreferencesUtils.getIsPlay()) {
                            Logger.d(Constant.LOG_TAG_POWER, "isPlay:false");
                            SharedPreferencesUtils.setIsPlay(false);
                        }
                    }
                }

                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_STATE_CHANGE)
                        .bundle(Constant.PlayConstant.KEY_AUDIO, mLastAudioNotNull)
                        .bundle(Constant.PlayConstant.KEY_PLAY_STATE, state).build());
                //同步给Core,当前播放的状态
                SyncCoreData.syncCurPlayerStatus(IMediaPlayer.STATE_ON_PLAYING == state);
                SyncCoreData.sendStatusByCurrent(state);
            }

            @Override
            public void onProgressChanged(long position, long duration) {
                PlayHelper.get().mPosition = position;
                PlayHelper.get().mDuration = duration;
                if (duration != 0 && mLastAudioNotNull != null) {
                    mLastAudioNotNull.duration = duration;
                }
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_PROGRESS_CHANGE)
                        .bundle(Constant.PlayConstant.KEY_AUDIO, mLastAudioNotNull)
                        .bundle(Constant.PlayConstant.KEY_POSITION, position)
                        .bundle(Constant.PlayConstant.KEY_DURATION, duration).build());
            }

            @Override
            public void onSeekComplete() {
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_SEEK_COMPLETE)
                        .bundle(Constant.PlayConstant.KEY_AUDIO, mLastAudioNotNull)
                        .build());
            }

            @Override
            public void onCompletion() {
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_COMPLETION)
                        .bundle(Constant.PlayConstant.KEY_AUDIO, mLastAudioNotNull)
                        .build());
            }

            @Override
            public void onError(Error error) {
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_ERROR)
                        .bundle(Constant.PlayConstant.KEY_AUDIO, mLastAudioNotNull)
                        .bundle(Constant.PlayConstant.KEY_ERROR, error).build());
            }
        });
    }

    // 初始化代理
    private void initPlayUrlProvider() {
        AudioPlayer.getDefault().setPlayUrlProvider((audio, urlCallback) -> {
            if (TextUtils.isEmpty(audio.sourceUrl)) {
                // 根据audio查询播放链接，目前接的数据源不会走这个分支
            } else {
                if (audio.sourceUrl.startsWith("txz")) { // 解析
                    // 本地存在缓存的话，按本地路径传递给代理服务, #离线的情况下，无法获取真实播放链接#
                    File tmdFile = AudioUtils.getAudioTMDFile(audio);
                    if (tmdFile != null && tmdFile.exists()) {
                        urlCallback.onPlayUrlResp(ProxyHelper.getProxyUrl(audio, tmdFile.getAbsolutePath()));
                        return;
                    }
                    NetRequestManager.removeSameRequest("getTXZUrl");
                    TXZUri uri = TXZUri.parse(audio.sourceUrl);
                    if (TXZAudio.DOWNLOADTYPE_PROXY.equals(uri.downloadType)) { // qq 需要预处理
                        Disposable disposable = TXZMusicDataSource.get().getAudioPlayUrls(AudioConverts.convert2Audio(audio))
                                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(playUrlInfo -> {
                                    List<String> urls = new ArrayList<>();
                                    urls.add(playUrlInfo.strUrl);
                                    if (playUrlInfo.arrBackUpUrl != null) {
                                        urls.addAll(playUrlInfo.arrBackUpUrl);
                                    }
                                    urlCallback.onPlayUrlResp(ProxyHelper.getProxyUrl(audio, urls.toArray(new String[0])));
                                }, throwable -> {
                                    urlCallback.onError();
                                });
                        NetRequestManager.addMonitor("getTXZUrl", disposable);
                    } else { // 可直接用downloadUrl下载
                        urlCallback.onPlayUrlResp(ProxyHelper.getProxyUrl(audio, uri.downloadUrl));
                    }
                } else {
                    urlCallback.onPlayUrlResp(ProxyHelper.getProxyUrl(audio, audio.sourceUrl));
                }
            }
        });
    }


    // 初始化媒体按键
    private void initPlaybackConfig() {
        int nextCode = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_KEYCODE_NEXT, -1);
        if (nextCode != -1) {
            MediaPlaybackController.get().setExtraKeyCode(KeyEvent.KEYCODE_MEDIA_NEXT, nextCode);
        }

        int pervCode = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_KEYCODE_PREV, -1);
        if (pervCode != -1) {
            MediaPlaybackController.get().setExtraKeyCode(KeyEvent.KEYCODE_MEDIA_PREVIOUS, nextCode);
        }
    }

    // 初始化队列监听
    private void initPlayQueueListener() {
        AudioPlayer.getDefault().getQueue().setOnPlayQueueChangeListener(audioList -> {
            AppLogic.runOnBackGround(() -> {
                Logger.w(Constant.LOG_TAG_QUEUE, "=== start println ===");
                List<Audio> myList = new ArrayList<>(audioList);
                for (Audio audio : myList) {
                    if (BuildConfig.DEBUG || audio == null) {
                        Logger.w(Constant.LOG_TAG_QUEUE, audio);
                    } else {
                        Logger.w(Constant.LOG_TAG_QUEUE, audio.sid + ":" + audio.id + ":" + audio.name);
                    }
                }
                Logger.w(Constant.LOG_TAG_QUEUE, "=== end println ===");
            });

            Disposable disposable = Observable.create(emitter -> {
                List<AudioV5> audioV5List = AudioConverts.convert2List(audioList, AudioConverts::convert2Audio);
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
                emitter.onNext(audioV5List);
                emitter.onComplete();
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(result -> {
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_QUEUE_ON_CHANGED)
                        .bundle(Constant.PlayQueueConstant.KEY_AUDIO_LIST, result)
                        .build());
                // 保存数据库
                Album album = PlayHelper.get().mCurrAlbum;
                PlayListDataHelper.updatePlayListAsync(PlayHelper.get().mCurrScene, album, new ArrayList<>(audioList));
            });
            DisposableManager.get().add("onQueueChanged", disposable);
        });
    }

    // 初始化队列拦截器
    private void initPlayQueueInterceptor() {
        AudioPlayer.getDefault().setAudioPlayerQueueInterceptor(new AudioPlayer.AudioPlayerQueueInterceptor() {
            @Override
            public void pickPrevItem(PlayQueue queue, Audio oriAudio, Callback callback) {
                PlayLogicFactory.get().getQueueItemPicker(PlayHelper.get().mCurrAlbum).pickPrevItem(queue, oriAudio, callback);
            }

            @Override
            public void pickNextItem(PlayQueue queue, Audio oriAudio, boolean fromUser, Callback callback) {
                PlayLogicFactory.get().getQueueItemPicker(PlayHelper.get().mCurrAlbum).pickNextItem(queue, oriAudio, fromUser, callback);
            }
        });
    }
}
