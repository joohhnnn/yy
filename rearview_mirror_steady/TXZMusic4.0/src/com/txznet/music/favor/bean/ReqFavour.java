package com.txznet.music.favor.bean;

/**
 * Created by telenewbie on 2017/11/28.
 * 请求收藏
 */

public class ReqFavour {
    public final static int AUDIO_TYPE = 1; //音乐， 收藏
    public final static int ALBUM_TYPE = 2;  //专辑，订阅


    private int storeType;  //1.
    private int sid;
    private long id;    //音频id, 或专辑id， 0：从头开始
    private int count;  //拉取数据(默认十条)
    private long operTime;//请求的时间戳(第一次为0,其他的时候先是本地最后一条的时间戳)


    public int getStoreType() {
        return storeType;
    }

    public void setStoreType(int storeType) {
        this.storeType = storeType;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getOperTime() {
        return operTime;
    }

    public void setOperTime(long operTime) {
        this.operTime = operTime;
    }

    @Override
    public String toString() {
        return "ReqFavour{" +
                "storeType=" + storeType +
                ", sid=" + sid +
                ", id=" + id +
                ", count=" + count +
                ", operTime=" + operTime +
                '}';
    }
}
