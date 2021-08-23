package com.txznet.music.playerModule.logic;

import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.EnumState.Operation;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.ReportHistory;
import com.txznet.music.report.ReportManager;
import com.txznet.music.report.bean.PlayEvent;

import java.util.List;

public class PlayerEngineReportDecorator implements IPlayerEngine {
    IPlayerEngine engine;
    private TXZAudioPlayer mPlayer;

    public PlayerEngineReportDecorator(IPlayerEngine engine) {
        this.engine = engine;
    }

    @Override
    public void setState(int state) {
//        LogUtil.loge("test::state:::"+state.getValue(),new RuntimeException());
        engine.setState(state);
    }

    @Override
    public int getState() {
        return engine.getState();
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
    }

    @Override
    public void play(Operation operation) {

        if (Operation.manual.equals(operation)) {
//            ReportHelper.getInstance().sendReportData(ReqDataStats.Action.PLAY);
        } else if (Operation.sound.equals(operation)) {
//            ReportHelper.getInstance().sendReportData(ReqDataStats.Action.PLAY_SOUND);
        } else if (Operation.extra.equals(operation)) {
//            ReportHelper.getInstance().sendReportData(ReqDataStats.Action.PLAY_EXTERA);
        } else if (EnumState.Operation.auto.equals(operation)) {
//            ReportHelper.getInstance().sendReportData(ReqDataStats.Action.PLAY_AUTO);
        }
        engine.play(operation);
    }

    @Override
    public void pause(EnumState.Operation operation) {
        ReportEvent.updatePauseStartTime(TimeManager.getInstance().getTimeMillis());
        if (Operation.manual.equals(operation)) {
//            ReportHelper.getInstance().sendReportData(ReqDataStats.Action.PAUSE);
        } else if (EnumState.Operation.sound.equals(operation)) {
//            ReportHelper.getInstance().sendReportData(ReqDataStats.Action.PAUSE_SOUND);
        } else if (EnumState.Operation.extra.equals(operation)) {
//            ReportHelper.getInstance().sendReportData(ReqDataStats.Action.PAUSE_EXTERA);
        }
        engine.pause(operation);
    }

    @Override
    public void playOrPause(Operation operation) {
        engine.playOrPause(operation);
    }

    @Override
    public void playAudio(EnumState.Operation operation, Audio willPlayAudio) {
        engine.playAudio(operation, willPlayAudio);
    }

    @Override
    public void release(EnumState.Operation operation) {
        String type;
        if (EnumState.Operation.manual.equals(operation)) {
            type = PlayEvent.ACTION_SWITCH_MANUAL;
        } else if (EnumState.Operation.sound.equals(operation)) {
            type = PlayEvent.ACTION_SWITCH_SOUND;
        } else {
            type = PlayEvent.ACTION_SWITCH_AUTO;
        }
        long pos = PlayInfoManager.getInstance().getCurrentPosition();
        pos *= 1000;
        pos += Math.random() * 799;
        ReportEvent.reportPlayEvent(type, PlayInfoManager.getInstance().getCurrentAudio(), PlayInfoManager.getInstance().getCurrentAlbum(), pos);
        if (operation != EnumState.Operation.auto) {
            Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
            if (null != currentAudio) {
                ReportManager.getInstance().reportAudioPlay(currentAudio, ReportHistory.TYPE_SITCH);
            }
        }
        engine.release(operation);
        mPlayer = null;
    }

    @Override
    public void next(Operation operation) {
        engine.next(operation);
    }

    @Override
    public void last(Operation operation) {
        engine.last(operation);
    }

    @Override
    public void changeMode(EnumState.Operation operation) {

        engine.changeMode(operation);
    }

    @Override
    public void changeMode(Operation operation, @PlayerInfo.PlayerMode int mode) {

        engine.changeMode(operation, mode);
    }

    @Override
    public void seekTo(Operation operation, long position) {

//        ReportHelper.getInstance().sendReportData(ReqDataStats.Action.ACT_SEEK_START);
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
    public void setAudios(Operation operation, List<Audio> audios, Album album,
                          int index, int ori) {
        engine.setAudios(operation, audios, album, index, ori);
    }

    @Override
    public void addAudios(EnumState.Operation operation, List<Audio> audios,
                          boolean isAddLast) {
        engine.addAudios(operation, audios, isAddLast);
    }

    @Override
    public boolean isPlaying() {
        return engine.isPlaying();
    }


    @Override
    public void prepareAsync(Operation operation) {
        engine.prepareAsync(operation);
    }


    @Override
    public void playAudioList(Operation sound, List<Audio> localAudios, int index, Album album) {
        engine.playAudioList(sound, localAudios, index, album);
    }
}
