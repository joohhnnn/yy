package com.txznet.music.ui.setting;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * author：Drawthink
 * describe：BaseViewHolder
 * date: 2017/5/22
 */

public abstract class BaseViewHolder extends RecyclerView.ViewHolder {

    public static final int VIEW_TYPE_PARENT = 1;
    public static final int VIEW_TYPE_CHILD = 2;

    public ViewGroup childView;

    public ViewGroup groupView;

    public BaseViewHolder(View itemView, int viewType) {
        super(itemView);
        switch (viewType) {
            case VIEW_TYPE_PARENT:
                groupView = (ViewGroup) itemView;
                break;
            case VIEW_TYPE_CHILD:
                childView = (ViewGroup) itemView;
                break;
        }
    }


}