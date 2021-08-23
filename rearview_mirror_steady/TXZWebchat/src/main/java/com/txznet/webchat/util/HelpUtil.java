package com.txznet.webchat.util;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.webchat.sp.WebChatSp;

/**
 * 管理语音提示相关逻辑
 * Created by J on 2016/4/26.
 */
public class HelpUtil {
    private static final int TIP_PLAY_TIME = 3; // 提示信息播报次数

    // 播放继续发送提示相关的字段
    public static int REPEAT_TIP_PLAYED = 0; // 继续发送提示已播放的次数
    public static boolean REPEAT_TIP_ENABLED = true; // 是否启用了继续提示的播报
    // 回复微信提示相关的字段
    public static int REPLY_TIP_PLAYED = 0; // 已播报次数
    public static boolean REPLY_TIP_ENABLED = true; // 启用标志
    // 导航提示
    public static int NAV_TIP_PLAYED = 0;
    public static boolean NAV_TIP_ENABLED = true;
    // 屏蔽消息提示
    public static int MASK_TIP_PLAYED = 0;
    public static boolean MASK_TIP_ENABLED = true;

    static {

        try {
            REPEAT_TIP_PLAYED = WebChatSp.getInstance(GlobalContext.get()).getRepeatTipPlayed();
            REPEAT_TIP_ENABLED = REPEAT_TIP_PLAYED <= TIP_PLAY_TIME;
        } catch (Exception e) {
            REPEAT_TIP_ENABLED = false;
        }

        try {
            REPLY_TIP_PLAYED = WebChatSp.getInstance(GlobalContext.get()).getReplyTipPlayed();
            REPLY_TIP_ENABLED = REPLY_TIP_PLAYED <= TIP_PLAY_TIME;
        } catch (Exception e) {
            REPLY_TIP_ENABLED = false;
        }

        try {
            NAV_TIP_PLAYED = WebChatSp.getInstance(GlobalContext.get()).getNavTipPlayed();
            NAV_TIP_ENABLED = NAV_TIP_PLAYED <= TIP_PLAY_TIME;
        } catch (Exception e) {
            NAV_TIP_ENABLED = false;
        }

        try {
            MASK_TIP_PLAYED = WebChatSp.getInstance(GlobalContext.get()).getMaskTipPLayed();
            MASK_TIP_ENABLED = MASK_TIP_PLAYED <= TIP_PLAY_TIME;
        } catch (Exception e) {
            MASK_TIP_ENABLED = false;
        }
    }

    public static void procRepeatTipPlayed() {
        if (!REPEAT_TIP_ENABLED) {
            return;
        }

        WebChatSp.getInstance(GlobalContext.get()).setRepeatTipPlayed(++REPEAT_TIP_PLAYED);
        if (REPEAT_TIP_PLAYED >= TIP_PLAY_TIME) {
            REPEAT_TIP_ENABLED = false;
        }
    }

    public static void procReplyTipPlayed() {
        if (!REPLY_TIP_ENABLED) {
            return;
        }

        WebChatSp.getInstance(GlobalContext.get()).setReplyTipPlayed(++REPLY_TIP_PLAYED);
        if (REPLY_TIP_PLAYED >= TIP_PLAY_TIME) {
            REPLY_TIP_ENABLED = false;
        }
    }

    public static void procNavTipPlayed() {
        if (!NAV_TIP_ENABLED) {
            return;
        }

        WebChatSp.getInstance(GlobalContext.get()).setNavTipPlayed(++NAV_TIP_PLAYED);
        if (NAV_TIP_PLAYED >= TIP_PLAY_TIME) {
            NAV_TIP_ENABLED = false;
        }
    }

    public static void procMaskTipPlayed() {
        if (!MASK_TIP_ENABLED) {
            return;
        }

        WebChatSp.getInstance(GlobalContext.get()).setMaskTipPlayed(++MASK_TIP_PLAYED);
        if (MASK_TIP_PLAYED >= TIP_PLAY_TIME) {
            MASK_TIP_ENABLED = false;
        }
    }

    public static void disableRepeatTip() {
        REPEAT_TIP_ENABLED = false;
        WebChatSp.getInstance(GlobalContext.get()).setRepeatTipPlayed(TIP_PLAY_TIME);
    }

    public static void disableReplyTip() {
        REPLY_TIP_ENABLED = false;
        WebChatSp.getInstance(GlobalContext.get()).setReplyTipPlayed(TIP_PLAY_TIME);
    }

    public static void disableNavTip() {
        NAV_TIP_ENABLED = false;
        WebChatSp.getInstance(GlobalContext.get()).setNavTipPlayed(TIP_PLAY_TIME);
    }

    public static void disableMaskTip() {
        MASK_TIP_ENABLED = false;
        WebChatSp.getInstance(GlobalContext.get()).setMaskTipPlayed(TIP_PLAY_TIME);
    }
}
