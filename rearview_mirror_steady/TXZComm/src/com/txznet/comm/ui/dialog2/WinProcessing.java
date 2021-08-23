package com.txznet.comm.ui.dialog2;

/**
 * 等待处理处理，用户按返回可关闭取消
 * 
 * @author bihongpi
 *
 */
public abstract class WinProcessing extends WinProgress {

	/**
	 * 快速构造
	 * 
	 * @param txt
	 *            处理中文本
	 */
	public WinProcessing(String txt) {
		super(txt);
	}

	/**
	 * 快速构造
	 * 
	 * @param txt
	 * @param isSystem
	 */
	public WinProcessing(String txt, boolean isSystem) {
		super(txt, isSystem);
	}

	/**
	 * 取消处理时回调
	 */
	public abstract void onCancelProcess();

	/**
	 * 返回键处理
	 */
	@Override
	public void onBackPressed() {
		this.onCancelProcess();
		this.dismissInner();
	}
}
