package com.txznet.comm.ui.theme.test.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.txz.ui.voice.VoiceData.WeatherData;
import com.txz.ui.voice.VoiceData.WeatherInfos;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.view.IChatWeatherView.WeatherViewHolder;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.util.LanguageConvertor;

import org.json.JSONException;

public class WeatherRefresher2 {

	private static WeatherRefresher2 mInstance;

	public static WeatherRefresher2 getInstance() {
		if (mInstance == null) {
			synchronized (WeatherRefresher2.class) {
				if (mInstance == null) {
					mInstance = new WeatherRefresher2();
				}
			}
		}
		return mInstance;
	}

	private WeatherRefresher2() {
		for (int i = 0; i < TEMPERATURE_DRAWABLE_RES.length; i++) {
			TEMPERATURE_DRAWABLE_RES[i] = LayouUtil
					.getDrawable("weather_number_" + i);
		}
	}

	private WeatherInfos mWeatherInfos;

	private final Drawable[] TEMPERATURE_DRAWABLE_RES = new Drawable[10];

	//展示一天还是三天天气
	public boolean isShowOneDay(String mContent){
		psrseData(mContent);
		LogUtil.logd(WinLayout.logTag+ "isShowOneDay: "+mWeatherInfos.uint32FocusIndex+"--"+WinLayout.getInstance().chatToSysText);
		if (mWeatherInfos.uint32FocusIndex == 0 && !WinLayout.getInstance().chatToSysText.contains("今天")){
			return false;
		}
		return true;
	}

