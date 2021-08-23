package com.txznet.webchat.actions;

import android.os.Bundle;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.webchat.dispatcher.Dispatcher;

public class TXZBindActionCreator {
    private static TXZBindActionCreator sInstance;
    private Dispatcher dispatcher;

    TXZBindActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static TXZBindActionCreator get() {
        if (sInstance == null) {
            synchronized (TXZBindActionCreator.class) {
                if (sInstance == null) {
                    sInstance = new TXZBindActionCreator(Dispatcher.get());
                }
            }
        }
        return sInstance;
    }

    public void subscribeBindInfo() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.TXZ_BIND_INFO_REQ, null));
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.subscribe.qrcode", null, null);
    }

    public void updateBindInfo(boolean isBind, String nick, String bindUrl) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("isBind", isBind);
        bundle.putString("nick", nick);
        bundle.putString("bindUrl", bindUrl);
        dispatcher.dispatch(new Action<Bundle>(ActionType.TXZ_BIND_INFO_RESP, bundle));
    }

    public void updateBindInfo(String nick) {
        Bundle bundle = new Bundle();
        bundle.putString("nick", nick);
        dispatcher.dispatch(new Action<Bundle>(ActionType.TXZ_BIND_INFO_RESP_ONLY_NICK, bundle));
    }

    public void updateBindInfoException() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.TXZ_BIND_INFO_RESP_ERROR, null));
    }
}
