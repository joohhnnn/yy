package com.txznet.txz.module.netdata;

import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.SystemClock;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.Resp_Weather;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.FlowInfo;
import com.txznet.sdk.bean.TrafficControlData;
import com.txznet.sdk.bean.TrafficControlData.TrafficControlInfo;
import com.txznet.sdk.bean.WeatherData;
import com.txznet.sdk.bean.WeatherData.WeatherDay;
import com.txznet.txz.component.text.IText.ITextCallBack;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.runnables.Runnable2;
import com.txznet.txz.util.runnables.Runnable3;

public class NetDataManager {
	private NetDataManager() {
	}

	private static NetDataManager sInstance;

	public static NetDataManager getInstance() {
		if (sInstance == null) {
			synchronized (NetDataManager.class) {
				if (sInstance == null) {
					sInstance = new NetDataManager();
				}
			}
		}
		return sInstance;
	}
	
	private int taskId = 0;

	public byte[] processInvoke(final String pkgName, String cmd, byte[] data) {
		// 目前采用云之声处理
		if (cmd.equals("weather")) {
//			return procWeatherYunzhisheng(pkgName, cmd, data);
			return procWeatherBackground(pkgName, cmd, data);
		} else if (cmd.equals("traffic")) {
			return procTrafficInfoYunzhisheng(pkgName, cmd, data);
		} else if (cmd.equals("flowInfo")) {
			return procQueryFlowInfo(pkgName, cmd, data);
		} else if(cmd.equals("joke")){
			return procJokeInfoYunzhisheng(pkgName, cmd, data);
		}
		return null;
	}
	
	long lastGetFlowInfoTime = 0;
	private int mQueryFlowInterval = 10 * 60 * 1000;
	private String mLastFlowInfo = null;
	private String mLastIccid = null;

