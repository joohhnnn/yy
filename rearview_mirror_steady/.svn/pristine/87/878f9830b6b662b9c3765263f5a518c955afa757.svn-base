package com.txznet.comm.ui.viewfactory.view.defaults;

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
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CompetitionDetailViewData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICompetitionDetailView;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author ASUS User
 */
@SuppressLint("NewApi")
public class DefaultCompetitionDetailView extends ICompetitionDetailView {

    private static DefaultCompetitionDetailView sInstance = new DefaultCompetitionDetailView();
    private int iconTeamWidth;
    private int iconTeamHeight;
    private int unit;
    private int mInfoPaddingTop;
    private int llCompetitionTopMargin;

    private DefaultCompetitionDetailView() {
    }

    public static DefaultCompetitionDetailView getInstance() {
        return sInstance;
    }

    private int detailVerMargin;    //详细内容上下间距


    @Override
    public void init() {
        super.init();
        unit = (int) LayouUtil.getDimen("y8");

        iconTeamWidth = (int) LayouUtil.getDimen("m120");
        iconTeamHeight = (int) LayouUtil.getDimen("m120");
        mInfoPaddingTop = 3 * unit;
        llCompetitionTopMargin = 3 * unit;

        detailVerMargin = 8 * unit;
    }


    @Override
    public ViewAdapter getView(ViewData data) {
        CompetitionDetailViewData detailViewData = (CompetitionDetailViewData) data;

        View view = createViewFull(detailViewData);

        ViewAdapter adapter = new ViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(data.getType());
        adapter.object = DefaultCompetitionDetailView.getInstance();
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

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ListTitleView.getInstance().getTitleHeight()));
        tvTitle.setTextSize(LayouUtil.getDimen("m18"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setText(mTitle);
        tvTitle.setGravity(Gravity.CENTER_VERTICAL);
        mLayout.addView(tvTitle);

        LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setPadding(0, 0, 0, detailVerMargin);
        layoutParams.bottomMargin = 3 * unit;
        mLayout.addView(linearLayout, layoutParams);


        linearLayout.setBackground(LayouUtil.getDrawable("white_range_layout"));

        TextView tvLabel = new TextView(GlobalContext.get());
        tvLabel.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvLabel, LayouUtil.getDimen("m25"));
        tvLabel.setTextColor(Color.WHITE);
        tvLabel.setText(viewData.mCompetitionBean.mCompetition);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = detailVerMargin;
        linearLayout.addView(tvLabel, params);

        TextView tvTime = new TextView(GlobalContext.get());
        tvTime.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvTime, LayouUtil.getDimen("m20"));
        tvTime.setTextColor(Color.parseColor("#FF89898A"));
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = unit;
        linearLayout.addView(tvTime, params);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日  E  HH:mm");
        tvTime.setText(formatter.format(new Date(viewData.mCompetitionBean.mStartTimeStamp * 1000L)));

        //
        LinearLayout llCompetition = new LinearLayout(GlobalContext.get());
        llCompetition.setOrientation(LinearLayout.HORIZONTAL);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.topMargin = llCompetitionTopMargin;
        linearLayout.addView(llCompetition, params);


        //主场队
        FrameLayout flHomeTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llHomeTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        llHomeTeamParams.leftMargin = (int) LayouUtil.getDimen("x20");
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
        TextViewUtil.setTextSize(tvHomeTeam, LayouUtil.getDimen("m20"));
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
        tvGoal.setSingleLine();
        TextViewUtil.setTextSize(tvGoal, LayouUtil.getDimen("m48"));
        tvGoal.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvGoalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvGoalParams.gravity = Gravity.CENTER;
        llInfo.addView(tvGoal, tvGoalParams);

        TextView tvPeriod = new TextView(GlobalContext.get());
        tvPeriod.setSingleLine();
        TextViewUtil.setTextSize(tvPeriod, LayouUtil.getDimen("m16"));
        LinearLayout.LayoutParams tvPeriodParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvPeriodParams.gravity = Gravity.CENTER;
        llInfo.addView(tvPeriod, tvPeriodParams);

        //客场队
        FrameLayout flAwayTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llAwayTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        llAwayTeamParams.gravity = Gravity.CENTER_HORIZONTAL;
        llAwayTeamParams.rightMargin = (int) LayouUtil.getDimen("x20");
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
        TextViewUtil.setTextSize(tvAwayTeam, LayouUtil.getDimen("m20"));
        tvAwayTeam.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvAwayTeamParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvAwayTeamParams.gravity = Gravity.CENTER;
        tvAwayTeamParams.topMargin = unit;
        llAwayTeam.addView(tvAwayTeam, tvAwayTeamParams);


        loadDrawableByUrl(ivHomeTeam, viewData.mCompetitionBean.mHomeTeam.mLogo);
        tvHomeTeam.setText(viewData.mCompetitionBean.mHomeTeam.mName);


        tvLabel.setText(viewData.mCompetitionBean.mRoundType + DateUtils.getDayQuantum(viewData.mCompetitionBean.mStartTimeStamp * 1000L));

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
