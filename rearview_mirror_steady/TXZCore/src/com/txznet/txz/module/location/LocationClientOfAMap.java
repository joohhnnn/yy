package com.txznet.txz.module.location;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.APSService;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.StreetNumber;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.jni.JNIHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.TextUtils;

public class LocationClientOfAMap implements ILocationClient {
	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;
	AMapLocation location = null;
	LocationInfo mLastLocation = null;
	Context mContext;
	long mLastCityTime = 0;
	GeoInfo mLastGeo = new GeoInfo();
	private boolean mIsBound = false;
	/** 定位时间间隔 */
	private int mTimeInterval = ILocationClient.LOCATION_DEFAULT_TIME_INTERVAL;
	//卫星数
	private int mSatellitesNum;
	//定位类型
	private int mLocationType;

	AMapLocationListener aMapLocationListener = new AMapLocationListener() {
		
		@Override
		public void onLocationChanged(AMapLocation location) {
			if (location == null || location.getLatitude() == 0.0D || location.getLongitude() == 0.0D) {
				if (DebugCfg.ENABLE_TRACE_GPS) {
					if (location != null) {
						JNIHelper.logw("onLocationChanged code:" + location.getErrorCode() + ",info:"
								+ location.getErrorInfo());
					} else {
						JNIHelper.logw("LocationClientOfAMap location is empty");
					}
				}
				return;
			}
			mSatellitesNum = location.getSatellites();
			mLocationType = location.getLocationType();
			mLastLocation = converLocation(location);
			LocationManager.getInstance().notifyUpdatedLocation();
		}
	};
	
	public LocationClientOfAMap() {
		mContext = GlobalContext.get();
		locationClient = new AMapLocationClient(mContext);
		locationOption = new AMapLocationClientOption();
		locationClient.setLocationListener(aMapLocationListener);
		locationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
		locationClient.setLocationOption(locationOption);
		bindService();
	}

	ServiceConnection mAPSServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			JNIHelper.loge("LocationClientOfAMap onServiceDisconnected");
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			JNIHelper.loge("LocationClientOfAMap onServiceConnected");
			quickLocation(true);
		}
	};
	
	private void bindService() {
		Intent intent = new Intent(GlobalContext.get(), APSService.class);
		intent.setPackage("com.txznet.txz");
		GlobalContext.get().bindService(intent, mAPSServiceConnection, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		mIsBound = true;
	}
	
	/**
	 * 将高德地图位置信息转为TXZ位置信息
	 * @param location
	 * @return
	 */
	private LocationInfo converLocation(AMapLocation location) {
		if (location == null || location.getLatitude() == 0.0D || location.getLongitude() == 0.0D) {
			if (DebugCfg.ENABLE_TRACE_GPS) {
				JNIHelper.logw("LocationClientOfAMap converLocation is invalid！");
			}
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
		
		//高德定位省辖县city字段填充的是province,需要特殊处理
		do{
			if (TextUtils.isEmpty(info.msgGeoInfo.strCity)){
				break;
			}
			//如果city字段填充的是省或者自治区,但是不包括直辖市和特别行政区，需要特殊处理
			if (!LocationManager.getInstance().isProvince(info.msgGeoInfo.strCity)){
				break;
			}
			//省辖县才需要特殊处理
			if (!LocationManager.getInstance().isMuniDistrict(info.msgGeoInfo.strDistrict)){
				break;
			}
			info.msgGeoInfo.strCity = info.msgGeoInfo.strDistrict;
			
		}while(false);
		
		if(info.msgGeoInfo == null || TextUtils.isEmpty(info.msgGeoInfo.strCity)){
			info.msgGeoInfo = mLastGeo;
			if(SystemClock.elapsedRealtime() - mLastCityTime > 3 * 60 * 1000){
				updateGeoInfo();
			}
		}
		// JNIHelper.logd("AMapLocation converLocation dblLat:"+info.msgGpsInfo.dblLat+",dblLng"+info.msgGpsInfo.dblLng);
		return info;
	}
	

	private void updateGeoInfo() {
		if (mLastLocation == null || mLastLocation.msgGpsInfo == null
				|| mLastLocation.msgGpsInfo.dblLat == null
				|| mLastLocation.msgGpsInfo.dblLng == null
				|| mLastLocation.msgGpsInfo.dblLat == 0.0D
				|| mLastLocation.msgGpsInfo.dblLng == 0.0D) {
			return;
		}
		mLastCityTime = SystemClock.elapsedRealtime();
		try {
            
		LocationManager.getInstance().reverseGeoCode(mLastLocation.msgGpsInfo.dblLat,
				mLastLocation.msgGpsInfo.dblLng, new OnGeocodeSearchListener() {

					@Override
					public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
					}

					@Override
					public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
						RegeocodeAddress rAddress = arg0.getRegeocodeAddress();
						if (rAddress == null) {
							return;
						}
						mLastGeo.strCity = rAddress.getCity();
						mLastGeo.strAddr = rAddress.getFormatAddress();
						mLastGeo.strProvice = rAddress.getProvince();
						mLastGeo.strDistrict = rAddress.getDistrict();
						if (TextUtils.isEmpty(mLastGeo.strCity)) {
							mLastGeo.strCity = mLastGeo.strProvice;
						}
						StreetNumber sn = rAddress.getStreetNumber();
						if (sn != null) {
							mLastGeo.strStreet = sn.getStreet();
							mLastGeo.strStreetNum = sn.getNumber();
						}
						LogUtil.logd("LocationClientOfAmap onRegeocodeSearched strCity:" + mLastGeo.strCity);

						mLastLocation.msgGeoInfo = mLastGeo;
						LocationManager.getInstance().notifyUpdatedLocation();
					}
				});
		} catch (Exception e) {
		    // TODO: handle exception
		}
	}

	@Override
	public void quickLocation(boolean bQuick) {
		JNIHelper.logd("AMapLocation quickLocation");
		if(bQuick){
			locationOption.setInterval(mTimeInterval * 1000);
		}else{
			locationOption.setInterval(3 * 60 * 1000);
		}
		locationClient.setLocationOption(locationOption);
		
		if(!locationClient.isStarted()){
			locationClient.startLocation();
		}
		
		LocationManager.getInstance().reinitLocationClientDelay();
	}

	@Override
	public void setLastLocation(LocationInfo location) {
		// JNIHelper.logd("AMapLocation setLastLocation");
		mLastLocation = location;
		if(mLastLocation.msgGeoInfo != null && !TextUtils.isEmpty(mLastLocation.msgGeoInfo.strCity)){
			mLastGeo = mLastLocation.msgGeoInfo;
		}
	}

	@Override
	public LocationInfo getLastLocation() {
		// JNIHelper.logd("AMapLocation getLastLocation");
		return mLastLocation;
	}

	@Override
	public void release() {
		locationClient.unRegisterLocationListener(aMapLocationListener);
		locationClient.stopLocation();
		locationClient.onDestroy();
		if(mIsBound){
		    GlobalContext.get().unbindService(mAPSServiceConnection);
		    mIsBound = false;
		}
		
		JNIHelper.logd("AMapLocation release");
		
		LocationManager.getInstance().removeReinitDelayRunnable();
	}

	@Override
	public void setTimeInterval(int timeInterval) {
		mTimeInterval = timeInterval;
	}

	public int getLocationType() {
		return mLocationType;
	}

	public int getSatellitesNum() {
		return mSatellitesNum;
	}
}
