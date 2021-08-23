package com.txznet.comm.ui.dialog;

/**
 * 等待处理处理，用户按返回可关闭取消
 * 
 * @author bihongpi
 *
 */
public abstract class WinProcessing extends WinProgress {
	
	public WinProcessing(String txt) {
		super(txt);
	}
	
	public WinProcessing(String txt, boolean isSystem){
		super(txt, isSystem);
	}

	public abstract void onCancelProcess();
	
	@Override
	public void onBackPressed() {
		onCancelProcess();
		dismiss();
	}
	
}
