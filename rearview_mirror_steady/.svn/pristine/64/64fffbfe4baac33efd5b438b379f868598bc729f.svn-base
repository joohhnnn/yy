package com.txznet.resholder.theme.ironman.view;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.WeatherRefresher;
import com.txznet.comm.ui.view.BoundedLinearLayout;
import com.txznet.comm.ui.view.IconTextView;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatWeatherViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatWeatherView;
import com.txznet.comm.util.TextViewUtil;

@SuppressLint("NewApi")
public class ChatWeatherView extends IChatWeatherView{
	
	private static ChatWeatherView sInstance = new ChatWeatherView();
	
	
	private int layoutPaddingTop;
	private int layoutPaddingBottom;
	private int cityMarginLeft;
	private int dateMarginLeft ;
	private int dateMarginRight ;
	private int llContentMarginTop ;
	private Drawable llContentBg ;
	private int mRlTopMarginLeft ;
	private int mRlTopMarginRight;
	private int mRlTopPaddingTop ;
	private int mMinusWidth ;
	private int mMinusHeight;
	private int mTempDecadeWidth;
	private int mTempDecadeHeight ;
	private int mTempUnitsWidth ;
	private int mTempUnitsHeight ;
	private int mTempUnitsMarginLeft ;
	private int mTempDegreeWidth ;
	private int mTempDegreeHeight ;
	private int mTempDegreeMarginLeft ;
	private int mBigLowMinusWidth ;
	private int mBigLowMinusHeight ;
	private int mBigLowTempDecadeWidth ;
	private int mBigLowTempDecadeHeight ;
	private int mBigLowTempUnitsWidth ;
	private int mBigLowTempUnitsHeight ;
	private int mBigLowTempUnitsMarginLeft;
	private int mBigSlashWidth ;
	private int mBigSlashHeight ;
	private int mBigSlashMarginLeft ;
	private int mBigHighMinusWidth ;
	private int mBigHighMinusHeight ;
	private int mBigHighTempDecadeWidth ;
	private int mBigHighTempDecadeHeight ;
	private int mBigHighTempUnitsWidth;
	private int mBigHighTempUnitsHeight;
	private int mBigHighTempUnitsMarginLeft ;
	private int mBigTempDegreeWidth;
	private int mBigTempDegreeHeight;
	private int mBigTempDegreeMarginLeft;
	private int tempRangeParentMarginTop;
	private int mTodayWeatherWidth;
	private int mTodayWeatherHeight;
	private int todayWeatherParentMarginTop;
	private int todayWeatherParentMarginRight;
	private int llAirMarginLeft;
	private int llAirMarginRight;
	private int llAirMarginTop;
	private int mWindMarginLeft;
	private int mAirDegreeMarginLeft;
	private int mAirQualityMarginLeft;
	private int llWeatherTopMargin;
	private float mCitySize;
	private int mCityColor;
	private float mDateSize;
	private int mDateColor;
	private float mWeatherSize;
	private int mWeatherColor;
	private float mWindSize;
	private int mWindColor;
	private float mTempRangeSize;
	private int mTempRangeColor;

	private float mAirQualityTextSize;
	private int mAirQualityTextColor;
	private float mAirQualitySize;
	private int mAirQualityColor;
	private float mAirDegreeSize;
	private int mAirDegreeColor;
	
	private float mTodayTitleSize;
	private int mTodayTitleColor;
	private float mTodayHeadSize;
	private int mTodayHeadColor;
	private float mTomorrowTitleSize;
	private int mTomorrowTitleColor;
	private float mTomorrowHeadSize;
	private int mTomorrowHeadColor;
	private float mTheDayAfterTomorrowTitleSize;
	private int mTheDayAfterTomorrowTitleColor;
	private float mTheDayAfterTomorrowHeadSize;
	private int mTheDayAfterTomorrowHeadColor;
	private int mWeatherMaxWidth;
	private Drawable mWeatherItemBg;
	
	private int llContentPaddingLeft;
	private int llContentPaddingTop;
	private int llContentPaddingRight;
	private int llContentPaddingBottom;
	
	private ChatWeatherView(){
	}
	
