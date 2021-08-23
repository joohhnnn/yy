package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 音频切换
 *
 * @author zackzhou
 * @date 2019/1/25,16:41
 */

public class AudioSwitchEvent extends BaseEvent {

    /**
     * 上一首
     */
    public static final int OP_TYPE_PREV = 0;

    /**
     * 下一首
     */
    public static final int OP_TYPE_NEXT = 1;

    @IntDef({
            OP_TYPE_PREV,
            OP_TYPE_NEXT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OpType {
    }

    /**
     * 音频长度
     */
    public long mediaLength;

    /**
     * 播放时长
     */
    public long playLength;

    public int audioSid;
    public long audioId;
    public int albumSid;
    public long albumId;

    @OpType
    public int opType;

    public AudioSwitchEvent(int eventId, ReportAudio audio, @OpType int opType) {
        super(eventId);
        this.audioSid = audio.audioSid;
        this.audioId = audio.audioId;
        this.albumSid = audio.albumSid;
        this.albumId = audio.albumId;
        this.svrData = audio.svrData;
        this.opType = opType;
    }
}
