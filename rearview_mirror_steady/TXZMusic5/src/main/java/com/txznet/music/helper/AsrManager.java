package com.txznet.music.helper;

import android.content.res.Resources;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.action.SoundCommandActionCreator;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.util.FileConfigUtil;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.Operation;
import com.txznet.sdk.TXZAsrManager;


/**
 * Created by ASUS User on 2017/4/22.
 */

public class AsrManager {

    private static final String TASK_ID_MUSIC_PLAY_STATUS_CONTROL = "TASK_ID_MUSIC_PLAY_STATUS_CONTROL";
    private static final String TASK_ID_MUSIC_PAUSE_STATUS_CONTROL = "TASK_ID_MUSIC_PAUSE_STATUS_CONTROL";
    private static final String TASK_ID_MUSIC_COMM_STATUS_CONTROL = "TASK_ID_MUSIC_COMM_STATUS_CONTROL";

    private static final String TAG = Constant.LOG_TAG_ASR;
    private static final String ASR_CMD_PLAYER_PAUSE = "ASR_CMD_PLAYER_PAUSE";
    private static final String ASR_CMD_PLAYER_PLAY = "ASR_CMD_PLAYER_PLAY";
    private static final String ASR_CMD_PLAYER_NEXT = "ASR_CMD_PLAYER_NEXT";
    private static final String ASR_CMD_PLAYER_PREVIOUS = "ASR_CMD_PLAYER_PREVIOUS";
    private static final String ASR_CMD_PLAYER_LIKE = "ASR_CMD_PLAYER_LIKE";
    private static final String ASR_CMD_PLAYER_LIKE_CANCEL = "ASR_CMD_PLAYER_LIKE_CANCEL";
    private static final String ASR_CMD_OPEN_SUPER_RADIO = "ASR_CMD_OPEN_SUPER_RADIO";
    private static final String ASR_CMD_PLAY_LOCAL_MUSIC = "ASR_CMD_PLAY_LOCAL_MUSIC";
    private static final String ASR_CMD_PLAY_HISTORY_MUSIC = "ASR_CMD_PLAY_HISTORY_MUSIC";
    private static AsrManager sInstance = null;

    private TXZAsrManager.CommandListener commandListener;

