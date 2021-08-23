package com.txznet.comm.ui.recordwin;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
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
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.ScreenLock;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * RecordWin2Impl1界面显示的窗口
 *
 */
public class RecordWin2True extends RecordWinBase {

	private IWinLayout mWinLayout;
	public static int mDisplayX;
	public static int mDisplayY;
	public static int mDisplayWidth;
	public static int mDisplayHeight;
	private static int mContentWidth = 0;
	private static boolean mIfSetWinBg = false;
	public static Boolean mDialogCancel = null;
	public static boolean mDialogCanceledOnTouchOutside = true;
	public static boolean mAllowOutSideClickSentToBehind = false;

	public RecordWin2True() {
		super();
		initRecordWin();
	}

	public RecordWin2True(boolean fullScreen) {
		super(true, fullScreen);
		initRecordWin();
	}

	public RecordWin2True(boolean fullScreen, IWinLayout winLayout) {
		super(true, fullScreen, winLayout);
		initRecordWin();
	}

	private void initRecordWin() {
		getWindow().setType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 3);
		//为了兼容已经第三方的皮肤包版本，先不将这个强制修改
		if (mIsFull || ConfigUtil.isUseFullScreenFlag()) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}

		/**
		 * 在输入法弹出的界面唤醒语音，语音界面被输入法盖住，
		 * 去掉{@link WindowManager.LayoutParams#FLAG_LOCAL_FOCUS_MODE}后弹出语音界面会自动关闭输入法
		 *<p>
		 * 【蓝鲸-东风小康】【客户提出】输入法打开界面，唤醒语音，保持输入法界面显示
		 *	https://www.tapd.cn/21709951/bugtrace/bugs/view/1121709951001035557
		 **/
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
//				| WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE
				| WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		notifyLocation();
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
		getWindow().setType(type);
	}

	public void setSystemUiVisibility(int type) {
		LogUtil.logd("RecordWin2True setSystemUiVisibility :" + type);
		mSystemUiVisibility = type;
		if (mView != null) {
			mView.post(new Runnable1<Integer>(type) {
				@Override
				public void run() {
					if (mView != null) {
						mView.setSystemUiVisibility(mP1);
					}
				}
			});
		}
	}

	public void setWinFlags(int flags) {
		LayoutParams attrs = getWindow().getAttributes();
		attrs.flags = flags;
		getWindow().setAttributes(attrs);
	}
	public void setWinSoft(int soft) {
		LogUtil.d("win2true setWinSoft :"+soft);
		getWindow().setSoftInputMode(soft);
	}

	public static void setContentWidth(int width){
		mContentWidth = width;
	}

	public void updateDisplayArea(int x, int y, int width, int height) {
		mDisplayX = x;
		mDisplayY = y;
		mDisplayWidth = width;
		mDisplayHeight = height;
		notifyLocation();
	}

	public void notifyLocation(){
		WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
		if (mDisplayWidth <= 0 && mDisplayWidth != LayoutParams.MATCH_PARENT
				&& mDisplayWidth != LayoutParams.WRAP_CONTENT) {
			mDisplayWidth = LayoutParams.MATCH_PARENT;
		}
		if (mDisplayHeight <= 0 && mDisplayHeight != LayoutParams.MATCH_PARENT
				&& mDisplayHeight != LayoutParams.WRAP_CONTENT) {
			mDisplayHeight = LayoutParams.MATCH_PARENT;
		}
		layoutParams.x = mDisplayX;
		layoutParams.y = mDisplayY;
		layoutParams.width = mDisplayWidth;
		layoutParams.height = mDisplayHeight;
		if (mDisplayWidth !=LayoutParams.MATCH_PARENT || mDisplayHeight != LayoutParams.MATCH_PARENT) {
			layoutParams.flags |= WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;
			if(mAllowOutSideClickSentToBehind) {
				layoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
			}
			getWindow().setGravity(Gravity.LEFT | Gravity.TOP);
		}
		if (mDialogCancel != null) {
			setCancelable(mDialogCancel);
		}
		getWindow().setAttributes(layoutParams);
	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			if (mDialogCanceledOnTouchOutside) {
				dismiss();
			}
		}
		return super.onTouchEvent(event);
	}

	WindowManager mWinManager;

	public void setDialogCancel(boolean flag){
		mDialogCancel = flag;
		LogUtil.logd("set dialog cacelable " + flag);
		setCancelable(flag);
	}
	public static void setIfSetWinBg(boolean mIfSetWinBg) {
		RecordWin2True.mIfSetWinBg = mIfSetWinBg;
	}

	@Override
	public void setIsFullSreenDialog(boolean isFullScreen) {
		if (mIsFull == isFullScreen) {
			return;
		}
		LogUtil.logd("setIsFullScreenDialog:" + isFullScreen);
		// sInstance = new RecordWin2True(isFullScreen);
		// sInstance.updateWinLayout(WinLayoutManager.getInstance().getLayout());
		// sInstance.init();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogUtil.logd("onKeyDown:" + keyCode);
		return RecordWin2Manager.getInstance().onKeyEvent(keyCode);
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
		View windowView = null;
		if (mWinLayout != null) {
			windowView = mWinLayout.get();
		}
		int layoutLeft = ConfigUtil.getLayoutPaddingLeft();
		int layoutTop = ConfigUtil.getLayoutPaddingTop();
		int layoutRight = ConfigUtil.getLayoutPaddingRight();
		int layoutBottom = ConfigUtil.getLayoutPaddingBottom();
		if (mContentWidth > 0 || layoutLeft != 0 || layoutTop != 0 || layoutRight != 0 || layoutBottom != 0) {
			RelativeLayout llLayout = new RelativeLayout(getContext());
			llLayout.setPadding(layoutLeft, layoutTop, layoutRight, layoutBottom);
			int width = mContentWidth > 0 ? mContentWidth : RelativeLayout.LayoutParams.MATCH_PARENT;
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width , RelativeLayout.LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			llLayout.addView(windowView,layoutParams);
			LogUtil.logd("layout padding left::" + layoutLeft + " top::" +
					layoutTop + " right::" + layoutRight + " bottom::" + layoutBottom
					+ " contentWidth::" + mContentWidth);
			return llLayout;
		}
		return windowView;
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
		notifyLocation();
		ConfigUtil.initScreenType(getWindow().getDecorView());
		ConfigUtil.checkViewRect(getWindow().getDecorView());
		// isInit = true;
	}

	public void updateWinLayout(IWinLayout winLayout) {
		LogUtil.logd("[UI2.0] update winLayout");
		if (winLayout != null && !winLayout.equals(mWinLayout)) {
			IWinLayout oldWinLayout = mWinLayout;
			mWinLayout = winLayout;
			mView = mWinLayout.get();
			ViewParent viewParent = mView.getParent();
			if (viewParent != null) {
				if (viewParent instanceof ViewGroup) {
					((ViewGroup) viewParent).removeView(mView);
				}
			}
			if (mIsFull) {
				mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_FULLSCREEN);
				WindowManager.LayoutParams windowAttributes = getWindow().getAttributes();
				windowAttributes.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | windowAttributes.flags;
			} else {
				mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
			setContentView(mView);
			notifyLocation();
			if (oldWinLayout != null) {
				oldWinLayout.release();
			}
		}
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
		LogUtil.logd("RecordWin2True show");
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
		LogUtil.logd("dialog_background_name::" + background);

		Drawable bgDraw = LayouUtil.getDrawable(background);
		if(mWinBgAlpha != null && bgDraw != null) {
			bgDraw.setAlpha((int) (mWinBgAlpha * 255));
		}

		if (mIfSetWinBg) {
			if(bgDraw != null) {
				getWindow().setBackgroundDrawable(bgDraw);
			}
		} else {
			if(mView != null && bgDraw != null) {
				mView.setBackgroundDrawable(bgDraw);
			}
		}

		if (mView == null) {
			ConfigUtil.checkViewRect(getWindow().getDecorView());
		}else {
			ConfigUtil.checkViewRect(mView);
		}
//		if (mWinBgAlpha != null) {
//			LayoutParams layoutParams = (LayoutParams) getWindow().getAttributes();
//			layoutParams.alpha = mWinBgAlpha;
//			getWindow().setAttributes(layoutParams);
//		}
//		WinLayoutManager.getInstance().updateState(0);
		super.show();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		resume();
	}

	// 复位
	private void resume() {
		// mWinLayout.reset();
	}

	@Override
	public void dismiss() {
		LogUtil.logd("RecordWin2True dismiss");
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
					RecordWin2True.super.dismiss();
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
		mView.setSystemUiVisibility(0);
        getWindow().getDecorView().setSystemUiVisibility(0);
	}

	@Override
	protected void onGetFocus() {
		GlobalObservableSupport.getWinRecordObserver().onGetFocus();
		if (mView != null) {
			if (mIsFull) {
				mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_FULLSCREEN | mSystemUiVisibility);
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_FULLSCREEN | mSystemUiVisibility);
			} else {
				mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}
		notifyLocation();
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND | WindowManager.LayoutParams.FLAG_DIM_BEHIND);
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

	public void setDialogCanceledOnTouchOutside(boolean mDialogCanceledOnTouchOutside) {
		RecordWin2True.mDialogCanceledOnTouchOutside = mDialogCanceledOnTouchOutside;
	}

	public void setAllowOutSideClickSentToBehind(boolean allowOutSideClickSentToBehind) {
		RecordWin2True.mAllowOutSideClickSentToBehind = allowOutSideClickSentToBehind;
	}

	public void updateBackground(Drawable drawable) {
		if (mIfSetWinBg || mView == null) {
			getWindow().setBackgroundDrawable(drawable);
		} else {
			mView.setBackgroundDrawable(drawable);
		}
	}
}
