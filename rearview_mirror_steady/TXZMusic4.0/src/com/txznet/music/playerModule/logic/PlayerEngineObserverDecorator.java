package com.txznet.music.playerModule.logic;

import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.EnumState.Operation;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.TtsHelper;
import com.txznet.music.utils.Utils;

import java.util.List;

public class PlayerEngineObserverDecorator implements IPlayerEngine {
    IPlayerEngine engine;
    private TXZAudioPlayer mPlayer;

    public PlayerEngineObserverDecorator(IPlayerEngine engine) {
        this.engine = engine;
    }

    @Override
    public int getState() {
        return engine.getState();
    }

    @Override
    public void setState(int state) {
        engine.setState(state);
        notifyState();
    }

    @Override
    public TXZAudioPlayer getAudioPlayer() {
        if (mPlayer == null) {
            mPlayer = engine.getAudioPlayer();
        }
        return mPlayer;
    }

    @Override
    public void init() {
        engine.init();
        ObserverManage.getObserver().send(InfoMessage.PLAYER_INIT);
        notifyState();
    }

    private void notifyState() {

        int state = PlayEngineFactory.getEngine().getState();
        switch (state){
            case PlayerInfo.PLAYER_STATUS_BUFFER:
                ObserverManage.getObserver().send(InfoMessage.PLAYER_LOADING);
                break;
            case PlayerInfo.PLAYER_STATUS_RELEASE:
            case PlayerInfo.PLAYER_STATUS_PAUSE:
                ObserverManage.getObserver().send(InfoMessage.PAUSE);
                break;
            case PlayerInfo.PLAYER_STATUS_PLAYING:
                ObserverManage.getObserver().send(InfoMessage.PLAY);
                break;
        }
    }

    @Override
    public void play(EnumState.Operation operation) {
        engine.play(operation);
    }

    @Override
    public void pause(EnumState.Operation operation) {
        engine.pause(operation);
    }

    @Override
    public void playOrPause(Operation operation) {
        ObserverManage.getObserver().send(InfoMessage.BE_PLAY_OR_PAUSE);
        engine.playOrPause(operation);
    }

    @Override
    public void playAudio(Operation operation, Audio willPlayAudio) {
        engine.playAudio(operation, willPlayAudio);
    }

    public Runnable releaseEvent = new Runnable() {
        @Override
        public void run() {
            ObserverManage.getObserver().send(InfoMessage.RELEASE);
        }
    };

    @Override
    public void release(EnumState.Operation operation) {
        engine.release(operation);
        if (operation== Operation.error){
            AppLogic.runOnBackGround(releaseEvent, 2000);
        }else {
            ObserverManage.getObserver().send(InfoMessage.RELEASE);
        }
        mPlayer = null;
    }

    @Override
    public void next(Operation operation) {
        ObserverManage.getObserver().send(InfoMessage.BE_PLAY_NEXT);
        engine.next(operation);

    }

    @Override
    public void last(EnumState.Operation operation) {
        ObserverManage.getObserver().send(InfoMessage.BE_PLAY_LAST);
        engine.last(operation);
    }

    @Override
    public void changeMode(Operation operation) {
        engine.changeMode(operation);
    }

    @Override
    public void changeMode(Operation operation, @PlayerInfo.PlayerMode int mode) {
        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (currentAudio != null && Utils.isSong(currentAudio.getSid())) {
            engine.changeMode(operation, mode);
            notifyMode();
        } else if (operation == Operation.sound && mode != PlayerInfo.PLAYER_MODE_SEQUENCE) {
            //声控播报
            TtsHelper.speakResource("RS_VOICE_SPEAK_CANTSUPPORT_TIPS", Constant.RS_VOICE_SPEAK_CANTSUPPORT_TIPS);
        }
    }

    private void notifyMode() {
        switch (SharedPreferencesUtils.getPlayMode()) {
            case PlayerInfo.PLAYER_MODE_RANDOM:
                ObserverManage.getObserver().send(InfoMessage.PLAYER_MODE_RANDOM);
                break;
            case PlayerInfo.PLAYER_MODE_SEQUENCE:
                ObserverManage.getObserver().send(InfoMessage.PLAYER_MODE_SEQUENCE);
                break;
            case PlayerInfo.PLAYER_MODE_SINGLE_CIRCLE:
                ObserverManage.getObserver().send(InfoMessage.PLAYER_MODE_SINGLE);
                break;
        }
    }

    @Override
    public void seekTo(EnumState.Operation operation, long position) {
        engine.seekTo(operation, position);
    }

    @Override
    public void setVolume(EnumState.Operation operation, float volume) {

        engine.setVolume(operation, volume);
    }

    @Override
    public void searchListData(EnumState.Operation operation, boolean isDown) {

        engine.searchListData(operation, isDown);
    }

    @Override
    public Album getCurrentAlbum() {
        return engine.getCurrentAlbum();
    }

    @Override
    public Audio getCurrentAudio() {
        return engine.getCurrentAudio();
    }

    @Override
    public void setAudios(Operation operation, List<Audio> audios, Album album, int index, int ori) {
        engine.setAudios(operation, audios, album, index ,ori);
        ObserverManage.getObserver().send(InfoMessage.PLAYER_LIST, PlayInfoManager.getInstance().getPlayList());
    }

    @Override
    public void addAudios(EnumState.Operation operation, List<Audio> audios, boolean isAddLast) {
        engine.addAudios(operation, audios, isAddLast);
        ObserverManage.getObserver().send(InfoMessage.PLAYER_LIST, PlayInfoManager.getInstance().getPlayList());
    }

    @Override
    public boolean isPlaying() {
        return engine.isPlaying();
    }


    @Override
    public void prepareAsync(Operation operation) {
        AppLogic.removeBackGroundCallback(releaseEvent);
        engine.prepareAsync(operation);
        ObserverManage.getObserver().send(InfoMessage.PLAYER_CURRENT_AUDIO, PlayInfoManager.getInstance().getCurrentAudio());
        if (PlayInfoManager.getInstance().getCurrentAudio() != null) {
            if (Utils.isSong(PlayInfoManager.getInstance().getCurrentAudio().getSid())) {
                notifyMode();
            }
        }

    }

    @Override
    public void playAudioList(EnumState.Operation sound, List<Audio> localAudios, int index, Album
            album) {
        engine.playAudioList(sound, localAudios, index, album);
    }

}
