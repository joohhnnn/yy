package com.txznet.music.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.txznet.music.data.entity.PlayListData;

/**
 * 播放列表记录的数据库操作
 *
 * @author zackzhou
 * @date 2018/12/28,14:36
 */
@Dao
public interface PlayListDataDao {

    @Query("SELECT * FROM PlayListData LIMIT 1")
    PlayListData getPlayListData();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveOrUpdate(PlayListData listData);

    @Query("DELETE FROM PlayListData")
    void delete();
}
