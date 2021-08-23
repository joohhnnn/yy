package com.txznet.comm.ui.theme.test.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;

/**
 * 说明：窗口弹出和消失时的动画
 * 需要剪切画布
 * <p>
 * 已弃用，美工重新设计了动画
 *
 * @author xiaolin
 * create at 2020-11-06 14:49
 */
@Deprecated
public class SmartHandyAnimFrameLayout extends FrameLayout {

    private static final String TAG = "SmartHandyAnimFrameLayo";

    private Scroller mScroller;

    public SmartHandyAnimFrameLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public SmartHandyAnimFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SmartHandyAnimFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext(), new LinearInterpolator());
    }

    @Override
    public void draw(Canvas canvas) {
        if (mScroller.computeScrollOffset()) {
            float progress = mScroller.getCurrX() / 1000F;// 动画进度， 0-1F

//            setAlpha(progress * 1F);

            int width = getWidth();
            int height = getHeight();
            int h = (int) (height / 2 * progress);
            int w = (int) (width * progress);

            canvas.save();
            canvas.clipRect(new Rect(0, height / 2 - h, w, height / 2 + h));
            canvas.translate(0, height / 2F - h);
            super.draw(canvas);
            canvas.restore();
            postInvalidate();
        } else {
            super.draw(canvas);
        }
    }

    /**
     * @param time     动画时长
     * @param runnable 结束回调
     */
    public void startAnimIn(int time, Runnable runnable) {
        mScroller.startScroll(0, 0, 1000, 1000, time);
        invalidate();
        if (runnable != null) {
            AppLogicBase.runOnUiGround(runnable, time);
        }
    }

    /**
     * @param time     动画时长
     * @param runnable 结束回调
     */
    public void startAnimOut(int time, Runnable runnable) {
        mScroller.startScroll(1000, 1000, -1000, -1000, time);
        invalidate();
        if (runnable != null) {
            AppLogicBase.runOnUiGround(runnable, time);
        }
    }
}
