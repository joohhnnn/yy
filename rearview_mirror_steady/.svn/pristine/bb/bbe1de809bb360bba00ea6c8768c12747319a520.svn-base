package com.txznet.resholder.wave.rec;

import com.txznet.comm.ui.util.LayouUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout.LayoutParams;

public class SearchView extends AbsStepView {
	private int mCurState = STATE_ENDED;
	private SoundWaveView mAnimView;
	
	public SearchView(Context context) {
		this(context, null);
	}
	
	public SearchView(Context context, AttributeSet attr) {
		super(context, attr);
		mAnimView = new SoundWaveView(getContext());
		LayoutParams layoutParams = new LayoutParams((int)LayouUtil.getDimen("x70"), (int)LayouUtil.getDimen("y70"));
		layoutParams.gravity = Gravity.CENTER;
		addView(mAnimView,layoutParams);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
	}
	
	@Override
	public void playStartAnim() {
		mCurState = STATE_START;
		setVisibility(VISIBLE);
		mAnimView.startLoading();
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
		mCurState = STATE_ENDED;
		mAnimView.stopLoading();
		if (mListener != null) {
			mListener.onAnimationStep(this, STATE_ENDED);
		}
	}

}
