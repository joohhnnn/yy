package com.txznet.txz.module.location;

import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.StreetNumber;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.jni.JNIHelper;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

public class LocationClientOfTencent implements ILocationClient{
	TencentLocationManager locationManager = null;
	TencentLocationRequest locationRequest = null;
	LocationInfo mLastLocation = null;
	Context mContext;
	
	GeoInfo mLastGeo = new GeoInfo();
	long mLastCityTime = 0;
	
	/** 定位时间间隔 */
	private int mTimeInterval = ILocationClient.LOCATION_DEFAULT_TIME_INTERVAL;
	
	TencentLocationListener tencentLocationListener = new TencentLocationListener() {
		
		@Override
		public void onStatusUpdate(String name, int status, String desc) {
		}
		
		@Override
		public void onLocationChanged(TencentLocation location, int error, String reason) {
			if (location == null || location.getLatitude() == 0.0D || location.getLongitude() == 0.0D) {
				if (DebugCfg.ENABLE_TRACE_GPS) {
					JNIHelper.logw("LocationClientOfTencent location is empty！");
				}
				return;
			}
			mLastLocation = converLocation(location);
			LocationManager.getInstance().notifyUpdatedLocation();
		}
	}; 
	
	public LocationClientOfTencent() {
		mContext = GlobalContext.get();
		locationManager = TencentLocationManager.getInstance(mContext);
		locationManager.setKey("tongxingzhe,TTQDK-XDHEX-YWCX5-UNLUD-SDRCT");
		locationManager.setCoordinateType(TencentLocationManager.COORDINATE_TYPE_GCJ02);
		locationRequest = TencentLocationRequest.create();
		locationRequest
				.setRequestLevel(
						TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA)
				.setAllowCache(true).setAllowDirection(true);
	}

	/**
	 * 将腾讯定位的结果转为TXZ位置信息
	 * @param location
	 * @return
	 */
	protected LocationInfo converLocation(TencentLocation location) {
		if (location == null || location.getLatitude() == 0.0D || location.getLongitude() == 0.0D) {
			return null;
		}
		LocationInfo info = new LocationInfo();
		info.uint32Time = (int) (location.getTime() /1000);
		
		info.msgGpsInfo = new GpsInfo();
		info.msgGpsInfo.dblLat = location.getLatitude();
		info.msgGpsInfo.dblLng = location.getLongitude();
		info.msgGpsInfo.dblAltitude = location.getAltitude();
		info.msgGpsInfo.fltRadius = location.getAccuracy();
		info.msgGpsInfo.fltDirection = location.getBearing();
		info.msgGpsInfo.fltSpeed = location.getSpeed();
		info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		
		info.msgGeoInfo = new GeoInfo();
		info.msgGeoInfo.strCity = location.getCity();
		info.msgGeoInfo.strCityCode = location.getCityCode();
		info.msgGeoInfo.strDistrict = location.getDistrict();
		info.msgGeoInfo.strProvice = location.getProvince();
		info.msgGeoInfo.strStreet = location.getStreet();
		info.msgGeoInfo.strStreetNum = location.getStreetNo();
		info.msgGeoInfo.strAddr = location.getNation()+location.getProvince()+location.getCity()+location.getDistrict()+location.getStreetNo();
		
		if(info.msgGeoInfo == null || TextUtils.isEmpty(info.msgGeoInfo.strCity)){
			info.msgGeoInfo = mLastGeo;
			if(SystemClock.elapsedRealtime() - mLastCityTime > 3 * 60 * 1000){
				updateGeoInfo();
			}
		}
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
						StreetNumber sn = rAddress.getStreetNumber();
						if (sn != null) {
							mLastGeo.strStreet = sn.getStreet();
							mLastGeo.strStreetNum = sn.getNumber();
						}

						mLastLocation.msgGeoInfo = mLastGeo;
						LocationManager.getInstance().notifyUpdatedLocation();
					}
				});
		} catch (Exception e) {
		}
	}

	@Override
	public void quickLocation(boolean bQuick) {
//		JNIHelper.logd("TencentLocation quickLocation");
		if(bQuick){
			locationRequest.setInterval(mTimeInterval*1000);
		}else{
			locationRequest.setInterval(3*60*1000);
		}
		locationManager.requestLocationUpdates(locationRequest, tencentLocationListener);
		
		LocationManager.getInstance().reinitLocationClientDelay();
	}

	@Override
	public void setLastLocation(LocationInfo location) {
		JNIHelper.logd("TencentLocation setLastLocation");
		mLastLocation = location;
		if(mLastLocation.msgGeoInfo != null && !TextUtils.isEmpty(mLastLocation.msgGeoInfo.strCity)){
			mLastGeo = mLastLocation.msgGeoInfo;
		}
	}

	@Override
	public LocationInfo getLastLocation() {
//		JNIHelper.logd("TencentLocation getLastLocation");
		return mLastLocation;
	}

	@Override
	public void release() {
		JNIHelper.logd("TencentLocation release");
		locationManager.removeUpdates(tencentLocationListener);
		
		LocationManager.getInstance().removeReinitDelayRunnable();
	}

	@Override
	public void setTimeInterval(int timeInterval) {
		mTimeInterval = timeInterval;
	}

}
