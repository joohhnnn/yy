package com.txznet.music.data.db.convert;

import android.arch.persistence.room.TypeConverter;

import com.txznet.audio.player.entity.Audio;
import com.txznet.music.util.SerializeUtils;

/**
 * @author zackzhou
 * @date 2018/12/28,15:29
 */

public class AudioConvert {

//    @TypeConverter
//    public static String fromAudio(Audio audio) {
//        return JsonHelper.toJson(audio);
//    }
//
//    @TypeConverter
//    public static Audio toAudio(String str) {
//        return JsonHelper.fromJson(str, Audio.class);
//    }

    @TypeConverter
    public static byte[] fromAudio(Audio audio) {
        return SerializeUtils.toBytes(audio);
    }

    @TypeConverter
    public static Audio toAudio(byte[] str) {
        return SerializeUtils.toObject(str, Audio.class);
    }
}
