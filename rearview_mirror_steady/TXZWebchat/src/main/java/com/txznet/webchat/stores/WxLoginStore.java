package com.txznet.webchat.stores;

import android.content.Intent;
import android.os.Bundle;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.actions.TtsActionCreator;
import com.txznet.webchat.actions.WxPluginActionCreator;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.helper.TXZSyncHelper;
import com.txznet.webchat.helper.WxStatusHelper;
import com.txznet.webchat.sp.TipManager;
import com.txznet.webchat.ui.base.widgets.PushLoginNotificationDialog;

public class WxLoginStore extends Store {
    private static WxLoginStore sInstance = new WxLoginStore(Dispatcher.get());
    private static final String ACTION_LOGIN = "com.txznet.webchat.action.NOTIFY_LOGIN";
    private static final String ACTION_LOGOUT = "com.txznet.webchat.action.NOTIFY_LOGOUT";

    /**
     * Constructs and registers an instance of this mWrapper with the given dispatcher.
     *
     * @param dispatcher
     */
    WxLoginStore(Dispatcher dispatcher) {
        super(dispatcher);
        // 初始化时调用下停止录音逻辑, 避免录音过程中微信进程重启导致录音逻辑空跑
        MessageActionCreator.get().cancelReply(true);
    }

    public static WxLoginStore get() {
        return sInstance;
    }

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;
        switch (action.getType()) {
            case ActionType.WX_LOGIN_SUCCESS:
                dispatcher.waitFor(AppStatusStore.get().getDispatchToken());
                changed = doLoginSuccess((Bundle) action.getData());
                break;
            case ActionType.WX_LOGIN_FAIL:
                changed = doLoginFail();
                break;
            case ActionType.WX_LOGOUT_REQUEST:
                dispatcher.waitFor(WxContactStore.getInstance().getDispatchToken(), WxMessageStore.getInstance().getDispatchToken());
                changed = doWxLogout();
                break;

            case ActionType.WX_PLUGIN_LOGIC_RESET:
                dispatcher.waitFor(WxContactStore.getInstance().getDispatchToken(), WxMessageStore.getInstance().getDispatchToken());
                changed = reset();
                break;
        }
        if (changed) {
            emitChange(new StoreChangeEvent(EVENT_TYPE_ALL));
        }
    }

    private boolean doWxLogout() {
        TtsActionCreator.get().insertTts(TipManager.getTip(TipManager.KEY_TIP_LOGOUT), true, null);
        if (isLogin) {
            GlobalContext.get().sendBroadcast(new Intent(ACTION_LOGOUT));
        }
        isLogin = false;
        TXZSyncHelper.getInstance().reportLoginStatus();

        // 通知进行插件装载
        WxPluginActionCreator.getInstance().notifyLoadPlugin();

        //reset();
        WxStatusHelper.getInstance().notifyLoginUserInfoChanged();
        return true;
    }

    private boolean doLoginFail() {
        if (bWakeupLogin) {
            bWakeupLogin = false;
            if (!bSilentLogin) {
                TtsUtil.speakText(TipManager.getTip(TipManager.KEY_TIP_LOGIN_RESTORE_FAILED));
            }
        } else {
            if (!bSilentLogin) {
                TtsUtil.speakText(TipManager.getTip(TipManager.KEY_TIP_LOGIN_FAILED));
            }
            if (isLogin) {
                GlobalContext.get().sendBroadcast(new Intent(ACTION_LOGOUT));
            }
            isLogin = false;
        }
        bSilentLogin = false;

        PushLoginNotificationDialog.getInstance().dismiss();

        return false;

    }

    private boolean doLoginSuccess(Bundle bundle) {
        if (!isLogin) {
            GlobalContext.get().sendBroadcast(new Intent(ACTION_LOGIN));
        }
        isLogin = true;
        loginTime = (int) (System.currentTimeMillis() / 1000);

        TXZSyncHelper.getInstance().reportLoginStatus();
        String hint;
        mHitCount = bundle.getInt("hitCount");

        if ((2 == mHitCount) && WxServerConfigStore.getInstance().isPushLoginEnabled() &&
                !AppStatusStore.get().isAutoLoginEnabled()) {
            // 该账户第二次登录, 引导设置自动登录
            hint = TipManager.getTip(TipManager.KEY_TIP_LOGIN_SUCCESS_INTRO_AUTO_LOGIN);
        } else if (1 == mHitCount) {
            hint = TipManager.getTip(TipManager.KEY_TIP_LOGIN_SUCCESS_TIP);
        } else {
            hint = TipManager.getTip(TipManager.KEY_TIP_LOGIN_SUCCESS);
        }

        TtsUtil.speakText(hint);

        PushLoginNotificationDialog.getInstance().dismiss();
        WxStatusHelper.getInstance().notifyLoginUserInfoChanged();
        return true;
    }

    private boolean reset() {
        bWakeupLogin = false;
        bSilentLogin = false;
        if (isLogin) {
            isLogin = false;
            GlobalContext.get().sendBroadcast(new Intent(ACTION_LOGOUT));
            return true;
        }
        return false;
    }

    private boolean isLogin;
    private int loginTime;
    private boolean bWakeupLogin; // 是否从唤醒登录
    private boolean bSilentLogin; // 是否静默登录

    private int mHitCount; // 当前用户成功登录次数(缓存命中次数)

    /*
        是否登录成功
     */
    public boolean isLogin() {
        return isLogin;
    }

    public static final String EVENT_TYPE_ALL = "wx_login_store";

    public int getLoginTime() {
        return this.loginTime;
    }
}
