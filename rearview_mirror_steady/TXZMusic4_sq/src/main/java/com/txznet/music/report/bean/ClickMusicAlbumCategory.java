package com.txznet.music.report.bean;

import java.util.List;

/**
 * Created by brainBear on 2018/1/8.
 */
public class ClickMusicAlbumCategory extends EventBase {
    public long type;
    public long albumId;
    public int position;
    public List<Integer> exposureIds;//æåçID

    public ClickMusicAlbumCategory(String eventId) {
        super(eventId);
    }
}
