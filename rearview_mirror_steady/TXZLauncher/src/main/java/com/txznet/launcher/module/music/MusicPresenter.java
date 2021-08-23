package com.txznet.launcher.module.music;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.domain.music.IMusicInfoChangedListener;
import com.txznet.launcher.domain.music.MusicManager;
import com.txznet.launcher.domain.music.bean.PlayInfo;

/**
 * Created by brainBear on 2018/2/23.
 */

public class MusicPresenter implements MusicContract.Presenter, IMusicInfoChangedListener {

    private static final String TAG = "MusicPresenter";
    private MusicContract.View mView;

    public MusicPresenter(MusicContract.View view) {
        mView = view;
    }


    @Override
    public void playOrPause() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public void play() {
        MusicManager.getInstance().play();
    }

    @Override
    public void pause() {
        MusicManager.getInstance().pause();
    }

    @Override
    public void next() {
        MusicManager.getInstance().playNext();
    }

    @Override
    public void prev() {
        MusicManager.getInstance().playPrevious();
    }

    @Override
    public boolean isPlaying() {
        return MusicManager.getInstance().isPlaying();
    }

    @Override
    public void refreshData() {

        PlayInfo currentPlayInfo = MusicManager.getInstance().getCurrentPlayInfo();
        if (null != currentPlayInfo) {
            mView.updatePlayInfo(currentPlayInfo);
        }

        int currentPlayState = MusicManager.getInstance().getCurrentPlayState();
        mView.updatePlayStatus(currentPlayState);
    }

    @Override
    public void attach() {
        MusicManager.getInstance().addMusicInfoChangedListener(this);
    }

    @Override
    public void detach() {
        MusicManager.getInstance().removeMusicInfoChangedListener(this);
        mView = null;
    }

    @Override
    public void onPlayStateChanged(int state) {
        LogUtil.d(TAG, "state=" + state);
        mView.updatePlayStatus(state);
    }

    @Override
    public void onPlayInfoChanged(PlayInfo playInfo) {
        LogUtil.d(TAG, playInfo.toString());
        mView.updatePlayInfo(playInfo);
    }

    @Override
    public void onPlayProgressChanged(long progress, long duration) {
        LogUtil.d(TAG, "progress:" + progress + " duration:" + duration);
        mView.updatePlayProgress(progress, duration);
    }
}
