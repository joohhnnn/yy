package com.txznet.webchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;

public class RecordStatusObservable extends Observable<RecordStatusObservable.StatusObserver> {
    private static final String ACTION_SHOW = "com.txznet.txz.record.show";
    private static final String ACTION_DISMISS = "com.txznet.txz.record.dismiss";

    public interface StatusObserver {
        void onStatusChanged(boolean isShowing);
    }

    private Context mContext;
    private boolean mIsShowing;
    private boolean mRegisted;

    public RecordStatusObservable(Context context) {
        mContext = context;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "record.ui.status.isShowing", null, new GetDataCallback() {
                    @Override
                    public void onGetInvokeResponse(ServiceData data) {
                        try {
                            mIsShowing = data.getBoolean();
                        } catch (NullPointerException e) {
                        }
                    }
                });
        // 注册监听广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SHOW);
        intentFilter.addAction(ACTION_DISMISS);
        mContext.registerReceiver(mRecordStatusReceiver, intentFilter);
        mRegisted = true;
    }

    private BroadcastReceiver mRecordStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_SHOW)) {
                mIsShowing = true;
                notifyChanged(true);
            } else if (intent.getAction().equals(ACTION_DISMISS)) {
                mIsShowing = false;
                notifyChanged(false);
            }
        }
    };

    private void notifyChanged(boolean isShowing) {
        synchronized (mObservers) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onStatusChanged(isShowing);
            }
        }
    }

    public void release() {
        if (mRegisted && mContext != null) {
            mContext.unregisterReceiver(mRecordStatusReceiver);
            mRegisted = false;
        }
        unregisterAll();
    }

    public boolean isShowing() {
        return mIsShowing;
    }
}
