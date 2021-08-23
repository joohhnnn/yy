package com.txznet.music.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.txznet.music.data.entity.BlackListAudio;

import java.util.List;

@Dao
public interface BlackListAudioDao {

    @Query("SELECT COUNT(*) FROM blacklistaudio")
    int getCount();

    @Query("SELECT * FROM blacklistaudio")
    List<BlackListAudio> listAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveOrUpdate(BlackListAudio... audios);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveOrUpdate(List<BlackListAudio> audioList);

    @Query("SELECT * FROM blacklistaudio WHERE  id == :id AND sid = :sid")
    BlackListAudio get(long id, int sid);

    @Delete
    void delete(BlackListAudio... blacklistaudio);

    @Delete
    void delete(List<BlackListAudio> blacklistaudio);

    @Query("DELETE FROM blacklistaudio")
    void deleteAll();
}
