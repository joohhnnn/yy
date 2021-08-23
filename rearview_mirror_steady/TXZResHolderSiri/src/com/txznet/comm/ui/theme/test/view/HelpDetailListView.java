package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.HelpDetailListViewData;
import com.txznet.comm.ui.viewfactory.data.HelpDetailListViewData.HelpDetailBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailListView;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;

@SuppressLint("NewApi")
public class HelpDetailListView extends IHelpDetailListView {

	private static HelpDetailListView sInstance = new HelpDetailListView();
	
	private View mView; //当前显示的View
	
	private List<View> mFocusViews;
	
	
	//字体等参数配置
	private Drawable llContentBg;
	
	private int flContentMarginTop;
	private int flContentMarginBottom;
	private int ivIconWidth;
	private int ivIconHeight;
	private int tvNumMarginLeft;
	private int llDetailMarginLeft;
	private int tvContentMarginLeft;
	private int tvContentMarginRight;
	
	private int tvDescMarginLeft;
	private int tvDescMarginRight;
	
	
	private int dividerHeight;
	
	private float tvContentSize;
	private int tvContentColor;
	private int tvContentColor2;
	private float tvDescSize;
	private int tvDescColor;
	private boolean isNew;

	private float tvTitleSize;
	private int tvTitleColor;
	private boolean hasNet = true;
	
	private HelpDetailListView() {
	}

	public static HelpDetailListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
		LogUtil.logd("updateProgress " + progress + "," + selection);
	}

	@Override
	public void release() {
		super.release();
		if (mFocusViews != null) {
			mFocusViews.clear();
		}
	}
	
	
	@Override
	public ViewAdapter getView(ViewData data) {
		HelpDetailListViewData helpListViewData = (HelpDetailListViewData) data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(helpListViewData);

		hasNet = helpListViewData.hasNet;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);
		
		int contentHeight = ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount();
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackground(llContentBg);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
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
		int itemHeight = (int)(contentHeight/(ConfigUtil.getVisbileCount()*1.5f));
		mFocusViews = new ArrayList<View>();
		for (int i = 0; i < helpListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,itemHeight);
			View itemView = null;
			if (i == 0) {
				isNew = helpListViewData.getData().get(i).isNew;
				itemView = createItemView(i,helpListViewData.getData().get(i),true);
			}else {
				itemView = createItemView(i,helpListViewData.getData().get(i),false);
				mFocusViews.add(itemView);
			}
			llContent.addView(itemView,layoutParams);
		}

		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = HelpDetailListView.getInstance();
		return viewAdapter;
	}

	@Override
	public void init() {
		super.init();
		// 初始化配置，例如字体颜色等
		llContentBg = LayouUtil.getDrawable("white_range_layout");
		flContentMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINTOP);
		flContentMarginBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINBOTTOM);
		ivIconWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_HELP_ICON_WIDTH);
		ivIconHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_HELP_ICON_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
		llDetailMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT);
		tvContentMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		tvContentMarginRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		tvDescMarginLeft = (int) LayouUtil.getDimen("x4");
		tvDescMarginRight = (int) LayouUtil.getDimen("x4");
		
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
		
		tvContentSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1);
		tvContentColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR2);
		tvContentColor2=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR3);
		tvDescSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE2);
		tvDescColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR2);

		tvTitleSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_LABEL_ITEM_SIZE1);
		tvTitleColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1);
	}
	

	@Override
	public void snapPage(boolean next) {
		LogUtil.logd("update snap "+next);
	}
	
	@Override
	public List<View> getFocusViews() {
		return mFocusViews;
	}

	/**
	 * 是否含有动画
	 * @return
	 */
	@Override
	public boolean hasViewAnimation() {
		return true;
	}
	
	private View createItemView(int position,HelpDetailBean helpBean,boolean showDivider){
		RelativeLayout itemView = new RelativeLayout(GlobalContext.get());
		itemView.setTag(position);
//		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
//		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;
		itemView.addView(flContent,layoutParams);
		
//		GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
//		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
//		flContent.addView(mProgressBar, mFLayoutParams);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llContent, mFLayoutParams);
		
//		ImageView ivIcon = new ImageView(GlobalContext.get());
//		ivIcon.setPadding(0, 0, 0, 0);
//		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(ivIconWidth,ivIconHeight);
//		mLLayoutParams.leftMargin = tvNumMarginLeft;
//		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		llContent.addView(ivIcon,mLLayoutParams);
		
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.leftMargin = llDetailMarginLeft;
		llContent.addView(llDetail,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER;
//		mLLayoutParams.leftMargin = tvContentMarginLeft;
		mLLayoutParams.rightMargin = tvContentMarginRight;
		llDetail.addView(tvContent,mLLayoutParams);
		
//		TextView tvDesc = new TextView(GlobalContext.get());
//		tvDesc.setSingleLine();
//		tvDesc.setEllipsize(TruncateAt.END);
//		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
////		mLLayoutParams.leftMargin = tvDescMarginLeft;
//		mLLayoutParams.rightMargin = tvDescMarginRight;
//		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		llDetail.addView(tvDesc,mLLayoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);
		
//		ImageView ivArrow = new ImageView(GlobalContext.get());
//		ivArrow.setPadding(0, 0, 0, 0);
//		mLLayoutParams = new LinearLayout.LayoutParams(ivIconWidth/2,ivIconHeight/2);
//		mLLayoutParams.rightMargin = tvNumMarginLeft;
//		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		ivArrow.setImageDrawable(LayouUtil.getDrawable("help_arrow"));
//		llContent.addView(ivArrow,mLLayoutParams);

		if (showDivider) {
			TextViewUtil.setTextSize(tvContent, tvTitleSize);
			TextViewUtil.setTextColor(tvContent, tvTitleColor);
		}else {
			TextViewUtil.setTextSize(tvContent, tvContentSize);
			if (!hasNet && (helpBean.netType == 1)){
				TextViewUtil.setTextColor(tvContent, tvContentColor2);
			} else {
				TextViewUtil.setTextColor(tvContent, tvContentColor);
			}
		}
//		TextViewUtil.setTextSize(tvDesc,tvDescSize);
//		TextViewUtil.setTextColor(tvDesc,tvDescColor);
//		if (isNew && (position == 0) || (!isNew)) {
//			// 是新显示的
//			if (helpBean.isNew) {
//				tvContent.setCompoundDrawablePadding((int) LayouUtil.getDimen("x4"));
//				Drawable drawable = LayouUtil.getDrawable("ic_help_new");
//				if (drawable != null) {
//					int height = drawable.getIntrinsicHeight();
//					float scale = (float) tvContent.getLineHeight() / (float) height * 0.65f;
//					drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * scale), (int) (height * scale));
//					tvContent.setCompoundDrawables(null, null, drawable, null);
//				}
//			}
//		}
		
//		ivIcon.setImageDrawable(LayouUtil.getDrawable(helpBean.iconName));
		tvContent.setText(StringUtils.isEmpty(helpBean.title) ?"" : helpBean.title);
//		tvDesc.setText(helpBean.intro);

//		mPb.setVisibility(musicItem.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
//		mPb.setProgress(musicItem.shouldWaiting ? musicItem.curPrg : 0);

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);
		itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setBackgroundColor(Color.parseColor("#4AA5FA"));
				} else {
					v.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});
		
		return itemView;
	}
	
	@Override
	public void updateItemSelect(int selection){

	}
}
