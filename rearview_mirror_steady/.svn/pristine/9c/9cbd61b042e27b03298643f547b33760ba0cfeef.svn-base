package com.txznet.webchat.sp;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.sp.CommonSp;

/**
 * 微信设置sp
 * 用于应用设置持久化
 * Created by J on 2017/3/30.
 */

public class WxConfigSp extends CommonSp {
    private static final String SP_NAME = "wx_config_sp";

    // KEYS for configuration items
    private static final String KEY_WX_ENTRY = "key_enable_wx_entry";
    private static final String KEY_MSG_BROAD = "key_msg_broad";
    private static final String KEY_MSG_GROUP_BROAD = "key_msg_group_broad";
    private static final String KEY_UI_ASR = "key_enable_ui_asr";
    private static final String KEY_AUTO_LOGIN = "key_enable_auto_login";
    private static final String KEY_GROUP_CONTACT = "key_enable_group_contact";

    private static final String KEY_LAST_LOGIN_UIN = "key_last_login_uin";

    private static WxConfigSp sInstance;

    public static WxConfigSp getInstance() {
        if (null == sInstance) {
            synchronized (WxConfigSp.class) {
                if (null == sInstance) {
                    sInstance = new WxConfigSp();
                }
            }
        }

        return sInstance;
    }

    private WxConfigSp() {
        super(GlobalContext.get(), SP_NAME);
    }

    public void setWxEntryEnabled(boolean value) {
        setValue(KEY_WX_ENTRY, value);
    }

    public boolean getWxEntryEnabled(boolean defValue) {
        return getValue(KEY_WX_ENTRY, defValue);
    }

    public void setMsgBroadEnabled(boolean value) {
        setValue(KEY_MSG_BROAD, value);
    }

    public boolean getMsgBroadEnabled(boolean defValue) {
        return getValue(KEY_MSG_BROAD, defValue);
    }

    public void setGroupMsgBroad(boolean value) {
        setValue(KEY_MSG_GROUP_BROAD, value);
    }

    public boolean getGroupMsgBroad(boolean defValue) {
        return getValue(KEY_MSG_GROUP_BROAD, defValue);
    }

    public void setUIAsrEnabled(boolean value) {
        setValue(KEY_UI_ASR, value);
    }

    public boolean getUIAsrEnabled(boolean defValue) {
        return getValue(KEY_UI_ASR, defValue);
    }

    public void setAutoLoginEnabled(boolean value) {
        setValue(KEY_AUTO_LOGIN, value);
    }

    public boolean getAutoLoginEnabled(boolean defValue) {
        return getValue(KEY_AUTO_LOGIN, defValue);
    }

    public void setGroupContactEnabled(boolean value) {
        setValue(KEY_GROUP_CONTACT, value);
    }

    public boolean getGroupContactEnabled(boolean defValue) {
        return getValue(KEY_GROUP_CONTACT, defValue);
    }

    public void setLastLoginUin(String uin) {
        setValue(KEY_LAST_LOGIN_UIN, uin);
    }

    public String getLastLoginUin(String defValue) {
        return getValue(KEY_LAST_LOGIN_UIN, defValue);
    }
}
