package com.txznet.sdk.bean;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class PoiDeepInfo {

	public static final int DEEPINFO_CATEGORY_PARKING = 0;
	public static final int DEEPINFO_CATEGORY_STATION = 1;
	public static final int DEEPINFO_CATEGORY_HOTEL = 2;

	public int category;
	public String tag;
	public Object tagInfo;
	public List<String> feature;

	public static class PackingInfo {
		public String priceInfo;
		public String parkinginfo;

		public static PackingInfo parseFromString(JSONObject info) {
			try {
				String str = info.getString("taginfo");
				JSONObject js = new JSONObject(str);
				PackingInfo packingInfo = new PackingInfo();
				if (js.has("priceinfo"))
					packingInfo.priceInfo = js.getString("priceinfo ");
				if (js.has("parkinginfo"))
					packingInfo.parkinginfo = js.getString("parkinginfo");
				return packingInfo;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	public static class StationInfo {
		public String type;
		public String price;
		public String pricetag;

		public static List<StationInfo> parseFromString(JSONObject info) {
			try {
				String str = info.getString("taginfo");
				JSONArray js = new JSONArray(str);
				List<StationInfo> result = new ArrayList<StationInfo>();
				for (int i = 0; i < js.length(); i++) {
					if (js.isNull(i))
						continue;
					JSONObject json = js.getJSONObject(i);
					StationInfo stationInfo = new StationInfo();
					stationInfo.type = json.getString("type").replace("#", "号");
					stationInfo.price = json.getString("price");
					stationInfo.pricetag = json.getString("pricetag");
					result.add(stationInfo);
				}
				return result;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	public static PoiDeepInfo parseFromString(String str) {
		PoiDeepInfo result = new PoiDeepInfo();
		if (TextUtils.isEmpty(str))
			return null;
		try {
			JSONObject js = new JSONObject(str);
			result.category = js.getInt("category");
			result.tag = js.getString("tag");
			getTagInfo(result, js);
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		
	}

	private static void getTagInfo(PoiDeepInfo result,  JSONObject js ) {
			Object tagInfo = null;
			List<String> feature = new ArrayList<String>();
			switch (result.category) {
			case DEEPINFO_CATEGORY_PARKING:
				PackingInfo packingInfo = PackingInfo.parseFromString(js);
				tagInfo = packingInfo;
				break;
			case DEEPINFO_CATEGORY_STATION:
				List<StationInfo> stationList = StationInfo
						.parseFromString(js);
				if(stationList!=null){
					for(int i= 0;i<stationList.size();i++)
						feature.add(stationList.get(i).type);
				}
				if(result.tag.matches("碧辟"))
					result.tag="碧辟";
				if(result.tag.matches("道达尔"))
					result.tag="道达尔";
				tagInfo = stationList;
				break;
			case DEEPINFO_CATEGORY_HOTEL:
				break;
			default:
				break;
			}			
			feature.add(result.tag);
			result.feature=feature;
			result.tagInfo=tagInfo;

	}

}
