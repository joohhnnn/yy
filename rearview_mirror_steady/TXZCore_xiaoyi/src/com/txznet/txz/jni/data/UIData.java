package com.txznet.txz.jni.data;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;

import com.google.protobuf.nano.MessageNano;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.ui.data.UiData;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.version.TXZVersion;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.camera.CameraManager;
import com.txznet.txz.module.contact.ContactManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.version.VersionManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.SDCardUtil;
import com.txznet.txz.util.SystemInfo;
import com.txznet.txz.util.TelephonyInfo;

/**
 * UI数据获取类，供native调用同步获取ui侧的数据
 * 
 * @author User
 *
 */
public class UIData {
	public static byte[] getUIData(int dataId, byte[] param) {
		switch (dataId) {
		case com.txz.ui.data.UiData.DATA_ID_CHECK_APP_EXIST: {
			if (PackageManager.getInstance().checkAppExist(new String(param))) {
				return "true".getBytes();
			}
			return null;
		}
		case com.txz.ui.data.UiData.DATA_ID_APP_INFO_LIST:
			try {
				return MessageNano
						.toByteArray(com.txznet.txz.module.app.PackageManager
								.getInstance().getAppList());
			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}
		case com.txz.ui.data.UiData.DATA_ID_TXZ_APP_VERSION_LIST:
			try {
				return MessageNano
						.toByteArray(com.txznet.txz.module.app.PackageManager
								.getInstance().getTxzAppList());
			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}
		case com.txz.ui.data.UiData.DATA_ID_APP_INFO:
			try {
				return MessageNano
						.toByteArray(com.txznet.txz.module.app.PackageManager
								.getInstance().getAppInfo(new String(param)));
			} catch (Exception e1) {
				e1.printStackTrace();
				return null;
			}
		case com.txz.ui.data.UiData.DATA_ID_SYSTEM_MEM_INFO_STR:
			return SystemInfo.getMemInfo().getBytes();
		case com.txz.ui.data.UiData.DATA_ID_UI_VERSION_INFO: {
			UiEvent.VersionInfo versionInfo = new UiEvent.VersionInfo();
			try {
				versionInfo.strUserVersion = VersionManager.getInstance()
						.getUserVersionNumber();

				versionInfo.strProjectName = VersionManager.getInstance()
						.getProjectName();

				versionInfo.strPackageName = GlobalContext.get()
						.getPackageName();

				return MessageNano.toByteArray(versionInfo);
			} catch (Exception e) {
				return null;
			}
		}
		case com.txz.ui.data.UiData.DATA_ID_VERSION_UI_INNER: {
			return ("UI_" + TXZVersion.PACKTIME + "_" + TXZVersion.COMPUTER
					+ "_" + TXZVersion.SVNVERSION).getBytes();
		}
		case com.txz.ui.data.UiData.DATA_ID_SYSTEM_ENV: {
			com.txz.ui.data.UiData.SystemEnv env = new com.txz.ui.data.UiData.SystemEnv();
			env.strPrivatePath = GlobalContext.get().getApplicationInfo().dataDir;
			if (Environment.getExternalStorageState() != null
					&& Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) // 判断sd卡是否存在
			{
				// env.strSdcardPath = Environment.getExternalStorageDirectory()
				// .getAbsolutePath();// 获取跟目录
				env.strSdcardPath = SDCardUtil.getSDCardPath();
				// 增加其他sdcard
				List<com.txz.ui.data.UiData.SDCard> listSDCard = new ArrayList<com.txz.ui.data.UiData.SDCard>();
				com.txz.ui.data.UiData.SDCard sdCard = new com.txz.ui.data.UiData.SDCard();
				sdCard.strPath = env.strSdcardPath;
				sdCard.uint64TotalSize = SystemInfo
						.getSDTotalSize(env.strSdcardPath);
				sdCard.uint64AvailableSize = SystemInfo
						.getSDAvailableSize(env.strSdcardPath);
				listSDCard.add(sdCard);

				try {

					StorageManager sm = (StorageManager) GlobalContext.get()
							.getSystemService(Context.STORAGE_SERVICE);

					// 获取sdcard的路径：外置和内置
					Method methodGetPaths = sm.getClass().getMethod(
							"getVolumePaths");
					String[] paths = (String[]) methodGetPaths.invoke(sm);

					for (int i = 0; i < paths.length; ++i) {

						if (env.strSdcardPath.equals(paths[i]))
							continue;
						sdCard = new com.txz.ui.data.UiData.SDCard();
						sdCard.strPath = paths[i];
						sdCard.uint64TotalSize = SystemInfo
								.getSDTotalSize(paths[i]);
						sdCard.uint64AvailableSize = SystemInfo
								.getSDAvailableSize(paths[i]);
						listSDCard.add(sdCard);

					}

				} catch (NoSuchMethodException ex) {
					ex.printStackTrace();
				} catch (IllegalArgumentException ex) {
					ex.printStackTrace();
				} catch (IllegalAccessException ex) {
					ex.printStackTrace();
				} catch (InvocationTargetException ex) {
					ex.printStackTrace();
				}

				env.rptSdcardList = (com.txz.ui.data.UiData.SDCard[]) listSDCard
						.toArray(new com.txz.ui.data.UiData.SDCard[0]);
			}
			env.uint32NetworkStatus = NetworkManager.getInstance().getNetType();

			TelephonyInfo telephonyInfo = TelephonyInfo.getInstance();
			env.strImei = DeviceInfo.getIMEI();
			env.strDeviceSn = DeviceInfo.getDeviceSerialNumber();
			env.strImsi1 = telephonyInfo.getIMSI1();
			env.strImsi2 = telephonyInfo.getIMSI2();
			env.strApkSourcePath = GlobalContext.get().getApplicationInfo().sourceDir;
			env.strVendor = Build.MANUFACTURER;
			env.strModel = Build.MODEL;
			env.strBluetoothMacAddr = DeviceInfo.getBluetoothMacAddress();
			env.strBuildSerial = DeviceInfo.getBuildSerialNumber();
			env.strCpuSerial = DeviceInfo.getCPUSerialNumber();
			env.strWifiMacAddr = DeviceInfo.getWifiMacAddress();
			env.strAndroidId = DeviceInfo.getAndroidId();
			env.strUuid = DeviceInfo.getUUID();
			env.strNoFormatPath = DeviceInfo.getNeverFormatRoot();

			WifiInfo wifiInfo = DeviceInfo.getCurrentWifiConnectionInfo();
			if (null != wifiInfo) {
				env.strConnectedWifiAddr = wifiInfo.getBSSID();
				env.strConnectedWifiSsid = wifiInfo.getSSID();
			}

			return MessageNano.toByteArray(env);
		}
		case UiData.DATA_ID_CHECK_SENCE_DISABLE_REASON: {
			String type = new String(param);
			if (type.equals("call")) {
				return CallManager.getInstance().getDisableResaon().getBytes();
			}
			if (type.equals("music")) {
				return MusicManager.getInstance().getDisableResaon().getBytes();
			}
			if (type.equals("nav")) {
				return NavManager.getInstance().getDisableResaon().getBytes();
			}
			return null;
		}
		case UiData.DATA_ID_REMOTE_SENCE_WAKEUP: {
			return SenceManager.getInstance()
					.procSenceByRemote("wakeup", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_SET_USER_WAKEUP_KEYWORDS: {
			return SenceManager.getInstance().procSenceByRemote(
					"set_user_wakeup_keywords", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_COMMAND: {
			return SenceManager.getInstance().procSenceByRemote("command",
					param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_APP: {
			return SenceManager.getInstance().procSenceByRemote("app", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_CALL: {
			return SenceManager.getInstance().procSenceByRemote("call", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_NAV: {
			return SenceManager.getInstance().procSenceByRemote("nav", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_POI_CHOICE: {
			return SenceManager.getInstance().procSenceByRemote("poi_choice",
					param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_MUSIC: {
			return SenceManager.getInstance().procSenceByRemote("music", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_WEATHER: {
			return SenceManager.getInstance().procSenceByRemote("weather",
					param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_STOCK: {
			return SenceManager.getInstance().procSenceByRemote("stock", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_LOCATION: {
			return SenceManager.getInstance().procSenceByRemote("location",
					param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_TRAFFIC: {
			return SenceManager.getInstance().procSenceByRemote("traffic",
					param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_LIMITE_NUMBER: {
			return SenceManager.getInstance().procSenceByRemote("limit_number",
					param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_UNKNOW: {
			return SenceManager.getInstance()
					.procSenceByRemote("unknow", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_UNSUPPORT: {
			return SenceManager.getInstance().procSenceByRemote("unsupport",
					param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_EMPTY: {
			return SenceManager.getInstance().procSenceByRemote("empty", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_HELP: {
			return SenceManager.getInstance().procSenceByRemote("help", param);
		}
		case UiData.DATA_ID_REMOTE_SENCE_AUDIO: {
			return SenceManager.getInstance().procSenceByRemote("audio", param);
		}
		case UiData.DATA_ID_REMOTE_PROC_TOOL: {
			String type = new String(param);
			if (type.equals("call")) {
				if (CallManager.getInstance().hasRemoteProcTool())
					return "true".getBytes();
				return null;
			}
			if (type.equals("nav")) {
				if (NavManager.getInstance().hasRemoteProcTool())
					return "true".getBytes();
				return null;
			}
			if (type.equals("music")) {
				if (MusicManager.getInstance().hasRemoteProcTool())
					return "true".getBytes();
				return null;
			}
			if (type.equals("camera")) {
				if (CameraManager.getInstance().hasRemoteProcTool())
					return "true".getBytes();
				return null;
			}
			if (type.equals("audio")) {
				if (com.txznet.txz.module.audio.AudioManager.getInstance().isAudioToolSet() 
						|| com.txznet.txz.module.audio.AudioManager.getInstance().hasRemoteTool())
					return "true".getBytes();
				return null;
			}
			return null;
		}
		case UiData.DATA_ID_ENABLE_WAKEUP: {
			return ("" + WakeupManager.getInstance().mEnableWakeup).getBytes();
		}
		case UiData.DATA_ID_ENABLE_CHANGE_WAKEUP_KEYWORDS: {
			// return "false".getBytes();
			return ("" + WakeupManager.getInstance().mEnableChangeWakeupKeywords)
					.getBytes();
		}
		case UiData.DATA_ID_ENABLE_SERVICE_CONTACTS: {
			return ("" + ContactManager.getInstance().mEnableServiceContact)
					.getBytes();
		}
		case UiData.DATA_ID_SYSTEM_REPORT_INFO: {
			EquipmentManager.RegisterReportInfo info = new EquipmentManager.RegisterReportInfo();
			info.strManufacturer = getStringBytes(DeviceInfo.getManufacturer());
			info.strModel = getStringBytes(DeviceInfo.getModel());
			info.strCpuabi = getStringBytes(DeviceInfo.getCPUABI());
			info.strKernelVersion = getStringBytes(DeviceInfo
					.getKernelVersion());
			info.strOsVersion = getStringBytes(DeviceInfo.getOsInfo());
			info.strSimCountryIso = getStringBytes(DeviceInfo
					.getSimCountryIso());
			info.strSimOperator = getStringBytes(DeviceInfo.getSimOperator());
			info.strSimSerialNumber = getStringBytes(DeviceInfo
					.getSimSerialNumber());
			info.strLineNumber = getStringBytes(DeviceInfo.getLine1Number());
			info.uint32Height = DeviceInfo.getScreenHeight();
			info.uint32Width = DeviceInfo.getScreenWidth();
			info.uint64MemSize = DeviceInfo.getMemSize();
			info.uint64RomSize = DeviceInfo.getRomSize();
			info.uint64SdcardSize = DeviceInfo.getSdcardSize();
			info.rptMsgCpus = DeviceInfo.getCpuInfo();
			return MessageNano.toByteArray(info);
		}
		}

		return null;
	}

	private static byte[] getStringBytes(String s) {
		if (s == null)
			return null;
		return s.getBytes();
	}
}
