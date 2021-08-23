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

public class OpEvent extends BaseEvent {

    /**
     * 开启
     */
    public static final int OP_TYPE_ENABLE = 1;
    /**
     * 关闭
     */
    public static final int OP_TYPE_DISABLE = 0;

    @IntDef({
            OP_TYPE_ENABLE,
            OP_TYPE_DISABLE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OpType {
    }

    @OpType
    public int opType;

    public OpEvent(int eventId, @OpType int opType) {
        super(eventId);
        this.opType = opType;
    }
}
