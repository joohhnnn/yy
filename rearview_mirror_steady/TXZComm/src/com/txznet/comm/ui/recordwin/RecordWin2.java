package com.txznet.comm.ui.recordwin;

import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.comm.util.ScreenUtils.ScreenSizeChangeListener;
import com.txznet.txz.util.runnables.Runnable1;

public class RecordWin2 {

	private static RecordWin2 sInstance = new RecordWin2();

	private IRecordWin2 mRecordWin2Impl;
	
	private boolean mScreenSizeChanged = false;
	
	private RecordWin2() {
	}
	
	public static RecordWin2 getInstance(){
		return sInstance;
	}
	
	
	private ScreenSizeChangeListener mScreenSizeChangeListener = new ScreenSizeChangeListener() {
		@Override
		public void onScreenSizeChange(int width, int height) {
			if (mRecordWin2Impl.isShowing()) {
				mScreenSizeChanged = true;
				return;
			}
			UI2Manager.runOnUIThread(new Runnable() {
				@Override
				public void run() {
					newWinInstance();					
				}
			}, 0);
		}
	};
	
	
	private void newWinInstance(){
		mRecordWin2Impl.newInstance();
		if (mRecordWin2Impl instanceof RecordWin2Impl2) {
			mRecordWin2Impl = RecordWin2Impl2.getInstance();
		}
		if (mRecordWin2Impl instanceof RecordWin2Impl3) {
			mRecordWin2Impl = RecordWin2Impl3.getInstance();
		}
	}
	
	public void init() {
		LogUtil.logd("init:" + ConfigUtil.getThemeWinRecordClassName());
		if (!TextUtils.isEmpty(ConfigUtil.getThemeWinRecordClassName())) {
			try {
				mRecordWin2Impl = (IRecordWin2) UIResLoader.getInstance()
						.getClassInstance(ConfigUtil.getThemeWinRecordClassName());
				mRecordWin2Impl.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init WinRecordImpl error!");
				mRecordWin2Impl = null;
			}
		}
		if (mRecordWin2Impl == null) {
			mRecordWin2Impl = RecordWin2Impl1.getInstance();
			mRecordWin2Impl.init();
		}
		if (mIsFullScreen != null) {
			mRecordWin2Impl.setIsFullSreenDialog(mIsFullScreen);
		}

		if (bCancelable != null) {
			mRecordWin2Impl.setDialogCancel(bCancelable);
		}

		if (bDialogCanceledOnTouchOutside != null) {
			mRecordWin2Impl.setDialogCanceledOnTouchOutside(bDialogCanceledOnTouchOutside);
		}
		
		if(mWinBgAlpha != null) {
			mRecordWin2Impl.setWinBgAlpha(mWinBgAlpha);
		}

		if (bAllowOutSideClickSentToBehind != null) {
			mRecordWin2Impl.setAllowOutSideClickSentToBehind(bAllowOutSideClickSentToBehind);
		}

		ScreenUtils.addSceenSizeChangeListener(mScreenSizeChangeListener);
	}
	
	public boolean isShowing(){
		return mRecordWin2Impl.isShowing();
	}

	private Boolean mIsFullScreen;
	
	public void setIsFullSreenDialog(boolean isFullScreen) {
		if (mRecordWin2Impl == null) {
			mIsFullScreen = isFullScreen;
			return;
		}
		mRecordWin2Impl.setIsFullSreenDialog(isFullScreen);
		if (mRecordWin2Impl instanceof RecordWin2Impl2) {// 设置全屏会new实例出来
			mRecordWin2Impl = RecordWin2Impl2.getInstance();
		}
	}
	
	
	private Float mWinBgAlpha = null;

	public void setWinBgAlpha(Float winBgAlpha) {
		if (mRecordWin2Impl == null) {
			mWinBgAlpha = winBgAlpha;
		} else {
			mRecordWin2Impl.setWinBgAlpha(winBgAlpha);
		}
	}
	
	public void updateWinLayout(IWinLayout winLayout) {
		mRecordWin2Impl.updateWinLayout(winLayout);
	}
	
	
	public void show() {
		mRecordWin2Impl.show();
	}

	public void dismiss() {
		mRecordWin2Impl.dismiss();
		if (mScreenSizeChanged) {
			UI2Manager.runOnUIThread(new Runnable() {
				@Override
				public void run() {
					if (!isShowing()) {
						newWinInstance();
						mScreenSizeChanged = false;
					}
				}
			}, 0);
		}
	}
	
