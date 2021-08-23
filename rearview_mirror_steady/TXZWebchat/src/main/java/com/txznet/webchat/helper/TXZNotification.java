package com.txznet.webchat.helper;

import android.text.TextUtils;

import com.txznet.comm.notification.WxNotificationInfo;
import com.txznet.webchat.actions.MessageActionCreator;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.log.L;
import com.txznet.webchat.sdk.WxSDKManager;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxResourceStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.UIHandler;
import com.txznet.webchat.ui.base.interfaces.IMessageNotification;
import com.txznet.webchat.util.SmileyParser;

public class TXZNotification implements IMessageNotification.OnNotificationClickListener {
    private static TXZNotification sInstance;
    private String mLastMsgId;
    private boolean bShowing;
    private WxNotificationInfo mNotificationInfo;

    private TXZNotification() {

    }

    public static TXZNotification getInstance() {
        if (sInstance == null) {
            synchronized (TXZNotification.class) {
                if (sInstance == null) {
                    sInstance = new TXZNotification();
                }
            }
        }
        return sInstance;
    }

    public boolean isShowing() {
        return bShowing;
    }

    public String getCurrentSessionId() {
        if (null == mNotificationInfo || TextUtils.isEmpty(mNotificationInfo.openId)) {
            return "";
        }

        return mNotificationInfo.openId;
    }

    private void notifyTts(WxNotificationInfo info) {
        if (UIHandler.getInstance().getNotificationEnabled()) {
            IMessageNotification notification = WxThemeStore.get().getNotificationPresenter();
            notification.showNotify(info);
            notification.setOnNotificationClickListener(this);
        }

        WxSDKManager.getInstance().notifyNotificationUpdate(true, info);
    }

    public void notifyBeginTts(String openId, String msgId) {
        WxContact contact = WxContactStore.getInstance().getContact(openId);
        if (contact != null && !WxContactStore.getInstance().getLoginUser().equals(contact.mUserOpenId)) {
            mNotificationInfo = new WxNotificationInfo();
            mNotificationInfo.nick = SmileyParser.removeEmoji(contact.getDisplayName());
            mNotificationInfo.openId = openId;
            mNotificationInfo.msgId = msgId;
            mNotificationInfo.hasSpeak = true;
            bShowing = true;
            notifyTts(mNotificationInfo);
            mLastMsgId = msgId;
            // load user head image
            WxResourceStore.get().getContactHeadImage(openId);
        }
    }

    public void notifyEndTts(String openId, String msgId) {
        if (!msgId.equals(mLastMsgId)) {
            return;
        }

        if (mNotificationInfo != null && !TextUtils.isEmpty(mNotificationInfo.msgId) && mNotificationInfo.msgId.equals(msgId)) {
            mNotificationInfo.hasSpeak = false;
            bShowing = true;
            notifyTts(mNotificationInfo);
        }
    }

    public void notifyCancelTts(String openId, String msgId) {
        L.d("notify cancel");
        bShowing = false;
        if (UIHandler.getInstance().getNotificationEnabled()) {
            IMessageNotification notification = WxThemeStore.get().getNotificationPresenter();
            notification.dismissNotify();
        }

        WxSDKManager.getInstance().notifyNotificationUpdate(false, null);
    }

    @Override
    public void onClick(String openId) {
        if (!TextUtils.isEmpty(openId)) {
            UIHandler.getInstance().showChat(openId, false);
        }
    }

    @Override
    public void onReply(String openId) {
        if (WxLoginStore.get().isLogin()) {
            if (!TextUtils.isEmpty(openId)) {
                MessageActionCreator.get().replyVoice(openId, false);
            }
        }
    }
}
