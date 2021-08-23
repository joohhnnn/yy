package com.txznet.nav;

import android.app.Activity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LocationUtil;
import com.txznet.comm.remote.util.LocationUtil.GetLocationCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.ui.CheckMapActivity;
import com.txznet.nav.util.BDLocationUtil;

public class NavService {

	private static NavService mInstance = new NavService();

	private LocationInfo mLocationInfo;
	private NavigateInfo mHome;
	private NavigateInfo mCompany;
	private NavigateInfoList mHistoryList;
	private boolean mIsInit;
	private LocationClient mLocationClient;

	public static NavService getInstance() {
		return mInstance;
	}

	private NavService() {
		// 请求获取家
		LocationUtil.getHome(new GetLocationCallback() {

			@Override
			public void onGet() {
				setHome(home);
			}
		});

		// 请求获取公司
		LocationUtil.getCompany(new GetLocationCallback() {

			@Override
			public void onGet() {
				setCompany(company);
			}
		});

		// 请求获取历史记录
		LocationUtil.getHistoryList(new GetLocationCallback() {

			@Override
			public void onGet() {
				mHistoryList = historylist;
			}
		});

		mLocationClient = new LocationClient(MyApplication.getApp()); // 声明LocationClient类
		mLocationClient.registerLocationListener(mLocationListener); // 注册监听函数
		quickLocation(false);
	}

	public void getLocationOnce() {
		quickLocation(false);
	}

	public void setLocationInfo(LocationInfo m) {
		if (mLocationInfo == m)
			return;
		mLocationInfo = m;

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.inner.updateLocation",
				LocationInfo.toByteArray(mLocationInfo), null);
		// 通知UI
		Activity a = MyApplication.getInstance().getActivity(
				CheckMapActivity.class);
		if (a == null)
			return;
		CheckMapActivity c = (CheckMapActivity) a;
		c.refreshMapLocation();
	}

	public LocationInfo getLocationInfo() {
		if (mLocationInfo == null)
			quickLocation(false);
		return mLocationInfo;
	}

	public void setHome(NavigateInfo h) {
		mHome = h;
	}

	public NavigateInfo getHome() {
		return mHome;
	}

	public void setCompany(NavigateInfo c) {
		mCompany = c;
	}

	public NavigateInfo getCompany() {
		return mCompany;
	}

	public void setHistory(String name, String address, double lat, double lng,
			int gpsType) {
		NavigateInfo history = new NavigateInfo();
		history.strTargetName = name;
		history.strTargetAddress = address;
		history.msgGpsInfo = new GpsInfo();
		if (UiMap.GPS_TYPE_BD09 == gpsType) {
			double xy[] = BDLocationUtil.Convert_BD09_To_GCJ02(lat, lng);
			lat = xy[0];
			lng = xy[1];
		}

		history.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		history.msgGpsInfo.dblLat = lat;
		history.msgGpsInfo.dblLng = lng;
		LocationUtil.setHisory(history);
	}

	public NavigateInfoList getHistoryList() {
		return mHistoryList;
	}

	public void setHistoryList(NavigateInfoList l) {
		mHistoryList = l;
	}

	public boolean isInit() {
		return mIsInit;
	}

	public void setIsInit(boolean mIsInit) {
		if (this.mIsInit == mIsInit)
			return;

		this.mIsInit = mIsInit;

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.inner.notifyInitStatus", ("" + mIsInit).getBytes(),
				null);
	}

	private BDLocationListener mLocationListener = new TXZLocationListener();

	private class TXZLocationListener implements BDLocationListener {
		@Override
		public void onReceiveLocation(BDLocation location) {
			if (location == null)
				return;

			switch (location.getLocType()) {
			case BDLocation.TypeGpsLocation:
			case BDLocation.TypeNetWorkLocation:
			case BDLocation.TypeCacheLocation:
			case BDLocation.TypeOffLineLocation:
				break;
			case BDLocation.TypeNone:
			case BDLocation.TypeCriteriaException:
			case BDLocation.TypeNetWorkException:
			case BDLocation.TypeOffLineLocationFail:
			case BDLocation.TypeOffLineLocationNetworkFail:
			case BDLocation.TypeServerError:
			default:
				LogUtil.logd("获取到错误位置: " + location.getLocType());
				return;
			}

			// 过滤定位失败的情况
			LogUtil.logd("获取到新位置: " + location.getLocType());

			LocationInfo loc = BDLocationUtil
					.Convert_BDLocation_To_LocationInfo(location);
			setLocationInfo(loc);
		}
	}

	public void quickLocation(boolean bQuick) {
		LogUtil.logd("quickLocation bQuick=" + Boolean.toString(bQuick));

		// mLocationClient.stop();
		LocationClientOption locationClientOption = new LocationClientOption();
		locationClientOption.setLocationMode(LocationMode.Hight_Accuracy);
		locationClientOption.setIsNeedAddress(true);
		locationClientOption.setCoorType("gcj02");
		locationClientOption.setNeedDeviceDirect(true);
		locationClientOption.setLocationNotify(false);
		if (bQuick) {
			locationClientOption.setOpenGps(true);
			locationClientOption.setScanSpan(3 * 1000);
		} else {
			locationClientOption.setOpenGps(false);
			locationClientOption.setScanSpan(3 * 60 * 1000);
		}

		mLocationClient.setLocOption(locationClientOption);

		mLocationClient.setForBaiduMap(true);

		if (!mLocationClient.isStarted()) {
			mLocationClient.start();
		}

		mLocationClient.requestLocation(); // 立马请求定位一次
	}
}
