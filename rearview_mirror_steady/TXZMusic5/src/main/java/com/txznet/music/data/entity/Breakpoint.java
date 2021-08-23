package com.txznet.music.data.entity;

import android.arch.persistence.room.Entity;

/**
 * 音频播放断点记录
 *
 * @author zackzhou
 * @date 2018/12/12,14:04
 */
@Entity(primaryKeys = {"id", "sid"})
public class Breakpoint {
    public long position; // 断点位置
    public long duration; // 总时长

    public int sid; // 音频sid
    public long id; // 音频audioId

    public String albumId; // 专辑id  ${sid}-${id}

    public int playEndCount; // 完整播放过的次数(触发onCompletion)

    @Override
    public String toString() {
        return "Breakpoint{" +
                "position=" + position +
                ", duration=" + duration +
                ", sid=" + sid +
                ", id=" + id +
                ", albumId='" + albumId + '\'' +
                ", playEndCount=" + playEndCount +
                '}';
    }
}
