package com.txznet.audio.player;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.ArraySet;
import android.text.TextUtils;

import com.txznet.audio.ErrCode;
import com.txznet.audio.player.core.ijk.IjkMediaPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.audio.player.focus.AudioFocusController;
import com.txznet.audio.player.focus.DefaultAudioFocusHandler;
import com.txznet.audio.player.focus.IAudioFocusHandler;
import com.txznet.audio.player.playback.MediaPlaybackController;
import com.txznet.audio.player.queue.IPlayQueue;
import com.txznet.audio.player.queue.PlayQueue;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static com.txznet.comm.util.ProcessUtil.getProcessIdByPkgName;

/**
 * 核心的播放器控制组件
 *
 * @提供播放器相关的基本操作
 * @基于远程代理
 * @一个本地实例对应一个远端IMediaPlayer实例(出错会重建，但id不变)，同时管理一个音频列表
 */
public class AudioPlayer {
    public static final String TAG = "AudioPlayer";
    private static Class<? extends MediaService> sServiceClass; // 服务实现类

    public static void setServiceClass(Class<? extends MediaService> clazz) {
        sServiceClass = clazz;
    }

    /**
     * 打断模式
     */
    public enum PreemptType {
        /*
         * 若队列不存在该音频，则把该音频插入到末端，并执行播放
         */
        PREEMPT_TYPE_NONE,
        /*
         * 把该音频插入当前播放前一位，并执行播放
         */
        PREEMPT_TYPE_IMMEDIATELY,
        /*
         * 把该音频插入当前播放后一位
         */
        PREEMPT_TYPE_NEXT,
        /*
         * 清空当前队列，把该音频插入到队列，并执行播放
         */
        PREEMPT_TYPE_FLUSH
    }

    /**
     * 播放状态变化监听
     */
    public interface AudioPlayerStateChangeListener extends OnPlayerStateChangeListener {
        /**
         * 音频变化
         *
         * @param audio   当前播放音频
         * @param willEnd 是否即将播放停止(顺序播放模式下，播放到末端)
         */
        void onAudioChanged(Audio audio, boolean willEnd);

        /**
         * 列表播放完毕
         */
        void onQueuePlayEnd();
    }

    /**
     * 播放队列拦截器
     */
    public interface AudioPlayerQueueInterceptor {

        interface Callback {
            /**
             * 返回的音频结果
             */
            void onPickResult(@Nullable Audio audio);
        }

        /**
         * 获取上一个音频
         *
         * @param queue    播放队列
         * @param oriAudio 默认候选
         * @return 实际需要播放器播放的上一个音频
         */
        void pickPrevItem(PlayQueue queue, Audio oriAudio, Callback callback);

        /**
         * 获取下一个音频
         *
         * @param queue    播放队列
         * @param oriAudio 默认候选
         * @param fromUser 来源于用户
         * @return 实际需要播放器播放的下一个音频
         */
        void pickNextItem(PlayQueue queue, Audio oriAudio, boolean fromUser, Callback callback);
    }

    /**
     * 配置项
     */
    public static class Config implements Serializable {
        public Class<? extends IMediaPlayer> playerImplClass = IjkMediaPlayer.class; // 播放器实现
        public boolean bRegMediaSession = true;  // 是否监听音频按键
        public boolean bAutoPlay = true; // prepared时自动播放
        public boolean bReqFocus = true; // 抢占焦点

        private Config() {
        }

        public static class Builder {
            private Config config;

            public Builder() {
                config = new Config();
            }

            /**
             * 设置播放器实现类
             */
            public Builder setPlayerImplClass(@NonNull Class<? extends IMediaPlayer> clazz) {
                config.playerImplClass = clazz;
                return this;
            }

            /**
             * 处理音频按键
             */
            public Builder handleMediaButtonEvent(boolean bHandle) {
                config.bRegMediaSession = bHandle;
                return this;
            }

            /**
             * prepared时自动播放
             */
            public Builder autoPlay(boolean bAuto) {
                config.bAutoPlay = bAuto;
                return this;
            }

            /**
             * 是否主动申请焦点
             */
            public Builder shouldReqAudioFocus(boolean bReq) {
                config.bReqFocus = bReq;
                return this;
            }

