package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txz.ui.voice.VoiceData.StockInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.view.BoundedLinearLayout;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatShockViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatShockView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

/**
 * @author ASUS User
 *
 *	股票的界面
 */
@SuppressLint("NewApi")
public class ChatShockView extends IChatShockView{
	
	private static ChatShockView sInstance = new ChatShockView();
	
	private ChatShockView(){
	}
	
	public static ChatShockView getInstance(){
		return sInstance;
	}
	
	private StockInfo mStockInfo;
	private int shareColorUp;
	private int shareColorDown;
	private int shareColorNormal;

	private int countHeight;    //内容高度
    private int verPadding;    //对话框上下间距
    private int leftMargin;    //内容左边距
    private int detailVerMargin;    //详细内容上下间距
   //private int tvNameWidth;    //股票名称宽度
    private int tvNameHeight;    //股票名称高度
    private int tvNameBottomMargin;    //股票名称下边距
    //private int tvPriceWidth;    //股票价格宽度
    private int tvPriceHeight;    //股票价格高度
    private int ivUpAndDownSize;    //上涨、下跌箭头大小
    private int topHorMargin;    //股票信息标题内容间距
    private int detailNameWidth;    //股票详细信息名称宽度
    private int detailNumWidth;    //股票详细信息数值宽度
    private int detailHeight;    //股票详细信息高度
    private int tvNameSize;    //股票标题字体大小
    private int tvNameColor;    //股票标题字体颜色
    private int tvPriceSize;    //股票价格字体大小
    private int detailSize;    //股票详细内容字体大小
    private int detailColor;    //股票详细内容字体颜色


