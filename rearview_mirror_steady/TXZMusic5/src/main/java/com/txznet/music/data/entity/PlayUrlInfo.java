package com.txznet.music.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import java.util.List;

@Entity
public class PlayUrlInfo {
    @Ignore
    @Expose
    public static PlayUrlInfo NONE = new PlayUrlInfo();

    @PrimaryKey(autoGenerate = true)
    public long _id;
    public String strUrl; // 实际播放路径
    public long iExpTime; // 超时时间
    public long audioId; // 对应的音频id
    public int sid;
    public List<String> arrBackUpUrl; // 候选播放地址

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayUrlInfo that = (PlayUrlInfo) o;

        if (audioId != that.audioId) return false;
        return sid == that.sid;
    }

    @Override
    public int hashCode() {
        int result = (int) (audioId ^ (audioId >>> 32));
        result = 31 * result + sid;
        return result;
    }
}
