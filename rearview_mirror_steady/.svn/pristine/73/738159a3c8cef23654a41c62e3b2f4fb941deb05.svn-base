package com.txznet.launcher.widget.image;

import android.content.Context;
import android.util.AttributeSet;

import com.txznet.launcher.R;
import com.txznet.launcher.utils.AnimUtils;
import com.txznet.launcher.utils.IAnim;

/**
 * 小欧监听声音时候的展示声波动画的view。动画是通过handler延时实现的。
 */
public class WaveView extends android.support.v7.widget.AppCompatImageView {
    private final int ANIM_FADE_IN = R.array.anim_list_shengbochuxian;
    private final int ANIM_PLAY = R.array.anim_list_shengbochongfu;
    private final int ANIM_FADE_OUT = R.array.anim_list_shengboxiaoshi;
    private final int DEFAULT_DURATION = 83;

    private long mFadeInDuration;
    private long mFadeOutDuration;

    public WaveView(Context context) {
        super(context);
        initialView();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialView();
    }

    public WaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView();
    }

    private void initialView() {
        int[] resArr = AnimUtils.getRes(getContext(), ANIM_FADE_IN);
        mFadeInDuration = resArr.length * DEFAULT_DURATION;
    }

    private boolean isPlaying;
    private IAnim mAnim;

    private Runnable mSwitch2PlayTask = new Runnable() {
        @Override
        public void run() {
            mAnim = AnimUtils.load(WaveView.this, ANIM_PLAY, DEFAULT_DURATION, true);
        }
    };


    public void play() {
        if (isPlaying) {
            return;
        }
        isPlaying = true;
        if (mAnim != null) {
            mAnim.release();
            mAnim = null;
        }
        mAnim = AnimUtils.load(WaveView.this, ANIM_FADE_IN, DEFAULT_DURATION, false);
        removeCallbacks(mSwitch2PlayTask);
        postDelayed(mSwitch2PlayTask, mFadeInDuration);
    }


    public void stop() {
        if (!isPlaying) {
            return;
        }
        isPlaying = false;
        removeCallbacks(mSwitch2PlayTask);
        if (mAnim != null) {
            mAnim.release();
            mAnim = null;
        }
        mAnim = AnimUtils.load(WaveView.this, ANIM_FADE_OUT, DEFAULT_DURATION, false);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAnim != null) {
            mAnim.release();
            mAnim = null;
        }
        removeCallbacks(mSwitch2PlayTask);
    }
}
