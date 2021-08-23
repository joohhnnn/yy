package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 订阅/取消订阅
 *
 * @author zackzhou
 * @date 2019/1/21,20:40
 */

public class SubscribeEvent extends BaseEvent {

    /**
     * 取消收藏
     */
    public static final int OP_TYPE_UN_SUBSCRIBE = 0;
    /**
     * 收藏
     */
    public static final int OP_TYPE_SUBSCRIBE = 1;

    @IntDef({
            OP_TYPE_UN_SUBSCRIBE,
            OP_TYPE_SUBSCRIBE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OpType {
    }

    public long albumId;
    public int albumSid;

    @OpType
    public int opType;


    public SubscribeEvent(int eventId, ReportAlbum album, @OpType int opType) {
        super(eventId);
        this.albumId = album.albumId;
        this.albumSid = album.albumSid;
        this.opType = opType;
    }
}
