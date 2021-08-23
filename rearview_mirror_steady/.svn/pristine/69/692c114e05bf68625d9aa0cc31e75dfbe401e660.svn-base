package com.txznet.webchat.comm.plugin.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Keep;

import java.util.Date;

/**
 * 本地用户缓存Entity
 * 用于数据库操作
 * Created by J on 2017/3/29.
 */
@Entity
public class WxUserCacheEntity {
    @Id(autoincrement = true)
    private Long id;
    @Index(unique = true)
    private Long uin; // uin
    private String host; // 用户对应的微信服务器
    private String userNick; // 用户昵称
    private String userAvatar; // 用户头像
    private String cookie; // http cookie

    private boolean valid; // 缓存有效性
    private int hitCount; // 缓存使用次数
    private Date lastHit; // 最后使用时间

    @Generated(hash = 2135018408)
    public WxUserCacheEntity(Long id, Long uin, String host, String userNick,
            String userAvatar, String cookie, boolean valid, int hitCount, Date lastHit) {
        this.id = id;
        this.uin = uin;
        this.host = host;
        this.userNick = userNick;
        this.userAvatar = userAvatar;
        this.cookie = cookie;
        this.valid = valid;
        this.hitCount = hitCount;
        this.lastHit = lastHit;
    }

    @Keep
    public WxUserCacheEntity() {
        clear();
    }

    public void clear() {
        // 所有数据赋默认值
        this.uin = 0L;
        this.userNick = "";
        this.userAvatar = "";
        this.cookie = "";
        this.valid = true;
        this.hitCount = 0;
        this.lastHit = new Date();
        this.host = "wx.qq.com";
    }

    public Long getUin() {
        return this.uin;
    }

    public void setUin(Long uin) {
        this.uin = uin;
    }

    public String getUserNick() {
        return this.userNick;
    }

    public void setUserNick(String userNick) {
        this.userNick = userNick;
    }

    public String getUserAvatar() {
        return this.userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getCookie() {
        return this.cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public boolean getValid() {
        return this.valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getHitCount() {
        return this.hitCount;
    }

    public void setHitCount(int hitCount) {
        this.hitCount = hitCount;
    }

    public Date getLastHit() {
        return this.lastHit;
    }

    public void setLastHit(Date lastHit) {
        this.lastHit = lastHit;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
