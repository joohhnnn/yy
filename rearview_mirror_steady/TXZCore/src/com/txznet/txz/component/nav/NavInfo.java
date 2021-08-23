package com.txznet.txz.component.nav;

import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.component.nav.cld.CldDataStore;
import com.txznet.txz.component.nav.kgo.internal.KgoKeyConstants;
import com.txznet.txz.jni.JNIHelper;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * 实时导航返回的数据
 */
public class NavInfo {

	/**
	 * 高德地图 1 自车图标 2 左转图标 3 右转图标 4 左前方图标 5 右前方图标 6 左后方图标 7 右后方图标 8 左转掉头图标 9 直行图标
	 * 10 到达途经点图标 11 进入环岛图标 12 驶出环岛图标 13 到达服务区图标 14 到达收费站图标 15 到达目的地图标 16 进入隧道图标
	 */

	/**
	 * 凯立德导航 0:直行 1:右前方 2:向右 3:右后方 4:调头 5:向左 6:左前方
	 */
	public Integer direction = null; // 转弯方向
	public String dirDes = null; // 转向描述
	public Long dirDistance = null; // 距离转向点的距离，单位m
	public Long dirTime = null; // 距离转向点的时间，单位秒 （高德有）
	public Long remainDistance = null; // 距离目的地的距离，单位m
	public Long remainTime = null; // 距离目的地的剩余时间
	public Integer carDir = null; // 车的方向，以正北方向顺时针增加，单位为度（高德）
	public Double longitude = null; // 自车经度（高德）
	public Double latitude = null; // 自车纬度（高德）
	public Long totalDistance = null; // 出发地与目的地之间的总距离，单位m（凯立德）
	public Long totalTime = null; // 出发地与目的地之间的总时间（凯立德）
	public Long currentLimitedSpeed = null;// 当前限制车速，单位km/h
	public Integer cameraLimitSpeed = null;//电子眼限速
	public String currentRoadName = null; // 当前道路名字
	public String nextRoadName = null; // 下一道路名字
	public Long currentRoadType = null; // 当前道路类型（凯立德）
	public Long currentSpeed = null; // 当前车速，单位km/h（凯立德）
	public Boolean hasArrive = null;// 是否到达了目的地（凯行）
	public String toolPKN = null; // 应用包名
	public String destName;// 目的地POI名称
	public String destAddress;// 目的地详细地址

	public String toJson() {
		JSONBuilder jb = new JSONBuilder();
		jb.put("toolPKN", toolPKN);
		jb.put("direction", direction);
		jb.put("dirDes", dirDes);
		jb.put("dirDistance", dirDistance);
		jb.put("dirTime", dirTime);
		jb.put("remainDistance", remainDistance);
		jb.put("remainTime", remainTime);
		jb.put("carDir", carDir);
		jb.put("longitude", longitude);
		jb.put("latitude", latitude);
		jb.put("totalDistance", totalDistance);
		jb.put("totalTime", totalTime);
		jb.put("currentLimitedSpeed", currentLimitedSpeed);
		jb.put("cameraLimitSpeed",cameraLimitSpeed);
		jb.put("currentRoadName", currentRoadName);
		jb.put("nextRoadName", nextRoadName);
		jb.put("currentRoadType", currentRoadType);
		jb.put("currentSpeed", currentSpeed);
		jb.put("hasArrive", hasArrive);
		jb.put("destName",destName);
		jb.put("destAddress",destAddress);
		return jb.toString();
	}

	public void reset() {
		direction = -1;
		dirDes = "";
		dirDistance = -1L;
		dirTime = -1L;
		remainDistance = -1L;
		remainTime = -1L;
		carDir = -1;
		longitude = -1D;
		latitude = -1D;
		totalDistance = -1L;
		totalTime = -1L;
		currentLimitedSpeed = -1L;
		cameraLimitSpeed = -1;
		currentRoadName = "";
		nextRoadName = "";
		currentRoadType = -1L;
		currentSpeed = -1L;
		toolPKN = "";
		hasArrive = null;
		destName = "";
		destAddress = "";
	}