	public void setWinRecordObserver(WinRecordCycleObserver observer) {
		mRecordWin2Impl.setWinRecordObserver(observer);
	}
	
	public void setWinImpl(IRecordWin2 impl) {
		LogUtil.logd("setWinImpl :" + impl);
		if(impl==null){
			return;
		}

		if (mRecordWin2Impl != null) {
			mRecordWin2Impl.newInstance();
		}

		mRecordWin2Impl = impl;
		if (mIsFullScreen != null) {
			mRecordWin2Impl.setIsFullSreenDialog(mIsFullScreen);
		}
		mRecordWin2Impl.init();
	}

	public void setWinImpl(int implType) {
		LogUtil.logd("setWinImpl :" + implType);
		switch (implType) {
		case 1:
			mRecordWin2Impl = RecordWin2Impl2.getInstance();
			break;
		case 2:
			mRecordWin2Impl = RecordWin2Impl1.getInstance();
			break;
		case 3:
			mRecordWin2Impl = RecordWin2Impl3.getInstance();
			break;
		default:
			mRecordWin2Impl = RecordWin2Impl1.getInstance();
			break;
		}
		mRecordWin2Impl.init();
		if (mIsFullScreen != null) {
			mRecordWin2Impl.setIsFullSreenDialog(mIsFullScreen);
		}
	}

	public void setWinType(int type) {
		if(Looper.myLooper()!=Looper.getMainLooper()){
			UI2Manager.runOnUIThread(new Runnable1<Integer>(type) {
				@Override
				public void run() {
					mRecordWin2Impl.setWinType(mP1);
				}
			},0);
			return;
		}
		mRecordWin2Impl.setWinType(type);
	}
	
	public void setSystemUiVisibility(int systemUiVisibility) {
		if(Looper.myLooper()!=Looper.getMainLooper()){
			UI2Manager.runOnUIThread(new Runnable1<Integer>(systemUiVisibility) {
				@Override
				public void run() {
					mRecordWin2Impl.setSystemUiVisibility(mP1);
				}
			},0);
			return;
		}
		mRecordWin2Impl.setSystemUiVisibility(systemUiVisibility);
	}

	public void setIfsetWinBg(boolean isSet) {
		mRecordWin2Impl.setIfSetWinBg(isSet);
	}
	
	public void setWinFlags(int flags){
		if(Looper.myLooper()!=Looper.getMainLooper()){
			UI2Manager.runOnUIThread(new Runnable1<Integer>(flags) {
				@Override
				public void run() {
					mRecordWin2Impl.setWinFlags(mP1);
				}
			},0);
			return;
		}
		mRecordWin2Impl.setWinFlags(flags);
	}
	public void setWinSoft(int soft){
		LogUtil.d("win2 setWinSoft :"+soft);
		if(Looper.myLooper()!=Looper.getMainLooper()){
			UI2Manager.runOnUIThread(new Runnable1<Integer>(soft) {
				@Override
				public void run() {
					mRecordWin2Impl.setWinSoft(mP1);
				}
			},0);
			return;
		}
		mRecordWin2Impl.setWinSoft(soft);
	}
	
	public void setWinContentWidth(int width){
		mRecordWin2Impl.setContentWidth(width);
	}
	
	public void updateDisplayArea(int x,int y,int width,int height){
		LogUtil.logd("updateDisplayArea:" + x + "," + y + "," + width + "," + height);
		if(mRecordWin2Impl != null) {
			mRecordWin2Impl.updateDisplayArea(x, y, width, height);
		}
	}
	


	private Boolean bCancelable;
	public void setCancelable(boolean flag){
		if (mRecordWin2Impl == null) {
			bCancelable = flag;
		} else {
			mRecordWin2Impl.setDialogCancel(flag);
		}
	}

	private Boolean bDialogCanceledOnTouchOutside;
	public void setCanceledOnTouchOutside(boolean cancel) {
		if (mRecordWin2Impl == null) {
			bDialogCanceledOnTouchOutside = cancel;
		} else {
			mRecordWin2Impl.setDialogCanceledOnTouchOutside(cancel);
		}
	}

	private Boolean bAllowOutSideClickSentToBehind;
	public void setAllowOutSideClickSentToBehind(boolean allow) {
		if (mRecordWin2Impl == null) {
			bAllowOutSideClickSentToBehind = allow;
		} else {
			mRecordWin2Impl.setAllowOutSideClickSentToBehind(allow);
		}
	}
	
		public void updateBackground(Drawable drawable){
		if(mRecordWin2Impl != null){
			mRecordWin2Impl.updateBackground(drawable);
		}
	}
}
