package com.txznet.audio.player.core.ffmpeg;

import com.txznet.audio.player.OnPlayerStateChangeListener;

public interface ICodecTrack {
    void release();

    void stop();

    boolean isComplete();

    int write(byte[] audioData, int offsetInBytes, int sizeInBytes);

    void flush(Runnable runAfterFlush);

    boolean isPlaying();

    void setStereoVolume(float leftVolume, float rightVolume);

    void play();

    void pause();

    /**
     * 跳转
     * @param time position 单位秒
     * @param position position/duration*文件总大小
     */
    void seek(long time, long position);

    long getDuration();

    long getPosition();

    void setOnPlayStateChangeListener(OnPlayerStateChangeListener listener);

    void request();
}
