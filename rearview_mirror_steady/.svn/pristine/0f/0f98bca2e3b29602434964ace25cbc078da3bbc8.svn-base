package com.txznet.comm.ui.dialog2;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.version.TXZVersion;

/**
 * 等待处理处理，用户按返回关闭，按确定执行某操作
 * 
 * @author pppi
 *
 */
public abstract class WinConfirmAsr extends WinConfirm {
	/**
	 * 对话框构建数据类型
	 * 
	 * @author pppi
	 *
	 */
	public static class WinConfirmAsrBuildData extends WinConfirmBuildData {
		String[] mSureCmds;
		String[] mCancelCmds;

		@Override
		public void check() {
			super.check();
			this.addExtraInfo("mSureCmds", mSureCmds);
			this.addExtraInfo("mCancelCmds", mCancelCmds);
			this.addExtraInfo("dialogType", WinConfirmAsr.class.getSimpleName());
		}

		/**
		 * 设置消息文本，如果没有设置tts，tts将被设置为相同文本
		 * 
		 * @param text
		 *            消息文本
		 */
		@Override
		public WinConfirmAsrBuildData setMessageText(String text) {
			super.setMessageText(text);
			if (this.mHintTts == null) {
				super.setHintTts(text);
			}
			return this;
		}

		/**
		 * 设置消息文本
		 * 
		 * @param text
		 *            消息文本
		 * @param tts
		 *            是否将tts设置为相同文本，只有tts文本没有设置，才生效
		 */
		public WinConfirmAsrBuildData setMessageText(String text, boolean tts) {
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
		public WinConfirmAsrBuildData setHintTts(String tts) {
			super.setHintTts(tts);
			if (this.mMessageText == null) {
				super.setMessageText(mMessageText);
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
		public WinConfirmAsrBuildData setHintTts(String tts, PreemptType type) {
			super.setHintTts(tts, type);
			if (this.mMessageText == null) {
				super.setMessageText(mMessageText);
			}
			return this;
		}

		/**
		 * 设置取消按钮文本，默认识别指令与之相同
		 * 
		 * @param text
		 *            按钮文本
		 * @return
		 */
		public WinConfirmAsrBuildData setCancelText(String text) {
			this.setCancelText(text,
					mCancelCmds == null ? new String[] { text } : mCancelCmds);
			return this;
		}

		/**
		 * 设置取消按钮文本
		 * 
		 * @param text
		 *            按钮文本
		 * @param cmds
		 *            识别的指令，设置为null则不需要支持声控
		 * @return
		 */
		public WinConfirmAsrBuildData setCancelText(String text, String[] cmds) {
			super.setCancelText(text);
			this.mCancelCmds = cmds;
			if (this.mCancelCmds != null) {
				this.addAsrTask(new DialogAsrCallback() {
					@Override
					public void onSpeak(WinDialog win, String cmd) {
						((WinConfirmAsr) win).onSpeakCancel();
						win.dismissInner();
					}

					@Override
					public String getReportId(WinDialog win) {
						return "cancel";
					}
				}, this.mCancelCmds);
			}
			return this;
		}

		/**
		 * 设置确定按钮文本，默认识别指令与之相同
		 * 
		 * @param text
		 *            按钮文本
		 * @return
		 */
		public WinConfirmAsrBuildData setSureText(String text) {
			this.setSureText(text, mSureCmds == null ? new String[] { text }
					: mSureCmds);
			return this;
		}

		/**
		 * 设置确定按钮文本
		 * 
		 * @param text
		 *            按钮文本
		 * @param cmds
		 *            识别的指令，设置为null则不需要支持声控
		 * @return
		 */
		public WinConfirmAsrBuildData setSureText(String text, String[] cmds) {
			super.setSureText(text);
			this.mSureCmds = cmds;
			if (this.mSureCmds != null) {
				this.addAsrTask(new DialogAsrCallback() {
					@Override
					public void onSpeak(WinDialog win, String cmd) {
						((WinConfirmAsr) win).onSpeakOk();
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
	 * 通过构建数据构造对话框
	 * 
	 * @param data
	 */
	public WinConfirmAsr(WinConfirmAsrBuildData data) {
		this(data, true);
	}

	/**
	 * 通过构造数据构造对话框，用于给派生类构造，构造时先不初始化
	 * 
	 * @param data
	 *            构建数据
	 * @param init
	 *            是否初始化，自己构造时传true，派生类构造时传false
	 */
	protected WinConfirmAsr(WinConfirmAsrBuildData data, boolean init) {
		super(data, false);

		if (init) {
			initDialog();
		}
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
}
