package com.txznet.txz.util;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

/**
 * Created by TXZ-METEORLUO on 2017/5/19.
 */
public class DeviceUtil {

	public static boolean isSimAvaible() {
		TelephonyManager tm = (TelephonyManager) GlobalContext.get().getSystemService(Service.TELEPHONY_SERVICE);
		int simState = tm.getSimState();
		LogUtil.logd("checkSimStatus: " + simState);
		if (TelephonyManager.SIM_STATE_READY == simState) {
			return true;
		}

		return false;
	}

	/**
	 * 判断网络连接是否可用
	 *
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
		} else {
			NetworkInfo[] info = cm.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean isWifiConnect(Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isConnected())
			return true;
		else
			return false;
	}
}
