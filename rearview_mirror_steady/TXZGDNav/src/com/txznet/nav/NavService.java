package com.txznet.nav;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LocationUtil;
import com.txznet.comm.remote.util.LocationUtil.GetLocationCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;

public class NavService {
	private static final String LOCATIONINFO = "LocationInfo";
	private static NavService mInstance = new NavService();

	private boolean mIsInit;
	private LocationInfo mLocationInfo;
	private NavigateInfo mHome;
	private NavigateInfo mCompany;
	private NavigateInfoList mHistoryList;
	private LocationManagerProxy mLocationManagerProxy;
	private List<OnLocationListener> mListeners = new ArrayList<OnLocationListener>();
	private OnHistoryNavigateInfoChangeListener mChangeListener;

	private SharedPreferences mSp = AppLogic.getApp().getSharedPreferences("txznav_sp", Context.MODE_PRIVATE);
	private Editor mEditor = mSp.edit();

	public static NavService getInstance() {
		return mInstance;
	}

	private NavService() {
		requestHistory();
		quickLocation();
	}

	public void requestHistory() {
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
	}

	public void quickLocation() {
		if (mLocationManagerProxy == null) {
			mLocationManagerProxy = LocationManagerProxy.getInstance(AppLogic.getApp());
		}

		mLocationManagerProxy.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 15, mLocationListener);
	}

	public void setLocationInfo(LocationInfo m) {
		if (mLocationInfo == m) {
			return;
		}

		mLocationInfo = m;
		saveToPreference(m);
	}

	public void setLocationInfo(AMapLocation location) {
		if (location != null && location.getAMapException().getErrorCode() == 0) {
			LocationInfo info = new LocationInfo();
			GeoInfo gi = new GeoInfo();
			gi.strAddr = location.getAddress();
			gi.strCity = location.getCity();
			gi.strCityCode = location.getCityCode();
			gi.strDistrict = location.getDistrict();
			gi.strProvice = location.getProvince();
			gi.strStreet = location.getStreet();
			gi.strStreetNum = location.getAdCode();
			info.msgGeoInfo = gi;
			GpsInfo gps = new GpsInfo();
			gps.dblAltitude = location.getAltitude();
			gps.dblLat = location.getLatitude();
			gps.dblLng = location.getLongitude();
			gps.fltSpeed = location.getSpeed();
			info.msgGpsInfo = gps;

			setLocationInfo(info);
			invokeLocationListener(location);
		} else {
			LogUtil.loge("获取最新定位失败！");
		}
	}

	public LocationInfo getLocationInfo() {
		if (mLocationInfo == null) {
			quickLocation();

			byte[] data = ServiceManager.getInstance().sendTXZInvokeSync("txz.nav.getLocationInfo", null);
			try {
				if (data != null) {
					LocationInfo info = LocationInfo.parseFrom(data);
					if (info != null) {
						return info;
					}
				}
			} catch (InvalidProtocolBufferNanoException e) {
				LogUtil.loge(e.toString());
			}

			return getSharedLocationInfo();
		}
		return mLocationInfo;
	}

	public void setHome(NavigateInfo h) {
		mHome = h;
		invokeHistoryChange();
	}

	public NavigateInfo getHome() {
		return mHome;
	}

	public void setCompany(NavigateInfo c) {
		mCompany = c;
		invokeHistoryChange();
	}

	public NavigateInfo getCompany() {
		return mCompany;
	}

	public void setHistory(NavigateInfo info) {
		LocationUtil.setHisory(info);
	}

	public NavigateInfoList getHistoryList() {
		return mHistoryList;
	}

	public void setHistoryList(NavigateInfoList l) {
		mHistoryList = l;
		invokeHistoryChange();
	}

	public boolean isInit() {
		return mIsInit;
	}

	public void setIsInit(boolean mIsInit) {
		if (this.mIsInit == mIsInit)
			return;

		this.mIsInit = mIsInit;

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.nav.inner.notifyInitStatus",
				("" + mIsInit).getBytes(), null);
	}

	private AMapLocationListener mLocationListener = new AMapLocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onLocationChanged(Location location) {
		}

		@Override
		public void onLocationChanged(AMapLocation location) {
			if (location != null && location.getAMapException().getErrorCode() == 0) {
				LocationInfo info = new LocationInfo();
				GeoInfo gi = new GeoInfo();
				gi.strAddr = location.getAddress();
				gi.strCity = location.getCity();
				gi.strCityCode = location.getCityCode();
				gi.strDistrict = location.getDistrict();
				gi.strProvice = location.getProvince();
				gi.strStreet = location.getStreet();
				gi.strStreetNum = location.getAdCode();
				info.msgGeoInfo = gi;
				GpsInfo gps = new GpsInfo();
				gps.dblAltitude = location.getAltitude();
				gps.dblLat = location.getLatitude();
				gps.dblLng = location.getLongitude();
				gps.fltSpeed = location.getSpeed();
				info.msgGpsInfo = gps;

				setLocationInfo(info);
				invokeLocationListener(location);
			} else {
				LogUtil.loge("定位失败，请重试！");
			}
		}
	};

	public void saveToPreference(LocationInfo info) {
		GeoInfo mGeoInfo = null;
		GpsInfo mGpsInfo = null;
		if ((mGeoInfo = info.msgGeoInfo) == null || (mGpsInfo = info.msgGpsInfo) == null) {
			return;
		}

		JSONObject jo = new JSONObject();
		try {
			String strAddr = "";
			String strCity = "";
			String strCityCode = "";
			String strDistrict = "";
			String strProvice = "";
			String strStreet = "";
			String strStreetNum = "";
			if (!TextUtils.isEmpty(mGeoInfo.strAddr)) {
				strAddr = mGeoInfo.strAddr;
			}

			if (!TextUtils.isEmpty(mGeoInfo.strCity)) {
				strCity = mGeoInfo.strCity;
			}

			if (!TextUtils.isEmpty(mGeoInfo.strCityCode)) {
				strCityCode = mGeoInfo.strCityCode;
			}

			if (!TextUtils.isEmpty(mGeoInfo.strDistrict)) {
				strDistrict = mGeoInfo.strDistrict;
			}

			if (!TextUtils.isEmpty(mGeoInfo.strProvice)) {
				strProvice = mGeoInfo.strProvice;
			}

			if (!TextUtils.isEmpty(mGeoInfo.strStreet)) {
				strStreet = mGeoInfo.strStreet;
			}

			if (!TextUtils.isEmpty(mGeoInfo.strStreetNum)) {
				strStreetNum = mGeoInfo.strStreetNum;
			}

			jo.put("strAddr", strAddr);
			jo.put("strCity", strCity);
			jo.put("strCityCode", strCityCode);
			jo.put("strDistrict", strDistrict);
			jo.put("strProvice", strProvice);
			jo.put("strStreet", strStreet);
			jo.put("strStreetNum", strStreetNum);

			double dblAltitude = mGpsInfo.dblAltitude;
			double dblLat = mGpsInfo.dblLat;
			double dblLng = mGpsInfo.dblLng;
			float fltSpeed = mGpsInfo.fltSpeed;
			jo.put("dblAltitude", dblAltitude);
			jo.put("dblLat", dblLat);
			jo.put("dblLng", dblLng);
			jo.put("fltSpeed", fltSpeed);

			String result = jo.toString();
			mEditor.putString(LOCATIONINFO, result);
			mEditor.commit();
		} catch (JSONException e) {
			LogUtil.loge("JsonError -- >" + e.toString());
		} catch (Exception e) {
			LogUtil.loge("setShared error -- > " + e.toString());
		}
	}

	public LocationInfo getSharedLocationInfo() {
		String result = mSp.getString(LOCATIONINFO, "");
		if (TextUtils.isEmpty(result)) {
			return null;
		}

		try {
			JSONObject jo = new JSONObject(result);

			LocationInfo info = new LocationInfo();
			GeoInfo gi = new GeoInfo();
			gi.strAddr = jo.getString("strAddr");
			gi.strCity = jo.getString("strCity");
			gi.strCityCode = jo.getString("strCityCode");
			gi.strDistrict = jo.getString("strDistrict");
			gi.strProvice = jo.getString("strProvice");
			gi.strStreet = jo.getString("strStreet");
			gi.strStreetNum = jo.getString("strStreetNum");
			info.msgGeoInfo = gi;
			GpsInfo gps = new GpsInfo();
			gps.dblAltitude = jo.getDouble("dblAltitude");
			gps.dblLat = jo.getDouble("dblLat");
			gps.dblLng = jo.getDouble("dblLng");
			gps.fltSpeed = (float) jo.getDouble("fltSpeed");
			info.msgGpsInfo = gps;

			setLocationInfo(info);
			return info;
		} catch (JSONException e) {
			LogUtil.loge("JsonError -- > " + e.toString());
		} catch (Exception e) {
			LogUtil.loge("getShared error -- > " + e.toString());
		}

		return null;
	}

	public void addLocationListener(OnLocationListener listener) {
		if (listener != null) {
			mListeners.add(listener);
		}
	}

	public void removeLocationListener(OnLocationListener listener) {
		if (listener != null) {
			if (mListeners.contains(listener)) {
				mListeners.remove(listener);
			}
		}
	}

	public void invokeLocationListener(AMapLocation location) {
		for (OnLocationListener listener : mListeners) {
			listener.onLocation(location);
		}
	}

	public void addHistoryChangeListener(OnHistoryNavigateInfoChangeListener listener) {
		mChangeListener = listener;
	}

	private void invokeHistoryChange() {
		if (mChangeListener != null) {
			mChangeListener.onNavigateListChange();
		}
	}

	public interface OnLocationListener {
		public void onLocation(AMapLocation location);
	}

	public interface OnHistoryNavigateInfoChangeListener {
		public void onNavigateListChange();
	}
}
