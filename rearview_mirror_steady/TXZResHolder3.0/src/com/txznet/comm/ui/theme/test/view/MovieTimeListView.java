package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.IMovieTimeListView;
import com.txznet.comm.ui.viewfactory.data.MovieTimeListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;

import java.util.ArrayList;
import java.util.List;

public class MovieTimeListView extends IMovieTimeListView {


    private static MovieTimeListView sInstance = new MovieTimeListView();

    public static MovieTimeListView getInstance(){
        return sInstance;
    }

    private MovieTimeListView(){}

    private List<View> mItemViews;

    private int tvNumSide;    //序号宽高
    private int tvNumHorMargin;    //序号左右边距
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
    private int tvContentSize;    //内容字体大小
    private int tvContentHeight;    //内容行高
    private int tvContentColor;    //内容字体颜色
    private int centerInterval;    //内容到描述的间距
    private int tvDescSize;    //描述字体大小
    private int tvDescHeight;    //描述行高
    private int tvDescColor;    //描述字体颜色
    private int tvPriceColor;    //价格字体颜色
    private int dividerHeight;

    @Override
    public void init() {
        super.init();
        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvContentColor =  Color.parseColor(LayouUtil.getString("color_main_title"));
        tvDescColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        tvPriceColor = Color.parseColor("#FF8205");    //价格字体颜色
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
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
        if (WinLayout.isVertScreen){
            tvNumSide =  6 * unit;
            tvNumHorMargin = unit;
            tvNumSize  = ViewParamsUtil.h0;
            tvContentSize = ViewParamsUtil.h3;
            tvContentHeight = ViewParamsUtil.h3Height;
            centerInterval = unit;
            tvDescSize = ViewParamsUtil.h5;
            tvDescHeight = ViewParamsUtil.h5Height;
        }else {
            tvNumSide =  6 * unit;
            tvNumHorMargin = 2 * unit;
            tvNumSize  = ViewParamsUtil.h0;
            tvContentSize = ViewParamsUtil.h4;
            tvContentHeight = ViewParamsUtil.h4Height;
            centerInterval = unit;
            tvDescSize = ViewParamsUtil.h6;
            tvDescHeight = ViewParamsUtil.h6Height;
        }
    }

