package com.txznet.txz.component.roadtraffic;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.txznet.comm.util.JSONBuilder;

public class RoadTrafficResult {
	
	private String resultText;
	
	private List<RoadDetail> detailList=new ArrayList<RoadTrafficResult.RoadDetail>();
	
	private int sourceType;
	
	private int errorCode;
	//TODO
	public static class RoadDetail{
		String name;
		String report;
		List<TrafficDetail> trafficeList ;
		
		public static List<RoadDetail> fromJSONArray(JSONArray jsonArray){
			if(jsonArray==null)
				return null;
			List<RoadDetail> result=new ArrayList<RoadTrafficResult.RoadDetail>();
			for(int i =0;i<jsonArray.length();i++){
				if(jsonArray.isNull(i))
					continue;
				try {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					RoadDetail detail = new RoadDetail();
					detail.setName(jsonObject.getString("name"));
					detail.setReport(jsonObject.getString("report"));
					detail.setTrafficeList(TrafficDetail.fromJSONArray(new JSONArray("traffice")));
					result.add(detail);
				} catch (JSONException e) {
					
				}
			}
			if(result.size()>0)
				return result;
			return null;
		}
		
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getReport() {
			return report;
		}
		public void setReport(String report) {
			this.report = report;
		}
		public List<TrafficDetail> getTrafficeList() {
			return trafficeList;
		}
		public void setTrafficeList(List<TrafficDetail> trafficeList) {
			this.trafficeList = trafficeList;
		}	
	}
	public static class TrafficLineGps{
		public double lng;
		public double lat;
	}
	
	//TODO
	public static class TrafficDetail{
		String repore;
		List<TrafficLineGps>polyline;
		int status;
		
		public static List<TrafficDetail> fromJSONArray(JSONArray jsonArray){
			if(jsonArray==null)
				return null;
			List<TrafficDetail> result=new ArrayList<RoadTrafficResult.TrafficDetail>();
			for(int i =0;i<jsonArray.length();i++){
				if(jsonArray.isNull(i))
					continue;
				try {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					TrafficDetail detail = new TrafficDetail();
					detail.setRepore(jsonObject.getString("repore"));
					detail.setStatus(jsonObject.getInt("status"));
					detail.setPolyline(getPolylineFromString(jsonObject.getString("polyline")));
					result.add(detail);
				} catch (JSONException e) {
					
				}
			}
			if(result.size()>0)
				return result;
			return null;
		}
		private static List<TrafficLineGps> getPolylineFromString(String str) {
			if(TextUtils.isEmpty(str))
				return null;
			List<TrafficLineGps> result=new ArrayList<TrafficLineGps>();
			String[] split = str.split(";");
			for(String gps:split){
				if(TextUtils.isEmpty(str))
					continue;
				String[] split2 = gps.split(",");
				TrafficLineGps gpsData = null;
				gpsData.lat=new Double(split2[0]);
				gpsData.lng=new Double(split2[1]);
				result.add(gpsData);
			}
			if(result.size()==0)
				return null;
			return result;
		}

		public String getRepore() {
			return repore;
		}
		public void setRepore(String repore) {
			this.repore = repore;
		}
		public List<TrafficLineGps> getPolyline() {
			return polyline;
		}
		public void setPolyline(List<TrafficLineGps> polyline) {
			this.polyline = polyline;
		}
		public int getStatus() {
			return status;
		}
		public void setStatus(int status) {
			this.status = status;
		}

	}
	
	public String getPolyline(){		
		JSONArray jsArray= new JSONArray();
		if(detailList==null||detailList.size()==0)
			return null;
		for(RoadDetail road:detailList){
			if(road.trafficeList==null||road.trafficeList.size()==0)
				continue;
			for(TrafficDetail traffic:road.trafficeList){
				if(traffic.polyline==null||traffic.polyline.size()==0)
					continue;
				StringBuilder str = new StringBuilder();
				for(TrafficLineGps data:traffic.polyline){
					str.append(data.lat);
					str.append(",");
					str.append(data.lng);
					str.append(";");
				}
				JSONObject js = new JSONObject();
				try {
					js.put("line", str.toString());
					js.put("status", traffic.status);
					jsArray.put(js);
				} catch (Exception e) {
				}
			}

		}
		return jsArray.toString();
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	//TODO
	protected JSONBuilder toJsonObj() {
		JSONBuilder json = new JSONBuilder();
		return json;
	}

	public String toString() {
		JSONBuilder json = toJsonObj();
		return json.toString();
	}

	public JSONObject toJsonObject() {
		JSONBuilder json = toJsonObj();
		return json.build();
	}

	protected void fromJsonObject(JSONBuilder json) {
		JSONObject js= json.build();
		try {
			resultText= js.getString("resultText");
			sourceType= js.getInt("sourceType");
			errorCode= js.getInt("code");
			detailList= RoadDetail.fromJSONArray(js.getJSONArray("detail"));
		} catch (Exception e) {
			
		}
	}

	public static RoadTrafficResult fromString(String data) {
		RoadTrafficResult p = new RoadTrafficResult();
		JSONBuilder json = new JSONBuilder(data);
		p.fromJsonObject(json);
		return p;
	}
	
	public String getResultText() {
		return resultText;
	}

	public void setResultText(String resultText) {
		this.resultText = resultText;
	}

	public List<RoadDetail> getDetailList() {
		return detailList;
	}

	public void setDetailList(List<RoadDetail> detailList) {
		this.detailList = detailList;
	}

	public int getSourceType() {
		return sourceType;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}
	

}
