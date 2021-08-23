package com.txznet.txz.component.poi.baidu;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemClock;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

public class PoiSearchToolBaiduWebImpl implements
		TXZPoiSearchManager.PoiSearchTool {
	public final static int MAX_NEARBY_RADIUS = 10000;

	private final static String APP_KEY = "yB75f4N8uRUtSyqqFZuq3G7I";
	// private final static String APP_SECRET =
	// "76cf5118da06543bb72b84f50747fd04";
	private final static String APP_DOMAIN = "http://api.map.baidu.com";
	private long mSearchTime= 0;
	private static RequestQueue mVolleyReqQueue = Volley.newRequestQueue(GlobalContext.get());
	
	PoiSearchOption mOption;
	private int mRetryCount=-1;
	boolean isCitySearch=false;
	
	private String encodeParams(String url, TreeMap<String, String> map) {
		StringBuilder sb = new StringBuilder();
		StringBuilder sbsign = new StringBuilder();
		// map.put("key", APP_KEY);
		for (Entry<String, String> entry : map.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();
			if (k.equals("region") && v.endsWith("市"))
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
		sb.append("&ak=");
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
		// JNIHelper.loge("baidu exception: " + e.getMessage());
		// }

		// JNIHelper.logd("baidu request url=" + sb.toString());

		return url + "?" + sb.toString();
	}

	private PoiDetail getPoiFromResult(JSONObject json) {
		JSONBuilder data = new JSONBuilder(json);
		PoiDetail poi = new PoiDetail();
		try {
			poi.setName(data.getVal("name", String.class));
			if (TextUtils.isEmpty(poi.getName()))
				return null;
			JSONObject location = data.getVal("location", JSONObject.class);
			if (location == null)
				return null;
			poi.setLat(location.getDouble("lat"));
			poi.setLng(location.getDouble("lng"));
			poi.setDistance(BDLocationUtil.calDistance(poi.getLat(),
					poi.getLng()));
			poi.setGeoinfo(data.getVal("address", String.class));
			poi.setSourceType(Poi.POI_SOURCE_BAIDU_WEB);
		} catch (Exception e) {
			return null;
		}
		return poi;
	}

	private SearchReq reqSearch(CityPoiSearchOption option, String url,
			final PoiSearchResultListener listener) {
        mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();
//		NavManager.getInstance().reportNetStatus("baiduweb", "begin");
		
		mSearchTime=SystemClock.elapsedRealtime();
		try {
			final JsonObjectRequest req = new JsonObjectRequest(APP_DOMAIN
					+ url, null, new Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject json) {
//					NavManager.getInstance().reportNetStatus("baiduweb", "end");
					mSearchTime = SystemClock.elapsedRealtime()-mSearchTime;
					NavManager.reportBack("baiduweb",mSearchTime);
					try {
						int status = json.optInt("status", -1);
						if (status == 0) {
							JSONArray poiresults = json.getJSONArray("results");
							List<Poi> pois = new ArrayList<Poi>();
							for (int i = 0; i < poiresults.length(); ++i) {
								JSONObject b = poiresults.getJSONObject(i);
								PoiDetail poi = getPoiFromResult(b);
								if (poi != null) {
									pois.add(poi);
								}
							}
							if (pois.isEmpty()) {
								MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_BAIDU_WEB);
								listener.onError(
										TXZPoiSearchManager.ERROR_CODE_EMPTY,
										"");
								return;
							}
							MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_BAIDU_WEB);
							JNIHelper.logd("POISearchLog:PoiSearchToolBaiduLocalImpl return Poi count= "+pois.size());
							NavManager.getInstance().setNomCityList(pois);
							PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout() - mSearchTime, listener, pois);
							return;

						} else {
							JNIHelper.loge("baidu search error: "
									+ json.getString("message").toString());
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
					MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_BAIDU_WEB);
					listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
				}
			}, new ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError err) {
					if (err == null) {
						JNIHelper.loge("onErrorResponse: null");
//						NavManager.getInstance().reportNetStatus("baiduweb", "end");
						NavManager.reportBack("baiduweb", SystemClock.elapsedRealtime()-mSearchTime);
						MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_BAIDU_WEB);
						listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW,
								"");
						return;
					}
					if (err instanceof TimeoutError) {
						JNIHelper.logd("POISearchLog:"+getClass().toString()+" mRetryCount="+mRetryCount);
						if(mRetryCount>0){
							mRetryCount--;
							if(isCitySearch){
								searchInCity((CityPoiSearchOption)mOption, listener);
							}else{
								searchNearby((NearbyPoiSearchOption)mOption, listener);
							}
							return;
						}
//						NavManager.getInstance().reportNetStatus("baiduweb", "timeout");
						MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_BAIDU_WEB);
						listener.onError(
								TXZPoiSearchManager.ERROR_CODE_TIMEOUT, "");
						return;
					}
					String statusCode = "null";
					if (err.networkResponse != null)
						statusCode = "" + err.networkResponse.statusCode;
					JNIHelper.loge("onErrorResponse: " + statusCode + "-"
							+ err.getMessage());
//					NavManager.getInstance().reportNetStatus("baiduweb", "end");
					NavManager.reportBack("baiduweb", SystemClock.elapsedRealtime()-mSearchTime);
					MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_BAIDU_WEB);
					listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
				}
			}) {
				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					Map<String, String> hs = new HashMap<String, String>();
					hs.put("Referer", "www.txzing.com");
					return hs;
				}
			};

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
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_BAIDU_WEB);
			listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
			return null;
		}
	}

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_BAIDU_WEB)){
			JNIHelper.logd("POISearchLog:PoiSearToolBaiduWeb is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_BAIDU_WEB);
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("region",option.getCity());
		map.put("query",keyword);
		map.put("output", "json");
		int num = option.getNum();
		if (num > 20)
			num = 20;
		map.put("page_size", "" + num);
		map.put("coord_type", "2"); // 坐标类型，1（wgs84ll），2（gcj02ll），3（bd09ll），4（bd09mc）
		String url = encodeParams("/place/v2/search", map);
		
		isCitySearch=true;
		return reqSearch(option, url, listener);
	}
	PoiSearchResultListener mResultListener = null;
	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			final PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_BAIDU_WEB)){
			JNIHelper.logd("POISearchLog:PoiSearToolBaiduWeb is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_BAIDU_WEB);

		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("location",
				"" + option.getCenterLat() + "," + option.getCenterLng());
		int radius = option.getRadius();
		if (radius > MAX_NEARBY_RADIUS) {
			radius = MAX_NEARBY_RADIUS;
		} else if (radius <= 0) {
			radius = MAX_NEARBY_RADIUS;
		}
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		map.put("radius", "" + radius);
		map.put("query", keyword);
		map.put("output", "json");
		int num = option.getNum();
		if (num > 20)
			num = 20;
		map.put("page_size", "" + num);
		map.put("coord_type", "2"); // 坐标类型，1（wgs84ll），2（gcj02ll），3（bd09ll），4（bd09mc）
		String url = encodeParams("/place/v2/search", map);

		isCitySearch=false;
		return reqSearch(option, url, listener);
	}
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType &  1<<(Poi.POI_SOURCE_BAIDU_WEB-1)) != 0 && mResultListener != null){
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mResultListener = null;
		}
	}
	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_BAIDU_WEB;
	}
}
