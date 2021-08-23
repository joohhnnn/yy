package com.txznet.webchat.actions;

import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.model.WxUIConfig;

/**
 * 微信设置改变相关ActionCreator
 * Created by J on 2017/5/26.
 */

public class ConfigActionCreator {
    // ----------SingleInstance
    public static ConfigActionCreator sInstance;

    public static ConfigActionCreator getInstance() {
        if (null == sInstance) {
            synchronized (ConfigActionCreator.class) {
                if (null == sInstance) {
                    sInstance = new ConfigActionCreator();
                }
            }
        }

        return sInstance;
    }

    private ConfigActionCreator() {}
    // ----------eof SingleInstance

    /**
     * 改变当前界面显示相关参数
     * @param newConfig 新参数
     */
    public void changeUILayoutConfig(WxUIConfig newConfig) {
        Dispatcher.get().dispatch(new Action<>(ActionType.WX_WINDOW_PARAM_CHANGED, newConfig));
    }

    /**
     * 通知Store应用可见性发生变化
     *
     * @param visible
     */
    public void appVisibilityChanged(boolean visible) {
        Dispatcher.get().dispatch(new Action<>(ActionType.WX_APP_VISIBILITY_CHANGED, visible));
    }
}
