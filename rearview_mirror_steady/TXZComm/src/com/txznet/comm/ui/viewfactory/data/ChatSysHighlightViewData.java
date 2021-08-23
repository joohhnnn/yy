package com.txznet.comm.ui.viewfactory.data;

import android.view.View;

public class ChatSysHighlightViewData extends ViewData{
	public String textContent;

	public ChatSysHighlightViewData() {
		super(TYPE_CHAT_FROM_SYS_HL);
	}

	public void setTextContent(String content) {
		this.textContent = content;
	}
	
	public View.OnClickListener onClickListener;
	
	public void setOnClickListener(View.OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}
	
}
