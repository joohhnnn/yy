package com.txznet.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;

/**
 * Created by ASUS User on 2015/9/21.
 */
public class ThemeObservable extends Observable<ThemeObservable.ThemeObserver> {
    public static interface ThemeObserver {
        public void onThemeChanged(String themeName);
    }

    private static final String ACTION_NAME = "com.txznet.action.THEME_CHANGE";

    private Context mContext;
    private boolean mRegisted;

    public ThemeObservable(Context context) {
        mContext = context;
        IntentFilter intentFilter = new IntentFilter(ACTION_NAME);
        mContext.registerReceiver(mThemeChangeReceiver, intentFilter);
        mRegisted = true;
    }

    private BroadcastReceiver mThemeChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_NAME)) {
                String themeName = intent.getStringExtra("theme");
                notifyChanged(themeName);
            }
        }
    };

    public void notifyChanged(String themeName) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onThemeChanged(themeName);
            }
        }
    }

    public boolean containsObserver(ThemeObserver observer) {
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
                return true;
            }
        }
        return false;
    }

    public void release() {
        if (mRegisted && mContext != null) {
            mContext.unregisterReceiver(mThemeChangeReceiver);
            mRegisted = false;
        }
        unregisterAll();
    }
}
