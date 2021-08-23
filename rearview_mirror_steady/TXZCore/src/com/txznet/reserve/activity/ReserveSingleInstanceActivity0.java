package com.txznet.reserve.activity;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable.HomeObserver;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.recordwin.RecordWin2Impl3;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.ScreenLock;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

/**
 * UI2.0 语音界面
 * 
 * @author Terry
 *
 */
public class ReserveSingleInstanceActivity0 extends BaseActivity {
	
	private static ReserveSingleInstanceActivity0 sInstance;
	
	private IWinLayout mWinLayout;
	private int sessionId = 0;
	// 是否锁定了屏幕锁
	private boolean mHasScreenLock;
	protected ScreenLock mScreenLock;
	private static boolean sInited = false;
	
	private View mView;
	
	private static HomeObserver mHomeObserver = new HomeObserver() {
		@Override
		public void onHomePressed() {
			dismiss();
		}
	};
	
	
	private static BroadcastReceiver mWebchatReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtil.logd("onReceive webchat show");
			dismiss();
		}
	};
	
	// 当前是否正在显示，主要是为了防止异常情况下activity还没建立就调了dismiss接口
	private static  boolean mIsShow = false;
	private static Integer sIntentFlags = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sessionId = getIntent().getIntExtra("sessionId", -1);
		LogUtil.logd("onCreate :" + mIsShow + this.hashCode() + ",sessionId:" + sessionId + ",sCurrentSessionId:"
				+ sCurrentSessionId);
		if (sessionId != sCurrentSessionId) {
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		WinLayoutManager.getInstance().addInnerRecordView();
		WinLayoutManager.getInstance().updateState(0);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setTheme(R.style.AppTransparentTheme);
		sInstance = this;
		mWinLayout = WinLayoutManager.getInstance().getLayout();
		mView = mWinLayout.get();
		ViewParent viewParent = mView.getParent();
		if (viewParent!=null) {
			if (viewParent instanceof ViewGroup) {
				((ViewGroup)viewParent).removeView(mView);
			}
		}
		mView.setBackgroundDrawable(LayouUtil.getDrawable("dialog_bg"));
		if (!sInited) {
			ConfigUtil.initScreenType(getWindow().getDecorView());
			ConfigUtil.checkViewRect(mView);
			sInited = true;
		}
		setContentView(mView);
		if (RecordWin2Impl3.getInstance().mWinBgAlpha != null) {
			LayoutParams layoutParams = (LayoutParams) getWindow().getAttributes();
			layoutParams.alpha = RecordWin2Impl3.getInstance().mWinBgAlpha;
			getWindow().setAttributes(layoutParams);
		}
	}
	
	@Override
	protected void onResume() {
		LogUtil.logd("onResume " + this.hashCode());
		if (!mHasScreenLock) {
			if(mScreenLock==null){
				mScreenLock = new ScreenLock(this);
			}
			mScreenLock.lock();
			mHasScreenLock = true;
		}
		if (mView != null) {
			ConfigUtil.checkViewRect(mView);
		}
		sendBroadcast(new Intent("com.txznet.txz.action.FLOAT_WIN_SHOW"));
		super.onResume();
	}


	@Override
	public void onLoseFocus() {
		LogUtil.logd("onLoseFocus " + this.hashCode());
		super.onLoseFocus();
		GlobalObservableSupport.getWinRecordObserver().onLoseFocus();
	}

	@Override
	public void onGetFocus() {
		super.onGetFocus();
		GlobalObservableSupport.getWinRecordObserver().onGetFocus();
	}

	@Override
	protected void onStop() {
		LogUtil.logd("onStop " + this.hashCode());
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		LogUtil.logd(
				"onDestroy " + this.hashCode() + ",sessionId:" + sessionId + ",sCurrentSessionId:" + sCurrentSessionId);
		if (sCurrentSessionId == sessionId) {
			mHasScreenLock = false;
			if (mScreenLock != null) {
				mScreenLock.release();
			}
			sendBroadcast(new Intent("com.txznet.txz.action.FLOAT_WIN_DISMISS"));
			AsrUtil.closeRecordWinLock();
			if (mWinLayout != null) {
				mWinLayout.release();
			}
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.dismiss", null, null);
			setContentView(new TextView(this));
			if (ImageLoader.getInstance().isInited()) {
				ImageLoader.getInstance().clearDiskCache();
				ImageLoader.getInstance().clearMemoryCache();
			}
			WinLayoutManager.getInstance().releaseRecordView();
			LayouUtil.release();
			sInstance = null;
			System.gc();
		}
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			dismiss();
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private static boolean sRegistered =false;
	public static void show() {
		LogUtil.logd("show");
		if (!sRegistered) {
			IntentFilter intentFilter = new IntentFilter("com.txznet.webchat.action.ACTIVITY_FOREGROUND");
			GlobalContext.get().registerReceiver(mWebchatReceiver, intentFilter);
			sRegistered = true;
			GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
			if (RecordWin2Impl3.getInstance().mWinRecordCycleObserver != null) {
				try {
					GlobalObservableSupport.getWinRecordObserver().registerObserver(RecordWin2Impl3.getInstance().mWinRecordCycleObserver);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (Looper.myLooper() != Looper.getMainLooper()) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					showInner();
				}
			});
			return;
		}
		showInner();
	}

	public static void dismiss() {
		LogUtil.logd("dismiss");
		if (Looper.myLooper() != Looper.getMainLooper()) {
			AppLogic.runOnUiGround(new Runnable() {
				
				@Override
				public void run() {
					dismissInner();
				}
			});
			return;
		}
		dismissInner();
	}

	private static void dismissInner() {
		mIsShow = false;
		if (sInstance != null) {
			sInstance.finish();
		} else {
			// 可能还未显示
			++sCurrentSessionId;
			LogUtil.logd("sCurrentSessionId:" + sCurrentSessionId);
			WinLayoutManager.getInstance().releaseRecordView();
			LayouUtil.release();
			WinLayoutManager.getInstance().getLayout().release();
			System.gc();
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.dismiss", null, null);
	}

	public static void setIntentFlags(int flags){
		sIntentFlags = flags;
	}

	private static void showInner() {
		mIsShow = true;
		Intent intent = new Intent(GlobalContext.get(), ReserveSingleInstanceActivity0.class);
		if (sIntentFlags != null) {
			intent.setFlags(sIntentFlags);
		} else {
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		int id = ++sCurrentSessionId;
		LogUtil.logd("id:" + id + ",sCurrentSessionId:" + sCurrentSessionId);
		intent.putExtra("sessionId", id);
		// HOME键后5s内通过application startActivity会有延迟
		PendingIntent pendingIntent =
                PendingIntent.getActivity(GlobalContext.get(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
	}
	
	private static int sCurrentSessionId = 0;
	@Override
	public void onBackPressed() {
		dismiss();
		super.onBackPressed();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// No call for super(), Fix Bug TXZ-13844
//		super.onSaveInstanceState(outState);
	}
}
