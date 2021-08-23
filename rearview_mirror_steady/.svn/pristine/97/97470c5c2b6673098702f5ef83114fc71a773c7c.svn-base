package com.txznet.record;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;
import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.record.HomeObservable.HomeObserver;

public class HomeObservable extends Observable<HomeObserver> {
	public static interface HomeObserver {
		void onHomePressed();
	}

	private Context mContext;

	HomeObservable(Context context) {
		mContext = context;
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		mContext.registerReceiver(mHomeReceiver, intentFilter);
	}

	public void release() {
		mContext.unregisterReceiver(mHomeReceiver);
	}

	private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
		private static final String LOG_TAG = "HomeReceiver";
		private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
		private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
		private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
		private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
		private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.logd("onReceive: action: " + action);
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				// android.intent.action.CLOSE_SYSTEM_DIALOGS
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
					// 短按Home键
					notifyChanged();
				} else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
					// 长按Home键 或者 activity切换键
				} else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
					// 锁屏
				} else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
					// samsung 长按Home键
				}
			}
		}
	};

	public void notifyChanged() {
		synchronized (mObservers) {
			for (int i = mObservers.size() - 1; i >= 0; i--) {
				mObservers.get(i).onHomePressed();
			}
		}
	}
}