            public Config build() {
                return config;
            }
        }

        @Override
        public String toString() {
            return "Config{" +
                    "playerImplClass=" + playerImplClass +
                    ", bRegMediaSession=" + bRegMediaSession +
                    ", bAutoPlay=" + bAutoPlay +
                    ", bReqFocus=" + bReqFocus +
                    '}';
        }
    }

    /**
     * 私有配置项
     */
    private Config mPrivateConfig = new Config();

    /**
     * 设置配置项
     */
    public void setConfig(Config config) {
        mPrivateConfig = config;
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                if (sAidlPlayer != null) {
                    Map<String, Object> map = new ArrayMap<>(1);
                    map.put("config", mPrivateConfig);
                    try {
                        sAidlPlayer.syncConfig(getSessionId(), map);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private static Set<Integer> SIDS = new ArraySet<>(); // client_sid集合
    private static CountDownLatch sCountDownLatch; // 同步锁
    private static IAidlPlayer sAidlPlayer;

    private AudioPlayerStateChangeListener mAudioPlayerStateChangeListener;
    private AudioPlayerQueueInterceptor mAudioPlayerQueueInterceptor;
    private PlayQueue mPlayQueue = new PlayQueue();
    private HandlerThread mPlayerThread;
    private Handler mPlayerHandler;

    private AudioFocusController mAudioFocusController;
    private final IAudioFocusHandler mInnerAudioFocusHandler = new IAudioFocusHandler() {
        @Override
        public void onAudioFocusChange(AudioPlayer player, int focusChange) {
            LogUtil.d(TAG, "onAudioFocusChange focusChange=" + focusChange);
            if (mPrivateConfig.bRegMediaSession) {
                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    MediaPlaybackController.get().enable();
                } else {
                    MediaPlaybackController.get().disable();
                }
            }
            if (mOuterAudioFocusHandler == null) {
                mOuterAudioFocusHandler = new DefaultAudioFocusHandler();
            }
            mOuterAudioFocusHandler.onAudioFocusChange(player, focusChange);
        }
    };
    private IAudioFocusHandler mOuterAudioFocusHandler;
    private PlayUrlProvider mPlayUrlProvider;

    private @IMediaPlayer.PlayState
    int mCurrPlayState = IMediaPlayer.STATE_ON_IDLE; // 当前播放器状态
    private @AudioFocusController.DurationHint
    int mLastDurationHint = AudioManager.AUDIOFOCUS_GAIN; // 抢占模式


    private static ServiceConnection sConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.d(TAG, "onServiceConnected");
            sAidlPlayer = IAidlPlayer.Stub.asInterface(service);
            try {
                sAidlPlayer.asBinder().linkToDeath(sBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            sCountDownLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private static IBinder.DeathRecipient sBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            LogUtil.w(TAG, "binderDied");
            sAidlPlayer.asBinder().unlinkToDeath(sBinderPoolDeathRecipient, 0);
            sAidlPlayer = null;
            connect2Service();
        }
    };

    private final BroadcastReceiver mPlayerStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int sid = intent.getIntExtra("sid", -1);

            if (!SIDS.contains(sid)) {
                SIDS.remove(sid);
                mPlayerHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (sAidlPlayer != null) {
                                sAidlPlayer.release(sid);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            if (sid != getSessionId()) {
                return;
            }
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(MediaService.ACTION_PLAYER_ON_STATE_CHANGED)) {
                    int state = intent.getIntExtra(MediaService.EXTRA_KEY_STATE, 0);
                    mCurrPlayState = state;
                    if (mAudioPlayerStateChangeListener != null) {
                        mAudioPlayerStateChangeListener.onPlayStateChanged(state);
                    }
                    if (IMediaPlayer.STATE_ON_PREPARED == mCurrPlayState) { // 自动播放
                        if (mPrivateConfig.bAutoPlay && getAudioFocusController().isFocusInThere()) {
                            start();
                        }
                    }
                    return;
                }

                if (action.equals(MediaService.ACTION_PLAYER_ON_PROGRESS_CHANGED)) {
                    if (mAudioPlayerStateChangeListener != null) {
                        long pos = intent.getLongExtra(MediaService.EXTRA_KEY_POS, 0);
                        long duration = intent.getLongExtra(MediaService.EXTRA_KEY_DURATION, 0);
                        mAudioPlayerStateChangeListener.onProgressChanged(pos, duration);
                    }
                    return;
                }

                if (action.equals(MediaService.ACTION_PLAYER_ON_SEEK_COMPLETE)) {
                    if (mAudioPlayerStateChangeListener != null) {
                        mAudioPlayerStateChangeListener.onSeekComplete();
                    }
                    if (mPrivateConfig.bAutoPlay) { // 自动播放
                        start();
                    }
                    return;
                }

                if (action.equals(MediaService.ACTION_PLAYER_ON_COMPLETION)) {
                    if (mAudioPlayerStateChangeListener != null) {
                        mAudioPlayerStateChangeListener.onCompletion();
                    }
                    if (PlayQueue.REPEAT_MODE_ONE == mPlayQueue.getRepeatMode()) {// 当前模式为单曲循环
                        play(getCurrentAudio());
                    } else {
                        next(false); // 下一首
                    }
                    return;
                }

                if (action.equals(MediaService.ACTION_PLAYER_ON_ERROR)) {
                    if (mAudioPlayerStateChangeListener != null) {
                        int errorCode = intent.getIntExtra(MediaService.EXTRA_KEY_ERROR_CODE, 0);
                        String desc = intent.getStringExtra(MediaService.EXTRA_KEY_DESC);
                        String hint = intent.getStringExtra(MediaService.EXTRA_KEY_HINT);
                        mAudioPlayerStateChangeListener.onError(new Error(errorCode, desc, hint));
                    }
                }
            }
        }
    };

