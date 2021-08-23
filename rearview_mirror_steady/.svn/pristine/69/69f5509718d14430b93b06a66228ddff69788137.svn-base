package com.txznet.music.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.txznet.music.data.entity.PushItem;

import java.util.List;

/**
 * @author telen
 * @date 2018/12/22,10:45
 */
@Dao
public interface PushItemDao extends BaseDao<PushItem, String> {

    // FIXME: 2018/12/22 这个是怎么做到@Query可以做到关联的?好奇怪,有时间看一下是怎么做到的吧.
    @Override
    @Query("select * from PushItem order by timestamp desc")
    List<PushItem> listAll();

    //Methods annotated with @Insert can return either void, long, Long, long[], Long[] or List<Long>.
    @Override
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveOrUpdate(PushItem pushItem);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] saveOrUpdate(List<PushItem> pushItem);

    @Override
    @Query("select * from PushItem where id = :id")
    PushItem get(String id);

    @Query("select * from PushItem where id = :id and sid = :sid")
    PushItem findBySidAndId(int sid, long id);

    @Override
    @Delete
    int delete(PushItem pushItem);

    @Delete
    int delete(List<PushItem> pushItems);

    @Override
    @Query("DELETE FROM PushItem")
    int deleteAll();
}
