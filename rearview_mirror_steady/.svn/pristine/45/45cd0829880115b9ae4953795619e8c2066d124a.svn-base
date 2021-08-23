package com.txznet.launcher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.txznet.comm.remote.util.LogUtil;

import java.util.Random;

/**
 * Created by Anudorannador on 2018/3/13.
 * 音乐界面上表示音量的随机波动的柱子
 */

public class RandomWave extends View {

    private int columnNum = 4;
    private int random;
    private boolean isStart = true;
    private Random mRandom;
    private int[] mRectHeight;
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private double mRect_w;
    private RectF[] mRect;

    public RandomWave(Context context) {
        this(context, null);
    }

    public RandomWave(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RandomWave(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mRandom = new Random();
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#FFE53F40"));
        mPaint.setStyle(Paint.Style.FILL);
        mRect = new RectF[columnNum];
        for (int i = 0 ; i < columnNum; i++) {
            mRect[i] = new RectF();
        }

        mRectHeight = new int[columnNum];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        mRect_w = mWidth / (2 * columnNum + 1);
        random = mHeight / 5;
    }

    public void start() {
        isStart = true;
        invalidate();
    }

    public void stop() {
        isStart = false;
        invalidate();
    }

    public void reset() {
        mRectHeight = new int[columnNum];
        invalidate();
    }

    public boolean isStart() {
        return isStart;
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (isStart)
        isStart = visibility == VISIBLE;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < columnNum; i++) {
                mRectHeight[i] = mRandom.nextInt(random);
            }
            invalidate();
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isStart) {
            removeCallbacks(runnable);
            postDelayed(runnable, 300);
        }
        //画柱状；动态图，就要改变柱状的top值
        for (int i = 0; i < columnNum; i++) {
            mRect[i].set((float) (mRect_w * (2 * i + 1)), mRectHeight[i] * 5, (float) (mRect_w * (2 * i + 2)), (float) (mHeight * 0.9));
            canvas.drawRect(mRect[i], mPaint);
        }

    }
}
