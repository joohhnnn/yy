package com.txznet.comm.ui.dialog;

import android.content.DialogInterface;
import android.view.View;

import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogicBase;

/**
 * 通知确认窗，有确定按钮，只能确定执行某操作
 * 
 * @author bihongpi
 *
 */
public abstract class WinNotice extends WinMessageBox {
	public WinNotice() {
		super();
		setMidButton("确定");
		setCancelable(false);
		m_focus = new View[1];
        m_focus[0] = mViewHolder.mMid;
	}

	public WinNotice(boolean isSystem) {
		super(isSystem);
		setMidButton("确定");
		setCancelable(false);
		m_focus = new View[1];
        m_focus[0] = mViewHolder.mMid;
	}

	public WinNotice setTitle(String s) {
		super.setTitle(s);
		return this;
	}

	public WinNotice setMessage(String s) {
		super.setMessage(s);
		return this;
	}

	public WinNotice setMessageData(Object data) {
		super.setMessageData(data);
		return this;
	}

	public WinNotice setSureText(String s) {
		super.setMidButton(s);
		return this;
	}

	/**
	 * 确定
	 */
	public abstract void onClickOk();

	@Override
	public void onClickMid() {
		dismiss();
		onClickOk();
	}

	public static void showNotice(final String hint, boolean tts,
			final boolean system, final Runnable r) {
		final int ttsId;
		if (tts)
			ttsId = TtsUtil.speakText(hint);
		else
			ttsId = TtsUtil.INVALID_TTS_TASK_ID;
		AppLogicBase.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				WinNotice win = new WinNotice(system) {
					@Override
					public void onClickOk() {
						if (r != null)
							r.run();
					}
				};
				win.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						TtsUtil.cancelSpeak(ttsId);
					}
				});
				win.setMessage(hint);
				win.show();
			}
		}, 0);
	}
}
