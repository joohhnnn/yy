package com.txznet.webchat.ui.rearview_mirror.widget;

import com.txznet.comm.notification.WxNotificationInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.webchat.ui.base.interfaces.IMessageNotification;
import com.txznet.webchat.util.ContactEncryptUtil;

/**
 * 后视镜版本的消息提示工具，用于向Core发送远程调用展示新消息提示Dialog
 * 已弃用，消息提示统一改为车机版本样式
 * Created by J on 2016/10/19.
 */
@Deprecated
public class MessageNotification implements IMessageNotification {

    private WxNotificationInfo mInfo;

    private static final MessageNotification sInstance = new MessageNotification();

    public static MessageNotification getInstance() {
        return sInstance;
    }

    private MessageNotification() {
    }

    @Override
    public void showNotify(WxNotificationInfo info) {
        mInfo = info;
        JSONBuilder builder = new JSONBuilder();
        builder.put("msgId", info.msgId);
        builder.put("type", info.type);
        builder.put("nick", info.nick);
        builder.put("hasSpeak", info.hasSpeak);
        builder.put("openId", ContactEncryptUtil.encrypt(info.openId));

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.notification.notify", builder.toBytes(), null);
    }

    @Override
    public void dismissNotify() {
        if (null == mInfo) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.notification.cancel", null, null);
            return;
        }

        JSONBuilder builder = new JSONBuilder();
        builder.put("openId", mInfo.openId);
        builder.put("msgId", mInfo.msgId);

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.notification.cancel", builder.toBytes(), null);
    }

    @Override
    public void setOnNotificationClickListener(OnNotificationClickListener listener) {

    }
}
