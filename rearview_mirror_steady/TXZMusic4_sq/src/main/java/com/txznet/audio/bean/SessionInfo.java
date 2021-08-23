package com.txznet.audio.bean;

import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.server.response.MediaResponseBase;

import java.util.HashSet;
import java.util.Set;

/**
 * 保存一系列的关系（不混淆）
 * Created by ASUS User on 2016/12/1.
 */

public class SessionInfo {
    public PlayerAudio audio;
    public TXZAudioPlayer player;
    public long len;//文件大小，用于做拖动
    protected Set<MediaResponseBase> responses = new HashSet<MediaResponseBase>();

    public SessionInfo(PlayerAudio audio) {
        this.audio = audio;
    }

    public void addResponse(MediaResponseBase res) {
        synchronized (responses) {
            responses.add(res);
        }
    }

    public void cancelAllResponse() {
        synchronized (responses) {
            for (MediaResponseBase response : responses) {
                response.cancel();
            }
            responses.clear();
        }
    }

    public String getLogId() {
        String name = audio.getAudioName();
        if (name.length() > 4) {
            name = name.substring(0, 4);
        }
        return "" + this.hashCode() + "#" + name;
    }
}
