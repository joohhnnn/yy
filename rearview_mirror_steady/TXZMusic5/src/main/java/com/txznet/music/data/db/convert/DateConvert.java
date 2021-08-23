package com.txznet.music.data.db.convert;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

public class DateConvert {
    @TypeConverter
    public static Date fromLong(long time) {
        return new Date(time);
    }

    @TypeConverter
    public static long toDate(Date date) {
        return date.getTime();
    }
}
