package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

/**
 * Created by ASUS User on 2018/3/5.
 */

public class NoTtsQrcodeMsgData extends BaseMsgData {
    public String qrCode;
    public String title;
    public int key;
    public NoTtsQrcodeMsgData() {
        super(TYPE_FULL_NO_TTS_QRCORD);
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        key = jsData.getVal("key",Integer.class);
        JSONBuilder value = new JSONBuilder(jsData.getVal("value", String.class));
        title = value.getVal("title", String.class);
        qrCode = value.getVal("qrCode", String.class);
    }
}
