package com.txznet.music.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.txznet.comm.remote.GlobalContext;

public class NetWorkObservable extends Observable<NetWorkObservable.NetWorkObserver> {
    public interface NetWorkObserver {
        void onNetWorkStatusChange(boolean connected);
    }

    private Context mContext;
    private ConnectivityManager mConnectivityManager;
    private boolean mIsNetWorkActive;// 当前网络是否连接上
    private boolean mRegisted;// 是否注册了广播

    private static NetWorkObservable INSTANCE = new NetWorkObservable(GlobalContext.get());

    private NetWorkObservable(Context context) {
        mContext = context;
        // 获取程序启动时的网络状态
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mIsNetWorkActive = isGprsOrWifiConnected();
        // 注册网络监听广播
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        mContext.registerReceiver(mNetWorkChangeReceiver, intentFilter);
        mRegisted = true;
    }

    public static NetWorkObservable get() {
        return INSTANCE;
    }

    private boolean isGprsOrWifiConnected() {
        NetworkInfo gprs = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isConnectedGprs = gprs != null && gprs.isConnected();
        boolean isConnectedWifi = wifi != null && wifi.isConnected();
        return isConnectedGprs || isConnectedWifi;
    }

    private BroadcastReceiver mNetWorkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                return;
            }
            final boolean isConnected = isGprsOrWifiConnected();
            if (mIsNetWorkActive != isConnected) {// 和之前的状态不同
                mIsNetWorkActive = isConnected;
                notifyChanged(mIsNetWorkActive);
            }
        }
    };

    public void notifyChanged(boolean connected) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onNetWorkStatusChange(connected);
            }
        }
    }

    public void release() {
        if (mRegisted && mContext != null) {
            mContext.unregisterReceiver(mNetWorkChangeReceiver);
            mRegisted = false;
        }
        unregisterAll();
    }

    public boolean isNetworkActive() {
        return mIsNetWorkActive;
    }

}
