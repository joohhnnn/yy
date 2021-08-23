package com.txznet.music.playerModule.logic;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.PlayerInfo;

import java.util.List;

/**
 * Created by brainBear on 2017/12/4.
 */

public interface PlayerInfoUpdateListener {

    /**
     * 当前播放音频改变时候回调
     *
     * @param audio 新播放的音频
     * @param album 所在Album
     */
    void onPlayInfoUpdated(Audio audio, Album album);

    /**
     * 当播放进度更新时回调
     *
     * @param position 当前时长
     * @param duration 总时长
     */
    void onProgressUpdated(long position, long duration);

    /**
     * 播放模式更新时回调
     *
     * @param mode 播放模式
     */
    void onPlayerModeUpdated(@PlayerInfo.PlayerMode int mode);

    /**
     * 播放状态更新时回调
     *
     * @param status 播放状态
     */
    void onPlayerStatusUpdated(@PlayerInfo.PlayerUIStatus int status);

    /**
     * 缓冲数据更新时回调
     *
     * @param buffers 缓冲数据
     */
    void onBufferProgressUpdated(List<LocalBuffer> buffers);

//    /**
//     * 当播放完成
//     *
//     * @param audio
//     */
//    void onPlayComplete(Audio audio);

    /**
     * @param favourState 收藏状态 一共四位
     */
    void onFavourStatusUpdated(int favourState);
}
