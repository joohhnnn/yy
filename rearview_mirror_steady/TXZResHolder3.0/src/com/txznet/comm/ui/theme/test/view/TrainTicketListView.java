package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
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
import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;
import com.txznet.comm.ui.viewfactory.data.TrainListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITrainTicketList;
import com.txznet.comm.util.TextViewUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daviddai on 2019/9/5
 * 火车票列表
 */
public class TrainTicketListView extends ITrainTicketList {

    private static TrainTicketListView sInstance = new TrainTicketListView();

    public static TrainTicketListView getInstance() {
        return sInstance;
    }

    private List<View> mItemViews;

    private int tvNumSide;    //序号宽高
    private int tvNumHorMargin;    //序号左右边距
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
    private int dividerHeight;

    private int countHorMargin;    //内容左右边距
    private int timeVerMargin;    //出发、到达时间上下边距
    private int timeHorMargin;    //出发、到达时间左右边距
    private int tvTrainNoSize;    //火车车次字体大小
    private int tvTrainNoHeight;    //火车车次行高
    private int tvTimeSize;    //路程时长字体大小
    private int tvTimeHeight;    //路程时长行高
    private int timeSize;    //出发、到达时间字体大小
    private int timeHeight;    //出发、到达时间行高
    private int timeColor;    //出发、到达时间字体颜色
    private int placeSize;    //出发、到达地点（座位）字体大小
    private int placeHeight;    //出发、到达地点（座位）行高
    private int placeColor;    //出发、到达地点（座位）字体颜色
    private int ivLineWidth;    //出发到达连接线宽度
    private int ivLineHeight;    //出发到达连接线高度
    private int tvPriceSize;    //价格字体大小
    private int tvPriceHeight;    //价格字体行高
    private int tvPriceColor;    //价格字体颜色

    //半屏布局
    private int tvTrainNoTopMargin;    //火车车次上边距
    private int tvTrainNoBottomMargin;    //火车车次下边距
    private int tvTimeBottomMargin;    //路程时间下边距
    private int placeBottomMargin;    //出发、到达地点下边距
    private int tvPriceBottomMargin;    //价格下边距
    private int seatSize;    //座位字体大小
    private int seatHeight;    //座位行高

    @Override
    public void init() {
        super.init();
        dividerHeight = 1;
        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        timeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        placeColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        tvPriceColor = Color.parseColor(LayouUtil.getString("color_flight_price"));
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        LogUtil.logd(WinLayout.logTag + "TrainTicket onUpdateParams: " + styleIndex);

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
            tvTrainNoSize = ViewParamsUtil.h4;
            tvTrainNoHeight = ViewParamsUtil.h4Height;
            tvTimeSize = ViewParamsUtil.h6;
            tvTimeHeight = ViewParamsUtil.h6Height;
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
            tvTrainNoSize = ViewParamsUtil.h5;
            tvTrainNoHeight = ViewParamsUtil.h5Height;
            tvTimeSize = ViewParamsUtil.h7;
            tvTimeHeight = ViewParamsUtil.h7Height;
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
        if (WinLayout.isVertScreen) {
            tvNumHorMargin = unit;
            countHorMargin = 3 * unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvTrainNoSize = ViewParamsUtil.h4;
            tvTrainNoHeight = ViewParamsUtil.h4Height;
            tvTimeSize =  ViewParamsUtil.h6;
            tvTimeHeight =  ViewParamsUtil.h6Height;
            timeSize = ViewParamsUtil.h2;
            timeHeight = ViewParamsUtil.h2Height;
            placeSize = ViewParamsUtil.h6;
            placeHeight = ViewParamsUtil.h6Height;
            ivLineWidth = 12 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = ViewParamsUtil.h2;
            tvPriceHeight = ViewParamsUtil.h2Height;
        }else {
            if (SizeConfig.screenHeight < 480){
                tvTrainNoTopMargin = unit ;    //火车车次上边距
                tvTimeBottomMargin = unit;    //路程时间下边距
                placeBottomMargin = unit;    //出发、到达地点下边距
            }else {
                tvTrainNoTopMargin = 4 * unit ;    //火车车次上边距
                tvTimeBottomMargin = 3 * unit;    //路程时间下边距
                placeBottomMargin = 3 * unit;    //出发、到达地点下边距
            }
            tvNumHorMargin = unit;
            countHorMargin = unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvTrainNoSize = ViewParamsUtil.h3;
            tvTrainNoHeight = ViewParamsUtil.h3Height;
            tvTimeSize =  ViewParamsUtil.h7;
            tvTimeHeight =  ViewParamsUtil.h7Height;
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

            tvTrainNoBottomMargin = unit ;    //火车车次下边距
            tvPriceBottomMargin = unit;    //价格下边距
        }
    }

