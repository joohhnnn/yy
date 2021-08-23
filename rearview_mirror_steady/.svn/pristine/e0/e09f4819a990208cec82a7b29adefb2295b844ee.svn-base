package com.txznet.music.util;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.SessionManager;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.playerModule.logic.IPlayerStateListener;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.Objects;

import java.util.List;

public class PreLoadListener implements IPlayerStateListener {


    //##创建一个单例类##
    private volatile static PreLoadListener singleton;

    private PreLoadListener() {
    }

    public static PreLoadListener getInstance() {
        if (singleton == null) {
            synchronized (PreLoadListener.class) {
                if (singleton == null) {
                    singleton = new PreLoadListener();
                }
            }
        }
        return singleton;
    }

    private boolean isNeedNextAudioPreloadData;

    @Override
    public void onIdle(Audio audio) {

    }

    @Override
    public void onPlayerPreparing(Audio audio) {
        isNeedNextAudioPreloadData = true;
    }

    @Override
    public void onPlayerPrepareStart(Audio audio) {

    }

    @Override
    public void onPlayerPlaying(Audio audio) {

    }

    @Override
    public void onPlayerPaused(Audio audio) {

    }

    @Override
    public void onProgress(Audio audio, long position, long duration) {
        int needPosition = TXZAudioPlayer.NEED_BUFFER_DATA_TIME / 1000 * Constant.TIME_UNIT;
        if (isNeedNextAudioPreloadData && ((position + needPosition) >= duration)) {
            LogUtil.logd(Constant.PRELOAD_TAG + "position:" + position + ",duration=" + duration + ",needposition:" + (position + needPosition) + Objects.getObj2String(audio));
            Audio nextAudio = PlayInfoManager.getInstance().getNextAudio(false);
            if (nextAudio != audio) {
                isNeedNextAudioPreloadData = false;
                SessionManager.preloadData(nextAudio);
            }
        }
    }

    @Override
    public void onBufferProgress(Audio audio, List<LocalBuffer> buffers) {

    }

    @Override
    public void onPlayerFailed(Audio audio, Error error) {

    }

    @Override
    public void onPlayerEnd(Audio audio) {

    }

    @Override
    public void onSeekStart(Audio audio) {

    }

    @Override
    public void onSeekComplete(Audio audio, long seekTime) {

    }

    @Override
    public void onBufferingStart(Audio audio) {

    }

    @Override
    public void onBufferingEnd(Audio audio) {

    }
}
