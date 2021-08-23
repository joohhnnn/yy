package com.txznet.resholder.wave.rec;

import com.txznet.comm.ui.util.LayouUtil;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

public class SearchAnimView extends View {
	private static final String TAG = SearchAnimView.class.getSimpleName();

	private int mWidth;
	private int mHeight;
	private int mCenterX;
	private float mCenterY;
	private int mLastX;
	private int mSpeed = 10;
	private int mCount = 20;
	private int mStartX,mEndX;

	private Drawable mLeftDrawable;
	private Drawable mRightDrawable;
	private boolean isNeedStop = false;
	private static final int ANIM_DELAY_TIME = 50;
	private static final int STATE_GO_RIGHT = 1;
	private static final int STATE_GO_LEFT = 2;
	private static final int STATE_GO_STOP = 3;
	private int mState = STATE_GO_RIGHT;
	private AbsStepView parentView;

	public SearchAnimView(AbsStepView parentView,Context context) {
		super(context);
		this.parentView = parentView;
		mLeftDrawable = LayouUtil.getDrawable("ic_search_left");
		mRightDrawable = LayouUtil.getDrawable("ic_search_right");
		mLeftDrawable.setBounds(-mLeftDrawable.getIntrinsicWidth() / 2,
				-mLeftDrawable.getIntrinsicHeight() / 2,
				mLeftDrawable.getIntrinsicWidth() / 2,
				mLeftDrawable.getIntrinsicHeight() / 2);
		mRightDrawable.setBounds(-mRightDrawable.getIntrinsicWidth() / 2,
				-mRightDrawable.getIntrinsicHeight() / 2,
				mRightDrawable.getIntrinsicWidth() / 2,
				mRightDrawable.getIntrinsicHeight() / 2);
	}
	
	private Runnable playAnimRunnable = new Runnable() {
		
		@Override
		public void run() {
			invalidate();
			removeCallbacks(this);
			postDelayed(this, ANIM_DELAY_TIME);
		}
	};
	
	public void playStartAnim() {
		isNeedStop = false;
		removeCallbacks(playAnimRunnable);
		post(playAnimRunnable);
		if (parentView.mListener!=null) {
			parentView.mListener.onAnimationStep(parentView, AbsStepView.STATE_START);
		}
	}
	
	public void playStopAnim() {
		isNeedStop = true;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			mWidth = getWidth();
			mHeight = getHeight();
			mCenterX = mWidth / 2;
			mCenterY = mHeight / 2;

			mSpeed = mWidth / mCount;
			mStartX = mLeftDrawable.getIntrinsicWidth()/2;
			mEndX = mWidth - mRightDrawable.getIntrinsicWidth()/2;
			mLastX = mStartX;
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		switch (mState) {
		case STATE_GO_RIGHT:
			mLastX += mSpeed;
			if (mLastX > mEndX) {
				mLastX = mEndX;
				mState = STATE_GO_LEFT;
				canvas.translate(mLastX, mCenterY);
				canvas.scale(0.7f, 1f, 0, 0);
			}else {
				canvas.translate(mLastX, mCenterY);
			}
			mRightDrawable.draw(canvas);
			break;
		case STATE_GO_LEFT:
			mLastX -= mSpeed;
			if (mLastX < mStartX) {
				mLastX = mStartX;
				mState = STATE_GO_RIGHT;
				canvas.translate(mLastX, mCenterY);
				canvas.scale(0.7f, 1f, 0, 0);
			}else {
				canvas.translate(mLastX, mCenterY);
			}
			mLeftDrawable.draw(canvas);
			break;
		case STATE_GO_STOP:
			mLastX -= mSpeed;
			if (mLastX < mCenterX) {
				mLastX = mCenterX;
				mState = STATE_GO_RIGHT;
				removeCallbacks(playAnimRunnable);
				Log.e(TAG, "real stop:");
//				if (parentView.mListener!=null) {
//					parentView.mListener.onAnimationStep(parentView, AbsStepView.STATE_ENDED);
//				}
			}
			canvas.translate(mLastX, mCenterY);
			mLeftDrawable.draw(canvas);
			break;
		}
		
	}

}
