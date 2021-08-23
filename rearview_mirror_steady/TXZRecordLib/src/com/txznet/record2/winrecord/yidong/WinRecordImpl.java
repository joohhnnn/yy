package com.txznet.record2.winrecord.yidong;

import android.graphics.drawable.Drawable;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.layout.layout1.TXZWinLayout2;
import com.txznet.comm.ui.recordwin.IRecordWin2;
import com.txznet.comm.ui.UI2Manager;

/**
 * 移动界面impl，直接放在皮肤包中不知为何不能全屏。。。所以就添加了这个类<p>
 * 如果有特殊需求， 请直接改动，但需要保持类名不变并且implements IRecordWin2
 * 
 * 低内存实现类,但是每次show都会new一个出来,dismiss时回收
 * @author Terry
 */
public class WinRecordImpl implements IRecordWin2{

	private WinRecordTrue mWin2True;
	private static WinRecordImpl sInstance = new WinRecordImpl();
	
	private Object mWin2Lock = new Object();

	
	private Boolean mIsFullScreen = null;
	private Float mWinBgAlpha = null;
	private IWinLayout mWinLayout = null;
	private WinRecordCycleObserver mCycleObserver = null;
	private Integer mWinType = null;
	private Integer mWinFlags = null;
	private Integer mWinSoft = null;
	

	private WinRecordImpl(){
	}

	public static WinRecordImpl getInstance(){
		return sInstance;
	}
	
	private void log(String log) {
		LogUtil.logd("RecordWin2Impl1:" + log);
	}
	
	
	/**
	 * 创建并初始化
	 */
	private void createWinRecord() {
		if (mWinLayout != null) {
			mWin2True = new WinRecordTrue(mIsFullScreen == null ? false : mIsFullScreen, mWinLayout);
		} else {
			TXZWinLayout2.getInstance().init();
			IWinLayout winLayout = TXZWinLayout2.getInstance();
			mWin2True = new WinRecordTrue(mIsFullScreen == null ? false : mIsFullScreen, winLayout);
		}
		if (mCycleObserver != null) {
			mWin2True.setWinRecordObserver(mCycleObserver);
		}
		if (mWinBgAlpha != null) {
			mWin2True.setWinBgAlpha(mWinBgAlpha);
		}
		if (mWinType != null) {
			mWin2True.setWinType(mWinType);
		}
		if (mWinFlags != null) {
			mWin2True.setWinFlags(mWinFlags);
		}
	}

	/**
	 * 释放
	 */
	private void releaseWinRecord() {
		log("releaseWinRecord");
		if (mWin2True != null) {
			if (mWin2True.isShowing()) {
				mWin2True.dismiss();
			}
			mWin2True.release();
			WinLayoutManager.getInstance().releaseRecordView();
			mWin2True = null;
		}
		System.gc();
	}
	

	// 窗口消失5秒后进行释放
	private static final int DELAY_RELEASE = 5000;

	Runnable mTaskRelease = new Runnable() {
		@Override
		public void run() {
			if (mWin2True != null && !mWin2True.isShowing()) {
				releaseWinRecord();
			}
		}
	};
	
	
	public void init() {
		synchronized (mWin2Lock) {
			if (mWin2True == null) {
				createWinRecord();
			}
		}
		mWin2True.init();
		UI2Manager.removeUIThread(mTaskRelease);
		UI2Manager.runOnUIThread(mTaskRelease, DELAY_RELEASE);
	}
	
	public boolean isShowing() {
		if (mWin2True == null) {
			return false;
		}
		return mWin2True.isShowing();
	}

	public void setIsFullSreenDialog(boolean isFullScreen) {
	}

	public void setWinBgAlpha(Float winBgAlpha) {
		mWinBgAlpha = winBgAlpha;
		if (mWin2True != null) {
			mWin2True.setWinBgAlpha(mWinBgAlpha);
		}
	}
	
	public void updateWinLayout(IWinLayout winLayout) {
		mWinLayout = winLayout;
		if (mWin2True != null) {
			mWin2True.updateWinLayout(mWinLayout);
		}
	}
	
	
	public void show() {
		UI2Manager.removeUIThread(mTaskRelease);
		synchronized (mWin2Lock) {
			if (mWin2True == null) {
				createWinRecord();
			}
			WinLayoutManager.getInstance().addInnerRecordView();
			mWin2True.show();
		}
	}
	
	public void dismiss() {
		synchronized (mWin2Lock) {
			if (mWin2True != null) {
				mWin2True.dismiss();
				UI2Manager.removeUIThread(mTaskRelease);
				UI2Manager.runOnUIThread(mTaskRelease, DELAY_RELEASE);
			}
		}
	}

	public void setWinRecordObserver(WinRecordCycleObserver observer) {
		this.mCycleObserver = observer;
		if (mWin2True != null) {
			mWin2True.setWinRecordObserver(mCycleObserver);
		}
	}

	@Override
	public void setWinType(int type) {
	}


	@Override
	public void setWinFlags(int flags) {
	}


	@Override
	public void newInstance() {
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
	public void updateDisplayArea(final int x, final int y, final int width,
			final int height) {
	}

	@Override
	public void setDialogCancel(final boolean flag) {
		if (mWin2True != null) {
			mWin2True.setDialogCancel(flag);
		}
		WinRecordTrue.mDialogCancel = flag;
	}

	@Override
	public void setSystemUiVisibility(int type) {
		
	}

	@Override
	public void setDialogCanceledOnTouchOutside(boolean cancel) {
		if (mWin2True != null) {
			mWin2True.setDialogCanceledOnTouchOutside(cancel);
		}
		WinRecordTrue.mDialogCanceledOnTouchOutside = cancel;
	}

	@Override
	public void updateBackground(Drawable drawable) {

	}

	@Override
	public void setWinSoft(int soft) {
		this.mWinSoft = soft;
		if (mWin2True != null) {
			mWin2True.setWinType(mWinType);
		}
	}

	@Override
	public void setAllowOutSideClickSentToBehind(boolean allow) {
		if (mWin2True != null) {
			mWin2True.setAllowOutSideClickSentToBehind(allow);
		}
		WinRecordTrue.mAllowOutSideClickSentToBehind = allow;		
	}
}
