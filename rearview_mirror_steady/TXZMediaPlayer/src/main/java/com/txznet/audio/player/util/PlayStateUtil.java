package com.txznet.audio.player.util;

import com.txznet.audio.player.IMediaPlayer;

import static com.txznet.audio.player.IMediaPlayer.STATE_ON_BUFFERING;
import static com.txznet.audio.player.IMediaPlayer.STATE_ON_END;
import static com.txznet.audio.player.IMediaPlayer.STATE_ON_ERROR;
import static com.txznet.audio.player.IMediaPlayer.STATE_ON_IDLE;
import static com.txznet.audio.player.IMediaPlayer.STATE_ON_INITIALIZED;
import static com.txznet.audio.player.IMediaPlayer.STATE_ON_PAUSED;
import static com.txznet.audio.player.IMediaPlayer.STATE_ON_PLAYING;
import static com.txznet.audio.player.IMediaPlayer.STATE_ON_PREPARED;
import static com.txznet.audio.player.IMediaPlayer.STATE_ON_PREPARING;
import static com.txznet.audio.player.IMediaPlayer.STATE_ON_STOPPED;

public class PlayStateUtil {

    public static String convert2Str(@IMediaPlayer.PlayState int state) {
        String strState = null;
        switch (state) {
            case STATE_ON_IDLE:
                strState = "STATE_ON_IDLE";
                break;
            case STATE_ON_INITIALIZED:
                strState = "STATE_ON_INITIALIZED";
                break;
            case STATE_ON_PREPARING:
                strState = "STATE_ON_PREPARING";
                break;
            case STATE_ON_PREPARED:
                strState = "STATE_ON_PREPARED";
                break;
            case STATE_ON_PLAYING:
                strState = "STATE_ON_PLAYING";
                break;
            case STATE_ON_BUFFERING:
                strState = "STATE_ON_BUFFERING";
                break;
            case STATE_ON_PAUSED:
                strState = "STATE_ON_PAUSED";
                break;
            case STATE_ON_STOPPED:
                strState = "STATE_ON_STOPPED";
                break;
            case STATE_ON_END:
                strState = "STATE_ON_END";
                break;
            case STATE_ON_ERROR:
                strState = "STATE_ON_ERROR";
                break;
        }
        return strState;
    }
}
