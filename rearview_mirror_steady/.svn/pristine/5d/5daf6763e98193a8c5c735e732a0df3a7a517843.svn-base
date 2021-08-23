package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.util.WeatherRefresher;
import com.txznet.comm.ui.view.BoundedLinearLayout;
import com.txznet.comm.ui.view.IconTextView;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatWeatherViewData;
import com.txznet.comm.ui.viewfactory.data.PoiListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatWeatherView;
import com.txznet.comm.util.TextViewUtil;

@SuppressLint("NewApi")
public class ChatWeatherView extends IChatWeatherView{
	
	private static ChatWeatherView sInstance = new ChatWeatherView();

	private LinearLayout llContent;

	private LinearLayout today;
	private TextView today_title;
	private ImageView today_image;
	private TextView today_weather;
	private TextView today_tem;

	private LinearLayout tomorrow;
	private TextView tomorrow_title;
	private ImageView tomorrow_image;
	private TextView tomorrow_weather;
	private TextView tomorrow_tem;

	private LinearLayout dayft;
	private TextView dayft_title;
	private ImageView dayft_image;
	private TextView dayft_weather;
	private TextView dayft_tem;

	//第四天的天气
	private View dividerMore;
    private LinearLayout daymore;
    private TextView daymore_title;
    private ImageView daymore_image;
    private TextView daymore_weather;
    private TextView daymore_tem;

    //第五天的天气
    private View dividerLast;
    private LinearLayout daylast;
    private TextView daylast_title;
    private ImageView daylast_image;
    private TextView daylast_weather;
    private TextView daylast_tem;

	//三天的天气展示
	private int contentWidth;    //内容宽度
	private int contentHeight;    //内容高度
	private int verPadding;    //内容上下边距
    private int todayLeftMargin;    //当天的天气左边距
    private int todayRightMargin;    //当天的天气右边距
	private int tvTimeSize;    //城市、时间字体大小
	private int tvTimeHeight;    //城市、时间行高
	private int tvTimeColor;    //城市、时间字体颜色
    private int ivWeatherTopMargin;    //天气图片上边距
    private int ivMinusWidth;    //负号图片宽度
    private int ivMinusHeight;    //负号图片高度
	private int ivTempWidth;    //温度图片宽度
	private int ivTempHeight;    //温度图片高度
    private int ivDegreeWidth;    //摄氏度图片宽度
    private int ivDegreeHeight;    //摄氏度图片高度
    private int ivWeatherSide;    //天气图片大小
	private int tvWeatherTopMargin;    //风向、天气文字数据上边距
    private int tvWeatherSize;    //风向、天气文字数据字体大小
    private int tvWeatherHeight;    //风向、天气文字数据行高
    private int tvWeatherColor;    //风向、天气文字数据字体颜色
    private int tvTempRangeTopMargin;    //空气质量、温度范围文字数据上边距
    private int tvTempRangeSize;    //空气质量、温度范围文字数据字体大小
    private int tvTempRangeHeight;    //空气质量、温度范围文字数据行高
    private int tvTempRangeColor;    //空气质量、温度范围文字数据字体颜色
    private int tvAirSize;    //空气质量文字数据字体大小
    private int tvAirHorMargin;    //空气质量文字数据左右边距

    //一天的天气展示
    private int ivMinusWidth1;    //负号图片宽度
    private int ivMinusHeight1;    //负号图片高度
    private int ivTempWidth1;    //温度图片宽度
    private int ivTempHeight1;    //温度图片高度
    private int ivDegreeWidth1;    //摄氏度图片宽度
    private int ivDegreeHeight1;    //摄氏度图片高度
    private int ivWeatherSide1;    //天气图片大小
    private int tvWeatherSize1;    //风向、天气文字数据字体大小
    private int tvWeatherHeight1;    //风向、天气文字数据行高

    //无屏间距
    private int ivWeatherTopMarginNone;    //天气图片上边距
    private int tvWeatherTopMarginNone;    //风向、天气文字数据上边距
    private int tvTempRangeTopMarginNone;    //空气质量、温度范围文字数据上边距

    public static int showDayCountFull;    //全屏是否展示更多天气，当设备宽度高比较大时，展示四天天气
    public static int showDayCountHalf;    //半屏是否展示更多天气，当设备宽度高比较大时，展示四天天气

	private ChatWeatherView(){
	}
	
