package com.txznet.resholder.theme.ironman.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
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
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
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
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class PoiListView extends IPoiListView {

	private static PoiListView sInstance = new PoiListView();
	
	private View mView; //当前显示的View
	private List<View> mItemViews; 
	
	
	//字体等参数配置
	
	private Drawable llContentBg;
	
	private int flContentMarginTop;
	private int flContentMarginBottom;
	private int flContentMarginLeft;
	private int flContentMarginRight;
	private Drawable tvNumBg;
	private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;
	private int llDetailMarginLeft;
	private int llTopPaddingTop;
	private int tvContentMarginLeft;
	private int tvContentMarginRight;
	private int llMarkIconMarginLeft;
	private int llMarkIconMarginRight;
	private int ivStarGradeWidth;
	private int ivStarGradeHeight;
	private int ivStarGradeMarginRight;
	private int ivJuanWidth;
	private int ivJuanHeight;
	private int ivJuanMarginLeft;
	private int ivJuanMarginRight;
	private Drawable ivJuanDrawable;
	private int ivHuiWidth;
	private int ivHuiHeight;
	private int ivHuiMarginRight;
	private int ivHuiMarginLeft;
	private Drawable ivHuiDrawable;
	
	private int ivTuanWidth;
	private int ivTuanHeight;
	private int ivTuanMarginRight;
	private int ivTuanMarginLeft;
	private Drawable ivTuanDrawable;
	
	private int tvDescMarginLeft;
	private int tvDescMarginRight;
	
	private int tvCostMarginRight;
	
	private int tvDistanceMinWidth;
	private int tvDistanceMarginLeft;
	private int tvDistanceMarginRight;
	
	private int dividerHeight;
	private int listItemMarginTop;
	private int llContentPaddingLeft;
	private int llContentPaddingTop;
	private int llContentPaddingRight;
	private int llContentPaddingBottom;
	
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
	private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	
	private PoiListView() {
	}

	public static PoiListView getInstance(){
		return sInstance;
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
	public ViewAdapter getView(ViewData data) {
		PoiListViewData poiListViewData = (PoiListViewData) data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(poiListViewData);
		
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackground(llContentBg);
		llContent.setPadding(llContentPaddingLeft,llContentPaddingTop,llContentPaddingRight,llContentPaddingBottom);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llContent,layoutParams);
		if (RecordWin2Manager.getInstance().getLastViewType() == data.getType()) {
			llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController(ListViewItemAnim.TYPE_ROTATEANIMATION));
		}else {
			llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController(ListViewItemAnim.TYPE_TRANSLATE));
		}
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
		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < poiListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams.topMargin = listItemMarginTop;
			View itemView = createItemView(i, poiListViewData.getData().get(i));
			llContent.addView(itemView, layoutParams);
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
		// 初始化配置，例如字体颜色等
		llContentBg = LayouUtil.getDrawable("list_bg");
		flContentMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINTOP);
		flContentMarginBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINBOTTOM);
		tvNumBg = LayouUtil.getDrawable("poi_item_circle_bg");
		tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
		llDetailMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT);
		llTopPaddingTop = (int) LayouUtil.getDimen("x40");
		tvContentMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		tvContentMarginRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		llMarkIconMarginLeft = (int) LayouUtil.getDimen("x16");
		llMarkIconMarginRight = (int) LayouUtil.getDimen("x5");
		ivStarGradeWidth = (int) LayouUtil.getDimen("x124");
		ivStarGradeHeight = (int) LayouUtil.getDimen("y20");
		ivStarGradeMarginRight = (int) LayouUtil.getDimen("x4");
		ivJuanWidth = (int) LayouUtil.getDimen("x20");
		ivJuanHeight = (int) LayouUtil.getDimen("y20");
		ivJuanMarginLeft = (int) LayouUtil.getDimen("x2");
		ivJuanMarginRight = (int) LayouUtil.getDimen("x2");
		ivJuanDrawable = LayouUtil.getDrawable("dz_juan");
		ivHuiWidth = (int) LayouUtil.getDimen("x20");
		ivHuiHeight = (int) LayouUtil.getDimen("y20");
		ivHuiMarginRight = (int) LayouUtil.getDimen("x2");
		ivHuiMarginLeft = (int) LayouUtil.getDimen("x2");
		ivHuiDrawable = LayouUtil.getDrawable("dz_hui");
		ivTuanWidth = (int) LayouUtil.getDimen("x20");
		ivTuanHeight = (int) LayouUtil.getDimen("y20");
		ivTuanMarginRight = (int) LayouUtil.getDimen("x2");
		ivTuanMarginLeft = (int) LayouUtil.getDimen("x2");
		ivTuanDrawable = LayouUtil.getDrawable("dz_tuan");
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
		
		listItemMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_ITEM_MARGINTOP);
		llContentPaddingLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_CONTENT_PADDINGLEFT);
		llContentPaddingTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_PADDINGTOP);
		llContentPaddingRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_CONTENT_PADDINGRIGHT);
		llContentPaddingBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_CONTENT_PADDINGBOTTOM);
	}
	

	@Override
	public void snapPage(boolean next) {
		LogUtil.logd("update snap "+next);
	}
	
	
	@Override
	public List<View> getFocusViews() {
		return mItemViews;
	}
	
	
	private View createItemView(int position, Poi poi){
		RelativeLayout itemView = new RelativeLayout(GlobalContext.get());
		itemView.setBackground(LayouUtil.getDrawable("list_item_bg"));
		itemView.setPadding(0, 0, 0, 0);
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
		tvNum.setBackground(tvNumBg);
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		layoutParams = new RelativeLayout.LayoutParams(tvNumWidth,tvNumHeight);
		layoutParams.leftMargin = tvNumMarginLeft;
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		itemView.addView(tvNum,layoutParams);
		
		View line = new View(GlobalContext.get());
		line.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams(0,0);
		layoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		itemView.addView(line,layoutParams);
		RelativeLayout rlTop = new RelativeLayout(GlobalContext.get());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ABOVE, line.getId());
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
		layoutParams.rightMargin = llMarkIconMarginRight;
