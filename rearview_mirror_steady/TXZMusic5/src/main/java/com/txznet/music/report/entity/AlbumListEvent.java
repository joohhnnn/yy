package com.txznet.music.report.entity;

import java.util.List;

/**
 * 删除管理
 *
 * @author zackzhou
 * @date 2019/1/20,15:20
 */

public class AlbumListEvent extends BaseEvent {

    public List<ReportAlbum> albumList;

    public AlbumListEvent(int eventId, List<ReportAlbum> albumList) {
        super(eventId);
        this.albumList = albumList;
    }

}
