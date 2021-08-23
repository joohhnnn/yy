package com.txznet.wifi.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Service;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.txznet.wifi.R;

public class MainActivity extends Activity {

	private ListView mListView;
	private TextView mTextView;
	private WifiAdapter mAdapter;
	private WifiManager mWifiManager;
	private List<Map<String, String>> mWifiInfos = new ArrayList<Map<String, String>>();

	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			refreshView();
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		mListView = (ListView) findViewById(R.id.list);
		mTextView = (TextView) findViewById(R.id.empty_tv);

		mWifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);
		getConnectionInfo();
		// syncScanWifi();
		printApplicationInfo();
	}

	private void printApplicationInfo() {
		Log.d("LOG", "dataDir:" + getApplicationInfo().dataDir);
		Log.d("LOG", "sourceDir:" + getApplicationInfo().sourceDir);
		Log.d("LOG", "Context path:" + getFilesDir().getAbsolutePath());
		Log.d("LOG", "backupAgentName:" + getApplicationInfo().backupAgentName);
		Log.d("LOG", "className:" + getApplicationInfo().className);
		Log.d("LOG", "descriptionRes:" + getApplicationInfo().descriptionRes);
		Log.d("LOG", "name:" + getApplicationInfo().name);
		Log.d("LOG", "nativeLibraryDir:"
				+ getApplicationInfo().nativeLibraryDir);
		Log.d("LOG", "packageName:" + getApplicationInfo().packageName);
		Log.d("LOG", "permission:" + getApplicationInfo().permission);
		Log.d("LOG", "processName:" + getApplicationInfo().processName);
		Log.d("LOG", "publicSourceDir:" + getApplicationInfo().publicSourceDir);
		Log.d("LOG", "taskAffinity:" + getApplicationInfo().taskAffinity);

		Display display = getWindowManager().getDefaultDisplay();
		if (display != null) {
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getMetrics(outMetrics);
			if (outMetrics != null) {
				Log.d("LOG", "outMetrics.density:" + outMetrics.density);
				Log.d("LOG", "outMetrics.densityDpi:" + outMetrics.densityDpi);
				Log.d("LOG", "outMetrics.heightPixels:"
						+ outMetrics.heightPixels);
				Log.d("LOG", "outMetrics.scaledDensity:"
						+ outMetrics.scaledDensity);
				Log.d("LOG", "outMetrics.widthPixels:" + outMetrics.widthPixels);
				Log.d("LOG", "outMetrics.xdpi:" + outMetrics.xdpi);
				Log.d("LOG", "outMetrics.ydpi:" + outMetrics.ydpi);
			}
		}

		String mStrAndroidId = android.provider.Settings.Secure.getString(
				getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		// mTextView.setText(mStrAndroidId);
	}

	private boolean checkWifiState() {
		int wifiState = mWifiManager.getWifiState();
		if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
			return true;
		}
		return false;
	}

	private Thread mThread;

	private void syncScanWifi() {
		if (mThread != null && mThread.isAlive()) {
			mThread = null;
		}

		mThread = new Thread(new Runnable() {

			@Override
			public void run() {
				mScanWifiTaskRunnable.run();
			}
		});
		mThread.start();
	}

	private void getConnectionInfo() {
		WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
		String txt = "";
		if (mWifiInfo != null) {
			String ssid = mWifiInfo.getSSID();
			String bssid = mWifiInfo.getBSSID();
			// mTextView.setText("WiFi名称:" + ssid + "\n" + "Mac地址:" + bssid);
			txt = "WIFI:" + ssid + "\n" + "MAC:" + bssid;
		} else {
			// mTextView.setText("");
			txt = "当前没有连接WiFi";
		}
		txt = txt.replaceAll("：", ":");
		mTextView.setText(txt);
	}

	private void scanWifi() {
		if (!checkWifiState()) {
			return;
		}

		mWifiManager.startScan();
		List<ScanResult> mScanResults = mWifiManager.getScanResults();
		if (mScanResults != null) {
			mWifiInfos.clear();
			for (ScanResult sr : mScanResults) {
				if (sr != null) {
					Map<String, String> info = new HashMap<String, String>();
					info.put("BSSID", sr.BSSID);
					info.put("SSID", sr.SSID);
					mWifiInfos.add(info);
				}
			}

			Message.obtain(mHandler).sendToTarget();
		}
	}

	Runnable mScanWifiTaskRunnable = new Runnable() {

		@Override
		public void run() {
			scanWifi();
		}
	};

	private void refreshView() {
		if (mWifiInfos.size() < 1) {
			mTextView.setText("没有WiFi");
			mTextView.setVisibility(View.VISIBLE);
		} else {
			mTextView.setVisibility(View.GONE);
		}

		if (mAdapter == null) {
			mAdapter = new WifiAdapter();
			mListView.setAdapter(mAdapter);
		}

		mAdapter.notifyDataSetChanged();
	}

	private class WifiAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mWifiInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return mWifiInfos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(MainActivity.this).inflate(
						R.layout.wifi_item_layout, null);
				vh = new ViewHolder();
				vh.mSSIDTv = (TextView) convertView.findViewById(R.id.ssid);
				vh.mBSSIDTv = (TextView) convertView.findViewById(R.id.bssid);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}

			Map<String, String> wifi = (Map<String, String>) getItem(position);
			if (wifi == null) {
				return convertView;
			}

			String ssid = wifi.get("SSID");
			String bssid = wifi.get("BSSID");
			vh.mSSIDTv.setText("SSID:" + ssid);
			vh.mBSSIDTv.setText("BSSID:" + bssid);

			return convertView;
		}

		class ViewHolder {
			TextView mSSIDTv;
			TextView mBSSIDTv;
		}
	}

	// @Override
	// protected void onStart() {
	// super.onStart();
	// register();
	// }
	//
	// @Override
	// protected void onDestroy() {
	// unRegister();
	// super.onDestroy();
	// }

	// private WifiReceiver mWifiReceiver;
	//
	// private void register() {
	// mWifiReceiver = new WifiReceiver();
	// IntentFilter filter = new IntentFilter();
	// filter.addAction("android.net.wifi.RSSI_CHANGED");
	// filter.addAction("android.net.wifi.STATE_CHANGE");
	// filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
	// registerReceiver(mWifiReceiver, filter);
	// }
	//
	// private void unRegister() {
	// unregisterReceiver(mWifiReceiver);
	// }

	// private class WifiReceiver extends BroadcastReceiver {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String action = intent.getAction();
	// if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
	// getConnectionInfo();
	// }
	// }
	// }
}