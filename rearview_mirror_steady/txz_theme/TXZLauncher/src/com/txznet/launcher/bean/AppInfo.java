package com.txznet.launcher.bean;

import android.graphics.drawable.Drawable;

/**
 * 应用实体类
 * 
 * @author ZYH
 *
 */
public class AppInfo {
	private String appName;
	private Drawable icon;
	private String packageName;
	private String appNameId;
	private String className;
	private boolean isSystemApp;

	public AppInfo() {
	}

	public AppInfo(String appName, Drawable icon, String packageName) {
		this.appName = appName;
		this.icon = icon;
		this.packageName = packageName;
	}
	
	

	public String getAppNameId() {
		return appNameId;
	}

	public void setAppNameId(String appNameId) {
		this.appNameId = appNameId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public boolean isSystemApp() {
		return isSystemApp;
	}

	public void setSystemApp(boolean isSystemApp) {
		this.isSystemApp = isSystemApp;
	}
}
