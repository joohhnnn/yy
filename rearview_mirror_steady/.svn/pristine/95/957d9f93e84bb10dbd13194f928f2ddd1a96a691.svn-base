package com.txznet.music.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.txznet.music.data.entity.BeSendData;

import java.util.List;

/**
 * 历史专辑数据库操作实现类
 *
 * @author telen
 * @date 2018/12/3,14:24
 */
@Dao
public interface BeSendDataDao {
    @Query("select * from BeSendData")
    List<BeSendData> listAll();

    //Methods annotated with @Insert can return either void, long, Long, long[], Long[] or List<Long>.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveOrUpdate(BeSendData beSendData);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] saveOrUpdate(List<BeSendData> beSendData);

    @Query("select * from BeSendData where sid = :sid and id = :id")
    BeSendData get(long id, int sid);

    @Delete
    int delete(BeSendData beSendData);

    @Delete
    int delete(List<BeSendData> beSendData);

    @Query("DELETE FROM BeSendData")
    int deleteAll();
}
