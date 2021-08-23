package com.txznet.audio.player;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface IMediaPlayer {

    /*
     播放状态，变化逻辑参考
     @See https://img-blog.csdn.net/20140621192014671?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvc29uZ3NoaXpodXl1YW4=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast
      */
    int STATE_ON_IDLE = 0; // 空闲，默认状态
    int STATE_ON_INITIALIZED = 1; // 初始化完成  setDataSource
    int STATE_ON_PREPARING = 2; // 加载中  prepareAsync
    int STATE_ON_PREPARED = 3; // 加载完毕 prepared
    int STATE_ON_PLAYING = 4; // 播放中 start
    int STATE_ON_BUFFERING = 5; // 缓冲中 数据量不足导致播放暂停
    int STATE_ON_PAUSED = 6; // 暂停中 pause
    int STATE_ON_STOPPED = 7; // 暂停中 pause
    int STATE_ON_END = -1; // 释放 release
    int STATE_ON_ERROR = -2; // 错误 error

    @IntDef({
            STATE_ON_IDLE,
            STATE_ON_INITIALIZED,
            STATE_ON_PREPARING,
            STATE_ON_PREPARED,
            STATE_ON_PLAYING,
            STATE_ON_BUFFERING,
            STATE_ON_PAUSED,
            STATE_ON_STOPPED,
            STATE_ON_END,
            STATE_ON_ERROR,
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface PlayState {
    }

    void setDataSource(String path);

    String getDataSource();

    void prepareAsync();

    void start();

    void stop();

    void pause();

    boolean isPlaying();

    void seekTo(long msec);

    long getCurrentPosition();

    long getDuration();

    void release();

    void reset();

    void setVolume(float leftVolume, float rightVolume);

    void setAudioStreamType(int streamType);

    @PlayState
    int getPlayState();

    void setOnPlayStateChangeListener(OnPlayerStateChangeListener listener);
}
