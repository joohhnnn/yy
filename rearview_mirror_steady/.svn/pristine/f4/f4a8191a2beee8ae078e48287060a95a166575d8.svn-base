package com.txznet.comm.ui.theme.test.view;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CallListViewData;
import com.txznet.comm.ui.viewfactory.data.CallListViewData.CallBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICallListView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class CallListView extends ICallListView {

	private static CallListView sInstance = new CallListView();
	
	
	private List<View> mItemViews; 
	
	//字体等参数配置
	
	private int dividerHeight;

	private int tvNumSide;    //序号宽高
	private int tvNumHorMargin;    //序号左右边距
	private int tvNumSize;    //序号字体大小
	private int tvNumColor;    //序号字体颜色
    private int tvNameBgSide;    //名字背景大小
    private int tvNameRightMargin;    //名字右边距
    private int tvNameSize;    //名字字体大小
    private int tvNameColor;    //名字字体颜色
    private int tvContentHeight;    //全名名字行高
    private int tvContentSize;    //全名名字字体大小
    private int tvContentColor;    //全名名字字体颜色
    private int tvDeatilHeight;    //号码、归属地信息行高
    private int tvDeatilSize;    //号码、归属地信息字体大小
    private int tvDetailColor;    //号码、归属地信息字体颜色

	private CallListView() {
	}

	public static CallListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
		/*if (progressBars.size() > selection) {
			GradientProgressBar progressBar = progressBars.get(selection);
			if (progress < 0) {
				if (progressBar.getVisibility() == View.VISIBLE) {
					progressBar.setVisibility(View.GONE);
				}
			}else {
				if (progressBar.getVisibility() == View.GONE) {
					progressBar.setVisibility(View.VISIBLE);
				}
				progressBar.setProgress(progress);
			}
		}*/
	}

	
	@Override
	public void release() {
		super.release();
		if (mItemViews != null) {
			mItemViews.clear();
		}
		/*if (progressBars != null) {
			progressBars.clear();
		}*/
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		CallListViewData callListViewData = (CallListViewData) data;
		WinLayout.getInstance().vTips = callListViewData.vTips;

		View view = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = createViewFull(callListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = createViewHalf(callListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(callListViewData);
				break;
		}

		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.isListView = true;
		viewAdapter.object = CallListView.getInstance();
		return viewAdapter;
	}

	private View createViewFull(CallListViewData callListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(callListViewData,"phone","电话");
		//ListTitleView.getInstance().setIvIcon("phone");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setOrientation(LinearLayout.VERTICAL);
		llLayout.setGravity(Gravity.CENTER_VERTICAL);

		LinearLayout.LayoutParams   layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),callListViewData.mTitleInfo.curPage,callListViewData.mTitleInfo.maxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

		llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
		llContent.setLayoutAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_START);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_REPEAT);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, 3);
				}
			}
		});
		//progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < callListViewData.count; i++) {
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i, callListViewData.isMultName, callListViewData.getData().get(i),
					i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (callListViewData.count < 4){
			LinearLayout lFill = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,4 - callListViewData.count);
			llContent.addView(lFill, layoutParams);
		}*/

		return llLayout;
	}

	private View createViewHalf(CallListViewData callListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(callListViewData,"phone","电话");
		//ListTitleView.getInstance().setIvIcon("phone");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setOrientation(LinearLayout.VERTICAL);
		llLayout.setGravity(Gravity.CENTER_VERTICAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),callListViewData.mTitleInfo.curPage,callListViewData.mTitleInfo.maxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

		llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
		llContent.setLayoutAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_START);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_REPEAT);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, 3);
				}
			}
		});
		//progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < callListViewData.count; i++) {
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i, callListViewData.isMultName, callListViewData.getData().get(i),
					i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (callListViewData.count < 4){
			LinearLayout lFill = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,4 - callListViewData.count);
			llContent.addView(lFill, layoutParams);
		}*/

		return llLayout;
	}

	private View createViewNone(CallListViewData callListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(callListViewData,"phone","电话");
		//ListTitleView.getInstance().setIvIcon("phone");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(llContents,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),callListViewData.mTitleInfo.curPage,callListViewData.mTitleInfo.maxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llPager,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llContents.addView(titleViewAdapter.view,layoutParams);

		View divider = new View(GlobalContext.get());
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		llContents.addView(divider, layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llContents.addView(llContent,layoutParams);

		llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
		llContent.setLayoutAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_START);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_REPEAT);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, 3);
				}
			}
		});
		//progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < callListViewData.count; i++) {
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i, callListViewData.isMultName, callListViewData.getData().get(i),
					i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (callListViewData.count < 3){
			LinearLayout lFill = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,3 - callListViewData.count);
			llContent.addView(lFill, layoutParams);
		}*/

		return llLayout;
	}

	@Override
	public void init() {
		super.init();
		dividerHeight = 1;

        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvContentColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvDetailColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){
		int unit = ViewParamsUtil.unit;
		tvNumSide = 6 * unit;
		tvNumHorMargin = 2 * unit;
		tvNumSize = ViewParamsUtil.h0;
		tvNameBgSide = 7 * unit;
		tvNameRightMargin = unit;
		tvNameSize = 5 * unit;
		tvContentSize = ViewParamsUtil.h4;
		tvContentHeight = ViewParamsUtil.h4Height;
		tvDeatilSize = ViewParamsUtil.h7;
		tvDeatilHeight = ViewParamsUtil.h7Height;
		//竖屏全屏、半屏列表字体size加2
		if (styleIndex != StyleConfig.STYLE_ROBOT_NONE_SCREES && WinLayout.isVertScreen){
			tvContentSize = ViewParamsUtil.h3;
			tvContentHeight = ViewParamsUtil.h3Height;
			tvDeatilSize = ViewParamsUtil.h6;
			tvDeatilHeight = ViewParamsUtil.h6Height;
		}
	}

	@Override
	public void snapPage(boolean next) {
		LogUtil.logd("update snap "+next);
	}

	@Override
	public List<View> getFocusViews() {
		return mItemViews;
	}
	
	/**
	 * 是否含有动画
	 * @return
	 */
	@Override
	public boolean hasViewAnimation() {
		return true;
	}
	
	private View createItemView(int position, boolean isMultiName,CallBean callBean,boolean showDivider){
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		ListTitleView.getInstance().mItemViews = mItemViews;
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		/*itemView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()){
					case MotionEvent.ACTION_UP:
						showSelectItem((int)v.getTag());
						break;
				}
				return false;
			}
		});*/
		/*FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		*//*layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;*//*
		itemView.addView(flContent,layoutParams);*/
		
		/*GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(mProgressBar, mFLayoutParams);
		progressBars.add(mProgressBar);*/
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		//mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		itemView.addView(llContent, layoutParams);
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
		mLLayoutParams.leftMargin = tvNumHorMargin;
		mLLayoutParams.rightMargin = tvNumHorMargin;
		mLLayoutParams.gravity = Gravity.CENTER;
		llContent.addView(tvNum,mLLayoutParams);

		TextView tvName = new TextView(GlobalContext.get());
		tvName.setGravity(Gravity.CENTER);
		tvName.setIncludeFontPadding(false);
		mLLayoutParams = new LinearLayout.LayoutParams(tvNameBgSide,tvNameBgSide);
		mLLayoutParams.rightMargin = tvNameRightMargin;
		mLLayoutParams.gravity = Gravity.CENTER;
		llContent.addView(tvName,mLLayoutParams);
		
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContent.addView(llDetail,mLLayoutParams);
		
		FrameLayout tvLayout = new FrameLayout(GlobalContext.get());
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,0,1);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llDetail.addView(tvLayout,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.CENTER);
		FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,tvContentHeight);
        flLayoutParams.gravity = Gravity.BOTTOM;
		tvLayout.addView(tvContent, flLayoutParams);

		LinearLayout llDesc = new LinearLayout(GlobalContext.get());
		llDesc.setOrientation(LinearLayout.HORIZONTAL);
		llDesc.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
		llDetail.addView(llDesc,mLLayoutParams);

		TextView tvPhone = new TextView(GlobalContext.get());
		tvPhone.setEllipsize(TruncateAt.END);
		tvPhone.setSingleLine();
		tvPhone.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvContentHeight);
		mLLayoutParams.rightMargin = tvNameRightMargin;
		mLLayoutParams.gravity = Gravity.TOP;
		llDesc.addView(tvPhone, mLLayoutParams);

		TextView tvProvince = new TextView(GlobalContext.get());
		tvProvince.setSingleLine();
		tvProvince.setEllipsize(TruncateAt.END);
		tvProvince.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvContentHeight);
        mLLayoutParams.gravity = Gravity.TOP;
		llDesc.addView(tvProvince,mLLayoutParams);
		
		TextView tvCity = new TextView(GlobalContext.get());
		tvCity.setSingleLine();
		tvCity.setEllipsize(TruncateAt.END);
		tvCity.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvContentHeight);
		mLLayoutParams.rightMargin = tvNameRightMargin;
        mLLayoutParams.gravity = Gravity.TOP;
		llDesc.addView(tvCity,mLLayoutParams);
		
		TextView tvIsp = new TextView(GlobalContext.get());
		tvIsp.setSingleLine();
		tvIsp.setEllipsize(TruncateAt.END);
		tvIsp.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvContentHeight);
