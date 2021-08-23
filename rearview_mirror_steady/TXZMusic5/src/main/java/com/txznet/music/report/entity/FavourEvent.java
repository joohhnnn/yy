package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 收藏/取消收藏
 *
 * @author zackzhou
 * @date 2019/1/21,20:40
 */

public class FavourEvent extends BaseEvent {

    /**
     * 取消收藏
     */
    public static final int OP_TYPE_UN_FAVOUR = 0;
    /**
     * 收藏
     */
    public static final int OP_TYPE_FAVOUR = 1;

    @IntDef({
            OP_TYPE_UN_FAVOUR,
            OP_TYPE_FAVOUR
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OpType {
    }

    public long audioId;
    public int audioSid;
    public long albumId;
    public int albumSid;

    @OpType
    public int opType;


    public FavourEvent(int eventId, ReportAudio audio, @OpType int opType) {
        super(eventId);
        this.audioId = audio.audioId;
        this.audioSid = audio.audioSid;
        this.albumId = audio.albumId;
        this.albumSid = audio.albumSid;
        this.svrData = audio.svrData;
        this.opType = opType;
    }
}
