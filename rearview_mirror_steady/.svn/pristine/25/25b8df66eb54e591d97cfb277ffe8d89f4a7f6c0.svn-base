package com.txznet.music.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.txznet.music.data.entity.HistoryAlbum;

import java.util.List;

/**
 * 历史专辑数据库操作实现类
 *
 * @author telen
 * @date 2018/12/3,14:24
 */
@Dao
public interface HistoryAlbumDao extends BaseDao<HistoryAlbum, String> {
    @Override
    @Query("select * from HistoryAlbum")
    List<HistoryAlbum> listAll();

    @Query("select * from HistoryAlbum where flag = 0")
    List<HistoryAlbum> listAllNotHidden();

    //Methods annotated with @Insert can return either void, long, Long, long[], Long[] or List<Long>.
    @Override
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveOrUpdate(HistoryAlbum historyAlbum);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveOrUpdate(List<HistoryAlbum> historyAlbumList);

    @Override
    @Query("select * from HistoryAlbum where id = :id")
    HistoryAlbum get(String id);

    @Query("select count(*) from HistoryAlbum")
    int getCount();

    @Query("select count(*) from HistoryAlbum where flag = 0")
    int getNotHiddenCount();

    @Override
    @Delete
    int delete(HistoryAlbum historyAlbum);

    @Delete
    int delete(List<HistoryAlbum> historyAlbums);

    @Query("select * from HistoryAlbum where id = :albumId AND sid=:albumSid")
    HistoryAlbum findByAlbum(int albumSid, long albumId);

    @Override
    @Query("DELETE FROM HistoryAlbum")
    int deleteAll();
}
