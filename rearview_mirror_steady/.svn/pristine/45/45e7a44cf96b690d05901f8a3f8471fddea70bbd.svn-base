package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
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

    private int tvNumWidth;
    private int tvNumHeight;
    private int tvNumMarginLeft;
    private int dividerHeight;

    @Override
    public void init() {
        super.init();
        tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
        tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
        tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));

    }

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        MovieTimeListViewData movieTimeListViewData = (MovieTimeListViewData) data;
        ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(movieTimeListViewData,
                "movie",movieTimeListViewData.getData().get(0).showName);
        WinLayout.getInstance().vTips = movieTimeListViewData.vTips;
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, com.txznet.comm.ui.viewfactory.view.defaults.ListTitleView.getInstance().getTitleHeight());
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
            View itemView = createItemView(i,movieTimeListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
            llContents.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }
        LinearLayout llPager = new PageView(GlobalContext.get(),movieTimeListViewData.mTitleInfo.curPage,movieTimeListViewData.mTitleInfo.maxPage);
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
        llC.addView(llPager,layoutParams);
        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = llLayout;
        viewAdapter.isListView = true;
        viewAdapter.object = MovieTimeListView.getInstance();
        return viewAdapter;
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
        TextViewUtil.setTextSize(tvNum,LayouUtil.getDimen("h2"));
        mLayoutParams = new LinearLayout.LayoutParams(tvNumWidth,tvNumHeight);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        mLayoutParams.leftMargin = tvNumMarginLeft;
        mLayoutParams.rightMargin = tvNumMarginLeft;
        rlItem.addView(tvNum, mLayoutParams);

        LinearLayout lyTimes = new LinearLayout(GlobalContext.get());
        lyTimes.setOrientation(LinearLayout.VERTICAL);
        //lyTimes.setPadding((int) LayouUtil.getDimen("m16"),(int) LayouUtil.getDimen("m19"),0,0);
        mLayoutParams = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT,3);
        // mLayoutParams.leftMargin = (int) LayouUtil.getDimen("m20");
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        rlItem.addView(lyTimes,mLayoutParams);

        TextView tvShowTime = new TextView(GlobalContext.get());
        tvShowTime.setTextColor(Color.WHITE);
        TextViewUtil.setTextSize(tvShowTime,LayouUtil.getDimen("m23"));
        tvShowTime.setSingleLine();
        tvShowTime.setEllipsize(TextUtils.TruncateAt.END);
        tvShowTime.setId(ViewUtils.generateViewId());
        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lyTimes.addView(tvShowTime, tvLayoutParams);
        tvShowTime.setSingleLine();
        String sShowTime = movieTimeItem.showTime.split(" ")[1];
        String[] sShowTimes = sShowTime.split(":");
        if(sShowTimes.length > 2){
            sShowTime = sShowTimes[0]+":"+sShowTimes[1];
        }
        tvShowTime.setText(sShowTime);

        TextView tvCloseTime = new TextView(GlobalContext.get());
        TextViewUtil.setTextColor(tvCloseTime, Color.parseColor("#85868B"));
        TextViewUtil.setTextSize(tvCloseTime,LayouUtil.getDimen("m18"));
        String sCloseTime =  movieTimeItem.closeTime.split(" ")[1];
        String[] sCloseTimes = sCloseTime.split(":");
        if(sCloseTimes.length >= 2){
            sCloseTime = sCloseTimes[0]+":"+sCloseTimes[1];
        }
        sCloseTime = sCloseTime+"散场";
        tvCloseTime.setText(sCloseTime);
        tvCloseTime.setSingleLine();
        tvLayoutParams.topMargin = (int) LayouUtil.getDimen("m2");
        lyTimes.addView(tvCloseTime, tvLayoutParams);

        LinearLayout lyHall = new LinearLayout(GlobalContext.get());
        lyHall.setOrientation(LinearLayout.VERTICAL);
        mLayoutParams = new LinearLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT,5);
        mLayoutParams.leftMargin = (int) LayouUtil.getDimen("m23");
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        rlItem.addView(lyHall, mLayoutParams);

        TextView tvShowVersion = new TextView(GlobalContext.get());
        tvShowVersion.setTextColor(Color.WHITE);
        TextViewUtil.setTextSize(tvShowVersion,LayouUtil.getDimen("m23"));
        tvShowVersion.setSingleLine();
        String showVersion = movieTimeItem.showVersion;
        tvShowVersion.setText(showVersion);
        lyHall.addView(tvShowVersion,tvLayoutParams);

        TextView tvHallName = new TextView(GlobalContext.get());
        TextViewUtil.setTextColor(tvHallName, Color.parseColor("#85868B"));
        TextViewUtil.setTextSize(tvHallName,LayouUtil.getDimen("m18"));
        String sHallName = movieTimeItem.hallName;
        if(sHallName.length() > 10){
            sHallName = sHallName.substring(0,10)+"...";
        }
        tvHallName.setText(sHallName);
        tvHallName.setSingleLine();
        tvHallName.setEllipsize(TextUtils.TruncateAt.END);
        TextViewUtil.setTextSize(tvHallName,LayouUtil.getDimen("m18"));
        tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvLayoutParams.topMargin = (int) LayouUtil.getDimen("m2");
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
        tvPrice.setTextColor(Color.WHITE);
        tvPrice.setTextSize(LayouUtil.getDimen("m27"));
        TextViewUtil.setTextSize(tvPrice,LayouUtil.getDimen("m27"));
        double price = movieTimeItem.unitPrice;
        price = price /100.0;
        String sPrice = "￥"+String.valueOf(price);
        tvPrice.setText(sPrice);
        tvPrice.setSingleLine();

        mRelativeLayout = new  RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mRelativeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mRelativeLayout.addRule(RelativeLayout.CENTER_VERTICAL);
        mRelativeLayout.addRule(RelativeLayout.RIGHT_OF,lyPrices.getId());
        tvPrice.setPadding(0,0,(int) LayouUtil.getDimen("m4"),0);
        prices.addView(tvPrice,mRelativeLayout);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
        itemView.addView(divider, layoutParams);


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
