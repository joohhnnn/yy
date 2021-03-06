package com.txznet.music.playerModule.logic;

import android.text.TextUtils;
import android.util.Log;
import android.util.LongSparseArray;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.bean.BreakpointAudio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.listener.WinListener;
import com.txznet.music.localModule.logic.StorageUtil;
import com.txznet.music.net.NetManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.logic.focus.MyFocusListener;
import com.txznet.music.playerModule.logic.net.request.ReqError;
import com.txznet.music.playerModule.logic.net.request.ReqThirdSearch;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.ReportHistory;
import com.txznet.music.report.ReportManager;
import com.txznet.music.report.bean.PlayEvent;
import com.txznet.music.search.SearchEngine;
import com.txznet.music.soundControlModule.asr.PlayerAboutAsrManager;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.AudioUtils;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.NetworkUtil;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.TtsHelper;
import com.txznet.music.utils.Utils;
import com.txznet.txz.util.runnables.Runnable2;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.music.playerModule.logic.factory.PlayEngineFactory.getEngine;
import static com.txznet.music.util.TimeUtils.startTime;

/**
 * Created by telenewbie on 2017/6/30.
 */

public class PlayerCallbackManager implements IPlayerStateListener {
    // ????????????
    protected static String TAG = "music:callback:";
    //##?????????????????????##
    private volatile static PlayerCallbackManager singleton;
    protected boolean tempBuffer;
    int retryCount = 0;
    Audio errReplayAudio = null;
    private long lastSaveTime = 0;
    private LongSparseArray<Audio> errorAudios = new LongSparseArray<>();
    private boolean isSavedListenNum = false;
    /**
     * ?????????????????????id
     */
    private long mLastAudioId = 0;
    private boolean needPrepare = false;

    private PlayerCallbackManager() {
    }

    public static PlayerCallbackManager getInstance() {
        if (singleton == null) {
            synchronized (PlayerCallbackManager.class) {
                if (singleton == null) {
                    singleton = new PlayerCallbackManager();
                }
            }
        }
        return singleton;
    }

    @Override
    public void onPlayerPlaying(Audio audio) {
        LogUtil.logd(TAG + "invoke:" + "PlayingListener");
        PlayEngineFactory.getEngine().setState(PlayerInfo.PLAYER_STATUS_PLAYING);
        if (needPrepare) {
            prepare();
        }
        PlayEngineFactory.getEngine().setVolume(EnumState.Operation.auto, 1.0f);
    }

    @Override
    public void onIdle(Audio audio) {
        LogUtil.logd(TAG + "invoke:" + "IdelListener");
        PlayEngineFactory.getEngine().setState(PlayerInfo.PLAYER_STATUS_PAUSE);
        removeTimeoutThisSessionId();
    }

    @Override
    public void onPlayerPaused(Audio audio) {
        LogUtil.logd(TAG + "invoke:" + "PauseComleteListener");
        PlayEngineFactory.getEngine().setState(PlayerInfo.PLAYER_STATUS_PAUSE);
    }

    @Override
    public void onBufferingStart(Audio audio) {
        LogUtil.logd(TAG + "invoke:" + "BufferStartListener");
        PlayEngineFactory.getEngine().setState(PlayerInfo.PLAYER_STATUS_BUFFER);
    }

    @Override
    public void onBufferingEnd(Audio audio) {
        LogUtil.logd(TAG + "invoke:" + "BufferStartEnd");
        if (PlayEngineFactory.getEngine().getState() != PlayerInfo.PLAYER_STATUS_PLAYING) {
            PlayEngineFactory.getEngine().setState(PlayerInfo.PLAYER_STATUS_PLAYING);
            PlayEngineFactory.getEngine().play(EnumState.Operation.auto);
        }
    }

//    @Override
//    public void onPlayListEnd(Audio audio, int scene) {
//        //??????????????????????????????,???????????????
//        //????????????:???????????????,????????????,????????????.
//    }

