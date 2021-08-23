package com.txznet.resholder.theme.ironman.rec;

import com.txznet.comm.ui.util.LayouUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class RecIronView extends FrameLayout {
	public static final int STATE_START = 1;
	public static final int STATE_RECORD = 2;
	public static final int STATE_PROCESS = 3;

	int curState;
	int mRepeatCount;
	boolean mInterrupt;

	ImageView mBgIv;
	ImageView mRingIv;
	ImageView mWheelIv;
	ImageView mLightIv;
	RelativeLayout mWheelLy;

	ScaleAnimation mBgRepScaleAnim = null;
	ScaleAnimation mRingRepScaleAnim = null;
	ScaleAnimation mLightScaleAnim = null;
	RotateAnimation mRingRotateAnim = null;
	RotateAnimation mWheelRotateAnim = null;

	AnimationSet mLightInAnims = null;
	AnimationSet mLightOutAnims = null;

	AlphaAnimation mAlphaInAnim = null;
	AlphaAnimation mAlphaOutAnim = null;
	AlphaAnimation mAlphaRepAnim = null;

	public RecIronView(Context context) {
		this(context, null);
	}

	public RecIronView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAnim();
		initView();
	}

	private void initView() {
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER;

		mBgIv = new ImageView(getContext());
		mBgIv.setScaleType(ScaleType.CENTER);
		mBgIv.setImageDrawable(LayouUtil.getDrawable("ironman_rec_1"));
		addViewInLayout(mBgIv, 0, params, true);

		mWheelLy = new RelativeLayout(getContext());
		mWheelLy.setGravity(Gravity.CENTER);

		mRingIv = new ImageView(getContext());
		mRingIv.setScaleType(ScaleType.CENTER);
		mRingIv.setImageDrawable(LayouUtil.getDrawable("ironman_rec_2"));
		mWheelLy.addView(mRingIv);

		mWheelIv = new ImageView(getContext());
		mWheelIv.setScaleType(ScaleType.CENTER);
		mWheelIv.setImageDrawable(LayouUtil.getDrawable("ironman_rec_3"));
		mWheelLy.addView(mWheelIv);

		addViewInLayout(mWheelLy, 1, params);

		mLightIv = new ImageView(getContext());
		mLightIv.setScaleType(ScaleType.CENTER);
		mLightIv.setImageDrawable(LayouUtil.getDrawable("ironman_rec_4"));
		addViewInLayout(mLightIv, 2, params, true);

		mLightIv.setVisibility(INVISIBLE);

		requestLayout();
	}

	private void initAnim() {
		mBgRepScaleAnim = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mBgRepScaleAnim.setRepeatMode(Animation.REVERSE);
		mBgRepScaleAnim.setInterpolator(new LinearInterpolator());
		mBgRepScaleAnim.setRepeatCount(Animation.INFINITE);
		mBgRepScaleAnim.setDuration(600);
		mBgRepScaleAnim.setAnimationListener(mInnerListener);

		mRingRepScaleAnim = new ScaleAnimation(1.0f, 1.15f, 1.0f, 1.15f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mRingRepScaleAnim.setRepeatMode(Animation.REVERSE);
		mRingRepScaleAnim.setInterpolator(new LinearInterpolator());
		mRingRepScaleAnim.setRepeatCount(Animation.INFINITE);
		mRingRepScaleAnim.setDuration(600);
		mRingRepScaleAnim.setAnimationListener(mInnerListener);

		mLightScaleAnim = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		mLightScaleAnim.setRepeatMode(Animation.REVERSE);
		mLightScaleAnim.setInterpolator(new LinearInterpolator());
		mLightScaleAnim.setRepeatCount(Animation.INFINITE);
		mLightScaleAnim.setDuration(600);
		mLightScaleAnim.setAnimationListener(mInnerListener);

		mRingRotateAnim = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mRingRotateAnim.setRepeatCount(Animation.INFINITE);
		mRingRotateAnim.setRepeatMode(Animation.RESTART);
		mRingRotateAnim.setInterpolator(new LinearInterpolator());
		mRingRotateAnim.setDuration(1000);
		mRingRotateAnim.setAnimationListener(mInnerListener);

		mWheelRotateAnim = new RotateAnimation(0, -360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		mWheelRotateAnim.setRepeatCount(Animation.INFINITE);
		mWheelRotateAnim.setRepeatMode(Animation.RESTART);
		mWheelRotateAnim.setInterpolator(new LinearInterpolator());
		mWheelRotateAnim.setDuration(2000);
		mWheelRotateAnim.setAnimationListener(mInnerListener);

		mAlphaInAnim = new AlphaAnimation(0.0f, 1.0f);
		mAlphaInAnim.setFillAfter(true);
		mAlphaInAnim.setDuration(1000);
		mAlphaInAnim.setAnimationListener(mInnerListener);

		mAlphaOutAnim = new AlphaAnimation(1.0f, 0.0f);
		mAlphaOutAnim.setDuration(1000);
		mAlphaOutAnim.setFillAfter(true);
		mAlphaOutAnim.setAnimationListener(mInnerListener);

		mAlphaRepAnim = new AlphaAnimation(0.75f, 1.0f);
		mAlphaRepAnim.setAnimationListener(mInnerListener);
		mAlphaRepAnim.setRepeatCount(Animation.INFINITE);
		mAlphaRepAnim.setRepeatMode(Animation.REVERSE);
		mAlphaRepAnim.setDuration(600);

		mLightInAnims = new AnimationSet(false);
		mLightInAnims.addAnimation(mAlphaInAnim);
		mLightInAnims.addAnimation(mAlphaRepAnim);
		mLightInAnims.addAnimation(mLightScaleAnim);

		mLightOutAnims = new AnimationSet(false);
		mLightOutAnims.addAnimation(mAlphaOutAnim);
		mLightOutAnims.addAnimation(mLightScaleAnim);
	}

	private AnimationListener mInnerListener = new AnimationListener() {

		@Override
		public void onAnimationStart(Animation animation) {
			mRepeatCount = 0;
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			mRepeatCount = 0;
			doAnimEnd(animation);
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			if (animation == mRingRepScaleAnim && curState == STATE_RECORD) {
				mRepeatCount++;
			}

			if (isStopAnim()) {
				cancelAnim();
			}
		}
	};

	@SuppressLint("NewApi")
	private boolean isStopAnim() {
		if (!mInterrupt) {
			return false;
		}
		if (mRepeatCount != 0 && mRepeatCount % 2 == 1) {
			return true;
		}

		return false;
	}

	private void doAnimEnd(Animation animation) {
		if (animation == mRingRepScaleAnim || animation == mAlphaOutAnim) {
			if (curState == STATE_PROCESS && animation == mRingRepScaleAnim) {
				return;
			}

			fireAnimStart();
			return;
		}
	}

	private void cancelAnim() {
		mBgRepScaleAnim.cancel();
		mRingRepScaleAnim.cancel();
		mRingRotateAnim.cancel();
		mWheelRotateAnim.cancel();
		mAlphaInAnim.cancel();
		mAlphaOutAnim.cancel();
		mLightInAnims.cancel();
		mLightOutAnims.cancel();
		mLightScaleAnim.cancel();
		mAlphaRepAnim.cancel();
	}

	private void clearAnim() {
		mBgIv.clearAnimation();
		mRingIv.clearAnimation();
		mWheelIv.clearAnimation();
		mWheelLy.clearAnimation();
		mLightIv.clearAnimation();
		if (mLightIv.getVisibility() == VISIBLE) {
			mLightIv.setVisibility(INVISIBLE);
		}
	}

	public void onStart() {
		playStateAnim(STATE_START);
	}

	public void stop() {
		curState = 0;
		post(mStopInner);
	}

	Runnable mStopInner = new Runnable() {

		@Override
		public void run() {
			cancelAnim();
			clearAnim();
		}
	};

	public void onRecord() {
		playStateAnim(STATE_RECORD);
	}

	public void onLoading() {
		playStateAnim(STATE_PROCESS);
	}

	private void playStateAnim(int stepAnim) {
		if (stepAnim == curState) {
			return;
		}
		curState = stepAnim;

		switch (curState) {
		case STATE_START:
			cancelAnim();
			post(mStartRun);
			break;

		case STATE_RECORD:
			mInterrupt = true;
			break;

		case STATE_PROCESS:
			mInterrupt = true;
			playRecordEnd();
			break;
		}
	}

	/**
	 * 播放录音结束动画
	 */
	private void playRecordEnd() {
		mLightIv.setVisibility(VISIBLE);
		mLightIv.startAnimation(mLightOutAnims);
	}

	private void fireAnimStart() {
		mInterrupt = false;

		switch (curState) {
		case STATE_RECORD:
			post(mRecordRun);
			break;

		case STATE_PROCESS:
			post(mProcRun);
			break;
		}
	}

	Runnable mStartRun = new Runnable() {

		@Override
		public void run() {
			clearAnim();

			mBgIv.startAnimation(mBgRepScaleAnim);
			mWheelLy.startAnimation(mRingRepScaleAnim);
		}
	};

	Runnable mRecordRun = new Runnable() {

		@Override
		public void run() {
			clearAnim();
			mLightIv.setVisibility(VISIBLE);

			mWheelLy.startAnimation(mRingRepScaleAnim);
			mLightIv.startAnimation(mLightInAnims);
		}
	};

	Runnable mProcRun = new Runnable() {

		@Override
		public void run() {
			clearAnim();

			mRingIv.startAnimation(mRingRotateAnim);
			mWheelIv.startAnimation(mWheelRotateAnim);
		}
	};
}