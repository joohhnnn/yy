package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ListViewData;
import com.txznet.comm.ui.viewfactory.data.ListViewData.TitleInfo;
import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;
import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IListTitleView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.util.LanguageConvertor;

import java.security.Policy;
import java.util.List;

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
	private int tvCitySize;
	private int tvCityColor;
	private float tvPrePagerSize;
    private int tvPrePagerColor;
    private float tvCurPagerSize;
    private int tvCurPagerColor;
    private float tvNextPagerSize;
    private int tvNextPagerColor;
	private int llTitleViewPaddingLeft;
	private int llTitleViewPaddingRight;
	private int tvTitleMarginRight;
	private int tvTitleMarginLeft;

	//保证标题与内容对齐
	private int iconWidth;
	private int iconHeight;
	private int iconMarginLeft;
	private int iconMarginRight;
	private int trafficNameMargin;    //票务名称边距
	private int trafficLineMargin;    //票务连接线边距
	private int trafficLineWidth;    //票务连接线宽度
	private int trafficLineHeight;    //票务连接线高度

	//当前是否是无屏，无屏部分标题需要缩小
	private boolean isNone;

	private static ListTitleView instance = new ListTitleView();

    public static ListTitleView getInstance() {
        return instance;
    }
	
	private ListTitleView() {
		init();
	}

	public boolean isBusinessTitle;    //是否是美食信息标题


	@Override
	public ViewAdapter getView(ViewData data) {
		if (data instanceof ListViewData) {
			initTitleData(((ListViewData)data).mTitleInfo);
		}else {
            isBusinessTitle = false;
        }
        createPageView();
		ViewAdapter adapter = new ViewAdapter();
		adapter.view = createTitleView();
		adapter.type = ViewData.TYPE_LIST_TITLE_VIEW;
		return adapter;
	}

	public ViewAdapter getView(ViewData data,String iconName,String titleName) {

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                isNone = false;
				break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                isNone = true;
                break;
		}

		if (data instanceof ListViewData) {
			initTitleData(((ListViewData)data).mTitleInfo);
		}else {    //非列表标题不显示翻页
            mCurPage = 0;
            mMaxPage = 0;
            isBusinessTitle = false;
        }
        createPageView();

		ViewAdapter adapter = new ViewAdapter();
		if (iconName.equals("nav")){
			adapter.view = createTitleView();
		}else if (iconName.equals("flight")){
			adapter.view = createFlightTitleView(data);
		}else if (iconName.equals("train")){
			adapter.view = createTrainTitleView(data);
		}else if (iconName.equals("help")){
            adapter.view = createHelpTitleView();
        }
		else {
			adapter.view = createTitleView2(titleName);
		}
		adapter.type = ViewData.TYPE_LIST_TITLE_VIEW;
		setIvIcon(iconName);

		return adapter;
	}

	@Override
	public void init() {
        isBusinessTitle = false;

        tvTitleColor = Color.parseColor(LayouUtil.getString("color_vice_title"));

        tvPreFixColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        tvAftFixColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        tvCityColor = Color.parseColor(LayouUtil.getString("color_main_select"));
        tvPrePagerColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        tvCurPagerColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvNextPagerColor = Color.parseColor(LayouUtil.getString("color_main_title"));
	}

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex){
		int unit = ViewParamsUtil.unit;
		iconWidth = 6 * unit;
		iconHeight = 6 * unit;
		iconMarginLeft = unit;
		iconMarginRight = unit;
		trafficNameMargin = 2 * unit ;    //票务名称边距
		trafficLineMargin = unit;    //票务连接线边距
		trafficLineWidth = 6 * unit;
		trafficLineHeight = 3 * unit;    //票务连接线高度
		tvTitleSize = ViewParamsUtil.h4;
		tvPreFixSize = ViewParamsUtil.h5;
		tvAftFixSize = ViewParamsUtil.h3;
		tvCitySize = ViewParamsUtil.h3;
		tvPrePagerSize = ViewParamsUtil.h5;
		tvCurPagerSize = ViewParamsUtil.h5;
		tvNextPagerSize = ViewParamsUtil.h5;
		if (WinLayout.isVertScreen || styleIndex == StyleConfig.STYLE_ROBOT_NONE_SCREES){
			llTitleViewPaddingLeft = unit;
		}else {
			llTitleViewPaddingLeft = 0;
		}
		llTitleViewPaddingRight = unit;
    }

	protected void initTitleData(TitleInfo titleInfo) {
		mCurPage = titleInfo.curPage;
		mMaxPage = titleInfo.maxPage < 1 ? 1:titleInfo.maxPage;    //只有一页数据时仍然显示翻页

		mPrefix = titleInfo.prefix;
		mTitlefix = titleInfo.titlefix;
		mCityfix = titleInfo.cityfix;
		mMidfix = titleInfo.midfix;
		mAftfix = titleInfo.aftfix;

		LogUtil.logd(WinLayout.logTag+ "initTitleData: mPrefix:"+mPrefix+"--mTitlefix:"+mTitlefix+"--mCityfix:"+mCityfix+"--mMidfix:"+mMidfix+"--mAftfix:"+mAftfix
        +"--mCurPage:"+mCurPage+"--mMaxPage:"+mMaxPage);

	}

	public LinearLayout llPager;
	private TextView tvPrePager;
	private TextView tvNextPager;
	private TextView tvCurPager;
	private TextView tvPreFix;
	private TextView tvCity;
	private TextView tvMidFix;
	private TextView tvTitle;
	private TextView tvAftFix;
	private ImageView ivIcon;

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

	//翻页部分的内容
	private void createPageView(){
        llPager = new LinearLayout(GlobalContext.get());
        llPager.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        llPager.setOrientation(LinearLayout.HORIZONTAL);
        llPager.setVisibility(View.GONE);/*
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        layoutParams.gravity = Gravity.END;
        llTitleView.addView(llPager,layoutParams);*/

        tvPrePager = new TextView(GlobalContext.get());
        tvPrePager.setSingleLine();
        tvPrePager.setText(LanguageConvertor.toLocale("上一页"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.rightMargin = iconMarginRight;
        llPager.addView(tvPrePager,layoutParams);

        tvCurPager = new TextView(GlobalContext.get());
        tvCurPager.setSingleLine();
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llPager.addView(tvCurPager,layoutParams);

        tvNextPager = new TextView(GlobalContext.get());
        tvNextPager.setSingleLine();
        tvNextPager.setText(LanguageConvertor.toLocale("下一页"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = iconMarginLeft;
        llPager.addView(tvNextPager,layoutParams);
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

        //竖屏全半屏使用标题翻页
        //if (WinLayout.isVertScreen && mMaxPage != 0 && mMaxPage != -1 && mMaxPage > 1) {
        if (WinLayout.isVertScreen && StyleConfig.getInstance().getSelectStyleIndex() != StyleConfig.STYLE_ROBOT_NONE_SCREES && mMaxPage >= 1) {
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

            tvCurPager.setText((mCurPage + 1) + "/" + mMaxPage);
        } else {
            llPager.setVisibility(View.GONE);
        }

        ViewGroup pageParent =  (ViewGroup) llPager.getParent();
        if (pageParent != null){
            pageParent.removeAllViews();
        }
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

		ivIcon = new ImageView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(iconWidth,iconHeight);
		//layoutParams.leftMargin = iconMarginLeft;
		layoutParams.rightMargin = iconMarginRight;
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTips.addView(ivIcon,layoutParams);

		tvPreFix = new TextView(GlobalContext.get());
		tvPreFix.setSingleLine(true);
		tvPreFix.setEllipsize(TruncateAt.END);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llTips.addView(tvPreFix,layoutParams);
		
		tvCity = new TextView(GlobalContext.get());
		tvCity.setClickable(true);
		tvCity.setVisibility(View.GONE);
		tvCity.setEllipsize(TruncateAt.END);
		tvCity.setGravity(Gravity.CENTER);
		int maxEms =7;
		tvCity.setMaxEms(maxEms);
		tvCity.setSingleLine(true);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
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
		tvTitle.setEllipsize(TruncateAt.END);
		tvTitle.setGravity(Gravity.CENTER);
		maxEms = 8;
		tvTitle.setMaxEms(maxEms);
		tvTitle.setSingleLine(true);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		//layoutParams.leftMargin = tvTitleMarginLeft;
		//layoutParams.rightMargin = tvTitleMarginRight;
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

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = iconMarginRight;
        llTitleView.addView(llPager,layoutParams);

		TextViewUtil.setTextSize(tvPreFix,tvPreFixSize);
		TextViewUtil.setTextColor(tvPreFix,tvPreFixColor);
		TextViewUtil.setTextSize(tvMidFix,tvPreFixSize);
		TextViewUtil.setTextColor(tvMidFix,tvPreFixColor);
		TextViewUtil.setTextSize(tvAftFix,tvAftFixSize);
		TextViewUtil.setTextColor(tvAftFix,tvAftFixColor);
		TextViewUtil.setTextSize(tvCity,tvCitySize);
		TextViewUtil.setTextColor(tvCity,tvCityColor);
		TextViewUtil.setTextSize(tvTitle,tvCitySize);
		TextViewUtil.setTextColor(tvTitle,tvCityColor);

		if (!TextUtils.isEmpty(mPrefix)) {
			tvPreFix.setText(LanguageConvertor.toLocale(mPrefix));
			tvPreFix.setVisibility(View.VISIBLE);
		}
		if(!TextUtils.isEmpty(mCityfix)){
			tvCity.setText(LanguageConvertor.toLocale(mCityfix));
			tvCity.setVisibility(View.VISIBLE);
			Drawable tvCityDrawableLeft = LayouUtil.getDrawable("title_icon_nav_selection_city");
			if (tvCityDrawableLeft != null) {
				int height = tvCityDrawableLeft.getIntrinsicHeight();
				float scale = (float)tvCity.getLineHeight()/(float)height;
				//tvCityDrawableLeft.setBounds(0, 0, (int)(tvCityDrawableLeft.getIntrinsicWidth()*scale), (int)(height*scale));
				tvCityDrawableLeft.setBounds(0, 0, tvCitySize, tvCitySize);
				tvCity.setCompoundDrawables(null, null, tvCityDrawableLeft, null);
				int pad = (int) LayouUtil.getDimen("x3");
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
			Drawable tvTitleDrawableLeft = LayouUtil.getDrawable("title_icon_nav_edit");
			if (tvTitleDrawableLeft != null) {
				int height = tvTitleDrawableLeft.getIntrinsicHeight();
				float scale = (float)tvTitle.getLineHeight()/(float)height;
				//tvTitleDrawableLeft.setBounds(0, 0, (int)(tvTitleDrawableLeft.getIntrinsicWidth()*scale), (int)(height*scale));
				tvTitleDrawableLeft.setBounds(0, 0, tvCitySize, tvCitySize);
				tvTitle.setCompoundDrawables(null, null, tvTitleDrawableLeft, null);
				int pad = (int) LayouUtil.getDimen("x8");
				tvTitle.setCompoundDrawablePadding(pad);
			}
		}

		if (!TextUtils.isEmpty(mAftfix)) {
			tvAftFix.setText(LanguageConvertor.toLocale(mAftfix));
			tvAftFix.setVisibility(View.VISIBLE);
		}

		return llTitleView;
	}

	private View createTitleView2(String titleName){
		LinearLayout llTitleView = new LinearLayout(GlobalContext.get());
		llTitleView.setOrientation(LinearLayout.HORIZONTAL);
		llTitleView.setGravity(Gravity.CENTER_VERTICAL);
        llTitleView.setPadding(llTitleViewPaddingLeft, 0, llTitleViewPaddingRight, 0);

		ivIcon = new ImageView(GlobalContext.get());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconWidth,iconHeight);
		//layoutParams.leftMargin = iconMarginLeft;
		layoutParams.rightMargin = iconMarginRight;
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTitleView.addView(ivIcon,layoutParams);

		TextView tvTittle = new TextView(GlobalContext.get());
		tvTittle.setGravity(Gravity.CENTER_VERTICAL);
		tvTittle.setText(LanguageConvertor.toLocale(titleName));
		tvTittle.setSingleLine(true);
		tvTittle.setEllipsize(TruncateAt.END);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llTitleView.addView(tvTittle,layoutParams);

		layoutParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = iconMarginRight;
		llTitleView.addView(llPager,layoutParams);

		TextViewUtil.setTextSize(tvTittle,tvTitleSize);
		TextViewUtil.setTextColor(tvTittle,tvTitleColor);

		return llTitleView;
	}

    private View createHelpTitleView(){
		LinearLayout llTitleView = new LinearLayout(GlobalContext.get());
		llTitleView.setOrientation(LinearLayout.HORIZONTAL);
		llTitleView.setGravity(Gravity.CENTER_VERTICAL);
		llTitleView.setPadding(WinLayout.isVertScreen?0:llTitleViewPaddingLeft, 0, 0, 0);

        TextView tvPreFix0 = new TextView(GlobalContext.get());
        tvPreFix0.setSingleLine(true);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llTitleView.addView(tvPreFix0,layoutParams);

		tvPreFix = new TextView(GlobalContext.get());
		tvPreFix.setSingleLine(true);
		tvPreFix.setEllipsize(TruncateAt.END);
        tvPreFix.setMaxEms(5);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llTitleView.addView(tvPreFix,layoutParams);

        TextView tvPreFix2 = new TextView(GlobalContext.get());
        tvPreFix2.setSingleLine(true);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llTitleView.addView(tvPreFix2,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		layoutParams.gravity = Gravity.END;
		llTitleView.addView(llPager,layoutParams);

		TextViewUtil.setTextSize(tvPreFix,tvPreFixSize);
		TextViewUtil.setTextColor(tvPreFix,Color.parseColor("#16CAF9"));
        TextViewUtil.setTextSize(tvPreFix0,tvPreFixSize);
        TextViewUtil.setTextColor(tvPreFix0,tvPreFixColor);
        TextViewUtil.setTextSize(tvPreFix2,tvPreFixSize);
        TextViewUtil.setTextColor(tvPreFix2,tvPreFixColor);

		if (!TextUtils.isEmpty(mPrefix)) {
            tvPreFix.setVisibility(View.VISIBLE);

            int startIdx = mPrefix.indexOf("“")+1;
            int endIdx = mPrefix.indexOf("”");
            if (endIdx != -1){
                /*SpannableString textString = new SpannableString(mPrefix);
                textString.setSpan(new ForegroundColorSpan(Color.parseColor("#16CAF9")),startIdx,endIdx,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                tvPreFix.setText(textString);*/
                tvPreFix0.setText(LanguageConvertor.toLocale(mPrefix.substring(0,startIdx)));
                tvPreFix.setText(LanguageConvertor.toLocale(mPrefix.substring(startIdx,endIdx)));
                tvPreFix2.setText(LanguageConvertor.toLocale(mPrefix.substring(endIdx,mPrefix.length())));
            }else {
                tvPreFix.setMaxEms(20);
                tvPreFix.setText(LanguageConvertor.toLocale(mPrefix));
            }
		}

		return llTitleView;
	}

	private View createFlightTitleView(ViewData data){
		LinearLayout llTitleView = new LinearLayout(GlobalContext.get());
		llTitleView.setOrientation(LinearLayout.HORIZONTAL);
		llTitleView.setGravity(Gravity.CENTER_VERTICAL);
		llTitleView.setPadding(llTitleViewPaddingLeft, 0, llTitleViewPaddingRight, 0);

		ivIcon = new ImageView(GlobalContext.get());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconWidth,iconHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
        //layoutParams.leftMargin = iconMarginLeft;
        layoutParams.rightMargin = iconMarginRight;
		llTitleView.addView(ivIcon,layoutParams);

		TextView tvDepartName = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTitleView.addView(tvDepartName,layoutParams);

		ImageView ivLine = new ImageView(GlobalContext.get());
		ivLine.setBackground(LayouUtil.getDrawable("airlines_to1"));
        //layoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen(isNone?"x40":"x75"),(int) LayouUtil.getDimen(isNone?"x15":"x28"));
        layoutParams = new LinearLayout.LayoutParams(trafficLineWidth,trafficLineHeight);
        layoutParams.leftMargin = trafficLineMargin;
        layoutParams.rightMargin = trafficLineMargin;
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTitleView.addView(ivLine,layoutParams);

		TextView tvArrivalNmae = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		layoutParams.rightMargin = trafficNameMargin;
		llTitleView.addView(tvArrivalNmae,layoutParams);

		TextView tvTime = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTitleView.addView(tvTime,layoutParams);

        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        layoutParams.gravity = Gravity.END;
        llTitleView.addView(llPager,layoutParams);

		String title ;
		if (data instanceof QiWuFlightTicketData) {
			title = ((ListViewData) data).mTitleInfo.prefix;
		} else {
			title = ((ListViewData) data).mTitleInfo.titlefix;
		}
		if (title != null && title != ""){
			int len = title.length();
			int i = title.indexOf('-');
			int j = title.indexOf(' ');
			LogUtil.logd(WinLayout.logTag+ "createFlightTitleView: title："+title+"--"+len+"--"+i+"--"+j);

			if (WinLayout.isVertScreen){
                ivLine.setVisibility(View.GONE);
                tvArrivalNmae.setVisibility(View.GONE);
                tvDepartName.setText(LanguageConvertor.toLocale("机票  "));
                tvTime.setText(LanguageConvertor.toLocale(title.substring(j + 1,len)));
            }else {
                tvDepartName.setText(LanguageConvertor.toLocale(title.substring(0,i)));
                tvArrivalNmae.setText(LanguageConvertor.toLocale(title.substring(i+1,j)));
				String date;
				// 齐悟飞机票的展示方式和一般的飞机票不一样，特殊处理下。
				if (data instanceof QiWuFlightTicketData) {
					date = title.substring(j + 1, len);
				} else {
					date = title.substring(j + 1, len).replace("月", "-").
							replace("日", "");
				}
				tvTime.setText(LanguageConvertor.toLocale(date));
            }
		}

		TextViewUtil.setTextSize(tvDepartName,tvTitleSize);
		TextViewUtil.setTextColor(tvDepartName,Color.GRAY);
		TextViewUtil.setTextSize(tvArrivalNmae,tvTitleSize);
		TextViewUtil.setTextColor(tvArrivalNmae,Color.GRAY);
		TextViewUtil.setTextSize(tvTime,tvTitleSize);
		TextViewUtil.setTextColor(tvTime,Color.GRAY);
		return llTitleView;

	}

	private View createTrainTitleView(ViewData data){
		LinearLayout llTitleView = new LinearLayout(GlobalContext.get());
		llTitleView.setOrientation(LinearLayout.HORIZONTAL);
		llTitleView.setGravity(Gravity.CENTER_VERTICAL);
		llTitleView.setPadding(llTitleViewPaddingLeft, 0, llTitleViewPaddingRight, 0);

		ivIcon = new ImageView(GlobalContext.get());
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconWidth,iconHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
        //layoutParams.leftMargin = iconMarginLeft;
        layoutParams.rightMargin = iconMarginRight;
		llTitleView.addView(ivIcon,layoutParams);

		TextView tvDepartName = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTitleView.addView(tvDepartName,layoutParams);

		ImageView ivLine = new ImageView(GlobalContext.get());
		ivLine.setBackground(LayouUtil.getDrawable("trainlines_to0"));
		//layoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen(isNone?"x40":"x75"),(int) LayouUtil.getDimen(isNone?"x15":"x28"));
		layoutParams = new LinearLayout.LayoutParams(trafficLineWidth,trafficLineHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = trafficLineMargin;
        layoutParams.rightMargin = trafficLineMargin;
		llTitleView.addView(ivLine,layoutParams);

		TextView tvArrivalNmae = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		layoutParams.rightMargin = trafficNameMargin;
		llTitleView.addView(tvArrivalNmae,layoutParams);

		TextView tvTime = new TextView(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llTitleView.addView(tvTime,layoutParams);

        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        layoutParams.gravity = Gravity.END;
        llTitleView.addView(llPager,layoutParams);

		String title;
		if (data instanceof QiWuTrainTicketData) {
			title = ((ListViewData) data).mTitleInfo.prefix;
		} else {
			title = ((ListViewData) data).mTitleInfo.titlefix;
		}
        //String title = "武汉-背景 4月12日";
		if (title != null && title != ""){
			int len = title.length();
			int i = title.indexOf('-');
			int j = title.indexOf(' ');
			LogUtil.logd(WinLayout.logTag+ "createTrainTitleView: title："+title+"--"+len+"--"+i+"--"+j);

            if (WinLayout.isVertScreen){
                ivLine.setVisibility(View.GONE);
                tvArrivalNmae.setVisibility(View.GONE);
                tvDepartName.setText(LanguageConvertor.toLocale("火车票  "));
                tvTime.setText(LanguageConvertor.toLocale(title.substring(j + 1,len)));
            }else {
                tvDepartName.setText(LanguageConvertor.toLocale(title.substring(0,i)));
                tvArrivalNmae.setText(LanguageConvertor.toLocale(title.substring(i+1,j)));
				String date;
				// 齐悟火车票的展示方式和一般的火车票不一样，特殊处理下。
				if (data instanceof QiWuTrainTicketData) {
					date = title.substring(j + 1, len);
				} else {
					date = title.substring(j + 1, len).replace("月", "-").
							replace("日", "");
					switch (date.length()) {
						case 3:
							date = "0" + date.replace("-", "-0");
							break;
						case 4:
							if (date.indexOf("-") == 1) {
								date = "0" + date;
							} else {
								date = date.replace("-", "-0");
							}
							break;
					}
				}
                tvTime.setText(LanguageConvertor.toLocale(date));
            }
		}

		TextViewUtil.setTextSize(tvDepartName,tvTitleSize);
		TextViewUtil.setTextColor(tvDepartName,Color.GRAY);
		TextViewUtil.setTextSize(tvArrivalNmae,tvTitleSize);
		TextViewUtil.setTextColor(tvArrivalNmae,Color.GRAY);
		TextViewUtil.setTextSize(tvTime,tvTitleSize);
		TextViewUtil.setTextColor(tvTime,Color.GRAY);
		return llTitleView;

	}

	public View.OnClickListener getOnItemClickListener() {
		return onItemClickListener;
	}
	public OnTouchListener getOnTouchListener() {
		return onTouchListener;
	}
	
	/*public int getTitleHeight() {
		return llTitleHeight;
	}*/

	public List<View> mItemViews;
    private void showSelectItem(int index){
        if (mItemViews != null){
            for (int i = 0;i< mItemViews.size();i++){
                if (i == index){
                    mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
                }else {
                    mItemViews.get(i).setBackground(null);
                }
            }
        }
    }
	/**
	 * 必须设置tag为position
	 */
	protected View.OnClickListener onItemClickListener = new View.OnClickListener() {

		public void onClick(View v) {
            showSelectItem((int)v.getTag());
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

	public void updata(ViewData data,boolean isHistory){
		if (data instanceof ListViewData) {
			initTitleData(((ListViewData)data).mTitleInfo);
		}else {
		    isBusinessTitle = false;
        }
		//竖屏全半屏使用标题翻页
		//if (WinLayout.isVertScreen && mMaxPage != 0 && mMaxPage != -1 && mMaxPage > 1) {
		if (WinLayout.isVertScreen && StyleConfig.getInstance().getSelectStyleIndex() != StyleConfig.STYLE_ROBOT_NONE_SCREES && mMaxPage >= 1) {
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

		if (!isHistory){//导航历史记录仅更新翻页
			if (!TextUtils.isEmpty(mPrefix) && tvPreFix != null) {
				tvPreFix.setText(LanguageConvertor.toLocale(mPrefix));
				tvPreFix.setVisibility(View.VISIBLE);
			}
			if(!TextUtils.isEmpty(mCityfix) && tvCity != null){
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
			if (!TextUtils.isEmpty(mMidfix) && tvMidFix != null) {
				tvMidFix.setText(LanguageConvertor.toLocale(mMidfix));
				tvMidFix.setVisibility(View.VISIBLE);
			}

			if (!TextUtils.isEmpty(mTitlefix) && tvTitle != null) {
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

			if (!TextUtils.isEmpty(mAftfix) && mAftfix != null) {
				tvAftFix.setText(LanguageConvertor.toLocale(mAftfix));
				tvAftFix.setVisibility(View.VISIBLE);
			}

			//只有地图模式会走update
			if (isBusinessTitle){
				ivIcon.setImageDrawable(LayouUtil.getDrawable("title_icon_nearby"));
				isBusinessTitle = false;
			}else {
				ivIcon.setImageDrawable(LayouUtil.getDrawable("title_icon_nav"));
			}
		}
	}

	public void setIvIcon(String icon){
		LogUtil.logd(WinLayout.logTag+ "setIvIcon:mTitlefix: "+mTitlefix);
		if (icon.equals("help")){
		    return;
        }
		//if (mTitlefix != null && mTitlefix.equals("美食")){
		//非导航历史记录需要更新icon
        if (isBusinessTitle){
            ivIcon.setImageDrawable(LayouUtil.getDrawable("title_icon_nearby"));
            isBusinessTitle = false;
        }else {
            ivIcon.setImageDrawable(LayouUtil.getDrawable("title_icon_"+icon));
        }

	}
}
