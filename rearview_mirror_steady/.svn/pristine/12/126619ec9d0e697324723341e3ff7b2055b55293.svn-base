package com.txznet.txzcar.util;

import android.util.FloatMath;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.model.LatLng;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;

public class BDLocationUtil {

	// 返回百度坐标系的Latlng
	public static LatLng getLocation(GpsInfo gps) {
		if (gps == null)
			return null;
		if (gps.uint32GpsType == null)
			gps.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		switch (gps.uint32GpsType) {
		case UiMap.GPS_TYPE_GCJ02:
			double xy[] = Convert_GCJ02_To_BD09(gps.dblLat, gps.dblLng);
			return new LatLng(xy[0], xy[1]);
		case UiMap.GPS_TYPE_BD09:
			return new LatLng(gps.dblLat, gps.dblLng);
		}
		return null;
	}

	public static double[] getGCJ02(GpsInfo gps) {
		if (gps == null)
			return null;
		if (gps.uint32GpsType == null)
			gps.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		switch (gps.uint32GpsType) {
		case UiMap.GPS_TYPE_GCJ02:
			return new double[] { gps.dblLat, gps.dblLng };
		case UiMap.GPS_TYPE_BD09:
			return Convert_BD09_To_GCJ02(gps.dblLat, gps.dblLng);
		}
		return null;
	}

	private static final double PI = 3.14159265358979324;
	private static double X_PI = PI * 3000.0 / 180.0;

	/**
	 * 中国正常坐标系GCJ02协议的坐标，转到 百度地图对应的 BD09 协议坐标
	 * 
	 * @param lat
	 * @param lng
	 */
	public static double[] Convert_GCJ02_To_BD09(double lat, double lng) {
		double x = lng, y = lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * X_PI);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * X_PI);
		lng = z * Math.cos(theta) + 0.0065;
		lat = z * Math.sin(theta) + 0.006;
		double[] point = new double[2];
		point[0] = lat;
		point[1] = lng;
		return point;
	}

	/**
	 * 百度地图对应的 BD09 协议坐标，转到 中国正常坐标系GCJ02协议的坐标
	 * 
	 * @param lat
	 * @param lng
	 */
	public static double[] Convert_BD09_To_GCJ02(double lat, double lng) {
		double x = lng - 0.0065, y = lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
		lng = z * Math.cos(theta);
		lat = z * Math.sin(theta);
		double[] point = new double[2];
		point[0] = lat;
		point[1] = lng;
		return point;
	}

	/**
	 * 百度位置信息转txz位置信息
	 */
	public static LocationInfo Convert_BDLocation_To_LocationInfo(
			BDLocation location) {
		if (null == location)
			return null;

		LocationInfo locationInfo = new LocationInfo();
		locationInfo.msgGpsInfo = new GpsInfo();
		locationInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		// locationInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_BD09;

		locationInfo.msgGpsInfo.dblLat = location.getLatitude();
		locationInfo.msgGpsInfo.dblLng = location.getLongitude();
		// if (location.hasAltitude())
		locationInfo.msgGpsInfo.dblAltitude = location.getAltitude();
		locationInfo.msgGpsInfo.fltDirection = location.getDirection();
		// if (location.hasSpeed())
		locationInfo.msgGpsInfo.fltSpeed = location.getSpeed();
		locationInfo.msgGpsInfo.fltRadius = location.getRadius();

		if (location.hasAddr()) {
			locationInfo.msgGeoInfo = new GeoInfo();
			locationInfo.msgGeoInfo.strAddr = location.getAddrStr();
			locationInfo.msgGeoInfo.strProvice = location.getProvince();
			locationInfo.msgGeoInfo.strCity = location.getCity();
			locationInfo.msgGeoInfo.strCityCode = location.getCityCode();
			locationInfo.msgGeoInfo.strDistrict = location.getDistrict();
			locationInfo.msgGeoInfo.strStreet = location.getStreet();
			locationInfo.msgGeoInfo.strStreetNum = location.getStreetNumber();
		}

		return locationInfo;
	}

	/**
	 * 
	 * @param lat_a
	 * @param lng_a
	 * @param lat_b
	 * @param lng_b
	 * @return 精确到米
	 */
	public static int calDistance(double lat_a, double lng_a, double lat_b,
			double lng_b) {
		double pk = (double) (180 / PI);

		float a1 = (float) (lat_a / pk);
		float a2 = (float) (lng_a / pk);
		float b1 = (float) (lat_b / pk);
		float b2 = (float) (lng_b / pk);

		double t1 = FloatMath.cos(a1) * FloatMath.cos(a2) * FloatMath.cos(b1)
				* FloatMath.cos(b2);
		double t2 = FloatMath.cos(a1) * FloatMath.sin(a2) * FloatMath.cos(b1)
				* FloatMath.sin(b2);
		double t3 = FloatMath.sin(a1) * FloatMath.sin(b1);
		double tt = Math.acos(t1 + t2 + t3);

		return (int) (6366000 * tt);
	}
}
