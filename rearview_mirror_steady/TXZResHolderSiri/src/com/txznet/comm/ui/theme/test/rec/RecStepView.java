package com.txznet.comm.ui.theme.test.rec;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

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

	public void release() {
		LogUtil.logd("RecStepView release");
		UI2Manager.removeUIThread(mProcView);
		for (AbsStepView stepView : mStepViews) {
			if(stepView instanceof RecStepView){
				continue;
			}
			stepView.release();
		}
	}
	
	public RecStepView(Context context) {
		super(context);

		initStepView();
	}

	public RecStepView(Context context, AttributeSet attr) {
		super(context, attr);

		initStepView();
	}

	private void initStepView() {
		RelativeLayout rlCon = new RelativeLayout(GlobalContext.get());
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.CENTER;
		addView(rlCon,layoutParams);
		
		mStepViews = new AbsStepView[3];
		
		mStepViews[0] = new RecStartView(GlobalContext.get());
		RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		mRLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		rlCon.addView(mStepViews[0],mRLayoutParams);
		
		mStepViews[1] = new RecordView(GlobalContext.get());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		mRLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		rlCon.addView(mStepViews[1],mRLayoutParams);
		
		mStepViews[2] = new SearchView(GlobalContext.get());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		mRLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		rlCon.addView(mStepViews[2],mRLayoutParams);

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
		LogUtil.logi("parent:" + parent.getClass().getName() + ",state:" + state);
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
		UI2Manager.runOnUIThread(mProcView, 0);
	}

	public void onRecord() {
		if (mStepViews[1] == mNextStepView && mCurStepView != null && mCurStepView.isAniming()) {
			return;
		}

		mNextStepView = mStepViews[1];
		UI2Manager.runOnUIThread(mProcView, 0);
	}

	public void onLoading() {
		if (mStepViews[2] == mNextStepView && mCurStepView != null && mCurStepView.isAniming()) {
			return;
		}
		mNextStepView = mStepViews[2];
		UI2Manager.runOnUIThread(mProcView, 0);
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