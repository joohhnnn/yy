package com.txznet.txz.module.app;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ProgressBar;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.spreada.utils.chinese.ZHConverter;
import com.txz.ui.app.UiApp;
import com.txz.ui.app.UiApp.AppInfo;
import com.txz.ui.app.UiApp.AppInfoList;
import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.PushCmd_NotifyGetApplicationInfo;
import com.txz.ui.equipment.UiEquipment.Req_ReportApplicationInfo;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.ProcessUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.version.ApkVersion;
import com.txznet.comm.version.TXZVersion;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.MediaPriorityManager;
import com.txznet.txz.component.media.chooser.AudioPriorityChooser;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.cmd.CmdManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sys.SysTool;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 包管理模块，管理系统内安装的应用包
 * 
 * @author bihongpi
 *
 */
public class PackageManager extends IModule {
	static PackageManager sIns = new PackageManager();

	public boolean mInstalledRecord = false;
	public boolean mInstalledSetting = false;
	// 是否初始化过中文应用列表
	public boolean mInitedInChinese = false;
	
	public Boolean closeZHConverter = null;

	public static PackageManager getInstance() {
		return sIns;
	}

	private PackageManager() {
		ApkInfo info = new ApkInfo();
		info.packageName = ServiceManager.TXZ;
		info.versionCode = ApkVersion.versionCode;
		info.versionName = ApkVersion.versionName;
		info.sourceDir = GlobalContext.get().getApplicationInfo().sourceDir;
		info.versionCompile = TXZVersion.PACKTIME + "_" + TXZVersion.SVNVERSION;
		info.channelName = ApkVersion.channelName;
		synchronized (mTxzApkInfos) {
			mTxzApkInfos.put(ServiceManager.TXZ, info);
		}
		
		mInstalledRecord = checkAppExist(ServiceManager.RECORD);
		mInstalledSetting = checkAppExist(ServiceManager.SETTING);
		if (mInstalledSetting) {
			ConfigUtil.setShowSettings(true);
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_PACKAGE_ADDED);
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		filter.addDataScheme("package");
		GlobalContext.get().registerReceiver(new MyInstallReceiver(), filter);

		mInitedInChinese = "zh".equals(Locale.getDefault().getLanguage());
	}

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_SYSTEM_APP);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_APP_INFO);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE);
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterInitSuccess() {
		sendAppList();
		return super.initialize_AfterInitSuccess();
	}

	public UiData.AppInfo getAppInfo(String strPackageName) {
		UiData.AppInfo pbAppInfo = new UiData.AppInfo();

		try {
			PackageInfo info = getApkInfo(strPackageName);
			if (info == null || info.applicationInfo == null)
				return null;
			pbAppInfo.strSourcePath = info.applicationInfo.sourceDir;
			pbAppInfo.strVersion = info.versionName;
			return pbAppInfo;
		} catch (Exception e) {
			JNIHelper.loge("get app info failed");
			e.printStackTrace();
			return null;
		}
	}

	public static class ApkInfo {
		public String packageName;
		public int versionCode;
		public String versionName;
		public String versionCompile;
		public String sourceDir;
		public String channelName;
	}

	private final Map<String, ApkInfo> mTxzApkInfos = new HashMap<String, ApkInfo>();

	private final Map<Integer, Set<String>> mAppTypePkgs = new HashMap<Integer, Set<String>>(); // 应用类型和包名的映射

	public void refreshAllTXZVersionInfo() {
		List<PackageInfo> packages = GlobalContext.get().getPackageManager()
				.getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			refreshApkInfo(packageInfo.packageName);
		}
	}

	/**
	 * 重新同步app列表 用于语言环境切换为中文且未在中文环境下同步过代码时
	 */
	public void refreshAppList() {
		sendAppList();
		mInitedInChinese = true;
	}

	private Runnable mRunnableNotifyApk = new Runnable() {
		@Override
		public void run() {
			AppLogic.removeUiGroundCallback(mRunnableNotifyApk);
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
					UiEquipment.SUBEVENT_NOTIFY_APK_VERSIONS);
		}
	};

	public void refreshApkInfo(final String packageName) {
		if (packageName.startsWith("com.txznet.") == false) {
			return;
		}
		if (ServiceManager.TXZ.equals(packageName)) {
			AppLogic.removeUiGroundCallback(mRunnableNotifyApk);
			AppLogic.runOnUiGround(mRunnableNotifyApk, 5000);
			return;
		}
		ServiceManager.getInstance().sendInvoke(packageName,
				"comm.PackageInfo", null, new GetDataCallback() {
					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (data == null) {
							JNIHelper.logw("get package VERSION failed: "
									+ packageName);
							return;
						}
						JSONObject json = data.getJSONObject();
						try {
							if (json == null) {
								JNIHelper.logw("get package VERSION failed: "
										+ packageName
										+ ", try to get from package manager");
								json = new JSONObject();
							} else {
								// 取到了包的版本，删除其他版本的包文件，新增dpk逻辑，由native去删除
//								try {
//									String apkPath = json.optString(
//											"sourceDir", "");
//									if (!apkPath.isEmpty()) {
//										File d = new File(Environment
//												.getExternalStorageDirectory(),
//												"txz/install");
//										final File f = new File(apkPath);
//										File[] fs = d
//												.listFiles(new FileFilter() {
//													@Override
//													public boolean accept(
//															File pathname) {
//														String name = pathname
//																.getName()
//																.toLowerCase();
//														if (!name.startsWith(packageName
//																.toLowerCase()
//																+ "_"))
//															return false;
//														if (!name
//																.endsWith(".apk"))
//															return false;
//
//														if (pathname
//																.getAbsolutePath()
//																.equals(f
//																		.getAbsolutePath()))
//															return false;
//
//														return true;
//													}
//												});
//										if (fs != null) {
//											for (File df : fs) {
//												JNIHelper
//														.logd("remove old apk file: "
//																+ df.getPath());
//
//												df.delete();
//											}
//										}
//									}
//								} catch (Exception e) {
//									JNIHelper
//											.logw("clear old version apk exception: "
//													+ e.getMessage());
//								}
							}
							PackageInfo packInfo = GlobalContext.get()
									.getPackageManager()
									.getPackageInfo(packageName, 0);
							ApkInfo info = new ApkInfo();
							info.packageName = packageName;
							info.versionCode = json.optInt("versionCode",
									packInfo.versionCode);
							info.versionName = json.optString("versionName",
									packInfo.versionName);
							info.sourceDir = json.optString("sourceDir",
									packInfo.applicationInfo.sourceDir);
							info.versionCompile = json
									.optString("versionCompile");
							info.channelName = json.optString("channelName");
							synchronized (mTxzApkInfos) {
								mTxzApkInfos.put(packageName, info);
							}

							// JNIHelper.logd("refreshApk " + packageName
							// + " VERSION: " + info.versionName + "#"
							// + info.sourceDir);

							AppLogic.removeUiGroundCallback(mRunnableNotifyApk);
							AppLogic.runOnUiGround(mRunnableNotifyApk, 5000);
						} catch (Exception e) {
							JNIHelper.loge("refreshApk " + packageName
									+ " exception: " + e.getMessage());
						}
					}
                }, 60 * 1000);
    }

	// 取软件版本号都要从这里取
	public PackageInfo getApkInfo(String packageName) {
		try {
			PackageInfo packInfo = GlobalContext.get().getPackageManager()
					.getPackageInfo(packageName, 0);
			synchronized (mTxzApkInfos) {
				ApkInfo info = mTxzApkInfos.get(packageName);
				if (info != null) {
					packInfo.versionCode = info.versionCode;
					packInfo.versionName = info.versionName;
					packInfo.applicationInfo.sourceDir = info.sourceDir;
				}
			}
			return packInfo;
		} catch (Exception e) {
		}
		return null;
	}

	public UiEquipment.VersionInfo getTxzAppList() {
		UiEquipment.VersionInfo pbVersionInfo = new UiEquipment.VersionInfo();

		ArrayList<UiEquipment.PackageInfo> packageList = new ArrayList<UiEquipment.PackageInfo>();
		synchronized (mTxzApkInfos) {
			JNIHelper.logd("TXZ_VERSION_COUNT: " + mTxzApkInfos.size());
			for (Entry<String, ApkInfo> entry : mTxzApkInfos.entrySet()) {
				if (!checkAppExist(entry.getKey())) {
					continue;
				}
				UiEquipment.PackageInfo tmpInfo = new UiEquipment.PackageInfo();
				tmpInfo.strPackageName = entry.getKey();
				tmpInfo.strPackageVersion = entry.getValue().versionName;
				tmpInfo.strSourceApk = entry.getValue().sourceDir;
				tmpInfo.strCompileVersion = entry.getValue().versionCompile;
				tmpInfo.strChannelName = entry.getValue().channelName;

				packageList.add(tmpInfo);

				JNIHelper.logd(entry.getKey() + " VERSION: "
						+ tmpInfo.strPackageVersion + "#"
						+ tmpInfo.strSourceApk +(" ; CHANNEL : " + ((tmpInfo.strChannelName == null) ? "" : StringUtils.getHideString(tmpInfo.strChannelName,3,3))));
			}
		}
		pbVersionInfo.rptMsgPackageList = packageList
				.toArray(new UiEquipment.PackageInfo[packageList.size()]);
		return pbVersionInfo;
	}

	// public String getApkSourceDir(String strPackageName) {
	//
	// List<PackageInfo> packages = GlobalContext.get().getPackageManager()
	// .getInstalledPackages(0);
	// for (int i = 0; i < packages.size(); i++) {
	// PackageInfo packageInfo = packages.get(i);
	// if (packageInfo.packageName.equals(strPackageName) == true) {
	// return packageInfo.applicationInfo.sourceDir;
	// }
	// }
	// return "";
	// }

	public AppInfoList getAppList() {
		ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
		
		Intent resolveIntent;
		// 尝试查找MAIN+LAUNCHER
		resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		android.content.pm.PackageManager pm = GlobalContext.get()
				.getPackageManager();

		List<ResolveInfo> resolveinfoList = pm.queryIntentActivities(
				resolveIntent, 0);

		StringBuilder tmpAppListInfo = new StringBuilder();

		if (resolveinfoList != null) {
			for (ResolveInfo r : resolveinfoList) {
				if (r == null || r.activityInfo == null) {
					continue;
				}
				AppInfo tmpInfo = new AppInfo();
				if (closeZHConverter == null) {
					HashMap<String, String> config = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_CLOSE_ZH_CONVERTER);
					closeZHConverter = false;
					if (config != null && config.get(TXZFileConfigUtil.KEY_CLOSE_ZH_CONVERTER) != null) {
						try {
							closeZHConverter = Boolean.parseBoolean(config.get(TXZFileConfigUtil.KEY_CLOSE_ZH_CONVERTER));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					JNIHelper.logd("closeZHConverter::" + closeZHConverter);
					if (closeZHConverter == false) {
						//不关闭繁简体转化时，判断当前系统是否是繁体，如果不是繁体就不进行转化
						closeZHConverter = !GlobalContext.get().getResources().getConfiguration().locale.getCountry().equals("TW");
						JNIHelper.logd("closeZHConverter::" + closeZHConverter);
					}
				}
				
				tmpInfo.strAppName = closeZHConverter ? r.loadLabel(pm)
						.toString() : ZHConverter.convert(r.loadLabel(pm)
								.toString(), ZHConverter.SIMPLIFIED);
				tmpInfo.strPackageName = r.activityInfo.packageName;
				appList.add(tmpInfo);

				tmpAppListInfo.append(tmpInfo.strPackageName + "("
						+ tmpInfo.strAppName + "); ");
			}
			
			resolveinfoList.clear();
			
			System.gc();
		}
		JNIHelper.logd("findappList: " + tmpAppListInfo.toString());

		AppInfoList infoList = new AppInfoList();
		infoList.rptMsgApps = appList.toArray(new AppInfo[0]);
		return infoList;
	}

	String trimAppName(String appName) {
		String space = new String(new byte[] { (byte) 0xC2, (byte) 0xA0 });
		return appName.replaceAll(space + "|\\s|　", "");
	}

	public void sendAppList() {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				AppInfoList infoList = SysTool.getSyncAppInfoList() == null ? getAppList()
						: SysTool.getSyncAppInfoList();
				if (infoList != null && infoList.rptMsgApps != null) {
					for (int i = 0; i < infoList.rptMsgApps.length; ++i) {
						infoList.rptMsgApps[i].strAppName = trimAppName(infoList.rptMsgApps[i].strAppName);
					}
					JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_APP,
							UiApp.SUBEVENT_UPDATED_APP_LIST, infoList);
				}
			}
		}, 0);
	}
	/**
	 * 根据应用名获取app的信息
	 * @return
	 */
	public AppInfo getUIAppInfo(String strAppName){
		AppInfo appInfo = new AppInfo();
		AppInfoList infoList = SysTool.getSyncAppInfoList() == null ? getAppList()
				: SysTool.getSyncAppInfoList();
		if (infoList != null && infoList.rptMsgApps != null && infoList.rptMsgApps.length>0) {
			for (int i = 0; i < infoList.rptMsgApps.length; i++) {
				if(infoList.rptMsgApps[i].strAppName != null && infoList.rptMsgApps[i].strAppName.equals(strAppName)){
					appInfo = infoList.rptMsgApps[i];
					return appInfo;
				}
			}
		}
		return null;
	}
	@Override
	public int initialize_addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("txz.package.", new CommandProcessor() {
			
			@Override
			public Object invoke(String command, Object[] args) {
				try {
					if("getUIAppInfo".equals(command)){
						if(!(args[0] instanceof String)){
							return null;
						}
						String strAppName = (String) args[0];
						return getUIAppInfo(strAppName);
					}
				} catch (Exception e) {
				}
				return null;
			}
		});
		return super.initialize_addPluginCommandProcessor();
	}

	public void onAppAdded(String packageName) {
		if (packageName.endsWith(ServiceManager.RECORD)) {
			mInstalledRecord = true;
		}else if (packageName.endsWith(ServiceManager.SETTING)) {
			mInstalledSetting = true;
			if (mInstalledSetting) {
				CmdManager.getInstance().addSettingCmd();
				ConfigUtil.setShowSettings(true);
			}
		}
	}

	public void onAppRemoved(String packageName) {
		if (packageName.endsWith(ServiceManager.RECORD)) {
			mInstalledRecord = false;
		}else if (packageName.endsWith(ServiceManager.SETTING)) {
			mInstalledSetting = false;
			if (TextUtils.isEmpty(ProjectCfg.getSDKSettingPackage())) {
				ConfigUtil.setShowSettings(false);
			}
		}
		synchronized (mTxzApkInfos) {
			mTxzApkInfos.remove(packageName);
		}
	}
	/**
	 * 判断清单文件Application中是否包含某种meta
	 * @param packageName
	 * @param key
	 * @return
	 */
	public boolean manifestHasMeta(String packageName,String key) {
		Bundle bundle = null;
		try {
			ApplicationInfo ai = GlobalContext.get().getPackageManager()
					.getApplicationInfo(packageName, android.content.pm.PackageManager.GET_META_DATA);
			bundle = ai.metaData;
		} catch (NameNotFoundException e) {
			JNIHelper.logd("manifestHasMeta ApplicationInfo");
		}
		if(bundle!=null && bundle.containsKey(key)){
			JNIHelper.logd("manifestHasMeta return true. packageName:"+packageName+" key:"+key);
			return true;
		}
		JNIHelper.logd("manifestHasMeta return false. packageName:"+packageName+" key:"+key);
		return false;
	}
	
	

	public boolean checkAppExist(String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = GlobalContext
					.get()
					.getPackageManager()
					.getApplicationInfo(
							packageName,
							android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES);
			if (info != null)
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	public boolean checkBluetoothModulerExist() {
		return checkAppExist("com.daxun.bluetooth.activities")
				|| checkAppExist("com.txznet.bluetooth");
	}

	private boolean isLaucherApp(String packageName) {
		Intent resolveIntent;

		// 尝试查找MAIN+LAUNCHER
		resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageName);

		List<ResolveInfo> resolveinfoList = GlobalContext.get()
				.getPackageManager().queryIntentActivities(resolveIntent, 0);

		if (resolveinfoList == null || resolveinfoList.isEmpty()) {
			return false;
		}
		return true;
	}

	private boolean startAppByActivityIntent(Intent resolveIntent) {
		// 通过getPackageManager()的queryIntentActivities方法遍历
		try {
			List<ResolveInfo> resolveinfoList = GlobalContext.get()
					.getPackageManager()
					.queryIntentActivities(resolveIntent, 0);
			if (resolveinfoList.isEmpty())
				return false;
			ResolveInfo resolveinfo = resolveinfoList.iterator().next();
			if (resolveinfo == null)
				return false;
			// packagename = 参数packname
			String packageName = resolveinfo.activityInfo.packageName;
			// 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
			String className = resolveinfo.activityInfo.name;

			resolveIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			// 设置ComponentName参数1:packagename参数2:MainActivity路径
			ComponentName cn = new ComponentName(packageName, className);

			resolveIntent.setComponent(cn);
			GlobalContext.get().startActivity(resolveIntent);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static final String KEY_START_FROM_WHERE = "com.txznet.txz:startFromWhere";

	public void openApp(String packageName, String name) {
		openApp(packageName, name, false, null);
	}

	public void openApp(String packageName, String name, String startFromWhere) {
		openApp(packageName, name, false, startFromWhere);
	}


	public void openApp(String packageName, String name, boolean forceInner, String startFromWhere) {
		// 如果有远程注册的打开应用工具，则让远程注册工具处理
		if (SysTool.procByRemoteTool(SysTool.APP_MGR, "openApp", packageName) && !forceInner) {
			JNIHelper.logd("openApp " + packageName + "by remoute");
			return;
		}

		JNIHelper.logd("openApp:" + packageName);

		try {
			// 默认按getLaunchIntentForPackage方式启动
			Intent in = GlobalContext.get().getPackageManager()
					.getLaunchIntentForPackage(packageName);
			if (in != null) {
				in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				if (!TextUtils.isEmpty(startFromWhere)) {
					in.putExtra(KEY_START_FROM_WHERE, startFromWhere);
				}
				GlobalContext.get().startActivity(in);
				JNIHelper.logd("openApp by getLaunchIntentForPackage");
				reportApp(true, packageName, name);
				return;
			}

			Intent resolveIntent;

			// 尝试查找MAIN+LAUNCHER
			resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(packageName);
			if (!TextUtils.isEmpty(startFromWhere)) {
				resolveIntent.putExtra(KEY_START_FROM_WHERE, startFromWhere);
			}
			if (startAppByActivityIntent(resolveIntent)) {
				JNIHelper.logd("openApp by MAIN+LAUNCHER");
				reportApp(true, packageName, name);
				return;
			}

			// 尝试查找MAIN
			resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.setPackage(packageName);
			if (!TextUtils.isEmpty(startFromWhere)) {
				resolveIntent.putExtra(KEY_START_FROM_WHERE, startFromWhere);
			}
			if (startAppByActivityIntent(resolveIntent)) {
				JNIHelper.logd("openApp by MAIN");
				reportApp(true, packageName, name);
				return;
			}

			// 尝试查找LAUNCHER
			resolveIntent = new Intent();
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(packageName);
			if (!TextUtils.isEmpty(startFromWhere)) {
				resolveIntent.putExtra(KEY_START_FROM_WHERE, startFromWhere);
			}
			if (startAppByActivityIntent(resolveIntent)) {
				JNIHelper.logd("openApp by LAUNCHER");
				reportApp(true, packageName, name);
				return;
			}

			// 尝试启动第一个Activity
			try {
				PackageInfo packageinfo = GlobalContext.get()
						.getPackageManager().getPackageInfo(packageName, 0);
				if (packageinfo.activities.length > 0) {
					ComponentName cn = new ComponentName(packageName,
							packageinfo.activities[0].name);
					resolveIntent = new Intent();
					resolveIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					resolveIntent.setComponent(cn);
					if (!TextUtils.isEmpty(startFromWhere)) {
						resolveIntent.putExtra(KEY_START_FROM_WHERE, startFromWhere);
					}
					GlobalContext.get().startActivity(resolveIntent);
					JNIHelper.logd("openApp by last method");
					reportApp(true, packageName, name);
					return;
				}
			} catch (Exception e) {
			}
		} catch (Exception e) {
		}
		reportApp(false, packageName, name);
		String spk = NativeData.getResString("RS_APP_OPEN_FAIL");
		TtsManager.getInstance().speakText(spk);
	}

	public void openApp(String packageName) {
		openApp(packageName, "");
	}

	private void reportApp(boolean bOpen, String packageName, String name) {
		ReportUtil.doReport(new ReportUtil.Report.Builder().setSessionId().setType("app")
                .setAction("open").putExtra("bopen", bOpen)
				.putExtra("package", packageName).putExtra("name", name).buildCommReport());
	}

	public void closeApp(final String packageName) {
		JNIHelper.logd("closeApp:" + packageName);
		// 如果有远程注册的关闭应用工具，则让远程注册工具处理
		if (SysTool.procByRemoteTool(SysTool.APP_MGR, "closeApp", packageName)) {
			return;
		}
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("app").setSessionId()
				.setAction("close").buildCommReport());
		if (ProcessUtil.isForeground(packageName)) {
			returnHome();
		}

		if (packageName.startsWith("com.txznet.")) {
			ServiceManager.getInstance().sendInvoke(packageName,
					"comm.closeApp", null, null);
			return;
		}

		Runnable r = new Runnable() {
			int n = 0;

			@Override
			public void run() {
				boolean running = isAppRunning(packageName);
				JNIHelper.logd("killBackgroundProcesses:run=" + running
						+ ",count=" + n + ",pack=" + packageName);
				ActivityManager am = (ActivityManager) GlobalContext.get()
						.getSystemService(Context.ACTIVITY_SERVICE);
				try {
					am.killBackgroundProcesses(packageName);
				} catch (Exception e) {
				}
				if (running) {
					try {
						n++;
						am.killBackgroundProcesses(packageName);
					} catch (Exception e) {
					}

					if (n < 50)
						AppLogic.runOnUiGround(this, 100);
					return;
				}

				// 2秒后再检查次
				if (n > 0) {
					n = 0;
					AppLogic.runOnUiGround(this, 2000);
				}
			}
		};
		AppLogic.runOnUiGround(r, 0);
	}

	public boolean isAppRunning(String packageName) {
		try {
			ActivityManager am = (ActivityManager) GlobalContext.get()
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
			for (RunningAppProcessInfo rapi : infos) {
				if (rapi.processName.equals(packageName))
					return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_SYSTEM_APP:

			switch (subEventId) {
			case UiApp.SUBEVENT_OPEN_APP:
				try {
					final AppInfo appInfo = AppInfo.parseFrom(data);
					JNIHelper.logd("launch app:" + appInfo.strPackageName);
					//String spk = NativeData.getResPlaceholderString(
					//		"RS_APP_WILL_OPEN", "%APP%", appInfo.strAppName);
					String spk = NativeData.getResString("RS_VOICE_MEDIA_CONTROL_CONFIRM");
					RecorderWin.speakTextWithClose(spk, new Runnable() {
						@Override
						public void run() {
							openApp(appInfo.strPackageName, appInfo.strAppName);
						}
					});
					
					if(NetworkManager.getInstance().checkLeastFlow()){
						if(!TextUtils.equals("设置", appInfo.strAppName) 
								&& !TextUtils.equals("文件管理器", appInfo.strAppName) 
								&& !TextUtils.equals("蓝牙", appInfo.strAppName) 
								&& !TextUtils.equals("行车记录仪", appInfo.strAppName) 
								&& !TextUtils.equals("FM发射", appInfo.strAppName)){
							String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
							TtsManager.getInstance().speakText(resText);
						}
					}
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case UiApp.SUBEVENT_CLOSE_APP: {
				try {
					final AppInfo appInfo = AppInfo.parseFrom(data);
					JNIHelper.logd("exit app:" + appInfo.strPackageName);
					/*String spk = NativeData.getResPlaceholderString(
							"RS_APP_WILL_CLOSE", "%APP%", appInfo.strAppName);*/
					String spk = NativeData.getResString("RS_VOICE_MEDIA_CONTROL_CONFIRM");
					RecorderWin.speakTextWithClose(spk, new Runnable() {
						@Override
						public void run() {
							closeApp(appInfo.strPackageName);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			default:
				break;
			}
			break;

		case UiEvent.EVENT_ACTION_EQUIPMENT:
			switch (subEventId) {
			case UiEquipment.SUBEVENT_NOTIFY_APP_INFO:
				// 后台push的查询指定应用信息请求
				queryPackageInfo(data);
				break;
			case UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE:
				try {
					JNIHelper.logd("notify server config update");
					UiEquipment.ServerConfig pbServerConfig = UiEquipment.ServerConfig.parseFrom(data);
					if (pbServerConfig.rptAppReportList != null) {
						for (UiEquipment.AppReportList report : pbServerConfig.rptAppReportList) {
							byte[][] strAppList = report.strAppList;
							if (report.strAppList != null) {
								synchronized (mAppTypePkgs) {
									for (byte[] pkgBytes : strAppList) {
										if (pkgBytes != null) {
											String pkgName = new String(pkgBytes);
											JNIHelper.logd("AppReportList parse, pkgName= " + pkgName);

											Set<String> pkgs = mAppTypePkgs.get(report.uint32AppType);
											if (pkgs == null) {
												pkgs = new HashSet<String>();
												mAppTypePkgs.put(report.uint32AppType, pkgs);
											}
											pkgs.add(pkgName);

											try {
												PackageInfo packInfo = GlobalContext.get()
														.getPackageManager()
														.getPackageInfo(pkgName, 0);
												ApkInfo info = new ApkInfo();
												info.packageName = pkgName;
												info.versionCode = packInfo.versionCode;
												info.versionName = packInfo.versionName;
												info.sourceDir = packInfo.applicationInfo.sourceDir;
												info.versionCompile = null;
												synchronized (mTxzApkInfos) {
													mTxzApkInfos.put(pkgName, info);
												}
											} catch (NameNotFoundException e) {
												e.printStackTrace();
											}
										}
									}
								}
							}
							AppLogic.removeUiGroundCallback(mRunnableNotifyApk);
							AppLogic.runOnUiGround(mRunnableNotifyApk, 5000);
						}
					}
				} catch (Exception e) {
				}
				break;
			default:
				break;
			}
			break;

		default:
			break;
		}

		return 0;
	}

	/**
	 * 根据后台调用参数查询app信息上报
	 * 
	 * @param data
	 */
	private void queryPackageInfo(final byte[] data) {
		String str = new String(data);

		mGetAppListTask.update(data);
		if (NavAppManager.getInstance().isInit()) {
			AppLogic.runOnSlowGround(mGetAppListTask, 0);
		} else {
			NavAppManager.getInstance().addDelayTask(mGetAppListTask);
		}
	}
	
	Runnable1<byte[]> mGetAppListTask = new Runnable1<byte[]>(null) {

		@Override
		public void run() {
			if (mP1 == null) {
				return;
			}

			// 解析后台传入参数
			PackageInfo[] infoList = getPkgInfoList(mP1);

			if (null == infoList) {
				return;
			}

			// 构建回传参数
			Req_ReportApplicationInfo reqInfo = new Req_ReportApplicationInfo();
			reqInfo.rptMsgApplicationInfo = new UiEquipment.ApplicationInfo[infoList.length];

			int len = infoList.length;
			for (int i = 0; i < len; i++) {
				PackageInfo pkgInfo = infoList[i];

				if (null == pkgInfo) {
					reqInfo.rptMsgApplicationInfo[i] = null;
				} else {
					// 获取包信息
					final String packageName = pkgInfo.packageName;
					final String installPath = pkgInfo.applicationInfo.sourceDir;
					final String version = pkgInfo.versionName + " : "
							+ pkgInfo.versionCode;
					final File file = new File(
							pkgInfo.applicationInfo.sourceDir);
					String md5 = MD5Util.generateMD5(file);

					// 包信息写入回传参数
					UiEquipment.ApplicationInfo info = new UiEquipment.ApplicationInfo();
					info.strPackageName = packageName;
					info.strVersion = version;
					info.strInstallPah = installPath;
					info.strMd5 = md5;

					reqInfo.rptMsgApplicationInfo[i] = info;
				}
			}

			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
					UiEquipment.SUBEVENT_REQ_APP_INFO, reqInfo);

		}
	};

	private PackageInfo[] getPkgInfoList(byte[] data) {

		try {
			// 解析后台传来的参数列表
			PushCmd_NotifyGetApplicationInfo info = PushCmd_NotifyGetApplicationInfo
					.parseFrom(data);
			int infoLength = info.strPackageName.length;
			PackageInfo[] infoList = new PackageInfo[infoLength];

			for (int i = 0; i < infoLength; i++) {
				infoList[i] = getPackageInfo(info.strPackageName[i]);
			}

			return infoList;

		} catch (InvalidProtocolBufferNanoException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static final String PKG_TYPE_NAV = "$nav";
	private static final String PKG_TYPE_MUSIC = "$music";
	private static final String PKG_TYPE_CALL = "$call";
	private static final String PKG_TYPE_AUDIO = "$audio";

	/**
	 * 根据传入参数尝试获取包信息
	 * 
	 * @param pkgName
	 */
	private PackageInfo getPackageInfo(String pkgName) {
		if (StringUtils.isEmpty(pkgName)) {
			return null;
		}
		try {
			String resolvedPkgName = "";

			if (pkgName.startsWith("$")) { // 参数化的包名
				if (pkgName.equals(PKG_TYPE_NAV)) {
					NavThirdApp nta = NavManager.getInstance()
							.getLocalNavImpl();
					if(nta != null){
						resolvedPkgName = nta.getPackageName();
					}
				} else if (pkgName.equals(PKG_TYPE_MUSIC)) {
					IMediaTool tool = MediaPriorityManager.getInstance().getMediaToolWithPriority(
							MediaPriorityManager.PRIORITY_TYPE.MUSIC, null);
					if(tool != null){
						resolvedPkgName = tool.getPackageName();
					}
				} else if (pkgName.equals(PKG_TYPE_AUDIO)) {
                    IMediaTool tool = AudioPriorityChooser.getInstance().getMediaTool(null);
                    if (tool != null) {
                        resolvedPkgName = tool.getPackageName();
                    }
				} else if (pkgName.equals(PKG_TYPE_CALL)) {
					resolvedPkgName = CallManager.getInstance()
							.getPackageName();
				}
			} else { // 实际包名
				resolvedPkgName = pkgName;
			}

			PackageInfo info = GlobalContext.get().getPackageManager()
					.getPackageInfo(resolvedPkgName, 0);
			return info;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public byte[] processInvoke(final String packageName, String command,
			byte[] data) {
		if (command.equals("refresh")) {
			// 通知服务器订阅模块事件
			refreshApkInfo(packageName);
			return null;
		}

		return null;
	}

	/**
	 * 返回桌面
	 */
	public void returnHome() {
		JNIHelper.logd("returnHome");
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		try {
			GlobalContext.get().startActivity(intent);
		} catch (Exception e) {
			LogUtil.loge("返回桌面错误！");
		}
	}
	
	/**
	 * 打开WIFI界面
	 */
	public void goWifiSettings() {
		Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
	}

	/**
	 * 判断当前界面是否是桌面
	 */
	public boolean isHome() {
		boolean ret = false;
		try {
			ActivityManager mActivityManager = (ActivityManager) GlobalContext
					.get().getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> rti = mActivityManager
					.getRunningTasks(1);
			ret = getHomes().contains(rti.get(0).topActivity.getPackageName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		// JNIHelper.logd("isHome=" + ret);
		return ret;
	}

	/**
	 * 获得属于桌面的应用的应用包名称
	 *
	 * @return 返回包含所有包名的字符串列表
	 */
	private static List<String> getHomes() {
		List<String> names = new ArrayList<String>();
		try {
			android.content.pm.PackageManager packageManager = GlobalContext.get()
					.getPackageManager();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(
					intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
			for (ResolveInfo ri : resolveInfo) {
				names.add(ri.activityInfo.packageName);
			}
		} catch (Exception e) {
		}
		return names;
	}
	
	/**
	 * 获取WIFI_SETTINGS界面
	 * @return
	 */
	private static List<String> getWifiSettings() {
		List<String> names = new ArrayList<String>();
		try {
			android.content.pm.PackageManager packageManager = GlobalContext.get().getPackageManager();
			Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
			List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
					android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
			for (ResolveInfo ri : resolveInfo) {
				names.add(ri.activityInfo.packageName);
			}
		} catch (Exception e) {
		}
		return names;
	}
	
	/**
	 * 判断当前是否处于设置界面
	 * @return
	 */
	public boolean isWifiSettings() {
		boolean ret = false;
		try {
			ActivityManager mActivityManager = (ActivityManager) GlobalContext.get()
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
			ret = getWifiSettings().contains(rti.get(0).topActivity.getPackageName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	public int getVerionCode(String appName) {
		try {
			PackageInfo pi = GlobalContext.get().getPackageManager()
					.getPackageInfo(appName, 0);
			int vc = pi.versionCode;
			return vc;
		} catch (Exception e) {
		}
		return -1;
	}
	
	public String getVersionName(String packageName){
		try {
			PackageInfo pi = GlobalContext.get().getPackageManager()
					.getPackageInfo(packageName, 0);
			return pi.versionName;
		} catch (Exception e) {
		}
		return "";
	}

	/*
	  根据包名获取应用类型
 	 */
	public int getAppType(String packageName) {
		synchronized (mAppTypePkgs) {
			for (int appType : mAppTypePkgs.keySet()) {
				if (mAppTypePkgs.get(appType).contains(packageName)) {
					return appType;
				}
			}
		}
		return -1;
	}

	/*
		根据包名判断是否导航应用
	 */
	public boolean isNavApp(String packageName) {
		return UiEquipment.APP_TYPE_NAVI == getAppType(packageName);
	}
}
