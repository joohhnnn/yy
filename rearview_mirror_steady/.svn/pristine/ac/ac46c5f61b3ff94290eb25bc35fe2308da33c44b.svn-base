package com.txznet.comm.ui.viewfactory.view.defaults;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.TrainListViewData;
import com.txznet.comm.ui.viewfactory.data.TrainListViewData.TicketListBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITrainListView;

import java.util.ArrayList;
import java.util.List;

public class DefaultTrainListView extends ITrainListView {

    private static DefaultTrainListView sInstance = new DefaultTrainListView();

    public static DefaultTrainListView getInstance() {
        return sInstance;
    }

    private List<View> mItemViews;

    private int tvNumWidth;
    private int tvNumHeight;
    private int tvNumMarginLeft;
    private int dividerHeight;

    @Override
    public void init() {
        super.init();
        tvNumWidth = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
        tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
        tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
    }

    @Override
    public ViewAdapter getView(ViewData data) {
        TrainListViewData trainListViewData = ((TrainListViewData) data);
        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(trainListViewData);

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
        }
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
        llLayout.addView(llContent,layoutParams);
        llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
        llContent.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_START);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_REPEAT);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mViewStateListener != null) {
                    mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
                }
            }
        });
        mItemViews = new ArrayList<View>();
        for (int i = 0; i < trainListViewData.ticketList.size(); i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ConfigUtil.getDisplayLvItemH(false));
            View itemView = createItemView(i, trainListViewData.ticketList.get(i), i != ConfigUtil.getVisbileCount() - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        ViewAdapter viewAdapter = new ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = llLayout;
        viewAdapter.isListView = true;
        viewAdapter.object = DefaultTrainListView.getInstance();
        return viewAdapter;
    }

    private View createItemView(int position, TicketListBean ticketListBean, boolean showDivider) {
        Context context = GlobalContext.get();
        RippleView itemView = new RippleView(context);
        itemView.setTag(position);
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));

        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                RippleView rippleView = (RippleView) v;
                if (hasFocus) {
                    rippleView.setBackgroundColor(Color.parseColor("#4AA5FA"));
                } else {
                    rippleView.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.HORIZONTAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        int margin = (int) LayouUtil.getDimen("m15");
        layoutParams.setMargins(margin, 0, margin, 0);
        itemView.addView(content, layoutParams);

        LinearLayout child1 = new LinearLayout(context);
        child1.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        content.addView(child1, layoutParams1);
        TextView tvTrainNo = new TextView(context);
        tvTrainNo.setTextColor(Color.WHITE);
        tvTrainNo.setTextSize(LayouUtil.getDimen("m14"));
        tvTrainNo.setSingleLine();
        tvTrainNo.setEllipsize(TextUtils.TruncateAt.END);
        tvTrainNo.setId(ViewUtils.generateViewId());
        tvTrainNo.setText(ticketListBean.trainNo);
        LinearLayout.LayoutParams lp1_tv1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        child1.addView(tvTrainNo, lp1_tv1);
        TextView tvTime = new TextView(context);
        tvTime.setTextColor(Color.parseColor("#80FFFFFF"));
        tvTime.setTextSize(LayouUtil.getDimen("m14"));
        tvTime.setSingleLine();
        tvTime.setEllipsize(TextUtils.TruncateAt.END);
        tvTime.setId(ViewUtils.generateViewId());
        tvTime.setText(ticketListBean.time);
        LinearLayout.LayoutParams lp1_tv2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        child1.addView(tvTime, lp1_tv2);

        LinearLayout child2 = new LinearLayout(context);
        child2.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 3);
        content.addView(child2, layoutParams2);
        TextView tvDepartTime = new TextView(context);
        tvDepartTime.setTextColor(Color.WHITE);
        tvDepartTime.setTextSize(LayouUtil.getDimen("m14"));
        tvDepartTime.setSingleLine();
        tvDepartTime.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartTime.setId(ViewUtils.generateViewId());
        tvDepartTime.setGravity(Gravity.CENTER);
        tvDepartTime.setText(ticketListBean.departureTime);
        LinearLayout.LayoutParams lp2_tv1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        child2.addView(tvDepartTime, lp2_tv1);
        TextView tvArrivalTime = new TextView(context);
        tvArrivalTime.setTextColor(Color.WHITE);
        tvArrivalTime.setTextSize(LayouUtil.getDimen("m14"));
        tvArrivalTime.setSingleLine();
        tvArrivalTime.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalTime.setId(ViewUtils.generateViewId());
        tvArrivalTime.setGravity(Gravity.CENTER);
        tvArrivalTime.setText(ticketListBean.arrivalTime);
        LinearLayout.LayoutParams lp2_tv2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        child2.addView(tvArrivalTime, lp2_tv2);

        LinearLayout child3 = new LinearLayout(context);
        child3.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 5);
        content.addView(child3, layoutParams3);
        TextView tvDepartureStation = new TextView(context);
        tvDepartureStation.setTextColor(Color.WHITE);
        tvDepartureStation.setTextSize(LayouUtil.getDimen("m14"));
        tvDepartureStation.setSingleLine();
        tvDepartureStation.setEllipsize(TextUtils.TruncateAt.END);
        tvDepartureStation.setId(ViewUtils.generateViewId());
        SpannableString departureString = new SpannableString("始 " + ticketListBean.departureStation);
        departureString.setSpan(new RelativeSizeSpan(0.7f), 0 , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        departureString.setSpan(new ForegroundColorSpan(Color.parseColor("#80FFFFFF")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvDepartureStation.setText(departureString);
        LinearLayout.LayoutParams lp3_tv1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        child3.addView(tvDepartureStation, lp3_tv1);
        TextView tvArrivalStation = new TextView(context);
        tvArrivalStation.setTextColor(Color.WHITE);
        tvArrivalStation.setTextSize(LayouUtil.getDimen("m14"));
        tvArrivalStation.setSingleLine();
        tvArrivalStation.setEllipsize(TextUtils.TruncateAt.END);
        tvArrivalStation.setId(ViewUtils.generateViewId());
        SpannableString arrivalString = new SpannableString("终 " + ticketListBean.arrivalStation);
        arrivalString.setSpan(new RelativeSizeSpan(0.7f), 0 , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        arrivalString.setSpan(new ForegroundColorSpan(Color.parseColor("#80FFFFFF")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvArrivalStation.setText(arrivalString);
        LinearLayout.LayoutParams lp3_tv2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        child3.addView(tvArrivalStation, lp3_tv2);

        TextView child4 = new TextView(context);
        child4.setTextColor(Color.parseColor("#FF00B9FF"));
        child4.setTextSize(LayouUtil.getDimen("m16"));
        child4.setSingleLine();
        child4.setEllipsize(TextUtils.TruncateAt.END);
        child4.setId(ViewUtils.generateViewId());
        child4.setGravity(Gravity.RIGHT);
        String price = "¥" + ticketListBean.minPrice + " 起";
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.6f), price.length() -1 , price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        child4.setText(priceString);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        content.addView(child4, layoutParams4);


        LinearLayout child5 = new LinearLayout(context);
        child5.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        layoutParams5.leftMargin = (int) LayouUtil.getDimen("m10");
        content.addView(child5, layoutParams5);
        addGridView(child5, 0, ticketListBean.trainSeats);
        addGridView(child5, 2, ticketListBean.trainSeats);

        LinearLayout child6 = new LinearLayout(context);
        child6.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4);
        content.addView(child6, layoutParams6);
        addGridView(child6, 1, ticketListBean.trainSeats);
        addGridView(child6, 3, ticketListBean.trainSeats);

        View divider = new View(context);
        divider.setVisibility(View.GONE);
        divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
        itemView.addView(divider, layoutParams);
        return itemView;
    }

    private void addGridView(ViewGroup parent, int index, List<TicketListBean.TrainSeatsBean> trainSeats) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        parent.addView(getGridView(index, trainSeats), params);
    }

    private TextView getGridView(int index, List<TicketListBean.TrainSeatsBean> trainSeats) {
        TextView tvSeat = new TextView(GlobalContext.get());
        tvSeat.setTextColor(Color.WHITE);
        tvSeat.setTextSize(LayouUtil.getDimen("m14"));
        tvSeat.setSingleLine();
        tvSeat.setEllipsize(TextUtils.TruncateAt.MIDDLE);
        tvSeat.setId(ViewUtils.generateViewId());
        tvSeat.setGravity(Gravity.RIGHT);
        if (index < trainSeats.size()) {
            TicketListBean.TrainSeatsBean seat = trainSeats.get(index);
            String string = seat.seatName + ":";
            string += seat.ticketsRemainingNumer > 0 ? String.valueOf(seat.ticketsRemainingNumer) : "无";
            tvSeat.setText(string);
        }
        return tvSeat;
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
