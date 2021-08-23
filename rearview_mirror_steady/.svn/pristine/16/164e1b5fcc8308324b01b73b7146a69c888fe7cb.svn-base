package com.txznet.music.utils;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.txznet.comm.remote.util.LogUtil;

public class SpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int left;
    private int bottom;
    private int right;

    public SpaceItemDecoration(int left, int bottom, int right) {
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        LogUtil.d("SpaceItemDecoration", "bottom" + bottom + "||left:" + left);
        if (parent.getChildLayoutPosition(view) % 2 == 1) {
            LogUtil.d("SpaceItemDecoration", "右");
            outRect.set(right, 0, left, bottom);
        } else {
            LogUtil.d("SpaceItemDecoration", "左");
            outRect.set(left, 0, right , bottom);
        }
    }

}