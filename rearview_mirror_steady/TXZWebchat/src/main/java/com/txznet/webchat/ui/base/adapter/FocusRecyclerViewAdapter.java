package com.txznet.webchat.ui.base.adapter;

import android.support.v7.widget.RecyclerView;

/**
 * 支持焦点控制的RecyclerView基类
 * 提供了焦点相关操作的支持，实现类需要重写指定方法提供layout方向，行、列数量， 必要时重写基类
 * Created by J on 2017/5/5.
 */

public abstract class FocusRecyclerViewAdapter extends RecyclerView.Adapter {
    public abstract void onNavGainFocus(Object rawFocus, int operation);
    public abstract void onNavLoseFocus(Object newFocus, int operation);
    public abstract boolean onNavOperation(int operation);

    public abstract int getCurrentFocusPosition();
    public abstract void setCurrentFoucsPosition(int position);
}
