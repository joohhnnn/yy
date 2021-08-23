package com.txznet.rxflux;

import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;

import java.util.Iterator;

import io.reactivex.disposables.Disposable;

/**
 * Disposable管理器
 * 管理异步处理RxAction产生的Disposable，便于取消
 * <p>
 * 注意：同类型(actionType)不同操作来源(operation)的action是隔离的！！！不会互相取消
 *
 * @author github
 */
class DisposableManager {

    private static final class Holder {
        private static final DisposableManager MANAGER = new DisposableManager();
    }

    private ArrayMap<String, Pair<Integer, Disposable>> mMap;

    private DisposableManager() {
        mMap = new ArrayMap<>();
    }

    public static DisposableManager get() {
        return Holder.MANAGER;
    }

    /**
     * SDK操作只覆盖相同类型操作
     * 非SDK操作会覆盖除SDK操作类型以外的操作
     */
    public synchronized void add(RxAction action, Disposable disposable) {
        mMap.remove(null);
        Iterator<String> iterator = mMap.keySet().iterator();
        while (iterator.hasNext()) {
            Pair<Integer, Disposable> pair = mMap.get(iterator.next());
            if (pair != null && !pair.second.isDisposed()) {
                iterator.remove();
            }
        }
        if (Operation.SDK != action.operation) {
            for (String key : mMap.keySet()) {
                if (key != null && key.startsWith(action.type) && !key.endsWith(Operation.SDK.name())) {
                    Pair<Integer, Disposable> pair = mMap.get(key);
                    if (pair != null && !pair.second.isDisposed()) {
                        pair.second.dispose();
                    }
                }
            }
        }
        Pair<Integer, Disposable> old = mMap.put(action.getKey(), getPair(action, disposable));
        if (old != null && !old.second.isDisposed()) {
            old.second.dispose();
        }
    }

    /**
     * SDK操作只覆盖相同类型操作
     * 非SDK操作会覆盖除SDK操作类型以外的操作
     */
    public synchronized void remove(RxAction action) {
        mMap.remove(null);
        if (Operation.SDK != action.operation) {
            for (String key : mMap.keySet()) {
                if (key != null && key.startsWith(action.type) && !key.endsWith(Operation.SDK.name())) {
                    Pair<Integer, Disposable> pair = mMap.get(key);
                    if (pair != null && !pair.second.isDisposed()) {
                        pair.second.dispose();
                    }
                }
            }
        }
        Pair<Integer, Disposable> old = mMap.remove(action.getKey());
        if (old != null && !old.second.isDisposed()) {
            old.second.dispose();
        }
    }

    public synchronized boolean contains(RxAction action) {
        Pair<Integer, Disposable> old = mMap.get(action.getKey());
        return old != null && old.first == action.hashCode() && !old.second.isDisposed();
    }

    public synchronized void clear() {
        if (mMap.isEmpty()) {
            return;
        }
        for (Pair<Integer, Disposable> pair : mMap.values()) {
            if (!pair.second.isDisposed()) {
                pair.second.dispose();
            }
        }
    }

    private Pair<Integer, Disposable> getPair(RxAction action, Disposable disposable) {
        return new Pair<>(action.hashCode(), disposable);
    }
}
