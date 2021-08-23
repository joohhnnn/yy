package com.txznet.record.view.rec;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.record.lib.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class RecStepView extends AbsStepView {
	public static final String TAG = RecStepView.class.getSimpleName();

	boolean mIsPlayInter = false;
	private AbsStepView mCurStepView;
	private AbsStepView mNextStepView;
	private AbsStepView[] mStepViews;

	private RecordStepAnimListener mStepAnimListener = new RecordStepAnimListener() {

		@Override
		public void onAnimationStep(AbsStepView parent, int state) {
			doStepAnimState(parent, state);
		}
	};

	public RecStepView(Context context) {
		super(context);

		initStepView();
	}

	public RecStepView(Context context, AttributeSet attr) {
		super(context, attr);

		initStepView();
	}

	private void initStepView() {
		final View con = inflate(getContext(), R.layout.rec_step_ly, this);

		mStepViews = new AbsStepView[3];
		mStepViews[0] = (RecStartView) con.findViewById(R.id.rec_widget_start);
		mStepViews[1] = (AbsStepView) con.findViewById(R.id.rec_widget_record);
		mStepViews[2] = (AbsStepView) con.findViewById(R.id.rec_widget_search);

		for (AbsStepView asv : mStepViews) {
			asv.setRecordStepListener(mStepAnimListener);
			asv.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void playStartAnim() {
	}

	@Override
	public void playLoopAnim() {
	}

	@Override
	public void playEndAnim() {
	}

	@Override
	public boolean isAniming() {
		return false;
	}

	private void doStepAnimState(AbsStepView parent, int state) {
//		LogUtil.loge("parent:" + parent.getClass().getName() + ",state:" + state);
		if (mCurStepView != null && mCurStepView == parent && state == STATE_ENDED) {
			procStepView();
		}
	}

	private void procStepView() {
		if (mCurStepView == mNextStepView) {
			if (!mIsPlayInter) {
				return;
			}
		}

		if (mCurStepView != null && mCurStepView.isAniming()) {
			mCurStepView.playEndAnim();
			return;
		}

		if (mNextStepView != null) {
			for (int i = 0; i < mStepViews.length; i++) {
				if (mNextStepView != mStepViews[i]) {
					mStepViews[i].setVisibility(INVISIBLE);
				}
			}

			mNextStepView.setVisibility(VISIBLE);
			mNextStepView.playStartAnim();
			mCurStepView = mNextStepView;
		}
	}

	Runnable mProcView = new Runnable() {

		@Override
		public void run() {
			procStepView();
		}
	};

	public void onStart() {
		if (mStepViews[0] == mNextStepView && mCurStepView != null && mCurStepView.isAniming()) {
			return;
		}
		mNextStepView = mStepViews[0];
		post(mProcView);
	}

	public void onRecord() {
		if (mStepViews[1] == mNextStepView && mCurStepView != null && mCurStepView.isAniming()) {
			return;
		}

		mNextStepView = mStepViews[1];
		post(mProcView);
	}

	public void onLoading() {
		if (mStepViews[2] == mNextStepView && mCurStepView != null && mCurStepView.isAniming()) {
			return;
		}
		mNextStepView = mStepViews[2];
		post(mProcView);
	}

	public void onShow() {
		onStart();
	}

	public void onDismiss() {
		mCurStepView = null;
		mNextStepView = null;
		updateVol(0);
	}

	@Override
	public void updateVol(int vol) {
		if (mStepViews == null) {
			throw new NullPointerException("onUpdateVol throw null！");
		}

		for (AbsStepView stepView : mStepViews) {
			stepView.updateVol(vol);
		}
	}
}