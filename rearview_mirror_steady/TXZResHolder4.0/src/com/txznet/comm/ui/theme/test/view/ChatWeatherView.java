package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.utils.DimenUtils;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.data.ChatWeatherViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatWeatherView;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.resholder.R;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 天气页面
 * <p>
 * 2020-08-05 10:00
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class ChatWeatherView extends IChatWeatherView {

    private static ChatWeatherView sInstance = new ChatWeatherView();
    private final int[] TEMPERATURE_DRAWABLE_RES = {
            R.drawable.weather_number_0,
            R.drawable.weather_number_1,
            R.drawable.weather_number_2,
            R.drawable.weather_number_3,
            R.drawable.weather_number_4,
            R.drawable.weather_number_5,
            R.drawable.weather_number_6,
            R.drawable.weather_number_7,
            R.drawable.weather_number_8,
            R.drawable.weather_number_9,
    };


    private ChatWeatherView() {
    }

    public static ChatWeatherView getInstance() {
        return sInstance;
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        ChatWeatherViewData viewData = (ChatWeatherViewData) data;
        VoiceData.WeatherInfos infos = parseData(viewData.textContent);

        View view;
        if (isShowOneDay(infos)) {
            view = createViewByOne(infos);
        } else {
            view = createViewMore(infos);
        }

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = ChatWeatherView.getInstance();
        return adapter;
    }

    /**
     * 只显示一天的天气
     *
     * @param infos
     * @return
     */
    private View createViewByOne(VoiceData.WeatherInfos infos) {
        Context context = UIResLoader.getInstance().getModifyContext();
        Resources resources = context.getResources();

        View view = LayoutInflater.from(context).inflate(R.layout.chat_weather_view_oneday, (ViewGroup) null, false);
        TextView tvTemperature = view.findViewById(R.id.tvTemperature);
        TextView tvWeather = view.findViewById(R.id.tvWeather);
        TextView tvWind = view.findViewById(R.id.tvWind);
        TextView tvCity = view.findViewById(R.id.tvCity);
        ImageView ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
        TextView tvAirQuality = view.findViewById(R.id.tvAirQuality);
        View bgView = view.findViewById(R.id.bg);// 背景

        try {
            // uint32FocusIndex要显示的第几天数据
            VoiceData.WeatherData row = infos.rptMsgWeather[infos.uint32FocusIndex];

            tvTemperature.setText(String.format(Locale.getDefault(), "%d°C / %d°C",
                    row.int32LowTemperature,
                    row.int32HighTemperature));
            tvWeather.setText(row.strWeather);
            refreshBackground(bgView, row.strWeather);
            tvWind.setText(row.strWind);
            tvCity.setText(infos.strCityName);
            ivWeatherIcon.setImageDrawable(getDrawableByWeather(row.strWeather, true));
            if (TextUtils.isEmpty(row.strAirQuality)) {
                tvAirQuality.setText("未知");
                tvAirQuality.setBackground(getBgDrawable(resources, "未知"));
            } else {
                tvAirQuality.setText(row.int32Pm25 + " " + row.strAirQuality);
                tvAirQuality.setBackground(getBgDrawable(resources, row.strAirQuality));
            }
        }catch (Exception e){
            // ignore
        }

        return view;
    }

    /**
     * 显示多天的天气
     *
     * @param infos
     * @return
     */
    private View createViewMore(VoiceData.WeatherInfos infos) {
        Context context = UIResLoader.getInstance().getModifyContext();
        Resources resources = context.getResources();

        View rootView = LayoutInflater.from(context).inflate(R.layout.chat_weather_view_more, (ViewGroup) null, false);
        ViewGroup container = rootView.findViewById(R.id.containerWeather);

        {
            TextView tvTemperature = rootView.findViewById(R.id.tvTemperature);
            TextView tvWeather = rootView.findViewById(R.id.tvWeather);
            TextView tvWind = rootView.findViewById(R.id.tvWind);
            TextView tvCity = rootView.findViewById(R.id.tvCity);
            ImageView ivWeatherIcon = rootView.findViewById(R.id.ivWeatherIcon);
            TextView tvAirQuality = rootView.findViewById(R.id.tvAirQuality);
            View bgView = rootView.findViewById(R.id.bgView);// 背景

            VoiceData.WeatherData row = infos.rptMsgWeather[0];

            refreshBackground(bgView, row.strWeather);// 背景
            tvTemperature.setText(String.format(Locale.getDefault(), "%d°C / %d°C",
                    row.int32LowTemperature,
                    row.int32HighTemperature));
            tvWeather.setText(row.strWeather);
            tvWind.setText(row.strWind);
            tvCity.setText(infos.strCityName);
            ivWeatherIcon.setImageDrawable(getDrawableByWeather(row.strWeather, true));
            if (TextUtils.isEmpty(row.strAirQuality)) {
                tvAirQuality.setText("未知");
                tvAirQuality.setBackground(getBgDrawable(resources, "未知"));
            } else {
                tvAirQuality.setText(row.int32Pm25 + " " + row.strAirQuality);
                tvAirQuality.setBackground(getBgDrawable(resources, row.strAirQuality));
            }
        }


        int len = infos.rptMsgWeather.length;
        for (int i = 0; i < len; i++) {
            VoiceData.WeatherData row = infos.rptMsgWeather[i];

            View view = LayoutInflater.from(context).inflate(R.layout.chat_weather_view_more_item, (ViewGroup) null, false);
            TextView tvTemperature = view.findViewById(R.id.tvTemperature);
            TextView tvDate = view.findViewById(R.id.tvDate);
            TextView tvWeather = view.findViewById(R.id.tvWeather);
            ImageView ivWeatherIcon = view.findViewById(R.id.ivWeatherIcon);
            TextView tvAirQuality = view.findViewById(R.id.tvAirQuality);

            tvTemperature.setText(String.format(Locale.getDefault(), "%d°C / %d°C",
                    row.int32LowTemperature, row.int32HighTemperature));

            String today = String.format(Locale.getDefault(), "%d月%d日",
                    row.uint32Month, row.uint32Day);
            tvDate.setText(today);

            tvWeather.setText(row.strWeather);
            ivWeatherIcon.setImageDrawable(getDrawableByWeather(row.strWeather, true));
            if (TextUtils.isEmpty(row.strAirQuality)) {
                tvAirQuality.setText("未知");
            } else {
                tvAirQuality.setText(row.strAirQuality);
            }

            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            container.addView(view, lp);
        }

        return rootView;
    }


    /**
     * 解析天气数据放入WeatherInfos
     *
     * @param jsonStr JSON字符串
     */
    public VoiceData.WeatherInfos parseData(String jsonStr) {
        VoiceData.WeatherInfos infos = new VoiceData.WeatherInfos();
        JSONBuilder jsonBuilder = new JSONBuilder(jsonStr);
        LogUtil.logd(WinLayout.logTag + "weatherData: " + jsonStr);
        WinLayout.getInstance().vTips = jsonBuilder.getVal("vTips", String.class);
        LogUtil.logd(WinLayout.logTag + "weatherData: --vTips" + WinLayout.getInstance().vTips);
        infos.strCityName = jsonBuilder.getVal("strCityName", String.class);
        infos.uint32FocusIndex = jsonBuilder.getVal("uint32FocusIndex", Integer.class);
        org.json.JSONArray rptMsgWeather = jsonBuilder.getVal("rptMsgWeather", org.json.JSONArray.class);
        int length = rptMsgWeather.length();
        infos.rptMsgWeather = new VoiceData.WeatherData[length];
        for (int i = 0; i < length; i++) {
            VoiceData.WeatherData weatherData = new VoiceData.WeatherData();
            try {
                JSONBuilder jWeather = new JSONBuilder(rptMsgWeather.getJSONObject(i));
                weatherData.uint32Year = jWeather.getVal("uint32Year", Integer.class);
                weatherData.uint32Month = jWeather.getVal("uint32Month", Integer.class);
                weatherData.uint32Day = jWeather.getVal("uint32Day", Integer.class);
                weatherData.uint32DayOfWeek = jWeather.getVal("uint32DayOfWeek", Integer.class);
                weatherData.strWeather = jWeather.getVal("strWeather", String.class);
                weatherData.int32CurTemperature = jWeather.getVal("int32CurTemperature", Integer.class);
                weatherData.int32LowTemperature = jWeather.getVal("int32LowTemperature", Integer.class);
                weatherData.int32HighTemperature = jWeather.getVal("int32HighTemperature", Integer.class);
                weatherData.int32Pm25 = jWeather.getVal("int32Pm25", Integer.class);
                weatherData.strAirQuality = jWeather.getVal("strAirQuality", String.class);
                weatherData.strWind = jWeather.getVal("strWind", String.class);
                weatherData.strCarWashIndex = jWeather.getVal("strCarWashIndex", String.class);
                weatherData.strCarWashIndexDesc = jWeather.getVal("strCarWashIndexDesc", String.class);
                weatherData.strTravelIndex = jWeather.getVal("strTravelIndex", String.class);
                weatherData.strTravelIndexDesc = jWeather.getVal("strTravelIndexDesc", String.class);
                weatherData.strSportIndex = jWeather.getVal("strSportIndex", String.class);
                weatherData.strSportIndexDesc = jWeather.getVal("strSportIndexDesc", String.class);
                weatherData.strSuggest = jWeather.getVal("strSuggest", String.class);
                weatherData.strComfortIndex = jWeather.getVal("strComfortIndex", String.class);
                weatherData.strComfortIndexDesc = jWeather.getVal("strComfortIndexDesc", String.class);
                weatherData.strColdIndex = jWeather.getVal("strColdIndex", String.class);
                weatherData.strColdIndexDesc = jWeather.getVal("strColdIndexDesc", String.class);
                weatherData.strMorningExerciseIndex = jWeather.getVal("strMorningExerciseIndex", String.class);
                weatherData.strMorningExerciseIndexDesc = jWeather.getVal("strMorningExerciseIndexDesc", String.class);
                weatherData.strDressIndex = jWeather.getVal("strDressIndex", String.class);
                weatherData.strDressIndexDesc = jWeather.getVal("strDressIndexDesc", String.class);
                weatherData.strUmbrellaIndex = jWeather.getVal("strUmbrellaIndex", String.class);
                weatherData.strUmbrellaIndexDesc = jWeather.getVal("strUmbrellaIndexDesc", String.class);
                weatherData.strSunBlockIndex = jWeather.getVal("strSunBlockIndex", String.class);
                weatherData.strSunBlockIndexDesc = jWeather.getVal("strSunBlockIndexDesc", String.class);
                weatherData.strDryingIndex = jWeather.getVal("strDryingIndex", String.class);
                weatherData.strDryingIndexDesc = jWeather.getVal("strDryingIndexDesc", String.class);
                weatherData.strDatingIndex = jWeather.getVal("strDatingIndex", String.class);
                weatherData.strDatingIndexDesc = jWeather.getVal("strDatingIndexDesc", String.class);
                infos.rptMsgWeather[i] = weatherData;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return infos;
    }

    @Override
    public void init() {
        super.init();
    }

    public void onUpdateParams(int styleIndex) {


    }


    /**
     * @param strWeather 天气：暴雪、大雪、...
     */
    private void refreshBackground(View view, String strWeather) {
        if (TextUtils.isEmpty(strWeather)) {
            return;
        }
        LogUtil.logd(WinLayout.logTag + "refrshBackground:strWeather: " + strWeather);
        strWeather = filterWeather(strWeather);


        final int[] defaultColors = {0xFF525E8B, 0xFFCCB4B9};
        Map<String, int[]> map = new HashMap<String, int[]>() {{
            put("阴", new int[]{0xFF5388AB, 0xFFA2CFED});
            put("小雨", new int[]{0xFF3DACA0, 0xFF93D2BF});
            put("中雨", new int[]{0xFF3D9A9C, 0xFF59D7B0});
            put("大雨", new int[]{0xFF107173, 0xFF4CCCA4});
            put("暴雨", new int[]{0xFF045B5D, 0xFFC2E6FE});
            put("大暴雨", new int[]{0xFF064B49, 0xFF299092});
            put("特大暴雨", new int[]{0xFF002A30, 0xFF19706E});
            put("冰雨", new int[]{0xFF30B1A3, 0xFF83D3BA});
            put("雷阵雨", new int[]{0xFF3044F7, 0xFFB5AFFC});
            put("雷阵雨伴有冰雹", new int[]{0xFF3044F7, 0xFFB5AFFC});
            put("雨夹雪", new int[]{0xFF7B96CA, 0xFFACCEEF});
            put("小雪", new int[]{0xFF7B96CA, 0xFFACCEFF});
            put("中雪", new int[]{0xFF6883B7, 0xFF8EB5D8});
            put("大雪", new int[]{0xFF536D9E, 0xFF789CBE});
            put("暴雪", new int[]{0xFF435B88, 0xFF5980A6});
            put("沙尘暴", new int[]{0xFFC19448, 0xFFDAB980});
            put("扬沙", new int[]{0xFFC19448, 0xFFDAB980});
            put("浮尘", new int[]{0xFFC19448, 0xFFDAB980});
            put("雾", new int[]{0xFF5F6F84, 0xFFACC4DA});
            put("霾", new int[]{0xFF5F6F84, 0xFF93ABC0});

            put("夜间晴", defaultColors);
            put("夜间多云", defaultColors);
            put("夜间阵雨", defaultColors);
            put("夜间阵雪", defaultColors);

        }};

        int[] colorAry = map.get(strWeather);
        if (colorAry != null) {
            GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colorAry);
            refreshBackground(view, drawable);
        }

        GradientDrawable gradientDrawable;
        switch (strWeather) {
            case "晴": {
                if (DateUtils.isNight()) {
                    int[] colors = {0xFF2A3E58, 0xFF6B8BAA};
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                } else {
                    int[] colors = {0xFF0067BF, 0xFF8AD6F7};
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                }
                break;
            }
            case "多云": {
                if (DateUtils.isNight()) {
                    int[] colors = {0xFF253851, 0xFF5D7D9B};
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                } else {
                    int[] colors = {0xFF6AA4CA, 0xFFC2E6FE};
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                }
                break;
            }
            case "阵雨": {
                if (DateUtils.isNight()) {
                    int[] colors = {0xFF003E67, 0xFF005679};
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                } else {
                    int[] colors = {0xFF3DACA0, 0xFF93D2BF};
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                }
                break;
            }
            case "阵雪":
                if (DateUtils.isNight()) {
                    int[] colors = {0xFF444B57, 0xFF717B89};
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                } else {
                    int[] colors = {0xFF7B96CA, 0xFFACCEEF};
                    gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
                }
                break;
            default:
                gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, defaultColors);
                break;
        }

        refreshBackground(view, gradientDrawable);
    }

    /**
     * 更新天气界面背景
     *
     * @param gradientDrawable
     */
    public void refreshBackground(View view, GradientDrawable gradientDrawable) {
        if (gradientDrawable != null) {
            float radius = UIResLoader.getInstance().getModifyContext().getResources().getDimension(R.dimen.mdp10);
            gradientDrawable.setCornerRadius(radius);
            view.setBackground(gradientDrawable);
        } else {
            view.setBackgroundResource(R.drawable.xml_weather_background_default);
        }
    }

    /**
     * 根据空气质量获取背景图片
     *
     * @param
     */
    private Drawable getBgDrawable(Resources resources, String degree) {
        LogUtil.logd(WinLayout.logTag + "getAirBgDrawable: " + degree);

        int drawableId;
        switch (degree) {
            case "优":
                drawableId = R.drawable.air_bg_excellent;
                break;
            case "良":
                drawableId = R.drawable.air_bg_good;
                break;
            default:
                drawableId = R.drawable.air_bg_bad;
                break;
        }

        return resources.getDrawable(drawableId);
    }

    //过滤小到XXXX 中到XXX 大到XXXXX
    private String filterWeather(String weather) {
        if (weather.contains("到")) {
            int index = weather.indexOf("到");
            weather = weather.substring(index + 1);
        }

        if (weather.contains("转")) {
            int index = weather.indexOf("转");
            weather = weather.substring(index + 1);
        }
        return weather;
    }

    /**
     * 根据天气获取图片
     *
     * @param weather 天气：暴雪、大雪、...
     * @return Drawable
     */
    private Drawable getDrawableByWeather(String weather, boolean isToday) {
        if (TextUtils.isEmpty(weather)) {
            return null;
        }
        weather = filterWeather(weather);

        Map<String, Integer> map = new HashMap<String, Integer>() {{
            put("暴雪", R.drawable.weather_baoxue);
            put("暴雨", R.drawable.weather_baoyu);
            put("冰雨", R.drawable.weather_bingyu);
            put("大暴雨", R.drawable.weather_dabaoyu);
            put("大雪", R.drawable.weather_daxue);
            put("大雨", R.drawable.weather_dayu);
            put("浮尘", R.drawable.weather_fuchen);
            put("雷阵雨", R.drawable.weather_leizhenyu);
            put("雷阵雨伴有冰雹", R.drawable.weather_leizhenyubanyoubingbao);
            put("霾", R.drawable.weather_mai);
            put("沙尘暴", R.drawable.weather_shachenbao);
            put("特大暴雨", R.drawable.weather_tedabaoyu);
            put("雾", R.drawable.weather_wu);
            put("小雪", R.drawable.weather_xiaoxue);
            put("小雨", R.drawable.weather_xiaoyu);
            put("扬沙", R.drawable.weather_yangsha);
            put("阴", R.drawable.weather_yin);
            put("雨夹雪", R.drawable.weather_yujiaxue);
            put("中雪", R.drawable.weather_zhongxue);
            put("雨", R.drawable.weather_xiaoyu);
        }};

        Integer resId = map.get(weather);
        if (resId != null) {
            return UIResLoader.getInstance().getModifyContext().getResources().getDrawable(resId);
        }

        switch (weather) {
            case "阵雨":
                if (DateUtils.isNight()) {
                    resId = R.drawable.weather_zhenyu_night;
                } else {
                    resId = R.drawable.weather_zhenyu;
                }
                break;
            case "阵雪":
                if (DateUtils.isNight()) {
                    resId = R.drawable.weather_zhenxue_night;
                } else {
                    resId = R.drawable.weather_zhenxue;
                }
                break;
            case "多云":
                if (DateUtils.isNight() && isToday) {
                    resId = R.drawable.weather_duoyun_night;
                } else {
                    resId = R.drawable.weather_duoyun;
                }
                break;
            case "晴":
                if (DateUtils.isNight()) {
                    resId = R.drawable.weather_qing_night;
                } else {
                    resId = R.drawable.weather_qing;
                }
                break;
            default:
                resId = R.drawable.weather_na;
                break;
        }
        return UIResLoader.getInstance().getModifyContext().getResources().getDrawable(resId);
    }

    /**
     * 是否展示一天天气
     *
     * @return <p>
     * true: 展示一天天气
     * false: 展示多天天气
     */
    public boolean isShowOneDay(VoiceData.WeatherInfos infos) {
        return infos.uint32FocusIndex != 0 || WinLayout.getInstance().chatToSysText.contains("今天");
    }
}
