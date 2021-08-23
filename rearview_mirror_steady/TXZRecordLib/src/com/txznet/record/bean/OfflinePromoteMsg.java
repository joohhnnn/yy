package com.txznet.record.bean;

public class OfflinePromoteMsg extends  ChatMessage{
    public String qrCode;
    public String text;
    public OfflinePromoteMsg() {
        super(TYPE_CHAT_OFFLINE_PROMOTE);
    }
}
