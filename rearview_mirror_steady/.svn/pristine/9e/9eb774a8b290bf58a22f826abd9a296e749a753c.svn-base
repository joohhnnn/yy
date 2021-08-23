package com.txznet.record.ui;

import com.txz.ui.voice.VoiceData.WeatherInfos;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.DateUtils;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.adapter.ChatContentAdapter.WeatherViewHolder;
import com.txznet.record.adapter.ChatContentAdapter.WeatherViewHolderLarge;
import com.txznet.record.lib.R;
import com.txznet.txz.util.LanguageConvertor;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

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

	private WeatherInfos mWeatherInfos;

	private final int[] TEMPERATURE_DRAWABLE_RES = { R.drawable.weather_number_0, R.drawable.weather_number_1,
			R.drawable.weather_number_2, R.drawable.weather_number_3, R.drawable.weather_number_4,
			R.drawable.weather_number_5, R.drawable.weather_number_6, R.drawable.weather_number_7,
			R.drawable.weather_number_8, R.drawable.weather_number_9 };

	public void updateData(WeatherInfos infos, WeatherViewHolder v) {
		if (infos == null)
			return;

		mWeatherInfos = infos;
		try {
			if (mWeatherInfos.uint32FocusIndex == 0) {
				v.mCurrentTemp.setVisibility(View.VISIBLE);
				v.mTempRange.setVisibility(View.VISIBLE);
				v.mBigTempRange.setVisibility(View.INVISIBLE);
				refreshTemp(v, mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32CurTemperature);
			} else {
				v.mCurrentTemp.setVisibility(View.INVISIBLE);
				v.mTempRange.setVisibility(View.INVISIBLE);
				v.mBigTempRange.setVisibility(View.VISIBLE);
				refreshBigTempRange(v, mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32LowTemperature,
						mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32HighTemperature);
			}
			refreshWeather(v, mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather,
					mWeatherInfos.uint32FocusIndex == 0);
			refreshTempRange(v, mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32HighTemperature);
			refreshDate(v, mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Month,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Day,
					mWeatherInfos.uint32FocusIndex,mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32DayOfWeek);
			v.mWind.setText(LanguageConvertor.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWind));
			v.mWeather.setText(LanguageConvertor.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather));
			int pm25 = 0;
			if (mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25 != null) {
				pm25 = mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25.intValue();
			}
			v.mAirQuality.setText(pm25==0? "" : pm25 + "");
			v.mAirDegree.setText(TextUtils.isEmpty(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality)?"未知":mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality);
			v.mCity.setText(LanguageConvertor.toLocale(mWeatherInfos.strCityName));

			refreshThreeDay(v, mWeatherInfos.rptMsgWeather[0].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[0].int32HighTemperature, mWeatherInfos.rptMsgWeather[0].strWeather,
					mWeatherInfos.rptMsgWeather[1].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[1].int32HighTemperature, mWeatherInfos.rptMsgWeather[1].strWeather,
					mWeatherInfos.rptMsgWeather[2].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[2].int32HighTemperature, mWeatherInfos.rptMsgWeather[2].strWeather,
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

		v.mTempDecade.setImageResource(TEMPERATURE_DRAWABLE_RES[decade]);
		v.mTempUnits.setImageResource(TEMPERATURE_DRAWABLE_RES[units]);
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
		String text = low + "/" + high + "℃";
		v.mTempRange.setText(text);
	}

	/**
	 * 刷新当前日期
	 * 
	 * @param month
	 * @param day
	 * @param index
	 */
	private void refreshDate(WeatherViewHolder v, Integer month, Integer day, Integer index,Integer dayOfWeek) {
		if (month == null || day == null || index == null) {
			return;
		}
		String date = month + "月" + day + "日";
		v.mDate.setText(valueDayOfWeek(dayOfWeek)+" "+date);
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

		int resId = getResourceIdByWeather(weather, isToday);
		if (resId == 0) {
			v.mTodayWeather.setImageDrawable(null);
		} else {
			v.mTodayWeather.setImageResource(resId);
		}
	}

	/**
	 * 根据天气获取图片ID
	 * 
	 * @param weather
	 * @return
	 */
	private int getResourceIdByWeather(String weather,boolean isToday) {
		int id = 0;
		if (weather == null || weather.equals(""))
			return id;
		
		LogUtil.e("weather = " + weather);

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
			if (DateUtils.isNight() && isToday) {
				id = R.drawable.weather_duoyun_night;
			} else {
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
			if (DateUtils.isNight() && isToday) {
				id = R.drawable.weather_qing_night;
			} else {
				id = R.drawable.weather_qing;
			}
		} else if ("沙尘暴".equals(weather)) {
			id = R.drawable.weather_shachenbao;
		} else if ("强沙尘暴".equals(weather)) {
			id = R.drawable.weather_qiangshachenbao;
		} else if ("特大暴雨".equals(weather)) {
			id = R.drawable.weather_tedabaoyu;
		} else if ("雾".equals(weather)) {
			id = R.drawable.weather_wu;
		} else if ("小雪".equals(weather)) {
			id = R.drawable.weather_xiaoxue;
		} else if ("小雨".equals(weather) || "雨".equals(weather)) {
			id = R.drawable.weather_xiaoyu;
		} else if ("扬沙".equals(weather)) {
			id = R.drawable.weather_yangsha;
		} else if ("阴".equals(weather)) {
			id = R.drawable.weather_yin;
		} else if ("雨夹雪".equals(weather)) {
			id = R.drawable.weather_yujiaxue;
		} else if ("阵雨".equals(weather)) {
			if (DateUtils.isNight() && isToday) {
				id = R.drawable.weather_zhenyu_night;
			} else {
				id = R.drawable.weather_zhenyu;
			}
		} else if ("阵雪".equals(weather)) {
			if (DateUtils.isNight() && isToday) {
				id = R.drawable.weather_zhenxue_night;
			} else {
				id = R.drawable.weather_zhenxue;
			}
		} else if ("中雪".equals(weather)) {
			id = R.drawable.weather_zhongxue;
		} else if ("中雨".equals(weather)) {
			id = R.drawable.weather_zhongyu;
		} else {
			id = R.drawable.weather_na;
		}
		return id;
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
	private void refreshThreeDay(WeatherViewHolder v, Integer todayLow, Integer todayHigh, String todayWeather,
			Integer tomorrowLow, Integer tomorrowHigh, String tomorrowWeather, Integer thedayafterLow,
			Integer thedayafterHigh, String thedayafterWeather, Integer currentIndex) {
		// 金天
		int resId = getResourceIdByWeather(todayWeather, true);
		if (resId == 0) {
			v.mToday.setDrawable(AppLogicBase.getApp().getResources().getDrawable(R.drawable.weather_na));
		} else {
			v.mToday.setDrawable(AppLogicBase.getApp().getResources().getDrawable(resId));
		}

		if (todayLow == null || todayHigh == null) {
			v.mToday.setTitle("");
			return;
		}

		String text = todayLow + "/" + todayHigh + "℃";
		v.mToday.setTitle(LanguageConvertor.toLocale(text));

		// 明天
		resId = getResourceIdByWeather(tomorrowWeather, false);
		if (resId == 0) {
			v.mTomorrow.setDrawable(AppLogicBase.getApp().getResources().getDrawable(R.drawable.weather_na));
		} else {
			v.mTomorrow.setDrawable(AppLogicBase.getApp().getResources().getDrawable(resId));
		}

		if (tomorrowLow == null || tomorrowHigh == null) {
			return;
		}

		text = tomorrowLow + "/" + tomorrowHigh + "℃";
		v.mTomorrow.setTitle(LanguageConvertor.toLocale(text));

		// 后天
		resId = getResourceIdByWeather(thedayafterWeather, false);
		if (resId == 0) {
			v.mTheDayAfterTomorrow.setDrawable(AppLogicBase.getApp().getResources().getDrawable(R.drawable.weather_na));
		} else {
			v.mTheDayAfterTomorrow.setDrawable(AppLogicBase.getApp().getResources().getDrawable(resId));
		}

		if (thedayafterLow == null || thedayafterHigh == null) {
			return;
		}

		text = thedayafterLow + "/" + thedayafterHigh + "℃";
		v.mTheDayAfterTomorrow.setTitle(LanguageConvertor.toLocale(text));

		v.mToday.reset();
		v.mTomorrow.reset();
		v.mTheDayAfterTomorrow.reset();
		if (currentIndex == 0) {
			v.mToday.open();
		} else if (currentIndex == 1) {
			v.mTomorrow.open();
		} else if (currentIndex == 2) {
			v.mTheDayAfterTomorrow.open();
		}
	}

	private void refreshBigTempRange(WeatherViewHolder v, Integer low, Integer high) {
		if (low == null || high == null) {
			return;
		}

		if (low >= 0) {
			v.mBigLowMinus.setVisibility(View.GONE);
		} else {
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

		v.mBigLowTempDecade.setImageResource(TEMPERATURE_DRAWABLE_RES[decade]);
		v.mBigLowTempUnits.setImageResource(TEMPERATURE_DRAWABLE_RES[units]);

		if (high >= 0) {
			v.mBigHighMinus.setVisibility(View.GONE);
		} else {
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

		v.mBigHighTempDecade.setImageResource(TEMPERATURE_DRAWABLE_RES[decade]);
		v.mBigHighTempUnits.setImageResource(TEMPERATURE_DRAWABLE_RES[units]);
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

	public void updateData(WeatherInfos infos, WeatherViewHolderLarge wvhl) {
		if (infos == null) {
			return;
		}

		mWeatherInfos = infos;
		try {
			if (mWeatherInfos.uint32FocusIndex == 0) {
				wvhl.mCurrentTempLy.setVisibility(View.VISIBLE);
				wvhl.mBigTempLy.setVisibility(View.INVISIBLE);
				refreshTemp(wvhl, mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32CurTemperature);
			} else {
				wvhl.mCurrentTempLy.setVisibility(View.INVISIBLE);
				wvhl.mBigTempLy.setVisibility(View.VISIBLE);
				refreshBigTempRange(wvhl,
						mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32LowTemperature,
						mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32HighTemperature);
			}
			refreshWeather(wvhl, mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather,
					mWeatherInfos.uint32FocusIndex == 0);
			refreshTempRange(wvhl, mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32HighTemperature);
			refreshDate(wvhl, mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Month,
					mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].uint32Day,
					mWeatherInfos.uint32FocusIndex);
			wvhl.mWingTv.setText(LanguageConvertor.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWind));
			wvhl.mDesTv.setText(LanguageConvertor.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strWeather));
			int pm25 = 0;
			if (mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25 != null) {
				pm25 = mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].int32Pm25.intValue();
			}
			wvhl.mAirNumTv.setText(pm25 + "");
			wvhl.mAirDesTv.setText(LanguageConvertor.toLocale(mWeatherInfos.rptMsgWeather[mWeatherInfos.uint32FocusIndex].strAirQuality));
			wvhl.mCityTv.setText(LanguageConvertor.toLocale(mWeatherInfos.strCityName));

			refreshThreeDay(wvhl, mWeatherInfos.rptMsgWeather[0].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[0].int32HighTemperature, mWeatherInfos.rptMsgWeather[0].strWeather,
					mWeatherInfos.rptMsgWeather[1].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[1].int32HighTemperature, mWeatherInfos.rptMsgWeather[1].strWeather,
					mWeatherInfos.rptMsgWeather[2].int32LowTemperature,
					mWeatherInfos.rptMsgWeather[2].int32HighTemperature, mWeatherInfos.rptMsgWeather[2].strWeather,
					mWeatherInfos.uint32FocusIndex);
		} catch (Exception e) {
		}
	}

	/**
	 * 刷新当前温度
	 * 
	 * @param tmp
	 */
	private void refreshTemp(WeatherViewHolderLarge v, Integer tmp) {
		if (tmp == null) {
			v.mMinusIv.setVisibility(View.GONE);
			v.mLeftIv.setVisibility(View.GONE);
			v.mRightIv.setVisibility(View.GONE);
			v.mDegreeIv.setVisibility(View.GONE);
			return;
		}

		if (tmp >= 0) {
			v.mMinusIv.setVisibility(View.GONE);
		} else {
			v.mMinusIv.setVisibility(View.VISIBLE);
			tmp = -tmp;
		}

		if (tmp < 10) {
			v.mLeftIv.setVisibility(View.GONE);
		} else {
			v.mLeftIv.setVisibility(View.VISIBLE);
		}

		v.mRightIv.setVisibility(View.VISIBLE);
		v.mDegreeIv.setVisibility(View.VISIBLE);

		int decade = tmp / 10;
		int units = tmp % 10;

		v.mLeftIv.setImageResource(TEMPERATURE_DRAWABLE_RES[decade]);
		v.mRightIv.setImageResource(TEMPERATURE_DRAWABLE_RES[units]);
	}

	private void refreshBigTempRange(WeatherViewHolderLarge wvhl, Integer low, Integer high) {
		if (low == null || high == null) {
			return;
		}

		if (low >= 0) {
			wvhl.mBigLowMinusIv.setVisibility(View.GONE);
		} else {
			wvhl.mBigLowMinusIv.setVisibility(View.VISIBLE);
			low = -low;
		}

		if (low < 10) {
			wvhl.mBigLowTempDecade.setVisibility(View.GONE);
		} else {
			wvhl.mBigLowTempDecade.setVisibility(View.VISIBLE);
		}

		int decade = low / 10;
		int units = low % 10;

		wvhl.mBigLowTempDecade.setImageResource(TEMPERATURE_DRAWABLE_RES[decade]);
		wvhl.mBigLowTempUnits.setImageResource(TEMPERATURE_DRAWABLE_RES[units]);

		if (high >= 0) {
			wvhl.mBigHighMinus.setVisibility(View.GONE);
		} else {
			wvhl.mBigHighMinus.setVisibility(View.VISIBLE);
			high = -high;
		}

		if (high < 10) {
			wvhl.mBigHighTempDecade.setVisibility(View.GONE);
		} else {
			wvhl.mBigHighTempDecade.setVisibility(View.VISIBLE);
		}

		decade = high / 10;
		units = high % 10;

		wvhl.mBigHighTempDecade.setImageResource(TEMPERATURE_DRAWABLE_RES[decade]);
		wvhl.mBigHighTempUnits.setImageResource(TEMPERATURE_DRAWABLE_RES[units]);
	}

	/**
	 * 刷新天气
	 * 
	 * @param weather
	 */
	private void refreshWeather(WeatherViewHolderLarge v, String weather,boolean isToday) {
		if (weather == null || weather.equals(""))
			return;

		int resId = getResourceIdByWeather(weather,isToday);
		if (resId == 0) {
			v.mIv.setImageDrawable(null);
		} else {
			v.mIv.setImageResource(resId);
		}
	}

	/**
	 * 刷新当前温度范围
	 * 
	 * @param low
	 * @param high
	 */
	private void refreshTempRange(WeatherViewHolderLarge v, Integer low, Integer high) {
		if (low == null || high == null) {
			return;
		}
		String text = low + "/" + high + "℃";
		v.mTempRangeTv.setText(text);
	}

	/**
	 * 刷新当前日期
	 * 
	 * @param month
	 * @param day
	 * @param index
	 */
	private void refreshDate(WeatherViewHolderLarge v, Integer month, Integer day, Integer index) {
		if (month == null || day == null || index == null) {
			return;
		}
		String date = month + "月" + day + "日";
		v.mDateTv.setText(LanguageConvertor.toLocale(date));
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
			v.mTodayTv.setText(LanguageConvertor.toLocale(d));
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
	private void refreshThreeDay(WeatherViewHolderLarge v, Integer todayLow, Integer todayHigh, String todayWeather,
			Integer tomorrowLow, Integer tomorrowHigh, String tomorrowWeather, Integer thedayafterLow,
			Integer thedayafterHigh, String thedayafterWeather, Integer currentIndex) {
		// 金天
		int resId = getResourceIdByWeather(todayWeather,true);
		if (resId == 0) {
			v.mTodayIv.setImageDrawable(AppLogicBase.getApp().getResources().getDrawable(R.drawable.weather_na));
		} else {
			v.mTodayIv.setImageDrawable(AppLogicBase.getApp().getResources().getDrawable(resId));
		}

		if (todayLow == null || todayHigh == null) {
			return;
		}

		String text = todayLow + "/" + todayHigh + "℃";
		v.mTodayDegree.setText(text);
		v.mTodayDesTv.setText(todayWeather);

		// 明天
		resId = getResourceIdByWeather(tomorrowWeather, false);
		if (resId == 0) {
			v.mTomorrowIv.setImageDrawable(AppLogicBase.getApp().getResources().getDrawable(R.drawable.weather_na));
		} else {
			v.mTomorrowIv.setImageDrawable(AppLogicBase.getApp().getResources().getDrawable(resId));
		}

		if (tomorrowLow == null || tomorrowHigh == null) {
			return;
		}

		text = tomorrowLow + "/" + tomorrowHigh + "℃";
		v.mTomorrowDegree.setText(text);
		v.mTomorrowDesTv.setText(tomorrowWeather);

		// 后天
		resId = getResourceIdByWeather(thedayafterWeather, false);
		if (resId == 0) {
			v.mDayAfterIv.setImageDrawable(AppLogicBase.getApp().getResources().getDrawable(R.drawable.weather_na));
		} else {
			v.mDayAfterIv.setImageDrawable(AppLogicBase.getApp().getResources().getDrawable(resId));
		}

		if (thedayafterLow == null || thedayafterHigh == null) {
			return;
		}

		text = thedayafterLow + "/" + thedayafterHigh + "℃";
		v.mDayAfterDegree.setText(text);
		v.mDayAfterDesTv.setText(thedayafterWeather);

		v.mTodayLayout.setBackgroundColor(Color.parseColor("#CC000000"));
		v.mTomorrowLayout.setBackgroundColor(Color.parseColor("#CC000000"));
		v.mDayAfterLayout.setBackgroundColor(Color.parseColor("#CC000000"));
		if (currentIndex == 0) {
			v.mTodayLayout.setBackgroundColor(Color.parseColor("#33FFFFFF"));
		} else if (currentIndex == 1) {
			v.mTomorrowLayout.setBackgroundColor(Color.parseColor("#33FFFFFF"));
		} else if (currentIndex == 2) {
			v.mDayAfterLayout.setBackgroundColor(Color.parseColor("#33FFFFFF"));
		}
	}
	
	private String valueDayOfWeek(Integer dayOfWeek){
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
