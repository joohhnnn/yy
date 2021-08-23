package com.txznet.webchat.sdk.v2;

import android.os.Bundle;
import android.os.Parcel;

import com.txznet.comm.notification.WxNotificationInfo;
import com.txznet.sdk.bean.WechatContactV2;
import com.txznet.sdk.bean.WechatMessageV2;
import com.txznet.sdk.wechat.InvokeConstants;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.log.L;
import com.txznet.webchat.sdk.base.AbsWxSdkInvoker;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.stores.WxQrCodeStore;
import com.txznet.webchat.stores.WxResourceStore;
import com.txznet.webchat.util.ContactEncryptUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信SDK调用发起工具(V2)
 * Created by J on 2018/4/16.
 */

public class WxSDKInvokerV2 extends AbsWxSdkInvoker {
    private static final String LOG_TAG = "WxSDKInvokerV2";

    @Override
    public int getVersion() {
        return 2;
    }
    @Override
    public void invokeLaunch() {
        Parcel p = Parcel.obtain();
        if (WxLoginStore.get().isLogin()) {
            convertSDKContact(WxContactStore.getInstance().getLoginUser()).writeToParcel(p, 0);
            sendInvoke(InvokeConstants.WX_CMD_LAUNCH_WHEN_LOGGEDIN, p.marshall());
        } else {
            p.writeString(WxQrCodeStore.get().getQrCode());
            sendInvoke(InvokeConstants.WX_CMD_LAUNCH, p.marshall());
        }

        p.recycle();
    }

