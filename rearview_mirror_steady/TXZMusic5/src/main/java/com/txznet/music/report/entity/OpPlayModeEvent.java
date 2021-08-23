package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 开关事件
 *
 * @author zackzhou
 * @date 2019/1/22,17:21
 */

public class OpPlayModeEvent extends BaseEvent {

    /**
     * 列表循环
     */
    public static final int OP_TYPE_QUEUE_LOOP = 0;
    /**
     * 随机播放
     */
    public static final int OP_TYPE_RANDOM_PLAY = 1;
    /**
     * 单曲循环
     */
    public static final int OP_TYPE_SINGLE_LOOP = 2;

    @IntDef({
            OP_TYPE_QUEUE_LOOP,
            OP_TYPE_RANDOM_PLAY,
            OP_TYPE_SINGLE_LOOP
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OpType {
    }

    @OpType
    public int opType;

    public OpPlayModeEvent(int eventId, @OpType int opType) {
        super(eventId);
        this.opType = opType;
    }
}
