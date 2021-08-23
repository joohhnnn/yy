package com.txznet.webchat.actions;

import android.os.Bundle;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.plugin.WxPluginManager;
import com.txznet.webchat.sp.PowerSp;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.WxLoginStore;

public class LoginActionCreator {
    private static LoginActionCreator sInstance;
    private Dispatcher dispatcher;

    LoginActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static LoginActionCreator get() {
        if (sInstance == null) {
            synchronized (LoginActionCreator.class) {
                if (sInstance == null) {
                    sInstance = new LoginActionCreator(Dispatcher.get());
                }
            }
        }
        return sInstance;
    }

    /**
     * 获取本地缓存的用户列表
     */
    public void getUserCacheList() {
        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_GET_USER_CACHE);
    }

    /**
     * 清除本地缓存列表(数据库中的数据也会被清空)
     */
    public void clearUserCache() {
        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_CLEAR_CACHE);
    }

    /**
     * 切换用户缓存
     */
    public void switchUserCache(String uid) {
        dispatcher.dispatch(new Action<>(ActionType.WX_PLUGIN_PUSH_LOGIN_SWITCH_CONTACT, uid));
    }

    /**
     * 开始推送登录
     * @param uid 对应用户uid
     */
    public void pushLogin(String uid) {
        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_PUSH_LOGIN, uid);
    }

    /**
     *   获取二维码
     */
    public void getQrCode() {
        if (PowerSp.getInstance(GlobalContext.get()).getSleepMode(false)) {
            return;
        }

        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_START);
    }

    /**
     * 刷新二维码
     */
    public void refreshQRCode() {
        if (PowerSp.getInstance(GlobalContext.get()).getSleepMode(false)) {
            return;
        }

        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_REFRESH_LOGIN_QR);
    }

    /**
     *   登出
     */
    public void doLogout(boolean exitWx) {
        if (!WxLoginStore.get().isLogin()) {
            return;
        }

        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_LOGOUT, exitWx);
    }

    /**
     * 休眠
     */
    public void doSleep() {
//        dispatcher.dispatch(new Action<String>(ActionType.SYSTEM_POWER_SLEEP, null));
        // 关闭正在显示的消息提醒Dialog和录音Dialog， 避免休眠后没有结束回调导致界面卡住
        PowerSp.getInstance(GlobalContext.get()).setSleepMode(true);
        MessageActionCreator.get().cancelNotify();
        MessageActionCreator.get().cancelReply(true);
        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_SLEEP);
    }

    /**
     *   唤醒
     */
    public void doWakeup() {
//        dispatcher.dispatch(new Action<Boolean>(ActionType.SYSTEM_POWER_WAKEUP, bSilent));
        PowerSp.getInstance(GlobalContext.get()).setSleepMode(false);
        Bundle bundle = new Bundle();
        bundle.putBoolean("autoLogin", AppStatusStore.get().isAutoLoginEnabled());
        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_WAKEUP, bundle);
    }
}
