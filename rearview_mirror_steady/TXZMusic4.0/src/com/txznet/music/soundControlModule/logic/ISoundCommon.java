package com.txznet.music.soundControlModule.logic;

/**
 * Created by telenewbie on 2016/12/23.
 */

public interface ISoundCommon extends ISound {

    /**
     * 当前是否播放
     * @return
     */
    public byte[] isPlaying();

    /**
     * 版本
     * @return
     */
    public byte[] getVersion();
}
