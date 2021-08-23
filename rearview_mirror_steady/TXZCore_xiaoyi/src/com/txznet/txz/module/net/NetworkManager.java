package com.txznet.txz.module.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.txz.ui.data.UiData;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;

/**
 * 网络管理模块，负责网络状态监听，网络事件处理
 * 
 * @author bihongpi
 *
 */
public class NetworkManager extends IModule {

	int mNetworkType = UiData.NETWORK_STATUS_NONE;
	private String mApnType = "";
	// 加一个timer避免
	private Runnable checkRun = new Runnable() {
		@Override
		public void run() {
			onNetChange();
			AppLogic.runOnUiGround(this, 2 * 60 * 1000);
		}
	};

	static NetworkManager sModuleInstance = null;

	private NetworkManager() {
		AppLogic.runOnUiGround(checkRun, 1 * 60 * 1000);
		registerDateTransReceiver();
		updateNetInfo();
	}

	public static NetworkManager getInstance() {
		if (sModuleInstance == null) {
			synchronized (NetworkManager.class) {
				
				if (sModuleInstance == null) {
					sModuleInstance = new NetworkManager();
				}
			}
		}
		return sModuleInstance;
	}

	@Override
	public int initialize_AfterStartJni() {
		return super.initialize_AfterStartJni();
	}

	public synchronized void updateNetInfo() {
		try {

			int oldType = mNetworkType;
			int netType = NetworkUtil.getSystemNetwork(GlobalContext.get());
			mNetworkType = netType;
			JNIHelper.logd("oldType=" + Integer.toString(oldType)
					+ "; netType=" + Integer.toString(netType));
			if (netType != oldType) {
				notifyNetChange();
			}
			ConnectivityManager connectivityManager = (ConnectivityManager)GlobalContext.get().getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager
					.getActiveNetworkInfo();
			String exrea = null;
			if (activeNetworkInfo != null) {
				exrea = activeNetworkInfo.getExtraInfo();
			}
			JNIHelper.logd("net event:" + mNetworkType + " " + exrea);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void onNetChange() {
		updateNetInfo();
	}

	public int getNetType() {
		return mNetworkType;
	}

	public synchronized String getApnType() {
		return mApnType;
	}

	public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

	private void registerDateTransReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(CONNECTIVITY_CHANGE_ACTION);
		// filter.setPriority(1000);
		GlobalContext.get().registerReceiver(new NetStatReceiver(), filter);
	}

	public class NetStatReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			JNIHelper.logd("onReceive:" + action);
			if (TextUtils.equals(action, CONNECTIVITY_CHANGE_ACTION)) {// 网络变化的时候会发送通知
				updateNetInfo();
				return;
			}
		}
	}

	void notifyNetChange() {
		JNIHelper.sendEvent(UiEvent.EVENT_NETWORK_CHANGE, 0);
		// TODO 网络变化通知界面
	}
	
	public boolean hasNet() {
		boolean bRet = false;
		switch (NetworkManager.getInstance().getNetType()) {
		case UiData.NETWORK_STATUS_3G:
		case UiData.NETWORK_STATUS_4G:
		case UiData.NETWORK_STATUS_WIFI:
			bRet = true;
			break;
		default:
		}
		return bRet;
	}
}