    @Override
    public void onPlayerFailed(final Audio audio, Error err) {
        final Audio tempAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (tempAudio == null)
            return;
        if (PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_PAUSE) {//???????????????????????? ?????????????????????
            return;
        }
        LogUtil.logd(TAG + "invoke:" + tempAudio.getName() + ":ErrorListener=" + err.toString());

        final int what = err.getErrorCode();

        if (what == Error.ERROR_CLIENT_NET_OFFLINE) {
            if (!NetworkUtil.isNetworkAvailable(GlobalContext.get()) && !Utils.isLocalSong(tempAudio.getSid())) {
                //???????????????
//                ToastUtils.showLong(err.getHint());
            }
            return;
        }

        if (errorAudios.get(tempAudio.getId()) != null) {
            return;
        }
        if (what < 0) {
            replay(tempAudio);
            return;
        }
        //???????????????????????????-3??????????????????
        deleteCachePlayItem(audio);
        errorAudios.put(tempAudio.getId(), tempAudio);
        if (what == Error.ERROR_CLIENT_MEDIA_NOT_FOUND
                || what == Error.ERROR_CLIENT_MEDIA_GATE_WAY
                || what == Error.ERROR_CLIENT_MEDIA_WRONG_URL
                || what == Error.ERROR_CLIENT_MEDIA_FILE_FORBIDDEN
                || what == Error.ERROR_CLIENT_MEDIA_BAD_REQUEST
                || what == Error.ERROR_CLIENT_MEDIA_REQ_SERVER) {


            //??????????????????????????????
            if (what == Error.ERROR_CLIENT_MEDIA_NOT_FOUND) {
                //????????????????????????
                ObserverManage.getObserver().send(InfoMessage.REFRESH_NOT_EXIT_AUDIO);
                PlayInfoManager.getInstance().removeNotExistLocalFile();
                //??????????????????
                if (PlayInfoManager.getInstance().isPlayListEmpty()) {
                    PlayInfoManager.getInstance().setCurrentAlbum(null);
                    PlayInfoManager.getInstance().setCurrentAudio(null);
                    ObserverManage.getObserver().send(InfoMessage.RELEASE);
                    ObserverManage.getObserver().send(InfoMessage.PLAYER_INIT, PlayInfoManager.getInstance().getPlayList());
                }/* else {
                    ObserverManage.getObserver().send(InfoMessage.PLAYER_LIST, PlayInfoManager.getInstance().getPlayList());
                }*/
            }
            AppLogic.runOnBackGround(new Runnable2<Audio, Album>(tempAudio, PlayInfoManager.getInstance().getCurrentAlbum()) {
                @Override
                public void run() {
                    if (mP1 != null && mP2 != null && mP1.getSid() != 0) {//????????????
                        ReqError error = new ReqError();
                        error.albumId = mP2.getId();
                        error.audioId = mP1.getId();
                        error.rawText = SearchEngine.getInstance().getRawText();
                        error.sourceId = mP1.getSid();
                        error.strName = mP1.getName();
                        error.strUrl = TextUtils.equals("1", mP1.getDownloadType()) ? mP1.getStrProcessingUrl() : mP1.getStrDownloadUrl();
                        error.artist = JsonHelper.toJson(mP1.getArrArtistName());
                        error.errCode = what;
//                        error.errString = err.getErrDesc();
                        LogUtil.loge("[Audio]error:" + error.toString());
                        ReportManager.getInstance().reportInRealTime(error);
                    }
                }
            }, 0);
            if (what == Error.ERROR_CLIENT_MEDIA_NOT_FOUND && !Utils.isSong(tempAudio.getSid())) {
                TtsUtil.speakText("???????????????????????????????????????", new TtsUtil.ITtsCallback() {
                    @Override
                    public void onEnd() {
                        super.onEnd();
                        PlayEngineFactory.getEngine().release(EnumState.Operation.error);
                        errorAudios.remove(tempAudio.getId());
                    }
                });
                return;
            }
            errorAudios.remove(tempAudio.getId());
            if (what != Error.ERROR_CLIENT_MEDIA_WRONG_URL) {
                ToastUtils.showShort(err.getHint());

                if (retryCount > 5) {
                    retryCount = 0;
                    return;
                }
                nextOrRelease();
                retryCount++;
            }
            return;
        }

        switch (what) {
            case Error.ERROR_CLIENT_MEDIA_SYS_PLAYER:
            case Error.ERROR_CLIENT_MEDIA_REMOTE://?????????????????????
            case Error.ERROR_CLIENT_MEDIA_BAD_DATA:
                ToastUtils.showShortOnUI(err.getHint());
                nextOrRelease();
                break;
            case Error.ERROR_CLIENT_MEDIA_ERR_IO:

                break;
            case Error.ERROR_CLIENT_MEDIA_NULL_STATE:
            case Error.ERROR_CLIENT_MEDIA_URL_CHANGE:
            case Error.ERROR_CLIENT_MEDIA_GET_AUDIO://????????????
                replay(tempAudio);
                break;
            case Error.ERROR_CLIENT_MEDIA_REQ_TIMEOUT:
                ToastUtils.showShortOnUI(err.getHint());
                PlayEngineFactory.getEngine().release(EnumState.Operation.error);
                break;
            case Error.ERROR_CLIENT_MEDIA_FILE_CHECK_FAIL:
                File file = Utils.getAudioTMDFile(tempAudio);
                if (file != null && file.exists()) {
                    file.delete();
                    nextOrRelease();
                } else {
                    PlayEngineFactory.getEngine().release(EnumState.Operation.error);
                }
                ToastUtils.showShortOnUI(err.getHint());
                break;
            default:
                ToastUtils.showShortOnUI(err.getHint());
                nextOrRelease();
                break;
        }
    }

