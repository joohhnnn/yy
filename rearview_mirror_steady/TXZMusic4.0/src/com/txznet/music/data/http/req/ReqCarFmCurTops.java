package com.txznet.music.data.http.req;

import android.os.SystemClock;

import com.txznet.music.Time.TimeManager;

/**
 * 请求的车主fm是否在当前分时段，用于逻辑代码
 * /fm/SuperFm?action=checkNextAlbum&album_id=1000757
 * {
 * 'category_id': 200001,
 * 'album_id': 0,
 * 'audio_id': 0
 * }
 */
public class ReqCarFmCurTops extends BaseReq {

    public static final String CHECK_NEXT_ALBUM_NAME = "checkNextAlbumName";
    public static final String CHECK_NEXT_ALBUM = "checkNextAlbum";


    private String action;
    private long album_id;
    //    private long timestamp;
    private long click_time;//  点击  的时间戳(s)
    private long send_time;//  发送 的时间戳(s)

    public ReqCarFmCurTops(long album_id, long click_time) {
        this.album_id = album_id;
        action = CHECK_NEXT_ALBUM;
//        timestamp = TimeManager.getInstance().getTime();
        send_time = SystemClock.elapsedRealtime() / 1000;
        this.click_time = click_time / 1000;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public long getAlbum_id() {
        return album_id;
    }

    public void setAlbum_id(long album_id) {
        this.album_id = album_id;
    }
}
