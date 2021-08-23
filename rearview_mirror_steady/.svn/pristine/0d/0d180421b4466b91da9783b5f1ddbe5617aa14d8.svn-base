package com.txznet.music.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.SwipeLoadMoreTrigger;
import com.aspsine.swipetoloadlayout.SwipeTrigger;
import com.txznet.music.R;

/**
 * Created by brainBear on 2017/7/10.
 */

public class LoadMoreFooterView extends RelativeLayout implements SwipeTrigger, SwipeLoadMoreTrigger {

    private ImageView ivArrow;
    private ImageView ivProgress;
    private TextView tvTitle;

    private boolean rotated = false;
    private Animation mAnimRotateDown;
    private Animation mAnimRotateUp;
    private Animation mAnimProgress;
    private ImageView ivSuccess;


    public LoadMoreFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAnimRotateDown = AnimationUtils.loadAnimation(context, R.anim.arrow_rotate_down);
        mAnimRotateUp = AnimationUtils.loadAnimation(context, R.anim.arrow_rotate_up);
        mAnimProgress = AnimationUtils.loadAnimation(context, R.anim.progress_rotate);
    }

    public LoadMoreFooterView(Context context) {
        this(context, null);
    }

    public LoadMoreFooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ivArrow = (ImageView) findViewById(R.id.iv_arrow);
        ivProgress = (ImageView) findViewById(R.id.iv_progress);
        ivSuccess = (ImageView) findViewById(R.id.iv_success);
        tvTitle = (TextView) findViewById(R.id.tv_title);
    }

    @Override
    public void onLoadMore() {
        ivArrow.clearAnimation();
        ivArrow.setVisibility(View.INVISIBLE);
        ivProgress.setVisibility(View.VISIBLE);
        ivProgress.clearAnimation();
        ivProgress.startAnimation(mAnimProgress);
        tvTitle.setText(R.string.on_load_more);
    }

    @Override
    public void onPrepare() {
    }

    @Override
    public void onMove(int yScrolled, boolean isComplete, boolean automatic) {
        if (!isComplete) {
            ivArrow.setVisibility(View.VISIBLE);
            ivProgress.setVisibility(View.INVISIBLE);
            ivSuccess.setVisibility(View.INVISIBLE);
            if (yScrolled <= -getHeight()) {
                tvTitle.setText(R.string.release_to_load_more);
                if (!rotated) {
                    ivArrow.clearAnimation();
                    ivArrow.startAnimation(mAnimRotateUp);
                    rotated = true;
                }
            } else {
                tvTitle.setText(R.string.swipe_to_load_more);
                if (rotated) {
                    ivArrow.clearAnimation();
                    ivArrow.startAnimation(mAnimRotateDown);
                    rotated = false;
                }
            }
        }
    }

    @Override
    public void onRelease() {
    }

    @Override
    public void onComplete() {
        tvTitle.setText(R.string.load_more_complete);
        ivProgress.clearAnimation();
        ivProgress.setVisibility(View.INVISIBLE);
        ivSuccess.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReset() {
    }
}