package com.txznet.webchat.ui.car.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.txznet.webchat.R;

public class WaveItemView extends View {

    private WaveItemView(Context context) {
        super(context);
    }

    public int minHeight, maxHeight;

    public ObjectAnimator animation;
    private float mScale;

    public WaveItemView(Context context, int minHeight, int maxHeight, Drawable src) {
        super(context);
        setBackground(getResources().getDrawable(R.drawable.shape_car_record_sound_item));
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        mScale = minHeight / (float) maxHeight;
        this.setScaleY(mScale);

        animation = ObjectAnimator.ofFloat(this, "scaleY", mScale, 1.0f, mScale);
        animation.setDuration(520);
        animation.setRepeatMode(ValueAnimator.REVERSE);
        animation.setRepeatCount(ValueAnimator.INFINITE);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (!isWaving) {
                    animation.cancel();
                }
            }
        });
    }

    public void start() {
        isWaving = true;
        setScaleY(mScale);
        animation.start();

    }

    private boolean isWaving = false;

    public void stop() {
        stop(false);
    }

    public void stop(boolean reset) {
        isWaving = false;

        if (reset) {
            setScaleY(mScale);
        }
    }

}
