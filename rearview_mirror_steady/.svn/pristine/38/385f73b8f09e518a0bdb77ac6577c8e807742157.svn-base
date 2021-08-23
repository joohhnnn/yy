package com.txznet.resholder.theme.ironman.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
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
	
	private BoundedLinearLayout mLayout;
	
	private StockInfo mStockInfo;
	private int mLayoutMaxWidth;
	private int llContentPaddingLeft;
	private int llContentPaddingTop;
	private int llContentPaddingRight;
	private int llContentPaddingBottom;
	private int llTopPaddingTop;
	private int llTopPaddingBottom;
	private int llTopMarginLeft;
	private int llTopMarginRight;
	private int tvPriceMarginLeft;
	private int ivUpAndDownMarginLeft;
	private int tvChangeAmountMarginLeft;
	private int tvChangeRateMarginLeft;
	private int llDetailMarginLeft;
	private int llDetailMarginTop;
	private int ivPicWidth;
	private int ivPicHeight;
	private int llStockInfoWidth ;
	private int llStockInfoMarginLeft ;
	private int llStockInfoPaddingTop ;
	private int llStockInfoPaddingBottom ;
	private int rlYestodayClosePriceMarginTop;
	private int rlTodayOpenPriceMarginTop;
	private int rlHighestPriceMarginTop;
	private int rlLowestPriceMarginTop;
	private float tvNameSize;
	private int tvNameColor;
	private float tvCodeSize;
	private int tvCodeColor ;
	private float tvPriceSize;
	private float tvChangeAmountSize;
	private float tvChangeRateSize;
	private float tvYestodayClosePriceSize;
	private float tvTodayOpenPriceSize;
	private float tvHighestPriceSize;
	private float tvLowestPriceSize;
	private float tvYestodayClosePriceLabelSize;
	private float tvTodayOpenPriceLabelSize;
	private float tvHighestPriceLabelSize;
	private float tvLowestPriceLabelSize;
	private int shareColorUp;
	private int shareColorDown;
	private int shareColorNormal;
	private Drawable upDrawable;
	private Drawable downDrawable;
	@Override
	public void init() {
		mLayoutMaxWidth = (int) ThemeConfigManager.getX(ThemeConfigManager.STOCK_MAXWIDTH);
		llContentPaddingLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_CONTENT_PADDINGLEFT);
		llContentPaddingTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_PADDINGTOP);
		llContentPaddingRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_CONTENT_PADDINGRIGHT);
		llContentPaddingBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_PADDINGBOTTOM);
		llTopPaddingTop = (int)LayouUtil.getDimen("y5");
		llTopPaddingBottom = (int)LayouUtil.getDimen("y5");
		llTopMarginLeft = (int) LayouUtil.getDimen("x20");
		llTopMarginRight = (int) LayouUtil.getDimen("x10");
		tvPriceMarginLeft = (int) LayouUtil.getDimen("x10");
		ivUpAndDownMarginLeft = (int) LayouUtil.getDimen("x10");
		tvChangeAmountMarginLeft = (int) LayouUtil.getDimen("x10");
		tvChangeRateMarginLeft = (int) LayouUtil.getDimen("x10");
		llDetailMarginLeft = (int) LayouUtil.getDimen("x20");
		llDetailMarginTop = (int) LayouUtil.getDimen("y25");
		ivPicWidth = (int) ThemeConfigManager.getX(ThemeConfigManager.STOCK_PIC_WIDTH);
		ivPicHeight = (int) ThemeConfigManager.getY(ThemeConfigManager.STOCK_PIC_HEIGHT);
		llStockInfoWidth = (int) ThemeConfigManager.getX(ThemeConfigManager.STOCK_INFO_LY_WIDTH);
		llStockInfoMarginLeft = (int) LayouUtil.getDimen("x20");
		llStockInfoPaddingTop = (int)LayouUtil.getDimen("y20");
		llStockInfoPaddingBottom = (int)LayouUtil.getDimen("y20");
		rlYestodayClosePriceMarginTop = (int) LayouUtil.getDimen("y10");
		rlTodayOpenPriceMarginTop = (int) LayouUtil.getDimen("y20");
		rlHighestPriceMarginTop = (int) LayouUtil.getDimen("y20");
		rlLowestPriceMarginTop = (int) LayouUtil.getDimen("y20");
		
		tvNameSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_NAME_SIZE1);
		tvNameColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_NAME_COLOR1);
		tvCodeSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_NAME_SIZE2);
		tvCodeColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_NAME_COLOR2);
		tvPriceSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_VALUE_SIZE1);
		tvChangeAmountSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_RISE_SIZE1);
		tvChangeRateSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_RISE_SIZE1);
		tvYestodayClosePriceSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
		tvTodayOpenPriceSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
		tvHighestPriceSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
		tvLowestPriceSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
		tvYestodayClosePriceLabelSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
		tvTodayOpenPriceLabelSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
		tvHighestPriceLabelSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
		tvLowestPriceLabelSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SHARE_ITEM_SIZE1);
		
		shareColorUp = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_VALUE_COLOR1);
		shareColorDown = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_VALUE_COLOR2);
		shareColorNormal = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SHARE_ITEM_COLOR2);
		upDrawable = LayouUtil.getDrawable("stock_up_icon");
		downDrawable = LayouUtil.getDrawable("stock_down_icon");
	}
	
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatShockViewData viewData = (ChatShockViewData) data;
		mLayout = new BoundedLinearLayout(GlobalContext.get());
		mLayout.setBoundedWidth(mLayoutMaxWidth);
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		RelativeLayout.LayoutParams mRelativeLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		mLayout.addView(llContent,mRelativeLayoutParams);
		
		LinearLayout llTop = new LinearLayout(GlobalContext.get());
		llTop.setOrientation(LinearLayout.HORIZONTAL);
