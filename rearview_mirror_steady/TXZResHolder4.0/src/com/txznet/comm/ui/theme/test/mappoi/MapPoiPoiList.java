package com.txznet.comm.ui.theme.test.mappoi;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.view.ListTitleView;
import com.txznet.comm.ui.theme.test.view.MapPoiListView.TextClickListener;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.MapPoiListViewData;
import com.txznet.comm.ui.viewfactory.data.PoiListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IPoiListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.TXZCommUtil;

@SuppressLint("NewApi")
public class MapPoiPoiList extends IPoiListView {

	private static MapPoiPoiList sInstance = null;
	
	private List<View> mItemViews; 
	
	
	//字体等参数配置
	
	/*private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;
	private int llDetailMarginLeft;
	private int tvContentMarginRight;
	private int llMarkIconMarginLeft;
	private int llMarkIconMarginRight;
	private int ivStarGradeWidth;
	private int ivStarGradeHeight;
	private int ivStarGradeMarginRight;
	private int ivJuanWidth;
	private int ivJuanHeight;
	private int ivJuanMarginRight;
	private int ivHuiWidth;
	private int ivHuiHeight;
	private int ivHuiMarginRight;
	
	private int ivTuanWidth;
	private int ivTuanHeight;
	private int ivTuanMarginRight;
	private int ivTuanMarginLeft;
	
	private int tvDescMarginLeft;
	private int tvDescMarginRight;
	
	private int tvCostMarginRight;
	
	private int tvDistanceMinWidth;
	private int tvDistanceMarginLeft;
	private int tvDistanceMarginRight;*/
	
	private int dividerHeight;
	
	/*private float tvNumSize;
	private int tvNumColor;
	private float tvContentSize;
	private int tvContentColor;
	private float tvDescSize;
	private int tvDescColor;
	private float tvDistanceSize;
	private int tvDistanceColor;
	private float tvCostSize;
	private int tvCostColor;*/

	//private int unit;    //基本单位
    private int tvNumSide;    //序号宽高
    private int tvNumHorMargin;    //序号左右边距
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
    private int tvContentSize;    //内容字体大小
    private int tvContentHeight;    //内容行高
    private int tvContentColor;    //内容字体颜色
    private int centerInterval;    //内容到距离的间距
    private int centerInterval2;    //评分到距离的间距
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
	
	private int contentBgColor;
	private int itemBgColor;
	private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	
	protected MapPoiPoiList() {
		init();
	}

	public static MapPoiPoiList getInstance() {
		if (sInstance == null) {
			synchronized (MapPoiPoiList.class) {
				if (sInstance == null) {
					sInstance = new MapPoiPoiList();
				}
			}
		}
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
	public boolean hasViewAnimation() {
		return true;
	}
	
	@Override
	public void release() {
		super.release();
		if (mItemViews != null) {
			mItemViews.clear();
		}
		if (mNumberView != null) {
			mNumberView.clear();
		}
		if (progressBars != null) {
			progressBars.clear();
		}
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		if(mNumberView != null){
			mNumberView.clear();
		}

		MapPoiListViewData poiListViewData = (MapPoiListViewData) data;
		//WinLayout.getInstance().vTips = poiListViewData.vTips;

		mIsHistory = poiListViewData.action.equals(Poi.PoiAction.ACTION_NAV_HISTORY);

		View view = null;
        mIsUseNewLayout = poiListViewData.isBus;

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
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.isListView = true;
		viewAdapter.object = MapPoiPoiList.getInstance();
		return viewAdapter;
	}

	private View createViewFull(MapPoiListViewData poiListViewData){
		/*isVertical = ScreenUtil.mListViewRectHeight > ScreenUtil.mListViewRectWidth;
		mIsUseNewLayout = ( !isVertical && poiListViewData.isBus);
		mIsHistory = poiListViewData.action.equals(PoiAction.ACTION_NAV_HISTORY);*/
//		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData);
//		mCurPage = poiListViewData.mTitleInfo.curPage;
//		mMaxPage = poiListViewData.mTitleInfo.maxPage;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
//		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
//		layoutParams.gravity = Gravity.CENTER_VERTICAL;
//		llLayout.addView(titleViewAdapter.view,layoutParams);


		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackgroundColor(Color.argb(204, 0, 0, 0));
		int itemHeight = ConfigUtil.getDisplayLvItemH(false);
		//LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight * ConfigUtil.getVisbileCount());
		LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		llLayout.addView(llContent,layoutParams);
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
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,mIsUseNewLayout?SizeConfig.itemHeightPro:SizeConfig.itemHeight);
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			//createItemView(itemView,i,poiListViewData.getData().get(i),i != SizeConfig.getInstance().getPageMapPoiCount() - 1);
            if(PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
                createHistoryItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.count - 1);
            }else {
                createItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.count - 1);
            }
