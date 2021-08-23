package com.txznet.webchat.ui.base;

import android.content.Intent;

import com.squareup.otto.Subscribe;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.webchat.sdk.WxSDKManager;
import com.txznet.webchat.sp.WebChatSp;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.stores.WxThemeStore;

/**
 * 处理界面展示相关逻辑
 * Created by J on 2016/5/3.
 */
public class UIHandler {
    private static final String ACTION_QR_REFRESH = "com.txznet.webchat.action.QR_REFRESH";

    private static UIHandler sInstance;
    private boolean bNotificationEnabled;
    private boolean bUIEnabled;
    private boolean bRecordWindowEnabled;

    private UIHandler() {
        bUIEnabled = WebChatSp.getInstance(GlobalContext.get()).getUIEnabled();
        bNotificationEnabled = WebChatSp.getInstance(GlobalContext.get()).getNotificationEnabled();
        bRecordWindowEnabled = WebChatSp.getInstance(GlobalContext.get()).getRecordWindowEnabled();

        TXZBindStore.get().register(this);
    }

    public static UIHandler getInstance() {
        if (null == sInstance) {
            synchronized (UIHandler.class) {
                if (null == sInstance) {
                    sInstance = new UIHandler();
                }
            }
        }

        return sInstance;
    }

    public boolean getWxUIEnabled() {
        return bUIEnabled;
    }

    public void setUIEnabled(boolean enable) {
        this.bUIEnabled = enable;
        WebChatSp.getInstance(GlobalContext.get()).setUIEnabled(enable);
    }

    public void setNotificationEnabled(boolean enable) {
        this.bNotificationEnabled = enable;
        WebChatSp.getInstance(GlobalContext.get()).setNotificationEnabled(enable);
    }

    public boolean getNotificationEnabled() {
        return bNotificationEnabled;
    }

    public void setRecordWindowEnabled(boolean enable) {
        this.bRecordWindowEnabled = enable;
        WebChatSp.getInstance(GlobalContext.get()).setRecordWindowEnabled(enable);
    }

    public boolean getRecordWindowEnabled() {
        return bRecordWindowEnabled;
    }

    public void showAppStart(boolean needTts) {
        if (bUIEnabled) {
            launchUIForceWechat();
        }

        WxSDKManager.getInstance().notifyLaunch();
    }

    /**
     * 显示微信界面
     */
    public void launchUI() {
        if (bUIEnabled) {
            final Intent intent = new Intent();
            intent.setClass(GlobalContext.get(), WxThemeStore.get().getClassForQRActivity());
            intent.putExtra("intent_key_target_page", "remote_control");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            // 添加标志位，登录成功后退出微信界面
            intent.putExtra("quitAfterLogin", true);
            GlobalContext.get().startActivity(intent);
        }

    }

    /**
     * 显示微信界面
     */
    public void launchUIForceWechat() {
        if (bUIEnabled) {
            final Intent intent = new Intent();
            intent.setClass(GlobalContext.get(), WxThemeStore.get().getClassForQRActivity());
            intent.putExtra("intent_key_target_page", "wechat");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            // 添加标志位，登录成功后退出微信界面
            intent.putExtra("quitAfterLogin", true);
            GlobalContext.get().startActivity(intent);
        }

    }

    /**
     * 隐藏微信界面
     */
    public void hideUI() {
        if (ActivityStack.getInstance().has()) {
            ActivityStack.getInstance().currentActivity().moveTaskToBack(true);
        }
    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
    }
}
