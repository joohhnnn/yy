package com.txznet.launcher.widget.image;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.listener.SimpleAnimationListener;
import com.txznet.launcher.widget.IImage;

/**
 * Created by TXZ-METEORLUO on 2018/2/26.
 * 包含了小欧和文字的View，这个是我们开机时会看到的小欧，包括了全屏和半屏时的小欧。全屏就是界面只有小欧一个人，半屏就是小欧在右边，左边有东西。
 */

public class SQImageDynamic extends FrameLayout implements IImage {
    private TextView mTtsTv;
    private SQCharacter mImageIv;

    public SQImageDynamic(@NonNull Context context) {
        this(context, null);
    }

    public SQImageDynamic(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SQImageDynamic(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClipChildren(false);
        initNormalView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        playEnterAnim();
    }

    private void playEnterAnim() {
        Animation sqAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_fade_scale_down_in);
        sqAnim.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                super.onAnimationStart(animation);
                mTtsTv.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animation);
                mTtsTv.setVisibility(VISIBLE);
                Animation ttsTvAnim = AnimationUtils.loadAnimation(getContext(), R.anim.anim_fade_slide_up_in);
                mTtsTv.startAnimation(ttsTvAnim);
            }
        });
        mImageIv.startAnimation(sqAnim);
    }

    private void initNormalView() {
        removeAllViews();
        View.inflate(getContext(), R.layout.sq_image_dyna_widget_ly, this);
        mTtsTv = (TextView) findViewById(R.id.tts_tv);
        mImageIv = (SQCharacter) findViewById(R.id.image_iv);
    }


    @Override
    public void updateState(final int state) {
        LogUtil.logi("updateState: state="+state);
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mImageIv.updateState(state);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    updateState(state);
                }
            });
        }
    }

    @Override
    public void toggleScreen(final boolean isFullScreen) {
        setClipChildren(!isFullScreen);
        mImageIv.ivLight.setAlpha(isFullScreen ? 1f : 0.56f);
        mTtsTv.setTextSize(isFullScreen ? getResources().getDimensionPixelSize(R.dimen.dimen_full_text_size):getResources().getDimensionPixelSize(R.dimen.dimen_half_text_size));
    }

    @Override
    public void showDescText(final String descText) {
        if (mTtsTv == null) {
            return;
        }
        LogUtil.logd("showDescText descText:" + descText);
//        if (Looper.myLooper() == Looper.getMainLooper()) {
//            mTtsTv.setText(descText);
//        } else {
        post(new Runnable() {
            @Override
            public void run() {
                mTtsTv.setText(descText);
            }
        });
//        }
    }
}