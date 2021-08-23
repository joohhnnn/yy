package com.txznet.music.report.bean;

/**
 * Created by brainBear on 2018/1/8.
 */
public class ClickAudio extends EventBase {
    public int sid;
    public long audioId;
    public String name;
    public int isInsert;

    public ClickAudio(String eventId) {
        super(eventId);
    }
}
