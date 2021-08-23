package com.txznet.comm.remote.util;

/**
 * Created by brainBear on 2017/10/30.
 */

public class L {

    /**
     * 解码器native层会反射调用该方法，该方法不能混淆
     *
     * @param level   日志等级
     * @param tag     TAG
     * @param content 内容
     */
    public static void nativeReflectLog(int level, String tag, String content) {
        LogUtil.invokeRemoteLog(level, tag, content);
    }

}
