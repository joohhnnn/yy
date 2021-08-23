package com.txznet.music.dao;

import com.google.gson.reflect.TypeToken;
import com.txznet.music.utils.JsonHelper;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.Map;

/**
 * @author zackzhou
 * @date 2019/2/20,14:24
 */
public class MapConverter implements PropertyConverter<Map<String, Object>, String> {
    @Override
    public Map<String, Object> convertToEntityProperty(String databaseValue) {
        if (null != databaseValue) {
            return JsonHelper.toObject(databaseValue, new TypeToken<Map<String, Object>>() {
            }.getType());
        }
        return null;
    }

    @Override
    public String convertToDatabaseValue(Map<String, Object> entityProperty) {
        if (null != entityProperty) {
            return JsonHelper.toJson(entityProperty);
        }
        return null;
    }
}

