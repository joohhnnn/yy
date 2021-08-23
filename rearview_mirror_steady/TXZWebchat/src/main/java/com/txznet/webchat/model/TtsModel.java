package com.txznet.webchat.model;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.webchat.comm.plugin.model.WxMessage;

/**
 * Tts播报Model
 * Created by J on 2016/11/30.
 */

public class TtsModel {
    public String resId;
    public String[] resArgs;
    public String text;
    public boolean force;
    public TtsUtil.ITtsCallback callback;
    public WxMessage message;

    @Override
    public String toString() {
        return "TtsModel{" +
                "resId = " + resId +
                "text ='" + text + '\'' +
                ", force =" + force +
                ", callback =" + callback +
                "WxMessage = " + message +
                "}";
    }
}
