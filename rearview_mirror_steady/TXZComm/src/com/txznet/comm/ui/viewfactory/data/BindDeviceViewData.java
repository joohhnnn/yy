package com.txznet.comm.ui.viewfactory.data;

public class BindDeviceViewData extends ViewData {
    public String qrCode;
    public String imageUrl;
    public BindDeviceViewData() {
        super(ViewData.TYPE_CHAT_BIND_DEVICE_QRCODE);
    }
}
