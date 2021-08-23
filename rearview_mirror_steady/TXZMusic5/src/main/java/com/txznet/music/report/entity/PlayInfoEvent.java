package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author zackzhou
 * @date 2019/1/25,19:58
 */

public class PlayInfoEvent extends BaseEvent {

    public static final int ONLINE_TYPE_ONLINE = 1;
    public static final int ONLINE_TYPE_OFFLINE = 0;

    @IntDef({
            ONLINE_TYPE_ONLINE,
            ONLINE_TYPE_OFFLINE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OnlineType {
    }

    public static final int EXIT_TYPE_AUTO = 0;
    public static final int EXIT_TYPE_MANUAL = 1;
    public static final int EXIT_TYPE_SOUND = 2;
    public static final int EXIT_TYPE_CLOSE = 3;
    public static final int EXIT_TYPE_AUDIO_FOCUS = 4;
    public static final int EXIT_TYPE_OTHER = 5;

    @IntDef({
            EXIT_TYPE_AUTO,
            EXIT_TYPE_MANUAL,
            EXIT_TYPE_SOUND,
            EXIT_TYPE_CLOSE,
            EXIT_TYPE_AUDIO_FOCUS,
            EXIT_TYPE_OTHER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExitType {
    }

    public static final int MANUAL_TYPE_AUTO = 0;
    public static final int MANUAL_TYPE_MANUAL = 1;

    @IntDef({
            MANUAL_TYPE_AUTO,
            MANUAL_TYPE_MANUAL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ManualType {
    }


    public int audioSid;
    public long audioId;
    public int albumSid;
    public long albumId;
    public int manual;
    public int online;
    public long mediaLength;
    public long playLength;
    public int exitType;

    public PlayInfoEvent(int eventId, ReportAudio audio, @ManualType int manual, @OnlineType int online, long mediaLen, long playLen, @ExitType int exitType) {
        super(eventId);
        audioSid = audio.audioSid;
        audioId = audio.audioId;
        albumSid = audio.albumSid;
        albumId = audio.albumId;
        svrData = audio.svrData;
        this.manual = manual;
        this.online = online;
        this.mediaLength = mediaLen;
        this.playLength = playLen;
        this.exitType = exitType;
    }
}
