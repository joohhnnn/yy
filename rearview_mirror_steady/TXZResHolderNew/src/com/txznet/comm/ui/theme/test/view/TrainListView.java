package com.txznet.comm.ui.theme.test.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
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
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.TrainListViewData;
import com.txznet.comm.ui.viewfactory.data.TrainListViewData.TicketListBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITrainListView;

import java.util.ArrayList;
import java.util.List;

public class TrainListView extends ITrainListView {

    private static TrainListView sInstance = new TrainListView();

    public static TrainListView getInstance() {
        return sInstance;
    }

    private List<View> mItemViews;

    /*private int tvNumWidth;
    private int tvNumHeight;
    private int tvNumMarginLeft;*/
    private int dividerHeight;

    private int countHorMargin;    //??????????????????
    private int timeVerMargin;    //?????????????????????????????????
    private int timeHorMargin;    //?????????????????????????????????
    private int tvTrainNoSize;    //????????????????????????
    private int tvTrainNoHeight;    //??????????????????
    private int tvTimeSize;    //????????????????????????
    private int tvTimeHeight;    //??????????????????
    private int timeSize;    //?????????????????????????????????
    private int timeHeight;    //???????????????????????????
    private int timeColor;    //?????????????????????????????????
    private int placeSize;    //?????????????????????????????????????????????
    private int placeHeight;    //???????????????????????????????????????
    private int placeColor;    //?????????????????????????????????????????????
    private int ivLineWidth;    //???????????????????????????
    private int ivLineHeight;    //???????????????????????????
    private int tvPriceSize;    //??????????????????
    private int tvPriceHeight;    //??????????????????
    private int tvPriceColor;    //??????????????????

    //????????????
    int tvTrainNoTopMargin;    //?????????????????????
    int tvTrainNoBottomMargin;    //?????????????????????
    int tvTimeBottomMargin;    //?????????????????????
    int placeBottomMargin;    //??????????????????????????????
    int tvPriceBottomMargin;    //???????????????

    @Override
    public void init() {
        super.init();
        /*tvNumWidth = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
        tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
        tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);*/
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
        timeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        placeColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        tvPriceColor = Color.parseColor(LayouUtil.getString("color_flight_price"));
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
        if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            countHorMargin = 3 * unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvTrainNoSize = (int) LayouUtil.getDimen("vertical_h5");
            tvTrainNoHeight = (int) LayouUtil.getDimen("vertical_h5_height");
            tvTimeSize =  (int) LayouUtil.getDimen("vertical_h7");
            tvTimeHeight =  (int) LayouUtil.getDimen("vertical_h7_height");
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
            tvTrainNoSize = (int) LayouUtil.getDimen("h5");
            tvTrainNoHeight = (int) LayouUtil.getDimen("h5_height");
            tvTimeSize =  (int) LayouUtil.getDimen("h7");
            tvTimeHeight =  (int) LayouUtil.getDimen("h7_height");
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

    //??????????????????
    private void initHalf(){
        if (WinLayout.isVertScreen) {
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            countHorMargin = 3 * unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvTrainNoSize = (int) LayouUtil.getDimen("vertical_h5");
            tvTrainNoHeight = (int) LayouUtil.getDimen("vertical_h5_height");
            tvTimeSize =  (int) LayouUtil.getDimen("vertical_h7");
            tvTimeHeight =  (int) LayouUtil.getDimen("vertical_h7_height");
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
                tvTrainNoTopMargin = unit ;    //?????????????????????
                tvTimeBottomMargin = unit;    //?????????????????????
                placeBottomMargin = unit;    //??????????????????????????????
            }else {
                tvTrainNoTopMargin = 4 * unit ;    //?????????????????????
                tvTimeBottomMargin = 3 * unit;    //?????????????????????
                placeBottomMargin = 3 * unit;    //??????????????????????????????
            }
            countHorMargin = unit;
            timeVerMargin = unit;
            timeHorMargin = unit;
            tvTrainNoSize = (int) LayouUtil.getDimen("h3");
            tvTrainNoHeight = (int) LayouUtil.getDimen("h3_height");
            tvTimeSize =  (int) LayouUtil.getDimen("h7");
            tvTimeHeight =  (int) LayouUtil.getDimen("h7_height");
            timeSize = (int) LayouUtil.getDimen("h3");
            timeHeight = (int) LayouUtil.getDimen("h3_height");
            placeSize = (int) LayouUtil.getDimen("h7");
            placeHeight = (int) LayouUtil.getDimen("h7_height");
            ivLineWidth = 4 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = (int) LayouUtil.getDimen("h7");
            tvPriceHeight = (int) LayouUtil.getDimen("h7_height");

            tvTrainNoBottomMargin = unit ;    //?????????????????????
            tvPriceBottomMargin = unit;    //???????????????
        }
    }

