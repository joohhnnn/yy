package com.txznet.launcher.module.wechat.bean;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.WechatMessageV2;

/**
 * Created by ASUS User on 2018/3/23.
 * 和微信交互的数据类
 */

public class WechatMsgData {
    public String mMsgId;
    public int mMsgType;
    public String mContent;
    public String mSessionId;
    public String mSessionNick;
    public String mSenderId;
    public String mSenderNick;
    public double mLatitude;
    public double mLongitude;
    public String mAddress;

    public WechatMsgData(WechatMessageV2 message) {
        mMsgId = message.getMsgId();
        mMsgType = message.getMsgType();
        mContent = message.getContent();
        mSessionId = message.getSessionId();
        mSessionNick = message.getSessionNick();
        mSenderId = message.getSenderId();
        mSenderNick = message.getSenderNick();
        if (mMsgType == WechatMessageV2.MSG_TYPE_LOCATION) {
            mLatitude = message.getExtraInfo().getDouble("latitude");
            mLongitude = message.getExtraInfo().getDouble("longitude");
            mAddress = message.getExtraInfo().getString("address");
        }
    }

    public WechatMsgData() {

    }

    @Override
    public String toString() {
        return toJsonString();
    }

    public String toJsonString() {
        return new JSONBuilder()
                .put("mMsgId", mMsgId)
                .put("mMsgType", mMsgType)
                .put("mContent", mContent)
                .put("mSessionId", mSessionId)
                .put("mSessionNick", mSessionNick)
                .put("mSenderId", mSenderId)
                .put("mSenderNick", mSenderNick)
                .put("mLatitude", mLatitude)
                .put("mLongitude", mLongitude)
                .put("mAddress", mAddress)
                .toString();
    }

    public void parseFromJsonString(String data) {
        JSONBuilder jsonBuilder = new JSONBuilder(data);
        mMsgId = jsonBuilder.getVal("mMsgId", String.class);
        mMsgType = jsonBuilder.getVal("mMsgType", Integer.class, WechatMessageV2.MSG_TYPE_TEXT);
        mContent = jsonBuilder.getVal("mContent", String.class);
        mSessionId = jsonBuilder.getVal("mSessionId", String.class);
        mSessionNick = jsonBuilder.getVal("mSessionNick", String.class);
        mSenderId = jsonBuilder.getVal("mSenderId", String.class);
        mSenderNick = jsonBuilder.getVal("mSenderNick", String.class);
        mLatitude = jsonBuilder.getVal("mLatitude", Double.class, 0.0);
        mLongitude = jsonBuilder.getVal("mLongitude", Double.class, 0.0);
        mAddress = jsonBuilder.getVal("mAddress", String.class);
    }
}
