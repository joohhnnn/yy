package com.txznet.comm.ui.theme.test.keyevent;

import java.util.List;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.keyevent.KeyEventDispatcherBase;
import com.txznet.comm.ui.keyevent.KeyEventManager;
import com.txznet.comm.ui.keyevent.KeyEventManager.FocusSupportListener;
import com.txznet.comm.ui.recordwin.RecordWin2;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.view.IListView;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;

import android.os.SystemClock;
import android.view.View;


public class KeyEventDispatcher extends KeyEventDispatcherBase{

	private List<View> mFiexdFocusViews; // 固定的需要获取焦点的View,例如返回、帮助等。
	private List<View> mChatFocusViews; // 聊天内容中需要获取焦点的View，例如POI列表等
	private ViewBase mCurChatView; // 当前正在显示的聊天内容
	private ViewBase mLastChatView; // 刚刚显示的聊天内容
	private int mCurChatFocusIndex = -1;
	private boolean mSupportKeyEvent = false; // 默认不支持按键操控
	private int mLastPageKey = KEYCODE_DOWN; // 翻页时所按的键，当按上键翻页时默认焦点是最后一个，按下键翻页时默认焦点是第一个
	
	private static KeyEventDispatcher sInstance = new KeyEventDispatcher();
	private Object mViewOperateLock = new Object();
	private KeyEventDispatcher() {
		KeyEventManager.getInstance().addFocusSupportListener(new FocusSupportListener() {
			
			@Override
			public void onStateChanged(boolean support) {
				LogUtil.logd("[UI2.0] onStateChanged:" + support);
				if (mSupportKeyEvent != support) {
					mSupportKeyEvent = support;
					synchronized (mViewOperateLock) {
						if (mChatFocusViews == null || mChatFocusViews.size() == 0) {
							return;
						}
						UI2Manager.runOnUIThread(new Runnable() {
							@Override
							public void run() {
								if (mSupportKeyEvent) {
									// if (mCurChatFocusIndex != -1) {
									// View lastFocusView =
									// mChatFocusViews.get(mCurChatFocusIndex);
									// lastFocusView.getOnFocusChangeListener().onFocusChange(lastFocusView,
									// false);
									// }
									// View view = mChatFocusViews.get(0);
									// view.getOnFocusChangeListener().onFocusChange(view,
									// true);
									mCurChatFocusIndex = 0;
									updateFocus(mCurChatFocusIndex);
								} else {
									if (mCurChatFocusIndex != -1) {
										// View lastFocusView =
										// mChatFocusViews.get(mCurChatFocusIndex);
										// lastFocusView.getOnFocusChangeListener().onFocusChange(lastFocusView,
										// false);
										mCurChatFocusIndex = -1;
										updateFocus(mCurChatFocusIndex);
									}
								}
							}
						}, 0);
					}
				}
			}
		});
	}
	
	public static KeyEventDispatcher getInstance(){
		return sInstance;
	}
	
	@Override
	public int getCurMode() {
		return 0;
	}

	public int getFocusIndex() {
		if (isTurningPage() && mLastPageKey == KEYCODE_UP && mIsTurningPre) {
			return mChatFocusViews.size() - 1;
		}
		return 0;
	}
	
