package com.txznet.music.message;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.dao.AlbumConverter;
import com.txznet.music.dao.AudioListConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by brainBear on 2017/11/30.
 */
@Entity
public class Message {

    public static final int TYPE_AUDIO = 0;
    public static final int TYPE_ALBUM = 1;

    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;

    private long time;
    private String title;
    private int type;

    @Convert(columnType = String.class, converter = AudioListConverter.class)
    private List<Audio> audios;

    @Convert(columnType = String.class, converter = AlbumConverter.class)
    private Album album;
    private int status;
    private long id;
    private long sid;

    @Id(autoincrement = true)
    private Long dbId;

    @Generated(hash = 1593022765)
    public Message(long time, String title, int type, List<Audio> audios,
            Album album, int status, long id, long sid, Long dbId) {
        this.time = time;
        this.title = title;
        this.type = type;
        this.audios = audios;
        this.album = album;
        this.status = status;
        this.id = id;
        this.sid = sid;
        this.dbId = dbId;
    }

    @Generated(hash = 637306882)
    public Message() {
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<Audio> getAudios() {
        return this.audios;
    }

    public void setAudios(List<Audio> audios) {
        this.audios = audios;
    }

    public Album getAlbum() {
        return this.album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSid() {
        return this.sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public Long getDbId() {
        return this.dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

}
