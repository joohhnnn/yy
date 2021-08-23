package com.txznet.webchat.sdk.base;

import com.txznet.comm.notification.WxNotificationInfo;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;

/**
 * 微信sdk invoker接口
 *
 * @see AbsWxSdkInvoker
 * Created by J on 2018/4/10.
 */

public interface IWxSDKInvoker {
    int getVersion();
    void setRemotePackageName(String packageName);
    void invokeLaunch();
    void invokeQrCode(String qrCode);
    void invokeQrScanned(String userAvatar);
    void invokeLogin();
    void invokeLogout();
    void invokeLoginUserInfo(WxContact loginUser);
    void invokeShowChat(String sessionId);
    void invokeRecordUpdate(String sessionId, int timeRemain);
    void invokeRecordDismiss(String sessionId, boolean sendSuccess, boolean isCancelled);
    void invokeNotificationUpdate(WxNotificationInfo info);
    void invokeNotificationDismiss();
    void invokeSyncContact(int count);
    void invokeSyncMessage(String sessionId, int count);
    void invokeSendMessageStart(WxMessage msg);
    void invokeSendMessageResult(WxMessage msg, boolean success);

    // 设置相关状态
    void invokeMsgBroadcastEnabled(boolean enable);

    // 联系人变动通知接口
    void invokeModContact(WxContact contact);
    void invokeDeleteContact(String id);
}