	/**
	 * 是否是翻页导致的内容改变
	 */
	public boolean isTurningPage() {
		LogUtil.logd("isTurningPage:" + mIsTurningPage);
		if (mIsTurningPage && mLastChatView != null && mCurChatView != null && mLastChatView instanceof IListView
				&& mCurChatView instanceof IListView) {
			if (mLastChatView.getClass().getName().equals(mCurChatView.getClass().getName())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 聊天内容发生改变时,窗口dismiss时会传一个null过来
	 */
	@Override
	public void onChatViewChange(ViewBase viewBase) {
		synchronized (mViewOperateLock) {
			UI2Manager.removeBackGroundCallback(tastResetClear);
			mCurChatFocusIndex = -1;
			mLastChatView = mCurChatView;
			mCurChatView = viewBase;
			if (viewBase != null && viewBase instanceof IListView) {
				mChatFocusViews = viewBase.getFocusViews();
				if (!mSupportKeyEvent) {
					return;
				}
				if (mChatFocusViews != null && mChatFocusViews.size() > 0) {
					UI2Manager.runOnUIThread(new Runnable() {
						@Override
						public void run() {
							if (mChatFocusViews != null && mChatFocusViews.size() > 0) {
								if (mCurChatFocusIndex != -1) {
									View lastFocusView = mChatFocusViews.get(mCurChatFocusIndex);
									if(lastFocusView != null && lastFocusView.getOnFocusChangeListener() != null){
										lastFocusView.getOnFocusChangeListener().onFocusChange(lastFocusView, false);
									}
								}
								int focus = getFocusIndex();
								View view = mChatFocusViews.get(focus);
								view.setFocusable(true);
								view.setFocusableInTouchMode(true);
								view.requestFocus();
								mCurChatFocusIndex = focus;
							}
						}
					}, 0);
				}
			} else {
				mChatFocusViews = null;
				mIsTurningPage = false;
				mIsTurningPre = false;
				mIsTurningNext = false;
			}
		}
	}

	private static final int TIME_PROGRESS_PROTECT = 500;
	private boolean isJustClearProgress = false;
	private Runnable tastResetClear = new Runnable() {
		@Override
		public void run() {
			isJustClearProgress = false;
		}
	};
	
	@Override
	public void onUpdateProgress(int selection, int value) {
		if (selection == mCurChatFocusIndex && value != 0) {
			if (mCurChatFocusIndex == 0 && !isJustClearProgress) {// 目前只会走第一个默认的进度
				// clearAllFocus();
			}
		}
		super.onUpdateProgress(selection, value);
	}
	
	private void clearProgress() {
		RecordWin2Manager.getInstance().sendEventToCore(RecordWin2Manager.EVENT_EVENT_CLEAR_PROGRESS);
		UI2Manager.removeBackGroundCallback(tastResetClear);
		isJustClearProgress = true;
		UI2Manager.runOnUIThread(tastResetClear, TIME_PROGRESS_PROTECT);
	}
	
	private boolean mIsTurningPage = false;
	private boolean mIsTurningPre = false;
	private boolean mIsTurningNext = false;
	
	Runnable mTaskResetTurning = new Runnable() {
		@Override
		public void run() {
			mIsTurningPage = false;
			mIsTurningNext = false;
			mIsTurningPre = false;
		}
	};

	private void tryPageByKeyEvent(boolean isUp) {
		mIsTurningPage = true;
		if (isUp) {
			mIsTurningPre = true;
		} else {
			mIsTurningNext = true;
		}
		UI2Manager.removeBackGroundCallback(mTaskResetTurning);
		UI2Manager.runOnBackGround(mTaskResetTurning, 500);
	}
	
	/**
	 * 收到按键事件时
	 */
	@Override
	public boolean onKeyEvent(int keyEvent) {
		LogUtil.logd("[UI2.0] onKeyEvent:" + keyEvent);
		mSupportKeyEvent = true;
		mLastPageKey = KEYCODE_DOWN;
		if (mChatFocusViews == null || mChatFocusViews.size() == 0) {
			LogUtil.logd("[UI2.0] onKeyEvent mChatFocusViews null");
			return false;
		}
		if (mCurChatFocusIndex > mChatFocusViews.size()) {
			mCurChatFocusIndex = 0;
		}
		switch (keyEvent) {
		case KEYCODE_LEFT:
			break;
		case KEYCODE_RIGHT:
			break;
		case KEYCODE_UP:
			if (mCurChatFocusIndex > 0) {
				mCurChatFocusIndex--;
			} else if (mCurChatFocusIndex == 0) {
				mLastPageKey = KEYCODE_UP;
				tryPageByKeyEvent(true);
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_LIST_PREPAGE, 0, 0);
			} else if (mCurChatFocusIndex == -1) {
				mCurChatFocusIndex = mChatFocusViews.size() - 1;
			}
			clearProgress();
			updateFocus(mCurChatFocusIndex);
			return true;
		case KEYCODE_DOWN:
			if (mCurChatFocusIndex < mChatFocusViews.size() - 1) {
				mCurChatFocusIndex++;
			} else if (mCurChatFocusIndex == mChatFocusViews.size() - 1) {
				mLastPageKey = KEYCODE_DOWN;
				tryPageByKeyEvent(false);
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_LIST_NEXTPAGE, 0, 0);
			} else if (mCurChatFocusIndex == -1) {
				mCurChatFocusIndex = 0;
			}
			clearProgress();
			updateFocus(mCurChatFocusIndex);
			return true;
		case KEYCODE_OK:
			if (mCurChatFocusIndex != -1) {
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_LIST_ITEM, 0, mCurChatFocusIndex);
			}
			return true;
		case KEYCODE_VOICE:
			return false;
		default:
			break;
		}
		return false;
	}

	private void clearAllFocus(){
		if (mChatFocusViews == null) {
			return;
		}
		for (int i = 0; i < mChatFocusViews.size(); i++) {
				mChatFocusViews.get(i).setFocusable(false);
				mChatFocusViews.get(i).setFocusableInTouchMode(false);
		}
	}
	
	private void updateFocus(int position) {
		LogUtil.logd("[UI2.0] onKeyEvent updateFocus:" + position);
		if (mChatFocusViews == null || mChatFocusViews.size() < position) {
			return;
		}
		for (int i = 0; i < mChatFocusViews.size(); i++) {
			if (i != position) {
				mChatFocusViews.get(i).setFocusable(false);
				mChatFocusViews.get(i).setFocusableInTouchMode(false);
			} else {
				mChatFocusViews.get(i).setFocusable(true);
				mChatFocusViews.get(i).setFocusableInTouchMode(true);
			}
		}
		if (position >= 0 && position < mChatFocusViews.size()) {
			mChatFocusViews.get(position).requestFocus();
		}
	}
	
}
