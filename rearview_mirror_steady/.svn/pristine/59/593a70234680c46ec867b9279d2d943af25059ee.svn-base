package com.txznet.music.dao;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brainBear on 2017/9/8.
 */

public class StringListConverter implements PropertyConverter<List<String>, String> {

    @Override
    public List<String> convertToEntityProperty(String databaseValue) {
        if (null != databaseValue) {
            String[] split = databaseValue.split(",");
            if (split.length > 0) {
                List<String> list = new ArrayList<>();
                for (String str : split) {
                    list.add(str);
                }
                return list;
            }
        }
        return null;
    }

    @Override
    public String convertToDatabaseValue(List<String> entityProperty) {
        if (null != entityProperty) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < entityProperty.size(); i++) {
                sb.append(entityProperty.get(i));

                if (i != entityProperty.size() - 1) {
                    sb.append(",");
                }
            }
            return sb.toString();
        }
        return null;
    }
}
