package com.txznet.music.soundControlModule.asr;

import android.content.res.Resources;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.config.ConfigManager;
import com.txznet.music.data.http.AlbumRepository;
import com.txznet.music.data.http.TXZFunction;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.listener.WinListener;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.PlayerInfoUpdateListener;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.CarFmUtils;
import com.txznet.music.ui.PlayRadioRecPresenter;
import com.txznet.music.soundControlModule.logic.SoundCommand;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.music.utils.JumpUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.Utils;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by ASUS User on 2017/4/22.
 */

public class AsrManager implements PlayerInfoUpdateListener {

    private static final String TASK_ID_MUSIC_PLAY_STATUS_CONTROL = "TASK_ID_MUSIC_PLAY_STATUS_CONTROL";
    private static final String TASK_ID_MUSIC_PAUSE_STATUS_CONTROL = "TASK_ID_MUSIC_PAUSE_STATUS_CONTROL";
    private static final String TASK_ID_MUSIC_COMM_STATUS_CONTROL = "TASK_ID_MUSIC_COMM_STATUS_CONTROL";

    private static final String TAG = "Music:Asr:";
    private static final String ASR_CMD_PLAYER_PAUSE = "ASR_CMD_PLAYER_PAUSE";
    private static final String ASR_CMD_PLAYER_PLAY = "ASR_CMD_PLAYER_PLAY";
    private static final String ASR_CMD_PLAYER_NEXT = "ASR_CMD_PLAYER_NEXT";
    private static final String ASR_CMD_PLAYER_PREVIOUS = "ASR_CMD_PLAYER_PREVIOUS";
    private static final String ASR_CMD_PLAYER_FORWARD = "ASR_CMD_PLAYER_FORWARD";
    private static final String ASR_CMD_PLAYER_BACKWARD = "ASR_CMD_PLAYER_BACKWARD";
    private static final String ASR_CMD_PLAYER_LIKE = "ASR_CMD_PLAYER_LIKE";
    private static final String ASR_CMD_PLAYER_LIKE_CANCEL = "ASR_CMD_PLAYER_LIKE_CANCEL";
    private static final String ASR_CMD_OPEN_SUPER_RADIO = "ASR_CMD_OPEN_SUPER_RADIO";
    private static final String ASR_CMD_PLAY_LOCAL_MUSIC = "ASR_CMD_PLAY_LOCAL_MUSIC";
    private static final String ASR_CMD_PLAY_HISTORY_MUSIC = "ASR_CMD_PLAY_HISTORY_MUSIC";
    private static AsrManager sInstance = null;

    TXZAsrManager.CommandListener commandListener = null;
    //    Observable<Integer> just = Observable.just(1);;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AsrManager() {
        PlayInfoManager.getInstance().addPlayerInfoUpdateListener(this);