//		llMarkIcon.setPadding(0, 0, llMarkIconMarginRight, 0);
		rlTop.addView(llMarkIcon,layoutParams);
		
		
		
		//券
		ImageView ivJuan = new ImageView(GlobalContext.get());
		ivJuan.setScaleType(ScaleType.FIT_END);
		ivJuan.setVisibility(View.GONE);
		LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(ivJuanWidth,ivJuanHeight);
		mLLayoutParams.rightMargin = ivJuanMarginRight;
		// mLLayoutParams.leftMargin = ivJuanMarginLeft;
		ivJuan.setImageDrawable(ivJuanDrawable);
		llMarkIcon.addView(ivJuan,mLLayoutParams);
		
		//有优惠
		ImageView ivHui= new ImageView(GlobalContext.get());
		ivHui.setScaleType(ScaleType.FIT_END);
		ivHui.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivHuiWidth,ivHuiHeight);
		mLLayoutParams.rightMargin = ivHuiMarginRight;
		// mLLayoutParams.leftMargin = ivHuiMarginLeft;
		ivHui.setImageDrawable(ivHuiDrawable);
		llMarkIcon.addView(ivHui,mLLayoutParams);
		
		//有团购
		ImageView ivTuan= new ImageView(GlobalContext.get());
		ivTuan.setScaleType(ScaleType.FIT_END);
		ivTuan.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivTuanWidth,ivTuanHeight);
		mLLayoutParams.rightMargin = ivTuanMarginRight;
		// mLLayoutParams.leftMargin = ivTuanMarginLeft;
		ivTuan.setImageDrawable(ivTuanDrawable);
		llMarkIcon.addView(ivTuan,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.LEFT_OF, llMarkIcon.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//		layoutParams.rightMargin = tvContentMarginRight;
		layoutParams.leftMargin = llDetailMarginLeft;
		rlTop.addView(tvContent,layoutParams);
		
		
		//下面的那一条
		FrameLayout flDistance = new FrameLayout(GlobalContext.get());
		flDistance.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
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
		tvDistance.setId(ViewUtils.generateViewId());
		flayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
		flayoutParams.gravity = Gravity.RIGHT;
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
		
//		View divider = new View(GlobalContext.get());
//		divider.setVisibility(View.GONE);
//		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
//		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
//		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//		itemView.addView(divider, layoutParams);
		
		
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
		
		tvNum.setText(String.valueOf(position + 1));
		tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
		tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

//		divider.setVisibility(position == 3?View.INVISIBLE:View.VISIBLE);
		
		
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
	
	
}
