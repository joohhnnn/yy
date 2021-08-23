package com.txznet.music.albumModule.bean;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.Locale;

/**
 * Created by brainBear on 2017/9/12.
 */
@Entity
public class BreakpointAudio {
    @Id
    private String dbId;

    private int breakpoint;

    private long duration;

    private int playEndCount;//1,表示收听过.2+表示收听次数,在车主FM的开发中增加这个逻辑,判断是否收听过的逻辑判断是否>0,判断是否需要走车主FM的跳歌逻辑==1

    private long id;

    private int sid;

    private long index;

    private String audioDbId;

    private String albumId;//专辑底下唯一(表示,某一个音频,在自己所属的专辑里面拥有断点的能力).因为车主FM的需求是,轮询播放的时候,下次播放的时候,不从断点处开始播放,而是重新开始.而如果播放过一轮则下一轮记为都没有播放过的样子
    //思路,这里如果开启新的一轮,则把该专辑所属的断点全部删除(这里的albumId为Album的id,而非Audio的albumid,因为车主FM的id和底下的audio的albumId不一致)


    @ToOne(joinProperty = "audioDbId")
    private Audio audio;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 2129732536)
    private transient BreakpointAudioDao myDao;
    @Generated(hash = 1763480235)
    private transient String audio__resolvedKey;


    @Generated(hash = 1801222066)
    public BreakpointAudio(String dbId, int breakpoint, long duration, int playEndCount, long id, int sid, long index,
                           String audioDbId, String albumId) {
        this.dbId = dbId;
        this.breakpoint = breakpoint;
        this.duration = duration;
        this.playEndCount = playEndCount;
        this.id = id;
        this.sid = sid;
        this.index = index;
        this.audioDbId = audioDbId;
        this.albumId = albumId;
    }

    @Generated(hash = 1387745737)
    public BreakpointAudio() {
    }


    public String getDbId() {
        return String.format(Locale.getDefault(), "%d-%d", sid, id);
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public int getBreakpoint() {
        return this.breakpoint;
    }

    public void setBreakpoint(int breakpoint) {
        this.breakpoint = breakpoint;
    }

    public long getIndex() {
        return this.index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public String getAudioDbId() {
        return this.audioDbId;
    }

    public void setAudioDbId(String audioDbId) {
        this.audioDbId = audioDbId;
    }

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 2111734578)
    public Audio getAudio() {
        String __key = this.audioDbId;
        if (audio__resolvedKey == null || audio__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AudioDao targetDao = daoSession.getAudioDao();
            Audio audioNew = targetDao.load(__key);
            synchronized (this) {
                audio = audioNew;
                audio__resolvedKey = __key;
            }
        }
        return audio;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1434286603)
    public void setAudio(Audio audio) {
        synchronized (this) {
            this.audio = audio;
            audioDbId = audio == null ? null : audio.getAudioDbId();
            audio__resolvedKey = audioDbId;
        }
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
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

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    public int getPlayEndCount() {
        return this.playEndCount;
    }

    public void setPlayEndCount(int playEndCount) {
        this.playEndCount = playEndCount;
    }

    public String getAlbumId() {
        return this.albumId;
    }

    public void setAlbumId(long albumId, int albumSid) {
        this.albumId = albumSid + "-" + albumId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BreakpointAudio that = (BreakpointAudio) o;

        return getDbId().equals(that.getDbId());
    }

    @Override
    public int hashCode() {
        return getDbId().hashCode();
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    @Override
    public String toString() {
        return "BreakpointAudio{" +
                "dbId='" + dbId + '\'' +
                ", breakpoint=" + breakpoint +
                ", duration=" + duration +
                ", playEndCount=" + playEndCount +
                ", id=" + id +
                ", sid=" + sid +
                ", albumId='" + albumId + '\'' +
                ", audio=" + getAudio() +
                '}';
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1991053655)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBreakpointAudioDao() : null;
    }
}
