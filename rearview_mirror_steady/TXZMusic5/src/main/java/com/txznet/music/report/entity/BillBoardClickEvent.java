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

public class BillBoardClickEvent extends BaseEvent {

    /**
     * 头部
     */
    public static final int CLICK_POS_HEADER = 0;
    /**
     * 尾部
     */
    public static final int CLICK_POS_FOOTER = 1;

    @IntDef({
            CLICK_POS_HEADER,
            CLICK_POS_FOOTER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ClickPos {
    }

    @ClickPos
    public int clickPos;

    public BillBoardClickEvent(int eventId, @ClickPos int clickPos) {
        super(eventId);
        this.clickPos = clickPos;
    }
}
