package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.CompetitionBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICompetitionView;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.resholder.R;
import com.txznet.txz.util.runnables.Runnable1;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * 赛事列表
 * <p>
 * 2020-08-18
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class CompetitionView extends ICompetitionView {

    private static CompetitionView sInstance = new CompetitionView();
    private static final String[] WEEK = {
            "周日", "周一", "周二", "周三", "周四", "周五", "周六",
    };

    private List<View> mItemViews;

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
    public ExtViewAdapter getView(ViewData data) {
        CompetitionViewData competitionViewData = (CompetitionViewData) data;
        WinLayout.getInstance().vTips = competitionViewData.vTips;

        View view = createViewNone(competitionViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = CompetitionView.getInstance();
        return viewAdapter;
    }

    private View createViewNone(CompetitionViewData data) {
        ArrayList<CompetitionBean> competitionBeans = data.mCompetitionData.mCompetitionBeans;
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = data.mTitleInfo.maxPage;
        int curPage = data.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        mItemViews = new ArrayList<>();
        for (int i = 0; i < data.count; i++) {
            CompetitionBean row = competitionBeans.get(i);

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

    private View createItemView(Context context, int position, CompetitionBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.competitio_view_item, (ViewGroup) null);

        if(row == null){
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvCompetition = view.findViewById(R.id.tvCompetition);
        ImageView ivHomeTeamLogo = view.findViewById(R.id.ivHomeTeamLogo);
        ImageView ivAwayTeamLogo = view.findViewById(R.id.ivAwayTeamLogo);
        TextView tvHomeTeamName = view.findViewById(R.id.tvHomeTeamName);
        TextView tvAwayTeamName = view.findViewById(R.id.tvAwayTeamName);
        TextView tvGoal = view.findViewById(R.id.tvGoal);       // 比分或时间
        TextView tvState = view.findViewById(R.id.tvState);     // 比赛状态，未开始、进行中、已结束
        View divider = view.findViewById(R.id.divider);

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(new Date(row.mStartTimeStamp * 1000L));
        String strStartTime = new SimpleDateFormat("MM-dd", Locale.getDefault()).format(startCalendar.getTime())
                + WEEK[startCalendar.get(Calendar.DAY_OF_WEEK)-1];
        tvDate.setText(strStartTime);
        tvCompetition.setText(row.mCompetition);
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

        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        return view;
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

    @Override
    public void init() {
        super.init();

    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        initNone();
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
    }

    @Override
    public void updateItemSelect(int index) {
        LogUtil.logd(WinLayout.logTag + "train updateItemSelect " + index);
        showSelectItem(index);

    }

    //无屏布局参数
    private void initNone() {

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
