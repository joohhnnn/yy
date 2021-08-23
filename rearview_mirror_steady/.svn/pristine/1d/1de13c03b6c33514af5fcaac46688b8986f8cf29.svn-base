package com.txznet.record2.winrecord.yidong;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable.HomeObserver;
import com.txznet.comm.ui.ScrollObservable.OnSizeObserver;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.layout.layout1.TXZWinLayout2;
import com.txznet.comm.ui.recordwin.RecordWin2True;
import com.txznet.comm.ui.recordwin.RecordWinBase;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.ScreenLock;
import com.txznet.record.lib.R;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * RecordWin2Impl1界面显示的窗口
 *
 */
public class WinRecordTrue extends WinDialog {

	private IWinLayout mWinLayout;
	public static int mDisplayX;
	public static int mDisplayY;
	public static int mDisplayWidth;
	public static int mDisplayHeight;
	private static int mContentWidth = 0;
	private static boolean mIfSetWinBg = false;
	public static Boolean mDialogCancel = null;
	public static boolean mDialogCanceledOnTouchOutside = true;
	public static boolean mAllowOutSideClickSentToBehind = true;

	public WinRecordTrue(boolean fullScreen) {
		super(true, fullScreen);
		initRecordWin();
	}

	public WinRecordTrue(boolean fullScreen, IWinLayout winLayout) {
		super(true, fullScreen, winLayout);
		initRecordWin();
	}

