package com.txznet.record.view.rec;

import android.content.Context;
import android.util.AttributeSet;

public class SearchView extends AbsStepView {
	private int mCurState = STATE_ENDED;
	private SearchAnimView mAnimView;
	
	public SearchView(Context context, AttributeSet attr) {
		super(context, attr);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mAnimView = new SearchAnimView(this,getContext());
		
		addView(mAnimView,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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

}
