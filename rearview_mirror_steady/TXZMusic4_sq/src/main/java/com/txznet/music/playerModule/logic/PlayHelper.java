package com.txznet.music.playerModule.logic;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.net.NetManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.service.ThirdHelper;
import com.txznet.music.utils.NetworkUtil;
import com.txznet.music.utils.PlayerCommunicationManager;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.TtsHelper;
import com.txznet.music.utils.Utils;

import java.util.List;

import io.reactivex.disposables.Disposable;

import static com.txznet.music.utils.PlayerCommunicationManager.STATE_ON_PLAYING;

/**
 * Created by brainBear on 2017/12/1.
 */

public class PlayHelper {

    private static final String TAG = "PlayHelper:";

    private PlayHelper() {

    }

    public static boolean playLastRadio(EnumState.Operation operation) {
        Audio currAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (null != currAudio && !Utils.isSong(currAudio.getSid())) {
            int state = PlayEngineFactory.getEngine().getState();
            if (state == PlayerInfo.PLAYER_STATUS_PAUSE || state == PlayerInfo.PLAYER_STATUS_RELEASE) {
                PlayerCommunicationManager.getInstance().sendPlayItemChanged(currAudio);
                PlayEngineFactory.getEngine().playOrPause(operation);
            } else {
                LogUtil.logd(TAG + "[audio]audioplayer can't support ,to "
                        + PlayEngineFactory.getEngine().getState() + ",currentAudio is "
                        + currAudio.getName());
            }

            return true;
        }
        return false;
    }

    static Album album;
    static Disposable subscribe = null;

    public static boolean playRecommandRadio(final EnumState.Operation operation) {
        if (subscribe == null) {
            ThirdHelper.getInstance().playRecommandRadio(operation);

//            subscribe = ThirdHelper.getInstance().getRecommandRadioAlbum().subscribeOn(Schedulers.io())

        }
        return false;
    }

    public static boolean playHistoryRadio(EnumState.Operation operation) {
        List<HistoryData> albumHistory = DBManager.getInstance().findAlbumHistory();
        if (CollectionUtils.isNotEmpty(albumHistory)) {
            HistoryData historyData = albumHistory.get(0);
            Album album = historyData.getAlbum();
            Audio audio = historyData.getAudio();
            List<Audio> audiosByAlbumId = DBManager.getInstance().findAudiosByAlbumId(album.getId());
            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.auto, audiosByAlbumId, album, audiosByAlbumId.indexOf(audio), PlayInfoManager.DATA_HISTORY);
            PlayEngineFactory.getEngine().playOrPause(operation);
            return true;
        }
        return false;
    }

    public static void playRadio(final EnumState.Operation operation) {
        Audio audio = PlayInfoManager.getInstance().getCurrentAudio();
        // ??????????????????????????????????????????open????????????launcher??????UI???????????????????????????????????????????????????????????????????????????
        if (audio != null && !Utils.isSong(audio.getSid()) && PlayEngineFactory.getEngine().getState()==PlayerInfo.PLAYER_STATUS_PLAYING) {// ????????????????????????????????????
            // ????????????
            LogUtil.logd("player:pass play radio because is playing radio");
            return;
        }
        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_OPEN);
        LogUtil.logd("player:play radio");
        if (!NetManager.isNetworkConnected()) {
            if (operation == EnumState.Operation.sound) {
                TtsUtil.speakResource("RS_VOICE_MUSIC_NO_NET_TIPS", Constant.RS_VOICE_MUSIC_NO_NET_TIPS);
                ToastUtils.showShort(Constant.RS_VOICE_MUSIC_NO_NET_TIPS);
            } else {
                TtsHelper.speakResource("RS_VOICE_MUSIC_NO_NET_TIPS", Constant.RS_VOICE_MUSIC_NO_NET_TIPS);
            }
            LogUtil.loge("player:play radio failed, cause=network, player exit");
            PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_EXIT);
        } else {
            if (!playLastRadio(operation)) {
//                if (!playHistoryRadio(operation)) {
                    playRecommandRadio(operation);
//                }
            }
        }
    }


    /**
     * ??????????????????????????????
     *
     * @return
     */
    public static boolean playLastMusic(EnumState.Operation operation) {
        if (null != PlayInfoManager.getInstance().getCurrentAudio() && Utils.isSong(PlayInfoManager.getInstance().getCurrentAudio().getSid())) {
            int state = PlayEngineFactory.getEngine().getState();
            if (state == PlayerInfo.PLAYER_STATUS_PAUSE || state == PlayerInfo.PLAYER_STATUS_RELEASE) {
                PlayEngineFactory.getEngine().playOrPause(operation);
            } else {
                LogUtil.logd(TAG + "[music]audioplayer can't support ,to "
                        + PlayEngineFactory.getEngine().getState() + ",currentAudio is "
                        + PlayInfoManager.getInstance().getCurrentAudio().getName());
            }
            return true;
        }
        PlayEngineFactory.getEngine().release(EnumState.Operation.sound);
        return false;
    }

    public static void playMusic(EnumState.Operation operation) {
        LogUtil.logd("player:play music");
        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_OPEN);
        LogUtil.logd("isNetworkAvailable=" + NetworkUtil.isNetworkAvailable(GlobalContext.get()));
        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            // ???????????????toast????????????????????????????????????????????????????????????????????????????????????
            LogUtil.logd("player:play local music");
            if (!playLocalMusic(operation)) {
                LogUtil.loge("player:play music, play local failed, player exit");
                // ??????????????????????????????
                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NET_POOR);
                TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NET_POOR);
                PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_EXIT);
