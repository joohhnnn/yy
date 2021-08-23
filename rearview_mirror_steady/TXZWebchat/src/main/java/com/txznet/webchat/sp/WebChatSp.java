package com.txznet.webchat.sp;

import android.content.Context;

import com.txznet.comm.sp.CommonSp;

public class WebChatSp extends CommonSp {
    private static final String SP_NAME = "webchat_info";// FILE_NAME
    private static WebChatSp instance;

    /* known key */
    private static final String KEY_BINDING = "binding";
    // 继续发送提示
    private static final String KEY_REPEAT_TIP_PLAYED = "wechat_repeat_tip_played";
    // 回复微信提示
    private static final String KEY_REPLY_TIP_PLAYED = "wechat_reply_tip_played";
    // 导航提示
    private static final String KEY_NAV_TIP_PLAYED = "wechat_nav_tip_played";
    // 屏蔽消息提示
    private static final String KEY_MASK_TIP_PLAYED = "wechat_mask_tip_played";

    //// sdk 相关key
    private static final String KEY_REMOTE_PACKAGE_NAME = "wechat_sdk_remote_pkg_name";
    private static final String KEY_WX_UI_ENABLED = "wechat_ui_enabled";
    private static final String KEY_WX_NOTIFICATION_ENABLED = "wechat_notification_enabled";
    private static final String KEY_WX_RECORD_WINDOW_ENABLED = "wechat_record_window_enabled";
    private static final String KEY_WX_SDK_VERSION = "wechat_sdk_version";

    public static final WebChatSp getInstance(Context context) {
        if (instance == null) {
            synchronized (WebChatSp.class) {
                if (instance == null) {
                    instance = new WebChatSp(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private WebChatSp(Context context) {
        super(context, SP_NAME);
    }

    public boolean isBinding(boolean defaultValue) {
        return getValue(KEY_BINDING, defaultValue);
    }

    public void setBinding(boolean value) {
        setValue(KEY_BINDING, value);
    }

    public void setRepeatTipPlayed(int times) {
        setValue(KEY_REPEAT_TIP_PLAYED, times);
    }

    public int getRepeatTipPlayed() {
        return getValue(KEY_REPEAT_TIP_PLAYED, 0);
    }

    public void setReplyTipPlayed(int times) {
        setValue(KEY_REPLY_TIP_PLAYED, times);
    }

    public int getReplyTipPlayed() {
        return getValue(KEY_REPLY_TIP_PLAYED, 0);
    }

    public void setNavTipPlayed(int times) {
        setValue(KEY_NAV_TIP_PLAYED, times);
    }

    public int getNavTipPlayed() {
        return getValue(KEY_NAV_TIP_PLAYED, 0);
    }

    public void setMaskTipPlayed(int times) {
        setValue(KEY_MASK_TIP_PLAYED, times);
    }

    public int getMaskTipPLayed() {
        return getValue(KEY_MASK_TIP_PLAYED, 0);
    }

    public void setRemotePackageName(String pkgName) {
        setValue(KEY_REMOTE_PACKAGE_NAME, pkgName);
    }

    public String getRemotePackageName() {
        return getValue(KEY_REMOTE_PACKAGE_NAME, "");
    }

    public void setSDKVersion(int version) {
        setValue(KEY_WX_SDK_VERSION, version);
    }

    public int getSDKVersion() {
        return getValue(KEY_WX_SDK_VERSION, 2);
    }

    public void setNotificationEnabled(boolean enable) {
        setValue(KEY_WX_NOTIFICATION_ENABLED, enable);
    }

    public boolean getNotificationEnabled() {
        return getValue(KEY_WX_NOTIFICATION_ENABLED, true);
    }

    public void setRecordWindowEnabled(boolean enable) {
        setValue(KEY_WX_RECORD_WINDOW_ENABLED, enable);
    }

    public boolean getRecordWindowEnabled() {
        return getValue(KEY_WX_RECORD_WINDOW_ENABLED, true);
    }

    public void setUIEnabled(boolean enable) {
        setValue(KEY_WX_UI_ENABLED, enable);
    }

    public boolean getUIEnabled() {
        return getValue(KEY_WX_UI_ENABLED, true);
    }
}
