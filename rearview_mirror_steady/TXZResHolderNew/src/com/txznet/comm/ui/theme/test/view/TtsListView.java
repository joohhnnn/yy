package com.txznet.comm.ui.theme.test.view;

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
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.TtsListViewData;
import com.txznet.comm.ui.viewfactory.data.TtsListViewData.TtsBean;
import com.txznet.comm.ui.viewfactory.view.ITtsListView;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class TtsListView extends ITtsListView {

	private static TtsListView sInstance = new TtsListView();
	
	private List<View> mItemViews; 
	//?????????????????????
	/*private int flContentMarginTop;
	private int flContentMarginBottom;
	private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;
	private int tvContentMarginLeft;
	private int tvContentMarginRight;*/
	
	private int dividerHeight;
	
	/*private float tvNumSize;
	private int tvNumColor;
	private float tvContentSize;
	private int tvContentColor;*/
    private int tvNumSize;    //??????????????????
    private int tvNumColor;    //??????????????????
    private int tvNumSide;    //??????????????????
    private int tvNumHorMargin;    //??????????????????
    private int tvContentSize;    //??????????????????
    private int tvContentColor;    //??????????????????
	//private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);
	
	private TtsListView() {
	}

	public static TtsListView getInstance(){
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
	public ViewAdapter getView(ViewData data) {
		TtsListViewData ttsListViewData = (TtsListViewData) data;
		WinLayout.getInstance().vTips = ttsListViewData.vTips;
		LogUtil.logd(WinLayout.logTag+ "ttsListViewData.vTips: "+ttsListViewData.vTips);

		View view = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = createViewFull(ttsListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = createViewHalf(ttsListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(ttsListViewData);
				break;
		}

		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.isListView = true;
		viewAdapter.object = TtsListView.getInstance();
		return viewAdapter;
	}

	private View createViewFull(TtsListViewData ttsListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(ttsListViewData,"tts","????????????");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		mCurPage = ttsListViewData.mTitleInfo.curPage;
		mMaxPage = ttsListViewData.mTitleInfo.maxPage;
		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

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
		for (int i = 0; i < ttsListViewData.count; i++) {
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,ttsListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		return llLayout;
	}

	private View createViewHalf(TtsListViewData ttsListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(ttsListViewData,"tts","????????????");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		mCurPage = ttsListViewData.mTitleInfo.curPage;
		mMaxPage = ttsListViewData.mTitleInfo.maxPage;
		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

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
		for (int i = 0; i < ttsListViewData.count; i++) {
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,ttsListViewData.getData().get(i),i !=  SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		return llLayout;
	}

	private View createViewNone(TtsListViewData ttsListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(ttsListViewData,"tts","????????????");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
		//llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llLayout.addView(llContents,layoutParams);

		mCurPage = ttsListViewData.mTitleInfo.curPage;
		mMaxPage = ttsListViewData.mTitleInfo.maxPage;
		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llPager,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContents.addView(titleViewAdapter.view,layoutParams);

		View divider = new View(GlobalContext.get());
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		llContents.addView(divider, layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,6);
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
		for (int i = 0; i < ttsListViewData.count; i++) {
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,ttsListViewData.getData().get(i),i !=  SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		return llLayout;
	}

	@Override
	public void init() {
		super.init();
		// ???????????????????????????????????????
//		flContentMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINTOP);
//		flContentMarginBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINBOTTOM);
//		tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
//		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
//		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
//		//tvContentMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
//		tvContentMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT);
//		tvContentMarginRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));

        tvContentColor = Color.parseColor(LayouUtil.getString("color_main_title"));   //??????????????????
        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //??????????????????
	}

	//??????????????????????????????
	public void onUpdateParams(int styleIndex){
		switch (styleIndex) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				initFull();
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				initHalf();
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
				initNone();
				break;
			default:
				break;
		}
	}

    //??????????????????
    private void initFull(){
        // ???????????????????????????????????????
        if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            tvNumSize = (int) LayouUtil.getDimen("vertical_h0");    //??????????????????
            tvNumSide = 6 * unit;    //??????????????????
            tvNumHorMargin = unit;    //??????????????????
            tvContentSize = (int) LayouUtil.getDimen("vertical_h1");    //??????????????????
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            tvNumSize = (int) LayouUtil.getDimen("h0");    //??????????????????
            tvNumSide = 6 * unit;    //??????????????????
            tvNumHorMargin = unit;    //??????????????????
            tvContentSize = (int) LayouUtil.getDimen("h1");    //??????????????????
        }
    }

    //??????????????????
    private void initHalf(){
        // ???????????????????????????????????????
        if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            tvNumSize = (int) LayouUtil.getDimen("vertical_h0");    //??????????????????
            tvNumSide = 6 * unit;    //??????????????????
            tvNumHorMargin = unit;    //??????????????????
            tvContentSize = (int) LayouUtil.getDimen("vertical_h1");    //??????????????????
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            tvNumSize = (int) LayouUtil.getDimen("h0");    //??????????????????
            tvNumSide = 6 * unit;    //??????????????????
            tvNumHorMargin = unit;    //??????????????????
            tvContentSize = (int) LayouUtil.getDimen("h1");    //??????????????????
        }

    }

    //??????????????????
    private void initNone(){
        if (WinLayout.isVertScreen){
            tvNumSize = (int) LayouUtil.getDimen("x36");    //??????????????????
            tvNumSide = (int) LayouUtil.getDimen("x61");    //??????????????????
            tvNumHorMargin =(int) LayouUtil.getDimen("x10");    //??????????????????
            tvContentSize = (int) LayouUtil.getDimen("x31");    //??????????????????
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            tvNumSize = (int) LayouUtil.getDimen("h0_none");    //??????????????????
            tvNumSide = 6 * unit;    //??????????????????
            tvNumHorMargin = unit;    //??????????????????
            tvContentSize = (int) LayouUtil.getDimen("h1_none");    //??????????????????
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
	
	
	@Override
	public boolean supportKeyEvent() {
		return true;
	}
	
	
	/**
	 * ??????????????????
	 * @return
	 */
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
		/*if (progressBars != null) {
			progressBars.clear();
		}*/
	}
	
	private View createItemView(int position, TtsBean ttsBean,boolean showDivider){
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		ListTitleView.getInstance().mItemViews = mItemViews;
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(flContent,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llContent, mFLayoutParams);

		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
		mLLayoutParams.leftMargin = tvNumHorMargin;
		mLLayoutParams.rightMargin = tvNumHorMargin;
		mLLayoutParams.gravity = Gravity.CENTER;
		llContent.addView(tvNum,mLLayoutParams);

		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		/*mLLayoutParams.leftMargin = tvContentMarginLeft;
		mLLayoutParams.rightMargin = tvContentMarginRight;*/
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
		TextViewUtil.setTextSize(tvContent,tvContentSize);
		TextViewUtil.setTextColor(tvContent,tvContentColor);

		tvNum.setText(String.valueOf(position + 1));
        tvContent.setText(StringUtils.isEmpty(ttsBean.name) ?"" : LanguageConvertor.toLocale(ttsBean.name));

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);
		return itemView;
	}

	@Override
	public void updateItemSelect(int index) {
		LogUtil.logd(WinLayout.logTag+ "train updateItemSelect " + index);
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
