package com.txznet.comm.ui.util;

import org.json.JSONException;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import com.txz.ui.voice.VoiceData.WeatherData;
import com.txz.ui.voice.VoiceData.WeatherInfos;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.viewfactory.view.IChatWeatherView.WeatherViewHolder;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.util.LanguageConvertor;

public class WeatherRefresher {

	private static WeatherRefresher mInstance;

	public static WeatherRefresher getInstance() {
		if (mInstance == null) {
			synchronized (WeatherRefresher.class) {
				if (mInstance == null) {
					mInstance = new WeatherRefresher();
				}
			}
		}
		return mInstance;
	}

	private WeatherRefresher() {
		for (int i = 0; i < TEMPERATURE_DRAWABLE_RES.length; i++) {
			TEMPERATURE_DRAWABLE_RES[i] = LayouUtil
					.getDrawable("weather_number_" + i);
		}
	}

	private WeatherInfos mWeatherInfos;

	private final Drawable[] TEMPERATURE_DRAWABLE_RES = new Drawable[10];

	public void updateData(String mContent, WeatherViewHolder v) {
		WeatherInfos infos = new WeatherInfos();
		JSONBuilder jsonBuilder = new JSONBuilder(mContent);
		infos.strCityName = jsonBuilder.getVal("strCityName", String.class);
		infos.uint32FocusIndex = jsonBuilder.getVal("uint32FocusIndex", Integer.class);
		org.json.JSONArray rptMsgWeather = jsonBuilder.getVal("rptMsgWeather", org.json.JSONArray.class);
		int length = rptMsgWeather.length();
		infos.rptMsgWeather = new WeatherData[length];
		for (int i = 0; i < length; i++) {
			WeatherData weatherData = new WeatherData();
			try {
				JSONBuilder jWeather = new JSONBuilder(rptMsgWeather.getJSONObject(i));
				weatherData.uint32Year = jWeather.getVal("uint32Year",Integer.class);
				weatherData.uint32Month = jWeather.getVal("uint32Month",Integer.class);
				weatherData.uint32Day = jWeather.getVal("uint32Day",Integer.class);
				weatherData.uint32DayOfWeek = jWeather.getVal("uint32DayOfWeek",Integer.class);
				weatherData.strWeather = jWeather.getVal("strWeather",String.class);
				weatherData.int32CurTemperature = jWeather.getVal("int32CurTemperature",Integer.class);
				weatherData.int32LowTemperature = jWeather.getVal("int32LowTemperature",Integer.class);
				weatherData.int32HighTemperature = jWeather.getVal("int32HighTemperature",Integer.class);
				weatherData.int32Pm25 = jWeather.getVal("int32Pm25",Integer.class);
				weatherData.strAirQuality = jWeather.getVal("strAirQuality",String.class);
				weatherData.strWind = jWeather.getVal("strWind",String.class);
				weatherData.strCarWashIndex = jWeather.getVal("strCarWashIndex",String.class);
				weatherData.strCarWashIndexDesc = jWeather.getVal("strCarWashIndexDesc",String.class);
				weatherData.strTravelIndex = jWeather.getVal("strTravelIndex",String.class);
				weatherData.strTravelIndexDesc = jWeather.getVal("strTravelIndexDesc",String.class);
				weatherData.strSportIndex = jWeather.getVal("strSportIndex",String.class);
				weatherData.strSportIndexDesc = jWeather.getVal("strSportIndexDesc",String.class);
				weatherData.strSuggest = jWeather.getVal("strSuggest",String.class);
				weatherData.strComfortIndex = jWeather.getVal("strComfortIndex",String.class);
				weatherData.strComfortIndexDesc = jWeather.getVal("strComfortIndexDesc",String.class);
				weatherData.strColdIndex = jWeather.getVal("strColdIndex",String.class);
				weatherData.strColdIndexDesc = jWeather.getVal("strColdIndexDesc",String.class);
				weatherData.strMorningExerciseIndex = jWeather.getVal("strMorningExerciseIndex",String.class);
				weatherData.strMorningExerciseIndexDesc = jWeather.getVal("strMorningExerciseIndexDesc",String.class);
				weatherData.strDressIndex = jWeather.getVal("strDressIndex",String.class);
				weatherData.strDressIndexDesc = jWeather.getVal("strDressIndexDesc",String.class);
				weatherData.strUmbrellaIndex = jWeather.getVal("strUmbrellaIndex",String.class);
				weatherData.strUmbrellaIndexDesc = jWeather.getVal("strUmbrellaIndexDesc",String.class);
				weatherData.strSunBlockIndex = jWeather.getVal("strSunBlockIndex",String.class);
				weatherData.strSunBlockIndexDesc = jWeather.getVal("strSunBlockIndexDesc",String.class);
				weatherData.strDryingIndex = jWeather.getVal("strDryingIndex",String.class);
				weatherData.strDryingIndexDesc = jWeather.getVal("strDryingIndexDesc",String.class);
				weatherData.strDatingIndex = jWeather.getVal("strDatingIndex",String.class);
				weatherData.strDatingIndexDesc = jWeather.getVal("strDatingIndexDesc",String.class);
				infos.rptMsgWeather[i] = weatherData;
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		}

		mWeatherInfos = infos;
		try {
			if (mWeatherInfos.uint32FocusIndex == 0) {
				v.mCurrentTemp.setVisibility(View.VISIBLE);
				v.mTempRange.setVisibility(View.VISIBLE);
				v.mBigTempRange.setVisibility(View.INVISIBLE);
				refreshTemp(
						v,
						mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32CurTemperature);
			} else {
				v.mCurrentTemp.setVisibility(View.INVISIBLE);
				v.mTempRange.setVisibility(View.INVISIBLE);
				v.mBigTempRange.setVisibility(View.VISIBLE);
				refreshBigTempRange(
						v,
						mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32LowTemperature,
						mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32HighTemperature);
			}
			refreshWeather(
					v,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather,mWeatherInfos.uint32FocusIndex == 0);
			refreshTempRange(
					v,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32HighTemperature);
			refreshDate(
					v,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Month,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Day,
					mWeatherInfos.uint32FocusIndex,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32DayOfWeek);
			v.mWind.setText(LanguageConvertor
					.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWind));
			v.mWeather
					.setText(LanguageConvertor
							.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather));
			int pm25 = 0;
			if (mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25 != null) {
				pm25 = mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25
						.intValue();
			}
			v.mAirQualityText.setText(LayouUtil.getString("label_weather_air_quality"));
			v.mAirQuality.setText(pm25==0? "" : pm25 + "");
			v.mAirDegree.setText(TextUtils.isEmpty(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality)?"未知":mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality);
			v.mCity.setText(LanguageConvertor
					.toLocale(mWeatherInfos.strCityName));

			refreshThreeDay(v,
					mWeatherInfos.rptMsgWeather[0].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[0].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[0].strWeather,
					mWeatherInfos.rptMsgWeather[1].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[1].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[1].strWeather,
					mWeatherInfos.rptMsgWeather[2].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[2].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[2].strWeather,
					mWeatherInfos.uint32FocusIndex);
		} catch (Exception e) {
			LogUtil.loge("mWeatherInfos error! " + e.getMessage());
		}
	}

	/**
	 * 刷新当前温度
	 * 
	 * @param tmp
	 */
	private void refreshTemp(WeatherViewHolder v, Integer tmp) {
		if (tmp == null) {
			v.mMinus.setVisibility(View.GONE);
			v.mTempDecade.setVisibility(View.GONE);
			v.mTempUnits.setVisibility(View.GONE);
			v.mTempDegree.setVisibility(View.GONE);
			return;
		}

		if (tmp >= 0) {
			v.mMinus.setVisibility(View.GONE);
		} else {
			v.mMinus.setVisibility(View.VISIBLE);
			tmp = -tmp;
		}

		if (tmp < 10) {
			v.mTempDecade.setVisibility(View.GONE);
		} else {
			v.mTempDecade.setVisibility(View.VISIBLE);
		}

		v.mTempUnits.setVisibility(View.VISIBLE);
		v.mTempDegree.setVisibility(View.VISIBLE);

		int decade = tmp / 10;
		int units = tmp % 10;

		v.mTempDegree.setImageDrawable(LayouUtil.getDrawable("weather_number_degree"));
		v.mTempDecade.setImageDrawable(TEMPERATURE_DRAWABLE_RES[decade]);
		v.mTempUnits.setImageDrawable(TEMPERATURE_DRAWABLE_RES[units]);
	}

	/**
	 * 刷新当前温度范围
	 * 
	 * @param low
	 * @param high
	 */
	private void refreshTempRange(WeatherViewHolder v, Integer low, Integer high) {
		if (low == null || high == null) {
			return;
		}
		String text = low + "/" + high + "°";
		v.mTempRange.setText(text);
	}

	/**
	 * 刷新当前日期
	 * 
	 * @param month
	 * @param day
	 * @param index
	 */
	private void refreshDate(WeatherViewHolder v, Integer month, Integer day,
			Integer index, Integer dayOfWeek) {
		if (month == null || day == null || index == null) {
			return;
		}
		String date = month + "月" + day + "日";
		v.mDate.setText(valueDayOfWeek(dayOfWeek) + " " + date);
		String d = null;
		switch (index) {
		case 0:
			d = "今天";
			break;
		case 1:
			d = "明天";
			break;
		case 2:
			d = "后天";
			break;
		case 3:
			d = "大后天";
			break;
		case 4:
			d = "大大后天";
			break;
		default:
			break;
		}
		if (d != null)
			v.mDay.setText(LanguageConvertor.toLocale(d));
	}

	/**
	 * 刷新天气
	 * 
	 * @param weather
	 */
	private void refreshWeather(WeatherViewHolder v, String weather,boolean isToday) {
		if (weather == null || weather.equals(""))
			return;

		Drawable drawable = getDrawableByWeather(weather,isToday);
		if (drawable == null) {
			v.mTodayWeather.setImageDrawable(null);
		} else {
			v.mTodayWeather.setImageDrawable(drawable);
		}
	}

	/**
	 * 根据天气获取图片ID
	 * 
	 * @param weather
	 * @return
	 */
	private Drawable getDrawableByWeather(String weather,boolean isToday) {

		if (weather == null || weather.equals(""))
			return null;
		String drawableName = "";
		// 过滤小到XXXX 中到XXX 大到XXXXX
//		if (weather.contains("到") || weather.contains("转")) {
//			int index = weather.indexOf("到");
//			weather = weather.substring(index + 1, weather.length());
//		}
		if (weather.contains("到")) {
			int index = weather.indexOf("到");
			weather = weather.substring(index + 1, weather.length());
		}

		if (weather.contains("转")) {
			int index = weather.indexOf("转");
			weather = weather.substring(index + 1, weather.length());
		}

		if ("暴雪".equals(weather)) {
			drawableName = "weather_baoxue";
		} else if ("暴雨".equals(weather)) {
			drawableName = "weather_baoyu";
		} else if ("冰雨".equals(weather)) {
			drawableName = "weather_bingyu";
		} else if ("大暴雨".equals(weather)) {
			drawableName = "weather_dabaoyu";
		} else if ("大雪".equals(weather)) {
			drawableName = "weather_daxue";
		} else if ("大雨".equals(weather)) {
			drawableName = "weather_dayu";
		} else if ("多云".equals(weather)) {
			if (DateUtils.isNight() && isToday) {
				drawableName = "weather_duoyun_night";
			} else {
				drawableName = "weather_duoyun";
			}
		} else if ("浮尘".equals(weather)) {
			drawableName = "weather_fuchen";
		} else if ("雷阵雨".equals(weather)) {
			drawableName = "weather_leizhenyu";
		} else if ("雷阵雨伴有冰雹".equals(weather)) {
			drawableName = "weather_leizhenyubanyoubingbao";
		} else if ("霾".equals(weather)) {
			drawableName = "weather_mai";
		} else if ("晴".equals(weather)) {
			if (DateUtils.isNight()) {
				drawableName = "weather_qing_night";
			} else {
				drawableName = "weather_qing";
			}
		} else if ("沙尘暴".equals(weather)) {
			drawableName = "weather_shachenbao";
		} else if ("特大暴雨".equals(weather)) {
			drawableName = "weather_tedabaoyu";
		} else if ("雾".equals(weather)) {
			drawableName = "weather_wu";
		} else if ("小雪".equals(weather)) {
			drawableName = "weather_xiaoxue";
		} else if ("小雨".equals(weather) || "雨".equals(weather)) {
			drawableName = "weather_xiaoyu";
		} else if ("扬沙".equals(weather)) {
			drawableName = "weather_yangsha";
		} else if ("阴".equals(weather)) {
			drawableName = "weather_yin";
		} else if ("雨夹雪".equals(weather)) {
			drawableName = "weather_yujiaxue";
		} else if ("阵雨".equals(weather)) {
			if (DateUtils.isNight()) {
				drawableName = "weather_zhenyu_night";
			} else {
				drawableName = "weather_zhenyu";
			}
		} else if ("阵雪".equals(weather)) {
			if (DateUtils.isNight()) {
				drawableName = "weather_zhenxue_night";
			} else {
				drawableName = "weather_zhenxue";
			}
		} else if ("中雪".equals(weather)) {
			drawableName = "weather_zhongxue";
		} else if ("中雨".equals(weather)) {
			drawableName = "weather_zhongyu";
		} else {
			drawableName = "weather_na";
		}
		return LayouUtil.getDrawable(drawableName);
	}

	/**
	 * 刷新3天的天气界面
	 * 
	 * @param todayLow
	 * @param todayHigh
	 * @param todayWeather
	 * @param tomorrowLow
	 * @param tomorrowHigh
	 * @param tomorrowWeather
	 * @param thedayafterLow
	 * @param thedayafterHigh
	 * @param thedayafterWeather
	 * @param currentIndex
	 */
	private void refreshThreeDay(WeatherViewHolder v, Integer todayLow,
			Integer todayHigh, String todayWeather, Integer tomorrowLow,
			Integer tomorrowHigh, String tomorrowWeather,
			Integer thedayafterLow, Integer thedayafterHigh,
			String thedayafterWeather, Integer currentIndex) {
		// 金天
		Drawable drawable = getDrawableByWeather(todayWeather,true);
		if (drawable == null) {
			v.mToday.setDrawable(LayouUtil.getDrawable("weather_na"));
		} else {
			v.mToday.setDrawable(drawable);
		}

		if (todayLow == null || todayHigh == null) {
			v.mToday.setTitle("");
			return;
		}

		String text = todayLow + "/" + todayHigh + "°";
		v.mToday.setTitle(LanguageConvertor.toLocale(text));
		v.mToday.setHead(LayouUtil.getString("label_weather_today"));

		// 明天
		drawable = getDrawableByWeather(tomorrowWeather,false);
		if (drawable == null) {
			v.mTomorrow.setDrawable(LayouUtil.getDrawable("weather_na"));
		} else {
			v.mTomorrow.setDrawable(drawable);
		}

		if (tomorrowLow == null || tomorrowHigh == null) {
			return;
		}

		text = tomorrowLow + "/" + tomorrowHigh + "°";
		v.mTomorrow.setTitle(LanguageConvertor.toLocale(text));
		v.mTomorrow.setHead(LayouUtil.getString("label_weather_tommorow"));

		// 后天
		drawable = getDrawableByWeather(thedayafterWeather,false);
		if (drawable == null) {
			v.mTheDayAfterTomorrow.setDrawable(LayouUtil
					.getDrawable("weather_na"));
		} else {
			v.mTheDayAfterTomorrow.setDrawable(drawable);
		}

		if (thedayafterLow == null || thedayafterHigh == null) {
			return;
		}

		text = thedayafterLow + "/" + thedayafterHigh + "°";
		v.mTheDayAfterTomorrow.setTitle(LanguageConvertor.toLocale(text));
		v.mTheDayAfterTomorrow.setHead(LayouUtil.getString("label_weather_day_after_tommorow"));

	}

	private void refreshBigTempRange(WeatherViewHolder v, Integer low,
			Integer high) {
		if (low == null || high == null) {
			return;
		}

		if (low >= 0) {
			v.mBigLowMinus.setVisibility(View.GONE);
		} else {
			v.mBigLowMinus.setImageDrawable(LayouUtil.getDrawable("weather_number_minus"));
			v.mBigLowMinus.setVisibility(View.VISIBLE);
			low = -low;
		}

		if (low < 10) {
			v.mBigLowTempDecade.setVisibility(View.GONE);
		} else {
			v.mBigLowTempDecade.setVisibility(View.VISIBLE);
		}

		int decade = low / 10;
		int units = low % 10;

		v.mBigLowTempDecade.setImageDrawable(TEMPERATURE_DRAWABLE_RES[decade]);
		v.mBigLowTempUnits.setImageDrawable(TEMPERATURE_DRAWABLE_RES[units]);

		if (high >= 0) {
			v.mBigHighMinus.setVisibility(View.GONE);
		} else {
			v.mBigHighMinus.setImageDrawable(LayouUtil.getDrawable("weather_number_minus"));
			v.mBigHighMinus.setVisibility(View.VISIBLE);
			high = -high;
		}

		if (high < 10) {
			v.mBigHighTempDecade.setVisibility(View.GONE);
		} else {
			v.mBigHighTempDecade.setVisibility(View.VISIBLE);
		}

		decade = high / 10;
		units = high % 10;
		
		v.mBigSlash.setImageDrawable(LayouUtil.getDrawable("weather_slash"));
		v.mBigTempDegree.setImageDrawable(LayouUtil.getDrawable("weather_number_degree"));
		v.mBigHighTempDecade.setImageDrawable(TEMPERATURE_DRAWABLE_RES[decade]);
		v.mBigHighTempUnits.setImageDrawable(TEMPERATURE_DRAWABLE_RES[units]);
	}

	public void release() {
		if (mWeatherInfos == null) {
			return;
		}
		synchronized (mInstance) {
			synchronized (mWeatherInfos) {
				mWeatherInfos = null;
			}
			mInstance = null;
		}
	}

	private String valueDayOfWeek(Integer dayOfWeek) {
		String str = "";
		switch (dayOfWeek) {
		case 1:
			str = "周日";
			break;
		case 2:
			str = "周一";
			break;
		case 3:
			str = "周二";
			break;
		case 4:
			str = "周三";
			break;
		case 5:
			str = "周四";
			break;
		case 6:
			str = "周五";
			break;
		case 7:
			str = "周六";
			break;

		default:
			break;
		}
		return str;
	}
}
