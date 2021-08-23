package com.txznet.music.baseModule.bean;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.AlbumDao;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.bean.AudioDao;
import com.txznet.music.albumModule.bean.DaoSession;
import com.txznet.music.dao.String2AudiosConverter;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;

import java.util.List;

/**
 * 用于存放播放列表的数据，用于下次恢复的时候，从上次指定位置开始，继续播放
 * 该表只保存一条记录
 */
@Entity
public class PlayListData {
    private String albumDbId;
    @ToOne(joinProperty = "albumDbId")
    public Album album;

    private String audioDbId;
    @ToOne(joinProperty = "audioDbId")
    private Audio audio;
    @Convert(columnType = String.class, converter = String2AudiosConverter.class)
    private List<Audio> audioStr;//播放列表，使用json进行保存

    private int dataOri;//播放的来源

    @Id
    private long index;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1706349819)
    private transient PlayListDataDao myDao;

    @Generated(hash = 1406462139)
    public PlayListData(String albumDbId, String audioDbId, List<Audio> audioStr, int dataOri,
                        long index) {
        this.albumDbId = albumDbId;
        this.audioDbId = audioDbId;
        this.audioStr = audioStr;
        this.dataOri = dataOri;
        this.index = index;
    }

    @Generated(hash = 361575438)
    public PlayListData() {
    }

    public String getAlbumDbId() {
        return this.albumDbId;
    }

    public void setAlbumDbId(String albumDbId) {
        this.albumDbId = albumDbId;
    }

    public String getAudioDbId() {
        return this.audioDbId;
    }

    public void setAudioDbId(String audioDbId) {
        this.audioDbId = audioDbId;
    }

    public List<Audio> getAudioStr() {
        return this.audioStr;
    }

    public void setAudioStr(List<Audio> audioStr) {
        this.audioStr = audioStr;
    }

    @Generated(hash = 1470384725)
    private transient String album__resolvedKey;

    /**
     * To-one relationship, resolved on first access.
     */
    @Generated(hash = 1057805319)
    public Album getAlbum() {
        String __key = this.albumDbId;
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
    @Generated(hash = 623279884)
    public void setAlbum(Album album) {
        synchronized (this) {
            this.album = album;
            albumDbId = album == null ? null : album.getAlbumDbId();
            album__resolvedKey = albumDbId;
        }
    }

    @Generated(hash = 1763480235)
    private transient String audio__resolvedKey;

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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1124721654)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPlayListDataDao() : null;
    }

    public long getIndex() {
        return this.index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public int getDataOri() {
        return this.dataOri;
    }

    public void setDataOri(int dataOri) {
        this.dataOri = dataOri;
    }

    @Override
    public String toString() {
        return "PlayListData{" +
                "audioDbId='" + audioDbId + '\'' +
                ", dataOri=" + dataOri +
                ", index=" + index +
                ", audio=" + getAudio() +
                ",album=" + getAlbum() +
                '}';
    }
}
