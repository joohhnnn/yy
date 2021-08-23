package com.txznet.txz.component.poi.gaode;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

public class PoiSearchToolGaodeWebImpl implements
		TXZPoiSearchManager.PoiSearchTool {
	private final static String APP_KEY = "9dea4488ab474c04aa9f5f5f3ed3c774";
	// private final static String APP_SECRET =
	// "76cf5118da06543bb72b84f50747fd04";
	private final static String APP_DOMAIN = "http://restapi.amap.com";

	public final static int MAX_NEARBY_RADIUS = 10000;

	private static RequestQueue mVolleyReqQueue = Volley
			.newRequestQueue(GlobalContext.get());

	private String encodeParams(String url, TreeMap<String, String> map) {
		StringBuilder sb = new StringBuilder();
		StringBuilder sbsign = new StringBuilder();
		// map.put("key", APP_KEY);
		for (Entry<String, String> entry : map.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();
			if (k.equals("city") && v.endsWith("市"))
				v = v.substring(0, v.length() - 1);
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
		sb.append("&key=");
		sb.append(APP_KEY);
		// sb.append("&sig=");
		// sbsign.append(APP_SECRET);
		// try {
		// MessageDigest md = MessageDigest.getInstance("MD5");
		// md.update(sbsign.toString().getBytes());
		// byte[] bs = md.digest();
		// for (byte b : bs) {
		// int n = b;
		// n &= 0xFF;
		// if (n <= 0xF) {
		// sb.append('0');
		// }
		// sb.append(Integer.toHexString(n).toUpperCase());
		// }
		// } catch (Exception e) {
		// JNIHelper.loge("gaode exception: " + e.getMessage());
		// }

		// JNIHelper.logd("gaode request url=" + sb.toString());

		return url + "?" + sb.toString();
	}

	private PoiDetail getPoiFromResult(JSONObject json) {
		JSONBuilder data = new JSONBuilder(json);
		PoiDetail poi = new PoiDetail();
		try {
			poi.setName(data.getVal("name", String.class));
			if (TextUtils.isEmpty(poi.getName()))
				return null;
			String location = data.getVal("entr_location", String.class);
			if (location == null)
				location = data.getVal("location", String.class);
			if (location == null)
				return null;
			String[] locs = location.split(",");
			if (locs.length != 2)
				return null;
			poi.setLat(Double.parseDouble(locs[1]));
			poi.setLng(Double.parseDouble(locs[0]));
			poi.setDistance(BDLocationUtil.calDistance(poi.getLat(),
					poi.getLng()));
			poi.setCity(data.getVal("cityname", String.class));
			poi.setGeoinfo(data.getVal("address", String.class));
			String alias = data.getVal("alias", String.class);
			if (alias != null) {
				poi.setAlias(alias.split("\\|"));
			}
			poi.setSourceType(Poi.POI_SOURCE_GAODE_WEB);
		} catch (Exception e) {
		}
		return poi;
	}

	private SearchReq reqSearch(CityPoiSearchOption option, String url,
			final PoiSearchResultListener listener) {
		try {
			final JsonObjectRequest req = new JsonObjectRequest(APP_DOMAIN
					+ url, null, new Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject json) {
					try {
						String status = json.getString("status");
						if (status.equals("1")) {
							JSONArray poiresults = json.getJSONArray("pois");
							List<Poi> pois = new ArrayList<Poi>();
							for (int i = 0; i < poiresults.length(); ++i) {
								JSONObject b = poiresults.getJSONObject(i);
								PoiDetail poi = getPoiFromResult(b);
								if (poi != null) {
									pois.add(poi);
								}
							}
							if (pois.isEmpty()) {
								MonitorUtil
										.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_GAODE_WEB);
								listener.onError(
										TXZPoiSearchManager.ERROR_CODE_EMPTY,
										"");
								return;
							}
							MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_GAODE_WEB);
							listener.onResult(pois);
							return;

						} else {
							JNIHelper.loge("gaode search error: "
									+ json.getString("info").toString());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					MonitorUtil
							.monitorCumulant(MonitorUtil.POISEARCH_ERROR_GAODE_WEB);
					listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
				}
			}, new ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError err) {
					if (err == null) {
						JNIHelper.loge("onErrorResponse: null");
						MonitorUtil
								.monitorCumulant(MonitorUtil.POISEARCH_ERROR_GAODE_WEB);
						listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW,
								"");
						return;
					}
					if (err instanceof TimeoutError) {
						MonitorUtil
								.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_GAODE_WEB);
						listener.onError(
								TXZPoiSearchManager.ERROR_CODE_TIMEOUT, "");
						return;
					}
					String statusCode = "null";
					if (err.networkResponse != null)
						statusCode = "" + err.networkResponse.statusCode;
					JNIHelper.loge("onErrorResponse: " + statusCode + "-"
							+ err.getMessage());
					MonitorUtil
							.monitorCumulant(MonitorUtil.POISEARCH_ERROR_GAODE_WEB);
					listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
				}
			});

			req.setRetryPolicy(new DefaultRetryPolicy(option.getTimeout(), 0,
					0F));
			mVolleyReqQueue.add(req);

			return new SearchReq() {
				@Override
				public void cancel() {
					req.cancel();
				}
			};
		} catch (Exception e) {
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_GAODE_WEB);
			listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
			return null;
		}
	}

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_GAODE_WEB);

		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("city", option.getCity());
		map.put("keywords", option.getKeywords());
		map.put("offset", "" + option.getNum());
		String url = encodeParams("/v3/place/text", map);

		return reqSearch(option, url, listener);
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			final PoiSearchResultListener listener) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_GAODE_WEB);

		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("city", option.getCity());
		map.put("keyword", option.getKeywords());
		map.put("location",
				"" + option.getCenterLng() + "," + option.getCenterLat());

		int radius = option.getRadius();
		if (radius > MAX_NEARBY_RADIUS) {
			radius = MAX_NEARBY_RADIUS;
		} else if (radius <= 0) {
			radius = TXZPoiSearchManager.DEFAULT_NEARBY_RADIUS;
		}

		map.put("radius", "" + radius);
		map.put("sortrule", "weight"); // 按距离排序：distance；综合排序：weight
		map.put("offset", "" + option.getNum());
		String url = encodeParams("/v3/place/around", map);

		return reqSearch(option, url, listener);
	}
}
