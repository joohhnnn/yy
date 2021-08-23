package com.txznet.launcher.widget.container;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.listener.SimpleAnimationListener;
import com.txznet.launcher.widget.IImage;
import com.txznet.launcher.widget.IScreen;

/**
 * Created by meteorluo on 2018/2/16.
 * 首页界面的架子。
 */

public class MainContainer extends ViewContainer implements IScreen {
    // 是否处于全屏
    private boolean isFullScreen = true;

    private int mPaddingLeft;
    private int mPaddingRight;
    private int mPaddingTop;
    private int mPaddingBottom;

    private int mDefPaddingLeft;
    private int mDefPaddingRight;
    private int mDefPaddingTop;
    private int mDefPaddingBottom;

    private Animation slideIn, slideOut;

    public MainContainer(@NonNull Context context) {
        this(context, null);
    }

    public MainContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        isFullScreen = true;
        mPaddingLeft = mDefPaddingLeft = (int) getResources().getDimension(R.dimen.dimen_container_left);
        mPaddingRight = mDefPaddingRight = (int) getResources().getDimension(R.dimen.dimen_container_right);
        mPaddingTop = mDefPaddingTop = (int) getResources().getDimension(R.dimen.dimen_container_top);
        mPaddingBottom = mDefPaddingBottom = (int) getResources().getDimension(R.dimen.dimen_container_bottom);
        super.init();
        activeView();
        mContentLayout.setVisibility(INVISIBLE);
        getContext().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                toggleScreen(!isFullScreen);
            }
        }, new IntentFilter("toggle"));


        slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.anim_fade_slide_up_out);
        slideOut.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (mContentLayout != null) {
                    mContentLayout.setVisibility(INVISIBLE);
                }
            }
        });
        slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.anim_fade_slide_up_in);
        slideIn.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                LogUtil.e("onAnimationStart: ");
                if (mContentLayout != null) {
                    mContentLayout.setVisibility(VISIBLE);
                }
            }
        });
    }

    /**
     * 更新padding的值，-1则使用默认
     * @param left
     * @param right
     * @param top
     * @param bottom
     */
    public void updatePadding(int left, int right, int top, int bottom) {
        if (left == -1) {
            mPaddingLeft = mDefPaddingLeft;
        } else {
            mPaddingLeft = left;
        }
        if (right == -1) {
            mPaddingRight = mDefPaddingRight;
        } else {
            mPaddingRight = right;
        }
        if (top == -1) {
            mPaddingTop = mDefPaddingTop;
        } else {
            mPaddingTop = top;
        }
        if (bottom == -1) {
            mPaddingBottom = mDefPaddingBottom;
        } else {
            mPaddingBottom = bottom;
        }
    }

    /**
     * 恢复默认的padding值
     */
    public void recoverDefaultPadding() {
//        mPaddingLeft = mDefPaddingLeft;
//        mPaddingRight = mDefPaddingRight;
//        mPaddingTop = mDefPaddingTop;
        mPaddingBottom = mDefPaddingBottom;
//        updatePadding(mPaddingLeft, mPaddingRight, mPaddingTop, mPaddingBottom);
        updateContentLayoutParams(mPaddingBottom);
    }

    @Override
    public void fullScreen() {
        toggleScreen(true);
    }

    @Override
    public void halfScreen() {
        toggleScreen(false);
    }

    @Override
    public boolean isFullScreen() {
        return isFullScreen;
    }

    private void toggleScreen(final boolean isFullScreen) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runToggle(isFullScreen);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    runToggle(isFullScreen);
                }
            });
        }
    }

    private void runToggle(final boolean isFullScreen) {
        if (mImageView != null && mImageView instanceof IImage) {
            ((IImage) mImageView).toggleScreen(isFullScreen);
        }

        Rect outRect = new Rect();
        getWindowVisibleDisplayFrame(outRect);
        int disPlayW = outRect.width();
        int sqBaseW = getResources().getDimensionPixelSize(R.dimen.dimen_full_image_w);
        float bScale2;
        int bTranX2, bTranY2;
        long bDelay = 100;
        if (isFullScreen) {
            bScale2 = 1f;
            bTranX2 = 0;
            bTranY2 = 0;
            if (mContentLayout.getVisibility() != INVISIBLE) {
                mContentLayout.startAnimation(slideOut);
            }
        } else {
            mContentLayout.setAlpha(0);
            bScale2 = 0.82f;
            sqBaseW *= bScale2;
            bTranX2 = (int) (disPlayW / 2f - sqBaseW + sqBaseW / 2f + mPaddingRight / 2);
            bTranY2 = (int) (sqBaseW * (1 - 0.9f));
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mContentLayout.setAlpha(1);
                    mContentLayout.startAnimation(slideIn);
                }
            }, bDelay);
        }
        // 当两次的isFullScreen为相同的时候，动画会执行了，只是因为值没有变化所以看上来没有动。
        mImageLayout.animate()
                .scaleX(bScale2)
                .scaleY(bScale2)
                .translationX(bTranX2)
                .translationY(bTranY2)
                .setDuration(400)
                .setStartDelay(bDelay)
                .setListener(animatorListener)
                .start();
        this.isFullScreen = isFullScreen;
    }

    @Override
    protected LayoutParams createImageLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        return params;
    }

    @Override
    protected LayoutParams createContentLayoutParams() {
        LayoutParams params = new LayoutParams(310, LayoutParams.MATCH_PARENT);
        params.setMargins(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
        return params;
    }

    @Override
    protected LayoutParams createStatusBarLayoutParams() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        return params;
    }

    private AnimatorListenerAdapter animatorListener= new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            if (isFullScreen()) {
                clearContentLayout();
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_TIME_CHANGE);
            }
        }
    };
}