	public byte[] procQueryFlowInfo(String pkgName, String cmd, byte[] data) {
		int remoteTaskId = taskId++;
		String iccid = DeviceInfo.getSimSerialNumber();
		if(TextUtils.isEmpty(iccid)){
			LogUtil.logd("flowInfo iccid empty");
			JSONBuilder jDoc = new JSONBuilder();
			jDoc.put("rc", 1);
			jDoc.put("taskId", remoteTaskId + "");
			jDoc.put("errorCode", 1001);
			ServiceManager.getInstance().sendInvoke(pkgName, "comm.netdata.resp.flowInfo", jDoc.toBytes(), null);
			return (remoteTaskId + "").getBytes();
		}
		NetworkManager.getInstance().checkNetConnect(5000, new Runnable3<String,String,Integer>(iccid, pkgName, remoteTaskId) {
			
			@Override
			public void run() {
				LogUtil.logd("flowInfo net true.");
				if (checkNeedRequest(mP1)) {
					EquipmentManager.Req_SIMQueryFlowInfo mSimQueryFlowInfo = new EquipmentManager.Req_SIMQueryFlowInfo();
					mSimQueryFlowInfo.strSimIccid = mP1.getBytes();
					mSimQueryFlowInfo.strPkgName = mP2.getBytes();
					mSimQueryFlowInfo.strTaskId = (mP3 + "").getBytes();
					LogUtil.logd("flowInfo request taskId = " + mP3);
					JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
							UiEquipment.SUBEVENT_REQ_FLOW_INFO, mSimQueryFlowInfo);
				} else {
					LogUtil.logd("flowInfo cache return");
					JSONBuilder jDoc = new JSONBuilder();
					jDoc.put("rc", 0);
					jDoc.put("taskId", mP3 + "");
					jDoc.put("data", mLastFlowInfo);
					ServiceManager.getInstance().sendInvoke(mP2,
							"comm.netdata.resp.flowInfo", jDoc.toBytes(), null);
				}
			}
		}, new Runnable2<String, Integer>(pkgName, remoteTaskId) {
			
			@Override
			public void run() {
				LogUtil.logd("flowInfo net false.");
				JSONBuilder jDoc = new JSONBuilder();
				jDoc.put("rc", 0);
				jDoc.put("taskId", mP2 + "");
				jDoc.put("data", mLastFlowInfo);
				jDoc.put("net", false);
				ServiceManager.getInstance().sendInvoke(mP1,
						"comm.netdata.resp.flowInfo", jDoc.toBytes(), null);
			}
		});
		return (remoteTaskId + "").getBytes();
	}

	private boolean checkNeedRequest(String iccid) {
		if(iccid == null || mLastIccid == null){
			return true;
		}
		if(!iccid.startsWith(mLastIccid)){
			LogUtil.logd("flowInfo iccid changed("+mLastIccid+"-"+iccid+")");
			return true;
		}
		if(lastGetFlowInfoTime == 0 || (SystemClock.elapsedRealtime()-lastGetFlowInfoTime > mQueryFlowInterval)){
			return true;
		}
		return false;
	}
	
	public void updateFlowInfoCache() {
		String iccid = DeviceInfo.getSimSerialNumber();
		if (TextUtils.isEmpty(iccid)) {
			return;
		}
		EquipmentManager.Req_SIMQueryFlowInfo mSimQueryFlowInfo = new EquipmentManager.Req_SIMQueryFlowInfo();
		mSimQueryFlowInfo.strSimIccid = iccid.getBytes();
		mSimQueryFlowInfo.strPkgName = ServiceManager.TXZ.getBytes();
		mSimQueryFlowInfo.strTaskId = (-1 + "").getBytes();
		LogUtil.logd("flowInfo request taskId = -1");
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_REQ_FLOW_INFO, mSimQueryFlowInfo);
	}

	long lastGetWeatherTime = 0;
	
	public byte[] procWeatherBackground_1_0(String pkgName, String cmd, byte[] data){
		if (lastGetWeatherTime == 0
				|| SystemClock.elapsedRealtime() - lastGetWeatherTime > 30 * 60 * 1000) {
			lastGetWeatherTime = SystemClock.elapsedRealtime();
			final int remoteTaskId = taskId++;
			JSONBuilder doc = new JSONBuilder(data);
			UiEquipment.Req_Weather weather = new UiEquipment.Req_Weather();
			LocationInfo locationInfo = LocationManager.getInstance()
					.getLastLocation();
			if (locationInfo != null && locationInfo.msgGpsInfo != null) {
				weather.fltLat = Float.valueOf(locationInfo.msgGpsInfo.dblLat
						.floatValue());
				weather.fltLng = Float.valueOf(locationInfo.msgGpsInfo.dblLng
						.floatValue());
			}
			String city = doc.getVal("city", String.class);
			if (TextUtils.equals(city, "cur")) {
				if (locationInfo != null && locationInfo.msgGeoInfo != null) {
					city = locationInfo.msgGeoInfo.strCity;
				} else {
					city = "";
				}
			}
			weather.strCity = city;
			weather.uint32Time = (int) (System.currentTimeMillis()/1000);
			weather.strTime = "今天";
			weather.strPkg = "1_0_" + pkgName;
			weather.strTaskid = remoteTaskId + "";
			JNIHelper.logd("procWeatherBackground_1_0 strCity = " + city);
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
					UiEquipment.SUBEVENT_REQ_WEATHER, weather);
			return (remoteTaskId + "").getBytes();
		}

		ServiceManager.getInstance().sendInvoke(pkgName,
				"comm.text.event.result", lastWeatherCache.getBytes(), null);
		return null;
	}
	
	/**
	 * 通过后台获取天气
	 * @param pkgName
	 * @param cmd
	 * @param data
	 * @return
	 */
	private byte[] procWeatherBackground(String pkgName, String cmd, byte[] data) {
		final int remoteTaskId = taskId++;
		JSONBuilder doc = new JSONBuilder(data);
		UiEquipment.Req_Weather weather = new UiEquipment.Req_Weather();
		LocationInfo locationInfo = LocationManager.getInstance().getLastLocation();
		if(locationInfo != null && locationInfo.msgGpsInfo != null){
			weather.fltLat = Float.valueOf(locationInfo.msgGpsInfo.dblLat.floatValue());
			weather.fltLng = Float.valueOf(locationInfo.msgGpsInfo.dblLng.floatValue());
		}
		String city = doc.getVal("city", String.class);
		if(TextUtils.equals(city, "cur")){
			if(locationInfo != null && locationInfo.msgGeoInfo != null){
				city = locationInfo.msgGeoInfo.strCity;
			}else{
				city = "";
			}
		}
			
		weather.strCity = city;
		weather.uint32Time = (int) (System.currentTimeMillis()/1000);
		weather.strTime = "今天";
		weather.strPkg = pkgName;
		weather.strTaskid = remoteTaskId+"";
		JNIHelper.logd("procWeatherBackground strCity = "+city);
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_REQ_WEATHER, weather);
		return (remoteTaskId + "").getBytes();
	}
	
	public String procWeatherBackgroundInner(String city,String date,String text){
		final int innerTaskId = taskId++;
		UiEquipment.Req_Weather weather = new UiEquipment.Req_Weather();
		LocationInfo locationInfo = LocationManager.getInstance().getLastLocation();
		if(locationInfo != null && locationInfo.msgGpsInfo != null){
			weather.fltLat = Float.valueOf(locationInfo.msgGpsInfo.dblLat.floatValue());
			weather.fltLng = Float.valueOf(locationInfo.msgGpsInfo.dblLng.floatValue());
		}
		if(TextUtils.isEmpty(date)){
			date = "今天";
		} else if (TextUtils.equals("CURRENT_DAY", date)) {
			date = "今天";
		}else {
			date = date.replace("-", "");
		}
		
		weather.strCity = city;
		weather.uint32Time = (int) (System.currentTimeMillis()/1000);
		weather.strTime = date;
		weather.strPkg = "core_com.txznet.txz";
		weather.strTaskid = innerTaskId+"";
		weather.strText = text;
		JNIHelper.logd("procWeatherBackground strCity = "+city);
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
			UiEquipment.SUBEVENT_REQ_WEATHER, weather);
		return innerTaskId +"";
	}
	
	String lastWeatherCache = WeatherData1.getDefaultWeatherInfo();
	
	public void handleWeatherData_1_0(Resp_Weather weatherResult) {
		if(weatherResult == null || weatherResult.weather == null){
			return;
		}
		String jsonResult = new String(weatherResult.weather);
		JNIHelper.logd("weatherString_1_0 = " + jsonResult);
		try {
			JSONObject doc = new JSONObject(jsonResult);
			String scene = doc.optString("scene");
			String action = doc.optString("action");
			String pkg = doc.optString("pkg");
			String taskid = doc.optString("taskid");
			if (scene.equals("weather") && action.equals("query")) {
				if (doc.has("data")) {
					JSONObject result = doc.getJSONObject("data")
							.getJSONObject("result");
					JSONObject jsonRE = new JSONObject();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm");
					jsonRE.put("cityName", result.optString("cityName"));
					jsonRE.put("cityCode", result.optString("cityCode"));
					jsonRE.put("updateTime", dateFormat.parse(result
							.getString("updateTime")));
					jsonRE.put("focusDateIndex", "");
					jsonRE.put("errorCode", 0);
					JSONArray jInfos = result.getJSONArray("weatherDays");
					jsonRE.put("weatherDays", jInfos);
					JSONBuilder jsonWeatherData = new JSONBuilder();
					jsonWeatherData.put("rc", 0);
					jsonWeatherData.put("taskId", taskid);
					jsonWeatherData.put("text", "");
					jsonWeatherData.put("history", "cn.yunzhisheng.weather");
					jsonWeatherData.put("responseId", taskid);
					jsonWeatherData.put("service", "cn.yunzhisheng.weather");
					jsonWeatherData.put("code", "FORECAST");
					JSONObject jsonSem = new JSONObject();
					JSONObject jsonInt = new JSONObject();
					jsonInt.put("province", "");
					jsonInt.put("city", result.optString("cityName"));
					jsonInt.put("cityCode", result.optString("cityCode"));
					jsonInt.put("focusDate", dateFormat.parse(result
							.getString("updateTime")));
					jsonInt.put("topic", "WEATHER");
					jsonSem.put("intent", jsonInt);
					jsonWeatherData.put("semantic", jsonSem);
					JSONObject jsonGen = new JSONObject();
					jsonGen.put("type", "T");
					jsonGen.put("text", "");
					jsonWeatherData.put("general", jsonGen);
					JSONObject jsonRes = new JSONObject();
					jsonRes.put("result", jsonRE);
					jsonRes.put("header", "");
					jsonWeatherData.put("data", jsonRes);
					JNIHelper.logd("handleWeatherData_1_0 result = "
							+ jsonWeatherData.toString());
					lastWeatherCache = jsonWeatherData.toString();
					ServiceManager.getInstance().sendInvoke(
							pkg.replace("1_0_", ""), "comm.text.event.result",
							jsonWeatherData.toString().getBytes(), null);
					return;

				} else {
					ServiceManager.getInstance().sendInvoke(pkg.replace("1_0_", ""),
							"comm.text.event.error", "1004".getBytes(), null);
					return;
				}
			} else {
				ServiceManager.getInstance().sendInvoke(pkg.replace("1_0_", ""),
						"comm.text.event.error", "1001".getBytes(), null);
				return;
			}
		} catch (Exception e) {
			JNIHelper.loge("server weather parse error:"+e.getMessage());
		}
		
	}

	public void handleFlowInfoData(
			EquipmentManager.Resp_SIMQueryFlowInfo queryFlowInfo) {
		if (queryFlowInfo == null || queryFlowInfo.strTaskId == null || queryFlowInfo.strPkgName == null) {
			LogUtil.logd("flowInfo request fail");
			return;
		}
		LogUtil.logd("flowInfo handle taskId = "
				+ (queryFlowInfo.strTaskId == null ? "null" : new String(queryFlowInfo.strTaskId))
				+ ", queryInterval = " + queryFlowInfo.uint32QueryInterval + ", errorCode = "
				+ queryFlowInfo.uint32ErrorCode);
		if (queryFlowInfo.uint32QueryInterval != null
				&& queryFlowInfo.uint32QueryInterval >= 60 * 1000
				&& queryFlowInfo.uint32QueryInterval <= 60 * 60 * 1000) {
			mQueryFlowInterval = queryFlowInfo.uint32QueryInterval;
		}
		if(queryFlowInfo.uint32ErrorCode != null && queryFlowInfo.uint32ErrorCode != FlowInfo.EC_FLOW_OK){
			JSONBuilder jDoc = new JSONBuilder();
			jDoc.put("rc", 1);
			jDoc.put("taskId", new String(queryFlowInfo.strTaskId));
			jDoc.put("errorCode", queryFlowInfo.uint32ErrorCode);
			ServiceManager.getInstance().sendInvoke(new String(queryFlowInfo.strPkgName),
					"comm.netdata.resp.flowInfo", jDoc.toBytes(), null);
			return;
		}
		FlowInfo flowInfo = new FlowInfo();
		if (queryFlowInfo.strSimIccid != null) {
			flowInfo.iccid = new String(queryFlowInfo.strSimIccid);
			mLastIccid = flowInfo.iccid;
		}
		if (queryFlowInfo.strPlanName != null) {
			flowInfo.planName = new String(queryFlowInfo.strPlanName);
		}
		if (queryFlowInfo.uint32Outtime != null) {
			flowInfo.outtime = queryFlowInfo.uint32Outtime;
		}
		if (queryFlowInfo.uint32PlanType != null) {
			flowInfo.planType = queryFlowInfo.uint32PlanType;
		}
		if (queryFlowInfo.uint32RemainData != null) {
			flowInfo.remainData = queryFlowInfo.uint32RemainData;
		}
		if (queryFlowInfo.uint32RemainDay != null) {
			flowInfo.remainDay = queryFlowInfo.uint32RemainDay;
		}
		if (queryFlowInfo.uint32TotalData != null) {
			flowInfo.totalFlow = queryFlowInfo.uint32TotalData;
		}
		if (queryFlowInfo.uint32UseData != null) {
			flowInfo.useData = queryFlowInfo.uint32UseData;
		}
		String strRes = JSON.toJSONString(flowInfo);
		LogUtil.logd("flowInfo handle Result = "+strRes);
		mLastFlowInfo = strRes;
		lastGetFlowInfoTime = SystemClock.elapsedRealtime();
		if (!new String(queryFlowInfo.strTaskId).equals("-1")) {// 为-1的taskId用于更新本地流量数据
			JSONBuilder jDoc = new JSONBuilder();
			jDoc.put("rc", 0);
			jDoc.put("taskId", new String(queryFlowInfo.strTaskId));
			jDoc.put("data", strRes);
			ServiceManager.getInstance().sendInvoke(new String(queryFlowInfo.strPkgName),
					"comm.netdata.resp.flowInfo", jDoc.toBytes(), null);
		}
	}

	public void handleWeatherData(Resp_Weather weatherResult) {
		if(weatherResult == null || weatherResult.weather == null){
			return;
		}
		String jsonResult = new String(weatherResult.weather);
		JNIHelper.logd("weatherString = " + jsonResult);
		try {
			JSONObject doc = new JSONObject(jsonResult);
			String scene = doc.optString("scene");
			String action = doc.optString("action");
			String pkg = doc.optString("pkg");
			String taskid = doc.optString("taskid");
			if (pkg.startsWith("1_0_")) {
				handleWeatherData_1_0(weatherResult);
				return;
			}else if (TextUtils.equals(pkg, "core_com.txznet.txz")) {
				//如果是语义请求的天气返回来，走到这里，说明语义那边已经处理了一次天气返回结果错误的情况，直接抛弃掉
				return;
			}
			if (scene.equals("weather") && action.equals("query")) {
				if (doc.has("data")) {
					JSONObject result = doc.getJSONObject("data")
							.getJSONObject("result");
					WeatherData weatherData = new WeatherData();
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm");
					weatherData.cityCode = result.optString("cityCode");
					weatherData.cityName = result.optString("cityName");
					weatherData.updateTime = dateFormat.parse(result
							.getString("updateTime"));
					JSONArray jInfos = result.getJSONArray("weatherDays");
					weatherData.weatherDays = new WeatherDay[jInfos.length()];
					for (int i = 0; i < jInfos.length(); i++) {
						JSONObject jInfo = jInfos.getJSONObject(i);
						WeatherDay info = new WeatherDay();
						info.year = jInfo.optInt("year");
						info.month = jInfo.optInt("month");
						info.day = jInfo.optInt("day");
						info.dayOfWeek = jInfo.optInt("dayOfWeek");
						info.weather = jInfo.optString("weather");
						info.currentTemperature = jInfo
								.optInt("currentTemperature");
						info.highestTemperature = jInfo
								.optInt("highestTemperature");
						info.lowestTemperature = jInfo
								.optInt("lowestTemperature");
						info.pm2_5 = jInfo.optInt("pm2_5", -1);
						info.quality = jInfo.optString("quality");
						info.wind = jInfo.optString("wind");
						info.comfortIndex = jInfo.optString("comfortIndex");
						info.comfortIndexDesc = jInfo
								.optString("comfortIndexDesc");
						info.carWashIndex = jInfo.optString("carWashIndex");
						info.carWashIndexDesc = jInfo
								.optString("carWashIndexDesc");
						info.dressIndex = jInfo.optString("dressIndex");
						info.dressIndexDesc = jInfo.optString("dressIndexDesc");
						info.sunBlockIndex = jInfo.optString("sunBlockIndex");
						info.sunBlockIndexDesc = jInfo
								.optString("sunBlockIndexDesc");
						info.sportIndex = jInfo.optString("sportIndex");
						info.sportIndexDesc = jInfo.optString("sportIndexDesc");
						info.dryingIndex = jInfo.optString("dryingIndex");
						info.dryingIndexDesc = jInfo
								.optString("dryingIndexDesc");
						info.morningExerciseIndex = jInfo
								.optString("morningExerciseIndex");
						info.morningExerciseIndexDesc = jInfo
								.optString("morningExerciseIndexDesc");
						info.coldIndex = jInfo.optString("coldIndex");
						info.coldIndexDesc = jInfo.optString("coldIndexDesc");
						info.datingIndex = jInfo.optString("datingIndex");
						info.datingIndexDesc = jInfo
								.optString("datingIndexDesc");
						info.umbrellaIndex = jInfo.optString("umbrellaIndex");
						info.umbrellaIndexDesc = jInfo
								.optString("umbrellaIndexDesc");
						info.travelIndex = jInfo.optString("travelIndex");
						info.travelIndexDesc = jInfo
								.optString("travelIndexDesc");
						info.suggest = jInfo.optString("suggest");
						weatherData.weatherDays[i] = info;
					}
					JSONBuilder jDoc = new JSONBuilder();
					jDoc.put("rc", 0);
					jDoc.put("taskId", taskid);
					jDoc.put("data", JSON.toJSONString(weatherData));
					ServiceManager.getInstance().sendInvoke(pkg,
							"comm.netdata.resp.weather", jDoc.toBytes(), null);
				} else {
					JSONBuilder docResp = new JSONBuilder();
					docResp.put("rc", 1);
					docResp.put("taskId", taskid);
					docResp.put("errorCode", 1004);
					ServiceManager.getInstance().sendInvoke(pkg, "comm.netdata.resp.weather", docResp.toBytes(), null);		 // 不支持
				}
			} else {
				JSONBuilder docResp = new JSONBuilder();
				docResp.put("rc", 1);
				docResp.put("taskId", taskid);
				docResp.put("errorCode", 1001);
				ServiceManager.getInstance().sendInvoke(pkg,
						"comm.netdata.resp.weather", docResp.toBytes(), null);// 输入错误
			}
		} catch (Exception e) {
			JNIHelper.loge("server weather parse error:"+e.getMessage());
		}
	}
	


	private byte[] procWeatherYunzhisheng(final String pkgName, String cmd, byte[] data) {
		final int remoteTaskId = taskId++;
		JSONBuilder doc = new JSONBuilder(data);
		String city = doc.getVal("city", String.class);
		String query = "cur".equals(city) ? "今天天气" : String.format("%s的天气", city);
		TextResultHandle.getInstance().parseText(query,TextResultHandle.MODULE_YUNZHISHENG_MASK, new ITextCallBack() {
//		TextManager.getInstance().parseText(query, new ITextCallBack() {
			@Override
			public void onResult(String jsonResult) {
				// TODO 解析
				try {
					JSONObject doc = new JSONObject(jsonResult);
//					int rc = doc.optInt("rc", -1);
//					String service = doc.optString("service");
//					if (rc == 0 && "cn.yunzhisheng.weather".equals(service)) {
					String scene = doc.optString("scene");
					String action = doc.optString("action");
					if (scene.equals("weather") && action.equals("query")){
						if (doc.has("data")) {
							JSONObject result = doc.getJSONObject("data").getJSONObject("result");
							WeatherData weatherData = new WeatherData();
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							weatherData.cityCode = result.optString("cityCode");
							weatherData.cityName = result.optString("cityName");
							weatherData.updateTime = dateFormat.parse(result.getString("updateTime"));
							JSONArray jInfos = result.getJSONArray("weatherDays");
							weatherData.weatherDays = new WeatherDay[jInfos.length()];
							for(int i = 0; i < jInfos.length(); i++){
								JSONObject jInfo = jInfos.getJSONObject(i);
								WeatherDay info = new WeatherDay();
								info.year = jInfo.optInt("year");
								info.month = jInfo.optInt("month");
								info.day = jInfo.optInt("day");
								info.dayOfWeek = jInfo.optInt("dayOfWeek");
								info.weather = jInfo.optString("weather");
								info.currentTemperature = jInfo.optInt("currentTemperature");
								info.highestTemperature = jInfo.optInt("highestTemperature");
								info.lowestTemperature = jInfo.optInt("lowestTemperature");
								info.pm2_5 = jInfo.optInt("pm2_5", -1);
								info.quality = jInfo.optString("quality");
								info.wind = jInfo.optString("wind");
								info.comfortIndex = jInfo.optString("comfortIndex");
								info.comfortIndexDesc = jInfo.optString("comfortIndexDesc");
								info.carWashIndex = jInfo.optString("carWashIndex");
								info.carWashIndexDesc = jInfo.optString("carWashIndexDesc");
								info.dressIndex = jInfo.optString("dressIndex");
								info.dressIndexDesc = jInfo.optString("dressIndexDesc");
								info.sunBlockIndex = jInfo.optString("sunBlockIndex");
								info.sunBlockIndexDesc = jInfo.optString("sunBlockIndexDesc");
								info.sportIndex = jInfo.optString("sportIndex");
								info.sportIndexDesc = jInfo.optString("sportIndexDesc");
								info.dryingIndex = jInfo.optString("dryingIndex");
								info.dryingIndexDesc = jInfo.optString("dryingIndexDesc");
								info.morningExerciseIndex = jInfo.optString("morningExerciseIndex");
								info.morningExerciseIndexDesc = jInfo.optString("morningExerciseIndexDesc");
								info.coldIndex = jInfo.optString("coldIndex");
								info.coldIndexDesc = jInfo.optString("coldIndexDesc");
								info.datingIndex = jInfo.optString("datingIndex");
								info.datingIndexDesc = jInfo.optString("datingIndexDesc");
								info.umbrellaIndex = jInfo.optString("umbrellaIndex");
								info.umbrellaIndexDesc = jInfo.optString("umbrellaIndexDesc");
								info.travelIndex = jInfo.optString("travelIndex");
								info.travelIndexDesc = jInfo.optString("travelIndexDesc");
								info.suggest = jInfo.optString("suggest");
								weatherData.weatherDays[i] = info;
							}

							JSONBuilder jDoc = new JSONBuilder();
							jDoc.put("rc", 0);
							jDoc.put("taskId", remoteTaskId);
							jDoc.put("data", JSON.toJSONString(weatherData));
							ServiceManager.getInstance().sendInvoke(pkgName, "comm.netdata.resp.weather", jDoc.toBytes(), null);
						} else {
							onError(1004); // 不支持
						}
					} else {
						onError(1001); // 输入错误
					}
				} catch (Exception e) {
					onError(-1);
				}
			}

			@Override
			public void onError(int errorCode) {
				JSONBuilder doc = new JSONBuilder();
				doc.put("rc", 1);
				doc.put("taskId", remoteTaskId);
				doc.put("errorCode", errorCode);
				ServiceManager.getInstance().sendInvoke(pkgName, "comm.netdata.resp.weather", doc.toBytes(), null);
			}
		});
		return (remoteTaskId + "").getBytes();
	}

	private byte[] procTrafficInfoYunzhisheng(final String pkgName, String cmd, byte[] data) {
		final int remoteTaskId = taskId++;
		JSONBuilder doc = new JSONBuilder(data);
		String city = doc.getVal("city", String.class);
		String query = "cur".equals(city) ? "限行" : String.format("%s的限行", city);
		TextResultHandle.getInstance().parseText(query,TextResultHandle.MODULE_YUNZHISHENG_MASK, new ITextCallBack() {
//		TextManager.getInstance().parseText(query, new ITextCallBack() {
			@Override
			public void onResult(String jsonResult) {
				try {
					JSONObject doc = new JSONObject(jsonResult);
//					int rc = doc.optInt("rc", -1);
//					String service = doc.optString("service");
//					if (rc == 0 && "cn.yunzhisheng.traffic.control".equals(service)) {
					String scene = doc.optString("scene");
					String action = doc.optString("action");
					if (scene.equals("limit_number") && action.equals("query")){
						if (doc.has("data")) {
							JSONObject result = doc.getJSONObject("data").getJSONObject("result");
							TrafficControlData trafficControlData = new TrafficControlData();
							trafficControlData.city = result.optString("city");
							trafficControlData.local = result.optString("local");
							trafficControlData.nonlocal = result.optString("nonlocal");
							JSONArray jInfos = result.getJSONArray("trafficControlInfos");
							trafficControlData.trafficControlInfos = new TrafficControlInfo[jInfos.length()];
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							for (int i = 0; i < jInfos.length(); i++) {
								JSONObject jInfo = jInfos.getJSONObject(i);
								TrafficControlInfo info = new TrafficControlInfo();
								if (jInfo.has("forbiddenTailNumber")) {
									info.forbiddenTailNumber = jInfo.getString("forbiddenTailNumber").split(",");
								}
								info.week = jInfo.getInt("week");
								info.forbiddenDate = dateFormat.parse(jInfo.getString("forbiddenDate"));
								trafficControlData.trafficControlInfos[i] = info;
							}

							JSONBuilder jDoc = new JSONBuilder();
							jDoc.put("rc", 0);
							jDoc.put("taskId", remoteTaskId);
							jDoc.put("data", JSON.toJSONString(trafficControlData));
							ServiceManager.getInstance().sendInvoke(pkgName, "comm.netdata.resp.traffic",
									jDoc.toBytes(), null);
						} else {
							onError(1004); // 不支持
						}
					} else {
						onError(1001); // 输入错误
					}
				} catch (Exception e) {
					onError(-1);
				}
			}

			@Override
			public void onError(int errorCode) {
				JSONBuilder doc = new JSONBuilder();
				doc.put("rc", 1);
				doc.put("taskId", remoteTaskId);
				doc.put("errorCode", errorCode);
				ServiceManager.getInstance().sendInvoke(pkgName, "comm.netdata.resp.traffic", doc.toBytes(), null);
			}
		});
		return (remoteTaskId + "").getBytes();
	}

	private byte[] procJokeInfoYunzhisheng(final String pkgName, String cmd, byte[] data){
		final int remoteTaskId = taskId++;
		String query = "讲个笑话";

		TextResultHandle.getInstance().parseText(query,TextResultHandle.MODULE_YUNZHISHENG_MASK, new ITextCallBack() {
			@Override
			public void onResult(String jsonResult) {
				try {
					JSONObject doc = new JSONObject(jsonResult);
					String style = doc.optString("style");
					String answer = doc.optString("answer");
					if ("joke".equals(style)) {
						JSONBuilder jDoc = new JSONBuilder();
						jDoc.put("rc", 0);
						jDoc.put("taskId", remoteTaskId);
						jDoc.put("data", answer);
						ServiceManager.getInstance().sendInvoke(pkgName, "comm.netdata.resp.joke",
								jDoc.toBytes(), null);
					} else {
						onError(-2);
					}
				} catch (Exception e) {
					onError(-1);
				}
			}

			@Override
			public void onError(int errorCode) {
				JSONBuilder doc = new JSONBuilder();
				doc.put("rc", 1);
				doc.put("taskId", remoteTaskId);
				doc.put("errorCode", errorCode);
				ServiceManager.getInstance().sendInvoke(pkgName, "comm.netdata.resp.joke", doc.toBytes(), null);
			}
		});

		return (remoteTaskId + "").getBytes();
	}
}
