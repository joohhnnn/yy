package com.txznet.txz.component.roadtraffic.tencent;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.voice.VoiceData.RoadTrafficQueryInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.component.roadtraffic.IInquiryRoadTrafficListener;
import com.txznet.txz.component.roadtraffic.IRoadTrafficTool;
import com.txznet.txz.component.roadtraffic.RoadTrafficResult;
import com.txznet.txz.component.roadtraffic.RoadTrafficResult.RoadDetail;
import com.txznet.txz.component.roadtraffic.RoadTrafficResult.TrafficDetail;
import com.txznet.txz.component.roadtraffic.RoadTrafficResult.TrafficLineGps;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.roadtraffic.RoadTrafficManager;
import com.txznet.txz.module.roadtraffic.RoadTrafficManager.SearchReq;

public class RoadTrafficTencentTool implements IRoadTrafficTool{
	
	public static final String TECENT_UID = "58468c43524dd64e747e93c6";
	public static final String TENCET_KEY = "IBGBZ-C5SKV-YKEPJ-U5VP3-NBXKE-JZFBT";
	public static final String TENCET_FRONT_URL = "http://apis.map.qq.com/ws/traffic_query/v1/?";
	public static final String TENCET_ROAD_URL =   "http://apis.map.qq.com/ws/traffic_query/v1/?";
	private static RequestQueue mVolleyReqQueue = Volley
			.newRequestQueue(GlobalContext.get());

	IInquiryRoadTrafficListener mListener = null;
	SearchReq mSearchReq = new SearchReq() {
		@Override
		public void cancel() {
			JNIHelper.logd("RoadTrafficDebug:tencent cancel inqury");
			mListener = null;
		}
	};

	@Override
	public void init() {

	}

	@Override
	public SearchReq inquiryRoadTrafficByPoi(RoadTrafficQueryInfo info,
			IInquiryRoadTrafficListener listener) {
		mListener = listener;
		return inquiryRoadTraffic(0, "" + 5, info.strCity, info.strKeywords);
	}

	@Override
	public SearchReq inquiryRoadTrafficByFront(RoadTrafficQueryInfo info,
			IInquiryRoadTrafficListener listener) {
		mListener = listener;
		return inquiryRoadTraffic(0, "" + 5, info.strCity, null);
	}

	@Override
	public SearchReq inquiryRoadTrafficByNearby(RoadTrafficQueryInfo info,
			IInquiryRoadTrafficListener listener) {
		return mSearchReq;
	}

