package com.txznet.webchat.stores;

import android.os.Bundle;
import android.text.TextUtils;

import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.actions.ContactActionCreator;
import com.txznet.webchat.actions.ResourceActionCreator;
import com.txznet.webchat.actions.TtsActionCreator;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.log.L;
import com.txznet.webchat.sdk.WxSDKManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息Store
 * Created by J on 2016/8/25.
 */
public class WxMessageStore extends Store {
    public static final String EVENT_TYPE_ALL = "wechat_message_store";
    private static final String LOG_TAG = "WxMessageStore";
    private static final WxMessageStore sInstance = new WxMessageStore(Dispatcher.get());

    // 消息map
    private final Map<String, List<WxMessage>> mMessageMap = new HashMap<>();
    // 未读消息数map
    private Map<String, Integer> mUnreadCountMap = new HashMap<>();
    // 当前会话界面打开的对话id
    private String mCurrentOpenedSession = "";

    private WxMessage mLastSendMessage = null;

    private WxMessageStore(Dispatcher dispatcher) {
        super(dispatcher);
    }


    @Override
    public void onDispatch(Action action) {
        boolean changed = false;

        switch (action.getType()) {
            case ActionType.WX_PLUGIN_LOGIC_RESET:
                reset();
                break;

            case ActionType.WX_PLUGIN_MSG_ADD_MSG:
                changed = doAddMessage((WxMessage) action.getData());
                break;

            case ActionType.WX_OPEN_CHAT:
                changed = doSessionOpen((Bundle) action.getData());
                break;

            case ActionType.WX_CLOSE_CHAT:
                changed = doSessionClose((Bundle) action.getData());
                break;

            case ActionType.WX_DOWNLOAD_VOICE_RESP:
                changed = doVoiceDownloadResp((WxMessage) action.getData());
                break;

            case ActionType.WX_PLUGIN_REVOKE_MSG_SUCCESS:
                changed = doRevokeMsgSuccess((WxMessage) action.getData());
                break;

            case ActionType.WX_PLUGIN_REVOKE_MSG_FAILED:
                changed = doRevokeMsgFailed((WxMessage) action.getData());

            case ActionType.WX_SEND_MSG_REQ:
                WxSDKManager.getInstance().notifySendMessageStart((WxMessage) action.getData());
                break;

            case ActionType.WX_SEND_MSG_RESP:
                WxSDKManager.getInstance().notifySendMessageResult((WxMessage) action.getData(),
                        true);
                changed = doAddMessageLocal((WxMessage) action.getData());
                break;

            case ActionType.WX_SEND_MSG_RESP_ERROR:
                WxSDKManager.getInstance().notifySendMessageResult((WxMessage) action.getData(),
                        false);
                break;
        }

        if (changed) {
            emitChange(EVENT_TYPE_ALL);
        }
    }

    public static WxMessageStore getInstance() {
        return sInstance;
    }

    private void reset() {
        mMessageMap.clear();
        mUnreadCountMap.clear();
        mCurrentOpenedSession = "";
    }

    public List<WxMessage> getMessageList(String sessionId) {
        List<WxMessage> msgList = mMessageMap.get(sessionId);

        return msgList;
    }

    public WxMessage getMessage(String msgId, String sessionId) {
        List<WxMessage> list = getMessageList(sessionId);

        long id = Long.valueOf(msgId);
        if (null != list) {
            for (int i = 0; i < list.size(); i++) {
                WxMessage msg = list.get(i);
                if (msg.mMsgId == id) {
                    return msg;
                }
            }
        }

        return null;
    }

    public int getUnreadMsgCount(String openId) {
        Integer count = mUnreadCountMap.get(openId);

        if (null == count) {
            return 0;
        }

        return count;
    }

    /**
     * 获取最后一条发送的消息
     *
     * @return
     */
    public WxMessage getLastSendMessage() {
        return mLastSendMessage;
    }

