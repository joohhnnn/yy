package com.txznet.audio.player;

import com.txznet.comm.err.Error;

public interface OnPlayerStateChangeListener {

    /**
     * 播放状态改变
     *
     * @param state 播放状态
     */
    void onPlayStateChanged(@IMediaPlayer.PlayState int state);

    /**
     * 播放进度改变
     *
     * @param position 当前播放帧
     * @param duration 播放总时长
     */
    void onProgressChanged(long position, long duration);

    /**
     * 跳转完成
     */
    void onSeekComplete();

    /**
     * 播放结束
     */
    void onCompletion();

    /**
     * 错误通知，
     * 注意：抛出错误时，播放器不一定会处于Error状态(不可恢复状态)，以播放器当前PlayState为准
     */
    void onError(Error error);
}