//			itemView.setBackground(LayouUtil.getDrawable("list_item_bg"));
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		return llLayout;
	}

	private View createViewHalf(MapPoiListViewData poiListViewData){
		mIsUseNewLayout = ( !WinLayout.isVertScreen && poiListViewData.isBus);
		mIsHistory = poiListViewData.action.equals(PoiAction.ACTION_NAV_HISTORY);
//		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData);
//		mCurPage = poiListViewData.mTitleInfo.curPage;
//		mMaxPage = poiListViewData.mTitleInfo.maxPage;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
//		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
//		layoutParams.gravity = Gravity.CENTER_VERTICAL;
//		llLayout.addView(titleViewAdapter.view,layoutParams);


		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackgroundColor(Color.argb(204, 0, 0, 0));
		int itemHeight = ConfigUtil.getDisplayLvItemH(false);
		//LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight * ConfigUtil.getVisbileCount());
		LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		llLayout.addView(llContent,layoutParams);
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
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,mIsUseNewLayout?SizeConfig.itemHeightPro:SizeConfig.itemHeight);
			//createItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.mShowCount - 1);
            if(PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
                createHistoryItemView(itemView,i,poiListViewData.getData().get(i),i != (WinLayout.isVertScreen?poiListViewData.count:SizeConfig.pageMapPoiHistoryCount) - 1);
            }else {
                if (mIsUseNewLayout){
                    createBusItemView(itemView,i,poiListViewData.getData().get(i),i != (WinLayout.isVertScreen?poiListViewData.count:SizeConfig.pageBusinessMapPoiCount) - 1);
                }else {
                    createItemView(itemView,i,poiListViewData.getData().get(i),i != (WinLayout.isVertScreen?poiListViewData.count:SizeConfig.pageMapPoiCount) - 1);
                }
            }
//			itemView.setBackground(LayouUtil.getDrawable("list_item_bg"));
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		return llLayout;
	}


	//不会调用，无屏禁用了地图模式
	private View createViewNone(MapPoiListViewData poiListViewData){
		isVertical = ScreenUtil.mListViewRectHeight > ScreenUtil.mListViewRectWidth;
		mIsUseNewLayout = ( !isVertical && poiListViewData.isBus);
		mIsHistory = poiListViewData.action.equals(PoiAction.ACTION_NAV_HISTORY);
//		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData);
//		mCurPage = poiListViewData.mTitleInfo.curPage;
//		mMaxPage = poiListViewData.mTitleInfo.maxPage;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
//		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
//		layoutParams.gravity = Gravity.CENTER_VERTICAL;
//		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		//llContent.setBackgroundColor(Color.argb(204, 0, 0, 0));
		//int itemHeight = SizeConfig.getInstance().getItemHeight();
		LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llContent,layoutParams);
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
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.getInstance().getItemHeight());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,mIsUseNewLayout?SizeConfig.itemHeightPro:SizeConfig.itemHeight);
			//createItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.mShowCount - 1);
            if(PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
                createHistoryItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.count - 1);
            }else {
                if (mIsUseNewLayout){
                    createBusItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.count - 1);
                }else {
                    createItemView(itemView,i,poiListViewData.getData().get(i),i != poiListViewData.count - 1);
                }
            }
