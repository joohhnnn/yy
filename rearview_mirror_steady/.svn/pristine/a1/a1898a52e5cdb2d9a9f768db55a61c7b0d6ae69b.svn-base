package com.txznet.resholder.wave.rec;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public abstract class AbsStepView extends FrameLayout {
	public static final int STATE_START = 1;
	public static final int STATE_LOOP = 2;
	public static final int STATE_ENDED = 3;

	protected RecordStepAnimListener mListener;

	public AbsStepView(Context context) {
		super(context);
	}

	public AbsStepView(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public void updateVol(int vol) {
	}

	public abstract void playStartAnim();

	public abstract void playLoopAnim();

	public abstract void playEndAnim();

	public abstract boolean isAniming();

	public void setRecordStepListener(RecordStepAnimListener listener) {
		mListener = listener;
	}

	public static interface RecordStepAnimListener {

		public void onAnimationStep(AbsStepView parent, int state);

	}
}