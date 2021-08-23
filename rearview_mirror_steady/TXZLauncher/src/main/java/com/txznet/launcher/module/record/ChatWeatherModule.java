package com.txznet.launcher.module.record;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.R;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.module.IModule;
import com.txznet.launcher.module.notification.INoticeModule;
import com.txznet.launcher.module.record.bean.ChatWeatherMsgData;
import com.txznet.launcher.utils.WeatherUtils;
import com.txznet.launcher.widget.CornerFrameLayout;
import com.txznet.sdk.TXZTtsManager;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 展示天气的界面
 */
public class ChatWeatherModule extends BaseModule implements INoticeModule {

    private ChatWeatherMsgData chatWeatherMsgData;
    @Bind(R.id.vg_weather_content)
    CornerFrameLayout vgWeatherContent;
    @Bind(R.id.tv_weather_title)
    @Nullable
    TextView tvWeatherTitle;
    @Bind(R.id.ll_weather)
    ViewGroup llWeather;
    @Bind(R.id.tv_weather_msg)
    TextView tvWeatherMsg;
    @Bind(R.id.tv_weather_temp)
    TextView tvWeatherTemp;
    @Bind(R.id.tv_weather_cur_temp)
    @Nullable
    TextView tvWeatherCurTemp;
    @Bind(R.id.tv_weather_pm25)
    TextView tvWeatherPm25;
    @Bind(R.id.tv_weather_city)
    @Nullable
    TextView tvWeatherCity;

    private int mViewStatus = IModule.STATUS_FULL; // 视图布局风格

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        chatWeatherMsgData = parseData(data);
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        chatWeatherMsgData = parseData(data);
        refreshWeather();
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View contentView = null;
        mViewStatus = status;
        switch (status) {
            case IModule.STATUS_THIRD:
                contentView = View.inflate(context, R.layout.module_record_chat_weather_third, null);
                break;
            case IModule.STATUS_HALF:
                contentView = View.inflate(context, R.layout.module_record_chat_weather_third, null);
                break;
            default:
                contentView = View.inflate(context, R.layout.module_record_chat_weather, null);
                break;
        }
        ButterKnife.bind(this, contentView);
        vgWeatherContent.setCorner(10);
        refreshWeather();
        return contentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private void refreshWeather() {
        if (chatWeatherMsgData == null) {
            return;
        }
        ChatWeatherMsgData.WeatherData weatherData = chatWeatherMsgData.mDatas.get(chatWeatherMsgData.uint32FocusIndex);
        if (tvWeatherTitle != null) {
            tvWeatherTitle.setText(chatWeatherMsgData.strCityName + "／" + parseDate(weatherData.uint32Year, weatherData.uint32Month, weatherData.uint32Day) + "天气");
        }

        tvWeatherMsg.setText((DateUtils.isNight() ? ("夜间" + weatherData.strWeather) : weatherData.strWeather));
        if (TextUtils.isEmpty(weatherData.strAirQuality)) {
            tvWeatherPm25.setVisibility(View.GONE);
            tvWeatherPm25.setText(null);
        } else {
            tvWeatherPm25.setVisibility(View.VISIBLE);
            tvWeatherPm25.setText("空气" + weatherData.strAirQuality);
        }
        if (mViewStatus == IModule.STATUS_FULL) {
            llWeather.setBackgroundResource(WeatherUtils.getInstance().getWeatherBg(weatherData.strWeather, DateUtils.isNight()).bigBg);

            if (chatWeatherMsgData.uint32FocusIndex == 0) {
                tvWeatherCurTemp.setText(weatherData.int32CurTemperature + "°");
                tvWeatherTemp.setVisibility(View.VISIBLE);
                tvWeatherTemp.setText(weatherData.int32LowTemperature + "° ~ " + weatherData.int32HighTemperature + "°");
            } else {
                tvWeatherTemp.setVisibility(View.GONE);
                tvWeatherCurTemp.setText(weatherData.int32LowTemperature + "~" + weatherData.int32HighTemperature + "°");
            }

        } else {
            if (mViewStatus == IModule.STATUS_HALF) {
                llWeather.setBackgroundResource(WeatherUtils.getInstance().getWeatherBg(weatherData.strWeather, DateUtils.isNight()).normalBg);
            } else {
                llWeather.setBackgroundResource(WeatherUtils.getInstance().getWeatherBg(weatherData.strWeather, DateUtils.isNight()).littleBg);
            }
            tvWeatherTemp.setText(weatherData.int32CurTemperature + "°");
        }

        if (tvWeatherCity != null) {
            tvWeatherCity.setText(chatWeatherMsgData.strCityName);
        }
    }

    private ChatWeatherMsgData parseData(String data) {
        ChatWeatherMsgData chatWeatherMsgData = new ChatWeatherMsgData();
        chatWeatherMsgData.parseData(data);
        return chatWeatherMsgData;
    }

    /**
     * 格式化日期
     *
     * @param year
     * @param month
     * @param day
     * @return
     */
    private String parseDate(int year, int month, int day) {
        String strDate;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long currentDate = calendar.getTimeInMillis();
        calendar.set(year, month - 1, day, 0, 0, 0);
        long selectDate = calendar.getTimeInMillis();
        long tmp = selectDate - currentDate;
        if (tmp == THE_DAY_BEFORE_YESTERDAY) {
            strDate = "前天";
        } else if (tmp == YESTERDAY) {
            strDate = "昨天";
        } else if (tmp == TODAY) {
            strDate = "今天";
        } else if (tmp == TOMORROW) {
            strDate = "明天";
        } else if (tmp == THE_DAY_AFTER_TOMORROW) {
            strDate = "后天";
        } else if (tmp == THREE_DAYS_FROM_NOW) {
            strDate = "大后天";
        } else {
            strDate = year + "年" + month + "月" + day + "日";
        }
        return strDate;
    }

    private static final long THE_DAY_BEFORE_YESTERDAY = -2 * 24 * 60 * 60 * 1000;
    private static final long YESTERDAY = -24 * 60 * 60 * 1000;
    private static final long TODAY = 0;
    private static final long TOMORROW = 24 * 60 * 60 * 1000;
    private static final long THE_DAY_AFTER_TOMORROW = 2 * 24 * 60 * 60 * 1000;
    private static final long THREE_DAYS_FROM_NOW = 3 * 24 * 60 * 60 * 1000;


    private int mTtsId;

    @Override
    public void playNotice(int order, final Runnable ttsCallback) {
        if (chatWeatherMsgData == null) { // 数据尚未加载出来时
            return;
        }
        ChatWeatherMsgData.WeatherData weatherData = chatWeatherMsgData.mDatas.get(chatWeatherMsgData.uint32FocusIndex);
        String tts;
        if (mViewStatus == IModule.STATUS_FULL) {
            tts = String.format("今天天气%s, 温度%s至%s度，空气质量%s", weatherData.strWeather, weatherData.int32LowTemperature, weatherData.int32HighTemperature, weatherData.strAirQuality);
        } else {
            tts = String.format("今天天气%s, 温度%s度，空气质量%s", weatherData.strWeather, weatherData.int32CurTemperature, weatherData.strAirQuality);
        }
        mTtsId = TXZTtsManager.getInstance().speakText(tts, new TXZTtsManager.ITtsCallback() {
            @Override
            public void onEnd() {
                ttsCallback.run();
            }
        });
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        TtsUtil.cancelSpeak(mTtsId);
    }
}