    @Override
    public void invokeQrCode(final String qrCode) {
        Parcel p = Parcel.obtain();
        p.writeString(qrCode);

        sendInvoke(InvokeConstants.WX_CMD_QR_UPDATE, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeQrScanned(final String userAvatar) {
        Parcel p = Parcel.obtain();
        p.writeString(userAvatar);

        sendInvoke(InvokeConstants.WX_CMD_QR_SCAN, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeLogin() {
        sendInvoke(InvokeConstants.WX_CMD_LOGIN, null);
    }

    @Override
    public void invokeLogout() {
        sendInvoke(InvokeConstants.WX_CMD_LOGOUT, null);
    }

    @Override
    public void invokeLoginUserInfo(final WxContact loginUser) {
        WechatContactV2 con = convertSDKContact(loginUser);

        Parcel p = Parcel.obtain();
        con.writeToParcel(p, 0);

        sendInvoke(InvokeConstants.WX_CMD_UPDATE_USER, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeShowChat(final String sessionId) {
        WxContact session = WxContactStore.getInstance().getContact(sessionId);
        if (null == session) {
            L.e(LOG_TAG, "invokeShowChat: cannot find specified session");
            return;
        }

        // write contact
        Parcel p = Parcel.obtain();
        WechatContactV2 contact = convertSDKContact(session);
        contact.writeToParcel(p, 0);

        // write msg list
        List<WxMessage> msgList = WxMessageStore.getInstance().getMessageList(sessionId);
        if (null != msgList && !msgList.isEmpty()) {
            List<WechatMessageV2> sdkMsgList = new ArrayList<>(msgList.size());
            for (WxMessage msg : msgList) {
                sdkMsgList.add(convertSDKMessage(msg));
            }

            p.writeTypedList(sdkMsgList);
        }

        sendInvoke(InvokeConstants.WX_CMD_SHOW_CHAT, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeRecordUpdate(final String sessionId, final int timeRemain) {
        Parcel p = Parcel.obtain();
        p.writeInt(timeRemain);

        WxContact contact = WxContactStore.getInstance().getContact(sessionId);
        convertSDKContact(contact).writeToParcel(p, 0);

        sendInvoke(InvokeConstants.WX_CMD_RECORD_UPDATE, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeRecordDismiss(final String sessionId, final boolean sendSuccess,
                                    boolean isCancelled) {
        Parcel p = Parcel.obtain();
        p.writeInt(sendSuccess ? 1 : 0);
        p.writeInt(isCancelled ? 1 : 0);

        sendInvoke(InvokeConstants.WX_CMD_RECORD_DISMISS, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeNotificationUpdate(final WxNotificationInfo info) {
        WxMessage msg = WxMessageStore.getInstance().getMessage(info.msgId, info.openId);

        if (null == msg) {
            L.e(LOG_TAG, String.format("invokeNotificationUpdate: cannot find message(id = %s, " +
                    "session = %s)", info.msgId, info.openId));
            return;
        }

        Parcel p = Parcel.obtain();
        convertSDKMessage(msg).writeToParcel(p, 0);
        p.writeInt(info.hasSpeak ? 1 : 0);

        sendInvoke(InvokeConstants.WX_CMD_NOTIFICATION_UPDATE, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeNotificationDismiss() {
        sendInvoke(InvokeConstants.WX_CMD_NOTIFICATION_DISMISS, null);
    }

    @Override
    public void invokeSyncContact(final int count) {
        int syncCount = count > 10 ? 10 : count;

        List<String> idList = WxContactStore.getInstance().getSessionList();
        List<WechatContactV2> syncList = new ArrayList<>();
        for (int i = 0, j = 0; i < idList.size() && j < syncCount; i++) {
            WxContact contact = WxContactStore.getInstance().getContact(idList.get(i));

            if (null != contact) {
                syncList.add(convertSDKContact(contact));
                j++;
            }
        }

        Parcel p = Parcel.obtain();
        p.writeTypedList(syncList);

        sendInvoke(InvokeConstants.WX_CMD_SYNC_CONTACT, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeSyncMessage(final String sessionId, final int count) {
        String decryptedId = ContactEncryptUtil.decrypt(sessionId);
        WxContact contact = WxContactStore.getInstance().getContact(decryptedId);

        if (null == contact) {
            L.e(LOG_TAG, "invokeSyncMessage: cannot find specified contact: " + decryptedId);
            return;
        }

        List<WxMessage> msgList = WxMessageStore.getInstance().getMessageList(ContactEncryptUtil
                .decrypt(sessionId));
        List<WechatMessageV2> syncList = new ArrayList<>();
        int startPos = msgList.size() - count;
        for (int i = startPos < 0 ? 0 : startPos; i < msgList.size(); i++) {
            syncList.add(convertSDKMessage(msgList.get(i)));
        }

        Parcel p = Parcel.obtain();
        convertSDKContact(contact).writeToParcel(p, 0);
        p.writeTypedList(syncList);

        sendInvoke(InvokeConstants.WX_CMD_SYNC_MESSAGE, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeMsgBroadcastEnabled(final boolean enable) {
        Parcel p = Parcel.obtain();
        p.writeInt(enable ? 1 : 0);

        sendInvoke(InvokeConstants.WX_CMD_MSG_BROADCAST_ENABLED, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeModContact(final WxContact contact) {
        WechatContactV2 modContact = convertSDKContact(contact);

        Parcel p = Parcel.obtain();
        modContact.writeToParcel(p, 0);

        sendInvoke(InvokeConstants.WX_CMD_MOD_CONTACT, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeDeleteContact(final String id) {
        Parcel p = Parcel.obtain();
        p.writeString(ContactEncryptUtil.encrypt(id));

        sendInvoke(InvokeConstants.WX_CMD_DEL_CONTACT, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeSendMessageStart(final WxMessage msg) {
        WechatMessageV2 message = convertSDKMessage(msg);

        Parcel p = Parcel.obtain();
        message.writeToParcel(p, 0);

        sendInvoke(InvokeConstants.WX_CMD_SEND_MSG_BEGIN, p.marshall());
        p.recycle();
    }

    @Override
    public void invokeSendMessageResult(final WxMessage msg, final boolean success) {
        WechatMessageV2 message = convertSDKMessage(msg);

        Parcel p = Parcel.obtain();
        message.writeToParcel(p, 0);
        p.writeInt(success ? 1 : 0);

        sendInvoke(InvokeConstants.WX_CMD_SEND_MSG_RESULT, p.marshall());
        p.recycle();
    }

    private WechatContactV2 convertSDKContact(WxContact contact) {
        WechatContactV2 result = new WechatContactV2();
        result.setId(ContactEncryptUtil.encrypt(contact.mUserOpenId));

        // set type
        switch (contact.mType) {
            case PEOPLE:
            case GROUP_MEMBER:
                result.setType(WechatContactV2.CONTACT_TYPE_PEOPLE);
                break;

            case GROUP:
                result.setType(WechatContactV2.CONTACT_TYPE_GROUP);
                break;
        }

        // set avatar if possible
        result.setAvatar(WxResourceStore.get().getContactHeadImage(contact.mUserOpenId, false));
        // set nickname
        result.setNickName(contact.getDisplayName());
        // set notify stat
        result.setNotifyMsg(!WxContactStore.getInstance().isContactBlocked(contact.mUserOpenId));

        return result;
    }

    private WechatMessageV2 convertSDKMessage(WxMessage message) {
        WechatMessageV2 result = new WechatMessageV2();
        result.setMsgId(String.valueOf(message.mMsgId));

        WxContact sender = WxContactStore.getInstance().getContact(message.mSenderUserId);
        result.setSenderId(ContactEncryptUtil.encrypt(sender.mUserOpenId));
        result.setSenderNick(sender.getDisplayName());
        WxContact session = WxContactStore.getInstance().getContact(message.mSessionId);
        result.setSessionId(ContactEncryptUtil.encrypt(session.mUserOpenId));
        result.setSessionNick(session.getDisplayName());
        result.setContent(message.mContent);

        switch (message.mMsgType) {
            case WxMessage.MSG_TYPE_TEXT:
                result.setMsgType(WechatMessageV2.MSG_TYPE_TEXT);
                break;

            case WxMessage.MSG_TYPE_VOICE:
                result.setMsgType(WechatMessageV2.MSG_TYPE_VOICE);
                result.setContent("[语音]");

                Bundle voiceInfo = new Bundle();
                voiceInfo.putInt("length", message.mVoiceLength);
                result.setExtraInfo(voiceInfo);
                break;

            case WxMessage.MSG_TYPE_ANIM:
                result.setMsgType(WechatMessageV2.MSG_TYPE_ANIM);
                break;

            case WxMessage.MSG_TYPE_IMG:
                result.setMsgType(WechatMessageV2.MSG_TYPE_IMG);
                break;

            case WxMessage.MSG_TYPE_URL:
                result.setMsgType(WechatMessageV2.MSG_TYPE_URL);
                break;

            case WxMessage.MSG_TYPE_LOCATION:
                result.setMsgType(WechatMessageV2.MSG_TYPE_LOCATION);

                Bundle locInfo = new Bundle();
                locInfo.putDouble("latitude", message.mLatitude);
                locInfo.putDouble("longitude", message.mLongtitude);
                locInfo.putString("address", message.mAddress);
                result.setExtraInfo(locInfo);
                break;

            case WxMessage.MSG_TYPE_FILE:
                result.setMsgType(WechatMessageV2.MSG_TYPE_FILE);

                Bundle fileInfo = new Bundle();
                fileInfo.putString("name", message.mFileName);
                fileInfo.putString("url", message.mFileUrl);
                fileInfo.putLong("size", message.mFileSize);
                result.setExtraInfo(fileInfo);
                break;

            case WxMessage.MSG_TYPE_SYSTEM:
                result.setMsgType(WechatMessageV2.MSG_TYPE_SYSTEM);
                break;
        }

        return result;
    }
}
