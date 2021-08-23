package com.txznet.resholder.theme.ironman.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RoundImageView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.data.WeChatListViewData;
import com.txznet.comm.ui.viewfactory.data.WeChatListViewData.WeChatBean;
import com.txznet.comm.ui.viewfactory.view.IWechatListView;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZWechatManager;
import com.txznet.sdk.TXZWechatManager.ImageListener;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class WeChatListView extends IWechatListView {

	private static WeChatListView sInstance = new WeChatListView();
	
	private View mView; //当前显示的View
	private List<View> mItemViews;
	
	//字体等参数配置
	
	private Drawable llContentBg;
	
	private int flContentMarginTop;
	private int flContentMarginBottom;
	private Drawable tvNumBg;
	private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;
	private int tvContentMarginLeft;
	
	private int ivHeadWidth;
	private int ivHeadHeight;
	private int ivHeadMarginLeft;	
	
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

	private Drawable dfHead;
	private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	private WeChatListViewData weChatListViewData;
	
	private WeChatListView() {
	}

	public static WeChatListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
//		LogUtil.logd("updateProgress " + progress + "," + selection);
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
		weChatListViewData = (WeChatListViewData) data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(weChatListViewData);
		
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
		for (int i = 0; i < weChatListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					ConfigUtil.getDisplayLvItemH(false));
			layoutParams.topMargin = listItemMarginTop;
			View itemView = createItemView(i, weChatListViewData.getData().get(i));
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = WeChatListView.getInstance();
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
		tvContentMarginLeft = (int) LayouUtil.getDimen("y16");
		ivHeadWidth = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_WX_HEAD_WIDTH);
		ivHeadHeight = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_WX_HEAD_HEIGHT);
		ivHeadMarginLeft = (int) LayouUtil.getDimen("y16");
		
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
		
		tvNumSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_INDEX_SIZE1);
		tvNumColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_INDEX_COLOR1);
		tvContentSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_ITEM_SIZE1);
		tvContentColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_ITEM_COLOR1);
		
		dfHead = LayouUtil.getDrawable("default_head");
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
	
	private View createItemView(int position, WeChatBean weChatBean){
		RelativeLayout itemView = new RelativeLayout(GlobalContext.get());
		itemView.setBackground(LayouUtil.getDrawable("list_item_bg"));
		itemView.setPadding(0, 0, 0, 0);
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
		
		FrameLayout flBg = new FrameLayout(GlobalContext.get());
		flBg.setBackground(LayouUtil.getDrawable("list_item_wechat_head_bg"));
		mLLayoutParams = new LinearLayout.LayoutParams(ivHeadWidth,ivHeadHeight);
		flBg.setPadding((int)LayouUtil.getDimen("y2"), (int)LayouUtil.getDimen("y2"), (int)LayouUtil.getDimen("y2"), (int)LayouUtil.getDimen("y2"));
		mLLayoutParams.leftMargin = ivHeadMarginLeft;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContent.addView(flBg,mLLayoutParams);
		
		final RoundImageView ivHead = new RoundImageView(GlobalContext.get());
		mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER;
		flBg.addView(ivHead,mFLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.leftMargin = tvContentMarginLeft;
		llContent.addView(tvContent,mLLayoutParams);
		
		TextViewUtil.setTextSize(tvNum,tvNumSize);
		TextViewUtil.setTextColor(tvNum,tvNumColor);
		TextViewUtil.setTextSize(tvContent,tvContentSize);
		TextViewUtil.setTextColor(tvContent,tvContentColor);

		
		tvNum.setText(String.valueOf(position + 1));
		tvContent.setText(StringUtils.isEmpty(weChatBean.name) ? "" : LanguageConvertor.toLocale(weChatBean.name));
		ivHead.setImageDrawable(dfHead);
		TXZWechatManager.getInstance().getUsericon(weChatBean.id, new ImageListener() {
			@Override
			public void onImageReady(String id, String imgPath) {
				if (ivHead != null) {
					File header = new File(imgPath);
					if (header.exists()) {
						ImageLoader.getInstance().displayImage("file://" + header.getAbsolutePath(), new ImageViewAware(ivHead));
					}
				}
			}
		});
		
		
		return itemView;
	}

	
}
