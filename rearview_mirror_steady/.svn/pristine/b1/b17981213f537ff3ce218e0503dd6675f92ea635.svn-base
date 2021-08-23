package com.txznet.txz.module.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.APSService;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.jni.JNIHelper;

public class LocationClientOfAMap implements ILocationClient {
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	AMapLocation location = null;
	LocationInfo mLastLocation = null;
	Context mContext;
	long mLastCityTime = 0;
	GeoInfo mLastGeo = new GeoInfo();
	
	
	AMapLocationListener aMapLocationListener = new AMapLocationListener() {
		
		@Override
		public void onLocationChanged(AMapLocation location) {
			mLastLocation = converLocation(location);
			JNIHelper.logd("AMapLocation mLastLocation:"+mLastLocation.toString());
			LocationManager.getInstance().notifyUpdatedLocation();
		}
	};
	
	public LocationClientOfAMap() {
		bindService();
		mContext = GlobalContext.get();
		locationClient = new AMapLocationClient(mContext);
		locationOption = new AMapLocationClientOption();
		locationClient.setLocationListener(aMapLocationListener);
		locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		locationClient.setLocationOption(locationOption);
	}

	private void bindService() {
		Intent intent = new Intent(GlobalContext.get(), APSService.class);
		intent.setPackage("com.txznet.txz");
		GlobalContext.get().bindService(intent, new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				JNIHelper.loge("LocationClientOfAMap onServiceDisconnected");
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				JNIHelper.loge("LocationClientOfAMap onServiceConnected");
				quickLocation(true);
			}
		}, Context.BIND_AUTO_CREATE);
	}
	
	/**
	 * 将高德地图位置信息转为TXZ位置信息
	 * @param location
	 * @return
	 */
	private LocationInfo converLocation(AMapLocation location) {
		if (location == null || location.getLatitude() == 0.0D || location.getLongitude() == 0.0D) {
			return null;
		}
		LocationInfo info = new LocationInfo();
		info.uint32Time = (int) (location.getTime() /1000);
		info.msgGpsInfo = new GpsInfo();
		info.msgGpsInfo.dblLat = location.getLatitude();
		info.msgGpsInfo.dblLng = location.getLongitude();
		if(location.hasAltitude()){
			info.msgGpsInfo.dblAltitude = location.getAltitude();
		}
		if(location.hasAccuracy()){
			info.msgGpsInfo.fltRadius = location.getAccuracy();
		}
		if(location.hasBearing()){
			info.msgGpsInfo.fltDirection = location.getBearing();
		}
		if(location.hasSpeed()){
			info.msgGpsInfo.fltSpeed = location.getSpeed();
		}
		info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		
		info.msgGeoInfo = new GeoInfo();
		info.msgGeoInfo.strAddr = location.getAddress();
		info.msgGeoInfo.strCity = location.getCity();
		info.msgGeoInfo.strCityCode = location.getCityCode();
		info.msgGeoInfo.strDistrict = location.getDistrict();
		info.msgGeoInfo.strProvice = location.getProvince();
		info.msgGeoInfo.strStreet = location.getStreet();
		info.msgGeoInfo.strStreetNum = location.getStreetNum();
		
		JNIHelper.logd("AMapLocation converLocation dblLat:"+info.msgGpsInfo.dblLat+",dblLng"+info.msgGpsInfo.dblLng);
		return info;
	}
	

	@Override
	public void quickLocation(boolean bQuick) {
		JNIHelper.logd("AMapLocation quickLocation");
		if(bQuick){
			locationOption.setInterval(3 * 1000);
		}else{
			locationOption.setInterval(3 * 60 * 1000);
		}
		locationClient.setLocationOption(locationOption);
		
		if(!locationClient.isStarted()){
			locationClient.startLocation();
		}
		
		
	}

	@Override
	public void setLastLocation(LocationInfo location) {
		JNIHelper.logd("AMapLocation setLastLocation");
		mLastLocation = location;
	}

	@Override
	public LocationInfo getLastLocation() {
		JNIHelper.logd("AMapLocation getLastLocation");
		return mLastLocation;
	}

	@Override
	public void release() {
		locationClient.unRegisterLocationListener(aMapLocationListener);
		locationClient.stopLocation(); 
		JNIHelper.logd("AMapLocation release");
	}

}
