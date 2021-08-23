package com.txznet.sdk.tongting;

public class TongTingPlayItem {
    String title;
    String logo;
    String source;
    String artists;
    String albumName;// 专辑名称
    int favourFlag;//收藏订阅的状态，详看TongtingUtils
    int playState;//播放的状态

    int sid;
    long id;


    public TongTingPlayItem(int sid, long id, String title, String logo, String source, String artists, String albumName, int favourFlag, int playState) {
        this.id = id;
        this.sid = sid;
        this.favourFlag = favourFlag;
        this.title = title;
        this.logo = logo;
        this.source = source;
        this.artists = artists;
        this.albumName = albumName;
        this.playState = playState;
    }

    @Override
    public String toString() {
        return "PlayItem{" +
                "title='" + title + '\'' +
                ", logo='" + logo + '\'' +
                ", source='" + source + '\'' +
                ", artists='" + artists + '\'' +
                ", albumName='" + albumName + '\'' +
                ", favourFlag=" + favourFlag +
                ", playState=" + playState +
                ", sid=" + sid +
                ", id=" + id +
                '}';
    }

    public String getTitle() {
        return title;
    }

    public String getLogo() {
        return logo;
    }

    public String getSource() {
        return source;
    }

    public String getArtists() {
        return artists;
    }

    public String getAlbumName() {
        return albumName;
    }

    public int getFavourFlag() {
        return favourFlag;
    }

    public void setFavourFlag(int favourFlag) {
        this.favourFlag = favourFlag;
    }

    public int getSid() {
        return sid;
    }

    public long getId() {
        return id;
    }

    public int getPlayState() {
        return playState;
    }

    public void setPlayState(int playState) {
        this.playState = playState;
    }
}
