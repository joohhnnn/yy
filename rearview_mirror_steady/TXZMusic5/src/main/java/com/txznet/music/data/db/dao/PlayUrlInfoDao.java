package com.txznet.music.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.txznet.music.data.entity.PlayUrlInfo;

import java.util.List;

@Dao
public interface PlayUrlInfoDao {

    @Query("SELECT * FROM playurlinfo")
    List<PlayUrlInfo> listAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveOrUpdate(PlayUrlInfo... playUrlInfos);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveOrUpdate(List<PlayUrlInfo> playUrlInfoList);

    @Query("SELECT * FROM playurlinfo WHERE  sid == :sid and audioId = :id")
    PlayUrlInfo findBySidAndId(int sid, long id);

    @Delete
    void delete(PlayUrlInfo... playUrlInfos);

    @Delete
    void delete(List<PlayUrlInfo> playUrlInfos);

    @Query("DELETE FROM playurlinfo")
    void deleteAll();
}
