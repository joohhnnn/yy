package com.txznet.music.playerModule.logic;

import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.EnumState;

import java.util.List;

public class APlayerEngingDecorator implements IPlayerEngine {
    IPlayerEngine engine;

    public APlayerEngingDecorator(IPlayerEngine engine) {
        this.engine = engine;
    }

    @Override
    public int getState() {
        return engine.getState();
    }

    @Override
    public void setState(int state) {
        engine.setState(state);
    }

    @Override
    public TXZAudioPlayer getAudioPlayer() {
        return engine.getAudioPlayer();
    }

    @Override
    public void init() {
        engine.init();
    }

    @Override
    public void prepareAsync(EnumState.Operation operation) {
        engine.prepareAsync(operation);
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
    public void playOrPause(EnumState.Operation operation) {
        engine.playOrPause(operation);
    }

    @Override
    public void playAudio(EnumState.Operation operation, Audio willPlayAudio) {
        engine.playAudio(operation, willPlayAudio);
    }

    @Override
    public void release(EnumState.Operation operation) {
        engine.release(operation);
    }

    @Override
    public void next(EnumState.Operation operation) {
        engine.next(operation);
    }

    @Override
    public void last(EnumState.Operation operation) {
        engine.last(operation);
    }

    @Override
    public void setAudios(EnumState.Operation operation, List<Audio> audios, Album album, int index, int ori) {
        engine.setAudios(operation, audios, album, index, ori);
    }

    @Override
    public void addAudios(EnumState.Operation operation, List<Audio> audios, boolean isAddLast) {
        engine.addAudios(operation, audios, isAddLast);
    }

    @Override
    public void changeMode(EnumState.Operation operation) {
        engine.changeMode(operation);
    }

    @Override
    public void changeMode(EnumState.Operation operation, int mode) {
        engine.changeMode(operation, mode);
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
    public boolean isPlaying() {
        return engine.isPlaying();
    }

    @Override
    public void playAudioList(EnumState.Operation sound, List<Audio> localAudios, int index, Album album) {
        engine.playAudioList(sound, localAudios, index, album);
    }
}