    //半屏布局参数
    private void initHalf() {
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen){
            tvNumSide =  6 * unit;
            tvNumHorMargin = unit;
            tvNumSize  = ViewParamsUtil.h0;
            tvContentSize = ViewParamsUtil.h3;
            tvContentHeight = ViewParamsUtil.h3Height;
            centerInterval = unit;
            tvDescSize = ViewParamsUtil.h5;
            tvDescHeight = ViewParamsUtil.h5Height;
        }else {
            tvNumSide =  6 * unit;
            tvNumHorMargin = 2 * unit;
            tvNumSize  = ViewParamsUtil.h0;
            tvContentSize = ViewParamsUtil.h4;
            tvContentHeight = ViewParamsUtil.h4Height;
            centerInterval = unit;
            tvDescSize = ViewParamsUtil.h6;
            tvDescHeight = ViewParamsUtil.h6Height;
        }
    }

    //无屏布局参数
    private void initNone() {
        int unit = ViewParamsUtil.unit;
        tvNumSide =  6 * unit;
        tvNumHorMargin = 2 * unit;
        tvNumSize  = ViewParamsUtil.h0;
        tvContentSize = ViewParamsUtil.h4;
        tvContentHeight = ViewParamsUtil.h4Height;
        centerInterval = unit;
        tvDescSize = ViewParamsUtil.h6;
        tvDescHeight = ViewParamsUtil.h6Height;
    }

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        MovieTimeListViewData movieTimeListViewData = (MovieTimeListViewData) data;
        WinLayout.getInstance().vTips = movieTimeListViewData.vTips;
        View view;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = createViewFull(movieTimeListViewData);
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(movieTimeListViewData);
                break;
        }

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.object = MovieTimeListView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(MovieTimeListViewData movieTimeListViewData){
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);

        ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(movieTimeListViewData,
                "movie",movieTimeListViewData.getData().get(0).showName);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llC = new LinearLayout(GlobalContext.get());
        llC.setOrientation(LinearLayout.HORIZONTAL);
        llC.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        layoutParams = new  LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageCount * SizeConfig.itemHeight);
        llLayout.addView(llC,layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        llC.addView(llContents,layoutParams);

        mItemViews = new ArrayList<View>();
        for (int i = 0; i < movieTimeListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
            View itemView = createItemView(i,movieTimeListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
            llContents.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        LinearLayout llPager = new PageView(GlobalContext.get(),movieTimeListViewData.mTitleInfo.curPage,movieTimeListViewData.mTitleInfo.maxPage);
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
        llC.addView(llPager,layoutParams);

        return llLayout;
    }

    private View createViewNone(MovieTimeListViewData movieTimeListViewData){
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout llC = new LinearLayout(GlobalContext.get());
        llC.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new  LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        llLayout.addView(llC,layoutParams);

        LinearLayout llPager = new PageView(GlobalContext.get(),movieTimeListViewData.mTitleInfo.curPage,movieTimeListViewData.mTitleInfo.maxPage);
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
        llLayout.addView(llPager,layoutParams);

        ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(movieTimeListViewData,
                "movie",movieTimeListViewData.getData().get(0).showName);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llC.addView(titleViewAdapter.view,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        llC.addView(divider, layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llC.addView(llContents,layoutParams);

        mItemViews = new ArrayList<View>();
        for (int i = 0; i < movieTimeListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
            View itemView = createItemView(i,movieTimeListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
            llContents.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    @SuppressLint("NewApi")
    private View createItemView(int position, MovieTimeListViewData.MovieTimeItem movieTimeItem, boolean showDivider) {

        RippleView itemView = new RippleView(GlobalContext.get());

        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setTag(position);
        llItem.setOnClickListener(com.txznet.comm.ui.viewfactory.view.defaults.ListTitleView.getInstance().getOnItemClickListener());
        llItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_UP:
                        showSelectItem((int)v.getTag());
                        break;
                }
                return false;
            }
        });
        llItem.setMinimumHeight((int) LayouUtil.getDimen("m100"));
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llItem, llLayoutParams);

        LinearLayout rlItem = new LinearLayout(GlobalContext.get());
        rlItem.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llItem.addView(rlItem, mLayoutParams);
        rlItem.setMinimumHeight((int) LayouUtil.getDimen("m80"));

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setIncludeFontPadding(false);
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setPadding(0, 0, 0, 0);
        tvNum.setText(String.valueOf(position + 1));
//        TextViewUtil.setTextSize(tvNum,LayouUtil.getDimen("h2"));
        mLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        mLayoutParams.leftMargin = tvNumHorMargin;
        mLayoutParams.rightMargin = tvNumHorMargin;
        rlItem.addView(tvNum, mLayoutParams);

        LinearLayout lyTimes = new LinearLayout(GlobalContext.get());
        lyTimes.setOrientation(LinearLayout.VERTICAL);
        //lyTimes.setPadding((int) LayouUtil.getDimen("m16"),(int) LayouUtil.getDimen("m19"),0,0);
        mLayoutParams = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT,3);
        // mLayoutParams.leftMargin = (int) LayouUtil.getDimen("m20");
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        rlItem.addView(lyTimes,mLayoutParams);

        TextView tvShowTime = new TextView(GlobalContext.get());
//        tvShowTime.setTextColor(Color.WHITE);
//        TextViewUtil.setTextSize(tvShowTime,LayouUtil.getDimen("m23"));
        tvShowTime.setSingleLine();
        tvShowTime.setGravity(Gravity.CENTER_VERTICAL);
        tvShowTime.setEllipsize(TextUtils.TruncateAt.END);
        tvShowTime.setId(ViewUtils.generateViewId());
        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvContentHeight);
        lyTimes.addView(tvShowTime, tvLayoutParams);
        tvShowTime.setSingleLine();
        String sShowTime = movieTimeItem.showTime.split(" ")[1];
        String[] sShowTimes = sShowTime.split(":");
        if(sShowTimes.length > 2){
            sShowTime = sShowTimes[0]+":"+sShowTimes[1];
        }
        tvShowTime.setText(sShowTime);

        TextView tvCloseTime = new TextView(GlobalContext.get());
        tvCloseTime.setGravity(Gravity.CENTER_VERTICAL);
//        TextViewUtil.setTextColor(tvCloseTime, Color.parseColor("#85868B"));
//        TextViewUtil.setTextSize(tvCloseTime,LayouUtil.getDimen("m18"));
        String sCloseTime =  movieTimeItem.closeTime.split(" ")[1];
        String[] sCloseTimes = sCloseTime.split(":");
        if(sCloseTimes.length >= 2){
            sCloseTime = sCloseTimes[0]+":"+sCloseTimes[1];
        }
        sCloseTime = sCloseTime+"散场";
        tvCloseTime.setText(sCloseTime);
        tvCloseTime.setSingleLine();
        tvLayoutParams.topMargin = centerInterval;
        lyTimes.addView(tvCloseTime, tvLayoutParams);

        LinearLayout lyHall = new LinearLayout(GlobalContext.get());
        lyHall.setOrientation(LinearLayout.VERTICAL);
        mLayoutParams = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT,5);
        mLayoutParams.leftMargin = ViewParamsUtil.unit;
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        rlItem.addView(lyHall, mLayoutParams);

        TextView tvShowVersion = new TextView(GlobalContext.get());
        tvShowVersion.setGravity(Gravity.CENTER_VERTICAL);
