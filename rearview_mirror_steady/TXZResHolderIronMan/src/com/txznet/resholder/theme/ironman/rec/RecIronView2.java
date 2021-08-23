package com.txznet.resholder.theme.ironman.rec;

import com.txznet.comm.ui.util.LayouUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

public class RecIronView2 extends FrameLayout {
	public static final int STATE_START = 1;
	public static final int STATE_RECORD = 2;
	public static final int STATE_PROCESS = 3;

	float[] scaleBg = { 1.0f, 1.01f, 1.02f, 1.03f, 1.04f, 1.05f, 1.06f, 1.07f, 1.08f };
	float[] scale = { 1.0f, 1.02f, 1.04f, 1.06f, 1.08f, 1.10f, 1.12f, 1.14f, 1.15f };
	float[] alpha = { 0.0f, 0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f, };
	float[] rotate1 = { 0, -30, -60, -90, -120, -150, -180, -210, -240, -270, -300, -330 };
	float[] rotate = { 0, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300, 330 };

	float[] lightAlpha = { 0.739f, 0.768f, 0.797f, 0.826f, 0.855f, 0.884f, 0.913f, 0.942f, 0.971f, 1.0f };
	
	float mNormalScale = 1.0f;

	ImageView mBgIv;
	ImageView mRingIv;
	ImageView mWheelIv;
	ImageView mLightIv;
	RelativeLayout mWheelLy;

	public RecIronView2(Context context) {
		this(context, null);
	}

	public RecIronView2(Context context, AttributeSet attrs) {
		super(context, attrs);

		initView();
		initAnimer();
	}

	int curState;

	ViewAnimer mBgViewAnimer;
	ViewAnimer mWheelScaleAnimer;
	ViewAnimer mRingRotateAnimer;
	ViewAnimer mWheelRotateAnimer;

	private void initAnimer() {
		mBgViewAnimer = new ViewAnimer(mBgIv, scaleBg, "scale");
		mBgViewAnimer.FRAME_TIME = 60;

		mWheelScaleAnimer = new ViewAnimer(mWheelLy, scale, "scale");
		mWheelScaleAnimer.FRAME_TIME = 60;

		mRingRotateAnimer = new ViewAnimer(mRingIv, rotate1, "rotate");
		mRingRotateAnimer.repeatMode = Animation.RESTART;
		mRingRotateAnimer.isInterupter = true;
		mRingRotateAnimer.FRAME_TIME = 50;

		mWheelRotateAnimer = new ViewAnimer(mWheelIv, rotate, "rotate");
		mWheelRotateAnimer.repeatMode = Animation.RESTART;
		mWheelRotateAnimer.isInterupter = true;
		mWheelRotateAnimer.FRAME_TIME = 50;

		mBgViewAnimer.mListener = mInnerListener;
		mWheelScaleAnimer.mListener = mInnerListener;
		mRingRotateAnimer.mListener = mInnerListener;
		mWheelRotateAnimer.mListener = mInnerListener;
	}

	boolean expired;

	private AnimerListener mInnerListener = new AnimerListener() {

		@SuppressLint("NewApi")
		@Override
		public void onUpdate(ViewAnimer va, int total, int curIndex) {
			if (va == mWheelScaleAnimer && curState == STATE_RECORD) {
				if (curIndex == total - 1) {
					expired = true;
				}

				float f = curIndex / (float) total;
				if (mLightIv.getVisibility() == INVISIBLE) {
					mLightIv.setVisibility(VISIBLE);
				}

				if (!expired || mWheelScaleAnimer.isCancel) {
					Log.d("lyd", "onUpdate:" + f);
					mLightIv.setAlpha(f);
				} else {
					mLightIv.setAlpha(lightAlpha[curIndex]);
				}

				mLightIv.setScaleX(mWheelScaleAnimer.mJumpDatas[curIndex]);
				mLightIv.setScaleY(mWheelScaleAnimer.mJumpDatas[curIndex]);
			} else {
				expired = false;
			}

			if (va == mWheelScaleAnimer && curState == STATE_PROCESS) {
				if (mLightIv.getVisibility() == INVISIBLE) {
					mLightIv.setVisibility(VISIBLE);
				}

				mLightIv.setAlpha(alpha[curIndex]);
				mLightIv.setScaleX(mWheelScaleAnimer.mJumpDatas[curIndex]);
				mLightIv.setScaleY(mWheelScaleAnimer.mJumpDatas[curIndex]);
			}

			if (va == mRingRotateAnimer && curState == STATE_PROCESS) {
				mWheelIv.setRotation(rotate[curIndex]);
			}

			if (va == mWheelScaleAnimer && curState == STATE_START) {
				mBgIv.setScaleX(scaleBg[curIndex]);
				mBgIv.setScaleY(scaleBg[curIndex]);
			}
		}

		@Override
		public void onStart(ViewAnimer va) {
		}

		@Override
		public void onEnd(ViewAnimer va) {
			if (va == mWheelScaleAnimer) {
				if (curState == STATE_RECORD) {
					fireAnimStart();
					return;
				} else if (curState == STATE_PROCESS) {
					fireAnimStart();
					return;
				}
			}
		}
	};

	public void onStart() {
		playStateAnim(STATE_START);
	}

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
			resetViewState();
			playStart();
			break;

