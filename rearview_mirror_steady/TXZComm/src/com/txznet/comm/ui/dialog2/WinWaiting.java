package com.txznet.comm.ui.dialog2;

/**
 * 等待处理处理，用户按返回不可关闭
 * 
 * @author bihongpi
 *
 */
public abstract class WinWaiting extends WinProgress {
	/**
	 * 快速构造
	 * 
	 * @param txt
	 *            等待提示文本
	 */
	public WinWaiting(String txt) {
		super(
				(WinProgress.WinProgressBuildData) new WinProgress.WinProgressBuildData()
						.setMessageText(txt).setCancelable(false)
						.setCancelOutside(false));
	}

	/**
	 * 返回键处理
	 */
	@Override
	public void onBackPressed() {
		// do nothing
	}
}
