package com.txznet.record.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import com.txznet.record.lib.R;


public class GradientProgressBar extends View {
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

    private Drawable mDrawable;

    private void init() {
        mDrawable =  getResources().getDrawable(R.drawable.clip_bg);
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
        mDrawable.setBounds(0, 0, right, height);
        mDrawable.setLevel((int) (10000 * (curVal * 1f / MAX_VAL)));
        mDrawable.draw(canvas);
    }
}
