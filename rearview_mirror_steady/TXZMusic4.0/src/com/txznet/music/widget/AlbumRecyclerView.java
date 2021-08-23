package com.txznet.music.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.txznet.music.albumModule.ui.adapter.ItemAlbumFragmentAdapter;
import com.txznet.music.image.ImageFactory;

/**
 * 显示专辑列表的RecyclerView。
 * <p>
 * Created by Terry on 2017/5/3.
 */

public class AlbumRecyclerView extends NavRecyclerView {

    private static final float FLING_SCALE_DOWN_FACTOR = 0.5f; // 减速因子
    private static final int FLING_MAX_VELOCITY = 8000; // 最大顺时滑动速度

    public AlbumRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        addOnScrollListener(new MyScrollListener());
    }

    private static class MyScrollListener extends OnScrollListener {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            try {
                switch (newState) {
                    case SCROLL_STATE_IDLE:
                        ImageFactory.getInstance().resumeRequests(recyclerView.getContext());
                        break;
                    case SCROLL_STATE_SETTLING:
                    case SCROLL_STATE_DRAGGING:
                        ImageFactory.getInstance().pauseRequests(recyclerView.getContext());
                        break;
                }
            } catch (Exception e) {

            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    }

    public AlbumRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumRecyclerView(Context context) {
        this(context, null);
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        velocityX = solveVelocity(velocityX);
        velocityY = solveVelocity(velocityY);
        return super.fling(velocityX, velocityY);
    }

    private int solveVelocity(int velocity) {
        if (velocity > 0) {
            return Math.min(velocity, FLING_MAX_VELOCITY);
        } else {
            return Math.max(velocity, -FLING_MAX_VELOCITY);
        }
    }

    public void setAlbumAdapter(ItemAlbumFragmentAdapter adapter) {
        setAdapter(adapter);
    }
}
