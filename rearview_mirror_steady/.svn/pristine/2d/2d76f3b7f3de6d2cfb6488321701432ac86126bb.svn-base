package com.txznet.music.data.entity;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.BuildConfig;

@Entity(primaryKeys = {"id", "sid"})
public class AudioV5 extends AbstractExtraBean {
    public static final int SOURCE_ID_LOCAL = 0;
    public static final int SOURCE_ID_KAOLA = 1;
    public static final int SOURCE_ID_QQ = 2;
    public static final int SOURCE_ID_XMLY = 3;

    public long id; // 唯一标记这个音频的id
    public String name; // 音频的名称
    public long albumId; // 专辑id
    public int albumSid; // 专辑sid
    public String albumName; // 专辑名称
    public String[] artist; // 歌手
    public String logo; // 音频图片
    public long resLen; // 文件大小
    public long duration; // 播放总长
    public String sourceUrl; // 直连的url，若设置，则不会致行异步获取播放链接的逻辑
    public int sid; // 来源id，用于多来源区分; 目前定义 0 本地
    public String announce;//语音搜索结果界面,用来播报的文本

    @Ignore //数据库排除字段
    @Expose //Gson序列排除字段
    public boolean isFavour; // 是否收藏
    @Ignore //数据库排除字段
    @Expose //Gson序列排除字段
    public boolean hasPlay; // 是否播放过超过10s
    @Ignore //数据库排除字段
    @Expose //Gson序列排除字段
    public int progress; // 播放百分比

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioV5)) return false;

        AudioV5 audio = (AudioV5) o;

        if (0 == sid && audio.sid == sid) {
            // 同名、同歌手
            if (TextUtils.equals(StringUtils.toString(audio.artist), StringUtils.toString(artist))
                    && TextUtils.equals(audio.name, name)) {
                return true;
            } else if (!TextUtils.equals(audio.sourceUrl, sourceUrl)) {
                return false;
            }
        }
        if (id != audio.id) return false;
        return sid == audio.sid;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (sid != 0) {
            result = prime * result + (int) (id ^ (id >>> 32));
        }
        result = prime * result + sid;
        return result;
    }


    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "AudioV5{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", sourceUrl='" + sourceUrl + '\'' +
                    ", sid=" + sid +
                    '}';
        } else {
            return "AudioV5{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", sid=" + sid +
                    '}';
        }
    }
}
