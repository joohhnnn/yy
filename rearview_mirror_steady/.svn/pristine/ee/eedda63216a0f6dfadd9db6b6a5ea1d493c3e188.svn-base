package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 榜单查看
 *
 * @author zackzhou
 * @date 2019/1/22,20:09
 */

public class BillBoardContentClickEvent extends BaseEvent {

    /**
     * 播放按钮
     */
    public static final int CLICK_POS_PLAY_BTN = 0;
    /**
     * 列表中的一个
     */
    public static final int CLICK_POS_LIST = 1;
    /**
     * 二级界面
     */
    public static final int CLICK_POS_LIST_SECONDARY = 2;

    @IntDef({
            CLICK_POS_PLAY_BTN,
            CLICK_POS_LIST,
            CLICK_POS_LIST_SECONDARY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ClickPos {
    }

    @ClickPos
    public int clickPos;

    public int albumSid;
    public long albumId;

    public BillBoardContentClickEvent(int eventId, ReportAlbum album, @ClickPos int clickPos) {
        super(eventId);
        this.clickPos = clickPos;
        this.albumSid = album.albumSid;
        this.albumId = album.albumId;
    }
}
