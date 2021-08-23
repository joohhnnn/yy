package com.txznet.webchat.ui.common.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.txznet.webchat.R;

/**
 * 录音声波动画View
 * Created by J on 2018/9/26.
 */

public class VoiceView extends View {
    private int mBarCount;
    private int mBarWidth;
    private int mBarHeightMin;
    private int mBarHeightMax;
    private Drawable mBarSrc;

    private ValueAnimator mAnim;
    private int mAnimDuration;

    private boolean bAnimStarted;
    double mAxisInterval;
    double mAxisShift = 0;
    private boolean bRestartAnim = false;

    public VoiceView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        // 部分Android4.4设备上启用硬件加速会导致draw失效动画无法显示, 所以显式屏蔽掉硬件加速
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        initAttrs(attrs);
        init();
    }

    public void start() {
        if (mAnim.isRunning()) {
            bRestartAnim = true;
        } else {
            bAnimStarted = true;
            mAxisShift = 0;
            mAnim.start();
        }
    }

    public void stop() {
        bRestartAnim = false;
        bAnimStarted = false;
    }

    public void stopImmediately() {
        bRestartAnim = false;
        bAnimStarted = false;
        mAnim.cancel();
        invalidate();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.VoiceView);
        mBarCount = ta.getInt(R.styleable.VoiceView_vv_bar_count, 7);
        mBarWidth = ta.getDimensionPixelSize(R.styleable.VoiceView_vv_bar_width, 5);
        mBarHeightMin = ta.getDimensionPixelSize(R.styleable.VoiceView_vv_bar_height_min, 5);
        mBarHeightMax = ta.getDimensionPixelSize(R.styleable.VoiceView_vv_bar_height_max, 100);
        int srcId = ta.getResourceId(R.styleable.VoiceView_vv_bar_src, 0);
        mBarSrc = getResources().getDrawable(srcId);
        mAnimDuration = ta.getInt(R.styleable.VoiceView_vv_anim_duration, 500);
        ta.recycle();
    }

    private void init() {
        // 初始化bar
        initBar();
        initAnim();
    }

    private void initBar() {
        mAxisInterval = Math.PI / (mBarCount - 1);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        int barMargin = (canvas.getWidth() - mBarWidth * mBarCount) / (mBarCount - 1);
        float f = (float) mAnim.getAnimatedValue();
        // draw bars
        for (int i = 0; i < mBarCount; i++) {
            int barHeight = getBarHeight(mAxisShift + f * Math.PI - mAxisInterval * i);
            int left = (mBarWidth + barMargin) * i;
            int top = canvas.getHeight() / 2 - barHeight / 2;
            int right = left + mBarWidth;
            int bottom = top + barHeight;

            mBarSrc.setBounds(left, top, right, bottom);
            mBarSrc.draw(canvas);
        }
    }

    private int getBarHeight(double i) {
        if (i < 0 || i > Math.PI * 2) {
            return mBarHeightMin;
        }

        return (int) Math.abs(Math.sin(i) * (mBarHeightMax - mBarHeightMin)) + mBarHeightMin;
    }

    private void initAnim() {
        mAnim = ValueAnimator.ofFloat(0, 1);
        mAnim.setDuration(mAnimDuration);
        mAnim.setInterpolator(new LinearInterpolator());
        mAnim.setRepeatCount(ValueAnimator.INFINITE);
        mAnim.setRepeatMode(ValueAnimator.RESTART);
        mAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                invalidate();
            }
        });

        mAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(final Animator animation) {
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
            }

            @Override
            public void onAnimationCancel(final Animator animation) {
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
                if (bAnimStarted) {
                    mAxisShift = Math.PI;
                } else {
                    if (mAxisShift == Math.PI * 2) {
                        mAxisShift = Math.PI * 3;

                        if (!bRestartAnim) {
                            animation.cancel();
                        } else {
                            bAnimStarted = true;
                            mAxisShift = 0;
                        }

                    } else {
                        mAxisShift = Math.PI * 2;
                    }
                }
            }
        });
    }
}