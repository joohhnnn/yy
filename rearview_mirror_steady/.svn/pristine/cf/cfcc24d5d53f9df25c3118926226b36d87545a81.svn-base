package com.txznet.sdk.bean;

import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.util.JSONBuilder;

public class NaviLatLng {

	private double mLatitude;

	private double mLongitude;

	public double getLatitude() {
		return mLatitude;
	}

	public void setLatitude(double latitude) {
		this.mLatitude = latitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLongitude(double longitude) {
		this.mLongitude = longitude;
	}

	protected JSONBuilder toJsonObj() {
		JSONBuilder json = new JSONBuilder();
		json.put("mLatitude", this.mLatitude);
		json.put("mLongitude", this.mLongitude);
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
		this.mLatitude = json.getVal("mLatitude", Double.class, 0.0);
		this.mLongitude = json.getVal("mLongitude", Double.class, 0.0);
	}

	public static NaviLatLng fromString(String data) {
		NaviLatLng naviLatLng = new NaviLatLng();
		JSONBuilder json = new JSONBuilder(data);
		naviLatLng.fromJsonObject(json);
		return naviLatLng;
	}

	public String toJson() {
		JSONObject jo = new JSONObject();
		try {
			jo.put("mLatitude", mLatitude);
			jo.put("mLongitude", mLongitude);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jo.toString();
	}
}