//		mLLayoutParams.rightMargin = tvNameRightMargin;
		mLLayoutParams.gravity = Gravity.TOP;
		llDesc.addView(tvIsp,mLLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

		tvNum.setText(String.valueOf(position + 1));
		String name = callBean.name;
		String number = callBean.number;
		tvName.setText("");
		tvName.setBackground(LayouUtil.getDrawable("phone_name"));
		if (TextUtils.isEmpty(name)){
			tvLayout.setVisibility(View.GONE);

			//没有姓名时号码居中显示
			mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
			mLLayoutParams.rightMargin = tvNameRightMargin;
			tvPhone.setLayoutParams(mLLayoutParams);
		}else {
			if(isChinese(name.substring(name.length()-1,name.length()))) {
				LogUtil.logd("-isChinese:" + name);
				tvName.setText(name.substring(name.length() - 1, name.length()));
				tvName.setBackground(LayouUtil.getDrawable("phone_name_bg"));
			}
			if (TextUtils.isEmpty(number)){
				flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
				flLayoutParams.gravity = Gravity.CENTER_VERTICAL;
				tvContent.setLayoutParams(flLayoutParams);
			}
			tvContent.setText(LanguageConvertor.toLocale(name));
		}
		if (TextUtils.isEmpty(number)){
			llDesc.setVisibility(View.GONE);
		}else {
			if (number.length() == 11){
				StringBuilder stringBuilder = new StringBuilder(number);
				stringBuilder.insert(3," ");
				stringBuilder.insert(8," ");
				number = stringBuilder.toString();
			}
			tvPhone.setText(number);

			tvProvince.setText(callBean.province == null ? "" : LanguageConvertor.toLocale(callBean.province));
			tvCity.setText(callBean.city == null ? "" : LanguageConvertor.toLocale(callBean.city));
			tvIsp.setText(callBean.isp == null ? "" : LanguageConvertor.toLocale(callBean.isp));

			if (callBean.province == null && callBean.city == null && callBean.isp == null) {
				tvProvince.setVisibility(View.GONE);
				tvCity.setVisibility(View.GONE);
				tvIsp.setVisibility(View.GONE);
			} else {
				tvProvince.setVisibility(View.VISIBLE);
				tvCity.setVisibility(View.VISIBLE);
				tvIsp.setVisibility(View.VISIBLE);
				//名称为空归属地不为空时，号码作为主标题，归属地作为副标题来显示
				if (TextUtils.isEmpty(name)){
					tvLayout.setVisibility(View.VISIBLE);
					tvContent.setText(number);
					tvPhone.setVisibility(View.GONE);
				}
			}
		}

//        tvIsp.setVisibility(callBean.isp == null ? View.GONE : View.VISIBLE);
        //mProgressBar.setVisibility(View.GONE);

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);
		
		/*itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				RippleView rippleView = (RippleView) v;
				if (hasFocus) {
					rippleView.setBackgroundColor(Color.parseColor("#4AA5FA"));
				} else {
					rippleView.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});*/

		TextViewUtil.setTextSize(tvNum, tvNumSize);
		TextViewUtil.setTextColor(tvNum, tvNumColor);
        TextViewUtil.setTextSize(tvName, tvNameSize);
        TextViewUtil.setTextColor(tvName, tvNameColor);
		TextViewUtil.setTextSize(tvContent, tvContentSize);
		TextViewUtil.setTextColor(tvContent, tvContentColor);
		TextViewUtil.setTextSize(tvPhone, tvDeatilSize);
		TextViewUtil.setTextColor(tvPhone, tvDetailColor);
		TextViewUtil.setTextSize(tvProvince, tvDeatilSize);
		TextViewUtil.setTextColor(tvProvince, tvDetailColor);
		TextViewUtil.setTextSize(tvCity, tvDeatilSize);
		TextViewUtil.setTextColor(tvCity, tvDetailColor);
		TextViewUtil.setTextSize(tvIsp, tvDeatilSize);
		TextViewUtil.setTextColor(tvIsp, tvDetailColor);
		
		return itemView;
	}

	@Override
	public void updateItemSelect(int index) {
		LogUtil.logd(WinLayout.logTag+ "train updateItemSelect " + index);
		showSelectItem(index);
	}


	private void showSelectItem(int index){
		for (int i = 0;i< mItemViews.size();i++){
			if (i == index){
				mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
			}else {
				mItemViews.get(i).setBackground(null);
			}
		}
	}

	//中文字符占2个字节，英文字符占1个字节，判断一个字符串是否包含汉字
	private boolean isChinese(String str){
        try {
            LogUtil.logd(WinLayout.logTag+str+ "-isChinese: "+str.getBytes("GBK").length+"--"+str.length());
            return str.getBytes("GBK").length != str.length();
        } catch (UnsupportedEncodingException e) {
            LogUtil.loge(WinLayout.logTag+ "isChinese: getGBK false" );
            e.printStackTrace();
        }
        return false;
    }

}