	@Override
	public void init() {
		super.init();
		shareColorUp = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_VALUE_COLOR1);
		shareColorDown = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_VALUE_COLOR2);
		shareColorNormal = Color.parseColor("#FFFFFFFF");

        tvNameColor = Color.parseColor("#FFFFFFFF");
        detailColor =  Color.parseColor("#88FFFFFF");
	}

    //切换模式修改布局参数
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
    }

    //全屏布局参数
    private void initFull(){
        int unit = ViewParamsUtil.unit;
        countHeight = WinLayout.isVertScreen?46 * unit:SizeConfig.pagePoiCount * SizeConfig.itemHeight;
        leftMargin = 4 * unit;
        detailVerMargin = 4 * unit;
        //tvNameWidth = 10 * unit;
        tvNameHeight = ViewParamsUtil.h1Height;
        tvNameBottomMargin = unit;
        //tvPriceWidth = 24 * unit;
        topHorMargin = 2 * unit;
        ivUpAndDownSize = 5 * unit;
        detailNameWidth = 10 * unit;
        detailNumWidth = 24 * unit;
        tvPriceSize =  ViewParamsUtil.shockPriceSize;
        tvPriceHeight = ViewParamsUtil.shockPriceHeight;
        tvNameSize = ViewParamsUtil.shockNameSize;
        detailSize = ViewParamsUtil.h5 ;
        detailHeight = ViewParamsUtil.h5Height;
        verPadding = WinLayout.isVertScreen?2 * unit:0;

    }

    //半屏布局参数
    private void initHalf(){
        int unit = ViewParamsUtil.unit;
        countHeight = WinLayout.isVertScreen?46 * unit:SizeConfig.pagePoiCount * SizeConfig.itemHeight;
        if (SizeConfig.screenHeight < 480){
            detailVerMargin = 2 * unit;
        }else {
            detailVerMargin = 4 * unit;
        }
        leftMargin = 4 * unit;
        //tvNameWidth = 10 * unit;
        tvNameHeight = ViewParamsUtil.h1Height;
        tvNameBottomMargin = unit;
        //tvPriceWidth = 24 * unit;
        topHorMargin = 2 * unit;
        ivUpAndDownSize = 5 * unit;
        detailNameWidth = 10 * unit;
        detailNumWidth = 24 * unit;
        tvNameSize = ViewParamsUtil.shockNameSize;
        tvPriceSize = ViewParamsUtil.shockPriceSize;
        tvPriceHeight = ViewParamsUtil.shockPriceHeight;
        detailSize = ViewParamsUtil.h5;
        detailHeight = ViewParamsUtil.h5Height;
        verPadding = WinLayout.isVertScreen?2 * unit:0;

    }

    //无屏布局参数
    private void initNone(){
        int unit = ViewParamsUtil.unit;
        leftMargin = 3 * unit;
        detailVerMargin = 4 * unit;
        //tvNameWidth = 10 * unit;
        tvNameHeight = ViewParamsUtil.h1 + 10;
        tvNameBottomMargin = unit;
        //tvPriceWidth = 24 * unit;
        topHorMargin = 2 * unit;
        ivUpAndDownSize = 3 * unit;
        detailNameWidth = 9 * unit;
        detailNumWidth = 22 * unit;
        tvNameSize = ViewParamsUtil.shockNameSize;
        tvPriceSize = ViewParamsUtil.shockPriceSize;
        tvPriceHeight = ViewParamsUtil.shockPriceHeight;
        detailSize = ViewParamsUtil.h5;
        detailHeight = ViewParamsUtil.h5Height;

    }
	
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatShockViewData chatShockViewData = (ChatShockViewData) data;
		WinLayout.getInstance().vTips = chatShockViewData.vTips;
		LogUtil.logd(WinLayout.logTag+ "ChatShockViewData.vTips: "+chatShockViewData.vTips);

		View view = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = createViewFull(chatShockViewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = createViewHalf(chatShockViewData);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(chatShockViewData);
				break;
		}

		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = view;
		adapter.view.setTag(data.getType());
		adapter.object = ChatShockView.getInstance();
		return adapter;
	}

	private View createViewFull(ChatShockViewData viewData){
		LinearLayout mLayout = new LinearLayout(GlobalContext.get());
		mLayout.setGravity(Gravity.CENTER_VERTICAL);
		mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setPadding(0,verPadding,0,verPadding);

		/*LinearLayout ltittle = new LinearLayout(GlobalContext.get());
		ltittle.setOrientation(LinearLayout.HORIZONTAL);
		ltittle.setGravity(Gravity.CENTER_VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
		mLayout.addView(ltittle,layoutParams);

		ImageView ivIcon = new ImageView(GlobalContext.get());
		ivIcon.setImageDrawable(LayouUtil.getDrawable("title_icon_shares"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
		ltittle.addView(ivIcon,layoutParams);

		TextView tvTittle = new TextView(GlobalContext.get());
		tvTittle.setText("股票");
		tvTittle.setGravity(Gravity.CENTER_VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
		ltittle.addView(tvTittle,layoutParams);*/
        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null,"shares","股票");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mLayout.addView(titleViewAdapter.view,layoutParams);

        FrameLayout frameLayout = new FrameLayout(GlobalContext.get());
        frameLayout.setBackground(LayouUtil.getDrawable("white_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,countHeight);
        mLayout.addView(frameLayout,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		//llContent.setPadding(0,llContentPaddingTop, 0, llContentPaddingBottom);
		//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,countHeight);
		//mLayout.addView(llContent,layoutParams);
        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        flLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flLayoutParams.leftMargin = leftMargin;
        frameLayout.addView(llContent,flLayoutParams);

		LinearLayout llTop = new LinearLayout(GlobalContext.get());
		llTop.setOrientation(LinearLayout.HORIZONTAL);
		//llTop.setPadding(0, llTopPaddingTop, 0, llTopPaddingBottom);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		/*layoutParams.leftMargin = llTopMarginLeft;
		layoutParams.rightMargin = llTopMarginRight;*/
        layoutParams.bottomMargin = detailVerMargin;
		llContent.addView(llTop,layoutParams);

		LinearLayout llName = new LinearLayout(GlobalContext.get());
        llName.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTop.addView(llName,layoutParams);

		//股票名
		TextView tvName = new TextView(GlobalContext.get());
        tvName.setGravity(Gravity.CENTER_VERTICAL);
		//layoutParams = new LinearLayout.LayoutParams(tvNameWidth,tvNameHeight);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvNameHeight);
		layoutParams.bottomMargin = tvNameBottomMargin;
		llName.addView(tvName, layoutParams);
		//股票代码
		TextView tvCode = new TextView(GlobalContext.get());
        tvCode.setGravity(Gravity.CENTER_VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,detailHeight);
		llName.addView(tvCode, layoutParams);
		//价格
		TextView tvPrice = new TextView(GlobalContext.get());
        tvPrice.setGravity(Gravity.CENTER_VERTICAL);
        tvPrice.setIncludeFontPadding(false);
        tvPrice.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPriceHeight);
        layoutParams.leftMargin = topHorMargin;
		/*layoutParams.leftMargin = tvPriceMarginLeft;
		layoutParams.gravity = Gravity.CENTER_VERTICAL;*/
		llTop.addView(tvPrice,layoutParams);
		//上涨或下跌图标
		ImageView ivUpAndDown = new ImageView(GlobalContext.get());
		//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams = new LinearLayout.LayoutParams(ivUpAndDownSize,ivUpAndDownSize);
		//layoutParams.leftMargin = ivUpAndDownMarginLeft;
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		layoutParams.leftMargin = topHorMargin;
		llTop.addView(ivUpAndDown,layoutParams);

		/*LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,3);
		*//*layoutParams.leftMargin = llDetailMarginLeft;
		layoutParams.topMargin = llDetailMarginTop;*//*
		llDetail.setOrientation(LinearLayout.VERTICAL);
		llContent.addView(llDetail, layoutParams);*/

		LinearLayout llStockInfo0 = new LinearLayout(GlobalContext.get());
        llStockInfo0.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		/*layoutParams.leftMargin = llStockInfoMarginLeft;
		llStockInfo0.setPadding(0, 0, llStockInfoPaddingRight, 0);*/
        layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llStockInfo0, layoutParams);

		//上涨或下跌多少
		/*LinearLayout changePrice = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		//layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
//		layoutParams.topMargin = rlYestodayClosePriceMarginTop;
		llStockInfo0.addView(changePrice,layoutParams);*/

		TextView changePriceLabel = new TextView(GlobalContext.get());
        changePriceLabel.setGravity(Gravity.CENTER_VERTICAL);
		changePriceLabel.setText("涨涨涨值");
		layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo0.addView(changePriceLabel,layoutParams);

		TextView tvChangeAmount = new TextView(GlobalContext.get());
        tvChangeAmount.setGravity(Gravity.CENTER_VERTICAL);
		/*layoutParams.leftMargin = tvChangeAmountMarginLeft;*/
		tvChangeAmount.setSingleLine();
		tvChangeAmount.setEllipsize(TruncateAt.END);
		//tvChangeAmount.setGravity(Gravity.END);
		//layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
		//layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
        llStockInfo0.addView(tvChangeAmount, layoutParams);

		//上涨或下跌指数
		/*LinearLayout changePercent = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		//layoutParams.topMargin = rlTodayOpenPriceMarginTop;
		llStockInfo0.addView(changePercent,layoutParams);*/

		TextView changePercentLabel = new TextView(GlobalContext.get());
        changePercentLabel.setGravity(Gravity.CENTER_VERTICAL);
		changePercentLabel.setText("涨涨涨幅");
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo0.addView(changePercentLabel,layoutParams);

		TextView tvChangeRate = new TextView(GlobalContext.get());
        tvChangeRate.setGravity(Gravity.CENTER_VERTICAL);
		tvChangeRate.setSingleLine();
		tvChangeRate.setEllipsize(TruncateAt.END);
		//tvChangeRate.setGravity(Gravity.END);
		/*layoutParams.leftMargin = tvChangeRateMarginLeft;
		layoutParams.gravity = Gravity.CENTER_VERTICAL;*/
		//layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo0.addView(tvChangeRate,layoutParams);

		LinearLayout llStockInfo = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llStockInfo.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llStockInfo, layoutParams);

		//昨收
		/*LinearLayout rlYestodayClosePrice = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
		llStockInfo.addView(rlYestodayClosePrice,layoutParams);*/

		TextView tvYestodayClosePriceLabel = new TextView(GlobalContext.get());
        tvYestodayClosePriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo.addView(tvYestodayClosePriceLabel,layoutParams);

		TextView tvYestodayClosePrice = new TextView(GlobalContext.get());
		tvYestodayClosePrice.setSingleLine();
		tvYestodayClosePrice.setEllipsize(TruncateAt.END);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
		//layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
        llStockInfo.addView(tvYestodayClosePrice,layoutParams);

        TextView tvHighestPriceLabel = new TextView(GlobalContext.get());
        tvHighestPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo.addView(tvHighestPriceLabel,layoutParams);

        TextView tvHighestPrice = new TextView(GlobalContext.get());
        tvHighestPrice.setSingleLine();
        tvHighestPrice.setEllipsize(TruncateAt.END);
        tvHighestPrice.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        //layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
        llStockInfo.addView(tvHighestPrice,layoutParams);

		//今天
		/*LinearLayout rlTodayOpenPrice = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llStockInfo.addView(rlTodayOpenPrice,layoutParams);*/

		LinearLayout llStockInfo2 = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
		llStockInfo2.setOrientation(LinearLayout.HORIZONTAL);
        //layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llStockInfo2, layoutParams);

        TextView tvTodayOpenPriceLabel = new TextView(GlobalContext.get());
        tvTodayOpenPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo2.addView(tvTodayOpenPriceLabel,layoutParams);

        TextView tvTodayOpenPrice = new TextView(GlobalContext.get());
        tvTodayOpenPrice.setSingleLine();
        tvTodayOpenPrice.setEllipsize(TruncateAt.END);
        tvTodayOpenPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        //layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
        llStockInfo2.addView(tvTodayOpenPrice,layoutParams);

		//最高
		/*LinearLayout rlHighestPrice = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0 ,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llStockInfo2.setGravity(Gravity.CENTER_VERTICAL);
		layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
		llStockInfo2.addView(rlHighestPrice,layoutParams);*/

		//最低
		/*LinearLayout rlLowestPrice = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(0 ,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llStockInfo2.addView(rlLowestPrice,layoutParams);*/

		TextView tvLowestPriceLabel = new TextView(GlobalContext.get());
        tvLowestPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo2.addView(tvLowestPriceLabel,layoutParams);

		TextView tvLowestPrice = new TextView(GlobalContext.get());
		tvLowestPrice.setSingleLine();
		tvLowestPrice.setEllipsize(TruncateAt.END);
        tvHighestPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
		//layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
        llStockInfo2.addView(tvLowestPrice,layoutParams);

		//TextViewUtil.setTextSize(tvTittle,tvTodayOpenPriceSize);
		TextViewUtil.setTextSize(tvName,tvNameSize);
		TextViewUtil.setTextColor(tvName,tvNameColor);
		TextViewUtil.setTextSize(tvCode,detailSize);
		TextViewUtil.setTextColor(tvCode,detailColor);
		TextViewUtil.setTextSize(tvPrice,tvPriceSize);
		TextViewUtil.setTextSize(tvChangeAmount,detailSize);
		TextViewUtil.setTextSize(changePriceLabel,detailSize);
        TextViewUtil.setTextColor(changePriceLabel,detailColor);
		TextViewUtil.setTextSize(changePercentLabel,detailSize);
        TextViewUtil.setTextColor(changePercentLabel,detailColor);
		TextViewUtil.setTextSize(tvChangeRate,detailSize);
		TextViewUtil.setTextSize(tvYestodayClosePrice,detailSize);
		TextViewUtil.setTextSize(tvTodayOpenPrice,detailSize);
		TextViewUtil.setTextSize(tvHighestPrice,detailSize);
		TextViewUtil.setTextSize(tvLowestPrice,detailSize);
		TextViewUtil.setTextSize(tvYestodayClosePriceLabel,detailSize);
        TextViewUtil.setTextColor(tvYestodayClosePriceLabel,detailColor);
		TextViewUtil.setTextSize(tvTodayOpenPriceLabel,detailSize);
        TextViewUtil.setTextColor(tvTodayOpenPriceLabel,detailColor);
		TextViewUtil.setTextSize(tvHighestPriceLabel,detailSize);
        TextViewUtil.setTextColor(tvHighestPriceLabel,detailColor);
		TextViewUtil.setTextSize(tvLowestPriceLabel,detailSize);
        TextViewUtil.setTextColor(tvLowestPriceLabel,detailColor);

		mStockInfo = viewData.mStockInfo;

		tvName.setText(LanguageConvertor.toLocale(mStockInfo.strName));
		tvCode.setText(LanguageConvertor.toLocale(mStockInfo.strCode));

		String price = mStockInfo.strCurrentPrice;
