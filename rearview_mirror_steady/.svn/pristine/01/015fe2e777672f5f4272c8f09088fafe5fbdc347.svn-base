package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ListViewData;
import com.txznet.comm.ui.viewfactory.data.ListViewData.TitleInfo;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IListTitleView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.LanguageConvertor;

public class ListTitleView extends IListTitleView{
	protected int mCurPage;
	protected int mMaxPage;
	protected String mPrefix;
	protected String mCityfix;
	protected String mMidfix;
	protected String mTitlefix;
	protected String mAftfix;
	
	private float tvPreFixSize;
	private int tvPreFixColor;
	private float tvAftFixSize;
	private int tvAftFixColor;
	private float tvTitleSize;
	private int tvTitleColor;
	private float tvPrePagerSize;
	private int tvPrePagerColor;
	private float tvCurPagerSize;
	private int tvCurPagerColor;
	private float tvNextPagerSize;
	private int tvNextPagerColor;
	private int llTitleViewPaddingLeft;
	private int llTitleViewPaddingRight;
	private int llTitleViewPaddingTop;
	private int llTitleViewPaddingBottom;
	private int tvTitlePaddingLeft;
	private int tvTitlePaddingRight;
	private int tvTitleMarginRight;
	private int tvTitleMarginLeft;
	private int tvPrePagerMarginRight;
	private int tvNextPagerMarginLeft;
	protected int llTitleHeight;
	
	private static ListTitleView instance = new ListTitleView();
	
	public static ListTitleView getInstance() {
		return instance;
	}
	
	private ListTitleView() {
		init();
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		if (data instanceof ListViewData) {
			initTitleData(((ListViewData)data).mTitleInfo);
		}
		ViewAdapter adapter = new ViewAdapter();
		adapter.view = createTitleView();
		adapter.type = ViewData.TYPE_LIST_TITLE_VIEW;
		return adapter;
	}

	@Override
	public void init() {
		tvPreFixSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_INTRO_SIZE1);
		tvPreFixColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_INTRO_COLOR1);
		tvAftFixSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_INTRO_SIZE1);
		tvAftFixColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_INTRO_COLOR1);
		tvTitleSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_INTRO_SIZE2);
		tvTitleColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_INTRO_CLOR2);
		tvPrePagerSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_PAGE_SIZE1);
		tvPrePagerColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_PAGE_COLOR1);
		tvCurPagerSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_PAGE_SIZE1);
		tvCurPagerColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_PAGE_COLOR2);
		tvNextPagerSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_PAGE_SIZE1);
		tvNextPagerColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_PAGE_COLOR2);
		
		llTitleHeight = (int) LayouUtil.getDimen("y64");
		llTitleViewPaddingLeft = (int) LayouUtil.getDimen("x10");
		llTitleViewPaddingTop = (int) LayouUtil.getDimen("x10"); 
		llTitleViewPaddingRight = (int) LayouUtil.getDimen("x10"); 
		llTitleViewPaddingBottom = (int) LayouUtil.getDimen("x10");
		tvTitlePaddingLeft = (int) LayouUtil.getDimen("x14");
		tvTitlePaddingRight = (int) LayouUtil.getDimen("x14");
		tvTitleMarginLeft = (int) LayouUtil.getDimen("x5");
		tvTitleMarginRight = (int) LayouUtil.getDimen("x5");
		tvPrePagerMarginRight = (int) LayouUtil.getDimen("x10");
		tvNextPagerMarginLeft = (int) LayouUtil.getDimen("x10");
	}
	
	protected void initTitleData(TitleInfo titleInfo) {
		mCurPage = titleInfo.curPage;
		mMaxPage = titleInfo.maxPage;

		mPrefix = titleInfo.prefix;
		mTitlefix = titleInfo.titlefix;
		mCityfix = titleInfo.cityfix;
		mMidfix = titleInfo.midfix;
		mAftfix = titleInfo.aftfix;
	}
	private LinearLayout  llPager;
	private TextView tvPrePager;
	private TextView tvNextPager;
	private TextView tvCurPager;
	private TextView tvPreFix;
	private TextView tvCity;
	private TextView tvMidFix;
	private TextView tvTitle;
	private TextView tvAftFix;
	public void dismiss(){
		llPager = null;
		tvPrePager = null; 
		tvNextPager = null;
		tvCurPager = null;
		tvPreFix =null;
		tvCity = null ;
		tvMidFix = null;
		tvTitle = null;
		tvAftFix =null;
	}
	
	@SuppressLint("NewApi")
	protected View createTitleView(){
		LinearLayout llTitleView = new LinearLayout(GlobalContext.get());
		llTitleView.setOrientation(LinearLayout.HORIZONTAL);
		llTitleView.setGravity(Gravity.CENTER_VERTICAL);
		llTitleView.setPadding(llTitleViewPaddingLeft, 0, llTitleViewPaddingRight, 0);
		
		LinearLayout llTips = new LinearLayout(GlobalContext.get());
		llTips.setOrientation(LinearLayout.HORIZONTAL);
		llTips.setGravity(Gravity.CENTER_VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
		llTitleView.addView(llTips,layoutParams);
		
		tvPreFix = new TextView(GlobalContext.get());
		tvPreFix.setSingleLine(true);
		tvPreFix.setEllipsize(TruncateAt.END);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llTips.addView(tvPreFix,layoutParams);
		
		tvCity = new TextView(GlobalContext.get());
		tvCity.setClickable(true);
		tvCity.setVisibility(View.GONE);
		tvCity.setBackground(LayouUtil.getDrawable("white_range_layout"));
		tvCity.setEllipsize(TruncateAt.END);
		tvCity.setGravity(Gravity.CENTER);
		int maxEms =7;
//		InputFilter[] fArray = new InputFilter[1];
//		fArray[0] = new InputFilter.LengthFilter(maxLength);
//		tvCity.setFilters(fArray);
		tvCity.setMaxEms(maxEms);
		tvCity.setPadding(tvTitlePaddingLeft,0, tvTitlePaddingRight, 0);
		tvCity.setSingleLine(true);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = tvTitleMarginLeft;
		layoutParams.rightMargin = tvTitleMarginRight;
		llTips.addView(tvCity,layoutParams);
		tvCity.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK, RecordWinController.VIEW_CITY, 0, 0);		
			}
		});
		
		tvMidFix = new TextView(GlobalContext.get());
		tvMidFix.setSingleLine(true);
		tvMidFix.setEllipsize(TruncateAt.END);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llTips.addView(tvMidFix,layoutParams);		
		
		tvTitle = new TextView(GlobalContext.get());
		tvTitle.setClickable(true);
		tvTitle.setVisibility(View.GONE);
		tvTitle.setBackground(LayouUtil.getDrawable("white_range_layout"));
		tvTitle.setEllipsize(TruncateAt.END);
		tvTitle.setGravity(Gravity.CENTER);
		maxEms = 8;
