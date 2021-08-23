package com.txznet.comm.ui.viewfactory.view.defaults;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.IMovieTimeListView;
import com.txznet.comm.ui.viewfactory.data.MovieTimeListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;

public class DefaultMovieTimeListView extends IMovieTimeListView {


    private static DefaultMovieTimeListView sInstance = new DefaultMovieTimeListView();

    public static DefaultMovieTimeListView getInstance(){
        return sInstance;
    }

    private DefaultMovieTimeListView(){}

    private int tvNumWidth;
    private int tvNumHeight;
    private int tvNumMarginLeft;
    private int dividerHeight;

    @Override
    public void init() {
        super.init();
    }

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        MovieTimeListViewData movieTimeListViewData = (MovieTimeListViewData) data;
        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(movieTimeListViewData);
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);
        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        llContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
        llLayout.addView(llContents,layoutParams);
        for (int i = 0; i < movieTimeListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
            View itemView = createItemView(i,movieTimeListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
            llContents.addView(itemView, layoutParams);
        }
        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = llLayout;
        viewAdapter.isListView = true;
        viewAdapter.object = DefaultMovieTimeListView.getInstance();
        return viewAdapter;
    }

    @SuppressLint("NewApi")
    public View createItemView(int position, MovieTimeListViewData.MovieTimeItem movieTimeItem, boolean showDivider) {
        tvNumWidth = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
        tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
        if (tvNumWidth == 0) {
            tvNumWidth = (int) LayouUtil.getDimen("y44");
        }
        if (tvNumHeight == 0) {
            tvNumHeight = (int) LayouUtil.getDimen("y44");
        }
        tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));

        LinearLayout rlItem = new LinearLayout(GlobalContext.get());
        rlItem.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        itemView.addView(rlItem, mLayoutParams);
        rlItem.setMinimumHeight((int) LayouUtil.getDimen("m80"));

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setIncludeFontPadding(false);
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setPadding(0, 0, 0, 0);
        tvNum.setText(String.valueOf(position + 1));
        TextViewUtil.setTextSize(tvNum,(int)LayouUtil.getDimen("m28"));
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
        LinearLayout.LayoutParams tvLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
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
        TextViewUtil.setTextSize(tvHallName,LayouUtil.getDimen("m23"));
        String sHallName = movieTimeItem.hallName;
        if(sHallName.length() > 10){
            sHallName = sHallName.substring(0,10)+"...";
        }
        tvHallName.setText(sHallName);
        tvHallName.setSingleLine();
        tvHallName.setEllipsize(TextUtils.TruncateAt.END);
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
        divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

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

    }


}
