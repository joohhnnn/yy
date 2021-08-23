package com.txznet.music.ui.layout;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.txznet.comm.remote.util.LogUtil;

/**
 * Created by Terry on 2017/6/9.
 */

public class TXZGridLayoutManager extends GridLayoutManager {
    public TXZGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TXZGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public TXZGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
            LogUtil.loge("error onLayoutChildren", e);
        }
    }
}
