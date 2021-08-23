package com.txznet.comm.ui.dialog;

import android.view.View;

/**
 * 信息提示窗，没有按钮
 * 
 * @author bihongpi
 *
 */
public class WinInfo extends WinMessageBox {

	public WinInfo() {
		super();
		m_focus = new View[0];
	}
	
	public WinInfo(boolean isSystem){
		super(isSystem);
		m_focus = new View[0];
	}

	public WinInfo setTitle(String s) {
		super.setTitle(s);
		return this;
	}

	public WinInfo setMessage(String s) {
		super.setMessage(s);
		return this;
	}

	public WinInfo setMessageData(Object data) {
		super.setMessageData(data);
		return this;
	}
	
	@Override
	public void onClickBlank() {
		onBackPressed();
	}
}
