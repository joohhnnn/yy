package com.txznet.comm.ui.theme.test.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.utils.L;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.FlightListViewData;
import com.txznet.comm.ui.viewfactory.data.FlightListViewData.FlightItemBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFlightListView;
import com.txznet.comm.util.TextViewUtil;


public class FlightListView extends IFlightListView {
	private static FlightListView instance = new FlightListView();

	private List<View> mItemViews;

	/*private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;*/
	private int dividerHeight;

	private int countHorMargin;    //内容左右边距
    private int timeVerMargin;    //出发、到达时间上下边距
    private int timeHorMargin;    //出发、到达时间左右边距
    private int tvflightNoSize;    //航班编号字体大小
    private int tvflightNoHeight;    //航班编号行高
    private int ivLogoSize;    //航空公司logo大小
    private int ivLogoHorMargin;    //航空公司logo左右间距
    private int tvflightNameSize;    //航空公司名字字体大小
    private int tvflightNameHeight;    //航空公司名字行高
    private int timeSize;    //出发、到达时间字体大小
    private int timeHeight;    //出发、到达时间行高
    private int timeColor;    //出发、到达时间字体颜色
    private int placeSize;    //出发、到达地点（折扣）字体大小
    private int placeHeight;    //出发、到达地点（折扣）行高
    private int placeColor;    //出发、到达地点（折扣）字体颜色
    private int ivLineWidth;    //出发到达连接线宽度
    private int ivLineHeight;    //出发到达连接线高度
    private int tvPriceSize;    //价格字体大小
    private int tvPriceHeight;    //价格字体行高
    private int tvPriceColor;    //价格字体颜色

    //半屏布局
    int tvflightNoTopMargin;    //航班编号上边距
    int tvflightNoBottomMargin;    //航班编号下边距
    int tvflightNameBottomMargin;    //航空公司名字下边距
    int placeBottomMargin;    //出发、到达地点下边距
    int tvPriceBottomMargin;    //价格下边距

	public static FlightListView getInstance() {
		return instance;
	}

