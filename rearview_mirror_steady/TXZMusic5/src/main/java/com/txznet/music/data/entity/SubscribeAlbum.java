package com.txznet.music.data.entity;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Entity;

/**
 * @author telen
 * @date 2018/12/4,16:52
 */
@Entity
public class SubscribeAlbum extends Album implements IHaveTimeStamp {
    /**
     * 操作发生的时间(ms)
     */
    public long timestamp;


    /**
     * 更新期数是距离上次我收听的时候他更新了多少
     * 比如上一次我收听的时候更新到了99期，这一次更新到了102期
     * 则更新3期
     * <p>
     * 相对于后台的 "newItemNum"字段
     */
    public long updateNum;
    //delete from favouraudio  where
    public static SupportSQLiteQuery getDeleteSupportSQLiteQuery(long starttime, long endTime) {

        String sb = "delete from " +
                SubscribeAlbum.class.getSimpleName() +
                " where " +
                IHaveTimeStamp.getQueryCondition(starttime, endTime);
        return getSupportSQLiteQuery(sb);
    }

    private static SupportSQLiteQuery getSupportSQLiteQuery(String sql) {
        return new SimpleSQLiteQuery(sql);
    }
}
