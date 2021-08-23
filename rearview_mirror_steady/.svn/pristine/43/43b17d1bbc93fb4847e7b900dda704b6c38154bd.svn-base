package com.txznet.music.data.entity;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Entity;

/**
 * 收藏音乐
 *
 * @author telen
 * @date 2018/12/4,16:51
 */
@Entity
public class FavourAudio extends AudioV5 implements IHaveTimeStamp {
    public long timestamp;

    //delete from favouraudio  where  and sid !=0 and sid !=24
    public static SupportSQLiteQuery getDeleteSupportSQLiteQuery(long starttime, long endTime) {

        String sb = "delete from " +
                FavourAudio.class.getSimpleName() +
                " where " +
                IHaveTimeStamp.getQueryCondition(starttime, endTime) +
                " and sid !=0" +
                " and sid !=24";
        return getSupportSQLiteQuery(sb);
    }

    //select *  from favouraudio  where
    public static SupportSQLiteQuery getSelectSupportSQLiteQuery(long starttime, long endTime) {

        String sb = "select * from " +
                FavourAudio.class.getSimpleName() +
                " where " +
                IHaveTimeStamp.getQueryCondition(starttime, endTime);
        return getSupportSQLiteQuery(sb);
    }

    private static SupportSQLiteQuery getSupportSQLiteQuery(String sql) {
        return new SimpleSQLiteQuery(sql);
    }
}
