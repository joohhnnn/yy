package com.txznet.proxy.util;

/**
 * @author zackzhou
 * @date 2019/7/2,10:26
 * @see <a href="https://github.com/square/leakcanary/blob/828c98076df8d0cb9df3d5cc8ad1c8db8c45d525/leakcanary-watcher/src/main/java/leakcanary/GcTrigger.kt"></a>
 */

public class GcTrigger {

    private GcTrigger() {
    }

    public static void runGc() {
        System.gc();
    }

    private static void enqueueReferences() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
    }
}