	public static ChatWeatherView getInstance(){
		return sInstance;
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatWeatherViewData viewData = (ChatWeatherViewData) data;

		View view = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = WeatherRefresher2.getInstance().isShowOneDay(viewData.textContent)
						?createWeatherViewFullOneday(viewData):createWeatherViewFull(viewData);
                view.setPadding(0,verPadding,0,verPadding);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = WeatherRefresher2.getInstance().isShowOneDay(viewData.textContent)
						?createWeatherViewHalfOneday(viewData):createWeatherViewHalf(viewData);
                view.setPadding(0,verPadding,0,verPadding);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = WeatherRefresher2.getInstance().isShowOneDay(viewData.textContent)
						?createWeatherViewNoneOneday(viewData):createWeatherViewNone(viewData);
				break;
		}
		
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = view;
		adapter.view.setTag(data.getType());
		adapter.object = ChatWeatherView.getInstance();
		return adapter;
	}

	//全屏界面是否需要展示四天天气
	private void setShowMoreWeather(){
	    int unit = ViewParamsUtil.unit;
	    int contentWidthFull = SizeConfig.screenWidth * 3/4 - SizeConfig.pageWidth - 4 * unit;
	    int contentHeightFull = SizeConfig.itemHeight * SizeConfig.pageCount;
	    int contentWidthHalf = SizeConfig.screenWidth - SizeConfig.pageWidth - 10 * unit;
        int contentHeightHalf = SizeConfig.itemHeight * SizeConfig.pageCount;
        if (contentWidthFull * 2 / 5 >= 1.2 * contentHeightFull){
            showDayCountFull = 5;
        }else if(contentWidthFull / 2 >= 1.2 * contentHeightFull){
            showDayCountFull = 4;
        }else {
            showDayCountFull = 3;
        }
        if (contentWidthHalf * 2 / 5 >= 1.2 * contentHeightHalf){
            showDayCountHalf = 5;
        }else if(contentWidthHalf / 2 >= 1.2 * contentHeightHalf){
            showDayCountHalf = 4;
        }else {
            showDayCountHalf = 3;
        }
        //竖屏只显示三天天气
        if (WinLayout.isVertScreen){
            showDayCountFull = 3;
            showDayCountHalf = 3;
        }
    }

	private View createWeatherViewFull(ChatWeatherViewData viewData){
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setOrientation(LinearLayout.VERTICAL);
        llLayout.setGravity(Gravity.CENTER_VERTICAL);

		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null,"weather","天气");
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

		WeatherViewHolder v = new WeatherViewHolder();
		v.layout = (BoundedLinearLayout) LayouUtil.getView("chat_weather_view2");
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,SizeConfig.itemHeight * SizeConfig.pageCount);
        //layoutParams = new LinearLayout.LayoutParams(isSetWidth()?(int)(SizeConfig.itemHeight * SizeConfig.pageCount * 1.8):LayoutParams.MATCH_PARENT,
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                contentHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(v.layout,layoutParams);

		llContent = (LinearLayout) LayouUtil.findViewByName("llContent",v.layout);
		v.mCurrentTemp = (LinearLayout) LayouUtil.findViewByName("current_temp", v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        //layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = ivWeatherTopMargin;
        v.mCurrentTemp.setLayoutParams(layoutParams);
		//v.mBigTempRange = (LinearLayout) LayouUtil.findViewByName("bigTempRange",v.layout);
		v.mMinus = (ImageView) LayouUtil.findViewByName("minus",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth,ivMinusHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        v.mMinus.setLayoutParams(layoutParams);
		v.mTempDecade = (ImageView) LayouUtil.findViewByName("tempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth,ivTempHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        v.mTempDecade.setLayoutParams(layoutParams);
		v.mTempUnits = (ImageView) LayouUtil.findViewByName("tempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth,ivTempHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        v.mTempUnits.setLayoutParams(layoutParams);
		v.mTempDegree = (ImageView) LayouUtil.findViewByName("tempDegree",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivDegreeWidth,ivDegreeHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        v.mTempDegree.setLayoutParams(layoutParams);
		//v.mTempRange = (TextView) LayouUtil.findViewByName("tempRange",v.layout);
		//v.mTodayWeather = (ImageView) LayouUtil.findViewByName("todayWeather",v.layout);

		/*v.mBigLowMinus = (ImageView) LayouUtil.findViewByName("bigLowMinus",v.layout);
		v.mBigLowTempDecade = (ImageView) LayouUtil.findViewByName("bigLowTempDecade",v.layout);
		v.mBigLowTempUnits = (ImageView) LayouUtil.findViewByName("bigLowTempUnits",v.layout);
		v.mBigHighMinus = (ImageView) LayouUtil.findViewByName("bigHighMinus",v.layout);
		v.mBigHighTempDecade = (ImageView) LayouUtil.findViewByName("bigHighTempDecade",v.layout);
		v.mBigHighTempUnits = (ImageView) LayouUtil.findViewByName("bigHighTempUnits",v.layout);
		v.mBigTempDegree = (ImageView) LayouUtil.findViewByName("bigTempDegree",v.layout);
		v.mBigSlash = (ImageView) LayouUtil.findViewByName("bigSlash", v.layout);*/

		//v.mDate = (TextView) LayouUtil.findViewByName("date",v.layout);
		/*v.mDay = (TextView) LayouUtil.findViewByName("day",v.layout);
		v.mWeather = (TextView) LayouUtil.findViewByName("weather",v.layout);*/
		v.mWind = (TextView) LayouUtil.findViewByName("wind",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        //layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = tvWeatherTopMargin;
        v.mWind.setLayoutParams(layoutParams);

		//v.mAirQualityText = (TextView) LayouUtil.findViewByName("airQualityText",v.layout);
		v.mAirQuality = (TextView) LayouUtil.findViewByName("airQuality",v.layout);
		v.mAirQuality.setSingleLine(true);
		v.mAirQuality.setEllipsize(TextUtils.TruncateAt.END);
        //v.mAirQuality.setPadding(tvAirHorMargin,0,tvAirHorMargin,0);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        //layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = tvTempRangeTopMargin;
        v.mAirQuality.setLayoutParams(layoutParams);
		//v.mAirDegree = (TextView) LayouUtil.findViewByName("airDegree",v.layout);

        LinearLayout llTop = (LinearLayout) LayouUtil.findViewByName("llTop",v.layout);

		//v.mCity = (TextView) LayouUtil.findViewByName("city",v.layout);
		v.mCity = (TextView) LayouUtil.findViewByName("city",llTop);
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        layoutParams = (LinearLayout.LayoutParams) v.mCity.getLayoutParams();
        layoutParams.height = tvTimeHeight;
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = 0;
        v.mCity.setLayoutParams(layoutParams);

		/*v.mToday = (IconTextView) LayouUtil.findViewByName("today",v.layout);
		v.mTomorrow = (IconTextView) LayouUtil.findViewByName("tomorrow",v.layout);
		v.mTheDayAfterTomorrow = (IconTextView) LayouUtil.findViewByName("theDayAfterTomorrow",v.layout);
		v.mToday.init();
		v.mTomorrow.init();
		v.mTheDayAfterTomorrow.init();*/

		//llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
		llContent.setBackground(LayouUtil.getDrawable("weather_background"));


		/*today = (LinearLayout) LayouUtil.findViewByName("today",v.layout);
        layoutParams = (LinearLayout.LayoutParams)today.getLayoutParams();
        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        today.setLayoutParams(layoutParams);*/
		//today_title = (TextView) LayouUtil.findViewByName("today_title",v.layout);
		today_title = (TextView) LayouUtil.findViewByName("today_title",llTop);
        today_title.setGravity(Gravity.RIGHT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.rightMargin = todayRightMargin;
        today_title.setLayoutParams(layoutParams);
		today_image = (ImageView) LayouUtil.findViewByName("today_image",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,ivWeatherSide);
        today_image.setScaleType(ImageView.ScaleType.FIT_END);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.rightMargin = todayRightMargin;
        today_image.setLayoutParams(layoutParams);
		today_weather = (TextView) LayouUtil.findViewByName("today_weather",v.layout);
        today_weather.setGravity(Gravity.RIGHT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMargin;
        layoutParams.rightMargin = todayRightMargin;
        today_weather.setLayoutParams(layoutParams);
		today_tem = (TextView) LayouUtil.findViewByName("today_tem",v.layout);
        today_tem.setGravity(Gravity.RIGHT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,tvTempRangeHeight);
        layoutParams.topMargin = tvTempRangeTopMargin;
        layoutParams.rightMargin = todayRightMargin;
        today_tem.setLayoutParams(layoutParams);

		tomorrow = (LinearLayout) LayouUtil.findViewByName("tomorrow",v.layout);
        layoutParams = (LinearLayout.LayoutParams)tomorrow.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        tomorrow.setLayoutParams(layoutParams);
		tomorrow_title = (TextView) LayouUtil.findViewByName("tomorrow_title",tomorrow);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        tomorrow_title.setLayoutParams(layoutParams);
		tomorrow_image = (ImageView) LayouUtil.findViewByName("tomorrow_image",tomorrow);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.bottomMargin = tvWeatherTopMargin;
        tomorrow_image.setLayoutParams(layoutParams);
		tomorrow_weather = (TextView) LayouUtil.findViewByName("tomorrow_weather",tomorrow);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        //layoutParams.topMargin = tvWeatherTopMargin;
        //tomorrow_weather.setLayoutParams(layoutParams);
		tomorrow_tem = (TextView) LayouUtil.findViewByName("tomorrow_tem",tomorrow);
        layoutParams.topMargin = tvTempRangeTopMargin;
        tomorrow_tem.setLayoutParams(layoutParams);

		dayft = (LinearLayout) LayouUtil.findViewByName("dayft",v.layout);
        layoutParams = (LinearLayout.LayoutParams)dayft.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        dayft.setLayoutParams(layoutParams);
		dayft_title = (TextView) LayouUtil.findViewByName("dayft_title",dayft);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        dayft_title.setLayoutParams(layoutParams);
		dayft_image = (ImageView) LayouUtil.findViewByName("dayft_image",dayft);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.bottomMargin = tvWeatherTopMargin;
        dayft_image.setLayoutParams(layoutParams);
		dayft_weather = (TextView) LayouUtil.findViewByName("dayft_weather",dayft);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMargin;
        //dayft_weather.setLayoutParams(layoutParams);
		dayft_tem = (TextView) LayouUtil.findViewByName("dayft_tem",dayft);
        layoutParams.topMargin = tvTempRangeTopMargin;
        dayft_tem.setLayoutParams(layoutParams);

        dividerMore = (View) LayouUtil.findViewByName("dividerMore",v.layout);

        daymore = (LinearLayout) LayouUtil.findViewByName("daymore",v.layout);
        layoutParams = (LinearLayout.LayoutParams)daymore.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        daymore.setLayoutParams(layoutParams);
        daymore_title = (TextView) LayouUtil.findViewByName("daymore_title",daymore);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        daymore_title.setLayoutParams(layoutParams);
        daymore_image = (ImageView) LayouUtil.findViewByName("daymore_image",daymore);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.bottomMargin = tvWeatherTopMargin;
        daymore_image.setLayoutParams(layoutParams);
        daymore_weather = (TextView) LayouUtil.findViewByName("daymore_weather",daymore);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMargin;
        //dayft_weather.setLayoutParams(layoutParams);
        daymore_tem = (TextView) LayouUtil.findViewByName("daymore_tem",daymore);
        layoutParams.topMargin = tvTempRangeTopMargin;
        daymore_tem.setLayoutParams(layoutParams);

        dividerLast = (View) LayouUtil.findViewByName("dividerLast",v.layout);

        daylast = (LinearLayout) LayouUtil.findViewByName("daylast",v.layout);
        layoutParams = (LinearLayout.LayoutParams)daylast.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        daylast.setLayoutParams(layoutParams);
        daylast_title = (TextView) LayouUtil.findViewByName("daylast_title",daylast);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        daylast_title.setLayoutParams(layoutParams);
        daylast_image = (ImageView) LayouUtil.findViewByName("daylast_image",daylast);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.bottomMargin = tvWeatherTopMargin;
        daylast_image.setLayoutParams(layoutParams);
        daylast_weather = (TextView) LayouUtil.findViewByName("daylast_weather",daylast);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMargin;
        //dayft_weather.setLayoutParams(layoutParams);
        daylast_tem = (TextView) LayouUtil.findViewByName("daylast_tem",daylast);
        layoutParams.topMargin = tvTempRangeTopMargin;
        daylast_tem.setLayoutParams(layoutParams);

		v.mMinus.setImageDrawable(LayouUtil.getDrawable("weather_number_minus"));

		TextViewUtil.setTextSize(v.mCity,tvTimeSize);
		TextViewUtil.setTextColor(v.mCity,tvTimeColor);
		TextViewUtil.setTextSize(v.mWind,tvWeatherSize);
		TextViewUtil.setTextColor(v.mWind,tvWeatherColor);
		TextViewUtil.setTextSize(v.mAirQuality,tvAirSize);
		TextViewUtil.setTextColor(v.mAirQuality,tvTempRangeColor);

		TextViewUtil.setTextSize(today_title,tvTimeSize);
		TextViewUtil.setTextColor(today_title,tvTimeColor);
		TextViewUtil.setTextSize(today_weather,tvWeatherSize);
		TextViewUtil.setTextColor(today_weather,tvWeatherColor);
		TextViewUtil.setTextSize(today_tem,tvTempRangeSize);
		TextViewUtil.setTextColor(today_tem,tvTempRangeColor);

		TextViewUtil.setTextSize(tomorrow_title,tvTimeSize);
		TextViewUtil.setTextColor(tomorrow_title,tvTimeColor);
		TextViewUtil.setTextSize(tomorrow_weather,tvWeatherSize);
		TextViewUtil.setTextColor(tomorrow_weather,tvWeatherColor);
		TextViewUtil.setTextSize(tomorrow_tem,tvTempRangeSize);
		TextViewUtil.setTextColor(tomorrow_tem,tvTempRangeColor);

		TextViewUtil.setTextSize(dayft_title,tvTimeSize);
		TextViewUtil.setTextColor(dayft_title,tvTimeColor);
		TextViewUtil.setTextSize(dayft_weather,tvWeatherSize);
		TextViewUtil.setTextColor(dayft_weather,tvWeatherColor);
		TextViewUtil.setTextSize(dayft_tem,tvTempRangeSize);
		TextViewUtil.setTextColor(dayft_tem,tvTempRangeColor);

        TextViewUtil.setTextSize(daymore_title,tvTimeSize);
        TextViewUtil.setTextColor(daymore_title,tvTimeColor);
        TextViewUtil.setTextSize(daymore_weather,tvWeatherSize);
        TextViewUtil.setTextColor(daymore_weather,tvWeatherColor);
        TextViewUtil.setTextSize(daymore_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(daymore_tem,tvTempRangeColor);

        TextViewUtil.setTextSize(daylast_title,tvTimeSize);
        TextViewUtil.setTextColor(daylast_title,tvTimeColor);
        TextViewUtil.setTextSize(daylast_weather,tvWeatherSize);
        TextViewUtil.setTextColor(daylast_weather,tvWeatherColor);
        TextViewUtil.setTextSize(daylast_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(daylast_tem,tvTempRangeColor);

        if (showDayCountFull == 5){
            dividerMore.setVisibility(View.VISIBLE);
            daymore.setVisibility(View.VISIBLE);
            dividerLast.setVisibility(View.VISIBLE);
            daylast.setVisibility(View.VISIBLE);
        }else if (showDayCountFull == 4){
            dividerMore.setVisibility(View.VISIBLE);
            daymore.setVisibility(View.VISIBLE);
            dividerLast.setVisibility(View.GONE);
            daylast.setVisibility(View.GONE);
        }else {
            dividerMore.setVisibility(View.GONE);
            daymore.setVisibility(View.GONE);
            dividerLast.setVisibility(View.GONE);
            daylast.setVisibility(View.GONE);
        }

		WeatherRefresher2.getInstance().updateData(viewData.textContent, v);

		//return v.layout;
		return llLayout;
	}

	private View createWeatherViewFullOneday(ChatWeatherViewData viewData){
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setOrientation(LinearLayout.VERTICAL);
        llLayout.setGravity(Gravity.CENTER_VERTICAL);

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null,"weather","天气");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

		WeatherViewHolder v = new WeatherViewHolder();
		v.layout = (BoundedLinearLayout) LayouUtil.getView("chat_weather_view_oneday");
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,SizeConfig.itemHeight * SizeConfig.pageCount);
        //layoutParams = new LinearLayout.LayoutParams(isSetWidth()?(int)(SizeConfig.itemHeight * SizeConfig.pageCount * 1.8):LayoutParams.MATCH_PARENT,
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                contentHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(v.layout,layoutParams);

		llContent = (LinearLayout) LayouUtil.findViewByName("llContent",v.layout);
        LinearLayout temp = (LinearLayout) LayouUtil.findViewByName("temp",v.layout);
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTempRangeTopMargin + ivWeatherSide);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = tvTempRangeTopMargin;
        temp.setLayoutParams(layoutParams);
		v.mCurrentTemp = (LinearLayout) LayouUtil.findViewByName("current_temp", v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        //layoutParams.topMargin = tvTempRangeTopMargin;
        v.mCurrentTemp.setLayoutParams(layoutParams);
		v.mBigTempRange = (LinearLayout) LayouUtil.findViewByName("bigTempRange",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        //layoutParams.topMargin = tvTempRangeTopMargin;
        v.mBigTempRange.setLayoutParams(layoutParams);
        v.mMinus = (ImageView) LayouUtil.findViewByName("minus",v.layout);
		v.mMinus.setImageDrawable(LayouUtil.getDrawable("weather_number_minus"));
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mMinus.setLayoutParams(layoutParams);
        v.mTempDecade = (ImageView) LayouUtil.findViewByName("tempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mTempDecade.setLayoutParams(layoutParams);
        v.mTempUnits = (ImageView) LayouUtil.findViewByName("tempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mTempUnits.setLayoutParams(layoutParams);
        v.mTempDegree = (ImageView) LayouUtil.findViewByName("tempDegree",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivDegreeWidth1,ivDegreeHeight1);
        layoutParams.gravity = Gravity.TOP;
        v.mTempDegree.setLayoutParams(layoutParams);
		//v.mTempRange = (TextView) LayouUtil.findViewByName("tempRange",v.layout);
		//v.mTodayWeather = (ImageView) LayouUtil.findViewByName("todayWeather",v.layout);

		v.mBigLowMinus = (ImageView) LayouUtil.findViewByName("bigLowMinus",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigLowMinus.setLayoutParams(layoutParams);
		v.mBigLowTempDecade = (ImageView) LayouUtil.findViewByName("bigLowTempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigLowTempDecade.setLayoutParams(layoutParams);
		v.mBigLowTempUnits = (ImageView) LayouUtil.findViewByName("bigLowTempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigLowTempUnits.setLayoutParams(layoutParams);
        v.mBigSlash = (ImageView) LayouUtil.findViewByName("bigSlash", v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigSlash.setLayoutParams(layoutParams);
		v.mBigHighMinus = (ImageView) LayouUtil.findViewByName("bigHighMinus",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigHighMinus.setLayoutParams(layoutParams);
        v.mBigHighTempDecade = (ImageView) LayouUtil.findViewByName("bigHighTempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigHighTempDecade.setLayoutParams(layoutParams);
		v.mBigHighTempUnits = (ImageView) LayouUtil.findViewByName("bigHighTempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigHighTempUnits.setLayoutParams(layoutParams);
		v.mBigTempDegree = (ImageView) LayouUtil.findViewByName("bigTempDegree",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivDegreeWidth1,ivDegreeHeight1);
        layoutParams.gravity = Gravity.TOP;
        v.mBigTempDegree.setLayoutParams(layoutParams);

		v.mDate = (TextView) LayouUtil.findViewByName("date",v.layout);
        layoutParams = new LinearLayout.LayoutParams(0,tvTimeHeight,1);
        layoutParams.gravity = Gravity.RIGHT;
        layoutParams.rightMargin = todayRightMargin;
        v.mDate.setLayoutParams(layoutParams);
		v.mDay = (TextView) LayouUtil.findViewByName("day",v.layout);
		v.mWeather = (TextView) LayouUtil.findViewByName("weather",v.layout);
		v.mWind = (TextView) LayouUtil.findViewByName("wind",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight1);
        layoutParams.leftMargin = todayLeftMargin;
        v.mWind.setLayoutParams(layoutParams);

		//v.mAirQualityText = (TextView) LayouUtil.findViewByName("airQualityText",v.layout);
		v.mAirQuality = (TextView) LayouUtil.findViewByName("airQuality",v.layout);
        v.mAirQuality.setPadding(tvAirHorMargin,0,tvAirHorMargin,0);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = tvWeatherTopMargin;
        v.mAirQuality.setLayoutParams(layoutParams);
		//v.mAirDegree = (TextView) LayouUtil.findViewByName("airDegree",v.layout);
		v.mCity = (TextView) LayouUtil.findViewByName("city",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        layoutParams.gravity = Gravity.LEFT;
        layoutParams.leftMargin = todayRightMargin;
        v.mCity.setLayoutParams(layoutParams);

//		today = (LinearLayout) LayouUtil.findViewByName("today",v.layout);
		//today_title = (TextView) LayouUtil.findViewByName("today_title",v.layout);
		v.mTodayWeather = (ImageView) LayouUtil.findViewByName("today_image",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide1,ivWeatherSide1);
        layoutParams.gravity = Gravity.CENTER;
        //layoutParams.topMargin = tvTempRangeTopMargin;
        layoutParams.rightMargin = todayLeftMargin;
        v.mTodayWeather.setLayoutParams(layoutParams);
		v.mWeather = (TextView) LayouUtil.findViewByName("today_weather",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mWeather.setLayoutParams(layoutParams);
		v.mTempRange = (TextView) LayouUtil.findViewByName("today_tem",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight1);
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.gravity = Gravity.CENTER;
        v.mTempRange.setLayoutParams(layoutParams);

		llContent.setBackground(LayouUtil.getDrawable("weather_background"));
//
        LinearLayout llTextCount =  (LinearLayout) LayouUtil.findViewByName("textCount",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = todayRightMargin;
        llTextCount.setLayoutParams(layoutParams);

		TextViewUtil.setTextSize(v.mCity,tvTimeSize);
		TextViewUtil.setTextColor(v.mCity,tvTimeColor);
		TextViewUtil.setTextSize(v.mDate,tvTimeSize);
		TextViewUtil.setTextColor(v.mDate,tvTimeColor);

		TextViewUtil.setTextSize(v.mWeather,tvWeatherSize1);
		TextViewUtil.setTextColor(v.mWeather,tvWeatherColor);
		TextViewUtil.setTextSize(v.mTempRange,tvWeatherSize1);
		TextViewUtil.setTextColor(v.mTempRange,tvWeatherColor);
		TextViewUtil.setTextSize(v.mWind,tvWeatherSize1);
		TextViewUtil.setTextColor(v.mWind,tvWeatherColor);

		TextViewUtil.setTextSize(v.mAirQuality,tvAirSize);
		TextViewUtil.setTextColor(v.mAirQuality,tvWeatherColor);

		WeatherRefresher2.getInstance().updateDataOneday(viewData.textContent, v);

		//return v.layout;
		return llLayout;
	}

    private View createWeatherViewHalf(ChatWeatherViewData viewData){
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setOrientation(LinearLayout.VERTICAL);
        llLayout.setGravity(Gravity.CENTER_VERTICAL);

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null,"weather","天气");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

        WeatherViewHolder v = new WeatherViewHolder();
        v.layout = (BoundedLinearLayout) LayouUtil.getView("chat_weather_view2");
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,SizeConfig.itemHeight * SizeConfig.pageCount);
        //layoutParams = new LinearLayout.LayoutParams(WinLayout.isVertScreen?LayoutParams.MATCH_PARENT:(int)(SizeConfig.itemHeight * SizeConfig.pageCount * 1.8),contentHeight);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,contentHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(v.layout,layoutParams);

        llContent = (LinearLayout) LayouUtil.findViewByName("llContent",v.layout);
        v.mCurrentTemp = (LinearLayout) LayouUtil.findViewByName("current_temp", v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        //layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = ivWeatherTopMargin;
        v.mCurrentTemp.setLayoutParams(layoutParams);
        //v.mBigTempRange = (LinearLayout) LayouUtil.findViewByName("bigTempRange",v.layout);
        v.mMinus = (ImageView) LayouUtil.findViewByName("minus",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth,ivMinusHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        v.mMinus.setLayoutParams(layoutParams);
        v.mTempDecade = (ImageView) LayouUtil.findViewByName("tempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth,ivTempHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        v.mTempDecade.setLayoutParams(layoutParams);
        v.mTempUnits = (ImageView) LayouUtil.findViewByName("tempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth,ivTempHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        v.mTempUnits.setLayoutParams(layoutParams);
        v.mTempDegree = (ImageView) LayouUtil.findViewByName("tempDegree",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivDegreeWidth,ivDegreeHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        v.mTempDegree.setLayoutParams(layoutParams);
        //v.mTempRange = (TextView) LayouUtil.findViewByName("tempRange",v.layout);
        //v.mTodayWeather = (ImageView) LayouUtil.findViewByName("todayWeather",v.layout);

		/*v.mBigLowMinus = (ImageView) LayouUtil.findViewByName("bigLowMinus",v.layout);
		v.mBigLowTempDecade = (ImageView) LayouUtil.findViewByName("bigLowTempDecade",v.layout);
		v.mBigLowTempUnits = (ImageView) LayouUtil.findViewByName("bigLowTempUnits",v.layout);
		v.mBigHighMinus = (ImageView) LayouUtil.findViewByName("bigHighMinus",v.layout);
		v.mBigHighTempDecade = (ImageView) LayouUtil.findViewByName("bigHighTempDecade",v.layout);
		v.mBigHighTempUnits = (ImageView) LayouUtil.findViewByName("bigHighTempUnits",v.layout);
		v.mBigTempDegree = (ImageView) LayouUtil.findViewByName("bigTempDegree",v.layout);
		v.mBigSlash = (ImageView) LayouUtil.findViewByName("bigSlash", v.layout);*/

        //v.mDate = (TextView) LayouUtil.findViewByName("date",v.layout);
		/*v.mDay = (TextView) LayouUtil.findViewByName("day",v.layout);
		v.mWeather = (TextView) LayouUtil.findViewByName("weather",v.layout);*/
        v.mWind = (TextView) LayouUtil.findViewByName("wind",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        //layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = tvWeatherTopMargin;
        v.mWind.setLayoutParams(layoutParams);

        //v.mAirQualityText = (TextView) LayouUtil.findViewByName("airQualityText",v.layout);
        v.mAirQuality = (TextView) LayouUtil.findViewByName("airQuality",v.layout);
        v.mAirQuality.setSingleLine(true);
        v.mAirQuality.setEllipsize(TextUtils.TruncateAt.END);
        //v.mAirQuality.setPadding(tvAirHorMargin,0,tvAirHorMargin,0);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        //layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = tvTempRangeTopMargin;
        v.mAirQuality.setLayoutParams(layoutParams);
        //v.mAirDegree = (TextView) LayouUtil.findViewByName("airDegree",v.layout);

        LinearLayout llTop = (LinearLayout) LayouUtil.findViewByName("llTop",v.layout);

        //v.mCity = (TextView) LayouUtil.findViewByName("city",v.layout);
        v.mCity = (TextView) LayouUtil.findViewByName("city",llTop);
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        layoutParams = (LinearLayout.LayoutParams) v.mCity.getLayoutParams();
        layoutParams.height = tvTimeHeight;
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        v.mCity.setLayoutParams(layoutParams);

		/*v.mToday = (IconTextView) LayouUtil.findViewByName("today",v.layout);
		v.mTomorrow = (IconTextView) LayouUtil.findViewByName("tomorrow",v.layout);
		v.mTheDayAfterTomorrow = (IconTextView) LayouUtil.findViewByName("theDayAfterTomorrow",v.layout);
		v.mToday.init();
		v.mTomorrow.init();
		v.mTheDayAfterTomorrow.init();*/

        //llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
        llContent.setBackground(LayouUtil.getDrawable("weather_background"));


		/*today = (LinearLayout) LayouUtil.findViewByName("today",v.layout);
        layoutParams = (LinearLayout.LayoutParams)today.getLayoutParams();
        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        today.setLayoutParams(layoutParams);*/
        //today_title = (TextView) LayouUtil.findViewByName("today_title",v.layout);
        today_title = (TextView) LayouUtil.findViewByName("today_title",llTop);
        today_title.setGravity(Gravity.RIGHT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.rightMargin = todayRightMargin;
        today_title.setLayoutParams(layoutParams);
        today_image = (ImageView) LayouUtil.findViewByName("today_image",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,ivWeatherSide);
        today_image.setScaleType(ImageView.ScaleType.FIT_END);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.rightMargin = todayRightMargin;
        today_image.setLayoutParams(layoutParams);
        today_weather = (TextView) LayouUtil.findViewByName("today_weather",v.layout);
        today_weather.setGravity(Gravity.RIGHT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMargin;
        layoutParams.rightMargin = todayRightMargin;
        today_weather.setLayoutParams(layoutParams);
        today_tem = (TextView) LayouUtil.findViewByName("today_tem",v.layout);
        today_tem.setGravity(Gravity.RIGHT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,tvTempRangeHeight);
        layoutParams.topMargin = tvTempRangeTopMargin;
        layoutParams.rightMargin = todayRightMargin;
        today_tem.setLayoutParams(layoutParams);

        tomorrow = (LinearLayout) LayouUtil.findViewByName("tomorrow",v.layout);
        layoutParams = (LinearLayout.LayoutParams)tomorrow.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        tomorrow.setLayoutParams(layoutParams);
        tomorrow_title = (TextView) LayouUtil.findViewByName("tomorrow_title",tomorrow);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        tomorrow_title.setLayoutParams(layoutParams);
        tomorrow_image = (ImageView) LayouUtil.findViewByName("tomorrow_image",tomorrow);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.bottomMargin = tvWeatherTopMargin;
        tomorrow_image.setLayoutParams(layoutParams);
        tomorrow_weather = (TextView) LayouUtil.findViewByName("tomorrow_weather",tomorrow);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        //layoutParams.topMargin = tvWeatherTopMargin;
        //tomorrow_weather.setLayoutParams(layoutParams);
        tomorrow_tem = (TextView) LayouUtil.findViewByName("tomorrow_tem",tomorrow);
        layoutParams.topMargin = tvTempRangeTopMargin;
        tomorrow_tem.setLayoutParams(layoutParams);

        dayft = (LinearLayout) LayouUtil.findViewByName("dayft",v.layout);
        layoutParams = (LinearLayout.LayoutParams)dayft.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        dayft.setLayoutParams(layoutParams);
        dayft_title = (TextView) LayouUtil.findViewByName("dayft_title",dayft);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        dayft_title.setLayoutParams(layoutParams);
        dayft_image = (ImageView) LayouUtil.findViewByName("dayft_image",dayft);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.bottomMargin = tvWeatherTopMargin;
        dayft_image.setLayoutParams(layoutParams);
        dayft_weather = (TextView) LayouUtil.findViewByName("dayft_weather",dayft);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMargin;
        //dayft_weather.setLayoutParams(layoutParams);
        dayft_tem = (TextView) LayouUtil.findViewByName("dayft_tem",dayft);
        layoutParams.topMargin = tvTempRangeTopMargin;
        dayft_tem.setLayoutParams(layoutParams);

        dividerMore = (View) LayouUtil.findViewByName("dividerMore",v.layout);

        daymore = (LinearLayout) LayouUtil.findViewByName("daymore",v.layout);
        layoutParams = (LinearLayout.LayoutParams)daymore.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        daymore.setLayoutParams(layoutParams);
        daymore_title = (TextView) LayouUtil.findViewByName("daymore_title",daymore);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        daymore_title.setLayoutParams(layoutParams);
        daymore_image = (ImageView) LayouUtil.findViewByName("daymore_image",daymore);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.bottomMargin = tvWeatherTopMargin;
        daymore_image.setLayoutParams(layoutParams);
        daymore_weather = (TextView) LayouUtil.findViewByName("daymore_weather",daymore);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMargin;
        //dayft_weather.setLayoutParams(layoutParams);
        daymore_tem = (TextView) LayouUtil.findViewByName("daymore_tem",daymore);
        layoutParams.topMargin = tvTempRangeTopMargin;
        daymore_tem.setLayoutParams(layoutParams);

        dividerLast = (View) LayouUtil.findViewByName("dividerLast",v.layout);

        daylast = (LinearLayout) LayouUtil.findViewByName("daylast",v.layout);
        layoutParams = (LinearLayout.LayoutParams)daylast.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        daylast.setLayoutParams(layoutParams);
        daylast_title = (TextView) LayouUtil.findViewByName("daylast_title",daylast);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        daylast_title.setLayoutParams(layoutParams);
        daylast_image = (ImageView) LayouUtil.findViewByName("daylast_image",daylast);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMargin;
        layoutParams.bottomMargin = tvWeatherTopMargin;
        daylast_image.setLayoutParams(layoutParams);
        daylast_weather = (TextView) LayouUtil.findViewByName("daylast_weather",daylast);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMargin;
        //dayft_weather.setLayoutParams(layoutParams);
        daylast_tem = (TextView) LayouUtil.findViewByName("daylast_tem",daylast);
        layoutParams.topMargin = tvTempRangeTopMargin;
        daylast_tem.setLayoutParams(layoutParams);

        v.mMinus.setImageDrawable(LayouUtil.getDrawable("weather_number_minus"));

        TextViewUtil.setTextSize(v.mCity,tvTimeSize);
        TextViewUtil.setTextColor(v.mCity,tvTimeColor);
        TextViewUtil.setTextSize(v.mWind,tvWeatherSize);
        TextViewUtil.setTextColor(v.mWind,tvWeatherColor);
        TextViewUtil.setTextSize(v.mAirQuality,tvAirSize);
        TextViewUtil.setTextColor(v.mAirQuality,tvTempRangeColor);

        TextViewUtil.setTextSize(today_title,tvTimeSize);
        TextViewUtil.setTextColor(today_title,tvTimeColor);
        TextViewUtil.setTextSize(today_weather,tvWeatherSize);
        TextViewUtil.setTextColor(today_weather,tvWeatherColor);
        TextViewUtil.setTextSize(today_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(today_tem,tvTempRangeColor);

        TextViewUtil.setTextSize(tomorrow_title,tvTimeSize);
        TextViewUtil.setTextColor(tomorrow_title,tvTimeColor);
        TextViewUtil.setTextSize(tomorrow_weather,tvWeatherSize);
        TextViewUtil.setTextColor(tomorrow_weather,tvWeatherColor);
        TextViewUtil.setTextSize(tomorrow_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(tomorrow_tem,tvTempRangeColor);

        TextViewUtil.setTextSize(dayft_title,tvTimeSize);
        TextViewUtil.setTextColor(dayft_title,tvTimeColor);
        TextViewUtil.setTextSize(dayft_weather,tvWeatherSize);
        TextViewUtil.setTextColor(dayft_weather,tvWeatherColor);
        TextViewUtil.setTextSize(dayft_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(dayft_tem,tvTempRangeColor);

        TextViewUtil.setTextSize(daymore_title,tvTimeSize);
        TextViewUtil.setTextColor(daymore_title,tvTimeColor);
        TextViewUtil.setTextSize(daymore_weather,tvWeatherSize);
        TextViewUtil.setTextColor(daymore_weather,tvWeatherColor);
        TextViewUtil.setTextSize(daymore_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(daymore_tem,tvTempRangeColor);

        TextViewUtil.setTextSize(daylast_title,tvTimeSize);
        TextViewUtil.setTextColor(daylast_title,tvTimeColor);
        TextViewUtil.setTextSize(daylast_weather,tvWeatherSize);
        TextViewUtil.setTextColor(daylast_weather,tvWeatherColor);
        TextViewUtil.setTextSize(daylast_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(daylast_tem,tvTempRangeColor);

        if (showDayCountHalf == 5){
            dividerMore.setVisibility(View.VISIBLE);
            daymore.setVisibility(View.VISIBLE);
            dividerLast.setVisibility(View.VISIBLE);
            daylast.setVisibility(View.VISIBLE);
        }else if (showDayCountHalf == 4){
            dividerMore.setVisibility(View.VISIBLE);
            daymore.setVisibility(View.VISIBLE);
            dividerLast.setVisibility(View.GONE);
            daylast.setVisibility(View.GONE);
        }else {
            dividerMore.setVisibility(View.GONE);
            daymore.setVisibility(View.GONE);
            dividerLast.setVisibility(View.GONE);
            daylast.setVisibility(View.GONE);
        }

        WeatherRefresher2.getInstance().updateData(viewData.textContent, v);

        //return v.layout;
        return llLayout;
    }

    private View createWeatherViewHalfOneday(ChatWeatherViewData viewData){
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setOrientation(LinearLayout.VERTICAL);
        llLayout.setGravity(Gravity.CENTER_VERTICAL);

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null,"weather","天气");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

        WeatherViewHolder v = new WeatherViewHolder();
        v.layout = (BoundedLinearLayout) LayouUtil.getView("chat_weather_view_oneday");
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,SizeConfig.itemHeight * SizeConfig.pageCount);
        //layoutParams = new LinearLayout.LayoutParams(WinLayout.isVertScreen?LayoutParams.MATCH_PARENT:(int)(SizeConfig.itemHeight * SizeConfig.pageCount * 1.8),contentHeight);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,contentHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(v.layout,layoutParams);

        llContent = (LinearLayout) LayouUtil.findViewByName("llContent",v.layout);
        LinearLayout temp = (LinearLayout) LayouUtil.findViewByName("temp",v.layout);
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTempRangeTopMargin + ivWeatherSide);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = tvTempRangeTopMargin;
        temp.setLayoutParams(layoutParams);
        v.mCurrentTemp = (LinearLayout) LayouUtil.findViewByName("current_temp", v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        //layoutParams.topMargin = tvTempRangeTopMargin;
        v.mCurrentTemp.setLayoutParams(layoutParams);
        v.mBigTempRange = (LinearLayout) LayouUtil.findViewByName("bigTempRange",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        //layoutParams.topMargin = tvTempRangeTopMargin;
        v.mBigTempRange.setLayoutParams(layoutParams);
        v.mMinus = (ImageView) LayouUtil.findViewByName("minus",v.layout);
        v.mMinus.setImageDrawable(LayouUtil.getDrawable("weather_number_minus"));
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mMinus.setLayoutParams(layoutParams);
        v.mTempDecade = (ImageView) LayouUtil.findViewByName("tempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mTempDecade.setLayoutParams(layoutParams);
        v.mTempUnits = (ImageView) LayouUtil.findViewByName("tempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mTempUnits.setLayoutParams(layoutParams);
        v.mTempDegree = (ImageView) LayouUtil.findViewByName("tempDegree",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivDegreeWidth1,ivDegreeHeight1);
        layoutParams.gravity = Gravity.TOP;
        v.mTempDegree.setLayoutParams(layoutParams);
        //v.mTempRange = (TextView) LayouUtil.findViewByName("tempRange",v.layout);
        //v.mTodayWeather = (ImageView) LayouUtil.findViewByName("todayWeather",v.layout);

        v.mBigLowMinus = (ImageView) LayouUtil.findViewByName("bigLowMinus",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigLowMinus.setLayoutParams(layoutParams);
        v.mBigLowTempDecade = (ImageView) LayouUtil.findViewByName("bigLowTempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigLowTempDecade.setLayoutParams(layoutParams);
        v.mBigLowTempUnits = (ImageView) LayouUtil.findViewByName("bigLowTempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigLowTempUnits.setLayoutParams(layoutParams);
        v.mBigSlash = (ImageView) LayouUtil.findViewByName("bigSlash", v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigSlash.setLayoutParams(layoutParams);
        v.mBigHighMinus = (ImageView) LayouUtil.findViewByName("bigHighMinus",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigHighMinus.setLayoutParams(layoutParams);
        v.mBigHighTempDecade = (ImageView) LayouUtil.findViewByName("bigHighTempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigHighTempDecade.setLayoutParams(layoutParams);
        v.mBigHighTempUnits = (ImageView) LayouUtil.findViewByName("bigHighTempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigHighTempUnits.setLayoutParams(layoutParams);
        v.mBigTempDegree = (ImageView) LayouUtil.findViewByName("bigTempDegree",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivDegreeWidth1,ivDegreeHeight1);
        layoutParams.gravity = Gravity.TOP;
        v.mBigTempDegree.setLayoutParams(layoutParams);

        v.mDate = (TextView) LayouUtil.findViewByName("date",v.layout);
        layoutParams = new LinearLayout.LayoutParams(0,tvTimeHeight,1);
        layoutParams.gravity = Gravity.RIGHT;
        layoutParams.rightMargin = todayRightMargin;
        v.mDate.setLayoutParams(layoutParams);
        v.mDay = (TextView) LayouUtil.findViewByName("day",v.layout);
        v.mWeather = (TextView) LayouUtil.findViewByName("weather",v.layout);
        v.mWind = (TextView) LayouUtil.findViewByName("wind",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight1);
        layoutParams.leftMargin = todayLeftMargin;
        v.mWind.setLayoutParams(layoutParams);

        //v.mAirQualityText = (TextView) LayouUtil.findViewByName("airQualityText",v.layout);
        v.mAirQuality = (TextView) LayouUtil.findViewByName("airQuality",v.layout);
        v.mAirQuality.setPadding(tvAirHorMargin,0,tvAirHorMargin,0);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = tvWeatherTopMarginNone;
        v.mAirQuality.setLayoutParams(layoutParams);
        //v.mAirDegree = (TextView) LayouUtil.findViewByName("airDegree",v.layout);
        v.mCity = (TextView) LayouUtil.findViewByName("city",v.layout);
        layoutParams = new LinearLayout.LayoutParams(0,tvTimeHeight,1);
        layoutParams.gravity = Gravity.LEFT;
        layoutParams.leftMargin = todayRightMargin;
        v.mCity.setLayoutParams(layoutParams);

//		today = (LinearLayout) LayouUtil.findViewByName("today",v.layout);
        //today_title = (TextView) LayouUtil.findViewByName("today_title",v.layout);
        v.mTodayWeather = (ImageView) LayouUtil.findViewByName("today_image",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide1,ivWeatherSide1);
        layoutParams.gravity = Gravity.CENTER;
        //layoutParams.topMargin = tvTempRangeTopMargin;
        layoutParams.rightMargin = todayLeftMargin;
        v.mTodayWeather.setLayoutParams(layoutParams);
        v.mWeather = (TextView) LayouUtil.findViewByName("today_weather",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mWeather.setLayoutParams(layoutParams);
        v.mTempRange = (TextView) LayouUtil.findViewByName("today_tem",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight1);
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.gravity = Gravity.CENTER;
        v.mTempRange.setLayoutParams(layoutParams);

        llContent.setBackground(LayouUtil.getDrawable("weather_background"));
//
        LinearLayout llTextCount =  (LinearLayout) LayouUtil.findViewByName("textCount",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = todayRightMargin;
        llTextCount.setLayoutParams(layoutParams);

        TextViewUtil.setTextSize(v.mCity,tvTimeSize);
        TextViewUtil.setTextColor(v.mCity,tvTimeColor);
        TextViewUtil.setTextSize(v.mDate,tvTimeSize);
        TextViewUtil.setTextColor(v.mDate,tvTimeColor);

        TextViewUtil.setTextSize(v.mWeather,tvWeatherSize1);
        TextViewUtil.setTextColor(v.mWeather,tvWeatherColor);
        TextViewUtil.setTextSize(v.mTempRange,tvWeatherSize1);
        TextViewUtil.setTextColor(v.mTempRange,tvWeatherColor);
        TextViewUtil.setTextSize(v.mWind,tvWeatherSize1);
        TextViewUtil.setTextColor(v.mWind,tvWeatherColor);

        TextViewUtil.setTextSize(v.mAirQuality,tvAirSize);
        TextViewUtil.setTextColor(v.mAirQuality,tvWeatherColor);

        WeatherRefresher2.getInstance().updateDataOneday(viewData.textContent, v);

        //return v.layout;
        return llLayout;
    }

    private View createWeatherViewNone(ChatWeatherViewData viewData){
        WeatherViewHolder v = new WeatherViewHolder();
        v.layout = (BoundedLinearLayout) LayouUtil.getView("chat_weather_view2");

        llContent = (LinearLayout) LayouUtil.findViewByName("llContent",v.layout);
        v.mCurrentTemp = (LinearLayout) LayouUtil.findViewByName("current_temp", v.layout);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        //layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = ivWeatherTopMarginNone;
        v.mCurrentTemp.setLayoutParams(layoutParams);
        //v.mBigTempRange = (LinearLayout) LayouUtil.findViewByName("bigTempRange",v.layout);
        v.mMinus = (ImageView) LayouUtil.findViewByName("minus",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth,ivMinusHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        v.mMinus.setLayoutParams(layoutParams);
        v.mTempDecade = (ImageView) LayouUtil.findViewByName("tempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth,ivTempHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        v.mTempDecade.setLayoutParams(layoutParams);
        v.mTempUnits = (ImageView) LayouUtil.findViewByName("tempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth,ivTempHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        v.mTempUnits.setLayoutParams(layoutParams);
        v.mTempDegree = (ImageView) LayouUtil.findViewByName("tempDegree",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivDegreeWidth,ivDegreeHeight);
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        v.mTempDegree.setLayoutParams(layoutParams);
        //v.mTempRange = (TextView) LayouUtil.findViewByName("tempRange",v.layout);
        //v.mTodayWeather = (ImageView) LayouUtil.findViewByName("todayWeather",v.layout);

		/*v.mBigLowMinus = (ImageView) LayouUtil.findViewByName("bigLowMinus",v.layout);
		v.mBigLowTempDecade = (ImageView) LayouUtil.findViewByName("bigLowTempDecade",v.layout);
		v.mBigLowTempUnits = (ImageView) LayouUtil.findViewByName("bigLowTempUnits",v.layout);
		v.mBigHighMinus = (ImageView) LayouUtil.findViewByName("bigHighMinus",v.layout);
		v.mBigHighTempDecade = (ImageView) LayouUtil.findViewByName("bigHighTempDecade",v.layout);
		v.mBigHighTempUnits = (ImageView) LayouUtil.findViewByName("bigHighTempUnits",v.layout);
		v.mBigTempDegree = (ImageView) LayouUtil.findViewByName("bigTempDegree",v.layout);
		v.mBigSlash = (ImageView) LayouUtil.findViewByName("bigSlash", v.layout);*/

        //v.mDate = (TextView) LayouUtil.findViewByName("date",v.layout);
		/*v.mDay = (TextView) LayouUtil.findViewByName("day",v.layout);
		v.mWeather = (TextView) LayouUtil.findViewByName("weather",v.layout);*/
        v.mWind = (TextView) LayouUtil.findViewByName("wind",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        //layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = tvWeatherTopMarginNone;
        v.mWind.setLayoutParams(layoutParams);

        //v.mAirQualityText = (TextView) LayouUtil.findViewByName("airQualityText",v.layout);
        v.mAirQuality = (TextView) LayouUtil.findViewByName("airQuality",v.layout);
        v.mAirQuality.setSingleLine(true);
        v.mAirQuality.setEllipsize(TextUtils.TruncateAt.END);
        //v.mAirQuality.setPadding(tvAirHorMargin,0,tvAirHorMargin,0);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        //layoutParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.topMargin = tvTempRangeTopMarginNone;
        v.mAirQuality.setLayoutParams(layoutParams);
        //v.mAirDegree = (TextView) LayouUtil.findViewByName("airDegree",v.layout);

        LinearLayout llTop = (LinearLayout) LayouUtil.findViewByName("llTop",v.layout);

        //v.mCity = (TextView) LayouUtil.findViewByName("city",v.layout);
        v.mCity = (TextView) LayouUtil.findViewByName("city",llTop);
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        layoutParams = (LinearLayout.LayoutParams) v.mCity.getLayoutParams();
        layoutParams.height = tvTimeHeight;
        layoutParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = todayLeftMargin;
        v.mCity.setLayoutParams(layoutParams);

		/*v.mToday = (IconTextView) LayouUtil.findViewByName("today",v.layout);
		v.mTomorrow = (IconTextView) LayouUtil.findViewByName("tomorrow",v.layout);
		v.mTheDayAfterTomorrow = (IconTextView) LayouUtil.findViewByName("theDayAfterTomorrow",v.layout);
		v.mToday.init();
		v.mTomorrow.init();
		v.mTheDayAfterTomorrow.init();*/

        //llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
        llContent.setBackground(LayouUtil.getDrawable("weather_background"));


		/*today = (LinearLayout) LayouUtil.findViewByName("today",v.layout);
        layoutParams = (LinearLayout.LayoutParams)today.getLayoutParams();
        layoutParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        today.setLayoutParams(layoutParams);*/
        //today_title = (TextView) LayouUtil.findViewByName("today_title",v.layout);
        today_title = (TextView) LayouUtil.findViewByName("today_title",llTop);
        today_title.setGravity(Gravity.RIGHT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.rightMargin = todayRightMargin;
        today_title.setLayoutParams(layoutParams);
        today_image = (ImageView) LayouUtil.findViewByName("today_image",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,ivWeatherSide);
        today_image.setScaleType(ImageView.ScaleType.FIT_END);
        layoutParams.topMargin = ivWeatherTopMarginNone;
        layoutParams.rightMargin = todayRightMargin;
        today_image.setLayoutParams(layoutParams);
        today_weather = (TextView) LayouUtil.findViewByName("today_weather",v.layout);
        today_weather.setGravity(Gravity.RIGHT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMarginNone;
        layoutParams.rightMargin = todayRightMargin;
        today_weather.setLayoutParams(layoutParams);
        today_tem = (TextView) LayouUtil.findViewByName("today_tem",v.layout);
        today_tem.setGravity(Gravity.RIGHT);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,tvTempRangeHeight);
        layoutParams.topMargin = tvTempRangeTopMarginNone;
        layoutParams.rightMargin = todayRightMargin;
        today_tem.setLayoutParams(layoutParams);

        tomorrow = (LinearLayout) LayouUtil.findViewByName("tomorrow",v.layout);
        layoutParams = (LinearLayout.LayoutParams)tomorrow.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        tomorrow.setLayoutParams(layoutParams);
        tomorrow_title = (TextView) LayouUtil.findViewByName("tomorrow_title",tomorrow);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        tomorrow_title.setLayoutParams(layoutParams);
        tomorrow_image = (ImageView) LayouUtil.findViewByName("tomorrow_image",tomorrow);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMarginNone;
        layoutParams.bottomMargin = tvWeatherTopMarginNone;
        tomorrow_image.setLayoutParams(layoutParams);
        tomorrow_weather = (TextView) LayouUtil.findViewByName("tomorrow_weather",tomorrow);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        //layoutParams.topMargin = tvWeatherTopMargin;
        //tomorrow_weather.setLayoutParams(layoutParams);
        tomorrow_tem = (TextView) LayouUtil.findViewByName("tomorrow_tem",tomorrow);
        layoutParams.topMargin = tvTempRangeTopMarginNone;
        tomorrow_tem.setLayoutParams(layoutParams);

        dayft = (LinearLayout) LayouUtil.findViewByName("dayft",v.layout);
        layoutParams = (LinearLayout.LayoutParams)dayft.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        dayft.setLayoutParams(layoutParams);
        dayft_title = (TextView) LayouUtil.findViewByName("dayft_title",dayft);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTimeHeight);
        dayft_title.setLayoutParams(layoutParams);
        dayft_image = (ImageView) LayouUtil.findViewByName("dayft_image",dayft);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide,ivWeatherSide);
        layoutParams.topMargin = ivWeatherTopMarginNone;
        layoutParams.bottomMargin = tvWeatherTopMarginNone;
        dayft_image.setLayoutParams(layoutParams);
        dayft_weather = (TextView) LayouUtil.findViewByName("dayft_weather",dayft);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight);
        layoutParams.topMargin = tvWeatherTopMarginNone;
        //dayft_weather.setLayoutParams(layoutParams);
        dayft_tem = (TextView) LayouUtil.findViewByName("dayft_tem",dayft);
        layoutParams.topMargin = tvTempRangeTopMarginNone;
        dayft_tem.setLayoutParams(layoutParams);

        v.mMinus.setImageDrawable(LayouUtil.getDrawable("weather_number_minus"));

		/*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llTittle.setLayoutParams(layoutParams);
		tittle_icon.setImageDrawable(LayouUtil.getDrawable("title_icon_weather"));
		tittle_text.setText("天气");*/

		/*LinearLayout.LayoutParams mRelativeLayoutParams = (android.widget.LinearLayout.LayoutParams) v.mCity.getLayoutParams();
		mRelativeLayoutParams.leftMargin = cityMarginLeft;
		v.mCity.setLayoutParams(mRelativeLayoutParams);
		*//*mRelativeLayoutParams = (android.widget.LinearLayout.LayoutParams) v.mDate.getLayoutParams();
		mRelativeLayoutParams.leftMargin = dateMarginLeft;
		mRelativeLayoutParams.rightMargin = dateMarginRight;
		v.mDate.setLayoutParams(mRelativeLayoutParams);*//*
		LinearLayout llContent = (LinearLayout) LayouUtil.findViewByName("llContent", v.layout);
		LinearLayout.LayoutParams mLinearLayoutParams = (LayoutParams) llContent.getLayoutParams();
		mLinearLayoutParams.topMargin = llContentMarginTop;
		llContent.setLayoutParams(mLinearLayoutParams);
		llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));

		LinearLayout mLleft = (LinearLayout) LayouUtil.findViewByName("llLeft", v.layout);
		mLinearLayoutParams = (LayoutParams) mLleft.getLayoutParams();
		mLinearLayoutParams.leftMargin = mRlTopMarginLeft;
		mLinearLayoutParams.rightMargin = mRlTopMarginRight;
		mLleft.setLayoutParams(mLinearLayoutParams);
		mLleft.setPadding(0, mRlTopPaddingTop, 0, 0);

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

		*//*mLinearLayoutParams = (LayoutParams) v.mBigLowMinus.getLayoutParams();
		mLinearLayoutParams.width = mBigLowMinusWidth;
		mLinearLayoutParams.height = mBigLowMinusHeight;
		v.mBigLowMinus.setLayoutParams(mLinearLayoutParams);*//*

         *//*mLinearLayoutParams = (LayoutParams) v.mBigLowTempDecade.getLayoutParams();
		mLinearLayoutParams.width = mBigLowTempDecadeWidth;
		mLinearLayoutParams.height = mBigLowTempDecadeHeight;
		v.mBigLowTempDecade.setLayoutParams(mLinearLayoutParams);*//*

         *//*mLinearLayoutParams = (LayoutParams) v.mBigLowTempUnits.getLayoutParams();
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
		v.mBigTempDegree.setLayoutParams(mLinearLayoutParams);*//*

         *//*LinearLayout tempRangeParent = (LinearLayout) v.mTempRange.getParent();
		mLinearLayoutParams = (android.widget.LinearLayout.LayoutParams) tempRangeParent.getLayoutParams();
		mLinearLayoutParams.topMargin = tempRangeParentMarginTop;
		tempRangeParent.setLayoutParams(mLinearLayoutParams);*//*

         *//*mLinearLayoutParams = (LayoutParams) v.mTodayWeather.getLayoutParams();
		mLinearLayoutParams.width = mTodayWeatherWidth;
		mLinearLayoutParams.height = mTodayWeatherHeight;
		v.mTodayWeather.setLayoutParams(mLinearLayoutParams);
		LinearLayout todayWeatherParent = (LinearLayout) v.mTodayWeather.getParent();
		mLinearLayoutParams = (android.widget.LinearLayout.LayoutParams) todayWeatherParent.getLayoutParams();
		mLinearLayoutParams.rightMargin = todayWeatherParentMarginRight;
		mLinearLayoutParams.topMargin = todayWeatherParentMarginTop;
		todayWeatherParent.setLayoutParams(mLinearLayoutParams);*//*

		LinearLayout llAir = (LinearLayout) LayouUtil.findViewByName("llAir", v.layout);
		*//*mLinearLayoutParams = (LayoutParams) llAir.getLayoutParams();
		mLinearLayoutParams.leftMargin = llAirMarginLeft;
		mLinearLayoutParams.rightMargin = llAirMarginRight;
		mLinearLayoutParams.topMargin = llAirMarginTop;*//*
		mLinearLayoutParams = (LinearLayout.LayoutParams) llAir.getLayoutParams();
		mLinearLayoutParams.leftMargin = llAirMarginLeft;
		mLinearLayoutParams.rightMargin = llAirMarginRight;
		mLinearLayoutParams.topMargin = llAirMarginTop;
		llAir.setLayoutParams(mLinearLayoutParams);

		mLinearLayoutParams = (android.widget.LinearLayout.LayoutParams) v.mWind.getLayoutParams();
		mLinearLayoutParams.leftMargin = mWindMarginLeft;
		v.mWind.setLayoutParams(mLinearLayoutParams);

		*//*mLinearLayoutParams = (android.widget.LinearLayout.LayoutParams) v.mAirDegree.getLayoutParams();
		mRelativeLayoutParams.leftMargin = mAirDegreeMarginLeft;
		v.mAirDegree.setLayoutParams(mRelativeLayoutParams);*//*

		mLinearLayoutParams = (android.widget.LinearLayout.LayoutParams) v.mAirQuality.getLayoutParams();
		mLinearLayoutParams.leftMargin = mAirQualityMarginLeft;
		v.mAirQuality.setLayoutParams(mLinearLayoutParams);

		LinearLayout llWeather = (LinearLayout) LayouUtil.findViewByName("llWeather", v.layout);
		mLinearLayoutParams = (LayoutParams) llWeather.getLayoutParams();
		mLinearLayoutParams.topMargin = llWeatherTopMargin;
		llWeather.setLayoutParams(mLinearLayoutParams);

		TextViewUtil.setTextSize(v.mCity,mCitySize);
		TextViewUtil.setTextColor(v.mCity,mCityColor);
		*//*TextViewUtil.setTextSize(v.mDate,mDateSize);
		TextViewUtil.setTextColor(v.mDate,mDateColor);*//*
         *//*TextViewUtil.setTextSize(v.mWeather,mWeatherSize);
		TextViewUtil.setTextColor(v.mWeather,mWeatherColor);*//*
		TextViewUtil.setTextSize(v.mWind,mWindSize);
		TextViewUtil.setTextColor(v.mWind,mWindColor);
		*//*TextViewUtil.setTextSize(v.mTempRange,mTempRangeSize);
		TextViewUtil.setTextColor(v.mTempRange,mTempRangeColor);*//*

         *//*TextViewUtil.setTextSize(v.mAirQualityText,mAirQualityTextSize);
		TextViewUtil.setTextColor(v.mAirQualityText,mAirQualityTextColor);*//*
		TextViewUtil.setTextSize(v.mAirQuality,mAirQualitySize);
		TextViewUtil.setTextColor(v.mAirQuality,mAirQualityColor);
		*//*TextViewUtil.setTextSize(v.mAirDegree,mAirDegreeSize);
		TextViewUtil.setTextColor(v.mAirDegree,mAirDegreeColor);*//*

         *//*v.mToday.setTitleSize(mTodayTitleSize);
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
		v.mTheDayAfterTomorrow.setHeadColor(mTheDayAfterTomorrowHeadColor);*/

        //TextViewUtil.setTextSize(tittle_text,mCitySize);

        TextViewUtil.setTextSize(v.mCity,tvTimeSize);
        TextViewUtil.setTextColor(v.mCity,tvTimeColor);
        TextViewUtil.setTextSize(v.mWind,tvWeatherSize);
        TextViewUtil.setTextColor(v.mWind,tvWeatherColor);
        TextViewUtil.setTextSize(v.mAirQuality,tvAirSize);
        TextViewUtil.setTextColor(v.mAirQuality,tvTempRangeColor);

        TextViewUtil.setTextSize(today_title,tvTimeSize);
        TextViewUtil.setTextColor(today_title,tvTimeColor);
        TextViewUtil.setTextSize(today_weather,tvWeatherSize);
        TextViewUtil.setTextColor(today_weather,tvWeatherColor);
        TextViewUtil.setTextSize(today_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(today_tem,tvTempRangeColor);

        TextViewUtil.setTextSize(tomorrow_title,tvTimeSize);
        TextViewUtil.setTextColor(tomorrow_title,tvTimeColor);
        TextViewUtil.setTextSize(tomorrow_weather,tvWeatherSize);
        TextViewUtil.setTextColor(tomorrow_weather,tvWeatherColor);
        TextViewUtil.setTextSize(tomorrow_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(tomorrow_tem,tvTempRangeColor);

        TextViewUtil.setTextSize(dayft_title,tvTimeSize);
        TextViewUtil.setTextColor(dayft_title,tvTimeColor);
        TextViewUtil.setTextSize(dayft_weather,tvWeatherSize);
        TextViewUtil.setTextColor(dayft_weather,tvWeatherColor);
        TextViewUtil.setTextSize(dayft_tem,tvTempRangeSize);
        TextViewUtil.setTextColor(dayft_tem,tvTempRangeColor);

        WeatherRefresher2.getInstance().updateData(viewData.textContent, v);

        //return v.layout;
        return v.layout;
    }

    private View createWeatherViewNoneOneday(ChatWeatherViewData viewData){
	    WeatherViewHolder v = new WeatherViewHolder();
        v.layout = (BoundedLinearLayout) LayouUtil.getView("chat_weather_view_oneday");

        llContent = (LinearLayout) LayouUtil.findViewByName("llContent",v.layout);
        LinearLayout temp = (LinearLayout) LayouUtil.findViewByName("temp",v.layout);
        //layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvTempRangeTopMargin + ivWeatherSide);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = tvTempRangeTopMarginNone;
        temp.setLayoutParams(layoutParams);
        v.mCurrentTemp = (LinearLayout) LayouUtil.findViewByName("current_temp", v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        v.mCurrentTemp.setLayoutParams(layoutParams);
        v.mBigTempRange = (LinearLayout) LayouUtil.findViewByName("bigTempRange",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.BOTTOM;
        v.mBigTempRange.setLayoutParams(layoutParams);
        v.mMinus = (ImageView) LayouUtil.findViewByName("minus",v.layout);
        v.mMinus.setImageDrawable(LayouUtil.getDrawable("weather_number_minus"));
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mMinus.setLayoutParams(layoutParams);
        v.mTempDecade = (ImageView) LayouUtil.findViewByName("tempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mTempDecade.setLayoutParams(layoutParams);
        v.mTempUnits = (ImageView) LayouUtil.findViewByName("tempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mTempUnits.setLayoutParams(layoutParams);
        v.mTempDegree = (ImageView) LayouUtil.findViewByName("tempDegree",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivDegreeWidth1,ivDegreeHeight1);
        layoutParams.gravity = Gravity.TOP;
        v.mTempDegree.setLayoutParams(layoutParams);
        //v.mTempRange = (TextView) LayouUtil.findViewByName("tempRange",v.layout);
        //v.mTodayWeather = (ImageView) LayouUtil.findViewByName("todayWeather",v.layout);

        v.mBigLowMinus = (ImageView) LayouUtil.findViewByName("bigLowMinus",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigLowMinus.setLayoutParams(layoutParams);
        v.mBigLowTempDecade = (ImageView) LayouUtil.findViewByName("bigLowTempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigLowTempDecade.setLayoutParams(layoutParams);
        v.mBigLowTempUnits = (ImageView) LayouUtil.findViewByName("bigLowTempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigLowTempUnits.setLayoutParams(layoutParams);
        v.mBigSlash = (ImageView) LayouUtil.findViewByName("bigSlash", v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigSlash.setLayoutParams(layoutParams);
        v.mBigHighMinus = (ImageView) LayouUtil.findViewByName("bigHighMinus",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivMinusWidth1,ivMinusHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigHighMinus.setLayoutParams(layoutParams);
        v.mBigHighTempDecade = (ImageView) LayouUtil.findViewByName("bigHighTempDecade",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigHighTempDecade.setLayoutParams(layoutParams);
        v.mBigHighTempUnits = (ImageView) LayouUtil.findViewByName("bigHighTempUnits",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivTempWidth1,ivTempHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mBigHighTempUnits.setLayoutParams(layoutParams);
        v.mBigTempDegree = (ImageView) LayouUtil.findViewByName("bigTempDegree",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivDegreeWidth1,ivDegreeHeight1);
        layoutParams.gravity = Gravity.TOP;
        v.mBigTempDegree.setLayoutParams(layoutParams);

        v.mDate = (TextView) LayouUtil.findViewByName("date",v.layout);
        layoutParams = new LinearLayout.LayoutParams(0,tvTimeHeight,1);
        layoutParams.gravity = Gravity.RIGHT;
        layoutParams.rightMargin = todayRightMargin;
        v.mDate.setLayoutParams(layoutParams);
        v.mDay = (TextView) LayouUtil.findViewByName("day",v.layout);
        v.mWeather = (TextView) LayouUtil.findViewByName("weather",v.layout);
        v.mWind = (TextView) LayouUtil.findViewByName("wind",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight1);
        layoutParams.leftMargin = todayLeftMargin;
        v.mWind.setLayoutParams(layoutParams);

        //v.mAirQualityText = (TextView) LayouUtil.findViewByName("airQualityText",v.layout);
        v.mAirQuality = (TextView) LayouUtil.findViewByName("airQuality",v.layout);
        v.mAirQuality.setPadding(tvAirHorMargin,0,tvAirHorMargin,0);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = tvTempRangeTopMarginNone;
        v.mAirQuality.setLayoutParams(layoutParams);
        //v.mAirDegree = (TextView) LayouUtil.findViewByName("airDegree",v.layout);
        v.mCity = (TextView) LayouUtil.findViewByName("city",v.layout);
        layoutParams = new LinearLayout.LayoutParams(0,tvTimeHeight,1);
        layoutParams.gravity = Gravity.LEFT;
        layoutParams.leftMargin = todayRightMargin;
        v.mCity.setLayoutParams(layoutParams);

//		today = (LinearLayout) LayouUtil.findViewByName("today",v.layout);
        //today_title = (TextView) LayouUtil.findViewByName("today_title",v.layout);
        v.mTodayWeather = (ImageView) LayouUtil.findViewByName("today_image",v.layout);
        layoutParams = new LinearLayout.LayoutParams(ivWeatherSide1,ivWeatherSide1);
        layoutParams.gravity = Gravity.CENTER;
        //layoutParams.topMargin = tvTempRangeTopMargin;
        layoutParams.rightMargin = todayLeftMargin;
        v.mTodayWeather.setLayoutParams(layoutParams);
        v.mWeather = (TextView) LayouUtil.findViewByName("today_weather",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight1);
        layoutParams.gravity = Gravity.CENTER;
        v.mWeather.setLayoutParams(layoutParams);
        v.mTempRange = (TextView) LayouUtil.findViewByName("today_tem",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvWeatherHeight1);
        layoutParams.leftMargin = todayLeftMargin;
        layoutParams.gravity = Gravity.CENTER;
        v.mTempRange.setLayoutParams(layoutParams);

        llContent.setBackground(LayouUtil.getDrawable("weather_background"));
//
        LinearLayout llTextCount =  (LinearLayout) LayouUtil.findViewByName("textCount",v.layout);
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.topMargin = tvTempRangeTopMarginNone;
        llTextCount.setLayoutParams(layoutParams);

        TextViewUtil.setTextSize(v.mCity,tvTimeSize);
        TextViewUtil.setTextColor(v.mCity,tvTimeColor);
        TextViewUtil.setTextSize(v.mDate,tvTimeSize);
        TextViewUtil.setTextColor(v.mDate,tvTimeColor);

        TextViewUtil.setTextSize(v.mWeather,tvWeatherSize1);
        TextViewUtil.setTextColor(v.mWeather,tvWeatherColor);
        TextViewUtil.setTextSize(v.mTempRange,tvWeatherSize1);
        TextViewUtil.setTextColor(v.mTempRange,tvWeatherColor);
        TextViewUtil.setTextSize(v.mWind,tvWeatherSize1);
        TextViewUtil.setTextColor(v.mWind,tvWeatherColor);

        TextViewUtil.setTextSize(v.mAirQuality,tvAirSize);
        TextViewUtil.setTextColor(v.mAirQuality,tvWeatherColor);

        WeatherRefresher2.getInstance().updateDataOneday(viewData.textContent, v);

        return v.layout;
    }

	@Override
	public int getViewType() {
		return super.getViewType();
	}

	@Override
	public void init() {
		super.init();
		/*layoutPaddingTop = (int)LayouUtil.getDimen("y20");
		layoutPaddingBottom = (int)LayouUtil.getDimen("y60");
		cityMarginLeft = (int) LayouUtil.getDimen("x10");
		dateMarginLeft = (int) LayouUtil.getDimen("x10");
		dateMarginRight = (int) LayouUtil.getDimen("x30");
		llContentMarginTop = (int) LayouUtil.getDimen("y10");
		mRlTopMarginLeft = (int) LayouUtil.getDimen("x30");
		mRlTopMarginRight = (int) LayouUtil.getDimen("x30");
		mRlTopPaddingTop = (int) LayouUtil.getDimen("y10");
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
		mWeatherMaxWidth = ThemeConfigManager.WEATHER_MAXWIDTH;*/

        tvTimeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvWeatherColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvTempRangeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        setShowMoreWeather();

	}

    public void onUpdateParams(int styleIndex){
        switch (styleIndex) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                initFull();
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                initHalf();
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                initNone();
                break;
            default:
                break;
        }
        setShowMoreWeather();
    }


    //全屏模式布局
    private void initFull(){
        if (WinLayout.isVertScreen){
            int unit = ViewParamsUtil.unit;
            //contentWidth;
            contentHeight = 46 * unit;
            todayLeftMargin = 2 * unit;
            todayRightMargin = 3 * unit;
            tvTimeSize = ViewParamsUtil.h5;
            tvTimeHeight = ViewParamsUtil.h5Height;
            ivWeatherTopMargin = 6 * unit;
            ivMinusWidth = 28 * unit /12;
            ivMinusHeight = 10 * unit;
            ivTempWidth = 44 * unit / 12;
            ivTempHeight = 10 * unit;
            ivDegreeWidth = 28 * unit / 12;
            ivDegreeHeight = 10 * unit;
            ivWeatherSide = 10 * unit;
            tvWeatherTopMargin = 4 * unit;
            tvWeatherSize = ViewParamsUtil.h4;
            tvWeatherHeight = ViewParamsUtil.h4Height;
            tvTempRangeTopMargin = 5 * unit;
            tvTempRangeSize = ViewParamsUtil.h5;
            tvTempRangeHeight = ViewParamsUtil.h5Height;
            tvAirSize = ViewParamsUtil.h7;
            tvAirHorMargin = unit;

            ivMinusWidth1 = 28 * unit /10;
            ivMinusHeight1 = 12 * unit;
            ivTempWidth1 = 44 * unit / 10;
            ivTempHeight1 = 12 * unit;
            ivDegreeWidth1 = 28 * unit/ 10;
            ivDegreeHeight1 =  12 * unit;
            ivWeatherSide1 = 12 * unit;
            tvWeatherSize1 = ViewParamsUtil.h3;
            tvWeatherHeight1 = ViewParamsUtil.h3Height;

            ivWeatherTopMarginNone = 4 * unit;
            tvWeatherTopMarginNone = 4 * unit;
            tvTempRangeTopMarginNone = 3 * unit;
            verPadding = 2 * unit;
        }else {
            int unit = ViewParamsUtil.unit;
            if (SizeConfig.screenHeight < 464 ){
                ivWeatherTopMargin = 2 * unit;
                ivMinusWidth = 28 * unit / 15;
                ivMinusHeight = 8 * unit;
                ivTempWidth = 44 * unit / 15;
                ivTempHeight = 8 * unit;
                ivDegreeWidth = 28 * unit / 15;
                ivDegreeHeight = 8 * unit;
                ivWeatherSide = 8 * unit;
                tvWeatherTopMargin = unit;
                tvTempRangeTopMargin = unit;

                ivMinusWidth1 = 28 * unit / 15;
                ivMinusHeight1 = 8 * unit;
                ivTempWidth1 = 44 * unit / 15;
                ivTempHeight1 = 8 * unit;
                ivDegreeWidth1 = 28 * unit / 15;
                ivDegreeHeight1 = 8 * unit;
                ivWeatherSide1 = 8 * unit;
            }else {
                ivWeatherTopMargin = 6 * unit;
                ivMinusWidth = 28 * unit / 12;
                ivMinusHeight = 10 * unit;
                ivTempWidth = 44 * unit / 12;
                ivTempHeight = 10 * unit;
                ivDegreeWidth = 28 * unit / 12;
                ivDegreeHeight = 10 * unit;
                ivWeatherSide = 10 * unit;
                tvWeatherTopMargin = 4 * unit;
                tvTempRangeTopMargin = 5 * unit;

                ivMinusWidth1 = 28 * unit / 10;
                ivMinusHeight1 = 12 * unit;
                ivTempWidth1 = 44 * unit / 10;
                ivTempHeight1 = 12 * unit;
                ivDegreeWidth1 = 28 * unit / 10;
                ivDegreeHeight1 = 12 * unit;
                ivWeatherSide1 = 12 * unit;
            }
            contentHeight = SizeConfig.itemHeight * SizeConfig.pageCount;
            todayLeftMargin = 2 * unit;
            todayRightMargin = 3 * unit;
            tvTimeSize = ViewParamsUtil.h5;
            tvTimeHeight = ViewParamsUtil.h5Height;
            tvTimeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
            tvWeatherSize = ViewParamsUtil.h4;
            tvWeatherHeight = ViewParamsUtil.h4Height;
            tvWeatherColor = Color.parseColor(LayouUtil.getString("color_main_title"));
            tvTempRangeSize = ViewParamsUtil.h5;
            tvTempRangeHeight = ViewParamsUtil.h5Height;
            tvTempRangeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
            tvAirSize = ViewParamsUtil.h7;
            tvAirHorMargin = unit;

            tvWeatherSize1 = ViewParamsUtil.h3;
            tvWeatherHeight1 = ViewParamsUtil.h3Height;

            ivWeatherTopMarginNone = 4 * unit;
            tvWeatherTopMarginNone = 4 * unit;
            tvTempRangeTopMarginNone = 3 * unit;
            verPadding = 0;
        }
    }

    //全屏模式布局
    private void initHalf(){
        if (WinLayout.isVertScreen){
            int unit = ViewParamsUtil.unit;
            //contentWidth;
            //contentHeight = SizeConfig.itemHeight * SizeConfig.pageCount;
            contentHeight =  46 * unit;
            todayLeftMargin = 2 * unit;
            todayRightMargin = 3 * unit;
            tvTimeSize = ViewParamsUtil.h5;
            tvTimeHeight = ViewParamsUtil.h5Height;
            ivWeatherTopMargin = 6 * unit;
            ivMinusWidth = 28 * unit / 12;
            ivMinusHeight = 10 * unit;
            ivTempWidth = 44 * unit / 12;
            ivTempHeight = 10 * unit;
            ivDegreeWidth = 28 * unit / 12;
            ivDegreeHeight = 10 * unit;
            ivWeatherSide = 10 * unit;
            tvWeatherTopMargin = 4 * unit;
            tvWeatherSize = ViewParamsUtil.h4;
            tvWeatherHeight = ViewParamsUtil.h4Height;
            tvTempRangeTopMargin = 5 * unit;
            tvTempRangeSize = ViewParamsUtil.h5;
            tvTempRangeHeight = ViewParamsUtil.h5Height;
            tvAirSize = ViewParamsUtil.h7;
            tvAirHorMargin = unit;

            ivMinusWidth1 = 28 * unit / 10;
            ivMinusHeight1 = 12 * unit;
            ivTempWidth1 = 44 * unit / 10;
            ivTempHeight1 = 12 * unit;
            ivDegreeWidth1 = 28 * unit / 10;
            ivDegreeHeight1 = 12 * unit;
            ivWeatherSide1 = 12 * unit;
            tvWeatherSize1 = ViewParamsUtil.h3;
            tvWeatherHeight1 = ViewParamsUtil.h3Height;

            ivWeatherTopMarginNone = 4 * unit;
            tvWeatherTopMarginNone = 4 * unit;
            tvTempRangeTopMarginNone = 3 * unit;
            verPadding = 2 * unit;
        }else {
            int unit = ViewParamsUtil.unit;
            if (SizeConfig.screenHeight < 464 ){
                ivWeatherTopMargin = 2 * unit;
                ivMinusWidth = 28 * unit / 15;
                ivMinusHeight = 8 * unit;
                ivTempWidth = 44 * unit / 15;
                ivTempHeight = 8 * unit;
                ivDegreeWidth = 28 * unit / 15;
                ivDegreeHeight = 8 * unit;
                ivWeatherSide = 8 * unit;
                tvWeatherTopMargin = unit;
                tvTempRangeTopMargin = unit;

                ivMinusWidth1 = 28 * unit / 15;
                ivMinusHeight1 = 8 * unit;
                ivTempWidth1 = 44 * unit / 15;
                ivTempHeight1 = 8 * unit;
                ivDegreeWidth1 = 28 * unit / 15;
                ivDegreeHeight1 = 8 * unit;
                ivWeatherSide1 = 8 * unit;

                ivWeatherTopMarginNone = 2 * unit;
                tvWeatherTopMarginNone = unit;
                tvTempRangeTopMarginNone = unit;
            }else {
                ivWeatherTopMargin = 6 * unit;
                ivMinusWidth = 28 * unit / 12;
                ivMinusHeight = 10 * unit;
                ivTempWidth = 44 * unit / 12;
                ivTempHeight = 10 * unit;
                ivDegreeWidth = 28 * unit / 12;
                ivDegreeHeight = 10 * unit;
                ivWeatherSide = 10 * unit;
                tvWeatherTopMargin = 4 * unit;
                tvTempRangeTopMargin = 5 * unit;

                ivMinusWidth1 = 28 * unit / 10;
                ivMinusHeight1 = 12 * unit;
                ivTempWidth1 = 44 * unit / 10;
                ivTempHeight1 = 12 * unit;
                ivDegreeWidth1 = 28 * unit / 10;
                ivDegreeHeight1 = 12 * unit;
                ivWeatherSide1 = 12 * unit;

                ivWeatherTopMarginNone = 4 * unit;
                tvWeatherTopMarginNone = 4 * unit;
                tvTempRangeTopMarginNone = 3 * unit;
            }
            contentHeight = SizeConfig.itemHeight * SizeConfig.pageCount;
            todayLeftMargin = 2 * unit;
            todayRightMargin = 3 * unit;
            tvTimeSize = ViewParamsUtil.h5;
            tvTimeHeight = ViewParamsUtil.h5Height;
            tvTimeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
            tvWeatherSize = ViewParamsUtil.h4;
            tvWeatherHeight = ViewParamsUtil.h4Height;
            tvWeatherColor = Color.parseColor(LayouUtil.getString("color_main_title"));
            tvTempRangeSize = ViewParamsUtil.h5;
            tvTempRangeHeight = ViewParamsUtil.h5Height;
            tvTempRangeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
            tvAirSize = ViewParamsUtil.h7;
            tvAirHorMargin = unit;

            tvWeatherSize1 = ViewParamsUtil.h3;
            tvWeatherHeight1 = ViewParamsUtil.h3Height;
            verPadding = 0;
        }
    }

    //全屏模式布局
    private void initNone(){
//        if (WinLayout.isVertScreen){
//            //int unit = (int) LayouUtil.getDimen("unit");
//            int unit = (int) LayouUtil.getDimen("x11");
//            //contentWidth;
//            //contentHeight;
//            todayLeftMargin = 2 * unit;
//            todayRightMargin = 2 * unit;
//            tvTimeSize = (int) LayouUtil.getDimen("x33");
//            tvTimeHeight = tvTimeSize + 10;
//            ivWeatherTopMargin = 5 * unit;
//            ivMinusWidth = 28 * unit / 12;
//            ivMinusHeight = 10 * unit;
//            ivTempWidth = 44 * unit / 12;
//            ivTempHeight = 10 * unit;
//            ivDegreeWidth = 28 * unit / 12;
//            ivDegreeHeight = 10 * unit;
//            ivWeatherSide = 10 * unit;
//            tvWeatherTopMargin = 3 * unit;
//            tvWeatherSize = (int) LayouUtil.getDimen("x35");
//            tvWeatherHeight = tvWeatherSize + 10;
//            tvTempRangeTopMargin = 4 * unit;
//            tvTempRangeSize = (int) LayouUtil.getDimen("x31");
//            tvTempRangeHeight = tvTempRangeSize + 10;
//            tvAirSize = (int) LayouUtil.getDimen("x29");
//            tvAirHorMargin = unit;
//
//            ivMinusWidth1 = 28 * unit / 10;
//            ivMinusHeight1 = 12 * unit;
//            ivTempWidth1 = 44 * unit /10;
//            ivTempHeight1 = 12 * unit;
//            ivDegreeWidth1 = 28 * unit / 10;
//            ivDegreeHeight1 = 12 * unit;
//            ivWeatherSide1 = 12 * unit;
//            tvWeatherSize1 = (int) LayouUtil.getDimen("x37");
//            tvWeatherHeight1 = tvWeatherSize1 + 10;
//
//            ivWeatherTopMarginNone = 3 * unit;
//            tvWeatherTopMarginNone = 3 * unit;
//            tvTempRangeTopMarginNone = 2 * unit;
//        }else {
//            int unit = (int) LayouUtil.getDimen("unit");
//            if (SizeConfig.screenHeight < 464 ){
//                ivWeatherTopMargin = 2 * unit;
//                ivMinusWidth = 28 * unit / 15;
//                ivMinusHeight = 8 * unit;
//                ivTempWidth = 44 * unit / 15;
//                ivTempHeight = 8 * unit;
//                ivDegreeWidth = 28 * unit / 15;
//                ivDegreeHeight = 8 * unit;
//                ivWeatherSide = 8 * unit;
//                tvWeatherTopMargin = unit;
//                tvTempRangeTopMargin = unit;
//
//                ivMinusWidth1 = 28 * unit / 15;
//                ivMinusHeight1 = 8 * unit;
//                ivTempWidth1 = 44 * unit / 15;
//                ivTempHeight1 = 8 * unit;
//                ivDegreeWidth1 = 28 * unit / 15;
//                ivDegreeHeight1 = 8 * unit;
//                ivWeatherSide1 = 8 * unit;
//
//                ivWeatherTopMarginNone = 2 * unit;
//                tvWeatherTopMarginNone = unit;
//                tvTempRangeTopMarginNone = unit;
//            }else {
//                ivWeatherTopMargin = 6 * unit;
//                ivMinusWidth = 28 * unit / 12;
//                ivMinusHeight = 10 * unit;
//                ivTempWidth = 44 * unit / 12;
//                ivTempHeight = 10 * unit;
//                ivDegreeWidth = 28 * unit / 12;
//                ivDegreeHeight = 10 * unit;
//                ivWeatherSide = 10 * unit;
//                tvWeatherTopMargin = 4 * unit;
//                tvTempRangeTopMargin = 5 * unit;
//
//                ivMinusWidth1 = 28 * unit / 10;
//                ivMinusHeight1 = 12 * unit;
//                ivTempWidth1 = 44 * unit / 10;
//                ivTempHeight1 = 12 * unit;
//                ivDegreeWidth1 = 28 * unit / 10;
//                ivDegreeHeight1 = 12 * unit;
//                ivWeatherSide1 = 12 * unit;
//
//                ivWeatherTopMarginNone = 4 * unit;
//                tvWeatherTopMarginNone = 4 * unit;
//                tvTempRangeTopMarginNone = 3 * unit;
//            }
//            todayLeftMargin = 2 * unit;
//            todayRightMargin = 3 * unit;
//            tvTimeSize = (int) LayouUtil.getDimen("h5_none");
//            tvTimeHeight = (int) LayouUtil.getDimen("h5_height_none");
//            tvWeatherSize = (int) LayouUtil.getDimen("h4_none");
//            tvWeatherHeight = (int) LayouUtil.getDimen("h4_height_none");
//            tvTempRangeSize = (int) LayouUtil.getDimen("h5_none");
//            tvTempRangeHeight = (int) LayouUtil.getDimen("h5_height_none");
//            tvAirSize = (int) LayouUtil.getDimen("h7_none");
//            tvAirHorMargin = unit;
//
//            tvWeatherSize1 = (int) LayouUtil.getDimen("h3_none");
//            tvWeatherHeight1 = (int) LayouUtil.getDimen("h3_height_none");
//        }
        int unit = WinLayout.isVertScreen?(int) LayouUtil.getDimen("x666")/64:ViewParamsUtil.unit;
        if (SizeConfig.screenHeight < 464 ){
            ivWeatherTopMargin = 2 * unit;
            ivMinusWidth = 28 * unit / 15;
            ivMinusHeight = 8 * unit;
            ivTempWidth = 44 * unit / 15;
            ivTempHeight = 8 * unit;
            ivDegreeWidth = 28 * unit / 15;
            ivDegreeHeight = 8 * unit;
            ivWeatherSide = 8 * unit;
            tvWeatherTopMargin = unit;
            tvTempRangeTopMargin = unit;

            ivMinusWidth1 = 28 * unit / 15;
            ivMinusHeight1 = 8 * unit;
            ivTempWidth1 = 44 * unit / 15;
            ivTempHeight1 = 8 * unit;
            ivDegreeWidth1 = 28 * unit / 15;
            ivDegreeHeight1 = 8 * unit;
            ivWeatherSide1 = 8 * unit;

            ivWeatherTopMarginNone = 2 * unit;
            tvWeatherTopMarginNone = unit;
            tvTempRangeTopMarginNone = unit;
        }else {
            ivWeatherTopMargin = 6 * unit;
            ivMinusWidth = 28 * unit / 12;
            ivMinusHeight = 10 * unit;
            ivTempWidth = 44 * unit / 12;
            ivTempHeight = 10 * unit;
            ivDegreeWidth = 28 * unit / 12;
            ivDegreeHeight = 10 * unit;
            ivWeatherSide = 10 * unit;
            tvWeatherTopMargin = 4 * unit;
            tvTempRangeTopMargin = 5 * unit;

            ivMinusWidth1 = 28 * unit / 10;
            ivMinusHeight1 = 12 * unit;
            ivTempWidth1 = 44 * unit / 10;
            ivTempHeight1 = 12 * unit;
            ivDegreeWidth1 = 28 * unit / 10;
            ivDegreeHeight1 = 12 * unit;
            ivWeatherSide1 = 12 * unit;

            ivWeatherTopMarginNone = 4 * unit;
            tvWeatherTopMarginNone = 4 * unit;
            tvTempRangeTopMarginNone = 3 * unit;
        }
        todayLeftMargin = 2 * unit;
        todayRightMargin = 3 * unit;
        tvTimeSize = ViewParamsUtil.h5;
        tvTimeHeight = ViewParamsUtil.h5Height;
        tvWeatherSize = ViewParamsUtil.h4;
        tvWeatherHeight = ViewParamsUtil.h4Height;
        tvTempRangeSize = ViewParamsUtil.h5;
        tvTempRangeHeight = ViewParamsUtil.h5Height;
        tvAirSize = ViewParamsUtil.h7;
        tvAirHorMargin = unit;

        tvWeatherSize1 = ViewParamsUtil.h3;
        tvWeatherHeight1 = ViewParamsUtil.h3Height;
    }

	//展示三天的天气
	public void refreshThreeDay(String today_title,Drawable today_image,String today_weather,String today_tem,
								String tomorrow_title,Drawable tomorrow_image,String tomorrow_weather,String tomorrow_tem,
								String dayft_title,Drawable dayft_image,String dayft_weather,String dayft_tem){
        int index = today_title.indexOf(" ");
        if (index != -1){
            today_title = today_title.substring(0,index);
        }
		this.today_title.setText(today_title);
		this.today_image.setImageDrawable(today_image);
		this.today_weather.setText(today_weather);
		this.today_tem.setText(today_tem);

		this.tomorrow_title.setText(tomorrow_title);
		this.tomorrow_image.setImageDrawable(tomorrow_image);
		this.tomorrow_weather.setText(tomorrow_weather);
		this.tomorrow_tem.setText(tomorrow_tem);

		this.dayft_title.setText(dayft_title);
		this.dayft_image.setImageDrawable(dayft_image);
		this.dayft_weather.setText(dayft_weather);
		this.dayft_tem.setText(dayft_tem);
	}

    //最多展示五天的天气
    public void refreshMoreDay(String today_title,Drawable today_image,String today_weather,String today_tem,
                                String tomorrow_title,Drawable tomorrow_image,String tomorrow_weather,String tomorrow_tem,
                                String dayft_title,Drawable dayft_image,String dayft_weather,String dayft_tem,
                               String daymore_title,Drawable daymore_image,String daymore_weather,String daymore_tem,
                               String daylast_title,Drawable daylast_image,String daylast_weather,String daylast_tem){
        int index = today_title.indexOf(" ");
        if (index != -1){
            today_title = today_title.substring(0,index);
        }
        this.today_title.setText(today_title);
        this.today_image.setImageDrawable(today_image);
        this.today_weather.setText(today_weather);
        this.today_tem.setText(today_tem);

        this.tomorrow_title.setText(tomorrow_title);
        this.tomorrow_image.setImageDrawable(tomorrow_image);
        this.tomorrow_weather.setText(tomorrow_weather);
        this.tomorrow_tem.setText(tomorrow_tem);

        this.dayft_title.setText(dayft_title);
        this.dayft_image.setImageDrawable(dayft_image);
        this.dayft_weather.setText(dayft_weather);
        this.dayft_tem.setText(dayft_tem);

        this.daymore_title.setText(daymore_title);
        this.daymore_image.setImageDrawable(daymore_image);
        this.daymore_weather.setText(daymore_weather);
        this.daymore_tem.setText(daymore_tem);

        this.daylast_title.setText(daylast_title);
        this.daylast_image.setImageDrawable(daylast_image);
        this.daylast_weather.setText(daylast_weather);
        this.daylast_tem.setText(daylast_tem);
    }

	//更新天气界面背景
	public void refreshBackground(GradientDrawable gradientDrawable){
		if (gradientDrawable != null) {
			LogUtil.logd(WinLayout.logTag+ "refreshBackground: ");
			gradientDrawable.setCornerRadius(10);
			this.llContent.setBackground(gradientDrawable);
		} else {
			this.llContent.setBackground(LayouUtil.getDrawable("weather_background"));
		}
	}

	public void refreshWeather(Drawable today_image,String today_weather){
		this.today_image.setImageDrawable(today_image);
		this.today_weather.setText(today_weather);
	}
}
