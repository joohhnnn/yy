package com.txznet.webchat.stores;


import android.os.Bundle;
import android.text.TextUtils;

import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.actions.AppStatusActionCreator;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.helper.TXZSyncHelper;
import com.txznet.webchat.sp.WxConfigSp;

public class AppStatusStore extends Store {
    private static AppStatusStore sInstance = new AppStatusStore(Dispatcher.get());
    private boolean bEnableMsgBroad = true; // 消息自动播报
    private boolean bEnableGroupMsgBroad = true; // 群消息自动播报
    private boolean bEnableUIAsr = true; // 启用界面唤醒命令
    private boolean bEnableAutoLogin; // 是否开机自动登录

    // ---------------sdk相关设置--------------------
    private boolean bEnableGroupContact = true; // 展示群联系人(联系人选择界面)

    /**
     * Constructs and registers an instance of this mWrapper with the given dispatcher.
     *
     * @param dispatcher
     */
    AppStatusStore(Dispatcher dispatcher) {
        super(dispatcher);
        // 加载设置
        bEnableMsgBroad = WxConfigSp.getInstance().getMsgBroadEnabled(true);
        bEnableGroupMsgBroad = WxConfigSp.getInstance().getGroupMsgBroad(true);
        bEnableUIAsr = WxConfigSp.getInstance().getUIAsrEnabled(true);
        bEnableAutoLogin = WxConfigSp.getInstance().getAutoLoginEnabled(false);
        bEnableGroupContact = WxConfigSp.getInstance().getGroupContactEnabled(true);
    }

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;
        switch (action.getType()) {
            case ActionType.WX_LOGIN_SUCCESS:
                changed = doLoginSuccess((Bundle) action.getData());
                break;
            case ActionType.WX_AUTO_BROAD_ENABLE:
                changed = setAutoBroadEnabled(true);
                break;
            case ActionType.WX_AUTO_BROAD_DISABLE:
                changed = setAutoBroadEnabled(false);
                break;

            case ActionType.WX_GROUP_MSG_BROAD_ENABLE:
                changed = setGroupMsgBroadEnabled(true);
                break;

            case ActionType.WX_GROUP_MSG_BROAD_DISABLE:
                changed = setGroupMsgBroadEnabled(false);
                break;

            case ActionType.WX_GROUP_CONTACT_ENABLE:
                changed = setGroupContactEnabled(true);
                break;

            case ActionType.WX_GROUP_CONTACT_DISABLE:
                changed = setGroupContactEnabled(false);
                break;

            case ActionType.WX_WAKEUP_ASR_CMD_ENABLE:
                changed = setUIAsrEnabled(true);
                break;

            case ActionType.WX_WAKEUP_ASR_CMD_DISABLE:
                changed = setUIAsrEnabled(false);
                break;

            case ActionType.WX_AUTO_LOGIN_ENABLE:
                changed = setAutoLoginEnabled(true);
                break;

            case ActionType.WX_AUTO_LOGIN_DISABLE:
                changed = setAutoLoginEnabled(false);
                break;
        }
        if (changed) {
            emitChange(new StoreChangeEvent(EVENT_TYPE_ALL));
        }
    }

    public static AppStatusStore get() {
        return sInstance;
    }

    private boolean doLoginSuccess(Bundle bundle) {
        String uin = bundle.getString("uin");

        // 判断当前登录用户与上次的是否相同, 若不同则关闭开机自动登录并打开自动播报开关
        if(TextUtils.isEmpty(uin) || !uin.equals(WxConfigSp.getInstance().getLastLoginUin(""))) {
            AppStatusActionCreator.get().disableAutoLogin();
            AppStatusActionCreator.get().enableAutoSpeak();
            AppStatusActionCreator.get().enableGroupMsgSpeak();
        }

        // 更新最后登录用户uin
        WxConfigSp.getInstance().setLastLoginUin(uin);

        return true;
    }

    /**
     * 是否自动播报
     *
     * @return
     */
    public boolean isAutoBroadEnabled() {
        return bEnableMsgBroad;
    }

    /**
     * 是否自动播报群消息
     *
     * @return
     */
    public boolean isGroupMsgBroadEnabled() {
        return bEnableGroupMsgBroad;
    }

    /**
     * 是否展示群联系人
     *
     * @return
     */
    public boolean isGroupContactEnabled() {
        return bEnableGroupContact;
    }

    /**
     * 是否开机自动登录
     * @return
     */
    public boolean isAutoLoginEnabled() {
        return bEnableAutoLogin;
    }

    /**
     * 是否启用界面唤醒命令
     *
     * @return
     */
    public boolean isUIAsrEnabled() {
        return bEnableUIAsr;
    }

    private boolean setAutoBroadEnabled(boolean enable) {
        if (enable != bEnableMsgBroad) {
            bEnableMsgBroad = enable;
            WxConfigSp.getInstance().setMsgBroadEnabled(enable);
            return true;
        }

        return false;
    }

    private boolean setGroupMsgBroadEnabled(boolean enable) {
        if (enable != bEnableGroupMsgBroad) {
            bEnableGroupMsgBroad = enable;
            WxConfigSp.getInstance().setGroupMsgBroad(enable);
            return true;
        }

        return false;
    }

    private boolean setGroupContactEnabled(boolean enable) {
        if (enable != bEnableGroupContact) {
            bEnableGroupContact = enable;
            // 群联系人启用状态发生变化需要重新进行联系人同步
            TXZSyncHelper.getInstance().syncContact();
            WxConfigSp.getInstance().setGroupContactEnabled(enable);
            return true;
        }

        return false;
    }

    private boolean setUIAsrEnabled(boolean enable) {
        if (enable != bEnableUIAsr) {
            bEnableUIAsr = enable;
            WxConfigSp.getInstance().setUIAsrEnabled(enable);
            return true;
        }

        return false;
    }

    private boolean setAutoLoginEnabled(boolean enable) {
        if (enable != bEnableAutoLogin) {
            bEnableAutoLogin = enable;
            WxConfigSp.getInstance().setAutoLoginEnabled(enable);
            return true;
        }

        return false;
    }

    public static final String EVENT_TYPE_ALL = "app_status_store";
}
