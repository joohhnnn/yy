package com.txznet.txz.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.txz.equipment_manager.EquipmentManager;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;

public class DeviceInfo {

	static String mStrWifiMacAddress;

	/**
	 * 获取wifi的mac地址
	 * 
	 * @return
	 */
	public static String getWifiMacAddress() {
		if (mStrWifiMacAddress == null) {
			WifiManager wifi = (WifiManager) GlobalContext.get()
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = (null == wifi ? null : wifi.getConnectionInfo());
			// if (!wifi.isWifiEnabled()) {
			// // 必须先打开，才能获取到MAC地址
			// wifi.setWifiEnabled(true);
			// wifi.setWifiEnabled(false);
			// }
			if (null != info) {
				mStrWifiMacAddress = info.getMacAddress();
				LogUtil.logd("getWifiMacAddress device info result: "
						+ mStrWifiMacAddress);
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
			try {
				BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
				if (null != adapter) {
					mStrBluetoothMacAddress = adapter.getAddress();
					LogUtil.logd("getBluetoothMacAddress device info result: "
							+ mStrBluetoothMacAddress);
				}
			}catch (Exception e) {
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
			try {
				mStrIMEI = ((TelephonyManager) GlobalContext.get()
						.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
				LogUtil.logd("getIMEI device info result: " + mStrIMEI);
			} catch (Exception e) {
				LogUtil.loge("getIMEI failed", e);
			}
		}
		return mStrIMEI;
	}

	static String mStrCPUSerialNumber;
	static boolean mNeedGetCPUSerialNumber = true;

	/**
	 * 获取cpu序列号
	 * 
	 * @return
	 */
	public static String getCPUSerialNumber() {
		if (mNeedGetCPUSerialNumber) {
			synchronized (DeviceInfo.class) {
				if (mNeedGetCPUSerialNumber) {
					InputStreamReader ir = null;
					LineNumberReader input = null;
					LogUtil.logw("getCPUSerialNumber device info enter");
					try {
						Process pp = Runtime.getRuntime().exec(
								"/system/bin/cat /proc/cpuinfo");
						ir = new InputStreamReader(pp.getInputStream());
						input = new LineNumberReader(ir);
						String str;
						do {
							str = input.readLine();
							if (str == null) {
								LogUtil.logw("getCPUSerialNumber device info failed");
								break;
							}
							String[] ss = str.split("\\:");
							if (ss.length != 2)
								continue;
							String key = ss[0].trim();
							if (key.equals("Serial")) {
								mStrCPUSerialNumber = ss[1].trim();
								LogUtil.logd("getCPUSerialNumber device info result: "
										+ mStrCPUSerialNumber);
								break;
							}
						} while (null != str);
						mNeedGetCPUSerialNumber = false;
					} catch (Exception e) {
						LogUtil.loge("getCPUSerialNumber device info exception: "
								+ e.getClass() + "-" + e.getMessage());
					} finally {
						if (input != null) {
							try {
								input.close();
							} catch (IOException e) {
							}
						}
						if (ir != null) {
							try {
								ir.close();
							} catch (IOException e) {
							}
						}
					}
				}
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
		LogUtil.logd("getBuildSerialNumber device info result: " + Build.SERIAL);
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
			mStrAndroidId = android.provider.Settings.Secure.getString(
					GlobalContext.get().getContentResolver(),
					android.provider.Settings.Secure.ANDROID_ID);
			LogUtil.logd("getAndroidId device info result: " + mStrAndroidId);
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
						LogUtil.logd("getDeviceSerialNumber device info result: "
								+ mStrDeviceSerialNumber);
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
		WifiManager wifiMgr = (WifiManager) GlobalContext.get()
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiMgr.getWifiState() != WifiManager.WIFI_STATE_ENABLED)
			return null;
		WifiInfo info = wifiMgr.getConnectionInfo();
		if (info != null) {
			LogUtil.logd("getCurrentWifiConnectionInfo device info result: "
					+ info);
		}
		return info;
	}
	
	private static Object getObjectField(Class<?> clazz, String name, Object obj) {
		try {
			Field f = clazz.getDeclaredField(name);
			f.setAccessible(true);
			return f.get(obj);
		} catch (Exception e) {
			return null;
		}
	}
	
	private static Integer getObjectIntField(Class<?> clazz, String name, Object obj) {
		try {
			return (Integer)getObjectField(clazz, name, obj);
		} catch (Exception e) {
			return null;
		}
	}

	// 获取当前Wifi列表信息
	public static EquipmentManager.TraceWifiInfoList getWifiInfo() {
		EquipmentManager.TraceWifiInfoList ret = new EquipmentManager.TraceWifiInfoList();
		try {
			WifiManager wifiMgr = (WifiManager) GlobalContext.get()
					.getSystemService(Context.WIFI_SERVICE);
			if (wifiMgr.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

				List<ScanResult> wifis = wifiMgr.getScanResults();
				if (wifis != null) {
					ret.rptMsgWifiInfo = new EquipmentManager.TraceWifiInfo[wifis
							.size()];
					for (int i = 0; i < wifis.size(); ++i) {
						ret.rptMsgWifiInfo[i] = new EquipmentManager.TraceWifiInfo();
						ScanResult wifi = wifis.get(i);
						if (wifi.BSSID != null) {
							ret.rptMsgWifiInfo[i].strBssid = wifi.BSSID
									.getBytes();
						}
						if (wifi.SSID != null) {
							ret.rptMsgWifiInfo[i].strSsid = wifi.SSID
									.getBytes();
						}
						ret.rptMsgWifiInfo[i].int32Level = wifi.level;
						ret.rptMsgWifiInfo[i].int32Distance = getObjectIntField(ScanResult.class, "distanceCm", wifi);
						if (ret.rptMsgWifiInfo[i].int32Distance != null && ret.rptMsgWifiInfo[i].int32Distance < 0) {
							ret.rptMsgWifiInfo[i].int32Distance = null;
						}
						ret.rptMsgWifiInfo[i].int32DistanceSd = getObjectIntField(ScanResult.class, "distanceSdCm", wifi);
						if (ret.rptMsgWifiInfo[i].int32DistanceSd != null && ret.rptMsgWifiInfo[i].int32DistanceSd < 0) {
							ret.rptMsgWifiInfo[i].int32DistanceSd = null;
						}
						// Log.d("test", "pbhpbh wifi=" + wifi.toString());
					}
				}
				WifiInfo info = wifiMgr.getConnectionInfo();
				if (info != null) {
					ret.msgCurWifi = new EquipmentManager.TraceWifiInfo();
					if (info.getBSSID() != null) {
						ret.msgCurWifi.strBssid = info.getBSSID().getBytes();
					}
					if (info.getSSID() != null) {
						ret.msgCurWifi.strSsid = info.getSSID().getBytes();
					}
					ret.msgCurWifi.int32Level = info.getRssi();
					// Log.d("test", "pbhpbh conn=" + info.toString());
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	// 获取当前基站信息
	public static EquipmentManager.TraceCellInfoList getCellInfo() {
		EquipmentManager.TraceCellInfoList ret = new EquipmentManager.TraceCellInfoList();
		try {
			TelephonyManager manager = (TelephonyManager) GlobalContext.get()
					.getSystemService(Context.TELEPHONY_SERVICE);
			List<NeighboringCellInfo> cells = manager.getNeighboringCellInfo();
			if (cells != null) {
				ret.rptMsgCellInfo = new EquipmentManager.TraceCellInfo[cells
						.size()];
				for (int i = 0; i < cells.size(); ++i) {
					NeighboringCellInfo cell = cells.get(i);
					ret.rptMsgCellInfo[i] = new EquipmentManager.TraceCellInfo();
					ret.rptMsgCellInfo[i].int32Cid = cell.getCid();
					ret.rptMsgCellInfo[i].int32Lac = cell.getLac();
					ret.rptMsgCellInfo[i].int32NetType = cell.getNetworkType();
					ret.rptMsgCellInfo[i].int32Rssi = cell.getRssi();
					ret.rptMsgCellInfo[i].int32Psc = cell.getPsc();
					// Log.d("test", "pbhpbh cell=" + cell.toString());
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	static String mStrUUID = null;

	// 设置UUID
	public static void setUUID(String uuid) {
		LogUtil.logd("setUUID: " + uuid);
		mStrUUID = uuid;
	}

	// 更新UUID
	public static String getUUID() {
		return mStrUUID;
	}
	
	private static byte[] mHardwareParams = null;
	public static void setHardwareParams(byte[] params){
		mHardwareParams = params;
	}
	
	public static byte[] getHardwareParams(){
		return mHardwareParams;
	}
	
	static String mStrNeverFormatRoot = null;

	public static void setNeverFormatRoot(String root) {
		LogUtil.logd("setNeverFormatRoot: " + root);
		mStrNeverFormatRoot = root;
	}

	public static String getNeverFormatRoot() {
		return mStrNeverFormatRoot;
	}

	// 获取制造商
	public static String getManufacturer() {
		return Build.MANUFACTURER;
	}

	// 获取手机型号
	public static String getModel() {
		return Build.MODEL;
	}

	// 获取平台版本号
	public static String getOsInfo() {
		return Build.VERSION.RELEASE + "_" + Build.VERSION.SDK_INT;
	}

	// 获取cpu架构
	public static String getCPUABI() {
		return Build.CPU_ABI;
	}

	// 获取IMSI
	public static String getIMSI() {
		try {
			TelephonyManager tm = (TelephonyManager) GlobalContext.get()
					.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getSubscriberId();
		} catch (Exception e) {
			return null;
		}
	}

	// 返回手机号码，对于GSM网络来说即MSISDN
	public static String getLine1Number() {
		try {
			TelephonyManager tm = (TelephonyManager) GlobalContext.get()
					.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getLine1Number();
		} catch (Exception e) {
			return null;
		}
	}

	// 返回SIM卡提供商的国家代码
	public static String getSimCountryIso() {
		try {
			TelephonyManager tm = (TelephonyManager) GlobalContext.get()
					.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getSimCountryIso();
		} catch (Exception e) {
			return null;
		}
	}

	// 返回MCC+MNC代码 (SIM卡运营商国家代码和运营商网络代码)(IMSI)
	public static String getSimOperator() {
		try {
			TelephonyManager tm = (TelephonyManager) GlobalContext.get()
					.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getSimOperator();
		} catch (Exception e) {
			return null;
		}
	}

	// 返回SIM卡的序列号(IMEI)
	public static String getSimSerialNumber() {
		try {
			TelephonyManager tm = (TelephonyManager) GlobalContext.get()
					.getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getSimSerialNumber();
		} catch (Exception e) {
			return null;
		}
	}

	// 获得屏幕宽度
	public static int getScreenWidth() {
		WindowManager wm = (WindowManager) GlobalContext.get()
				.getSystemService(Context.WINDOW_SERVICE);
		Point outSize = new Point();
		wm.getDefaultDisplay().getSize(outSize);
		return outSize.x;
		// DisplayMetrics outMetrics = new DisplayMetrics();
		// wm.getDefaultDisplay().getMetrics(outMetrics);
		// return outMetrics.widthPixels;
	}

	// 获得屏幕高度
	public static int getScreenHeight() {
		WindowManager wm = (WindowManager) GlobalContext.get()
				.getSystemService(Context.WINDOW_SERVICE);
		Point outSize = new Point();
		wm.getDefaultDisplay().getSize(outSize);
		return outSize.y;
		// DisplayMetrics outMetrics = new DisplayMetrics();
		// wm.getDefaultDisplay().getMetrics(outMetrics);
		// return outMetrics.heightPixels;
	}

	static long mMemSize = -1;

	// 获取内存
	public static long getMemSize() {
		try {
			if (mMemSize == -1) {
				FileReader localFileReader = new FileReader("/proc/meminfo");
				BufferedReader localBufferedReader = new BufferedReader(
						localFileReader, 8192);
				String[] arrayOfString = localBufferedReader.readLine().split(
						"\\s+");
				mMemSize = Long.parseLong(arrayOfString[1]) * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
				localBufferedReader.close();
				localFileReader.close();
			}
			return mMemSize;
		} catch (Exception e) {
		}
		return 0;
	}

	static long mSdcardSize = -1;

	// 获取sdcard空间
	public static long getSdcardSize() {
		try {
			if (mSdcardSize == -1) {
				mSdcardSize = Environment.getExternalStorageDirectory()
						.getTotalSpace();
			}
			return mSdcardSize;
		} catch (Exception e) {
		}
		return 0;
	}

	static long mRomSize = -1;

	// 获取ROM分区空间大小
	public static long getRomSize() {
		try {
			if (mRomSize == -1) {
				mRomSize = Environment.getDataDirectory().getTotalSpace();
			}
			return mRomSize;
		} catch (Exception e) {
		}
		return 0;
	}

	private static String getCommandLineContent(String... cmds) {
		try {
			ProcessBuilder cmd = new ProcessBuilder(cmds);
			Process process = cmd.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			String line = reader.readLine();
			reader.close();
			return line;
		} catch (Exception e) {
			return null;
		}
	}

	private static int getCommandLineContentInt(String... cmds) {
		try {
			return Integer.valueOf(getCommandLineContent(cmds));
		} catch (Exception e) {
			return 0;
		}
	}

	static String mKernelVersion = null;

	public static String getKernelVersion() {
		try {
			if (mKernelVersion == null) {
				mKernelVersion = getCommandLineContent("/system/bin/cat",
						"/proc/version");
			}
			return mKernelVersion;
		} catch (Exception e) {
		}
		return null;
	}

	static EquipmentManager.CpuInfo[] mCpuInfos = null;

	public static EquipmentManager.CpuInfo[] getCpuInfo() {
		try {
			if (mCpuInfos == null) {
				File dir = new File("/sys/devices/system/cpu/");
				// Filter to only list the devices we care about
				File[] files = dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						return pathname.isDirectory()
								&& Pattern.matches("cpu\\d+",
										pathname.getName());
					}
				});

				List<EquipmentManager.CpuInfo> cpus = new ArrayList<EquipmentManager.CpuInfo>();
				for (File f : files) {
					try {
						EquipmentManager.CpuInfo cpu = new EquipmentManager.CpuInfo();
						cpu.uint32CurFreq = getCommandLineContentInt(
								"/system/bin/cat", new File(f,
										"cpufreq/scaling_cur_freq").getPath());
						cpu.uint32MinFreq = getCommandLineContentInt(
								"/system/bin/cat", new File(f,
										"cpufreq/cpuinfo_min_freq").getPath());
						cpu.uint32MaxFreq = getCommandLineContentInt(
								"/system/bin/cat", new File(f,
										"cpufreq/cpuinfo_max_freq").getPath());
						cpus.add(cpu);
					} catch (Exception e) {
					}
				}

				mCpuInfos = cpus.toArray(new EquipmentManager.CpuInfo[cpus
						.size()]);
			}
			return mCpuInfos;
		} catch (Exception e) {
		}
		return null;
	}

	public static String getDefaultUserAgent(Context context) {
		String ua;
		if (Build.VERSION.SDK_INT >= 17) {
			ua = getDefaultUserAgentInner(context);
		} else if (Build.VERSION.SDK_INT >= 16) {
			ua = getUserAgent(context);
		} else {
			try {
				Constructor<WebSettings> constructor = WebSettings.class.getDeclaredConstructor(
						Context.class, WebView.class);
				constructor.setAccessible(true);
				try {
					WebSettings settings = constructor.newInstance(context, null);
					ua = settings.getUserAgentString();
				} finally {
					constructor.setAccessible(false);
				}
			} catch (Exception e) {
				ua = new WebView(context).getSettings().getUserAgentString();
			}
		}
		return ua;
	}

	@SuppressLint("NewApi")
	static String getDefaultUserAgentInner(Context context) {
		return WebSettings.getDefaultUserAgent(context);
	}

	static String getUserAgent(Context context) {
		String userAgent;
		try {
			@SuppressWarnings("unchecked")
			Class<? extends WebSettings> clz = (Class<? extends WebSettings>) Class
					.forName("android.webkit.WebSettingsClassic");
			Class<?> webViewClassicClz = (Class<?>) Class
					.forName("android.webkit.WebViewClassic");
			Constructor<? extends WebSettings> constructor = clz.getDeclaredConstructor(
					Context.class, webViewClassicClz);
			constructor.setAccessible(true);
			try {
				WebSettings settings = constructor.newInstance(context, null);
				userAgent = settings.getUserAgentString();
			} finally {
				constructor.setAccessible(false);
			}
		} catch (Exception e) {
			userAgent = new WebView(context).getSettings().getUserAgentString();
		}
		return userAgent;
	}
}