    private void deleteCachePlayItem(final Audio audio) {
        if (Utils.isSong(audio.getSid()) && Utils.isNeedPreLoad(audio)) {
            Observable.create(new ObservableOnSubscribe<Integer>() {
                @Override
                public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                    DBManager.getInstance().removePlayItem(audio);
                    e.onNext(1);
                    e.onComplete();
                }
            }).subscribeOn(Schedulers.computation()).subscribe(new Observer<Integer>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Integer integer) {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
        }
    }

    private void replay(Audio tempAudio) {
        errReplayAudio = tempAudio;
        PlayEngineFactory.getEngine().release(EnumState.Operation.error);
        PlayEngineFactory.getEngine().play(EnumState.Operation.error);
    }

    private void nextOrRelease() {
        PlayEngineFactory.getEngine().release(EnumState.Operation.auto);
        if (PlayInfoManager.getInstance().getPlayListSize() > 1) {
            PlayEngineFactory.getEngine().next(EnumState.Operation.error);
        } else {
            PlayInfoManager.getInstance().setAudios(null, PlayInfoManager.getInstance().getCurrentScene());
            ObserverManage.getObserver().send(InfoMessage.DELETE_HISTORY);
        }
    }

    @Override
    public void onPlayerEnd(Audio audio) {
        LogUtil.logd(TAG + "invoke:" + "CompleteListener");

        ReportManager.getInstance().reportAudioPlay(audio, ReportHistory.TYPE_END);

        Audio tempAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (null == tempAudio) {
            return;
        }
        PlayInfoManager.getInstance().setCompleteState(audio);
        DBManager.getInstance().saveBreakpoint(tempAudio, 0, true);
        getEngine().next(EnumState.Operation.auto);
    }

    /**
     * ????????????????????????????????????
     *
     * @param audio    ??????????????????
     * @param position ???????????????????????????
     * @return ??????????????????
     */
    private boolean needSaveListenNum(Audio audio, long position) {
        if (!Utils.isSong(audio.getSid()) && position >= 10 * Constant.TIME_UNIT && !isSavedListenNum) {
            isSavedListenNum = true;
            return true;
        }
        return false;
    }

