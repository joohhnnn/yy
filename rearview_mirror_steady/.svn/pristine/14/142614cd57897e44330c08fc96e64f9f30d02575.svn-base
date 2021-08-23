package com.txznet.music.utils;

import android.os.Looper;
import android.util.AndroidRuntimeException;

/**
 * Created by brainBear on 2018/1/2.
 */

public class ThreadUtil {


    private ThreadUtil() {

    }


    public static void checkMainThread(String methodName) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new AndroidRuntimeException("Must be in the main thread to invoke this method:" + methodName);
        }
    }

}
