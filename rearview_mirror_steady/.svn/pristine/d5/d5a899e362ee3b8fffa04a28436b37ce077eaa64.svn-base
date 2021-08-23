package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.FlightListViewData;
import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFlightTicketList;
import com.txznet.comm.util.TextViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daviddai on 2019/9/7
 */
public class FlightTicketListView extends IFlightTicketList {
    private static FlightTicketListView instance = new FlightTicketListView();

    private List<View> mItemViews;


    private int tvNumSide;    //序号宽高
    private int tvNumHorMargin;    //序号左右边距
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
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
    private int tvflightNoTopMargin;    //航班编号上边距
    private int tvflightNoBottomMargin;    //航班编号下边距
    private int tvflightNameBottomMargin;    //航空公司名字下边距
    private int placeBottomMargin;    //出发、到达地点下边距
    private int tvPriceBottomMargin;    //价格下边距
    private int seatSize;    //座位字体大小
    private int seatHeight;    //座位行高

    public static FlightTicketListView getInstance() {
        return instance;
    }

    @Override
    public void init() {
        super.init();
        dividerHeight = 1;

        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvPriceColor = Color.parseColor(LayouUtil.getString("color_flight_price"));
        placeColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        timeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        LogUtil.logd(WinLayout.logTag + "FlightTicket onUpdateParams: " + styleIndex);

        // XXX: 2019/9/9 这里是不是抛运行时异常会比较好？
        // 齐悟飞机票的UI只做全屏的，由core里面的代码控制当不是全屏的时候不进入这里。
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
    private void initFull() {
        int unit = ViewParamsUtil.unit;
        tvNumSide = 6 * unit;
        tvNumSize = ViewParamsUtil.h0;
        if (WinLayout.isVertScreen) {
            tvNumHorMargin = unit;
            countHorMargin = 3 * unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvflightNoSize = ViewParamsUtil.h4;
            tvflightNoHeight = ViewParamsUtil.h4Height;
            ivLogoSize = ViewParamsUtil.h4;
            ivLogoHorMargin = unit / 2;
            tvflightNameSize = ViewParamsUtil.h6;
            tvflightNameHeight = ViewParamsUtil.h6Height;
            timeSize = ViewParamsUtil.h2;
            timeHeight = ViewParamsUtil.h2Height;
            placeSize = ViewParamsUtil.h6;
            placeHeight = ViewParamsUtil.h6Height;
            ivLineWidth = 12 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = ViewParamsUtil.h2;
            tvPriceHeight = ViewParamsUtil.h2Height;
        } else {
            tvNumHorMargin = 2 * unit;
            countHorMargin = 3 * unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvflightNoSize = ViewParamsUtil.h5;
            tvflightNoHeight = ViewParamsUtil.h5Height;
            ivLogoSize = ViewParamsUtil.h5;
            ivLogoHorMargin = unit / 2;
            tvflightNameSize = ViewParamsUtil.h7;
            tvflightNameHeight = ViewParamsUtil.h7Height;
            timeSize = ViewParamsUtil.h3;
            timeHeight = ViewParamsUtil.h3Height;
            placeSize = ViewParamsUtil.h7;
            placeHeight = ViewParamsUtil.h7Height;
            ivLineWidth = 12 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = ViewParamsUtil.h3;
            tvPriceHeight = ViewParamsUtil.h3Height;
        }
    }

    //半屏布局参数
    private void initHalf(){
        int unit = ViewParamsUtil.unit;
        tvNumSide = 6 * unit;
        tvNumSize = ViewParamsUtil.h0;
        if(WinLayout.isVertScreen){
            tvNumHorMargin = unit;
            countHorMargin = 3 * unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvflightNoSize = ViewParamsUtil.h4;
            tvflightNoHeight = ViewParamsUtil.h4Height;
            ivLogoSize = ViewParamsUtil.h4;
            ivLogoHorMargin = unit / 2;
            tvflightNameSize =  ViewParamsUtil.h6;
            tvflightNameHeight =  ViewParamsUtil.h6Height;
            timeSize = ViewParamsUtil.h2;
            timeHeight = ViewParamsUtil.h2Height;
            placeSize = ViewParamsUtil.h6;
            placeHeight =ViewParamsUtil.h6Height;
            ivLineWidth = 12 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = ViewParamsUtil.h2;
            tvPriceHeight = ViewParamsUtil.h2Height;
        }else {
            if (SizeConfig.screenHeight < 480){
                tvflightNoTopMargin = unit ;    //航班编号上边距
                tvflightNameBottomMargin =  unit;    //航空公司名字下边距
                placeBottomMargin = unit;    //出发、到达地点下边距
            }else {
                tvflightNoTopMargin = 4 * unit ;    //航班编号上边距
                tvflightNameBottomMargin = 3 * unit;    //航空公司名字下边距
                placeBottomMargin = 3 * unit;    //出发、到达地点下边距
            }
            tvNumHorMargin = unit;
            countHorMargin = unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvflightNoSize = ViewParamsUtil.h3;
            tvflightNoHeight = ViewParamsUtil.h3Height;
            ivLogoSize = ViewParamsUtil.h5;
            ivLogoHorMargin = unit / 2;
            tvflightNameSize =  ViewParamsUtil.h7;
            tvflightNameHeight =  ViewParamsUtil.h7Height;
            timeSize = ViewParamsUtil.h3;
            timeHeight = ViewParamsUtil.h3Height;
            placeSize = ViewParamsUtil.h7;
            placeHeight = ViewParamsUtil.h7Height;
            seatSize = ViewParamsUtil.h5;
            seatHeight = ViewParamsUtil.h5Height;
            ivLineWidth = 4 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = ViewParamsUtil.h3;
            tvPriceHeight = ViewParamsUtil.h3Height;
            tvflightNoBottomMargin = unit ;    //航班编号下边距
            tvPriceBottomMargin = unit;    //价格下边距
        }

    }

    //无屏布局参数
    private void initNone(){
        int unit = ViewParamsUtil.unit;
        tvNumHorMargin = 2 * unit;
        tvNumSide = 6 * unit;
        tvNumSize = ViewParamsUtil.h0;
        countHorMargin = 2 * unit;
        timeVerMargin = unit / 2;
        timeHorMargin = unit;
        tvflightNoSize = ViewParamsUtil.h5;
        tvflightNoHeight = ViewParamsUtil.h5Height;
        ivLogoSize = ViewParamsUtil.h5;
        ivLogoHorMargin = unit / 2;
        tvflightNameSize =  ViewParamsUtil.h7;
        tvflightNameHeight =  ViewParamsUtil.h7Height;
        timeSize = ViewParamsUtil.h3;
        timeHeight = ViewParamsUtil.h3Height;
        placeSize = ViewParamsUtil.h7;
        placeHeight = ViewParamsUtil.h7Height;
        ivLineWidth = (int)(12 * 0.9 * unit);
        ivLineHeight = (int)(4 * 0.9 * unit);
        tvPriceSize = ViewParamsUtil.h3;
        tvPriceHeight = ViewParamsUtil.h3Height;

    }

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        QiWuFlightTicketData flightTicketData = (QiWuFlightTicketData) data;
        WinLayout.getInstance().vTips = flightTicketData.vTips;
        LogUtil.logd(
                WinLayout.logTag + "getView--QiWuFlightTicketData.vTips:" + flightTicketData.vTips);

        View view;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                view = createViewFull(flightTicketData);
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                if (WinLayout.isVertScreen){
                    view = createViewFull(flightTicketData);
                }else {
                    view = createViewHalf(flightTicketData);
                }
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(flightTicketData);
                break;
        }

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.object = FlightListView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(QiWuFlightTicketData flightTicketData) {
        ViewFactory.ViewAdapter
                titleViewAdapter =
                ListTitleView.getInstance().getView(flightTicketData, "flight", "");
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view, layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                SizeConfig.pageFlightCount * SizeConfig.itemHeightPro);
        llLayout.addView(llContents, layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        llContents.addView(llContent, layoutParams);

        mCurPage = flightTicketData.mTitleInfo.curPage;
        mMaxPage = flightTicketData.mTitleInfo.maxPage;
        LinearLayout llPager = new PageView(GlobalContext.get(), mCurPage, mMaxPage);
        //llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,
                LinearLayout.LayoutParams.MATCH_PARENT);
        llContents.addView(llPager, layoutParams);

        llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
        llContent.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation,
                            IViewStateListener.STATE_ANIM_ON_START);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation,
                            IViewStateListener.STATE_ANIM_ON_REPEAT);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener
                            .onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
                }
            }
        });
        mItemViews = new ArrayList<>();
        for (int i = 0; i < flightTicketData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    SizeConfig.itemHeightPro);
            View itemView = createItemView(i, flightTicketData.mFlightTicketBeans.get(i),
                    i != SizeConfig.pageFlightCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    private View createViewHalf(QiWuFlightTicketData flightListViewData){
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);

        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(flightListViewData,"flight","");
//		LogUtil.logd(WinLayout.logTag+ "flightListViewData.mTitleInfo.titlefix: " + flightListViewData.mTitleInfo.titlefix);
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
        llContent.setLayoutAnimationListener(new Animation.AnimationListener() {
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
                layoutParams.rightMargin = ViewParamsUtil.unit;
            }
            View itemView = createItemViewHalf(i,flightListViewData.mFlightTicketBeans.get(i),i != flightListViewData.count - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }
		/*if (flightListViewData.count < SizeConfig.getInstance().getPageFlightCount()){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.getInstance().getPageFlightCount() - flightListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/
        int blankCount = SizeConfig.pageFlightCount - flightListViewData.count;
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

    private View createViewNone(QiWuFlightTicketData flightListViewData){
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

        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(flightListViewData,"flight","");
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
        llContent.setLayoutAnimationListener(new Animation.AnimationListener() {
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
            View itemView = createItemView(i,flightListViewData.mFlightTicketBeans.get(i),i != SizeConfig.pageFlightCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    @SuppressLint({"NewApi", "RtlHardcoded"})
    private View createItemView(int position, QiWuFlightTicketData.FlightTicketBean flightBean,
            boolean showDivider) {
        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());

        // 表示整个item的view
        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llItemLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llItem, llItemLayoutParams);
        llItem.setGravity(Gravity.CENTER_VERTICAL);

        // 表示序号的view
        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        LinearLayout.LayoutParams tvNumLayoutParams =
                new LinearLayout.LayoutParams(tvNumSide,
                        tvNumSide);
        tvNumLayoutParams.leftMargin = tvNumHorMargin;
        tvNumLayoutParams.rightMargin = tvNumHorMargin;
        tvNum.setText(String.valueOf(position + 1));
        llItem.addView(tvNum, tvNumLayoutParams);

        // 包含所有飞机票信息的layout
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams mLayoutParams =
                new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llItem.addView(llContent, mLayoutParams);

        // 包含飞机编号和航空公司信息的layout
        LinearLayout llTop = new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.bottomMargin = timeVerMargin;
        llContent.addView(llTop, llLayoutParams);

        TextView tvflightNo = new TextView(GlobalContext.get());
        tvflightNo.setGravity(Gravity.CENTER_VERTICAL);
        tvflightNo.setSingleLine();
        tvflightNo.setEllipsize(TextUtils.TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                tvflightNoHeight);
        llLayoutParams.rightMargin = timeHorMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvflightNo, llLayoutParams);

        TextView tvAirline = new TextView(GlobalContext.get());
        tvAirline.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));
        tvAirline.setSingleLine();
        tvAirline.setEllipsize(TextUtils.TruncateAt.END);
        tvAirline.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvAirline, llLayoutParams);

        LinearLayout llRoute = new LinearLayout(GlobalContext.get());
        llRoute.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llContent.addView(llRoute, llLayoutParams);

        LinearLayout llDepart = new LinearLayout(GlobalContext.get());
        llDepart.setOrientation(LinearLayout.VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(llDepart, llLayoutParams);

        TextView tvDepartTime = new TextView(GlobalContext.get());
        tvDepartTime.setGravity(Gravity.CENTER_VERTICAL);
        tvDepartTime.setSingleLine();
        llLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, timeHeight);
        llLayoutParams.bottomMargin = timeVerMargin;
        llDepart.addView(tvDepartTime, llLayoutParams);

        TextView tvDepartName = new TextView(GlobalContext.get());
        tvDepartName.setSingleLine();
        tvDepartName.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartName.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, placeHeight);
        llDepart.addView(tvDepartName, llLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setImageDrawable(LayouUtil.getDrawable("airlines_to2"));
        llLayoutParams = new LinearLayout.LayoutParams(ivLineWidth, ivLineHeight);
        llLayoutParams.leftMargin = timeVerMargin;
        llLayoutParams.rightMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llRoute.addView(view, llLayoutParams);

        LinearLayout lArrival = new LinearLayout(GlobalContext.get());
        lArrival.setOrientation(LinearLayout.VERTICAL);
        lArrival.setGravity(Gravity.LEFT);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lArrival, llLayoutParams);

        LinearLayout lArrivalTime = new LinearLayout(GlobalContext.get());
        lArrival.addView(lArrivalTime, llLayoutParams);

        TextView tvArrivalTime = new TextView(GlobalContext.get());
        tvArrivalTime.setSingleLine();
        tvArrivalTime.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalTime.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, timeHeight);
        lLayoutParams.bottomMargin = timeVerMargin;
        lArrivalTime.addView(tvArrivalTime, lLayoutParams);

        TextView addDate = new TextView(GlobalContext.get());
        addDate.setTextColor(Color.RED);
        addDate.setSingleLine();
        addDate.setEllipsize(TextUtils.TruncateAt.END);
        addDate.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        addDate.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) LayouUtil.getDimen("m16"));
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lArrivalTime.addView(addDate, lLayoutParams);
        if(Integer.valueOf(flightBean.addDate) > 0){
            addDate.setText("+"+flightBean.addDate);
        }

        TextView tvArrivalName = new TextView(GlobalContext.get());
        tvArrivalName.setSingleLine();
        tvArrivalName.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalName.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        tvArrivalName.setMinEms(6);
        lLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, placeHeight);
        lArrival.addView(tvArrivalName, lLayoutParams);

        LinearLayout lPrice = new LinearLayout(GlobalContext.get());
        lPrice.setOrientation(LinearLayout.VERTICAL);
        lPrice.setGravity(Gravity.CENTER_HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lPrice, llLayoutParams);

        TextView tvPrice = new TextView(GlobalContext.get());
        tvPrice.setSingleLine();
        tvPrice.setEllipsize(TextUtils.TruncateAt.END);
        tvPrice.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                tvPriceHeight);
        llLayoutParams.bottomMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvPrice, llLayoutParams);

        TextView tvSeat = new TextView(GlobalContext.get());
        tvSeat.setSingleLine();
        tvSeat.setEllipsize(TextUtils.TruncateAt.END);
        tvSeat.setGravity(Gravity.CENTER_VERTICAL);
        lLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, placeHeight);
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvSeat, lLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        RelativeLayout.LayoutParams dividerLayoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        dividerHeight);
        dividerLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, dividerLayoutParams);

        tvAirline.setText(String.format(" %s", flightBean.airline));
        Drawable drawable = getAirIcon(flightBean.airline);
        drawable.setBounds(0, 0, ivLogoSize, ivLogoSize);
        tvAirline.setCompoundDrawables(drawable, null, null, null);
        tvflightNo.setText(flightBean.flightNo);
        if (!TextUtils.isEmpty(flightBean.departAirportName)) {
            tvDepartName.setText(flightBean.departAirportName);
        } else {
            tvDepartName.setText("未知名称");
        }
        if (!TextUtils.isEmpty(flightBean.arrivalAirportName)) {
            tvArrivalName.setText(flightBean.arrivalAirportName);
        } else {
            tvArrivalName.setText("未知名称");
        }
        tvDepartTime.setText(flightBean.departureTime);
        tvArrivalTime.setText(flightBean.arrivalTime);
        tvSeat.setText(flightBean.recommendSeat);
        String price = "¥" + flightBean.recommendPrice;
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

        TextViewUtil.setTextSize(tvNum, tvNumSize);
        TextViewUtil.setTextColor(tvNum, tvNumColor);
        tvSeat.setTextColor(timeColor);
        tvSeat.setTextSize(TypedValue.COMPLEX_UNIT_PX, placeSize);
        tvPrice.setTextColor(tvPriceColor);
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvPriceSize);
        tvArrivalName.setTextColor(placeColor);
        tvArrivalName.setTextSize(TypedValue.COMPLEX_UNIT_PX, placeSize);
        tvArrivalTime.setTextColor(timeColor);
        tvArrivalTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize);
        tvDepartName.setTextColor(placeColor);
        tvDepartName.setTextSize(TypedValue.COMPLEX_UNIT_PX, placeSize);
        tvDepartTime.setTextColor(timeColor);
        tvDepartTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize);
        tvAirline.setTextColor(placeColor);
        tvAirline.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvflightNameSize);
        tvflightNo.setTextColor(placeColor);
        tvflightNo.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvflightNoSize);

        return itemView;
    }

    TextView tvAirline;

    private View createItemViewHalf(int position, QiWuFlightTicketData.FlightTicketBean flightBean, boolean showDivider){

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

        // 表示序号的view
        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        LinearLayout.LayoutParams tvNumLayoutParams =
                new LinearLayout.LayoutParams(tvNumSide, tvNumSide);
        tvNumLayoutParams.topMargin = tvNumHorMargin;
        tvNumLayoutParams.bottomMargin = tvNumHorMargin;
        tvNumLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        tvNum.setText(String.valueOf(position + 1));
        lItem.addView(tvNum, tvNumLayoutParams);

        TextView tvflightNo = new TextView(GlobalContext.get());
        tvflightNo.setGravity(Gravity.CENTER_VERTICAL);
        tvflightNo.setSingleLine();
        tvflightNo.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvflightNoHeight);
        lLayoutParams.bottomMargin = tvflightNoBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvflightNo, lLayoutParams);

        TextView tvAirline = new TextView(GlobalContext.get());
        tvAirline.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));
        tvAirline.setSingleLine();
        tvAirline.setEllipsize(TextUtils.TruncateAt.END);
        tvAirline.setGravity(Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lLayoutParams.bottomMargin = tvflightNameBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvAirline, lLayoutParams);

        LinearLayout llRoute = new LinearLayout(GlobalContext.get());
        llRoute.setOrientation(LinearLayout.HORIZONTAL);
//		rlRoute.setId(ViewUtils.generateViewId());
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lLayoutParams.bottomMargin = placeBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(llRoute,lLayoutParams);

        LinearLayout llDepart = new LinearLayout(GlobalContext.get());
        llDepart.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams mlLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1);
        llRoute.addView(llDepart, mlLayoutParams);

        TextView tvDepartTime = new TextView(GlobalContext.get());
        tvDepartTime.setGravity(Gravity.CENTER);
        tvDepartTime.setSingleLine();
        tvDepartTime.setId(ViewUtils.generateViewId());
        mlLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, timeHeight);
        llDepart.addView(tvDepartTime, mlLayoutParams);

        TextView tvDepartName = new TextView(GlobalContext.get());
        tvDepartName.setSingleLine();
        tvDepartName.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartName.setId(ViewUtils.generateViewId());
        tvDepartName.setGravity(Gravity.CENTER);
        mlLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, timeHeight);
        llDepart.addView(tvDepartName, mlLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setId(ViewUtils.generateViewId());
        view.setImageDrawable(LayouUtil.getDrawable("airlines_to0"));
        mlLayoutParams = new LinearLayout.LayoutParams(ivLineWidth, ivLineHeight);
        mlLayoutParams.leftMargin = timeVerMargin;
        mlLayoutParams.rightMargin = timeVerMargin;
        mlLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		mLLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        llRoute.addView(view, mlLayoutParams);

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

        LinearLayout llArrival = new LinearLayout(GlobalContext.get());
        llArrival.setOrientation(LinearLayout.VERTICAL);
        mlLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT,1);
        llRoute.addView(llArrival, mlLayoutParams);

        TextView tvArrivalTime = new TextView(GlobalContext.get());
        tvArrivalTime.setSingleLine();
        tvArrivalTime.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalTime.setId(ViewUtils.generateViewId());
        tvArrivalTime.setGravity(Gravity.CENTER);
        mlLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, timeHeight);
//		mlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        llArrival.addView(tvArrivalTime, mlLayoutParams);

        TextView tvArrivalName = new TextView(GlobalContext.get());
        tvArrivalName.setSingleLine();
        tvArrivalName.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalName.setId(ViewUtils.generateViewId());
        tvArrivalName.setGravity(Gravity.CENTER);
        mlLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, timeHeight);
//		mlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        llArrival.addView(tvArrivalName, mlLayoutParams);

		/*LinearLayout lPrice = new LinearLayout(GlobalContext.get());
		lPrice.setOrientation(LinearLayout.VERTICAL);
		lPrice.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
		lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,3);
		lItem.addView(lPrice,lLayoutParams);*/

        TextView tvPrice = new TextView(GlobalContext.get());
        tvPrice.setSingleLine();
        tvPrice.setEllipsize(TextUtils.TruncateAt.END);
        tvPrice.setGravity(Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPriceHeight);
        lLayoutParams.bottomMargin = tvPriceBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvPrice, lLayoutParams);

        TextView tvDiscount = new TextView(GlobalContext.get());
        tvDiscount.setSingleLine();
        tvDiscount.setEllipsize(TextUtils.TruncateAt.END);
        tvDiscount.setGravity(Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,seatHeight);
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvDiscount, lLayoutParams);

        tvAirline.setText(" "+flightBean.airline);
        Drawable drawable = getAirIcon(flightBean.airline);
        drawable.setBounds(0,0,(int)LayouUtil.getDimen("m15"),(int)LayouUtil.getDimen("m15"));
        tvAirline.setCompoundDrawables(drawable,null,null,null);
        tvflightNo.setText(flightBean.flightNo);
        if(!TextUtils.isEmpty(flightBean.departAirportName)){
            Log.d("jack", "createItemViewHalf: "+flightBean.departAirportName);
            tvDepartName.setText(flightBean.departAirportName);
        }else{
            tvDepartName.setText("未知名称");
        }
        if(!TextUtils.isEmpty(flightBean.arrivalAirportName)){
            Log.d("jack", "createItemViewHalf: "+flightBean.arrivalAirportName);
            tvArrivalName.setText(flightBean.arrivalAirportName);
        }else{
            tvArrivalName.setText("未知名称");
        }
        tvDepartTime.setText(flightBean.departureTime);
        tvArrivalTime.setText(flightBean.arrivalTime);
        tvDiscount.setText(flightBean.recommendSeat);
        String price = "¥" + flightBean.recommendPrice;
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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


        TextViewUtil.setTextSize(tvNum, tvNumSize);
        TextViewUtil.setTextColor(tvNum, tvNumColor);
        tvDiscount.setTextColor(placeColor);
        tvDiscount.setTextSize(TypedValue.COMPLEX_UNIT_PX,seatSize);
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

    private View createItemViewNone(int position, QiWuFlightTicketData.FlightTicketBean flightBean, boolean showDivider){
        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        FrameLayout flContent = new FrameLayout(GlobalContext.get());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = countHorMargin;
        itemView.addView(flContent,layoutParams);

        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flContent.addView(llItem, mLayoutParams);

        // 表示序号的view
        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        LinearLayout.LayoutParams tvNumLayoutParams =
                new LinearLayout.LayoutParams(tvNumSide,
                        tvNumSide);

        tvNumLayoutParams.leftMargin = tvNumHorMargin;
        tvNumLayoutParams.rightMargin = tvNumHorMargin;
        tvNum.setText(String.valueOf(position + 1));
        llItem.addView(tvNum, tvNumLayoutParams);

        LinearLayout llTop = new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.bottomMargin = timeVerMargin;
        llItem.addView(llTop, llLayoutParams);

        TextView tvflightNo = new TextView(GlobalContext.get());
        tvflightNo.setGravity(Gravity.CENTER_VERTICAL);
        tvflightNo.setSingleLine();
        tvflightNo.setEllipsize(TextUtils.TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvflightNoHeight);
        llLayoutParams.rightMargin = timeHorMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvflightNo, llLayoutParams);

        tvAirline = new TextView(GlobalContext.get());
        tvAirline.setBackground(LayouUtil.getDrawable("poi_item_distant_bg"));
        tvAirline.setSingleLine();
        tvAirline.setEllipsize(TextUtils.TruncateAt.END);
        tvAirline.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvAirline, llLayoutParams);

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
        tvDepartName.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartName.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        llDepart.addView(tvDepartName, llLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setImageDrawable(LayouUtil.getDrawable("airlines_to2"));
        llLayoutParams = new LinearLayout.LayoutParams(ivLineWidth,ivLineHeight);
        llLayoutParams.leftMargin = timeVerMargin;
        llLayoutParams.rightMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llRoute.addView(view, llLayoutParams);

        LinearLayout lArrival = new LinearLayout(GlobalContext.get());
        lArrival.setOrientation(LinearLayout.VERTICAL);
        lArrival.setGravity(Gravity.RIGHT);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lArrival, llLayoutParams);

        TextView tvArrivalTime = new TextView(GlobalContext.get());
        tvArrivalTime.setSingleLine();
        tvArrivalTime.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalTime.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,timeHeight);
        lLayoutParams.bottomMargin = timeVerMargin;
        lArrival.addView(tvArrivalTime, lLayoutParams);

        TextView tvArrivalName = new TextView(GlobalContext.get());
        tvArrivalName.setSingleLine();
        tvArrivalName.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        lArrival.addView(tvArrivalName, lLayoutParams);

        LinearLayout lPrice = new LinearLayout(GlobalContext.get());
        lPrice.setOrientation(LinearLayout.VERTICAL);
        lPrice.setGravity(Gravity.CENTER_HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lPrice, llLayoutParams);

        TextView tvPrice = new TextView(GlobalContext.get());
        tvPrice.setSingleLine();
        tvPrice.setEllipsize(TextUtils.TruncateAt.END);
        tvPrice.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPriceHeight);
        llLayoutParams.bottomMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvPrice, llLayoutParams);

        TextView tvDiscount = new TextView(GlobalContext.get());
        tvDiscount.setSingleLine();
        tvDiscount.setEllipsize(TextUtils.TruncateAt.END);
        tvDiscount.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvDiscount, llLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        tvAirline.setText(" "+flightBean.airline);
        Drawable drawable = getAirIcon(flightBean.airline);
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
        tvDepartTime.setText(flightBean.departureTime);
        tvArrivalTime.setText(flightBean.arrivalTime);
        tvDiscount.setText(flightBean.recommendSeat);
        String price = "¥" + flightBean.recommendPrice;
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

        tvDiscount.setTextColor(placeColor);
        tvDiscount.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
        tvPrice.setTextColor(tvPriceColor);
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvPriceSize);
        tvArrivalName.setTextColor(placeColor);
        tvArrivalName.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
        tvArrivalTime.setTextColor(timeColor);
        tvArrivalTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,timeSize);
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
    private Drawable getAirIcon(String airName) {
        Drawable drawable;
        switch (airName) {
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
            case "中国东方航空":
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
            case "中国南方航空":
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

        LogUtil.logd(WinLayout.logTag + "getAirIcon: " + airName);
        if (drawable != null) {
            LogUtil.logd(WinLayout.logTag + "getAirIcon: get it");
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
        showSelectItem(index);
    }

    private void showSelectItem(int index) {
        for (int i = 0; i < mItemViews.size(); i++) {
            if (i == index) {
                mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
            } else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }
}