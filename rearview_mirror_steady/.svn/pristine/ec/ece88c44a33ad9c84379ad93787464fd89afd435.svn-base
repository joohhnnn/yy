package com.txznet.resholder.wave.rec;


import com.txznet.comm.ui.util.LayouUtil;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;

public class RecordView extends AbsStepView {
	private SoundWaveView mVoiceView;
	
	public RecordView(Context context) {
		this(context, null);
	}
	
	public RecordView(Context context, AttributeSet attr) {
		super(context, attr);
		mVoiceView = new SoundWaveView(getContext());
		LayoutParams layoutParams = new LayoutParams((int)LayouUtil.getDimen("x70"), (int)LayouUtil.getDimen("y70"));
		layoutParams.gravity = Gravity.CENTER;
		addView(mVoiceView,layoutParams);
	}
	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
	}

	@Override
	public void updateVol(int vol) {
		if (mVoiceView!=null) {
//			mVoiceView.updateVol(vol);
		}
	}
	
	@Override
	public void playStartAnim() {
		setVisibility(VISIBLE);
		if (mVoiceView!=null) {
			mVoiceView.start();
		}
		if (mListener != null) {
			mListener.onAnimationStep(this, AbsStepView.STATE_START);
		}
	}

	@Override
	public void playLoopAnim() {
		
	}
	
	@Override
	public boolean isAniming() {
		if (mVoiceView != null) {
			return mVoiceView.isAniming();
		}
		return false;
	}

	@Override
	public void playEndAnim() {
		setVisibility(GONE);
		if (mVoiceView!=null) {
			mVoiceView.stop();
		}
		if (mListener != null) {
			mListener.onAnimationStep(this, AbsStepView.STATE_ENDED);
		}
	}

}
