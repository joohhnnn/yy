package com.txznet.music.util;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * https://www.jianshu.com/p/7110bedfdb5e
 */
public class SnappedLinearLayoutManager extends LinearLayoutManager {

    public SnappedLinearLayoutManager(Context context) {
        super(context);
    }

    public SnappedLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public SnappedLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        SnappedSmoothScroller snappedSmoothScroller = new SnappedSmoothScroller(recyclerView.getContext());
        snappedSmoothScroller.setTargetPosition(position);
        startSmoothScroll(snappedSmoothScroller);
    }

    class SnappedSmoothScroller extends LinearSmoothScroller {

        public SnappedSmoothScroller(Context context) {
            super(context);
        }

        @Nullable
        @Override
        public PointF computeScrollVectorForPosition(int targetPosition) {
            return SnappedLinearLayoutManager.this.computeScrollVectorForPosition(targetPosition);
        }

        @Override
        protected int getVerticalSnapPreference() {
            return SNAP_TO_START;//设置滚动位置
        }

        @Override
        protected int getHorizontalSnapPreference() {
            return SNAP_TO_START;
        }
    }
}