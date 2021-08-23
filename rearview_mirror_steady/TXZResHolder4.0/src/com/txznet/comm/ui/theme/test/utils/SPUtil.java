package com.txznet.comm.ui.theme.test.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-11-12 19:57
 */
public class SPUtil {

    private SPUtil() {

    }

    public static final String SP_SMART_HANDY = "sp_ui_smart_handy";
    public static final String SP_SMART_HANDY_DIRECT_CMD_HOME = "direct_cmd_home";
    public static final String SP_SMART_HANDY_DIRECT_CMD_COMPANY = "direct_cmd_company";

    /**
     * 回家模式使用的指令集
     *
     * @param context
     * @return
     */
    public static Set<String> getSmartHandyHomeDirectCmd(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_SMART_HANDY, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(SP_SMART_HANDY_DIRECT_CMD_HOME, new HashSet<String>());
    }

    /**
     * 回家模式使用的指令集
     *
     * @param context
     * @param set
     */
    public static void setSmartHandyHomeDirectCmd(Context context, Set<String> set) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_SMART_HANDY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(SP_SMART_HANDY_DIRECT_CMD_HOME, set);
        editor.apply();
    }

    /**
     * 上班模式使用的指令集
     *
     * @param context
     * @return
     */
    public static Set<String> getSmartHandyCompanyDirectCmd(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_SMART_HANDY, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(SP_SMART_HANDY_DIRECT_CMD_COMPANY, new HashSet<String>());
    }

    /**
     * 上班模式使用的指令集
     *
     * @param context
     * @param set
     */
    public static void setSmartHandyCompanyDirectCmd(Context context, Set<String> set) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SP_SMART_HANDY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(SP_SMART_HANDY_DIRECT_CMD_COMPANY, set);
        editor.apply();
    }
}
