package com.txznet.record.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.record.bean.BaseDisplayMsg;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.TitleView;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CompetitionListAdapter extends ChatDisplayAdapter {
    private LruCache<String, Bitmap> mCachePost = new LruCache<String, Bitmap>(10);

    public CompetitionListAdapter(Context context, List displayList) {
        super(context, displayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_competition, parent, false);
            prepareSetLayoutParams(convertView);
        }
        CompetitionItem competitionItem = (CompetitionItem) getItem(position);
        CompetitionViewData.CompetitionData.CompetitionBean competitionBean = competitionItem.mItem;

        View mDivider = ViewHolder.get(convertView, R.id.divider);
        mDivider.setVisibility(position == (getCount() - 1) ? View.INVISIBLE : View.VISIBLE);
        View layoutItem = ViewHolder.get(convertView, R.id.layout_item);

        TextView tvNum = ViewHolder.get(convertView, R.id.tv_num);
        ImageView ivHomeTeam = ViewHolder.get(convertView, R.id.ivHomeTeam);
        TextView tvHomeTeam = ViewHolder.get(convertView, R.id.tvHomeTeam);
        TextView tvCompetition = ViewHolder.get(convertView, R.id.tvCompetition);
        TextView tvGoal = ViewHolder.get(convertView, R.id.tvGoal);
        TextView tvPeriod = ViewHolder.get(convertView, R.id.tvPeriod);
        ImageView ivAwayTeam = ViewHolder.get(convertView, R.id.ivAwayTeam);
        TextView tvAwayTeam = ViewHolder.get(convertView, R.id.tvAwayTeam);


        tvNum.setText(String.valueOf(position + 1));

        loadDrawableByUrl(ivHomeTeam, competitionBean.mHomeTeam.mLogo);
        tvHomeTeam.setText(competitionBean.mHomeTeam.mName);

        tvCompetition.setText(competitionBean.mCompetition + competitionBean.mRoundType);
        tvGoal.setIncludeFontPadding(false);
        tvPeriod.setIncludeFontPadding(false);
        tvCompetition.setIncludeFontPadding(false);

        tvPeriod.setPadding((int) LayouUtil.getDimen("x16"), 2, (int) LayouUtil.getDimen("x16"), 2);
        tvPeriod.setGravity(Gravity.CENTER);
        if (TextUtils.equals("未开始", competitionBean.mPeriod)) {
            tvPeriod.setTextColor(Color.parseColor("#CCFFFFFF"));
            tvPeriod.setBackground(null);
            tvGoal.setText("VS");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(competitionBean.mStartTimeStamp * 1000L);
            tvPeriod.setText(convertDatetime(competitionItem.mCurTimeStamp, competitionBean.mStartTimeStamp));

        } else {
            tvGoal.setText(competitionBean.mHomeTeam.mGoal + ":" + competitionBean.mAwayTeam.mGoal);
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


        if (position == mFocusIndex) {
            layoutItem.setBackgroundColor(GlobalContext.get().getResources()
                    .getColor(R.color.bg_ripple_focused));
        } else {
            layoutItem.setBackgroundColor(GlobalContext.get().getResources()
                    .getColor(R.color.bg_ripple_nor));
        }

        return convertView;
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
            mDatetime = String.format("%d月%d日%02d:%02d", curMonth, curDay, curHour, curMin);
        }
        return mDatetime;
    }

    public static class CompetitionItem extends DisplayItem<CompetitionViewData.CompetitionData.CompetitionBean>{
        public long mCurTimeStamp;
    }
}
