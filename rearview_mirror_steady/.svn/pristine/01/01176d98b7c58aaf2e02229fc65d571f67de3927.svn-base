package com.txznet.txz.component.geo;

import com.txznet.sdk.bean.Poi;

public interface IGeoCoder {
	public static final int ERROR_SUCCESS = 0;
	public static final int ERROR_TIMEOUT = 1;
	public static final int ERROR_UNKNOW = 2;

	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}

	public static class GeoInfo {
		public String address;
		public String country;
		public String province;
		public String city;
		public String district;
		public String street;
		public String street_number;
		public String country_code;
		public String desc;
		public Poi nearest; // 最近的一个poi
	}

	public static class PoiInfo {
		public double lat;
		public double lng;
		public int precise; // 位置的附加信息，是否精确查找。1为精确查找，即准确打点；0为不精确，即模糊打点
		public int confidence; // 可信度0-100
	}

	public static abstract class onGetReverseGeoCodeResultListener {
		public void onResult(GeoInfo result) {
		}

		public void onError(int errorCode) {
		}
	}

	public static abstract class onGetGeoCodeResultListener {
		public void onResult(PoiInfo result) {
		}

		public void onError(int errorCode) {
		}
	}

	public static interface Request {
		public void cancel();
	}

	public int initialize(final IInitCallback oRun);

	public Request GeoCode(String keywords, String city,
			onGetGeoCodeResultListener listener);

	public Request ReverseGeoCode(double lat, double lng,
			onGetReverseGeoCodeResultListener listener);

	public void release();
}
