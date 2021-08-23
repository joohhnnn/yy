package com.txznet.launcher.sp;

import android.content.Context;

import com.txznet.comm.sp.CommonSp;

public class WeatherPluginSp extends CommonSp {
    private static final String SP_NAME = "weather_conf";

    protected WeatherPluginSp(Context context) {
        super(context, SP_NAME);
    }

    private static WeatherPluginSp sInstance;

    public static WeatherPluginSp getInstance(Context context) {
        if (sInstance == null) {
            synchronized (WeatherPluginSp.class) {
                sInstance = new WeatherPluginSp(context);
            }
        }
        return sInstance;
    }

    private static final String KEY_WEATHER = "weather";
    private static final String KEY_TEMPERATURE = "temperature";
    private static final String KEY_PM = "pm";

    public void setWeather(String val) {
        setValue(KEY_WEATHER, val);
    }

    public String getWeather(String defVal) {
        return getValue(KEY_WEATHER, defVal);
    }

    public void setTemperature(int temperature) {
        setValue(KEY_TEMPERATURE, temperature);
    }

    public int getTemperature(int defVal) {
        return getValue(KEY_TEMPERATURE, defVal);
    }
    public void setPm(String temperature) {
    	setValue(KEY_PM, temperature);
    }
    
    public String getpm(String defVal) {
    	return getValue(KEY_PM, defVal);
    }

    
    
}
