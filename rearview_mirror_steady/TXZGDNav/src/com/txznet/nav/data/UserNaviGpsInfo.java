package com.txznet.nav.data;

public class UserNaviGpsInfo {
	public double lat;
	public double lng;
	public double altitude;
	public double direction;
	public float radius;
	public float speed;
	public boolean isGpsOnly;
	public String userId;
	public String imagePath;
	public String nickName;
	public int gpsType;
	public int distance;
	public int time;
	public NaviPath naviPath;

	@Override
	public String toString() {
		return "userId -- >" + userId + ",imagePath -- >" + imagePath
				+ ",lat -- >" + lat + ",lng -- >" + lng + ",direction -- >"
				+ direction;
	}
}