//		if (!TextUtils.isEmpty(price)) {
//			if (price.length() > 6) {
//				price = price.substring(0, 5);
//			}
//			char end = price.charAt(price.length() - 1);
//			if ('.' == end) {
//				price = price.substring(0, price.length() - 1);
//			}
//		}
		tvPrice.setText(subZeroAndDot(format(price)));
		tvPrice.setTextColor(getColorByPrice(
				Float.parseFloat(mStockInfo.strCurrentPrice),
				Float.parseFloat(mStockInfo.strYestodayClosePrice)));
		refreshUpAndDown(ivUpAndDown);

		if (format(mStockInfo.strChangeRate).startsWith("-")){
			changePriceLabel.setText("跌值");
			changePercentLabel.setText("跌幅");
		}else {
			changePriceLabel.setText("涨值");
			changePercentLabel.setText("涨幅");
		}

		if (!TextUtils.isEmpty(mStockInfo.strChangeRate)) {
			tvChangeRate.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeRate))) + "%");
			tvChangeRate.setTextColor(getColorByPrice(
					Float.parseFloat(mStockInfo.strCurrentPrice),
					Float.parseFloat(mStockInfo.strYestodayClosePrice)));


			tvChangeAmount.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeAmount))));
			tvChangeAmount.setTextColor(getColorByPrice(
					Float.parseFloat(mStockInfo.strCurrentPrice),
					Float.parseFloat(mStockInfo.strYestodayClosePrice)));
		}else {
			tvChangeAmount.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeAmount))));
			tvChangeAmount.setTextColor(getColorByPrice(
					Float.parseFloat(mStockInfo.strCurrentPrice),
					Float.parseFloat(mStockInfo.strYestodayClosePrice)));
		}

		tvYestodayClosePriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_yesterday_close_price")));
		tvYestodayClosePrice.setText(format(mStockInfo.strYestodayClosePrice));
        tvYestodayClosePrice.setTextColor(shareColorNormal);

		tvTodayOpenPriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_today_open_price")));
		tvTodayOpenPrice.setText(format(mStockInfo.strTodayOpenPrice));
		tvTodayOpenPrice.setTextColor(getColorByPrice(
				Float.parseFloat(mStockInfo.strTodayOpenPrice),
				Float.parseFloat(mStockInfo.strYestodayClosePrice)));

		tvHighestPriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_highest_price")));
		tvHighestPrice.setText(format(mStockInfo.strHighestPrice));
		tvHighestPrice.setTextColor(getColorByPrice(
				Float.parseFloat(mStockInfo.strHighestPrice),
				Float.parseFloat(mStockInfo.strYestodayClosePrice)));

		tvLowestPriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_lowest_price")));
		tvLowestPrice.setText(format(mStockInfo.strLowestPrice));
		tvLowestPrice.setTextColor(getColorByPrice(
				Float.parseFloat(mStockInfo.strLowestPrice),
				Float.parseFloat(mStockInfo.strYestodayClosePrice)));


		return mLayout;
	}

    private View createViewHalf(ChatShockViewData viewData){
        LinearLayout mLayout = new LinearLayout(GlobalContext.get());
        mLayout.setGravity(Gravity.CENTER_VERTICAL);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setPadding(0,verPadding,0,verPadding);

       /* LinearLayout ltittle = new LinearLayout(GlobalContext.get());
        ltittle.setOrientation(LinearLayout.HORIZONTAL);
        ltittle.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
        mLayout.addView(ltittle,layoutParams);

        ImageView ivIcon = new ImageView(GlobalContext.get());
        ivIcon.setImageDrawable(LayouUtil.getDrawable("title_icon_shares"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
        ltittle.addView(ivIcon,layoutParams);

        TextView tvTittle = new TextView(GlobalContext.get());
        tvTittle.setText("股票");
        tvTittle.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,SizeConfig.titleHeight);
        ltittle.addView(tvTittle,layoutParams);*/
        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null,"shares","股票");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mLayout.addView(titleViewAdapter.view,layoutParams);

        FrameLayout frameLayout = new FrameLayout(GlobalContext.get());
        frameLayout.setBackground(LayouUtil.getDrawable("white_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,countHeight);
        mLayout.addView(frameLayout,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        flLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flLayoutParams.leftMargin = leftMargin;
        frameLayout.addView(llContent,flLayoutParams);

        LinearLayout llTop = new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llTop,layoutParams);

        LinearLayout llName = new LinearLayout(GlobalContext.get());
        llName.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(llName,layoutParams);

        //股票名
        TextView tvName = new TextView(GlobalContext.get());
        tvName.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvNameHeight);
        layoutParams.bottomMargin = tvNameBottomMargin;
        llName.addView(tvName, layoutParams);
        //股票代码
        TextView tvCode = new TextView(GlobalContext.get());
        tvCode.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,detailHeight);
        llName.addView(tvCode, layoutParams);
        //价格
        TextView tvPrice = new TextView(GlobalContext.get());
        tvPrice.setGravity(Gravity.CENTER_VERTICAL);
        tvPrice.setIncludeFontPadding(false);
        tvPrice.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPriceHeight);
        layoutParams.leftMargin = topHorMargin;
        llTop.addView(tvPrice,layoutParams);
        //上涨或下跌图标
        ImageView ivUpAndDown = new ImageView(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(ivUpAndDownSize,ivUpAndDownSize);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = topHorMargin;
        llTop.addView(ivUpAndDown,layoutParams);

        LinearLayout llStockInfo0 = new LinearLayout(GlobalContext.get());
        llStockInfo0.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llStockInfo0, layoutParams);

        TextView changePriceLabel = new TextView(GlobalContext.get());
        changePriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        changePriceLabel.setText("涨涨涨值");
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo0.addView(changePriceLabel,layoutParams);

        TextView tvChangeAmount = new TextView(GlobalContext.get());
        tvChangeAmount.setGravity(Gravity.CENTER_VERTICAL);
        tvChangeAmount.setSingleLine();
        tvChangeAmount.setEllipsize(TruncateAt.END);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo0.addView(tvChangeAmount, layoutParams);

        TextView changePercentLabel = new TextView(GlobalContext.get());
        changePercentLabel.setGravity(Gravity.CENTER_VERTICAL);
        changePercentLabel.setText("涨涨涨幅");
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo0.addView(changePercentLabel,layoutParams);

        TextView tvChangeRate = new TextView(GlobalContext.get());
        tvChangeRate.setGravity(Gravity.CENTER_VERTICAL);
        tvChangeRate.setSingleLine();
        tvChangeRate.setEllipsize(TruncateAt.END);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo0.addView(tvChangeRate,layoutParams);

        LinearLayout llStockInfo = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llStockInfo.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llStockInfo, layoutParams);

        TextView tvYestodayClosePriceLabel = new TextView(GlobalContext.get());
        tvYestodayClosePriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo.addView(tvYestodayClosePriceLabel,layoutParams);

        TextView tvYestodayClosePrice = new TextView(GlobalContext.get());
        tvYestodayClosePrice.setSingleLine();
        tvYestodayClosePrice.setEllipsize(TruncateAt.END);
        tvYestodayClosePriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo.addView(tvYestodayClosePrice,layoutParams);

        TextView tvHighestPriceLabel = new TextView(GlobalContext.get());
        tvHighestPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo.addView(tvHighestPriceLabel,layoutParams);

        TextView tvHighestPrice = new TextView(GlobalContext.get());
        tvHighestPrice.setSingleLine();
        tvHighestPrice.setEllipsize(TruncateAt.END);
        tvHighestPrice.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo.addView(tvHighestPrice,layoutParams);

        LinearLayout llStockInfo2 = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llStockInfo2.setOrientation(LinearLayout.HORIZONTAL);
        //layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llStockInfo2, layoutParams);

        TextView tvTodayOpenPriceLabel = new TextView(GlobalContext.get());
        tvTodayOpenPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo2.addView(tvTodayOpenPriceLabel,layoutParams);

        TextView tvTodayOpenPrice = new TextView(GlobalContext.get());
        tvTodayOpenPrice.setSingleLine();
        tvTodayOpenPrice.setEllipsize(TruncateAt.END);
        tvTodayOpenPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo2.addView(tvTodayOpenPrice,layoutParams);

        TextView tvLowestPriceLabel = new TextView(GlobalContext.get());
        tvLowestPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo2.addView(tvLowestPriceLabel,layoutParams);

        TextView tvLowestPrice = new TextView(GlobalContext.get());
        tvLowestPrice.setSingleLine();
        tvLowestPrice.setEllipsize(TruncateAt.END);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo2.addView(tvLowestPrice,layoutParams);

        TextViewUtil.setTextSize(tvName,tvNameSize);
        TextViewUtil.setTextColor(tvName,tvNameColor);
        TextViewUtil.setTextSize(tvCode,detailSize);
        TextViewUtil.setTextColor(tvCode,detailColor);
        TextViewUtil.setTextSize(tvPrice,tvPriceSize);
        TextViewUtil.setTextSize(tvChangeAmount,detailSize);
        TextViewUtil.setTextSize(changePriceLabel,detailSize);
        TextViewUtil.setTextColor(changePriceLabel,detailColor);
        TextViewUtil.setTextSize(changePercentLabel,detailSize);
        TextViewUtil.setTextColor(changePercentLabel,detailColor);
        TextViewUtil.setTextSize(tvChangeRate,detailSize);
        TextViewUtil.setTextSize(tvYestodayClosePrice,detailSize);
        TextViewUtil.setTextSize(tvTodayOpenPrice,detailSize);
        TextViewUtil.setTextSize(tvHighestPrice,detailSize);
        TextViewUtil.setTextSize(tvLowestPrice,detailSize);
        TextViewUtil.setTextSize(tvYestodayClosePriceLabel,detailSize);
        TextViewUtil.setTextColor(tvYestodayClosePriceLabel,detailColor);
        TextViewUtil.setTextSize(tvTodayOpenPriceLabel,detailSize);
        TextViewUtil.setTextColor(tvTodayOpenPriceLabel,detailColor);
        TextViewUtil.setTextSize(tvHighestPriceLabel,detailSize);
        TextViewUtil.setTextColor(tvHighestPriceLabel,detailColor);
        TextViewUtil.setTextSize(tvLowestPriceLabel,detailSize);
        TextViewUtil.setTextColor(tvLowestPriceLabel,detailColor);

        mStockInfo = viewData.mStockInfo;

        tvName.setText(LanguageConvertor.toLocale(mStockInfo.strName));
        tvCode.setText(LanguageConvertor.toLocale(mStockInfo.strCode));

        String price = mStockInfo.strCurrentPrice;
