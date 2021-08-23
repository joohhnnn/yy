package com.txznet.launcher.utils;

import com.txznet.launcher.R;

import java.util.HashMap;

import io.reactivex.Observable;

/**
 * Created by ASUS User on 2018/4/11.
 * 管理天气和图片的对应关系。
 */

public class WeatherUtils {

    static WeatherUtils instance = new WeatherUtils();
    private HashMap<String, WeatherBg> weatherBgHashMap;


    public static WeatherUtils getInstance() {
        return instance;
    }

    private WeatherUtils() {
        initWeatherBg();
    }

    /**
     * 天气和图片的对应关系
     */
    private void initWeatherBg() {
        weatherBgHashMap = new HashMap<String, WeatherBg>();
        weatherBgHashMap.put("暴雪", new WeatherBg(R.drawable.bg_baoxue_big, R.drawable.bg_baoxue, R.drawable.bg_baoxue_little));
        weatherBgHashMap.put("暴雨", new WeatherBg(R.drawable.bg_baoyu_big, R.drawable.bg_baoyu, R.drawable.bg_baoyu_little));
        weatherBgHashMap.put("冰雨", new WeatherBg(R.drawable.bg_bingyu_big, R.drawable.bg_bingyu, R.drawable.bg_bingyu_little));
        weatherBgHashMap.put("大暴雨", new WeatherBg(R.drawable.bg_dabaoyu_big, R.drawable.bg_dabaoyu, R.drawable.bg_dabaoyu_little));
        weatherBgHashMap.put("大雪", new WeatherBg(R.drawable.bg_daxue_big, R.drawable.bg_daxue, R.drawable.bg_daxue_little));
        weatherBgHashMap.put("大雨", new WeatherBg(R.drawable.bg_dayu_big, R.drawable.bg_dayu, R.drawable.bg_weather_dayu_little));
        weatherBgHashMap.put("多云", new WeatherBg(R.drawable.bg_duoyun_big, R.drawable.bg_duoyun, R.drawable.bg_duoyun_little));
        weatherBgHashMap.put("浮尘", new WeatherBg(R.drawable.bg_fucheng_big, R.drawable.bg_fucheng, R.drawable.bg_fucheng_little));
        weatherBgHashMap.put("雷阵雨", new WeatherBg(R.drawable.bg_leizhengyu_big, R.drawable.bg_leizhengyu, R.drawable.bg_leizhengyu_little));
        weatherBgHashMap.put("雷阵雨伴有冰雹", new WeatherBg(R.drawable.bg_leiyubingbao_big, R.drawable.bg_leiyubingbao, R.drawable.bg_leiyubingbao_little));
        weatherBgHashMap.put("霾", new WeatherBg(R.drawable.bg_mai_big, R.drawable.bg_mai, R.drawable.bg_mai_little));
        weatherBgHashMap.put("晴", new WeatherBg(R.drawable.bg_qing_big, R.drawable.bg_qing, R.drawable.bg_qing_little));
        weatherBgHashMap.put("沙尘暴", new WeatherBg(R.drawable.bg_shachengbao_big, R.drawable.bg_shachengbao, R.drawable.bg_shachengbao_little));
        weatherBgHashMap.put("强沙尘暴", new WeatherBg(R.drawable.bg_shachengbao_big, R.drawable.bg_shachengbao, R.drawable.bg_shachengbao_little));
        weatherBgHashMap.put("特大暴雨", new WeatherBg(R.drawable.bg_tedabaoyu_big, R.drawable.bg_tedabaoyu, R.drawable.bg_tedabaoyu_little));
        weatherBgHashMap.put("雾", new WeatherBg(R.drawable.bg_wu_big, R.drawable.bg_wu, R.drawable.bg_wu_little));
        weatherBgHashMap.put("小雪", new WeatherBg(R.drawable.bg_xiaoxue_big, R.drawable.bg_xiaoxue, R.drawable.bg_xiaoxue_little));
        weatherBgHashMap.put("小雨", new WeatherBg(R.drawable.bg_xiaoyu_big, R.drawable.bg_xiaoyu, R.drawable.bg_xiaoyu_little));
        weatherBgHashMap.put("扬沙", new WeatherBg(R.drawable.bg_yangsha_big, R.drawable.bg_yangsha, R.drawable.bg_yangsha_little));
        weatherBgHashMap.put("阴", new WeatherBg(R.drawable.bg_ying_big, R.drawable.bg_ying, R.drawable.bg_ying_little));
        weatherBgHashMap.put("雨夹雪", new WeatherBg(R.drawable.bg_yujiaxue_big, R.drawable.bg_yujiaxue, R.drawable.bg_yujiaxue_little));
        weatherBgHashMap.put("阵雨", new WeatherBg(R.drawable.bg_zhengyu_big, R.drawable.bg_zhengyu, R.drawable.bg_zhengyu_little));
        weatherBgHashMap.put("阵雪", new WeatherBg(R.drawable.bg_zhengxue_big, R.drawable.bg_zhengxue, R.drawable.bg_zhengxue_little));
        weatherBgHashMap.put("中雪", new WeatherBg(R.drawable.bg_zhongxue_big, R.drawable.bg_zhongxue, R.drawable.bg_zhongxue_little));
        weatherBgHashMap.put("中雨", new WeatherBg(R.drawable.bg_zhongyu_big, R.drawable.bg_zhongyu, R.drawable.bg_zhongyu_little));
        weatherBgHashMap.put("NA", new WeatherBg(R.drawable.bg_meiyoushuji_big, R.drawable.bg_meiyoushuji, R.drawable.bg_meiyoushuji_little));

        weatherBgHashMap.put("夜间多云", new WeatherBg(R.drawable.bg_yejianduoyun_big, R.drawable.bg_yejianduoyun, R.drawable.bg_yejianduoyun_little));
        weatherBgHashMap.put("夜间晴", new WeatherBg(R.drawable.bg_yejianqing_big, R.drawable.bg_yejianqing, R.drawable.bg_yejianqing_little));
        weatherBgHashMap.put("夜间阵雪", new WeatherBg(R.drawable.bg_yejianzhengxue_big, R.drawable.bg_yejianzhengxue, R.drawable.bg_yejianzhengxue_little));
        weatherBgHashMap.put("夜间阵雨", new WeatherBg(R.drawable.bg_yejianzhengyu_big, R.drawable.bg_yejianzhengyu, R.drawable.bg_yejianzhengyu_little));
    }

    public static class WeatherBg {
        public WeatherBg(int bigBg, int normalBg, int littleBg) {
            this.bigBg = bigBg;
            this.normalBg = normalBg;
            this.littleBg = littleBg;
        }

        public int bigBg;
        public int normalBg;
        public int littleBg;
    }

    public WeatherBg getWeatherBg(String weather) {
        return getWeatherBg(weather, false);
    }

    /**
     * 根据天气获取图片ID
     *
     * @param weather
     * @return
     */
    public WeatherBg getWeatherBg(String weather, boolean isNight) {
        WeatherBg weatherBg = null;
        if (weather == null || weather.equals("")) {
            weather = "NA";
        } else {
            if (weather.contains("到")) {
                int index = weather.indexOf("到");
                weather = weather.substring(index + 1, weather.length());
            }
            if (weather.contains("转")) {
                int index = weather.indexOf("转");
                weather = weather.substring(index + 1, weather.length());
            }
        }
        if (isNight) {
            weatherBg = weatherBgHashMap.get("夜间" + weather);
        }
        if (weatherBg == null) {
            weatherBg = weatherBgHashMap.get(weather);
        }
        if (weatherBg == null) {
            weatherBg = weatherBgHashMap.get("NA");
        }
        return weatherBg;
    }
}
