package com.txznet.webchat.actions;

import android.os.Bundle;
import android.text.TextUtils;

import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.log.L;

public class ContactActionCreator {
    private static final String LOG_TAG = "ContactActionCreator";
    private static ContactActionCreator sInstance;
    private Dispatcher dispatcher;

    ContactActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static ContactActionCreator get() {
        if (sInstance == null) {
            synchronized (ContactActionCreator.class) {
                if (sInstance == null) {
                    sInstance = new ContactActionCreator(Dispatcher.get());
                }
            }
        }
        return sInstance;
    }

    /**
     * 置顶指定会话
     *
     * @param openId
     */
    public void topSession(String openId) {
        dispatcher.dispatch(new Action<String>(ActionType.WX_PLUGIN_SYNC_TOP_SESSION, openId));
    }

    /**
     * 切换焦点会话
     * @param openId
     */
    public void switchFocusSession(String openId) {
        if (TextUtils.isEmpty(openId)) {
            L.e(LOG_TAG, "switch focus session: openId is null, skip");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("uid", openId);
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_SWITCH_SESSION, bundle));
    }

    /**
     * 通知聊天界面被打开
     *
     * @param openId
     */
    public void openSession(String openId) {
        L.i(LOG_TAG, "open session: " + openId);
        if (TextUtils.isEmpty(openId)) {
            L.e(LOG_TAG, "open session: openId is null, skip");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("uid", openId);
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_OPEN_CHAT, bundle));
    }

    /**
     * 通知聊天界面被关闭
     *
     * @param openId
     */
    public void closeSession(String openId) {
        L.i(LOG_TAG, "close session: " + openId);
        if (TextUtils.isEmpty(openId)) {
            L.e(LOG_TAG, "open session: openId is null, skip");
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("uid", openId);
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_CLOSE_CHAT, bundle));
    }

    /**
     * 屏蔽消息
     * @param openId
     * @param manual
     */
    public void filterSpeak(String openId, boolean manual) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", openId);
        bundle.putBoolean("manual", manual);
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_BLOCK_CONTACT, bundle));
    }

    /**
     * 解除消息屏蔽
     *
     * @param openId
     * @param manual
     */
    public void unfilterSpeak(String openId, boolean manual) {
        Bundle bundle = new Bundle();
        bundle.putString("uid", openId);
        bundle.putBoolean("manual", manual);
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_UNBLOCK_CONTACT, bundle));
    }

    /**
     * 屏蔽当前用户消息
     */
    public void filterCurSpeak() {
        dispatcher.dispatch(new Action<Bundle>(ActionType.WX_BLOCK_CONTACT, null));
    }
}
