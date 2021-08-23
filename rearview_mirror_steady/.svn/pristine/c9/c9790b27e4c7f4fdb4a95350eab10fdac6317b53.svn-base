package com.txznet.music.data.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.txznet.music.data.entity.Breakpoint;

import java.util.List;

/**
 * 音频断点记录数据库操作实现类
 *
 * @author zackzhou
 * @date 2018/12/12,14:20
 */

@Dao
public interface BreakpointDao {

    @Query("SELECT * FROM Breakpoint")
    List<Breakpoint> listAll();

    //Methods annotated with @Insert can return either void, long, Long, long[], Long[] or List<Long>.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveOrUpdate(Breakpoint breakpoint);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] saveOrUpdate(List<Breakpoint> breakpointList);

    @Query("SELECT * FROM Breakpoint where id = :id and sid = :sid")
    Breakpoint findByAudio(long id, int sid);

    @Query("SELECT * FROM Breakpoint where albumId = :albumId")
    List<Breakpoint> findByAlbum(String albumId);

    //    @Query("SELECT * FROM Breakpoint WHERE albumId = '3-19092426' LIMIT (SELECT count(*)-1 FROM Breakpoint WHERE albumId = '3-19092426'), 1")
    @Query("SELECT * FROM Breakpoint WHERE albumId = :albumId ORDER BY rowid DESC LIMIT 1")
    Breakpoint getLastestByAlbum(String albumId);

    @Delete
    int delete(Breakpoint breakpoint);

    @Query("DELETE FROM Breakpoint")
    int deleteAll();
}
