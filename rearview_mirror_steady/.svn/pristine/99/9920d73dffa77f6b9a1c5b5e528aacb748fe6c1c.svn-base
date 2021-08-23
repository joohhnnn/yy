package com.txznet.audio.player.audio;

import android.media.AudioManager;

import java.io.Serializable;

public class PlayerAudio implements Serializable {

    public int getStreamType() {
        return AudioManager.STREAM_MUSIC;
    }

    public boolean needCodecPlayer() {
        return false;
    }

    /**
     * 重写这个方法调试线程名字
     *
     * @return
     */
    public String getAudioName() {
        return "Audio";
    }

}