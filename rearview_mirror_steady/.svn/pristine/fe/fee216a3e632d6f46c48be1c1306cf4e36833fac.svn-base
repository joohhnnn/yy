package com.txznet.txzcar;

import android.content.Intent;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.navisdk.ui.routeguide.BNavigator;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txzcar.ui.MainActivity;
import com.txznet.txzcar.util.BDLocationUtil;

public class NavManager {
	private static final String TAG = "NavManager";
	private static NavManager instance;
	
	private boolean isNav;
	private NavigateInfo mNavigateInfo;
	private LocationInfo mLocationInfo;
	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;
	
	private NavManager(){
		mLocationClient = new LocationClient(MyApplication.getApp()); // 声明LocationClient类
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener); // 注册监听函数
		quickLocation(false);
	}
	
	public static NavManager getInstance(){
		if(instance == null){
			synchronized (NavManager.class) {
				if(instance == null){
					instance = new NavManager();
				}
			}
		}
		return instance;
	}
	
	public LocationInfo getLocationInfo(){
		if(mLocationInfo == null){
			quickLocation(false);
		}
		
		return mLocationInfo;
	}
	
	public NavigateInfo getNavigateInfo(){
		return mNavigateInfo;
	}
	
	public void NavigateTo(NavigateInfo info) {
		if (info == null || info.strTargetName == null
				|| info.strTargetAddress == null || info.msgGpsInfo == null
				|| info.msgGpsInfo.dblLat == null
				|| info.msgGpsInfo.dblLng == null
				|| info.msgGpsInfo.uint32GpsType == null)
			return;
		mNavigateInfo = info;
		startPreview(info.strTargetName, info.strTargetAddress,
				info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng,
				info.msgGpsInfo.uint32GpsType);
	}
	
	public void startPreview(String name, String address, double lat,
			double lng, int type) {
		Intent intent = new Intent(MyApplication.getApp(),
				MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("name", name);
		intent.putExtra("address", address);
		intent.putExtra("lat", lat);
		intent.putExtra("lng", lng);
		intent.putExtra("type", type);
		
		MyApplication.getApp().runOnUiGround(
				new Runnable1<Intent>(intent) {

					@Override
					public void run() {
						stopNavi();
						MyApplication.getApp().runOnUiGround(
								new Runnable1<Intent>(mP1) {

									@Override
									public void run() {
										MyApplication.getApp().startActivity(
												mP1);
									}
								}, 0);
					}
				}, 0);
	}
	
	public void stopNavi(){
		MultiNavManager.getInstance().reqEnd();
		if(isNav()){
			BNavigator.destory();
			NavManager.getInstance().setIsNav(false);
		}
	}
	
	public boolean isNav(){
		return isNav;
	}
	
	public void setIsNav(boolean isNav){
		this.isNav = isNav;
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
	
	private class MyLocationListener implements BDLocationListener{

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

			LocationInfo loc = BDLocationUtil.Convert_BDLocation_To_LocationInfo(location);
			setLocationInfo(loc);
		}
	}
	
	private void setLocationInfo(LocationInfo location){
		if(location == null){
			Log.e(TAG, "NavManager -- > 定位失败！");
		}
		
		mLocationInfo = location;
	}
}
