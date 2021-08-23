package com.txznet.music.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by telenewbie on 2017/12/14.
 * 通用的网络请求的界面,涉及错误界面,空数据界面
 */

public class NetRecycleView extends FrameLayout {
    View emptyView, errorView, contentView;
    RecyclerView recyclerView;
    int id;

    public NetRecycleView(@NonNull Context context) {
        super(context);
        init();
    }

    public NetRecycleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NetRecycleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    public void setEmptyView(View view) {
        emptyView = view;
    }

    public void setErrorView(View view) {
        errorView = view;
    }

    public void setContentView(View view) {
        contentView = view;
    }

    public void setRecycleViewId(int id) {
        this.id = id;
    }

    public RecyclerView getRecycleView() {
        if (null != contentView && null == recyclerView) {
            recyclerView = (RecyclerView) contentView.findViewById(id);
        }
        return recyclerView;
    }

    private void showView(View view) {
        if (null != view && !view.isShown()) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void hiddenView(View view) {
        if (null != view && view.isShown()) {
            view.setVisibility(View.GONE);
        }
    }


    /**
     * 加载更多
     */
    public void loadMore() {

    }

    public void showEmptyView() {
        showView(emptyView);
        hiddenView(contentView);
        hiddenView(errorView);
    }

    public void showErrorView() {
        hiddenView(emptyView);
        hiddenView(contentView);
        showView(errorView);
    }

    public void notifyDataSetChanged() {
        hiddenView(emptyView);
        hiddenView(errorView);
        showView(contentView);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

}
