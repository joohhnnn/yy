package com.txznet.txz.component.poi.dzdp;

import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

import android.text.TextUtils;

public class BussinessPoiSearchDzdpImpl implements
		TXZPoiSearchManager.PoiSearchTool {
	private final static String APP_KEY = "1094069028";
	private final static String APP_SECRET = "98cbe36ad04e4d05b6630ff798eb0504";
	private final static String APP_DOMAIN = "http://api.dianping.com";

	public final static int MAX_NEARBY_RADIUS = 5000;

	private static RequestQueue mVolleyReqQueue = Volley
			.newRequestQueue(GlobalContext.get());

	private String encodeParams(String url, TreeMap<String, String> map) {
		StringBuilder sb = new StringBuilder(url);
		StringBuilder sbsign = new StringBuilder(APP_KEY);
		sb.append('?');
		for (Entry<String, String> entry : map.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();
			if (v == null) {
				v = "";
			}
			if (k.equals("city") && v.endsWith("市"))
				v = v.substring(0, v.length() - 1);
			String val = URLEncoder.encode(v);
			sb.append(k);
			sb.append('=');
			sb.append(val);
			sb.append('&');
			sbsign.append(k);
			sbsign.append(v);
		}
		sbsign.append(APP_SECRET);
		sb.append("appkey=");
		sb.append(APP_KEY);
		sb.append("&sign=");
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(sbsign.toString().getBytes());
			byte[] bs = md.digest();
			for (byte b : bs) {
				int n = b;
				n &= 0xFF;
				if (n <= 0xF) {
					sb.append('0');
				}
				sb.append(Integer.toHexString(n).toUpperCase());
			}
		} catch (Exception e) {
		}

		// JNIHelper.logd("dzdp request url=" + sb.toString());

		return sb.toString();
	}

	private BusinessPoiDetail getPoiFromResult(JSONObject json) {
		JSONBuilder data = new JSONBuilder(json);
		BusinessPoiDetail poi = new BusinessPoiDetail();
		try {
			poi.setName(data.getVal("name", String.class));
			if (TextUtils.isEmpty(poi.getName()))
				return null;
			Double lat = data.getVal("latitude", Double.class);
			if (lat == null)
				return null;
			Double lng = data.getVal("longitude", Double.class);
			if (lng == null)
				return null;
			poi.setLat(lat);
			poi.setLng(lng);
			poi.setDistance(BDLocationUtil.calDistance(lat, lng));
			poi.setCity(data.getVal("city", String.class));
			poi.setBranchName(data.getVal("branch_name", String.class));
			poi.setCategories(data.getVal("categories", String[].class));
			poi.setRegions(data.getVal("regions", String[].class));
			poi.setAvgPrice(data.getVal("avg_price", Integer.class, 0));
			poi.setDealCount(data.getVal("deal_count", Integer.class, 0));
			poi.setHasDeal(data.getVal("has_deal", Integer.class, 0) != 0);
			poi.setGeoinfo(data.getVal("address", String.class));
			poi.setHasCoupon(data.getVal("has_coupon", Integer.class, 0) != 0);
			poi.setHasPark(false); // 暂时取不到
			poi.setHasWifi(false); // 是否有wifi获取不到
			poi.setPhotoUrl(data.getVal("s_photo_url", String.class));
			poi.setReviewCount(data.getVal("review_count", Integer.class, 0));
			poi.setScore(data.getVal("avg_rating", Float.class, 0F) * 2);
			poi.setScoreDecoration(data.getVal("decoration_grade", Float.class,
					0F));
			poi.setScoreService(data.getVal("service_grade", Float.class, 0F));
			poi.setScoreProduct(data.getVal("product_score", Float.class, 0F));
			poi.setTelephone(data.getVal("telephone", String.class));
			poi.setWebsite(data.getVal("business_url", String.class));
			poi.setSourceType(Poi.POI_SOURCE_DZDP);
		} catch (Exception e) {
		}
		return poi;
	}

	private SearchReq reqBusiness(CityPoiSearchOption option, String url,
			final PoiSearchResultListener listener) {
		JNIHelper.logd("PoiSearch Dzdp url:" + url);
		try {
			final JsonObjectRequest req = new JsonObjectRequest(APP_DOMAIN
					+ url, null, new Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject json) {
					try {
						String status = json.getString("status");
						if (status.equalsIgnoreCase("OK")) {
							JSONArray businesses = json
									.getJSONArray("businesses");
							JNIHelper
									.logd("PoiSearch dzdp OK:" + businesses != null ? businesses
											.toString() : "");
							List<Poi> pois = new ArrayList<Poi>();
							for (int i = 0; i < businesses.length(); ++i) {
								JSONObject b = businesses.getJSONObject(i);
								BusinessPoiDetail poi = getPoiFromResult(b);
								if (poi != null) {
									pois.add(poi);
								}
							}
							if (pois.isEmpty()) {
								MonitorUtil
										.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_DZDP);
								listener.onError(
										TXZPoiSearchManager.ERROR_CODE_EMPTY,
										"");
								return;
							}
							MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_DZDP);
							listener.onResult(pois);
							return;
						} else {
							JNIHelper.loge("dzdp search error: "
									+ json.getJSONObject("error").toString());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					MonitorUtil
							.monitorCumulant(MonitorUtil.POISEARCH_ERROR_DZDP);
					listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
				}
			}, new ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError err) {
					if (err == null) {
						JNIHelper.loge("onErrorResponse: null");
						MonitorUtil
								.monitorCumulant(MonitorUtil.POISEARCH_ERROR_DZDP);
						listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW,
								"");
						return;
					}
					if (err instanceof TimeoutError) {
						MonitorUtil
								.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_DZDP);
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
							.monitorCumulant(MonitorUtil.POISEARCH_ERROR_DZDP);
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
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_DZDP);
			listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
			return null;
		}
	}

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_DZDP);

		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("city", option.getCity());
		map.put("keyword", option.getKeywords());
		map.put("platform", "2"); // 1:web站链接（适用于网页应用），2:HTML5站链接（适用于移动应用和联网车载应用）
		map.put("out_offset_type", "1"); // 1:高德坐标系偏移，2:图吧坐标系偏移
		map.put("sort", "1"); // 1:默认，2:星级高优先，3:产品评价高优先，4:环境评价高优先，5:服务评价高优先，6:点评数量多优先，7:离传入经纬度坐标距离近优先，8:人均价格低优先，9：人均价格高优先
		// map.put("page", "1");
		map.put("limit", "" + option.getNum());
		String url = encodeParams("/v1/business/find_businesses", map);

		return reqBusiness(option, url, listener);
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			final PoiSearchResultListener listener) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_DZDP);

		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("city", option.getCity());
		map.put("keyword", option.getKeywords());
		map.put("latitude", "" + option.getCenterLat());
		map.put("longitude", "" + option.getCenterLng());
		int radius = option.getRadius();
		if (radius > MAX_NEARBY_RADIUS) {
			radius = MAX_NEARBY_RADIUS;
		} else if (radius <= 0) {
			radius = TXZPoiSearchManager.DEFAULT_NEARBY_RADIUS;
		}
		map.put("radius", "" + radius);
		map.put("platform", "2"); // 1:web站链接（适用于网页应用），2:HTML5站链接（适用于移动应用和联网车载应用）
		map.put("out_offset_type", "1"); // 1:高德坐标系偏移，2:图吧坐标系偏移
		map.put("sort", "1"); // 1:默认，2:星级高优先，3:产品评价高优先，4:环境评价高优先，5:服务评价高优先，6:点评数量多优先，7:离传入经纬度坐标距离近优先，8:人均价格低优先，9：人均价格高优先
		// map.put("page", "1");
		if (option.getNum() > 40)
			map.put("limit", "40");
		else
			map.put("limit", "" + option.getNum());
		String url = encodeParams("/v1/business/find_businesses", map);

		return reqBusiness(option, url, listener);
	}
}
