package com.txznet.txz.module.location;

import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.StreetNumber;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.service.TXZPowerControl;

import android.content.Context;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

public class LocationClientOfAndroidSystem implements ILocationClient {
	LocationInfo mLastLocation = null;

	GeoInfo mLastGeo = new GeoInfo();
	long mLastCityTime = 0;

	boolean mHasLocationAlready = false; // 是否已经有定位到的了
	boolean mProviderEnabled = false;
	/** 定位时间间隔 */
	private int mTimeInterval = ILocationClient.LOCATION_DEFAULT_TIME_INTERVAL;

	LocationListener mLocationListener = new LocationListener() {
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			JNIHelper.logd("onProviderEnabled: " + provider + ", status: "
					+ status + ", extras: " + extras);
		}

		@Override
		public void onProviderEnabled(String provider) {
			if (mProviderEnabled == false) {
				JNIHelper.logd("onProviderEnabled: " + provider);
				mProviderEnabled = true;
				quickLocation(mQuickLocation);
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			if (mProviderEnabled) {
				JNIHelper.logw("onProviderDisabled: " + provider);
				mProviderEnabled = false;
			}
		}

		@Override
		public void onLocationChanged(Location location) {
			if (location.getLatitude() == 0.0D
					|| location.getLongitude() == 0.0D) {
				return;
			}

			mLastLocation = converLocation(location);

			// JNIHelper.logd("get new location: "
			// + mLastLocation.msgGpsInfo.dblLat + ","
			// + mLastLocation.msgGpsInfo.dblLng);
			LocationManager.getInstance().notifyUpdatedLocation();

			mHasLocationAlready = true;
		}
	};
	
	
	private boolean mGpsStarted = false;
	
	long lastRequestTime = 0;
	long delayTime = 0;

	Listener mGpsListener = new Listener() {
		@Override
		public void onGpsStatusChanged(int event) {
			// JNIHelper.logd("onGpsStatusChanged: " + event);
			if (event == GpsStatus.GPS_EVENT_STARTED) {
				if (mGpsStarted == false) {
					mGpsStarted = true;
					if(DebugCfg.ENABLE_TRACE_GPS) {
						JNIHelper.logd("onGpsStatusChanged: " + event);
					}
					
					long temp = SystemClock.elapsedRealtime();
					if((temp - lastRequestTime) > (LOCATION_DEFAULT_TIME_INTERVAL * 1000)) {
						delayTime = 0;
					} else {
						delayTime = LOCATION_DEFAULT_TIME_INTERVAL * 1000;
					}
					AppLogicBase.runOnBackGround(new Runnable() {
						
						@Override
						public void run() {
							quickLocation(mQuickLocation);
							
						}
					}, delayTime);
					
				}
			}
			else if (event == GpsStatus.GPS_EVENT_STOPPED) {
				if (mGpsStarted) {
					mGpsStarted = false;
					if(DebugCfg.ENABLE_TRACE_GPS) {
						JNIHelper.logd("onGpsStatusChanged: " + event);
					}
				}
			}
		}
	};

	public LocationClientOfAndroidSystem() {
		mLastLocation = getLastBestLocation();
	}

	private LocationInfo converLocation(Location location) {
		if (location == null || location.getLatitude() == 0.0D
				|| location.getLongitude() == 0.0D) {
			return null;
		}

		LocationInfo info = new LocationInfo();
		info.msgGeoInfo = mLastGeo;
		info.uint32Time = (int) (location.getTime() / 1000);

		info.msgGpsInfo = new GpsInfo();
		Converter c = new Converter();
		Converter.Point p = c.getEncryPoint(location.getLongitude(),
				location.getLatitude());
		info.msgGpsInfo.dblLat = p.y;
		info.msgGpsInfo.dblLng = p.x;
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

		if (info.msgGeoInfo == null
				|| TextUtils.isEmpty(info.msgGeoInfo.strCity)
				|| SystemClock.elapsedRealtime() - mLastCityTime > 3 * 60 * 1000) {
			updateGeoInfo();
		}

		return info;
	}