//		llTop.setPadding(0, llTopPaddingTop, 0, llTopPaddingBottom);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		layoutParams.leftMargin = llTopMarginLeft;
//		layoutParams.rightMargin = llTopMarginRight;
//		layoutParams.topMargin = llContentPaddingTop;
		llContent.addView(llTop,layoutParams);
		
		LinearLayout llName = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		llName.setOrientation(LinearLayout.VERTICAL);
		llName.setGravity(Gravity.CENTER_VERTICAL);
		llTop.addView(llName,layoutParams);
		
		//股票名
		TextView tvName = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llName.addView(tvName, layoutParams);
		//股票代码
		TextView tvCode = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llName.addView(tvCode, layoutParams);
		//价格
		TextView tvPrice = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = tvPriceMarginLeft;
		tvPrice.setIncludeFontPadding(false);
		tvPrice.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		llTop.addView(tvPrice,layoutParams);
		//上涨或下跌图标
		ImageView ivUpAndDown = new ImageView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = ivUpAndDownMarginLeft;
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTop.addView(ivUpAndDown,layoutParams);
		//上涨或下跌多少
		TextView tvChangeAmount = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = tvChangeAmountMarginLeft;
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTop.addView(tvChangeAmount, layoutParams);
		//上涨或下跌指数
		TextView tvChangeRate = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = tvChangeRateMarginLeft;
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTop.addView(tvChangeRate,layoutParams);
		tvChangeRate.setSingleLine();
		tvChangeRate.setEllipsize(TruncateAt.END);
		//分割线
