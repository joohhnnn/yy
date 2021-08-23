package com.txznet.music.playerModule.logic;

import com.txznet.music.albumModule.bean.Audio;

import java.util.List;

/**
 * Created by brainBear on 2017/7/5.
 */

public interface IPlayListChangedListener {

    void onPlayListChanged(List<Audio> audios);
}
