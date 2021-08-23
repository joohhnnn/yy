package com.txznet.launcher.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;

/**
 * Created by Anudorannador on 2018/3/14.
 * 圆形进度条
 */

public class CircleProgressView extends View {


    private long progress;
    private long max;
    private int strokeWidth;

    private int frontgroundColor;
    private int backgroundColor;

    private RectF mArcRectF;
    private Paint mArcPaint;
    private Paint mArcBgPaint;
    private PaintFlagsDrawFilter mPaintFlagsDrawFilter;


    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView);
        progress = typedArray.getInteger(R.styleable.CircleProgressView_progress, -1);
        max = typedArray.getInteger(R.styleable.CircleProgressView_max, -1);
        strokeWidth = typedArray.getDimensionPixelSize(R.styleable.CircleProgressView_strokeWidth, 10);
        backgroundColor = typedArray.getColor(R.styleable.CircleProgressView_backgroundColor, Color.BLACK);
        frontgroundColor = typedArray.getColor(R.styleable.CircleProgressView_frontgroundColor, Color.RED);

        LogUtil.d("!@#", "strokeWidth:" + strokeWidth + " backgroundColor:" + backgroundColor + " frontgroundColor:" + frontgroundColor);

        typedArray.recycle();
        init();
    }

    private void init() {
        mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mArcRectF = new RectF(0, 0, getWidth() - strokeWidth, getHeight() - strokeWidth);

        mArcPaint = new Paint();
        mArcPaint.setColor(frontgroundColor);
        mArcPaint.setStrokeWidth(strokeWidth);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);

        mArcBgPaint = new Paint();
        mArcBgPaint.setColor(backgroundColor);
        mArcBgPaint.setStrokeWidth(strokeWidth);
        mArcBgPaint.setStyle(Paint.Style.STROKE);
        mArcBgPaint.setStrokeCap(Paint.Cap.ROUND);
        mArcBgPaint.setAntiAlias(true);
        mArcBgPaint.setDither(true);
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (progress < 0 || max <= 0) {
            return;
        }
        canvas.setDrawFilter(mPaintFlagsDrawFilter);

        mArcRectF.set(0, 0, getWidth()-strokeWidth, getHeight()-strokeWidth);
        canvas.translate(strokeWidth/2f,strokeWidth/2f);
        canvas.drawArc(mArcRectF, -90, 360, false, mArcBgPaint);
        canvas.drawArc(mArcRectF, -90, (float)progress / max * 360, false, mArcPaint);
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
        invalidate();
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

}
