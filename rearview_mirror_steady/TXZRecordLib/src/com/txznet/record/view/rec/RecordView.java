package com.txznet.record.view.rec;


import android.content.Context;
import android.util.AttributeSet;

public class RecordView extends AbsStepView {
	private RecordAnimView mVoiceView;
	
	public RecordView(Context context, AttributeSet attr) {
		super(context, attr);
	}
	
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mVoiceView = new RecordAnimView(this,getContext());
		
		addView(mVoiceView,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
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

}
