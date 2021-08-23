package com.txznet.music.utils;

import android.os.Debug;

import com.txznet.audio.ProcessMemoryMonitor;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;

/**
 * 打印
 */
public class MyLog {

    private static final String TAG = "music:mylog:";

    private static int calSize = 0;

    public static void printCurrentMemory(String myTag) {
        calSize++;
        Debug.MemoryInfo memoryInfo = ProcessMemoryMonitor.getInstance().getMemoryInfo(GlobalContext.get().getPackageName());
        if (null != memoryInfo) {
            Logger.d(TAG, "[" + myTag + "]current memory:[" + calSize + "] " + memoryInfo.getTotalPss());
        } else {
            Logger.d(TAG, "[" + myTag + "]current memory:[" + calSize + "] 0M");
        }
    }


}
