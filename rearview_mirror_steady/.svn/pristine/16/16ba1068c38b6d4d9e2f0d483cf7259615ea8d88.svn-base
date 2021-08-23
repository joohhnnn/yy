package com.txznet.webchat.sp;

import android.content.Context;

import com.txznet.comm.sp.CommonSp;

public class PowerSp extends CommonSp {
    private static final String SP_NAME = "power_info";
    private static PowerSp instance;

    public static final PowerSp getInstance(Context context) {
        if (instance == null) {
            synchronized (WebChatSp.class) {
                if (instance == null) {
                    instance = new PowerSp(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    protected PowerSp(Context context) {
        super(context, SP_NAME);
    }

    /* known key */
    private static final String KEY_SLEEP_MODE = "sleep_mode";
    private static final String KEY_HAS_LOGIN_BEFORE_SLEEP = "logined_before_sleep";
    private static final String KEY_ENABLE_WAKEUP_LOGIN = "enable_wakeup_login";
    private static final String KEY_WAKE_ACTION_TRIGGERED = "wake_action_triggered";

    public void setSleepMode(boolean sleepMode) {
        setValue(KEY_SLEEP_MODE, sleepMode);
    }

    public boolean getSleepMode(boolean defVal) {
        return getValue(KEY_SLEEP_MODE, defVal);
    }

    public void setHasLoginBeforeSleep(boolean val) {
        setValue(KEY_HAS_LOGIN_BEFORE_SLEEP, val);
    }

    public boolean hasLoginBeforeSleep(boolean defVal) {
        return getValue(KEY_HAS_LOGIN_BEFORE_SLEEP, defVal);
    }

    public void setWakeupLoginEnabled(boolean enable) {
        setValue(KEY_ENABLE_WAKEUP_LOGIN, enable);
    }

    public boolean wakeupLoginEnabled(boolean defVal) {
        return getValue(KEY_ENABLE_WAKEUP_LOGIN, defVal);
    }

    public boolean isWakeActionTriggered(boolean defValue) {
        return getValue(KEY_WAKE_ACTION_TRIGGERED, defValue);
    }

    public void setWakeActionTriggered(boolean triggered) {
        setValue(KEY_WAKE_ACTION_TRIGGERED, triggered);
    }


}
