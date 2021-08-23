package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
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
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory;
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

    private int tvNumWidth; // 序号的宽
    private int tvNumHeight; // 序号的高
    private int tvNumMarginLeft; // 序号的左外边距
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

    public static FlightTicketListView getInstance() {
        return instance;
    }

    @Override
    public void init() {
        super.init();
        tvNumWidth = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
        tvNumHeight = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
        tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager
                .LIST_ITEM_TXTNUM_MARGINLEFT);
        dividerHeight = (int) Math
                .ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));

        tvPriceColor = Color.parseColor(LayouUtil.getString("color_flight_price"));
        placeColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        timeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        LogUtil.logd(WinLayout.logTag + "FlightTicket onUpdateParams: " + styleIndex);

        // XXX: 2019/9/9 这里是不是抛运行时异常会比较好？
        // 齐悟飞机票的UI只做全屏的，由core里面的代码控制当不是全屏的时候不进入这里。
        initFull();
    }

    //全屏布局参数
    private void initFull() {
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
            ivLineWidth =12 * unit;
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
            ivLineWidth = 12 * unit;
            ivLineHeight = 4 * unit;
            tvPriceSize = (int) LayouUtil.getDimen("h3");
            tvPriceHeight = (int) LayouUtil.getDimen("h3_height");
        }
    }

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        QiWuFlightTicketData flightTicketData = (QiWuFlightTicketData) data;
        WinLayout.getInstance().vTips = flightTicketData.vTips;
        LogUtil.logd(
                WinLayout.logTag + "getView--QiWuFlightTicketData.vTips:" + flightTicketData.vTips);

        View view;
        view = createViewFull(flightTicketData);

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.object = FlightTicketListView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(QiWuFlightTicketData flightTicketData) {
        ViewFactory.ViewAdapter
                titleViewAdapter =
                ListTitleView.getInstance().getView(flightTicketData, "flight", "");
        LogUtil.logd(WinLayout.logTag + "flightTicketData.mTitleInfo.titlefix: " +
                flightTicketData.mTitleInfo.titlefix);
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
        LogUtil.logd(WinLayout.logTag + "flightTicketData.count:" + flightTicketData.count);
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

    @SuppressLint({"NewApi", "RtlHardcoded"})
    private View createItemView(int position, QiWuFlightTicketData.FlightTicketBean flightBean,
            boolean showDivider) {
        LogUtil.logd(WinLayout.logTag + "FlightTicketBean: " + flightBean.departureTime + "--" +
                flightBean.arrivalTime + "--" + "--" + flightBean.addDate);
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
                new LinearLayout.LayoutParams(tvNumWidth,
                        tvNumHeight);
        tvNumLayoutParams.leftMargin = tvNumMarginLeft;
        tvNumLayoutParams.rightMargin = tvNumMarginLeft;
        tvNum.setText(String.valueOf(position + 1));
        TextViewUtil.setTextSize(tvNum, (int) LayouUtil.getDimen("h2"));
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
        view.setImageDrawable(LayouUtil.getDrawable("airlines_to4"));
        llLayoutParams = new LinearLayout.LayoutParams(ivLineWidth, ivLineHeight);
        llLayoutParams.leftMargin = timeVerMargin;
        llLayoutParams.rightMargin = timeVerMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llRoute.addView(view, llLayoutParams);

        LinearLayout lArrival = new LinearLayout(GlobalContext.get());
        lArrival.setOrientation(LinearLayout.VERTICAL);
        lArrival.setGravity(Gravity.RIGHT);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llRoute.addView(lArrival, llLayoutParams);

        LinearLayout lArrivalTime = new LinearLayout(GlobalContext.get());
        lArrival.addView(lArrivalTime, llLayoutParams);

        TextView tvArrivalTime = new TextView(GlobalContext.get());
        tvArrivalTime.setSingleLine();
        tvArrivalTime.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalTime.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lLayoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, timeHeight);
        lLayoutParams.bottomMargin = timeVerMargin;
        lArrivalTime.addView(tvArrivalTime, lLayoutParams);

        TextView addDate = new TextView(GlobalContext.get());
        addDate.setTextColor(Color.RED);
        addDate.setSingleLine();
        addDate.setEllipsize(TextUtils.TruncateAt.END);
        addDate.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        addDate.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) LayouUtil.getDimen("m16"));
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lArrivalTime.addView(addDate, lLayoutParams);
        if(Integer.valueOf(flightBean.addDate) > 0){
            addDate.setText("+"+flightBean.addDate);
        }

        TextView tvArrivalName = new TextView(GlobalContext.get());
        tvArrivalName.setSingleLine();
        tvArrivalName.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
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
        LogUtil.logd(WinLayout.logTag + "flight updateItemSelect " + index);

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