package com.txznet.music.utils;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.playerModule.logic.IPlayerStateListener;
import com.txznet.music.playerModule.logic.PlayerInfoUpdateListener;

import java.util.List;

/**
 * Created by telenewbie on 2017/9/18.
 */

public class SyncOtherAppBroadcastListener implements IPlayerStateListener, PlayerInfoUpdateListener {
    @Override
    public void onIdle(Audio audio) {
        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_PAUSED);
    }

    @Override
    public void onPlayerPreparing(Audio audio) {
//        PlayerCommunicationManager.getInstance().sendPlayItemChanged(audio);
    }

    @Override
    public void onPlayerPrepareStart(Audio audio) {
    }

    @Override
    public void onPlayerPlaying(Audio audio) {
        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_PLAYING);
    }

    @Override
    public void onPlayerPaused(Audio audio) {
        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_PAUSED);
    }

    @Override
    public void onProgress(Audio audio, long position, long duration) {
        PlayerCommunicationManager.getInstance().sendProgressChanged((int) position, (int) duration);
    }

    @Override
    public void onBufferProgress(Audio audio, List<LocalBuffer> buffers) {

    }

    @Override
    public void onPlayerFailed(Audio audio, Error error) {
        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_FAILED);
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
        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_BUFFERING);
    }

    @Override
    public void onBufferingEnd(Audio audio) {

    }

    @Override
    public void onPlayInfoUpdated(Audio audio, Album album) {
        PlayerCommunicationManager.getInstance().sendPlayItemChanged(audio);
    }

    @Override
    public void onProgressUpdated(long position, long duration) {

    }

    @Override
    public void onPlayerModeUpdated(int mode) {

    }

    @Override
    public void onPlayerStatusUpdated(int status) {
        PlayerCommunicationManager.getInstance().sendPlayerUIStatus(status);
    }

    @Override
    public void onBufferProgressUpdated(List<LocalBuffer> buffers) {

    }

    @Override
    public void onFavourStatusUpdated(int favourState) {

    }
}
