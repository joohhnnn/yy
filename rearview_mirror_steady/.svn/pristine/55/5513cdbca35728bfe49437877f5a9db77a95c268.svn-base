package com.txznet.sdk.bean;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 微信消息
 * Created by J on 2018/4/8.
 */

public class WechatMessageV2 implements Parcelable {
    public static final int MSG_TYPE_TEXT = 1; //文本消息
    public static final int MSG_TYPE_URL = 2; //URL消息
    public static final int MSG_TYPE_IMG = 3; //图片消息
    public static final int MSG_TYPE_VOICE = 4; //语音消息
    public static final int MSG_TYPE_LOCATION = 5; // 位置消息
    public static final int MSG_TYPE_ANIM = 6; // 动画消息
    public static final int MSG_TYPE_FILE = 7; // 文件消息
    public static final int MSG_TYPE_SYSTEM = 1000; // 系统消息

    private String mMsgId;
    private int mMsgType;
    private String mContent;
    private String mSessionId;
    private String mSessionNick;
    private String mSenderId;
    private String mSenderNick;

    private Bundle mExtraInfo;

    public WechatMessageV2() {

    }

    /**
     * 获取消息id
     *
     * @return 消息id
     */
    public String getMsgId() {
        return mMsgId;
    }

    public void setMsgId(final String msgId) {
        mMsgId = msgId;
    }

    /**
     * 获取消息类型
     *
     * @return 消息类型
     * @see WechatMessageV2#MSG_TYPE_TEXT
     * @see WechatMessageV2#MSG_TYPE_URL
     * @see WechatMessageV2#MSG_TYPE_IMG
     * @see WechatMessageV2#MSG_TYPE_VOICE
     * @see WechatMessageV2#MSG_TYPE_LOCATION
     * @see WechatMessageV2#MSG_TYPE_ANIM
     * @see WechatMessageV2#MSG_TYPE_FILE
     * @see WechatMessageV2#MSG_TYPE_SYSTEM
     */
    public int getMsgType() {
        return mMsgType;
    }

    /**
     * 获取消息文本
     *
     * 对应车载微信界面中显示的消息文本, 仅供参考, 根据消息类型不同可能文本需要显示时进行特殊处理
     *
     * @return 消息文本
     */
    public String getContent() {
        return mContent;
    }

    /**
     * 获取消息所在会话id
     *
     * @return 会话id
     */
    public String getSessionId() {
        return mSessionId;
    }

    /**
     * 获取消息所在会话昵称
     *
     * @return 会话昵称
     */
    public String getSessionNick() {
        return mSessionNick;
    }

    /**
     * 获取消息发送者id
     *
     * @return 发送者id
     */
    public String getSenderId() {
        return mSenderId;
    }

    /**
     * 获取消息发送者昵称
     *
     * @return 发送者昵称
     */
    public String getSenderNick() {
        return mSenderNick;
    }

    /**
     * 获取消息额外信息
     *
     * 针对某些特殊消息类型, 提供一些额外信息供显示, 具体信息可从返回的bundle中获取, 额外信息可参考下面的说明:
     *
     * 位置消息
     * {@link WechatMessageV2#MSG_TYPE_LOCATION}:
     * latitude(double) 纬度
     * longitude(double) 经度
     * address(String) 地址文本
     *
     * 文件消息
     * {@link WechatMessageV2#MSG_TYPE_FILE}:
     * name(String) 文件名
     * url(String) 文件url
     * size(long) 文件大小(Byte)
     *
     * @return 该消息包含的额外信息
     */
    public Bundle getExtraInfo() {
        return mExtraInfo;
    }

    public void setMsgType(final int msgType) {
        mMsgType = msgType;
    }

    public void setContent(final String content) {
        mContent = content;
    }

    public void setSessionId(final String sessionId) {
        mSessionId = sessionId;
    }

    public void setSessionNick(final String sessionNick) {
        mSessionNick = sessionNick;
    }

    public void setSenderId(final String senderId) {
        mSenderId = senderId;
    }

    public void setSenderNick(final String senderNick) {
        mSenderNick = senderNick;
    }

    public void setExtraInfo(final Bundle extraInfo) {
        mExtraInfo = extraInfo;
    }

    protected WechatMessageV2(Parcel in) {
        mMsgId = in.readString();
        mMsgType = in.readInt();
        mContent = in.readString();
        mSessionId = in.readString();
        mSessionNick = in.readString();
        mSenderId = in.readString();
        mSenderNick = in.readString();
        mExtraInfo = in.readBundle(getClass().getClassLoader());
    }

    public static final Creator<WechatMessageV2> CREATOR = new Creator<WechatMessageV2>() {
        @Override
        public WechatMessageV2 createFromParcel(Parcel in) {
            return new WechatMessageV2(in);
        }

        @Override
        public WechatMessageV2[] newArray(int size) {
            return new WechatMessageV2[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(mMsgId);
        dest.writeInt(mMsgType);
        dest.writeString(mContent);
        dest.writeString(mSessionId);
        dest.writeString(mSessionNick);
        dest.writeString(mSenderId);
        dest.writeString(mSenderNick);
        dest.writeBundle(mExtraInfo);
    }

    @Override
    public String toString() {
        return String.format("WechatMessageV2[id = %s, type = %s, content = %s, extra = %s]",
                mMsgId, mMsgType, mContent, extraInfoToString());
    }

    private String extraInfoToString() {
        StringBuilder builder = new StringBuilder("{ ");

        if (null != mExtraInfo) {
            for (String key : mExtraInfo.keySet()) {
                builder.append(key + " = " + mExtraInfo.get(key) + ",");
            }
        }

        builder.deleteCharAt(builder.length() - 1);

        builder.append(" }");
        return builder.toString();
    }
}
