package com.txznet.comm.ui.dialog2;

import android.view.View;

/**
 * 信息提示窗，没有按钮，可以设置为点击空白处关闭
 * 
 * @author bihongpi
 *
 */
public abstract class WinInfo extends WinMessageBox {
	/**
	 * 对话框构建数据类
	 * 
	 * @author pppi
	 *
	 */
	public static class WinInfoBuildData extends WinMessageBoxBuildData {
		/**
		 * 快速构造
		 * 
		 * @param msg
		 *            提示消息
		 */
		public WinInfoBuildData(String msg) {
			super();
			super.setMessageText(msg);
		}

		/**
		 * 快速构造
		 * 
		 * @param title
		 *            标题
		 * @param msg
		 *            提示消息
		 */
		public WinInfoBuildData(String title, String msg) {
			super();
			super.setTitleText(title);
			super.setMessageText(msg);
		}

		@Override
		public void check() {
			super.check();
			this.addExtraInfo("dialogType", WinInfo.class.getSimpleName());
		}
	};

	/**
	 * 快速构建
	 * 
	 * @param title
	 *            标题
	 * @param msg
	 *            提示消息
	 */
	public WinInfo(String title, String msg) {
		this((WinInfoBuildData) new WinInfoBuildData(title, msg));
	}

	/**
	 * 快速构建
	 * 
	 * @param isSystem
	 *            是否为系统弹窗
	 * @param title
	 *            标题
	 * @param msg
	 *            提示消息
	 */
	public WinInfo(boolean isSystem, String title, String msg) {
		this((WinInfoBuildData) new WinInfoBuildData(title, msg)
				.setSystemDialog(isSystem));
	}

	/**
	 * 快速构建
	 * 
	 * @param msg
	 *            提示消息
	 */
	public WinInfo(String msg) {
		this((WinInfoBuildData) new WinInfoBuildData(msg));
	}

	/**
	 * 快速构建
	 * 
	 * @param isSystem
	 *            是否为系统弹窗
	 * @param msg
	 *            提示消息
	 */
	public WinInfo(boolean isSystem, String msg) {
		this((WinInfoBuildData) new WinInfoBuildData(msg)
				.setSystemDialog(isSystem));
	}

	/**
	 * 通过构建数据构造对话框
	 * 
	 * @param data
	 */
	public WinInfo(WinInfoBuildData data) {
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
	protected WinInfo(WinInfoBuildData data, boolean init) {
		super(data, false);

		if (init) {
			initDialog();
		}
	}

	/**
	 * 初始化需要方控控制的焦点列表
	 */
	@Override
	protected void onInitFocusView() {
	}

	/**
	 * 点击空白处回调
	 */
	@Override
	public void onClickBlank() {
		onBackPressed();
	}
}