    //无屏布局参数
    private void initNone(){
        int unit = ViewParamsUtil.unit;
        tvNumSide = 6 * unit;
        tvNumSize = ViewParamsUtil.h0;
        tvNumHorMargin = 2 * unit;
        countHorMargin = 2 * unit;
        timeVerMargin = unit / 2;
        timeHorMargin = unit;
        tvTrainNoSize = ViewParamsUtil.h5;
        tvTrainNoHeight = ViewParamsUtil.h5Height;
        tvTimeSize =  ViewParamsUtil.h7;
        tvTimeHeight =  ViewParamsUtil.h7Height;
        timeSize = ViewParamsUtil.h3;
        timeHeight = ViewParamsUtil.h3Height;
        placeSize = ViewParamsUtil.h7;
        placeHeight = ViewParamsUtil.h7Height;
        ivLineWidth = (int)(12 * 0.9 * unit);
        ivLineHeight = (int)(4 * 0.9 * unit);
        tvPriceSize = ViewParamsUtil.h3;
        tvPriceHeight =  ViewParamsUtil.h3Height;
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        QiWuTrainTicketData trainListViewData = ((QiWuTrainTicketData) data);
        WinLayout.getInstance().vTips = trainListViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "trainListViewData.vTips:" + trainListViewData.vTips);

