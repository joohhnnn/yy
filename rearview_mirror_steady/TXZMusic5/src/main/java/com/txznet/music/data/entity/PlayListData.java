package com.txznet.music.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.txznet.audio.player.entity.Audio;

import java.util.List;

/**
 * 播放列表记录
 * 用于存放播放列表的数据，用于下次恢复的时候，从上次指定位置开始，继续播放
 *
 * @author zackzhou
 * @date 2018/12/28,14:25
 */

@Entity
public class PlayListData {
    @PrimaryKey(autoGenerate = true)
    public long _id;

    public Album album; // 最后一次播放的专辑

    public Audio audio; // 最后一次播放的音频

    public PlayScene scene; // 最后一次播放的场景

    public List<Audio> audioList; // 音频列表

    @Override
    public String toString() {
        return "PlayListData{" +
                "album=" + album +
                ", audio=" + audio +
                ", scene=" + scene +
                '}';
    }
}
