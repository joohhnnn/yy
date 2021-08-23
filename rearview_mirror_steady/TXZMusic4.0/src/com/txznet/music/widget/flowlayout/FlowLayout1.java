package com.txznet.music.widget.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.music.R;

import java.util.ArrayList;
import java.util.List;

public class FlowLayout1 extends ViewGroup {
    private static final String TAG = "FlowLayout";
    private static final int LEFT = -1;
    private static final int CENTER = 0;
    private static final int RIGHT = 1;

    protected List<List<View>> mAllViews = new ArrayList<List<View>>();
    protected List<Integer> mLineHeight = new ArrayList<Integer>();
    protected List<Integer> mLineWidth = new ArrayList<Integer>();
    protected List<Integer> mPageCount = new ArrayList<Integer>();
    private int mGravity;
    private List<View> lineViews = new ArrayList<>();
    private int mMaxHeight = -1;
    private int mCurrentPage = 0;

    public FlowLayout1(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        mGravity = ta.getInt(R.styleable.TagFlowLayout_tag_gravity, LEFT);
        ta.recycle();
    }

    public FlowLayout1(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout1(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        // wrap_content
        int width = 0;
        int height = 0;

        int lineWidth = 0;
        int lineHeight = 0;

        int cCount = getChildCount();
        int pageCount = 0;
        int lineItemCount = 0;// 一行的个数
        int lastItemCount = 0;//上一行的个数
        mPageCount.clear();


        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                if (i == cCount - 1) {
                    width = Math.max(lineWidth, width);
                    height += lineHeight;
                }
                continue;
            }
            pageCount++;
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();

            int childWidth = child.getMeasuredWidth() + lp.leftMargin
                    + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin
                    + lp.bottomMargin;

            if (lineWidth + childWidth > sizeWidth - getPaddingLeft() - getPaddingRight()) {
                width = Math.max(width, lineWidth);
                lineWidth = childWidth;
                height += lineHeight;
                lineHeight = childHeight;
                lastItemCount = lineItemCount;
                lineItemCount = 1;
            } else {
                lineItemCount++;
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            if (i == cCount - 1) {
                width = Math.max(lineWidth, width);
                height += lineHeight;
            }

            if (mMaxHeight > 0) {
                if (height >= mMaxHeight * (mPageCount.size() + 1)) {//?
                    mPageCount.add(pageCount - 1 - lastItemCount);
                    pageCount = 1 + lastItemCount;//此时的height是多了一个之后，再比较产生的。所以此时的pagecount应该从1开始计数
                    lastItemCount = 0;
                }
            }
        }
        if (mMaxHeight > 0) {
            if (pageCount != 0) {
                mPageCount.add(pageCount);
                pageCount = 0;
            }
            for (int i = 0; i < mPageCount.size(); i++) {
                Log.d(TAG, "onMeasure: " + i + "," + mPageCount.get(i));

            }
        }


        int tempHeight = modeHeight == MeasureSpec.EXACTLY ? sizeHeight : height + getPaddingTop() + getPaddingBottom();
        if (mMaxHeight > 0 && mMaxHeight < tempHeight) {
            tempHeight = mMaxHeight + getPaddingTop() + getPaddingBottom();
        }

        setMeasuredDimension(
                //
                modeWidth == MeasureSpec.EXACTLY ? sizeWidth : width + getPaddingLeft() + getPaddingRight(),
                tempHeight//
        );

    }

    /**
     * 获取已经显示的个数
     *
     * @return
     */
    public int getShowTotal() {
        int showTotal = 0;
        for (int i = 0; i < mCurrentPage; i++) {
            showTotal += mPageCount.get(i);
        }
        return showTotal;
    }

    /**
     * 获取任意一页显示的个数
     *
     * @param pageIndex
     * @return
     */
    public int getPageCount(int pageIndex) {
        if (pageIndex >= mPageCount.size() || pageIndex < 0) {
            return 0;
        }
        return mPageCount.get(pageIndex);
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public int getNextPage() {
        if (getCurrentPage() + 1 >= mPageCount.size()) {
            return 0;
        }
        return getCurrentPage() + 1;
    }

    public int getPrevPage() {
        if (getCurrentPage() - 1 < 0) {
            return mPageCount.size() - 1;
        }
        return getCurrentPage() - 1;
    }

    public void changeToNextPage() {
        mCurrentPage = getNextPage();
        mAllViews.clear();
        requestLayout();
    }

    public int getPageIndex(int pageIndex) {
        int index = 0;
        for (int i = 0; i < pageIndex; i++) {
            index += mPageCount.get(i);
        }
        return index;
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();
        mLineWidth.clear();
        lineViews.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

//        int cCount = getChildCount();

        int initIndex = 0;//getShowTotal();
        int pageCount = getChildCount();

        int hasShowCount = 0;
        if (mMaxHeight > 0) {
            hasShowCount = getShowTotal();
            if (mPageCount.size() > getCurrentPage()) {
                pageCount = mPageCount.get(getCurrentPage());
            }
            // TODO: 2018/5/12 有没有一种方式能够非常快速的对已经显示过的数据进行隐藏？
            //隐藏上一页的数据
            int hideIndex = getPageIndex(getPrevPage());
            for (int i = 0; i < mPageCount.get(getPrevPage()); i++) {
                getChildAt(hideIndex + i).setVisibility(INVISIBLE);
            }
            int showIndex = getPageIndex(getCurrentPage());
            //显示下一页的数据
            for (Integer i = 0; i < mPageCount.get(getCurrentPage()); i++) {
                getChildAt(showIndex + i).setVisibility(VISIBLE);
            }
        }


        for (int i = initIndex; i < pageCount; i++) {
            int childIndex = i;
            if (getCurrentPage() != 0) {
                childIndex = hasShowCount + i;
            }
            View child = getChildAt(childIndex);
            if (child.getVisibility() == View.GONE) continue;
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin > width - getPaddingLeft() - getPaddingRight()) {
                mLineHeight.add(lineHeight);
                mAllViews.add(lineViews);
                mLineWidth.add(lineWidth);

                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                lineViews = new ArrayList<View>();
            }
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin
                    + lp.bottomMargin);
            lineViews.add(child);

        }
        mLineHeight.add(lineHeight);
        mLineWidth.add(lineWidth);
        mAllViews.add(lineViews);


        int left = getPaddingLeft();
        int top = getPaddingTop();

        int lineNum = mAllViews.size();

        for (int i = 0; i < lineNum; i++) {
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);

            // set gravity
            int currentLineWidth = this.mLineWidth.get(i);
            switch (this.mGravity) {
                case LEFT:
                    left = getPaddingLeft();
                    break;
                case CENTER:
                    left = (width - currentLineWidth) / 2 + getPaddingLeft();
                    break;
                case RIGHT:
                    left = width - currentLineWidth + getPaddingLeft();
                    break;
            }

            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }

                MarginLayoutParams lp = (MarginLayoutParams) child
                        .getLayoutParams();

                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);

                left += child.getMeasuredWidth() + lp.leftMargin
                        + lp.rightMargin;
            }
            top += lineHeight;
        }

    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected LayoutParams generateLayoutParams(LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    /**
     * 设置控件最大的高度，如果内容总和的高度大于最大的高度则对内容进行裁剪
     * 如果值设为负数，则认为最大高度为无限，即没有最大高度的限制
     */
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
    }
}
