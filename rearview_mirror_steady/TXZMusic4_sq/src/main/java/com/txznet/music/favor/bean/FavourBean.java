package com.txznet.music.favor.bean;

import com.txznet.music.albumModule.bean.Audio;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

import com.txznet.music.albumModule.bean.DaoSession;
import com.txznet.music.albumModule.bean.AudioDao;

/**
 * 收藏类
 * Created by telenewbie on 2017/11/28.
 */

@Entity(nameInDb = "FavorMusic",
        indexes = {
                @Index(value = "id,sid", unique = true)
        }
)
public class FavourBean {

    public static final int SOURCE_FROM_NET = 1;//来源在线来源的数据
    public static final int SOURCE_FROM_LOCAL = 2;//来源sd卡的数据
    @Id
    String table_id;
    @Unique
    String audioDbId;
    @ToOne(joinProperty = "audioDbId")
    Audio audio;
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
    @Generated(hash = 614425027)
    private transient FavourBeanDao myDao;

    @Generated(hash = 174514578)
    public FavourBean(String table_id, String audioDbId, long timestamp, long id, int sid) {
        this.table_id = table_id;
        this.audioDbId = audioDbId;
        this.timestamp = timestamp;
        this.id = id;
        this.sid = sid;
    }

    @Generated(hash = 1588286521)
    public FavourBean() {
    }

    public FavourBean(Audio audio) {
        this.setId(audio.getId());
        this.setSid(audio.getSid());
        this.setTimestamp(audio.getCreateTime());
        this.setAudio(audio);
    }

    @Generated(hash = 1763480235)
    private transient String audio__resolvedKey;

    public String getTable_id() {
        return sid + "_" + id;
    }

    public void setTable_id(String table_id) {
        this.table_id = table_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FavourBean that = (FavourBean) o;

        return getTable_id() != null ? getTable_id().equals(that.getTable_id()) : that.getTable_id() == null;
    }

    @Override
    public int hashCode() {
        return table_id != null ? table_id.hashCode() : 0;
    }

    public String getAudioDbId() {
        return this.audioDbId;
    }

    public void setAudioDbId(String audioDbId) {
        this.audioDbId = audioDbId;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
    @Generated(hash = 899065015)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFavourBeanDao() : null;
    }

    @Override
    public String toString() {
        return "FavourBean{" +
                "table_id='" + getTable_id() + '\'' +
                ", audioDbId='" + audioDbId + '\'' +
                ", audio=" + getAudio() +
                ", timestamp=" + timestamp +
                ", id=" + id +
                ", sid=" + sid +
                '}';
    }
}
