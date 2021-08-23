package com.txznet.txz.module.login;

import java.util.ArrayList;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.util.DeviceUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;

/**
 * Created by TXZ-METEORLUO on 2017/5/19.
 */
public class SimNetManagerImpl {
	public static interface OnSimNetChangeListener {
		/**
		 * 网络异常
		 */
		int STATE_NET_ERROR = -2;
		/**
		 * 没有SIM卡
		 */
		int STATE_NO_SIM = -1;
		/**
		 * 正在检测
		 */
		int STATE_CHECKING = 0;
		/**
		 * 有SIM卡
		 */
		int STATE_HAVE_SIM = 1;
		/**
		 * 网络良好
		 */
		int STATE_NET_WELL = 2;

		/**
		 * WIFI连接上
		 */
		int STATE_NET_ALL_WELL = 3;

		public void onChange(int status);
	}
	
	private ArrayList<OnSimNetChangeListener> mListeners = new ArrayList<OnSimNetChangeListener>();
	private static SimNetManagerImpl sInstance = new SimNetManagerImpl();
	
	private Object mLock = new Object();
	private int mCurrNetStatus = OnSimNetChangeListener.STATE_NO_SIM;
	private BroadcastReceiver mNetReceiver = null;

	public static SimNetManagerImpl getInstance() {
		return sInstance;
	}

	private SimNetManagerImpl() {
		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		iFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
		mNetReceiver = new BroadcastReceiver() {

			public void onReceive(Context context, Intent intent) {
				String act = intent.getAction();
				LogUtil.logd("SimNet onReceive:" + act);
				notifySimStatus();
			}
		};
		GlobalContext.get().registerReceiver(mNetReceiver, iFilter);
	}

	/**
	 * 检查sim卡状态
	 *
	 * @return
	 */
	private void notifySimStatus() {
		if (!DeviceUtil.isWifiConnect(GlobalContext.get())) {
			if (DeviceUtil.isSimAvaible()) {
				notifyStatusListener(OnSimNetChangeListener.STATE_HAVE_SIM);

				// 开始检测网络情况
				checkNetSpeed(2000);
			} else {
				// 没有SIM卡的话
				notifyStatusListener(OnSimNetChangeListener.STATE_NO_SIM);
			}
		} else {
			notifyStatusListener(OnSimNetChangeListener.STATE_NET_ALL_WELL);
		}
	}
	
	private long startCheckTime;
	
	/**
	 * 检测SIM卡网络状态
	 */
	Runnable mCheckNetTask = new Runnable() {

		public void run() {
			notifyStatusListener(OnSimNetChangeListener.STATE_CHECKING);
			startCheckTime = SystemClock.elapsedRealtime();
			NetworkManager.getInstance().checkNetConnect(3000, new Runnable() {

				@Override
				public void run() {
					long delay = 2000 - (SystemClock.elapsedRealtime() - startCheckTime);
					LogUtil.logd("startDelay:" + delay);
					if (delay >= 0) {
						AppLogic.runOnBackGround(new Runnable() {

							@Override
							public void run() {
								notifyStatusListener(OnSimNetChangeListener.STATE_NET_WELL);
							}
						}, delay);
					} else {
						notifyStatusListener(OnSimNetChangeListener.STATE_NET_WELL);
					}
				}
			}, new Runnable() {

				@Override
				public void run() {
					long delay = 2000 - (SystemClock.elapsedRealtime() - startCheckTime);
					LogUtil.logd("startDelay:" + delay);
					if (delay >= 0) {
						AppLogic.runOnBackGround(new Runnable() {

							@Override
							public void run() {
								notifyStatusListener(OnSimNetChangeListener.STATE_NET_ERROR);
							}
						}, delay);
					} else {
						notifyStatusListener(OnSimNetChangeListener.STATE_NET_ERROR);
					}
				}
			});
		}
	};

	private void checkNetSpeed(int delay) {
		AppLogic.removeBackGroundCallback(mCheckNetTask);
		AppLogic.runOnBackGround(mCheckNetTask, delay);
	}

	/**
	 * 播报当前状态值
	 * @param status
	 */
	private void notifyStatusListener(final int status) {
		synchronized (mLock) {
			mCurrNetStatus = status;
		}

		AppLogic.runOnUiGround(new Runnable() {

			
			public void run() {
				synchronized (mListeners) {
					for (OnSimNetChangeListener listener : mListeners) {
						listener.onChange(status);
					}
				}
			}
		}, 0);
	}

	
	public boolean isChecking() {
		return !DeviceUtil.isWifiConnect(GlobalContext.get())
				&& mCurrNetStatus != OnSimNetChangeListener.STATE_NET_ALL_WELL
				&& mCurrNetStatus != OnSimNetChangeListener.STATE_NET_WELL;
	}

	/**
	 * 请求当前状态
	 * @return
	 */
	public int requestState() {
		notifySimStatus();
		synchronized (mLock) {
			return mCurrNetStatus;
		}
	}

	public void registerListener(OnSimNetChangeListener listener) {
		if (mListeners.contains(listener)) {
			return;
		}

		mListeners.add(listener);
	}

	public void unRegisterListener(OnSimNetChangeListener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}
	
	public void release() {
		GlobalContext.get().unregisterReceiver(mNetReceiver);
		clearStaticObject();
	}
	
	public static void clearStaticObject() {
		sInstance = null;
	}
}