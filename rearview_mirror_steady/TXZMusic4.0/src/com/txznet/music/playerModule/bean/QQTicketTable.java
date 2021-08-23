package com.txznet.music.playerModule.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by telenewbie on 2017/11/1.
 */

@Entity
public class QQTicketTable {

    @Id
    public long id;
    public int sid;

    public String url;
    public String hashcode;//不同的url可能有相同的hashcode
    public long iExpTime;//当前连接的超时时间,基准是:服务器时间

    @Generated(hash = 587449795)
    public QQTicketTable(long id, int sid, String url, String hashcode,
                         long iExpTime) {
        this.id = id;
        this.sid = sid;
        this.url = url;
        this.hashcode = hashcode;
        this.iExpTime = iExpTime;
    }

    @Generated(hash = 1324921707)
    public QQTicketTable() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSid() {
        return this.sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHashcode() {
        return this.hashcode;
    }

    public void setHashcode(String hashcode) {
        this.hashcode = hashcode;
    }

    public long getIExpTime() {
        return this.iExpTime;
    }

    public void setIExpTime(long iExpTime) {
        this.iExpTime = iExpTime;
    }

    @Override
    public String toString() {
        return "QQTicketTable{" +
                "id=" + id +
                ", sid=" + sid +
                ", url='" + url + '\'' +
                ", hashcode='" + hashcode + '\'' +
                ", iExpTime=" + iExpTime +
                '}';
    }
}