	private LocationInfo getLastBestLocation() {
		android.location.LocationManager locationManager = (android.location.LocationManager) GlobalContext
				.get().getSystemService(Context.LOCATION_SERVICE);
		return converLocation(locationManager
				.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER));
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
//			final GeoCoder mGeoCoder = GeoCoder.newInstance();
//			mGeoCoder
//					.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
//						@Override
//						public void onGetReverseGeoCodeResult(
//								ReverseGeoCodeResult result) {
//							mGeoCoder.destroy();
//
//							if (result.error == ERRORNO.NO_ERROR) {
//								try {
//									mLastGeo.strCity = result.getAddressDetail().city;
//									mLastGeo.strAddr = result.getAddress();
//									mLastGeo.strProvice = result.getAddressDetail().province;
//									mLastGeo.strDistrict = result
//											.getAddressDetail().district;
//									mLastGeo.strStreet = result.getAddressDetail().street;
//									mLastGeo.strStreetNum = result
//											.getAddressDetail().streetNumber;
//
//									mLastLocation.msgGeoInfo = mLastGeo;
//									LocationManager.getInstance()
//											.notifyUpdatedLocation();
//								} catch (Exception e) {
//								}
//								JNIHelper
//										.logd("geo code city: " + mLastGeo.strCity);
//							}
//						}
//
//						@Override
//						public void onGetGeoCodeResult(GeoCodeResult result) {
//						}
//					});
//			mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
//					.location(new LatLng(mLastLocation.msgGpsInfo.dblLat,
//							mLastLocation.msgGpsInfo.dblLng)));
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
							LogUtil.logd("LocationClientOfAndroidSystem onRegeocodeSearched strCity:" + mLastGeo.strCity);

							mLastLocation.msgGeoInfo = mLastGeo;
							LocationManager.getInstance().notifyUpdatedLocation();
						}
					});
		} catch (Exception e) {
			/**
			 * java.lang.IllegalStateException: you have not supplyed the global app context info from SDKInitializer.initialize(Context) function.
                at com.baidu.mapapi.a.b(Unknown Source)
                at com.baidu.mapapi.search.geocode.GeoCoder.newInstance(Unknown Source)
			 */
		}
	}

	private boolean mQuickLocation = true;

	@Override
	public void quickLocation(boolean bQuick) {
		mQuickLocation = bQuick;

		android.location.LocationManager locationManager = (android.location.LocationManager) GlobalContext
				.get().getSystemService(Context.LOCATION_SERVICE);
				
		if (TXZPowerControl.hasReleased()) {
			LogUtil.logd("quickLocation power hasReleased!");
			return ;
		}

		try {
			locationManager.addGpsStatusListener(mGpsListener);
			locationManager.removeUpdates(mLocationListener);
		} catch (Exception e1) {
			LogUtil.loge("Location", e1);
		}

		if (!mHasLocationAlready) {
			try {
				if (locationManager != null
						&& locationManager
								.getProvider(android.location.LocationManager.NETWORK_PROVIDER) != null) {
					String networkProvider = locationManager.getProvider(
							android.location.LocationManager.NETWORK_PROVIDER)
							.getName();
					locationManager
							.requestLocationUpdates(networkProvider,
									mQuickLocation ? mTimeInterval * 1000  : 60000, 5,
									mLocationListener);
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}

		try {
			String gpsProvider = locationManager.getProvider(
					android.location.LocationManager.GPS_PROVIDER).getName();
			locationManager.requestLocationUpdates(gpsProvider,
					mQuickLocation ? mTimeInterval * 1000 : 60000, 5, mLocationListener);
			lastRequestTime = SystemClock.elapsedRealtime();
		} catch (Exception e) {
			// e.printStackTrace();
		}
		
		LocationManager.getInstance().reinitLocationClientDelay();
	}

	@Override
	public void setLastLocation(LocationInfo location) {
		mLastLocation = location;
		if(mLastLocation.msgGeoInfo != null && !TextUtils.isEmpty(mLastLocation.msgGeoInfo.strCity)){
			mLastGeo = mLastLocation.msgGeoInfo;
		}
	}

	@Override
	public LocationInfo getLastLocation() {
		return mLastLocation;
	}

	@Override
	public void release() {
		android.location.LocationManager locationManager = (android.location.LocationManager) GlobalContext.get()
				.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(mLocationListener);
		locationManager.removeGpsStatusListener(mGpsListener);

		LocationManager.getInstance().removeReinitDelayRunnable();
	}

	@Override
	public void setTimeInterval(int timeInterval) {
		mTimeInterval = timeInterval;
	}
}
