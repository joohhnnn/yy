package com.txznet.music.utils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.HashMap;

/**
 * Created by brainBear on 2017/11/1.
 * 用于简化获取配置项的值
 */

public class FileConfigUtil {

    /**
     * 需要获取的配置项
     */
    private static String[] sKEY = {TXZFileConfigUtil.KEY_MUSIC_REVERSING_PLAY
            , TXZFileConfigUtil.KEY_MUSIC_SCREEN_TYPE};

    /**
     * 是否已经装载过配置项
     */
    private static boolean sLoaded = false;
    private static HashMap<String, String> sConfig;

    private FileConfigUtil() {

    }

    private static void loadConfig() {
        sConfig = TXZFileConfigUtil.getConfig(sKEY);
        if (null == sConfig || sConfig.isEmpty()) {
            LogUtil.d("load file config error:config is empty");
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
