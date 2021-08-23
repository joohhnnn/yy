package com.txznet.txz.component.geo.baidu;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.txz.component.geo.IGeoCoder;
import com.txznet.txz.jni.JNIHelper;

public class GeoCoderBaiduWebImpl implements IGeoCoder {

	private final static String APP_KEY = "yB75f4N8uRUtSyqqFZuq3G7I";
	private final static String APP_DOMAIN = "http://api.map.baidu.com";

	private static RequestQueue mVolleyReqQueue = Volley
			.newRequestQueue(GlobalContext.get());

	@Override
	public int initialize(final IInitCallback oRun) {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				oRun.onInit(true);
			}
		}, 0);
		return 0;
	}

	private String encodeParams(String url, TreeMap<String, String> map) {
		StringBuilder sb = new StringBuilder();
		StringBuilder sbsign = new StringBuilder();
		map.put("output", "json");
		for (Entry<String, String> entry : map.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();
			String val = URLEncoder.encode(v);
			if (sb.length() != 0) {
				sb.append('&');
			}
			sb.append(k);
			sb.append('=');
			sb.append(val);
			if (sbsign.length() != 0) {
				sbsign.append('&');
			}
			sbsign.append(k);
			sbsign.append('=');
			sbsign.append(v);
		}
		sb.append("&ak=");
		sb.append(APP_KEY);

		// JNIHelper.logd("baidu request url=" + sb.toString());

		return url + "?" + sb.toString();
	}

	@Override
	public Request GeoCode(String keywords, String city,
			final onGetGeoCodeResultListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Request ReverseGeoCode(double lat, double lng,
			final onGetReverseGeoCodeResultListener listener) {
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("coordtype", "gcj02ll");// bd09ll（百度经纬度坐标）、bd09mc（百度米制坐标）、gcj02ll（国测局经纬度坐标）、wgs84ll
		map.put("location", "" + lat + "," + lng);
		map.put("pois", "1");// 是否显示周边100米内的poi。
		String url = encodeParams("/geocoder/v2/", map);
		try {
			final JsonObjectRequest req = new JsonObjectRequest(APP_DOMAIN
					+ url, null, new Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject json) {
					try {
						int status = json.optInt("status", -1);
						if (status == 0) {
							json = json.optJSONObject("result");
							GeoInfo result = new GeoInfo();
							result.address = json
									.optString("formatted_address");
							JSONObject addr = json
									.optJSONObject("addressComponent");
							if (addr != null) {
								result.country = addr.optString("country");
								result.province = addr.optString("province");
								result.city = addr.optString("city");
								result.district = addr.optString("district");
								result.street = addr.optString("street");
								result.street_number = addr
										.optString("street_number");
								result.country_code = addr
										.optString("country_code");
							}
							result.desc = json.optString("sematic_description");
							JSONArray pois = json.optJSONArray("pois");
							if (pois != null && pois.length() > 0) {
								do {
									PoiDetail p = new PoiDetail();
									JSONObject poi = pois.getJSONObject(0);
									p.setName(poi.optString("name", null));
									p.setGeoinfo(poi.optString("addr", null));
									JSONObject xy = poi.optJSONObject("point");
									if (xy == null)
										break;
									p.setLat(xy.optDouble("y", 0));
									p.setLng(xy.optDouble("x", 0));
									p.setTelephone(poi.optString("tel", null));
									result.nearest = p;
								} while (false);
							}

							listener.onResult(result);
							return;
						} else {
							JNIHelper.loge("baidu geo coder error: "
									+ json.getString("message").toString());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					listener.onError(ERROR_UNKNOW);
				}
			}, new ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError err) {
					if (err == null) {
						JNIHelper.loge("onErrorResponse: null");
						listener.onError(ERROR_UNKNOW);
						return;
					}
					if (err instanceof TimeoutError) {
						listener.onError(ERROR_TIMEOUT);
						return;
					}
					String statusCode = "null";
					if (err.networkResponse != null)
						statusCode = "" + err.networkResponse.statusCode;
					JNIHelper.loge("onErrorResponse: " + statusCode + "-"
							+ err.getMessage());
					listener.onError(ERROR_UNKNOW);
				}
			}) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> hs = new HashMap<String, String>();
					hs.put("Referer", "www.txzing.com");
					return hs;
				}
			};

			req.setRetryPolicy(new DefaultRetryPolicy(10000, 0, 0F)); // 设置超时

			mVolleyReqQueue.add(req);

			return new Request() {
				@Override
				public void cancel() {
					req.cancel();
				}
			};
		} catch (Exception e) {
			listener.onError(ERROR_UNKNOW);
			return null;
		}
	}

	@Override
	public void release() {
	}

}
