package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.ui.viewfactory.data.StyleListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICompetitionView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.runnables.Runnable1;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@SuppressLint("NewApi")
public class CompetitionView extends ICompetitionView {

    private static CompetitionView sInstance = new CompetitionView();

    private List<View> mItemViews;

    private int tvNumSize;    //编号字体大小
    private int tvNumColor;    //编号字体颜色
    private int tvNumSide;    //编号布局宽高
    private int tvNumHorMargin;    //编号左右边距
    private int tvContentSize;    //内容字体大小
    private int tvContentColor;    //内容字体颜色

    private int dividerHeight;    //分隔线高度

    private int iconTeamWidth;    //队伍icon宽度
    private int iconTeamHeight;    //队伍icon高度
    private int tvTeamSize;    //队伍名字字体大小
    private int tvTeamWidth;    //队伍名字宽度
    private int tvCompetitionSize;    //比赛名称字体大小
    private int tvCompetitionColor;    //比赛名称字体颜色
    private int tvPeriodSize;    //比赛状态字体大小
    private int tvGoalSize;    //赛事比分字体大小
    private long mStartTimeStamp;

    private LruCache<String, Bitmap> mCachePost = new LruCache<String, Bitmap>(10);

    private CompetitionView() {
    }

    public static CompetitionView getInstance() {
        return sInstance;
    }

    @Override
    public void updateProgress(int progress, int selection) {
    }

