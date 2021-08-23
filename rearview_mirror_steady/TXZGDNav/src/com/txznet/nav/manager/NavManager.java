package com.txznet.nav.manager;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.text.TextUtils;

import com.amap.api.navi.AMapNavi;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LocationUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.NavService;
import com.txznet.nav.NavService.OnLocationListener;
import com.txznet.nav.helper.NavInfoCache;
import com.txznet.nav.ui.AMapConfig;
import com.txznet.nav.ui.CheckMapActivity;
import com.txznet.nav.ui.NaviCustomView;
import com.txznet.nav.ui.NavViewActivity;
import com.txznet.nav.ui.RoutePlanActivity;
import com.txznet.nav.ui.SetLocationActivity;
import com.txznet.txz.util.runnables.Runnable1;

public class NavManager {
	public static final int LOCATION_COMPANY = 2;
	public static final int LOCATION_HOME = 1;
	public static final int LOCATION_NONE = 0;

	private int mCurrentStrategy;

	private volatile boolean isNav;
	private volatile boolean isMultiNav;

	private NavService mNavService;
	private NavigateInfo mNavigateInfo;

	private static NavManager instance;

	private NavManager() {
		mNavService = NavService.getInstance();
	}

	public static NavManager getInstance() {
		if (instance == null) {
			synchronized (NavManager.class) {
				if (instance == null) {
					instance = new NavManager();
				}
			}
		}
		return instance;
	}

	public LocationInfo getLocationInfo() {
		return mNavService.getLocationInfo();
	}

	public NavigateInfo getNavigateInfo() {
		return mNavigateInfo;
	}

	public void NavigateTo(NavigateInfo info) {
		if (info == null || info.strTargetName == null
				|| info.strTargetAddress == null || info.msgGpsInfo == null
				|| info.msgGpsInfo.dblLat == null
				|| info.msgGpsInfo.dblLng == null
				|| info.msgGpsInfo.uint32GpsType == null) {
			LogUtil.logd("navigateInfo");
			return;
		}
		mNavigateInfo = info;

		int type = 2;
		if (mNavigateInfo.msgServerPushInfo != null) {
			if (mNavigateInfo.msgServerPushInfo.uint32Type == 1) {
				type = 1;
			}
		}

		stopNavi(true);
		startPreview(type);
		NavInfoCache.getInstance().saveCache(mNavigateInfo);
	}

