package com.txznet.txz.module.app;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

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
import android.os.Environment;

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
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.ProcessUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sys.SysTool;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.MD5Util;

/**
 * 包管理模块，管理系统内安装的应用包
 * 
 * @author bihongpi
 *
 */
public class PackageManager extends IModule {
	static PackageManager sIns = new PackageManager();

	public boolean mInstalledRecord = false;
	// 是否初始化过中文应用列表
	public boolean mInitedInChinese = false;

	public static PackageManager getInstance() {
		return sIns;
	}

	private PackageManager() {
		mInstalledRecord = checkAppExist(ServiceManager.RECORD);

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
	}

	private Map<String, ApkInfo> mTxzApkInfos = new HashMap<String, ApkInfo>();

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
		ServiceManager.getInstance().sendInvoke(packageName,
				"comm.PackageInfo", null, new GetDataCallback() {
					@Override
					public int getTimeout() {
						return 60 * 1000;
					}

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
								// 取到了包的版本，删除其他版本的包文件
								try {
									String apkPath = json.optString(
											"sourceDir", "");
									if (!apkPath.isEmpty()) {
										File d = new File(Environment
												.getExternalStorageDirectory(),
												"txz/install");
										final File f = new File(apkPath);
										File[] fs = d
												.listFiles(new FileFilter() {
													@Override
													public boolean accept(
															File pathname) {
														String name = pathname
																.getName()
																.toLowerCase();
														if (!name.startsWith(packageName
																.toLowerCase()
																+ "_"))
															return false;
														if (!name
																.endsWith(".apk"))
															return false;

														if (pathname
																.getAbsolutePath()
																.equals(f
																		.getAbsolutePath()))
															return false;

														return true;
													}
												});
										if (fs != null) {
											for (File df : fs) {
												JNIHelper
														.logd("remove old apk file: "
																+ df.getPath());

												df.delete();
											}
										}
									}
								} catch (Exception e) {
									JNIHelper
											.logw("clear old version apk exception: "
													+ e.getMessage());
								}
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
				});
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

				packageList.add(tmpInfo);

				JNIHelper.logd(entry.getKey() + " VERSION: "
						+ tmpInfo.strPackageVersion + "#"
						+ tmpInfo.strSourceApk);
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
		List<PackageInfo> packages = GlobalContext.get().getPackageManager()
				.getInstalledPackages(0);

		StringBuilder tmpAppListInfo = new StringBuilder();
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			// if (packageInfo.packageName.startsWith("com.txznet."))
			// continue;

			// 跳过不包含启动图标的应用
			if (!isLaucherApp(packageInfo.packageName)) {
				continue;
			}

			AppInfo tmpInfo = new AppInfo();
			tmpInfo.strAppName = ZHConverter
					.convert(
							packageInfo.applicationInfo.loadLabel(
									GlobalContext.get().getPackageManager())
									.toString(), ZHConverter.SIMPLIFIED);
			tmpInfo.strPackageName = packageInfo.packageName;
			appList.add(tmpInfo);

			tmpAppListInfo.append(tmpInfo.strPackageName + "("
					+ tmpInfo.strAppName + "); ");

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

	public void onAppAdded(String packageName) {
		if (packageName.endsWith(ServiceManager.RECORD)) {
			mInstalledRecord = true;
		}
	}

	public void onAppRemoved(String packageName) {
		if (packageName.endsWith(ServiceManager.RECORD)) {
			mInstalledRecord = false;
		}
		synchronized (mTxzApkInfos) {
			mTxzApkInfos.remove(packageName);
		}
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

	public void openApp(String packageName) {
		// 如果有远程注册的打开应用工具，则让远程注册工具处理
		if (SysTool.procByRemoteTool(SysTool.APP_MGR, "openApp", packageName)) {
			return;
		}

		try {
			// 默认按getLaunchIntentForPackage方式启动
			Intent in = GlobalContext.get().getPackageManager()
					.getLaunchIntentForPackage(packageName);
			if (in != null) {
				in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				GlobalContext.get().startActivity(in);
				JNIHelper.logd("openApp by getLaunchIntentForPackage");
				return;
			}

			Intent resolveIntent;

			// 尝试查找MAIN+LAUNCHER
			resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(packageName);
			if (startAppByActivityIntent(resolveIntent)) {
				JNIHelper.logd("openApp by MAIN+LAUNCHER");
				return;
			}

			// 尝试查找MAIN
			resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.setPackage(packageName);
			if (startAppByActivityIntent(resolveIntent)) {
				JNIHelper.logd("openApp by MAIN");
				return;
			}

			// 尝试查找LAUNCHER
			resolveIntent = new Intent();
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(packageName);
			if (startAppByActivityIntent(resolveIntent)) {
				JNIHelper.logd("openApp by LAUNCHER");
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
					GlobalContext.get().startActivity(resolveIntent);
					JNIHelper.logd("openApp by last method");
					return;
				}
			} catch (Exception e) {
			}
		} catch (Exception e) {
		}
		String spk = NativeData.getResString("RS_APP_OPEN_FAIL");
		TtsManager.getInstance().speakText(spk);
	}

	public void closeApp(final String packageName) {
		JNIHelper.logd("closeApp:" + packageName);
		// 如果有远程注册的关闭应用工具，则让远程注册工具处理
		if (SysTool.procByRemoteTool(SysTool.APP_MGR, "closeApp", packageName)) {
			return;
		}
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
					String spk = NativeData.getResPlaceholderString(
							"RS_APP_WILL_OPEN", "%CMD%", appInfo.strAppName);
					RecorderWin.speakTextWithClose(
							spk, new Runnable() {
								@Override
								public void run() {
									openApp(appInfo.strPackageName);
								}
							});
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case UiApp.SUBEVENT_CLOSE_APP: {
				try {
					final AppInfo appInfo = AppInfo.parseFrom(data);
					JNIHelper.logd("exit app:" + appInfo.strPackageName);
					String spk = NativeData.getResPlaceholderString(
							"RS_APP_WILL_CLOSE", "%CMD%", appInfo.strAppName);
					RecorderWin.speakTextWithClose(
							spk, new Runnable() {
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
	 * @param pkgInfo
	 */
	private void queryPackageInfo(final byte[] data) {
		String str = new String(data);

		AppLogic.runOnSlowGround(new Runnable() {

			@Override
			public void run() {

				// 解析后台传入参数
				PackageInfo[] infoList = getPkgInfoList(data);

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
		}, 0);
	}

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
					resolvedPkgName = NavManager.getInstance()
							.getLocalNavImpl().getPackageName();
				} else if (pkgName.equals(PKG_TYPE_MUSIC)) {
					resolvedPkgName = MusicManager.getInstance().getMusicTool()
							.getPackageName();
				} else if (pkgName.equals(PKG_TYPE_AUDIO)) {
					resolvedPkgName = AudioManager.getInstance()
							.getLocalAudioTool().getPackageName();
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
}
