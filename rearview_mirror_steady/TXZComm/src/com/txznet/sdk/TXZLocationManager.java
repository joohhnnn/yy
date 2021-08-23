package com.txznet.sdk;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.sdk.TXZService.CommandProcessor;
import com.txznet.sdk.bean.LocationData;

/**
 * 位置管理器
 *
 */
public class TXZLocationManager {
	private static TXZLocationManager sInstance = new TXZLocationManager();

	private TXZLocationManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static TXZLocationManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mHasSetTool) {
			if (mGpsTool == null) {
				setGpsTool((GpsToolType) null);
			} else if (mGpsTool instanceof GpsTool) {
				setGpsTool((GpsTool) mGpsTool);
			} else if (mGpsTool instanceof GpsToolType) {
				setGpsTool((GpsToolType) mGpsTool);
			}
		}
		
	}

	/**
	 * GPS工具类
	 * 
	 * @author txz
	 *
	 */
	/*public*/ static interface GpsTool {
		/**
		 * 获取最后已知的位置
		 * 
		 * @return
		 */
		public Location getLastKnownLocation();

		public void requestLocationUpdates(long minTime, float minDistance,
				LocationListener listener);
	}
	
	/**
	 * 位置监听器
	 */
	public static interface OnLocationListener {
		public void onLocationUpdate(LocationData data);
	}

	private boolean mHasSetTool = false;
	private Object mGpsTool = null;
	
	private OnLocationListener mListener;
	private boolean mHasSetListener;

	/**
	 * 设置gps工具
	 * 
	 * @param tool
	 */
	/*public*/ void setGpsTool(GpsTool tool) {
		mHasSetTool = true;
		mGpsTool = tool;
	}
	
	/**
	 * 设置位置更新监听器
	 * @param listener
	 */
	public void setLocationListener(OnLocationListener listener) {
		mListener = listener;
		if (mHasSetListener) {
			return;
		}
		mHasSetListener = true;

		TXZService.setCommandProcessor("tool.loc.", new CommandProcessor() {

			@Override
			public byte[] process(String packageName, String command, byte[] data) {
				if (command.equals("updateLoc")) {
					if (mListener != null) {
						LocationInfo locationInfo;
						try {
							locationInfo = LocationInfo.parseFrom(data);
							convertToLocation(locationInfo);
							mListener.onLocationUpdate(mGpsLocation);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				return null;
			}
		});
	}
	
	/**
	 * 内置定位工具类型
	 */
	public static enum GpsToolType {
		/**
		 * 同行者
		 */
		TXZ,
		/**
		 * 360定位
		 */
		QIHOO,
		/**
		 * 高德定位
		 */
		AMAP
	}
	
	/**
	 * 设置gps工具
	 */
	public void setGpsTool(GpsToolType type) {
		mHasSetTool = true;
		mGpsTool = type;
		if (type == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.loc.cleartool", null, null);
			return;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.loc.setInnerTool", type.name().getBytes(),
				null);
	}
	
	

	/**
	 * 获取当前的位置信息
	 * @return
	 */
	public LocationData getCurrentLocationInfo() {
		if (mGpsTool != null && mGpsTool instanceof GpsTool) {
			convertLocation(((GpsTool)mGpsTool).getLastKnownLocation());
			return mGpsLocation;
		}

		byte[] locInfo = ServiceManager.getInstance().sendTXZInvokeSync(
				"txz.loc.getLocation", null);
		if (locInfo != null) {
			try {
				LocationInfo locationInfo = LocationInfo.parseFrom(locInfo);
				convertToLocation(locationInfo);
				LogUtil.logd("mGpsLocation:" + mGpsLocation.toString());
				return mGpsLocation;
			} catch (Exception e) {
				return null;
			}
		}

		return null;
	}

	private LocationData mGpsLocation;

	public LocationData convertLocationData(LocationInfo info) {
		if (info == null) {
			return null;
		}

		LocationData data = new LocationData();
		GeoInfo geoInfo = info.msgGeoInfo;
		GpsInfo gpsInfo = info.msgGpsInfo;

		if (geoInfo == null && gpsInfo == null) {
			return null;
		}


		if (gpsInfo != null) {
			data.gps_type = gpsInfo.uint32GpsType;
			data.dbl_lat = gpsInfo.dblLat;
			data.dbl_lng = gpsInfo.dblLng;
			data.flt_direction = gpsInfo.fltDirection;
			data.flt_speed = gpsInfo.fltSpeed;
			data.dbl_altitude = gpsInfo.dblAltitude;
			data.flt_radius = gpsInfo.fltRadius;
		}

		if (geoInfo != null) {
			data.str_addr = geoInfo.strAddr;
			data.str_provice = geoInfo.strProvice;
			data.str_city = geoInfo.strCity;
			data.str_city_code = geoInfo.strCityCode;
			data.str_district = geoInfo.strDistrict;
			data.str_street = geoInfo.strStreet;
			data.str_street_num = geoInfo.strStreetNum;
		}

		data.accuracy = null;
		data.extra_bundle = null;
		
		return data;
	}
	
	private void convertToLocation(LocationInfo info) {
		if (info == null) {
			return;
		}

		GeoInfo geoInfo = info.msgGeoInfo;
		GpsInfo gpsInfo = info.msgGpsInfo;

		if (geoInfo == null && gpsInfo == null) {
			return;
		}

		if (mGpsLocation == null) {
			mGpsLocation = new LocationData();
		}

		if (gpsInfo != null) {
			mGpsLocation.gps_type = gpsInfo.uint32GpsType;
			mGpsLocation.dbl_lat = gpsInfo.dblLat;
			mGpsLocation.dbl_lng = gpsInfo.dblLng;
			mGpsLocation.flt_direction = gpsInfo.fltDirection;
			mGpsLocation.flt_speed = gpsInfo.fltSpeed;
			mGpsLocation.dbl_altitude = gpsInfo.dblAltitude;
			mGpsLocation.flt_radius = gpsInfo.fltRadius;
		}

		if (geoInfo != null) {
			mGpsLocation.str_addr = geoInfo.strAddr;
			mGpsLocation.str_provice = geoInfo.strProvice;
			mGpsLocation.str_city = geoInfo.strCity;
			mGpsLocation.str_city_code = geoInfo.strCityCode;
			mGpsLocation.str_district = geoInfo.strDistrict;
			mGpsLocation.str_street = geoInfo.strStreet;
			mGpsLocation.str_street_num = geoInfo.strStreetNum;
		}

		mGpsLocation.accuracy = null;
		mGpsLocation.extra_bundle = null;
	}

	private void convertLocation(Location location) {
		if (location == null) {
			if (mGpsLocation != null) {
				mGpsLocation.reset();
			}
			return;
		}

		Float accu = location.getAccuracy();
		Double alt = location.getAltitude();
		Float bear = location.getBearing();
		Bundle bundle = location.getExtras();
		Double lat = location.getLatitude();
		Double lng = location.getLongitude();
		Float speed = location.getSpeed();

		if (mGpsLocation == null) {
			mGpsLocation = new LocationData();
		}

		mGpsLocation.gps_type = null;
		mGpsLocation.dbl_lat = lat;
		mGpsLocation.dbl_lng = lng;
		mGpsLocation.flt_direction = bear;
		mGpsLocation.flt_speed = speed;
		mGpsLocation.dbl_altitude = alt;
		mGpsLocation.flt_radius = null;
		mGpsLocation.accuracy = accu;
		mGpsLocation.extra_bundle = bundle;

		mGpsLocation.str_addr = null;
		mGpsLocation.str_provice = null;
		mGpsLocation.str_city = null;
		mGpsLocation.str_city_code = null;
		mGpsLocation.str_district = null;
		mGpsLocation.str_street = null;
		mGpsLocation.str_street_num = null;
	}
}