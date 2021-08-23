package com.txznet.music.util;

/**
 * 小程序专用工具类
 *
 * @author telen
 * @date 2018/12/28,11:58
 */
public class ProgramUtils {

    /**
     * 是不是小程序
     *
     * @return
     */
    public static boolean isProgram() {
        return false;
//        return RePlugin.getHostContext() != null;
    }
}
