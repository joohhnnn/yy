package com.txznet.music.soundControlModule.asr;

import android.content.res.Resources;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.config.ConfigManager;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.listener.WinListener;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.PlayerInfoUpdateListener;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.TtsUtilWrapper;
import com.txznet.music.utils.Utils;
import com.txznet.sdk.TXZAsrManager;

import java.util.List;


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
    private static AsrManager sInstance = null;

    private AsrManager() {
        PlayInfoManager.getInstance().addPlayerInfoUpdateListener(this);
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

                            if (wakeupCount > 2) {
                                TtsUtilWrapper.speakResource("RS_VOICE_SPEAK_PLAY_NEXT", Constant.RS_VOICE_SPEAK_PLAY_NEXT, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().next(EnumState.Operation.sound);
                                    }
                                });
                            } else {
                                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_WAKEUP_PLAY_NEXT", Constant.RS_VOICE_MUSIC_WAKEUP_PLAY_NEXT, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().next(EnumState.Operation.sound);
                                    }
                                });
                            }
                        }
                        break;
                    case ASR_CMD_PLAYER_PREVIOUS:
                        if (PlayEngineFactory.getEngine().isPlaying()) {

                            if (wakeupCount > 2) {
                                TtsUtilWrapper.speakResource("RS_VOICE_SPEAK_PLAY_PREV", Constant.RS_VOICE_SPEAK_PLAY_PREV, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().last(EnumState.Operation.sound);
                                    }
                                });
                            } else {
                                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_WAKEUP_PLAY_PREV", Constant.RS_VOICE_MUSIC_WAKEUP_PLAY_PREV, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().last(EnumState.Operation.sound);
                                    }
                                });
                            }
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
                                TtsUtilWrapper.speakResource("RS_VOICE_SPEAK_PLAY_PAUSE", Constant.RS_VOICE_SPEAK_PLAY_PAUSE, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().pause(EnumState.Operation.sound);
                                    }
                                });
                            } else {
                                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_WAKEUP_PAUSE", Constant.RS_VOICE_MUSIC_WAKEUP_PAUSE, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().pause(EnumState.Operation.sound);
                                    }
                                });
                            }
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
                                TtsUtilWrapper.speakResource("RS_VOICE_SPEAK_PLAY_PLAY", Constant.RS_VOICE_SPEAK_PLAY_PLAY, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
                                    }
                                });
                            } else {
                                TtsUtilWrapper.speakResource("RS_VOICE_MUSIC_WAKEUP_PLAY", Constant.RS_VOICE_MUSIC_WAKEUP_PLAY, new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
                                    }
                                });
                            }
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
}
