package com.txznet.record.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ASUS User on 2015/7/2.
 */
public class HorizontalProgressBar extends View {
    private static final int COLOR_BACKGROUND = Color.parseColor("#000000");
    private static final int COLOR_PROGRESS = Color.parseColor("#33c0ff");

    private static final int MAX_VAL = 100;
    private int curVal = 0;

    private Paint paint;

    public HorizontalProgressBar(Context context) {
        super(context);
        init();
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(COLOR_PROGRESS);
    }

    public void setProgress(int val) {
        if (val >= 0 && val <= MAX_VAL) {
            curVal = val;
            postInvalidate();
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
        canvas.drawColor(COLOR_BACKGROUND);
        // 绘制进度
        int right = (int) (width * (curVal * 1f / MAX_VAL));
        Rect progressRect = new Rect(0, 0, right, height);
        canvas.drawRect(progressRect, paint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
