package com.txznet.music.data.db.convert;

import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

import com.txznet.music.Constant;

public class LongArrayConvert {

    @TypeConverter
    public static String fromArr(long[] arr) {
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
    public static long[] toArr(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        String[] splits = str.split(Constant.URL_SPLIT);
        long[] result = new long[splits.length];
        for (int i = 0; i < splits.length; i++) {
            String split = splits[i];
            try {
                result[i] = Long.parseLong(split);
            } catch (Exception e) {
            }
        }
        return result;
    }
}