        View view;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                view = createViewFull(trainListViewData);
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                if (WinLayout.isVertScreen){
                    view = createViewFull(trainListViewData);
                }else {
                    view = createViewHalf(trainListViewData);
                }
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(trainListViewData);
                break;
        }

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.object = TrainTicketListView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(QiWuTrainTicketData trainListViewData) {
        ViewFactory.ViewAdapter titleViewAdapter =
                ListTitleView.getInstance().getView(trainListViewData, "train", "");

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
        //llLayout.addView(llContent,layoutParams);
        llContents.addView(llContent, layoutParams);

        mCurPage = trainListViewData.mTitleInfo.curPage;
        mMaxPage = trainListViewData.mTitleInfo.maxPage;
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
        for (int i = 0; i < trainListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    SizeConfig.itemHeightPro);
            View itemView = createItemView(i, trainListViewData.mTrainTicketBeans.get(i),
                    i != SizeConfig.pageTrainCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    private View createViewHalf(QiWuTrainTicketData trainListViewData){
        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(trainListViewData,"train","");

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageCount * SizeConfig.itemHeight);
        llLayout.addView(llContents,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setBackground(LayouUtil.getDrawable("white_range_layout_movie"));
        llContent.setOrientation(LinearLayout.HORIZONTAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
        }
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        //llLayout.addView(llContent,layoutParams);
        llContents.addView(llContent,layoutParams);

        mCurPage = trainListViewData.mTitleInfo.curPage;
        mMaxPage = trainListViewData.mTitleInfo.maxPage;
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
        for (int i = 0; i < trainListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1);
            if (i != trainListViewData.count - 1){
                layoutParams.rightMargin =  ViewParamsUtil.unit;
            }
            View itemView = createItemViewHalf(i, trainListViewData.mTrainTicketBeans.get(i), i != SizeConfig.pageTrainCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }
        /*if (trainListViewData.ticketList.size() < SizeConfig.getInstance().getPageTrainCount()){
            LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.getInstance().getPageTrainCount() - trainListViewData.ticketList.size());
            llContent.addView(linearLayout, layoutParams);
        }*/
        int blankCount = SizeConfig.pageTrainCount - trainListViewData.count;
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

    private View createViewNone(QiWuTrainTicketData trainListViewData){
        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(trainListViewData,"train","");

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        llLayout.addView(llContents,layoutParams);

        mCurPage = trainListViewData.mTitleInfo.curPage;
        mMaxPage = trainListViewData.mTitleInfo.maxPage;
        LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
        }
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageFlightCount * SizeConfig.itemHeightPro);
        //llLayout.addView(llContent,layoutParams);
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
        for (int i = 0; i < trainListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.itemHeightPro);
            View itemView = createItemView(i, trainListViewData.mTrainTicketBeans.get(i), i != SizeConfig.pageTrainCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    /**
     * 创建item
     *
     * @param position 序号，从0开始
     * @return item
     */
    @SuppressLint("RtlHardcoded")
    private View createItemView(int position, QiWuTrainTicketData.TrainTicketBean ticketListBean,
            boolean showDivider) {
        Context context = GlobalContext.get();
        RippleView itemView = new RippleView(context);
        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));

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
                new LinearLayout.LayoutParams(tvNumSide, tvNumSide);
        tvNumLayoutParams.leftMargin = tvNumHorMargin;
        tvNumLayoutParams.rightMargin = tvNumHorMargin;
        tvNum.setText(String.valueOf(position + 1));
        llItem.addView(tvNum, tvNumLayoutParams);

        // 包含所有车票信息的layout
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams mLayoutParams =
                new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llItem.addView(llContent, mLayoutParams);

        // 包含车辆编号和耗时的layout
        LinearLayout llTop = new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.bottomMargin = timeVerMargin;
        llContent.addView(llTop, llLayoutParams);

        // 车辆编号
        TextView tvTrainNo = new TextView(GlobalContext.get());
        tvTrainNo.setGravity(Gravity.CENTER_VERTICAL);
        tvTrainNo.setSingleLine();
        tvTrainNo.setEllipsize(TextUtils.TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                tvTrainNoHeight);
        llLayoutParams.rightMargin = timeHorMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvTrainNo, llLayoutParams);

        // 耗时
        TextView tvTime = new TextView(GlobalContext.get());
        tvTime.setSingleLine();
        tvTime.setEllipsize(TextUtils.TruncateAt.END);
        tvTime.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvTime, llLayoutParams);

        // 包含线路信息的layout
        LinearLayout llRoute = new LinearLayout(GlobalContext.get());
        llRoute.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llContent.addView(llRoute, llLayoutParams);

        // 包含起点站信息的layout
        LinearLayout llDepart = new LinearLayout(GlobalContext.get());
        llDepart.setOrientation(LinearLayout.VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(llDepart, llLayoutParams);

        // 出发的时间
        TextView tvDepartTime = new TextView(GlobalContext.get());
        tvDepartTime.setGravity(Gravity.CENTER_VERTICAL);
        tvDepartTime.setSingleLine();
        llLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, timeHeight);
        llLayoutParams.bottomMargin = timeVerMargin;
        llDepart.addView(tvDepartTime, llLayoutParams);

        // 起点站
        TextView tvDepartureStation = new TextView(GlobalContext.get());
        tvDepartureStation.setSingleLine();
        tvDepartureStation.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartureStation.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, placeHeight);
        llDepart.addView(tvDepartureStation, llLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setImageDrawable(LayouUtil.getDrawable("trainlines_to1"));
        llLayoutParams = new LinearLayout.LayoutParams(ivLineWidth, ivLineHeight);
        llLayoutParams.leftMargin = timeVerMargin;
        llLayoutParams.rightMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llRoute.addView(view, llLayoutParams);

        // 包含终点站信息的layout
        LinearLayout lArrival = new LinearLayout(GlobalContext.get());
        lArrival.setOrientation(LinearLayout.VERTICAL);
        lArrival.setGravity(Gravity.LEFT);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lArrival, llLayoutParams);

        LinearLayout lArrivalTime = new LinearLayout(GlobalContext.get());
        lArrival.addView(lArrivalTime, llLayoutParams);


        // 下车时间
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
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lArrivalTime.addView(addDate, llLayoutParams);
        if(Integer.valueOf(ticketListBean.addDate) > 0){
            addDate.setText("+"+ticketListBean.addDate);
        }
        // 终点站
        TextView tvArrivalStation = new TextView(GlobalContext.get());
        tvArrivalStation.setSingleLine();
        tvArrivalStation.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalStation.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        tvArrivalStation.setMinEms(6);
        lLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, placeHeight);
        lArrival.addView(tvArrivalStation, lLayoutParams);

        // 包含价格和是否有座的信息的layout
        LinearLayout lPrice = new LinearLayout(GlobalContext.get());
        lPrice.setOrientation(LinearLayout.VERTICAL);
        lPrice.setGravity(Gravity.CENTER_HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lPrice, llLayoutParams);

        // 价格
        TextView tvPrice = new TextView(GlobalContext.get());
        tvPrice.setSingleLine();
        tvPrice.setEllipsize(TextUtils.TruncateAt.END);
        tvPrice.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                tvPriceHeight);
        llLayoutParams.bottomMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvPrice, llLayoutParams);

        // 是否有座
        TextView tvSeat = new TextView(GlobalContext.get());
        tvSeat.setSingleLine();
        tvSeat.setEllipsize(TextUtils.TruncateAt.END);
        tvSeat.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, placeHeight);
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvSeat, llLayoutParams);

        // 分割线
        View divider = new View(GlobalContext.get());
        String costTime = formatCostTime(ticketListBean.costTime);
        tvTime.setText(costTime);
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        // 赋值序号
        tvNum.setText(String.valueOf(position + 1));

        tvTrainNo.setText(ticketListBean.trainNo);
        if (!TextUtils.isEmpty(ticketListBean.station)) {
            tvDepartureStation.setText(ticketListBean.station);
        } else {
            tvDepartureStation.setText("未知名称");
        }
        if (!TextUtils.isEmpty(ticketListBean.endStation)) {
            tvArrivalStation.setText(ticketListBean.endStation);
        } else {
            tvArrivalStation.setText("未知名称");
        }
        tvDepartTime.setText(ticketListBean.departureTime);
        tvArrivalTime.setText(ticketListBean.arrivalTime);
        String price = "¥" + ticketListBean.recommendPrice + "起";
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceString.setSpan(new RelativeSizeSpan(0.8f), price.length() - 1, price.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);
        tvSeat.setText(ticketListBean.recommendSeat);

        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

        TextViewUtil.setTextSize(tvNum, tvNumSize);
        TextViewUtil.setTextColor(tvNum, tvNumColor);
        tvSeat.setTextColor(placeColor);
        tvSeat.setTextSize(TypedValue.COMPLEX_UNIT_PX, placeSize);
        tvPrice.setTextColor(tvPriceColor);
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvPriceSize);
        tvArrivalStation.setTextColor(placeColor);
        tvArrivalStation.setTextSize(TypedValue.COMPLEX_UNIT_PX, placeSize);
        tvArrivalTime.setTextColor(timeColor);
        tvArrivalTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize);
        tvDepartureStation.setTextColor(placeColor);
        tvDepartureStation.setTextSize(TypedValue.COMPLEX_UNIT_PX, placeSize);
        tvDepartTime.setTextColor(timeColor);
        tvDepartTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, timeSize);
        tvTime.setTextColor(placeColor);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvTimeSize);
        tvTrainNo.setTextColor(placeColor);
        tvTrainNo.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvTrainNoSize);

        return itemView;
    }


    private View createItemViewHalf(int position, QiWuTrainTicketData.TrainTicketBean ticketListBean, boolean showDivider) {

        Context context = GlobalContext.get();
        RippleView itemView = new RippleView(context);
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

        TextView tvTrainNo = new TextView(GlobalContext.get());
        tvTrainNo.setGravity(Gravity.CENTER_VERTICAL);
        tvTrainNo.setSingleLine();
        tvTrainNo.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvTrainNoHeight);
        lLayoutParams.bottomMargin = tvTrainNoBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvTrainNo, lLayoutParams);

        TextView tvTime = new TextView(GlobalContext.get());
        tvTime.setSingleLine();
        tvTime.setEllipsize(TextUtils.TruncateAt.END);
        tvTime.setGravity(Gravity.CENTER);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lLayoutParams.bottomMargin = tvTimeBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvTime, lLayoutParams);

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

        TextView tvDepartureStation = new TextView(GlobalContext.get());
        tvDepartureStation.setSingleLine();
        tvDepartureStation.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartureStation.setId(ViewUtils.generateViewId());
        tvDepartureStation.setGravity(Gravity.CENTER);
        mlLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, timeHeight);
        llDepart.addView(tvDepartureStation, mlLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setId(ViewUtils.generateViewId());
        view.setImageDrawable(LayouUtil.getDrawable("trainlines_to2"));
        mlLayoutParams = new LinearLayout.LayoutParams(ivLineWidth, ivLineHeight);
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
        mlLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, timeHeight);
