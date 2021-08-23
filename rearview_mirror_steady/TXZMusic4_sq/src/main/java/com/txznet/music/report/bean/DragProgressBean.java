package com.txznet.music.report.bean;

/**
 * Created by telenewbie on 2018/1/16.
 */

public class DragProgressBean extends EventBase {
    public long startTime;
    public long stopTime;

    public DragProgressBean(String eventId) {
        super(eventId);
    }
}