	//解析天气数据放入WeatherInfos
	public WeatherInfos psrseData(String mContent){
		WeatherInfos infos = new WeatherInfos();
		JSONBuilder jsonBuilder = new JSONBuilder(mContent);
		LogUtil.logd(WinLayout.logTag+ "weatherData: "+ mContent);
		WinLayout.getInstance().vTips = jsonBuilder.getVal("vTips", String.class);
		LogUtil.logd(WinLayout.logTag+ "weatherData: --vTips"+ WinLayout.getInstance().vTips);
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
				return null;
			}
		}
		mWeatherInfos = infos;
		return infos;
	}

	public void updateData(String mContent, WeatherViewHolder v) {
		LogUtil.logd(WinLayout.logTag+ "updateData: ");
		//mWeatherInfos = psrseData(mContent);
		refreshTemp(
				v,
				mWeatherInfos.rptMsgWeather[0].int32CurTemperature);
		refreshBackground(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather);
		String date = refreshDate(
				v,
				mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Month,
				mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Day,
				mWeatherInfos.uint32FocusIndex,
				mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32DayOfWeek);
		String dateLast = refreshDate(
				v,
				mWeatherInfos.rptMsgWeather[4].uint32Month,
				mWeatherInfos.rptMsgWeather[4].uint32Day,
				4,
				mWeatherInfos.rptMsgWeather[4].uint32DayOfWeek);
		String wind = LanguageConvertor
				.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWind);
		try {
			wind = wind.substring(0,wind.indexOf('('));
		}catch (Exception e){
			LogUtil.loge(WinLayout.logTag+ "wind parse error: " + e.getMessage());
		}
		v.mWind.setText(LanguageConvertor.toLocale(wind));
		int pm25 = 0;
		if (mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25 != null) {
			pm25 = mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25
					.intValue();
		}
		v.mAirQuality.setText(LanguageConvertor.toLocale("  "+(pm25==0? "" : pm25 + " ")+
				(TextUtils.isEmpty(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality)?
						"未知":mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality)+"  "));

		v.mAirQuality.setBackground(
				getBgDrawable(
						TextUtils.isEmpty(
								mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality)?
								"未知":mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality));
		v.mCity.setText(LanguageConvertor
				.toLocale(mWeatherInfos.strCityName));
		try {
			refreshThreeDay(v,
					date,
					mWeatherInfos.rptMsgWeather[0].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[0].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[0].strWeather,
					mWeatherInfos.rptMsgWeather[1].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[1].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[1].strWeather,
					mWeatherInfos.rptMsgWeather[2].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[2].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[2].strWeather,
					mWeatherInfos.rptMsgWeather[3].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[3].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[3].strWeather,
					mWeatherInfos.rptMsgWeather[4].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[4].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[4].strWeather,
					dateLast,
					mWeatherInfos.uint32FocusIndex);
		} catch (Exception e) {
			LogUtil.loge(WinLayout.logTag+"mWeatherInfos error! " + e.getMessage());
		}
	}

	public void updateDataOneday(String mContent, WeatherViewHolder v) {
		//mWeatherInfos = psrseData(mContent);
		try {
			if (mWeatherInfos.uint32FocusIndex == 0) {
				v.mCurrentTemp.setVisibility(View.VISIBLE);
				//v.mTempRange.setVisibility(View.VISIBLE);
				v.mBigTempRange.setVisibility(View.GONE);
				refreshTemp(
						v,
						mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32CurTemperature);
			} else {
				v.mCurrentTemp.setVisibility(View.GONE);
				//v.mTempRange.setVisibility(View.INVISIBLE);
				v.mBigTempRange.setVisibility(View.VISIBLE);
				refreshBigTempRange(
						v,
						mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32LowTemperature,
						mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32HighTemperature);
			}
			refreshWeather(
					v,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather,mWeatherInfos.uint32FocusIndex == 0);
			refreshBackground(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather);
			/*refreshTempRange(
					v,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32HighTemperature);*/
			String dataText = refreshDate(
					v,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Month,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Day,
					mWeatherInfos.uint32FocusIndex,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32DayOfWeek);
			v.mDate .setText(LanguageConvertor.toLocale(dataText));
			v.mWind.setText(LanguageConvertor
					.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWind.
							replace("("," ").replace(")","")));
			String weatherText = mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather;
			v.mWeather.setText(LanguageConvertor.toLocale(weatherText));
			v.mTempRange.setText(LanguageConvertor.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32LowTemperature + "～" + mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32HighTemperature + "°C"));
			int pm25 = 0;
			if (mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25 != null) {
				pm25 = mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25
						.intValue();
			}
			//v.mAirQualityText.setText(LayouUtil.getString("label_weather_air_quality"));
			String airQualityText = (TextUtils.isEmpty(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality)?"未知":mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality);
			v.mAirQuality.setText("  "+(pm25==0? "" : pm25 + " ")+airQualityText+"  ");
			LogUtil.logd(WinLayout.logTag+ "updateDataOneday: airQualityText:"+airQualityText);
			v.mAirQuality.setBackground(getBgDrawable(airQualityText));

			v.mCity.setText(LanguageConvertor
					.toLocale(mWeatherInfos.strCityName));

			/*refreshThreeDay(v,
					mWeatherInfos.rptMsgWeather[0].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[0].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[0].strWeather,
					mWeatherInfos.rptMsgWeather[1].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[1].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[1].strWeather,
					mWeatherInfos.rptMsgWeather[2].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[2].int32HighTemperature,
					mWeatherInfos.rptMsgWeather[2].strWeather,
					mWeatherInfos.uint32FocusIndex);*/
		} catch (Exception e) {
			LogUtil.loge("mWeatherInfos error! " + e.getMessage());
		}
	}

	private void refreshBackground(String strWeather){
		if (strWeather == null || strWeather.equals(""))
			return;
		LogUtil.logd(WinLayout.logTag+ "refrshBackground:strWeather: "+strWeather);
		strWeather = filterWeather(strWeather);
		GradientDrawable gradientDrawable = null;
		switch (strWeather){
			case "晴":
				if (DateUtils.isNight()){
					int colors24[] = {0xFF2A3E58,0xFF6B8BAA};
					gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors24);
				}else {
					int colors[] = {0xFF0067BF,0xFF8AD6F7};
					gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors);
				}
				break;
			case "多云":
				if (DateUtils.isNight()){
					int colors25[] = {0xFF253851,0xFF5D7D9B};
					gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors25);
				}else {
					int colors1[] = {0xFF6AA4CA,0xFFC2E6FE};
					gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors1);
				}
				break;
			case "阴":
				int colors2[] = {0xFF5388AB,0xFFA2CFED};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors2);
				break;
			case "小雨":
				int colors3[] = {0xFF3DACA0,0xFF93D2BF};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors3);
				break;
			case "中雨":
				int colors4[] = {0xFF3D9A9C,0xFF59D7B0};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors4);
				break;
			case "大雨":
				int colors5[] = {0xFF107173,0xFF4CCCA4};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors5);
				break;
			case "暴雨":
				int colors6[] = {0xFF045B5D,0xFFC2E6FE};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors6);
				break;
			case "大暴雨":
				int colors7[] = {0xFF064B49,0xFF299092};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors7);
				break;
			case "特大暴雨":
				int colors8[] = {0xFF002A30,0xFF19706E};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors8);
				break;
			case "冰雨":
				int colors9[] = {0xFF30B1A3,0xFF83D3BA};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors9);
				break;
			case "阵雨":
				if (DateUtils.isNight()){
					int colors26[] = {0xFF003E67,0xFF005679};
					gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors26);
				}else {
					int colors10[] = {0xFF3DACA0,0xFF93D2BF};
					gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors10);
				}
				break;
			case "雷阵雨":
				int colors11[] = {0xFF3044F7,0xFFB5AFFC};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors11);
				break;
			case "雷阵雨伴有冰雹":
				int colors12[] = {0xFF3044F7,0xFFB5AFFC};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors12);
				break;
			case "雨夹雪":
				int colors13[] = {0xFF7B96CA,0xFFACCEEF};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors13);
				break;
			case "小雪":
				int colors14[] = {0xFF7B96CA,0xFFACCEFF};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors14);
				break;
			case "中雪":
				int colors15[] = {0xFF6883B7,0xFF8EB5D8};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors15);
				break;
			case "大雪":
				int colors16[] = {0xFF536D9E,0xFF789CBE};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors16);
				break;
			case "暴雪":
				int colors17[] = {0xFF435B88,0xFF5980A6};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors17);
				break;
			case "阵雪":
				if (DateUtils.isNight()){
					int colors27[] = {0xFF444B57,0xFF717B89};
					gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors27);
				}else {
					int colors18[] = {0xFF7B96CA,0xFFACCEEF};
					gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors18);
				}
				break;
			case "沙尘暴":
				int colors19[] = {0xFFC19448,0xFFDAB980};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors19);
				break;
			case "扬沙":
				int colors20[] = {0xFFC19448,0xFFDAB980};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors20);
				break;
			case "浮尘":
				int colors21[] = {0xFFC19448,0xFFDAB980};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors21);
				break;
			case "雾":
				int colors22[] = {0xFF5F6F84,0xFFACC4DA};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors22);
				break;
			case "霾":
				int colors23[] = {0xFF5F6F84,0xFF93ABC0};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors23);
				break;
			case "夜间晴":
				break;
			case "夜间多云":
				break;
			case "夜间阵雨":
				break;
			case "夜间阵雪":
				break;
			default:
				int colors28[] = {0xFF525E8B,0xFFCCB4B9};
				gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,colors28);
				break;
		}

		ChatWeatherView.getInstance().refreshBackground(gradientDrawable);
	}

	/**
	 * 根据空气质量获取背景图片
	 *
	 * @param
	 */
	private Drawable getBgDrawable(String degree){
		LogUtil.logd(WinLayout.logTag+ "getAirBgDrawable: "+degree);
		Drawable drawable = LayouUtil.getDrawable("");
		switch (degree){
			case "优":
				drawable = LayouUtil.getDrawable("air_bg_excellent");
				break;
			case "良":
				drawable = LayouUtil.getDrawable("air_bg_good");
				break;
			default:
				drawable = LayouUtil.getDrawable("air_bg_bad");
				break;
		}

		return drawable;
	}

	/**
	 * 刷新当前温度
	 * 
	 * @param tmp
	 */
	private void refreshTemp(WeatherViewHolder v, Integer tmp) {
		if (v == null || tmp == null){return;}
        LogUtil.logd(WinLayout.logTag+ "refreshTemp: "+tmp.toString());
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
		v.mTempDegree.setMaxWidth(28);
		v.mTempDegree.setMaxHeight(32);
		v.mTempDegree.setAdjustViewBounds(true);

		v.mTempDecade.setImageDrawable(TEMPERATURE_DRAWABLE_RES[decade]);
		v.mTempDecade.setAdjustViewBounds(true);

		v.mTempUnits.setImageDrawable(TEMPERATURE_DRAWABLE_RES[units]);
		v.mTempUnits.setAdjustViewBounds(true);
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
		/*v.mTempRange.setText(text);*/
	}

	/**
	 * 刷新当前日期
	 * 
	 * @param month
	 * @param day
	 * @param index
	 */
	private String refreshDate(WeatherViewHolder v, Integer month, Integer day,
			Integer index, Integer dayOfWeek) {
		if (month == null || day == null || index == null) {
			return "";
		}
		String date = month + "月" + day + "日"+ " "+valueDayOfWeek(dayOfWeek);
		return date;
	}

	/**
	 * 刷新天气
	 * 
	 * @param weather
	 */
	private void refreshWeather(WeatherViewHolder v, String weather,boolean isToday) {
		if (weather == null || weather.equals(""))
			return;

		Drawable drawable = getDrawableByWeather(weather,true);
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
		weather = filterWeather(weather);

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
		} else if ("小雨".equals(weather)) {
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
		}else if ("雨".equals(weather)) {
			drawableName = "weather_xiaoyu";
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
	private void refreshThreeDay(WeatherViewHolder v, String date,
			Integer todayLow,Integer todayHigh, String todayWeather,
			Integer tomorrowLow,Integer tomorrowHigh, String tomorrowWeather,
			Integer thedayafterLow, Integer thedayafterHigh,String thedayafterWeather,
			Integer thedaymoreLow, Integer thedaymoreHigh,String thedaymoreWeather,
			Integer thedaylastLow, Integer thedaylastHigh,String thedaylastWeather,String dateLast,
			Integer currentIndex) {
		LogUtil.logd(WinLayout.logTag+ "parseDataThreeDay ");
		// 今天
		Drawable today_drawable = getDrawableByWeather(todayWeather,true);
		if (today_drawable == null) {
			today_drawable = LayouUtil.getDrawable("weather_na");
		}
		todayWeather = filterWeather(todayWeather);
		if (todayLow == null || todayHigh == null) {
			return;
		}
		String today_tem = todayLow + "～" + todayHigh + "°C";

		// 明天
		String tomorrow_tittle = LayouUtil.getString("label_weather_tommorow");
		Drawable tomorrow_drawable = getDrawableByWeather(tomorrowWeather,false);
		if (tomorrow_drawable == null) {
			tomorrow_drawable = LayouUtil.getDrawable("weather_na");
		}
		if (tomorrowLow == null || tomorrowHigh == null) {
			return;
		}
		tomorrowWeather = filterWeather(tomorrowWeather);
		String tomorrow_tem = tomorrowLow + "～" + tomorrowHigh + "°C";

		// 后天
		String dayft_tittle =LayouUtil.getString("label_weather_day_after_tommorow");
		Drawable dayft_drawable = getDrawableByWeather(thedayafterWeather,false);
		if (dayft_drawable == null) {
			dayft_drawable = LayouUtil.getDrawable("weather_na");
		}
		thedayafterWeather = filterWeather(thedayafterWeather);
		if (thedayafterLow == null || thedayafterHigh == null) {
			return;
		}
		String dayft_tem = thedayafterLow + "～" + thedayafterHigh + "°C";

		// 大后天
		String daymore_tittle =LayouUtil.getString("label_weather_day_more");
		Drawable daymore_drawable = getDrawableByWeather(thedaymoreWeather,false);
		if (daymore_drawable == null) {
			daymore_drawable = LayouUtil.getDrawable("weather_na");
		}
		thedaymoreWeather = filterWeather(thedaymoreWeather);
		if (thedaymoreLow == null || thedaymoreHigh == null) {
			return;
		}
		String daymore_tem = thedaymoreLow + "～" + thedaymoreHigh + "°C";

		// 第五天
		String daylast_tittle = dateLast;
		Drawable daylast_drawable = getDrawableByWeather(thedaylastWeather,false);
		if (daylast_drawable == null) {
			daylast_drawable = LayouUtil.getDrawable("weather_na");
		}
		thedaylastWeather = filterWeather(thedaylastWeather);
		if (thedaylastLow == null || thedaylastHigh == null) {
			return;
		}
		String daylast_tem = thedaylastLow + "～" + thedaylastHigh + "°C";

//		if (ChatWeatherView.isShowMoreWeatherHalf || ChatWeatherView.isShowMoreWeatherFull){
//			ChatWeatherView.getInstance().refreshMoreDay(date,today_drawable,todayWeather,today_tem,
//					tomorrow_tittle,tomorrow_drawable,tomorrowWeather,tomorrow_tem,
//					dayft_tittle,dayft_drawable,thedayafterWeather,dayft_tem,
//					daymore_tittle,daymore_drawable,thedaymoreWeather,
//					daymore_tem);
//		}else {
//			ChatWeatherView.getInstance().refreshThreeDay(date,today_drawable,todayWeather,today_tem,
//					tomorrow_tittle,tomorrow_drawable,tomorrowWeather,tomorrow_tem,
//					dayft_tittle,dayft_drawable,thedayafterWeather,dayft_tem);
//		}

		ChatWeatherView.getInstance().refreshMoreDay(date,today_drawable,todayWeather,today_tem,
				tomorrow_tittle,tomorrow_drawable,tomorrowWeather,tomorrow_tem,
				dayft_tittle,dayft_drawable,thedayafterWeather,dayft_tem,
				daymore_tittle,daymore_drawable,thedaymoreWeather,daymore_tem,
				daylast_tittle,daylast_drawable,thedaylastWeather,daylast_tem
				);
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

	/*public void release() {
		if (mWeatherInfos == null) {
			return;
		}
		synchronized (mInstance) {
			synchronized (mWeatherInfos) {
				mWeatherInfos = null;
			}
			mInstance = null;
		}
	}*/

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

	//过滤小到XXXX 中到XXX 大到XXXXX
	private String filterWeather(String weather){
		if (weather.contains("到")) {
			int index = weather.indexOf("到");
			weather = weather.substring(index + 1, weather.length());
		}

		if (weather.contains("转")) {
			int index = weather.indexOf("转");
			weather = weather.substring(index + 1, weather.length());
		}
		return weather;
	}
}