	public void parseGDNavInfo(String json, String pkn) {
		reset();
		try {
			JSONObject jo = new JSONObject(json);
			toolPKN = pkn;
			if (json == null || TextUtils.isEmpty(json)) {
				return;
			}

			direction = jo.optInt("icon");
			if (direction == 0) {
				direction = -1;
				dirDes = "";
			} else {
				if (AUTONAVI_DES.length >= direction && direction > 0) {
					dirDes = AUTONAVI_DES[direction - 1];
				}
			}
			dirDistance = jo.optLong("segRemainDis");
			dirTime = jo.optLong("segRemainTime");
			remainDistance = jo.optLong("routeRemainDis");
			remainTime = jo.optLong("routeRemainTime");
			carDir = jo.optInt("carDirection");
			longitude = jo.optDouble("longitude");
			latitude = jo.optDouble("latitude");
			totalDistance = -1L;
			totalTime = -1L;
			currentLimitedSpeed = jo.optLong("limitedSpeed");
			currentRoadName = jo.optString("curRoadName");
			nextRoadName = jo.optString("nextRoadName");
			currentRoadType = -1L;
			currentSpeed = -1L;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void parseCldNavInfo(CldDataStore cds, String pkn) {
		reset();
		toolPKN = pkn;
		if (cds == null) {
			return;
		}

		if (cds.lDirection == null) {
			direction = -1;
			dirDes = "";
		} else {
			direction = Integer.parseInt(String.valueOf(cds.lDirection));
			if (direction > -1 && direction < CLDNAVI_DES.length) {
				dirDes = CLDNAVI_DES[direction];
			}
		}
		dirDistance = cds.lDistance;
		dirTime = -1L;
		remainDistance = cds.lRemainDistance;
		remainTime = cds.lRemainTime;
		carDir = -1;
		longitude = -1D;
		latitude = -1D;
		totalDistance = cds.lTotalDistance;
		totalTime = cds.lTotalTime;
		currentLimitedSpeed = cds.lCurrentLimitedSpeed;
		currentRoadName = cds.szCurrentRoadName;
		nextRoadName = cds.szNextRoadName;
		currentRoadType = cds.lCurrentRoadType;
		currentSpeed = cds.lCurrentSpeed;
	}

	public void parseAmapAutoNav(Bundle bundle, String pkn) {
		if (DebugCfg.ENABLE_NAV_LOG) {
			JNIHelper.logd("amap navinfo:" + bundle);
		}
		try {
			toolPKN = pkn;
			direction = bundle.getInt("ICON");
			if (direction == 0) {
				direction = -1;
				dirDes = "";
			} else {
				if (AUTONAVI_DES.length >= direction && direction > 0) {
					dirDes = AUTONAVI_DES[direction - 1];
				}
			}

			// dirDistance = bundle.getLong("SEG_REMAIN_DIS");
			dirDistance = (long) bundle.getInt("SEG_REMAIN_DIS");
			// dirTime = bundle.getLong("SEG_REMAIN_TIME");
			dirTime = (long) bundle.getInt("SEG_REMAIN_TIME");
			// remainDistance = bundle.getLong("ROUTE_REMAIN_DIS");
			remainDistance = (long) bundle.getInt("ROUTE_REMAIN_DIS");
			// remainTime = bundle.getLong("ROUTE_REMAIN_TIME");
			remainTime = (long) bundle.getInt("ROUTE_REMAIN_TIME");
			carDir = bundle.getInt("CAR_DIRECTION");
			longitude = bundle.getDouble("CAR_LONGITUDE");
			latitude = bundle.getDouble("CAR_LATITUDE");
			// totalDistance = bundle.getLong("ROUTE_ALL_DIS");
			totalDistance = (long) bundle.getInt("ROUTE_ALL_DIS");
			// totalTime = bundle.getLong("ROUTE_ALL_TIME");
			totalTime = (long) bundle.getInt("ROUTE_ALL_TIME");
			// currentLimitedSpeed = bundle.getLong("LIMITED_SPEED");
			currentLimitedSpeed = (long) bundle.getInt("LIMITED_SPEED");
			cameraLimitSpeed = bundle.getInt("CAMERA_SPEED");
			currentRoadName = bundle.getString("CUR_ROAD_NAME");
			nextRoadName = bundle.getString("NEXT_ROAD_NAME");
			currentRoadType = -1L;
			// currentSpeed = bundle.getLong("CUR_SPEED");
			currentSpeed = (long) bundle.getInt("CUR_SPEED");
		} catch (Exception e) {
			LogUtil.logw(e.toString());
		}
	}
	
	public void parseBundle(String packageName, Bundle bundle) {
		toolPKN = packageName;
		if (bundle.containsKey("direction")) {
			direction = bundle.getInt("direction");
		}
		if (bundle.containsKey("dirDes")) {
			dirDes = bundle.getString("dirDes");
		}
		if (bundle.containsKey("dirDistance")) {
			dirDistance = (long) bundle.getInt("dirDistance");
		}
		if (bundle.containsKey("dirTime")) {
			dirTime = bundle.getLong("dirTime");
		}
		if (bundle.containsKey("remainDistance")) {
			remainDistance = (long) bundle.getInt("remainDistance");
		}
		if (bundle.containsKey("remainTime")) {
			remainTime = (long) bundle.getInt("remainTime");
		}
		if (bundle.containsKey("totalDistance")) {
			totalDistance = (long) bundle.getInt("totalDistance");
		}
		if (bundle.containsKey("totalTime")) {
			totalTime = (long) bundle.getInt("totalTime");
		}
		if (bundle.containsKey("currentLimitedSpeed")) {
			currentLimitedSpeed = (long) bundle.getInt("currentLimitedSpeed");
		}
		if (bundle.containsKey("cameraLimitSpeed")) {
			cameraLimitSpeed = bundle.getInt("cameraLimitSpeed");
		}
		if (bundle.containsKey("currentRoadName")) {
			currentRoadName = bundle.getString("currentRoadName");
		}
		if (bundle.containsKey("nextRoadName")) {
			nextRoadName = bundle.getString("nextRoadName");
		}
		if (bundle.containsKey("currentRoadType")) {
			currentRoadType = (long) bundle.getInt("currentRoadType");
		}
		if (bundle.containsKey("currentSpeed")) {
			currentSpeed = (long) bundle.getInt("currentSpeed");
		}
		if (bundle.containsKey("destAddress")) {
			destAddress = bundle.getString("destAddress");
		}
		if (bundle.containsKey("destName")) {
			destName = bundle.getString("destName");
		}
	}
	
	public void parseKgo(Intent intent) {
		reset();
		try {
			toolPKN = KgoKeyConstants.NAVI_PACKAGE_NAME;
//		intent.getIntExtra(KGoKeyConstants.KEY.TYPE, -1);
			currentRoadName = intent.getStringExtra(KgoKeyConstants.KEY.CUR_ROAD_NAME);
			nextRoadName = intent.getStringExtra(KgoKeyConstants.KEY.NEXT_ROAD_NAME);
//		intent.getIntExtra(KGoKeyConstants.KEY.SAPA_DIST, -1);
//		intent.getIntExtra(KGoKeyConstants.KEY.CAMERA_TYPE, -1);
//		intent.getIntExtra(KGoKeyConstants.KEY.CAMERA_SPEED, -1);
			direction = intent.getIntExtra(KgoKeyConstants.KEY.ICON, -1);
			if (direction != -1) {
				if (direction < KGO_DES.length) {
					dirDes = KGO_DES[direction];
				}
			}
			remainDistance = (long) intent.getIntExtra(KgoKeyConstants.KEY.ROUTE_REMAIN_DIS, -1);
			remainTime = (long) intent.getIntExtra(KgoKeyConstants.KEY.ROUTE_REMAIN_TIME, -1);
			dirDistance = (long) intent.getIntExtra(KgoKeyConstants.KEY.SEG_REMAIN_DIS, -1);
			dirTime = (long) intent.getIntExtra(KgoKeyConstants.KEY.SEG_REMAIN_TIME, -1);
			currentLimitedSpeed = (long) intent.getIntExtra(KgoKeyConstants.KEY.LIMITED_SPEED, -1);
			totalDistance = (long) intent.getIntExtra(KgoKeyConstants.KEY.ROUTE_ALL_DIS, -1);
			totalTime = (long) intent.getIntExtra(KgoKeyConstants.KEY.ROUTE_ALL_TIME, -1);
			currentSpeed = (long) intent.getIntExtra(KgoKeyConstants.KEY.CUR_SPEED,-1);
			hasArrive = intent.getBooleanExtra(KgoKeyConstants.KEY.ARRIVE_STATUS, false);
		} catch (Exception e) {
			LogUtil.loge(e.getMessage());
		}
	}

	public String getDirectionDes() {
		return dirDes;
	}

	/*
	 * 高德auto引导数据
	 * 前16个为高德1.x的导航信息
	 * 后12个为高德2.x的导航信息
	 */
	public static final String[] AUTONAVI_DES = new String[] { "自车", "左转", "右转", "左前方", "右前方", "左后方", "右后方", "左转掉头",
			"直行", "到达途经点", "进入环岛", "驶出环岛", "到达服务区", "到达收费站", "到达目的地", "进入隧道" ,"进入环岛","驶出环岛",
			"右转掉头","顺行","绕环岛左转","绕环岛右转","绕环岛直行","绕环岛掉头","绕环岛左转","绕环岛右转","绕环岛直行","绕环岛掉头"};
	// 凯立德引导数据
	public static final String[] CLDNAVI_DES = new String[] { "直行", "右前方", "向右", "右后方", "调头", "向左", "左前方" };
	// 凯行引导数据
	public static final String[] KGO_DES = CLDNAVI_DES;
}
