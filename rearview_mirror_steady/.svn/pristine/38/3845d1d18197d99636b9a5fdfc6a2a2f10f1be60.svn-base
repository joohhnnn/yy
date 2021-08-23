package com.txznet.comm.ui.theme.test.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.SkillfulReminding;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.PoiListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IPoiListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.TXZFileConfigUtil;

@SuppressLint("NewApi")
public class PoiListView extends IPoiListView {

	private static PoiListView sInstance = new PoiListView();
	
	private List<View> mItemViews;

    private int dividerHeight;

    private int tvNumSide;    //序号宽高
    private int tvNumHorMargin;    //序号左右边距
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
    private int tvContentSize;    //内容字体大小
    private int tvContentHeight;    //内容行高
    private int tvContentColor;    //内容字体颜色
    private int centerInterval;    //内容到距离的间距
    private int tvDistanceSize;    //距离字体大小
    private int tvDistanceHeight;    //距离行高
    private int tvDistanceColor;    //距离字体颜色
    private int tvDescSize;    //地址字体大小
    private int tvDescHeight;    //地址行高
    private int tvDescColor;    //地址字体颜色
    private int tvDescLeftMargin;    //地址左边距
	private int ivBusSide;    //商圈信息图标大小
	private int ivBusRightMargin;    //商圈信息图标右边距
	private int ivStarWidth;    //商圈信息评分宽度
	private int ivStarHeight;    //商圈信息评分高度
    private int tvPriceSize;    //商圈信息价格字号
	private int historyLineHeight;    //导航历史竖线高度
    private int ivDeleteWidth;    //导航历史删除键宽度
    private int ivDeleteHeight;    //导航历史删除键高度
    private int flDeleteWidth;    //导航历史删除键可点击区域宽度

	private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	private boolean mIsHistory = false;
	public boolean mIsUseNewLayout = false;    //是否是美食信息
	private boolean isVertical = false;

	LinearLayout llLayoutLoading  = null;    //加载动画布局
	ImageView mLoadingImg = null;    //加载动画
    LinearLayout llLayout = null;    //view跟布局
	
	private PoiListView() {
        mFlags = Integer.valueOf(0);
        // 表明当前View支持更新
        mFlags = mFlags | UPDATEABLE;
	}

	public static PoiListView getInstance(){
		return sInstance;
	}
	
