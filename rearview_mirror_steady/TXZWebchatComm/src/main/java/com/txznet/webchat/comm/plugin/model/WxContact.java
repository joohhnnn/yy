package com.txznet.webchat.comm.plugin.model;

import android.text.TextUtils;

import com.txznet.comm.util.StringUtils;

import java.util.ArrayList;

/**
 * Created by J on 2016/8/18.
 */
public class WxContact {

    public enum Type {
        /*
         * 系统联系人
         */
        SYSTEM,
        /*
         * 自己
         */
        PEOPLE,
        /*
         * 群或聊天组
         */
        GROUP,
        /*
         * 服务号
         */
        SERVICE,
        /*
         * 讨论组成员
         */
        GROUP_MEMBER
    }

    public enum Sex {
        UNKNOW, MALE, FEMALE
    }

    public static String sLoginUser;
    public Type mType;
    public String mUserOpenId; // 对应json的UserName
    public String mNickName; // 昵称
    public String mRemarkName; // 备注名
    private String mDisplayName; // 显示名称，由程序填充
    private String mRawDisplayName; // 未过滤emoji表情的显示名称, 用于微信内部昵称显示
    public Sex mSex; // 性别
    public boolean mNotifyMsg; // 是否通知消息
    public String mHeadImgUrl; // 头像链接
    public int mMemberCount = 0; // 讨论组成员个数
    public ArrayList<WxContact> mGroupMembers = new ArrayList<>(); // 讨论组成员
    public String mEncryChatroomId;

    public static boolean isGroupOpenId(String id) {
        if (null == id) {
            return false;
        }

        return (id.startsWith("@@") || id.endsWith("@chatroom") || id.endsWith("@talkroom"));
    }

    public String getDisplayName() {
        if (!StringUtils.isEmpty(mDisplayName)) {
            return mDisplayName;
        }

        String raw = getRawDisplayName();
        if (TextUtils.isEmpty(raw)) {
            return "";
        }

        mDisplayName = removeEmoji(raw);
        return mDisplayName;
    }

    public String getRawDisplayName() {
        if (!StringUtils.isEmpty(mRawDisplayName)) {
            return mRawDisplayName;
        }

        if (!TextUtils.isEmpty(mRemarkName))
            mRawDisplayName = unescape(parseEmoji(mRemarkName.trim()));
        else if (!TextUtils.isEmpty(mNickName)) {
            mRawDisplayName = unescape(parseEmoji(mNickName.trim()));
        } else {
            return "";
        }

        return mRawDisplayName;
    }

    public void setDisplayName(String displayName) {
        if (!StringUtils.isEmpty(displayName)) {
            mDisplayName = removeEmoji(unescape(parseEmoji(displayName.trim())));
        }
    }

    private static String unescape(String text) {
        return text
                .replaceAll("&ensp;", "")
                .replaceAll("&emsp;", "")
                .replaceAll("&nbsp;", "")
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">")
                .replaceAll("&amp;", "&");
    }

    private static String parseEmoji(String text) {
        return text.replaceAll("<span class=\"emoji ", "[").replaceAll("\"></span(>)?", "]").replaceAll("<br/>", "");
    }

    private static String removeEmoji(String text) {
        return text.replaceAll("\\[emoji\\w+\\]", "");
    }
}
