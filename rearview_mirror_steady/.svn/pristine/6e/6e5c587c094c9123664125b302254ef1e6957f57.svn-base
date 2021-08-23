package com.txznet.music.baseModule.bean;

import android.support.annotation.IntDef;

import com.txznet.sdk.tongting.ITongTingPlayState;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by brainBear on 2017/9/28.
 */

public class PlayerInfo implements ITongTingPlayState {


    //界面使用的状态
    public static final int PLAYER_UI_STATUS_RELEASE = 0;
    public static final int PLAYER_UI_STATUS_BUFFER = 1;
    public static final int PLAYER_UI_STATUS_PLAYING = 2;
    public static final int PLAYER_UI_STATUS_PAUSE = 3;
    /**
     * 顺序播放
     */
    public static final int PLAYER_MODE_SEQUENCE = 0;
    /**
     * 单曲循环
     */
    public static final int PLAYER_MODE_SINGLE_CIRCLE = 1;
    /**
     * 随机播放
     */
    public static final int PLAYER_MODE_RANDOM = 2;


    @IntDef({PLAYER_STATUS_RELEASE, PLAYER_STATUS_BUFFER, PLAYER_STATUS_PLAYING, PLAYER_STATUS_PAUSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayerStatus {
    }

    @IntDef({PLAYER_UI_STATUS_RELEASE, PLAYER_UI_STATUS_BUFFER, PLAYER_UI_STATUS_PLAYING, PLAYER_UI_STATUS_PAUSE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayerUIStatus {
    }

    @IntDef({PLAYER_MODE_SEQUENCE, PLAYER_MODE_SINGLE_CIRCLE, PLAYER_MODE_RANDOM})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayerMode {

    }

}