    //??????????????????
    private void initNone(){
        if (WinLayout.isVertScreen){
            countHorMargin = (int) LayouUtil.getDimen("x20");
            timeVerMargin = (int) LayouUtil.getDimen("x5");
            timeHorMargin = (int) LayouUtil.getDimen("x10");
            tvTrainNoSize = (int) LayouUtil.getDimen("x30");
            tvTrainNoHeight = tvTrainNoSize + 10;
            tvTimeSize =  (int) LayouUtil.getDimen("x18");
            tvTimeHeight =  tvTimeSize + 10;
            timeSize = (int) LayouUtil.getDimen("x26");
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
            tvTrainNoSize = (int) LayouUtil.getDimen("h5_none");
            tvTrainNoHeight = (int) LayouUtil.getDimen("h5_height_none");
            tvTimeSize =  (int) LayouUtil.getDimen("h7_none");
            tvTimeHeight =  (int) LayouUtil.getDimen("h7_height_none");
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

    @Override
    public ViewAdapter getView(ViewData data) {
        TrainListViewData trainListViewData = ((TrainListViewData) data);
        WinLayout.getInstance().vTips = trainListViewData.vTips;
        LogUtil.logd(WinLayout.logTag+ "trainListViewData.vTips:" + trainListViewData.vTips);

        View view = null;

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

        ViewAdapter viewAdapter = new ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.object = TrainListView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(TrainListViewData trainListViewData){
        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(trainListViewData,"train","");

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
        for (int i = 0; i < trainListViewData.ticketList.size(); i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.itemHeightPro);
            View itemView = createItemView(i, trainListViewData.ticketList.get(i), i != SizeConfig.getInstance().getPageTrainCount() - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }
        /*if (trainListViewData.ticketList.size() < SizeConfig.getInstance().getPageTrainCount()){
            LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,SizeConfig.getInstance().getPageTrainCount() - trainListViewData.ticketList.size());
            llContent.addView(linearLayout, layoutParams);
        }*/

        return llLayout;
    }

    private View createViewHalf(TrainListViewData trainListViewData){
        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(trainListViewData,"train","");

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
        for (int i = 0; i < trainListViewData.ticketList.size(); i++) {
            layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT,1);
            if (i != trainListViewData.ticketList.size() - 1){
                layoutParams.rightMargin = (int) LayouUtil.getDimen("x15");
            }
            View itemView = createItemViewHalf(i, trainListViewData.ticketList.get(i), i != SizeConfig.getInstance().getPageTrainCount() - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }
        /*if (trainListViewData.ticketList.size() < SizeConfig.getInstance().getPageTrainCount()){
            LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.getInstance().getPageTrainCount() - trainListViewData.ticketList.size());
            llContent.addView(linearLayout, layoutParams);
        }*/
        int blankCount = SizeConfig.getInstance().getPageTrainCount() - trainListViewData.ticketList.size();
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

    private View createViewNone(TrainListViewData trainListViewData){
        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(trainListViewData,"train","");

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
        for (int i = 0; i < trainListViewData.ticketList.size(); i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.itemHeightPro);
            View itemView = createItemView(i, trainListViewData.ticketList.get(i), i != SizeConfig.getInstance().getPageTrainCount() - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    private View createItemView(int position, TicketListBean ticketListBean, boolean showDivider) {
        Context context = GlobalContext.get();
        RippleView itemView = new RippleView(context);
        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        //itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
       /* itemView.setOnTouchListener(new View.OnTouchListener() {
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
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = countHorMargin;
        itemView.addView(flContent,layoutParams);

        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flContent.addView(llItem, mLayoutParams);

        LinearLayout llTop = new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.bottomMargin = timeVerMargin;
        llItem.addView(llTop, llLayoutParams);

        TextView tvTrainNo = new TextView(GlobalContext.get());
        tvTrainNo.setGravity(Gravity.CENTER_VERTICAL);
        tvTrainNo.setSingleLine();
        tvTrainNo.setEllipsize(TextUtils.TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvTrainNoHeight);
        llLayoutParams.rightMargin = timeHorMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvTrainNo, llLayoutParams);

        TextView tvTime = new TextView(GlobalContext.get());
        tvTime.setSingleLine();
        tvTime.setEllipsize(TextUtils.TruncateAt.END);
        tvTime.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvTime, llLayoutParams);

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

        TextView tvDepartureStation = new TextView(GlobalContext.get());
        tvDepartureStation.setSingleLine();
        tvDepartureStation.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartureStation.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        llDepart.addView(tvDepartureStation, llLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setImageDrawable(LayouUtil.getDrawable("trainlines_to1"));
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

        TextView tvArrivalStation = new TextView(GlobalContext.get());
        tvArrivalStation.setSingleLine();
        tvArrivalStation.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalStation.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        lArrival.addView(tvArrivalStation, lLayoutParams);

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

        TextView tvSeat = new TextView(GlobalContext.get());
        tvSeat.setSingleLine();
        tvSeat.setEllipsize(TextUtils.TruncateAt.END);
        tvSeat.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvSeat, llLayoutParams);

        View divider = new View(GlobalContext.get());
        tvTime.setText(ticketListBean.time);
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        tvTrainNo.setText(ticketListBean.trainNo);
        if(!TextUtils.isEmpty(ticketListBean.departureStation)){
            tvDepartureStation.setText(ticketListBean.departureStation);
        }else{
            tvDepartureStation.setText("????????????");
        }
        if(!TextUtils.isEmpty(ticketListBean.arrivalStation)){
            tvArrivalStation.setText(ticketListBean.arrivalStation);
        }else{
            tvArrivalStation.setText("????????????");
        }
        tvDepartTime.setText(ticketListBean.departureTime);
        tvArrivalTime.setText(ticketListBean.arrivalTime);
        String price = "??" + ticketListBean.minPrice + "???";
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f),0  , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceString.setSpan(new RelativeSizeSpan(0.8f),price.length()-1  , price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);
        boolean isHaveSeat = false;
        for (int i = 0;i < ticketListBean.trainSeats.size();i++){
            TicketListBean.TrainSeatsBean seat = ticketListBean.trainSeats.get(i);
            isHaveSeat = seat.seatName != "??????" && seat.ticketsRemainingNumer > 0;
            if (isHaveSeat){
                break;
            }
        }
        tvSeat.setText(isHaveSeat?"??????":"??????");

        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

        tvSeat.setTextColor(placeColor);
        tvSeat.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
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
        tvTrainNo.setTextColor(placeColor);
        tvTrainNo.setTextSize(TypedValue.COMPLEX_UNIT_PX,tvTrainNoSize);

        return itemView;
    }

    private View createItemViewHalf(int position, TicketListBean ticketListBean, boolean showDivider) {

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

        TextView tvTrainNo = new TextView(GlobalContext.get());
        tvTrainNo.setGravity(Gravity.CENTER_VERTICAL);
        tvTrainNo.setSingleLine();
        tvTrainNo.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvTrainNoHeight);
        lLayoutParams.topMargin = tvTrainNoTopMargin;
        lLayoutParams.bottomMargin = tvTrainNoBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvTrainNo, lLayoutParams);

        TextView tvTime = new TextView(GlobalContext.get());
        tvTime.setSingleLine();
        tvTime.setEllipsize(TextUtils.TruncateAt.END);
        tvTime.setGravity(Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lLayoutParams.bottomMargin = tvTimeBottomMargin;
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvTime, lLayoutParams);

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

        TextView tvDepartureStation = new TextView(GlobalContext.get());
        tvDepartureStation.setSingleLine();
        tvDepartureStation.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartureStation.setId(ViewUtils.generateViewId());
        tvDepartureStation.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams mLLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, placeHeight);
        mLLayoutParams.addRule(RelativeLayout.BELOW,tvDepartTime.getId());
        rlRoute.addView(tvDepartureStation, mLLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setId(ViewUtils.generateViewId());
        view.setImageDrawable(LayouUtil.getDrawable("trainlines_to2"));
        mLLayoutParams = new RelativeLayout.LayoutParams(ivLineWidth, ivLineHeight);
        mLLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        rlRoute.addView(view, mLLayoutParams);

        TextView tvArrivalTime = new TextView(GlobalContext.get());
        tvArrivalTime.setSingleLine();
        tvArrivalTime.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalTime.setId(ViewUtils.generateViewId());
        tvArrivalTime.setGravity(Gravity.CENTER_VERTICAL);
        mLLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, timeHeight);
        mLLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlRoute.addView(tvArrivalTime, mLLayoutParams);

        TextView tvArrivalStation = new TextView(GlobalContext.get());
        tvArrivalStation.setSingleLine();
        tvArrivalStation.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalStation.setId(ViewUtils.generateViewId());
        tvArrivalStation.setGravity(Gravity.CENTER_VERTICAL);
        mLLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, placeHeight);
        mLLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mLLayoutParams.addRule(RelativeLayout.BELOW,tvArrivalTime.getId());
        rlRoute.addView(tvArrivalStation, mLLayoutParams);

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
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,placeHeight);
        lLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lItem.addView(tvSeat, lLayoutParams);

        tvTrainNo.setText(ticketListBean.trainNo);
        tvTime.setText(ticketListBean.time);
        if(!TextUtils.isEmpty(ticketListBean.departureStation)){
            tvDepartureStation.setText(ticketListBean.departureStation);
        }else{
            tvDepartureStation.setText("????????????");
        }
        if(!TextUtils.isEmpty(ticketListBean.arrivalStation)){
            tvArrivalStation.setText(ticketListBean.arrivalStation);
        }else{
            tvArrivalStation.setText("????????????");
        }
        tvDepartTime.setText(ticketListBean.departureTime);
        tvArrivalTime.setText(ticketListBean.arrivalTime);
        String price = "??" + ticketListBean.minPrice + "???";
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f),0  , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceString.setSpan(new RelativeSizeSpan(0.8f),price.length()-1  , price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);
        boolean isHaveSeat = false;
        for (int i = 0;i < ticketListBean.trainSeats.size();i++){
            TicketListBean.TrainSeatsBean seat = ticketListBean.trainSeats.get(i);
            isHaveSeat = seat.seatName != "??????" && seat.ticketsRemainingNumer > 0;
            if (isHaveSeat){
                break;
            }
        }
        tvSeat.setText(isHaveSeat?"??????":"??????");

        tvSeat.setTextColor(timeColor);
        tvSeat.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
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

    private View createItemViewHalfBlank(){

        View itemView = new View(GlobalContext.get());
        itemView.setBackground(LayouUtil.getDrawable("white_range_layout"));

        return itemView;
    }

    private void addGridView(ViewGroup parent, int index, List<TicketListBean.TrainSeatsBean> trainSeats) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        parent.addView(getGridView(index, trainSeats), params);
    }

    private TextView getGridView(int index, List<TicketListBean.TrainSeatsBean> trainSeats) {
        TextView tvSeat = new TextView(GlobalContext.get());
        tvSeat.setTextColor(Color.WHITE);
        tvSeat.setTextSize(LayouUtil.getDimen("m14"));
        tvSeat.setSingleLine();
        tvSeat.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        tvSeat.setId(ViewUtils.generateViewId());
        tvSeat.setGravity(Gravity.RIGHT);
        if (index < trainSeats.size()) {
            TicketListBean.TrainSeatsBean seat = trainSeats.get(index);
            String string = seat.seatName + ":";
            string += seat.ticketsRemainingNumer > 0 ? String.valueOf(seat.ticketsRemainingNumer) : "???";
            tvSeat.setText(string);
        }
        return tvSeat;
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public void snapPage(boolean next) {

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
