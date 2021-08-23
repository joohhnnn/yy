package com.txznet.comm.ui.view;

import com.txznet.comm.ui.IKeepClass;
import com.txznet.comm.ui.util.LayouUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;


public class GradientProgressBar extends View  implements IKeepClass{
    private static final int MAX_VAL = 100;
    private int curVal = 0;

    public GradientProgressBar(Context context) {
        super(context);
        init();
    }

    public GradientProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
    }

    public void setProgress(int val) {
        if (val >= 0 && val <= MAX_VAL) {
            curVal = val;
            if (Looper.myLooper() == Looper.getMainLooper()) {
                invalidate();
            } else {
                postInvalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        int width = getWidth();
        int height = getHeight();

        // 绘制背景
        canvas.drawColor(Color.TRANSPARENT);
        // 绘制进度
        int right = (int) (width * (curVal * 1f / MAX_VAL));
        Drawable mDrawable = LayouUtil.getDrawable("gradient_bg");
        mDrawable.setBounds(0, 0, right, height);
        mDrawable.setLevel((int) (10000 * (curVal * 1f / MAX_VAL)));
        mDrawable.draw(canvas);
    }
}
