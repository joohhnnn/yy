package com.txznet.comm.ui.dialog2;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.util.TtsUtil.PreemptType;

import org.json.JSONObject;

/**
 * 通知确认窗，有确定按钮，只能确定执行某操作
 * 
 * @author bihongpi
 *
 */
public abstract class WinNotice extends WinMessageBox {
	/**
	 * 通知确认窗构建数据
	 * 
	 * @author pppi
	 *
	 */
	public static class WinNoticeBuildData extends WinMessageBoxBuildData {
		/**
		 * 确认按钮支持的识别指令
		 */
		String[] mSureCmds;

		@Override
		public void check() {
			// 设置默认确定文本
			if (this.mMidText == null) {
				this.setSureText(DEFAULT_TEXT_SURE);
			}
			// 不可取消
			this.setCancelable(false);
			// 外部点击不可取消
			this.setCancelOutside(false);
			super.check();
			this.addExtraInfo("mSureCmds", mSureCmds);
			this.addExtraInfo("dialogType", WinNotice.class.getSimpleName());
		}

		/**
		 * 设置消息文本，如果没有设置tts，tts将被设置为相同文本
		 * 
		 * @param text
		 *            消息文本
		 */
		@Override
		public WinNoticeBuildData setMessageText(String text) {
			return this.setMessageText(text, true);
		}

		/**
		 * 设置消息文本
		 * 
		 * @param text
		 *            消息文本
		 * @param tts
		 *            是否将tts设置为相同文本，只有tts文本没有设置，才生效
		 */
		public WinNoticeBuildData setMessageText(String text, boolean tts) {
			super.setMessageText(text);
			if (this.mHintTts == null && tts) {
				super.setHintTts(text);
			}
			return this;
		}

		/**
		 * 设置tts提示文本，如果没有设置显示的消息文本，将被设置为同一个
		 * 
		 * @param tts
		 *            语音提示文本
		 */
		@Override
		public WinNoticeBuildData setHintTts(String tts) {
			super.setHintTts(tts);
			if (this.mMessageText == null) {
				super.setMessageText(tts);
			}
			return this;
		}

		/**
		 * 设置tts提示文本，如果没有设置显示的消息文本，将被设置为同一个
		 * 
		 * @param tts
		 *            语音提示文本
		 * @param type
		 *            语音提示打断类型
		 */
		@Override
		public WinNoticeBuildData setHintTts(String tts, PreemptType type) {
			super.setHintTts(tts, type);
			if (this.mMessageText == null) {
				super.setMessageText(tts);
			}
			return this;
		}

		/**
		 * 设置确认按钮文本，默认语音识别文本与显示文本一致
		 * 
		 * @param text
		 *            按钮文本
		 * @return
		 */
		public WinNoticeBuildData setSureText(String text) {
			return this.setSureText(text, new String[] { text });
		}

		/**
		 * 设置确认按钮文本
		 * 
		 * @param text
		 *            按钮文本
		 * @param cmds
		 *            支持的识别指令
		 * @return
		 */
		public WinNoticeBuildData setSureText(String text, String[] cmds) {
			super.setMidText(text);
			this.mSureCmds = cmds;
			if (this.mSureCmds != null) {
				this.addAsrTask(new DialogAsrCallback() {
					@Override
					public void onSpeak(WinDialog win, String cmd) {
						((WinNotice) win).onSpeakOk();
						win.dismissInner();
					}

					@Override
					public String getReportId(WinDialog win) {
						return "ok";
					}
				}, this.mSureCmds);
			}
			return this;
		}
	};

	/**
	 * 快速构造
	 * 
	 * @param isSystem
	 *            是否为系统对话框
	 * @param msg
	 *            提示消息
	 */
	public WinNotice(boolean isSystem, String msg) {
		super((WinNoticeBuildData) new WinNoticeBuildData().setMessageText(msg)
				.setSystemDialog(isSystem));
	}

	/**
	 * 快速构造
	 * 
	 * @param msg
	 *            提示消息
	 */
	public WinNotice(String msg) {
		super((WinNoticeBuildData) new WinNoticeBuildData().setMessageText(msg));
	}

	/**
	 * 快速构造
	 * 
	 * @param title
	 *            对话框标题
	 * @param msg
	 *            提示消息
	 */
	public WinNotice(String title, String msg) {
		super((WinNoticeBuildData) new WinNoticeBuildData().setMessageText(msg)
				.setTitleText(title));
	}

	/**
	 * 快速构造
	 * 
	 * @param isSystem
	 *            是否为系统对话框
	 * @param title
	 *            对话框标题
	 * @param msg
	 *            提示消息
	 */
	public WinNotice(boolean isSystem, String title, String msg) {
		super((WinNoticeBuildData) new WinNoticeBuildData().setMessageText(msg)
				.setTitleText(title).setSystemDialog(isSystem));
	}

	public WinNotice(WinNoticeBuildData data) {
		this(data, true);
	}

	protected WinNotice(WinNoticeBuildData data, boolean init) {
		super(data, false);

		if (init) {
			initDialog();
		}
	}

	/**
	 * 初始化方控视图列表
	 */
	@Override
	protected void onInitFocusView() {
		List<FocusView> focusViews = new ArrayList<WinDialog.FocusView>();
		focusViews.add(new FocusView(mViewHolder.mMidButton, "BtnOk"));
		setFocusViews(focusViews, -1);
	}

	/**
	 * 语音识别到确定的回调
	 */
	public void onSpeakOk() {
		onClickOk();
	}

	/**
	 * 点击确定的执行回调
	 */
	public abstract void onClickOk();

	/**
	 * 点击中间按钮的执行回调
	 */
	@Override
	public void onClickMid() {
		onClickOk();
		dismissInner();
	}

	/**
	 * 倒计时点击按钮
	 * 
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	public void clickOkCountDown(int time) {
		super.clickMidCountDown(mWinMessageBoxBuildData.mMidText
				+ DEFAULT_COUNT_DOWN_SUFFIX, time);
	}

	/**
	 * 倒计时点击按钮
	 * 
	 * @param text
	 *            格式化文本
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	public void clickOkCountDown(String text, int time) {
		super.clickMidCountDown(text, time);
	}
}
