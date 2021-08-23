package com.txznet.txz.module.location;

import java.util.List;

import com.qihu.mobile.lbs.geocoder.Geocoder.GeocoderResult;
import com.qihu.mobile.lbs.geocoder.Geocoder.QHAddress;
import com.qihu.mobile.lbs.geocoder.GeocoderAsy;
import com.qihu.mobile.lbs.geocoder.GeocoderAsy.GeocoderListener;
import com.qihu.mobile.lbs.location.IQHLocationListener;
import com.qihu.mobile.lbs.location.QHLocation;
import com.qihu.mobile.lbs.location.QHLocationClient;
import com.qihu.mobile.lbs.location.QHLocationClientOption;
import com.qihu.mobile.lbs.location.QHLocationClientOption.LocationMode;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

public class LocationClientOfQihoo implements ILocationClient {
	Context mContext;
	LocationInfo mLastLocation = null;
	QHLocationClient mLocClient = null;
	QHLocationClientOption mLocOption = null;
	GeoInfo mLastGeo = new GeoInfo();
	long mLastCityTime = 0;
	/** 定位时间间隔 */
	private int mTimeInterval = ILocationClient.LOCATION_DEFAULT_TIME_INTERVAL;

	IQHLocationListener mLocationListener = new IQHLocationListener() {
		@Override
		public void onReceiveLocation(QHLocation location) {
			mLastLocation = converLocation(location);
			LocationManager.getInstance().notifyUpdatedLocation();
		}

		@Override
		public void onReceiveCompass(float azumith) {

		}

		@Override
		public void onProviderStatusChanged(String provider, int status) {

		}

		@Override
		public void onProviderServiceChanged(String provider, boolean enable) {

		}

		@Override
		public void onLocationError(int code) {

		}

		@Override
		public void onGpsSatelliteStatusChanged(int satellite) {
		}
	};

	private LocationInfo converLocation(QHLocation location) {
		if (location == null || location.getLatitude() == 0.0D || location.getLongitude() == 0.0D) {
			return null;
		}
		LocationInfo info = new LocationInfo();
		info.msgGeoInfo = mLastGeo;
		info.uint32Time = (int) (location.getTime() / 1000);

		info.msgGpsInfo = new GpsInfo();
		info.msgGpsInfo.dblLat = location.getLatitude();
		info.msgGpsInfo.dblLng = location.getLongitude();
		if (location.hasAltitude())
			info.msgGpsInfo.dblAltitude = location.getAltitude();
		if (location.hasAccuracy())
			info.msgGpsInfo.fltRadius = location.getAccuracy();
		if (location.hasBearing())
			info.msgGpsInfo.fltDirection = location.getBearing();
		if (location.hasSpeed())
			info.msgGpsInfo.fltSpeed = location.getSpeed();
		info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;

		info.msgGeoInfo = mLastGeo;
		if (info.msgGeoInfo == null || TextUtils.isEmpty(info.msgGeoInfo.strCity)
				|| SystemClock.elapsedRealtime() - mLastCityTime > 3 * 60 * 1000) {
			updateGeoInfo();
		}
		return info;
	}

	private void updateGeoInfo() {
		if (mLastLocation == null || mLastLocation.msgGpsInfo == null || mLastLocation.msgGpsInfo.dblLat == null
				|| mLastLocation.msgGpsInfo.dblLng == null || mLastLocation.msgGpsInfo.dblLat == 0.0D
				|| mLastLocation.msgGpsInfo.dblLng == 0.0D) {
			return;
		}

		mLastCityTime = SystemClock.elapsedRealtime();
		GeocoderAsy geocoder = new GeocoderAsy(mContext);
		// 创建逆地理编码和地理编码检索监听者
		GeocoderListener listener = new GeocoderListener() {
			// 地理编码监听函数
			@Override
			public void onGeocodeResult(GeocoderResult result) {
			}

			// 逆地理编码监听函数，输出经纬度点对应的地址信息
			@Override
			public void onRegeoCodeResult(GeocoderResult result, String description) {
				if (result.code != 0) {
					return;
				}
				List<QHAddress> list = result.address;
				if (list != null && list.size() > 0) {
					QHAddress address = list.get(0);
					mLastGeo.strAddr = address.getFormatedAddress();
					mLastGeo.strCity = address.getCity();
					mLastGeo.strDistrict = address.getDistrict();
					mLastGeo.strProvice = address.getProvince();
					mLastGeo.strStreet = address.getStreet();
					Log.d("LocationService", "geo code city: " + mLastGeo.strCity + ", addr:" + mLastGeo.strAddr);
				}
			}
		};
		geocoder.regeocode(mLastLocation.msgGpsInfo.dblLat, mLastLocation.msgGpsInfo.dblLng, listener);
	}

	public LocationClientOfQihoo() {
		mContext = GlobalContext.get();
		mLocClient = new QHLocationClient(mContext);
		mLocClient.registerLocationListener(mLocationListener);
		mLocOption = new QHLocationClientOption();
		mLocOption.setLocationMode(LocationMode.Fused);
		mLocOption.setCoorType("gcj02");
		mLocOption.setNeedDeviceDirect(false);
		mLocClient.setLocOption(mLocOption);
	}

	@Override
	public void quickLocation(boolean bQuick) {
		Log.d("LocationService", "LocationService  quickLocation: " + bQuick);
		if (bQuick) {
			mLocOption.setOpenGps(true);
			mLocOption.setInterval(mTimeInterval * 1000);
		} else {
			mLocOption.setOpenGps(false);
			mLocOption.setInterval(3 * 60 * 1000);
		}

		if (!mLocClient.isStarted()) {
			mLocClient.start();
		}

		mLocClient.requestLocation();
		
		LocationManager.getInstance().reinitLocationClientDelay();
	}

	@Override
	public void setLastLocation(LocationInfo location) {
		mLastLocation = location;
	}

	@Override
	public LocationInfo getLastLocation() {
		return mLastLocation;
	}

	@Override
	public void release() {
		mLocClient.unregisterLocationListener(mLocationListener);
		mLocClient.stop();
		
		LocationManager.getInstance().removeReinitDelayRunnable();
	}

	@Override
	public void setTimeInterval(int timeInterval) {
		mTimeInterval = timeInterval;
	}
}
