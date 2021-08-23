package com.txznet.music.service;

import com.txznet.music.Constant;

public interface ICommand {

    String TAG = Constant.LOG_TAG_CMD;

    byte[] invoke(String pkgName, String cmd, byte[] data);

    boolean intercept(String cmd);

    interface ICallback {
        byte[] invoke(String pkgName, String cmd, byte[] data);
    }
}
