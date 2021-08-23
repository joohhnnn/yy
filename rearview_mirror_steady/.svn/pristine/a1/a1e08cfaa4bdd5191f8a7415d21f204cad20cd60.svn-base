package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * tab滑动报点
 *
 * @author zackzhou
 * @date 2018/12/27,15:37
 */

public class SysPageSlideEvent extends BaseEvent {

    public static final int PAGE_ID_RECOMMEND_FIRST = 10;
    public static final int PAGE_ID_RECOMMEND_SECOND = 11;
    public static final int PAGE_ID_MUSIC_CHOICE = 20;
    public static final int PAGE_ID_MUSIC_CATEGORY = 21;
    public static final int PAGE_ID_RADIO_CHOICE = 30;
    public static final int PAGE_ID_RADIO_CATEGORY = 31;

    @IntDef({
            PAGE_ID_RECOMMEND_FIRST,
            PAGE_ID_RECOMMEND_SECOND,
            PAGE_ID_MUSIC_CHOICE,
            PAGE_ID_MUSIC_CATEGORY,
            PAGE_ID_RADIO_CHOICE,
            PAGE_ID_RADIO_CATEGORY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PageId {
    }

    public int curTabId; // 当前tab编号
    public int preTabId; // 前一个tab编号

    public SysPageSlideEvent(int eventId, @PageId int curTabId, @PageId int preTabId) {
        super(eventId);
        this.curTabId = curTabId;
        this.preTabId = preTabId;
    }
}
