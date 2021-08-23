package com.txznet.music.albumModule.logic.net.request;

public class ReqWST {

    public transient final int TYPE_RECOMMAND = 0;
    public transient final int TYPE_SUBSCRIBE = 1;

    private int limit;
    private int up;//0代表下拉刷新，1代表上拉加载
    private long albumId;
    private int type;//0：推荐，1：订阅

    public ReqWST() {

    }

    public ReqWST(int limit, int up, long albumId) {
        this.limit = limit;
        this.up = up;
        this.albumId = albumId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }


    public int getLimit() {
        return limit;
    }

    public int getUp() {
        return up;
    }

    public long getAlbumId() {
        return albumId;
    }
}
