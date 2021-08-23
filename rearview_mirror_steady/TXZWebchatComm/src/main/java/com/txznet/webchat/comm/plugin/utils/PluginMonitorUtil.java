package com.txznet.webchat.comm.plugin.utils;

import com.txznet.txz.plugin.PluginManager;

/**
 * Created by J on 2016/8/17.
 */
public class PluginMonitorUtil {
    // auth异常状态码上报前缀
    public static final String WX_LOGIN_FAILED_AUTH_PREFIX = "wx3.login.E.auth_";
    // init异常状态码上报前缀
    public static final String WX_LOGIN_FAILED_INIT_PREFIX = "wx3.login.E.init_";

    public static void doMonitor(String tag) {
        PluginManager.invoke("wx.cmd.monitor", tag);
    }

    public static void monitorAuthRet(int ret) {
        doMonitor(WX_LOGIN_FAILED_AUTH_PREFIX + ret);
    }

    public static void monitorInitRet(int ret) {
        doMonitor(WX_LOGIN_FAILED_INIT_PREFIX + ret);
    }

}
