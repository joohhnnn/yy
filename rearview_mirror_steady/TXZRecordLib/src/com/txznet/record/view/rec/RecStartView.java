package com.txznet.record.view.rec;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.record.lib.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class RecStartView extends AbsStepView {
	private int currState = STATE_ENDED;

	private View mCircleView;
	private ImageView mVoiceDrawableView;
	private AlphaAnimation mAlphaSet = null;

	private int mAlphaDuration = 100;
	private int mAlphaOffset = 50;
	private int mExitDelay = 5;

	private int[] mResId = { R.drawable.mic_01, R.drawable.mic_02, R.drawable.mic_03, R.drawable.mic_04,
			R.drawable.mic_05, R.drawable.mic_06, R.drawable.mic_07, R.drawable.mic_08, R.drawable.mic_09,
			R.drawable.mic_10, R.drawable.mic_11 };
	private Drawable[] mDrawables;

	public RecStartView(Context context) {
		this(context, null);
	}

	public RecStartView(Context context, AttributeSet attr) {
		super(context, attr);

		init();
	}

	private void init() {
		View con = inflate(getContext(), R.layout.rec_step_start_view, this);
		mCircleView = con.findViewById(R.id.circle_ly);
		mVoiceDrawableView = (ImageView) con.findViewById(R.id.voice_view);

		mDrawables = new Drawable[mResId.length];
		for (int i = 0; i < mResId.length; i++) {
			mDrawables[i] = getResources().getDrawable(mResId[i]);
		}

		mAlphaSet = new AlphaAnimation(1f, 0);
		mAlphaSet.setFillAfter(false);
		mAlphaSet.setDuration(mAlphaDuration);
		mAlphaSet.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				mAlphaSet.reset();
				mAlphaSet.cancel();
				mCircleView.clearAnimation();
				mCircleView.setVisibility(INVISIBLE);
			}
		});
	}

	@Override
	public void playStartAnim() {
//		LogUtil.loge("playStartAnim");
		resetVoiceStep();

		currState = STATE_START;
		if (mListener != null) {
			mListener.onAnimationStep(this, STATE_START);
		}
		mCircleView.clearAnimation();
		post(new Runnable() {
			@Override
			public void run() {
				mCircleView.setVisibility(VISIBLE);
				mVoiceDrawableView.setVisibility(VISIBLE);
			}
		});

		this.setVisibility(VISIBLE);
	}

	@Override
	public void playLoopAnim() {
	}

	@Override
	public void playEndAnim() {
//		LogUtil.loge("playEndAnim");
		currState = STATE_LOOP;
		post(new Runnable() {
			@Override
			public void run() {
				mCircleView.setVisibility(VISIBLE);
				mVoiceDrawableView.setVisibility(VISIBLE);
			}
		});
		playVoiceStep();

		postDelayed(new Runnable() {

			@Override
			public void run() {
				mCircleView.setAnimation(mAlphaSet);
				mCircleView.startAnimation(mAlphaSet);
			}
		}, mAlphaOffset);
	}

	@Override
	public boolean isAniming() {
		return currState != STATE_ENDED;
	}

	private void playVoiceStep() {
		resetVoiceStep();
		post(mVoiceExit);
	}

	private void resetVoiceStep() {
		mCurIndex = 0;
		mVoiceDrawableView.setImageDrawable(mDrawables[mCurIndex]);
		removeCallbacks(mVoiceExit);
	}

	int mCurIndex;

	Runnable mVoiceExit = new Runnable() {

		@Override
		public void run() {
//			LogUtil.loge("mCurIndex:" + mCurIndex);
			if (mCurIndex >= mResId.length) {
				removeCallbacks(this);
				resetVoiceStep();
				currState = STATE_ENDED;
				if (mListener != null) {
					mListener.onAnimationStep(RecStartView.this, STATE_ENDED);
				}
				return;
			}
			mVoiceDrawableView.setImageDrawable(mDrawables[mCurIndex]);

			mCurIndex++;
			removeCallbacks(this);
			postDelayed(this, mExitDelay);
		}
	};
}