    private AsrManager() {
        commandListener = (cmd, data) -> {
            Logger.d(TAG, "-command:" + cmd + " isWakeupResult " + data);
            TtsUtil.speakTextOnRecordWin("RS_VOICE_MEDIA_CONTROL_CONFIRM", "遵命", true, () -> {
                if (ASR_CMD_PLAY_LOCAL_MUSIC.equals(data)) {
                    PlayerActionCreator.get().playLocal(Operation.SOUND);
                } else if (ASR_CMD_PLAY_HISTORY_MUSIC.equals(data)) {
                    PlayerActionCreator.get().playHistoryMusic(Operation.SOUND);
                }
            });
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


    public void regCMD() {
        regCommCMD();
        regPauseCMD();
        regPlayCMD();
    }

    public void unregCMD() {
        //声控界面启动的时候
        //声控界面关闭的时候

        unregCommCMD();
        unregPauseCMD();
        unregPlayCMD();
    }


    /**
     * 注册唤醒声控之后的词汇
     */
    private void regSoundUiCmd() {
        if (FileConfigUtil.getBooleanConfig(FileConfigUtil.KEY_MUSIC_OPEN_LOCAL_MUSIC, true)) {
            regPlayLocalMusic();
        }
        if (FileConfigUtil.getBooleanConfig(FileConfigUtil.KEY_MUSIC_OPEN_HISTORY_MUSIC, true)) {
            regPlayHistoryMusic();
        }

    }

    public void initCmd() {
        if (canRegister()) {
            if (PlayHelper.get().getCurrAudio() != null) {
                regCMD();
            } else {
                unregCMD();
            }
        }
        regSoundUiCmd();
    }


    //////////////////////////////注册唤醒词


    /**
     * 注册加入收藏,取消收藏等指令
     */
    private void regCommCMD() {
        if (!canRegister()) {
            return;
        }
        TXZAsrManager.AsrComplexSelectCallback asrCallBack = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(String type, String command) {
                switch (type) {
                    case ASR_CMD_PLAYER_LIKE:
                        SoundCommandActionCreator.getInstance().favourOrSubscribe(Operation.SOUND, isWakeupResult());
                        break;
                    case ASR_CMD_PLAYER_LIKE_CANCEL:
                        SoundCommandActionCreator.getInstance().unfavourOrUnSubscribe(Operation.SOUND, isWakeupResult());
                        break;
                    default:
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

        asrCallBack.addCommand(ASR_CMD_PLAYER_LIKE, resources.getStringArray(R.array.asr_cmd_player_like));
        asrCallBack.addCommand(ASR_CMD_PLAYER_LIKE_CANCEL, resources.getStringArray(R.array.asr_cmd_player_like_cancel));
        AsrUtil.useWakeupAsAsr(asrCallBack);
        Logger.d(TAG, "reg comm cmd");
    }

    /**
     * 注册"暂停播放"注册下一首,上一首,
     */
    private void regPauseCMD() {
        if (!canRegister()) {
            return;
        }
        TXZAsrManager.AsrComplexSelectCallback asrCallBack = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(String type, String command) {
                Logger.d(TAG, type + "-command:" + command + " isWakeupResult " + isWakeupResult());
                switch (type) {
                    case ASR_CMD_PLAYER_PAUSE:
                        if (!PlayHelper.get().isPlaying()) {
                            //如果不是播放中的状态,则不响应
                            return;
                        }

                        PlayerActionCreator.get().pause(Operation.SOUND);
                        break;
                    case ASR_CMD_PLAYER_NEXT:
//                        PlayerAboutAsr.getInstance().broadcastTTS();
                        PlayerActionCreator.get().next(Operation.SOUND);
                        break;
                    case ASR_CMD_PLAYER_PREVIOUS:
//                        PlayerAboutAsr.getInstance().broadcastTTS();
                        PlayerActionCreator.get().prev(Operation.SOUND);
                        break;
                    default:
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
        asrCallBack.addCommand(ASR_CMD_PLAYER_NEXT, resources.getStringArray(R.array.asr_cmd_player_next));
        asrCallBack.addCommand(ASR_CMD_PLAYER_PREVIOUS, resources.getStringArray(R.array.asr_cmd_player_previous));
        AsrUtil.useWakeupAsAsr(asrCallBack);
        Logger.d(TAG, "reg play cmd");
    }

    /**
     * 注册"继续播放"
     */
    private void regPlayCMD() {
        if (!canRegister()) {
            return;
        }
        TXZAsrManager.AsrComplexSelectCallback asrCallBack = new TXZAsrManager.AsrComplexSelectCallback() {
            @Override
            public void onCommandSelected(String type, String command) {
                Logger.d(TAG, type + "-command:" + command + " isWakeupResult " + isWakeupResult());
                switch (type) {
                    case ASR_CMD_PLAYER_PLAY:
                        if (PlayHelper.get().isPlaying()) {
                            //如果不是播放中的状态,则不响应
                            return;
                        }
                        PlayerActionCreator.get().start(Operation.SOUND);
                        break;
                    default:
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
        Logger.d(TAG, "reg pause cmd");
    }


    private void unregCommCMD() {
        Logger.d(TAG, "unreg comm cmd");
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_MUSIC_COMM_STATUS_CONTROL);
    }

    private void unregPlayCMD() {
        Logger.d(TAG, "unreg play cmd");
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_MUSIC_PLAY_STATUS_CONTROL);
    }

    private void unregPauseCMD() {
        Logger.d(TAG, "unreg pause cmd");
        TXZAsrManager.getInstance().recoverWakeupFromAsr(TASK_ID_MUSIC_PAUSE_STATUS_CONTROL);
    }


    ///////////////////////////////////以下为注册唤醒词

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

    /**
     * 判断是否可以注册
     *
     * @return
     */
    private boolean canRegister() {
        return SharedPreferencesUtils.isWakeupEnable();
    }

}
