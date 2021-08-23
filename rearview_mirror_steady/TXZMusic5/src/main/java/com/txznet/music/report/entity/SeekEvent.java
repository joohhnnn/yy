package com.txznet.music.report.entity;

/**
 * @author zackzhou
 * @date 2019/1/25,17:07
 */

public class SeekEvent extends BaseEvent {

    public int audioSid;
    public long audioId;
    public int albumSid;
    public long albumId;

    public long barOpStartPos;
    public long barOpEndPos;
    public long mediaLength;

    public SeekEvent(int eventId, ReportAudio audio, long spos, long epos, long mediaLengtho) {
        super(eventId);
        this.audioSid = audio.audioSid;
        this.audioId = audio.audioId;
        this.albumSid = audio.albumSid;
        this.albumId = audio.albumId;
        this.svrData = audio.svrData;
        this.barOpStartPos = spos;
        this.barOpEndPos = epos;
        this.mediaLength = mediaLengtho;
    }
}