//		mlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        llArrival.addView(tvArrivalTime, mlLayoutParams);

        TextView tvArrivalStation = new TextView(GlobalContext.get());
        tvArrivalStation.setSingleLine();
        tvArrivalStation.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalStation.setId(ViewUtils.generateViewId());
        tvArrivalStation.setGravity(Gravity.CENTER);
        mlLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, timeHeight);
//		mlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        llArrival.addView(tvArrivalStation, mlLayoutParams);

        TextView tvPrice = new TextView(GlobalContext.get());
        tvPrice.setSingleLine();
        tvPrice.setEllipsize(TextUtils.TruncateAt.END);
        tvPrice.setGravity(Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPriceHeight);
        lLayoutParams.bottomMargin = tvPriceBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvPrice, lLayoutParams);

        TextView tvSeat = new TextView(GlobalContext.get());
        tvSeat.setSingleLine();
        tvSeat.setEllipsize(TextUtils.TruncateAt.END);
        tvSeat.setGravity(Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,seatHeight);
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvSeat, lLayoutParams);

        tvTrainNo.setText(ticketListBean.trainNo);
        if (!TextUtils.isEmpty(ticketListBean.station)) {
            tvDepartureStation.setText(ticketListBean.station);
        } else {
            tvDepartureStation.setText("未知名称");
        }
        if (!TextUtils.isEmpty(ticketListBean.endStation)) {
            tvArrivalStation.setText(ticketListBean.endStation);
        } else {
            tvArrivalStation.setText("未知名称");
        }

        tvTrainNo.setText(ticketListBean.trainNo);
        String costTime = formatCostTime(ticketListBean.costTime);
        tvTime.setText(costTime);
        if(!TextUtils.isEmpty(ticketListBean.station)){
            tvDepartureStation.setText(ticketListBean.station);
        }else{
            tvDepartureStation.setText("未知名称");
        }
        if(!TextUtils.isEmpty(ticketListBean.endStation)){
            tvArrivalStation.setText(ticketListBean.endStation);
        }else{
            tvArrivalStation.setText("未知名称");
        }

        tvDepartTime.setText(ticketListBean.departureTime);
        tvArrivalTime.setText(ticketListBean.arrivalTime);
        String price = "¥" + ticketListBean.recommendPrice + "起";
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceString.setSpan(new RelativeSizeSpan(0.8f), price.length() - 1, price.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);
        tvSeat.setText(ticketListBean.recommendSeat);


        TextViewUtil.setTextSize(tvNum, tvNumSize);
        TextViewUtil.setTextColor(tvNum, tvNumColor);
        tvSeat.setTextColor(placeColor);
        tvSeat.setTextSize(TypedValue.COMPLEX_UNIT_PX,seatSize);
        tvPrice.setTextColor(tvPriceColor);
        tvPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvPriceSize);
        tvArrivalStation.setTextColor(placeColor);
        tvArrivalStation.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
        tvArrivalTime.setTextColor(timeColor);
        tvArrivalTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,timeSize);
        tvDepartureStation.setTextColor(placeColor);
        tvDepartureStation.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
        tvDepartTime.setTextColor(timeColor);
        tvDepartTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,timeSize);
        tvTime.setTextColor(placeColor);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvTimeSize);
        tvTrainNo.setTextColor(timeColor);
        tvTrainNo.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvTrainNoSize);

        return itemView;
    }

    @NonNull
    private String formatCostTime(String costTime) {
        int costTimeMi = Integer.valueOf(costTime) % 60;
        int costTimeHour = Integer.valueOf(costTime) / 60;
        String costTimeText = "";
        if (costTimeHour > 0) {
            costTimeText += costTimeHour + "时";
        }
        if (costTimeMi > 0) {
            costTimeText += costTimeMi + "分";
        }
        return costTimeText;
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
