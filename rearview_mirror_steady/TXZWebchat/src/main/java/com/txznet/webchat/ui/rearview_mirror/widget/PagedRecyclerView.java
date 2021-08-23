package com.txznet.webchat.ui.rearview_mirror.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.webchat.R;
import com.txznet.webchat.ui.rearview_mirror.adapter.SessionListAdapter;


/**
 * 按页翻动的RecyclerView
 * Created by J on 2016/3/24.
 */
public class PagedRecyclerView extends RecyclerView implements IFocusOperationPresenter, IFocusView {
    // layout consts
    private static final int DESIRED_ITEM_HEIGHT = R.dimen.y90;
    private static final int DESIRED_ITEM_GAP = R.dimen.y4;
    // turning page threshold
    private static final float PAGE_TURNING_THRESHOLD = 0.2f;
    // layout consts converted into pixels
    private float mDesiredItemHeight;
    private float mDesiredItemGap;
    // actual item gap
    private int mItemGap;
    // the actual size of this RecyclerView
    private int mWidth;
    private int mHeight;
    // page variables
    private int mPageSize;
    private int mCurrentPage;
    private int mPageCount;
    private float mPageTurningThreshold = PAGE_TURNING_THRESHOLD;

    //current touched view, used for adding click features
    private View mCurrentView;

    float mLastTouchY = -1; // used for calculating scroll distance
    private Context mContext;

    public PagedRecyclerView(Context context) {
        this(context, null);
    }

    public PagedRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initAttrs(attrs);
        init(context);

    }

    private void initAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.PagedRecyclerView);
            mDesiredItemHeight = ta.getDimension(R.styleable.PagedRecyclerView_prv_desired_item_height, mContext.getResources().getDimension(DESIRED_ITEM_HEIGHT));
            mDesiredItemGap = ta.getDimension(R.styleable.PagedRecyclerView_prv_desired_item_gap, mContext.getResources().getDimension(DESIRED_ITEM_GAP));
            mPageTurningThreshold = ta.getFloat(R.styleable.PagedRecyclerView_prv_page_turning_threshold, PAGE_TURNING_THRESHOLD);
            ta.recycle();
        }
    }

    public void scrollToPage(int index) {
        LinearLayoutManager lm = (LinearLayoutManager) this.getLayoutManager();

        //calculate scrolled distance
        int firstCompletlyVisibleIndex = lm.findFirstCompletelyVisibleItemPosition();
        View v = lm.findViewByPosition(firstCompletlyVisibleIndex);
        if (null == v) {
            return;
        }

        float scrolledDistance = firstCompletlyVisibleIndex * mDesiredItemHeight + (firstCompletlyVisibleIndex + 1) * mItemGap - v.getTop();

        //compute distance to scroll to
        float destination = index * mPageSize * (mDesiredItemHeight + mItemGap);
        int distance = (int) (destination - scrolledDistance + 1);

        //scroll
        this.stopScroll();
        this.smoothScrollBy(0, distance);
        mCurrentPage = index;
    }

    private void init(Context context) {
        final PagedRecyclerView mThis = this;
        this.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mThis.getViewTreeObserver().removeOnPreDrawListener(this);
                mWidth = mThis.getMeasuredWidth();
                mHeight = mThis.getMeasuredHeight();

                initLayoutParams();
                return true;
            }
        });

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                boolean consume = false;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mLastTouchY < 0) {
                            mLastTouchY = event.getY();
                        }

                        break;

                    case MotionEvent.ACTION_UP:
                        judgePageScroll(event.getY() - mLastTouchY);
                        mLastTouchY = -1;
                        consume = true;
                        break;
                }

                return consume;
            }
        });
    }

    private void judgePageScroll(float scrolledDistance) {
        //refresh item count
        if (this.getAdapter() != null) {
            int count = this.getAdapter().getItemCount();
            mPageCount = count / mPageSize + 1;
        }

        int targetPage = mCurrentPage;

        if (scrolledDistance > mHeight * mPageTurningThreshold) {
            // previous page
            targetPage--;
            if (targetPage < 0) {
                targetPage = 0;
            }
        } else if (-scrolledDistance > mHeight * mPageTurningThreshold) {
            // next page
            targetPage++;

            if (targetPage > mPageCount - 1) {
                targetPage = mPageCount - 1;
            }
        }

        scrollToPage(targetPage);
    }

    private void initLayoutParams() {
        // get optimized page size
        int rawPageSize = (int) (mHeight / mDesiredItemHeight);
        if ((rawPageSize * mDesiredItemHeight + (rawPageSize + 1) * mDesiredItemGap) > mHeight) {
            mPageSize = rawPageSize - 1;
        } else {
            mPageSize = rawPageSize;
        }
        // calculate item gap
        mItemGap = (int) ((mHeight - mPageSize * mDesiredItemHeight) / (mPageSize + 1));
        // set optimized item gap
        this.addItemDecoration(new PagedItemDecoration(mItemGap));
    }

    class PagedItemDecoration extends ItemDecoration {
        private int space;

        public PagedItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildPosition(view) == 0)
                outRect.top = space;
        }
    }

    // 导航按键支持相关代码

    @Override
    public boolean onNavOperation(int operation) {
        switch (operation) {
            case FocusSupporter.NAV_BTN_CLICK:
                ((SessionListAdapter) getAdapter()).performClick();
                return true;


            case FocusSupporter.NAV_BTN_PREV:
            case FocusSupporter.NAV_BTN_UP:
                scrollToFocusPage(((SessionListAdapter) getAdapter()).performPrev());
                return true;


            case FocusSupporter.NAV_BTN_NEXT:
            case FocusSupporter.NAV_BTN_DOWN:
                scrollToFocusPage(((SessionListAdapter) getAdapter()).performNext());
                return true;

        }

        return false;
    }

    @Override
    public boolean showDefaultSelectIndicator() {
        return false;
    }

    @Override
    public void onNavGainFocus(Object rawFocus, int operation) {
        ((SessionListAdapter) getAdapter()).setFocusIndex(mCurrentPage * mPageSize);
    }

    @Override
    public void onNavLoseFocus(Object newFocus, int operation) {

    }

    private void scrollToFocusPage(int index) {
        int page = index / mPageSize;

        if (page != mCurrentPage) {
            scrollToPage(page);
        }
    }
}
