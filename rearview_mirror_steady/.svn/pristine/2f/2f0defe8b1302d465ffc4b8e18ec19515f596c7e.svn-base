package com.txznet.music.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EdgeEffect;

import com.txznet.comm.remote.util.LogUtil;

import java.lang.reflect.Field;

/**
 * Created by brainBear on 2018/2/24.
 */

public class TXZViewPager extends ViewPager {

    private boolean isCanScroll;

    public TXZViewPager(Context context) {
        super(context);
        init();
    }

    public TXZViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
//        initViewPager();
    }

    public void setScanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isCanScroll && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isCanScroll && super.onTouchEvent(ev);
    }


    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item, false);
    }

//    EdgeEffect leftEdge;
//    EdgeEffect rightEdge;
//
//    private void initViewPager() {
//        try {
//            Field leftEdgeField = ViewPager.class.getDeclaredField("mLeftEdge");
//            Field rightEdgeField = ViewPager.class.getDeclaredField("mRightEdge");
//            if (leftEdgeField != null && rightEdgeField != null) {
//                leftEdgeField.setAccessible(true);
//                rightEdgeField.setAccessible(true);
//                leftEdge = (EdgeEffect) leftEdgeField.get(this);
//                rightEdge = (EdgeEffect) rightEdgeField.get(this);
//            }
//            LogUtil.logd("test::::" + leftEdgeField + " , " + rightEdgeField);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    public void onPageScrolled(int arg0, float arg1, int arg2) {
//        super.onPageScrolled(arg0, arg1, arg2);
//        if (leftEdge != null && rightEdge != null) {
//            leftEdge.finish();
//            rightEdge.finish();
//            leftEdge.setSize(0, 0);
//            rightEdge.setSize(0, 0);
//        }
//    }
}
