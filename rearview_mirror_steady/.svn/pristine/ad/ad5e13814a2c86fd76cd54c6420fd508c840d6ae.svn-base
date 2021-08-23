package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 本地扫描
 *
 * @author zackzhou
 * @date 2019/1/20,15:15
 */

public class LocalScanEvent extends BaseEvent {

    public static final int EXIT_TYPE_NORMAL = 0;
    public static final int EXIT_TYPE_MANUAL = 1;

    public long costTime; // 扫描花费时间
    public int audioNum; // 扫描得到的歌曲数

    @IntDef({
            EXIT_TYPE_NORMAL,
            EXIT_TYPE_MANUAL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExitType {
    }

    @ExitType
    public int exitType;

    public LocalScanEvent(int eventId, long costTime, int audioNum, @ExitType int exitType) {
        super(eventId);
        this.costTime = costTime;
        this.audioNum = audioNum;
        this.exitType = exitType;
    }
}
