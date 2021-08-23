package com.txznet.music.data.netease.net.bean;

/**
 * Created by telenewbie on 2018/2/8.
 */

public class NeteaseAudio {
    /**
     * name : 1
     * type : track
     * albumName : RANDOM
     * albumId : 44464
     * albumArtistId : 14598
     * albumArtistName : 冈本光市
     * coverUrl : http://p1.music.126.net/jWdcbq8MxwEm9-rbdwBGZQ==/6040716883333198.jpg
     * mvId : 0
     * duration : 5120
     * canPlay : true
     * publishTime : 1104508800000
     * id : 4C5E5BD45D1CA170B179A0365093862F
     */

    private String name;
    private String type;
    private String albumName;
    private int albumId;
    private int albumArtistId;
    private String albumArtistName;
    private String coverUrl;
    private int mvId;
    private int duration;
    private boolean canPlay;
    private long publishTime;
    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public int getAlbumArtistId() {
        return albumArtistId;
    }

    public void setAlbumArtistId(int albumArtistId) {
        this.albumArtistId = albumArtistId;
    }

    public String getAlbumArtistName() {
        return albumArtistName;
    }

    public void setAlbumArtistName(String albumArtistName) {
        this.albumArtistName = albumArtistName;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getMvId() {
        return mvId;
    }

    public void setMvId(int mvId) {
        this.mvId = mvId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isCanPlay() {
        return canPlay;
    }

    public void setCanPlay(boolean canPlay) {
        this.canPlay = canPlay;
    }

    public long getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public String toString() {
        return "NeteaseAudio{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", albumName='" + albumName + '\'' +
                ", albumId=" + albumId +
                ", albumArtistId=" + albumArtistId +
                ", albumArtistName='" + albumArtistName + '\'' +
                ", coverUrl='" + coverUrl + '\'' +
                ", mvId=" + mvId +
                ", duration=" + duration +
                ", canPlay=" + canPlay +
                ", publishTime=" + publishTime +
                ", id='" + id + '\'' +
                '}';
    }
}

