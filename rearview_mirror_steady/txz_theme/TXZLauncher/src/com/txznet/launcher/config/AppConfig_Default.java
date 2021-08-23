package com.txznet.launcher.config;

import com.txznet.launcher.R;

public class AppConfig_Default {
	public static final int[] RESIDENT_APP_NAME_ID = { 
			R.string.ResidentAppNamePhone, 
			R.string.ResidentAppNameNavi,
			R.string.ResidentAppNameDrivingRecord, 
			R.string.ResidentAppNameWechatHelper, 
			R.string.ResidentAppNameMusic,
			R.string.ResidentAppNameFM, 
			R.string.ResidentAppNameVedio, 
			R.string.ResidentAppNameApps,
			R.string.ResidentAppNameSettings };
	public static final String[] RESIDENT_APP_DRAWABLE = { 
			"ic_phone", 
			"ic_navi", 
			"ic_driving_record",
			"ic_wechat_helper", 
			"ic_music", 
			"ic_fm", 
			"ic_video", 
			"ic_apps", 
			"ic_settings" };
	public static final String[] RESIDENT_APP_PACKAGE = { 
			"com.txznet.bluetooth", 
			"com.txznet.nav",
			"com.android.camera2", 
			"com.txznet.webchat", 
			"com.txznet.music", 
			"com.txznet.fm", 
			"com.txznet.video",
			"com.txznet.launcher", 
			"com.txznet.settings" };
	public static final String[] RESIDENT_APP_CLASS = { 
			"com.txznet.bluetooth.ui.activity.MainActivity",
			"com.txznet.nav.ui.MainActivity", 
			"com.anywhee.car.MainActivity", 
			"com.txznet.webchat.ui.AppStartActivity",
			"com.txznet.music.ui.MainActivity", 
			"com.txznet.fm.ui.FMActivity", 
			"com.txznet.video.MainActivity",
			"com.txznet.launcher.ui.AllAppView", 
			"com.txznet.settings.ui.MainActivity" };
}
