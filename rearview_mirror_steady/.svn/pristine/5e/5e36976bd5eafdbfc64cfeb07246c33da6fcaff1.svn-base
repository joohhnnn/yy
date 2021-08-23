package com.txznet.comm.ui.dialog2;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import static com.txznet.txz.util.TXZFileConfigUtil.KEY_WIN_DIALOG_STYLE_CANCELABLE;
import static com.txznet.txz.util.TXZFileConfigUtil.KEY_WIN_DIALOG_STYLE_CANCEL_OUTSIDE;
import static com.txznet.txz.util.TXZFileConfigUtil.KEY_WIN_DIALOG_STYLE_CANCEL_SCREEN_LOCK_TIME;
import static com.txznet.txz.util.TXZFileConfigUtil.KEY_WIN_DIALOG_STYLE_IS_FULL;
import static com.txznet.txz.util.TXZFileConfigUtil.KEY_WIN_DIALOG_STYLE_IS_SYSTEM;
import static com.txznet.txz.util.TXZFileConfigUtil.KEY_WIN_DIALOG_STYLE_PREEMPT_TYPE;
import static com.txznet.txz.util.TXZFileConfigUtil.KEY_WIN_DIALOG_STYLE_STOP_COUNT_DOWN_WHEN_LOSE_FOCUS;
import static com.txznet.txz.util.TXZFileConfigUtil.KEY_WIN_DIALOG_STYLE_WIN_TYPE;

/**
 * WinDialog样式配置
 */
public class WinDialogOptions {

    private static boolean isConfigInit;
    private static Option option;

    // DialogBuildData Option Config Params
    public static final class Option {
        Integer winType;
        Boolean isFull;
        Boolean isSystem;
        Boolean cancelable;
        Boolean cancelOutside;
        Integer screenLockTime;
        Boolean stopCountDownWhenLoseFocus;
        String preemptType;
    }

    private WinDialogOptions() {

    }

    private static void checkConfigInit() {
        if (!isConfigInit) {
            initConfig();
        }
    }

    private static void initConfig() {
        synchronized (WinDialogOptions.class) {
            if (option == null) {
                option = new Option();
                try {
                    option.winType = TXZFileConfigUtil.getSingleConfig(KEY_WIN_DIALOG_STYLE_WIN_TYPE, Integer.class, null);
                    option.isFull = TXZFileConfigUtil.getSingleConfig(KEY_WIN_DIALOG_STYLE_IS_FULL, Boolean.class, null);
                    option.isSystem = TXZFileConfigUtil.getSingleConfig(KEY_WIN_DIALOG_STYLE_IS_SYSTEM, Boolean.class, null);
                    option.cancelable = TXZFileConfigUtil.getSingleConfig(KEY_WIN_DIALOG_STYLE_CANCELABLE, Boolean.class, null);
                    option.cancelOutside = TXZFileConfigUtil.getSingleConfig(KEY_WIN_DIALOG_STYLE_CANCEL_OUTSIDE, Boolean.class, null);
                    option.screenLockTime = TXZFileConfigUtil.getSingleConfig(KEY_WIN_DIALOG_STYLE_CANCEL_SCREEN_LOCK_TIME, Integer.class, null);
                    option.stopCountDownWhenLoseFocus = TXZFileConfigUtil.getSingleConfig(KEY_WIN_DIALOG_STYLE_STOP_COUNT_DOWN_WHEN_LOSE_FOCUS, Boolean.class, null);
                    option.preemptType = TXZFileConfigUtil.getSingleConfig(KEY_WIN_DIALOG_STYLE_PREEMPT_TYPE, String.class, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        isConfigInit = true;
    }

    public static void mergeLocalConfig(WinDialog.DialogBuildData data) {
        if (data != null) {
            checkConfigInit();
            if (option.winType != null) {
                data.mWinType = option.winType;
            }
            if (option.isFull != null) {
                data.mIsFull = option.isFull;
            }
            if (option.isSystem != null) {
                data.mIsSystem = option.isSystem;
            }
            if (option.cancelable != null) {
                data.mCancelable = option.cancelable;
            }
            if (option.cancelOutside != null) {
                data.mCancelOutside = option.cancelOutside;
            }
            if (option.screenLockTime != null) {
                data.mScreenLockTime = option.screenLockTime;
            }
            if (option.stopCountDownWhenLoseFocus != null) {
                data.mStopCountDownWhenLoseFocus = option.stopCountDownWhenLoseFocus;
            }
            if (option.preemptType != null) {
                data.mPreemptType = TtsUtil.PreemptType.valueOf(option.preemptType);
            }
        }
    }
}
