package com.txznet.webchat.helper;

import android.content.Intent;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.webchat.log.L;

public class WxStatusHelper {
    private static final String ACTION_STATUS_CHANGED = "com.txznet.webchat.action.STATUS_CHANGED";

    private static final int STATUS_ON_OPEN = 0; // 打开的时候
    private static final int STATUS_ON_CLOSE = 1; // 关闭的时候
    private static final int STATUS_ON_GET_SUCCESS = 2; // 获取成功的时候
    private static final int STATUS_ON_GET_FAILED = 3; // 获取失败的时候
    private static final int STATUS_ON_GET_CHANGED = 4; // 状态发生改变的时候

    private static WxStatusHelper sInstace;

    public static WxStatusHelper getInstance() {
        if (sInstace == null) {
            synchronized (WxStatusHelper.class) {
                if (sInstace == null) {
                    sInstace = new WxStatusHelper();
                }
            }
        }
        return sInstace;
    }

    public void notifyOpen() {
        Intent intent = new Intent(ACTION_STATUS_CHANGED);
        intent.putExtra("status", STATUS_ON_OPEN);
        GlobalContext.get().sendBroadcast(intent);
        L.d("WxStatusHelper::notifyOpen");
    }

    public void notifyClose() {
        Intent intent = new Intent(ACTION_STATUS_CHANGED);
        intent.putExtra("status", STATUS_ON_CLOSE);
        GlobalContext.get().sendBroadcast(intent);
        L.d("WxStatusHelper::notifyClose");
    }

    public void notifyGetBindMsgSucc(Boolean isBind, String binder) {
        Intent intent = new Intent(ACTION_STATUS_CHANGED);
        intent.putExtra("status", STATUS_ON_GET_SUCCESS);
        intent.putExtra("isBind", isBind);
        intent.putExtra("binder", binder);
        GlobalContext.get().sendBroadcast(intent);
        L.d("WxStatusHelper::notifyGetBindMsgSucc, isBind=" + isBind + ", binder=" + binder);
    }

    public void notifyGetBindMsgFailed() {
        Intent intent = new Intent(ACTION_STATUS_CHANGED);
        intent.putExtra("status", STATUS_ON_GET_FAILED);
        GlobalContext.get().sendBroadcast(intent);
        L.d("WxStatusHelper::notifyGetBindMsgFailed");
    }

    public void notifyGetBindMsgChanged(Boolean isBind, String binder) {
        Intent intent = new Intent(ACTION_STATUS_CHANGED);
        intent.putExtra("status", STATUS_ON_GET_CHANGED);
        intent.putExtra("isBind", isBind);
        intent.putExtra("binder", binder);
        GlobalContext.get().sendBroadcast(intent);
        L.d("WxStatusHelper::notifyGetBindMsgChanged, isBind=" + isBind + ", binder=" + binder);
    }

    private boolean bChatModeEnabled = false;

    public void notifyChatModeStatusChanged(boolean enable) {
        if (bChatModeEnabled == enable) {
            return;
        }

        bChatModeEnabled = enable;
        Intent intent = new Intent("com.txznet.webchat.action.chatmode");
        intent.putExtra("enable", enable);
        GlobalContext.get().sendBroadcast(intent);
    }

}