	private SearchReq inquiryRoadTraffic(int policy, String type, String city,
			String keyword) {
		TreeMap<String, String> map = new TreeMap<String, String>();

		String location;
		String url_type;
		if (TextUtils.isEmpty(keyword)) {
			location = getLocatString();
			map.put("direction", "-1");
			map.put("region", city);
			map.put("qt", "fwd_tmc");
			url_type = TENCET_FRONT_URL;
		} else {
			map.put("qt", "vct_tmc");
			map.put("keyword", keyword);
			map.put("src", "wsapi");
			StringBuilder strBuidler = new StringBuilder();
			strBuidler
					.append(LocationManager.getInstance().getLastLocation().msgGpsInfo.dblLat);
			strBuidler.append(",");
			strBuidler
					.append(LocationManager.getInstance().getLastLocation().msgGpsInfo.dblLng);
			location = strBuidler.toString();
			url_type = TENCET_ROAD_URL;
		}

		if (!location.isEmpty())
			map.put("location", location);

		map.put("policy", policy + "");
		map.put("coord_type", type);
		map.put("user_id", TECENT_UID);
		map.put("key", TENCET_KEY);
		map.put("output", "json");

		String url = encodeParams(url_type, map);
		JNIHelper.logd("RoadTrafficDebug:url=" + url);
		final JsonObjectRequest request = new JsonObjectRequest(url, null,
				new Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject json) {
						RoadTrafficResult roadTrafficResult = new RoadTrafficResult();
						try {
							JNIHelper
									.logd("RoadTrafficDebug:RoadTrafficTencentTool result json="
											+ json.toString());
							int status = json.getInt("status");
							roadTrafficResult.setErrorCode(status);
							if (status != 0) {
								JNIHelper.logd("RoadTrafficDebug: TX inquiry statue is error");
								if (mListener != null)
									mListener.onError(RoadTrafficManager.ERROR_CODE_OTHER,"状态错误");
								return;
							}

							JSONArray result = null;
							boolean isRoad = false;
							if (json.has("result")) {
								result = json.getJSONArray("result");
								isRoad = false;
							} else if (json.has("road")) {
								roadTrafficResult.setResultText(json.getString("report"));
								result = json.getJSONArray("road");
								isRoad = true;
							}
							if(result != null){
								parsePolyline(roadTrafficResult, result, isRoad);
							}
							roadTrafficResult
									.setSourceType(RoadTrafficManager.SOURCE_TYPE_TENCENT);
							if (mListener != null) {
								JNIHelper.logd("RoadTrafficDebug:tencent onResult text="+roadTrafficResult.getResultText());
								mListener.onResult(roadTrafficResult);
							}
						} catch (Exception e) {
							if (mListener != null) {
								JNIHelper
										.logd("RoadTrafficDebug: TX analyze json faule");
								mListener.onError(
										RoadTrafficManager.ERROR_CODE_OTHER,
										"解析json失败");
							}
							e.printStackTrace();
						}
					}

				}, new ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError arg0) {
						JNIHelper.logd("RoadTrafficDebug: TX inquiry timeout");
						if(mListener!=null)
							mListener.onError(
									RoadTrafficManager.ERROR_CODE_TIMEOUT, "请求超时");
					}
				});
		request.setRetryPolicy(new DefaultRetryPolicy(8000, 0, 0F));
		mVolleyReqQueue.add(request);
		return mSearchReq;
	}

	private void parsePolyline(RoadTrafficResult roadTrafficResult,JSONArray result,boolean isRoad){
		List<RoadDetail> road= new ArrayList<RoadTrafficResult.RoadDetail>();
		StringBuilder spk= new StringBuilder();
		try {
			for(int i=0;i<result.length();i++){
				if(result.isNull(i))
					continue;
				RoadDetail roadDetail =new RoadDetail();
				List<TrafficDetail> trafficList= new ArrayList<TrafficDetail>();
				JSONObject jsonObject = result.getJSONObject(i);
				JNIHelper.logd(" jsonObject.getString(\"polylin\")"+ jsonObject.getString("polyline"));
				List<TrafficLineGps> polyline=getLoactionInfoList(jsonObject.getString("polyline"));
				if(!isRoad){
					roadDetail.setReport(jsonObject.getString("report"));					
				}
				JSONArray trafficArray = jsonObject.getJSONArray("traffic");
				for(int ii = 0;ii<trafficArray.length();ii++){
					if(trafficArray.isNull(ii))
						continue;
					JSONObject trafficJson;
	
						trafficJson = trafficArray.getJSONObject(ii);
	
					int eidx =trafficJson.getInt("eidx");
					int sidx =trafficJson.getInt("sidx");
					if(eidx>sidx){
						TrafficDetail trafficDetail = new TrafficDetail();
						List<TrafficLineGps> trafficPoly= new ArrayList<TrafficLineGps>();
						for(int j = sidx;j<=eidx;j++){
							TrafficLineGps data=new TrafficLineGps();
							data.lat=polyline.get(j).lat;
							data.lng=polyline.get(j).lng;
							trafficPoly.add(data);
						}
						trafficDetail.setPolyline(polyline);
						trafficDetail.setStatus(trafficJson.getInt("level"));
						trafficList.add(trafficDetail);
					}
				}
				roadDetail.setTrafficeList(trafficList);
				if(!isRoad){
					spk.append(roadDetail.getReport());
					spk.append(";");					
				}
				road.add(roadDetail);
			} 				
		}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}		
		if(!isRoad){
			if(TextUtils.isEmpty(spk.toString()))
				spk.append("前方路况良好");
			roadTrafficResult.setResultText(spk.toString());			
		}
		roadTrafficResult.setDetailList(road);
	}

	private List<TrafficLineGps> getLoactionInfoList(String polyline) {
		if (TextUtils.isEmpty(polyline)) {
			return null;
		}
		List<TrafficLineGps> data = new ArrayList<TrafficLineGps>();
		
		List<Double> polylineList = new ArrayList<Double>();
		String[] gps = polyline.split(",");
		for(int i=0;i<gps.length;i++){
			if(TextUtils.isEmpty(gps[i]))
				continue;
			if(i<2){
				polylineList.add(Double.parseDouble(gps[i]));
			}else{
				polylineList.add(Double.parseDouble(gps[i])/1000000+polylineList.get(i-2));
			}
		}
		if(polylineList.size()<2)
			return null;
		for(int i=0;i<polylineList.size()/2;i++){
			TrafficLineGps gpsData = new TrafficLineGps();
			gpsData.lat = polylineList.get(i*2);
			gpsData.lng = polylineList.get(i*2+1);
			data.add(gpsData);
		
		}
		return data;
	}

	private String getLocatString() {
		List<GpsInfo> lastLocationList = LocationManager.getInstance()
				.getLastLocationList();
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < lastLocationList.size(); i++) {
			GpsInfo info = lastLocationList.get(i);
			if (info == null)
				continue;
			 str.append(info.dblLat);
			 str.append(',');
			 str.append(info.dblLng);//22.5663820000,113.9655370000
			if (i < lastLocationList.size() - 1)
				str.append(';');
		}
		JNIHelper.logd("RoadTrafficDebug: lastLocationList=" + str.toString());
		return str.toString();
	}

	private String encodeParams(String url, TreeMap<String, String> map) {
		StringBuilder sb = new StringBuilder(url);
		for (Entry<String, String> entry : map.entrySet()) {
			String k = entry.getKey();
			String v = entry.getValue();
			if (v == null) {
				v = "";
			}
			String val = URLEncoder.encode(v);
			sb.append(k);
			sb.append('=');
			sb.append(val);
			sb.append('&');
		}
		return sb.toString();
	}
}
