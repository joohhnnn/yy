package com.txznet.sdk.media.constant;

/**
 * 播放器播放状态
 * Created by J on 2018/5/7.
 */

public enum PlayerStatus {
    /**
     * 播放器默认状态
     */
    IDLE("idle"),
    /**
     * 缓冲状态
     */
    BUFFERING("buffering"),
    /**
     * 正在播放
     */
    PLAYING("playing"),
    /**
     * 已暂停
     */
    PAUSED("paused"),
    /**
     * 已停止
     */
    STOPPED("stopped");

    private final String mStatusStr;
    PlayerStatus(String str) {
        mStatusStr = str;
    }

    public String toStatusString() {
        return mStatusStr;
    }

    public static PlayerStatus fromStatusString(String str) {
        if (IDLE.toStatusString().equals(str)) {
            return PlayerStatus.IDLE;
        } else if (BUFFERING.toStatusString().equals(str)) {
            return BUFFERING;
        } else if (PLAYING.toStatusString().equals(str)) {
            return PLAYING;
        } else if (PAUSED.toStatusString().equals(str)) {
            return PAUSED;
        } else if (STOPPED.toStatusString().equals(str)) {
            return STOPPED;
        }

        return IDLE;
    }
}
