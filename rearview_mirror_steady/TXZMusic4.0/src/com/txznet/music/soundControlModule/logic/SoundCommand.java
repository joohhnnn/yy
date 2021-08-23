package com.txznet.music.soundControlModule.logic;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.data.dao.DaoManager;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.playerModule.logic.PlayHelper;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.ui.HomeActivity;
import com.txznet.music.utils.TtsHelper;
import com.txznet.music.utils.UIHelper;
import com.txznet.music.utils.Utils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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
            PlayHelper.playRadio(PlayInfoManager.DATA_ALBUM, EnumState.Operation.sound);
        } finally {
            Utils.jumpTOMediaPlayerAct(false);
        }
        return null;
    }

    @Override
    public byte[] playMusic(byte[] data) {
        if (data != null) {
            //如果里面有值的话
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            String key = jsonBuilder.getVal("target", String.class, "");
            if ("audio".equals(key)) {
                playAudio();
//                handleAudioInvoke(packageName, command, data);
                return null;
            }
        }

        try {
            PlayHelper.playMusic(PlayInfoManager.DATA_ALBUM, EnumState.Operation.sound);
        } finally {
            Utils.jumpTOMediaPlayerAct(true);
        }
        return null;
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
    public byte[] changeMode(EnumState.Operation operation) {
        PlayEngineFactory.getEngine().changeMode(operation);
        return null;
    }

    @Override
    public byte[] open() {
        Utils.jumpTOMediaPlayerAct(true);
        return null;
    }

    @Override
    public byte[] favour(byte[] objects) {
        JSONBuilder jsonBuilder = new JSONBuilder(objects);
        Boolean favour = Boolean.parseBoolean(jsonBuilder.getVal("favour", String.class, "false"));
        final Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
        final Album currentAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();
        if (currentAudio != null) {
            if (Utils.isSong(currentAudio.getSid())) {
                if (favour) {
                    FavorHelper.favor(currentAudio, EnumState.Operation.sound);
                } else {
                    FavorHelper.unfavor(currentAudio, EnumState.Operation.sound);
                }
            } else {
                if (currentAlbum != null) {
                    if (favour) {
                        FavorHelper.subscribeRadio(currentAlbum, EnumState.Operation.sound);
                    } else {
                        FavorHelper.unSubscribeRadio(currentAlbum, EnumState.Operation.sound);
                    }
                }
            }
        } else {
            TtsUtil.speakTextOnRecordWin("当前还没有播放任何歌曲", true, null);
        }
        return null;
    }

    @Override
    public byte[] playfavour() {
        TtsHelper.speakResource("RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION", Constant.RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION);
        return null;
    }

    @Override
    public byte[] openLocal() {
        Utils.jumpTOMediaPlayerAct(true, HomeActivity.LOCAL_i);
        return null;
    }

    @Override
    public void playLocalAudios() {
        cleanDisposable();

        disposable = Observable.create(new ObservableOnSubscribe<List<Audio>>() {
            @Override
            public void subscribe(ObservableEmitter<List<Audio>> e) throws Exception {
                e.onNext(DaoManager.getInstance().findAllLocalAudios());
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposableConsumer)
                .subscribe(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {
                        PlayEngineFactory.getEngine().setAudios(EnumState.Operation.sound, audios, null, 0, PlayInfoManager.DATA_LOCAL);
                        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_PLAY_LOCAL_UNKNOW", Constant.RS_VOICE_SPEAK_PLAY_LOCAL_UNKNOW, true, null);
                    }
                });
    }

    private Consumer<Throwable> throwableConsumer = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) throws Exception {
            LogUtil.loge("music:local", throwable);
            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_TIPS_UNKNOWN", Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN, true, null);
        }
    };
    private Consumer<Disposable> disposableConsumer = new Consumer<Disposable>() {
        @Override
        public void accept(Disposable disposable) throws Exception {
            TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_SOUND_OK", Constant.RS_VOICE_SPEAK_SOUND_OK, true, null);
        }
    };


    @Override
    public byte[] hateAudio() {
        TtsHelper.speakResource("RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION", Constant.RS_VOICE_SPEAK_SUPPORT_NOT_FUNCTION);
        return null;
    }


    Disposable disposable = null;

    private void cleanDisposable() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }


    public void playHistoryAudios() {
        cleanDisposable();

        disposable = Observable.create(new ObservableOnSubscribe<List<HistoryData>>() {
            @Override
            public void subscribe(ObservableEmitter<List<HistoryData>> e) throws Exception {
                e.onNext(DBManager.getInstance().findMusicHistory());
                e.onComplete();
            }
        }).map(new Function<List<HistoryData>, List<Audio>>() {
            @Override
            public List<Audio> apply(List<HistoryData> historyData) throws Exception {
                return DBManager.getInstance().convertHistoryDataToAudios(historyData);
            }
        }).doOnSubscribe(disposableConsumer)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Audio>>() {
                    @Override
                    public void accept(List<Audio> audios) throws Exception {
                        PlayEngineFactory.getEngine().setAudios(EnumState.Operation.auto, audios, null, 0, PlayInfoManager.DATA_HISTORY);
                    }
                }, throwableConsumer);
    }
}