    @Override
    public void onProgress(final Audio audio, long position, final long duration) {
        LogUtil.logd(TAG + "invoke:" + "PlayProgressListener(" + position + "/" + duration + ")/" + PlayEngineFactory.getEngine().getState());

        if (audio.getId() != mLastAudioId) {
            mLastAudioId = audio.getId();
//            AppLogic.runOnBackGround(new Runnable() {
//                @Override
//                public void run() {
//                    audio.setDuration(duration);da
//                    DBManager.getInstance().saveAudio(audio);
//                }
//            });
        }

        PlayInfoManager.getInstance().setProgress(position, duration);

        if (needSaveListenNum(audio, position)) {
            if (PlayInfoManager.getInstance().playListContain(audio)) {
                audio.setClientListenNum(audio.getClientListenNum() + 1);
//                PlayInfoManager.getInstance().notifyPlayListChanged();
                LogUtil.d(TAG + "set client listen num:" + audio.getClientListenNum());
                AppLogic.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        DBManager.getInstance().updateAudioClientListenNum(audio);
                    }
                });
            }
        }

        final Audio tempAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (duration <= 0) {
            LogUtil.loge(TAG + "onplayProgress have error ,duration:" + duration);
            return;
        }
        if (PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_BUFFER) {
            return;
        }
        if (null == tempAudio) {
            return;
        }
        float percent = position * 1.0f / duration;
        if (PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_PLAYING) {
            if (position > duration) {
                onPlayerEnd(PlayInfoManager.getInstance().getCurrentAudio());
//                getEngine().next(EnumState.Operation.auto);
            }
            //??????????????????????????????
            if (PlayInfoManager.getInstance().getCurrentAudio() != null
                    && PlayInfoManager.getInstance().getCurrentAudio()
                    .getDuration() < Constant.TIME_UNIT) {
                PlayInfoManager.getInstance().getCurrentAudio().setDuration(duration);
            }
            PlayInfoManager.getInstance().setCurrentPosition(position);
            long time = position / Constant.TIME_UNIT;
            if (position <= duration && time > 10 && time % 5 == 0 && PlayEngineFactory.getEngine().isPlaying()) {// 5s???????????????,??????????????????????????????
                DBManager.getInstance().saveBreakpoint(tempAudio, (int) position, false);
                if (!Utils.isSong(tempAudio.getSid())) {
                    long curTime = TimeManager.getInstance().getEffectiveTimeMillis();
                    if (null != tempAudio.getAlbumId()) {
                        try {
                            Album album = DBManager.getInstance().findAlbumById(Long.parseLong(tempAudio.getAlbumId()), tempAudio.getSid());
                            if (null != album) {
                                album.setLastListen(curTime);
                                LogUtil.logd("updateTime:" + curTime + ",album:" + album);
                                DBManager.getInstance().saveAlbum(album);
                            }
                        } catch (Exception e) {
                            Logger.e(TAG, "save album last listen error:" + e.toString());
                        }
                    }
                }
            }
            if (Utils.isSong(tempAudio.getSid())) {
                if (position >= 20 * Constant.TIME_UNIT) {
                    PlayEngineFactory.getEngine().getAudioPlayer().forceNeedMoreData(true);
                }
            } else {
                if (position * 1.0f / duration >= 0.2f) {
                    PlayEngineFactory.getEngine().getAudioPlayer().forceNeedMoreData(true);
                }
            }

        }
        // ?????????????????????

    }

    @Override
    public void onBufferProgress(Audio audio, List<LocalBuffer> buffers) {
        LogUtil.logd(TAG + "invoke:" + "BufferProgressListener");
        PlayInfoManager.getInstance().setBufferProgress(buffers);
    }

    private void prepare() {
        needPrepare = false;
        final Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        final Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();

        final BreakpointAudio breakpoint = DBManager.getInstance().findBreakpoint(currentAudio);

        if (breakpoint != null && breakpoint.getBreakpoint() > 0 && !Utils.isSong(breakpoint.getSid())) {
            startTime(Constant.SPEND_TAG + "quick:prepare:seekto");
            LogUtil.logd(TAG + "[" + currentAudio.getName() + "]seekTo::" + breakpoint.getBreakpoint());
            LogUtil.logd(TAG + "[" + currentAudio.getName() + "]seekTo::" + (errReplayAudio == null) + "/" + (!currentAudio.equals(errReplayAudio)));
            if ((errReplayAudio == null || !currentAudio.equals(errReplayAudio)) && !WinListener.isShowSoundUI) {
                TtsHelper.speakResource("RS_VOICE_MUSIC_BREAKPOINT_TIPS", Constant.RS_VOICE_MUSIC_BREAKPOINT_TIPS, new TtsUtil.ITtsCallback() {
                    @Override
                    public void onEnd() {
                        super.onEnd();
                        long bp = breakpoint.getBreakpoint();
                        //??????????????????5s???????????????
                        if (bp - 5 * Constant.TIME_UNIT > 0) {
                            bp -= 5 * Constant.TIME_UNIT;
                        }
                        getEngine().seekTo(EnumState.Operation.auto, bp);
                    }
                });
            } else {
                getEngine().seekTo(EnumState.Operation.auto, breakpoint.getBreakpoint() + 4 * Constant.TIME_UNIT);
            }
        } else {
            LogUtil.logd(TAG + "[" + currentAudio.getName() + "]play music no need seek");
            PlayInfoManager.getInstance().setCurrentPosition(0);
            getEngine().play(EnumState.Operation.auto);
        }

    }

    @Override
    public void onPlayerPreparing(Audio audio) {
        final Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        final Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();

        isSavedListenNum = false;
        ReportManager.getInstance().reportAudioPlay(audio, ReportHistory.TYPE_START);

        ReportEvent.reportPlayEvent(PlayEvent.ACTION_PLAY_START, audio, currentAlbum, 0);

        TimeUtils.endTime(Constant.SPEND_TAG + "quick:prepare");
        LogUtil.logd(TAG + "invoke:" + "PreparedListener");
        if (errorAudios.size() > 0) {
            errorAudios.clear();//????????????
        }
        Constant.setIsExit(false);
        tempBuffer = false;

        retryCount = 0;


        ReqThirdSearch reqThirdSearch = new ReqThirdSearch(currentAudio.getSid(), currentAudio.getId(), 0);
        NetManager.getInstance().fakeRequest(reqThirdSearch);


        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                LogUtil.d(TAG, "focus:" + MyFocusListener.getInstance().focusInThere());
                if (!MyFocusListener.getInstance().focusInThere()) {
                    needPrepare = true;
                    PlayEngineFactory.getEngine().setState(PlayerInfo.PLAYER_STATUS_PAUSE);
                    return;
                }
                if (PausedHelper.get().hasPausedBeforePlay()) {
                    needPrepare = true;
                    PlayEngineFactory.getEngine().setState(PlayerInfo.PLAYER_STATUS_PAUSE);
                    LogUtil.logw(TAG + "has paused before play");
                    return;
                }
                prepare();
                PlayerAboutAsrManager.getInstance().stopBroadcastTTs();
            }
        });

        removeTimeoutThisSessionId();
    }

    int ttsId = 0;

    public Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
//            ttsId = TtsUtil.speakResource("RS_VOICE_SPEAK_NET_POOR", Constant.RS_VOICE_SPEAK_NET_POOR);
        }
    };

    /**
     * ????????????????????????ID?????????????????????
     */
    private void removeTimeoutThisSessionId() {
        AppLogic.removeBackGroundCallback(timeoutRunnable);
        TtsUtil.cancelSpeak(ttsId);
    }

    @Override
    public void onPlayerPrepareStart(Audio audio) {
        AppLogic.runOnBackGround(timeoutRunnable, 5000);
    }


    @Override
    public void onSeekComplete(Audio audio, long seekTime) {
        LogUtil.logd(TAG + "invoke:" + "SeekCompleteListener");
        TimeUtils.endTime(Constant.SPEND_TAG + "quick:seek");
        // ???????????????????????????????????????????????????????????????seekComplete??????????????????
        getEngine().play(EnumState.Operation.auto);
        PlayInfoManager.getInstance().setCurrentPosition(seekTime);
//        ReportHelper.getInstance().sendReportData(ReqDataStats.Action.ACT_SEEK_COMPLETE);
    }


    @Override
    public void onSeekStart(Audio audio) {

    }

}
