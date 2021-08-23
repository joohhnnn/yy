package com.txznet.music.dao;

import com.google.gson.reflect.TypeToken;
import com.txznet.music.utils.JsonHelper;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;

/**
 * Created by brainBear on 2017/9/8.
 */

public class IntegerListConverter implements PropertyConverter<List<Integer>, String> {
    @Override
    public List<Integer> convertToEntityProperty(String databaseValue) {
        if (null != databaseValue) {
            return JsonHelper.toObject(databaseValue, new TypeToken<List<Integer>>() {
            }.getType());
        }
        return null;
    }

    @Override
    public String convertToDatabaseValue(List<Integer> entityProperty) {
        if (null != entityProperty) {
            return JsonHelper.toJson(entityProperty);
        }
        return null;
    }
}
