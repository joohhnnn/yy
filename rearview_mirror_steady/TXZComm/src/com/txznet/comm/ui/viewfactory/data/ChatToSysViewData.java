package com.txznet.comm.ui.viewfactory.data;

public class ChatToSysViewData extends ViewData {

	public String textContent;

	public ChatToSysViewData() {
		super(TYPE_CHAT_TO_SYS);
	}

	public void setTextContent(String content) {
		this.textContent = content;
	}
}
