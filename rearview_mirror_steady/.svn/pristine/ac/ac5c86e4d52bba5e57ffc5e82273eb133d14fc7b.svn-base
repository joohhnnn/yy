package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 推荐位点击事件
 *
 * @author zackzhou
 * @date 2019/1/20,14:09
 */

public class SysPageAlbumItemClickEvent extends SysPageItemClickEvent {

    /**
     * 推荐
     */
    public static final int PAGE_TYPE_RECOMMEND = 1000100;
    /**
     * 音乐精选
     */
    public static final int PAGE_TYPE_MUSIC_CHOICE = 1000200;
    /**
     * 音乐分类
     */
    public static final int PAGE_TYPE_MUSIC_CATEGORY = 1000300;
    /**
     * 电台精选
     */
    public static final int PAGE_TYPE_RADIO_CHOICE = 1000400;
    /**
     * 电台分类
     */
    public static final int PAGE_TYPE_RADIO_CATEGORY = 1000500;


    @IntDef({
            PAGE_TYPE_RECOMMEND,
            PAGE_TYPE_MUSIC_CHOICE,
            PAGE_TYPE_MUSIC_CATEGORY,
            PAGE_TYPE_RADIO_CHOICE,
            PAGE_TYPE_RADIO_CATEGORY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PageType {
    }


    public long albumId;
    public int albumSid;

    public SysPageAlbumItemClickEvent(@PageType int pageType, int posId, ReportAlbum album) {
        super(pageType, posId);
        this.albumId = album.albumId;
        this.albumSid = album.albumSid;
    }
}
