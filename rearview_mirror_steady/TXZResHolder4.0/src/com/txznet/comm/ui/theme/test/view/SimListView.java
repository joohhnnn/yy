package com.txznet.comm.ui.theme.test.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.SimListViewData;
import com.txznet.comm.ui.viewfactory.data.SimListViewData.SimBean;
import com.txznet.comm.ui.viewfactory.view.ISimListView;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class SimListView extends ISimListView {

	private static SimListView sInstance = new SimListView();
	
	private View mView; //当前显示的View
	
	private List<View> mItemViews; 
	private int mCurFocusIndex;
	
	//字体等参数配置
	
	private Drawable llContentBg;
	
	private int flContentMarginTop;
	private int flContentMarginBottom;
	private Drawable tvNumBg;
	private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;
	private int tvContentMarginLeft;
	
	private int tvPriceMarginRight;
	
	
	private int dividerHeight;
	
	private float tvNumSize;
	private int tvNumColor;
	private float tvContentSize;
	private int tvContentColor;
	private float tvPriceSize;
	private int tvPriceColor;
	private int tvPriceRawPadding;
	private float tvPriceRawSize;
	private int tvPriceRawColor;
	private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	
	private SimListView() {
	}

	public static SimListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
		/*LogUtil.logd("updateProgress " + progress + "," + selection);
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
		}*/
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
		SimListViewData simListViewData = (SimListViewData) data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(simListViewData);

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackground(llContentBg);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
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
		mCurFocusIndex = -1;
		for (int i = 0; i < simListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			View itemView = createItemView(i,simListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = SimListView.getInstance();
		return viewAdapter;
	}

	@Override
	public void init() {
		super.init();
		// 初始化配置，例如字体颜色等
		llContentBg = LayouUtil.getDrawable("white_range_layout");
		flContentMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINTOP);
		flContentMarginBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINBOTTOM);
		tvNumBg = LayouUtil.getDrawable("poi_item_circle_bg");
		tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
		tvContentMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		tvPriceMarginRight = (int) LayouUtil.getDimen("x10");
		
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
		
		tvNumSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SIM_INDEX_SIZE1);
		tvNumColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SIM_INDEX_COLOR1);
		tvContentSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SIM_ITEM_SIZE1);
		tvContentColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SIM_ITEM_COLOR1);
		tvPriceSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SIM_ITEM_SIZE2);
		tvPriceColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SIM_ITEM_COLOR2);
		tvPriceRawSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_SIM_ITEM_SIZE3);
		tvPriceRawColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_SIM_ITEM_COLOR3);
		
		tvPriceRawPadding = (int) LayouUtil.getDimen("y2");
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
	
	
	private View createItemView(int position, SimBean simBean,boolean showDivider){
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;
		itemView.addView(flContent,layoutParams);
		
		/*GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(mProgressBar, mFLayoutParams);
		progressBars.add(mProgressBar);*/
		
		RelativeLayout rlContent = new RelativeLayout(GlobalContext.get());
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(rlContent, mFLayoutParams);
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setBackground(tvNumBg);
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		tvNum.setId(ViewUtils.generateViewId());
		RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(tvNumWidth,tvNumHeight);
		mRLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mRLayoutParams.leftMargin = tvNumMarginLeft;
		rlContent.addView(tvNum,mRLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.CENTER);
		tvContent.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		mRLayoutParams.leftMargin = tvContentMarginLeft;
		mRLayoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		rlContent.addView(tvContent,mRLayoutParams);
		
		TextView tvPrice = new TextView(GlobalContext.get());
		tvPrice.setSingleLine();
		tvPrice.setEllipsize(TruncateAt.END);
		tvPrice.setGravity(Gravity.CENTER);
		tvPrice.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		mRLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mRLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mRLayoutParams.rightMargin = tvPriceMarginRight;
		rlContent.addView(tvPrice,mRLayoutParams);
		
		TextView tvPriceRaw = new TextView(GlobalContext.get());
		tvPriceRaw.setSingleLine();
		tvPriceRaw.setEllipsize(TruncateAt.END);
		tvPriceRaw.setGravity(Gravity.CENTER);
		tvPriceRaw.setAlpha(0.7f);
		tvPriceRaw.setId(ViewUtils.generateViewId());
		tvPriceRaw.setPadding(tvPriceRawPadding, tvPriceRawPadding, tvPriceRawPadding, tvPriceRawPadding);
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		mRLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		mRLayoutParams.addRule(RelativeLayout.LEFT_OF,tvPrice.getId());
		mRLayoutParams.rightMargin = tvPriceMarginRight;
		rlContent.addView(tvPriceRaw,mRLayoutParams);
		
		View line = new View(GlobalContext.get());
		line.setAlpha(0.5f);
		line.setBackgroundColor(Color.parseColor("#FFFFFF"));
		line.setId(ViewUtils.generateViewId());
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_LEFT,tvPriceRaw.getId());
		layoutParams.addRule(RelativeLayout.ALIGN_RIGHT,tvPriceRaw.getId());
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlContent.addView(line, layoutParams);
		
		
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
		TextViewUtil.setTextSize(tvPrice,tvPriceSize);
		TextViewUtil.setTextColor(tvPrice,tvPriceColor);
		TextViewUtil.setTextSize(tvPrice,tvPriceRawSize);
		TextViewUtil.setTextColor(tvPrice,tvPriceRawColor);

		
		
		tvNum.setText(String.valueOf(position + 1));
		tvContent.setText(LanguageConvertor.toLocale(simBean.title));
		tvPrice.setText("￥"+simBean.price/100.0);
		tvPriceRaw.setText("￥"+simBean.rawPrice/100.0);

//		mPb.setVisibility(musicItem.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
//		mPb.setProgress(musicItem.shouldWaiting ? musicItem.curPrg : 0);

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
		
		return itemView;
	}

	@Override
	public void updateItemSelect(int arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
}