        commandListener = new TXZAsrManager.CommandListener() {
            @Override
            public void onCommand(String cmd, String data) {
                Logger.d(TAG, "-command:" + cmd + " isWakeupResult " + data);
                if (ASR_CMD_OPEN_SUPER_RADIO.equals(data)) {
                    //打开车主超级电台
                    TtsUtil.speakTextOnRecordWin("", false, false, null);
                    openCarFm();
                } else if (ASR_CMD_PLAY_LOCAL_MUSIC.equals(data)) {
                    SoundCommand.getInstance().playLocalAudios();
                } else if (ASR_CMD_PLAY_HISTORY_MUSIC.equals(data)) {
                    SoundCommand.getInstance().playHistoryAudios();
                }
            }
        };
    }

    public static AsrManager getInstance() {
        if (sInstance == null) {
            synchronized (AsrManager.class) {
                if (sInstance == null) {
                    sInstance = new AsrManager();
                }
            }
        }
        return sInstance;
    }


    private void regCommCMD() {
        Logger.d(TAG, "reg comm cmd:" + ConfigManager.getInstance().isEnableWakeup() + " " + WinListener.isShowSoundUI);
        if (!ConfigManager.getInstance().isEnableWakeup() || WinListener.isShowSoundUI) {
            return;
        }
        TXZAsrManager.AsrComplexSelectCallback asrCallBack = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(String type, String command) {
                Logger.d(TAG, type + "-command:" + command + " isWakeupResult " + isWakeupResult());
                int wakeupCount = SharedPreferencesUtils.getWakeupCount();
                if (isWakeupResult()) {
                    wakeupCount++;
                    if (wakeupCount < 4) {
                        SharedPreferencesUtils.setWakeupCount(wakeupCount);
                    }
                }
                switch (type) {
                    case ASR_CMD_PLAYER_NEXT:
                        if (PlayEngineFactory.getEngine().isPlaying()) {
                            PlayerAboutAsrManager.getInstance().broadcastTTS();
                            PlayEngineFactory.getEngine().next(EnumState.Operation.sound);
                            ReportEvent.clickSoundNext(PlayInfoManager.getInstance().getCurrentAudio());
                        }
                        break;
                    case ASR_CMD_PLAYER_PREVIOUS:
                        if (PlayEngineFactory.getEngine().isPlaying()) {
                            PlayerAboutAsrManager.getInstance().broadcastTTS();
                            PlayEngineFactory.getEngine().last(EnumState.Operation.sound);
                            ReportEvent.clickSoundPrev(PlayInfoManager.getInstance().getCurrentAudio());
                        }
                        break;
                    case ASR_CMD_PLAYER_LIKE:
                        if (PlayEngineFactory.getEngine().isPlaying()) {
                            final Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
                            final Album currentAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();
                            if (Utils.isSong(currentAudio.getSid())) {
                                if (currentAudio == null) {
                                    return;
                                }
                                FavorHelper.favor(currentAudio, EnumState.Operation.sound);
                            } else {
                                if (currentAlbum == null) {
                                    return;
                                }
                                FavorHelper.subscribeRadio(currentAlbum, EnumState.Operation.sound);
                            }
                            ReportEvent.clickSoundAddFavour(PlayInfoManager.getInstance().getCurrentAudio());
                        }
                        break;
                    case ASR_CMD_PLAYER_LIKE_CANCEL:

                        if (PlayEngineFactory.getEngine().isPlaying()) {
                            final Audio currentAudio = PlayEngineFactory.getEngine().getCurrentAudio();
                            final Album currentAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();
                            if (Utils.isSong(currentAudio.getSid())) {
                                if (currentAudio == null) {
                                    return;
                                }
                                FavorHelper.unfavor(currentAudio, EnumState.Operation.sound);
                            } else {
                                if (currentAlbum == null) {
                                    return;
                                }
                                FavorHelper.unSubscribeRadio(currentAlbum, EnumState.Operation.sound);
                            }
                            ReportEvent.clickSoundUnFavour(PlayInfoManager.getInstance().getCurrentAudio());
                        }
                        break;
                }
            }

            @Override
            public String getTaskId() {
                return TASK_ID_MUSIC_COMM_STATUS_CONTROL;
            }

            @Override
            public boolean needAsrState() {
                return false;
            }
        };
        Resources resources = GlobalContext.get().getResources();
        asrCallBack.addCommand(ASR_CMD_PLAYER_NEXT, resources.getStringArray(R.array.asr_cmd_player_next));
        asrCallBack.addCommand(ASR_CMD_PLAYER_PREVIOUS, resources.getStringArray(R.array.asr_cmd_player_previous));
        asrCallBack.addCommand(ASR_CMD_PLAYER_LIKE, resources.getStringArray(R.array.asr_cmd_player_like));
        asrCallBack.addCommand(ASR_CMD_PLAYER_LIKE_CANCEL, resources.getStringArray(R.array.asr_cmd_player_like_cancel));
        AsrUtil.useWakeupAsAsr(asrCallBack);
    }

    private void regPlayStatusCMD() {
        Logger.d(TAG, "reg play cmd:" + ConfigManager.getInstance().isEnableWakeup() + " " + WinListener.isShowSoundUI);
        if (!ConfigManager.getInstance().isEnableWakeup() || WinListener.isShowSoundUI) {
            return;
        }
        TXZAsrManager.AsrComplexSelectCallback asrCallBack = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(String type, String command) {
                Logger.d(TAG, type + "-command:" + command + " isWakeupResult " + isWakeupResult());
                int wakeupCount = SharedPreferencesUtils.getWakeupCount();
                if (isWakeupResult()) {
                    wakeupCount++;
                    if (wakeupCount < 4) {
                        SharedPreferencesUtils.setWakeupCount(wakeupCount);
                    }
                }
                switch (type) {
                    case ASR_CMD_PLAYER_PAUSE:
                        if (PlayEngineFactory.getEngine().isPlaying()) {
                            if (wakeupCount > 2) {
                                TtsUtil.speakResource("RS_VOICE_SPEAK_PLAY_PAUSE", Constant.RS_VOICE_SPEAK_PLAY_PAUSE, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().pause(EnumState.Operation.sound);
                                    }
                                });
                            } else {
                                TtsUtil.speakResource("RS_VOICE_MUSIC_WAKEUP_PAUSE", Constant.RS_VOICE_MUSIC_WAKEUP_PAUSE, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().pause(EnumState.Operation.sound);
                                    }
                                });
                            }
                            ReportEvent.clickSoundPause(PlayInfoManager.getInstance().getCurrentAudio());
                        }
                        break;
                }
            }

            @Override
            public String getTaskId() {
                return TASK_ID_MUSIC_PLAY_STATUS_CONTROL;
            }

            @Override
            public boolean needAsrState() {
                return false;
            }
        };
        Resources resources = GlobalContext.get().getResources();
        asrCallBack.addCommand(ASR_CMD_PLAYER_PAUSE, resources.getStringArray(R.array.asr_cmd_player_pause));
        AsrUtil.useWakeupAsAsr(asrCallBack);
    }

    private void regPauseStatusCMD() {
        Logger.d(TAG, "reg pause cmd:" + ConfigManager.getInstance().isEnableWakeup() + " " + WinListener.isShowSoundUI);
        if (!ConfigManager.getInstance().isEnableWakeup() || WinListener.isShowSoundUI) {
            return;
        }
        TXZAsrManager.AsrComplexSelectCallback asrCallBack = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(String type, String command) {
                Logger.d(TAG, type + "-command:" + command + " isWakeupResult " + isWakeupResult());
                int wakeupCount = SharedPreferencesUtils.getWakeupCount();
                if (isWakeupResult()) {
                    wakeupCount++;
                    if (wakeupCount < 4) {
                        SharedPreferencesUtils.setWakeupCount(wakeupCount);
                    }
                }
                switch (type) {
                    case ASR_CMD_PLAYER_PLAY:
                        if (!PlayEngineFactory.getEngine().isPlaying()) {
                            if (wakeupCount > 2) {
                                TtsUtil.speakResource("RS_VOICE_SPEAK_PLAY_PLAY", Constant.RS_VOICE_SPEAK_PLAY_PLAY, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
                                    }
                                });
                            } else {
                                TtsUtil.speakResource("RS_VOICE_MUSIC_WAKEUP_PLAY", Constant.RS_VOICE_MUSIC_WAKEUP_PLAY, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
                                    }
                                });
                            }
                            ReportEvent.clickSoundPlay(PlayInfoManager.getInstance().getCurrentAudio());

                        }
                        break;
                }
            }

            @Override
            public String getTaskId() {
                return TASK_ID_MUSIC_PAUSE_STATUS_CONTROL;
            }

            @Override
            public boolean needAsrState() {
                return false;
            }
        };
        Resources resources = GlobalContext.get().getResources();
        asrCallBack.addCommand(ASR_CMD_PLAYER_PLAY, resources.getStringArray(R.array.asr_cmd_player_play));
        AsrUtil.useWakeupAsAsr(asrCallBack);
    }


    private void unregCommCMD() {
        Logger.d(TAG, "unreg comm cmd");
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_MUSIC_COMM_STATUS_CONTROL);
    }

    private void unregPlayStatusCMD() {
        Logger.d(TAG, "unreg play cmd");
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_MUSIC_PLAY_STATUS_CONTROL);
    }

    private void unregPauseStatusCMD() {
        Logger.d(TAG, "unreg pause cmd");
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_MUSIC_PAUSE_STATUS_CONTROL);
    }


    public void regCMD() {
        int currentPlayerUIStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
        if (currentPlayerUIStatus != PlayerInfo.PLAYER_UI_STATUS_PLAYING) {
            regCommCMD();
            unregPlayStatusCMD();
            regPauseStatusCMD();
        } else {
            regCommCMD();
            unregPauseStatusCMD();
            regPlayStatusCMD();
        }
    }

    public void unregCMD() {
        unregCommCMD();
        unregPauseStatusCMD();
        unregPlayStatusCMD();
    }


    /**
     * 注册唤醒声控之后的词汇
     */
    public void regSoundUiCmd() {
        regSpecialAlbumCommand();
        if (FileConfigUtil.getBooleanConfig(FileConfigUtil.KEY_MUSIC_OPEN_LOCAL_MUSIC, true)) {
            regPlayLocalMusic();
        }
        if (FileConfigUtil.getBooleanConfig(FileConfigUtil.KEY_MUSIC_OPEN_HISTORY_MUSIC, true)) {
            regPlayHistoryMusic();
        }

    }

    /**
     * 反注册唤醒声控之后的词汇
     */
    public void unRegSoundUiCmd() {
        unRegSpecialAlbumCommand();
    }

    @Override
    public void onPlayInfoUpdated(Audio audio, Album album) {
    }

    @Override
    public void onProgressUpdated(long position, long duration) {
    }

    @Override
    public void onPlayerModeUpdated(int mode) {
    }

    @Override
    public void onPlayerStatusUpdated(int status) {
        if (status != PlayerInfo.PLAYER_UI_STATUS_PLAYING) {
            regCommCMD();
            unregPlayStatusCMD();
            regPauseStatusCMD();
        } else {
            regCommCMD();
            unregPauseStatusCMD();
            regPlayStatusCMD();
        }
    }

    @Override
    public void onBufferProgressUpdated(List<LocalBuffer> buffers) {

    }

    @Override
    public void onFavourStatusUpdated(int favour) {

    }


    /**
     * 注册特殊专辑的指令
     */
    private void regSpecialAlbumCommand() {
        final Resources resources = GlobalContext.get().getResources();
        TXZAsrManager.getInstance().addCommandListener(commandListener);//这里不能使用内部类，否则，可能导致实例化多个监听，导致回调多次
        TXZAsrManager.getInstance().regCommand(resources.getStringArray(R.array.asr_cmd_open_super_radio), ASR_CMD_OPEN_SUPER_RADIO);
    }


    private void regPlayLocalMusic() {
        final Resources resources = GlobalContext.get().getResources();
        TXZAsrManager.getInstance().addCommandListener(commandListener);//这里不能使用内部类，否则，可能导致实例化多个监听，导致回调多次
        TXZAsrManager.getInstance().regCommand(resources.getStringArray(R.array.asr_cmd_play_local_music), ASR_CMD_PLAY_LOCAL_MUSIC);
    }

    private void regPlayHistoryMusic() {
        final Resources resources = GlobalContext.get().getResources();
        TXZAsrManager.getInstance().addCommandListener(commandListener);//这里不能使用内部类，否则，可能导致实例化多个监听，导致回调多次
        TXZAsrManager.getInstance().regCommand(resources.getStringArray(R.array.asr_cmd_play_history_music), ASR_CMD_PLAY_HISTORY_MUSIC);
    }

    private void unRegSpecialAlbumCommand() {
        TXZAsrManager.getInstance().unregCommand(GlobalContext.get().getResources().getStringArray(R.array.asr_cmd_open_super_radio));
    }

    public void cancelRequest() {
        Logger.d(TAG, "asr:cancelRequest");
        if (compositeDisposable != null) {
//            compositeDisposable.dispose();
            compositeDisposable.clear();
        }

    }

    public void openCarFm() {
        long carFmId = 4295967991L;//车主超级电台，写死（我明明知道后面会改，但是这里还是这么写，我是有多蠢）
//        cancelRequest();

        final Disposable delayTimeOut = Observable.just(1).delay(5, TimeUnit.SECONDS).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                TtsUtil.speakTextOnRecordWin("RS_VOICE_MUSIC_FM_LOADING", Constant.RS_VOICE_MUSIC_FM_LOADING, null, false, false, null);
            }
        });
        compositeDisposable.add(delayTimeOut);
        Disposable subscribe = AlbumRepository.getInstance().getAlbums(carFmId, 1, false).flatMap(new Function<List<Album>, ObservableSource<Album>>() {

            @Override
            public ObservableSource<Album> apply(List<Album> albums) throws Exception {
                if (albums.isEmpty()) {
                    //当下架的时候，此处会获取不到数据
                    throw new IllegalAccessException("Albums is null,can't enter carFm");
                }
                PlayInfoManager.setCarFmAlbums(albums);
                Album currTimeAlbum = CarFmUtils.getInstance().getNeedPlayAlbum(albums);
                if (currTimeAlbum != null) {
                    return Observable.just(currTimeAlbum);
                } else {
                    return Observable.error(new IllegalAccessException());
                }
            }
        })

                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        delayTimeOut.dispose();
