package com.txznet.feedback.util;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.MessageDigest;

import com.txznet.feedback.AppLogic;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

public class DeviceInfo {

	static String mStrWifiMacAddress;

	/**
	 * 获取wifi的mac地址
	 * 
	 * @return
	 */
	public static String getWifiMacAddress() {
		if (mStrWifiMacAddress == null) {
			WifiManager wifi = (WifiManager) AppLogic.getApp().getSystemService(
					Context.WIFI_SERVICE);
			WifiInfo info = (null == wifi ? null : wifi.getConnectionInfo());
			if (!wifi.isWifiEnabled()) {
				// 必须先打开，才能获取到MAC地址
				wifi.setWifiEnabled(true);
				wifi.setWifiEnabled(false);
			}
			if (null != info) {
				mStrWifiMacAddress = info.getMacAddress();
			}
		}

		return mStrWifiMacAddress;
	}

	static String mStrBluetoothMacAddress;

	/**
	 * 获取蓝牙MAC地址
	 * 
	 * @return
	 */
	public static String getBluetoothMacAddress() {
		if (mStrBluetoothMacAddress == null) {
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			if (null != adapter) {
				mStrBluetoothMacAddress = adapter.getAddress();
			}
		}
		return mStrBluetoothMacAddress;
	}

	static String mStrIMEI;

	/**
	 * 获取IMEI
	 * 
	 * @return
	 */
	public static String getIMEI() {
		if (mStrIMEI == null || mStrIMEI.isEmpty()) {
			mStrIMEI = ((TelephonyManager) AppLogic.getApp().getSystemService(
					Context.TELEPHONY_SERVICE)).getDeviceId();
		}
		return mStrIMEI;
	}

	static String mStrCPUSerialNumber;

	/**
	 * 获取cpu序列号
	 * 
	 * @return
	 */
	public static String getCPUSerialNumber() {
		if (mStrCPUSerialNumber == null) {
			try {
				Process pp = Runtime.getRuntime().exec("cat /proc/cpuinfo");
				InputStreamReader ir = new InputStreamReader(
						pp.getInputStream());
				LineNumberReader input = new LineNumberReader(ir);

				String str;
				do {
					str = input.readLine();
					if (str == null)
						break;
					String[] ss = str.split("\\:");
					if (ss.length != 2)
						continue;
					String key = ss[0].trim();
					if (key.equals("Serial")) {
						mStrCPUSerialNumber = ss[1].trim();
						break;
					}
				} while (null != str);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return mStrCPUSerialNumber;
	}

	/**
	 * 获取Build.SERIAL
	 * 
	 * @return
	 */
	public static String getBuildSerialNumber() {
		return Build.SERIAL;
	}

	static String mStrAndroidId;

	/**
	 * 获取Android系统Id号
	 * 
	 * @return
	 */
	public static String getAndroidId() {
		if (mStrAndroidId == null) {
			mStrAndroidId = android.provider.Settings.Secure.getString(AppLogic
					.getApp().getContentResolver(),
					android.provider.Settings.Secure.ANDROID_ID);
		}
		return mStrAndroidId;
	}

	static String mStrDeviceSerialNumber;

	/**
	 * 获取设备唯一标志字符串
	 * 
	 * @return
	 */
	public static String getDeviceSerialNumber() {
		if (null == mStrDeviceSerialNumber) {
			synchronized (DeviceInfo.class) {
				if (null == mStrDeviceSerialNumber) {
					StringBuilder strId = new StringBuilder("txz");
					strId.append('\0');
					strId.append(getIMEI());
					strId.append('\0');
					strId.append(getWifiMacAddress());
					strId.append('\0');
					strId.append(getCPUSerialNumber());

					try {
						MessageDigest mdInst = MessageDigest.getInstance("MD5");
						byte[] md5Bytes = mdInst.digest(strId.toString()
								.getBytes());
						StringBuilder hexValue = new StringBuilder();
						for (int i = 0; i < md5Bytes.length; i++) {
							int val = ((int) md5Bytes[i]) & 0xff;
							if (val < 16)
								hexValue.append("0");
							hexValue.append(Integer.toHexString(val));
						}
						mStrDeviceSerialNumber = hexValue.toString();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		return mStrDeviceSerialNumber;
	}

	// 获取当前连接的Wifi信息
	public static WifiInfo getCurrentWifiConnectionInfo() {
		WifiManager wifiMgr = (WifiManager) AppLogic.getApp().getSystemService(
				Context.WIFI_SERVICE);
		if (wifiMgr.getWifiState() != WifiManager.WIFI_STATE_ENABLED)
			return null;
		WifiInfo info = wifiMgr.getConnectionInfo();
		if (info != null) {
		}
		return info;
	}
}
