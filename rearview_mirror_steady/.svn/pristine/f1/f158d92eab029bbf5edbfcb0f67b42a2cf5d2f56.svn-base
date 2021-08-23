package com.txznet.webchat.sdk.v1;

import com.alibaba.fastjson.JSON;
import com.txznet.comm.notification.WxNotificationInfo;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.WechatMessage;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.sdk.base.AbsWxSdkInvoker;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.stores.WxQrCodeStore;
import com.txznet.webchat.util.ContactEncryptUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信SDK调用发起工具(V1)
 *
 * 用于兼容旧版本微信sdk, 已不再维护, 不应该再被修改
 * Created by J on 2018/4/23.
 */

public class WxSDKInvokerV1 extends AbsWxSdkInvoker {
    private static final String LOG_TAG = "WxSDKInvokerV1";

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    protected String getInvokeCommand(final String cmd) {
        // 旧版本sdk调用使用tool.wechat.前缀
        return "tool.wechat." + cmd;
    }

    @Override
    public void invokeLaunch() {
        sendInvoke("launch", null);
    }

    @Override
    public void invokeQrCode(final String qrCode) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("qrcode", qrCode);
        sendInvoke("qr.update", builder.toBytes());
    }

    @Override
    public void invokeQrScanned(final String userAvatar) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("icon", WxQrCodeStore.get().getScannerPicStr());
        sendInvoke("qr.scanned", builder.toBytes());
    }

    @Override
    public void invokeLogin() {
        sendInvoke("login", null);
    }

    @Override
    public void invokeLogout() {
        sendInvoke("logout", null);
    }

    @Override
    public void invokeLoginUserInfo(final WxContact loginUser) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("id", ContactEncryptUtil.encrypt(loginUser.mUserOpenId));
        builder.put("nick", loginUser.getDisplayName());

        sendInvoke("update.self", builder.toBytes());
    }

    @Override
    public void invokeShowChat(final String sessionId) {
        List<WxMessage> msgList = WxMessageStore.getInstance().getMessageList(sessionId);
        List<WechatMessage> list = getMsgList(msgList);
        WxContact contact = WxContactStore.getInstance().getContact(sessionId);

        JSONBuilder builder = new JSONBuilder();
        builder.put("contactId", ContactEncryptUtil.encrypt(sessionId));
        builder.put("contactNick", contact.getDisplayName());
        builder.put("message", JSON.toJSONString(list));
        sendInvoke("chat.show", builder.toBytes());
    }

    @Override
    public void invokeRecordUpdate(final String sessionId, final int timeRemain) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("time", timeRemain);
        builder.put("id", ContactEncryptUtil.encrypt(sessionId));
        builder.put("nick", WxContactStore.getInstance().getContact(sessionId).getDisplayName());
        sendInvoke("record.update", builder.toBytes());
    }

    @Override
    public void invokeRecordDismiss(final String sessionId, final boolean sendSuccess,
                                    boolean isCancelled) {
        sendInvoke("record.dismiss", (sendSuccess + "").getBytes());
    }

    @Override
    public void invokeNotificationUpdate(final WxNotificationInfo info) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("msgId", info.msgId);
        builder.put("type", info.type);
        builder.put("nick", info.nick);
        builder.put("hasSpeak", info.hasSpeak);
        builder.put("id", ContactEncryptUtil.encrypt(info.openId));
        builder.put("isGroup", WxContact.isGroupOpenId(info.openId));
        sendInvoke("notify.show", builder.toBytes());
    }

    @Override
    public void invokeNotificationDismiss() {
        sendInvoke("notify.cancel", null);
    }

    @Override
    public void invokeSyncContact(final int count) {

    }

    @Override
    public void invokeSyncMessage(final String sessionId, final int count) {

    }

    @Override
    public void invokeMsgBroadcastEnabled(final boolean enable) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("enabled", enable);
        sendInvoke("notify.status", builder.toBytes());
    }

    @Override
    public void invokeModContact(final WxContact contact) {

    }

    @Override
    public void invokeDeleteContact(final String id) {

    }

    @Override
    public void invokeSendMessageStart(final WxMessage msg) {

    }

    @Override
    public void invokeSendMessageResult(final WxMessage msg, final boolean success) {

    }

    private List<WechatMessage> getMsgList(List<WxMessage> list) {
        List<WechatMessage> msgList = new ArrayList<>();

        if (null == list) {
            return msgList;
        }

        for (WxMessage msg : list) {
            WechatMessage message = new WechatMessage(String.valueOf(msg.mMsgId),
                    ContactEncryptUtil.encrypt(msg.mSessionId), ContactEncryptUtil.encrypt(msg
                    .mSenderUserId), msg.mMsgType, msg.mContent);
            msgList.add(message);
        }

        return msgList;
    }
}
