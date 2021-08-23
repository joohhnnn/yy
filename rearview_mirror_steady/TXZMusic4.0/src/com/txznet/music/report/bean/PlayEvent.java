package com.txznet.music.report.bean;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.report.ReportEventConst;

/**
 * Created by brainBear on 2018/1/24.
 */

public class PlayEvent extends EventBase {


    /**
     * 开始播放
     */
    public static final String ACTION_PLAY_START = "play_start";
    /**
     * 自动切换
     */
    public static final String ACTION_SWITCH_AUTO = "switch_auto";

    /**
     * 声控切换
     */
    public static final String ACTION_SWITCH_SOUND = "switch_sound";

    /**
     * 手动切换
     */
    public static final String ACTION_SWITCH_MANUAL = "switch_manual";

    public String action;
    public long audioId;
    public long albumId;
    public int audioSid;
    public int albumSid;
    public int isInsert;
    public String name;
    public long duration;

    public PlayEvent(String eventId, String action, Audio audio, Album album) {
        super(eventId);
        this.action = action;
        if (null != audio) {
            this.audioId = audio.getId();
            this.audioSid = audio.getSid();
            this.isInsert = audio.getIsInsert();
            this.name = audio.getName();
        }
        if (null != album) {
            this.albumId = album.getId();
            this.albumSid = album.getSid();
        }
    }
}
