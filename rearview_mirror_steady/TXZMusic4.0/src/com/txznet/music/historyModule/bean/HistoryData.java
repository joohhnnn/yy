package com.txznet.music.historyModule.bean;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import com.txznet.music.albumModule.bean.DaoSession;
import com.txznet.music.albumModule.bean.AlbumDao;
import com.txznet.music.albumModule.bean.AudioDao;

/**
 * Created by brainBear on 2018/1/17.
 */
@Entity(
        indexes = {
                @Index(value = "id,sid", unique = true)
        }
)
public class HistoryData {

    public final static int TYPE_AUDIO = 1;
    public final static int TYPE_ALBUM = 2;

    @Id(autoincrement = true)
    private Long index;

    private String audioRowId;

    private String albumRowId;
    @ToOne(joinProperty = "audioRowId")
    private Audio audio;
    @ToOne(joinProperty = "albumRowId")
    private Album album;

    private long id;

    private int sid;

    private int type;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 408544123)
    private transient HistoryDataDao myDao;

    @Generated(hash = 718743823)
    public HistoryData(Long index, String audioRowId, String albumRowId, long id,
                       int sid, int type) {
        this.index = index;
        this.audioRowId = audioRowId;
        this.albumRowId = albumRowId;
        this.id = id;
        this.sid = sid;
        this.type = type;
    }

    @Generated(hash = 422767273)
    public HistoryData() {
    }

    public Long getIndex() {
        return this.index;
    }

    public void setIndex(Long index) {
        this.index = index;
    }

    public String getAudioRowId() {
        return this.audioRowId;
    }

    public void setAudioRowId(String audioRowId) {
        this.audioRowId = audioRowId;
    }

    public String getAlbumRowId() {
        return this.albumRowId;
    }

    public void setAlbumRowId(String albumRowId) {
        this.albumRowId = albumRowId;
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

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Generated(hash = 1763480235)
    private transient String audio__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 829157214)
    public Audio getAudio() {
        String __key = this.audioRowId;
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
    @Generated(hash = 644858249)
    public void setAudio(Audio audio) {
        synchronized (this) {
            this.audio = audio;
            audioRowId = audio == null ? null : audio.getAudioDbId();
            audio__resolvedKey = audioRowId;
        }
    }

    @Generated(hash = 1470384725)
    private transient String album__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 535030363)
    public Album getAlbum() {
        String __key = this.albumRowId;
        if (album__resolvedKey == null || album__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            AlbumDao targetDao = daoSession.getAlbumDao();
            Album albumNew = targetDao.load(__key);
            synchronized (this) {
                album = albumNew;
                album__resolvedKey = __key;
            }
        }
        return album;
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 414047069)
    public void setAlbum(Album album) {
        synchronized (this) {
            this.album = album;
            albumRowId = album == null ? null : album.getAlbumDbId();
            album__resolvedKey = albumRowId;
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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 407103950)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getHistoryDataDao() : null;
    }

    @Override
    public String toString() {
        return "HistoryData{" +
                ", id=" + id +
                ", sid=" + sid +
                ", type=" + type +
                "audio=" + getAudio() +
                ", album=" + getAlbum() +
                '}';
    }
}