	@Override
	public Object updateView(ViewData data) {
        LogUtil.logd(WinLayout.logTag+ "updateView: ");

		return super.updateView(data);
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
	public Integer getFlags() {
		return super.getFlags();
	}

	@Override
	public boolean hasViewAnimation() {
		return true;
	}
	
	@Override
	public void release() {
		super.release();
		if (mItemViews != null) {
			mItemViews.clear();
		}
		if (progressBars != null) {
			progressBars.clear();
		}
		if (llLayout != null){
            llLayout.removeAllViews();
            llLayout = null;
        }
        if (llLayoutLoading != null){
            llLayoutLoading = null;
        }
        if (mLoadingImg != null){
            mLoadingImg = null;
        }
	}

	//展示加载中的动画
    public void showLoadingView(){

    }

	@Override
	public ViewAdapter getView(ViewData data) {
        PoiListViewData poiListViewData = (PoiListViewData) data;
        LogUtil.logd(WinLayout.logTag+ "getView: "+"poiListViewData.vTips:"+poiListViewData.vTips+"--"+poiListViewData);

        if (llLayoutLoading != null && mLoadingImg != null){
            llLayoutLoading.setVisibility(View.GONE);
            if (WinLayout.isSearch){
                LogUtil.logd(WinLayout.logTag+ "poilistView: mLoadingImg start");
                WinLayout.isSearch = false;
                llLayoutLoading.setVisibility(View.VISIBLE);
                ((AnimationDrawable) mLoadingImg.getDrawable()).start();
                WinLayout.getInstance().vTips = null;

                return null;
            }
        }

        WinLayout.getInstance().vTips = poiListViewData.vTips;
        mIsHistory = poiListViewData.action.equals(Poi.PoiAction.ACTION_NAV_HISTORY);
        mIsUseNewLayout = poiListViewData.isBus;
        ListTitleView.getInstance().isBusinessTitle = poiListViewData.isBus;
		
		if(mNumberView != null){
			mNumberView.clear();
		}

		View view;
		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = createViewFull(poiListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = createViewHalf(poiListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(poiListViewData);
				break;
		}

		ViewAdapter viewAdapter = new ViewAdapter();
        viewAdapter.flags = Integer.valueOf(0);
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.isListView = true;
		viewAdapter.object = PoiListView.getInstance();
		return viewAdapter;
	}

	private View createViewFull(PoiListViewData poiListViewData){
		ViewAdapter titleViewAdapter = null;
		if (PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
			titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData,"nav_history","历史目的地");
		}else {
			titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData);
			ListTitleView.getInstance().setIvIcon("nav");
		}
		mCurPage = poiListViewData.mTitleInfo.curPage;
		mMaxPage = poiListViewData.mTitleInfo.maxPage;
		llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));

        FrameLayout flRootLayout = new FrameLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        llContents.addView(flRootLayout,layoutParams);

        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        //llContents.addView(llContent,layoutParams);
        flRootLayout.addView(llContent,flLayoutParams);

		llLayoutLoading = new LinearLayout(GlobalContext.get());
		llLayoutLoading.setGravity(Gravity.CENTER);
		llLayoutLoading.setOrientation(LinearLayout.VERTICAL);
		llLayoutLoading.setVisibility(View.GONE);
		llLayoutLoading.setBackgroundColor(Color.argb(204, 0, 0, 0));
		flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
		flRootLayout.addView(llLayoutLoading,flLayoutParams);

		mLoadingImg = new ImageView(GlobalContext.get());
		mLoadingImg.setImageDrawable(LayouUtil.getDrawable("poimap_loading_anim"));
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		llLayoutLoading.addView(mLoadingImg,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents,layoutParams);
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
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
				}
			}
		});

		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < poiListViewData.count; i++) {
			RippleView itemView = new RippleView(GlobalContext.get());
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,mIsUseNewLayout?SizeConfig.itemHeightPro:SizeConfig.itemHeight);
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			llContent.addView(itemView, layoutParams);
			if(PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
				createHistoryItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.mShowCount - 1);
			}else {
				createItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.mShowCount - 1);
			}
			mItemViews.add(itemView);
		}/*
		if (poiListViewData.count < poiListViewData.mShowCount){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,poiListViewData.mShowCount - poiListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/

		return llLayout;
	}

	private View createViewHalf(PoiListViewData poiListViewData){
		ViewAdapter titleViewAdapter = null;
		if (PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
			titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData,"nav_history","历史目的地");
		}else {
			titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData);
			ListTitleView.getInstance().setIvIcon("nav");
		}
		mCurPage = poiListViewData.mTitleInfo.curPage;
		mMaxPage = poiListViewData.mTitleInfo.maxPage;

		llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));

        FrameLayout flRootLayout = new FrameLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        llContents.addView(flRootLayout,layoutParams);

        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        //llContents.addView(llContent,layoutParams);
        flRootLayout.addView(llContent,flLayoutParams);

        llLayoutLoading = new LinearLayout(GlobalContext.get());
        llLayoutLoading.setGravity(Gravity.CENTER);
        llLayoutLoading.setOrientation(LinearLayout.VERTICAL);
        llLayoutLoading.setVisibility(View.GONE);
        llLayoutLoading.setBackgroundColor(Color.argb(204, 0, 0, 0));
        flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        flRootLayout.addView(llLayoutLoading,flLayoutParams);

        mLoadingImg = new ImageView(GlobalContext.get());
        mLoadingImg.setImageDrawable(LayouUtil.getDrawable("poimap_loading_anim"));
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        llLayoutLoading.addView(mLoadingImg,layoutParams);
        LogUtil.logd(WinLayout.logTag+ "llLayoutLoading: "+llLayoutLoading.toString());

		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents,layoutParams);

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
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
				}
			}
		});
		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < poiListViewData.count; i++) {
			RippleView itemView = new RippleView(GlobalContext.get());
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,mIsUseNewLayout?SizeConfig.itemHeightPro:SizeConfig.itemHeight);
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			llContent.addView(itemView, layoutParams);
			if(PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
				createHistoryItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.mShowCount - 1);
			}else {
				createItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.mShowCount - 1);
			}
			mItemViews.add(itemView);
		}

		return llLayout;
	}

	public View createViewNone(PoiListViewData poiListViewData){
		Log.d("jack", "createViewNone: +"+poiListViewData.action);
		ViewAdapter titleViewAdapter = null;
		if (PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
			titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData,"nav_history","历史目的地");
		}else {
			titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData);
			ListTitleView.getInstance().setIvIcon("nav");
		}
		mCurPage = poiListViewData.mTitleInfo.curPage;
		mMaxPage = poiListViewData.mTitleInfo.maxPage;
		llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1);
		llLayout.addView(llContents,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContents.addView(titleViewAdapter.view,layoutParams);

		View divider = new View(GlobalContext.get());
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		llContents.addView(divider, layoutParams);

        FrameLayout flRootLayout = new FrameLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llContents.addView(flRootLayout,layoutParams);

        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        //llContents.addView(llContent,layoutParams);
        flRootLayout.addView(llContent,flLayoutParams);

        llLayoutLoading = new LinearLayout(GlobalContext.get());
        llLayoutLoading.setGravity(Gravity.CENTER);
        llLayoutLoading.setOrientation(LinearLayout.VERTICAL);
        llLayoutLoading.setVisibility(View.GONE);
        llLayoutLoading.setBackgroundColor(Color.argb(204, 0, 0, 0));
        flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
        flRootLayout.addView(llLayoutLoading,flLayoutParams);

        mLoadingImg = new ImageView(GlobalContext.get());
        mLoadingImg.setImageDrawable(LayouUtil.getDrawable("poimap_loading_anim"));
        layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        llLayoutLoading.addView(mLoadingImg,layoutParams);
        LogUtil.logd(WinLayout.logTag+ "llLayoutLoading: "+llLayoutLoading.toString());

		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LayoutParams.MATCH_PARENT);
		llLayout.addView(llPager,layoutParams);

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
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
				}
			}
		});
		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < poiListViewData.count; i++) {
			RippleView itemView = new RippleView(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,mIsUseNewLayout?SizeConfig.itemHeightPro:SizeConfig.itemHeight);
			llContent.addView(itemView, layoutParams);
			if(PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
				createHistoryItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.mShowCount - 1);
			}else {
				if (mIsUseNewLayout){
					createBusItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.mShowCount - 1);
				}else {
					createItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.mShowCount - 1);
				}
			}
			mItemViews.add(itemView);
		}

		return llLayout;
	}

	@Override
	public void init() {
		// 支持删除
		if (mFlags == null) {
			mFlags = SUPPORT_DELETE;
			mFlags |= SUPPORT_CITY_TITLE;
		} else {
			mFlags |= SUPPORT_DELETE;
			mFlags |= SUPPORT_CITY_TITLE;
		}

		/*mFlags = Integer.valueOf(0);
		// 表明当前View支持更新
		mFlags = mFlags | UPDATEABLE;*/
		LogUtil.logd(WinLayout.logTag+"init flags:" + mFlags);
		
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));

        //int unit = (int) LayouUtil.getDimen(WinLayout.isVertScreen?"vertical_unit":"unit");
        int unit = ViewParamsUtil.unit;
        tvNumSide = 6 * unit;
        tvNumHorMargin = WinLayout.isVertScreen?unit:2 * unit;
        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvContentColor =  Color.parseColor(LayouUtil.getString("color_main_title"));
        centerInterval = ViewParamsUtil.centerInterval;
        tvDistanceColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvDescColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        tvDescLeftMargin = unit;
        ivBusRightMargin = unit / 2;
        historyLineHeight = 6 * unit;
        ivDeleteWidth = 3 * unit;
        ivDeleteHeight = 3 * unit;
        flDeleteWidth = 7 * unit;
	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){
		tvNumSize = ViewParamsUtil.h0;
		tvContentSize = ViewParamsUtil.h4;
		tvContentHeight = ViewParamsUtil.h4Height;
		tvDistanceSize = ViewParamsUtil.h7;
		tvDistanceHeight = ViewParamsUtil.h7Height;
		tvDescSize = ViewParamsUtil.h6;
		tvDescHeight = ViewParamsUtil.h6Height;
		ivStarHeight = tvDescSize;
		ivStarWidth = (int)(5.6 * ivStarHeight);
		tvPriceSize = tvDescSize;
		ivBusSide = ViewParamsUtil.musicTagSide;
		if (styleIndex != StyleConfig.STYLE_ROBOT_NONE_SCREES && WinLayout.isVertScreen){
			tvContentSize = ViewParamsUtil.h3;
			tvContentHeight = ViewParamsUtil.h3Height;
			tvDistanceSize = ViewParamsUtil.h6;
			tvDistanceHeight = ViewParamsUtil.h6Height;
			tvDescSize = ViewParamsUtil.h5;
			tvDescHeight = ViewParamsUtil.h5Height;
			ivStarHeight = tvDescSize;
			ivStarWidth = (int)(5.6 * ivStarHeight);
			tvPriceSize = tvDescSize;
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
	
	
	private View createItemView(final RippleView itemView, final int position,Poi poi,boolean showDivider){
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
		itemView.setBackgroundColor(Color.TRANSPARENT);
		
		/*GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(mProgressBar, layoutParams);
		progressBars.add(mProgressBar);	*/

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llContents,layoutParams);

		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setId(ViewUtils.generateViewId());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
        llLayoutParams.gravity = Gravity.CENTER;
        llLayoutParams.leftMargin = tvNumHorMargin;
        llLayoutParams.rightMargin = tvNumHorMargin;
		mNumberView.add(tvNum);
        llContents.addView(tvNum,llLayoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        llContents.addView(llContent,llLayoutParams);

        LinearLayout llTop= new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        llLayoutParams.bottomMargin = centerInterval;
        llLayoutParams.rightMargin = ivBusRightMargin;
        llContent.addView(llTop,llLayoutParams);

        TextView tvContent = new TextView(GlobalContext.get());
        tvContent.setGravity(Gravity.CENTER_VERTICAL);
        tvContent.setEllipsize(TruncateAt.END);
        tvContent.setSingleLine();
        //llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvContentHeight);
        llLayoutParams = new LinearLayout.LayoutParams(0,tvContentHeight,1);
        llLayoutParams.gravity = Gravity.BOTTOM;
        llTop.addView(tvContent,llLayoutParams);

        //券
        ImageView ivJuan = new ImageView(GlobalContext.get());
        ivJuan.setVisibility(View.GONE);
        ivJuan.setImageDrawable(LayouUtil.getDrawable("dz_juan"));
        llLayoutParams = new LinearLayout.LayoutParams(ivBusSide,ivBusSide);
        llLayoutParams.rightMargin = ivBusRightMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        llTop.addView(ivJuan,llLayoutParams);

        //有优惠
        ImageView ivHui= new ImageView(GlobalContext.get());
        ivHui.setVisibility(View.GONE);
        ivHui.setImageDrawable(LayouUtil.getDrawable("dz_hui"));
        llLayoutParams = new LinearLayout.LayoutParams(ivBusSide,ivBusSide);
        llLayoutParams.rightMargin = ivBusRightMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        llTop.addView(ivHui,llLayoutParams);

        //有团购
        ImageView ivTuan= new ImageView(GlobalContext.get());
        ivTuan.setVisibility(View.GONE);
        ivTuan.setImageDrawable(LayouUtil.getDrawable("dz_tuan"));
        llLayoutParams = new LinearLayout.LayoutParams(ivBusSide,ivBusSide);
        llLayoutParams.rightMargin = ivBusRightMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        llTop.addView(ivTuan,llLayoutParams);

        //评分
		ImageView ivStarGrade = new ImageView(GlobalContext.get());
		ivStarGrade.setVisibility(View.GONE);
        llLayoutParams = new LinearLayout.LayoutParams(ivStarWidth,ivStarHeight);
        llLayoutParams.rightMargin = ivBusRightMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        llTop.addView(ivStarGrade,llLayoutParams);

        LinearLayout llDesc= new LinearLayout(GlobalContext.get());
        llDesc.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        llContent.addView(llDesc,llLayoutParams);

        TextView tvDistance = new TextView(GlobalContext.get());
        tvDistance.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.TOP;
        llDesc.addView(tvDistance,llLayoutParams);

        TextView tvDesc = new TextView(GlobalContext.get());
        tvDistance.setGravity(Gravity.CENTER_VERTICAL);
        tvDesc.setSingleLine();
        tvDesc.setEllipsize(TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(0,tvDescHeight,1);
        llLayoutParams.leftMargin = tvDescLeftMargin;
        llLayoutParams.gravity = Gravity.TOP;
        llDesc.addView(tvDesc,llLayoutParams);

        TextView tvCost = new TextView(GlobalContext.get());
        tvCost.setId(ViewUtils.generateViewId());
        tvCost.setGravity(Gravity.CENTER_VERTICAL);
        tvCost.setSingleLine();
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        llLayoutParams.rightMargin = tvDescLeftMargin;
        llLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        llDesc.addView(tvCost,llLayoutParams);
			
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);

		TextViewUtil.setTextSize(tvNum,tvNumSize);
		TextViewUtil.setTextColor(tvNum,tvNumColor);
        TextViewUtil.setTextSize(tvCost,tvPriceSize);
        TextViewUtil.setTextColor(tvCost,tvDescColor);
		TextViewUtil.setTextSize(tvContent,tvContentSize);
		TextViewUtil.setTextColor(tvContent,tvContentColor);
        TextViewUtil.setTextSize(tvDistance,tvDistanceSize);
        TextViewUtil.setTextColor(tvDistance,tvDistanceColor);
		TextViewUtil.setTextSize(tvDesc,tvDescSize);
		TextViewUtil.setTextColor(tvDesc,tvDescColor);

		if (poi instanceof BusinessPoiDetail) {
            ivHui.setVisibility(View.VISIBLE);
            ivTuan.setVisibility(View.VISIBLE);
            ivJuan.setVisibility(View.VISIBLE);
            ivStarGrade.setVisibility(View.VISIBLE);

			BusinessPoiDetail bpd = (BusinessPoiDetail) poi;
			double score = bpd.getScore();
			if (score < 1) {
				ivStarGrade.setVisibility(View.GONE);
			} else {
				ivStarGrade.setImageDrawable(getSoreMark(score));
			}

			if (bpd.isHasCoupon()) {
				ivHui.setVisibility(View.VISIBLE);
			} else {
				ivHui.setVisibility(View.GONE);
			}

			if (bpd.isHasDeal()) {
				ivTuan.setVisibility(View.VISIBLE);
			} else {
				ivTuan.setVisibility(View.GONE);
			}

			int price = (int) bpd.getAvgPrice();
			if (price > 0) {
				String txt = String.format("￥%d/人", price);
				tvCost.setText(LanguageConvertor.toLocale(txt));
			} else {
				tvCost.setVisibility(View.GONE);
			}
		} else {
			ivHui.setVisibility(View.GONE);
			ivTuan.setVisibility(View.GONE);
			ivJuan.setVisibility(View.GONE);
			ivStarGrade.setVisibility(View.GONE);
		}

		// 设置显示距离
		double d = poi.getDistance() / 1000.0;
		String mDistance = "";
		if (d < 1) {
			mDistance = d * 1000 + "m";
		} else {
			mDistance = String.format("%.1f", d) + "km";
		}
		tvDistance.setText(LanguageConvertor.toLocale(mDistance));
		tvDistance.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));
		
		tvNum.setText(String.valueOf(position + 1));
		/*tvNum.setClickable(true);
		tvNum.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// if(mTextClickListener != null){
				// mTextClickListener.onClick(position);
				// }
				checkPoiDeal(position);
			}
		});*/
		tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
		tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);
		
		return itemView;
	}

    private View createBusItemView(final RippleView itemView, final int position,Poi poi,boolean showDivider){
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
        itemView.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llContents,layoutParams);

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        tvNum.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
        llLayoutParams.gravity = Gravity.CENTER;
        llLayoutParams.leftMargin = tvNumHorMargin;
        llLayoutParams.rightMargin = tvNumHorMargin;
        mNumberView.add(tvNum);
        llContents.addView(tvNum,llLayoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        llContents.addView(llContent,llLayoutParams);

        LinearLayout llTop= new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        llLayoutParams.bottomMargin = centerInterval;
        llContent.addView(llTop,llLayoutParams);

        TextView tvContent = new TextView(GlobalContext.get());
        tvContent.setGravity(Gravity.CENTER_VERTICAL);
        tvContent.setEllipsize(TruncateAt.END);
        tvContent.setSingleLine();
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvContentHeight);
        llLayoutParams.gravity = Gravity.BOTTOM;
        llTop.addView(tvContent,llLayoutParams);

        LinearLayout llDesc= new LinearLayout(GlobalContext.get());
        llDesc.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        llContent.addView(llDesc,llLayoutParams);

        TextView tvDistance = new TextView(GlobalContext.get());
        tvDistance.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llDesc.addView(tvDistance,llLayoutParams);

        TextView tvDesc = new TextView(GlobalContext.get());
        tvDistance.setGravity(Gravity.CENTER_VERTICAL);
        tvDesc.setSingleLine();
        tvDesc.setEllipsize(TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvDescHeight);
        llLayoutParams.leftMargin = tvDescLeftMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llDesc.addView(tvDesc,llLayoutParams);

        LinearLayout llStar= new LinearLayout(GlobalContext.get());
        llStar.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,ivBusSide);
        llLayoutParams.topMargin = centerInterval;
        llContent.addView(llStar,llLayoutParams);

        //券
        ImageView ivJuan = new ImageView(GlobalContext.get());
        ivJuan.setVisibility(View.GONE);
        ivJuan.setImageDrawable(LayouUtil.getDrawable("dz_juan"));
        llLayoutParams = new LinearLayout.LayoutParams(ivBusSide,ivBusSide);
        llLayoutParams.rightMargin = ivBusRightMargin;
        llLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        llStar.addView(ivJuan,llLayoutParams);

        //有优惠
        ImageView ivHui= new ImageView(GlobalContext.get());
        ivHui.setVisibility(View.GONE);
        ivHui.setImageDrawable(LayouUtil.getDrawable("dz_hui"));
        llLayoutParams = new LinearLayout.LayoutParams(ivBusSide,ivBusSide);
        llLayoutParams.rightMargin = ivBusRightMargin;
        llLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        llStar.addView(ivHui,llLayoutParams);

        //有团购
        ImageView ivTuan= new ImageView(GlobalContext.get());
        ivTuan.setVisibility(View.GONE);
        ivTuan.setImageDrawable(LayouUtil.getDrawable("dz_tuan"));
        llLayoutParams = new LinearLayout.LayoutParams(ivBusSide,ivBusSide);
        llLayoutParams.rightMargin = ivBusRightMargin;
        llLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        llStar.addView(ivTuan,llLayoutParams);

        //评分
        ImageView ivStarGrade = new ImageView(GlobalContext.get());
        ivStarGrade.setVisibility(View.GONE);
        llLayoutParams = new LinearLayout.LayoutParams(ivStarWidth,ivStarHeight);
        llLayoutParams.rightMargin = ivBusRightMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        llStar.addView(ivStarGrade,llLayoutParams);

        View view = new View(GlobalContext.get());
        llLayoutParams = new LinearLayout.LayoutParams(0,LayoutParams.WRAP_CONTENT,1);
        llStar.addView(view,llLayoutParams);

        TextView tvCost = new TextView(GlobalContext.get());
        tvCost.setId(ViewUtils.generateViewId());
        tvCost.setGravity(Gravity.CENTER_VERTICAL);
        tvCost.setSingleLine();
		tvCost.setIncludeFontPadding(false);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        llLayoutParams.rightMargin = tvDescLeftMargin;
        llLayoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        llStar.addView(tvCost,llLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        TextViewUtil.setTextSize(tvNum,tvNumSize);
        TextViewUtil.setTextColor(tvNum,tvNumColor);
        TextViewUtil.setTextSize(tvCost,tvPriceSize);
        TextViewUtil.setTextColor(tvCost,tvDescColor);
        TextViewUtil.setTextSize(tvContent,tvContentSize);
        TextViewUtil.setTextColor(tvContent,tvContentColor);
        TextViewUtil.setTextSize(tvDistance,tvDistanceSize);
        TextViewUtil.setTextColor(tvDistance,tvDistanceColor);
        TextViewUtil.setTextSize(tvDesc,tvDescSize);
        TextViewUtil.setTextColor(tvDesc,tvDescColor);

        if (poi instanceof BusinessPoiDetail) {
            ivHui.setVisibility(View.VISIBLE);
            ivTuan.setVisibility(View.VISIBLE);
            ivJuan.setVisibility(View.VISIBLE);
            ivStarGrade.setVisibility(View.VISIBLE);

            BusinessPoiDetail bpd = (BusinessPoiDetail) poi;
            double score = bpd.getScore();
            if (score < 1) {
                ivStarGrade.setVisibility(View.GONE);
            } else {
                ivStarGrade.setImageDrawable(getSoreMark(score));
            }

            if (bpd.isHasCoupon()) {
                ivHui.setVisibility(View.VISIBLE);
            } else {
                ivHui.setVisibility(View.GONE);
            }

            if (bpd.isHasDeal()) {
                ivTuan.setVisibility(View.VISIBLE);
            } else {
                ivTuan.setVisibility(View.GONE);
            }

            int price = (int) bpd.getAvgPrice();
            if (price > 0) {
                String txt = String.format("￥%d/人", price);
                tvCost.setText(LanguageConvertor.toLocale(txt));
            } else {
                tvCost.setVisibility(View.GONE);
            }
        } else {
            ivHui.setVisibility(View.GONE);
            ivTuan.setVisibility(View.GONE);
            ivJuan.setVisibility(View.GONE);
            ivStarGrade.setVisibility(View.GONE);
        }

        // 设置显示距离
        double d = poi.getDistance() / 1000.0;
        String mDistance = "";
        if (d < 1) {
            mDistance = d * 1000 + "m";
        } else {
            mDistance = String.format("%.1f", d) + "km";
        }
        tvDistance.setText(LanguageConvertor.toLocale(mDistance));
        tvDistance.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));

        tvNum.setText(String.valueOf(position + 1));
        /*tvNum.setClickable(true);
        tvNum.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if(mTextClickListener != null){
				// mTextClickListener.onClick(position);
				// }
				checkPoiDeal(position);
			}
		});*/
        tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
        tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

        divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);
        Log.d("jack", "createBusItemView: "+ivStarGrade.getVisibility());
        return itemView;
    }

    private View createHistoryItemView(final RippleView itemView, final int position,Poi poi,boolean showDivider){
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
        itemView.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llContents,layoutParams);

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        tvNum.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
        llLayoutParams.gravity = Gravity.CENTER;
        llLayoutParams.leftMargin = tvNumHorMargin;
        llLayoutParams.rightMargin = tvNumHorMargin;
        mNumberView.add(tvNum);
        llContents.addView(tvNum,llLayoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(0,LayoutParams.MATCH_PARENT,1);
        llContents.addView(llContent,llLayoutParams);

        LinearLayout llTop= new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        llLayoutParams.bottomMargin = centerInterval;
        llContent.addView(llTop,llLayoutParams);

        TextView tvContent = new TextView(GlobalContext.get());
        tvContent.setEllipsize(TruncateAt.END);
        tvContent.setSingleLine();
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvContentHeight);
        llLayoutParams.gravity = Gravity.BOTTOM;
        llTop.addView(tvContent,llLayoutParams);

        LinearLayout llDesc= new LinearLayout(GlobalContext.get());
        llDesc.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        llContent.addView(llDesc,llLayoutParams);

        TextView tvDistance = new TextView(GlobalContext.get());
        tvDistance.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.TOP;
        llDesc.addView(tvDistance,llLayoutParams);

        TextView tvDesc = new TextView(GlobalContext.get());
        tvDistance.setGravity(Gravity.CENTER_VERTICAL);
        tvDesc.setSingleLine();
        tvDesc.setEllipsize(TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,tvDescHeight);
        llLayoutParams.leftMargin = tvDescLeftMargin;
        llLayoutParams.gravity = Gravity.TOP;
        llDesc.addView(tvDesc,llLayoutParams);

        final FrameLayout flDelete = new FrameLayout(GlobalContext.get());
        llLayoutParams = new LinearLayout.LayoutParams(flDeleteWidth,LayoutParams.MATCH_PARENT);
        llContents.addView(flDelete,llLayoutParams);
        flDelete.setClickable(true);
        flDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONBuilder jb = new JSONBuilder();
                jb.put("index", position);
                jb.put("action","delete");
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
                        jb.toBytes(), null);
            }
        });

        ImageView ivDelete = new ImageView(GlobalContext.get());
        ivDelete.setImageDrawable(LayouUtil.getDrawable("close"));
        ivDelete.setAlpha(0.3f);
        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(ivDeleteWidth,ivDeleteHeight);
        flLayoutParams.gravity  = Gravity.CENTER;
        flDelete.addView(ivDelete,flLayoutParams);

        View lineView = new View(GlobalContext.get());
        lineView.setBackgroundColor(tvContentColor);
        lineView.setAlpha(0.2f);
        flLayoutParams = new FrameLayout.LayoutParams(dividerHeight,historyLineHeight);
        flLayoutParams.gravity  = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        flDelete.addView(lineView,flLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        TextViewUtil.setTextSize(tvNum,tvNumSize);
        TextViewUtil.setTextColor(tvNum,tvNumColor);
        TextViewUtil.setTextSize(tvContent,tvContentSize);
        TextViewUtil.setTextColor(tvContent,tvContentColor);
        TextViewUtil.setTextSize(tvDistance,tvDistanceSize);
        TextViewUtil.setTextColor(tvDistance,tvDistanceColor);
        TextViewUtil.setTextSize(tvDesc,tvDescSize);
        TextViewUtil.setTextColor(tvDesc,tvDescColor);

        // 设置显示距离
        double d = poi.getDistance() / 1000.0;
        String mDistance = "";
        if (d < 1) {
            mDistance = d * 1000 + "m";
        } else {
            mDistance = String.format("%.1f", d) + "km";
        }
        tvDistance.setText(LanguageConvertor.toLocale(mDistance));
        tvDistance.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));

        tvNum.setText(String.valueOf(position + 1));
        /*tvNum.setClickable(true);
        tvNum.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                // if(mTextClickListener != null){
                // mTextClickListener.onClick(position);
                // }
                checkPoiDeal(position);
            }
        });*/
        tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
        tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

        divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);

        return itemView;
    }
	
	private Drawable getSoreMark(double score) {
		if (score < 1.0f) {
			return LayouUtil.getDrawable("dz_icon_star0");
		} else if (score < 2.0f) {
			return LayouUtil.getDrawable("dz_icon_star1");
		} else if (score < 3.0f) {
			return LayouUtil.getDrawable("dz_icon_star2");
		} else if (score < 4.0f) {
			return LayouUtil.getDrawable("dz_icon_star3");
		} else if (score < 5.0f) {
			return LayouUtil.getDrawable("dz_icon_star4");
		} else if (score < 6.0f) {
			return LayouUtil.getDrawable("dz_icon_star5");
		} else if (score < 7.0f) {
			return LayouUtil.getDrawable("dz_icon_star6");
		} else if (score < 8.0f) {
			return LayouUtil.getDrawable("dz_icon_star7");
		} else if (score < 9.0f) {
			return LayouUtil.getDrawable("dz_icon_star8");
		} else if (score < 10.0f) {
			return LayouUtil.getDrawable("dz_icon_star9");
		} else {
			return LayouUtil.getDrawable("dz_icon_star10");
		}
	}
//	private TextClickListener  mTextClickListener; 
//	public void setNumberOnClickListener(DefaultMapPoiListView.TextClickListener listener){
//		mTextClickListener = listener;
//	}
	List<View> mNumberView = new ArrayList<View>();
	public void checkPoiDeal(int index){
		for(View view :mNumberView){
			view.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		}
		if(index < mNumberView.size()){
			mNumberView.get(index).setBackground(LayouUtil.getDrawable("poi_item_circle_seleted_bg"));
		}
	}

	@Override
	public void updateItemSelect(int index) {
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
}
