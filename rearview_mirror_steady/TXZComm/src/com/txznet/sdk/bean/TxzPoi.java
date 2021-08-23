package com.txznet.sdk.bean;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.util.JSONBuilder;

import android.text.TextUtils;

public class TxzPoi extends Poi {
	public TxzPoi() {
		setPoiType(POI_TYPE_TXZ);
	}

	public static class GeoDetail {
		public String country;
		public String province;
		public String town;
		public String area;
		public String street;
		public String number;
		public String building;
		public String room;

		public static GeoDetail creatGeoDetail(String string) {
			GeoDetail gd = new GeoDetail();
			if (TextUtils.isEmpty(string)) {
				return gd;
			}

			JSONObject jo = new JSONBuilder(string).build();
			if (jo == null) {
				return gd;
			}

			try {
				if (jo.has("country")) {
					gd.country = jo.getString("country");
				}
				if (jo.has("province")) {
					gd.province = jo.getString("province");
				}
				if (jo.has("town")) {
					gd.town = jo.getString("town");
				}
				if (jo.has("area")) {
					gd.area = jo.getString("area");
				}
				if (jo.has("street")) {
					gd.street = jo.getString("street");
				}
				if (jo.has("number")) {
					gd.number = jo.getString("number");
				}
				if (jo.has("building")) {
					gd.building = jo.getString("building");
				}
				if (jo.has("room")) {
					gd.room = jo.getString("room");
				}
			} catch (Exception e) {

			}

			return gd;
		}

		public String toString() {
			JSONBuilder json = new JSONBuilder();
			json.put("country", country);
			json.put("province", province);
			json.put("town", town);
			json.put("area", area);
			json.put("street", street);
			json.put("number", number);
			json.put("building", building);
			json.put("room", room);
			return json.toString();
		}
	}

	String logo;
	int hot;
	GeoDetail geoDetail;
	//该POI显示是否置顶
	public boolean isTop;
	String[] keyWords;
	int poiShowType;
	
	public String[] getKeyWords() {
		return keyWords;
	}

	public void setKeyWords(String[] keyWords) {
		this.keyWords = keyWords;
	}

	public int getPoiShowType() {
		return poiShowType;
	}

	public void setPoiShowType(int poiType) {
		this.poiShowType = poiType;
	}

	public boolean isTop() {
		return isTop;
	}

	public void setTop(boolean isTop) {
		this.isTop = isTop;
	}

	public GeoDetail getGeoDetail() {
		return geoDetail;
	}

	public void setGeoDetail(GeoDetail geoDetail) {
		this.geoDetail = geoDetail;
	}

	public int getHot() {
		return hot;
	}

	public void setHot(int hot) {
		this.hot = hot;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public static ArrayList<Poi> getCompanyPoiForJson(String jsonResult) {
		if (TextUtils.isEmpty(jsonResult)) {
			return null;
		}
		ArrayList<Poi> pois = new ArrayList<Poi>();
		try {
			JSONArray jsonArray = new JSONArray(jsonResult);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject json = jsonArray.getJSONObject(i);
				if (json != null) {
					TxzPoi poi = creatCompanyPoi(json);
					if (poi != null) {
						pois.add(poi);
					}
				}
			}
			return pois;
		} catch (Exception e) {

		}
		return null;
	}

	public String toString() {
		JSONBuilder json = super.toJsonObj();
		json.put("geo", getGeoinfo());
		json.put("top", isTop());
		json.put("hot", getHot());
		json.put("logo", getLogo());
		json.put("detail", getGeoDetail()!=null?getGeoDetail().toString():null);

		return json.toString();
	}

	public static TxzPoi fromString(String str) {
		JSONBuilder json = new JSONBuilder(str);
		TxzPoi p = new TxzPoi();
		p.fromJsonObject(json);
		p.setTop(json.getVal("top", Boolean.class, false));
		p.setHot(json.getVal("hot", Integer.class, 0));
		p.setLogo(json.getVal("logo", String.class, ""));
		p.setGeoDetail(GeoDetail.creatGeoDetail(json.getVal("detail",
				String.class, "")));
		return p;
	}

	public static TxzPoi creatCompanyPoi(JSONObject jo) {

		TxzPoi po = new TxzPoi();
		try {
			if (jo.has("lng") && !jo.isNull("lng")) {
				po.setLng(jo.getDouble("lng"));
			} else {
				return null;
			}
			if (jo.has("lat") && !jo.isNull("lat")) {
				po.setLat(jo.getDouble("lat"));
			} else {
				return null;
			}
			if (jo.has("name") && !jo.isNull("name")) {
				po.setName(jo.getString("name"));
			} else {
				return null;
			}
			if (jo.has("geo") && !jo.isNull("geo")) {
				po.setGeoinfo(jo.getString("geo"));
			} else {
				return null;
			}
			if (jo.has("city") && !jo.isNull("city")) {
				po.setCity(jo.getString("city"));
			}
			if (jo.has("logo") && !jo.isNull("logo")) {
				po.setLogo(jo.getString("logo"));
			}
			if (jo.has("hot") && !jo.isNull("hot")) {
				po.setHot(jo.getInt("hot"));
			}
			if (jo.has("detail") && !jo.isNull("detail")) {
				po.setGeoDetail(GeoDetail.creatGeoDetail(jo.getString("detail")));
			}
			if (jo.has("top") && !jo.isNull("top")) {
				po.setTop(jo.getBoolean("top"));
			}
			return po;
		} catch (Exception e) {

		}
		return null;
	}
}
