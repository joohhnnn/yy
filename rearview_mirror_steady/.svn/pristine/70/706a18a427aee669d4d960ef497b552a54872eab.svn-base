package com.txznet.launcher.sp;

import android.content.Context;

import com.txznet.comm.sp.CommonSp;

/**
 * Created by ASUS User on 2015/9/21.
 */
public class ThemeSp extends CommonSp {
    private static final String SP_NAME = "theme_conf";

    protected ThemeSp(Context context) {
        super(context, SP_NAME);
    }

    private static final String KEY_CURRENT_THEME = "cur_theme";

    private static ThemeSp sInstance;

    public static final ThemeSp getInstance(Context context) {
        if (sInstance == null) {
            synchronized (ThemeSp.class) {
                if (sInstance == null) {
                    sInstance = new ThemeSp(context);
                }
            }
        }
        return sInstance;
    }

    public void setCurrentTheme(String val) {
        setValue(KEY_CURRENT_THEME, val);
    }

    public String getCurrentTheme(String defVal) {
        return getValue(KEY_CURRENT_THEME, defVal);
    }
}
