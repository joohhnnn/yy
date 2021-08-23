package com.txznet.comm.ui.dialog;

import com.txznet.comm.ui.IKeepClass;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.ScreenLock;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.comm.R;

// show -> onResume -> onLoseTop -> dismiss
public abstract class WinDialog extends Dialog implements IKeepClass{
	private Object mData; // 可以携带一个额外业务信息
	protected View mView;
	protected boolean mIsFull;
	public static int mType = -1;
	public static int mTimeout = -1;
	public static int mSystemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN;

	public WinDialog(Context context) {
		this(context, false, false);
	}

	public WinDialog() {
		this(ActivityStack.getInstance().currentActivity() == null ? GlobalContext.getModified()
				: ActivityStack.getInstance().currentActivity());
	}
	
	public WinDialog(Context context, boolean fullScreen) {
		this(context, fullScreen, false);
	}
	
	public WinDialog(boolean isSystem) {
		this(isSystem ? GlobalContext.getModified() : ActivityStack.getInstance().currentActivity(), false, isSystem);
	}
	
	public WinDialog(boolean isSystem, boolean isFullScreen) {
		this(isSystem ? GlobalContext.getModified() : ActivityStack.getInstance().currentActivity(), isFullScreen, isSystem);
	}
	
	public WinDialog(Context context,boolean isFullScreen,boolean isSystem){
		super(context, isFullScreen ? R.style.TXZ_Dialog_Style_Full : R.style.TXZ_Dialog_Style);
		mView = createView();
		mIsFull = isFullScreen;
		if (isFullScreen) {
			mView.setSystemUiVisibility(mSystemUiVisibility);
			// getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new
			// OnSystemUiVisibilityChangeListener() {
			// @Override
			// public void onSystemUiVisibilityChange(int visibility) {
			// LogUtil.logd("onSystemUiVisibilityChange:" + visibility);
			// if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
			// if (mIsFull) {
			// mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			// | View.SYSTEM_UI_FLAG_FULLSCREEN);
			// }
			// }
			// }
			// });
		}
		setContentView(mView);
		if (ActivityStack.getInstance().currentActivity() == null) {
			getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
		}
		if (isSystem) {
			getWindow().setType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
		}
		if (mType != -1) {
			getWindow().setType(mType);
		}
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	/**
	 * 由调用处决定是否要执行超时检测
	 */
	public void checkTimeout() {
		if (mTimeout != -1) {
			LogUtil.logd("winDialog checkTimeout:" + mTimeout);
			if (mTimeout > 0) {
				AppLogicBase.removeUiGroundCallback(mDismissRunnable);
				AppLogicBase.runOnUiGround(mDismissRunnable, mTimeout);
			}
		}
	}
	
	protected Runnable mDismissRunnable = new Runnable() {
		
		@Override
		public void run() {
			onTimeout();
		}
	};
	
	protected void onTimeout() {
		if (isShowing()) {
			dismiss();
		}
	}
	
	public void setIsFullSreenDialog(boolean isFullScreen) {
		LogUtil.logd("setIsFullScreenDialog:" + isFullScreen);
		mIsFull = isFullScreen;
	}
	
	public void updateDialogType(int type){
		LogUtil.logd("updateDialogType type:" + type);
		getWindow().setType(type);
	}

	protected abstract View createView();

	public WinDialog setData(Object data) {
		this.mData = data;
		return this;
	}

	public Object getData() {
		return this.mData;
	}

	protected ScreenLock mScreenLock;
	private boolean mShouldLock;

	public void requestScreenLock() {
		if (mScreenLock == null) {
			mScreenLock = new ScreenLock(getContext());
		}
		mShouldLock = true;
	}

	public void cancelScreenLock() {
		if (mScreenLock != null) {
			mScreenLock.release();
			mScreenLock = null;
		}
	}

	@Override
	public void show() {
		super.show();
		if (mShouldLock && mScreenLock != null) {
			mScreenLock.lock();
		}
		mHasFocus = true;
		onGetFocus();
		getContext().sendBroadcast(new Intent("com.txznet.txz.action.FLOAT_WIN_SHOW"));
		if (mIsFull && ((getWindow().getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)) {
			mView.setSystemUiVisibility(mSystemUiVisibility);		
		}
	}

	@Override
	public void dismiss() {
		mHasFocus = false;
		onLoseFocus();
		super.dismiss();
		if (mScreenLock != null) {
			mScreenLock.release();
		}
		AppLogicBase.removeUiGroundCallback(mDismissRunnable);
		getContext().sendBroadcast(new Intent("com.txznet.txz.action.FLOAT_WIN_DISMISS"));
	}

	private boolean mHasFocus = false;

	public boolean hasFocus() {
		return mHasFocus;
	}

	@Override
	public void onWindowFocusChanged(boolean newFocus) {
		LogUtil.logd(this.toString() + " onWindowFocusChanged: from " + mHasFocus + " to " + newFocus);

		if (mHasFocus != newFocus) {
			mHasFocus = newFocus;
			if (mHasFocus) {
				onGetFocus();
			} else {
				onLoseFocus();
			}
		}

		super.onWindowFocusChanged(newFocus);
	}

	// 失去焦点的回调
	protected void onLoseFocus() {

	}

	// 重新获得焦点的回调
	protected void onGetFocus() {

	}
}