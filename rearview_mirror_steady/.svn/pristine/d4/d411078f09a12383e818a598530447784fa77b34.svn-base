package com.txznet.launcher.ui.widget;

import com.txznet.loader.AppLogic;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager {
    public MyViewPager(Context context) {
        this(context, null);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    float down_x;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            down_x = event.getX();
        } else if (MotionEvent.ACTION_MOVE == event.getAction()) {
            // 处理嵌套view中点击事件导致的不能滑动
            if (Math.abs(event.getX() - down_x) > TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 10, AppLogic.getApp()
                            .getResources().getDisplayMetrics())) {
                return true;
            }
        }
        return super.onInterceptTouchEvent(event);
    }
}
