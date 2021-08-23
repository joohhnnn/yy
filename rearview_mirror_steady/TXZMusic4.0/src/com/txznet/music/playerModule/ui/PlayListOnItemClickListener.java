package com.txznet.music.playerModule.ui;

import com.txznet.music.albumModule.bean.Audio;

/**
 * Created by brainBear on 2017/12/20.
 */

public interface PlayListOnItemClickListener {

    void onPlay(Audio audio);


    void onFavor(Audio audio, boolean isCancel);

}
