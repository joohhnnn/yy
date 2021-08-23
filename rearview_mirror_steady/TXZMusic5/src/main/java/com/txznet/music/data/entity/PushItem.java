package com.txznet.music.data.entity;

import android.arch.persistence.room.Entity;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author telen
 * @date 2018/12/20,11:35
 */
@Entity
public class PushItem extends AudioV5 {

    public static final int STATUS_UNREAD = 0;
    public static final int STATUS_READ = 1;

    @IntDef({
            STATUS_UNREAD,
            STATUS_READ
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {
    }


    /**
     * 消息的状态,{@link #STATUS_UNREAD }{@link #STATUS_READ}
     */
    @Status
    public int status;

    /**
     * 推送下发的时间戳
     */
    public long timestamp;

}
