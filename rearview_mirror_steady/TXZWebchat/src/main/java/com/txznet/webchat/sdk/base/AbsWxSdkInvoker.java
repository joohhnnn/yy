package com.txznet.webchat.sdk.base;

import android.text.TextUtils;

import com.txznet.sdk.wechat.InvokeConstants;
import com.txznet.webchat.util.ServiceManagerInvoker;

/**
 * default implementation of IWxSDKInvoker
 * Created by J on 2018/8/16.
 */

public abstract class AbsWxSdkInvoker implements IWxSDKInvoker {
    protected String mRemotePackageName;

    @Override
    public void setRemotePackageName(final String packageName) {
        mRemotePackageName = packageName;
    }

    /**
     * 根据调用命令拼装用于ipc调用的命令字, 必要时子类可以重写以更改拼装逻辑
     * @param cmd 调用命令
     * @return ipc命令字
     */
    protected String getInvokeCommand(String cmd) {
        return InvokeConstants.WX_CMD_PREFIX + cmd;
    }

    /**
     * 发送微信sdk调用
     * @param cmd 命令字
     * @param data 调用数据
     */
    protected final void sendInvoke(String cmd, byte[] data) {
        if (TextUtils.isEmpty(mRemotePackageName)) {
            return;
        }

        ServiceManagerInvoker.sendInvoke(mRemotePackageName, getInvokeCommand(cmd), data);
    }
}
