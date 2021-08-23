package com.txznet.music.report.entity;

/**
 * 点击事件
 *
 * @author zackzhou
 * @date 2019/1/22,10:37
 */

public class AudioItemEvent extends BaseEvent {

    public int audioSid;
    public long audioId;
    public int albumSid;
    public long albumId;

    public AudioItemEvent(int eventId, ReportAudio audio) {
        super(eventId);
        audioSid = audio.audioSid;
        audioId = audio.audioId;
        albumSid = audio.albumSid;
        albumId = audio.albumId;
        svrData = audio.svrData;
    }
}
