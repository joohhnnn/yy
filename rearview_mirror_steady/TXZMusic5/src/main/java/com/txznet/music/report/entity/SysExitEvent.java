package com.txznet.music.report.entity;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 退出同听
 *
 * @author zackzhou
 * @date 2018/12/27,14:42
 */

public class SysExitEvent extends BaseEvent {

    public static final int EXIT_TYPE_VOICE = 100; // 声控关闭
    public static final int EXIT_TYPE_VOICE_AI_CANCEL = 101; // 智能播放声控取消
    public static final int EXIT_TYPE_MANUAL = 200; // 手动关闭
    public static final int EXIT_TYPE_OTHER = 999; // 其他

    @ExitType
    public int exitType;

    @IntDef({
            EXIT_TYPE_VOICE,
            EXIT_TYPE_VOICE_AI_CANCEL,
            EXIT_TYPE_MANUAL,
            EXIT_TYPE_OTHER
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ExitType {
    }

    public SysExitEvent(int eventId, @ExitType int exitType) {
        super(eventId);
        this.exitType = exitType;
    }
}
