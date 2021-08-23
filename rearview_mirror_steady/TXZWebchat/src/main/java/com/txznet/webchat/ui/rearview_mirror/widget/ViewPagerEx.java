package com.txznet.webchat.ui.rearview_mirror.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.txznet.webchat.R;

/**
 * 继承ViewPager，添加禁止滑动的选项
 *
 * @author meteorluo
 * @date 2015年3月30日
 * @company txznet
 */
public class ViewPagerEx extends ViewPager {
    private boolean isAllowScroll = true;

    public ViewPagerEx(Context context) {
        this(context, null);
    }

    public ViewPagerEx(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerEx);
        for (int i = 0; i < a.getIndexCount(); i++) {
            int index = a.getIndex(i);
            switch (index) {
                case R.styleable.ViewPagerEx_allow_scroll:
                    isAllowScroll = a.getBoolean(index, true);
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 设置是否可以左右滑动
     *
     * @param allowScroll
     */
    public void setAllowScroll(boolean allowScroll) {
        this.isAllowScroll = allowScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        if (!this.isAllowScroll) {
            return false;
        }

        return super.onInterceptTouchEvent(arg0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (!this.isAllowScroll) {
            return false;
        }

        return super.onTouchEvent(arg0);
    }
}
