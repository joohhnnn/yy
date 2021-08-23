package com.txznet.comm.ui.dialog;

/**
 * 等待处理处理，用户按返回不可关闭
 * 
 * @author bihongpi
 *
 */
public class WinWaiting extends WinProgress {
	
	public WinWaiting(String txt) {
		super(txt);
		setCancelable(false);
	}
	
	public WinWaiting setMessageData(Object data) {
		super.setMessageData(data);
		return this;
	}
	
	@Override
	public void onBackPressed() {
		// do nothing
	}
}