    private boolean doAddMessage(WxMessage message) {
        if (null == message) {
            L.e(LOG_TAG, "doAddMessage::message is null");
            return false;
        }

        if (TextUtils.isEmpty(message.mSessionId)) {
            L.e(LOG_TAG, "doAddMessage::sessionId is null");
            return false;
        }

        if (null == WxContactStore.getInstance().getContact(message.mSessionId)) {
            L.e(LOG_TAG, "doAddMessage::can't find specific session in ContactStore");
            return false;
        }

        List<WxMessage> msgList;

        if (!message.mSenderUserId.equals(
                WxContactStore.getInstance().getLoginUser().mUserOpenId)) {
            ContactActionCreator.get().topSession(message.mSessionId);
        }

        // 判断是否需要初始化对应的message list
        msgList = mMessageMap.get(message.mSessionId);
        if (msgList == null) {
            msgList = new ArrayList<WxMessage>();
            mMessageMap.put(message.mSessionId, msgList);
        }

        // 添加消息
        msgList.add(message);

        // 自己发送的消息不需要播报, 也不需要设置未读提示
        if (!TextUtils.isEmpty(message.mSenderUserId)
                && !message.mSenderUserId.equals(
                WxContactStore.getInstance().getLoginUser().mUserOpenId)) {
            TtsActionCreator.get().addTts(message);
            TtsActionCreator.get().procTtsQueue();

            // 非当前打开的会话, 设置未读提示
            if (!mCurrentOpenedSession.equals(message.mSessionId)) {
                increaseUnreadCount(message.mSessionId);
            }
        }

        // 自己发送的消息如果是语音消息, 提前进行语音文件下载, 用于消息重播
        if (WxMessage.MSG_TYPE_VOICE == message.mMsgType) {
            ResourceActionCreator.get().downloadVoice(message);
        }

        return true;
    }

    private boolean doAddMessageLocal(WxMessage message) {
        if (null == message) {
            return false;
        }

        List<WxMessage> msgList;

        // 判断是否需要初始化对应的message list
        msgList = mMessageMap.get(message.mSessionId);
        if (msgList == null) {
            msgList = new ArrayList<WxMessage>();
            mMessageMap.put(message.mSessionId, msgList);
        }

        // 添加消息
        msgList.add(message);
        mLastSendMessage = message;

        return true;
    }

    private boolean doVoiceDownloadResp(WxMessage message) {
        List<WxMessage> sessionList = mMessageMap.get(message.mSessionId);

        if (null == sessionList) {
            return false;
        }

        for (int i = 0, len = sessionList.size(); i < len; i++) {
            WxMessage msg = sessionList.get(i);
            if (msg.mMsgId == message.mMsgId) {
                msg.mVoiceCachePath = message.mVoiceCachePath;
            }
        }

        return false;
    }

    private void increaseUnreadCount(String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }

        Integer rawCount = mUnreadCountMap.get(sessionId);

        if (null == rawCount) {
            mUnreadCountMap.put(sessionId, 1);
        } else {
            mUnreadCountMap.put(sessionId, rawCount + 1);
        }
    }

    private boolean doSessionOpen(Bundle data) {
        String uid = data.getString("uid");
        mCurrentOpenedSession = uid;
        return resetUnreadCount(uid);
    }

    private boolean doSessionClose(Bundle data) {
        mCurrentOpenedSession = "";
        return false;
    }

    private boolean resetUnreadCount(String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return false;
        }

        Integer currentUnreadCount = mUnreadCountMap.get(sessionId);
        if (null != currentUnreadCount && currentUnreadCount != 0) {
            mUnreadCountMap.put(sessionId, 0);
            return true;
        }

        return false;
    }

    private boolean doRevokeMsgSuccess(WxMessage msg) {
        List<WxMessage> sessionList = mMessageMap.get(msg.mSessionId);

        if (null == sessionList) {
            return false;
        }

        for (int i = sessionList.size() - 1; i >= 0; i--) {
            WxMessage message = sessionList.get(i);

            if (message.mMsgId == msg.mMsgId) {
                sessionList.remove(message);
                return true;
            }
        }

        return false;
    }

    private boolean doRevokeMsgFailed(WxMessage msg) {
        return false;
    }

}
