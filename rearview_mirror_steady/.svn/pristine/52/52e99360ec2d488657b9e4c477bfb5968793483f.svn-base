package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 打开同听
 *
 * @author zackzhou
 * @date 2018/12/27,14:41
 */

public class SysOpenEvent extends BaseEvent {

    public static final int ENTRY_TYPE_VOICE = 100; // 声控打开同听
    public static final int ENTRY_TYPE_VOICE_SEARCH = 101; // 声控搜索进入
    public static final int ENTRY_TYPE_MANUAL = 200; // 手动点击
    public static final int ENTRY_TYPE_JY_MSG_LIST = 201; // 点击惊鱼消息队列
    public static final int ENTRY_TYPE_JY_FOUND = 202; // 点击惊鱼发现推荐位
    public static final int ENTRY_TYPE_FLOAT_WIN_PLAY_BTN = 203; // 点击悬浮窗播放键
    public static final int ENTRY_TYPE_AI = 300; // 智能播放
    public static final int ENTRY_TYPE_AI_SUB_BAR = 301; // 点击点火智能播放次栏进入
    public static final int ENTRY_TYPE_JY_MSG_LIST_AUTO_PLAY = 302; // 惊鱼消息队列默认播放
    public static final int ENTRY_TYPE_OTHER = 999; // 其他


    @EnterType
    public int entryType;

    @IntDef({
            ENTRY_TYPE_VOICE,
            ENTRY_TYPE_VOICE_SEARCH,
            ENTRY_TYPE_MANUAL,
            ENTRY_TYPE_JY_MSG_LIST,
            ENTRY_TYPE_JY_FOUND,
            ENTRY_TYPE_FLOAT_WIN_PLAY_BTN,
            ENTRY_TYPE_AI,
            ENTRY_TYPE_AI_SUB_BAR,
            ENTRY_TYPE_JY_MSG_LIST_AUTO_PLAY,
            ENTRY_TYPE_OTHER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface EnterType {
    }

    public SysOpenEvent(int eventId, @EnterType int entryType) {
        super(eventId);
        this.entryType = entryType;
    }
}