    @Override
    public ViewAdapter getView(ViewData data) {

        CompetitionViewData competitionViewData = (CompetitionViewData) data;
        mStartTimeStamp = competitionViewData.mCompetitionData.mStartTimeStamp;
        WinLayout.getInstance().vTips = competitionViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "CompetitionViewData.vTips: " + competitionViewData.vTips);
        View view = null;

        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                view = createViewFull(competitionViewData);
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = createViewHalf(competitionViewData);
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(competitionViewData);
                break;
        }

        ViewAdapter viewAdapter = new ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.view.setTag(data.getType());
        viewAdapter.object = CompetitionView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(CompetitionViewData competitionViewData) {

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(competitionViewData, "competition", competitionViewData.mTitleInfo.prefix);

        mCurPage = competitionViewData.mTitleInfo.curPage;
        mMaxPage = competitionViewData.mTitleInfo.maxPage;

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view, layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.pagePoiCount * SizeConfig.itemHeight);
        llLayout.addView(llContents, layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        llContents.addView(llContent, layoutParams);

        LinearLayout llPager = new PageView(GlobalContext.get(), mCurPage, mMaxPage);
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        llContents.addView(llPager, layoutParams);

        llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
        llContent.setLayoutAnimationListener(new AnimationListener() {
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
        for (int i = 0; i < competitionViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.itemHeight);
            View itemView = createFullItemView(i, competitionViewData.mCompetitionData.mCompetitionBeans.get(i), i != SizeConfig.pageCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }


    private View createFullItemView(int position, CompetitionViewData.CompetitionData.CompetitionBean competitionBean, boolean showDivider) {

        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());

        FrameLayout flContent = new FrameLayout(GlobalContext.get());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        /*layoutParams.topMargin = flContentMarginTop;
        layoutParams.bottomMargin = flContentMarginBottom;*/
        itemView.addView(flContent, layoutParams);

		/*GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(mProgressBar, mFLayoutParams);
		progressBars.add(mProgressBar);*/

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.HORIZONTAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flContent.addView(llContent, mFLayoutParams);

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        tvNum.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumSide, tvNumSide);
        mLLayoutParams.leftMargin = tvNumHorMargin;
        mLLayoutParams.rightMargin = tvNumHorMargin;
        mLLayoutParams.gravity = Gravity.CENTER;
        llContent.addView(tvNum, mLLayoutParams);

        //
        LinearLayout llCompetition = new LinearLayout(GlobalContext.get());
        llCompetition.setOrientation(LinearLayout.HORIZONTAL);
        llContent.addView(llCompetition, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //主场队
        FrameLayout flHomeTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llHomeTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        llHomeTeamParams.leftMargin = (int) ViewParamsUtil.getDimen("x16");
        llHomeTeamParams.gravity = Gravity.CENTER_VERTICAL;
        llCompetition.addView(flHomeTeam, llHomeTeamParams);

        LinearLayout llHomeTeam = new LinearLayout(GlobalContext.get());
        llHomeTeam.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams flHomeTeamParams = new FrameLayout.LayoutParams(tvTeamWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        flHomeTeamParams.gravity = Gravity.CENTER_VERTICAL;
        flHomeTeam.addView(llHomeTeam, flHomeTeamParams);

        ImageView ivHomeTeam = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams ivHomeTeamParams = new LinearLayout.LayoutParams(iconTeamWidth, iconTeamHeight);
        ivHomeTeamParams.gravity = Gravity.CENTER;
        llHomeTeam.addView(ivHomeTeam, ivHomeTeamParams);

        TextView tvHomeTeam = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvHomeTeam, tvTeamSize);
        tvHomeTeam.setSingleLine();
        tvHomeTeam.setEllipsize(TruncateAt.END);
        tvHomeTeam.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvHomeTeamParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvHomeTeamParams.gravity = Gravity.CENTER;
        llHomeTeam.addView(tvHomeTeam, tvHomeTeamParams);


        //比赛信息
        LinearLayout llInfo = new LinearLayout(GlobalContext.get());
        llInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams llInfoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        llInfoParams.gravity = Gravity.CENTER;
        llCompetition.addView(llInfo, llInfoParams);

        TextView tvCompetition = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvCompetition, tvCompetitionSize);
        tvCompetition.setSingleLine();
        tvCompetition.setEllipsize(TruncateAt.END);
        tvCompetition.setTextColor(tvCompetitionColor);
        LinearLayout.LayoutParams tvCompetitionParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvCompetitionParams.gravity = Gravity.CENTER;
        llInfo.addView(tvCompetition, tvCompetitionParams);

        TextView tvGoal = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvGoal, tvGoalSize);
        tvGoal.setSingleLine();
        tvGoal.setEllipsize(TruncateAt.END);
        tvGoal.setTypeface(Typeface.DEFAULT_BOLD);
        tvGoal.setTextColor(Color.WHITE);
        tvGoal.setPadding(0,0,0,0);
        LinearLayout.LayoutParams tvGoalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvGoalParams.gravity = Gravity.CENTER;
        llInfo.addView(tvGoal, tvGoalParams);

        TextView tvPeriod = new TextView(GlobalContext.get());
        tvPeriod.setSingleLine();
        tvPeriod.setEllipsize(TruncateAt.END);
        TextViewUtil.setTextSize(tvPeriod, tvPeriodSize);
        LinearLayout.LayoutParams tvPeriodParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvPeriodParams.gravity = Gravity.CENTER;
        llInfo.addView(tvPeriod, tvPeriodParams);

        //客场队
        FrameLayout flAwayTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llAwayTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        llAwayTeamParams.gravity = Gravity.CENTER_VERTICAL;
        llAwayTeamParams.rightMargin = (int) ViewParamsUtil.getDimen("x16");
        llCompetition.addView(flAwayTeam, llAwayTeamParams);

        LinearLayout llAwayTeam = new LinearLayout(GlobalContext.get());
        llAwayTeam.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams flAwayTeamParams = new FrameLayout.LayoutParams(tvTeamWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        flAwayTeamParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        flAwayTeam.addView(llAwayTeam, flAwayTeamParams);

        ImageView ivAwayTeam = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams ivAwayTeamParams = new LinearLayout.LayoutParams(iconTeamWidth, iconTeamHeight);
        ivAwayTeamParams.gravity = Gravity.CENTER;
        llAwayTeam.addView(ivAwayTeam, ivAwayTeamParams);

        TextView tvAwayTeam = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvAwayTeam, tvTeamSize);
        tvAwayTeam.setSingleLine();
        tvAwayTeam.setEllipsize(TruncateAt.END);
        tvAwayTeam.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvAwayTeamParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvAwayTeamParams.gravity = Gravity.CENTER;
        llAwayTeam.addView(tvAwayTeam, tvAwayTeamParams);
        //

//        TextView tvContent = new TextView(GlobalContext.get());
//        tvContent.setEllipsize(TruncateAt.END);
//        tvContent.setSingleLine();
//        tvContent.setGravity(Gravity.CENTER_VERTICAL);
//        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		/*mLLayoutParams.leftMargin = tvContentMarginLeft;
//		mLLayoutParams.rightMargin = tvContentMarginRight;*/
//        llContent.addView(tvContent, mLLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        //divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        TextViewUtil.setTextSize(tvNum, tvNumSize);
        TextViewUtil.setTextColor(tvNum, tvNumColor);

        tvNum.setText(String.valueOf(position + 1));

        loadDrawableByUrl(ivHomeTeam, competitionBean.mHomeTeam.mLogo);
        tvHomeTeam.setText(competitionBean.mHomeTeam.mName);

        tvCompetition.setText(competitionBean.mCompetition + competitionBean.mRoundType);


        if (TextUtils.equals("未开始", competitionBean.mPeriod)) {
            tvPeriod.setTextColor(Color.parseColor("#CCFFFFFF"));
            tvPeriod.setBackground(null);
            tvGoal.setText("VS");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(competitionBean.mStartTimeStamp * 1000L);
            tvPeriod.setText(convertDatetime(mStartTimeStamp, competitionBean.mStartTimeStamp));
            tvPeriod.setPadding(0,0,0,0);
        } else {
            tvPeriod.setPadding((int) ViewParamsUtil.getDimen("x16"), (int) ViewParamsUtil.getDimen("y3"), (int) ViewParamsUtil.getDimen("x16"), (int) ViewParamsUtil.getDimen("y3"));
            tvGoal.setText(competitionBean.mHomeTeam.mGoal + " : " + competitionBean.mAwayTeam.mGoal);
            tvPeriod.setText(competitionBean.mPeriod);
            if (TextUtils.equals("进行中", competitionBean.mPeriod)) {
                tvPeriod.setTextColor(Color.parseColor("#FF07AB6D"));
                tvPeriod.setBackground(LayouUtil.getDrawable("competition_under_way"));
            } else if (TextUtils.equals("已结束", competitionBean.mPeriod)) {
                tvPeriod.setBackground(LayouUtil.getDrawable("competition_ended"));
                tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
            } else {
                tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
                tvPeriod.setBackground(null);
            }
        }

        loadDrawableByUrl(ivAwayTeam, competitionBean.mAwayTeam.mLogo);
        tvAwayTeam.setText(competitionBean.mAwayTeam.mName);

        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

        return itemView;
    }


    private View createHalfItemView(int position, CompetitionViewData.CompetitionData.CompetitionBean competitionBean, boolean showDivider) {

        LogUtil.logd(WinLayout.logTag + "CompetitionData: " + competitionBean.mAwayTeam.mName + "--" + competitionBean.mHomeTeam.mName);

        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());

        FrameLayout flContent = new FrameLayout(GlobalContext.get());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        /*layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;*/
        itemView.addView(flContent, layoutParams);

		/*GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(mProgressBar, mFLayoutParams);
		progressBars.add(mProgressBar);*/

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.HORIZONTAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flContent.addView(llContent, mFLayoutParams);

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        tvNum.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumSide, tvNumSide);
        mLLayoutParams.leftMargin = tvNumHorMargin;
        mLLayoutParams.rightMargin = tvNumHorMargin;
        mLLayoutParams.gravity = Gravity.CENTER;
        llContent.addView(tvNum, mLLayoutParams);

        //
        LinearLayout llCompetition = new LinearLayout(GlobalContext.get());
        llCompetition.setOrientation(LinearLayout.HORIZONTAL);
        llContent.addView(llCompetition, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView tvCompetition = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvCompetition, tvCompetitionSize);
        tvCompetition.setSingleLine();
        tvCompetition.setEllipsize(TruncateAt.END);
        tvCompetition.setTextColor(tvCompetitionColor);
        LinearLayout.LayoutParams tvCompetitionParams = new LinearLayout.LayoutParams((int) ViewParamsUtil.getDimen("x120"), ViewGroup.LayoutParams.WRAP_CONTENT);
        tvCompetitionParams.gravity = Gravity.CENTER;
        llCompetition.addView(tvCompetition, tvCompetitionParams);

        //主场队
        FrameLayout flHomeTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llHomeTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 4);
        llHomeTeamParams.leftMargin = (int) ViewParamsUtil.getDimen("x16");
        llHomeTeamParams.gravity = Gravity.CENTER_VERTICAL;
        llCompetition.addView(flHomeTeam, llHomeTeamParams);

        LinearLayout llHomeTeam = new LinearLayout(GlobalContext.get());
        llHomeTeam.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams flHomeTeamParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flHomeTeamParams.gravity = Gravity.CENTER_VERTICAL;
        flHomeTeam.addView(llHomeTeam, flHomeTeamParams);

        ImageView ivHomeTeam = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams ivHomeTeamParams = new LinearLayout.LayoutParams(iconTeamWidth, iconTeamHeight);
        ivHomeTeamParams.gravity = Gravity.CENTER_VERTICAL;
        llHomeTeam.addView(ivHomeTeam, ivHomeTeamParams);

        TextView tvHomeTeam = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvHomeTeam, tvTeamSize);
        tvHomeTeam.setSingleLine();
        tvHomeTeam.setEllipsize(TruncateAt.END);
        tvHomeTeam.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvHomeTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        tvHomeTeamParams.gravity = Gravity.CENTER_VERTICAL;
        tvHomeTeamParams.leftMargin = (int) ViewParamsUtil.getDimen("x8");
        llHomeTeam.addView(tvHomeTeam, tvHomeTeamParams);


        //比赛信息
        LinearLayout llInfo = new LinearLayout(GlobalContext.get());
        llInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams llInfoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 3);
        llInfoParams.gravity = Gravity.CENTER;
        llInfoParams.leftMargin = (int) ViewParamsUtil.getDimen("x8");
        llCompetition.addView(llInfo, llInfoParams);

        TextView tvGoal = new TextView(GlobalContext.get());
        tvGoal.setTypeface(Typeface.DEFAULT_BOLD);
        TextViewUtil.setTextSize(tvGoal, tvGoalSize);
        tvGoal.setSingleLine();
        tvGoal.setEllipsize(TruncateAt.END);
        tvGoal.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvGoalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvGoalParams.gravity = Gravity.CENTER;
        llInfo.addView(tvGoal, tvGoalParams);

        TextView tvPeriod = new TextView(GlobalContext.get());
        tvPeriod.setSingleLine();
        tvPeriod.setEllipsize(TruncateAt.END);
        TextViewUtil.setTextSize(tvPeriod, tvPeriodSize);
        LinearLayout.LayoutParams tvPeriodParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvPeriodParams.gravity = Gravity.CENTER;
        tvPeriod.setTextColor(Color.parseColor("#CCFFFFFF"));
        llInfo.addView(tvPeriod, tvPeriodParams);

        //客场队
        FrameLayout flAwayTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llAwayTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 4);
        llAwayTeamParams.gravity = Gravity.CENTER_VERTICAL;
        llAwayTeamParams.rightMargin = (int) LayouUtil.getDimen("x8");
        llAwayTeamParams.leftMargin = (int) LayouUtil.getDimen("x8");
        llCompetition.addView(flAwayTeam, llAwayTeamParams);

        LinearLayout llAwayTeam = new LinearLayout(GlobalContext.get());
        llAwayTeam.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams flAwayTeamParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flAwayTeamParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        flAwayTeam.addView(llAwayTeam, flAwayTeamParams);

        TextView tvAwayTeam = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvAwayTeam, tvTeamSize);
        tvAwayTeam.setSingleLine();
        tvAwayTeam.setEllipsize(TruncateAt.END);
        tvAwayTeam.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvAwayTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        tvAwayTeamParams.gravity = Gravity.CENTER_VERTICAL;
        llAwayTeam.addView(tvAwayTeam, tvAwayTeamParams);

        ImageView ivAwayTeam = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams ivAwayTeamParams = new LinearLayout.LayoutParams(iconTeamWidth, iconTeamHeight);
        ivAwayTeamParams.gravity = Gravity.CENTER_VERTICAL;
        ivAwayTeamParams.leftMargin = (int) ViewParamsUtil.getDimen("x8");
        llAwayTeam.addView(ivAwayTeam, ivAwayTeamParams);


        //

