package com.txznet.txz.util;

import android.os.SystemClock;

public class QuickClickUtil {
    /**
     * 默认两秒
     */
    public static final long DEFAULT_CLICK_INTERVAL = 2000;
    private long mClickInterval = DEFAULT_CLICK_INTERVAL;
    private long mLastClick = 0;

    public QuickClickUtil() {
        this(DEFAULT_CLICK_INTERVAL);
    }

    public QuickClickUtil(long clickInterval) {
        mClickInterval = clickInterval;
    }

    public boolean check() {
        long now = SystemClock.elapsedRealtime();
        if (now - mLastClick < mClickInterval) {
            return true;
        }
        mLastClick = now;
        return false;
    }
}
