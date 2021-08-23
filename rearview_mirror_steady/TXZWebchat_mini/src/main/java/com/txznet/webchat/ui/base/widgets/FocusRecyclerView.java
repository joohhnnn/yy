package com.txznet.webchat.ui.base.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.webchat.ui.base.adapter.BaseRecyclerViewAdapter;

/**
 * Created by J on 2017/5/5.
 */

public class FocusRecyclerView extends RecyclerView implements IFocusView, IFocusOperationPresenter{
    private BaseRecyclerViewAdapter mAdapter;

    public FocusRecyclerView(Context context) {
        super(context);
    }

    public FocusRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        mAdapter = (BaseRecyclerViewAdapter) adapter;
        super.setAdapter(adapter);
    }

    // 方控相关操作

    public void setCurrentFocusPosition(int position) {
        mAdapter.setCurrentFocusPosition(position);
    }

    @Override
    public boolean showDefaultSelectIndicator() {
        return false;
    }

    @Override
    public void onNavGainFocus(Object rawFocus, int operation) {
        mAdapter.onNavGainFocus(rawFocus, operation);
        smoothScrollToPosition(mAdapter.getCurrentFocusPosition());
    }

    @Override
    public void onNavLoseFocus(Object newFocus, int operation) {
        mAdapter.onNavLoseFocus(newFocus, operation);
    }

    @Override
    public boolean onNavOperation(int operation) {
        boolean consume = mAdapter.onNavOperation(operation);
        if (consume) {
            smoothScrollToPosition(mAdapter.getCurrentFocusPosition());
        }

        return consume;
    }

    @Override
    public void smoothScrollToPosition(int position) {
        if (-1 == position) {
            return;
        }
        stopScroll();
        super.smoothScrollToPosition(position);
    }
}
