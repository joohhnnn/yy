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

    private int playEndCount;

    private long id;

    private int sid;

    private long index;

    private String audioDbId;

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

    @Generated(hash = 660316404)
    public BreakpointAudio(String dbId, int breakpoint, long duration, int playEndCount,
            long id, int sid, long index, String audioDbId) {
        this.dbId = dbId;
        this.breakpoint = breakpoint;
        this.duration = duration;
        this.playEndCount = playEndCount;
        this.id = id;
        this.sid = sid;
        this.index = index;
        this.audioDbId = audioDbId;
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1991053655)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getBreakpointAudioDao() : null;
    }

}
