package com.txznet.music.data.entity;

import android.arch.persistence.room.Entity;

import com.txznet.music.BuildConfig;

/**
 * 历史专辑
 *
 * @author telen
 * @date 2018/12/3,14:26
 */
@Entity
public class HistoryAlbum extends Album {
    public static final HistoryAlbum NONE = new HistoryAlbum();

    public static final int FLAG_NORMAL = 0;
    public static final int FLAG_HIDDEN = 1; // 删除后会置为1

    /**
     * 专辑内最后收听的音频
     */
    public AudioV5 audio;

    /**
     * 专辑内最新的音频，目前(5.0)只针对非小说类型专辑会记录
     */
    public AudioV5 newestAudio;

    public int flag = FLAG_NORMAL;

    @Override
    public String toString() {
        if (BuildConfig.DEBUG) {
            return super.toString() + "#HistoryAlbum{" +
                    "audio=" + audio +
                    ", newestAudio=" + newestAudio +
                    '}';
        } else {
            return super.toString();
        }
    }
}