//                        compositeDisposable.remove(delayTimeOut);
                        Logger.d(TAG, "openCarFm:finish");
                    }
                })

                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        delayTimeOut.dispose();
                        Logger.d(TAG, "openCarFm:doOnDispose");
                    }
                })
                .firstElement()
                .subscribe(new Consumer<Album>() {
                    @Override
                    public void accept(final Album album) throws Exception {
                        //可以正常跳转到车主FM界面
                        TtsUtil.speakTextOnRecordWin("RS_VOICE_MUSIC_FM_OPEN_CAR_FM_SUCCESS", Constant.RS_VOICE_MUSIC_FM_OPEN_CAR_FM_SUCCESS, true, new Runnable() {
                            @Override
                            public void run() {
                                ReportEvent.reportClickCarFM(String.valueOf(ReportEvent.TYPE_SOUND));
                                JumpUtils.getInstance().jumpToPlayUi(GlobalContext.get(), album);
                            }
                        });

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        //不能跳转到车主FM
                        if (throwable instanceof IllegalAccessException) {
                            //不能跳转过去
                            TtsUtil.speakTextOnRecordWin("RS_VOICE_MUSIC_FM_UN_ONLINE", Constant.RS_VOICE_MUSIC_FM_UN_ONLINE, true, null);
                            return;
                        }
                        TtsUtil.speakTextOnRecordWin("RS_VOICE_SPEAK_NETNOTCON_TIPS", Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS, true, null);
                    }
                });
        compositeDisposable.add(subscribe);
    }

    public void initCmd() {
        regCMD();
        regSoundUiCmd();
    }
}
