package com.txznet.launcher.helper;

import android.content.Context;
import android.view.View;

import com.txznet.launcher.R;
import com.txznet.launcher.bean.AppInfo;
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
    // 名字
    public static final int[] RESIDENT_APP_NAME_ID = {
            R.string.ResidentAppNamePhone,
            R.string.ResidentAppNameNavi,
            R.string.ResidentAppNameDrivingRecord,
            R.string.ResidentAppNameWechatHelper,
            R.string.ResidentAppNameMusic,
            R.string.ResidentAppNameFM,
            R.string.ResidentAppNameVedio,
            R.string.ResidentAppNameApps,
            R.string.ResidentAppNameSettings
    };
    // 显示的图片
    public static final String[] RESIDENT_APP_DRAWABLE = {
            "ic_phone",
            "ic_navi",
            "ic_driving_record",
            "ic_wechat_helper",
            "ic_music",
            "ic_fm",
            "ic_video",
            "ic_apps",
            "ic_settings"
    };
    /* 8801
    public static final String[] RESIDENT_APP_PACKAGE = {
    	"com.txznet.bluetooth",
        "com.txznet.nav",
        "com.android.camera2",
        "com.txznet.webchat",
        "com.txznet.music",
        "com.txznet.fm",
        "com.txznet.video",
        "com.txznet.launcher",
        "com.txznet.settings"
    };

    public static final String[] RESIDENT_APP_CLASS = {
    	"com.txznet.bluetooth.ui.activity.MainActivity",
        "com.txznet.nav.ui.MainActivity",
        "com.android.camera.CameraActivity",
        "com.txznet.webchat.ui.AppStartActivity",
        "com.txznet.music.ui.MainActivity",
        "com.txznet.fm.ui.FMActivity",
        "com.txznet.video.MainActivity",
        "com.txznet.launcher.ui.MainActivity",
        "com.txznet.settings.ui.MainActivity"};
    */
    public static final String[] RESIDENT_APP_PACKAGE = {
    	"com.txznet.bluetooth",
    	"com.txznet.nav",
    	"com.pg.software.recorder", 
    	"com.txznet.webchat",
		"com.txznet.music", 
		"com.pg.software.fm",
		"com.pg.software.gallery",
		"com.txznet.launcher", 
		"com.pg.software.setting" };

    public static final String[] RESIDENT_APP_CLASS ={ 
    	"com.txznet.bluetooth.ui.activity.MainActivity",
		"com.txznet.nav.ui.MainActivity",
		"com.pg.software.PGMainActivity",
		"com.txznet.webchat.ui.AppStartActivity",
		"com.txznet.music.ui.MainActivity",
		"com.pg.software.fm.MainActivity",
		"com.pg.software.gallery.GridMainActivity",
		"com.txznet.launcher.ui.MainActivity",
		"com.pg.software.PGMainActivity" };
		

    private ResidentApp() {
    }

    public static List<AppIcon> buildAppIcons(Context context, ProxyContext proxyContext, View.OnClickListener listener) {
        List<AppIcon> appIcons = new ArrayList<AppIcon>();
        for (int i = 0, len = RESIDENT_APP_DRAWABLE.length; i < len; i++) {
            AppInfo appInfo = getItem(context, proxyContext, i);
            AppIcon appIcon = new AppIcon(context, appInfo);
            appIcon.setTag(i);
            appIcon.setOnClickListener(listener);
            appIcons.add(appIcon);
        }
        return appIcons;
    }


    public static List<AppIcon> buildSmallAppIcons(Context context, ProxyContext proxyContext, View.OnClickListener listener) {
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
    private static AppInfo getItem(Context context, ProxyContext proxyContext, int index) {
        if (index >= 0 && index <= RESIDENT_APP_PACKAGE.length - 1) {
            AppInfo appInfo = new AppInfo();
            appInfo.setAppName(context.getResources().getString(RESIDENT_APP_NAME_ID[index]));
            appInfo.setPackageName(RESIDENT_APP_PACKAGE[index]);
            appInfo.setClassName(RESIDENT_APP_CLASS[index]);
            appInfo.setSystemApp(true);
            appInfo.setIcon(proxyContext.getDrawable(RESIDENT_APP_DRAWABLE[index]));
            return appInfo;
        }
        return null;
    }
}
