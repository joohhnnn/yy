package com.txznet.txz.module.netdata;

import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.TrafficControlData;
import com.txznet.sdk.bean.TrafficControlData.TrafficControlInfo;
import com.txznet.sdk.bean.WeatherData;
import com.txznet.sdk.bean.WeatherData.WeatherDay;
import com.txznet.txz.component.text.IText.ITextCallBack;
import com.txznet.txz.module.text.TextManager;

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
			return procWeatherYunzhisheng(pkgName, cmd, data);
		} else if (cmd.equals("traffic")) {
			return procTrafficInfoYunzhisheng(pkgName, cmd, data);
		}
		return null;
	}

	private byte[] procWeatherYunzhisheng(final String pkgName, String cmd, byte[] data) {
		final int remoteTaskId = taskId++;
		JSONBuilder doc = new JSONBuilder(data);
		String city = doc.getVal("city", String.class);
		String query = "cur".equals(city) ? "今天天气" : String.format("%s的天气", city);
		TextManager.getInstance().parseText(query, new ITextCallBack() {
			@Override
			public void onResult(String jsonResult) {
				// TODO 解析
				try {
					JSONObject doc = new JSONObject(jsonResult);
					int rc = doc.optInt("rc", -1);
					String service = doc.optString("service");
					if (rc == 0 && "cn.yunzhisheng.weather".equals(service)) {
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
		TextManager.getInstance().parseText(query, new ITextCallBack() {
			@Override
			public void onResult(String jsonResult) {
				try {
					JSONObject doc = new JSONObject(jsonResult);
					int rc = doc.optInt("rc", -1);
					String service = doc.optString("service");
					if (rc == 0 && "cn.yunzhisheng.traffic.control".equals(service)) {
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
}
