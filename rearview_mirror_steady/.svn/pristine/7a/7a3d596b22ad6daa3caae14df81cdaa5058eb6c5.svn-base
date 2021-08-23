package com.txznet.txz.util.focus_supporter.log;

/**
 * Created by J on 2017/4/24.
 */

public final class FocusLog {
    public static boolean LOG_ENABLED = true;
    public static final String LOG_TAG_DEFAULT = "FocusSupporter";

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
        if (LOG_ENABLED) {
            return android.util.Log.d(tag, msg);
        }

        return -1;
    }

    public static int i(String tag, String msg) {
        if (LOG_ENABLED) {
            return android.util.Log.i(tag, msg);
        }

        return -1;
    }

    public static int w(String tag, String msg) {
        if (LOG_ENABLED) {
            return android.util.Log.w(tag, msg);
        }

        return -1;
    }

    public static int e(String tag, String msg) {
        if (LOG_ENABLED) {
            return android.util.Log.e(tag, msg);
        }

        return -1;
    }

    public static int f(String tag, String msg) {
        if (LOG_ENABLED) {
            return android.util.Log.wtf(tag, msg);
        }

        return -1;
    }
}
