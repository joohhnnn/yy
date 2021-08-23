package com.txznet.music.ui.base.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.R;

public abstract class UnifyCheckAdapter<T> extends BaseCheckPlayerAdapter<T, UnifyCheckHolder> {


    public UnifyCheckAdapter(Context context) {
        super(context);
    }

    @Override
    protected void changePlayObj(@NonNull UnifyCheckHolder holder, int position) {
        changeVisible(holder, holder.ivPlaying);
        holder.tvName.setTextColor(getContext().getResources().getColor(R.color.red));
        holder.tvArtist.setTextColor(getContext().getResources().getColor(R.color.red_40));
//        if (!holder.tvName.hasFocus()) {
//            holder.tvName.requestFocus();
//        }
    }

    @Override
    protected void changeUnPlayingStatus(@NonNull UnifyCheckHolder holder, int position) {
        changeVisible(holder, holder.tvIndex);
        holder.tvName.setTextColor(getContext().getResources().getColor(R.color.white));
        holder.tvArtist.setTextColor(getContext().getResources().getColor(R.color.white_40));
//        holder.tvName.clearFocus();
    }


    @Override
    protected void initViewVisible(UnifyCheckHolder holder) {
        holder.tvIndex.setVisibility(View.INVISIBLE);
        holder.ivPlaying.setVisibility(View.GONE);
        if (holder.cbChecked != null) {
            holder.cbChecked.setVisibility(View.GONE);
        }
    }

    @Override
    public void OnBindViewHolder(BaseViewHolder holder, int position) {
        super.OnBindViewHolder(holder, position);


    }

    @Override
    public BaseViewHolder OnCreateViewHolder(ViewGroup parent, int viewType) {
        UnifyCheckHolder baseViewHolder = getBaseViewHolder(parent);
        if (BuildConfig.DEBUG) {
            Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",OnCreateViewHolder：" + baseViewHolder.ivPlaying.getVisibility() + ":" + mIsPlaying);
        }


        return baseViewHolder;
    }

    public abstract UnifyCheckHolder getBaseViewHolder(ViewGroup parent);
}
