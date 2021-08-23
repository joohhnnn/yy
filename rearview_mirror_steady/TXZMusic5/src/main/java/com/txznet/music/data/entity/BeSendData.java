package com.txznet.music.data.entity;

import android.arch.persistence.room.Entity;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 待同步的数据
 * 2018-12-4 20:43:18用于处理同步收藏不及时的问题
 *
 * @author telen
 * @date 2018/12/4,20:42
 */
@Entity(primaryKeys = {"id", "sid"})
public class BeSendData {

    public long id;

    public int sid;

    public String name;

    public String[] artist;

    /**
     * 操作发生的时间
     */
    public long timestamp;
    /**
     * 操作的方式:0(取消收藏)或1(收藏)
     */
    @OperationType
    public int operation;


    public String svrData;

    public static final int FAVOUR = 1;
    public static final int UNFAVOUR = 0;

    @IntDef({
            FAVOUR,
            UNFAVOUR
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OperationType {
    }
}
