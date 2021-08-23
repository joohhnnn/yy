package com.txznet.webchat.sdk.v1;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.webchat.sdk.base.AbsWxSdkInvoker;

/**
 * 微信SDK调用发起工具(V1)
 * <p>
 * 用于兼容旧版本微信sdk, 已不再维护, 不应该再被修改
 * Created by J on 2018/4/23.
 */

public class WxSDKInvokerV1 extends AbsWxSdkInvoker {
    private static final String LOG_TAG = "WxSDKInvokerV1";

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    protected String getInvokeCommand(final String cmd) {
        // 旧版本sdk调用使用tool.wechat.前缀
        return "tool.wechat." + cmd;
    }

    @Override
    public void invokeLaunch() {
        sendInvoke("launch", null);
    }

    @Override
    public void invokeQrCode(final String qrCode) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("qrcode", qrCode);
        sendInvoke("qr.update", builder.toBytes());
    }
}
