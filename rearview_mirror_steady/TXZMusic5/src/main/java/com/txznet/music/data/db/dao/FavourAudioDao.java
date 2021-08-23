package com.txznet.music.data.db.dao;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import com.txznet.music.data.entity.FavourAudio;

import java.util.List;

/**
 * 历史专辑数据库操作实现类
 *
 * @author telen
 * @date 2018/12/3,14:24
 */
@Dao
public interface FavourAudioDao {
    @Query("select * from FavourAudio order  by timestamp desc")
    List<FavourAudio> listAll();

    //Methods annotated with @Insert can return either void, long, Long, long[], Long[] or List<Long>.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveOrUpdate(FavourAudio favourAudio);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] saveOrUpdate(List<FavourAudio> favourAudio);

    @Query("select * from FavourAudio where id = :id and sid = :sid")
    FavourAudio get(long id, int sid);

    @Delete
    int delete(FavourAudio favourAudio);

    @Delete
    int delete(FavourAudio... favourAudios);

    @Delete
    int delete(List<FavourAudio> favourAudioList);


    @RawQuery(observedEntities = FavourAudio.class)
    List<FavourAudio> invokeBySql(SupportSQLiteQuery sqLiteQuery);

    @Query("DELETE FROM FavourAudio")
    int deleteAll();
}