//		View vDivider = new View(GlobalContext.get());
//		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
//		vDivider.setBackgroundColor(Color.parseColor("#AAFFFFFF"));
//		llContent.addView(vDivider,layoutParams);
		
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
//		layoutParams.leftMargin = llDetailMarginLeft;
//		layoutParams.topMargin = llDetailMarginTop;
//		layoutParams.bottomMargin = llContentPaddingBottom;
		llDetail.setOrientation(LinearLayout.HORIZONTAL);
		llContent.addView(llDetail, layoutParams);
		//股票图片
		ImageView ivPic = new ImageView(GlobalContext.get());
		ivPic.setScaleType(ScaleType.FIT_XY);
		layoutParams = new LinearLayout.LayoutParams(ivPicWidth,ivPicHeight);
		layoutParams.topMargin = (int) LayouUtil.getDimen("y20");
		llDetail.addView(ivPic, layoutParams);
		
		LinearLayout llStockInfo = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = llStockInfoMarginLeft;
		llStockInfo.setPadding(0, llStockInfoPaddingTop, 0, 0);
		llStockInfo.setOrientation(LinearLayout.VERTICAL);
		llDetail.addView(llStockInfo, layoutParams);
		//昨收
		LinearLayout rlYestodayClosePrice = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT ,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.topMargin = rlYestodayClosePriceMarginTop;
		llStockInfo.addView(rlYestodayClosePrice,layoutParams);
		
		TextView tvYestodayClosePriceLabel = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		rlYestodayClosePrice.addView(tvYestodayClosePriceLabel,layoutParams);
		
		TextView tvYestodayClosePrice = new TextView(GlobalContext.get());
		tvYestodayClosePrice.setSingleLine();
		tvYestodayClosePrice.setEllipsize(TruncateAt.END);
		tvYestodayClosePrice.setGravity(Gravity.END);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		mRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
		rlYestodayClosePrice.addView(tvYestodayClosePrice,layoutParams);
		//今天
		
		LinearLayout rlTodayOpenPrice = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT ,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.topMargin = rlTodayOpenPriceMarginTop;
		llStockInfo.addView(rlTodayOpenPrice,layoutParams);
		
		TextView tvTodayOpenPriceLabel = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		rlTodayOpenPrice.addView(tvTodayOpenPriceLabel,layoutParams);
		
		TextView tvTodayOpenPrice = new TextView(GlobalContext.get());
		tvTodayOpenPrice.setSingleLine();
		tvTodayOpenPrice.setEllipsize(TruncateAt.END);
		tvTodayOpenPrice.setGravity(Gravity.END);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		mRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
		rlTodayOpenPrice.addView(tvTodayOpenPrice,layoutParams);
		//最高
		LinearLayout rlHighestPrice = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT ,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.topMargin = rlHighestPriceMarginTop;
		llStockInfo.addView(rlHighestPrice,layoutParams);
		
		TextView tvHighestPriceLabel = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		rlHighestPrice.addView(tvHighestPriceLabel,layoutParams);
		
		TextView tvHighestPrice = new TextView(GlobalContext.get());
		tvHighestPrice.setSingleLine();
		tvHighestPrice.setEllipsize(TruncateAt.END);
		tvHighestPrice.setGravity(Gravity.END);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		mRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
		rlHighestPrice.addView(tvHighestPrice,layoutParams);
		//最低
		LinearLayout rlLowestPrice = new LinearLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT ,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.topMargin = rlLowestPriceMarginTop;
		llStockInfo.addView(rlLowestPrice,layoutParams);
		
		TextView tvLowestPriceLabel = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		rlLowestPrice.addView(tvLowestPriceLabel,layoutParams);
		
		TextView tvLowestPrice = new TextView(GlobalContext.get());
		tvLowestPrice.setSingleLine();
		tvLowestPrice.setEllipsize(TruncateAt.END);
		tvLowestPrice.setGravity(Gravity.END);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		mRelativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.leftMargin = (int) LayouUtil.getDimen("x20");
		rlLowestPrice.addView(tvLowestPrice,layoutParams);
		
		TextViewUtil.setTextSize(tvName,tvNameSize);
		TextViewUtil.setTextColor(tvName,tvNameColor);
		TextViewUtil.setTextSize(tvCode,tvCodeSize);
		TextViewUtil.setTextColor(tvCode,tvCodeColor);
		TextViewUtil.setTextSize(tvPrice,tvPriceSize);
		TextViewUtil.setTextSize(tvChangeAmount,tvChangeAmountSize);
		TextViewUtil.setTextSize(tvChangeRate,tvChangeRateSize);
		TextViewUtil.setTextSize(tvYestodayClosePrice,tvYestodayClosePriceSize);
		TextViewUtil.setTextSize(tvTodayOpenPrice,tvTodayOpenPriceSize);
		TextViewUtil.setTextSize(tvHighestPrice,tvHighestPriceSize);
		TextViewUtil.setTextSize(tvLowestPrice,tvLowestPriceSize);
		TextViewUtil.setTextSize(tvYestodayClosePriceLabel,tvYestodayClosePriceLabelSize);
		TextViewUtil.setTextSize(tvTodayOpenPriceLabel,tvTodayOpenPriceLabelSize);
		TextViewUtil.setTextSize(tvHighestPriceLabel,tvHighestPriceLabelSize);
		TextViewUtil.setTextSize(tvLowestPriceLabel,tvLowestPriceLabelSize);
		
		mStockInfo = viewData.mStockInfo;
		refreshBackGround(llContent);
		llContent.setPadding(llContentPaddingLeft,llContentPaddingTop,llContentPaddingRight,llContentPaddingBottom);
		
		tvName.setText(LanguageConvertor.toLocale(mStockInfo.strName));
		tvCode.setText(LanguageConvertor.toLocale(mStockInfo.strCode));

		String price = mStockInfo.strCurrentPrice;
		if (price.length() > 6) {
			price = price.substring(0, 5);
		}
		char end = price.charAt(price.length() - 1);
		if ('.' == end) {
			price = price.substring(0, price.length() - 1);
		}
		tvPrice.setText(subZeroAndDot(format(price)));
		tvPrice.setTextColor(getColorByPrice(
				Float.parseFloat(mStockInfo.strCurrentPrice),
				Float.parseFloat(mStockInfo.strYestodayClosePrice)));
		refreshUpAndDown(ivUpAndDown);

		if (!TextUtils.isEmpty(mStockInfo.strChangeRate)) {
			tvChangeRate.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeRate))) + "%");
			tvChangeRate.setTextColor(getColorByPrice(
					Float.parseFloat(mStockInfo.strCurrentPrice),
					Float.parseFloat(mStockInfo.strYestodayClosePrice)));
		}else {
//			tvChangeAmount.setText(addMark(subZeroAndDot(format(mStockInfo.strChangeAmount))));
//			tvChangeAmount.setTextColor(getColorByPrice(
//					Float.parseFloat(mStockInfo.strCurrentPrice),
//					Float.parseFloat(mStockInfo.strYestodayClosePrice)));
		}
		
		tvYestodayClosePriceLabel.setText(LanguageConvertor.toLocale(LayouUtil.getString("label_stock_yesterday_close_price")));
		tvYestodayClosePrice.setText(format(mStockInfo.strYestodayClosePrice));

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

		ImageLoader.getInstance().displayImage(mStockInfo.strUrl,ivPic );
		
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = mLayout;
		adapter.object = ChatShockView.getInstance();
		return adapter;
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
		if (currentPrice > todayOpenPrice) {
			ivUpAndDowm.setImageDrawable(upDrawable);
		} else if (currentPrice < todayOpenPrice) {
			ivUpAndDowm.setImageDrawable(downDrawable);
		} else {
			ivUpAndDowm.setVisibility(View.INVISIBLE);
		}
	}
	
	private void refreshBackGround(View view){
		float todayOpenPrice = Float
				.parseFloat(mStockInfo.strYestodayClosePrice);
		float currentPrice = Float.parseFloat(mStockInfo.strCurrentPrice);
		if (currentPrice > todayOpenPrice) {
			view.setBackground(LayouUtil.getDrawable("shares_bg_up"));
		} else if (currentPrice < todayOpenPrice) {
			view.setBackground(LayouUtil.getDrawable("shares_bg_down"));
		} else {
			view.setBackground(LayouUtil.getDrawable("list_bg"));
		}
	}

	
	
}
