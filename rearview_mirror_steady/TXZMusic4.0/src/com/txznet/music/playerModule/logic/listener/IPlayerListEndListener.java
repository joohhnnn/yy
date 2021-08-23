package com.txznet.music.playerModule.logic.listener;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;

/**
 * 播放列表结束时候的回调
 */
public interface IPlayerListEndListener {

    /**
     * 播放到最后一个时候的，当前的专辑，以及最后的一个音频
     *
     * @param audio
     * @param album
     * @return 是否需要拦截该处理
     */
    boolean onListEnd(Audio audio, Album album);
}
