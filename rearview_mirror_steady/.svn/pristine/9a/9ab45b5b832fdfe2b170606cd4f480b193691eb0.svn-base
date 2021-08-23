package com.txznet.webchat.sdk.v2;

import android.os.Parcel;

import com.txznet.sdk.wechat.InvokeConstants;
import com.txznet.webchat.sdk.base.AbsWxSdkInvoker;
import com.txznet.webchat.stores.TXZBindStore;

/**
 * 微信SDK调用发起工具(V2)
 * Created by J on 2018/4/16.
 */

public class WxSDKInvokerV2 extends AbsWxSdkInvoker {
    private static final String LOG_TAG = "WxSDKInvokerV2";

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public void invokeLaunch() {
        Parcel p = Parcel.obtain();
        p.writeString(TXZBindStore.get().getBindUrl());
        sendInvoke(InvokeConstants.WX_CMD_LAUNCH, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeQrCode(final String qrCode) {
        Parcel p = Parcel.obtain();
        p.writeString(qrCode);

        sendInvoke(InvokeConstants.WX_CMD_QR_UPDATE, p.marshall());
        p.recycle();
    }
}
