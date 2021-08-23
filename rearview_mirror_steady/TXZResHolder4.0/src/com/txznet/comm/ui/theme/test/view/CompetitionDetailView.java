package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CompetitionDetailViewData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICompetitionDetailView;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.resholder.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 赛事
 * <p>
 * 2020-08-18 11：12
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class CompetitionDetailView extends ICompetitionDetailView {

    private static final String[] WEEK = {
            "周日", "周一", "周二", "周三", "周四", "周五", "周六",
    };

    private static CompetitionDetailView sInstance = new CompetitionDetailView();
    private CompetitionDetailView() {
    }

    public static CompetitionDetailView getInstance() {
        return sInstance;
    }

    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        CompetitionDetailViewData detailViewData = (CompetitionDetailViewData) data;
        WinLayout.getInstance().vTips = detailViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "ChatShockViewData.vTips: " + detailViewData.vTips);

        View view = createView(detailViewData);

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = CompetitionDetailView.getInstance();
        return adapter;
    }

    private View createView(CompetitionDetailViewData viewData) {
        CompetitionViewData.CompetitionData.CompetitionBean row = viewData.mCompetitionBean;
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.competitio_view_detail, (ViewGroup) null);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDate = view.findViewById(R.id.tvDate);
        ImageView ivHomeTeamLogo = view.findViewById(R.id.ivHomeTeamLogo);
        ImageView ivAwayTeamLogo = view.findViewById(R.id.ivAwayTeamLogo);
        TextView tvHomeTeamName = view.findViewById(R.id.tvHomeTeamName);
        TextView tvAwayTeamName = view.findViewById(R.id.tvAwayTeamName);
        TextView tvGoal = view.findViewById(R.id.tvGoal);       // 比分或时间
        TextView tvState = view.findViewById(R.id.tvState);     // 比赛状态，未开始、进行中、已结束


        String title = "赛事";
        if (viewData.mCompetitionBean.mSportsType == CompetitionViewData.CompetitionData.TYPE_SPORTS_NBA) {
            title = "NBA赛事";
        } else if (viewData.mCompetitionBean.mSportsType == CompetitionViewData.CompetitionData.TYPE_SPORTS_FOOTBALL) {
            title = "足球比赛";
        }
        tvTitle.setText(title);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(new Date(row.mStartTimeStamp * 1000L));
        String strStartTime = new SimpleDateFormat("yyyy年MM月dd日 ", Locale.getDefault()).format(startCalendar.getTime())
                + WEEK[startCalendar.get(Calendar.DAY_OF_WEEK)-1];
        tvDate.setText(strStartTime);
        loadDrawableByUrl(ivHomeTeamLogo, row.mHomeTeam.mLogo);
        loadDrawableByUrl(ivAwayTeamLogo, row.mAwayTeam.mLogo);
        tvHomeTeamName.setText(row.mHomeTeam.mName);
        tvAwayTeamName.setText(row.mAwayTeam.mName);

        if (TextUtils.equals("未开始", row.mPeriod)) {
            tvGoal.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(startCalendar.getTime()));
            tvState.setText("未开始");
            tvState.setTextColor(context.getResources().getColor(R.color.textColorSecondary));
            tvState.setBackgroundResource(R.drawable.competition_not_started_detail);
        } else {
            tvGoal.setText(String.format(Locale.getDefault(), "%d-%d",
                    row.mHomeTeam.mGoal, row.mAwayTeam.mGoal));// 比分
            tvState.setText(row.mPeriod);// 比赛状态
            if (TextUtils.equals("进行中", row.mPeriod)) {
                tvState.setTextColor(context.getResources().getColor(R.color.textColorPrimary));
                tvState.setBackgroundResource(R.drawable.competition_under_way_detail);
            } else if (TextUtils.equals("已结束", row.mPeriod)) {
                tvState.setTextColor(context.getResources().getColor(R.color.textColorSecondary));
                tvState.setBackgroundResource(R.drawable.competition_ended_detail);
            } else {
                tvState.setTextColor(context.getResources().getColor(R.color.textColorSecondary));
                tvState.setBackground(null);
            }
        }

        return view;
    }

    private void loadDrawableByUrl(final ImageView ivHead, String uri) {
        ImageLoaderInitialize.ImageLoaderImpl.getInstance().displayImage(uri, ivHead, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                if (loadedImage != null) {
                    ((ImageView) view).setImageBitmap(loadedImage);
                    view.setVisibility(View.VISIBLE);
                }
            }
        });
    }


}