//			itemView.setBackground(LayouUtil.getDrawable("list_item_bg"));
			llContent.addView(itemView, layoutParams);
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
		// 初始化配置，例如字体颜色等
        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvContentColor =  Color.parseColor(LayouUtil.getString("color_main_title"));
        tvDistanceColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvDescColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
	}

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex){
//        switch (styleIndex) {
//            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
//                initFull();
//                break;
//            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
//                initHalf();
//                break;
//            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
//                initNone();
//                break;
//            default:
//                break;
//        }
        int unit = ViewParamsUtil.unit;
        tvNumSide = 6 * unit;
        tvNumHorMargin = unit;
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
        tvNumSize = ViewParamsUtil.h0;
        tvContentSize = ViewParamsUtil.h4;
        tvContentHeight = ViewParamsUtil.h4Height;
        centerInterval = SizeConfig.screenHeight < 480?0:ViewParamsUtil.centerInterval;
        centerInterval2 = ViewParamsUtil.centerInterval;
        tvDistanceSize = ViewParamsUtil.h7;
        tvDistanceHeight = ViewParamsUtil.h7Height;
        tvDescSize = ViewParamsUtil.h6;
        tvDescHeight = ViewParamsUtil.h6Height;
        tvDescLeftMargin = unit;
        ivBusSide = ViewParamsUtil.h1;
        ivBusRightMargin = unit / 2;
        ivStarHeight = SizeConfig.screenHeight < ViewParamsUtil.h6?0:ViewParamsUtil.h5;
        ivStarWidth = (int)(5.6 * ivStarHeight);
        tvPriceSize = ViewParamsUtil.h6;
        historyLineHeight = 6 * unit;
        ivDeleteWidth = 3 * unit;
        ivDeleteHeight = 3 * unit;
        flDeleteWidth = 7 * unit;
        if (styleIndex != StyleConfig.STYLE_ROBOT_NONE_SCREES && WinLayout.isVertScreen){
            tvContentSize = ViewParamsUtil.h3;
            tvContentHeight = ViewParamsUtil.h3Height;
            tvDistanceSize = ViewParamsUtil.h6;
            tvDistanceHeight = ViewParamsUtil.h6Height;
            tvDescSize = ViewParamsUtil.h5;
            tvDescHeight = ViewParamsUtil.h5Height;
            ivBusSide = ViewParamsUtil.h1 + 2;
            ivStarHeight = SizeConfig.screenHeight < ViewParamsUtil.h5?0:ViewParamsUtil.h4;
            ivStarWidth = (int)(5.6 * ivStarHeight);
            tvPriceSize = ViewParamsUtil.h5;
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
	
	
	/*private View createItemView(final RippleView itemView, final int position,Poi poi,boolean showDivider){

		LogUtil.logd(WinLayout.logTag+ "createItemView: MapPoi.name"+"--"+poi.getName()+"--"+poi.getGeoinfo());
		itemView.setTag(position);
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		itemView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()){
					case MotionEvent.ACTION_UP:
						showSelectItem((int)v.getTag());
						break;
				}
				return false;
			}
		});
		
		*//*GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(mProgressBar, layoutParams);
		progressBars.add(mProgressBar);	*//*
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setId(ViewUtils.generateViewId());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(tvNumWidth,tvNumHeight);
		layoutParams.leftMargin = tvNumMarginLeft;
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mNumberView.add(tvNum);
		itemView.addView(tvNum,layoutParams);
		
		View line = new View(GlobalContext.get());
		line.setId(ViewUtils.generateViewId());
//		line.setBackgroundColor(Color.WHITE);
		layoutParams = new RelativeLayout.LayoutParams(0,0);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		itemView.addView(line,layoutParams);
		
		//标题的那一栏
//		TextView tvSpace = new TextView(GlobalContext.get());
//		tvSpace.setId(ViewUtils.generateViewId());
//		tvSpace.setMinWidth(tvDistanceMinWidth);
//		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//		tvSpace.setPadding(0, 0, tvDistanceMarginRight, 0);
//		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		layoutParams.addRule(RelativeLayout.ABOVE, line.getId());
//		tvSpace.setVisibility(View.INVISIBLE);
//		itemView.addView(tvSpace,layoutParams);
		
		RelativeLayout rlTop = new RelativeLayout(GlobalContext.get());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		if(!TextUtils.isEmpty(poi.getName())){
		layoutParams.addRule(RelativeLayout.ABOVE, line.getId());
		}
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());	
		itemView.addView(rlTop,layoutParams);
		//评分
		ImageView ivStarGrade = new ImageView(GlobalContext.get());
		ivStarGrade.setVisibility(View.GONE);
		ivStarGrade.setId(ViewUtils.generateViewId());
		ivStarGrade.setScaleType(ScaleType.FIT_END);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,ivStarGradeHeight);
		layoutParams.rightMargin = tvDistanceMarginRight;
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlTop.addView(ivStarGrade,layoutParams);
		
		LinearLayout llMarkIcon = new LinearLayout(GlobalContext.get());
		llMarkIcon.setId(ViewUtils.generateViewId());
		llMarkIcon.setGravity(Gravity.CENTER_VERTICAL);
		llMarkIcon.setOrientation(LinearLayout.HORIZONTAL);
		llMarkIcon.setVisibility(View.GONE);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, ivStarGrade.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//		layoutParams.leftMargin = llMarkIconMarginLeft;
		//ayoutParams.rightMargin = llMarkIconMarginRight;
//		llMarkIcon.setPadding(0, 0, llMarkIconMarginRight, 0);
		rlTop.addView(llMarkIcon,layoutParams);
		
//		//评分
//		ImageView ivStarGrade = new ImageView(GlobalContext.get());
//		ivStarGrade.setVisibility(View.GONE);
//		ivStarGrade.setScaleType(ScaleType.FIT_END);
//		LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(ivStarGradeWidth,ivStarGradeHeight);
//		mLLayoutParams.rightMargin = ivStarGradeMarginRight;
//		llMarkIcon.addView(ivStarGrade,mLLayoutParams);
		
		//券
		ImageView ivJuan = new ImageView(GlobalContext.get());
		ivJuan.setScaleType(ScaleType.FIT_END);
		ivJuan.setVisibility(View.GONE);
		LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(ivJuanWidth,ivJuanHeight);
		mLLayoutParams.rightMargin = ivJuanMarginRight;
		// mLLayoutParams.leftMargin = ivJuanMarginLeft;
		ivJuan.setImageDrawable(LayouUtil.getDrawable("dz_juan"));
		llMarkIcon.addView(ivJuan,mLLayoutParams);
		
		//有优惠
		ImageView ivHui= new ImageView(GlobalContext.get());
		ivHui.setScaleType(ScaleType.FIT_END);
		ivHui.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivHuiWidth,ivHuiHeight);
		mLLayoutParams.rightMargin = ivHuiMarginRight;
		// mLLayoutParams.leftMargin = ivHuiMarginLeft;
		ivHui.setImageDrawable(LayouUtil.getDrawable("dz_hui"));
		llMarkIcon.addView(ivHui,mLLayoutParams);
		
		//有团购
		ImageView ivTuan= new ImageView(GlobalContext.get());
		ivTuan.setScaleType(ScaleType.FIT_END);
		ivTuan.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivTuanWidth,ivTuanHeight);
		mLLayoutParams.rightMargin = ivTuanMarginRight;
		// mLLayoutParams.leftMargin = ivTuanMarginLeft;
		ivTuan.setImageDrawable(LayouUtil.getDrawable("dz_tuan"));
		llMarkIcon.addView(ivTuan,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, llMarkIcon.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		// 和距离的边距一样
		layoutParams.rightMargin = tvDistanceMarginRight;
		layoutParams.leftMargin = llDetailMarginLeft;
		rlTop.addView(tvContent,layoutParams);
		
		
		//历史
		final RelativeLayout rlHistory = new RelativeLayout(GlobalContext.get());
		rlHistory.setVisibility(View.GONE);
		rlHistory.setId(ViewUtils.generateViewId());
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		itemView.addView(rlHistory,layoutParams);
		rlHistory.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				rlHistory.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				android.view.ViewGroup.LayoutParams layoutParams2 = rlHistory.getLayoutParams();
				int width = rlHistory.getBottom() - rlHistory.getTop();
				layoutParams2.width = width;
				rlHistory.setLayoutParams(layoutParams2);
			}
		});

		View historyLine = new View(GlobalContext.get());
		historyLine.setBackgroundColor(Color.parseColor("#7F4f4f4f"));
		layoutParams = new RelativeLayout.LayoutParams(dividerHeight,RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		layoutParams.topMargin = layoutParams.bottomMargin = (int) LayouUtil.getDimen("y20");
		rlHistory.addView(historyLine, layoutParams);
		
		ImageView historyDel = new ImageView(GlobalContext.get());
		historyDel.setScaleType(ScaleType.FIT_END);
		historyDel.setVisibility(View.VISIBLE);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		// mLLayoutParams.leftMargin = ivTuanMarginLeft;
		historyDel.setImageDrawable(LayouUtil.getDrawable("poi_history_del"));
		rlHistory.addView(historyDel, layoutParams);
		
		//下面的那一条
		FrameLayout flDistance = new FrameLayout(GlobalContext.get());
		flDistance.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (mIsHistory) {
			layoutParams.addRule(RelativeLayout.LEFT_OF, rlHistory.getId());
		} *//*else {
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		}*//*
		if(!TextUtils.isEmpty(poi.getName())){
		layoutParams.addRule(RelativeLayout.BELOW, line.getId());
		}else{
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		}
		//layoutParams.rightMargin = tvDistanceMarginRight;
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		itemView.addView(flDistance, layoutParams);
		if(TextUtils.isEmpty(poi.getName())){
			layoutParams = (RelativeLayout.LayoutParams)rlTop.getLayoutParams();
			layoutParams.addRule(RelativeLayout.LEFT_OF, flDistance.getId());
			rlTop.setLayoutParams(layoutParams);
		}
		
		ImageView ivStarGradeIn = new ImageView(GlobalContext.get());
		ivStarGradeIn.setVisibility(View.GONE);
		ivStarGradeIn.setId(ViewUtils.generateViewId());
		ivStarGradeIn.setScaleType(ScaleType.FIT_END);
		FrameLayout.LayoutParams flayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,ivStarGradeHeight);
		flDistance.addView(ivStarGradeIn,flayoutParams);
		
		TextView tvDistance = new TextView(GlobalContext.get());
		tvDistance.setId(ViewUtils.generateViewId());
		flayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
		//flayoutParams.gravity = Gravity.RIGHT;
		flayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flayoutParams.leftMargin =(int) tvDistanceSize;
		flDistance.addView(tvDistance,flayoutParams);
		
		TextView tvCost = new TextView(GlobalContext.get());
		tvCost.setId(ViewUtils.generateViewId());
		tvCost.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);
		tvCost.setSingleLine();
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		//layoutParams.addRule(RelativeLayout.LEFT_OF, flDistance.getId());
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.addRule(RelativeLayout.BELOW, line.getId());
		layoutParams.rightMargin = tvDescMarginRight;
//		tvCost.setPadding(0, 0, tvCostMarginRight, 0);
		itemView.addView(tvCost,layoutParams);
		
		TextView tvDesc = new TextView(GlobalContext.get());
		tvDesc.setSingleLine();
		tvDesc.setEllipsize(TruncateAt.END);
		tvDesc.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, flDistance.getId());
		//layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		layoutParams.addRule(RelativeLayout.BELOW, line.getId());
//		layoutParams.rightMargin = tvContentMarginRight;
//		layoutParams.leftMargin = llDetailMarginLeft;
		//tvDesc.setPadding(llDetailMarginLeft, 0, tvContentMarginRight, 0);
		//tvDesc.setPadding(0, 0, tvContentMarginRight, 0);
		tvDesc.setPadding(0, (int)((int)GlobalContext.get().getResources().getDisplayMetrics().density*3+0.5f), tvContentMarginRight, 0);
		layoutParams.rightMargin = (int)LayouUtil.getDimen("x89");
		itemView.addView(tvDesc,layoutParams);
		
		//第三条
		View line2 = new View(GlobalContext.get());
		line2.setId(ViewUtils.generateViewId());
//		line2.setBackgroundColor(Color.WHITE);
		layoutParams = new RelativeLayout.LayoutParams(0,0);	
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		layoutParams.addRule(RelativeLayout.BELOW, tvDesc.getId());
		itemView.addView(line2,layoutParams);
		
		RelativeLayout rlNewbus = new RelativeLayout(GlobalContext.get());
		rlNewbus.setVisibility(View.GONE);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.BELOW, line2.getId());
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());	
		itemView.addView(rlNewbus,layoutParams);
		
		final ImageView ivStarGradeNew = new ImageView(GlobalContext.get());
		ivStarGradeNew.setVisibility(View.GONE);
		ivStarGradeNew.setId(ViewUtils.generateViewId());
		ivStarGradeNew.setScaleType(ScaleType.FIT_START);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,ivStarGradeHeight);
		layoutParams.leftMargin = llDetailMarginLeft;
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlNewbus.addView(ivStarGradeNew,layoutParams);
		
		LinearLayout llMarkIconNew = new LinearLayout(GlobalContext.get());
		llMarkIconNew.setId(ViewUtils.generateViewId());
		llMarkIconNew.setGravity(Gravity.CENTER_VERTICAL);
		llMarkIconNew.setOrientation(LinearLayout.HORIZONTAL);
		llMarkIconNew.setVisibility(View.GONE);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, ivStarGradeNew.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//		layoutParams.leftMargin = llMarkIconMarginLeft;
		layoutParams.rightMargin = llMarkIconMarginRight;
//		llMarkIcon.setPadding(0, 0, llMarkIconMarginRight, 0);
		rlNewbus.addView(llMarkIconNew,layoutParams);

		//券
		ImageView ivJuanNew = new ImageView(GlobalContext.get());
		ivJuanNew.setScaleType(ScaleType.FIT_END);
		ivJuanNew.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivJuanWidth,ivJuanHeight);
		mLLayoutParams.rightMargin = ivJuanMarginRight;
		// mLLayoutParams.leftMargin = ivJuanMarginLeft;
		ivJuanNew.setImageDrawable(LayouUtil.getDrawable("dz_juan"));
		llMarkIconNew.addView(ivJuanNew,mLLayoutParams);
		
		//有优惠
		ImageView ivHuiNew= new ImageView(GlobalContext.get());
		ivHuiNew.setScaleType(ScaleType.FIT_END);
		ivHuiNew.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivHuiWidth,ivHuiHeight);
		mLLayoutParams.rightMargin = ivHuiMarginRight;
		// mLLayoutParams.leftMargin = ivHuiMarginLeft;
		ivHuiNew.setImageDrawable(LayouUtil.getDrawable("dz_hui"));
		llMarkIconNew.addView(ivHuiNew,mLLayoutParams);
		
		//有团购
		ImageView ivTuanNew= new ImageView(GlobalContext.get());
		ivTuanNew.setScaleType(ScaleType.FIT_END);
		ivTuanNew.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivTuanWidth,ivTuanHeight);
		mLLayoutParams.rightMargin = ivTuanMarginRight;
		// mLLayoutParams.leftMargin = ivTuanMarginLeft;
		ivTuanNew.setImageDrawable(LayouUtil.getDrawable("dz_tuan"));
		llMarkIconNew.addView(ivTuanNew,mLLayoutParams);
		
		TextView tvCostNew = new TextView(GlobalContext.get());
		tvCostNew.setId(ViewUtils.generateViewId());
		tvCostNew.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);
		tvCostNew.setSingleLine();
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.rightMargin = tvDistanceMarginRight;
//		tvCost.setPadding(0, 0, tvCostMarginRight, 0);
		rlNewbus.addView(tvCostNew,layoutParams);
		
		//历史
		
			

		
		// mLLayoutParams.leftMargin = ivTuanMarginLeft;
			
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		//divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);
		
		
		TextViewUtil.setTextSize(tvNum,tvNumSize);
		TextViewUtil.setTextColor(tvNum,tvNumColor);
		TextViewUtil.setTextSize(tvContent,tvContentSize);
		TextViewUtil.setTextColor(tvContent,tvContentColor);
		TextViewUtil.setTextSize(tvDesc,tvDescSize);
		TextViewUtil.setTextColor(tvDesc,tvDescColor);
		TextViewUtil.setTextSize(tvDistance,tvDistanceSize);
		TextViewUtil.setTextColor(tvDistance,tvDistanceColor);
		TextViewUtil.setTextSize(tvCost,tvCostSize);
		TextViewUtil.setTextColor(tvCost,tvCostColor);
		TextViewUtil.setTextSize(tvCostNew,tvCostSize);
		TextViewUtil.setTextColor(tvCostNew,tvCostColor);
		if(mIsHistory){
			rlHistory.setVisibility(View.VISIBLE);
			llMarkIcon.setVisibility(View.GONE);
			tvCost.setVisibility(View.GONE);
			ivStarGrade.setVisibility(View.GONE);
			ivStarGradeIn.setVisibility(View.GONE);
			flDistance.setVisibility(View.GONE);
//			flayoutParams = ( FrameLayout.LayoutParams )tvDistance.getLayoutParams();
//			layoutParams.addRule(RelativeLayout.LEFT_OF, rlHistory.getId());
//			tvDistance.setLayoutParams(flayoutParams);
			layoutParams = (RelativeLayout.LayoutParams)rlTop.getLayoutParams();
			layoutParams.addRule(RelativeLayout.LEFT_OF, rlHistory.getId());
			rlTop.setLayoutParams(layoutParams);
			rlHistory.setClickable(true);
			rlHistory.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					JSONBuilder jb = new JSONBuilder();
					jb.put("index", position);
					jb.put("action","delete");
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
							jb.toBytes(), null);
				}
			});
		} else if (mIsUseNewLayout && poi instanceof BusinessPoiDetail) {
			llMarkIcon.setVisibility(View.GONE);
			tvCost.setVisibility(View.GONE);
			ivStarGrade.setVisibility(View.GONE);
			ivStarGradeIn.setVisibility(View.GONE);

			rlNewbus.setVisibility(View.VISIBLE);
			llMarkIconNew.setVisibility(View.VISIBLE);
			tvCostNew.setVisibility(View.VISIBLE);
			ivStarGradeNew.setVisibility(View.VISIBLE);
			
			BusinessPoiDetail bpd = (BusinessPoiDetail) poi;
			double score = bpd.getScore();
			ivStarGradeNew.setImageDrawable(getSoreMark(score));
			ivStarGradeNew.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					ivStarGradeNew.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					Rect rect = ivStarGradeNew.getDrawable().getBounds();
	                Matrix m = ivStarGradeNew.getImageMatrix();  
	                float[] values = new float[10];  
	                m.getValues(values);  
	                float sx = values[0];  
	                float sy = values[4];  
	                int cw = (int)(rect.width() * sx);  
					RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) ivStarGradeNew.getLayoutParams();
					layoutParams.width = cw;
					LogUtil.loge("currentWidth:" + cw + "realWidth:" + ivStarGradeNew.getWidth());
				}
			});
			if (bpd.isHasCoupon()) {
				ivHuiNew.setVisibility(View.VISIBLE);
			} else {
				ivHuiNew.setVisibility(View.GONE);
			}

			if (bpd.isHasDeal()) {
				ivTuanNew.setVisibility(View.VISIBLE);
			} else {
				ivTuanNew.setVisibility(View.GONE);
			}

			int price = (int) bpd.getAvgPrice();
			if (price > 0) {
				String txt = String.format("￥%d/人", price);
				tvCostNew.setText(txt);
			} else {
				tvCostNew.setVisibility(View.GONE);
			}
		}else if (poi instanceof BusinessPoiDetail) {
			llMarkIcon.setVisibility(View.VISIBLE);
			tvCost.setVisibility(View.VISIBLE);
			ivStarGrade.setVisibility(View.VISIBLE);
			ivStarGradeIn.setVisibility(View.INVISIBLE);

			BusinessPoiDetail bpd = (BusinessPoiDetail) poi;
			double score = bpd.getScore();
			if (score < 1) {
				ivStarGrade.setVisibility(View.GONE);
			} else {
				ivStarGrade.setImageDrawable(getSoreMark(score));
				ivStarGradeIn.setImageDrawable(getSoreMark(score));
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
			llMarkIcon.setVisibility(View.GONE);
			tvCost.setVisibility(View.GONE);
			ivStarGrade.setVisibility(View.GONE);
			ivStarGradeIn.setVisibility(View.GONE);
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
		tvNum.setClickable(true);
		tvNum.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mTextClickListener != null){
					mTextClickListener.onClick(position);
				}
				checkPoiDeal(position);
			}
		});
		tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
		tvContent.setVisibility(View.VISIBLE);
		tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

		divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
		
		// itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
		// @Override
		// public void onFocusChange(View v, boolean hasFocus) {
		// LogUtil.logd("onFocusChange " + v.getTag() + "," + hasFocus);
		// RippleView rippleView = (RippleView) v;
		// if (hasFocus) {
		// rippleView.setBackgroundColor(Color.parseColor("#4AA5FA"));
		// } else {
		// rippleView.setBackgroundColor(Color.TRANSPARENT);
		// }
		// }
		// });
		
		return itemView;
	}*/

    private View createItemView(final RippleView itemView, final int position,Poi poi,boolean showDivider){

        LogUtil.logd(WinLayout.logTag+ "createItemView: Poi.name"+"--"+poi.getName()+"--"+poi.getGeoinfo());

        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
       /* itemView.setOnTouchListener(new View.OnTouchListener() {
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
        llLayoutParams.bottomMargin = centerInterval2;
        llLayoutParams.rightMargin = ivBusRightMargin;
        llContent.addView(llTop,llLayoutParams);

        TextView tvContent = new TextView(GlobalContext.get());
        tvContent.setGravity(Gravity.CENTER_VERTICAL);
        tvContent.setEllipsize(TruncateAt.END);
        tvContent.setSingleLine();
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
        tvNum.setClickable(true);
        tvNum.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                 if(mTextClickListener != null){
                 mTextClickListener.onClick(position);
                 }
                checkPoiDeal(position);
            }
        });
        tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
        tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

        divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);

        return itemView;
    }

    private View createBusItemView(final RippleView itemView, final int position,Poi poi,boolean showDivider){

        LogUtil.logd(WinLayout.logTag+ "createItemView: Poi.name"+"--"+poi.getName()+"--"+poi.getGeoinfo());

        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
       /* itemView.setOnTouchListener(new View.OnTouchListener() {
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
        //llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
        llLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,ivBusSide);
        llLayoutParams.topMargin = centerInterval2;
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
        tvNum.setClickable(true);
        tvNum.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                 if(mTextClickListener != null){
                 mTextClickListener.onClick(position);
                 }
                checkPoiDeal(position);
            }
        });
        tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
        tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

        divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);

        return itemView;
    }

    private View createHistoryItemView(final RippleView itemView, final int position,Poi poi,boolean showDivider){

        LogUtil.logd(WinLayout.logTag+ "createItemView: Poi.name"+"--"+poi.getName()+"--"+poi.getGeoinfo());

        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
       /* itemView.setOnTouchListener(new View.OnTouchListener() {
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
        llLayoutParams.bottomMargin = centerInterval2;
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
        tvNum.setClickable(true);
        tvNum.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mTextClickListener != null){
                    mTextClickListener.onClick(position);
                 }
                checkPoiDeal(position);
            }
        });
        tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
        tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

        divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);

        return itemView;
    }

	private boolean mIsHistory = false;
	private boolean mIsUseNewLayout = false;    //是否是美食信息
	private boolean isVertical = false;
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
	
	
	
	private TextClickListener  mTextClickListener; 
	public void setNumberOnClickListener(TextClickListener listener){
		mTextClickListener = listener;
	}
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
