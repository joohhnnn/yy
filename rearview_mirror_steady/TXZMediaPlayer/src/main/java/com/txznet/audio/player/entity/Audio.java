package com.txznet.audio.player.entity;

import com.txznet.audio.player.BuildConfig;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class Audio implements Serializable {
    public long id = -1; // 唯一标记这个音频的id
    public String name; // 音频的名称
    public long albumId; // 专辑id
    public int albumSid; // 专辑sid
    public String albumName; // 专辑名称
    public String[] artist; // 歌手
    public String logo; // 音频图片
    //    public String desc; // 音频描述
    public long resLen; // 文件大小
    public long duration; // 播放总长
    public String sourceUrl; // 播放链接，支持http、本地 txz://{strProcessingUrl}_{strDownloadUrl}_{downloadType} @See: TXZUri
    public int sid; // 来源id，用于多来源区分

    // 兼容字段
    public ConcurrentHashMap<String, Object> extra;

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "Audio{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", albumId=" + albumId +
                    ", albumSid=" + albumSid +
                    ", albumName='" + albumName + '\'' +
                    ", artist=" + Arrays.toString(artist) +
                    ", logo='" + logo + '\'' +
                    ", resLen=" + resLen +
                    ", duration=" + duration +
                    ", sourceUrl='" + sourceUrl + '\'' +
                    ", sid=" + sid +
                    '}';
        } else {
            return "Audio{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", albumId=" + albumId +
                    ", albumSid=" + albumSid +
                    ", albumName='" + albumName + '\'' +
                    ", artist=" + Arrays.toString(artist) +
                    ", logo='" + logo + '\'' +
                    ", resLen=" + resLen +
                    ", duration=" + duration +
                    ", sid=" + sid +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Audio audio = (Audio) o;

        if (id != audio.id) return false;
        return sid == audio.sid;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + sid;
        return result;
    }

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
