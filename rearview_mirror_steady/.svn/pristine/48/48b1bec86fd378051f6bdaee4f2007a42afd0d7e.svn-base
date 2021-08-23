package com.txznet.music.favor.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

/**
 * Created by telenewbie on 2017/11/29.
 * 等待发送的列表
 */
@Entity(
        indexes = {
                @Index(value = "id,sid", unique = true)
        }
)
public class BeSendBean {

    public static final int FAVOUR = 1;
    public static final int UNFAVOUR = 0;

    @Id
    String table_id;

    long id;

    int sid;


    long timestamp;//时间
    int operation;//操作的方式:0(取消收藏)或1(收藏)

    @Generated(hash = 828270301)
    public BeSendBean(String table_id, long id, int sid, long timestamp,
                      int operation) {
        this.table_id = table_id;
        this.id = id;
        this.sid = sid;
        this.timestamp = timestamp;
        this.operation = operation;
    }

    @Generated(hash = 1285790667)
    public BeSendBean() {
    }

    @Override
    public String toString() {
        return "BeSendBean{" +
                "id=" + id +
                ", sid=" + sid +
                ", timestamp=" + timestamp +
                ", operation=" + operation +
                '}';
    }

    public String getTable_id() {
        return sid + "_" + id;
    }

    public void setTable_id(String table_id) {
        this.table_id = table_id;
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

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getOperation() {
        return this.operation;
    }

    public void setOperation(int operation) {
        this.operation = operation;
    }
}
