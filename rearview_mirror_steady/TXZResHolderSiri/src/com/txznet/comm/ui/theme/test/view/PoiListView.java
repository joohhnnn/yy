package com.txznet.comm.ui.theme.test.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
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

@SuppressLint("NewApi")
public class PoiListView extends IPoiListView {

	private static PoiListView sInstance = new PoiListView();
	
	private List<View> mItemViews; 
	
	
	//?????????????????????
	
	private int tvNumWidth;
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
	private int tvDistanceMarginRight;
	
	private int dividerHeight;
	
	private float tvNumSize;
	private int tvNumColor;
	private float tvContentSize;
	private int tvContentColor;
	private float tvDescSize;
	private int tvDescColor;
	private float tvDistanceSize;
	private int tvDistanceColor;
	private float tvCostSize;
	private int tvCostColor;
	
	private int contentBgColor;
	private int itemBgColor;
	private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	private boolean mIsHistory = false;
	private boolean mIsUseNewLayout = false;
	private boolean isVertical = false;
	
	private PoiListView() {
        mFlags = Integer.valueOf(0);
        // ????????????View????????????
        mFlags = mFlags | UPDATEABLE;
	}

	public static PoiListView getInstance(){
		return sInstance;
	}
	
	@Override
	public Object updateView(ViewData data) {
		return super.updateView(data);
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
		if (progressBars.size() > selection) {
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
		}
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
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		if(mNumberView != null){
			mNumberView.clear();
		}
		PoiListViewData poiListViewData = (PoiListViewData) data;
		mIsUseNewLayout = ( !isVertical && poiListViewData.isBus);
		mIsHistory = poiListViewData.action.equals(Poi.PoiAction.ACTION_NAV_HISTORY);
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData);
		mCurPage = poiListViewData.mTitleInfo.curPage;
		mMaxPage = poiListViewData.mTitleInfo.maxPage;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);
		
		int itemHeight = ConfigUtil.getDisplayLvItemH(false);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight * ConfigUtil.getVisbileCount());
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
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);
			// itemView.setBackground(LayouUtil.getDrawable("list_item_bg"));
			llContent.addView(itemView, layoutParams);
			if(PoiAction.ACTION_NAV_HISTORY.equals(poiListViewData.action)){
				createHistoryItemView(itemView,i,poiListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
			}else {
				createItemView(itemView,i,poiListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
			}
			mItemViews.add(itemView);
		}
		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = PoiListView.getInstance();
		return viewAdapter;
	}

	
	
	@Override
	public void init() {
		// ????????????
		if (mFlags == null) {
			mFlags = SUPPORT_DELETE;
			mFlags |= SUPPORT_CITY_TITLE;
		} else {
			mFlags |= SUPPORT_DELETE;
			mFlags |= SUPPORT_CITY_TITLE;
		}
		LogUtil.logd("init flags:" + mFlags);
		// ???????????????????????????????????????
		tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
		llDetailMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT);
		tvContentMarginRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		llMarkIconMarginLeft = (int) LayouUtil.getDimen("x16");
		llMarkIconMarginRight = (int) LayouUtil.getDimen("x5");
		ivStarGradeWidth = (int) LayouUtil.getDimen("x124");
		ivStarGradeHeight = (int) LayouUtil.getDimen("y20");
		ivStarGradeMarginRight = (int) LayouUtil.getDimen("x4");
		ivJuanWidth = (int) LayouUtil.getDimen("x20");
		ivJuanHeight = (int) LayouUtil.getDimen("y20");
		ivJuanMarginRight = (int) LayouUtil.getDimen("x2");
		ivHuiWidth = (int) LayouUtil.getDimen("x20");
		ivHuiHeight = (int) LayouUtil.getDimen("y20");
		ivHuiMarginRight = (int) LayouUtil.getDimen("x2");
		ivTuanWidth = (int) LayouUtil.getDimen("x20");
		ivTuanHeight = (int) LayouUtil.getDimen("y20");
		ivTuanMarginRight = (int) LayouUtil.getDimen("x2");
		ivTuanMarginLeft = (int) LayouUtil.getDimen("x2");
		tvDescMarginLeft = (int) LayouUtil.getDimen("x4");
		tvDescMarginRight = (int) LayouUtil.getDimen("x7");
		tvCostMarginRight = (int) LayouUtil.getDimen("x16");
		
		tvDistanceMinWidth = (int) LayouUtil.getDimen("x40");
		tvDistanceMarginLeft = (int) LayouUtil.getDimen("x16");
		tvDistanceMarginRight = (int) LayouUtil.getDimen("x16");
		
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
		
		tvNumSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_INDEX_SIZE1);
		tvNumColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_INDEX_COLOR1);
		tvContentSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_ITEM_SIZE1);
		tvContentColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_ITEM_COLOR1);
		tvDescSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_ITEM_SIZE2);
		tvDescColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_ITEM_COLOR2);
		tvDistanceSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_ITEM_SIZE2);
		tvDistanceColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_ITEM_COLOR2);
		tvCostSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_ITEM_SIZE2);
		tvCostColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_ITEM_COLOR2);
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
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		itemView.setBackgroundColor(Color.TRANSPARENT);
		
		GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(mProgressBar, layoutParams);
		progressBars.add(mProgressBar);	
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setId(ViewUtils.generateViewId());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		layoutParams = new RelativeLayout.LayoutParams(tvNumWidth,tvNumHeight);
		layoutParams.leftMargin = tvNumMarginLeft;
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mNumberView.add(tvNum);
		itemView.addView(tvNum,layoutParams);
		
		View line = new View(GlobalContext.get());
		line.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams(0,0);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		itemView.addView(line,layoutParams);
		
		RelativeLayout rlTop = new RelativeLayout(GlobalContext.get());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		if(!TextUtils.isEmpty(poi.getGeoinfo())){
		layoutParams.addRule(RelativeLayout.ABOVE, line.getId());
		}		
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		
		itemView.addView(rlTop,layoutParams);
		//??????
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
		layoutParams.rightMargin = llMarkIconMarginRight;
		rlTop.addView(llMarkIcon,layoutParams);
		
		//???
		ImageView ivJuan = new ImageView(GlobalContext.get());
		ivJuan.setScaleType(ScaleType.FIT_END);
		ivJuan.setVisibility(View.GONE);
		LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(ivJuanWidth,ivJuanHeight);
		mLLayoutParams.rightMargin = ivJuanMarginRight;
		// mLLayoutParams.leftMargin = ivJuanMarginLeft;
		ivJuan.setImageDrawable(LayouUtil.getDrawable("dz_juan"));
		llMarkIcon.addView(ivJuan,mLLayoutParams);
		
		//?????????
		ImageView ivHui= new ImageView(GlobalContext.get());
		ivHui.setScaleType(ScaleType.FIT_END);
		ivHui.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivHuiWidth,ivHuiHeight);
		mLLayoutParams.rightMargin = ivHuiMarginRight;
		// mLLayoutParams.leftMargin = ivHuiMarginLeft;
		ivHui.setImageDrawable(LayouUtil.getDrawable("dz_hui"));
		llMarkIcon.addView(ivHui,mLLayoutParams);
		
		//?????????
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
		// ????????????????????????
		layoutParams.rightMargin = tvDistanceMarginRight;
		layoutParams.leftMargin = llDetailMarginLeft;
		rlTop.addView(tvContent,layoutParams);
		
		
		//??????????????????
		FrameLayout flDistance = new FrameLayout(GlobalContext.get());
		flDistance.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		if(!TextUtils.isEmpty(poi.getGeoinfo())){
		layoutParams.addRule(RelativeLayout.BELOW, line.getId());
		}else{
			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		}		
		layoutParams.rightMargin = tvDistanceMarginRight;
		itemView.addView(flDistance, layoutParams);
		
		if(TextUtils.isEmpty(poi.getGeoinfo())){
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
		flayoutParams.gravity = Gravity.RIGHT;
		flayoutParams.leftMargin =(int) tvDistanceSize;
		flDistance.addView(tvDistance,flayoutParams);
		
		TextView tvCost = new TextView(GlobalContext.get());
		tvCost.setId(ViewUtils.generateViewId());
		tvCost.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);
		tvCost.setSingleLine();
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, flDistance.getId());
		layoutParams.addRule(RelativeLayout.BELOW, line.getId());
		layoutParams.rightMargin = tvDescMarginRight;
