package com.txznet.launcher.helper;

import android.content.Context;
import android.view.View;

import com.txznet.launcher.bean.AppInfo;
import com.txznet.launcher.config.AppConfig_TJD;
import com.txznet.launcher.ui.base.ProxyContext;
import com.txznet.launcher.ui.widget.AppIcon;

import java.util.ArrayList;
import java.util.List;

/**
 * 常驻的APP管理
 *
 * @author ZYH
 */
public class ResidentApp {
	public static final int[] RESIDENT_APP_NAME_ID; // 名字
	public static final String[] RESIDENT_APP_DRAWABLE; // 显示的图片
	public static final String[] RESIDENT_APP_PACKAGE; // 包名
	public static final String[] RESIDENT_APP_CLASS; // 特定的Activity
	  
	static {
		RESIDENT_APP_NAME_ID = AppConfig_TJD.RESIDENT_APP_NAME_ID;
		RESIDENT_APP_DRAWABLE = AppConfig_TJD.RESIDENT_APP_DRAWABLE;
		RESIDENT_APP_PACKAGE = AppConfig_TJD.RESIDENT_APP_PACKAGE;
		RESIDENT_APP_CLASS = AppConfig_TJD.RESIDENT_APP_CLASS;
	}
	  
	private static List<AppInfo> CUSTOM_RESIDENT_APP = null;

	public static void syncCustomResidentApp(List<AppInfo> appInfos) {
		CUSTOM_RESIDENT_APP = appInfos;
	}

	public static List<AppInfo> getCustomResidentApp() {
		return CUSTOM_RESIDENT_APP;
	}

	private ResidentApp() {
	}

	public static List<AppIcon> buildAppIcons(Context context,
			ProxyContext proxyContext, View.OnClickListener listener) {
		List<AppIcon> appIcons = new ArrayList<AppIcon>();
		if (null != CUSTOM_RESIDENT_APP && CUSTOM_RESIDENT_APP.size() > 0) {
			for (int i = 0; i < CUSTOM_RESIDENT_APP.size(); i++) {
				AppInfo appInfo = CUSTOM_RESIDENT_APP.get(i);
				String appName;
				try {
					appName = ThemeManager.getInstance(context)
							.getProxyContext()
							.getString(appInfo.getAppNameId());
					appInfo.setAppName(appName);
				} catch (Exception e) {
					// TODO: handle exception
				}

				AppIcon appIcon = new AppIcon(context, appInfo);
				appIcon.setTag(i);
				appIcon.setOnClickListener(listener);
				appIcons.add(appIcon);
			}
		} else {
			for (int i = 0, len = RESIDENT_APP_DRAWABLE.length; i < len; i++) {
				AppInfo appInfo = getItem(context, proxyContext, i);
				AppIcon appIcon = new AppIcon(context, appInfo);
				appIcon.setTag(i);
				appIcon.setOnClickListener(listener);
				appIcons.add(appIcon);
			}
		}
		return appIcons;
	}

	public static List<AppIcon> buildSmallAppIcons(Context context,
			ProxyContext proxyContext, View.OnClickListener listener) {
		List<AppIcon> appIcons = new ArrayList<AppIcon>();
		for (int i = 0, len = RESIDENT_APP_DRAWABLE.length; i < len; i++) {
			AppInfo appInfo = getItem(context, proxyContext, i);
			AppIcon appIcon = new AppIcon(context, appInfo, AppIcon.STYLE_SMALL);
			appIcon.setTag(i);
			appIcon.setOnClickListener(listener);
			appIcons.add(appIcon);
		}
		return appIcons;
	}

	/**
	 * 获取appinfo
	 *
	 * @param index
	 * @return
	 */
	private static AppInfo getItem(Context context, ProxyContext proxyContext,
			int index) {
		if (index >= 0 && index <= RESIDENT_APP_PACKAGE.length - 1) {
			AppInfo appInfo = new AppInfo();
			appInfo.setAppName(context.getResources().getString(
					RESIDENT_APP_NAME_ID[index]));
			appInfo.setPackageName(RESIDENT_APP_PACKAGE[index]);
			appInfo.setClassName(RESIDENT_APP_CLASS[index]);
			appInfo.setSystemApp(true);
			appInfo.setIcon(proxyContext
					.getDrawable(RESIDENT_APP_DRAWABLE[index]));
			return appInfo;
		}
		return null;
	}
}
