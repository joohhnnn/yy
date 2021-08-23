package com.txznet.launcher.bean;

import com.txznet.loader.AppLogic;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

public class AppIconInfo implements LauncherIconInfo {
	static PackageManager mPackageManager = AppLogic.getApp()
			.getPackageManager();
	ResolveInfo mInfo;

	public AppIconInfo(ResolveInfo info) {
		mInfo = info;
	}

	@Override
	public String getTitle() {

		return mInfo.loadLabel(mPackageManager).toString();
	}

	@Override
	public Drawable getIcon() {
		return mInfo.activityInfo.loadIcon(mPackageManager);
	}

	public String getPackageName() {
		return mInfo.activityInfo.packageName;
	}

	public boolean isSystemApp() {
		PackageInfo packageInfo;
		try {
			packageInfo = mPackageManager.getPackageInfo(getPackageName(),
					PackageManager.GET_CONFIGURATIONS);
			return ((packageInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0);
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	@Override
	public void onClick() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		ComponentName componentName = new ComponentName(getPackageName(),
				mInfo.activityInfo.name);
		intent.setComponent(componentName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		AppLogic.getApp().startActivity(intent);
	}

}
