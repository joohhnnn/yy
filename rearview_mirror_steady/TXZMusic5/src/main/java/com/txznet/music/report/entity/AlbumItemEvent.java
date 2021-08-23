package com.txznet.music.report.entity;

/**
 * 点击事件
 *
 * @author zackzhou
 * @date 2019/1/22,10:37
 */

public class AlbumItemEvent extends BaseEvent {

    public int albumSid;
    public long albumId;

    public AlbumItemEvent(int eventId, ReportAlbum album) {
        super(eventId);
        albumSid = album.albumSid;
        albumId = album.albumId;
    }
}
