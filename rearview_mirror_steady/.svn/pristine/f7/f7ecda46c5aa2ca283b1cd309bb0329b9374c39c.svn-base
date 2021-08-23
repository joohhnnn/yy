package com.txznet.launcher.domain.music.bean;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by brainBear on 2018/3/5.
 * 播放音乐或电台的数据类
 */

public class PlayInfo {


    //    1缓冲（准备）,2播放,3表示暂停，4表示退出,5表示错误
    public static final int STATE_ON_IDLE = 0;
    public static final int STATE_ON_BUFFERING = 1;
    public static final int STATE_ON_PLAYING = 2;
    public static final int STATE_ON_PAUSED = 3;
    public static final int STATE_ON_EXIT = 4;
    public static final int STATE_ON_FAILED = 5;
    public static final int PLAY_STATE_PLAYING = 0;
    public static final int PLAY_STATE_PAUSE = 1;
    public static final int PLAY_STATE_FAILED = 2;
    public static final int PLAY_STATE_OPEN = 6;
    private int status;
    private String artists;
    private String title;
    private String logo;
    private boolean isSong;
    private long progress;
    private long duration;
    private float percent;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    public boolean isSong() {
        return isSong;
    }

    public void setSong(boolean song) {
        isSong = song;
    }

    @Override
    public String toString() {
        return "PlayInfo{" +
                "status=" + status +
                ", artists='" + artists + '\'' +
                ", title='" + title + '\'' +
                ", logo='" + logo + '\'' +
                ", isSong=" + isSong +
                ", progress=" + progress +
                ", duration=" + duration +
                ", percent=" + percent +
                '}';
    }

    @IntDef({PLAY_STATE_PLAYING, PLAY_STATE_PAUSE,PLAY_STATE_FAILED, PLAY_STATE_OPEN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PlayState {
    }
}
