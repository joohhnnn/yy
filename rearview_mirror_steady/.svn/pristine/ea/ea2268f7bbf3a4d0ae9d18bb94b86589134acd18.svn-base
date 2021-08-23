package com.txznet.music.baseModule.ui;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.txznet.music.R;

import java.util.Observable;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by telenewbie on 2017/12/15.45t
 */

public abstract class BaseSwipeLoadFragment extends BaseFragment implements OnLoadMoreListener, OnRefreshListener {
    @Bind(R.id.swipe_target)
    RecyclerView swipeTarget;
    @Bind(R.id.swipeToLoadLayout)
    protected SwipeToLoadLayout swipeToLoadLayout;


    public abstract RecyclerView.Adapter getAdapter();


    @Override
    public void bindViews() {
        swipeTarget.setAdapter(getAdapter());
    }

    @Override
    public void initListener() {
        swipeToLoadLayout.setOnLoadMoreListener(this);
        swipeToLoadLayout.setOnRefreshListener(this);
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_swipe_load_layout;
    }

    @Override
    public void onLoadMore() {
        swipeToLoadLayout.setLoadingMore(false);
    }

    @Override
    public void onRefresh() {
        swipeToLoadLayout.setRefreshing(false);
    }
}