//        TextView tvContent = new TextView(GlobalContext.get());
//        tvContent.setEllipsize(TruncateAt.END);
//        tvContent.setSingleLine();
//        tvContent.setGravity(Gravity.CENTER_VERTICAL);
//        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		/*mLLayoutParams.leftMargin = tvContentMarginLeft;
//		mLLayoutParams.rightMargin = tvContentMarginRight;*/
//        llContent.addView(tvContent, mLLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        //divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        TextViewUtil.setTextSize(tvNum, tvNumSize);
        TextViewUtil.setTextColor(tvNum, tvNumColor);

        tvNum.setText(String.valueOf(position + 1));

        loadDrawableByUrl(ivHomeTeam, competitionBean.mHomeTeam.mLogo);
        tvHomeTeam.setText(competitionBean.mHomeTeam.mName);

        tvCompetition.setText(competitionBean.mCompetition + competitionBean.mRoundType);


        if (TextUtils.equals("未开始", competitionBean.mPeriod)) {
            tvPeriod.setTextColor(Color.parseColor("#CCFFFFFF"));
            tvPeriod.setBackground(null);
            tvGoal.setText("VS");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(competitionBean.mStartTimeStamp * 1000L);
            tvPeriod.setText(convertDatetime(mStartTimeStamp, competitionBean.mStartTimeStamp));
            tvPeriod.setPadding(0,0,0,0);
        } else {
            tvPeriod.setPadding((int) ViewParamsUtil.getDimen("x16"), (int) ViewParamsUtil.getDimen("y3"), (int) ViewParamsUtil.getDimen("x16"), (int) ViewParamsUtil.getDimen("y3"));
            tvGoal.setText(competitionBean.mHomeTeam.mGoal + " : " + competitionBean.mAwayTeam.mGoal);
            tvPeriod.setText(competitionBean.mPeriod);
            if (TextUtils.equals("进行中", competitionBean.mPeriod)) {
                tvPeriod.setTextColor(Color.parseColor("#FF07AB6D"));
                tvPeriod.setBackground(LayouUtil.getDrawable("competition_under_way"));
            } else if (TextUtils.equals("已结束", competitionBean.mPeriod)) {
                tvPeriod.setBackground(LayouUtil.getDrawable("competition_ended"));
                tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
            } else {
                tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
                tvPeriod.setBackground(null);
            }
        }

        loadDrawableByUrl(ivAwayTeam, competitionBean.mAwayTeam.mLogo);
        tvAwayTeam.setText(competitionBean.mAwayTeam.mName);


        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);


        return itemView;
    }

    private View createNoneItemView(int position, CompetitionViewData.CompetitionData.CompetitionBean competitionBean, boolean showDivider) {

        LogUtil.logd(WinLayout.logTag + "CompetitionData: " + competitionBean.mAwayTeam.mName + "--" + competitionBean.mHomeTeam.mName);

        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
        ListTitleView.getInstance().mItemViews = mItemViews;
        itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());

        FrameLayout flContent = new FrameLayout(GlobalContext.get());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        /*layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;*/
        itemView.addView(flContent, layoutParams);

		/*GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
		mProgressBar.setVisibility(View.GONE);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(mProgressBar, mFLayoutParams);
		progressBars.add(mProgressBar);*/

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.HORIZONTAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        flContent.addView(llContent, mFLayoutParams);

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        tvNum.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumSide, tvNumSide);
        mLLayoutParams.leftMargin = tvNumHorMargin;
        mLLayoutParams.rightMargin = tvNumHorMargin;
        mLLayoutParams.gravity = Gravity.CENTER;
        llContent.addView(tvNum, mLLayoutParams);

        //
        LinearLayout llCompetition = new LinearLayout(GlobalContext.get());
        llCompetition.setOrientation(LinearLayout.HORIZONTAL);
        llContent.addView(llCompetition, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        //主场队
        FrameLayout flHomeTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llHomeTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        llHomeTeamParams.leftMargin = (int) ViewParamsUtil.getDimen("x16");
        llHomeTeamParams.gravity = Gravity.CENTER_VERTICAL;
        llCompetition.addView(flHomeTeam, llHomeTeamParams);

        LinearLayout llHomeTeam = new LinearLayout(GlobalContext.get());
        llHomeTeam.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams flHomeTeamParams = new FrameLayout.LayoutParams(tvTeamWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        flHomeTeamParams.gravity = Gravity.CENTER_VERTICAL;
        flHomeTeam.addView(llHomeTeam, flHomeTeamParams);

        ImageView ivHomeTeam = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams ivHomeTeamParams = new LinearLayout.LayoutParams(iconTeamWidth, iconTeamHeight);
        ivHomeTeamParams.gravity = Gravity.CENTER;
        llHomeTeam.addView(ivHomeTeam, ivHomeTeamParams);

        TextView tvHomeTeam = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvHomeTeam, tvTeamSize);
        tvHomeTeam.setSingleLine();
        tvHomeTeam.setEllipsize(TruncateAt.END);
        tvHomeTeam.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvHomeTeamParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvHomeTeamParams.gravity = Gravity.CENTER;
        llHomeTeam.addView(tvHomeTeam, tvHomeTeamParams);


        //比赛信息
        LinearLayout llInfo = new LinearLayout(GlobalContext.get());
        llInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams llInfoParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.2f);
        llInfoParams.gravity = Gravity.CENTER;
        llCompetition.addView(llInfo, llInfoParams);

        TextView tvCompetition = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvCompetition, tvCompetitionSize);
        tvCompetition.setSingleLine();
        tvCompetition.setEllipsize(TruncateAt.END);
        tvCompetition.setTextColor(tvCompetitionColor);
        LinearLayout.LayoutParams tvCompetitionParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvCompetitionParams.gravity = Gravity.CENTER;
        llInfo.addView(tvCompetition, tvCompetitionParams);

        TextView tvGoal = new TextView(GlobalContext.get());
        tvGoal.setTypeface(Typeface.DEFAULT_BOLD);
        TextViewUtil.setTextSize(tvGoal, tvGoalSize);
        tvGoal.setSingleLine();
        tvGoal.setEllipsize(TruncateAt.END);
        tvGoal.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvGoalParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvGoalParams.gravity = Gravity.CENTER;
        llInfo.addView(tvGoal, tvGoalParams);

        TextView tvPeriod = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvPeriod, tvPeriodSize);
        tvPeriod.setSingleLine();
        tvPeriod.setEllipsize(TruncateAt.END);
        LinearLayout.LayoutParams tvPeriodParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvPeriodParams.gravity = Gravity.CENTER;
        llInfo.addView(tvPeriod, tvPeriodParams);

        //客场队
        FrameLayout flAwayTeam = new FrameLayout(GlobalContext.get());
        LinearLayout.LayoutParams llAwayTeamParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        llAwayTeamParams.gravity = Gravity.CENTER_VERTICAL;
        llAwayTeamParams.rightMargin = (int) ViewParamsUtil.getDimen("x16");
        llCompetition.addView(flAwayTeam, llAwayTeamParams);

        LinearLayout llAwayTeam = new LinearLayout(GlobalContext.get());
        llAwayTeam.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams flAwayTeamParams = new FrameLayout.LayoutParams(tvTeamWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        flAwayTeamParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        flAwayTeam.addView(llAwayTeam, flAwayTeamParams);

        ImageView ivAwayTeam = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams ivAwayTeamParams = new LinearLayout.LayoutParams(iconTeamWidth, iconTeamHeight);
        ivAwayTeamParams.gravity = Gravity.CENTER;
        llAwayTeam.addView(ivAwayTeam, ivAwayTeamParams);

        TextView tvAwayTeam = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvAwayTeam, tvTeamSize);
        tvAwayTeam.setSingleLine();
        tvAwayTeam.setEllipsize(TruncateAt.END);
        tvAwayTeam.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tvAwayTeamParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvAwayTeamParams.gravity = Gravity.CENTER;
        llAwayTeam.addView(tvAwayTeam, tvAwayTeamParams);
        //

