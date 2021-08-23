package com.txznet.music.data.db.convert;

import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

import com.txznet.music.Constant;

public class StringArrayConvert {

    @TypeConverter
    public static String fromArr(String[] arr) {
        if (arr == null || arr.length < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) {
                sb.append(Constant.URL_SPLIT);
            }
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    @TypeConverter
    public static String[] toArr(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return str.split(Constant.URL_SPLIT);
    }
}
