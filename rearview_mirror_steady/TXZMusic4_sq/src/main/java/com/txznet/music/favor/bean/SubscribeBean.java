package com.txznet.music.favor.bean;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.AlbumDao;
import com.txznet.music.albumModule.bean.DaoSession;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Locale;

/**
 * Created by telenewbie on 2017/11/28.
 * 订阅类
 */

@Entity(
        indexes = {
                @Index(value = "id,sid", unique = true)
        }
)
public class SubscribeBean {
    @Id
    String table_id;
    @Unique
    String albumDbId;
    @ToOne(joinProperty = "albumDbId")
    Album album;
    long timestamp;//收藏的时间
    private long id;
    private int sid;
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1770963529)
    private transient SubscribeBeanDao myDao;
    @Generated(hash = 1470384725)
    private transient String album__resolvedKey;

    @Generated(hash = 518790211)
    public SubscribeBean(String table_id, String albumDbId, long timestamp, long id,
                         int sid) {
        this.table_id = table_id;
        this.albumDbId = albumDbId;
        this.timestamp = timestamp;
        this.id = id;
        this.sid = sid;
    }

    @Generated(hash = 781367487)
    public SubscribeBean() {
    }

    public SubscribeBean(Album album) {
        this.setId(album.getId());
        this.setSid(album.getSid());
        this.setAlbumDbId(String.format(Locale.getDefault(), "%d-%d", album.getSid(), album.getId()));
        this.setAlbum(album);
        this.setTimestamp(getTimestamp());
    }

    public String getTable_id() {
        return sid + "_" + id;
    }

    public void setTable_id(String table_id) {
        this.table_id = table_id;
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

    public String getAlbumDbId() {
        return this.albumDbId;
    }

    public void setAlbumDbId(String albumDbId) {
        this.albumDbId = albumDbId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

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
    @Generated(hash = 387561221)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getSubscribeBeanDao() : null;
    }

    @Override
    public String toString() {
        return "SubscribeBean{" +
                "table_id='" + table_id + '\'' +
                ", albumDbId='" + albumDbId + '\'' +
                ", album=" + getAlbum() +
                ", timestamp=" + timestamp +
                ", id=" + id +
                ", sid=" + sid +
                '}';
    }
}
