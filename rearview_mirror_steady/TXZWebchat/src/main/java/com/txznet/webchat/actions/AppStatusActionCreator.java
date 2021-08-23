package com.txznet.webchat.actions;

import android.os.Bundle;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.webchat.AppStatus;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.log.L;
import com.txznet.webchat.sp.PowerSp;

public class AppStatusActionCreator {
    private static AppStatusActionCreator sInstance;
    private Dispatcher dispatcher;

    AppStatusActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static AppStatusActionCreator get() {
        if (sInstance == null) {
            synchronized (ContactActionCreator.class) {
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
     * 启动自动播报
     */
    public void enableAutoSpeak() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_AUTO_BROAD_ENABLE, null));
    }

    /**
     * 关闭自动播报
     */
    public void disableAutoSpeak() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_AUTO_BROAD_DISABLE, null));
    }

    /**
     * 打开群消息播报
     */
    public void enableGroupMsgSpeak() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_GROUP_MSG_BROAD_ENABLE, null));
    }

    /**
     * 屏蔽群消息播报
     */
    public void disableGroupMsgSpeak() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_GROUP_MSG_BROAD_DISABLE, null));
    }

    /**
     * 启用位置消息处理
     */
    public void enableLocMsg() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_LOC_MSG_PROC_ENABLE, null));
    }

    /**
     * 禁用位置消息处理(位置消息不播报导航过去提示, 不响应导航过去命令)
     */
    public void disableLocMsg() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_LOC_MSG_PROC_DISABLE, null));
    }

    /**
     * 启用位置分享功能
     */
    public void enableLocShare() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_LOC_SHARE_ENABLE, null));
    }

    /**
     * 禁用位置分享功能
     */
    public void disableLocShare() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_LOC_SHARE_DISABLE, null));
    }

    /**
     * 启用群联系人展示
     */
    public void enableGroupContact() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_GROUP_CONTACT_ENABLE, null));
    }

    /**
     * 屏蔽群联系人展示
     */
    public void disableGroupContact() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_GROUP_CONTACT_DISABLE, null));
    }

    /**
     * 打开微信自动登录
     */
    public void enableAutoLogin() {
        dispatcher.dispatch(new Action<>(ActionType.WX_AUTO_LOGIN_ENABLE, null));
    }

    /**
     * 关闭微信自动登录
     */
    public void disableAutoLogin() {
        dispatcher.dispatch(new Action<>(ActionType.WX_AUTO_LOGIN_DISABLE, null));
    }

    /*
        退出应用
     */
    public void exitApp() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.REQUEST_APP_EXIT, null));
    }

    /*
        同行者发生重启
     */
    public void notifyTXZReStart() {
        if (!PowerSp.getInstance(GlobalContext.get()).isWakeActionTriggered(false)) {
            L.i("powerAction", "wake action not triggered yet, do wakeup when init success");
            LoginActionCreator.get().doWakeup();
        }

        dispatcher.dispatch(new Action<>(ActionType.TXZ_RESTART, null));
    }

    /*
        是否启用唤醒指令
     */
    public void enableWakeupAsrCmd(boolean enable) {
        if (enable) {
            dispatcher.dispatch(new Action<>(ActionType.WX_WAKEUP_ASR_CMD_ENABLE, null));
        } else {
            dispatcher.dispatch(new Action<>(ActionType.WX_WAKEUP_ASR_CMD_DISABLE, null));
        }
    }

    /**
     * 进入倒车
     */
    public void enterReverse() {
        AppStatus.getInstance().setReverseMode(true);
        dispatcher.dispatch(new Action<>(ActionType.SYSTEM_POWER_REVERSE_ENTER, null));
    }

    /**
     * 退出倒车
     */
    public void exitReverse() {
        AppStatus.getInstance().setReverseMode(false);
        dispatcher.dispatch(new Action<>(ActionType.SYSTEM_POWER_REVERSE_EXIT, null));
    }
}
