package com.txznet.music.util;

import android.os.SystemClock;

import com.txznet.comm.remote.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试性能
 * Created by telenewbie on 2017/4/18.
 */

public class TimeUtils {

    public static final String TAG = "music:time:";
    public static long startTime = SystemClock.elapsedRealtime();
    public static final Map<String, Long> spendMaps = new HashMap<>();
    public static boolean isNeedLog = true;

    public static void startTime(String tag) {
        if (isNeedLog) {
            synchronized (spendMaps) {
                startTime = SystemClock.elapsedRealtime();
                spendMaps.put(tag, startTime);
            }
        }
    }

    public static void endTime(String tag) {
        if (isNeedLog) {
            synchronized (spendMaps) {
                Long startTime = spendMaps.remove(tag);
                if (startTime != null){
                    LogUtil.logd(TAG + tag + ":spend:" + (SystemClock.elapsedRealtime() - startTime));
                }
            }
        }
    }
}
