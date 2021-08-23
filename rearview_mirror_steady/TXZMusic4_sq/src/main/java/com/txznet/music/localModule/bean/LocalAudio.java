package com.txznet.music.localModule.bean;

import com.txznet.music.albumModule.bean.Audio;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;
import com.txznet.music.albumModule.bean.DaoSession;
import com.txznet.music.albumModule.bean.AudioDao;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

/**
 * 本地音乐
 * 
 * @author telenewbie
 * @version 创建时间：2016年3月25日 下午6:15:24
 * 
 */
@Entity(
        indexes = {
                @Index(value = "id,sid", unique = true)
        }
)
public class LocalAudio {

    @Id(autoincrement = true)
    private Long dbId;

    private long id;

    private long sid;

    private String audioDbId;

    @ToOne(joinProperty = "audioDbId")
    private Audio audio;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 827465216)
    private transient LocalAudioDao myDao;

    @Generated(hash = 303646734)
    public LocalAudio(Long dbId, long id, long sid, String audioDbId) {
        this.dbId = dbId;
        this.id = id;
        this.sid = sid;
        this.audioDbId = audioDbId;
    }

    @Generated(hash = 86067968)
    public LocalAudio() {
    }

    public Long getDbId() {
        return this.dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public String getAudioDbId() {
        return this.audioDbId;
    }

    public void setAudioDbId(String audioDbId) {
        this.audioDbId = audioDbId;
    }

    @Generated(hash = 1763480235)
    private transient String audio__resolvedKey;

    /** To-one relationship, resolved on first access. */
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

    /** called by internal mechanisms, do not call yourself. */
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

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 969958859)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getLocalAudioDao() : null;
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

}
