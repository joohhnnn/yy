package com.txznet.music.data.db.convert;

import android.arch.persistence.room.TypeConverter;

import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.util.SerializeUtils;

/**
 * @author zackzhou
 * @date 2018/12/28,15:29
 */

public class AudioV5Convert {

//    @TypeConverter
//    public static String fromAudio(AudioV5 audio) {
//        return JsonHelper.toJson(audio);
//    }
//
//    @TypeConverter
//    public static AudioV5 toAudio(String str) {
//        return JsonHelper.fromJson(str, AudioV5.class);
//    }

    @TypeConverter
    public static byte[] fromAudio(AudioV5 audio) {
        return SerializeUtils.toBytes(audio);
    }

    @TypeConverter
    public static AudioV5 toAudio(byte[] str) {
        return SerializeUtils.toObject(str, AudioV5.class);
    }
}
