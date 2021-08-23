package com.txznet.music.data.db.dao;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.SubscribeAlbum;

import java.util.List;

/**
 * 历史专辑数据库操作实现类
 *
 * @author telen
 * @date 2018/12/3,14:24
 */
@Dao
public interface SubscribeAlbumDao {
    @Query("select * from SubscribeAlbum order by timestamp desc")
    List<SubscribeAlbum> listAll();

    @Query("select * from SubscribeAlbum LIMIT 1")
    SubscribeAlbum getFirst();

    //Methods annotated with @Insert can return either void, long, Long, long[], Long[] or List<Long>.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveOrUpdate(SubscribeAlbum subscribeAlbum);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] saveOrUpdate(List<SubscribeAlbum> subscribeAlbum);

    @Query("select * from SubscribeAlbum where id = :id and sid = :sid")
    SubscribeAlbum get(long id, int sid);

    @Delete
    int delete(SubscribeAlbum subscribeAlbum);

    @Delete
    int delete(List<SubscribeAlbum> subscribeAlbum);

    @Query("DELETE FROM SubscribeAlbum")
    int deleteAll();

    @RawQuery(observedEntities = SubscribeAlbum.class)
    List<LocalAudio> invokeBySql(SupportSQLiteQuery sqLiteQuery);
}
