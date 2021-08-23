package com.txznet.proxy.util;

import android.os.SystemClock;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.proxy.BuildConfig;

import java.util.Map;

/**
 * 测试性能
 * Created by telenewbie on 2017/4/18.
 */

public class TimeUtils {

    public static final String TAG = "Music:Time:";
    public static final Map<String, Long> spendMaps = new ArrayMap<>(1);
    public static boolean isNeedLog = true;

    public static void startTime(String tag) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (isNeedLog) {
            synchronized (spendMaps) {
                long startTime = SystemClock.elapsedRealtime();
                spendMaps.put(tag, startTime);
            }
        }
    }

    public static void endTime(String tag) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (isNeedLog) {
            synchronized (spendMaps) {
                Long startTime = spendMaps.remove(tag);
                if (startTime != null) {
                    long cost = (SystemClock.elapsedRealtime() - startTime);
                    if (cost > 50) {
                        LogUtil.logw(TAG + tag + ":cost too long time=" + (SystemClock.elapsedRealtime() - startTime) + "ms");
                    } else {
                        LogUtil.logd(TAG + tag + ":cost time=" + (SystemClock.elapsedRealtime() - startTime) + "ms");
                    }
                }
            }
        }
    }

    public static void hit(String tag) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        synchronized (spendMaps) {
            Long startTime = spendMaps.remove(tag);
            if (startTime != null) {
                long cost = (SystemClock.elapsedRealtime() - startTime);
                if (cost > 50) {
                    Log.w(TAG, tag + ":cost too long time=" + (SystemClock.elapsedRealtime() - startTime) + "ms");
                } else {
                    Log.d(TAG, tag + ":cost time=" + (SystemClock.elapsedRealtime() - startTime) + "ms");
                }
            } else {
                startTime = SystemClock.elapsedRealtime();
                spendMaps.put(tag, startTime);
            }
        }
    }
}
