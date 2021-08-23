package com.txznet.resholder.wave.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
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
	
	private List<View> mItemViews;
	
	//字体等参数配置
	
	private int flContentMarginTop;
	private int flContentMarginBottom;
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
		LogUtil.logd("updateProgress " + progress + "," + selection);
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
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
		llLayout.addView(llContent,layoutParams);
		llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < poiListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					ConfigUtil.getDisplayLvItemH(false));
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
		llMarkIconMarginRight = (int) LayouUtil.getDimen("x14");
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
		tvDescMarginRight = (int) LayouUtil.getDimen("x4");
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
	
	private View createItemView(int position,Poi poi){
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;
		itemView.addView(flContent,layoutParams);
		
		GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(mProgressBar, mFLayoutParams);
		progressBars.add(mProgressBar);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llContent, mFLayoutParams);
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setBackground(tvNumBg);
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumWidth,tvNumHeight);
		mLLayoutParams.leftMargin = tvNumMarginLeft;
		mLLayoutParams.gravity = Gravity.CENTER;
		llContent.addView(tvNum,mLLayoutParams);
		
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.leftMargin = llDetailMarginLeft;
		llContent.addView(llDetail,mLLayoutParams);
		
		LinearLayout llTop = new LinearLayout(GlobalContext.get());
		llTop.setPadding(0, 0, 0 , 0);
		llTop.setGravity(Gravity.CLIP_VERTICAL);
		llTop.setOrientation(LinearLayout.HORIZONTAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llDetail.addView(llTop,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		mLLayoutParams.leftMargin = tvContentMarginLeft;
		mLLayoutParams.rightMargin = tvContentMarginRight;
		mLLayoutParams.weight = 1;
		llTop.addView(tvContent,mLLayoutParams);
		
		LinearLayout llMarkIcon = new LinearLayout(GlobalContext.get());
		llMarkIcon.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);
		llMarkIcon.setOrientation(LinearLayout.HORIZONTAL);
		llMarkIcon.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL|Gravity.END;
		mLLayoutParams.leftMargin = llMarkIconMarginLeft;
		mLLayoutParams.rightMargin = llMarkIconMarginRight;
		llTop.addView(llMarkIcon,mLLayoutParams);
		
		//评分
		ImageView ivStarGrade = new ImageView(GlobalContext.get());
		ivStarGrade.setVisibility(View.GONE);
		ivStarGrade.setScaleType(ScaleType.FIT_END);
		mLLayoutParams = new LinearLayout.LayoutParams(ivStarGradeWidth,ivStarGradeHeight);
		mLLayoutParams.rightMargin = ivStarGradeMarginRight;
		llMarkIcon.addView(ivStarGrade,mLLayoutParams);
		
		//券
		ImageView ivJuan = new ImageView(GlobalContext.get());
		ivJuan.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivJuanWidth,ivJuanHeight);
		mLLayoutParams.rightMargin = ivJuanMarginRight;
//		mLLayoutParams.leftMargin = ivJuanMarginLeft;
		ivJuan.setImageDrawable(ivJuanDrawable);
		llMarkIcon.addView(ivJuan,mLLayoutParams);
		
		//有优惠
		ImageView ivHui= new ImageView(GlobalContext.get());
		ivHui.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivHuiWidth,ivHuiHeight);
		mLLayoutParams.rightMargin = ivHuiMarginRight;
//		mLLayoutParams.leftMargin = ivHuiMarginLeft;
		ivHui.setImageDrawable(ivHuiDrawable);
		llMarkIcon.addView(ivHui,mLLayoutParams);
		
		//有团购
		ImageView ivTuan= new ImageView(GlobalContext.get());
		ivTuan.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivTuanWidth,ivTuanHeight);
		mLLayoutParams.rightMargin = ivTuanMarginRight;
//		mLLayoutParams.leftMargin = ivTuanMarginLeft;
		ivTuan.setImageDrawable(ivTuanDrawable);
		llMarkIcon.addView(ivTuan,mLLayoutParams);
		
		TextView tvSpace = new TextView(GlobalContext.get());
		tvSpace.setMinWidth(tvDistanceMinWidth);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
		mLLayoutParams.rightMargin = tvDistanceMarginRight;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;
		tvSpace.setVisibility(View.INVISIBLE);
		llTop.addView(tvSpace,mLLayoutParams);
		
		LinearLayout llBottom = new LinearLayout(GlobalContext.get());
		llBottom.setOrientation(LinearLayout.HORIZONTAL);
		llBottom.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		llDetail.addView(llBottom,mLLayoutParams);
		
		LinearLayout llDesc = new LinearLayout(GlobalContext.get());
		mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
		llBottom.addView(llDesc,mLLayoutParams);
		
		TextView tvDesc = new TextView(GlobalContext.get());
		tvDesc.setSingleLine();
		tvDesc.setEllipsize(TruncateAt.END);
		mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
//		mLLayoutParams.leftMargin = tvDescMarginLeft;
		mLLayoutParams.rightMargin = tvDescMarginRight;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llDesc.addView(tvDesc,mLLayoutParams);
		
		TextView tvCost = new TextView(GlobalContext.get());
		tvCost.setGravity(Gravity.CENTER_VERTICAL|Gravity.END);
		tvCost.setSingleLine();
		tvCost.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.END;
		mLLayoutParams.rightMargin = tvCostMarginRight;
		llDesc.addView(tvCost,mLLayoutParams);
		
		TextView tvDistance = new TextView(GlobalContext.get());
		tvDistance.setMinWidth(tvDistanceMinWidth);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		mLLayoutParams.leftMargin = tvDistanceMarginLeft;
		mLLayoutParams.rightMargin = tvDistanceMarginRight;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL|Gravity.RIGHT;
		llBottom.addView(tvDistance,mLLayoutParams);
		
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
		TextViewUtil.setTextSize(tvSpace,tvDistanceSize);
		TextViewUtil.setTextColor(tvDistance,tvDistanceColor);
		TextViewUtil.setTextSize(tvCost,tvCostSize);
		TextViewUtil.setTextColor(tvCost,tvCostColor);

		if (poi instanceof BusinessPoiDetail) {
			llMarkIcon.setVisibility(View.VISIBLE);
			tvCost.setVisibility(View.VISIBLE);
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
			llMarkIcon.setVisibility(View.GONE);
			tvCost.setVisibility(View.GONE);
		}

		// 设置显示距离
		double d = poi.getDistance() / 1000.0;
		String mDistance = "";
		if (d < 1) {
			mDistance = d * 1000 + "m";
		} else {
			mDistance = String.format("%.1f", d) + "km";
		}
		tvSpace.setText(LanguageConvertor.toLocale(mDistance));
		tvDistance.setText(LanguageConvertor.toLocale(mDistance));
		
		tvNum.setText(String.valueOf(position + 1));
		tvContent.setText(LanguageConvertor.toLocale(poi.getName()));
		tvDesc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));

		divider.setVisibility(View.VISIBLE);
		
		itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				RippleView rippleView = (RippleView) v;
				if (hasFocus) {
					rippleView.setBackgroundColor(Color.parseColor("#4AA5FA"));
				} else {
					rippleView.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});
		
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
