package com.txznet.record.ui;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.bean.ChatMessage;

/**
 * 低内存实现类
 * 
 * @author Terry
 *
 */
public class WinRecordImpl1 implements IWinRecord{

	private WinRecordImpl1True mWinTure;
	private Object mLock = new Object();

	private Boolean mIsFullScreen = null;
	private WinRecordCycleObserver mCycleObserver = null;
	private Boolean mEnableAnim = null;
	private Integer mDialogType = null;
	private Integer mWinType = null;
	private Integer mWinFlags = null;
	private static WinRecordImpl1 sInstance = new WinRecordImpl1();

	public static WinRecordImpl1 getInstance() {
		return sInstance;
	}
	

	private void createWinRecord() {
		mWinTure = new WinRecordImpl1True(mIsFullScreen == null ? false : mIsFullScreen);
		if (mEnableAnim != null) {
			mWinTure.enableAnim(mEnableAnim);
		}
		if (mCycleObserver != null) {
			mWinTure.setWinRecordObserver(mCycleObserver);
		}
		if (mDialogType != null) {
			mWinTure.updateDialogType(mDialogType);
		}
		if (mWinType != null) {
			mWinTure.setWinType(mWinType);
		}
		if (mWinFlags != null) {
			mWinTure.setWinFlags(mWinFlags);
		}
	}

	private void releaseWinRecord() {
		LogUtil.logd("WinRecord releaseWinRecord");
		if (mWinTure == null) {
			return;
		}
		if (mWinTure.isShowing()) {
			mWinTure.dismiss();
		}
		mWinTure.release();
		mWinTure = null;
	}

	// 窗口消失5秒后进行释放
	private static final int DELAY_RELEASE = 1000;

	Runnable mTaskRelease = new Runnable() {
		@Override
		public void run() {
			if (mWinTure != null && !mWinTure.isShowing()) {
				releaseWinRecord();
			}
		}
	};

	public boolean isShowing() {
		if (mWinTure == null) {
			return false;
		}
		return mWinTure.isShowing();
	}

	public void setIsFullSreenDialog(boolean isFullScreen) {
		mIsFullScreen = isFullScreen;
	}

	public void realInit() {
		synchronized (mLock) {
			if (mWinTure == null) {
				createWinRecord();
			}
			mWinTure.realInit();
			if (!mWinTure.isShowing()) {
				AppLogicBase.runOnUiGround(mTaskRelease, DELAY_RELEASE);
			}
		}
	}

	public void updateDialogType(int type){
		this.mDialogType = type;
	}
	
	public void addMsg(ChatMessage chatMsg) {
		if (!isShowing() || chatMsg == null) {
			return;
		}
		mWinTure.addMsg(chatMsg);
	}

	public void notifyUpdateVolume(int volume) {
		if (!isShowing()) {
			return;
		}
		mWinTure.notifyUpdateVolume(volume);
	}

	public void notifyUpdateLayout(int status) {
		if (!isShowing()) {
			return;
		}
		mWinTure.notifyUpdateLayout(status);
	}

	public void notifyUpdateProgress(int val, int selection) {
		if (!isShowing()) {
			return;
		}
		mWinTure.notifyUpdateProgress(val, selection);
	}

	public void enableAnim(boolean enable) {
		this.mEnableAnim = enable;
	}

	public void show() {
		AppLogicBase.removeUiGroundCallback(mTaskRelease);
		synchronized (mLock) {
			if (mWinTure == null) {
				createWinRecord();
			}
			mWinTure.show();
		}
	}

	public void dismiss() {
		synchronized (mLock) {
			if (mWinTure != null) {
				mWinTure.dismiss();
				AppLogicBase.removeUiGroundCallback(mTaskRelease);
				AppLogicBase.runOnUiGround(mTaskRelease, DELAY_RELEASE);
			}
		}
	}

	public void setWinRecordObserver(WinRecordCycleObserver observer) {
		this.mCycleObserver = observer;
		if (mWinTure != null) {
			mWinTure.setWinRecordObserver(mCycleObserver);
		}
	}

	@Override
	public void setWinFlags(int flags) {
		this.mWinFlags = flags;
		if (mWinTure != null) {
			mWinTure.setWinFlags(mWinFlags);
		}
	}

	@Override
	public void setWinType(int type) {
		this.mWinType = type;
		if (mWinTure != null) {
			mWinTure.setWinType(mWinType);;
		}
	}

	@Override
	public void newWinInstance() {
		if (isShowing()) {
			LogUtil.loge("current win is showing,can't new instance!");
			return;
		}
		releaseWinRecord();
	}

	@Override
	public void setContentWidth(int width) {
		
	}


	@Override
	public void setIfSetWinBg(boolean ifSet) {
		
	}

	@Override
	public void updateDisplayArea(int x, int y, int width, int height) {

	}

	@Override
	public void setBannerAdvertisingView(View view) {
		if (mWinTure != null) {
			mWinTure.setBannerAdvertisingView(view);
		}
	}

	@Override
	public void removeBannerAdvertisingView() {
		if (mWinTure != null) {
			mWinTure.removeBannerAdvertisingView();
		}
	}

	@Override
	public void setBackground(Drawable drawable) {
		if (mWinTure != null) {
			mWinTure.setBackground(drawable);
		}
	}

	@Override
	public void setWinBgAlpha(Float winBgAlpha) {
		mWinTure.setWinBgAlpha(winBgAlpha);
	}

	@Override
	public void setDialogCancel(boolean flag) {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public void setSystemUiVisibility(int type) {
	
	}

	@Override
	public void setDialogCanceledOnTouchOutside(boolean cancel) {

	}


	@Override
	public void setWinSoft(int soft) {
		
	}

	@Override
	public void setAllowOutSideClickSentToBehind(boolean allow) {

	}

    public void removeHelpView() {
        if (mWinTure != null) {
            mWinTure.removeHelpView();
        }
    }
}
