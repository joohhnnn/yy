package com.txznet.music.data.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.disposables.Disposable;

/**
 * 网络请求管理类，用于做相同的请求，只有一次响应
 */
public class NetRequestManager {
    private static Map<String, Disposable> values = new ConcurrentHashMap<>();

    public static void removeSameRequest(String cmd) {
        Disposable remove = values.remove(cmd);
        if (remove != null && !remove.isDisposed()) {
            remove.dispose();
        }
    }

    public static void addMonitor(String cmd, Disposable disposable) {
        values.put(cmd, disposable);
    }

}
