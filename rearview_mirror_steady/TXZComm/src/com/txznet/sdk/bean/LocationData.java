package com.txznet.sdk.bean;

import android.os.Bundle;

/**
 * 当前位置信息
 * 
 */
public class LocationData {
	/**
	 * GPS类型，统一转为GPS_TYPE_GCJ02传递给native
	 */
	public Integer gps_type = null;
	/**
	 * GPS纬度
	 */
	public Double dbl_lat = null;
	/**
	 * GPS经度
	 */
	public Double dbl_lng = null;
	/**
	 * 方向
	 */
	public Float flt_direction = null;
	/**
	 * 速度
	 */
	public Float flt_speed = null;
	/**
	 * 高度
	 */
	public Double dbl_altitude = null;
	/**
	 * 经度半径
	 */
	public Float flt_radius = null;
	/**
	 * 估算值
	 */
	public Float accuracy = null;

	/**
	 * 详细地址信息
	 */
	public String str_addr = null;
	/**
	 * 省份
	 */
	public String str_provice = null;
	/**
	 * 城市名
	 */
	public String str_city = null;
	/**
	 * 城市编码
	 */
	public String str_city_code = null;
	/**
	 * 区/县信息
	 */
	public String str_district = null;
	/**
	 * 街道信息
	 */
	public String str_street = null;
	
	/**
	 * 街道号码
	 */
	public String str_street_num = null;

	public Bundle extra_bundle = null;

	public void reset() {
		gps_type = null;
		dbl_lat = null;
		dbl_lng = null;
		flt_direction = null;
		flt_speed = null;
		dbl_altitude = null;
		flt_radius = null;
		str_addr = null;
		str_provice = null;
		str_city = null;
		str_city_code = null;
		str_district = null;
		str_street = null;
		str_street_num = null;
		accuracy = null;
	}

	@Override
	public String toString() {
		return "LocationData [gps_type=" + gps_type + ", dbl_lat=" + dbl_lat
				+ ", dbl_lng=" + dbl_lng + ", flt_direction=" + flt_direction
				+ ", flt_speed=" + flt_speed + ", dbl_altitude=" + dbl_altitude
				+ ", flt_radius=" + flt_radius + ", accuracy=" + accuracy
				+ ", str_addr=" + str_addr + ", str_provice=" + str_provice
				+ ", str_city=" + str_city + ", str_city_code=" + str_city_code
				+ ", str_district=" + str_district + ", str_street="
				+ str_street + ", str_street_num=" + str_street_num
				+ ", extra_bundle=" + extra_bundle + "]";
	}
}
