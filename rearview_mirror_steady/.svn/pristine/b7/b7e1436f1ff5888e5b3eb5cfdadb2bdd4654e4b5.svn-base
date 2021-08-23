package com.txznet.launcher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by TXZ-METEORLUO on 2018/3/15.
 * 可以设置圆角的FrameLayout
 */

public class CornerFrameLayout extends FrameLayout {
    private final RectF roundRect = new RectF();
    private float rect_adius = 10;
    private final Paint maskPaint = new Paint();
    private final Paint zonePaint = new Paint();

    public CornerFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public CornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CornerFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        zonePaint.setAntiAlias(true);
        zonePaint.setColor(Color.parseColor("#000000"));
        float density = getResources().getDisplayMetrics().density;
        rect_adius = rect_adius * density;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w = getWidth();
        int h = getHeight();
        roundRect.set(0, 0, w, h);
    }

    @Override
    public void draw(Canvas canvas) {
        // 创建图层A，绘制圆角矩阵
        canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
        canvas.drawRoundRect(roundRect, rect_adius, rect_adius, zonePaint);
        // 创建图层B，使用xfermode将图层的形状变成圆角矩阵
        canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
        // 清空图层的颜色
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        super.draw(canvas);
        // 将图层B绘制到canvas上
        canvas.restore();
    }

    public void setCorner(float adius) {
        rect_adius = adius;
        invalidate();
    }
}