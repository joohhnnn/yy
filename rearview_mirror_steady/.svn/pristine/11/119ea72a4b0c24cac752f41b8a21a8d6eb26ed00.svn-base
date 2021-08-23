package com.txznet.music.data.entity;

import android.arch.persistence.room.Ignore;

import java.io.Serializable;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 预留字段的基类
 */
public abstract class AbstractExtraBean implements Serializable {

    @Ignore //数据库排除字段
    public ConcurrentHashMap<String, Object> extra;

    public <T> T getExtraKey(String key, T defaultData) {
        if (extra == null) {
            extra = new ConcurrentHashMap<>(2);
        }

        T t;

        Object o = extra.get(key);
        if (o != null) {
            try {
                t = (T) o;
                return t;
            } catch (Exception e) {
                t = null;
            }
        }
        return defaultData;
    }

    public <T> T getExtraKey(String key) {
        if (extra == null) {
            extra = new ConcurrentHashMap<>(2);
        }

        T t = null;

        Object o = extra.get(key);
        if (o != null) try {
            t = (T) o;
        } catch (Exception e) {
            t = null;
        }
        return t;
    }

    public void setExtraKey(String key, Object value) {
        if (extra == null) {
            extra = new ConcurrentHashMap<>(2);
        }

        if (value != null) {

            if (value instanceof String) {
                if (((String) value).length() == 0) {
                    return;
                }
            }

            if (value instanceof Collection) {
                if (((Collection) value).size() == 0) {
                    return;
                }
            }

            extra.put(key, value);
        }
    }

}
