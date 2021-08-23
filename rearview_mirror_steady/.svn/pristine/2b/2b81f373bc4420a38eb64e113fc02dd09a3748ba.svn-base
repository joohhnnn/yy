package com.txznet.comm.ui.theme.test.rec;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.txznet.comm.ui.UI2Manager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.LayouUtil;

public class RecStartView extends AbsStepView {
	private int currState = STATE_ENDED;

	private RelativeLayout mCircleView;
	private ImageView mVoiceDrawableView;
	private AlphaAnimation mAlphaSet = null;

	private int mAlphaDuration = 100;
	private int mAlphaOffset = 50;
	private int mExitDelay = 5;

	private String[] mResId = { "mic_01", "mic_02", "mic_03", "mic_04",
			"mic_05", "mic_06", "mic_07", "mic_08", "mic_09",
			"mic_10", "mic_11" };
	private Drawable[] mDrawables;

	public RecStartView(Context context) {
		this(context, null);
	}

	public RecStartView(Context context, AttributeSet attr) {
		super(context, attr);

		init();
	}

	private void init() {
		FrameLayout flCon = new FrameLayout(GlobalContext.get());
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
		addView(flCon,layoutParams);
		
		mCircleView = new RelativeLayout(GlobalContext.get());
		layoutParams = new FrameLayout.LayoutParams((int) ThemeConfigManager.getY(ThemeConfigManager.RECORD_WIN_CIRCLE_LY_WIDTH),(int) ThemeConfigManager.getY(ThemeConfigManager.RECORD_WIN_CIRCLE_LY_HEIGHT));
		layoutParams.gravity = Gravity.CENTER;
		flCon.addView(mCircleView,layoutParams);
		
		View ovalSolid = new View(GlobalContext.get());
		ovalSolid.setBackground(LayouUtil.getDrawable("shape_oval_solid"));
		RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		mCircleView.addView(ovalSolid,mRLayoutParams);
		
		View ovalStoke = new View(GlobalContext.get());
		ovalStoke.setBackground(LayouUtil.getDrawable("shape_oval_stoke"));
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		mCircleView.addView(ovalStoke,mRLayoutParams);

		mVoiceDrawableView = new ImageView(GlobalContext.get());
		layoutParams = new FrameLayout.LayoutParams((int) ThemeConfigManager.getY(ThemeConfigManager.RECORD_WIN_VOICE_VIEW_WIDTH),(int) ThemeConfigManager.getY(ThemeConfigManager.RECORD_WIN_VOICE_VIEW_HEIGHT));
		layoutParams.gravity = Gravity.CENTER;
		mVoiceDrawableView.setImageDrawable(LayouUtil.getDrawable("ic_voice_white"));
		flCon.addView(mVoiceDrawableView,layoutParams);

		mDrawables = new Drawable[mResId.length];
		for (int i = 0; i < mResId.length; i++) {
			mDrawables[i] = LayouUtil.getDrawable(mResId[i]);
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
				if (!isShowCircleView) {
					mCircleView.setVisibility(INVISIBLE);
				}
				isShowCircleView = false;
			}
		});
	}
	private boolean isShowCircleView = false;

	@Override
	public void playStartAnim() {
//		LogUtil.loge("playStartAnim");
		resetVoiceStep();

		currState = STATE_START;
		if (mListener != null) {
			mListener.onAnimationStep(this, STATE_START);
		}
		post(new Runnable() {
			@Override
			public void run() {
				isShowCircleView = true;
				mCircleView.clearAnimation();

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
		UI2Manager.runOnUIThread(mVoiceExit,0);
	}

	public void release(){
		LogUtil.logd("RecStartView release");
		UI2Manager.removeUIThread(mVoiceExit);
	}
	
	private void resetVoiceStep() {
		mCurIndex = 0;
		mVoiceDrawableView.setImageDrawable(mDrawables[mCurIndex]);
		UI2Manager.removeUIThread(mVoiceExit);
	}

	int mCurIndex;

	Runnable mVoiceExit = new Runnable() {

		@Override
		public void run() {
//			LogUtil.loge("mCurIndex:" + mCurIndex);
			if (mCurIndex >= mResId.length) {
				UI2Manager.removeUIThread(this);
				resetVoiceStep();
				currState = STATE_ENDED;
				if (mListener != null) {
					mListener.onAnimationStep(RecStartView.this, STATE_ENDED);
				}
				return;
			}
			mVoiceDrawableView.setImageDrawable(mDrawables[mCurIndex]);

			mCurIndex++;
			UI2Manager.removeUIThread(this);
			UI2Manager.runOnUIThread(this, mExitDelay);
		}
	};
}