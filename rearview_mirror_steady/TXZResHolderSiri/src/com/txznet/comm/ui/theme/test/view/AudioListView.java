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
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.AudioListViewData;
import com.txznet.comm.ui.viewfactory.data.AudioListViewData.AudioBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IAudioListView;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class AudioListView extends IAudioListView {

	private static AudioListView sInstance = new AudioListView();
	
	private List<View> mItemViews; 
	
	
	//字体等参数配置
	private int flContentMarginTop;
	private int flContentMarginBottom;
	private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;
	private int llDetailMarginLeft;
	private int tvContentMarginRight;
	
	private int tvDescMarginRight;
	
	
	private int dividerHeight;
	
	private float tvNumSize;
	private int tvNumColor;
	private float tvContentSize;
	private int tvContentColor;
	private float tvDescSize;
	private int tvDescColor;
	private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	
	private AudioListView() {
	}

	public static AudioListView getInstance(){
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
		AudioListViewData audioListViewData = (AudioListViewData)data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(audioListViewData);
		
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
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
					mViewStateListener.onAnimateStateChanged(animation, 3);
				}
			}
		});
		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < audioListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			View itemView = createItemView(i,audioListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = AudioListView.getInstance();
		return viewAdapter;
	}

	@Override
	public void init() {
		super.init();
		// 初始化配置，例如字体颜色等
		flContentMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINTOP);
		flContentMarginBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINBOTTOM);
		tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
		llDetailMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT);
		tvContentMarginRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		tvDescMarginRight = (int) LayouUtil.getDimen("x4");
		
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
		
		tvNumSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_AUDIO_INDEX_SIZE1);
		tvNumColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_AUDIO_INDEX_COLOR1);
		tvContentSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_AUDIO_ITEM_SIZE1);
		tvContentColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_AUDIO_ITEM_COLOR1);
		tvDescSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_AUDIO_ITEM_SIZE2);
		tvDescColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_AUDIO_ITEM_COLOR2);
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
	
	
	private View createItemView(int position,AudioBean audioBean,boolean showDivider){
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
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
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
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.leftMargin = llDetailMarginLeft;
		llContent.addView(llDetail,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.rightMargin = tvContentMarginRight;
		llDetail.addView(tvContent,mLLayoutParams);
		
		TextView tvDesc = new TextView(GlobalContext.get());
		tvDesc.setSingleLine();
		tvDesc.setEllipsize(TruncateAt.END);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		mLLayoutParams.leftMargin = tvDescMarginLeft;
		mLLayoutParams.rightMargin = tvDescMarginRight;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llDetail.addView(tvDesc,mLLayoutParams);
		
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
		
		tvNum.setText(String.valueOf(position + 1));
		tvContent.setText(StringUtils.isEmpty(audioBean.title) ?"" : LanguageConvertor.toLocale(audioBean.title));
		tvDesc.setText(LanguageConvertor.toLocale(audioBean.name));

		divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

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
	
	@Override
	public void updateItemSelect(int selection){

	}
}
