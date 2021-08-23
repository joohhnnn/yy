package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITrainTicketList;
import com.txznet.resholder.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 火车票列表
 *
 * 2020-08-20
 *
 * @author xiaolin
 */
public class TrainTicketListView extends ITrainTicketList {

    private static TrainTicketListView sInstance = new TrainTicketListView();

    public static TrainTicketListView getInstance() {
        return sInstance;
    }

    private List<View> mItemViews;


    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        QiWuTrainTicketData trainListViewData = ((QiWuTrainTicketData) data);
        WinLayout.getInstance().vTips = trainListViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "trainListViewData.vTips:" + trainListViewData.vTips);

        View view = createViewNone(trainListViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = TrainTicketListView.getInstance();
        return viewAdapter;
    }

    private View createViewNone(QiWuTrainTicketData viewData){
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = viewData.mTitleInfo.maxPage;
        int curPage = viewData.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        ArrayList<QiWuTrainTicketData.TrainTicketBean> dataAry = viewData.mTrainTicketBeans;

        mItemViews = new ArrayList<>();
        for (int i = 0; i < viewData.count; i++) {
            View itemView = createItemView(context, i, dataAry.get(i), i != viewData.count - 1);
            container.addView(itemView);
            mItemViews.add(itemView);
        }

        // 添加空视图填充空间
        int re = SizeConfig.pageTrainCount - viewData.count;
        for (int i = 0; i < re; i++) {
            View itemView = createItemView(context, i, null, false);
            container.addView(itemView);
        }

        return view;
    }

    /**
     * 创建item
     *
     * @param position 序号，从0开始
     * @return item
     */
    private View createItemView(Context context, int position, QiWuTrainTicketData.TrainTicketBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_train_list_view_item, (ViewGroup) null);
        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvPos = view.findViewById(R.id.tvPos);
        TextView tvTrainNum = view.findViewById(R.id.tvTrainNum); // 车次和时长
        TextView tvBeginTime = view.findViewById(R.id.tvBeginTime);
        TextView tvBeginAddr = view.findViewById(R.id.tvBeginAddr);
        TextView tvEndTime = view.findViewById(R.id.tvEndTime);
        TextView tvEndAddr = view.findViewById(R.id.tvEndAddr);
        TextView tvSeatType = view.findViewById(R.id.tvSeatType);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvDay = view.findViewById(R.id.tvDay);
        View divider = view.findViewById(R.id.divider);

        // 时长
        String costTime = formatCostTime(row.costTime);

        tvPos.setText(String.format(Locale.getDefault(), "%d.", position + 1));
        tvTrainNum.setText(row.trainNo + " " + costTime);
        tvBeginTime.setText(row.departureTime);
        tvEndTime.setText(row.arrivalTime);

        if (!TextUtils.isEmpty(row.station)) {
            tvBeginAddr.setText(row.station);
        } else {
            tvBeginAddr.setText("未知名称");
        }
        if (!TextUtils.isEmpty(row.endStation)) {
            tvEndAddr.setText(row.endStation);
        } else {
            tvEndAddr.setText("未知名称");
        }

        tvPrice.setText("¥" + row.recommendPrice);
        tvSeatType.setText(row.recommendSeat);

        if("0".equals(row.addDate)) {
            tvDay.setVisibility(View.GONE);
        } else {
            tvDay.setText("+"+row.addDate);
        }

        // 分隔线
        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, position);

        return view;
    }

    @NonNull
    private String formatCostTime(String costTime) {
        int costTimeMi = Integer.parseInt(costTime) % 60;
        int costTimeHour = Integer.parseInt(costTime) / 60;
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