//                if (operation == EnumState.Operation.sound) {
//                    TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_SPEAK_NONE_NET,true,null);
//                }else {
//                    TtsUtilWrapper.speakText(Constant.RS_VOICE_SPEAK_NONE_NET);
//                }
            }
        } else {
            //??????,??????,??????,??????
//            if (!playHistory(operation)) {
                playRecommandMusic(operation);
//            }
        }
    }

    /**
     * ??????????????????
     */
    public static void openMusic(EnumState.Operation operation) {
        playMusic(operation);

//        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_OPEN);
//        if (!playLastMusic(operation)) {
//            if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
//                if (!playLocalMusic(operation)) {
//                    // ??????????????????????????????
//                    ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NET_POOR);
//                    TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NET_POOR);
//                    PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_EXIT);
//
////                    if (operation == EnumState.Operation.sound) {
////                        TtsUtilWrapper.speakTextOnRecordWin(Constant.RS_VOICE_MUSIC_NO_NET_TIPS,true,null);
////                    }else {
////                        TtsUtilWrapper.speakText(Constant.RS_VOICE_MUSIC_NO_NET_TIPS);
////                    }
//                }
//            } else {
//                //??????
//                playRecommandMusic(operation);
//            }
//        }

    }


    /**
     * ????????????
     */
    public static boolean playHistory(EnumState.Operation operation) {
        List<HistoryData> musicHistory = DBManager.getInstance().findMusicHistory();
        if (CollectionUtils.isNotEmpty(musicHistory)) {
            SharedPreferencesUtils.setAudioSource(Constant.HISTORY_TYPE);

            List<Audio> audios = DBManager.getInstance().convertHistoryDataToAudio(musicHistory);

            PlayEngineFactory.getEngine().setAudios(operation, audios, null, 0, PlayInfoManager.DATA_HISTORY);
            PlayEngineFactory.getEngine().playOrPause(operation);
            return true;
        } else {
            return false;
        }

    }

    /**
     * ????????????
     */
    public static boolean playRecommandMusic(final EnumState.Operation operation) {
        //???????????????????????????????????????????????????????????????

        ThirdHelper.getInstance().playRecommandSong();

        return true;
    }

    /**
     * ????????????
     */
    public static boolean playLocalMusic(EnumState.Operation operation) {
        List<Audio> localMusic = DBManager.getInstance().findAllLocalAudios();
        if (CollectionUtils.isNotEmpty(localMusic)) {
            LogUtil.logd("player:play music, play local");
//            SharedPreferencesUtils.setAudioSource(Constant.LOCAL_MUSIC_TYPE);
            PlayEngineFactory.getEngine().setAudios(operation, localMusic, null, 0, PlayInfoManager.DATA_LOCAL);
            PlayEngineFactory.getEngine().playOrPause(operation);
            return true;
        } else {
            return false;
        }
    }
}
