package com.txznet.webchat.comm.plugin.model;

import android.os.Bundle;

/**
 * 微信消息Entity
 *
 * 注意：不要修改此文件!!!!!
 * 出于性能考虑, 微信插件与客户端同时引用了此类, 为了做版本兼容此文件的字段
 *
 * Created by J on 2016/8/24.
 */
public final class WxMessage {
    public static final int MSG_TYPE_TEXT = 1; //文本消息
    public static final int MSG_TYPE_URL = 2; //URL消息
    public static final int MSG_TYPE_IMG = 3; //图片消息
    public static final int MSG_TYPE_VOICE = 4; //语音消息
    public static final int MSG_TYPE_LOCATION = 5; // 位置消息
    public static final int MSG_TYPE_ANIM = 6; // 动画消息
    public static final int MSG_TYPE_FILE = 7; // 文件消息
    public static final int MSG_TYPE_SYSTEM = 1000; // 系统消息

    public String mSessionId; // 来源会话的ID
    public String mSenderUserId; // 发送者ID
    public int mMsgType; // 消息类型
    /**
     * 对应微信消息的ClientMsgId/LocalId, 微信客户端中采用此字段唯一标识一条微信消息
     */
    public long mMsgId;
    /**
     * 对应微信消息的MsgId, 服务端对消息进行唯一标识的字段
     * 因为存在本地发送消息的情况, 发送时填入的msgId为客户端生成, 服务端发送成功后会为消息再指定一个MsgId. 撤回
     * 消息时需要同时提供两个id, 所以以此字段对服务端的msdId进行记录.
     * 对于客户端收到的消息, mServerMsgId和mMsgId应该是同一个值, 对于本地发送的消息, 两个字段可能为不同的值.
     * <p>
     * Note: 此字段不保证一定有值, 微信客户端中不能以此字段对微信消息进行标识
     */
    public long mServerMsgId;
    public String mContent; // 结构化的消息文本，用于ui显示

    // 位置消息字段
    public double mLatitude; // 纬度
    public double mLongtitude; // 精度
    public String mAddress; // 位置文本

    // 语音消息url
    public String mVoiceUrl; // 语音Url
    public String mVoiceCachePath; // 语音文件本地路径
    public int mVoiceLength; // 语音长度

    // 图片消息
    public String mImgUrl; // 图片url
    public String mImgCachePath; // 图片本地路径

    // 文件消息
    public String mFileName; // 文件名
    public long mFileSize; // 文件大小(字节为单位)
    public String mFilePath; // 文件本地路径
    public String mFileUrl; // 文件下载url

    /**
     * 额外信息字段, 为了与插件做版本兼容
     */
    public Bundle mExtraInfo;
}
