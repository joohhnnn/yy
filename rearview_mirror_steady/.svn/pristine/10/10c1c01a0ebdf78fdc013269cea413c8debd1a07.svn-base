package com.txznet.sdk.media.constant;

/**
 * 播放器循环模式
 * Created by J on 2018/5/7.
 */

public enum PlayerLoopMode {
    /**
     * 顺序播放模式
     */
    SEQUENTIAL("sequential"),
    /**
     * 单曲循环模式
     */
    SINGLE_LOOP("singleLoop"),
    /**
     * 列表循环模式
     */
    LIST_LOOP("listLoop"),
    /**
     * 随机模式
     */
    SHUFFLE("shuffle");

    private String mModeStr;
    PlayerLoopMode(String modeStr) {
        mModeStr = modeStr;
    }

    public String toModeStr() {
        return mModeStr;
    }

    public static PlayerLoopMode fromModeStr(String modeStr) {
        if (SEQUENTIAL.toModeStr().equals(modeStr)) {
            return SEQUENTIAL;
        } else if (SINGLE_LOOP.toModeStr().equals(modeStr)) {
            return SINGLE_LOOP;
        } else if (LIST_LOOP.toModeStr().equals(modeStr)) {
            return LIST_LOOP;
        } else if (SHUFFLE.toModeStr().equals(modeStr)) {
            return SHUFFLE;
        }

        return SEQUENTIAL;
    }
}
