package com.txznet.sdk;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.WeatherData;

import org.json.JSONArray;

/**
 * 天气管理类，支持在询问天气的时候，从适配中获取天气来展示在界面中
 */

public class TXZWeatherManager {
    public static final String WEATHER_CMD_PREFIX = "txz.weather.cmd.";//core->sdk
    public static final String WEATHER_INVOKE_PREFIX = "txz.weather.invoke.";//sdk->core
    public static final String SET_TIMEOUT = "setTimeout";
    public static final String SET_WEATHER_TOOL = "setTool";
    public static final String CLEAR_WEATHER_TOOL = "clearTool";
    public static final String REQUEST_WEATHER = "request";
    public static final String RESULT_WEATHER = "result";
    public static final String ERROR_WEATHER = "error";
    private Long mTimeout = null;

    private static TXZWeatherManager sInstance = new TXZWeatherManager();

    private TXZWeatherManager() {
    }

    public static TXZWeatherManager getInstance() {
        return sInstance;
    }

    void onReconnectTXZ() {
        if (mTimeout != null) {
            setTimeout(mTimeout);
        }

        if (mWeatherTool != null) {
            setWeatherTool(mWeatherTool);
        }
    }


    WeatherTool mWeatherTool;

    /**
     * 设置天气工具
     *
     * @param weatherTool
     */
    public void setWeatherTool(WeatherTool weatherTool) {
        mWeatherTool = weatherTool;
        if (mWeatherTool == null) {
            TXZService.setCommandProcessor(WEATHER_CMD_PREFIX, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, WEATHER_INVOKE_PREFIX + CLEAR_WEATHER_TOOL, null, null);
        } else {
            TXZService.setCommandProcessor(WEATHER_CMD_PREFIX, mCommandProcessor);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, WEATHER_INVOKE_PREFIX + SET_WEATHER_TOOL, null, null);
        }

    }

    private TXZService.CommandProcessor mCommandProcessor = new TXZService.CommandProcessor() {
        @Override
        public byte[] process(String packageName, String command, byte[] data) {
            if (TextUtils.equals(REQUEST_WEATHER, command)) {
                if (mWeatherTool != null) {
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    String taskId = jsonBuilder.getVal("taskid", String.class);
                    String city = jsonBuilder.getVal("city", String.class);
                    String date = jsonBuilder.getVal("date", String.class);
                    String text = jsonBuilder.getVal("text", String.class);
                    mWeatherTool.requestWeather(city, date, text, new WeatherRequestListener(taskId));
                }
            }
            return null;
        }
    };


    /**
     * 天气数据获取工具
     */
    public static interface WeatherTool {
        /**
         * @param city                   查询的城市名
         * @param date                   查询的日期
         * @param text                   用户说的文本，视情况区分洗车，空气质量和普通的天气
         * @param weatherRequestListener 当次请求结果回调的接口
         */
        public void requestWeather(String city, String date, String text, WeatherRequestListener weatherRequestListener);
    }

    /**
     * 天气请求的回调接口，
     * 至少需要返回3天的天气数据
     */
    public static class WeatherRequestListener {
        private String mTaskId;
        private boolean mOnFinish = false;

        public WeatherRequestListener(String mTaskId) {
            this.mTaskId = mTaskId;
        }

        /**
         * 请求天气成功的回调，最少需要3天的天气
         *
         * @param weatherData
         */
        public synchronized void onResult(WeatherData weatherData) {
            if (!mOnFinish) {
                mOnFinish = true;
                if (weatherData != null && weatherData.weatherDays != null && weatherData.weatherDays.length >= 3) {
                    JSONBuilder jsonBuilder = parseWeatherData(weatherData);
                    jsonBuilder.put("taskid", mTaskId);
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, WEATHER_INVOKE_PREFIX + RESULT_WEATHER, jsonBuilder.toBytes(), null);
                } else {
                    LogUtil.logd("getWeather error weatherDays.length < 3 ");
                    JSONBuilder jsonBuilder = new JSONBuilder();
                    jsonBuilder.put("taskid", mTaskId);
                    jsonBuilder.put("msg", "data error");
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, WEATHER_INVOKE_PREFIX + ERROR_WEATHER, jsonBuilder.toBytes(), null);
                }
            }
        }

        /**
         * 请求天气失败时的回调
         *
         * @param msg
         */
        public synchronized void onError(String msg) {
            if (!mOnFinish) {
                mOnFinish = true;
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("taskid", mTaskId);
                jsonBuilder.put("msg", msg);
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, WEATHER_INVOKE_PREFIX + ERROR_WEATHER, jsonBuilder.toBytes(), null);
            }
        }

        private JSONBuilder parseWeatherData(WeatherData weatherData) {
            JSONBuilder jsonBuilder = new JSONBuilder();

            JSONArray jsonWeathers = new JSONArray();
            for (int i = 0; i < weatherData.weatherDays.length; i++) {
                JSONBuilder jsonWeather = new JSONBuilder();
                jsonWeather.put("year", weatherData.weatherDays[i].year);
                jsonWeather.put("month", weatherData.weatherDays[i].month);
                jsonWeather.put("day", weatherData.weatherDays[i].day);
                jsonWeather.put("dayOfWeek", weatherData.weatherDays[i].dayOfWeek);
                jsonWeather.put("currentTemperature", weatherData.weatherDays[i].currentTemperature);
                jsonWeather.put("highestTemperature", weatherData.weatherDays[i].highestTemperature);
                jsonWeather.put("lowestTemperature", weatherData.weatherDays[i].lowestTemperature);
                jsonWeather.put("pm2_5", weatherData.weatherDays[i].pm2_5);
                jsonWeather.put("quality", weatherData.weatherDays[i].quality);
                jsonWeather.put("weather", weatherData.weatherDays[i].weather);
                jsonWeather.put("wind", weatherData.weatherDays[i].wind);
                jsonWeather.put("carWashIndex", weatherData.weatherDays[i].carWashIndex);
                jsonWeather.put("carWashIndexDesc", weatherData.weatherDays[i].carWashIndexDesc);
                jsonWeather.put("travelIndex", weatherData.weatherDays[i].travelIndex);
                jsonWeather.put("travelIndexDesc", weatherData.weatherDays[i].travelIndexDesc);
                jsonWeathers.put(jsonWeather.getJSONObject());
            }
            JSONBuilder jsonResult = new JSONBuilder();
            jsonResult.put("weatherDays", jsonWeathers);
            jsonResult.put("focusDateIndex", weatherData.focusDateIndex);
            jsonResult.put("cityName", weatherData.cityName);

            JSONBuilder jsonData = new JSONBuilder();
            jsonData.put("result", jsonResult.getJSONObject());
            jsonData.put("header", weatherData.tts);

            jsonBuilder.put("data", jsonData.getJSONObject());

            return jsonBuilder;
        }
    }

    /**
     * 设置天气请求超时时间
     *
     * @param timeout
     */
    public void setTimeout(long timeout) {
        mTimeout = timeout;
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("timeout", timeout);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, WEATHER_INVOKE_PREFIX + SET_TIMEOUT, jsonBuilder.toBytes(), null);
    }

}
