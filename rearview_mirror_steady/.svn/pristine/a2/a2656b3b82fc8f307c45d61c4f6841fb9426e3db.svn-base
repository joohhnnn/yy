package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CompetitionDetailViewData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICompetitionDetailView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ASUS User
 */
@SuppressLint("NewApi")
public class CompetitionDetailView extends ICompetitionDetailView {

    private static CompetitionDetailView sInstance = new CompetitionDetailView();
    private int iconTeamWidth;
    private int iconTeamHeight;
    private int unit;
    private int mInfoHeight;
    private int llCompetitionTopMargin;
    private int mH1;
    private int mH4;
    private int mH5;
    private int mH7;

    private CompetitionDetailView() {
    }

    public static CompetitionDetailView getInstance() {
        return sInstance;
    }

    private int countHeight;    //内容高度
    private int detailVerMargin;    //详细内容上下间距


    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        unit = ViewParamsUtil.unit;
        switch (styleIndex) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                initFull();
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                initHalf();
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                initNone();
                break;
            default:
                break;
        }
    }

    //全屏布局参数
    private void initFull() {
        if (WinLayout.isVertScreen) {
            countHeight = 46 * unit;
            detailVerMargin = 8 * unit;
            mH1 = ViewParamsUtil.h1;
            mH4 = ViewParamsUtil.h4;
            mH5 = ViewParamsUtil.h5;
            mH7 = ViewParamsUtil.h7;
        } else {
            countHeight = SizeConfig.pagePoiCount * SizeConfig.itemHeight;
            detailVerMargin = 8 * unit;
            mH1 = ViewParamsUtil.h1;
            mH4 = ViewParamsUtil.h4;
            mH5 = ViewParamsUtil.h5;
            mH7 = ViewParamsUtil.h7;
        }

        iconTeamWidth = 12 * unit;
        iconTeamHeight = 12 * unit;
        mInfoHeight = 12 * unit;
        llCompetitionTopMargin = 3 * unit;

    }

    //半屏布局参数
    private void initHalf() {
        if (WinLayout.isVertScreen) {
            //countHeight = SizeConfig.pagePoiCount * SizeConfig.itemHeight;
            countHeight = 46 * unit;
            detailVerMargin = 4 * unit;
            mH1 = ViewParamsUtil.h1;
            mH4 = ViewParamsUtil.h4;
            mH5 = ViewParamsUtil.h5;
            mH7 = ViewParamsUtil.h7;
        } else {
            countHeight = SizeConfig.pagePoiCount * SizeConfig.itemHeight;
            if (SizeConfig.screenHeight < 464) {
                detailVerMargin = 2 * unit;
            } else {
                detailVerMargin = 4 * unit;
            }
            mH1 = ViewParamsUtil.h1;
            mH4 = ViewParamsUtil.h4;
            mH5 = ViewParamsUtil.h5;
            mH7 = ViewParamsUtil.h7;
        }
        iconTeamWidth = 12 * unit;
        iconTeamHeight = 12 * unit;
        mInfoHeight = 12 * unit;
        llCompetitionTopMargin = 3 * unit;
    }

    //无屏布局参数
    private void initNone() {
        if (WinLayout.isVertScreen) {
            detailVerMargin = (int) LayouUtil.getDimen("x41");
            mH1 = ViewParamsUtil.h1;
            mH4 = ViewParamsUtil.h4;
            mH5 = ViewParamsUtil.h5;
            mH7 = ViewParamsUtil.h7;
        } else {
            detailVerMargin = 2 * unit;
            mH1 = ViewParamsUtil.h1;
            mH4 = ViewParamsUtil.h4;
            mH5 = ViewParamsUtil.h5;
            mH7 = ViewParamsUtil.h7;
        }
        countHeight = LinearLayout.LayoutParams.MATCH_PARENT;
        iconTeamWidth = (int) (9.6 * unit);
        iconTeamHeight = (int) (9.6 * unit);
        mInfoHeight = (int) (9.6 * unit);
        llCompetitionTopMargin = unit;
    }


    @Override
    public ViewAdapter getView(ViewData data) {
        CompetitionDetailViewData detailViewData = (CompetitionDetailViewData) data;
        WinLayout.getInstance().vTips = detailViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "ChatShockViewData.vTips: " + detailViewData.vTips);

        View view = createViewFull(detailViewData);

        ViewAdapter adapter = new ViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(data.getType());
        adapter.object = CompetitionDetailView.getInstance();
        return adapter;
    }

    private View createViewFull(CompetitionDetailViewData viewData) {
        LinearLayout mLayout = new LinearLayout(GlobalContext.get());
        mLayout.setGravity(Gravity.CENTER_VERTICAL);
        mLayout.setOrientation(LinearLayout.VERTICAL);

        String mTitle = "赛事";
        if (viewData.mCompetitionBean.mSportsType == CompetitionViewData.CompetitionData.TYPE_SPORTS_NBA) {
            mTitle = "NBA赛事";
        } else if (viewData.mCompetitionBean.mSportsType == CompetitionViewData.CompetitionData.TYPE_SPORTS_FOOTBALL) {
            mTitle = "足球比赛";
        }

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(null, "competition", mTitle);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mLayout.addView(titleViewAdapter.view, layoutParams);

        LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER_VERTICAL);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, countHeight);

        if (StyleConfig.getInstance().getSelectStyleIndex() == StyleConfig.STYLE_ROBOT_NONE_SCREES) {
            View lineView = new View(GlobalContext.get());
            LinearLayout.LayoutParams lineViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
            lineView.setLayoutParams(lineViewParams);
            lineView.setBackground(LayouUtil.getDrawable("line"));
            mLayout.addView(lineView);
        } else {
            linearLayout.setBackground(LayouUtil.getDrawable("white_range_layout"));
        }

        mLayout.addView(linearLayout, layoutParams);

        TextView tvLabel = new TextView(GlobalContext.get());
        tvLabel.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvLabel, mH1);
        tvLabel.setTextColor(Color.WHITE);
        tvLabel.setText(viewData.mCompetitionBean.mCompetition);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.addView(tvLabel, params);

        TextView tvTime = new TextView(GlobalContext.get());
        tvTime.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvTime, mH5);
        tvTime.setTextColor(Color.parseColor("#FF89898A"));
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = unit;
        linearLayout.addView(tvTime, params);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日  E  HH:mm");
        tvTime.setText(formatter.format(new Date(viewData.mCompetitionBean.mStartTimeStamp * 1000L)));

        //
        LinearLayout llCompetition = new LinearLayout(GlobalContext.get());
        llCompetition.setOrientation(LinearLayout.HORIZONTAL);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = llCompetitionTopMargin;
        linearLayout.addView(llCompetition, params);


        //主场队
        FrameLayout flHomeTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llHomeTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        llHomeTeamParams.leftMargin = (int) LayouUtil.getDimen("x16");
        llHomeTeamParams.gravity = Gravity.CENTER_HORIZONTAL;
        llCompetition.addView(flHomeTeam, llHomeTeamParams);

        LinearLayout llHomeTeam = new LinearLayout(GlobalContext.get());
        llHomeTeam.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams flHomeTeamParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flHomeTeamParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
        flHomeTeam.addView(llHomeTeam, flHomeTeamParams);

        ImageView ivHomeTeam = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams ivHomeTeamParams = new LinearLayout.LayoutParams(iconTeamWidth, iconTeamHeight);
        ivHomeTeamParams.gravity = Gravity.CENTER;
        llHomeTeam.addView(ivHomeTeam, ivHomeTeamParams);

        TextView tvHomeTeam = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvHomeTeam, mH4);
        tvHomeTeam.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvHomeTeamParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvHomeTeamParams.gravity = Gravity.CENTER;
        tvHomeTeamParams.topMargin = unit;
        llHomeTeam.addView(tvHomeTeam, tvHomeTeamParams);


        //比赛信息
        LinearLayout llInfo = new LinearLayout(GlobalContext.get());
        llInfo.setOrientation(LinearLayout.VERTICAL);
        llInfo.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llInfoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.6f);
        llInfoParams.gravity = Gravity.CENTER_HORIZONTAL;
        llCompetition.addView(llInfo, llInfoParams);

        TextView tvGoal = new TextView(GlobalContext.get());
        tvGoal.setTypeface(Typeface.DEFAULT_BOLD);
        TextViewUtil.setTextSize(tvGoal, 2 * mH5);
        tvGoal.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvGoalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvGoalParams.gravity = Gravity.CENTER;
        llInfo.addView(tvGoal, tvGoalParams);

        TextView tvPeriod = new TextView(GlobalContext.get());
        tvPeriod.setSingleLine();
        TextViewUtil.setTextSize(tvPeriod, mH7);
        LinearLayout.LayoutParams tvPeriodParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvPeriodParams.gravity = Gravity.CENTER;
        llInfo.addView(tvPeriod, tvPeriodParams);

        //客场队
        FrameLayout flAwayTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llAwayTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        llAwayTeamParams.gravity = Gravity.CENTER_HORIZONTAL;
        llAwayTeamParams.rightMargin = (int) LayouUtil.getDimen("x16");
        llCompetition.addView(flAwayTeam, llAwayTeamParams);

        LinearLayout llAwayTeam = new LinearLayout(GlobalContext.get());
        llAwayTeam.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams flAwayTeamParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flAwayTeamParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
        flAwayTeam.addView(llAwayTeam, flAwayTeamParams);

        ImageView ivAwayTeam = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams ivAwayTeamParams = new LinearLayout.LayoutParams(iconTeamWidth, iconTeamHeight);
        ivAwayTeamParams.gravity = Gravity.CENTER;
        llAwayTeam.addView(ivAwayTeam, ivAwayTeamParams);

        TextView tvAwayTeam = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvAwayTeam, mH4);
        tvAwayTeam.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvAwayTeamParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvAwayTeamParams.gravity = Gravity.CENTER;
        tvAwayTeamParams.topMargin = unit;
        llAwayTeam.addView(tvAwayTeam, tvAwayTeamParams);


        loadDrawableByUrl(ivHomeTeam, viewData.mCompetitionBean.mHomeTeam.mLogo);
        tvHomeTeam.setText(viewData.mCompetitionBean.mHomeTeam.mName);


        tvLabel.setText(viewData.mCompetitionBean.mRoundType + formatTime(viewData.mCompetitionBean.mStartTimeStamp));

        tvPeriod.setSingleLine();

        if (TextUtils.equals("未开始", viewData.mCompetitionBean.mPeriod)) {
            tvPeriod.setTextColor(Color.parseColor("#FFFFFFFF"));
            tvGoal.setText("VS");
            tvPeriod.setText(viewData.mCompetitionBean.mPeriod);
            tvPeriod.setBackground(LayouUtil.getDrawable("competition_not_started_detail"));
            tvGoal.setPadding(0,0,0,0);
            tvPeriod.setPadding((int) LayouUtil.getDimen("x20"), (int) LayouUtil.getDimen("y3"), (int) LayouUtil.getDimen("x20"), (int) LayouUtil.getDimen("y3"));
        } else {
            tvPeriod.setPadding((int) LayouUtil.getDimen("x20"), (int) LayouUtil.getDimen("y3"), (int) LayouUtil.getDimen("x20"), (int) LayouUtil.getDimen("y3"));
            tvGoal.setText(viewData.mCompetitionBean.mHomeTeam.mGoal + ":" + viewData.mCompetitionBean.mAwayTeam.mGoal);
            tvPeriod.setText(viewData.mCompetitionBean.mPeriod);
            if (TextUtils.equals("进行中", viewData.mCompetitionBean.mPeriod)) {
                tvPeriod.setTextColor(Color.parseColor("#FFFFFFFF"));
                tvPeriod.setBackground(LayouUtil.getDrawable("competition_under_way_detail"));
            } else if (TextUtils.equals("已结束", viewData.mCompetitionBean.mPeriod)) {
                tvPeriod.setBackground(LayouUtil.getDrawable("competition_ended_detail"));
                tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
            } else {
                tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
                tvPeriod.setBackground(null);
            }
        }


        loadDrawableByUrl(ivAwayTeam, viewData.mCompetitionBean.mAwayTeam.mLogo);
        tvAwayTeam.setText(viewData.mCompetitionBean.mAwayTeam.mName);


        return mLayout;
    }

    private String formatTime(long time) {
        Calendar today = Calendar.getInstance();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time * 1000L);

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //老的时间减去今天的时间
        long intervalMilli = calendar.getTimeInMillis() - today.getTimeInMillis();
        int xcts = (int) (intervalMilli / (24 * 60 * 60 * 1000));
        String ret = "";
        // -2:前天 -1：昨天 0：今天 1：明天 2：后天
        if (xcts >= -2 && xcts <= 2) {
            switch (xcts) {
                case -2:
                    ret = "(前天)";
                    break;
                case -1:
                    ret = "(昨天)";
                    break;
                case 0:
                    ret = "(今天)";
                    break;
                case 1:
                    ret = "(明天)";
                    break;
                case 2:
                    ret = "(后天)";
                    break;
            }
        }
        return ret;
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
