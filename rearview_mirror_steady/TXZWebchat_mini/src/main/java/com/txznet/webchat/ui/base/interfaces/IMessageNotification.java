package com.txznet.webchat.ui.base.interfaces;

import com.txznet.comm.notification.WxNotificationInfo;

/**
 * Created by J on 2016/10/19.
 */

public interface IMessageNotification {
    void showNotify(WxNotificationInfo info);

    void dismissNotify();

    void setOnNotificationClickListener(OnNotificationClickListener listener);

    interface OnNotificationClickListener {
        void onClick(String openId);

        void onReply(String openId);
    }
}
