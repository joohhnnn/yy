package com.txznet.launcher.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.txznet.launcher.bean.AppInfo;
import com.txznet.launcher.helper.ResidentApp;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class AllAppsUtils {

	private final static String[] mFilterPackageName = { "com.android.camera2",
			"com.tencent.qqmusic", "com.txznet.sdkdemo" };
	
	public static ArrayList<AppInfo> getAllApps(Context context) {
		ArrayList<AppInfo> allApps = new ArrayList<AppInfo>();
		PackageManager pm = context.getPackageManager();
		Intent main = new Intent(Intent.ACTION_MAIN, null);
		main.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> apps = pm.queryIntentActivities(main, 0);
		Collections.sort(apps, new ResolveInfo.DisplayNameComparator(pm));
		if (apps != null) {
			for (int i = 0; i < apps.size(); i++) {
				ResolveInfo info = apps.get(i);
				// 过滤txznet包
				if (info.activityInfo.packageName
						.startsWith("com.txznet.bluetooth")
						|| info.activityInfo.packageName
								.startsWith("com.txznet.launcher")
						|| info.activityInfo.packageName
								.startsWith("com.txznet.nav")
						|| info.activityInfo.packageName
								.startsWith("com.txznet.music")
						|| info.activityInfo.packageName
								.startsWith("com.txznet.fm")
						|| info.activityInfo.packageName
								.startsWith("com.txznet.vedio")
						|| info.activityInfo.packageName
								.startsWith("com.txznet.webchat")
						|| info.activityInfo.packageName
								.startsWith("com.txznet.settings"))
					continue;
				// 过滤其他
				if (filterPackage(info.activityInfo.packageName))
					continue;

				AppInfo appInfo = new AppInfo();
				appInfo.setAppName(info.loadLabel(pm).toString());
				appInfo.setIcon(info.activityInfo.loadIcon(pm));
				appInfo.setPackageName(info.activityInfo.packageName);
				appInfo.setClassName(info.activityInfo.name);
				allApps.add(appInfo);
			}
		}
		return allApps;
	}

	private static boolean filterPackage(String packageName) {
		for (int i = 0; i < mFilterPackageName.length; i++) {
			if (packageName.equals(mFilterPackageName[i])) {
				return true;
			}
		}
		if(ResidentApp.getCustomResidentApp() != null && ResidentApp.getCustomResidentApp().size() > 0){
			for(AppInfo app : ResidentApp.getCustomResidentApp()){
				if (packageName.equals(app.getPackageName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 卸载应用
	 * 
	 * @param context
	 * @param packageName
	 */
	public static void uninstallApp(Context context, String packageName) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_DELETE);
		intent.setData(Uri.parse("package:" + packageName));
		context.startActivity(intent);
	}

	/**
	 * 获取屏幕最上面的Activity名
	 * 
	 * @param context
	 * @return
	 */
	public static String getTopActivityName(Context context) {
		String topActivityClassName = null;
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
		if (runningTasks != null) {
			ComponentName topActivity = runningTasks.get(0).topActivity;
			topActivityClassName = topActivity.getClassName();
		}
		return topActivityClassName;
	}

	/**
	 * 判断是否是系统应用软件
	 * 
	 * @param context
	 * @param packageName
	 * @return
	 */
	public static boolean isSystemApp(Context context, String packageName) {
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(
					packageName, 0);
			if ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
					|| (pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
				return true;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
}
