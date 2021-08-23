package com.txznet.txz.ui.win.nav;

import com.amap.api.maps.AMapUtils;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.txz.module.location.LocationManager;

public class BDLocationUtil {
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
		return (int) AMapUtils.calculateLineDistance(
				new com.amap.api.maps.model.LatLng(lat_a, lng_a),
				new com.amap.api.maps.model.LatLng(lat_b, lng_b));

		// double pk = (double) (180 / PI);
		//
		// float a1 = (float) (lat_a / pk);
		// float a2 = (float) (lng_a / pk);
		// float b1 = (float) (lat_b / pk);
		// float b2 = (float) (lng_b / pk);
		//
		// double t1 = FloatMath.cos(a1) * FloatMath.cos(a2) * FloatMath.cos(b1)
		// * FloatMath.cos(b2);
		// double t2 = FloatMath.cos(a1) * FloatMath.sin(a2) * FloatMath.cos(b1)
		// * FloatMath.sin(b2);
		// double t3 = FloatMath.sin(a1) * FloatMath.sin(b1);
		// double tt = Math.acos(t1 + t2 + t3);
		//
		// return (int) (6366000 * tt);
	}

	/**
	 * 计算和当前点的距离
	 * 
	 * @param lat
	 * @param lng
	 * @return
	 */
	public static int calDistance(double lat, double lng) {
		LocationInfo loc = LocationManager.getInstance().getLastLocation();
		try {
			return calDistance(loc.msgGpsInfo.dblLat, loc.msgGpsInfo.dblLng,
					lat, lng);
		} catch (Exception e) {
			return 0;
		}
	}
}
