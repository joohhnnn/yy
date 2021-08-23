package com.txznet.webchat.comm.plugin.model;

/**
 * 微信登录信息
 * Created by J on 2017/3/20.
 */
public class WxUserCache {
    // 用户信息
    private String mUserNick; // 用户昵称
    private String mUin; // 用户uin
    private String mUserHead; // 用户头像

    private int mHitCount; // 使用次数

    public WxUserCache(String uin, String userHead, String userNick, int hitCount) {
        this.mUin = uin;
        this.mUserHead = userHead;
        this.mUserNick = userNick;
        this.mHitCount = hitCount;
    }

    public String getUserNick() {
        return mUserNick;
    }

    public void setUserNick(String userNick) {
        this.mUserNick = userNick;
    }

    public String getUin() {
        return mUin;
    }

    public void setUin(String uin) {
        this.mUin = uin;
    }

    public String getUserHead() {
        return mUserHead;
    }

    public void setUserHead(String userHead) {
        this.mUserHead = userHead;
    }

    public int getHitCount() {
        return mHitCount;
    }

    public void setHitCount(int hitCount) {
        this.mHitCount = hitCount;
    }

}
