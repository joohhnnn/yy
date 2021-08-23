package com.txznet.music.data.kaola.net.bean;

/**
 * Created by telenewbie on 2018/2/5.
 */

public class RespActive {

    private String openid;

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }

    @Override
    public String toString() {
        return "RespActive{" +
                "openid='" + openid + '\'' +
                '}';
    }
}
