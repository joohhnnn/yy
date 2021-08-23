package com.txznet.comm.ui.viewfactory.data;

public class LogoQrCodeViewData extends ViewData {
    public String qrCode;

    public LogoQrCodeViewData() {
        super(ViewData.TYPE_CHAT_LOGO_QRCODE);
    }

    public void setQrCode(String qrCode){
        this.qrCode = qrCode;
    }
}
