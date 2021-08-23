package com.txznet.record.bean;

public class BindDeviceMsg extends ChatMessage{
	public String qrCode;
	public String imageUrl;

	public BindDeviceMsg() {
		super(TYPE_FROM_SYS_BIND_SERVICE);
	}

}