//		tvCost.setPadding(0, 0, tvCostMarginRight, 0);
		itemView.addView(tvCost,layoutParams);
		
		TextView tvDesc = new TextView(GlobalContext.get());
		tvDesc.setId(ViewUtils.generateViewId());
		tvDesc.setSingleLine();
		tvDesc.setEllipsize(TruncateAt.END);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, tvCost.getId());
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		layoutParams.addRule(RelativeLayout.BELOW, line.getId());
//		layoutParams.rightMargin = tvContentMarginRight;
//		layoutParams.leftMargin = llDetailMarginLeft;
		tvDesc.setPadding(llDetailMarginLeft, 0, tvContentMarginRight, 0);
		itemView.addView(tvDesc,layoutParams);
		
		//?????????
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

		//???
		ImageView ivJuanNew = new ImageView(GlobalContext.get());
		ivJuanNew.setScaleType(ScaleType.FIT_END);
		ivJuanNew.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivJuanWidth,ivJuanHeight);
		mLLayoutParams.rightMargin = ivJuanMarginRight;
		// mLLayoutParams.leftMargin = ivJuanMarginLeft;
		ivJuanNew.setImageDrawable(LayouUtil.getDrawable("dz_juan"));
		llMarkIconNew.addView(ivJuanNew,mLLayoutParams);
		
		//?????????
		ImageView ivHuiNew= new ImageView(GlobalContext.get());
		ivHuiNew.setScaleType(ScaleType.FIT_END);
		ivHuiNew.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivHuiWidth,ivHuiHeight);
		mLLayoutParams.rightMargin = ivHuiMarginRight;
		// mLLayoutParams.leftMargin = ivHuiMarginLeft;
		ivHuiNew.setImageDrawable(LayouUtil.getDrawable("dz_hui"));
		llMarkIconNew.addView(ivHuiNew,mLLayoutParams);
		
		//?????????
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
			
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
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
		if (mIsUseNewLayout && poi instanceof BusinessPoiDetail) {
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
					//??????ImageView???Image???????????????  
	                Matrix m = ivStarGradeNew.getImageMatrix();  
	                float[] values = new float[10];  
	                m.getValues(values);  
	                  
	                //Image????????????????????????????????????????????????x???y?????????????????????  
	                float sx = values[0];  
	                float sy = values[4];  
	                  
	                //??????Image?????????????????????????????????  
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
				String txt = String.format("???%d/???", price);
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
				String txt = String.format("???%d/???", price);
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

		// ??????????????????
		double d = poi.getDistance() / 1000.0;
		String mDistance = "";
		if (d < 1) {
			mDistance = d * 1000 + "???";
		} else {
			mDistance = String.format("%.1f", d) + "??????";
		}
		tvDistance.setText(LanguageConvertor.toLocale(mDistance));
		
		tvNum.setText(String.valueOf(position + 1));
		tvNum.setClickable(true);
		tvNum.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// if(mTextClickListener != null){
				// mTextClickListener.onClick(position);
				// }
				checkPoiDeal(position);
			}
		});
		tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
		tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);
		
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
	}

	private View createHistoryItemView(final RippleView itemView, final int position,Poi poi,boolean showDivider){
		itemView.setTag(position);
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		
		GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(mProgressBar, layoutParams);
		progressBars.add(mProgressBar);	
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setId(ViewUtils.generateViewId());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		layoutParams = new RelativeLayout.LayoutParams(tvNumWidth,tvNumHeight);
		layoutParams.leftMargin = tvNumMarginLeft;
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mNumberView.add(tvNum);
		itemView.addView(tvNum,layoutParams);
		
		View line = new View(GlobalContext.get());
		line.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams(0,0);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		itemView.addView(line,layoutParams);
		
		final RelativeLayout rlTop = new RelativeLayout(GlobalContext.get());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ABOVE, line.getId());
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		
		itemView.addView(rlTop,layoutParams);
		//??????
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
		layoutParams.rightMargin = llMarkIconMarginRight;
