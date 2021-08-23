package com.txznet.music.dao;

import com.google.gson.reflect.TypeToken;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.utils.JsonHelper;

import org.greenrobot.greendao.converter.PropertyConverter;

/**
 * Created by brainBear on 2017/12/9.
 */

public class AlbumConverter implements PropertyConverter<Album, String> {


    @Override
    public Album convertToEntityProperty(String databaseValue) {
        if (null != databaseValue) {
            return JsonHelper.toObject(databaseValue, new TypeToken<Album>() {
            }.getType());
        }
        return null;
    }

    @Override
    public String convertToDatabaseValue(Album entityProperty) {
        if (null != entityProperty) {
            return JsonHelper.toJson(entityProperty);
        }
        return null;
    }
}
