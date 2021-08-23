package com.txznet.comm.ui.theme.test.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 说明：路况的颜色条
 *
 * @author xiaolin
 * create at 2020-11-12 14:21
 */
public class TrafficLightCrossView extends View {

    public static class Step {
        public int color;
        public int distance;

        public Step() {
        }

        public Step(int color, int distance) {
            this.color = color;
            this.distance = distance;
        }
    }

    public static final int DEFAULT_COLOR = 0xFF0F89F5;

    private Paint mPaint;
    private RectF mViewBoundRect;
    private Path mClipPath;
    private RectF tmpRect = new RectF();

    private List<Step> trafficSteps = new ArrayList<>();

    public TrafficLightCrossView(Context context) {
        super(context);
        init();
    }

    public TrafficLightCrossView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TrafficLightCrossView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mViewBoundRect = new RectF();
        mClipPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int w = getMeasuredWidth();
        int h = getMeasuredHeight();
        mViewBoundRect.set(0, 0, w, h);

        int r = h / 2;// 圆角半径
        mClipPath.reset();
        mClipPath.addRoundRect(mViewBoundRect, r, r, Path.Direction.CCW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float width = getWidth();
        float height = getHeight();

        int maxWeight = 0;
        for (Step step : trafficSteps) {
            maxWeight += step.distance;
        }

        canvas.save();
        canvas.clipPath(mClipPath);

        if (maxWeight <= 0) {
            mPaint.setColor(DEFAULT_COLOR);
            canvas.drawRect(mViewBoundRect, mPaint);
        } else {
            float weight = 0;
            float left = 0;
            for (Step step : trafficSteps) {
                mPaint.setColor(step.color);
                weight += step.distance;

                float right = width * weight / maxWeight;
                tmpRect.set(left, 0, right, height);
                canvas.drawRect(tmpRect, mPaint);
                left = right;
            }
        }

        canvas.restore();
    }

    public void setTrafficSteps(List<Step> steps){
        this.trafficSteps = steps;
        invalidate();
        LogUtil.d("xxxx", JSONObject.toJSONString(steps));
    }
}
