package com.txznet.music.soundControlModule.bean;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;

/**
 * 共有信息
 *
 * @author ASUS User
 */
public class BaseAudio {
    public static final int MUSIC_TYPE = 1;
    public static final int ALBUM_TYPE = 2;

    private Audio audio;
    private Album album;

    public Audio getAudio() {
        return audio;
    }

    public void setAudio(Audio audio) {
        this.audio = audio;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public int getType() {
        if (audio != null) {
            return MUSIC_TYPE;
        } else if (album != null) {
            return ALBUM_TYPE;
        }
        return 0;
    }

}
