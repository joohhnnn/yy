package com.txznet.music.util;

import android.util.ArrayMap;

import java.util.Iterator;
import java.util.Map;

import io.reactivex.disposables.Disposable;

/**
 * DisposableManager
 *
 * @author zackzhou
 * @date 2018/12/21,10:19
 */

public class DisposableManager {
    private static final class Holder {
        private static final DisposableManager MANAGER = new DisposableManager();
    }

    private Map<String, Disposable> mMap;

    private DisposableManager() {
        mMap = new ArrayMap<>();
    }

    public static DisposableManager get() {
        return Holder.MANAGER;
    }

    public synchronized void add(String key, Disposable disposable) {
        Iterator<String> iterator = mMap.keySet().iterator();
        while (iterator.hasNext()) {
            Disposable d = mMap.get(iterator.next());
            if (d == null || d.isDisposed()) {
                iterator.remove();
            }
        }
        Disposable old = mMap.put(key, disposable);
        if (old != null && !old.isDisposed()) {
            old.dispose();
        }
    }

    public synchronized void remove(String key) {
        Disposable old = mMap.remove(key);
        if (old != null && !old.isDisposed()) {
            old.dispose();
        }
    }

    public synchronized boolean contains(String key) {
        Disposable old = mMap.get(key);
        return old != null && !old.isDisposed();
    }

    public synchronized void clear() {
        if (mMap.isEmpty()) {
            return;
        }
        for (Disposable disposable : mMap.values()) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        mMap.clear();
    }
}
