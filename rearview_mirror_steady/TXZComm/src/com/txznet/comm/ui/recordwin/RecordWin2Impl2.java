package com.txznet.comm.ui.recordwin;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.nostra13.universalimageloader.core.ImageLoader;
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

/**
 * 界面显示的窗口
 *
 */
public class RecordWin2Impl2 extends RecordWinBase implements IRecordWin2{

	private static RecordWin2Impl2 sInstance;
	
	
	private IWinLayout mWinLayout;
	
	private RecordWin2Impl2() {
		super();
		initRecordWin();
	}
	private RecordWin2Impl2(boolean fullScreen) {
		super(true,fullScreen);
		initRecordWin();
	}

	private void initRecordWin() {
		getWindow().setType(WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 3 );
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
				| WindowManager.LayoutParams.FLAG_LOCAL_FOCUS_MODE);
		if (mIsFull || ConfigUtil.isUseFullScreenFlag()) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
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
	
	public static RecordWin2Impl2 getInstance(){
		if(sInstance == null){
			synchronized (RecordWin2Impl2.class) {
				if(sInstance == null){
					sInstance = new RecordWin2Impl2();
				}
			}
		}
		return sInstance;
	}
	
	@Override
	public void setIsFullSreenDialog(boolean isFullScreen) {
		if (mIsFull == isFullScreen) {
			return;
		}
		LogUtil.logd("setIsFullScreenDialog:" + isFullScreen);
		sInstance = new RecordWin2Impl2(isFullScreen);
		sInstance.updateWinLayout(WinLayoutManager.getInstance().getLayout());
		sInstance.init();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		LogUtil.logd("onKeyDown:" + keyCode);
		return RecordWin2Manager.getInstance().onKeyEvent(keyCode);
	}
	
	
	@Override
	protected View createView(Object...objects) {
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
		return windowView;
	}
	
	private boolean isInit = false;
	
	private Float winBgAlpha = null;
	
	public void setWinBgAlpha(Float winBgAlpha) {
		if(winBgAlpha < 0.0f || winBgAlpha > 1.0f)
			return ;
		LogUtil.logi("setWinBgAlpha:" + winBgAlpha);
		this.winBgAlpha = winBgAlpha;
	}
	
	public void init(){
		LogUtil.logd("[UI2.0] init recordwin2");
		ConfigUtil.initScreenType(getWindow().getDecorView());
		ConfigUtil.checkViewRect(getWindow().getDecorView());
		if (mIsFull || ConfigUtil.isUseFullScreenFlag()) {
			getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		if(winBgAlpha != null) {
			LayoutParams layoutParams = (LayoutParams) getWindow().getAttributes();
			layoutParams.alpha = winBgAlpha;
			getWindow().setAttributes(layoutParams);
		}
		isInit = true;
	}
	
	public void updateWinLayout(IWinLayout winLayout) {
		LogUtil.logd("[UI2.0] update winLayout");
		if (winLayout != null && !winLayout.equals(mWinLayout)) {
			mWinLayout = winLayout;
			mView = mWinLayout.get();
			ViewParent viewParent = mView.getParent();
			if (viewParent!=null) {
				if (viewParent instanceof ViewGroup) {
					((ViewGroup)viewParent).removeView(mView);
				}
			}
			if (mIsFull) {
				mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_FULLSCREEN);
				WindowManager.LayoutParams windowAttributes = getWindow().getAttributes();
				windowAttributes.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | windowAttributes.flags;
			}else {
				mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
			setContentView(mView);
			if (mIsFull || ConfigUtil.isUseFullScreenFlag()) {
				getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			}
			getWindow().setBackgroundDrawable(LayouUtil.getDrawable("dialog_bg"));
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
		if (!isInit) {
			return;
		}
		if(!mRegisted){
			mRegisted = true;
			GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
			GlobalObservableSupport.getScrollObservable().registerObserver(mOnSizeObserver);
			if (mCycleObserver != null) {
				GlobalObservableSupport.getWinRecordObserver().registerObserver(mCycleObserver);
			}
		}
		if (!mHasScreenLock) {
			mScreenLock.lock();
			mHasScreenLock = true;
		}
		if (mView == null) {
			ConfigUtil.checkViewRect(getWindow().getDecorView());
		}else {
			ConfigUtil.checkViewRect(mView);
			mView.setBackgroundDrawable(LayouUtil.getDrawable("dialog_bg"));
		}
		super.show();
		resume();
	}
	
	// 复位
	private void resume(){
		// mWinLayout.reset();
	}
	
	@Override
	public void dismiss() {
		LogUtil.logd("RecordWin2 dismiss");
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
					RecordWin2Impl2.super.dismiss();
					release();
				}
			}, 0);
		} else {
			super.dismiss();
			release();
		}
	}
	
	@Override
	protected void onLoseFocus() {
		GlobalObservableSupport.getWinRecordObserver().onLoseFocus();
	}

	@Override
	protected void onGetFocus() {
		GlobalObservableSupport.getWinRecordObserver().onGetFocus();
		if (mView != null) {
			if (mIsFull) {
				mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_FULLSCREEN);
				WindowManager.LayoutParams windowAttributes = getWindow().getAttributes();
				windowAttributes.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | windowAttributes.flags;
			}else {
				mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
			}
		}
		if (mIsFull || ConfigUtil.isUseFullScreenFlag()) {
			getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
	}

	public void setWinRecordObserver(WinRecordCycleObserver observer) {
		this.mCycleObserver = observer;
	}
	
	private void release(){
		mWinLayout.reset();
		
		if (mRegisted) {
			GlobalObservableSupport.getWinRecordObserver().onDismiss();
			GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeObserver);
			GlobalObservableSupport.getScrollObservable().unregisterObserver(mOnSizeObserver);
			if (mCycleObserver != null) {
				GlobalObservableSupport.getWinRecordObserver().unregisterObserver(mCycleObserver);
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
	
	@Override
	public void setWinType(int type) {
		getWindow().setType(type);
	}
	
	@Override
	public void setWinFlags(int flags) {
		LayoutParams attrs = getWindow().getAttributes();
		attrs.flags = flags;
		getWindow().setAttributes(attrs);
	}
	
	@Override
	public void newInstance() {
		if (isShowing()) {
			LogUtil.loge("current win is showing,can't new instance!");
			return;
		}
		release();
		sInstance = new RecordWin2Impl2(mIsFull);
	}
	@Override
	public void setContentWidth(int width) {
		
	}

	@Override
	public void updateDisplayArea(int x, int y, int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setIfSetWinBg(boolean ifSet) {
		
	}
	@Override
	public void setDialogCancel(boolean flag) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setSystemUiVisibility(int type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDialogCanceledOnTouchOutside(boolean cancel) {

	}
	@Override
	public void setAllowOutSideClickSentToBehind(boolean allow) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void updateBackground(Drawable drawable) {
		mView.setBackgroundDrawable(drawable);
	}
	
		@Override
	public void setWinSoft(int soft) {
		// TODO Auto-generated method stub
		
	}
}
