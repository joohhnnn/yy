package com.txznet.music.data.db.convert;

import android.arch.persistence.room.TypeConverter;

import com.txznet.music.data.entity.PlayScene;

/**
 * @author zackzhou
 * @date 2018/12/28,16:15
 */

public class PlaySceneConvert {
    @TypeConverter
    public static String fromScene(PlayScene scene) {
        return scene.name();
    }

    @TypeConverter
    public static PlayScene toScene(String str) {
        return PlayScene.valueOf(str);
    }
}
