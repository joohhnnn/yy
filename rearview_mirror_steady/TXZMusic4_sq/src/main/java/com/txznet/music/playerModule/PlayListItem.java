package com.txznet.music.playerModule;

import com.txznet.music.albumModule.bean.Audio;

/**
 * Created by brainBear on 2017/12/19.
 */

public class PlayListItem {

    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_HIGHLINGHT = 1;
    public static final int STYLE_GREY = 2;

    private Audio audio;

    private float progress;

    private boolean isFavor;

    private boolean showFavor;

    private boolean showProgress;


    private int style;
    private boolean isFavorEnable;

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public boolean isFavorEnable() {
        return isFavorEnable;
    }

    public void setFavorEnable(boolean favorEnable) {
        isFavorEnable = favorEnable;
    }

    public Audio getAudio() {
        return audio;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public boolean isFavor() {
        return isFavor;
    }

    public void setFavor(boolean favor) {
        this.isFavor = favor;
    }

    public boolean isShowFavor() {
        return showFavor;
    }

    public void setShowFavor(boolean showFavor) {
        this.showFavor = showFavor;
    }

    public boolean isShowProgress() {
        return showProgress;
    }

    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }


    public boolean areContentsTheSame(PlayListItem other) {
        if (null == other) {
            return false;
        }

        if (this.getStyle() != other.getStyle()) {
            return false;
        }

        if (this.isShowFavor() != other.isShowFavor()) {
            return false;
        }

        if (this.isShowFavor() && other.isShowFavor()) {
            if (this.isFavor != other.isFavor) {
                return false;
            }
        }

        if (this.isFavorEnable() != other.isFavorEnable()) {
            return false;
        }

        if (this.isShowProgress() != other.isShowProgress()) {
            return false;
        }


        if (this.isShowProgress() && other.isShowProgress()) {
            if (this.getProgress() == other.getProgress()) {
                return false;
            }
        }
        return true;
    }
}
