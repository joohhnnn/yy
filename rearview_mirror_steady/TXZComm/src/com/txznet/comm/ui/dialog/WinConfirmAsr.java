package com.txznet.comm.ui.dialog;

import android.view.View;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * 等待处理处理，用户按返回关闭，按确定执行某操作
 * 
 * @author bihongpi
 *
 */
public abstract class WinConfirmAsr extends WinMessageBox {
	String mTtsHint;
	String[] mSureCmds;
	String[] mCancelCmds;

	Runnable mTtsEndRunnable;

	private Runnable mCancelLockTask;

	public WinConfirmAsr() {
		super();
		init();
	}

	public WinConfirmAsr(boolean isSystem) {
		super(isSystem);
		init();
	}

	private void init() {
		setLeftButton("确定");
		setRightButton("取消");
        m_focus = new View[2];
        m_focus[0] = mViewHolder.mLeft;
        m_focus[1] = mViewHolder.mRight;
		requestScreenLock();
		mCancelLockTask = new Runnable1<WinConfirmAsr>(this) {
			@Override
			public void run() {
				if (mP1 != null) {
					mP1.cancelScreenLock();
				}
			}
		};
	}

	public WinConfirmAsr setTitle(String s) {
		super.setTitle(s);
		return this;
	}

	public WinConfirmAsr setMessage(String s) {
		super.setMessage(s);
		return this;
	}

	public WinConfirmAsr setMessageData(Object data) {
		super.setMessageData(data);
		return this;
	}

	public WinConfirmAsr setCancelText(String s, String[] cmds) {
		mCancelCmds = cmds;
		super.setRightButton(s);
		return this;
	}

	public WinConfirmAsr setSureText(String s, String[] cmds) {
		mSureCmds = cmds;
		super.setLeftButton(s);
		return this;
	}

	public WinConfirmAsr setHintTts(String text) {
		mTtsHint = text;
		return this;
	}

	public WinConfirmAsr setTtsEndRunnable(Runnable runnable) {
		mTtsEndRunnable = runnable;
		return this;
	}

	/**
	 * 声控确定
	 */
	public void onSpeakOk() {
		onClickLeft();
	}

	/**
	 * 声控取消
	 */
	public void onSpeakCancel() {
		onClickRight();
	}

	/**
	 * 确定
	 */
	public abstract void onClickOk();

	/**
	 * 取消
	 */
	public void onClickCancel() {

	}

	@Override
	public void onClickLeft() {
		onClickOk();
		dismiss();
	}

	@Override
	public void onClickRight() {
		onClickCancel();
		dismiss();
	}

	@Override
	public void onClickBlank() {
		onClickRight();
	}

	@Override
	public void onBackPressed() {
		onClickCancel();
		dismiss();
	}
	
	@Override
	protected void onTimeout() {
		onClickRight();
	}

	boolean mFirstResume = true;

	@Override
	protected void onLoseFocus() {
		AsrUtil.recoverWakeupFromAsr(WinConfirmAsr.this.toString());
		super.onLoseFocus();
	}

	@Override
	protected void onGetFocus() {
		AsrUtil.cancel();

		AsrUtil.AsrConfirmCallback mWakeupAsrCallback = null;
		if (mSureCmds != null && mCancelCmds != null) {
			mWakeupAsrCallback = new AsrUtil.AsrConfirmCallback(mSureCmds, mCancelCmds) {
				@Override
				public String getTaskId() {
					return WinConfirmAsr.this.toString();
				}

				@Override
				public boolean needAsrState() {
					return true;
				}

				@Override
				public void onSure() {
					WinConfirmAsr.this.onSpeakOk();
				}

				@Override
				public void onCancel() {
					WinConfirmAsr.this.onSpeakCancel();
				}

				@Override
				public String needTts() {
					if (mFirstResume) {
						mFirstResume = false;
						return WinConfirmAsr.this.mTtsHint;
					}
					return null;
				}

				@Override
				public void onTtsEnd() {
					if(mTtsEndRunnable != null){
						mTtsEndRunnable.run();
					}
				}
				
				@Override
				public int getPriority() {
					return AsrUtil.WKASR_PRIORITY_NO_INSTANT_WK;
				}
			};
		}

		if (mWakeupAsrCallback != null) {
			AsrUtil.useWakeupAsAsr(mWakeupAsrCallback);
		}

		super.onGetFocus();
	}

	@Override
	public void show() {
		super.show();

		if (mTtsHint != null && mTtsHint.length() > 0) {
			// 1秒读2个字的语速
			// TODO 英文单词
			long delay = (mTtsHint.length() / 2) * 1000;
			if (delay < 1000 * 10) {
				delay = 1000 * 10;
			}
			AppLogicBase.runOnBackGround(mCancelLockTask, delay);
		}
	}

}
