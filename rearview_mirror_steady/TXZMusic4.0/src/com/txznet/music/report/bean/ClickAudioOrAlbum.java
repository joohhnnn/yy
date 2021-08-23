package com.txznet.music.report.bean;

public class ClickAudioOrAlbum extends EventBase {

    public int type;
    public int audioSid;
    public long audioId;
    public int albumSid;
    public long albumId;
    public String name;


    public ClickAudioOrAlbum(String eventId) {
        super(eventId);
    }
}
