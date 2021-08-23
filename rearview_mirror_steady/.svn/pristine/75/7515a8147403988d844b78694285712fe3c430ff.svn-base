package com.txznet.music.dao;

import com.google.gson.reflect.TypeToken;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.utils.JsonHelper;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.ArrayList;
import java.util.List;

public class String2AudiosConverter implements PropertyConverter<List<Audio>, String> {

    @Override
    public List<Audio> convertToEntityProperty(String databaseValue) {
        return JsonHelper.toObject(databaseValue, new TypeToken<List<Audio>>() {
        }.getType());
    }

    @Override
    public String convertToDatabaseValue(List<Audio> entityProperty) {
        //因为这个entityProperty可以被外界修改，导致异常：ConcurrentModificationException
        //【【同听4.4.1】【crash】java.util.ConcurrentModificationException】
        //https://www.tapd.cn/21711881/bugtrace/bugs/view?bug_id=1121711881001004277
        List<Audio> audios = new ArrayList<>(entityProperty);
        return JsonHelper.toJson(audios);
    }
}