	@Override
	public void init() {
		super.init();
		/*tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);*/
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));

		tvPriceColor = Color.parseColor(LayouUtil.getString("color_flight_price"));
		placeColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
		timeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){
        LogUtil.logd(WinLayout.logTag+ "flight onUpdateParams: "+styleIndex);
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

	//全屏布局参数
	private void initFull(){
		if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            countHorMargin = 3 * unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvflightNoSize = (int) LayouUtil.getDimen("vertical_h5");
            tvflightNoHeight = (int) LayouUtil.getDimen("vertical_h5_height");
            ivLogoSize = (int) LayouUtil.getDimen("vertical_h5");
            ivLogoHorMargin = unit / 2;
            tvflightNameSize =  (int) LayouUtil.getDimen("vertical_h7");
            tvflightNameHeight =  (int) LayouUtil.getDimen("vertical_h7_height");
            timeSize = (int) LayouUtil.getDimen("vertical_h3");
            timeHeight = (int) LayouUtil.getDimen("vertical_h3_height");
            placeSize = (int) LayouUtil.getDimen("vertical_h7");
            placeHeight = (int) LayouUtil.getDimen("vertical_h7_height");
            ivLineWidth = 20 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = (int) LayouUtil.getDimen("vertical_h3");
            tvPriceHeight = (int) LayouUtil.getDimen("vertical_h3_height");
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            countHorMargin = 3 * unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvflightNoSize = (int) LayouUtil.getDimen("h5");
            tvflightNoHeight = (int) LayouUtil.getDimen("h5_height");
            ivLogoSize = (int) LayouUtil.getDimen("h5");
            ivLogoHorMargin = unit / 2;
            tvflightNameSize =  (int) LayouUtil.getDimen("h7");
            tvflightNameHeight =  (int) LayouUtil.getDimen("h7_height");
            timeSize = (int) LayouUtil.getDimen("h3");
            timeHeight = (int) LayouUtil.getDimen("h3_height");
            placeSize = (int) LayouUtil.getDimen("h7");
            placeHeight = (int) LayouUtil.getDimen("h7_height");
            ivLineWidth = 20 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = (int) LayouUtil.getDimen("h3");
            tvPriceHeight = (int) LayouUtil.getDimen("h3_height");
		}
	}

	//半屏布局参数
	private void initHalf(){
        if(WinLayout.isVertScreen){
			int unit = (int) LayouUtil.getDimen("vertical_unit");
			countHorMargin = 3 * unit;
			timeVerMargin = unit;
			timeHorMargin = unit;
			tvflightNoSize = (int) LayouUtil.getDimen("vertical_h5");
			tvflightNoHeight = (int) LayouUtil.getDimen("vertical_h5_height");
			ivLogoSize = (int) LayouUtil.getDimen("vertical_h5");
			ivLogoHorMargin = unit / 2;
			tvflightNameSize =  (int) LayouUtil.getDimen("vertical_h7");
			tvflightNameHeight =  (int) LayouUtil.getDimen("vertical_h7_height");
			timeSize = (int) LayouUtil.getDimen("vertical_h3");
			timeHeight = (int) LayouUtil.getDimen("vertical_h3_height");
			placeSize = (int) LayouUtil.getDimen("vertical_h7");
			placeHeight = (int) LayouUtil.getDimen("vertical_h7_height");
			ivLineWidth = 20 * unit;
			ivLineHeight = 4 * unit;
			tvPriceSize = (int) LayouUtil.getDimen("vertical_h3");
			tvPriceHeight = (int) LayouUtil.getDimen("vertical_h3_height");
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            if (SizeConfig.screenHeight < 464){
                tvflightNoTopMargin = unit ;    //航班编号上边距
                tvflightNameBottomMargin =  unit;    //航空公司名字下边距
                placeBottomMargin = unit;    //出发、到达地点下边距
            }else {
                tvflightNoTopMargin = 4 * unit ;    //航班编号上边距
                tvflightNameBottomMargin = 3 * unit;    //航空公司名字下边距
                placeBottomMargin = 3 * unit;    //出发、到达地点下边距
            }
            countHorMargin = unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvflightNoSize = (int) LayouUtil.getDimen("h3");
            tvflightNoHeight = (int) LayouUtil.getDimen("h3_height");
            ivLogoSize = (int) LayouUtil.getDimen("h5");
            ivLogoHorMargin = unit / 2;
            tvflightNameSize =  (int) LayouUtil.getDimen("h7");
            tvflightNameHeight =  (int) LayouUtil.getDimen("h7_height");
            timeSize = (int) LayouUtil.getDimen("h3");
            timeHeight = (int) LayouUtil.getDimen("h3_height");
            placeSize = (int) LayouUtil.getDimen("h7");
            placeHeight = (int) LayouUtil.getDimen("h7_height");
            ivLineWidth = 4 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = (int) LayouUtil.getDimen("h7");
            tvPriceHeight = (int) LayouUtil.getDimen("h7_height");
            tvflightNoBottomMargin = unit ;    //航班编号下边距
            tvPriceBottomMargin = unit;    //价格下边距
        }

	}

	//无屏布局参数
	private void initNone(){
		if (WinLayout.isVertScreen){
            countHorMargin = (int) LayouUtil.getDimen("x20");
            timeVerMargin = (int) LayouUtil.getDimen("x5");
            timeHorMargin = (int) LayouUtil.getDimen("x10");
            tvflightNoSize = (int) LayouUtil.getDimen("x23");
            tvflightNoHeight = tvflightNoSize + 10;
            ivLogoSize = (int) LayouUtil.getDimen("x23");
            ivLogoHorMargin = (int) LayouUtil.getDimen("x5");
            tvflightNameSize =  (int) LayouUtil.getDimen("x11");
            tvflightNameHeight =  tvflightNameSize + 10;
            timeSize = (int) LayouUtil.getDimen("x18");
            timeHeight = timeSize + 10;
            placeSize = (int) LayouUtil.getDimen("x18");
            placeHeight = placeSize + 10;
            ivLineWidth = (int) LayouUtil.getDimen("x166");
            ivLineHeight = (int) LayouUtil.getDimen("x41");
            tvPriceSize = (int) LayouUtil.getDimen("x26");
            tvPriceHeight = tvPriceSize + 10;
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            countHorMargin = 2 * unit;
            timeVerMargin = unit / 2;
            timeHorMargin = unit;
            tvflightNoSize = (int) LayouUtil.getDimen("h5_none");
            tvflightNoHeight = (int) LayouUtil.getDimen("h5_height_none");
            ivLogoSize = (int) LayouUtil.getDimen("h5_none");
            ivLogoHorMargin = unit / 2;
            tvflightNameSize =  (int) LayouUtil.getDimen("h7_none");
            tvflightNameHeight =  (int) LayouUtil.getDimen("h7_height_none");
            timeSize = (int) LayouUtil.getDimen("h3_none");
            timeHeight = (int) LayouUtil.getDimen("h3_height_none");
            placeSize = (int) LayouUtil.getDimen("h7_none");
            placeHeight = (int) LayouUtil.getDimen("h7_height_none");
            ivLineWidth = 16 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = (int) LayouUtil.getDimen("h3_none");
            tvPriceHeight = (int) LayouUtil.getDimen("h3_height_none");
        }

	}

	@SuppressLint("NewApi")
	@Override
	public ViewAdapter getView(ViewData data) {
		FlightListViewData flightListViewData = (FlightListViewData) data;
		WinLayout.getInstance().vTips = flightListViewData.vTips;
		LogUtil.logd(WinLayout.logTag+ "getView--flightListViewData.vTips:" + flightListViewData.vTips);

		View view = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = createViewFull(flightListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				if (WinLayout.isVertScreen){
                    view = createViewFull(flightListViewData);
                }else {
                    view = createViewHalf(flightListViewData);
                }
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(flightListViewData);
				break;
		}

		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.isListView = true;
		viewAdapter.object = FlightListView.getInstance();
		return viewAdapter;
	}

	private View createViewFull(FlightListViewData flightListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(flightListViewData,"flight","");
		LogUtil.logd(WinLayout.logTag+ "flightListViewData.mTitleInfo.titlefix: " + flightListViewData.mTitleInfo.titlefix);
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageFlightCount * SizeConfig.itemHeightPro);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		mCurPage = flightListViewData.mTitleInfo.curPage;
		mMaxPage = flightListViewData.mTitleInfo.maxPage;
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
		mItemViews = new ArrayList<View>();
		LogUtil.logd(WinLayout.logTag+ "flightListViewData.count:" + flightListViewData.count);
		for (int i = 0; i < flightListViewData.count; i++) {
			//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeightPro);
			View itemView = createItemView(i,flightListViewData.getData().get(i),i != SizeConfig.getInstance().getPageFlightCount() - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (flightListViewData.count < SizeConfig.getInstance().getPageFlightCount()){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0, SizeConfig.getInstance().getPageFlightCount() - flightListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/

		return llLayout;
	}

	private View createViewHalf(FlightListViewData flightListViewData){
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(flightListViewData,"flight","");
		LogUtil.logd(WinLayout.logTag+ "flightListViewData.mTitleInfo.titlefix: " + flightListViewData.mTitleInfo.titlefix);
		LinearLayout.LayoutParams layoutParams  = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageCount * SizeConfig.itemHeight);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setBackground(LayouUtil.getDrawable("white_range_layout_movie"));
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		mCurPage = flightListViewData.mTitleInfo.curPage;
		mMaxPage = flightListViewData.mTitleInfo.maxPage;
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
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < flightListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
			if (i != flightListViewData.count-1){
				layoutParams.rightMargin = (int) LayouUtil.getDimen("x15");
			}
			View itemView = createItemViewHalf(i,flightListViewData.getData().get(i),i != flightListViewData.count - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (flightListViewData.count < SizeConfig.getInstance().getPageFlightCount()){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.getInstance().getPageFlightCount() - flightListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/
		int blankCount = SizeConfig.getInstance().getPageFlightCount() - flightListViewData.count;
		if (blankCount > 0){
            /*for (int i = 0; i < blankCount; i++) {
                layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
                if (i != flightListViewData.count-1){
                    layoutParams.rightMargin = (int) LayouUtil.getDimen("x15");
                }
                View itemView = createItemViewHalfBlank();
                llContent.addView(itemView, layoutParams);
                mItemViews.add(itemView);
            }*/
            View view = new View(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,blankCount);
            llContent.addView(view, layoutParams);
		}

		return llLayout;
	}

	private View createViewNone(FlightListViewData flightListViewData){
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llLayout.addView(llContents,layoutParams);

		mCurPage = flightListViewData.mTitleInfo.curPage;
		mMaxPage = flightListViewData.mTitleInfo.maxPage;
		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llPager,layoutParams);

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(flightListViewData,"flight","");
        LogUtil.logd(WinLayout.logTag+ "flightListViewData.mTitleInfo.titlefix: " + flightListViewData.mTitleInfo.titlefix);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContents.addView(titleViewAdapter.view,layoutParams);

		View divider = new View(GlobalContext.get());
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		llContents.addView(divider, layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageFlightCount * SizeConfig.itemHeightPro);
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
		mItemViews = new ArrayList<View>();
		LogUtil.logd(WinLayout.logTag+ "flightListViewData.count:" + flightListViewData.count);
		for (int i = 0; i < flightListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeightPro);
			View itemView = createItemViewNone(i,flightListViewData.getData().get(i),i != SizeConfig.getInstance().getPageFlightCount() - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		return llLayout;
	}

	/**
	 * 测试航空公司logo用
	 */
	TextView tvAirline;
	private void showLogo(String text){
		LogUtil.logd(WinLayout.logTag+ "showLogo: "+text);
		tvAirline.setText(text);
		Drawable drawable = getAirIcon(text);
		tvAirline.setCompoundDrawablesWithIntrinsicBounds(drawable,null,null,null);
	}

	@SuppressLint("NewApi")
	private View createItemView(int position, FlightItemBean flightBean,boolean showDivider){
		LogUtil.logd(WinLayout.logTag+ "FlightItemBean: "+flightBean.departTime+"--"+flightBean.departTimestamp+"--"+flightBean.arrivalTime+"--"+flightBean.arrivalTimestamp+"--"+flightBean.addDate);
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
		//itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = countHorMargin;
		itemView.addView(flContent,layoutParams);
		
		LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.VERTICAL);
		FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llItem, mLayoutParams);
		//rlItem.setMinimumHeight((int) LayouUtil.getDimen("m80"));

        LinearLayout llTop = new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.bottomMargin = timeVerMargin;
        llItem.addView(llTop, llLayoutParams);

		TextView tvflightNo = new TextView(GlobalContext.get());
        tvflightNo.setGravity(Gravity.CENTER_VERTICAL);
		tvflightNo.setSingleLine();
		tvflightNo.setEllipsize(TruncateAt.END);
		/*RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.topMargin = (int)((int)GlobalContext.get().getResources().getDisplayMetrics().density*3+0.5f);
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m20");*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvflightNoHeight);
        llLayoutParams.rightMargin = timeHorMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvflightNo, llLayoutParams);
		
		tvAirline = new TextView(GlobalContext.get());
		tvAirline.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));
		tvAirline.setSingleLine();
		tvAirline.setEllipsize(TruncateAt.END);
		tvAirline.setGravity(Gravity.CENTER_VERTICAL);
		/*mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m8");
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m4");
		mRLayoutParams.addRule(RelativeLayout.RIGHT_OF,tvflightNo.getId());
		rlItem.addView(tvAirline, mRLayoutParams);*/
        //llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvflightNameHeight);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvAirline, llLayoutParams);
		
		/*RelativeLayout rlRoute = new RelativeLayout(GlobalContext.get());
		rlRoute.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		//mRLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		//mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m19");
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m20");
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("y15");
		mRLayoutParams.addRule(RelativeLayout.BELOW,tvflightNo.getId());
		rlItem.addView(rlRoute, mRLayoutParams);*/

		LinearLayout llRoute = new LinearLayout(GlobalContext.get());
        llRoute.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llItem.addView(llRoute, llLayoutParams);

        LinearLayout llDepart = new LinearLayout(GlobalContext.get());
        llDepart.setOrientation(LinearLayout.VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(llDepart, llLayoutParams);

		TextView tvDepartTime = new TextView(GlobalContext.get());
		tvDepartTime.setGravity(Gravity.CENTER_VERTICAL);
		tvDepartTime.setSingleLine();
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,timeHeight);
        llLayoutParams.bottomMargin = timeVerMargin;
        llDepart.addView(tvDepartTime, llLayoutParams);
		
		TextView tvDepartName = new TextView(GlobalContext.get());
		tvDepartName.setSingleLine();
		tvDepartName.setEllipsize(TruncateAt.END);
		tvDepartName.setGravity(Gravity.CENTER_VERTICAL);
		/*RelativeLayout.LayoutParams mLLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.BELOW,tvDepartTime.getId());
		rlRoute.addView(tvDepartName, mLLayoutParams);*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        llDepart.addView(tvDepartName, llLayoutParams);
		
		ImageView view = new ImageView(GlobalContext.get());
		view.setImageDrawable(LayouUtil.getDrawable("airlines_to2"));
		/*mLLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m210"), (int) LayouUtil.getDimen("m35"));
		mLLayoutParams.leftMargin = (int) LayouUtil.getDimen("x10");
		mLLayoutParams.rightMargin = (int) LayouUtil.getDimen("x10");
		mLLayoutParams.addRule(RelativeLayout.RIGHT_OF, tvDepartTime.getId());
		mLLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlRoute.addView(view, mLLayoutParams);*/;
        llLayoutParams = new LinearLayout.LayoutParams(ivLineWidth,ivLineHeight);
        llLayoutParams.leftMargin = timeVerMargin;
        llLayoutParams.rightMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llRoute.addView(view, llLayoutParams);
		
		/*TextView tvDateAdd = new TextView(GlobalContext.get());
		tvDateAdd.setTextColor(Color.GRAY);
		tvDateAdd.setTextSize(LayouUtil.getDimen("m12"));
		tvDateAdd.setGravity(Gravity.CENTER_HORIZONTAL);
		tvDateAdd.setSingleLine();
		tvDateAdd.setEllipsize(TruncateAt.END);
		tvDateAdd.setId(ViewUtils.generateViewId());
		mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.ABOVE, view.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, view.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, view.getId());
		rlRoute.addView(tvDateAdd, mLLayoutParams);*/

		LinearLayout lArrival = new LinearLayout(GlobalContext.get());
		lArrival.setOrientation(LinearLayout.VERTICAL);
		lArrival.setGravity(Gravity.RIGHT);
		/*mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.RIGHT_OF, view.getId());
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m4");
		rlRoute.addView(lArrival, mLLayoutParams);*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lArrival, llLayoutParams);

		TextView tvArrivalTime = new TextView(GlobalContext.get());
		tvArrivalTime.setSingleLine();
		tvArrivalTime.setEllipsize(TruncateAt.END);
		tvArrivalTime.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,timeHeight);
        lLayoutParams.bottomMargin = timeVerMargin;
		lArrival.addView(tvArrivalTime, lLayoutParams);

		TextView tvArrivalName = new TextView(GlobalContext.get());
		tvArrivalName.setSingleLine();
		tvArrivalName.setEllipsize(TruncateAt.END);
		tvArrivalName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
		lArrival.addView(tvArrivalName, lLayoutParams);
		
		/*TextView tvCount = new TextView(GlobalContext.get());
		tvCount.setTextColor(Color.GRAY);
		tvCount.setTextSize(LayouUtil.getDimen("m14"));
		tvCount.setSingleLine();
		tvCount.setEllipsize(TruncateAt.END);
		tvCount.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m19");
		mRLayoutParams.rightMargin = (int) LayouUtil.getDimen("m40");
		rlItem.addView(tvCount, mRLayoutParams);*/

		LinearLayout lPrice = new LinearLayout(GlobalContext.get());
		lPrice.setOrientation(LinearLayout.VERTICAL);
		lPrice.setGravity(Gravity.CENTER_HORIZONTAL);
		/*mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mRLayoutParams.addRule(RelativeLayout.BELOW,tvflightNo.getId());
		mRLayoutParams.rightMargin = (int) LayouUtil.getDimen("m20");
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("y15");
		rlItem.addView(lPrice, mRLayoutParams);*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lPrice, llLayoutParams);

		TextView tvPrice = new TextView(GlobalContext.get());
		tvPrice.setSingleLine();
		tvPrice.setEllipsize(TruncateAt.END);
		tvPrice.setGravity(Gravity.CENTER_VERTICAL);
		/*lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		lPrice.addView(tvPrice, lLayoutParams);*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPriceHeight);
        llLayoutParams.bottomMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvPrice, llLayoutParams);
	
		TextView tvDiscount = new TextView(GlobalContext.get());
		tvDiscount.setSingleLine();
		tvDiscount.setEllipsize(TruncateAt.END);
		tvDiscount.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvDiscount, llLayoutParams);

		/*TextView tvSeat = new TextView(GlobalContext.get());
		tvSeat.setTextColor(Color.GRAY);
		tvSeat.setTextSize(LayouUtil.getDimen("m14"));
		tvSeat.setSingleLine();
		tvSeat.setEllipsize(TruncateAt.END);
		tvSeat.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.ALIGN_TOP, tvCount.getId());
		mRLayoutParams.addRule(RelativeLayout.LEFT_OF, tvCount.getId());
		mRLayoutParams.rightMargin = (int) LayouUtil.getDimen("m14");
		rlItem.addView(tvSeat, mRLayoutParams);*/
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);

		tvAirline.setText(" "+flightBean.airline);
        Drawable drawable = getAirIcon(flightBean.airline);
		//drawable.setBounds(0,0,(int)LayouUtil.getDimen("m15"),(int)LayouUtil.getDimen("m15"));
		drawable.setBounds(0,0,ivLogoSize,ivLogoSize);
        tvAirline.setCompoundDrawables(drawable,null,null,null);
		tvflightNo.setText(flightBean.flightNo);
		if(!TextUtils.isEmpty(flightBean.departAirportName)){
			tvDepartName.setText(flightBean.departAirportName+"机场");
		}else{
			tvDepartName.setText("未知名称");
		}
		if(!TextUtils.isEmpty(flightBean.arrivalAirportName)){
			tvArrivalName.setText(flightBean.arrivalAirportName+"机场");
		}else{
			tvArrivalName.setText("未知名称");
		}
		tvDepartTime.setText(flightBean.departTimeHm);
		tvArrivalTime.setText(flightBean.arrivalTimeHm);
		//tvCount.setText(flightBean.ticketCount + " 张");
		tvDiscount.setText(flightBean.economyCabinDiscount + "折");
		//tvSeat.setText("经济舱");
		String price = "¥" + flightBean.economyCabinPrice + "起";
		SpannableString priceString = new SpannableString(price);
		priceString.setSpan(new RelativeSizeSpan(0.8f),0  , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		priceString.setSpan(new RelativeSizeSpan(0.8f),price.length()-1  , price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tvPrice.setText(priceString);
		divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
		//tvDateAdd.setText(flightBean.addDate);
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

		tvDiscount.setTextColor(placeColor);
		tvDiscount.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
		tvPrice.setTextColor(tvPriceColor);
		tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvPriceSize);
		tvArrivalName.setTextColor(placeColor);
		tvArrivalName.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
		tvArrivalTime.setTextColor(timeColor);
		tvArrivalTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,timeSize);
		//tvDateAdd.setTextColor(Color.GRAY);
		//tvDateAdd.setTextSize(TypedValue.COMPLEX_UNIT_PX,LayouUtil.getDimen("m14"));
		tvDepartName.setTextColor(placeColor);
		tvDepartName.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
		tvDepartTime.setTextColor(timeColor);
		tvDepartTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,timeSize);
		tvAirline.setTextColor(placeColor);
		tvAirline.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvflightNameSize);
		tvflightNo.setTextColor(placeColor);
		tvflightNo.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvflightNoSize);
		
		return itemView;
	}

	@SuppressLint("NewApi")
	private View createItemViewHalf(int position, FlightItemBean flightBean,boolean showDivider){

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
		itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		flContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(flContent,layoutParams);

		/*RelativeLayout rlItem = new RelativeLayout(GlobalContext.get());
		FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(rlItem, mLayoutParams);
		rlItem.setMinimumHeight((int) LayouUtil.getDimen("m80"));*/
		LinearLayout lItem = new LinearLayout(GlobalContext.get());
		lItem.setOrientation(LinearLayout.VERTICAL);
		FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mLayoutParams.leftMargin = countHorMargin;
        mLayoutParams.rightMargin = countHorMargin;
		flContent.addView(lItem, mLayoutParams);

		TextView tvflightNo = new TextView(GlobalContext.get());
        tvflightNo.setGravity(Gravity.CENTER_VERTICAL);
		tvflightNo.setSingleLine();
		tvflightNo.setEllipsize(TruncateAt.END);
        LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvflightNoHeight);
        lLayoutParams.topMargin = tvflightNoTopMargin;
        lLayoutParams.bottomMargin = tvflightNoBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvflightNo, lLayoutParams);

		TextView tvAirline = new TextView(GlobalContext.get());
		tvAirline.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));
		tvAirline.setSingleLine();
		tvAirline.setEllipsize(TruncateAt.END);
		tvAirline.setGravity(Gravity.CENTER_VERTICAL);
		lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lLayoutParams.bottomMargin = tvflightNameBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvAirline, lLayoutParams);

		RelativeLayout rlRoute = new RelativeLayout(GlobalContext.get());
		rlRoute.setId(ViewUtils.generateViewId());
		lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lLayoutParams.bottomMargin = placeBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
		lItem.addView(rlRoute,lLayoutParams);

		TextView tvDepartTime = new TextView(GlobalContext.get());
        tvDepartTime.setGravity(Gravity.CENTER_VERTICAL);
		tvDepartTime.setSingleLine();
		tvDepartTime.setId(ViewUtils.generateViewId());
		RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, timeHeight);
		rlRoute.addView(tvDepartTime, mRLayoutParams);

		TextView tvDepartName = new TextView(GlobalContext.get());
		tvDepartName.setSingleLine();
		tvDepartName.setEllipsize(TruncateAt.END);
		tvDepartName.setId(ViewUtils.generateViewId());
		tvDepartName.setGravity(Gravity.CENTER_VERTICAL);
		RelativeLayout.LayoutParams mLLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, placeHeight);
		mLLayoutParams.addRule(RelativeLayout.BELOW,tvDepartTime.getId());
		rlRoute.addView(tvDepartName, mLLayoutParams);

		ImageView view = new ImageView(GlobalContext.get());
		view.setId(ViewUtils.generateViewId());
		view.setImageDrawable(LayouUtil.getDrawable("airlines_to0"));
		mLLayoutParams = new RelativeLayout.LayoutParams(ivLineWidth, ivLineHeight);
		mLLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		rlRoute.addView(view, mLLayoutParams);

		/*TextView tvDateAdd = new TextView(GlobalContext.get());
		tvDateAdd.setTextColor(Color.GRAY);
		tvDateAdd.setTextSize(LayouUtil.getDimen("m12"));
		tvDateAdd.setGravity(Gravity.CENTER_HORIZONTAL);
		tvDateAdd.setSingleLine();
		tvDateAdd.setEllipsize(TruncateAt.END);
		tvDateAdd.setId(ViewUtils.generateViewId());
		mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.ABOVE, view.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, view.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, view.getId());
		rlRoute.addView(tvDateAdd, mLLayoutParams);*/

		/*LinearLayout lArrival = new LinearLayout(GlobalContext.get());
		lArrival.setOrientation(LinearLayout.VERTICAL);
		lArrival.setGravity(Gravity.RIGHT);
		mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.RIGHT_OF, view.getId());
		//mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m4");
		rlRoute.addView(lArrival, mLLayoutParams);*/

		TextView tvArrivalTime = new TextView(GlobalContext.get());
		tvArrivalTime.setSingleLine();
		tvArrivalTime.setEllipsize(TruncateAt.END);
		tvArrivalTime.setId(ViewUtils.generateViewId());
		tvArrivalTime.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, timeHeight);
        mLLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlRoute.addView(tvArrivalTime, mLLayoutParams);

		TextView tvArrivalName = new TextView(GlobalContext.get());
		tvArrivalName.setSingleLine();
		tvArrivalName.setEllipsize(TruncateAt.END);
		tvArrivalName.setId(ViewUtils.generateViewId());
		tvArrivalName.setGravity(Gravity.CENTER_VERTICAL);
        mLLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, placeHeight);
        mLLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mLLayoutParams.addRule(RelativeLayout.BELOW,tvArrivalTime.getId());
        rlRoute.addView(tvArrivalName, mLLayoutParams);

		/*LinearLayout lPrice = new LinearLayout(GlobalContext.get());
		lPrice.setOrientation(LinearLayout.VERTICAL);
		lPrice.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,3);
		lItem.addView(lPrice,lLayoutParams);*/

		TextView tvPrice = new TextView(GlobalContext.get());
		tvPrice.setSingleLine();
		tvPrice.setEllipsize(TruncateAt.END);
		tvPrice.setGravity(Gravity.CENTER_VERTICAL);
		lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPriceHeight);
        lLayoutParams.bottomMargin = tvPriceBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvPrice, lLayoutParams);

		TextView tvDiscount = new TextView(GlobalContext.get());
		tvDiscount.setSingleLine();
		tvDiscount.setEllipsize(TruncateAt.END);
		tvDiscount.setGravity(Gravity.CENTER_VERTICAL);
		lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvDiscount, lLayoutParams);

		tvAirline.setText(" "+flightBean.airline);
        Drawable drawable = getAirIcon(flightBean.airline);
		drawable.setBounds(0,0,(int)LayouUtil.getDimen("m15"),(int)LayouUtil.getDimen("m15"));
		tvAirline.setCompoundDrawables(drawable,null,null,null);
		tvflightNo.setText(flightBean.flightNo);
		if(!TextUtils.isEmpty(flightBean.departAirportName)){
			tvDepartName.setText(flightBean.departAirportName+"机场");
		}else{
			tvDepartName.setText("未知名称");
		}
		if(!TextUtils.isEmpty(flightBean.arrivalAirportName)){
			tvArrivalName.setText(flightBean.arrivalAirportName+"机场");
		}else{
			tvArrivalName.setText("未知名称");
		}
		tvDepartTime.setText(flightBean.departTimeHm);
		tvArrivalTime.setText(flightBean.arrivalTimeHm);
		//tvCount.setText(flightBean.ticketCount + " 张");
		tvDiscount.setText(flightBean.economyCabinDiscount + "折");
		//tvSeat.setText("经济舱");
		String price = "¥" + flightBean.economyCabinPrice + "起";
		SpannableString priceString = new SpannableString(price);
		priceString.setSpan(new RelativeSizeSpan(0.8f),0  , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		priceString.setSpan(new RelativeSizeSpan(0.8f),price.length()-1  , price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		tvPrice.setText(priceString);
		//divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
		//tvDateAdd.setText(flightBean.addDate);

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

		tvDiscount.setTextColor(timeColor);
		tvDiscount.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
		tvPrice.setTextColor(tvPriceColor);
		tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvPriceSize);
		tvArrivalName.setTextColor(placeColor);
		tvArrivalName.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
		tvArrivalTime.setTextColor(timeColor);
		tvArrivalTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,timeSize);
		//tvDateAdd.setTextColor(Color.GRAY);
		//tvDateAdd.setTextSize(TypedValue.COMPLEX_UNIT_PX,LayouUtil.getDimen("m14"));
		tvDepartName.setTextColor(placeColor);
		tvDepartName.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
		tvDepartTime.setTextColor(timeColor);
		tvDepartTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,timeSize);
		tvAirline.setTextColor(placeColor);
		tvAirline.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvflightNameSize);
		tvflightNo.setTextColor(timeColor);
		tvflightNo.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvflightNoSize);

		return itemView;
	}

    private View createItemViewHalfBlank(){

        View itemView = new View(GlobalContext.get());
        itemView.setBackground(LayouUtil.getDrawable("white_range_layout"));

        return itemView;
    }

    @SuppressLint("NewApi")
    private View createItemViewNone(int position, FlightItemBean flightBean,boolean showDivider){
        LogUtil.logd(WinLayout.logTag+ "FlightItemBean: "+flightBean.departTime+"--"+flightBean.departTimestamp+"--"+flightBean.arrivalTime+"--"+flightBean.arrivalTimestamp+"--"+flightBean.addDate+"--"+
                flightBean.arrivalAirportName+"--"+flightBean.departAirportName);
        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
		ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        //itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));
        FrameLayout flContent = new FrameLayout(GlobalContext.get());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = countHorMargin;
        itemView.addView(flContent,layoutParams);

        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flContent.addView(llItem, mLayoutParams);
        //rlItem.setMinimumHeight((int) LayouUtil.getDimen("m80"));

        LinearLayout llTop = new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.bottomMargin = timeVerMargin;
        llItem.addView(llTop, llLayoutParams);

        TextView tvflightNo = new TextView(GlobalContext.get());
        tvflightNo.setGravity(Gravity.CENTER_VERTICAL);
        tvflightNo.setSingleLine();
        tvflightNo.setEllipsize(TruncateAt.END);
		/*RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.topMargin = (int)((int)GlobalContext.get().getResources().getDisplayMetrics().density*3+0.5f);
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m20");*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvflightNoHeight);
        llLayoutParams.rightMargin = timeHorMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvflightNo, llLayoutParams);

        tvAirline = new TextView(GlobalContext.get());
        tvAirline.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));
        tvAirline.setSingleLine();
        tvAirline.setEllipsize(TruncateAt.END);
        tvAirline.setGravity(Gravity.CENTER_VERTICAL);
		/*mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m8");
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m4");
		mRLayoutParams.addRule(RelativeLayout.RIGHT_OF,tvflightNo.getId());
		rlItem.addView(tvAirline, mRLayoutParams);*/
        //llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvflightNameHeight);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvAirline, llLayoutParams);

		/*RelativeLayout rlRoute = new RelativeLayout(GlobalContext.get());
		rlRoute.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		//mRLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		//mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m19");
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m20");
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("y15");
		mRLayoutParams.addRule(RelativeLayout.BELOW,tvflightNo.getId());
		rlItem.addView(rlRoute, mRLayoutParams);*/

        LinearLayout llRoute = new LinearLayout(GlobalContext.get());
        llRoute.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llItem.addView(llRoute, llLayoutParams);

        LinearLayout llDepart = new LinearLayout(GlobalContext.get());
        llDepart.setOrientation(LinearLayout.VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(llDepart, llLayoutParams);

        TextView tvDepartTime = new TextView(GlobalContext.get());
        tvDepartTime.setGravity(Gravity.CENTER_VERTICAL);
        tvDepartTime.setSingleLine();
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,timeHeight);
        llLayoutParams.bottomMargin = timeVerMargin;
        llDepart.addView(tvDepartTime, llLayoutParams);

        TextView tvDepartName = new TextView(GlobalContext.get());
        tvDepartName.setSingleLine();
        tvDepartName.setEllipsize(TruncateAt.END);
        tvDepartName.setGravity(Gravity.CENTER_VERTICAL);
		/*RelativeLayout.LayoutParams mLLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.BELOW,tvDepartTime.getId());
		rlRoute.addView(tvDepartName, mLLayoutParams);*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        llDepart.addView(tvDepartName, llLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setImageDrawable(LayouUtil.getDrawable("airlines_to2"));
		/*mLLayoutParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m210"), (int) LayouUtil.getDimen("m35"));
		mLLayoutParams.leftMargin = (int) LayouUtil.getDimen("x10");
		mLLayoutParams.rightMargin = (int) LayouUtil.getDimen("x10");
		mLLayoutParams.addRule(RelativeLayout.RIGHT_OF, tvDepartTime.getId());
		mLLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlRoute.addView(view, mLLayoutParams);*/;
        llLayoutParams = new LinearLayout.LayoutParams(ivLineWidth,ivLineHeight);
        llLayoutParams.leftMargin = timeVerMargin;
        llLayoutParams.rightMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llRoute.addView(view, llLayoutParams);

		/*TextView tvDateAdd = new TextView(GlobalContext.get());
		tvDateAdd.setTextColor(Color.GRAY);
		tvDateAdd.setTextSize(LayouUtil.getDimen("m12"));
		tvDateAdd.setGravity(Gravity.CENTER_HORIZONTAL);
		tvDateAdd.setSingleLine();
		tvDateAdd.setEllipsize(TruncateAt.END);
		tvDateAdd.setId(ViewUtils.generateViewId());
		mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.ABOVE, view.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, view.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, view.getId());
		rlRoute.addView(tvDateAdd, mLLayoutParams);*/

        LinearLayout lArrival = new LinearLayout(GlobalContext.get());
        lArrival.setOrientation(LinearLayout.VERTICAL);
        lArrival.setGravity(Gravity.RIGHT);
		/*mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.RIGHT_OF, view.getId());
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m4");
		rlRoute.addView(lArrival, mLLayoutParams);*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lArrival, llLayoutParams);

        TextView tvArrivalTime = new TextView(GlobalContext.get());
        tvArrivalTime.setSingleLine();
        tvArrivalTime.setEllipsize(TruncateAt.END);
        tvArrivalTime.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,timeHeight);
        lLayoutParams.bottomMargin = timeVerMargin;
        lArrival.addView(tvArrivalTime, lLayoutParams);

        TextView tvArrivalName = new TextView(GlobalContext.get());
        tvArrivalName.setSingleLine();
        tvArrivalName.setEllipsize(TruncateAt.END);
        tvArrivalName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        lArrival.addView(tvArrivalName, lLayoutParams);

		/*TextView tvCount = new TextView(GlobalContext.get());
		tvCount.setTextColor(Color.GRAY);
		tvCount.setTextSize(LayouUtil.getDimen("m14"));
		tvCount.setSingleLine();
		tvCount.setEllipsize(TruncateAt.END);
		tvCount.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m19");
		mRLayoutParams.rightMargin = (int) LayouUtil.getDimen("m40");
		rlItem.addView(tvCount, mRLayoutParams);*/

        LinearLayout lPrice = new LinearLayout(GlobalContext.get());
        lPrice.setOrientation(LinearLayout.VERTICAL);
        lPrice.setGravity(Gravity.CENTER_HORIZONTAL);
		/*mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mRLayoutParams.addRule(RelativeLayout.BELOW,tvflightNo.getId());
		mRLayoutParams.rightMargin = (int) LayouUtil.getDimen("m20");
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("y15");
		rlItem.addView(lPrice, mRLayoutParams);*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lPrice, llLayoutParams);

        TextView tvPrice = new TextView(GlobalContext.get());
        tvPrice.setSingleLine();
        tvPrice.setEllipsize(TruncateAt.END);
        tvPrice.setGravity(Gravity.CENTER_VERTICAL);
		/*lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		lPrice.addView(tvPrice, lLayoutParams);*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPriceHeight);
        llLayoutParams.bottomMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvPrice, llLayoutParams);

        TextView tvDiscount = new TextView(GlobalContext.get());
        tvDiscount.setSingleLine();
        tvDiscount.setEllipsize(TruncateAt.END);
        tvDiscount.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvDiscount, llLayoutParams);

		/*TextView tvSeat = new TextView(GlobalContext.get());
		tvSeat.setTextColor(Color.GRAY);
		tvSeat.setTextSize(LayouUtil.getDimen("m14"));
		tvSeat.setSingleLine();
		tvSeat.setEllipsize(TruncateAt.END);
		tvSeat.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.ALIGN_TOP, tvCount.getId());
		mRLayoutParams.addRule(RelativeLayout.LEFT_OF, tvCount.getId());
		mRLayoutParams.rightMargin = (int) LayouUtil.getDimen("m14");
		rlItem.addView(tvSeat, mRLayoutParams);*/

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        tvAirline.setText(" "+flightBean.airline);
        Drawable drawable = getAirIcon(flightBean.airline);
        //drawable.setBounds(0,0,(int)LayouUtil.getDimen("m15"),(int)LayouUtil.getDimen("m15"));
        drawable.setBounds(0,0,ivLogoSize,ivLogoSize);
        tvAirline.setCompoundDrawables(drawable,null,null,null);
        tvflightNo.setText(flightBean.flightNo);
        if(!TextUtils.isEmpty(flightBean.departAirportName)){
            tvDepartName.setText(flightBean.departAirportName+"机场");
        }else{
            tvDepartName.setText("未知名称");
        }
        if(!TextUtils.isEmpty(flightBean.arrivalAirportName)){
            tvArrivalName.setText(flightBean.arrivalAirportName+"机场");
        }else{
            tvArrivalName.setText("未知名称");
        }
        tvDepartTime.setText(flightBean.departTimeHm);
        tvArrivalTime.setText(flightBean.arrivalTimeHm);
        //tvCount.setText(flightBean.ticketCount + " 张");
        tvDiscount.setText(flightBean.economyCabinDiscount + "折");
        //tvSeat.setText("经济舱");
        String price = "¥" + flightBean.economyCabinPrice + "起";
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f),0  , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceString.setSpan(new RelativeSizeSpan(0.8f),price.length()-1  , price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
        //tvDateAdd.setText(flightBean.addDate);
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

        tvDiscount.setTextColor(placeColor);
        tvDiscount.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
        tvPrice.setTextColor(tvPriceColor);
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvPriceSize);
        tvArrivalName.setTextColor(placeColor);
        tvArrivalName.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
        tvArrivalTime.setTextColor(timeColor);
        tvArrivalTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,timeSize);
        //tvDateAdd.setTextColor(Color.GRAY);
        //tvDateAdd.setTextSize(TypedValue.COMPLEX_UNIT_PX,LayouUtil.getDimen("m14"));
        tvDepartName.setTextColor(placeColor);
        tvDepartName.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
        tvDepartTime.setTextColor(timeColor);
        tvDepartTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,timeSize);
        tvAirline.setTextColor(placeColor);
        tvAirline.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvflightNameSize);
        tvflightNo.setTextColor(placeColor);
        tvflightNo.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvflightNoSize);

        return itemView;
    }

	//根据航空公司名称获得图标
	private Drawable getAirIcon(String airName){
		Drawable drawable = null;
		switch(airName){
			case "奥凯航空":
				drawable = LayouUtil.getDrawable("air_aokai");
				break;
			case "澳门航空":
				drawable = LayouUtil.getDrawable("air_aomen");
				break;
			case "成都航空":
				drawable = LayouUtil.getDrawable("air_chengdu");
				break;
			case "春秋航空":
				drawable = LayouUtil.getDrawable("air_chunqiu");
				break;
			case "大连航空":
				drawable = LayouUtil.getDrawable("air_dalian");
				break;
			case "大新华航空":
				drawable = LayouUtil.getDrawable("air_daxinhua");
				break;
			case "东北航空":
				drawable = LayouUtil.getDrawable("air_dongbei");
				break;
			case "东方航空":
				drawable = LayouUtil.getDrawable("air_dongfang");
				break;
			case "东海航空":
				drawable = LayouUtil.getDrawable("air_donghai");
				break;
			case "多彩贵州航空":
				drawable = LayouUtil.getDrawable("air_duocaiguizhou");
				break;
			case "非凡航空":
				drawable = LayouUtil.getDrawable("air_feifan");
				break;
			case "福州航空":
				drawable = LayouUtil.getDrawable("air_fuzhou");
				break;
			case "桂林航空":
				drawable = LayouUtil.getDrawable("air_guilin");
				break;
			case "国泰航空":
				drawable = LayouUtil.getDrawable("air_guotai");
				break;
			case "海南航空":
				drawable = LayouUtil.getDrawable("air_hainan");
				break;
			case "河北航空":
				drawable = LayouUtil.getDrawable("air_hebei");
				break;
			case "华夏航空":
				drawable = LayouUtil.getDrawable("air_huaxia");
				break;
			case "华信航空":
				drawable = LayouUtil.getDrawable("air_huaxin");
				break;
			case "吉祥航空":
				drawable = LayouUtil.getDrawable("air_jixiang");
				break;
			case "江西航空":
				drawable = LayouUtil.getDrawable("air_jiangxi");
				break;
			case "金鹏航空":
				drawable = LayouUtil.getDrawable("air_jinpeng");
				break;
			case "九元航空":
				drawable = LayouUtil.getDrawable("air_jiuyuan");
				break;
			case "昆明航空":
				drawable = LayouUtil.getDrawable("air_kunming");
				break;
			case "鲲鹏航空":
				drawable = LayouUtil.getDrawable("air_kunpeng");
				break;
			case "立荣航空":
				drawable = LayouUtil.getDrawable("air_lirong");
				break;
			case "龙浩航空":
				drawable = LayouUtil.getDrawable("air_longhao");
				break;
			case "龙江航空":
				drawable = LayouUtil.getDrawable("air_longjiang");
				break;
			case "南方航空":
				drawable = LayouUtil.getDrawable("air_nanfang");
				break;
			case "青岛航空":
				drawable = LayouUtil.getDrawable("air_qingdao");
				break;
			case "瑞丽航空":
				drawable = LayouUtil.getDrawable("air_ruili");
				break;
			case "厦门航空":
				drawable = LayouUtil.getDrawable("air_xiamen");
				break;
			case "山东航空":
				drawable = LayouUtil.getDrawable("air_shandong");
				break;
			case "上海航空":
				drawable = LayouUtil.getDrawable("air_shanghai");
				break;
			case "深圳航空":
				drawable = LayouUtil.getDrawable("air_shenzhen");
				break;
			case "首都航空":
				drawable = LayouUtil.getDrawable("air_shoudu");
				break;
			case "四川航空":
				drawable = LayouUtil.getDrawable("air_sichuan");
				break;
			case "天津航空":
				drawable = LayouUtil.getDrawable("air_tianjing");
				break;
			case "乌鲁木齐航空":
				drawable = LayouUtil.getDrawable("air_wulumuqi");
				break;
			case "西部航空":
				drawable = LayouUtil.getDrawable("air_xibu");
				break;
			case "西藏航空":
				drawable = LayouUtil.getDrawable("air_xizang");
				break;
			case "香港航空":
				drawable = LayouUtil.getDrawable("air_hongkong");
				break;
			case "远东航空":
				drawable = LayouUtil.getDrawable("air_yuandong");
				break;
			case "云南红土航空":
				drawable = LayouUtil.getDrawable("air_yunnanhongtu");
				break;
			case "云南祥鹏航空":
				drawable = LayouUtil.getDrawable("air_yunnanxiangpeng");
				break;
			case "长龙航空":
				drawable = LayouUtil.getDrawable("air_changlong");
				break;
			case "中国国际航空":
			case "中国国航":
				drawable = LayouUtil.getDrawable("air_zhongguoguoji");
				break;
			case "中国联合航空":
				drawable = LayouUtil.getDrawable("air_zhongguolianhe");
				break;
			case "中华航空":
				drawable = LayouUtil.getDrawable("air_zhonghua");
				break;
			case "重庆航空":
				drawable = LayouUtil.getDrawable("air_chongqing");
				break;
			default:
				drawable = LayouUtil.getDrawable("air_default");
				break;
		}

		LogUtil.logd(WinLayout.logTag+ "getAirIcon: "+ airName);
		if (drawable != null){
			LogUtil.logd(WinLayout.logTag+ "getAirIcon: get it");
		}
		return drawable;
	}

	@Override
	public void updateProgress(int progress, int selection) {

	}

	@Override
	public void snapPage(boolean next) {

	}

	@Override
	public void updateItemSelect(int index) {
		LogUtil.logd(WinLayout.logTag+ "flight updateItemSelect " + index);

		//mItemViews.get(index).getBackground().setAlpha(127);
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
