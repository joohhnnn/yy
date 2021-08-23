package com.txznet.music.util;

import com.txznet.music.R;
import com.txznet.music.data.entity.PlayMode;

/**
 * 播放模式辅助工具
 *
 * @author zackzhou
 * @date 2018/12/26,15:33
 */

public class PlayModeUtil {

    private PlayModeUtil() {
    }

    public static String getName(PlayMode mode) {
        String name = null;
        switch (mode) {
            case SINGLE_LOOP:
                name = "单曲循环";
                break;
            case QUEUE_LOOP:
                name = "循环播放";
                break;
            case RANDOM_PLAY:
                name = "随机播放";
                break;
            default:
                break;
        }
        return name;
    }

    public static int getDrawableRes(PlayMode mode) {
        int res = 0;
        switch (mode) {
            case SINGLE_LOOP:
                res = R.drawable.player_queue_loop_btn;
                break;
            case QUEUE_LOOP:
                res = R.drawable.player_queue_btn;
                break;
            case RANDOM_PLAY:
                res = R.drawable.player_queue_random_btn;
                break;
            default:
                break;
        }
        return res;
    }
}
