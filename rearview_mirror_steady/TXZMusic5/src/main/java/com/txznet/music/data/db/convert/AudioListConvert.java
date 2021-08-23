package com.txznet.music.data.db.convert;

import android.arch.persistence.room.TypeConverter;

import com.txznet.audio.player.entity.Audio;
import com.txznet.music.util.SerializeUtils;

import java.util.List;

/**
 * @author zackzhou
 * @date 2018/12/28,14:37
 */

public class AudioListConvert {

//    @TypeConverter
//    public static String fromList(List<Audio> list) {
//        return JsonHelper.toJson(list);
//    }
//
//    @TypeConverter
//    public static List<Audio> toList(String str) {
//        return JsonHelper.fromJson(str, new TypeToken<List<Audio>>() {
//        });
//    }

    @TypeConverter
    public static byte[] fromList(List<Audio> list) {
        return SerializeUtils.toBytes(list);
    }

    @TypeConverter
    public static List<Audio> toList(byte[] str) {
        return (List<Audio>) SerializeUtils.toObject(str);
    }
}
