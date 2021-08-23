package com.txznet.txz.module.location;

import android.content.Context;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.text.TextUtils;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.StreetNumber;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.jni.JNIHelper;

public class LocationClientOfAndroidSystem implements ILocationClient {
	LocationInfo mLastLocation = null;

	GeoInfo mLastGeo = new GeoInfo();
	long mLastCityTime = 0;

	boolean mHasLocationAlready = false; // 是否已经有定位到的了
	
	boolean mProviderEnabled = false;

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

	Listener mGpsListener = new Listener() {
		@Override
		public void onGpsStatusChanged(int event) {
			// JNIHelper.logd("onGpsStatusChanged: " + event);
			if (event == GpsStatus.GPS_EVENT_STARTED) {
				if (mGpsStarted == false) {
					JNIHelper.logd("onGpsStatusChanged: " + event);
					mGpsStarted = true;
					quickLocation(mQuickLocation);
				}
			}
			else if (event == GpsStatus.GPS_EVENT_STOPPED) {
				if (mGpsStarted) {
					JNIHelper.logd("onGpsStatusChanged: " + event);
					mGpsStarted = false;
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
				|| System.currentTimeMillis() - mLastCityTime > 3 * 60 * 1000) {
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

		mLastCityTime = System.currentTimeMillis();
		final GeocodeSearch mGeocodeSearch = new GeocodeSearch(GlobalContext.get());
		mGeocodeSearch.setOnGeocodeSearchListener(new OnGeocodeSearchListener() {
			
			@Override
			public void onRegeocodeSearched(RegeocodeResult result, int resultID) {
				RegeocodeAddress regeocodeAddress = result.getRegeocodeAddress();
				try {
					if(regeocodeAddress!=null){
						mLastGeo.strCity = regeocodeAddress.getCity();
						mLastGeo.strAddr = regeocodeAddress.getFormatAddress();
						mLastGeo.strProvice = regeocodeAddress.getProvince();
						mLastGeo.strDistrict = regeocodeAddress.getDistrict();
						StreetNumber streetNumber = regeocodeAddress.getStreetNumber();
						if(streetNumber!=null){
							mLastGeo.strStreet = streetNumber.getStreet();
							mLastGeo.strStreetNum = streetNumber.getNumber();
						}
						mLastLocation.msgGeoInfo = mLastGeo;
						JNIHelper.logd("LocationClientOfAndroidSystem onRegeocodeSearched strCity:"+mLastGeo.strCity+",strAddr:"+
								mLastGeo.strAddr+",strProvice:"+mLastGeo.strProvice+",strDistrict:"+mLastGeo.strDistrict+","
										+ "strStreet:"+mLastGeo.strStreet+",strStreetNum:"+mLastGeo.strStreetNum);
						LocationManager.getInstance().notifyUpdatedLocation();
					
					}
				} catch (Exception e) {
					JNIHelper
					.logd("geo code city: " + mLastGeo.strCity);
				}
			}
			
			@Override
			public void onGeocodeSearched(GeocodeResult result, int resultID) {
			}
		});
		LatLonPoint point = new LatLonPoint(mLastLocation.msgGpsInfo.dblLat, mLastLocation.msgGpsInfo.dblLng);
		RegeocodeQuery regeocodeQuery = new RegeocodeQuery(point, 200, GeocodeSearch.AMAP);
		try {
			mGeocodeSearch.getFromLocation(regeocodeQuery);
		} catch (AMapException e) {
			e.printStackTrace();
			JNIHelper.loge("LocationClientOfAndroidSystem AMapException:"+e.getErrorCode());
		}
	}

	private boolean mQuickLocation = true;

	@Override
	public void quickLocation(boolean bQuick) {
		mQuickLocation = bQuick;

		android.location.LocationManager locationManager = (android.location.LocationManager) GlobalContext
				.get().getSystemService(Context.LOCATION_SERVICE);

		locationManager.addGpsStatusListener(mGpsListener);

		locationManager.removeUpdates(mLocationListener);

		if (!mHasLocationAlready) {
			try {
				String networkProvider = locationManager.getProvider(
						android.location.LocationManager.NETWORK_PROVIDER)
						.getName();
				locationManager.requestLocationUpdates(networkProvider,
						mQuickLocation ? 3000 : 60000, 5, mLocationListener);
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}

		try {
			String gpsProvider = locationManager.getProvider(
					android.location.LocationManager.GPS_PROVIDER).getName();
			locationManager.requestLocationUpdates(gpsProvider,
					mQuickLocation ? 3000 : 60000, 5, mLocationListener);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	@Override
	public void setLastLocation(LocationInfo location) {
		mLastLocation = location;
		mLastGeo = mLastLocation.msgGeoInfo;
	}

	@Override
	public LocationInfo getLastLocation() {
		return mLastLocation;
	}

	@Override
	public void release() {
		android.location.LocationManager locationManager = (android.location.LocationManager) GlobalContext
				.get().getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(mLocationListener);
		locationManager.removeGpsStatusListener(mGpsListener);
	}
}