//		fArray = new InputFilter[1];
//		fArray[0] = new InputFilter.LengthFilter(maxLength);
//		tvTitle.setFilters(fArray);
		tvTitle.setMaxEms(maxEms);
		tvTitle.setPadding(tvTitlePaddingLeft,0, tvTitlePaddingRight, 0);
		tvTitle.setSingleLine(true);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = tvTitleMarginLeft;
		layoutParams.rightMargin = tvTitleMarginRight;
		llTips.addView(tvTitle,layoutParams);
		tvTitle.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK, RecordWinController.VIEW_TIPS, 0, 0);		
			}
		});
		
		tvAftFix = new TextView(GlobalContext.get());
		tvAftFix.setSingleLine(true);		
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llTips.addView(tvAftFix,layoutParams);
		
		llPager = new LinearLayout(GlobalContext.get());
		llPager.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
		llPager.setOrientation(LinearLayout.HORIZONTAL);
		llPager.setVisibility(View.GONE);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.END;
		int y20 = (int) LayouUtil.getDimen("y20");
		layoutParams.leftMargin = y20;
		llTitleView.addView(llPager,layoutParams);
		
		tvPrePager = new TextView(GlobalContext.get());
		tvPrePager.setSingleLine();
		tvPrePager.setText(LanguageConvertor.toLocale("上一页"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.rightMargin = tvPrePagerMarginRight;
		llPager.addView(tvPrePager,layoutParams);

		tvCurPager = new TextView(GlobalContext.get());
		tvCurPager.setSingleLine();
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llPager.addView(tvCurPager,layoutParams);
		
		tvNextPager = new TextView(GlobalContext.get());
		tvNextPager.setSingleLine();
		tvNextPager.setText(LanguageConvertor.toLocale("下一页"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = tvNextPagerMarginLeft;
		llPager.addView(tvNextPager,layoutParams);
		
		TextViewUtil.setTextSize(tvPreFix,tvPreFixSize);
		TextViewUtil.setTextColor(tvPreFix,tvPreFixColor);
		TextViewUtil.setTextSize(tvMidFix,tvPreFixSize);
		TextViewUtil.setTextColor(tvMidFix,tvPreFixColor);
		TextViewUtil.setTextSize(tvAftFix,tvAftFixSize);
		TextViewUtil.setTextColor(tvAftFix,tvAftFixColor);
		TextViewUtil.setTextSize(tvCity,tvTitleSize);
		TextViewUtil.setTextColor(tvCity,tvTitleColor);
		TextViewUtil.setTextSize(tvTitle,tvTitleSize);
		TextViewUtil.setTextColor(tvTitle,tvTitleColor);
		TextViewUtil.setTextSize(tvPrePager,tvPrePagerSize);
		TextViewUtil.setTextColor(tvPrePager,tvPrePagerColor);
		TextViewUtil.setTextSize(tvCurPager,tvCurPagerSize);
		TextViewUtil.setTextColor(tvCurPager,tvCurPagerColor);
		TextViewUtil.setTextSize(tvNextPager,tvNextPagerSize);
		TextViewUtil.setTextColor(tvNextPager,tvNextPagerColor);
		
		tvPrePager.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK, RecordWinController.VIEW_LIST_PREPAGE, 0, 0);
			}
		});
		tvNextPager.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK, RecordWinController.VIEW_LIST_NEXTPAGE, 0, 0);
			}
		});

		if (mMaxPage != 0 && mMaxPage != -1 /*&& mMaxPage > 1*/) {
			llPager.setVisibility(View.VISIBLE);
			if (mCurPage == 0) {
				TextViewUtil.setTextColor(tvPrePager,tvPrePagerColor);
			} else {
				TextViewUtil.setTextColor(tvPrePager,tvNextPagerColor);
			}
			if (mCurPage == mMaxPage - 1) {
				TextViewUtil.setTextColor(tvNextPager,tvPrePagerColor);
			} else {
				TextViewUtil.setTextColor(tvNextPager,tvNextPagerColor);
			}
			if(mMaxPage == 1){
				TextViewUtil.setTextColor(tvPrePager,ViewConfiger.COLOR_POI_PAGE_COLOR1);
				TextViewUtil.setTextColor(tvNextPager,ViewConfiger.COLOR_POI_PAGE_COLOR1);
			}
			tvCurPager.setText((mCurPage + 1) + "/" + mMaxPage);
		} else {
			llPager.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(mPrefix)) {
			tvPreFix.setText(LanguageConvertor.toLocale(mPrefix));
			tvPreFix.setVisibility(View.VISIBLE);
		}
		if(!TextUtils.isEmpty(mCityfix)){
			tvCity.setText(LanguageConvertor.toLocale(mCityfix));
			tvCity.setVisibility(View.VISIBLE);
			Drawable tvCityDrawableLeft = LayouUtil.getDrawable("icon_arrow");
			if (tvCityDrawableLeft != null) {
				int height = tvCityDrawableLeft.getIntrinsicHeight();
				float scale = (float)tvCity.getLineHeight()/(float)height;
				tvCityDrawableLeft.setBounds(0, 0, (int)(tvCityDrawableLeft.getIntrinsicWidth()*scale), (int)(height*scale));
				tvCity.setCompoundDrawables(null, null, tvCityDrawableLeft, null);
				int pad = (int) LayouUtil.getDimen("x8");
				tvCity.setCompoundDrawablePadding(pad);
			}			
		}
		if (!TextUtils.isEmpty(mMidfix)) {
			tvMidFix.setText(LanguageConvertor.toLocale(mMidfix));
			tvMidFix.setVisibility(View.VISIBLE);
		}
		
		if (!TextUtils.isEmpty(mTitlefix)) {
			tvTitle.setText(LanguageConvertor.toLocale(mTitlefix));
			tvTitle.setVisibility(View.VISIBLE);
			Drawable tvTitleDrawableLeft = LayouUtil.getDrawable("icon_edit_new");
			if (tvTitleDrawableLeft != null) {
				int height = tvTitleDrawableLeft.getIntrinsicHeight();
				float scale = (float)tvTitle.getLineHeight()/(float)height;
				tvTitleDrawableLeft.setBounds(0, 0, (int)(tvTitleDrawableLeft.getIntrinsicWidth()*scale), (int)(height*scale));
				tvTitle.setCompoundDrawables(null, null, tvTitleDrawableLeft, null);
				int pad = (int) LayouUtil.getDimen("x8");
				tvTitle.setCompoundDrawablePadding(pad);
			}
		}

		if (!TextUtils.isEmpty(mAftfix)) {
			tvAftFix.setText(LanguageConvertor.toLocale(mAftfix));
			tvAftFix.setVisibility(View.VISIBLE);
		}

