package com.txznet.music.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.txznet.music.data.entity.HistoryAudio;

import java.util.List;

/**
 * 历史音乐
 *
 * @author telen
 * @date 2018/12/3,14:24
 */
@Dao
public interface HistoryAudioDao extends BaseDao<HistoryAudio, String> {
    @Override
    @Query("select * from HistoryAudio")
    List<HistoryAudio> listAll();

    //Methods annotated with @Insert can return either void, long, Long, long[], Long[] or List<Long>.
    @Override
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveOrUpdate(HistoryAudio historyAudio);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveOrUpdate(List<HistoryAudio> historyAudioList);

    @Override
    @Query("select * from HistoryAudio where id = :id")
    HistoryAudio get(String id);

    @Query("SELECT COUNT(*) from HistoryAudio")
    int getCount();

    @Override
    @Delete
    int delete(HistoryAudio historyAudio);

    @Delete
    int delete(List<HistoryAudio> historyAudio);

    @Override
    @Query("DELETE FROM historyaudio")
    int deleteAll();
}
