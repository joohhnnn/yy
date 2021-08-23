package com.txznet.music.report.bean;

import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.Utils;

/**
 * Created by brainBear on 2018/1/8.
 */
public class EventBase {
    private long timestamp;
    private long sessionId;
    private String eventId;
    private int dataOri;

    public EventBase(String eventId) {
        sessionId = getSessionId();
        timestamp = getTimestamp();
        this.eventId = eventId;
        dataOri = PlayInfoManager.getInstance().getCurrentScene();
    }

    public long getTimestamp() {
        return TimeManager.getInstance().getTimeMillis();
    }

    public long getSessionId() {
        Audio audio = PlayInfoManager.getInstance().getCurrentAudio();
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        if (null != audio) {
            if (Utils.isSong(audio.getSid())) {
                return gethashcode(audio.getId());
            } else if (null != currentAlbum) {
                return gethashcode(currentAlbum.getId());
            }
        }
        return 0;
    }

    private long gethashcode(long id) {
        if (ReportEvent.getPauseTime() == 0) {
            return String.valueOf(id).hashCode();
        }
        return 0;
    }
}
