package com.txznet.webchat.comm.plugin.utils;

import android.text.TextUtils;

import com.txznet.txz.plugin.PluginManager;

/**
 * 插件LogUtil, 插件内部打印log必须使用此工具
 * Created by J on 2016/11/20.
 */

public class PluginLogUtil {
    private static final String PLUGIN_LOG_PREFIX = "plugin";

    private static final String INVOKE_CMD_LOG_D = "wx.cmd.logd";
    private static final String INVOKE_CMD_LOG_E = "wx.cmd.loge";
    private static final String INVOKE_CMD_LOG_I = "wx.cmd.logi";
    private static final String INVOKE_CMD_LOG_W = "wx.cmd.logw";
    private static final String INVOKE_CMD_LOG_F = "wx.cmd.logf";

    public static void d(String msg) {
        d(null, msg);
    }

    public static void e(String msg) {
        e(null, msg);
    }

    public static void i(String msg) {
        i(null, msg);
    }

    public static void w(String msg) {
        w(null, msg);
    }

    public static void f(String msg) {
        f(null, msg);
    }

    public static void d(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            PluginManager.invoke(INVOKE_CMD_LOG_D, PLUGIN_LOG_PREFIX, msg);
            return;
        }

        PluginManager.invoke(INVOKE_CMD_LOG_D, tag, msg);
    }

    public static void e(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            PluginManager.invoke(INVOKE_CMD_LOG_E, PLUGIN_LOG_PREFIX, msg);
            return;
        }

        PluginManager.invoke(INVOKE_CMD_LOG_E, tag, msg);
    }

    public static void i(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            PluginManager.invoke(INVOKE_CMD_LOG_I, PLUGIN_LOG_PREFIX, msg);
            return;
        }

        PluginManager.invoke(INVOKE_CMD_LOG_I, tag, msg);
    }

    public static void w(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            PluginManager.invoke(INVOKE_CMD_LOG_W, PLUGIN_LOG_PREFIX, msg);
            return;
        }

        PluginManager.invoke(INVOKE_CMD_LOG_W, tag, msg);
    }

    public static void f(String tag, String msg) {
        if (TextUtils.isEmpty(tag)) {
            PluginManager.invoke(INVOKE_CMD_LOG_F, PLUGIN_LOG_PREFIX, msg);
            return;
        }

        PluginManager.invoke(INVOKE_CMD_LOG_F, tag, msg);
    }
}
