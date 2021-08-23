package com.txznet.music.service.impl;

import android.util.ArrayMap;

import com.txznet.music.service.ICommand;

import java.util.Map;

abstract class BaseCommand implements ICommand {

    protected final Map<String, ICallback> mSupportCmd = new ArrayMap<>();

    public void addCmd(String cmd, ICallback callback) {
        mSupportCmd.put(cmd, callback);
    }

    public void addCmd(String[] cmds, ICallback callback) {
        for (String cmd : cmds) {
            mSupportCmd.put(cmd, callback);
        }
    }

    @Override
    public byte[] invoke(String pkgName, String cmd, byte[] data) {
        ICallback iCallback = mSupportCmd.get(cmd);
        if (iCallback != null) {
            return iCallback.invoke(pkgName, cmd, data);
        } else {
            throw new RuntimeException("callback is null , cmd=" + cmd + ",values = " + mSupportCmd.keySet());
        }
    }

    @Override
    public boolean intercept(String cmd) {
        return mSupportCmd.get(cmd) != null;
    }
}
