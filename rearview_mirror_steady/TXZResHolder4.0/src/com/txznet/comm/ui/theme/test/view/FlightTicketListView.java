package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFlightTicketList;
import com.txznet.resholder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * 机票列表
 * <p>
 * 2020-08-10
 *
 * @author xiaolin
 */
public class FlightTicketListView extends IFlightTicketList {
    private static FlightTicketListView instance = new FlightTicketListView();

    private static HashMap<String, Integer> AIR_ICON = new HashMap<String, Integer>() {{
        put("default", R.drawable.air_default);// 必须项
        put("奥凯航空", R.drawable.air_aokai);
        put("澳门航空", R.drawable.air_aomen);
        put("成都航空", R.drawable.air_chengdu);
        put("春秋航空", R.drawable.air_chunqiu);
        put("大连航空", R.drawable.air_dalian);
        put("大新华航空", R.drawable.air_daxinhua);
        put("东北航空", R.drawable.air_dongbei);
        put("东方航空", R.drawable.air_dongfang);
        put("中国东方航空", R.drawable.air_dongfang);
        put("东海航空", R.drawable.air_donghai);
        put("多彩贵州航空", R.drawable.air_duocaiguizhou);
        put("非凡航空", R.drawable.air_feifan);
        put("福州航空", R.drawable.air_fuzhou);
        put("桂林航空", R.drawable.air_guilin);
        put("国泰航空", R.drawable.air_guotai);
        put("海南航空", R.drawable.air_hainan);
        put("河北航空", R.drawable.air_hebei);
        put("华夏航空", R.drawable.air_huaxia);
        put("华信航空", R.drawable.air_huaxin);
        put("吉祥航空", R.drawable.air_jixiang);
        put("江西航空", R.drawable.air_jiangxi);
        put("金鹏航空", R.drawable.air_jinpeng);
        put("九元航空", R.drawable.air_jiuyuan);
        put("昆明航空", R.drawable.air_kunming);
        put("鲲鹏航空", R.drawable.air_kunpeng);
        put("立荣航空", R.drawable.air_lirong);
        put("龙浩航空", R.drawable.air_longhao);
        put("龙江航空", R.drawable.air_longjiang);
        put("南方航空", R.drawable.air_nanfang);
        put("中国南方航空", R.drawable.air_nanfang);
        put("青岛航空", R.drawable.air_qingdao);
        put("瑞丽航空", R.drawable.air_ruili);
        put("厦门航空", R.drawable.air_xiamen);
        put("山东航空", R.drawable.air_shandong);
        put("上海航空", R.drawable.air_shanghai);
        put("深圳航空", R.drawable.air_shenzhen);
        put("首都航空", R.drawable.air_shoudu);
        put("四川航空", R.drawable.air_sichuan);
        put("天津航空", R.drawable.air_tianjing);
        put("乌鲁木齐航空", R.drawable.air_wulumuqi);
        put("西部航空", R.drawable.air_xibu);
        put("西藏航空", R.drawable.air_xizang);
        put("香港航空", R.drawable.air_hongkong);
        put("远东航空", R.drawable.air_yuandong);
        put("云南红土航空", R.drawable.air_yunnanhongtu);
        put("云南祥鹏航空", R.drawable.air_yunnanxiangpeng);
        put("长龙航空", R.drawable.air_changlong);
        put("中国国际航空", R.drawable.air_zhongguoguoji);
        put("中国国航", R.drawable.air_zhongguoguoji);
        put("中国联合航空", R.drawable.air_zhongguolianhe);
        put("中华航空", R.drawable.air_zhonghua);
        put("重庆航空", R.drawable.air_chongqing);
    }};


    private List<View> mItemViews;


    public static FlightTicketListView getInstance() {
        return instance;
    }

    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        LogUtil.logd(WinLayout.logTag + "FlightTicket onUpdateParams: " + styleIndex);

