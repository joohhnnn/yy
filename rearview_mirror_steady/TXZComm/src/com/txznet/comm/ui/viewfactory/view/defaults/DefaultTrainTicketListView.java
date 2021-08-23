package com.txznet.comm.ui.viewfactory.view.defaults;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITrainTicketList;
import com.txznet.comm.util.TextViewUtil;

public class DefaultTrainTicketListView extends ITrainTicketList {

    private static DefaultTrainTicketListView sInstance = new DefaultTrainTicketListView();

    public static DefaultTrainTicketListView getInstance(){
        return sInstance;
    }

    private int dividerHeight;

    private int tvTrainNoSize;    //火车车次字体大小
    private int tvTimeSize;    //路程时长字体大小
    private int timeSize;    //出发、到达时间字体大小
    private int timeColor;    //出发、到达时间字体颜色
    private int placeSize;    //出发、到达地点（座位）字体大小
    private int placeColor;    //出发、到达地点（座位）字体颜色
    private int tvPriceSize;    //价格字体大小
    private int tvPriceColor;    //价格字体颜色

    private int tvNumWidth;
    private int tvNumHeight;
    private int tvNumMarginLeft;


    @Override
    public void init() {
        super.init();
    }

    /**
     * 初始化界面使用的参数,
     *
     * 可能会被重复调用，所以不用在里面写一些不支持重复调用的代码。
     */
    public void initAttr(){
        tvNumWidth = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
        tvNumHeight = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
        if (tvNumWidth == 0) {
            tvNumWidth = (int) LayouUtil.getDimen("y44");
        }
        if (tvNumHeight == 0) {
            tvNumHeight = (int) LayouUtil.getDimen("y44");
        }
        tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
        timeColor = Color.parseColor("#FFFFFF");
        placeColor = Color.parseColor("#989A9B");
        tvPriceColor = Color.parseColor("#F98006");
        placeSize = (int) LayouUtil.getDimen("m16");
        tvPriceSize = (int) LayouUtil.getDimen("m23");
        timeSize = (int) LayouUtil.getDimen("m23");
        tvTimeSize = (int) LayouUtil.getDimen("m19");
        tvTrainNoSize = (int) LayouUtil.getDimen("m19");
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

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        QiWuTrainTicketData trainListViewData = ((QiWuTrainTicketData) data);
        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(trainListViewData);

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ListTitleView.getInstance().getTitleHeight());
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
        }
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
        llLayout.addView(llContent,layoutParams);
        for (int i = 0; i < trainListViewData.count; i++){
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ConfigUtil.getDisplayLvItemH(false));
            View itemView = createItemView(i,trainListViewData.mTrainTicketBeans.get(i),i != ConfigUtil.getVisbileCount() - 1);
            llContent.addView(itemView, layoutParams);
        }
        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = llLayout;
        viewAdapter.isListView = true;
        viewAdapter.object = DefaultTrainTicketListView.getInstance();
        return viewAdapter;
    }

    @SuppressLint("NewApi")
    public View createItemView(int position, QiWuTrainTicketData.TrainTicketBean trainTicketBean, boolean showDivider) {
        initAttr();
        Context context = GlobalContext.get();
        RippleView itemView = new RippleView(context);
        itemView.setTag(position);
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));
        LinearLayout flContent = new LinearLayout(GlobalContext.get());
        flContent.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        //layoutParams.leftMargin = countHorMargin;
        itemView.addView(flContent,layoutParams);

        LinearLayout.LayoutParams mLayoutParamNum;

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setId(ViewUtils.generateViewId());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setIncludeFontPadding(false);
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setPadding(0, 0, 0, 0);
        tvNum.setText(String.valueOf(position + 1));
        TextViewUtil.setTextSize(tvNum, ViewConfiger.SIZE_REMINDER_INDEX_SIZE1);
        mLayoutParamNum = new LinearLayout.LayoutParams(tvNumWidth,tvNumHeight);
        mLayoutParamNum.gravity = Gravity.CENTER_VERTICAL;
        mLayoutParamNum.leftMargin = tvNumMarginLeft;
        mLayoutParamNum.rightMargin = tvNumMarginLeft;
        flContent.addView(tvNum, mLayoutParamNum);

        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flContent.addView(llItem, mLayoutParams);

        LinearLayout llTop = new LinearLayout(GlobalContext.get());
        llTop.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //llLayoutParams.bottomMargin = timeVerMargin;
        llItem.addView(llTop, llLayoutParams);

        TextView tvTrainNo = new TextView(GlobalContext.get());
        tvTrainNo.setGravity(Gravity.CENTER_VERTICAL);
        tvTrainNo.setSingleLine();
        tvTrainNo.setEllipsize(TextUtils.TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //llLayoutParams.rightMargin = timeHorMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvTrainNo, llLayoutParams);

        TextView tvTime = new TextView(GlobalContext.get());
        tvTime.setSingleLine();
        tvTime.setEllipsize(TextUtils.TruncateAt.END);
        tvTime.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayoutParams.leftMargin =  (int) LayouUtil.getDimen("x8");
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
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //llLayoutParams.bottomMargin = timeVerMargin;
        llDepart.addView(tvDepartTime, llLayoutParams);

        TextView tvDepartureStation = new TextView(GlobalContext.get());
        tvDepartureStation.setSingleLine();
        tvDepartureStation.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartureStation.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llDepart.addView(tvDepartureStation, llLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setImageDrawable(LayouUtil.getDrawable("trainlines_to4"));
        llLayoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("x94"),(int) LayouUtil.getDimen("y32"));
        llLayoutParams.leftMargin = (int) LayouUtil.getDimen("x12");
        llLayoutParams.rightMargin = (int) LayouUtil.getDimen("x12");
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llRoute.addView(view, llLayoutParams);

        LinearLayout lArrival = new LinearLayout(GlobalContext.get());
        lArrival.setOrientation(LinearLayout.VERTICAL);
        lArrival.setGravity(Gravity.RIGHT);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lArrival, llLayoutParams);

        LinearLayout lArrivalTime = new LinearLayout(GlobalContext.get());
        lArrival.addView(lArrivalTime, llLayoutParams);

        TextView tvArrivalTime = new TextView(GlobalContext.get());
        tvArrivalTime.setSingleLine();
        tvArrivalTime.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalTime.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //lLayoutParams.bottomMargin = timeVerMargin;
        lArrivalTime.addView(tvArrivalTime, lLayoutParams);

        TextView addDate = new TextView(GlobalContext.get());
        addDate.setTextColor(Color.RED);
        addDate.setSingleLine();
        addDate.setEllipsize(TextUtils.TruncateAt.END);
        addDate.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        addDate.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) LayouUtil.getDimen("m16"));
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lArrivalTime.addView(addDate, lLayoutParams);
        if(Integer.valueOf(trainTicketBean.addDate) > 0){
            addDate.setText("+"+trainTicketBean.addDate);
        }

        TextView tvArrivalStation = new TextView(GlobalContext.get());
        tvArrivalStation.setSingleLine();
        tvArrivalStation.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalStation.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
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
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //llLayoutParams.bottomMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvPrice, llLayoutParams);

        TextView tvSeat = new TextView(GlobalContext.get());
        tvSeat.setSingleLine();
        tvSeat.setEllipsize(TextUtils.TruncateAt.END);
        tvSeat.setGravity(Gravity.CENTER_VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        lPrice.addView(tvSeat, llLayoutParams);

        View divider = new View(GlobalContext.get());
        int costTimeMi = Integer.valueOf(trainTicketBean.costTime) % 60;
        int costTimeHour = Integer.valueOf(trainTicketBean.costTime) / 60;
        String costTimeText = "";
        if(costTimeHour > 0){
            costTimeText += costTimeHour+"时";
        }
        if(costTimeMi > 0){
            costTimeText += costTimeMi+"分";
        }
        tvTime.setText(costTimeText);
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        tvTrainNo.setText(trainTicketBean.trainNo);
        if(!TextUtils.isEmpty(trainTicketBean.station)){
            tvDepartureStation.setText(trainTicketBean.station);
        }else{
            tvDepartureStation.setText("未知名称");
        }
        if(!TextUtils.isEmpty(trainTicketBean.endStation)){
            tvArrivalStation.setText(trainTicketBean.endStation);
        }else{
            tvArrivalStation.setText("未知名称");
        }
        tvDepartTime.setText(trainTicketBean.departureTime);
        tvArrivalTime.setText(trainTicketBean.arrivalTime);
        String price = "¥" + trainTicketBean.recommendPrice + "起";
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f),0  , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceString.setSpan(new RelativeSizeSpan(0.8f),price.length()-1  , price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);

        tvSeat.setText(trainTicketBean.recommendSeat);

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

}
