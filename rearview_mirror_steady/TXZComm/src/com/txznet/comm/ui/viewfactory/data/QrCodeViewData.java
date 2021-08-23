package com.txznet.comm.ui.viewfactory.data;

public class QrCodeViewData extends ViewData {
	public String qrCode;
	
	public QrCodeViewData() {
		super(TYPE_QRCODE);
	}
	
	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}
	
}
