package com.txznet.music.widget.flowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.text.TextUtilsCompat;
import android.util.AttributeSet;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.music.R;
import com.txznet.music.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlowLayout2 extends ViewGroup {
    private static final String TAG = "FlowLayout";
    private static final int LEFT = -1;
    private static final int CENTER = 0;
    private static final int RIGHT = 1;

    protected List<List<View>> mAllViews = new ArrayList<List<View>>();
    protected List<Integer> mLineHeight = new ArrayList<Integer>();
    protected List<Integer> mLineWidth = new ArrayList<Integer>();
    protected List<Integer> mPageCounts = new ArrayList<Integer>();
    private int mGravity;
    private List<View> lineViews = new ArrayList<>();
    private int mMaxHeight = -1;
    private int mCurrentPage = 0;

    public FlowLayout2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.TagFlowLayout);
        mGravity = ta.getInt(R.styleable.TagFlowLayout_tag_gravity, LEFT);
        int layoutDirection = TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault());
        if (layoutDirection == LayoutDirection.RTL) {
            if (mGravity == LEFT) {
                mGravity = RIGHT;
            } else {
                mGravity = LEFT;
            }
        }
        ta.recycle();
    }

    public FlowLayout2(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FlowLayout2(Context context) {
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

        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                if (i == cCount - 1) {
                    width = Math.max(lineWidth, width);
                    height += lineHeight;
                }
                continue;
            }
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
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }
            if (i == cCount - 1) {
                width = Math.max(lineWidth, width);
                height += lineHeight;
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
        if (mPageCounts.size() > mCurrentPage) {
            for (int i = 0; i < mCurrentPage; i++) {
                showTotal += mPageCounts.get(i);
            }
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
        if (pageIndex >= mPageCounts.size() || pageIndex < 0) {
            return 0;
        }
        return mPageCounts.get(pageIndex);
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public int getNextPage() {
        if (CollectionUtils.isNotEmpty(mPageCounts)) {
            if (mPageCounts.size()>getCurrentPage()){
                return getCurrentPage() + 1;
            } else if (mPageCounts.size() > 0) {

            }

            if ((getAllSize(getCurrentPage()) + mPageCounts.get(getCurrentPage())) >= getChildCount()) {
                return 0;
            } else {
                return getCurrentPage() + 1;
            }
        }

        if (getCurrentPage() + 1 >= mPageCounts.size()) {
            return 0;
        }
        return getCurrentPage() + 1;
    }

    public int getPrevPage() {
        if (getCurrentPage() - 1 < 0) {
            if (mPageCounts.size() > 0) {
                return mPageCounts.size() - 1;
            }
            return 0;
        }
        return getCurrentPage() - 1;
    }

    public void changeToNextPage() {
        mCurrentPage = getNextPage();
        requestLayout();
    }

    public int getPageIndex(int pageIndex) {
        int index = 0;
        if (CollectionUtils.isNotEmpty(mPageCounts)) {
            if (mPageCounts.size() > pageIndex) {
                for (int i = 0; i < pageIndex; i++) {
                    index += mPageCounts.get(i);
                }
            }
        }
        return index;
    }

    public void savePageCoutsInfo(int pageCount) {
        if (mPageCounts.size() <= mCurrentPage) {
            mPageCounts.add(pageCount);
        }
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

        int cCount = getChildCount();

        int startIndex = getStartIndex();
        int endIndex = getEndIndex();

        int totalHeight = 0;
        int pageCount = 0;
        boolean isAdded = false;
        int hasShowCount = 0;
        if (needChangePage()) {
//            hasShowCount = getShowTotal();
//            if (mPageCounts.size() > getCurrentPage()) {
//                pageCount = mPageCounts.get(getCurrentPage());
//            }
            // TODO: 2018/5/12 有没有一种方式能够非常快速的对已经显示过的数据进行隐藏？
            //隐藏上一页的数据
            //隐藏之前的视图
            hidePrevPageViews();
//显示当前页面的视图
            showCurrentPageViews();
        }

        Log.d(TAG, "onLayout: " + startIndex + "," + endIndex);
        for (int i = startIndex; i < endIndex; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) continue;
            pageCount++;
            MarginLayoutParams lp = (MarginLayoutParams) child
                    .getLayoutParams();

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth + lineWidth + lp.leftMargin + lp.rightMargin > width - getPaddingLeft() - getPaddingRight()) {
                mLineHeight.add(lineHeight);
                mAllViews.add(lineViews);
                mLineWidth.add(lineWidth);
                totalHeight += lineHeight;
                Log.d(TAG, "onLayout: test:" + lineHeight + "," + totalHeight + "," + mMaxHeight);
                if (needChangePage()) {
                    if (mMaxHeight < totalHeight) {
                        savePageCoutsInfo(pageCount - lineViews.size());
                        pageCount = 0;
                        isAdded = true;
                        break;
                    } else if (mMaxHeight == totalHeight) {
                        savePageCoutsInfo(pageCount);
                        pageCount = 0;
                        isAdded = true;
                        break;
                    }
                }


                lineWidth = 0;
                lineHeight = childHeight + lp.topMargin + lp.bottomMargin;
                lineViews = new ArrayList<View>();
            }
            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin
                    + lp.bottomMargin);
            lineViews.add(child);

        }
        if (!isAdded) {
            mLineHeight.add(lineHeight);
            mLineWidth.add(lineWidth);
            mAllViews.add(lineViews);
        }

        if (needChangePage() && pageCount > 0) {
            savePageCoutsInfo(pageCount);
        }

        Log.d(TAG, "onLayout: " + mPageCounts.toString());


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

    private void showCurrentPageViews() {
        if (CollectionUtils.isNotEmpty(mPageCounts)) {
            if (mPageCounts.size() > getCurrentPage()) {
                int startIndex = getAllSize(getCurrentPage());//TODO:
                int endIndex = mPageCounts.get(getCurrentPage());
                Log.d(TAG, "showCurrentPageViews: " + startIndex + "," + endIndex);
                for (int i = startIndex; i < endIndex; i++) {
                    getChildAt(i).setVisibility(VISIBLE);
                }
            }
        }
    }

    /**
     * 获取当前页再整个View中的索引
     */
    private int getAllSize(int pageIndex) {
        if (pageIndex > 0 && CollectionUtils.isNotEmpty(mPageCounts)) {
            int index = 0;
            if (mPageCounts.size() > pageIndex) {
                for (int i = 0; i < pageIndex; i++) {
                    index += mPageCounts.get(i);
                }
            } else {
                for (int i = 0; i < mPageCounts.size(); i++) {
                    index += mPageCounts.get(i);
                }
            }
        }
        return 0;
    }

    private void hidePrevPageViews() {
        int prevPage = getPrevPage();
        if (CollectionUtils.isNotEmpty(mPageCounts)) {
            if (mPageCounts.size() > prevPage) {
                int startIndex = getAllSize(prevPage);//TODO:
                int endIndex = mPageCounts.get(prevPage);
                Log.d(TAG, "hidePrevPageViews: " + startIndex + "," + endIndex);
                for (int i = startIndex; i < endIndex; i++) {
                    getChildAt(i).setVisibility(INVISIBLE);
                }
            }
        }
    }

    public boolean needChangePage() {
        return mMaxHeight > 0;
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

    public int getEndIndex() {
        if (mMaxHeight > 0) {
            if (!CollectionUtils.isEmpty(mPageCounts)) {
                if (mPageCounts.size() > mCurrentPage) {
                    return mPageCounts.get(mCurrentPage);
                } else {
                    int endIndex = 0;
                    for (int i = 0; i < mPageCounts.size(); i++) {
                        endIndex += i;
                    }
                    return getChildCount() - endIndex;
                }
            }
        }
        return getChildCount();
    }

    public int getStartIndex() {
        int startIndex = 0;
        if (mMaxHeight > 0) {
            if (!CollectionUtils.isEmpty(mPageCounts)) {
                if (mPageCounts.size() >= mCurrentPage - 1) {
                    for (int i = 0; i < mCurrentPage - 1; i++) {
                        startIndex += mPageCounts.get(i);
                    }
                }
            }
        }
        return startIndex;
    }
}