	public static ChatWeatherView getInstance(){
		return sInstance;
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatWeatherViewData viewData = (ChatWeatherViewData) data;
		WeatherViewHolder v = new WeatherViewHolder();
		v.layout = (BoundedLinearLayout) LayouUtil.getView("chat_weather_view");
		v.layout.setBoundedWidth(mWeatherMaxWidth);
		
		v.mCurrentTemp = (LinearLayout) LayouUtil.findViewByName("current_temp", v.layout);
		v.mBigTempRange = (LinearLayout) LayouUtil.findViewByName("bigTempRange",v.layout);
		v.mMinus = (ImageView) LayouUtil.findViewByName("minus",v.layout);
		v.mTempDecade = (ImageView) LayouUtil.findViewByName("tempDecade",v.layout);
		v.mTempUnits = (ImageView) LayouUtil.findViewByName("tempUnits",v.layout);
		v.mTempDegree = (ImageView) LayouUtil.findViewByName("tempDegree",v.layout);
		v.mTempRange = (TextView) LayouUtil.findViewByName("tempRange",v.layout);
		v.mTodayWeather = (ImageView) LayouUtil.findViewByName("todayWeather",v.layout);

		v.mBigLowMinus = (ImageView) LayouUtil.findViewByName("bigLowMinus",v.layout);
		v.mBigLowTempDecade = (ImageView) LayouUtil.findViewByName("bigLowTempDecade",v.layout);
		v.mBigLowTempUnits = (ImageView) LayouUtil.findViewByName("bigLowTempUnits",v.layout);
		v.mBigHighMinus = (ImageView) LayouUtil.findViewByName("bigHighMinus",v.layout);
		v.mBigHighTempDecade = (ImageView) LayouUtil.findViewByName("bigHighTempDecade",v.layout);
		v.mBigHighTempUnits = (ImageView) LayouUtil.findViewByName("bigHighTempUnits",v.layout);
		v.mBigTempDegree = (ImageView) LayouUtil.findViewByName("bigTempDegree",v.layout);
		v.mBigSlash = (ImageView) LayouUtil.findViewByName("bigSlash", v.layout);

		v.mDate = (TextView) LayouUtil.findViewByName("date",v.layout);
		v.mDay = (TextView) LayouUtil.findViewByName("day",v.layout);
		v.mWeather = (TextView) LayouUtil.findViewByName("weather",v.layout);
		v.mWind = (TextView) LayouUtil.findViewByName("wind",v.layout);

		v.mAirQualityText = (TextView) LayouUtil.findViewByName("airQualityText",v.layout);
		v.mAirQuality = (TextView) LayouUtil.findViewByName("airQuality",v.layout);
		v.mAirDegree = (TextView) LayouUtil.findViewByName("airDegree",v.layout);
		v.mCity = (TextView) LayouUtil.findViewByName("city",v.layout);

		v.mToday = (IconTextView) LayouUtil.findViewByName("today",v.layout);
		v.mTomorrow = (IconTextView) LayouUtil.findViewByName("tomorrow",v.layout);
		v.mTheDayAfterTomorrow = (IconTextView) LayouUtil.findViewByName("theDayAfterTomorrow",v.layout);
		v.mToday.init();
		v.mTomorrow.init();
		v.mTheDayAfterTomorrow.init();
		v.mToday.setBackground(mWeatherItemBg);
		v.mTomorrow.setBackground(mWeatherItemBg);
		v.mTheDayAfterTomorrow.setBackground(mWeatherItemBg);
		
		
		RelativeLayout.LayoutParams mRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) v.mCity.getLayoutParams();
//		mRelativeLayoutParams.leftMargin = cityMarginLeft;
		mRelativeLayoutParams.bottomMargin = (int) LayouUtil.getDimen("y16");
		v.mCity.setLayoutParams(mRelativeLayoutParams);
		mRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) v.mDate.getLayoutParams();
		mRelativeLayoutParams.leftMargin = dateMarginLeft;
		mRelativeLayoutParams.bottomMargin = (int) LayouUtil.getDimen("y16");
