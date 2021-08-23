package com.txznet.comm.ui.recordwin;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.util.ScreenLock;
import com.txznet.txz.comm.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

//UI2.0Dialog，去掉了style等需要资源ID的东西
// show -> onResume -> onLoseTop -> dismiss
public abstract class Win2Dialog extends Dialog {
	private Object mData; // 可以携带一个额外业务信息
	protected View mView;
	protected boolean mIsFull;
	protected boolean mViewInited = false;
	public static int mSystemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_FULLSCREEN;

	public Win2Dialog(Context context) {
		this(context, false);
	}

	public Win2Dialog() {
		this(ActivityStack.getInstance().currentActivity() == null ? GlobalContext.get()
				: ActivityStack.getInstance().currentActivity());
		if (ActivityStack.getInstance().currentActivity() == null) {
			getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
		}
	}
	
	public Win2Dialog(Context context,boolean fullScreen) {
		super(context, fullScreen ? R.style.TXZ_Dialog_Style_Full : R.style.TXZ_Dialog_Style);
		mIsFull = fullScreen;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mView = createView();
		ViewParent viewParent = mView.getParent();
		if (viewParent!=null) {
			if (viewParent instanceof ViewGroup) {
				((ViewGroup)viewParent).removeView(mView);
			}
		}
		if (fullScreen) {
			mView.setSystemUiVisibility(mSystemUiVisibility);
			WindowManager.LayoutParams windowAttributes = getWindow().getAttributes();
			windowAttributes.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | windowAttributes.flags;
		}else {
			mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		}
		setContentView(mView);
	}
	
	public Win2Dialog(Context context,boolean fullScreen,Object... objects) {
		super(context, fullScreen ? R.style.TXZ_Dialog_Style_Full : R.style.TXZ_Dialog_Style);
		mIsFull = fullScreen;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mView = createView(objects);
		ViewParent viewParent = mView.getParent();
		if (viewParent!=null) {
			if (viewParent instanceof ViewGroup) {
				((ViewGroup)viewParent).removeView(mView);
			}
		}
		if (fullScreen) {
			mView.setSystemUiVisibility(mSystemUiVisibility);
			WindowManager.LayoutParams windowAttributes = getWindow().getAttributes();
			windowAttributes.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | windowAttributes.flags;
		}else {
			mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		}
		setContentView(mView);
	}
	
	
	public void setIsFullSreenDialog(boolean isFullScreen) {
		LogUtil.logd("setIsFullScreenDialog:" + isFullScreen);
		mIsFull = isFullScreen;
	}
	
	public void updateDialogType(int type){
		LogUtil.logd("updateDialogType type:" + type);
		getWindow().setType(type);
	}

	public Win2Dialog(boolean isSystem) {
		this(isSystem ? GlobalContext.get() : ActivityStack.getInstance().currentActivity());
		if (isSystem) {
			getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
		}
	}
	
	public Win2Dialog(boolean isSystem, boolean isFullScreen) {
		this(isSystem ? GlobalContext.get() : ActivityStack.getInstance().currentActivity(), isFullScreen);
		if (isSystem) {
			getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
		}
	}

	public Win2Dialog(boolean isSystem, boolean isFullScreen, Object... objects) {
		this(isSystem ? GlobalContext.get() : ActivityStack.getInstance().currentActivity(), isFullScreen, objects);
		if (isSystem) {
			getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
		}
	}
	
	
	protected abstract View createView(Object...objects);

	public Win2Dialog setData(Object data) {
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

		if (newFocus && mHasFocus){
			onGetFocus();
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