//        TextView tvContent = new TextView(GlobalContext.get());
//        tvContent.setEllipsize(TruncateAt.END);
//        tvContent.setSingleLine();
//        tvContent.setGravity(Gravity.CENTER_VERTICAL);
//        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		/*mLLayoutParams.leftMargin = tvContentMarginLeft;
//		mLLayoutParams.rightMargin = tvContentMarginRight;*/
//        llContent.addView(tvContent, mLLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        //divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        TextViewUtil.setTextSize(tvNum, tvNumSize);
        TextViewUtil.setTextColor(tvNum, tvNumColor);

        tvNum.setText(String.valueOf(position + 1));

        loadDrawableByUrl(ivHomeTeam, competitionBean.mHomeTeam.mLogo);
        tvHomeTeam.setText(competitionBean.mHomeTeam.mName);

        tvCompetition.setText(competitionBean.mCompetition + competitionBean.mRoundType);


        if (TextUtils.equals("未开始", competitionBean.mPeriod)) {
            tvPeriod.setTextColor(Color.parseColor("#CCFFFFFF"));
            tvPeriod.setBackground(null);
            tvGoal.setText("VS");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(competitionBean.mStartTimeStamp * 1000L);
            tvPeriod.setText(convertDatetime(mStartTimeStamp, competitionBean.mStartTimeStamp));
            tvPeriod.setPadding(0,0,0,0);
        } else {
            tvPeriod.setPadding((int) ViewParamsUtil.getDimen("x16"), (int) ViewParamsUtil.getDimen("y3"), (int) ViewParamsUtil.getDimen("x16"), (int) ViewParamsUtil.getDimen("y3"));
            tvGoal.setText(competitionBean.mHomeTeam.mGoal + " : " + competitionBean.mAwayTeam.mGoal);
            tvPeriod.setText(competitionBean.mPeriod);
            if (TextUtils.equals("进行中", competitionBean.mPeriod)) {
                tvPeriod.setTextColor(Color.parseColor("#FF07AB6D"));
                tvPeriod.setBackground(LayouUtil.getDrawable("competition_under_way"));
            } else if (TextUtils.equals("已结束", competitionBean.mPeriod)) {
                tvPeriod.setBackground(LayouUtil.getDrawable("competition_ended"));
                tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
            } else {
                tvPeriod.setTextColor(Color.parseColor("#80FFFFFF"));
                tvPeriod.setBackground(null);
            }
        }


        loadDrawableByUrl(ivAwayTeam, competitionBean.mAwayTeam.mLogo);
        tvAwayTeam.setText(competitionBean.mAwayTeam.mName);


        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);


        return itemView;
    }


    private void loadDrawableByUrl(final ImageView ivHead, String uri) {
        Bitmap bitmap = null;
        synchronized (mCachePost) {
            bitmap = mCachePost.get(uri);
        }

        if (bitmap != null) {
            UI2Manager.runOnUIThread(new Runnable1<Bitmap>(bitmap) {

                @Override
                public void run() {
                    ivHead.setImageBitmap(mP1);
                    ivHead.setVisibility(View.VISIBLE);
                }
            }, 0);
            return;
        }

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
                    synchronized (mCachePost) {
                        mCachePost.put(imageUri, loadedImage);
                    }
                }
            }
        });
    }

    private View createViewHalf(CompetitionViewData competitionViewData) {

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(competitionViewData, "competition", competitionViewData.mTitleInfo.prefix);

        mCurPage = competitionViewData.mTitleInfo.curPage;
        mMaxPage = competitionViewData.mTitleInfo.maxPage;

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view, layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.pagePoiCount * SizeConfig.itemHeight);
        llLayout.addView(llContents, layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        llContents.addView(llContent, layoutParams);

        LinearLayout llPager = new PageView(GlobalContext.get(), mCurPage, mMaxPage);
        //llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        llContents.addView(llPager, layoutParams);

        llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
        llContent.setLayoutAnimationListener(new AnimationListener() {
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
        //progressBars.clear();
        mItemViews = new ArrayList<View>();
        for (int i = 0; i < competitionViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.itemHeight);
            View itemView = createHalfItemView(i, competitionViewData.mCompetitionData.mCompetitionBeans.get(i), i != SizeConfig.pageCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        return llLayout;
    }

    private View createViewNone(CompetitionViewData competitionViewData) {
        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(competitionViewData, "competition", competitionViewData.mTitleInfo.prefix);

        mCurPage = competitionViewData.mTitleInfo.curPage;
        mMaxPage = competitionViewData.mTitleInfo.maxPage;

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        llLayout.addView(llContents, layoutParams);

        LinearLayout llPager = new PageView(GlobalContext.get(), mCurPage, mMaxPage);
        //llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        llLayout.addView(llPager, layoutParams);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llContents.addView(titleViewAdapter.view, layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dividerHeight);
        llContents.addView(divider, layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.pagePoiCount * SizeConfig.itemHeight);
        llContents.addView(llContent, layoutParams);

        llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
        llContent.setLayoutAnimationListener(new AnimationListener() {
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
        //progressBars.clear();
        mItemViews = new ArrayList<View>();
        LogUtil.logd(WinLayout.logTag + "styleListViewData: ConfigUtil.getVisbileCount" + ConfigUtil.getVisbileCount());
        for (int i = 0; i < competitionViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.itemHeightPro);
            View itemView = createNoneItemView(i, competitionViewData.mCompetitionData.mCompetitionBeans.get(i), i != SizeConfig.pageCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }
		/*if (styleListViewData.count < 3){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,3-styleListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/

        return llLayout;
    }

    @Override
    public void init() {
        super.init();
//        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
        dividerHeight = 1;

        tvContentColor = Color.parseColor(LayouUtil.getString("color_main_title"));   //内容字体颜色
        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));    //编号字体颜色
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
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
        // 初始化配置，例如字体颜色等
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen) {
            tvNumSize = ViewParamsUtil.h0;    //编号字体大小
            tvNumSide = 6 * unit;    //编号布局宽高
            tvNumHorMargin = unit;    //编号左右边距
            tvContentSize = ViewParamsUtil.h1;    //内容字体大小
            iconTeamWidth = (int) (5.6 * unit);
            iconTeamHeight = (int) (5.6 * unit);
            tvTeamSize = ViewParamsUtil.h7;
            tvCompetitionSize = ViewParamsUtil.h7;
            tvPeriodSize = ViewParamsUtil.h7;
            tvGoalSize = ViewParamsUtil.h3;
            tvTeamWidth = 10 * unit;
        } else {
            tvNumSize = ViewParamsUtil.h0;    //编号字体大小
            tvNumSide = 6 * unit;    //编号布局宽高
            tvNumHorMargin = unit;    //编号左右边距
            tvContentSize = ViewParamsUtil.h1;    //内容字体大小
            iconTeamWidth = (int) (5.6 * unit);
            iconTeamHeight = (int) (5.6 * unit);
            tvTeamSize = ViewParamsUtil.h7;
            tvCompetitionSize = ViewParamsUtil.h7;
            tvPeriodSize = ViewParamsUtil.h7;
            tvGoalSize = ViewParamsUtil.h3;
            tvTeamWidth = 10 * unit;
        }
        tvCompetitionColor = Color.parseColor("#99FFFFFF");
    }

    //半屏布局参数
    private void initHalf() {
        // 初始化配置，例如字体颜色等
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen) {
            tvNumSize = ViewParamsUtil.h0;    //编号字体大小
            tvNumSide = 6 * unit;    //编号布局宽高
            tvNumHorMargin = unit;    //编号左右边距
            tvContentSize = ViewParamsUtil.h1;    //内容字体大小
            iconTeamWidth = (int) (5.6 * unit);
            iconTeamHeight = (int) (5.6 * unit);
            tvTeamSize = ViewParamsUtil.h7;
            tvCompetitionSize = ViewParamsUtil.h7;
            tvPeriodSize = ViewParamsUtil.h7;
            tvGoalSize = ViewParamsUtil.h3;
            tvTeamWidth = 10 * unit;
        } else {
            tvNumSize = ViewParamsUtil.h0;    //编号字体大小
            tvNumSide = 6 * unit;    //编号布局宽高
            tvNumHorMargin = unit;    //编号左右边距
            tvContentSize = ViewParamsUtil.h1;    //内容字体大小
            iconTeamWidth = (int) (5.6 * unit);
            iconTeamHeight = (int) (5.6 * unit);
            tvTeamSize = ViewParamsUtil.h7;
            tvCompetitionSize = ViewParamsUtil.h3;
            tvPeriodSize = ViewParamsUtil.h7;
            tvGoalSize = ViewParamsUtil.h3;
            tvTeamWidth = 10 * unit;
        }
        tvCompetitionColor = Color.parseColor("#99FFFFFF");
    }

    //无屏布局参数
    private void initNone() {
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen) {
            tvNumSize = ViewParamsUtil.h0;    //编号字体大小
            tvNumSide = 6 * unit;    //编号布局宽高
            tvNumHorMargin = unit;    //编号左右边距
            tvContentSize = ViewParamsUtil.h1;    //内容字体大小
            iconTeamWidth = (int) (5.6 * unit);
            iconTeamHeight = (int) (5.6 * unit);
            tvTeamSize = ViewParamsUtil.h7;
            tvCompetitionSize = ViewParamsUtil.h7;
            tvPeriodSize = ViewParamsUtil.h7;
            tvGoalSize = ViewParamsUtil.h3;
            tvTeamWidth = 10 * unit;
        } else {
            tvNumSize = ViewParamsUtil.h0;    //编号字体大小
            tvNumSide = 6 * unit;    //编号布局宽高
            tvNumHorMargin = unit;    //编号左右边距
            tvContentSize = ViewParamsUtil.h1;    //内容字体大小
            iconTeamWidth = (int) (5.6 * unit);
            iconTeamHeight = (int) (5.6 * unit);
            tvTeamSize = ViewParamsUtil.h7;
            tvCompetitionSize = ViewParamsUtil.h7;
            tvPeriodSize = ViewParamsUtil.h7;
            tvGoalSize = ViewParamsUtil.h3;
            tvTeamWidth = 10 * unit;
        }
        tvCompetitionColor = Color.WHITE;
    }

    @Override
    public void snapPage(boolean next) {
        LogUtil.logd("update snap " + next);
    }

    @Override
    public List<View> getFocusViews() {
        return mItemViews;
    }


    @Override
    public boolean supportKeyEvent() {
        return true;
    }


    /**
     * 是否含有动画
     *
     * @return
     */
    @Override
    public boolean hasViewAnimation() {
        return true;
    }

    @Override
    public void release() {
        super.release();
        if (mItemViews != null) {
            mItemViews.clear();
        }
		/*if (progressBars != null) {
			progressBars.clear();
		}*/
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

    private String convertDatetime(long startDate, long curDate) {
        String mDatetime;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(startDate * 1000L);
        int startYear = startCalendar.get(Calendar.YEAR);
        int startMonth = startCalendar.get(Calendar.MONTH) + 1;
        int startDay = startCalendar.get(Calendar.DAY_OF_MONTH);

        Calendar curCalendar = Calendar.getInstance();
        curCalendar.setTimeInMillis(curDate * 1000L);
        int curYear = curCalendar.get(Calendar.YEAR);
        int curMonth = curCalendar.get(Calendar.MONTH) + 1;
        int curDay = curCalendar.get(Calendar.DAY_OF_MONTH);
        int curHour = curCalendar.get(Calendar.HOUR_OF_DAY);
        int curMin = curCalendar.get(Calendar.MINUTE);


        if (startYear == curYear && startMonth == curMonth && startDay == curDay) {
            mDatetime = String.format("%02d:%02d", curHour, curMin);
        } else {
            mDatetime = String.format("%d-%d %02d:%02d", curMonth, curDay, curHour, curMin);
        }
        return mDatetime;
    }


}
