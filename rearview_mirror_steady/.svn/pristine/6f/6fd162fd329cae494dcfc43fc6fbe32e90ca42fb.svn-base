package com.txznet.music.data.http.api.txz.entity;

/**
 * 共有信息
 *
 * @author ASUS User
 */
public class TXZSearchData {
    public static final int MUSIC_TYPE = 1;
    public static final int ALBUM_TYPE = 2;

    public TXZAudio audio;
    public TXZAlbum album;

    public int getType() {
        if (audio != null) {
            return MUSIC_TYPE;
        } else if (album != null) {
            return ALBUM_TYPE;
        }
        return 0;
    }

}