	public void startPreview(int navType) {
		switch (navType) {
		case 1:
			setIsMultiNav(true);
			break;

		case 2:
			setIsMultiNav(false);
			break;
		}

		Intent intent = new Intent(AppLogic.getApp(),
				RoutePlanActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		if (NavManager.getInstance().isMultiNav()) {
			MultiNavManager.getInstance().startNavigate(getNavigateInfo());
		}

		AppLogic.runOnUiGround(new Runnable1<Intent>(intent) {

			@Override
			public void run() {
				AppLogic.getApp().startActivity(mP1);
			}
		}, 0);
	}

	public void stopNavi(final boolean shouldClean) {
		if (shouldClean) {
			NavInfoCache.getInstance().reset();
		}
		try {
			AMapNavi an = AMapNavi.getInstance(AppLogic.getApp());
			if (an != null) {
				LogUtil.logd("AMapNavi stopNavi");
				if (isNavi()) {
					an.stopNavi();
				}
				an.destroy();
			}
			MultiNavManager.getInstance().reqEnd();
			AppLogic
					.finishActivity(NavViewActivity.class);
			AppLogic
					.finishActivity(RoutePlanActivity.class);
			NavManager.getInstance().setIsNav(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public NavigateInfo getHome() {
		return mNavService.getHome();
	}

	public NavigateInfo getCompany() {
		return mNavService.getCompany();
	}

	public void setStrategy(int strategy) {
		this.mCurrentStrategy = strategy;
	}

	public int getStrategy() {
		return this.mCurrentStrategy;
	}

	public void setHome(String name, String address, double lat, double lng) {
		NavigateInfo home = new NavigateInfo();
		home.strTargetName = name;
		home.strTargetAddress = address;
		home.msgGpsInfo = new GpsInfo();
		home.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		home.msgGpsInfo.dblLat = lat;
		home.msgGpsInfo.dblLng = lng;
		LocationUtil.setHome(home);
	}

	public boolean deleteHistoryNaviateInfo(NavigateInfo info) {
		if (info == null) {
			return false;
		}

		byte[] data = ServiceManager.getInstance().sendTXZInvokeSync(
				"txz.nav.delete.history", MessageNano.toByteArray(info));
		return Boolean.parseBoolean(new String(data));
	}

	public void setCompany(String name, String address, double lat, double lng) {
		NavigateInfo company = new NavigateInfo();
		company.strTargetName = name;
		company.strTargetAddress = address;
		company.msgGpsInfo = new GpsInfo();
		company.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		company.msgGpsInfo.dblLat = lat;
		company.msgGpsInfo.dblLng = lng;
		LocationUtil.setCompany(company);
	}

	public void navigateHome() {
		NavigateTo(mNavService.getHome());
	}

	public void navigateCompany() {
		NavigateTo(mNavService.getCompany());
	}

	public void setHistory(NavigateInfo info) {
		mNavService.setHistory(info);
	}

	public NavigateInfoList getHistoryList() {
		return mNavService.getHistoryList();
	}

	// 主页目的地搜索
	public void startSearch(String strDest, String city,
			boolean isNearbySearch, int where) {

		JSONObject json = new JSONObject();
		try {
			json.put("keywords", strDest);
			json.put("city", city);
			json.put("isNearbySearch", isNearbySearch);
			json.put("where", where);
		} catch (Exception e) {
		}

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.inner.poiSearch", json.toString().getBytes(), null);
	}

	public void stopPreviewCount() {
		RoutePlanActivity rpa = (RoutePlanActivity) AppLogic.getActivity(RoutePlanActivity.class);
		if (rpa != null) {
			rpa.stopAutoNav();
		}
	}

	/**
	 * 启动设置地址界面
	 * 
	 * @param where
	 */
	public void startSetLocation(int where, int from, String searchKey) {
		Intent intent = new Intent(AppLogic.getApp(),
				SetLocationActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("where", where);
		intent.putExtra("key", searchKey);
		intent.putExtra("from", from);
		AppLogic.getApp().startActivity(intent);
	}

	public void startCheckMap(int where) {
		Intent intent = new Intent(AppLogic.getApp(),
				CheckMapActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("where", where);
		AppLogic.getApp().startActivity(intent);
	}

	public boolean isNavi() {
		return isNav;
	}

	public void setIsNav(boolean isNav) {
		this.isNav = isNav;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.inner.notifyNavStatus", ("" + isNav).getBytes(), null);
	}

	public boolean isInit() {
		return mNavService.isInit();
	}

	public boolean isMultiNav() {
		return isMultiNav;
	}

	public void setIsMultiNav(boolean isMultiNav) {
		this.isMultiNav = isMultiNav;
	}

	public void addLocationListener(OnLocationListener listener) {
		mNavService.addLocationListener(listener);
	}

	public void removeLocationListener(OnLocationListener listener) {
		mNavService.removeLocationListener(listener);
	}

	private String mRequestNaviInfoPackageName;

	private boolean mAccessLocalNaviToolUpdateNaviInfo;

	public boolean isAllowUpdateNaviInfo() {
		return mAccessLocalNaviToolUpdateNaviInfo;
	}

	public byte[] invokeNavi(final String packageName, String command,
			byte[] data) {
		if (command.equals("update.naviinfo")) {
			updateLocalNaviInfo(data);
		}
		if (command.equals("enableNaviInfo")) {
			enableUpdateTxzNaviInfo(packageName,
					Boolean.parseBoolean(new String(data)));
		}
		if (command.equals("changeDisplayType")) {
			invokeChangeDisplayType(data);
		}
		if (command.equals("initParams")) {
			try {
				mRequestNaviInfoPackageName = packageName;
				if (data == null) {
					mHasInitParams = false;
				} else {
					mHasInitParams = true;
				}
				JSONObject jo = new JSONObject(new String(data));
				if (jo != null) {
					mAccessLocalNaviToolUpdateNaviInfo = jo
							.getBoolean("needNaviInfo");
					mNaviDisplayType = jo.getString("nav_type");
					AMapConfig.mNaviViewSupport3D = jo.getBoolean("is3D");
				}
			} catch (JSONException e) {
				;
			}
		}
		return null;
	}

	public boolean mHasInitParams = false;
	public String mNaviDisplayType = "nav";

	private void enableUpdateTxzNaviInfo(String packageName, boolean enable) {
		LogUtil.logd("packageName:" + packageName + ",enable:" + enable);
		mAccessLocalNaviToolUpdateNaviInfo = enable;
		mRequestNaviInfoPackageName = packageName;
	}

	private void invokeChangeDisplayType(byte[] data) {
		try {
			String type = new String(data);
			if ("nav".equals(type)) {
				NaviCustomView.getInstance().setHudMode(false);
			} else if ("hud".equals(type)) {
				NaviCustomView.getInstance().setHudMode(true);
			}
		} catch (Exception e) {
			;
		}
	}

	public void updateLocalNaviInfo(byte[] data) {
		if (!mAccessLocalNaviToolUpdateNaviInfo
				|| TextUtils.isEmpty(mRequestNaviInfoPackageName)) {
			return;
		}
		LogUtil.logd("send NaviInfo to TXZNavManager");
		ServiceManager.getInstance().sendInvoke(mRequestNaviInfoPackageName,
				"tool.nav.updateNaviInfo", data, null);
		ServiceManager.getInstance().sendInvoke(mRequestNaviInfoPackageName,
				"tool.nav.custom.updateNaviInfo", data, null);
	}
}