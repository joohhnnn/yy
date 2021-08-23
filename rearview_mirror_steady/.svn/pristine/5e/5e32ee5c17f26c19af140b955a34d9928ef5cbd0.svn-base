package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class ChatMapViewData extends ViewData{
	public String mapData;
	public List<LineInfo> lineData;
	public double centerLat;
	public double centerLng;
	public Double minLat;
	public Double minLng;
	public Double maxLat;
	public Double maxLng;
	public Double myLat;
	public Double myLng;
	public static class LineInfo{
		public List<Double[]> list ;
		public int statue = 0;
		public String msg=null;
	}
	public ChatMapViewData() {
		super(TYPE_CHAT_MAP);
	}
	void  setMapData(String data) {
		this.mapData=data;
	}
	public void parseData(String data) {
		if(TextUtils.isEmpty(data))
			return;
		lineData= new ArrayList<ChatMapViewData.LineInfo>();
		try {
			
			JSONObject js = new JSONObject(data);
			if(js.has("local")){
				String myLocal = js.getString("local");
				if(!TextUtils.isEmpty(myLocal)){
					myLat=Double.parseDouble(myLocal.split(",")[0]);
					myLng=Double.parseDouble(myLocal.split(",")[1]);
					setCentPoint(new Double[]{myLat,myLng});
				}				
			}
			if(js.has("polyLine")){
				String polyLine = js.getString("polyLine");
				if(!TextUtils.isEmpty(polyLine)){
					JSONArray trafficArray =new JSONArray(polyLine);
					for(int i=0;i<trafficArray.length();i++){
						if(!trafficArray.isNull(i)){
							LineInfo info = new LineInfo();
							JSONObject json= trafficArray.getJSONObject(i);
							info.statue= json.getInt("status");
							info.list= getLinePoint(json.getString("line"));
							lineData.add(info);
						}
					}
				}				
			}
		} catch (Exception e) {
		}
		if(!(lineData.size()>0)){
			lineData=null;
		}
	}
	
	private List<Double[]> getLinePoint(String str) {
		if(TextUtils.isEmpty(str))
			return null;
		List<Double[]>result = new ArrayList<Double[]>();
		String[] split = str.split(";");
		for(String point:split){
			if(!TextUtils.isEmpty(point)){
				String[] split2 = point.split(",");
				Double[] gps={(double) 0,(double) 0};
				gps[0]=Double.parseDouble(split2[0]) ;
				gps[1]=Double.parseDouble(split2[1]);
				setCentPoint(gps);
				result.add(gps);			
			}
		}
		if(result.size()>0){
			return result;
		}
		return null;
	}
	
	private void setCentPoint(Double[] gps){
		double lat= gps[0];
		double lng= gps[1];
		if(minLat==null){
			minLat=lat;
			maxLat=lat;
			minLng=lng;
			maxLng=lng;
		}else{
			if(lat>maxLat){
				maxLat=lat;
			}
			if(lat<minLat){
				minLat=lat;
			}
			if(lng>maxLng){
				maxLng=lng;
			}
			if(lng<minLng){
				minLng=lng;
			}
		}
	}
}
