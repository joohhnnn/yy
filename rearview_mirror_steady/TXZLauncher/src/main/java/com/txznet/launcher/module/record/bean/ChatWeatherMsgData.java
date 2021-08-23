package com.txznet.launcher.module.record.bean;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.txznet.comm.util.JSONBuilder;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by ASUS User on 2018/3/5.
 * 天气数据
 */

public class ChatWeatherMsgData extends BaseMsgData {
    public String strCityName;
    public int uint32FocusIndex;
    public ArrayList<WeatherData> mDatas;

    public ChatWeatherMsgData() {
        super(TYPE_CHAT_WEATHER);
    }


    public void parseData(String data) {
        if (data.contains("focusDateIndex")) {
            parseData(new JSONBuilder(data));
        } else {
            parseData(new Gson().fromJson(data, com.txznet.sdk.bean.WeatherData.class));
        }
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        strCityName = jsData.getVal("cityName", String.class);
        uint32FocusIndex = jsData.getVal("focusDateIndex", Integer.class);
        org.json.JSONArray rptMsgWeather = jsData.getVal("weatherDays", org.json.JSONArray.class);
        mDatas = new ArrayList<WeatherData>();
        for (int i = 0; i < rptMsgWeather.length(); i++) {
            WeatherData weatherData = new WeatherData();
            JSONBuilder jWeather = null;
            try {
                jWeather = new JSONBuilder(rptMsgWeather.getJSONObject(i));
                weatherData.uint32Year = jWeather.getVal("year", Integer.class, 0);
                weatherData.uint32Month = jWeather.getVal("month", Integer.class, 0);
                weatherData.uint32Day = jWeather.getVal("day", Integer.class, 0);
                weatherData.uint32DayOfWeek = jWeather.getVal("dayOfWeek", Integer.class, 0);
                weatherData.strWeather = jWeather.getVal("weather", String.class);
                weatherData.int32CurTemperature = jWeather.getVal("currentTemperature", Integer.class, 0);
                weatherData.int32LowTemperature = jWeather.getVal("lowestTemperature", Integer.class, 0);
                weatherData.int32HighTemperature = jWeather.getVal("highestTemperature", Integer.class, 0);
                try {
                    weatherData.str32Pm25 = jWeather.getVal("pm2_5", String.class, "0");
                } catch (ClassCastException cce) {
                    weatherData.str32Pm25 = jWeather.getVal("pm2_5", Integer.class, 0) + "";
                }
                weatherData.strAirQuality = jWeather.getVal("quality", String.class);
                if (TextUtils.isEmpty(weatherData.strAirQuality)) {
                    weatherData.strAirQuality = jWeather.getVal("lowestAQIDesc", String.class);
                }
                weatherData.strWind = jWeather.getVal("wind", String.class);
                weatherData.strCarWashIndex = jWeather.getVal("carWashIndex", String.class);
                weatherData.strCarWashIndexDesc = jWeather.getVal("carWashIndexDesc", String.class);
                weatherData.strTravelIndex = jWeather.getVal("travelIndex", String.class);
                weatherData.strTravelIndexDesc = jWeather.getVal("travelIndexDesc", String.class);
                weatherData.strSportIndex = jWeather.getVal("sportIndex", String.class);
                weatherData.strSportIndexDesc = jWeather.getVal("sportIndexDesc", String.class);
                weatherData.strSuggest = jWeather.getVal("suggest", String.class);
                weatherData.strComfortIndex = jWeather.getVal("comfortIndex", String.class);
                weatherData.strComfortIndexDesc = jWeather.getVal("comfortIndexDesc", String.class);
                weatherData.strColdIndex = jWeather.getVal("coldIndex", String.class);
                weatherData.strColdIndexDesc = jWeather.getVal("coldIndexDesc", String.class);
                weatherData.strMorningExerciseIndex = jWeather.getVal("morningExerciseIndex", String.class);
                weatherData.strMorningExerciseIndexDesc = jWeather.getVal("morningExerciseIndexDesc", String.class);
                weatherData.strDressIndex = jWeather.getVal("dressIndex", String.class);
                weatherData.strDressIndexDesc = jWeather.getVal("dressIndexDesc", String.class);
                weatherData.strUmbrellaIndex = jWeather.getVal("umbrellaIndex", String.class);
                weatherData.strUmbrellaIndexDesc = jWeather.getVal("umbrellaIndexDesc", String.class);
                weatherData.strSunBlockIndex = jWeather.getVal("sunBlockIndex", String.class);
                weatherData.strSunBlockIndexDesc = jWeather.getVal("sunBlockIndexDesc", String.class);
                weatherData.strDryingIndex = jWeather.getVal("dryingIndex", String.class);
                weatherData.strDryingIndexDesc = jWeather.getVal("dryingIndexDesc", String.class);
                weatherData.strDatingIndex = jWeather.getVal("datingIndex", String.class);
                weatherData.strDatingIndexDesc = jWeather.getVal("datingIndexDesc", String.class);
                mDatas.add(weatherData);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public void parseData(com.txznet.sdk.bean.WeatherData data) {
        strCityName = data.cityName;
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        com.txznet.sdk.bean.WeatherData.WeatherDay[] weatherDays = data.weatherDays;
        mDatas = new ArrayList<>();
        for (int i = 0; i < weatherDays.length; i++) {
            com.txznet.sdk.bean.WeatherData.WeatherDay dayInfo = weatherDays[i];
            if (dayInfo.dayOfWeek == dayOfWeek) { // 当天天气
                uint32FocusIndex = i;
            }
            WeatherData weatherData = new WeatherData();
            weatherData.uint32Year = dayInfo.year;
            weatherData.uint32Month = dayInfo.month;
            weatherData.uint32Day = dayInfo.day;
            weatherData.uint32DayOfWeek = dayInfo.dayOfWeek;
            weatherData.strWeather = dayInfo.weather;
            weatherData.int32CurTemperature = dayInfo.currentTemperature;
            weatherData.int32LowTemperature = dayInfo.lowestTemperature;
            weatherData.int32HighTemperature = dayInfo.highestTemperature;
            weatherData.str32Pm25 = dayInfo.pm2_5 + "";
            weatherData.strAirQuality = dayInfo.quality;
            weatherData.strWind = dayInfo.wind;
            weatherData.strCarWashIndex = dayInfo.carWashIndex;
            weatherData.strCarWashIndexDesc = dayInfo.carWashIndexDesc;
            weatherData.strTravelIndex = dayInfo.travelIndex;
            weatherData.strTravelIndexDesc = dayInfo.travelIndexDesc;
            weatherData.strSportIndex = dayInfo.sportIndex;
            weatherData.strSportIndexDesc = dayInfo.sportIndexDesc;
            weatherData.strSuggest = dayInfo.suggest;
            weatherData.strComfortIndex = dayInfo.comfortIndex;
            weatherData.strComfortIndexDesc = dayInfo.comfortIndexDesc;
            weatherData.strColdIndex = dayInfo.coldIndex;
            weatherData.strColdIndexDesc = dayInfo.coldIndexDesc;
            weatherData.strMorningExerciseIndex = dayInfo.morningExerciseIndex;
            weatherData.strMorningExerciseIndexDesc = dayInfo.morningExerciseIndexDesc;
            weatherData.strDressIndex = dayInfo.dressIndex;
            weatherData.strDressIndexDesc = dayInfo.dressIndexDesc;
            weatherData.strUmbrellaIndex = dayInfo.umbrellaIndex;
            weatherData.strUmbrellaIndexDesc = dayInfo.umbrellaIndexDesc;
            weatherData.strSunBlockIndex = dayInfo.sunBlockIndex;
            weatherData.strSunBlockIndexDesc = dayInfo.sunBlockIndexDesc;
            weatherData.strDryingIndex = dayInfo.dryingIndex;
            weatherData.strDryingIndexDesc = dayInfo.dryingIndexDesc;
            weatherData.strDatingIndex = dayInfo.datingIndex;
            weatherData.strDatingIndexDesc = dayInfo.datingIndexDesc;
            mDatas.add(weatherData);
        }
    }

    public static class WeatherData {
        public int uint32Year;
        public int uint32Month;
        public int uint32Day;
        public int uint32DayOfWeek;
        public String strWeather;
        public int int32CurTemperature;
        public int int32LowTemperature;
        public int int32HighTemperature;
        public String str32Pm25;
        public String strAirQuality;
        public String strWind;
        public String strCarWashIndex;
        public String strCarWashIndexDesc;
        public String strTravelIndex;
        public String strTravelIndexDesc;
        public String strSportIndex;
        public String strSportIndexDesc;
        public String strSuggest;
        public String strComfortIndex;
        public String strComfortIndexDesc;
        public String strColdIndex;
        public String strColdIndexDesc;
        public String strMorningExerciseIndex;
        public String strMorningExerciseIndexDesc;
        public String strDressIndex;
        public String strDressIndexDesc;
        public String strUmbrellaIndex;
        public String strUmbrellaIndexDesc;
        public String strSunBlockIndex;
        public String strSunBlockIndexDesc;
        public String strDryingIndex;
        public String strDryingIndexDesc;
        public String strDatingIndex;
        public String strDatingIndexDesc;
    }
}