//		llMarkIcon.setPadding(0, 0, llMarkIconMarginRight, 0);
		rlTop.addView(llMarkIcon,layoutParams);
		
		//???
		ImageView ivJuan = new ImageView(GlobalContext.get());
		ivJuan.setScaleType(ScaleType.FIT_END);
		ivJuan.setVisibility(View.GONE);
		LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(ivJuanWidth,ivJuanHeight);
		mLLayoutParams.rightMargin = ivJuanMarginRight;
		// mLLayoutParams.leftMargin = ivJuanMarginLeft;
		ivJuan.setImageDrawable(LayouUtil.getDrawable("dz_juan"));
		llMarkIcon.addView(ivJuan,mLLayoutParams);
		
		//?????????
		ImageView ivHui= new ImageView(GlobalContext.get());
		ivHui.setScaleType(ScaleType.FIT_END);
		ivHui.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivHuiWidth,ivHuiHeight);
		mLLayoutParams.rightMargin = ivHuiMarginRight;
		// mLLayoutParams.leftMargin = ivHuiMarginLeft;
		ivHui.setImageDrawable(LayouUtil.getDrawable("dz_hui"));
		llMarkIcon.addView(ivHui,mLLayoutParams);
		
		//?????????
		ImageView ivTuan= new ImageView(GlobalContext.get());
		ivTuan.setScaleType(ScaleType.FIT_END);
		ivTuan.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivTuanWidth,ivTuanHeight);
		mLLayoutParams.rightMargin = ivTuanMarginRight;
		// mLLayoutParams.leftMargin = ivTuanMarginLeft;
		ivTuan.setImageDrawable(LayouUtil.getDrawable("dz_tuan"));
		llMarkIcon.addView(ivTuan,mLLayoutParams);
		
		final TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, llMarkIcon.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		// ????????????????????????
		layoutParams.rightMargin = tvDistanceMarginRight;
		layoutParams.leftMargin = llDetailMarginLeft;
		rlTop.addView(tvContent,layoutParams);
		
		//??????
		final RelativeLayout rlHistory = new RelativeLayout(GlobalContext.get());
		rlHistory.setVisibility(View.VISIBLE);
		rlHistory.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x100"),RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		itemView.addView(rlHistory,layoutParams);
		rlHistory.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@Override
			public void onGlobalLayout() {
				rlHistory.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				android.view.ViewGroup.LayoutParams layoutParams2 = rlHistory.getLayoutParams();
				int width = rlHistory.getBottom() - rlHistory.getTop();
				layoutParams2.width = width;
				RelativeLayout.LayoutParams rlToplayoutParams = (RelativeLayout.LayoutParams)rlTop.getLayoutParams();
				rlToplayoutParams.rightMargin = width;
				rlTop.setLayoutParams(rlToplayoutParams);
				rlHistory.setLayoutParams(layoutParams2);
			}
		});
		rlHistory.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				JSONBuilder jb = new JSONBuilder();
				jb.put("index", position);
				jb.put("action", "delete");
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
						jb.toBytes(), null);
			}
		});

		View historyLine = new View(GlobalContext.get());
		historyLine.setBackgroundColor(Color.parseColor("#7F4f4f4f"));
		layoutParams = new RelativeLayout.LayoutParams(dividerHeight,RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		layoutParams.topMargin = layoutParams.bottomMargin = (int) LayouUtil.getDimen("y20");
		rlHistory.addView(historyLine, layoutParams);
		
		ImageView historyDel = new ImageView(GlobalContext.get());
		historyDel.setVisibility(View.VISIBLE);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		// mLLayoutParams.leftMargin = ivTuanMarginLeft;
		historyDel.setImageDrawable(LayouUtil.getDrawable("poi_history_del"));
		rlHistory.addView(historyDel, layoutParams);
		
		//??????????????????
		FrameLayout flDistance = new FrameLayout(GlobalContext.get());
		flDistance.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, rlHistory.getId());
		layoutParams.addRule(RelativeLayout.BELOW, line.getId());
		layoutParams.rightMargin = tvDistanceMarginRight;
		itemView.addView(flDistance, layoutParams);
		
		ImageView ivStarGradeIn = new ImageView(GlobalContext.get());
		ivStarGradeIn.setVisibility(View.GONE);
		ivStarGradeIn.setId(ViewUtils.generateViewId());
		ivStarGradeIn.setScaleType(ScaleType.FIT_END);
		FrameLayout.LayoutParams flayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,ivStarGradeHeight);
		flDistance.addView(ivStarGradeIn,flayoutParams);
		
		TextView tvDistance = new TextView(GlobalContext.get());
		// ???????????????
		tvDistance.setVisibility(View.INVISIBLE);
		tvDistance.setId(ViewUtils.generateViewId());
		flayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
				FrameLayout.LayoutParams.WRAP_CONTENT);
		flayoutParams.gravity = Gravity.RIGHT;
		flDistance.addView(tvDistance, flayoutParams);
		
		TextView tvCost = new TextView(GlobalContext.get());
		tvCost.setId(ViewUtils.generateViewId());
		tvCost.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);
		tvCost.setSingleLine();
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, flDistance.getId());
		layoutParams.addRule(RelativeLayout.BELOW, line.getId());
		layoutParams.rightMargin = tvDescMarginRight;
//		tvCost.setPadding(0, 0, tvCostMarginRight, 0);
		itemView.addView(tvCost,layoutParams);
		
		TextView tvDesc = new TextView(GlobalContext.get());
		tvDesc.setSingleLine();
		tvDesc.setEllipsize(TruncateAt.END);
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, tvCost.getId());
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		layoutParams.addRule(RelativeLayout.BELOW, line.getId());
//		layoutParams.rightMargin = tvContentMarginRight;
//		layoutParams.leftMargin = llDetailMarginLeft;
		tvDesc.setPadding(llDetailMarginLeft, 0, tvContentMarginRight, 0);
		itemView.addView(tvDesc,layoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
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

		if (poi instanceof BusinessPoiDetail) {
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
				String txt = String.format("???%d/???", price);
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

		// ??????????????????
		double d = poi.getDistance() / 1000.0;
		String mDistance = "";
		if (d < 1) {
			mDistance = d * 1000 + "???";
		} else {
			mDistance = String.format("%.1f", d) + "??????";
		}
		tvDistance.setText(LanguageConvertor.toLocale(mDistance));
		
		tvNum.setText(String.valueOf(position + 1));
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
	public void updateItemSelect(int selection){

	}
}
