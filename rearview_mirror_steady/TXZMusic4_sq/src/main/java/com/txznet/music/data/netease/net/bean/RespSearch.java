package com.txznet.music.data.netease.net.bean;

/**
 * Created by telenewbie on 2018/2/8.
 */

public class RespSearch {

    /**
     * data : {"mediaList":[{"name":"1","type":"track","albumName":"RANDOM","albumId":44464,"albumArtistId":14598,"albumArtistName":"冈本光市","coverUrl":"http://p1.music.126.net/jWdcbq8MxwEm9-rbdwBGZQ==/6040716883333198.jpg","mvId":0,"duration":5120,"canPlay":true,"publishTime":1104508800000,"id":"4C5E5BD45D1CA170B179A0365093862F"}],"totalCount":1500}
     * hasMore : true
     * code : 200
     */

    private DataBean data;
    private boolean hasMore;
    private int code;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


    @Override
    public String toString() {
        return "RespSearch{" +
                "data=" + data +
                ", hasMore=" + hasMore +
                ", code=" + code +
                '}';
    }
}
