package com.txznet.music.playerModule.logic;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.Error;

import java.util.List;

/**
 * Created by telenewbie on 2017/5/26.
 */


public interface IPlayerStateListener {
    void onIdle(Audio audio);

    /**
     * 结束缓冲
     *
     * @param audio
     */
    void onPlayerPreparing(Audio audio);

    /**
     * 开始第一次播放
     *
     * @param audio
     */
    void onPlayerPrepareStart(Audio audio);

    void onPlayerPlaying(Audio audio);

    void onPlayerPaused(Audio audio);

    void onProgress(Audio audio, long position, long duration);

    void onBufferProgress(Audio audio, List<LocalBuffer> buffers);

    void onPlayerFailed(Audio audio, Error error);

    void onPlayerEnd(Audio audio);

    void onSeekStart(Audio audio);

    void onSeekComplete(Audio audio,long seekTime);

    void onBufferingStart(Audio audio);

    void onBufferingEnd(Audio audio);
}