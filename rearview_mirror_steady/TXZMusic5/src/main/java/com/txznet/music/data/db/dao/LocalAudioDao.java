package com.txznet.music.data.db.dao;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.RawQuery;

import com.txznet.music.data.entity.LocalAudio;

import java.util.List;

@Dao
public interface LocalAudioDao {

    @Query("SELECT COUNT(*) FROM localaudio")
    int getCount();

    @Query("SELECT * FROM localaudio")
    List<LocalAudio> listAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveOrUpdate(LocalAudio... audios);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveOrUpdate(List<LocalAudio> audioList);

    @Query("SELECT * FROM localaudio WHERE  id == :id AND sid = :sid")
    LocalAudio get(long id, int sid);

    @RawQuery(observedEntities = LocalAudio.class)
    List<LocalAudio> findBySql(SupportSQLiteQuery sqLiteQuery);


    @Delete
    void delete(LocalAudio... localAudios);

    @Delete
    void delete(List<LocalAudio> localAudios);

    @Query("DELETE FROM localaudio")
    void deleteAll();
}
