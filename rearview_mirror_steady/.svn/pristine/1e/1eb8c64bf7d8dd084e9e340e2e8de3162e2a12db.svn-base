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
import android.view.LayoutInflater;
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
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CallListViewData;
import com.txznet.comm.ui.viewfactory.data.TrainListViewData;
import com.txznet.comm.ui.viewfactory.data.TrainListViewData.TicketListBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITrainListView;
import com.txznet.resholder.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 好像没有用
 */
public class TrainListView extends ITrainListView {

    private static TrainListView sInstance = new TrainListView();

    public static TrainListView getInstance() {
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
    public ViewAdapter getView(ViewData data) {
        TrainListViewData trainListViewData = ((TrainListViewData) data);
        WinLayout.getInstance().vTips = trainListViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "trainListViewData.vTips:" + trainListViewData.vTips);

        View view = createViewNone(trainListViewData);

        ViewAdapter viewAdapter = new ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.object = TrainListView.getInstance();
        return viewAdapter;
    }

    private View createViewNone(TrainListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = viewData.mTitleInfo.maxPage;
        int curPage = viewData.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        List<TrainListViewData.TicketListBean> dataAry = viewData.ticketList;

        mItemViews = new ArrayList<>();
        for (int i = 0; i < viewData.count; i++) {
            View itemView = createItemView(context, i, dataAry.get(i), i != viewData.count - 1);
            container.addView(itemView);
            mItemViews.add(itemView);
        }

        // 添加空视图填充空间
        int re = SizeConfig.pagePoiCount - viewData.count;
        for (int i = 0; i < re; i++) {
            View itemView = createItemView(context, i, null, false);
            container.addView(itemView);
        }

        return view;
    }

    private View createItemView(Context context, int position, TicketListBean row, boolean showDivider) {
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
        View divider = view.findViewById(R.id.divider);

        tvPos.setText(String.format(Locale.getDefault(), "%d.", position + 1));
        tvTrainNum.setText(row.trainNo);
        tvBeginTime.setText(row.departureTime);
        tvEndTime.setText(row.arrivalTime);
        if (!TextUtils.isEmpty(row.departureStation)) {
            tvBeginAddr.setText(row.departureStation);
        } else {
            tvBeginAddr.setText("未知名称");
        }
        if (!TextUtils.isEmpty(row.arrivalStation)) {
            tvEndAddr.setText(row.arrivalStation);
        } else {
            tvEndAddr.setText("未知名称");
        }
        tvPrice.setText("¥" + row.minPrice);
        boolean isHaveSeat = false;
        for (int i = 0; i < row.trainSeats.size(); i++) {
            TicketListBean.TrainSeatsBean seat = row.trainSeats.get(i);
            isHaveSeat = seat.seatName != "无座" && seat.ticketsRemainingNumer > 0;
            if (isHaveSeat) {
                break;
            }
        }
        tvSeatType.setText(isHaveSeat ? "有座" : "无座");


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
        LogUtil.logd(WinLayout.logTag + "train updateItemSelect " + index);
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
