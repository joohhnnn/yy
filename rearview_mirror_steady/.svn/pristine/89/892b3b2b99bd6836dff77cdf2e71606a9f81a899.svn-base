package com.txznet.launcher.utils;

import android.os.Looper;

import com.txznet.launcher.BuildConfig;
import com.txznet.launcher.cfg.DebugCfg;

/**
 * Created by brainBear on 2018/3/10.
 * <p>
 * 常用的条件判断工具类
 */

public class Conditions {


    private Conditions() {
    }


    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }


    public static void assertMainThread(String methodName) {

        if (!isMainThread()) {
            throw new RuntimeException("must be invoke " + methodName + " in the main thread");
        }
    }

    public static boolean useAnjixingTestEnvironment (){
        if (DebugCfg.ANJIXING_TEST_ENVIRONMENT_DEBUG) {
            return true;
        }else {
            return BuildConfig.ANJIXING_TEST;
        }
    }

}
