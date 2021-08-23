package com.txznet.record2.winrecord.yidong;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.ScreenLock;
import com.txznet.record.lib.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * 显示基类，不是必须
 * @author Terry
 */
// show -> onResume -> onLoseTop -> dismiss
public abstract class WinDialog extends Dialog {
	private Object mData; // 可以携带一个额外业务信息
	protected View mView;
	protected boolean mIsFull;
	protected boolean mViewInited = false;
	public static int mSystemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;

	public WinDialog(Context context) {
		this(context, false);
	}


	public WinDialog(Context context,boolean fullScreen) {
		super(context, R.style.TXZ_Dialog_Style_Yidong);
		mIsFull = fullScreen;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mView = createView();
		ViewParent viewParent = mView.getParent();
		if (viewParent!=null) {
			if (viewParent instanceof ViewGroup) {
				((ViewGroup)viewParent).removeView(mView);
			}
		}
		getWindow().setType(2021);
		mView.setSystemUiVisibility(mSystemUiVisibility);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		getWindow().addFlags(LayoutParams.FLAG_DIM_BEHIND | LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		setContentView(mView);
	}
	
	public WinDialog(Context context,boolean fullScreen,Object... objects) {
		super(context, R.style.TXZ_Dialog_Style_Yidong);
		mIsFull = fullScreen;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mView = createView(objects);
		ViewParent viewParent = mView.getParent();
		if (viewParent!=null) {
			if (viewParent instanceof ViewGroup) {
				((ViewGroup) viewParent).removeView(mView);
			}
		}
		getWindow().setType(2021);
		mView.setSystemUiVisibility(mSystemUiVisibility);
		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		getWindow().addFlags(LayoutParams.FLAG_DIM_BEHIND | LayoutParams.FLAG_LAYOUT_IN_SCREEN);
		setContentView(mView);
	}
	
	
	public void setIsFullSreenDialog(boolean isFullScreen) {
		LogUtil.logd("setIsFullScreenDialog:" + isFullScreen);
		mIsFull = isFullScreen;
	}
	
	public void updateDialogType(int type){
		LogUtil.logd("updateDialogType type:" + type);
//		getWindow().setType(type);
	}
	

	public WinDialog(boolean isSystem, boolean isFullScreen, Object... objects) {
		this(GlobalContext.get(), isFullScreen, objects);
//		getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
	}
	
	protected abstract View createView(Object...objects);

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