		case STATE_RECORD:
			cancelAnim();
			resetViewState();
			mWheelScaleAnimer.startAnim();
			break;

		case STATE_PROCESS:
			cancelAnim();
			break;
		}
	}

	private void fireAnimStart() {
		if (mLightIv.getVisibility() == VISIBLE) {
			mLightIv.setVisibility(INVISIBLE);
		}
		switch (curState) {
		case STATE_RECORD:
			mWheelScaleAnimer.startAnim();
			break;

		case STATE_PROCESS:
			mRingRotateAnimer.startAnim();
			break;
		}
	}

	private void playStart() {
		mWheelScaleAnimer.startAnim();
	}

	@SuppressLint("NewApi")
	private void cancelAnim() {
		mBgViewAnimer.cancel();
		mWheelScaleAnimer.cancel();
		mRingRotateAnimer.cancel();
		mWheelRotateAnimer.cancel();
	}

	private void resetViewState() {
		mBgIv.setScaleX(mNormalScale);
		mBgIv.setScaleY(mNormalScale);

		mLightIv.setVisibility(INVISIBLE);
		mLightIv.setScaleX(mNormalScale);
		mLightIv.setScaleY(mNormalScale);
		mLightIv.setAlpha(mNormalScale);

		mWheelLy.setScaleX(mNormalScale);
		mWheelLy.setScaleY(mNormalScale);
	}

	public void stop() {
		curState = 0;
		cancelAnim();
		resetViewState();
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
		mWheelIv.setImageDrawable(LayouUtil.getDrawable("ironman2_rec_3"));
		mWheelLy.addView(mWheelIv);

		addViewInLayout(mWheelLy, 1, params,true);

		mLightIv = new ImageView(getContext());
		mLightIv.setScaleType(ScaleType.CENTER_INSIDE);
		mLightIv.setImageDrawable(LayouUtil.getDrawable("ironman_rec_4"));
		addViewInLayout(mLightIv, 2, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT), false);
		mLightIv.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				mLightIv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				int padding = (int) (mLightIv.getWidth() * 0.06);	
				mLightIv.setPadding(padding, padding, padding, padding);
			}
		});

		mLightIv.setVisibility(INVISIBLE);

		requestLayout();

	}
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			float sX = (float)mWheelLy.getWidth()/(float)getWidth();
			if (sX > 0.8f) {
				mNormalScale = 0.8f;
				for (int i = 0; i < scaleBg.length; i++) {
					scaleBg[i] = scaleBg[i]*mNormalScale;
					scale[i] = scale[i]* mNormalScale;
				}
			}
		}
	}

	public class ViewAnimer {
		public View mApplyView;
		public float[] mJumpDatas;
		public AnimerListener mListener;
		public String mApplyName;
		public int FRAME_TIME = 20;

		private int curIndex;
		private boolean isAniming;
		private boolean isCancel;

		public int repeatCount = Animation.INFINITE;
		public int repeatMode = Animation.REVERSE;
		public boolean isInterupter;

		public ViewAnimer(View applyView, float[] jumps, String name) {
			this.mApplyView = applyView;
			this.mJumpDatas = jumps;
			this.mApplyName = name;
		}

		public void startAnim() {
			if (mApplyView == null || mJumpDatas == null) {
				return;
			}
			reset();

			removeCallbacks(mStartRun);
			postDelayed(mStartRun, 10);
		}

		public boolean isAniming() {
			return isAniming;
		}

		boolean mCyclip;

		Runnable mStartRun = new Runnable() {

			@SuppressLint("NewApi")
			@Override
			public void run() {
				if ((isCancel && curIndex == 0) || isInterupter) {
					fireAnimEnd();
					removeCallbacks(this);
					return;
				}

				if (!isAniming) {
					if (mListener != null) {
						mListener.onStart(ViewAnimer.this);
					}
				}

				if (repeatMode == Animation.REVERSE) {
					if (mCyclip) {
						curIndex++;
						if (curIndex >= mJumpDatas.length) {
							curIndex--;
							mCyclip = false;
						}
					} else {
						curIndex--;
						if (curIndex < 0) {
							curIndex++;
							mCyclip = true;
						}
					}
				} else if (repeatMode == Animation.RESTART) {
					curIndex++;
					if (curIndex >= mJumpDatas.length)
						curIndex = 0;
				}

				isAniming = true;
				float f = mJumpDatas[curIndex];

				if ("scale".equals(mApplyName)) {
					mApplyView.setScaleX(f);
					mApplyView.setScaleY(f);
				} else if ("alpha".equals(mApplyName)) {
					mApplyView.setAlpha(f);
				} else if ("rotate".equals(mApplyName)) {
					mApplyView.setRotation(f);
				}

				if (mListener != null) {
					mListener.onUpdate(ViewAnimer.this, mJumpDatas.length, curIndex);
				}

				postDelayed(this, FRAME_TIME);
			}
		};

		private void reset() {
			isAniming = false;
			isCancel = false;
			isInterupter = false;
			curIndex = 0;
		}

		private void fireAnimEnd() {
			reset();
			if (mListener != null) {
				mListener.onEnd(this);
			}
		}

		public void cancel() {
			this.isCancel = true;
		}
	}

	public interface AnimerListener {
		public void onUpdate(ViewAnimer va, int total, int curIndex);

		public void onStart(ViewAnimer va);

		public void onEnd(ViewAnimer va);
	}
}