	private void initRecordWin() {
		setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.button.back", null,
						null);
				dismiss();
			}
		});
		mScreenLock = new ScreenLock(getContext());
	}

	public void setWinType(int type) {
	}

	public void setSystemUiVisibility(int type) {
	}

	public void setWinFlags(int flags) {
	}

	public static void setContentWidth(int width){
		mContentWidth = width;
	}


	WindowManager mWinManager;

	public void setDialogCancel(boolean flag){
		mDialogCancel = flag;
		LogUtil.logd("set dialog cacelable " + flag);
		setCancelable(flag);
	}
	public static void setIfSetWinBg(boolean mIfSetWinBg) {
		WinRecordTrue.mIfSetWinBg = mIfSetWinBg;
	}

	@Override
	public void setIsFullSreenDialog(boolean isFullScreen) {
		if (mIsFull == isFullScreen) {
			return;
		}
		LogUtil.logd("setIsFullScreenDialog:" + isFullScreen);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogUtil.logd("onKeyDown:" + keyCode);
		// TODO 如果需要支持方控，自己在这里进行处理
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected View createView(Object... objects) {
		if (objects.length > 0) {
			mWinLayout = (IWinLayout) objects[0];
		}
		if (mWinLayout == null) {
			// 初始化时设置默认的WinLayout，WinLayoutManager初始化完成
			// 后再更新成实际的WinLayout
			TXZWinLayout2.getInstance().init();
			mWinLayout = TXZWinLayout2.getInstance();
		}

		View view = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.dialog_wrapper_yidong, null);
		FrameLayout root = (FrameLayout) view.findViewById(R.id.layout_root);
		View windowView = null;
		if (mWinLayout != null) {
			windowView = mWinLayout.get();
			ViewParent viewParent = windowView.getParent();
			if (viewParent != null) {
				if (viewParent instanceof ViewGroup) {
					((ViewGroup) viewParent).removeView(windowView);
				}
			}
		}
		root.addView(windowView, android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		return view;
	}

	// private boolean isInit = false;

	private static Float mWinBgAlpha = null;

	public void setWinBgAlpha(Float winBgAlpha) {
		if (winBgAlpha < 0.0f || winBgAlpha > 1.0f)
			return;
		mWinBgAlpha = winBgAlpha;
		LogUtil.logi("setWinBgAlpha:" + winBgAlpha);
	}

	public void init() {
		LogUtil.logd("[UI2.0] init RecordWin2True");
		ConfigUtil.initScreenType(getWindow().getDecorView());
		ConfigUtil.checkViewRect(getWindow().getDecorView());
		// isInit = true;
	}

	public void updateWinLayout(IWinLayout winLayout) {
	/*		LogUtil.logd("[UI2.0] update winLayout");
	 		if (winLayout != null && !winLayout.equals(mWinLayout) && mView != null) {
			mWinLayout = winLayout;
			((ViewGroup)mView).removeAllViews();
			View layoutView = mWinLayout.get();
			ViewParent viewParent = layoutView.getParent();
			if (viewParent != null) {
				if (viewParent instanceof ViewGroup) {
					((ViewGroup) viewParent).removeView(layoutView);
				}
			}
//			layoutView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//			getWindow().addFlags(LayoutParams.FLAG_DIM_BEHIND | LayoutParams.FLAG_LAYOUT_IN_SCREEN);
//			setContentView(mView);
			((ViewGroup) mView).addView(layoutView,android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		}
		*/
	}

	// 是否注册了监听器
	private boolean mRegisted = false;
	// 是否锁定了屏幕锁
	private boolean mHasScreenLock;

	private HomeObserver mHomeObserver = new HomeObserver() {
		@Override
		public void onHomePressed() {
			dismiss();
		}
	};

	private OnSizeObserver mOnSizeObserver = new OnSizeObserver() {
		@Override
		public void onResSize() {
			// scrollToEnd();
		}
	};

	private WinRecordCycleObserver mCycleObserver = null;

	@Override
	public void show() {
		LayoutParams attributes = getWindow().getAttributes();
		attributes.type = 2021;
		attributes.flags = 8388866;
		getWindow().setAttributes(attributes);
		if (!mRegisted) {
			mRegisted = true;
			GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
			GlobalObservableSupport.getScrollObservable().registerObserver(mOnSizeObserver);
			if (mCycleObserver != null) {
				try {
					GlobalObservableSupport.getWinRecordObserver().registerObserver(mCycleObserver);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (!mHasScreenLock) {
			if(mScreenLock==null){
				mScreenLock = new ScreenLock(getContext());
			}
			mScreenLock.lock();
			mHasScreenLock = true;
		}
		String background = ConfigUtil.getDialogBackgroundName();
		if (TextUtils.isEmpty(background)) {
			background = "dialog_bg";
		}
//		if (mIfSetWinBg || mView == null) {
//			getWindow().setBackgroundDrawable(LayouUtil.getDrawable(background));
//		} else {
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		mView.setBackgroundDrawable(LayouUtil.getDrawable(background));
//		}

		if (mView == null) {
			ConfigUtil.checkViewRect(getWindow().getDecorView());
		}else {
			ConfigUtil.checkViewRect(mView);
		}
//		WinLayoutManager.getInstance().updateState(0);
		super.show();
		resume();
	}

	// 复位
	private void resume() {
		// mWinLayout.reset();
	}

	@Override
	public void dismiss() {
		LogUtil.logd("WinRecordTrue dismiss");
		AsrUtil.closeRecordWinLock();
		if (mHasScreenLock) {
			mScreenLock.release();
			mHasScreenLock = false;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.dismiss", null, null);
		if (Looper.myLooper() != Looper.getMainLooper()) {
			UI2Manager.runOnUIThread(new Runnable() {
				@Override
				public void run() {
					WinRecordTrue.super.dismiss();
					notifyDismiss();
					if (mWinLayout != null) {
						mWinLayout.reset();
					}
				}
			}, 0);
		} else {
			super.dismiss();
			notifyDismiss();
			if (mWinLayout != null) {
				mWinLayout.reset();
			}
		}
	}

	@Override
	protected void onLoseFocus() {
		GlobalObservableSupport.getWinRecordObserver().onLoseFocus();
	}

	@Override
	protected void onGetFocus() {
		GlobalObservableSupport.getWinRecordObserver().onGetFocus();
	}

	public void setWinRecordObserver(WinRecordCycleObserver observer) {
		this.mCycleObserver = observer;
	}

	private void notifyDismiss(){
		if (mRegisted) {
			GlobalObservableSupport.getWinRecordObserver().onDismiss();
			GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeObserver);
			GlobalObservableSupport.getScrollObservable().unregisterObserver(mOnSizeObserver);
			if (mCycleObserver != null) {
				// 有可能show之后这里才非空的,没有register直接unregister会报错
				try {
					GlobalObservableSupport.getWinRecordObserver().unregisterObserver(mCycleObserver);
				} catch (Exception e) {
				}
			}
			mRegisted = false;
		}
	}

	public void release() {
		mWinLayout.release();
		mView = null;
		TextView textView = new TextView(getContext());
		setContentView(textView);
		if (mRegisted) {
			GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeObserver);
			GlobalObservableSupport.getScrollObservable().unregisterObserver(mOnSizeObserver);
			if (mCycleObserver != null) {
				// 有可能show之后这里才非空的,没有register直接unregister会报错
				try {
					GlobalObservableSupport.getWinRecordObserver().unregisterObserver(mCycleObserver);
				} catch (Exception e) {
				}
			}
			mRegisted = false;
		}
		if (ImageLoader.getInstance().isInited()) {
			ImageLoader.getInstance().clearDiskCache();
			ImageLoader.getInstance().clearMemoryCache();
		}
		LayouUtil.release();
		System.gc();
	}

	public void setDialogCanceledOnTouchOutside (boolean canceledOnTouchOutside){
		mDialogCanceledOnTouchOutside = canceledOnTouchOutside;
	}
	
	
	public void setAllowOutSideClickSentToBehind(boolean allowOutSideClickSentToBehind) {
		RecordWin2True.mAllowOutSideClickSentToBehind = allowOutSideClickSentToBehind;
	}
}
