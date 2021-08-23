package com.txznet.music.data.db.convert;

import android.arch.persistence.room.TypeConverter;

import com.txznet.music.data.entity.Album;
import com.txznet.music.util.SerializeUtils;

/**
 * @author zackzhou
 * @date 2018/12/28,15:29
 */

public class AlbumConvert {

//    @TypeConverter
//    public static String fromAlbum(Album album) {
//        return JsonHelper.toJson(album);
//    }
//
//    @TypeConverter
//    public static Album toAlbum(String str) {
//        return JsonHelper.fromJson(str, Album.class);
//    }


    @TypeConverter
    public static byte[] fromAlbum(Album album) {
        return SerializeUtils.toBytes(album);
    }

    @TypeConverter
    public static Album toAlbum(byte[] str) {
        return SerializeUtils.toObject(str, Album.class);
    }
}
