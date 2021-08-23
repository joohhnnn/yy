package com.txznet.music.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.support.annotation.IntDef;

import com.google.gson.annotations.Expose;
import com.txznet.music.BuildConfig;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Objects;

@Entity(primaryKeys = {"id", "sid"})
public class Album extends AbstractExtraBean implements Serializable {
    public static final int ALBUM_TYPE_UNKNOWN = 0; // 未知
    public static final int ALBUM_TYPE_MUSIC = 1; // 音乐
    public static final int ALBUM_TYPE_FM = 2; // 电台
    public static final int ALBUM_TYPE_NEWS = 3; // 新闻
    public static final int ALBUM_TYPE_NOVEL = 4; // 小说
    public static final int ALBUM_TYPE_TALK_SHOW = 5; // 脱口秀
    public static final int ALBUM_TYPE_RECOMMEND = 6; //新闻推荐
    public static final int ALBUM_TYPE_CARERFM = 7; //车主FM
    public static final int ALBUM_TYPE_CFM = 8; // 分类电台，混合的，有多种专辑组合
    /**
     * arrArtistName : []
     * listenNum : 8702
     * arrCategoryIds : [500008]
     * score : 0
     * breakpoint : 1
     * serialize : 1
     * flag : 10
     * listenNumText : 8702次
     * audiosNum : 14集
     */

    public String listenNumText;
    public String audiosNum;

    @IntDef({
            ALBUM_TYPE_UNKNOWN,
            ALBUM_TYPE_MUSIC,
            ALBUM_TYPE_FM,
            ALBUM_TYPE_NEWS,
            ALBUM_TYPE_NOVEL,
            ALBUM_TYPE_TALK_SHOW,
            ALBUM_TYPE_CFM
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface AlbumType {
    }


    public int sid; // 来源
    public long id; // 专辑id

    public String name; // 专辑名称
    public String logo; // 专辑logo
    public String desc; // 专辑描述
    public List<String> arrArtistName; // 歌手
    public String report; // tts播报用

    @Deprecated
    @Expose //Gson序列排除字段
    public boolean supportSubscribe; // 是否支持订阅
    @Deprecated
    @Expose //Gson序列排除字段
    public boolean isRecommend; // 是否当前推荐专辑
    @Deprecated
    @Expose //Gson序列排除字段
    public boolean isCarFm; // 是否车主FM

    @Expose //Gson序列排除字段
    public boolean isSubscribe; // 是否订阅

    public int albumType; // 专辑类型

    @Ignore //数据库排除字段
    @Expose //Gson序列排除字段
    public boolean isPlayEnd; // 专辑是否播放完毕

    @Ignore
    public int posId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album)) return false;

        Album album = (Album) o;

//        if (AlbumUtils.isUserMusic(this) && AlbumUtils.isUserMusic(album)) {
//            return TextUtils.equals(name, album.name);
//        }
//
//        if (AlbumUtils.isUserRadio(this) && AlbumUtils.isUserRadio(album)) {
//            return TextUtils.equals(name, album.name);
//        }

        return sid == album.sid &&
                id == album.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sid, id);
    }

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return "Album{" +
                    "listenNumText='" + listenNumText + '\'' +
                    ", audiosNum='" + audiosNum + '\'' +
                    ", sid=" + sid +
                    ", id=" + id +
                    ", name='" + name + '\'' +
                    ", logo='" + logo + '\'' +
                    ", desc='" + desc + '\'' +
                    ", arrArtistName=" + arrArtistName +
                    ", report='" + report + '\'' +
                    ", albumType=" + albumType +
                    ", isPlayEnd=" + isPlayEnd +
                    '}';
        }

        return "Album{" +
                "sid=" + sid +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", albumType=" + albumType +
                '}';
    }

}
