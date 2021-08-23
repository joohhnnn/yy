package com.txznet.music.util;

import com.txznet.music.Constant;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.Map;

/**
 * Created by brainBear on 2017/11/1.
 * 用于简化获取配置项的值
 */

public class FileConfigUtil {

    //同听私有，不给方案商进行配置
    public static final String KEY_MUSIC_LOG_NEED_PRINT_LISTS = "log_list"; //“正在为您搜索”提示语的延时时间，如果结果提前回来则不播报，默认为0,单位ms
    public static final String KEY_MUSIC_OPEN_LOCAL_MUSIC = "open_local_music"; //“正在为您搜索”提示语的延时时间，如果结果提前回来则不播报，默认为0,单位ms
    public static final String KEY_MUSIC_OPEN_HISTORY_MUSIC = "open_history_music"; //“正在为您搜索”提示语的延时时间，如果结果提前回来则不播报，默认为0,单位ms
    public static final String KEY_MUSIC_RESUME_PLAY_AFTER_WAKEUP = "resumePlayAfterWakeup"; // 设备唤醒起来后是否恢复播放

    /**
     * 需要获取的配置项
     */
    private static String[] sKEY = {
            TXZFileConfigUtil.KEY_MUSIC_REVERSING_PLAY
            , TXZFileConfigUtil.KEY_MUSIC_SCREEN_TYPE
            , TXZFileConfigUtil.KEY_MUSIC_KEYCODE_NEXT
            , TXZFileConfigUtil.KEY_MUSIC_KEYCODE_PREV
            , TXZFileConfigUtil.KEY_MUSIC_LOSS_TRANSIENT_M
            , TXZFileConfigUtil.KEY_MUSIC_LOSS_TRANSIENT_R
            , TXZFileConfigUtil.KEY_MUSIC_LOSS
            , TXZFileConfigUtil.KEY_MEDIA_BUTTON
            , TXZFileConfigUtil.KEY_MUSIC_SCREEN_STYLE
            , TXZFileConfigUtil.KEY_MUSIC_MAX_CACHE_SIZE
            , TXZFileConfigUtil.KEY_MUSIC_FOCUS_CAN_REQUEST
            , TXZFileConfigUtil.KEY_MUSIC_SEARCH_TIPS_DELAY
            , TXZFileConfigUtil.KEY_MUSIC_LOSS_TRANSIENT_FACTOR
            , KEY_MUSIC_RESUME_PLAY_AFTER_WAKEUP
            , KEY_MUSIC_OPEN_LOCAL_MUSIC
            , KEY_MUSIC_OPEN_HISTORY_MUSIC
            , KEY_MUSIC_LOG_NEED_PRINT_LISTS
    };

    /**
     * 是否已经装载过配置项
     */
    private static boolean sLoaded = false;
    private static Map<String, String> sConfig;

    private FileConfigUtil() {

    }

    private static void loadConfig() {
        sConfig = TXZFileConfigUtil.getConfig(sKEY);
        if (null == sConfig || sConfig.isEmpty()) {
            Logger.d(Constant.LOG_TAG_UTILS, "load file config error:config is empty");
        }
        sLoaded = true;
    }


    public static boolean getBooleanConfig(String key, boolean defaultValue) {
        if (!sLoaded) {
            loadConfig();
        }
        if (sConfig.containsKey(key)) {
            return Boolean.valueOf(sConfig.get(key));
        }
        return defaultValue;
    }

    public static int getIntegerConfig(String key, int defaultValue) {
        if (!sLoaded) {
            loadConfig();
        }
        if (sConfig.containsKey(key)) {
            try {
                return Integer.valueOf(sConfig.get(key));
            } catch (NumberFormatException e) {
                Logger.e("loadfile :" + key, e);
                return defaultValue;
            }
        }
        return defaultValue;
    }


    public static long getLongConfig(String key, long defaultValue) {
        if (!sLoaded) {
            loadConfig();
        }
        if (sConfig.containsKey(key)) {
            try {
                return Long.valueOf(sConfig.get(key));
            } catch (NumberFormatException e) {
                Logger.e("loadfile :" + key, e);
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static String getStringConfig(String key, String defaultValue) {
        if (!sLoaded) {
            loadConfig();
        }
        if (sConfig.containsKey(key)) {
            return sConfig.get(key);
        }
        return defaultValue;
    }


}
