package com.txznet.launcher.ui.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.sp.WeatherPluginSp;
import com.txznet.loader.AppLogic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class WeatherPlugin extends LinearLayout {
    private static final String TAG = WeatherPlugin.class.getSimpleName();

    public WeatherPlugin(Context context) {
        super(context);
        initView();
    }

    public WeatherPlugin(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private ImageView mWeather;
    private TextView mDayInfo;
    private TextView mWeatherInfo;

    private void initView() {
        LayoutInflater.from(getContext()).inflate(
                R.layout.view_weather_plugin, this);
        mWeather = (ImageView) findViewById(R.id.img_Weather);
        mDayInfo = (TextView) findViewById(R.id.txt_Day);
        mWeatherInfo = (TextView) findViewById(R.id.txt_Weather);
        refreshWeatherInfo(WeatherPluginSp.getInstance(getContext())
                .getWeather("N/A"), WeatherPluginSp.getInstance(getContext())
                .getTemperature(25));
        refreshDayInfo();
        autoController();
    }

    private Runnable mWeatherRefreshTask;

    private void autoController() {
        registerWeatherHandler();
    }

    private void registerWeatherHandler() {
        // 间隔20分钟，
        mWeatherRefreshTask = new Runnable() {
            @Override
            public void run() {
                // 加一个超时刷新任务
                AppLogic.removeUiGroundCallback(mWeatherRefreshTask);
                AppLogic.runOnUiGround(mWeatherRefreshTask, 1000 * 30);

                TextUtil.parseText("今天天气", new TextUtil.ITextCallBack() {
                    @Override
                    public void onResult(String jsonResult) {
                        AppLogic.removeUiGroundCallback(mWeatherRefreshTask);

                        if (TextUtils.isEmpty(jsonResult)) {
                            // 错误的回调，延迟后再次执行
                            AppLogic.removeUiGroundCallback(mWeatherRefreshTask);
                            AppLogic.runOnUiGround(mWeatherRefreshTask, 1000 * 30);
                            return;
                        }
                        try {
                            JSONObject doc = new JSONObject(jsonResult);
                            if (doc.has("rc")) {
                                int rc = doc.getInt("rc");
                                if (rc == 0) {
                                    JSONArray data = doc.getJSONObject("data")
                                            .getJSONObject("result")
                                            .getJSONArray("weatherDays");
                                    if (data != null && data.length() > 0) {
                                        JSONObject weatherDay = data.getJSONObject(0);
                                        String weather = weatherDay.getString("weather");
                                        int currentTemperature = weatherDay.getInt("currentTemperature");
                                        refreshWeatherInfo(weather, currentTemperature);
                                        WeatherPluginSp.getInstance(getContext()).setWeather(weather);
                                        WeatherPluginSp.getInstance(getContext()).setTemperature(currentTemperature);

                                        // 成功的回调
                                        AppLogic.removeUiGroundCallback(mWeatherRefreshTask);
                                        AppLogic.runOnUiGround(mWeatherRefreshTask, 1000 * 60 * 20);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            AppLogic.removeUiGroundCallback(mWeatherRefreshTask);
                            AppLogic.runOnUiGround(mWeatherRefreshTask, 1000 * 30);
                        }
                    }

                    @Override
                    public void onError(int errorCode) {
                        super.onError(errorCode);
                    }
                });
            }
        };
        ServiceManager.getInstance().keepConnection(ServiceManager.TXZ, mWeatherRefreshTask);
        mWeatherRefreshTask.run();
    }

    private BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                refreshDayInfo(1000 * 5);
            }
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // ACTION_DATE_CHANGE
        getContext().registerReceiver(mTimeReceiver,
                new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mTimeReceiver);
    }

    public void refreshWeatherInfo(final String weather, final int temperature) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                String weatherInfo = "%s %s°C";
                mWeatherInfo.setText(String.format(weatherInfo, weather,
                        temperature));
                int res = getResourceIdByWeather(weather);
                if (res == 0) {
                    res = R.drawable.weather_duoyun;
                }
                mWeather.setImageResource(res);
                Log.d(TAG, "weather info[weather=" + weather + ", temperature="
                        + temperature + "]");
            }
        }, 0);
    }

    public void refreshDayInfo(long delay) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                int month = calendar.get(Calendar.MONTH) + 1;
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                String week = null;

                switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                    case Calendar.SATURDAY:
                        week = "周六";
                        break;
                    case Calendar.SUNDAY:
                        week = "周日";
                        break;
                    case Calendar.MONDAY:
                        week = "周一";
                        break;
                    case Calendar.TUESDAY:
                        week = "周二";
                        break;
                    case Calendar.WEDNESDAY:
                        week = "周三";
                        break;
                    case Calendar.THURSDAY:
                        week = "周四";
                        break;
                    case Calendar.FRIDAY:
                        week = "周五";
                        break;
                }
                String dayFormat = "%s %s/%s";
                mDayInfo.setText(String.format(dayFormat, week,
                        month < 10 ? "0" + month : month, day < 10 ? "0" + day
                                : day));
            }
        }, delay);
    }

    public void refreshDayInfo() {
        refreshDayInfo(0);
    }

    public void notifyException() {

    }

    /**
     * 根据天气获取图片ID
     *
     * @param weather
     * @return
     */
    private int getResourceIdByWeather(String weather) {
        int id = 0;
        if (weather == null || weather.equals(""))
            return id;

        // 过滤小到XXXX 中到XXX 大到XXXXX
        if (weather.contains("到") || weather.contains("转")) {
            int index = weather.indexOf("到");
            weather = weather.substring(index + 1, weather.length());
        }
        
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        boolean isNight = hour >= 18 || hour <= 6 ? true : false;

        if ("暴雪".equals(weather)) {
            id = R.drawable.weather_baoxue;
        } else if ("暴雨".equals(weather)) {
            id = R.drawable.weather_baoyu;
        } else if ("冰雨".equals(weather)) {
            id = R.drawable.weather_bingyu;
        } else if ("大暴雨".equals(weather)) {
            id = R.drawable.weather_dabaoyu;
        } else if ("大雪".equals(weather)) {
            id = R.drawable.weather_daxue;
        } else if ("大雨".equals(weather)) {
            id = R.drawable.weather_dayu;
        } else if ("多云".equals(weather)) {
        	if(isNight){
        		id = R.drawable.weather_duoyun_night;
        	}else{
        		id = R.drawable.weather_duoyun;
        	}
        } else if ("浮尘".equals(weather)) {
            id = R.drawable.weather_fuchen;
        } else if ("雷阵雨".equals(weather)) {
            id = R.drawable.weather_leizhenyu;
        } else if ("雷阵雨伴有冰雹".equals(weather)) {
            id = R.drawable.weather_leizhenyubanyoubingbao;
        } else if ("霾".equals(weather)) {
            id = R.drawable.weather_mai;
        } else if ("晴".equals(weather)) {
        	if(isNight){
        		id = R.drawable.weather_qing_night;
        	}else{
        		id = R.drawable.weather_qing;
        	}
        } else if ("沙尘暴".equals(weather)) {
            id = R.drawable.weather_shachenbao;
        } else if ("特大暴雨".equals(weather)) {
            id = R.drawable.weather_tedabaoyu;
        } else if ("雾".equals(weather)) {
            id = R.drawable.weather_wu;
        } else if ("小雪".equals(weather)) {
            id = R.drawable.weather_xiaoxue;
        } else if ("小雨".equals(weather)) {
            id = R.drawable.weather_xiaoyu;
        } else if ("扬沙".equals(weather)) {
            id = R.drawable.weather_yangsha;
        } else if ("阴".equals(weather)) {
            id = R.drawable.weather_yin;
        } else if ("雨夹雪".equals(weather)) {
            id = R.drawable.weather_yujiaxue;
        } else if ("阵雨".equals(weather)) {
        	if(isNight){
        		id = R.drawable.weather_zhenyu_night;
        	}else{
        		id = R.drawable.weather_zhenyu;
        	}
        } else if ("中雪".equals(weather)) {
            id = R.drawable.weather_zhongxue;
        } else if ("中雨".equals(weather)) {
            id = R.drawable.weather_zhongyu;
        } else if("阵雪".equals(weather)){
        	if(isNight){
        		id = R.drawable.weather_zhenxue_night;
        	}else{
        		id = R.drawable.weather_zhenxue;
        	}
        }
        return id;
    }
}
