package com.txznet.music.favor.bean;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;

/**
 * Created by telenewbie on 2017/12/8.
 */

public class ResponseFavourBean {
    private long operTime;   //收藏时间
    private Album album;
    private Audio audio;

    public long getOperTime() {
        return operTime;
    }

    public void setOperTime(long operTime) {
        this.operTime = operTime;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public Audio getAudio() {
        return audio;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }

    @Override
    public String toString() {
        return "ResponseFavourBean{" +
                "operTime=" + operTime +
                ", album=" + album +
                ", audio=" + audio +
                '}';
    }
}