//        tvShowVersion.setTextColor(Color.WHITE);
//        TextViewUtil.setTextSize(tvShowVersion,LayouUtil.getDimen("m23"));
        tvShowVersion.setSingleLine();
        String showVersion = movieTimeItem.showVersion;
        tvShowVersion.setText(showVersion);
        lyHall.addView(tvShowVersion,tvLayoutParams);

        TextView tvHallName = new TextView(GlobalContext.get());
        tvHallName.setGravity(Gravity.CENTER_VERTICAL);
//        TextViewUtil.setTextColor(tvHallName, Color.parseColor("#85868B"));
//        TextViewUtil.setTextSize(tvHallName,LayouUtil.getDimen("m18"));
        String sHallName = movieTimeItem.hallName;
        if(sHallName.length() > 10){
            sHallName = sHallName.substring(0,10)+"...";
        }
        tvHallName.setText(sHallName);
        tvHallName.setSingleLine();
        tvHallName.setEllipsize(TextUtils.TruncateAt.END);
        tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLayoutParams.topMargin = centerInterval;
        lyHall.addView(tvHallName,tvLayoutParams);

        LinearLayout lyPrices = new LinearLayout(GlobalContext.get());
        lyPrices.setId(ViewUtils.generateViewId());
        mLayoutParams = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT,3);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        rlItem.addView(lyPrices, mLayoutParams);

        RelativeLayout prices  = new RelativeLayout(GlobalContext.get());
        RelativeLayout.LayoutParams mRelativeLayout = new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mRelativeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        TextView tvPrice = new TextView(GlobalContext.get());
        lyPrices.addView(prices);
//        tvPrice.setTextColor(Color.WHITE);
//        TextViewUtil.setTextSize(tvPrice,LayouUtil.getDimen("m27"));
        double price = movieTimeItem.unitPrice;
        price = price /100.0;
        String sPrice = "￥"+String.valueOf(price) + "起";
        SpannableString priceString = new SpannableString(sPrice);
        priceString.setSpan(new RelativeSizeSpan(0.8f),0  , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceString.setSpan(new RelativeSizeSpan(0.8f),sPrice.length()-1  , sPrice.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);
        tvPrice.setSingleLine();

        mRelativeLayout = new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mRelativeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mRelativeLayout.addRule(RelativeLayout.CENTER_VERTICAL);
        mRelativeLayout.addRule(RelativeLayout.RIGHT_OF,lyPrices.getId());
        mRelativeLayout.rightMargin = 3 * ViewParamsUtil.unit;
        prices.addView(tvPrice,mRelativeLayout);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
        itemView.addView(divider, layoutParams);

        TextViewUtil.setTextSize(tvNum,tvNumSize);
        TextViewUtil.setTextColor(tvNum,tvNumColor);
        TextViewUtil.setTextSize(tvShowTime,tvContentSize);
        TextViewUtil.setTextColor(tvShowTime,tvContentColor);
        TextViewUtil.setTextSize(tvCloseTime,tvDescSize);
        TextViewUtil.setTextColor(tvCloseTime,tvDescColor);
        TextViewUtil.setTextSize(tvShowVersion,tvContentSize);
        TextViewUtil.setTextColor(tvShowVersion,tvContentColor);
        TextViewUtil.setTextSize(tvHallName,tvDescSize);
        TextViewUtil.setTextColor(tvHallName,tvDescColor);
        TextViewUtil.setTextSize(tvPrice,tvContentSize);
        TextViewUtil.setTextColor(tvPrice,tvPriceColor);

        return itemView;
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public void snapPage(boolean next) {

    }

    @Override
    public void updateItemSelect(int selection) {
        showSelectItem(selection);
    }

    private void showSelectItem(int index){
        index = index % SizeConfig.pageCount;
        for (int i = 0;i< mItemViews.size();i++){
            if (i == index){
                mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
            }else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }

}
