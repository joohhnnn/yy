package com.txznet.comm.ui.theme.test.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
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
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.view.RoundImageView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.PoiListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.data.WeChatListViewData;
import com.txznet.comm.ui.viewfactory.data.WeChatListViewData.WeChatBean;
import com.txznet.comm.ui.viewfactory.view.IWechatListView;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZWechatManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.sdk.TXZWechatManager.ImageListener;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class WeChatListView extends IWechatListView {

	private static WeChatListView sInstance = new WeChatListView();
	
	private List<View> mItemViews;

    private int dividerHeight;

    private int tvNumWidth;    //序号宽度
    private int tvNumHeight;    //序号高度
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
    private int tvNumHorMargin;    //序号左右边距
    private int ivHeadSize;    //头像大小
    private int ivHeadRightMargin;    //头像右边距
    private int tvCountSize;    //内容字体大小
    private int tvCountColor;    //内容字体颜色

	//private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	private WeChatListViewData weChatListViewData;
	
	private WeChatListView() {
	}

	public static WeChatListView getInstance(){
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
		weChatListViewData = (WeChatListViewData) data;
		WinLayout.getInstance().vTips = weChatListViewData.vTips;
		LogUtil.logd(WinLayout.logTag+ "weChatListViewData.vTips: "+weChatListViewData.vTips);

		View view = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = createViewFull(weChatListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = createViewHalf(weChatListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(weChatListViewData);
				break;
		}


		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.isListView = true;
		viewAdapter.object = WeChatListView.getInstance();
		return viewAdapter;
	}

	private View createViewFull(WeChatListViewData weChatListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(weChatListViewData,"wechat","微信");
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),weChatListViewData.mTitleInfo.curPage,weChatListViewData.mTitleInfo.maxPage);
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
		//progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < weChatListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,weChatListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (weChatListViewData.count < 4){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,4 - weChatListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/

		return llLayout;
	}

	private View createViewHalf(WeChatListViewData weChatListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(weChatListViewData,"wechat","微信");
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),weChatListViewData.mTitleInfo.curPage,weChatListViewData.mTitleInfo.maxPage);
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
		//progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < weChatListViewData.count; i++) {
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,weChatListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (weChatListViewData.count < 4){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,4 - weChatListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/

		return llLayout;
	}

	private View createViewNone(WeChatListViewData weChatListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(weChatListViewData,"wechat","微信");
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),weChatListViewData.mTitleInfo.curPage,weChatListViewData.mTitleInfo.maxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llPager,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
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
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
				}
			}
		});
		//progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < weChatListViewData.count; i++) {
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,weChatListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (weChatListViewData.count < 3){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,3 - weChatListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/

		return llLayout;
	}

	@Override
	public void init() {
		super.init();
		// 初始化配置，例如字体颜色等
		dividerHeight = 1;
		tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
		tvCountColor =  Color.parseColor(LayouUtil.getString("color_main_title"));

		if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            tvNumWidth = 6 * unit;
            tvNumHeight = 6 * unit;
            tvNumSize = (int) LayouUtil.getDimen("vertical_h0");
            tvNumHorMargin = 2 * unit;
            ivHeadSize = 7 * unit;
            ivHeadRightMargin = unit;
            tvCountSize = (int) LayouUtil.getDimen("vertical_h2");
		}else {
            int unit = (int) LayouUtil.getDimen("unit");
            tvNumWidth = 6 * unit;
            tvNumHeight = 6 * unit;
            tvNumSize = (int) LayouUtil.getDimen("h0");
            tvNumHorMargin = 2 * unit;
            ivHeadSize = 7 * unit;
            ivHeadRightMargin = unit;
            tvCountSize = (int) LayouUtil.getDimen("h2");
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
	
	
	private View createItemView(int position, WeChatBean weChatBean,boolean showDivider){
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
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(flContent,layoutParams);
		
		/*GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(mProgressBar, mFLayoutParams);*/
		//progressBars.add(mProgressBar);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams mFLayoutParams  = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llContent, mFLayoutParams);
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumWidth,tvNumHeight);
		mLLayoutParams.leftMargin = tvNumHorMargin;
		mLLayoutParams.rightMargin = tvNumHorMargin;
		mLLayoutParams.gravity = Gravity.CENTER;
		llContent.addView(tvNum,mLLayoutParams);
		
		final RoundImageView ivHead = new RoundImageView(GlobalContext.get());
		mLLayoutParams = new LinearLayout.LayoutParams(ivHeadSize,ivHeadSize);
		mLLayoutParams.rightMargin = ivHeadRightMargin;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContent.addView(ivHead,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContent.addView(tvContent,mLLayoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		//divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);
		
		TextViewUtil.setTextSize(tvNum,tvNumSize);
		TextViewUtil.setTextColor(tvNum,tvNumColor);
		TextViewUtil.setTextSize(tvContent,tvCountSize);
		TextViewUtil.setTextColor(tvContent,tvCountColor);

		tvNum.setText(String.valueOf(position + 1));
		tvContent.setText(StringUtils.isEmpty(weChatBean.name) ?""  : LanguageConvertor.toLocale(weChatBean.name));
		ivHead.setImageDrawable(LayouUtil.getDrawable("default_head"));
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
		
		divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

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
