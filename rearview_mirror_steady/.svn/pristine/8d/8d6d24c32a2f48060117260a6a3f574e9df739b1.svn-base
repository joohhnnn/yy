package com.txznet.music.report.entity;

/**
 * 点击事件
 *
 * @author zackzhou
 * @date 2019/1/22,10:37
 */

public class AlbumWithParentItemEvent extends BaseEvent {

    public long categoryId;
    public int albumSid;
    public long albumId;

    public AlbumWithParentItemEvent(int eventId, long categoryId, ReportAlbum album) {
        super(eventId);
        this.categoryId = categoryId;
        albumSid = album.albumSid;
        albumId = album.albumId;
    }
}
