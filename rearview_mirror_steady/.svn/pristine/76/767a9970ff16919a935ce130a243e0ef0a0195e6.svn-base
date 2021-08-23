package com.txznet.launcher.utils;

import android.content.Context;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DateUtils {
    private DateUtils() {
    }

    /**
     * 判断设备是否是采用24小时制展示时间。
     */
    public static boolean is24HourFormat(Context context) {
        // fixme 这个什么时候会返回null
        String value = Settings.System.getString(context.getContentResolver(),
                Settings.System.TIME_12_24);

        if (value == null) {
            // fixme: 2018/7/26 为什么返回null后，能用这种方法判断是12还是24小时制
            Locale locale = context.getResources().getConfiguration().locale;

            java.text.DateFormat natural =
                    java.text.DateFormat.getTimeInstance(java.text.DateFormat.LONG, locale);

            if (natural instanceof SimpleDateFormat) {
                SimpleDateFormat sdf = (SimpleDateFormat) natural;
                String pattern = sdf.toPattern();

                if (pattern.indexOf('H') >= 0) {
                    value = "24";
                } else {
                    value = "12";
                }
            } else {
                value = "12";
            }

            return value.equals("24");
        }

        return value.equals("24");
    }
}
