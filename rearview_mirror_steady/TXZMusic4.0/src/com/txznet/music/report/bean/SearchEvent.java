package com.txznet.music.report.bean;

/**
 * Created by telen on 2018/5/10.
 */

public class SearchEvent extends EventBase {
    public static final int CHOICE_AUTO = 1;
    public static final int CHOICE_USER = 2;
    public int audioSid;
    public long audioId;

    public int albumSid;
    public long albumId;
    public String name;
    public String json;
    public int type;//选择的项的原因：自动选择 1  ，用户选择 2

    public SearchEvent(String eventId) {
        super(eventId);
    }


}
