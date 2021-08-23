package com.txznet.music.playerModule.logic;

import com.txznet.music.albumModule.bean.Audio;

/**
 * Created by brainBear on 2017/8/9.
 */

public interface ISwitchAudioListener {

    public static final int ERROR_IS_FIRST = -1;
    public static final int ERROR_IS_END = -2;
    public static final int ERROR_NULL_ALBUM = -3;
    public static final int ERROR_EMPTY_DATA = -4;

    void onAudioReady(Audio audio);


    void onAudioUnavailable(int errorCode);

}
