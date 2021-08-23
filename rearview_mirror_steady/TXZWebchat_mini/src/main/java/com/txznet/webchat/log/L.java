package com.txznet.webchat.log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.webchat.BuildConfig;

public final class L {
    public static final String LOG_TAG_DEFAULT = "local";
    public static final String LOG_HEADER = "TXZWebchat::";

    L() {
    }

    public static int d(String msg) {
        return d(LOG_TAG_DEFAULT, msg);
    }

    public static int i(String msg) {
        return i(LOG_TAG_DEFAULT, msg);
    }

    public static int w(String msg) {
        return w(LOG_TAG_DEFAULT, msg);
    }

    public static int e(String msg) {
        return e(LOG_TAG_DEFAULT, msg);
    }

    public static int f(String msg) {
        return f(LOG_TAG_DEFAULT, msg);
    }

    public static int d(String tag, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            return android.util.Log.d(tag, msg);
        }
        return LogUtil.logd(LOG_HEADER + tag + "::" + adjustLongMsg(msg));
    }

    public static int i(String tag, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            return android.util.Log.i(tag, msg);
        }
        return LogUtil.logi(LOG_HEADER + tag + "::" + adjustLongMsg(msg));
    }

    public static int w(String tag, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            return android.util.Log.w(tag, msg);
        }
        return LogUtil.logw(LOG_HEADER + tag + "::" + adjustLongMsg(msg));
    }

    public static int e(String tag, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            return android.util.Log.e(tag, msg);
        }
        return LogUtil.loge(LOG_HEADER + tag + "::" + adjustLongMsg(msg));
    }

    public static int f(String tag, String msg) {
        if (BuildConfig.LOG_DEBUG) {
            return android.util.Log.wtf(tag, msg);
        }
        return LogUtil.logf(LOG_HEADER + tag + "::" + adjustLongMsg(msg));
    }

    private static String adjustLongMsg(String rawMsg) {
        // LogUtil中已添加日志长度处理逻辑, 此处处理已不必要
        /*if (rawMsg.length() > 1200) {
            return rawMsg.substring(0, 1200) + "...";
        }*/

        return rawMsg;
    }
}
