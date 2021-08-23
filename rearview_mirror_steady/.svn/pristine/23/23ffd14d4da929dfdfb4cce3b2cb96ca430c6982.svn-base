package com.txznet.comm.ui.theme.test.rec;

import android.content.Context;
import android.util.AttributeSet;

public class SearchView extends AbsStepView {
	private int mCurState = STATE_ENDED;
	private SearchAnimView mAnimView;
	
	public SearchView(Context context) {
		this(context, null);
	}
	
	public SearchView(Context context, AttributeSet attr) {
		super(context, attr);
		mAnimView = new SearchAnimView(this,getContext());
		
		addView(mAnimView,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
	}
	
	
	
	@Override
	public void playStartAnim() {
		mCurState = STATE_START;
		setVisibility(VISIBLE);
		mAnimView.playStartAnim();
	}

	@Override
	public void playLoopAnim() {
		
	}
	
	@Override
	public boolean isAniming() {
		return mCurState != STATE_ENDED;
	}

	@Override
	public void playEndAnim() {
		mAnimView.playStopAnim();
		if (mListener != null) {
			mCurState = STATE_ENDED;
			mListener.onAnimationStep(this, STATE_ENDED);
		}
	}

	@Override
	public void release() {
		mAnimView.release();
	}

}
