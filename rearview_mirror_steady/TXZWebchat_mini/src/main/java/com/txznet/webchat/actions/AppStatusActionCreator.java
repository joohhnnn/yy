package com.txznet.webchat.actions;

import android.os.Bundle;

import com.txznet.webchat.dispatcher.Dispatcher;

public class AppStatusActionCreator {
    private static AppStatusActionCreator sInstance;
    private Dispatcher dispatcher;

    AppStatusActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static AppStatusActionCreator get() {
        if (sInstance == null) {
            synchronized (AppStatusActionCreator.class) {
                if (sInstance == null) {
                    sInstance = new AppStatusActionCreator(Dispatcher.get());
                }
            }
        }
        return sInstance;
    }


    /**
     * 打开微信登陆入口
     */
    public void enableWxEntry() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_ENTRY_ENABLE, null));
    }

    /**
     * 关闭微信登陆入口
     */
    public void disableWxEntry() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_ENTRY_DISABLE, null));
    }

    /**
     * 打开远程控制入口
     */
    public void enableControlEntry() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_CONTROL_ENABLE, null));
    }

    /**
     * 关闭远程控制入口
     */
    public void disableControlEntry() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_CONTROL_DISABLE, null));
    }

    /**
     * 获取微信远程配置
     */
    public void getWxServerConfig() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_SERVER_CONFIG_REQ, null));
    }
}