//		if (ScreenUtil.getScreenWidth() <= 800) {
//			tvTitle.setMaxEms(8);
//		} else {
//			tvTitle.setMaxEms(10);
//		}
//		
		return llTitleView;
	}
	
	public View.OnClickListener getOnItemClickListener() {
		return onItemClickListener;
	}
	public OnTouchListener getOnTouchListener() {
		return onTouchListener;
	}
	
	public int getTitleHeight() {
		return llTitleHeight;
	}
	
	/**
	 * 必须设置tag为position
	 */
	protected View.OnClickListener onItemClickListener = new View.OnClickListener() {

		public void onClick(View v) {
			RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK, RecordWinController.VIEW_LIST_ITEM, 0, (Integer)v.getTag());
		}
	};

	protected OnTouchListener onTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_TOUCH, RecordWinController.VIEW_LIST_ITEM, 0, 0);
			return false;
		}
	};

	public void updata(ViewData data){
		if (data instanceof ListViewData) {
			initTitleData(((ListViewData)data).mTitleInfo);
		}
		if (mMaxPage != 0 && mMaxPage != -1 /*&& mMaxPage > 1*/) {
			llPager.setVisibility(View.VISIBLE);
			if (mCurPage == 0) {
				TextViewUtil.setTextColor(tvPrePager,tvPrePagerColor);
			} else {
				TextViewUtil.setTextColor(tvPrePager,tvNextPagerColor);
			}
			if (mCurPage == mMaxPage - 1) {
				TextViewUtil.setTextColor(tvNextPager,tvPrePagerColor);
			} else {
				TextViewUtil.setTextColor(tvNextPager,tvNextPagerColor);
			}
			if(mMaxPage == 1){
				TextViewUtil.setTextColor(tvPrePager,ViewConfiger.COLOR_POI_PAGE_COLOR1);
				TextViewUtil.setTextColor(tvNextPager,ViewConfiger.COLOR_POI_PAGE_COLOR1);
			}
			tvCurPager.setText((mCurPage + 1) + "/" + mMaxPage);
		} else {
			llPager.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(mPrefix)) {
			tvPreFix.setText(LanguageConvertor.toLocale(mPrefix));
			tvPreFix.setVisibility(View.VISIBLE);
		}
		if(!TextUtils.isEmpty(mCityfix)){
			tvCity.setText(LanguageConvertor.toLocale(mCityfix));
			tvCity.setVisibility(View.VISIBLE);
			Drawable tvCityDrawableLeft = LayouUtil.getDrawable("icon_arrow");
			if (tvCityDrawableLeft != null) {
				int height = tvCityDrawableLeft.getIntrinsicHeight();
				float scale = (float)tvCity.getLineHeight()/(float)height;
				tvCityDrawableLeft.setBounds(0, 0, (int)(tvCityDrawableLeft.getIntrinsicWidth()*scale), (int)(height*scale));
				tvCity.setCompoundDrawables(null, null, tvCityDrawableLeft, null);
			}			
		}
		if (!TextUtils.isEmpty(mMidfix)) {
			tvMidFix.setText(LanguageConvertor.toLocale(mMidfix));
			tvMidFix.setVisibility(View.VISIBLE);
		}
		
		if (!TextUtils.isEmpty(mTitlefix)) {
			tvTitle.setText(LanguageConvertor.toLocale(mTitlefix));
			tvTitle.setVisibility(View.VISIBLE);
			Drawable tvTitleDrawableLeft = LayouUtil.getDrawable("icon_edit_new");
			if (tvTitleDrawableLeft != null) {
				int height = tvTitleDrawableLeft.getIntrinsicHeight();
				float scale = (float)tvTitle.getLineHeight()/(float)height;
				tvTitleDrawableLeft.setBounds(0, 0, (int)(tvTitleDrawableLeft.getIntrinsicWidth()*scale), (int)(height*scale));
				tvTitle.setCompoundDrawables(null, null, tvTitleDrawableLeft, null);
			}
		}

		if (!TextUtils.isEmpty(mAftfix)) {
			tvAftFix.setText(LanguageConvertor.toLocale(mAftfix));
			tvAftFix.setVisibility(View.VISIBLE);
		}
	}
}