        // XXX: 2019/9/9 这里是不是抛运行时异常会比较好？
        // 齐悟飞机票的UI只做全屏的，由core里面的代码控制当不是全屏的时候不进入这里。
        initNone();
    }

    //无屏布局参数
    private void initNone() {

    }

    @SuppressLint("NewApi")
    @Override
    public ExtViewAdapter getView(ViewData data) {
        QiWuFlightTicketData flightTicketData = (QiWuFlightTicketData) data;
        WinLayout.getInstance().vTips = flightTicketData.vTips;
        LogUtil.logd(WinLayout.logTag + "getView--QiWuFlightTicketData.vTips:" + flightTicketData.vTips);

        View view = createViewNone(flightTicketData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = FlightListView.getInstance();
        return viewAdapter;
    }

    private View createViewNone(QiWuFlightTicketData data) {
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = data.mTitleInfo.maxPage;
        int curPage = data.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        mItemViews = new ArrayList<>();
        for (int i = 0; i < data.count; i++) {
            QiWuFlightTicketData.FlightTicketBean row = data.mFlightTicketBeans.get(i);

            View itemView = createItemView(context, i, row, i != data.count - 1);

            container.addView(itemView);
            mItemViews.add(itemView);
        }

        // 添加空视图填充空间
        int re = SizeConfig.pageFlightCount - data.count;
        for (int i = 0; i < re; i++) {
            View itemView = createItemView(context, i, null, false);
            container.addView(itemView);
        }

        return view;
    }

    private View createItemView(Context context, int index, QiWuFlightTicketData.FlightTicketBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_flight_list_view_item, (ViewGroup) null);
        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvIndex = view.findViewById(R.id.tvIndex);             // 序号
        TextView tvFlightNo = view.findViewById(R.id.tvFlightNo);       // 航班号
        ImageView ivLogo = view.findViewById(R.id.ivLogo);              // 般空公司logo
        TextView tvAirline = view.findViewById(R.id.tvAirline);         // 航空公司
        TextView tvBeginTime = view.findViewById(R.id.tvBeginTime);
        TextView tvBeginAddr = view.findViewById(R.id.tvBeginAddr);
        TextView tvEndTime = view.findViewById(R.id.tvEndTime);
        TextView tvEndAddr = view.findViewById(R.id.tvEndAddr);
        TextView tvSeatType = view.findViewById(R.id.tvSeatType);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvDay = view.findViewById(R.id.tvDay);
        View divider = view.findViewById(R.id.divider);


        tvIndex.setText(String.format(Locale.getDefault(), "%d.", index + 1));
        tvFlightNo.setText(row.flightNo);
        ivLogo.setImageResource(getAirIcon(row.airline));
        tvAirline.setText(row.airline);
        tvBeginTime.setText(row.departureTime);
        tvBeginAddr.setText(row.departAirportName);
        tvEndTime.setText(row.arrivalTime);
        tvEndAddr.setText(row.arrivalAirportName);
        tvSeatType.setText(row.seatCode);
        tvPrice.setText("¥" + row.recommendPrice);

        if (TextUtils.isEmpty(row.addDate) || "0".equals(row.addDate)) {
            tvDay.setVisibility(View.INVISIBLE);
        } else {
            tvDay.setText("+" + row.addDate);
        }

        // 分隔线
        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, index);
        return view;
    }

    /**
     * 根据航空公司名称获得图标
     *
     * @param airName
     * @return
     */
    @DrawableRes
    public static int getAirIcon(String airName) {
        Integer drawableId = AIR_ICON.get(airName);
        if (drawableId == null) {
            drawableId = AIR_ICON.get("default");
        }
        return drawableId;
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public void snapPage(boolean next) {

    }

    @Override
    public void updateItemSelect(int index) {
        for (int i = 0; i < mItemViews.size(); i++) {
            if (i == index) {
                mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
            } else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }
}