    private AudioPlayer() {
        mPlayerThread = new HandlerThread("AudioPlayer_" + getSessionId());
        mPlayerThread.start();
        mPlayerHandler = new Handler(mPlayerThread.getLooper());
        if (sAidlPlayer == null) {
            mPlayerHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (sAidlPlayer == null) {
                        connect2Service();
                    }
                }
            });
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(MediaService.ACTION_PLAYER_ON_STATE_CHANGED);
        filter.addAction(MediaService.ACTION_PLAYER_ON_PROGRESS_CHANGED);
        filter.addAction(MediaService.ACTION_PLAYER_ON_SEEK_COMPLETE);
        filter.addAction(MediaService.ACTION_PLAYER_ON_COMPLETION);
        filter.addAction(MediaService.ACTION_PLAYER_ON_ERROR);
        GlobalContext.get().registerReceiver(mPlayerStateReceiver, filter);
        mAudioFocusController = new AudioFocusController(this, mInnerAudioFocusHandler);
    }

    private static synchronized void connect2Service() {
        sCountDownLatch = new CountDownLatch(1);
        Intent intent = new Intent(GlobalContext.get(), sServiceClass == null ? MediaService.class : sServiceClass);
        GlobalContext.get().bindService(intent, sConn, Service.BIND_AUTO_CREATE | Service.BIND_IMPORTANT);
        try {
            sCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前播放状态
     */
    public int getCurrPlayState() {
        return mCurrPlayState;
    }

    public synchronized void destroy() {
        reInitCurr();
        GlobalContext.get().unregisterReceiver(mPlayerStateReceiver);
        SIDS.remove(getSessionId());
        mPlayerThread.quitSafely();
        if (mAudioFocusController != null && mPrivateConfig.bReqFocus) {
            mAudioFocusController.abandonAudioFocus();
            mAudioFocusController = null;
        }
        if (mAudioPlayerStateChangeListener != null) {
            mAudioPlayerStateChangeListener.onPlayStateChanged(IMediaPlayer.STATE_ON_END);
            mAudioPlayerStateChangeListener = null;
        }
        mPlayQueue.setOnPlayQueueChangeListener(null);
        try {
            GlobalContext.get().unbindService(sConn);
        } catch (Exception e) {
        }
        mPlayQueue = null;
        mPrivateConfig = null;
        if (this == sInstance) {
            sInstance = null;
        }
    }

    private static volatile AudioPlayer sInstance;

    public static AudioPlayer getDefault() {
        if (sInstance == null) {
            synchronized (AudioPlayer.class) {
                if (sInstance == null) {
                    sInstance = new AudioPlayer();
                }
            }
        }
        return sInstance;
    }

    public static AudioPlayer newInstance() {
        return new AudioPlayer();
    }

    // --------------------------
    // comm method
    public synchronized void setPlayUrlProvider(PlayUrlProvider urlProvider) {
        mPlayUrlProvider = urlProvider;
    }

    public synchronized AudioFocusController getAudioFocusController() {
        return mAudioFocusController;
    }

    public synchronized void setAudioFocusHandler(IAudioFocusHandler handler) {
        mOuterAudioFocusHandler = handler;
    }

    /**
     * 从索引项开始，播放指定音频资源列表
     *
     * @param audioList 音频资源列表
     * @param position  需要播放的索引
     */
    public void playAll(final List<Audio> audioList, final int position) {
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                mPlayQueue.setQueue(audioList);
                Audio audio = mPlayQueue.getItem(position);
                if (audio != null) {
                    play(audio);
                }
            }
        });
    }

    // 释放当前歌曲
    private void reInitCurr() {
//        if (Looper.myLooper() != mPlayerThread.getLooper()) {
//            mPlayerHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    reInitCurr();
//                }
//            });
//            return;
//        }
        try {
            sAidlPlayer.reset(getSessionId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void play(final Audio audio, @IAudioFocusHandler.AudioFocus final int durationHint, PreemptType preemptType) {
        play(audio, durationHint, preemptType, true);
    }

    private void notifyAudioChanged(Audio audio) {
        if (mAudioPlayerStateChangeListener != null) {
            // 列表播放
            if (PlayQueue.SHUFFLE_MODE_NONE == mPlayQueue.getShuffleMode()
                    && PlayQueue.REPEAT_MODE_NONE == mPlayQueue.getRepeatMode()) {
                mAudioPlayerStateChangeListener.onAudioChanged(audio, mPlayQueue.getNextPosition() < 0);
            } else {
                mAudioPlayerStateChangeListener.onAudioChanged(audio, false);
            }
        }
    }

    /**
     * 播放歌曲
     *
     * @param audio             音频
     * @param durationHint      焦点抢占模式，非{@link AudioManager#AUDIOFOCUS_GAIN }模式下，需要手动通过getAudioFocusController()进行释放
     * @param preemptType       打断模式
     * @param notifyInfoChanged 是否需要通知音频信息变更
     */
    public void play(final Audio audio, @IAudioFocusHandler.AudioFocus final int durationHint, final PreemptType preemptType, final boolean notifyInfoChanged) {
        LogUtil.w(TAG, "playAudio " + audio + "#" + preemptType);
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mPrivateConfig.bReqFocus) {
                    mAudioFocusController.requestAudioFocus(getStreamType(), durationHint);
                }
                if (mPrivateConfig.bRegMediaSession) {
                    MediaPlaybackController.get().enable();
                }
                if (audio == null) {
                    return;
                }
                if (notifyInfoChanged) {
                    AppLogicBase.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            notifyAudioChanged(audio);
                        }
                    }, 0);
                }
                int curIndex = mPlayQueue.getCurrentPosition();
                int index = mPlayQueue.indexOf(audio);
                // 对当前队列的影响
                switch (preemptType) {
                    case PREEMPT_TYPE_NONE:
                        reInitCurr();
                        if (mPlayQueue.indexOf(audio) == -1) {
                            mPlayQueue.addToQueue(audio);
                        }
                        break;
                    case PREEMPT_TYPE_NEXT:
                        if (curIndex < 0) { // 当前没有播放
                            if (index == -1) {
                                mPlayQueue.addToQueue(audio, 0);
                            } else {
                                mPlayQueue.moveQueueItem(index, 0);
                            }
                        } else { // 插入到当前播放的下一个
                            if (index == -1) {
                                mPlayQueue.addToQueue(audio, curIndex + 1);
                            } else {
                                mPlayQueue.moveQueueItem(index, curIndex + 1);
                            }
                        }
                        return;
                    case PREEMPT_TYPE_FLUSH:
                        reInitCurr();
                        mPlayQueue.clearQueue();
                        mPlayQueue.addToQueue(audio);
                        break;
                    case PREEMPT_TYPE_IMMEDIATELY:
                        reInitCurr();
                        if (curIndex < 0) { // 当前没有播放
                            if (index == -1) {
                                mPlayQueue.addToQueue(audio, 0);
                            } else {
                                mPlayQueue.moveQueueItem(index, 0);
                            }
                            mPlayQueue.addToQueue(audio, 0);
                        } else {
                            if (index == -1) {
                                mPlayQueue.addToQueue(audio, curIndex); // 插到当前播放的上一个
                            } else {
                                mPlayQueue.remove(audio);
                                mPlayQueue.addToQueue(audio, curIndex);
                            }
                        }
                        break;
                }
                // 实际播放
                mPlayQueue.setCurrentItem(audio);
                mPlayUrlProvider.getPlayUrl(audio, new PlayUrlProvider.OnPlayUrlCallback() {
                    @Override
                    public void onPlayUrlResp(final String playUrl) {
                        // 预检测
                        boolean bSafeLink = true;
                        if (TextUtils.isEmpty(playUrl)) {
                            bSafeLink = false;
                        } else if (!playUrl.startsWith("http") && !new File(playUrl).exists()) {
                            bSafeLink = false;
                        }
                        if (!bSafeLink) {
                            LogUtil.e(TAG, "check play url failed");
                            if (mAudioPlayerStateChangeListener != null) {
                                mAudioPlayerStateChangeListener.onError(new Error(ErrCode.ERROR_MEDIA_NOT_FOUND, "check play url unsafe, url=" + playUrl, "无法获取资源路径"));
                            }
                        }
                        mPlayUrl = playUrl;
                        mDurationHint = durationHint;
                        mPlayerHandler.removeCallbacks(mPlayUrlTask);
                        long now = SystemClock.elapsedRealtime();
                        if (now - mLastPlayTime > 1000) {
                            mPlayerHandler.post(mPlayUrlTask);
                        } else {
                            mPlayerHandler.postDelayed(mPlayUrlTask, 1000);
                        }
                        mLastPlayTime = now;
                    }

                    @Override
                    public void onError() {
                        LogUtil.e(TAG, "get playurl failed");
                        if (mAudioPlayerStateChangeListener != null) {
                            mAudioPlayerStateChangeListener.onError(new Error(ErrCode.ERROR_MEDIA_NOT_FOUND, "get play url failed", "无法获取资源路径"));
                        }
                    }
                });
            }
        });
    }


    private long mLastPlayTime;
    private String mPlayUrl;
    private int mDurationHint;

    private Runnable mPlayUrlTask = new Runnable() {
        @Override
        public void run() {
            playUrl(mPlayUrl, mDurationHint);
        }
    };

    private void playUrl(String playUrl, @IAudioFocusHandler.AudioFocus final int durationHint) {
        Map<String, Object> infos = new ArrayMap<>(5);
        infos.put("path", playUrl);
        infos.put("pid", getProcessIdByPkgName(GlobalContext.get().getPackageName()));
        infos.put("streamType", mStreamType);
        infos.put("leftVol", mLeftVol);
        infos.put("rightVol", mRightVol);
        int sid = getSessionId();
        try {
            SIDS.add(sid);
            sAidlPlayer.reset(sid);
            sAidlPlayer.createPlayer(sid, infos);
            sAidlPlayer.prepareAsync(sid);
        } catch (RemoteException e) {
            LogUtil.e(TAG, "create player failed");
            try {
                sAidlPlayer.release(sid);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
            if (mAudioPlayerStateChangeListener != null) {
                mAudioPlayerStateChangeListener.onError(new Error(ErrCode.ERROR_CREATE_PLAYER, "create player failed", "播放器创建失败"));
            }
            SIDS.remove(sid);
        }
    }

    /**
     * 播放歌曲
     *
     * @param audio 音频
     */
    public void play(final Audio audio) {
        play(audio, AudioManager.AUDIOFOCUS_GAIN, PreemptType.PREEMPT_TYPE_NONE);
    }

    /**
     * 重播当前播放内容
     */
    public void replay() {
        if (getCurrentAudio() != null) {
            play(getCurrentAudio(), AudioManager.AUDIOFOCUS_GAIN, PreemptType.PREEMPT_TYPE_NONE, false);
        }
    }

    /**
     * 重播指定音频
     */
    public void replay(Audio audio) {
        if (audio != null) {
            play(audio, AudioManager.AUDIOFOCUS_GAIN, PreemptType.PREEMPT_TYPE_NONE, false);
        }
    }

    /**
     * 设置播放队列
     */
    public void setQueue(List<Audio> audioList) {
        mPlayQueue.setQueue(audioList);
    }

    /**
     * 获取播放队列
     */
    public PlayQueue getQueue() {
        return mPlayQueue;
    }

    /**
     * 上一首
     * 当处于列表播放模式时，若无上一个则返回false
     */
    public void prev() {
        final Audio audio = mPlayQueue.getPreviousItem();
        if (mAudioPlayerQueueInterceptor != null) {
            mAudioPlayerQueueInterceptor.pickPrevItem(mPlayQueue, audio, new AudioPlayerQueueInterceptor.Callback() {
                @Override
                public void onPickResult(@Nullable final Audio audio) {
                    doPlayAudio(audio, false);
                }
            });
            return;
        }
        doPlayAudio(audio, false);
    }


    /**
     * 下一首
     * 当处于列表播放模式时，若无下一个则返回false
     */
    public void next(boolean fromUser) {
        final Audio audio = mPlayQueue.getNextItem();
        if (mAudioPlayerQueueInterceptor != null) {
            mAudioPlayerQueueInterceptor.pickNextItem(mPlayQueue, audio, fromUser, new AudioPlayerQueueInterceptor.Callback() {
                @Override
                public void onPickResult(@Nullable Audio audio) {
                    doPlayAudio(audio, true);
                }
            });
            return;
        }
        doPlayAudio(audio, true);
    }

    private void doPlayAudio(final Audio audio, boolean isNext) {
        if (audio == null && isNext) {
            AppLogicBase.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    if (mAudioPlayerStateChangeListener != null) {
                        mAudioPlayerStateChangeListener.onQueuePlayEnd();
                    }
                }
            });
        }
        if (audio != null) {
            mPlayerHandler.post(new Runnable() {
                @Override
                public void run() {
                    reInitCurr();
                    play(audio);
                }
            });
        }
    }

    /**
     * 设置洗牌模式
     */
    private void setShuffleMode(@PlayQueue.ShuffleMode int mode) {
        mPlayQueue.setShuffleMode(mode);
    }

    /**
     * 设置循环模式
     */
    private void setRepeatMode(@PlayQueue.RepeatMode int mode) {
        mPlayQueue.setRepeatMode(mode);
    }

    /**
     * 使用随机播放模式
     */
    public void useRandomPlay() {
        setShuffleMode(IPlayQueue.SHUFFLE_MODE_ALL);
        setRepeatMode(IPlayQueue.REPEAT_MODE_ALL);
    }

    /**
     * 列表循环
     */
    public void useQueueLoop() {
        setShuffleMode(IPlayQueue.SHUFFLE_MODE_NONE);
        setRepeatMode(IPlayQueue.REPEAT_MODE_ALL);
    }

    /**
     * 单曲循环
     */
    public void useSingleLoop() {
        setShuffleMode(IPlayQueue.SHUFFLE_MODE_NONE);
        setRepeatMode(IPlayQueue.REPEAT_MODE_ONE);
    }

    /**
     * 列表播放
     */
    public void useQueuePlay() {
        setShuffleMode(IPlayQueue.SHUFFLE_MODE_NONE);
        setRepeatMode(IPlayQueue.REPEAT_MODE_NONE);
    }

    /**
     * 跳转
     */
    public void seekTo(final long msec) {
        final long duration = getDuration();
        // 立即响应刷新
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (mAudioPlayerStateChangeListener != null) {
                    mAudioPlayerStateChangeListener.onProgressChanged(msec, duration);
                }
            }
        });
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    sAidlPlayer.seekTo(getSessionId(), msec);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取当前播放进度
     */
    public long getCurrentPosition() {
        try {
            return sAidlPlayer.getCurrentPosition(getSessionId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取播放时长
     */
    public long getDuration() {
        try {
            return sAidlPlayer.getDuration(getSessionId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取当前音频信息
     */
    public Audio getCurrentAudio() {
        return mPlayQueue.getCurrentItem();
    }

    /**
     * 自动播放列表中的元素
     */
    private void play() {
        Audio audio = mPlayQueue.pickItem();
        if (audio != null) {
            play(audio);
        }
    }

    /**
     * 播放/暂停
     */
    public void playOrPause() {
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (sAidlPlayer.isPlaying(getSessionId())) {
                        sAidlPlayer.pause(getSessionId());
                    } else {
//                        if (IMediaPlayer.STATE_ON_IDLE == mCurrPlayState) {
//                            play();
//                        } else {
                        // 二重保护，避免时序引起临时失去焦点-2时，进入该方法，重新抢占焦点1，使得焦点回归通知1收不到不开始播放
                        if (!mAudioFocusController.isFocusInThere()) {
                            return;
                        }
                        if (mPrivateConfig.bReqFocus) {
                            mAudioFocusController.requestAudioFocus(mStreamType, mLastDurationHint);
                        }
                        if (mPrivateConfig.bRegMediaSession) {
                            MediaPlaybackController.get().enable();
                        }
                        sAidlPlayer.start(getSessionId());
//                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 开始播放
     */
    public void start() {
        if (mPrivateConfig.bReqFocus) {
            mAudioFocusController.requestAudioFocus(mStreamType, mLastDurationHint);
        }
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                // 二重保护，避免时序引起临时失去焦点-2时，进入该方法，重新抢占焦点1，使得焦点回归通知1收不到不开始播放
                if (!mAudioFocusController.isFocusInThere()) {
                    return;
                }
                try {
                    if (mPrivateConfig.bReqFocus) {
                        mAudioFocusController.requestAudioFocus(mStreamType, mLastDurationHint);
                    }
                    if (mPrivateConfig.bRegMediaSession) {
                        MediaPlaybackController.get().enable();
                    }
                    sAidlPlayer.start(getSessionId());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 暂停
     */
    public void pause() {
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    sAidlPlayer.pause(getSessionId());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 停止
     */
    public void stop() {
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    sAidlPlayer.reset(getSessionId());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private int mStreamType = AudioManager.STREAM_MUSIC;

    /**
     * 设置音频流
     */
    public void setStreamType(int streamType) {
        mStreamType = streamType;
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    sAidlPlayer.setStreamType(getSessionId(), mStreamType);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 获取音频流
     */
    public int getStreamType() {
        return mStreamType;
    }

    private float mLeftVol = 1f, mRightVol = 1f;

    /**
     * 设置左右声道音量
     */
    public void setVolume(@FloatRange(from = 0, to = 1) float leftVol, @FloatRange(from = 0, to = 1) float rightVol) {
        mLeftVol = leftVol;
        mRightVol = rightVol;
        mPlayerHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    sAidlPlayer.setVolume(getSessionId(), mLeftVol, mRightVol);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 设置状态监听
     */
    public void setAudioPlayerStateChangeListener(AudioPlayerStateChangeListener listener) {
        mAudioPlayerStateChangeListener = listener;
    }

    /**
     * 设置队列管理拦截器
     */
    public void setAudioPlayerQueueInterceptor(AudioPlayerQueueInterceptor interceptor) {
        mAudioPlayerQueueInterceptor = interceptor;
    }

    // 当前播放器的session_id
    private int getSessionId() {
        return hashCode();
    }

    /**
     * 清空播放内容
     */
    public void clear() {
        mPlayQueue.clearQueue();
        reInitCurr();
        if (mAudioPlayerStateChangeListener != null) {
            mAudioPlayerStateChangeListener.onAudioChanged(null, false);
        }
    }
}
