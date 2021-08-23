package com.txznet.music.playerModule.logic;

import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;

import java.util.List;

public class PlayerEngingLogDecorator extends APlayerEngingDecorator {
    private static final String TAG = "music:enging:log:";

    public PlayerEngingLogDecorator(IPlayerEngine engine) {
        super(engine);
    }

    @Override
    public int getState() {
        int state = super.getState();
        Logger.d(TAG, "getState:" + state);
        return state;
    }

    @Override
    public void setState(int state) {
        Logger.d(TAG, "setState:" + state);
        super.setState(state);
    }

    @Override
    public TXZAudioPlayer getAudioPlayer() {
        Logger.d(TAG, "getAudioPlayer");
        return super.getAudioPlayer();
    }

    @Override
    public void init() {
        Logger.d(TAG, "init");
        super.init();
    }

    @Override
    public void prepareAsync(EnumState.Operation operation) {
        Logger.d(TAG, "prepareAsync:" + operation);
        super.prepareAsync(operation);
    }

    @Override
    public void play(EnumState.Operation operation) {
        Logger.d(TAG, "play:" + operation);
        super.play(operation);
    }

    @Override
    public void pause(EnumState.Operation operation) {
        Logger.d(TAG, "pause:" + operation);
        super.pause(operation);
    }

    @Override
    public void playOrPause(EnumState.Operation operation) {
        Logger.d(TAG, "playOrPause:" + operation);
        super.playOrPause(operation);
    }

    @Override
    public void playAudio(EnumState.Operation operation, Audio willPlayAudio) {
        Logger.d(TAG, "playAudio:" + operation + ",willplay:" + willPlayAudio);
        super.playAudio(operation, willPlayAudio);
    }

    @Override
    public void release(EnumState.Operation operation) {
        Logger.d(TAG, "release:" + operation);
        super.release(operation);
    }

    @Override
    public void next(EnumState.Operation operation) {
        Logger.d(TAG, "next:" + operation);
        super.next(operation);
    }

    @Override
    public void last(EnumState.Operation operation) {
        Logger.d(TAG, "last:" + operation);
        super.last(operation);
    }

    @Override
    public void setAudios(EnumState.Operation operation, List<Audio> audios, Album album, int index, int ori) {
        Logger.d(TAG, "setAudios:" + operation + "," + index + "," + ori + (album != null ? album.toString() : "album = null"));
        if (Constant.isNeedLog()) {
            Logger.d(TAG, audios);
        }
        super.setAudios(operation, audios, album, index, ori);
    }

    @Override
    public void addAudios(EnumState.Operation operation, List<Audio> audios, boolean isAddLast) {
        Logger.d(TAG, "addAudios:" + operation + ",audios");
        if (Constant.isNeedLog()) {
            Logger.d(TAG, audios);
        }
        super.addAudios(operation, audios, isAddLast);
    }

    @Override
    public void changeMode(EnumState.Operation operation) {
        Logger.d(TAG, "changeMode:" + operation);
        super.changeMode(operation);
    }

    @Override
    public void changeMode(EnumState.Operation operation, int mode) {
        Logger.d(TAG, "changeMode:" + operation + "," + mode);
        super.changeMode(operation, mode);
    }

    @Override
    public void seekTo(EnumState.Operation operation, long position) {
        Logger.d(TAG, "seekTo:" + operation + "," + position);
        super.seekTo(operation, position);
    }

    @Override
    public void setVolume(EnumState.Operation operation, float volume) {
        Logger.d(TAG, "setVolume:" + operation + "," + volume);
        super.setVolume(operation, volume);
    }

    @Override
    public void searchListData(EnumState.Operation operation, boolean isDown) {
        Logger.d(TAG, "searchListData:" + operation + "," + isDown);
        super.searchListData(operation, isDown);
    }

    @Override
    public Album getCurrentAlbum() {
        Logger.d(TAG, "getCurrentAlbum");
        return super.getCurrentAlbum();
    }

    @Override
    public Audio getCurrentAudio() {
        Logger.d(TAG, "getCurrentAudio");
        return super.getCurrentAudio();
    }

    @Override
    public boolean isPlaying() {
        boolean playing = super.isPlaying();
        Logger.d(TAG, "isPlaying:" + playing);
        return playing;
    }

    @Override
    public void playAudioList(EnumState.Operation operation, List<Audio> localAudios, int index, Album album) {
        Logger.d(TAG, "playAudioList:" + operation);
        super.playAudioList(operation, localAudios, index, album);
    }
}
