package com.txznet.record.bean;

/**
 * Created by ASUS User on 2015/8/18.
 */
public class WxContact {
    public String openid;
    public String nick;

    public WxContact(){

    }

    public WxContact(String openid, String nick) {
        this.openid = openid;
        this.nick = nick;
    }
}
