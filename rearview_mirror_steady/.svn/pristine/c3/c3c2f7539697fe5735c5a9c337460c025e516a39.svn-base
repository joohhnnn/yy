package com.txznet.record.ui;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.comm.util.ScreenUtils.ScreenSizeChangeListener;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.keyevent.KeyEventManagerUI1;

public class WinRecord {

	private static WinRecord sInstance = new WinRecord();

	private IWinRecord mWinRecordImpl;
	
	private boolean mScreenSizeChanged = false;

	private WinRecord() {
		mWinRecordImpl = WinRecordImpl2.getInstance();
		if (mObserver != null) {
			mWinRecordImpl.setWinRecordObserver(mObserver);
		}
		// mWinRecordImpl = new WinRecordImpl1();
	}

	public static WinRecord getInstance() {
		if (sInstance == null) {
			synchronized (WinRecord.class) {
				if (sInstance == null) {
					sInstance = new WinRecord();
				}
			}
		}
		return sInstance;
	}

	public boolean isShowing() {
		return mWinRecordImpl.isShowing();
	}

	public void setIsFullSreenDialog(boolean isFullScreen) {
		mWinRecordImpl.setIsFullSreenDialog(isFullScreen);
		if (mWinRecordImpl instanceof WinRecordImpl2) {
			mWinRecordImpl = WinRecordImpl2.getInstance();
			if (mObserver != null) {
				mWinRecordImpl.setWinRecordObserver(mObserver);
			}
		}
	}

	public void realInit() {
		mWinRecordImpl.realInit();
		ViewConfiger.getInstance().initRecordWin1ThemeConfig();
		ScreenUtils.addSceenSizeChangeListener(mScreenSizeChangeListener);
	}

	
	private ScreenSizeChangeListener mScreenSizeChangeListener = new ScreenSizeChangeListener() {
		@Override
		public void onScreenSizeChange(int width, int height) {
			LogUtil.logd("onScreenSizeChange width:" + width + ",height:" + height);
			if (mWinRecordImpl.isShowing()) {
				mScreenSizeChanged = true;
				return;
			}
			AppLogicBase.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					newWinInstance();		
				}
			},0);
			
		}
	};
	
	private void newWinInstance() {
		LogUtil.logd("newWinInstance");
		mWinRecordImpl.newWinInstance();
		if (mWinRecordImpl instanceof WinRecordImpl2) {
			mWinRecordImpl = WinRecordImpl2.getInstance();
			if (mObserver != null) {
				mWinRecordImpl.setWinRecordObserver(mObserver);
			}
		}
	}

	public void updateDialogType(int type) {
		mWinRecordImpl.updateDialogType(type);
	}

	public void addMsg(ChatMessage chatMsg) {
		mWinRecordImpl.addMsg(chatMsg);
	}

	public void notifyUpdateVolume(int volume) {
		mWinRecordImpl.notifyUpdateVolume(volume);
	}

	public void notifyUpdateLayout(String type, int status) {
		if (!"wheelControl".equals(type)) {
			mWinRecordImpl.notifyUpdateLayout(status);
		} else {
			LogUtil.logd("wheelControl :" + status);
			KeyEventManagerUI1.getInstance().onWheelControlStateChanged(status);
		}
	}

	public void notifyUpdateProgress(int val, int selection) {
		mWinRecordImpl.notifyUpdateProgress(val, selection);
	}

	public void enableAnim(boolean enable) {
		mWinRecordImpl.enableAnim(enable);
	}

	public void show() {
		mWinRecordImpl.show();
	}

	public void dismiss() {
		mWinRecordImpl.dismiss();
		if (mScreenSizeChanged) {
			AppLogicBase.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					if (!mWinRecordImpl.isShowing()) {
						newWinInstance();
						mScreenSizeChanged = false;
					}
				}
			}, 0);
		}
	}

	private static WinRecordCycleObserver mObserver = null;

	public void setWinRecordObserver(WinRecordCycleObserver observer) {
		LogUtil.logd("setWinRecordObserver:" + mWinRecordImpl + ",observer:"
				+ observer);
		if (mWinRecordImpl != null) {
			mWinRecordImpl.setWinRecordObserver(observer);
		} else {
			mObserver = observer;
		}
	}
	
	public void setWinFlags(int flags) {
		mWinRecordImpl.setWinFlags(flags);
	}
	
	public void setWinSoft(int soft) {
		mWinRecordImpl.setWinSoft(soft);
	}

	public void setWinBgAlpha(Float winBgAlpha) {
		mWinRecordImpl.setWinBgAlpha(winBgAlpha);
	}
	
	public void setWinType(int winType){
		mWinRecordImpl.setWinType(winType);
	}
	
	public void setSystemUiVisibility(int winType){
		mWinRecordImpl.setSystemUiVisibility(winType);
	}
	
	public void setWinImpl(int implType){
		switch (implType) {
		case 1:
			mWinRecordImpl = WinRecordImpl2.getInstance();
			break;
		case 2:
			mWinRecordImpl = WinRecordImpl1.getInstance();
			break;
		case 3:
			mWinRecordImpl = WinRecordImpl3.getInstance();
			break;
		default:
			mWinRecordImpl = WinRecordImpl2.getInstance();
			break;
		}
		if (mObserver != null) {
			mWinRecordImpl.setWinRecordObserver(mObserver);
		}
	}
	
	/**
	 * TODO 未考虑大小变化导致的分辨率兼容问题
	 */
	public void setContentWidth(final int width){
		AppLogicBase.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				mWinRecordImpl.setContentWidth(width);
				if (mWinRecordImpl instanceof WinRecordImpl2) {
					mWinRecordImpl = WinRecordImpl2.getInstance();
					if (mObserver != null) {
						mWinRecordImpl.setWinRecordObserver(mObserver);
					}
				}
			}
		}, 0);
	}
	/**
	 * 设置对话框是否是可撤销的
	 */
	public void setCancelable(boolean flag){
		LogUtil.logd("setCancelable::" + flag);
		mWinRecordImpl.setDialogCancel(flag);
	}
	
	public void setIfsetWinBg(boolean ifSet){
		mWinRecordImpl.setIfSetWinBg(ifSet);
	}

	public void updateDisplayArea(int x,int y,int width,int height){
		LogUtil.logd("updateDisplayArea:" + x + "," + y + "," + width + "," + height);
		mWinRecordImpl.updateDisplayArea(x, y, width, height);
	}

	public void setCanceledOnTouchOutside(boolean cancel) {
		LogUtil.logd("setCanceledOnTouchOutside::" + cancel);
		mWinRecordImpl.setDialogCanceledOnTouchOutside(cancel);
	}
	
	public void setAllowOutSideClickSentToBehind(boolean allow) {
		LogUtil.logd("setCanceledOnTouchOutside::" + allow);
		mWinRecordImpl.setAllowOutSideClickSentToBehind(allow);
	}
	
	public void setBannerAdvertisingView(View view){
		mWinRecordImpl.setBannerAdvertisingView(view);
	}

	public void removeBannerAdvertisingView(){
		mWinRecordImpl.removeBannerAdvertisingView();
	}

	public void setBackground(Drawable drawable){
		mWinRecordImpl.setBackground(drawable);
	}

	public void removeHelpView(){
		if (mWinRecordImpl instanceof WinRecordImpl2) {
			((WinRecordImpl2) mWinRecordImpl).removeHelpView();
		} else if (mWinRecordImpl instanceof WinRecordImpl1) {
			((WinRecordImpl1) mWinRecordImpl).removeHelpView();
		}
	}
}