//        if (!TextUtils.isEmpty(price)) {
//            if (price.length() > 6) {
//                price = price.substring(0, 5);
//            }
//            char end = price.charAt(price.length() - 1);
//            if ('.' == end) {
//                price = price.substring(0, price.length() - 1);
//            }
//        }
        tvPrice.setText(subZeroAndDot(format(price)));
        tvPrice.setTextColor(getColorByPrice(
                Float.parseFloat(mStockInfo.strCurrentPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice)));
        refreshUpAndDown(ivUpAndDown);

        if (format(mStockInfo.strChangeRate).startsWith("-")){
            changePriceLabel.setText("跌值");
            changePercentLabel.setText("跌幅");
        }else {
            changePriceLabel.setText("涨值");
            changePercentLabel.setText("涨幅");
        }

        if (!TextUtils.isEmpty(mStockInfo.strChangeRate)) {
            tvChangeRate.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeRate))) + "%");
            tvChangeRate.setTextColor(getColorByPrice(
                    Float.parseFloat(mStockInfo.strCurrentPrice),
                    Float.parseFloat(mStockInfo.strYestodayClosePrice)));


            tvChangeAmount.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeAmount))));
            tvChangeAmount.setTextColor(getColorByPrice(
                    Float.parseFloat(mStockInfo.strCurrentPrice),
                    Float.parseFloat(mStockInfo.strYestodayClosePrice)));
        }else {
            tvChangeAmount.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeAmount))));
            tvChangeAmount.setTextColor(getColorByPrice(
                    Float.parseFloat(mStockInfo.strCurrentPrice),
                    Float.parseFloat(mStockInfo.strYestodayClosePrice)));
        }

        tvYestodayClosePriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_yesterday_close_price")));
        tvYestodayClosePrice.setText(format(mStockInfo.strYestodayClosePrice));
        tvYestodayClosePrice.setTextColor(shareColorNormal);

        tvTodayOpenPriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_today_open_price")));
        tvTodayOpenPrice.setText(format(mStockInfo.strTodayOpenPrice));
        tvTodayOpenPrice.setTextColor(getColorByPrice(
                Float.parseFloat(mStockInfo.strTodayOpenPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice)));

        tvHighestPriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_highest_price")));
        tvHighestPrice.setText(format(mStockInfo.strHighestPrice));
        tvHighestPrice.setTextColor(getColorByPrice(
                Float.parseFloat(mStockInfo.strHighestPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice)));

        tvLowestPriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_lowest_price")));
        tvLowestPrice.setText(format(mStockInfo.strLowestPrice));
        tvLowestPrice.setTextColor(getColorByPrice(
                Float.parseFloat(mStockInfo.strLowestPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice)));


        return mLayout;
    }

    private View createViewNone(ChatShockViewData viewData){
        LinearLayout mLayout = new LinearLayout(GlobalContext.get());
        mLayout.setGravity(Gravity.CENTER_VERTICAL);
        mLayout.setOrientation(LinearLayout.VERTICAL);

        FrameLayout frameLayout = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        mLayout.addView(frameLayout,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        flLayoutParams.gravity = Gravity.CENTER;
        flLayoutParams.leftMargin = leftMargin;
        frameLayout.addView(llContent,flLayoutParams);

        LinearLayout llTop = new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llTop,layoutParams);

        LinearLayout llName = new LinearLayout(GlobalContext.get());
        llName.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(llName,layoutParams);

        //股票名
        TextView tvName = new TextView(GlobalContext.get());
        tvName.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvNameHeight);
        layoutParams.bottomMargin = tvNameBottomMargin;
        llName.addView(tvName, layoutParams);
        //股票代码
        TextView tvCode = new TextView(GlobalContext.get());
        tvCode.setGravity(Gravity.CENTER_VERTICAL);
        tvCode.setIncludeFontPadding(false);
        //layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,detailHeight);
        llName.addView(tvCode, layoutParams);
        //价格
        TextView tvPrice = new TextView(GlobalContext.get());
        tvPrice.setGravity(Gravity.CENTER_VERTICAL);
        tvPrice.setIncludeFontPadding(false);
        tvPrice.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPriceHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = topHorMargin;
        llTop.addView(tvPrice,layoutParams);
        //上涨或下跌图标
        ImageView ivUpAndDown = new ImageView(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(ivUpAndDownSize,ivUpAndDownSize);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = topHorMargin;
        llTop.addView(ivUpAndDown,layoutParams);

        LinearLayout llStockInfo0 = new LinearLayout(GlobalContext.get());
        llStockInfo0.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llStockInfo0, layoutParams);

        TextView changePriceLabel = new TextView(GlobalContext.get());
        changePriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        changePriceLabel.setText("涨涨涨值");
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo0.addView(changePriceLabel,layoutParams);

        TextView tvChangeAmount = new TextView(GlobalContext.get());
        tvChangeAmount.setGravity(Gravity.CENTER_VERTICAL);
        tvChangeAmount.setSingleLine();
        tvChangeAmount.setEllipsize(TruncateAt.END);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo0.addView(tvChangeAmount, layoutParams);

        TextView changePercentLabel = new TextView(GlobalContext.get());
        changePercentLabel.setGravity(Gravity.CENTER_VERTICAL);
        changePercentLabel.setText("涨涨涨幅");
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo0.addView(changePercentLabel,layoutParams);

        TextView tvChangeRate = new TextView(GlobalContext.get());
        tvChangeRate.setGravity(Gravity.CENTER_VERTICAL);
        tvChangeRate.setSingleLine();
        tvChangeRate.setEllipsize(TruncateAt.END);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo0.addView(tvChangeRate,layoutParams);

        LinearLayout llStockInfo = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llStockInfo.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llStockInfo, layoutParams);

        TextView tvYestodayClosePriceLabel = new TextView(GlobalContext.get());
        tvYestodayClosePriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo.addView(tvYestodayClosePriceLabel,layoutParams);

        TextView tvYestodayClosePrice = new TextView(GlobalContext.get());
        tvYestodayClosePrice.setSingleLine();
        tvYestodayClosePrice.setEllipsize(TruncateAt.END);
        tvYestodayClosePriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo.addView(tvYestodayClosePrice,layoutParams);

        TextView tvHighestPriceLabel = new TextView(GlobalContext.get());
        tvHighestPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo.addView(tvHighestPriceLabel,layoutParams);

        TextView tvHighestPrice = new TextView(GlobalContext.get());
        tvHighestPrice.setSingleLine();
        tvHighestPrice.setEllipsize(TruncateAt.END);
        tvHighestPrice.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo.addView(tvHighestPrice,layoutParams);

        LinearLayout llStockInfo2 = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llStockInfo2.setOrientation(LinearLayout.HORIZONTAL);
        //layoutParams.bottomMargin = detailVerMargin;
        llContent.addView(llStockInfo2, layoutParams);

        TextView tvTodayOpenPriceLabel = new TextView(GlobalContext.get());
        tvTodayOpenPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo2.addView(tvTodayOpenPriceLabel,layoutParams);

        TextView tvTodayOpenPrice = new TextView(GlobalContext.get());
        tvTodayOpenPrice.setSingleLine();
        tvTodayOpenPrice.setEllipsize(TruncateAt.END);
        tvTodayOpenPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo2.addView(tvTodayOpenPrice,layoutParams);

        TextView tvLowestPriceLabel = new TextView(GlobalContext.get());
        tvLowestPriceLabel.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(detailNameWidth,detailHeight);
        llStockInfo2.addView(tvLowestPriceLabel,layoutParams);

        TextView tvLowestPrice = new TextView(GlobalContext.get());
        tvLowestPrice.setSingleLine();
        tvLowestPrice.setEllipsize(TruncateAt.END);
        layoutParams = new LinearLayout.LayoutParams(detailNumWidth,detailHeight);
        llStockInfo2.addView(tvLowestPrice,layoutParams);

        TextViewUtil.setTextSize(tvName,tvNameSize);
        TextViewUtil.setTextColor(tvName,tvNameColor);
        TextViewUtil.setTextSize(tvCode,detailSize);
        TextViewUtil.setTextColor(tvCode,detailColor);
        TextViewUtil.setTextSize(tvPrice,tvPriceSize);
        TextViewUtil.setTextSize(tvChangeAmount,detailSize);
        TextViewUtil.setTextSize(changePriceLabel,detailSize);
        TextViewUtil.setTextColor(changePriceLabel,detailColor);
        TextViewUtil.setTextSize(changePercentLabel,detailSize);
        TextViewUtil.setTextColor(changePercentLabel,detailColor);
        TextViewUtil.setTextSize(tvChangeRate,detailSize);
        TextViewUtil.setTextSize(tvYestodayClosePrice,detailSize);
        TextViewUtil.setTextSize(tvTodayOpenPrice,detailSize);
        TextViewUtil.setTextSize(tvHighestPrice,detailSize);
        TextViewUtil.setTextSize(tvLowestPrice,detailSize);
        TextViewUtil.setTextSize(tvYestodayClosePriceLabel,detailSize);
        TextViewUtil.setTextColor(tvYestodayClosePriceLabel,detailColor);
        TextViewUtil.setTextSize(tvTodayOpenPriceLabel,detailSize);
        TextViewUtil.setTextColor(tvTodayOpenPriceLabel,detailColor);
        TextViewUtil.setTextSize(tvHighestPriceLabel,detailSize);
        TextViewUtil.setTextColor(tvHighestPriceLabel,detailColor);
        TextViewUtil.setTextSize(tvLowestPriceLabel,detailSize);
        TextViewUtil.setTextColor(tvLowestPriceLabel,detailColor);

        mStockInfo = viewData.mStockInfo;

        tvName.setText(LanguageConvertor.toLocale(mStockInfo.strName));
        tvCode.setText(LanguageConvertor.toLocale(mStockInfo.strCode));

        String price = mStockInfo.strCurrentPrice;
