package com.txznet.music.dao;

import com.google.gson.reflect.TypeToken;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.utils.JsonHelper;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;

/**
 * Created by brainBear on 2017/12/9.
 */

public class AudioListConverter implements PropertyConverter<List<Audio>, String> {

    @Override
    public List<Audio> convertToEntityProperty(String databaseValue) {
        if (null != databaseValue) {
            return JsonHelper.toObject(databaseValue, new TypeToken<List<Audio>>() {
            }.getType());
        }
        return null;
    }

    @Override
    public String convertToDatabaseValue(List<Audio> entityProperty) {
        if (null != entityProperty) {
            return JsonHelper.toJson(entityProperty);
        }
        return null;
    }
}
