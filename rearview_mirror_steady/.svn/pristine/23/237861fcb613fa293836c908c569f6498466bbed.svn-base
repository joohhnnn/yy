package com.txznet.music.soundControlModule.logic;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.playerModule.logic.PlayHelper;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.TtsHelper;
import com.txznet.music.utils.TtsUtilWrapper;
import com.txznet.music.utils.UIHelper;
import com.txznet.music.utils.Utils;

/**
 * 声控命令字响应
 * Created by telenewbie on 2016/12/23.
 */

public class SoundCommand implements ISoundCommand {

    //##创建一个单例类##
    private volatile static SoundCommand singleton;

    private SoundCommand() {
    }

    public static SoundCommand getInstance() {
        if (singleton == null) {
            synchronized (SoundCommand.class) {
                if (singleton == null) {
                    singleton = new SoundCommand();
                }
            }
        }
        return singleton;
    }


    @Override
    public byte[] playAudio() {
        try {
            PlayHelper.playRadio(EnumState.Operation.sound);
        } finally {
            Utils.jumpTOMediaPlayerAct(false);
        }
        return null;
    }

    @Override
    public byte[] playMusic() {
        try {
            PlayHelper.playMusic(EnumState.Operation.sound);
        } finally {
            Utils.jumpTOMediaPlayerAct(false);
        }
        return null;
    }


    @Override
    public byte[] playRecommandMusic() {
        return playMusic();
    }

    @Override
    public byte[] pause() {
        PlayEngineFactory.getEngine().pause(EnumState.Operation.sound);
        return null;
    }

    @Override
    public byte[] play() {
        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
        return null;
    }

    @Override
    public byte[] next() {
        PlayEngineFactory.getEngine().next(EnumState.Operation.sound);
        return null;
    }

    @Override
    public byte[] prev() {
        PlayEngineFactory.getEngine().last(EnumState.Operation.sound);
        return null;
    }

    @Override
    public byte[] exit() {
        UIHelper.exit();
        return null;
    }

    @Override
    public byte[] changeSingleMode(@PlayerInfo.PlayerMode int mode) {
        PlayEngineFactory.getEngine().changeMode(EnumState.Operation.sound, mode);
        return null;
    }

    @Override
    public byte[] open() {
        TtsUtilWrapper.speakTextOnRecordWin("RS_VOICE_RS_VOICE_SPEAK_OPEN_PLAYER",
                Constant.RS_VOICE_SPEAK_OPEN_PLAYER, true, new Runnable() {

                    @Override
                    public void run() {
                        Utils.jumpTOMediaPlayerAct(true);
                    }
                });
        return null;
    }

    @Override
    public byte[] favour(byte[] objects) {
        TtsHelper.speakResource("RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION", Constant.RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION);
        return null;
    }

    @Override
    public byte[] playfavour() {
        TtsHelper.speakResource("RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION", Constant.RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION);
        return null;
    }

    @Override
    public byte[] hateAudio() {
        TtsHelper.speakResource("RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION", Constant.RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION);
        return null;
    }


}
