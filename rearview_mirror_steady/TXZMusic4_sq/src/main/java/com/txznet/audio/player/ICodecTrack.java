package com.txznet.audio.player;

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

    void seek(long time, long position);


    long getDuration();

    void setOnStateListener(OnStateListener listener);

    void request();

    // 枚举
    enum State {
        inited, buffered, played, paused, seekComplete, exited;
    }
}
