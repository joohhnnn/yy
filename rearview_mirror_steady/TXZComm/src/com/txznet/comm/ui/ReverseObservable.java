package com.txznet.comm.ui;

import android.database.Observable;

import java.util.ArrayList;

/**
 * date：2021/07/31
 * 倒车开启的广播通知
 */
public class ReverseObservable extends Observable<ReverseObservable.ReverseObserver> {
    public final static String NOTIFY_OBSERVER_ACTION_REVERSE = "com.txznet.txz.event.NOTIFY.ACTION_REVERSE"; // 通知消息的主键，开启倒车影像通知

    public interface ReverseObserver {
        void onReversePressed();
    }


    public void onReverse() {
        synchronized (mObservers) {
            ArrayList<ReverseObservable.ReverseObserver> clone =(ArrayList<ReverseObservable.ReverseObserver>) mObservers.clone();
            for (int i = clone.size() - 1; i >= 0; i--) {
                clone.get(i).onReversePressed();
            }
        }
    }
}
