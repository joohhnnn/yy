package com.txznet.comm.ui.viewfactory.view.defaults;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFlightTicketList;
import com.txznet.comm.util.TextViewUtil;

import java.util.HashMap;

public class DefaultFlightTicketListView extends IFlightTicketList {

    private static DefaultFlightTicketListView sInstance = new DefaultFlightTicketListView();

    public static DefaultFlightTicketListView getInstance(){
        return sInstance;
    }

    private int dividerHeight;

    private int countHorMargin;    //内容左右边距
    private int timeVerMargin;    //出发、到达时间上下边距
    private int timeHorMargin;    //出发、到达时间左右边距
    private int tvflightNoSize;    //航班编号字体大小
    private int ivLogoSize;    //航空公司logo大小
    private int tvflightNameSize;    //航空公司名字字体大小
    private int timeSize;    //出发、到达时间字体大小
    private int timeColor;    //出发、到达时间字体颜色
    private int tvPriceSize;    //价格字体大小
    private int tvPriceColor;    //价格字体颜色
    private int placeColor;    //出发、到达地点（座位）字体颜色
    private int placeSize;    //出发、到达地点（座位）字体大小
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
        countHorMargin = (int) LayouUtil.getDimen("x1");
        timeVerMargin = (int) LayouUtil.getDimen("y4");
        timeHorMargin = (int) LayouUtil.getDimen("x8");
        tvflightNoSize = (int) LayouUtil.getDimen("m17");
        ivLogoSize = (int) LayouUtil.getDimen("m15");
        tvflightNameSize = (int) LayouUtil.getDimen("m16");
        timeSize = (int) LayouUtil.getDimen("m23");
        timeColor = Color.parseColor("#FFFFFF");
        tvPriceSize = (int) LayouUtil.getDimen("m23");
        tvPriceColor = Color.parseColor("#F98006");
        placeColor = Color.parseColor("#989A9B");
        placeSize = (int) LayouUtil.getDimen("m16");
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
        QiWuFlightTicketData flightListViewData = (QiWuFlightTicketData) data;
        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(flightListViewData);
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
        for (int i = 0; i < flightListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ConfigUtil.getDisplayLvItemH(false));
            View itemView = createItemView(i,flightListViewData.mFlightTicketBeans.get(i), i != ConfigUtil.getVisbileCount() - 1);
            llContent.addView(itemView, layoutParams);
        }
        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = llLayout;
        viewAdapter.isListView = true;
        viewAdapter.object = DefaultFlightTicketListView.getInstance();
        return viewAdapter;
    }

    @SuppressLint("NewApi")
    public View createItemView(int position, QiWuFlightTicketData.FlightTicketBean flightTicketBean, boolean showDivider) {
        initAttr();
        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));
        LinearLayout flContent = new LinearLayout(GlobalContext.get());
        flContent.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.leftMargin = countHorMargin;
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
        llLayoutParams.topMargin = timeVerMargin;
        llItem.addView(llTop, llLayoutParams);

        TextView tvflightNo = new TextView(GlobalContext.get());
        tvflightNo.setGravity(Gravity.CENTER_VERTICAL);
        tvflightNo.setSingleLine();
        tvflightNo.setEllipsize(TextUtils.TruncateAt.END);

        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llLayoutParams.rightMargin = timeHorMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTop.addView(tvflightNo, llLayoutParams);

        TextView tvAirline;
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
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llDepart.addView(tvDepartTime, llLayoutParams);

        TextView tvDepartName = new TextView(GlobalContext.get());
        tvDepartName.setSingleLine();
        tvDepartName.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartName.setGravity(Gravity.CENTER_VERTICAL);

        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llDepart.addView(tvDepartName, llLayoutParams);

        ImageView view = new ImageView(GlobalContext.get());
        view.setImageDrawable(LayouUtil.getDrawable("airlines_to4"));

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
        lArrivalTime.addView(tvArrivalTime, lLayoutParams);

        TextView addDate = new TextView(GlobalContext.get());
        addDate.setTextColor(Color.RED);
        addDate.setSingleLine();
        addDate.setEllipsize(TextUtils.TruncateAt.END);
        addDate.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        addDate.setTextSize(TypedValue.COMPLEX_UNIT_PX,(int) LayouUtil.getDimen("m16"));
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lArrivalTime.addView(addDate, lLayoutParams);
        if(Integer.valueOf(flightTicketBean.addDate) > 0){
            addDate.setText("+"+flightTicketBean.addDate);
        }

        TextView tvArrivalName = new TextView(GlobalContext.get());
        tvArrivalName.setSingleLine();
        tvArrivalName.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalName.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
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
		/*lLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		lPrice.addView(tvPrice, lLayoutParams);*/
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
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
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        tvAirline.setText(" "+flightTicketBean.airline);
        String airlineIcon = mMap.get(flightTicketBean.airline);
        if(airlineIcon == null){
            airlineIcon = "air_default";
        }
        Drawable drawable = LayouUtil.getDrawable(airlineIcon);
        //drawable.setBounds(0,0,(int)LayouUtil.getDimen("m15"),(int)LayouUtil.getDimen("m15"));
        drawable.setBounds(0,0,ivLogoSize,ivLogoSize);
        tvAirline.setCompoundDrawables(drawable,null,null,null);
        tvflightNo.setText(flightTicketBean.flightNo);
        if(!TextUtils.isEmpty(flightTicketBean.departAirportName)){
            tvDepartName.setText(flightTicketBean.departAirportName);
        }else{
            tvDepartName.setText("未知名称");
        }
        if(!TextUtils.isEmpty(flightTicketBean.arrivalAirportName)){
            tvArrivalName.setText(flightTicketBean.arrivalAirportName);
        }else{
            tvArrivalName.setText("未知名称");
        }
        if (!TextUtils.isEmpty(flightTicketBean.departureTime)){
            tvDepartTime.setText(flightTicketBean.departureTime);
        }
        if (!TextUtils.isEmpty(flightTicketBean.arrivalTime)){
            tvArrivalTime.setText(flightTicketBean.arrivalTime);
        }
        String price = "¥" + flightTicketBean.recommendPrice + "起";
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.8f),0  , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        priceString.setSpan(new RelativeSizeSpan(0.8f),price.length()-1  , price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);
        tvSeat.setText(flightTicketBean.recommendSeat);
        tvSeat.setTextColor(placeColor);
        tvSeat.setTextSize(TypedValue.COMPLEX_UNIT_PX,placeSize);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

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

    private static HashMap<String, String> mMap = new HashMap();

    static {
        mMap.put("奥凯航空", "air_aokai");
        mMap.put("澳门航空", "air_aomen");
        mMap.put("成都航空", "air_chengdu");
        mMap.put("春秋航空", "air_chunqiu");
        mMap.put("大连航空", "air_dalian");
        mMap.put("大新华航空", "air_daxinhua");
        mMap.put("东北航空", "air_dongbei");
        mMap.put("中国东方航空", "air_dongfang");
        mMap.put("东海航空", "air_donghai");
        mMap.put("多彩贵州航空", "air_duocaiguizhou");
        mMap.put("非凡航空", "air_feifan");
        mMap.put("福州航空", "air_fuzhou");
        mMap.put("桂林航空", "air_guilin");
        mMap.put("国泰航空", "air_guotai");
        mMap.put("海南航空", "air_hainan");
        mMap.put("河北航空", "air_hebei");
        mMap.put("华夏航空", "air_huaxia");
        mMap.put("华信航空", "air_huaxin");
        mMap.put("吉祥航空", "air_jixiang");
        mMap.put("江西航空", "air_jiangxi");
        mMap.put("金鹏航空", "air_jinpeng");
        mMap.put("九元航空", "air_jiuyuan");
        mMap.put("昆明航空", "air_kunming");
        mMap.put("鲲鹏航空", "air_kunpeng");
        mMap.put("立荣航空", "air_lirong");
        mMap.put("龙浩航空", "air_longhao");
        mMap.put("龙江航空", "air_longjiang");
        mMap.put("中国南方航空", "air_nanfang");
        mMap.put("青岛航空", "air_qingdao");
        mMap.put("瑞丽航空", "air_ruili");
        mMap.put("厦门航空", "air_xiamen");
        mMap.put("山东航空", "air_shandong");
        mMap.put("上海航空", "air_shanghai");
        mMap.put("深圳航空", "air_shenzhen");
        mMap.put("首都航空", "air_shoudu");
        mMap.put("四川航空", "air_sichuan");
        mMap.put("天津航空", "air_tianjing");
        mMap.put("乌鲁木齐航空", "air_wulumuqi");
        mMap.put("西部航空", "air_xibu");
        mMap.put("西藏航空", "air_xizang");
        mMap.put("香港航空", "air_hongkong");
        mMap.put("远东航空", "air_yuandong");
        mMap.put("云南红土航空", "air_yunnanhongtu");
        mMap.put("云南祥鹏航空", "air_yunnanxiangpeng");
        mMap.put("长龙航空", "air_changlong");
        mMap.put("中国国际航空", "air_zhongguoguoji");
        mMap.put("中国国航", "air_zhongguoguoji");
        mMap.put("中国联合航空", "air_zhongguolianhe");
        mMap.put("中华航空", "air_zhonghua");
        mMap.put("重庆航空", "air_chongqing");
    }

}
