package com.txznet.txz.module.wifi;

import com.txznet.txz.jni.JNIHelper;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class WifiDirectStateLisener extends BroadcastReceiver {
	static boolean mDisabled = false;

	@Override
	public void onReceive(Context context, Intent intent) {
		JNIHelper.logd("recv intent: " + intent.getAction());

		// P2P状态改变
		if (intent.getAction().equals(
				WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)) {
			int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
			JNIHelper.logd("wifi p2p state: " + state);

			if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
				mDisabled = true;
			}

			if (mDisabled && state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
				WifiDirectManager.getInstance().resetChannel(5000);
				mDisabled = false;
			}

			return;
		}

		// P2P列表发生改变
		if (intent.getAction().equals(
				WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)) {
			// WifiDirectManager.getInstance().requestPeers();
			return;
		}

		// P2P连接状态改变
		if (intent.getAction().equals(
				WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)) {

			NetworkInfo networkInfo = (NetworkInfo) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

			if (networkInfo.isConnected()) {
				WifiP2pInfo wifiP2pInfo = (WifiP2pInfo) intent
						.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
				WifiDirectManager.getInstance().onP2PConnected(wifiP2pInfo);
			} else if (networkInfo.isConnectedOrConnecting() == false) {
				// TODO 断开连接了
			}
		}

		// P2P设备信息改变
		if (intent.getAction().equals(
				WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)) {
			WifiP2pDevice d = (WifiP2pDevice) intent
					.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
			WifiDirectManager.getInstance().updateSelfDevice(d);
			return;
		}
	}
}
