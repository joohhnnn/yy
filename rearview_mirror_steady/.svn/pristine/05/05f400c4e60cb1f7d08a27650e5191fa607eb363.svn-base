package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.data.FlightListViewData;
import com.txznet.comm.ui.viewfactory.data.FlightListViewData.FlightItemBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFlightListView;
import com.txznet.resholder.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class FlightListView extends IFlightListView {
    private static FlightListView instance = new FlightListView();

    private List<View> mItemViews;

    public static FlightListView getInstance() {
        return instance;
    }

    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        LogUtil.logd(WinLayout.logTag + "flight onUpdateParams: " + styleIndex);

    }

    @SuppressLint("NewApi")
    @Override
    public ExtViewAdapter getView(ViewData data) {
        FlightListViewData flightListViewData = (FlightListViewData) data;
        WinLayout.getInstance().vTips = flightListViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "getView--flightListViewData.vTips:" + flightListViewData.vTips);
        LogUtil.logd(WinLayout.logTag + "getView--data:" + JSONObject.toJSONString(data));

        View view = createViewNone(flightListViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = FlightListView.getInstance();
        return viewAdapter;
    }

    private View createViewNone(FlightListViewData data) {
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = data.mTitleInfo.maxPage;
        int curPage = data.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        mItemViews = new ArrayList<>();
        for (int i = 0; i < data.count; i++) {
            FlightItemBean row = data.getData().get(i);

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

    @SuppressLint("NewApi")
    private View createItemView(Context context, int position, FlightItemBean row, boolean showDivider) {

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


		tvIndex.setText(String.format(Locale.getDefault(), "%d.", position + 1));
		tvFlightNo.setText(row.flightNo);
		ivLogo.setImageResource(FlightTicketListView.getAirIcon(row.airline));
		tvAirline.setText(row.airline);
		tvBeginTime.setText(row.departTimeHm);
		tvBeginAddr.setText(row.departAirportName);
		tvEndTime.setText(row.arrivalTimeHm);
		tvEndAddr.setText(row.arrivalAirportName);
		tvSeatType.setText("");// 座位
		tvSeatType.setVisibility(View.GONE);
		tvPrice.setText("¥" + row.economyCabinPrice);

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
		ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, position);
		return view;
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
