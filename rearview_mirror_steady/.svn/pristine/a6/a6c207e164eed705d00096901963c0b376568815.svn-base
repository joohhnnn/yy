package com.txznet.music.dao;

import com.google.gson.reflect.TypeToken;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.utils.JsonHelper;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.List;

/**
 * Created by brainBear on 2017/9/8.
 */

public class CategotyListConverter implements PropertyConverter<List<Category>, String> {

    @Override
    public List<Category> convertToEntityProperty(String databaseValue) {
        if (databaseValue != null) {
            return JsonHelper.toObject(databaseValue, new TypeToken<List<Category>>() {
            }.getType());
        }
        return null;
    }

    @Override
    public String convertToDatabaseValue(List<Category> entityProperty) {
        if (entityProperty != null) {
            return JsonHelper.toJson(entityProperty);
        }
        return null;
    }
}
