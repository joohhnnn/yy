package com.txznet.music.ui.helper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.music.ui.helper.entity.BaseEntity;

/**
 * Created by Bond on 2016/4/2.
 */
public abstract class BaseViewHolder<T extends BaseEntity> extends RecyclerView.ViewHolder {
    private Context context;

    public BaseViewHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;
        findView();
    }

    public BaseViewHolder(Context context, int itemViewResId, ViewGroup parent) {
        this(context, LayoutInflater.from(context).inflate(itemViewResId, parent, false));
    }

    public abstract void findView();

    public void bindData(int adapPos, T t) {
        if (t.mOnClickListener != null) {
            itemView.setOnClickListener(t.mOnClickListener);
        }
    }


    public Context getContext() {
        return context;
    }
}