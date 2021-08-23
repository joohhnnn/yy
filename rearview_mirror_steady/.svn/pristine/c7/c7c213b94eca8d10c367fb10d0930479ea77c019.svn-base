package com.txznet.music.playerModule.logic;

import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.EnumState.Operation;
import com.txznet.music.baseModule.bean.PlayerInfo;

import java.util.List;

/**
 * MV(p)统一处理处 内含多个p
 */
public interface IPlayerEngine {
    @PlayerInfo.PlayerStatus
    int getState();

    void setState(@PlayerInfo.PlayerStatus int state);

    TXZAudioPlayer getAudioPlayer();

    void init();

    void prepareAsync(EnumState.Operation operation);

    void play(EnumState.Operation operation);

    void pause(EnumState.Operation operation);

    void playOrPause(Operation operation);

    void playAudio(Operation operation, Audio willPlayAudio);

    void release(EnumState.Operation operation);

    void next(Operation operation);

    void last(Operation operation);

    boolean isNextOperationLastTime();

    void setAudios(Operation operation, final List<Audio> audios,
                   Album album, int index, int ori);

    void addAudios(Operation operation, final List<Audio> audios,
                   boolean isAddLast);

    void changeMode(EnumState.Operation operation);

    void changeMode(EnumState.Operation operation, @PlayerInfo.PlayerMode int mode);

    //毫秒数
    void seekTo(Operation operation, long position);

    void setVolume(EnumState.Operation operation, float volume);

    // XXX:以下方法，是否可以使用静态类，或者其他方式搞定？

    void searchListData(EnumState.Operation operation, boolean isDown);

    Album getCurrentAlbum();

    Audio getCurrentAudio();

    boolean isPlaying();

    void playAudioList(EnumState.Operation sound, List<Audio> localAudios, int index, Album album);
}
