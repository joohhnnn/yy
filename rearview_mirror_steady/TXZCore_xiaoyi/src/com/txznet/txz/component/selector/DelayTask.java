package com.txznet.txz.component.selector;

import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.util.Log;

public class DelayTask {
	public static final int AUTO_CALL_PERIOD = 50;

	protected float mProgress = 0;
	protected long mProgressBeginTime;
	protected boolean mCanAutoPerform;
	private long mDelay;

	Runnable mEndRunnable;

	public DelayTask(Runnable endRunnable, long delay) {
		this.mEndRunnable = endRunnable;
		this.mDelay = delay;
	}

	Runnable mRunnableAddProgress = new Runnable() {
		@Override
		public void run() {
			AppLogic.removeBackGroundCallback(mRunnableAddProgress);
			mProgress += (100.0f * AUTO_CALL_PERIOD / mDelay);
			RecorderWin.refreshProgressBar(Math.round(mProgress), 0);
			Log.d("Progress", "Progress:" + mProgress);
			if (mProgress >= 100.0f) {
				if (mEndRunnable != null) {
					mEndRunnable.run();
				}
				return;
			}
			AppLogic.runOnBackGround(mRunnableAddProgress, AUTO_CALL_PERIOD);
		}
	};

	protected Runnable mRunnableContinueProgress = new Runnable() {
		@Override
		public void run() {
			mProgressBeginTime = 0;
			continueProgress();
		}
	};

	public void continueProgress() {
		JNIHelper.logd("continueProgress");
		AppLogic.removeBackGroundCallback(mRunnableAddProgress);
		AppLogic.runOnBackGround(mRunnableAddProgress, AUTO_CALL_PERIOD);
	}

	public void pauseProgress() {
		JNIHelper.logd("pauseProgress");
		AppLogic.removeBackGroundCallback(mRunnableAddProgress);
	}

	public void clearProgress() {
		mProgress = 0;
		JNIHelper.logd("clearProgress");
		AppLogic.removeBackGroundCallback(mRunnableContinueProgress);
		mProgressBeginTime = 0;
		AppLogic.removeBackGroundCallback(mRunnableAddProgress);
	}
}