//		mRelativeLayoutParams.rightMargin = dateMarginRight;
		v.mDate.setLayoutParams(mRelativeLayoutParams);
		LinearLayout llContent = (LinearLayout) LayouUtil.findViewByName("llContent", v.layout);
		LinearLayout.LayoutParams mLinearLayoutParams = (LayoutParams) llContent.getLayoutParams();
		mLinearLayoutParams.topMargin = llContentMarginTop;
		llContent.setLayoutParams(mLinearLayoutParams);
		llContent.setBackground(llContentBg);
		llContent.setPadding(llContentPaddingLeft,llContent.getPaddingTop(),llContentPaddingRight,llContent.getPaddingBottom()-(int) LayouUtil.getDimen("y5"));
		RelativeLayout rlTitle = (RelativeLayout) LayouUtil.findViewByName("rlTitle", v.layout);
		rlTitle.setBackground(LayouUtil.getDrawable("title_bg"));
		mLinearLayoutParams = (LayoutParams) rlTitle.getLayoutParams();
		mLinearLayoutParams.height = ListTitleView.getInstance().getTitleHeight();
		rlTitle.setLayoutParams(mLinearLayoutParams);
		
		RelativeLayout mRlTop = (RelativeLayout) LayouUtil.findViewByName("rlTop", v.layout);
		mLinearLayoutParams = (LayoutParams) mRlTop.getLayoutParams();
//		mLinearLayoutParams.leftMargin = mRlTopMarginLeft;
//		mLinearLayoutParams.rightMargin = mRlTopMarginRight;
		mRlTop.setLayoutParams(mLinearLayoutParams);
		mRlTop.setPadding(0, mRlTopPaddingTop, 0, 0);
		
		mLinearLayoutParams = (LayoutParams) v.mMinus.getLayoutParams();
		mLinearLayoutParams.width = mMinusWidth;
		mLinearLayoutParams.height = mMinusHeight;
		v.mMinus.setLayoutParams(mLinearLayoutParams);
		
		mLinearLayoutParams = (LayoutParams) v.mTempDecade.getLayoutParams();
		mLinearLayoutParams.width = mTempDecadeWidth;
		mLinearLayoutParams.height = mTempDecadeHeight;
		v.mTempDecade.setLayoutParams(mLinearLayoutParams);
		
		mLinearLayoutParams = (LayoutParams) v.mTempUnits.getLayoutParams();
		mLinearLayoutParams.width = mTempUnitsWidth;
		mLinearLayoutParams.height = mTempUnitsHeight;
		mLinearLayoutParams.leftMargin = mTempUnitsMarginLeft;
		v.mTempUnits.setLayoutParams(mLinearLayoutParams);

		mLinearLayoutParams = (LayoutParams) v.mTempDegree.getLayoutParams();
		mLinearLayoutParams.width = mTempDegreeWidth;
		mLinearLayoutParams.height = mTempDegreeHeight;
		mLinearLayoutParams.leftMargin = mTempDegreeMarginLeft;
		v.mTempDegree.setLayoutParams(mLinearLayoutParams);

		mLinearLayoutParams = (LayoutParams) v.mBigLowMinus.getLayoutParams();
		mLinearLayoutParams.width = mBigLowMinusWidth;
		mLinearLayoutParams.height = mBigLowMinusHeight;
		v.mBigLowMinus.setLayoutParams(mLinearLayoutParams);

		mLinearLayoutParams = (LayoutParams) v.mBigLowTempDecade.getLayoutParams();
		mLinearLayoutParams.width = mBigLowTempDecadeWidth;
		mLinearLayoutParams.height = mBigLowTempDecadeHeight;
		v.mBigLowTempDecade.setLayoutParams(mLinearLayoutParams);
		
		mLinearLayoutParams = (LayoutParams) v.mBigLowTempUnits.getLayoutParams();
		mLinearLayoutParams.width = mBigLowTempUnitsWidth;
		mLinearLayoutParams.height = mBigLowTempUnitsHeight;
		mLinearLayoutParams.leftMargin = mBigLowTempUnitsMarginLeft;
		v.mBigLowTempUnits.setLayoutParams(mLinearLayoutParams);
		
		mLinearLayoutParams = (LayoutParams) v.mBigSlash.getLayoutParams();
		mLinearLayoutParams.width = mBigSlashWidth;
		mLinearLayoutParams.height = mBigSlashHeight;
		mLinearLayoutParams.leftMargin = mBigSlashMarginLeft;
		v.mBigSlash.setLayoutParams(mLinearLayoutParams);
		
		mLinearLayoutParams = (LayoutParams) v.mBigHighMinus.getLayoutParams();
		mLinearLayoutParams.width = mBigHighMinusWidth;
		mLinearLayoutParams.height = mBigHighMinusHeight;
		v.mBigHighMinus.setLayoutParams(mLinearLayoutParams);
		
		mLinearLayoutParams = (LayoutParams) v.mBigHighTempDecade.getLayoutParams();
		mLinearLayoutParams.width = mBigHighTempDecadeWidth;
		mLinearLayoutParams.height = mBigHighTempDecadeHeight;
		v.mBigHighTempDecade.setLayoutParams(mLinearLayoutParams);

		mLinearLayoutParams = (LayoutParams) v.mBigHighTempUnits.getLayoutParams();
		mLinearLayoutParams.width = mBigHighTempUnitsWidth;
		mLinearLayoutParams.height = mBigHighTempUnitsHeight;
		mLinearLayoutParams.leftMargin = mBigHighTempUnitsMarginLeft;
		v.mBigHighTempUnits.setLayoutParams(mLinearLayoutParams);
		
		mLinearLayoutParams = (LayoutParams) v.mBigTempDegree.getLayoutParams();
		mLinearLayoutParams.width = mBigTempDegreeWidth;
		mLinearLayoutParams.height = mBigTempDegreeHeight;
		mLinearLayoutParams.leftMargin = mBigTempDegreeMarginLeft;
		v.mBigTempDegree.setLayoutParams(mLinearLayoutParams);
		
		LinearLayout tempRangeParent = (LinearLayout) v.mTempRange.getParent();
		mRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) tempRangeParent.getLayoutParams();
		mRelativeLayoutParams.topMargin = tempRangeParentMarginTop;
		tempRangeParent.setLayoutParams(mRelativeLayoutParams);
		
		mLinearLayoutParams = (LayoutParams) v.mTodayWeather.getLayoutParams();
		mLinearLayoutParams.width = mTodayWeatherWidth;
		mLinearLayoutParams.height = mTodayWeatherHeight;
		v.mTodayWeather.setLayoutParams(mLinearLayoutParams);
		LinearLayout todayWeatherParent = (LinearLayout) v.mTodayWeather.getParent();
		mRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) todayWeatherParent.getLayoutParams();
