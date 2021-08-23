package com.txznet.comm.ui.theme.test.rec;


import android.content.Context;
import android.util.AttributeSet;

public class RecordView extends AbsStepView {
	private RecordAnimView mVoiceView;
	
	public RecordView(Context context) {
		this(context, null);
	}
	
	public RecordView(Context context, AttributeSet attr) {
		super(context, attr);
		mVoiceView = new RecordAnimView(this,getContext());
		
		addView(mVoiceView,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
	}

	@Override
	public void updateVol(int vol) {
		if (mVoiceView!=null) {
			mVoiceView.updateVol(vol);
		}
	}
	
	@Override
	public void playStartAnim() {
		setVisibility(VISIBLE);
		if (mVoiceView!=null) {
			mVoiceView.playStartAnim();
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
			mVoiceView.playEndAnim();
		}
	}

	@Override
	public void release() {
		mVoiceView.release();
	}

}
