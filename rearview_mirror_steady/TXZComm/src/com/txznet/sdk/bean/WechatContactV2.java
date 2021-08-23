package com.txznet.sdk.bean;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.txznet.sdk.TXZWechatManagerV2;

/**
 * 微信联系人
 * Created by J on 2018/4/8.
 */

public class WechatContactV2 implements Parcelable {
    public static final int CONTACT_TYPE_PEOPLE = 1;
    public static final int CONTACT_TYPE_GROUP = 2;

    private String mId;
    private int mType;
    private String mAvatar;
    private String mNickName;
    private boolean bNotifyMsg;
    // 用于属性扩充的预留字段
    private Bundle mExtraInfo;

    public WechatContactV2() {

    }

    /**
     * 获取联系人id
     *
     * @return 该联系人id
     */
    public String getId() {
        return mId;
    }

    /**
     * 获取联系人类型
     *
     * @return 联系人类型
     * @see WechatContactV2#CONTACT_TYPE_PEOPLE
     * @see WechatContactV2#CONTACT_TYPE_GROUP
     */
    public int getType() {
        return mType;
    }

    /**
     * 获取联系人头像地址
     *
     * 返回的是联系人头像的本地地址, 如果头像还未加载完成可能为空
     * 头像可通过{@link com.txznet.sdk.TXZWechatManagerV2#getUsericon(
     * String, TXZWechatManagerV2.ImageListener)} 加载
     *
     * @return 联系人头像本地地址
     */
    public String getAvatar() {
        return mAvatar;
    }

    /**
     * 获取联系人昵称
     *
     * @return 联系人昵称
     */
    public String getNickName() {
        return mNickName;
    }

    /**
     * 获取联系人屏蔽状态
     *
     * @return 联系人未被屏蔽(启动了消息播报)时返回true
     */
    public boolean notifyMsg() {
        return bNotifyMsg;
    }

    protected WechatContactV2(Parcel in) {
        mId = in.readString();
        mType = in.readInt();
        mAvatar = in.readString();
        mNickName = in.readString();
        bNotifyMsg = in.readByte() != 0;
        mExtraInfo = in.readBundle(getClass().getClassLoader());
    }

    public static final Creator<WechatContactV2> CREATOR = new Creator<WechatContactV2>() {
        @Override
        public WechatContactV2 createFromParcel(Parcel in) {
            return new WechatContactV2(in);
        }

        @Override
        public WechatContactV2[] newArray(int size) {
            return new WechatContactV2[size];
        }
    };

    public void setId(final String id) {
        mId = id;
    }

    public void setType(final int type) {
        mType = type;
    }

    public void setAvatar(final String avatar) {
        mAvatar = avatar;
    }

    public void setNickName(final String nickName) {
        mNickName = nickName;
    }

    public void setNotifyMsg(final boolean notifyMsg) {
        this.bNotifyMsg = notifyMsg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(mId);
        dest.writeInt(mType);
        dest.writeString(mAvatar);
        dest.writeString(mNickName);
        dest.writeByte((byte) (bNotifyMsg ? 1 : 0));
        dest.writeBundle(mExtraInfo);
    }

    @Override
    public String toString() {
        return String.format("WechatContactV2[id = %s, nick = %s]", mId, mNickName);
    }
}