//        if (!TextUtils.isEmpty(price)) {
//            if (price.length() > 6) {
//                price = price.substring(0, 5);
//            }
//            char end = price.charAt(price.length() - 1);
//            if ('.' == end) {
//                price = price.substring(0, price.length() - 1);
//            }
//        }
        tvPrice.setText(subZeroAndDot(format(price)));
        tvPrice.setTextColor(getColorByPrice(
                Float.parseFloat(mStockInfo.strCurrentPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice)));
        refreshUpAndDown(ivUpAndDown);

        if (format(mStockInfo.strChangeRate).startsWith("-")){
            changePriceLabel.setText("跌值");
            changePercentLabel.setText("跌幅");
        }else {
            changePriceLabel.setText("涨值");
            changePercentLabel.setText("涨幅");
        }

        if (!TextUtils.isEmpty(mStockInfo.strChangeRate)) {
            tvChangeRate.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeRate))) + "%");
            tvChangeRate.setTextColor(getColorByPrice(
                    Float.parseFloat(mStockInfo.strCurrentPrice),
                    Float.parseFloat(mStockInfo.strYestodayClosePrice)));


            tvChangeAmount.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeAmount))));
            tvChangeAmount.setTextColor(getColorByPrice(
                    Float.parseFloat(mStockInfo.strCurrentPrice),
                    Float.parseFloat(mStockInfo.strYestodayClosePrice)));
        }else {
            tvChangeAmount.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeAmount))));
            tvChangeAmount.setTextColor(getColorByPrice(
                    Float.parseFloat(mStockInfo.strCurrentPrice),
                    Float.parseFloat(mStockInfo.strYestodayClosePrice)));
        }

        tvYestodayClosePriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_yesterday_close_price")));
        tvYestodayClosePrice.setText(format(mStockInfo.strYestodayClosePrice));
        tvYestodayClosePrice.setTextColor(shareColorNormal);

        tvTodayOpenPriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_today_open_price")));
        tvTodayOpenPrice.setText(format(mStockInfo.strTodayOpenPrice));
        tvTodayOpenPrice.setTextColor(getColorByPrice(
                Float.parseFloat(mStockInfo.strTodayOpenPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice)));

        tvHighestPriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_highest_price")));
        tvHighestPrice.setText(format(mStockInfo.strHighestPrice));
        tvHighestPrice.setTextColor(getColorByPrice(
                Float.parseFloat(mStockInfo.strHighestPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice)));

        tvLowestPriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_lowest_price")));
        tvLowestPrice.setText(format(mStockInfo.strLowestPrice));
        tvLowestPrice.setTextColor(getColorByPrice(
                Float.parseFloat(mStockInfo.strLowestPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice)));


        return mLayout;
    }

	private String addMark(String value){
		if (!value.startsWith("-")) {
			value = "+"+value;
		}
		return value;
	}
	private String format(String value) {
		if (TextUtils.isEmpty(value)) {
			return "";
		}
		return String.format("%.2f", Float.parseFloat(value));
	}
	public static String subZeroAndDot(String s){    
        if(s.indexOf(".") > 0){    
            s = s.replaceAll("0+?$", "");//去掉多余的0    
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉    
        }    
        return s;    
    }

	private int getColorByPrice(float curprice, float compareprice) {
	    if (curprice == 0){  //停牌股票的昨收不为0，其他为0
	        return shareColorNormal;
        }
		if (curprice > compareprice)
			return shareColorUp;
		else if (curprice < compareprice)
			return shareColorDown;
		else
			return shareColorNormal;
	}
	
	private void refreshUpAndDown(ImageView ivUpAndDowm) {
		float todayOpenPrice = Float
				.parseFloat(mStockInfo.strYestodayClosePrice);
		float currentPrice = Float.parseFloat(mStockInfo.strCurrentPrice);
		ivUpAndDowm.setVisibility(View.VISIBLE);
		if (currentPrice == 0){  //停牌股票的昨收不为0，其他为0
            ivUpAndDowm.setVisibility(View.INVISIBLE);
            return;
        }
		if (currentPrice > todayOpenPrice) {
			ivUpAndDowm.setImageDrawable(LayouUtil.getDrawable("stock_up_icon"));
		} else if (currentPrice < todayOpenPrice) {
			ivUpAndDowm.setImageDrawable(LayouUtil.getDrawable("stock_down_icon"));
		} else {
			ivUpAndDowm.setVisibility(View.INVISIBLE);
		}
	}

	
	
}
