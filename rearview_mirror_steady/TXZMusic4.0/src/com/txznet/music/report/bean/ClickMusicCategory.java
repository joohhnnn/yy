package com.txznet.music.report.bean;

/**
 * Created by brainBear on 2018/1/8.
 */
public class ClickMusicCategory extends EventBase {
    public long categoryId;

    public ClickMusicCategory(String eventId) {
        super(eventId);
    }
}