//		mRelativeLayoutParams.rightMargin = todayWeatherParentMarginRight;
		mRelativeLayoutParams.topMargin = todayWeatherParentMarginTop;
		todayWeatherParent.setLayoutParams(mRelativeLayoutParams);
		
		LinearLayout llAir = (LinearLayout) LayouUtil.findViewByName("llAir", v.layout);
		mLinearLayoutParams = (LayoutParams) llAir.getLayoutParams();
//		mLinearLayoutParams.leftMargin = llAirMarginLeft;
//		mLinearLayoutParams.rightMargin = llAirMarginRight;
//		mLinearLayoutParams.topMargin = llAirMarginTop;
		llAir.setLayoutParams(mLinearLayoutParams);
		
		mRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) v.mWind.getLayoutParams();
		mRelativeLayoutParams.leftMargin = mWindMarginLeft;
		v.mWind.setLayoutParams(mRelativeLayoutParams);

		mRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) v.mAirDegree.getLayoutParams();
		mRelativeLayoutParams.leftMargin = mAirDegreeMarginLeft;
		v.mAirDegree.setLayoutParams(mRelativeLayoutParams);
		
		mRelativeLayoutParams = (android.widget.RelativeLayout.LayoutParams) v.mAirQuality.getLayoutParams();
		mRelativeLayoutParams.leftMargin = mAirQualityMarginLeft;
		v.mAirQuality.setLayoutParams(mRelativeLayoutParams);
		
		LinearLayout llWeather = (LinearLayout) LayouUtil.findViewByName("llWeather", v.layout);
		mLinearLayoutParams = (LayoutParams) llWeather.getLayoutParams();
		mLinearLayoutParams.topMargin = llWeatherTopMargin;
		llWeather.setPadding(0,0,0, (int)LayouUtil.getDimen("y16"));
		llWeather.setLayoutParams(mLinearLayoutParams);
		
		View divider1 = (View) LayouUtil.findViewByName("divider1", v.layout);
		mLinearLayoutParams = (LayoutParams) divider1.getLayoutParams();
		mLinearLayoutParams.width = 10;
		divider1.setLayoutParams(mLinearLayoutParams);
		View divider2 = (View) LayouUtil.findViewByName("divider2", v.layout);
		mLinearLayoutParams = (LayoutParams) divider2.getLayoutParams();
		mLinearLayoutParams.width = 10;
		divider2.setLayoutParams(mLinearLayoutParams);
		
		TextViewUtil.setTextSize(v.mCity,mCitySize);
		TextViewUtil.setTextColor(v.mCity,mCityColor);
		TextViewUtil.setTextSize(v.mDate,mDateSize);
		TextViewUtil.setTextColor(v.mDate,mDateColor);
		TextViewUtil.setTextSize(v.mWeather,mWeatherSize);
		TextViewUtil.setTextColor(v.mWeather,mWeatherColor);
		TextViewUtil.setTextSize(v.mWind,mWindSize);
		TextViewUtil.setTextColor(v.mWind,mWindColor);
		TextViewUtil.setTextSize(v.mTempRange,mTempRangeSize);
		TextViewUtil.setTextColor(v.mTempRange,mTempRangeColor);

		TextViewUtil.setTextSize(v.mAirQualityText,mAirQualityTextSize);
		TextViewUtil.setTextColor(v.mAirQualityText,mAirQualityTextColor);
		TextViewUtil.setTextSize(v.mAirQuality,mAirQualitySize);
		TextViewUtil.setTextColor(v.mAirQuality,mAirQualityColor);
		TextViewUtil.setTextSize(v.mAirDegree,mAirDegreeSize);
		TextViewUtil.setTextColor(v.mAirDegree,mAirDegreeColor);
		
		v.mToday.setTitleSize(mTodayTitleSize);
		v.mToday.setTitleColor(mTodayTitleColor);
		v.mToday.setHeadSize(mTodayHeadSize);
		v.mToday.setHeadColor(mTodayHeadColor);
		v.mTomorrow.setTitleSize(mTomorrowTitleSize);
		v.mTomorrow.setTitleColor(mTomorrowTitleColor);
		v.mTomorrow.setHeadSize(mTomorrowHeadSize);
		v.mTomorrow.setHeadColor(mTomorrowHeadColor);
		v.mTheDayAfterTomorrow.setTitleSize(mTheDayAfterTomorrowTitleSize);
		v.mTheDayAfterTomorrow.setTitleColor(mTheDayAfterTomorrowTitleColor);
		v.mTheDayAfterTomorrow.setHeadSize(mTheDayAfterTomorrowHeadSize);
		v.mTheDayAfterTomorrow.setHeadColor(mTheDayAfterTomorrowHeadColor);
		
		WeatherRefresher.getInstance().updateData(viewData.textContent, v);
		
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = v.layout;
		adapter.object = ChatWeatherView.getInstance();
		return adapter;
	}
	
	@Override
	public void init() {
		layoutPaddingTop = (int)LayouUtil.getDimen("y20");
		layoutPaddingBottom = (int)LayouUtil.getDimen("y60");
		cityMarginLeft = (int) LayouUtil.getDimen("x10");
		dateMarginLeft = (int) LayouUtil.getDimen("x10");
		dateMarginRight = (int) LayouUtil.getDimen("x30");
		llContentMarginTop = (int) LayouUtil.getDimen("y10");
		llContentBg = LayouUtil.getDrawable("list_bg");
		mRlTopMarginLeft = (int) LayouUtil.getDimen("x10");
		mRlTopMarginRight = (int) LayouUtil.getDimen("x10");
		mRlTopPaddingTop = (int) LayouUtil.getDimen("y5");
		mMinusWidth = (int) LayouUtil.getDimen("y36");
		mMinusHeight = (int) LayouUtil.getDimen("y90");
		mTempDecadeWidth = (int) LayouUtil.getDimen("y60");
		mTempDecadeHeight = (int) LayouUtil.getDimen("y90");
		mTempUnitsWidth = (int) LayouUtil.getDimen("y60");
		mTempUnitsHeight = (int) LayouUtil.getDimen("y90");
		mTempUnitsMarginLeft = (int) LayouUtil.getDimen("x5");
		mTempDegreeWidth = (int) LayouUtil.getDimen("y26");
		mTempDegreeHeight = (int) LayouUtil.getDimen("y26");
		mTempDegreeMarginLeft = (int) LayouUtil.getDimen("x10");
		mBigLowMinusWidth = (int) LayouUtil.getDimen("y36");
		mBigLowMinusHeight = (int) LayouUtil.getDimen("y90");
		mBigLowTempDecadeWidth = (int) LayouUtil.getDimen("y60");
		mBigLowTempDecadeHeight = (int) LayouUtil.getDimen("y90");
		mBigLowTempUnitsWidth = (int) LayouUtil.getDimen("y60");
		mBigLowTempUnitsHeight = (int) LayouUtil.getDimen("y90");
		mBigLowTempUnitsMarginLeft = (int) LayouUtil.getDimen("x5");
		mBigSlashWidth = (int) LayouUtil.getDimen("y28");
		mBigSlashHeight = (int) LayouUtil.getDimen("y90");
		mBigSlashMarginLeft = (int) LayouUtil.getDimen("x10");
		mBigHighMinusWidth = (int) LayouUtil.getDimen("y36");
		mBigHighMinusHeight = (int) LayouUtil.getDimen("y90");
		mBigHighTempDecadeWidth = (int) LayouUtil.getDimen("y60");
		mBigHighTempDecadeHeight = (int) LayouUtil.getDimen("y90");
		mBigHighTempUnitsWidth = (int) LayouUtil.getDimen("y60");
		mBigHighTempUnitsHeight = (int) LayouUtil.getDimen("y90");
		mBigHighTempUnitsMarginLeft = (int) LayouUtil.getDimen("x5");
		
		mBigTempDegreeWidth = (int) LayouUtil.getDimen("y26");
		mBigTempDegreeHeight = (int) LayouUtil.getDimen("y26");
		mBigTempDegreeMarginLeft = (int) LayouUtil.getDimen("x10");
		tempRangeParentMarginTop = (int) LayouUtil.getDimen("y20");
		mTodayWeatherWidth = (int) LayouUtil.getDimen("x90");
		mTodayWeatherHeight = (int) LayouUtil.getDimen("y90");
		todayWeatherParentMarginRight = (int) LayouUtil.getDimen("x30");
		todayWeatherParentMarginTop = (int) LayouUtil.getDimen("y20");
		llAirMarginLeft = (int) LayouUtil.getDimen("x30");
		llAirMarginRight = (int) LayouUtil.getDimen("x30");
		llAirMarginTop = (int) LayouUtil.getDimen("y5");
		mWindMarginLeft = (int) LayouUtil.getDimen("x10");
		mAirDegreeMarginLeft = (int) LayouUtil.getDimen("x10");
		mAirQualityMarginLeft = (int) LayouUtil.getDimen("x15");
		llWeatherTopMargin = (int) LayouUtil.getDimen("y10");
		
		mCitySize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_CITY_SIZE1);
		mCityColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_CITY_COLOR1);
		mDateSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_DATE_SIZE1);
		mDateColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_DATE_COLOR1);
		mWeatherSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_STATE_SIZE1);
		mWeatherColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_STATE_COLOR1);
		mWindSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_STATE_SIZE1);
		mWindColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_STATE_COLOR1);
		mTempRangeSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_TMP_SIZE2);
		mTempRangeColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_TMP_COLOR2);

		mAirQualityTextSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_AIR_SIZE1);
		mAirQualityTextColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_AIR_COLOR1);
		mAirQualitySize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_AIR_SIZE1);
		mAirQualityColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_AIR_COLOR1);
		mAirDegreeSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_AIR_SIZE1);
		mAirDegreeColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_AIR_COLOR1);
		
		mTodayTitleSize = ((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE2));
		mTodayTitleColor = ((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR2));
		mTodayHeadSize = ((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE1));
		mTodayHeadColor = ((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR1));
		mTomorrowTitleSize = ((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE2));
		mTomorrowTitleColor = ((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR2));
		mTomorrowHeadSize = ((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE1));
		mTomorrowHeadColor = ((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR1));
		mTheDayAfterTomorrowTitleSize = ((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE2));
		mTheDayAfterTomorrowTitleColor = ((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR2));
		mTheDayAfterTomorrowHeadSize = ((Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_WEATHER_ITEM_SIZE1));
		mTheDayAfterTomorrowHeadColor = ((Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_WEATHER_ITEM_COLOR1));
		mWeatherMaxWidth = ThemeConfigManager.WEATHER_MAXWIDTH;
		
		mWeatherItemBg = LayouUtil.getDrawable("weather_item_bg");
		
		llContentPaddingLeft = (int) (ThemeConfigManager.getX(ThemeConfigManager.LIST_CONTENT_PADDINGLEFT)+LayouUtil.getDimen("x10"));
		llContentPaddingTop = (int) (ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_PADDINGTOP)+LayouUtil.getDimen("y10"));
		llContentPaddingRight = (int) (ThemeConfigManager.getX(ThemeConfigManager.LIST_CONTENT_PADDINGRIGHT)+LayouUtil.getDimen("x10"));
		llContentPaddingBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_PADDINGBOTTOM);
	}

}
