package com.txznet.comm.ui.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.voice.VoiceData.StockInfo;
import com.txz.ui.voice.VoiceData.WeatherData;
import com.txz.ui.voice.VoiceData.WeatherInfos;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;

/**
 * 将旧类型的数据转成新的需传输的数据
 */
public class DataConvertUtil {
	private DataConvertUtil(){
	}
	
	
	public static String convertKeyEvent(int keyEvent){
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "onKeyEvent");
		jsonBuilder.put("keyEvent", keyEvent);
		return jsonBuilder.toString();
	}
	
	public static String convertMsg(int owner,String msg){
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "addMsg");
		if(owner==0){
			jsonBuilder.put("type", "fromSys");
		}else if(owner == 1){
			jsonBuilder.put("type", "toSys");
		} else if (owner == 2) {
			jsonBuilder.put("type", "toSysPart");
		}
		jsonBuilder.put("msg", msg);
		return jsonBuilder.toString();
	}
	
	public static String convertList(String data){
		try{
			JSONBuilder doc = new JSONBuilder();
			Integer type = doc.getVal("type", Integer.class);
			
			doc.put("action", "addMsg");
			doc.put("type", "list");
			doc.put("data", data);
			return doc.toString();
		}catch(Exception e){
		}
		return null;
	} 
	
	public static String convertWeather(byte[] data,String tips){
		WeatherInfos info;
		try {
			info = WeatherInfos.parseFrom(data);
			JSONObject jObj = new JSONObject();
			jObj.put("action", "addMsg");
			jObj.put("type", "weather");
			jObj.put("strCityName", info.strCityName);
			jObj.put("uint32FocusIndex", info.uint32FocusIndex);
			jObj.put("vTips",tips);
			WeatherData[] weatherDatas = info.rptMsgWeather;
			JSONArray jWeatherArr = new JSONArray();
			for (int i = 0; i < weatherDatas.length; i++) {
				WeatherData weatherData = weatherDatas[i];
				JSONObject jWeather = new JSONObject();
				jWeather.put("uint32Year",
						weatherData.uint32Year);
				jWeather.put("uint32Month",
						weatherData.uint32Month);
				jWeather.put("uint32Day", weatherData.uint32Day);
				jWeather.put("uint32DayOfWeek",
						weatherData.uint32DayOfWeek);
				jWeather.put("strWeather",
						weatherData.strWeather);
				jWeather.put("int32CurTemperature",
						weatherData.int32CurTemperature);
				jWeather.put("int32LowTemperature",
						weatherData.int32LowTemperature);
				jWeather.put("int32HighTemperature",
						weatherData.int32HighTemperature);
				jWeather.put("int32Pm25", weatherData.int32Pm25);
				jWeather.put("strAirQuality",
						weatherData.strAirQuality);
				jWeather.put("strWind", weatherData.strWind);
				jWeather.put("strCarWashIndex",
						weatherData.strCarWashIndex);
				jWeather.put("strCarWashIndexDesc",
						weatherData.strCarWashIndexDesc);
				jWeather.put("strTravelIndex",
						weatherData.strTravelIndex);
				jWeather.put("strTravelIndexDesc",
						weatherData.strTravelIndexDesc);
				jWeather.put("strSportIndex",
						weatherData.strSportIndex);
				jWeather.put("strSportIndexDesc",
						weatherData.strSportIndexDesc);
				jWeather.put("strSuggest",
						weatherData.strSuggest);
				jWeather.put("strComfortIndex",
						weatherData.strComfortIndex);
				jWeather.put("strComfortIndexDesc",
						weatherData.strComfortIndexDesc);
				jWeather.put("strColdIndex",
						weatherData.strColdIndex);
				jWeather.put("strColdIndexDesc",
						weatherData.strColdIndexDesc);
				jWeather.put("strMorningExerciseIndex",
						weatherData.strMorningExerciseIndex);
				jWeather.put("strMorningExerciseIndexDesc",
						weatherData.strMorningExerciseIndexDesc);
				jWeather.put("strDressIndex",
						weatherData.strDressIndex);
				jWeather.put("strDressIndexDesc",
						weatherData.strDressIndexDesc);
				jWeather.put("strUmbrellaIndex",
						weatherData.strUmbrellaIndex);
				jWeather.put("strUmbrellaIndexDesc",
						weatherData.strUmbrellaIndexDesc);
				jWeather.put("strSunBlockIndex",
						weatherData.strSunBlockIndex);
				jWeather.put("strSunBlockIndexDesc",
						weatherData.strSunBlockIndexDesc);
				jWeather.put("strDryingIndex",
						weatherData.strDryingIndex);
				jWeather.put("strDryingIndexDesc",
						weatherData.strDryingIndexDesc);
				jWeather.put("strDatingIndex",
						weatherData.strDatingIndex);
				jWeather.put("strDatingIndexDesc",
						weatherData.strDatingIndexDesc);
				jWeatherArr.put(jWeather);
			}
			jObj.put("rptMsgWeather", jWeatherArr);
			return jObj.toString();
		} catch (InvalidProtocolBufferNanoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static String convertShock(byte[] data,String tips){
		try {
			StockInfo info = StockInfo.parseFrom(data);
			JSONObject jObj = new JSONObject();
			jObj.put("action", "addMsg");
			jObj.put("type", "shock");
			jObj.put("strName", info.strName);
			jObj.put("strCode", info.strCode);
			jObj.put("strUrl", info.strUrl);
			jObj.put("strCurrentPrice", info.strCurrentPrice);
			jObj.put("strChangeAmount", info.strChangeAmount);
			jObj.put("strChangeRate", info.strChangeRate);
			jObj.put("strHighestPrice", info.strHighestPrice);
			jObj.put("strLowestPrice", info.strLowestPrice);
			jObj.put("strTradingVolume", info.strTradingVolume);
			jObj.put("strYestodayClosePrice",
					info.strYestodayClosePrice);
			jObj.put("strTodayOpenPrice",
					info.strTodayOpenPrice);
			jObj.put("strUpdateTime", info.strUpdateTime);
			jObj.put("vTips",tips);
			return jObj.toString();
		} catch (Exception e) {
		}
		return null;
	}
	
	public static String convertMap(byte[] data){
		try {
			JSONObject jObj = new JSONObject();
			jObj.put("action", "addMsg");
			jObj.put("type", "map");
			jObj.put("data", new String(data));
			return jObj.toString();
		} catch (Exception e) {
		}
		return null;
	}
	
	public static String convertSnapPage(boolean next){
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "snapPage");
		jsonBuilder.put("next", ""+next);
		return jsonBuilder.toString();
	}
	
	public static String convertVolume(int volume){
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "updateVolume");
		jsonBuilder.put("value", volume);
		return jsonBuilder.toString();
	}
	
	public static String convertProgress(int value,int selection){
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "updateProgress");
		jsonBuilder.put("value", value);
		jsonBuilder.put("selection", selection);
		return jsonBuilder.toString();
	}

	public static String convertItemSelect(int selection) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "updateItemSelect");
		jsonBuilder.put("selection", selection);
		return jsonBuilder.toString();
	}
	
	public static String convertState(String type, int state) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "updateState");
		jsonBuilder.put("type", type);
		jsonBuilder.put("state", "" + state);
		return jsonBuilder.toString();
	}
	
	public static String convertData(String data){
		try{
			JSONBuilder jsonBuilder = new JSONBuilder(data);
			jsonBuilder.put("dataType", jsonBuilder.getVal("type", Integer.class));
			jsonBuilder.put("action", "addMsg");
			jsonBuilder.put("type", "data");
			return jsonBuilder.toString();
		}catch(Exception e){
			return null;
		}
	}
	
	public static String convertInformation(String data){
		try{
			JSONBuilder jsonBuilder = new JSONBuilder(data);
			jsonBuilder.put("action", "sendInformation");
			return jsonBuilder.toString();
		}catch(Exception e){
			return null;
		}
	}
	
}
