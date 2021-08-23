package com.txznet.music.data.db.convert;

import android.arch.persistence.room.TypeConverter;
import android.text.TextUtils;

import com.txznet.music.Constant;

import java.util.Arrays;
import java.util.List;

public class StringListConvert {

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null || list.size() < 1) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(Constant.URL_SPLIT);
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    @TypeConverter
    public static List<String> toList(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        return Arrays.asList(str.split(Constant.URL_SPLIT));
    }
}
