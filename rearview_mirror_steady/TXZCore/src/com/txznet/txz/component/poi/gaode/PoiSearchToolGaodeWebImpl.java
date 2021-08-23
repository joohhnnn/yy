package com.txznet.txz.component.poi.gaode;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemClock;
import android.text.TextUtils;

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
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

public class PoiSearchToolGaodeWebImpl implements
		TXZPoiSearchManager.PoiSearchTool {
	public final static String APP_KEY = "9dea4488ab474c04aa9f5f5f3ed3c774";
	// private final static String APP_SECRET =
	// "76cf5118da06543bb72b84f50747fd04";
	private final static String APP_DOMAIN = "http://restapi.amap.com";

	public final static int MAX_NEARBY_RADIUS = 10000;
	private long mSearchTime=0;
	private static RequestQueue mVolleyReqQueue = Volley
			.newRequestQueue(GlobalContext.get());
	
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
	PoiSearchResultListener mResultListener = null;
	private SearchReq reqSearch(CityPoiSearchOption option, String url,
			final PoiSearchResultListener listener) {
		mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();
//		NavManager.getInstance().reportNetStatus("gaodeweb", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		try {
			final JsonObjectRequest req = new JsonObjectRequest(APP_DOMAIN
					+ url, null, new Listener<JSONObject>() {
				@Override
				public void onResponse(JSONObject json) {
					try {
//						NavManager.getInstance().reportNetStatus("gaodeweb", "end");
						mSearchTime = SystemClock.elapsedRealtime()-mSearchTime;
						NavManager.reportBack("geodeweb", mSearchTime);
						String status = json.getString("status");
						if (status.equals("1")) {
							List<Poi> pois = new ArrayList<Poi>();
							JSONArray poiresults = json.optJSONArray("pois");
							if (poiresults != null && poiresults.length() > 0){
								for (int i = 0; i < poiresults.length(); ++i) {
									JSONObject b = poiresults.getJSONObject(i);
									PoiDetail poi = getPoiFromResult(b);
									if (poi != null) {
										pois.add(poi);
									}
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
							JNIHelper.logd("POISearchLog: PoiSearchToolGaodeWebImpl return Poi count= "+pois.size());
							NavManager.getInstance().setNomCityList(pois);
							getPoisCity(mOption.getTimeout() - mSearchTime, listener, pois);
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
//						NavManager.getInstance().reportNetStatus("gaodeweb", "end");
						NavManager.reportBack("geodeweb", SystemClock.elapsedRealtime()-mSearchTime);
						MonitorUtil
								.monitorCumulant(MonitorUtil.POISEARCH_ERROR_GAODE_WEB);
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
//						NavManager.getInstance().reportNetStatus("gaodeweb", "timeout");
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
//					NavManager.getInstance().reportNetStatus("gaodeweb", "end");
					NavManager.reportBack("geodeweb", SystemClock.elapsedRealtime()-mSearchTime);
					MonitorUtil
							.monitorCumulant(MonitorUtil.POISEARCH_ERROR_GAODE_WEB);
					listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
				}
			});

			req.setRetryPolicy(new DefaultRetryPolicy(option.getTimeout(), 0,
					0F));
			req.setShouldCache(false);
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
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_GAODE_WEB)){
			JNIHelper.logd("POISearchLog:PoiSearToolGaodeWeb is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_GAODE_WEB);
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("city", option.getCity());
		map.put("keywords", keyword);
		map.put("offset", "" + option.getNum());
		String url = encodeParams("/v3/place/text", map);
		isCitySearch=true;
		return reqSearch(option, url, listener);
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			final PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_GAODE_WEB)){
			JNIHelper.logd("POISearchLog:PoiSearToolGaodeWeb is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_GAODE_WEB);
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("city", option.getCity());
		map.put("keyword",keyword);
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
		isCitySearch=false;
		return reqSearch(option, url, listener);
	}
	public static  void getPoisCity(final long  time,final PoiSearchResultListener listener ,final List<Poi> resultList) {		
		boolean isNeedToInquiryCity = false;		
		JNIHelper.logd("POISearchLog: "+resultList.get(0).getSourceType()+" start getPoisCity");
		StringBuilder  builder = new StringBuilder();
		builder.append("http://restapi.amap.com/v3/geocode/regeo?");
		builder.append("key=");
		builder.append(PoiSearchToolGaodeWebImpl.APP_KEY);
		builder.append("&location=");
		for(int i = 0 ; i < resultList.size() ;i++){
			Poi poi = resultList.get(i);
			if(TextUtils.isEmpty(poi.getCity())){
				isNeedToInquiryCity = true;
			}
			builder.append(poi.getLng()+","+poi.getLat());
			if(i <resultList.size()-1 ){
				builder.append("|");
			}
		}
		if( !isNeedToInquiryCity ){
			listener.onResult(resultList);
			return;
		}
		builder.append("&extensions=base&batch=true");
		JNIHelper.logd("zsbin: web getPoisCity url = "+builder.toString());
		JsonObjectRequest request = new JsonObjectRequest(builder.toString(), null, 
				new Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject json) {
						try {
							JNIHelper.logd("zsbin: web json = "+json.toString());
							String statusStr = json.optString("status");
							if(!TextUtils.isEmpty(statusStr)){
								int status = Integer.parseInt(statusStr);
								if(status == 1){
									JSONArray jsonArray = json.getJSONArray("regeocodes");
									if(jsonArray != null && jsonArray.length() >0){
										for(int i = 0 ;i <  jsonArray.length() ; i++){
											JSONObject js = jsonArray.getJSONObject(i);
											js = js.getJSONObject("addressComponent");
											 String city = js.optString("city", "");
											 if(TextUtils.isEmpty(city) || city.equals("[]")){
												 city = js.getString("province");
											 }
											 resultList.get(i).setCity(city);
										}
									}
								}else{
									JNIHelper.logd("zsbin : status = "+status);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						if (listener == null) {
							return;
						}
						NavManager.getInstance().setNomCityList(resultList);
						for(Poi p:resultList){
							JNIHelper.logd("TXZPoiSearchTool p city ="+p.getCity());
						}
						JNIHelper.logd("POISearchLog: "+resultList.get(0).getSourceType()+" getPoisCity return Poi count= "+resultList.size());
						listener.onResult(resultList);
					}
				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						if (listener == null) {
							return;
						}
						JNIHelper.logd("POISearchLog: onErrorResponse error"+error);
						NavManager.getInstance().setNomCityList(resultList);						
						JNIHelper.logd("POISearchLog: PoiSearchToolGDLocalImpl return Poi count= "+resultList.size());
						listener.onResult(resultList);
					}
				});
		request.setRetryPolicy(new DefaultRetryPolicy((int )(time), 0,
				0F));
		mVolleyReqQueue.add(request);
	}
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType &  1<<(Poi.POI_SOURCE_GAODE_WEB-1)) != 0 && mResultListener != null){
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mResultListener = null;
		}
	}
	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_GAODE_WEB;
	}
}
