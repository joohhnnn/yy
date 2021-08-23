package com.txznet.music.playerModule.logic;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.net.response.ResponseAlbumAudio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.EnumState.AudioType;
import com.txznet.music.baseModule.bean.EnumState.Operation;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.data.http.resp.RespCarFmCurTops;
import com.txznet.music.listener.WinListener;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestAudioCallBack;
import com.txznet.music.playerModule.bean.IOperation;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.logic.factory.TxzAudioPlayerFactory;
import com.txznet.music.playerModule.logic.focus.HeadSetHelper;
import com.txznet.music.playerModule.logic.focus.MyFocusListener;
import com.txznet.music.power.PowerChangedListener;
import com.txznet.music.power.PowerManager;
import com.txznet.music.search.SearchEngine;
import com.txznet.music.soundControlModule.asr.PlayerAboutAsrManager;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.music.utils.JumpUtils;
import com.txznet.music.utils.NetworkUtil;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.SyncOtherAppBroadcastListener;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.TtsHelper;
import com.txznet.music.utils.Utils;
import com.txznet.txz.util.TXZFileConfigUtil;

import junit.framework.Assert;

import java.util.List;

import static com.txznet.txz.util.TXZFileConfigUtil.KEY_MUSIC_RESUME_PLAY_AFTER_WAKEUP;


public class PlayerEngineCoreDecorator implements IPlayerEngine, PowerChangedListener {
    private static final String TAG = "music:engine:";

    private TXZAudioPlayer mPlayer;
    // 用于记录所需要的信息
//	private TESTRecode TESTRecode = new TESTRecode();

    //	private static IPluginPlayerClient mPlayer;
    // 界面操作的类
    private IOperation mOperation;

    private AudioManager audioManager;


//    private MyFocusListener focusListener;

    private volatile boolean isRelease = true;

    @PlayerInfo.PlayerStatus
    private int mState;


    private int mPlayerType = PlayEngineFactory.TYPE_ALL;
    private int mTtsId = -1;
    private long mPlayedId;
    private PlayListChangedListener listChangedListener = new PlayListChangedListener();
    private SavePlaylistHelper.PlayListSaveListener listSaveListener = new SavePlaylistHelper.PlayListSaveListener();
    private SavePlaylistHelper.SaveInfoListener saveInfoListener = new SavePlaylistHelper.SaveInfoListener();
    private int mRetryCount = 0;
    private SyncOtherAppBroadcastListener syncOtherAppBroadcastListener = new SyncOtherAppBroadcastListener();

    /**
     * 倒车导致的暂停
     */
    private boolean isReversingPause = false;
    private PrepareRunnable prepareRunnable = new PrepareRunnable();

    public PlayerEngineCoreDecorator() {
        mPlayerType = PlayEngineFactory.TYPE_ALL;
        init();
        PowerManager.getInstance().addPowerChangedListener(this);
    }

    public PlayerEngineCoreDecorator(int type) {
        mPlayerType = type;
        init();
    }


    @Override
    @Deprecated
    public void playAudioList(Operation operation, List<Audio> audios, int index, Album album) {
        //原因:所有设置播放列表的位置都需要,有带相应的场景,
        throw new RuntimeException("这个方法不能调用");
//        PlayEngineFactory.getEngine().setAudios(operState, audios, album, index);
//        PlayEngineFactory.getEngine().play(operState);
    }

    ISwitchAudioListener switchPlaylistlistener = new ISwitchAudioListener() {
        @Override
        public void onAudioReady(Audio audio) {
            ObserverManage.getObserver().send(InfoMessage.REQUEST_AUDIO_RESPONSE);//去掉上下拉的界面的相关回调
        }

        @Override
        public void onAudioUnavailable(int errorCode, Object obj) {
            ObserverManage.getObserver().send(InfoMessage.REQUEST_AUDIO_RESPONSE);
        }
    };


    @Override
    public void init() {
        audioManager = (AudioManager) AppLogic.getApp().getSystemService(
                Context.AUDIO_SERVICE);
//        focusListener = new MyFocusListener();
        // TODO:通过加载插件的形式进行加载
        mOperation = new IOperation() {

            @Override
            public void play(AudioType type) {
            }

            @Override
            public void searchListData(EnumState.Operation operation, boolean isDown) {


                if (isDown) {
                    PlayListPresent.getInstance().switchMoreAudios(operation, PlayListPresent.getInstance().getNextReqAudio(), switchPlaylistlistener);
                } else {
                    PlayListPresent.getInstance().getLasterAudios(operation, PlayListPresent.getInstance().getLastReqAudio(), switchPlaylistlistener);
                }
//
//
//                Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
//                if (currentAlbum != null) {
//                    LogUtil.logd(TAG + "search:more:audios:" + currentAlbum.getName());
//                    Audio reqAudio = null;
//                    if (isDown) {
//                        if (!PlayInfoManager.getInstance().isPlayListEmpty()) {
//                            reqAudio = PlayInfoManager.getInstance().getPlayListAudio(PlayInfoManager.getInstance().getPlayListSize() - 1);
//                        }
//                    } else {
//                        if (!PlayInfoManager.getInstance().isPlayListEmpty()) {
//                            reqAudio = PlayInfoManager.getInstance().getPlayListAudio(0);
//                        }
//                    }
//
//                    AlbumEngine.getInstance().requestMoreAudio(operation, currentAlbum, reqAudio, currentAlbum.getCategoryId(), isDown, null);
////                    NetManager.getInstance().requestAudio(operState, currentAlbum, reqAudio, isUp, currentAlbum.getCategoryID(), null);
////                    NetManager.getInstance().requestAudioByAudioId(currentAlbum, PlayInfoManager.getInstance().getCurrentAudio(), currentAlbum.getCategoryID());
//                } else {
//                    LogUtil.logd(TAG + "search:more:audios:null");
//                    ObserverManage.getObserver().send(InfoMessage.REQUEST_AUDIO_RESPONSE);
//                    if (Operation.manual == operation) {
//                        ToastUtils.showShortOnUI(Constant.RS_VOICE_MUSIC_NO_MORE_DATA);
//                    }
//                }
            }
        };
//		mPlayer = new DefaultPlayerEngine();

//        mPlayer = createInnerPlayer();

        if (PlayInfoManager.getInstance().getCurrentAudio() == null) {
            if (!PlayInfoManager.getInstance().isPlayListEmpty()) {
                PlayInfoManager.getInstance().setCurrentAudio(PlayInfoManager.getInstance().getPlayListAudio(0));
            } else {
//                readFromFile();
            }
        } else {
            LogUtil.logd("notify show audio info");
//            ObserverManage.getObserver().send(InfoMessage.PLAYER_CURRENT_AUDIO);
        }
    }

    @Override
    public void onSleep() {
        boolean isPlay = SharedPreferencesUtils.getIsPlay();
        LogUtil.logd(TAG + "POWER:: client sleep:  isPlay:" + isPlay);
        PlayEngineFactory.getEngine().release(EnumState.Operation.wakeUP);
    }

    @Override
    public void onWakeUp() {
        boolean wakeupPlay = SharedPreferencesUtils.getWakeupPlay();
        boolean isPlay = SharedPreferencesUtils.getIsPlay();
        LogUtil.logd(TAG + "POWER:: client wakeup:" + wakeupPlay + " isPlay:" + isPlay);
        if (wakeupPlay) {
            PlayEngineFactory.getEngine().init();
        }
        if (wakeupPlay && isPlay) {
            PlayEngineFactory.getEngine().play(EnumState.Operation.wakeUP);
        }
    }

    @Override
    public void onExit() {
        PlayEngineFactory.getEngine().release(EnumState.Operation.wakeUP);
    }

    @Override
    public void onReverseStart() {
        boolean play = FileConfigUtil.getBooleanConfig(TXZFileConfigUtil.KEY_MUSIC_REVERSING_PLAY, true);
        if (!play && isPlaying()) {
            PlayEngineFactory.getEngine().pause(EnumState.Operation.auto);
            isReversingPause = true;
        }
    }

    @Override
    public void onReverseEnd() {
        if (isReversingPause) {
            PlayEngineFactory.getEngine().play(EnumState.Operation.auto);
        }
    }

    private TXZAudioPlayer createInnerPlayer() {//多线程创建多个
        TXZAudioPlayer player = null;
        if (mPlayer == null || isRelease) {
            player = TxzAudioPlayerFactory.createPlayer(PlayInfoManager.getInstance().getCurrentAudio());
        } else {
            return mPlayer;
        }
        if (player != null) {
            isRelease = false;
            HeadSetHelper.getInstance().open(GlobalContext.get());

            LogUtil.logd(Constant.OOM_TAG + "createNewPlayer:success:(" + (mPlayer == null) + "/" + (isRelease) + ")");
            PlayerControlManager.getInstance().addStatusListener(player.hashCode(), new SyncCoreData());
            PlayerControlManager.getInstance().addStatusListener(player.hashCode(), syncOtherAppBroadcastListener);
            PlayInfoManager.getInstance().addPlayListChangedListener(listChangedListener);
            PlayInfoManager.getInstance().addPlayListChangedListener(listSaveListener);
            PlayInfoManager.getInstance().addPlayerInfoUpdateListener(saveInfoListener);
            PlayInfoManager.getInstance().addPlayerInfoUpdateListener(syncOtherAppBroadcastListener);
            PlayerControlManager.getInstance().addStatusListener(player.hashCode(), PlayerCallbackManager.getInstance());
        } else {
            LogUtil.logd(Constant.OOM_TAG + "createNewPlayer:error:(" + (mPlayer == null) + "/" + (isRelease) + ")");
        }
        return player;
    }

    /**
     * 设置播放列表 ,并会吧当前的状态设置为release
     *
     * @param audios
     * @param album  当前专辑
     * @param index  当前播放歌曲的序列号
     * @param ori    内容来源
     */
    @Override
    public void setAudios(EnumState.Operation operation, final List<Audio> audios, Album album, int index, int ori) {
        Assert.assertNotNull(audios);
        if (operation != EnumState.Operation.sound) {
            SearchEngine.getInstance().setRawText("");//除声控切换导致的播单更新则需要将rawText（用于上报）进行清除
        }
        SharedPreferencesUtils.setPlayListScence(ori);
        if (mTtsId != -1) {
            TtsUtil.cancelSpeak(mTtsId);//取消上一次的请求
        }
        TtsHelper.cancle();
        TimeUtils.startTime(Constant.SPEND_TAG + "quick:setAudios");
        LogUtil.logd(TAG + "来源source:" + ori + "  album ::" + (album == null ? "null" : album.toString()));
        PlayInfoManager.getInstance().setPlayListTotalNum(audios.size());
        PlayEngineFactory.getEngine().release(operation);
        PlayInfoManager.getInstance().setCurrentAlbum(album);

        Audio currentAudio = null;
        if (index < 0 || index >= audios.size()) {
            currentAudio = audios.get(0);
        } else {
            currentAudio = audios.get(index);
        }
        Log.e("music:sendBroadcast:", "onResponse:setAudios " + operation + "||||" + currentAudio.getName());
        PlayInfoManager.getInstance().setCurrentAudio(currentAudio);
        PlayInfoManager.getInstance().setAudios(audios, ori);
        if (CollectionUtils.isEmpty(audios)) {
            ToastUtils.showShortOnUI("获取的播放列表没有数据");
            return;
        }

        if (!Utils.isSong(PlayInfoManager.getInstance().getCurrentAudio().getSid())) {
            PlayInfoManager.getInstance().setCurrentPlayMode(PlayerInfo.PLAYER_MODE_SEQUENCE);
        } else {
            PlayInfoManager.getInstance().setCurrentPlayMode(SharedPreferencesUtils.getPlayMode());
        }
        PlayInfoManager.getInstance().setLastEnd(false);
        PlayInfoManager.getInstance().setFirstEnd(false);
        PlayInfoManager.getInstance().setRequestMoreAudio(audios.get(audios.size() - 1));

        TimeUtils.endTime(Constant.SPEND_TAG + "quick:setAudios");

        //添加额外的能力（跳转到播放详情界面）
        if (JumpUtils.getInstance().isNeedJumpToPlayer(album)) {
            ActivityStack.getInstance().popActivity();
            JumpUtils.getInstance().jumpToPlayUi(ActivityStack.getInstance().currentActivity(), album);
        }
    }

    /**
     * 添加内容到尾部
     *
     * @param audios
     * @param isAddLast 添加到集合的末尾
     */
    @Override
    public void addAudios(EnumState.Operation operation, final List<Audio> audios,
                          boolean isAddLast) {
        PlayInfoManager.getInstance().addAudios(isAddLast, audios);

    }

    @Override
    public void play(final EnumState.Operation operation) {
        if (EnumState.Operation.temp != operation) {
            MyFocusListener.getInstance().setManualPlay(true);
        }
        MyFocusListener.getInstance().requestAudioFocusImmediately(AudioManager.AUDIOFOCUS_GAIN);
        if (operation == EnumState.Operation.sound) {
            // TODO: 2017/12/4 这段逻辑应该移出去
            if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                //播放上次收听
                if (PlayInfoManager.getInstance().getCurrentAudio() != null) {
//                    getState().play(operation);
                    playByState(operation);
                } else {
                    //客户端写死逻辑，用于继续播放的逻辑
//                    ReqAlbumAudio reqAlbumAudio = new ReqAlbumAudio();
//                    reqAlbumAudio.setSid(9);
//                    reqAlbumAudio.setId(50100);
//                    NetManager.getInstance().sendRequestToCore(Constant.GET_ALBUM_AUDIO, reqAlbumAudio, new RequestCallBack<ResponseAlbumAudio>(ResponseAlbumAudio.class) {
//                        @Override
//                        public void onResponse(ResponseAlbumAudio responseAlbumAudio) {
//                            PlayEngineFactory.getEngine().setAudios(operation, responseAlbumAudio.getArrAudio(), null, 0, PlayInfoManager.DATA_NET);
//                            PlayEngineFactory.getEngine().play(operation);
//                        }
//
//                        @Override
//                        public void onError(String cmd, Error error) {
//                            TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS);
//                        }
//                    });
                }
                return;
            } else if (PlayInfoManager.getInstance().getCurrentAudio() == null) {
                //播放本地
                if (!playLocalMusic(operation)) {
                    TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NONE_NET);
                }
                return;
            }
        } else if (operation == EnumState.Operation.manual || operation == Operation.wakeUP) { // 手动点击
            if (PlayInfoManager.getInstance().getCurrentAudio() == null) { // 播放栏为空
                if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                    PlayHelper.playLastPlaylistObservable(operation);
//                    HistoryData newestHistory = DBManager.getInstance().findNewestHistory();
//                    boolean playHistory = false;
//                    if (newestHistory != null) {
//                        Logger.i(TAG, "newest history:" + newestHistory.toString());
//                        if (newestHistory.getType() == HistoryData.TYPE_AUDIO) {
//                            playHistory = PlayHelper.playHistoryMusic(Operation.auto, true);
//                        } else {
//                            playHistory = PlayHelper.playHistoryRadio(Operation.auto, true);
//                        }
//                    }
//                    if (!playHistory) {
//                        PlayHelper.playRecommend(PlayInfoManager.DATA_ALBUM, operation, true);
//                    }
                } else {
                    if (!playLocalMusic(operation)) {
                        TtsUtil.speakResource("RS_VOICE_MUSIC_PLAY_ERROR_TIPS", Constant.RS_VOICE_MUSIC_PLAY_ERROR_TIPS);
                    }
                }
                return;
            }
        } else if (operation == EnumState.Operation.extra) {
            // BUG-FIX，1008225	,修复关闭音乐后，通过SDK调用continuePlay接口无法继续播放的问题。
            if (PlayInfoManager.getInstance().getCurrentAudio() != null) {
                playByState(operation);
            } else {
                PlayHelper.playLastPlaylistObservable(operation);
            }
            return;
        }
//        getState().play(operation);
        playByState(operation);
    }

    private void playByState(EnumState.Operation operation) {
        int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
        Logger.d(TAG, "Current Play Status:" + currentPlayerStatus);
        switch (currentPlayerStatus) {
            case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                PausedHelper.get().notifyPlaying();
                getAudioPlayer().start();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PAUSE:
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.inner.isPlaying", ("" + true).getBytes(), null);
                PausedHelper.get().notifyPlaying();
                getAudioPlayer().start();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PLAYING:
                PausedHelper.get().notifyPlaying();
                getAudioPlayer().start();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_RELEASE:
                PausedHelper.get().notifyWait2Play();
                PlayEngineFactory.getEngine().prepareAsync(operation);
                break;
        }
        if (FileConfigUtil.getBooleanConfig(KEY_MUSIC_RESUME_PLAY_AFTER_WAKEUP, false)) {
            LogUtil.logd(TAG + "POWER:: isPlay:true");
            SharedPreferencesUtils.setIsPlay(true);
        }
    }

    private void pauseByState(EnumState.Operation operation) {
        int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
        switch (currentPlayerStatus) {
            case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                if (getAudioPlayer() != null) {
                    getAudioPlayer().pause();
                }
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PAUSE:
                getAudioPlayer().pause();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PLAYING:
                if (getAudioPlayer() != null) {
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.inner.isPlaying", ("" + false).getBytes(), null);
                    getAudioPlayer().pause();
                }
                break;
            case PlayerInfo.PLAYER_UI_STATUS_RELEASE:

                break;
        }
        if (FileConfigUtil.getBooleanConfig(KEY_MUSIC_RESUME_PLAY_AFTER_WAKEUP, false)) {
            LogUtil.logd(TAG + "POWER:: isPlay:false");
            SharedPreferencesUtils.setIsPlay(false);
        }
    }

    private void playOrPauseByState(EnumState.Operation operation) {
        int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
        switch (currentPlayerStatus) {
            case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                play(operation);
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PAUSE:
                play(operation);
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PLAYING:
                pause(operation);
                break;
            case PlayerInfo.PLAYER_UI_STATUS_RELEASE:
                play(operation);
                break;
        }
    }

    private void prepareAsyncReleaseStatus(EnumState.Operation operation) {
        if (null == getAudioPlayer()) {
            LogUtil.logd(Constant.OOM_TAG + "createNewPlayer:null:(" + operation.name() + ")");
            prepareRunnable.setOperation(operation);
            AppLogic.runOnUiGround(prepareRunnable, 100);
            return;
        }
        LogUtil.logd(Constant.OOM_TAG + "createNewPlayer:finished:(" + operation.name() + ")");

        PlayEngineFactory.getEngine().setState(PlayerInfo.PLAYER_STATUS_BUFFER);

        getAudioPlayer().prepareAsync();
        final Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        final Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();

        if (currentAudio == null) {
            return;
        }

        //存数据库
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                //存历史列表,
//                DBManager.getInstance().saveToHistory(currentAudio, currentAlbum);
                //存断点列表
                if (!Utils.isSong(currentAudio.getSid())
                        && null == DBManager.getInstance().findBreakpoint(currentAudio)) {
                    DBManager.getInstance().saveBreakpoint(currentAudio, 0, false);
                }

                if (Utils.isSong(currentAudio.getSid())) {
                    DBManager.getInstance().saveMusicHistory(currentAlbum, currentAudio);
                } else {
                    DBManager.getInstance().saveAlbumHistory(currentAlbum, currentAudio);
                }

                //保存数据库
                LogUtil.d(TAG, "save history:" + currentAudio.toString() + " "
                        + (currentAlbum == null ? "null" : currentAlbum.toString()));

                ObserverManage.getObserver().send(InfoMessage.REQUERY_HISTORY_MUSIC_LIST);

            }
        });
    }

    private void prepareAsyncByState(EnumState.Operation operation) {
        int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
        switch (currentPlayerStatus) {
            case PlayerInfo.PLAYER_UI_STATUS_BUFFER:

                break;
            case PlayerInfo.PLAYER_UI_STATUS_PAUSE:

                break;
            case PlayerInfo.PLAYER_UI_STATUS_PLAYING:

                break;
            case PlayerInfo.PLAYER_UI_STATUS_RELEASE:
                //原本的ReleaseState类对这块有加synchronized
                prepareAsyncReleaseStatus(operation);
                break;
        }
    }

    private void releaseByState(EnumState.Operation operation) {
        int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.inner.isPlaying", ("" + false).getBytes(), null);
        switch (currentPlayerStatus) {
            case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                getAudioPlayer().release();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PAUSE:
                getAudioPlayer().release();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PLAYING:
                getAudioPlayer().release();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_RELEASE:
                AppLogic.removeUiGroundCallback(prepareRunnable);
                getAudioPlayer().release();
                break;
        }
    }

    private void seekByState(Operation operation, long position) {
        int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
        switch (currentPlayerStatus) {
            case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                getAudioPlayer().seekTo(position);
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PAUSE:
                getAudioPlayer().seekTo(position);
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PLAYING:
                getAudioPlayer().seekTo(position);
                break;
            case PlayerInfo.PLAYER_UI_STATUS_RELEASE:

                break;
        }
    }

    public boolean playLocalMusic(EnumState.Operation operation) {
        return PlayHelper.playLocalMusicWithBreakpoint(operation);
    }

    @Override
    public void pause(EnumState.Operation operation) {
//        if (operation != OperState.auto && operation != OperState.temp) {//排除音频焦点失去的时候会调用
//            MyFocusListener.getInstance().isCallPlay=false;//主动暂停
//            MyFocusListener.getInstance().isPreFocusChangeState = false;
//        }
        if (EnumState.Operation.temp != operation) {
            MyFocusListener.getInstance().setManualPlay(false);
        }
        pauseByState(operation);
        SyncCoreData.syncCurPlayerStatus(false);
        PausedHelper.get().notifyPaused();
    }

    private void switchMoreAudio(final EnumState.Operation operation, final boolean isNext,
                                 @NonNull final ISwitchAudioListener listener) {
        final Audio audio;

        if (isNext) {
            audio = PlayListPresent.getInstance().getNextReqAudio();
        } else {
            audio = PlayListPresent.getInstance().getLastReqAudio();
        }


        if (!isNext && PlayInfoManager.getInstance().isFirstEnd()) {
            listener.onAudioUnavailable(ISwitchAudioListener.ERROR_IS_FIRST, null);
        } else if (isNext && PlayInfoManager.getInstance().isLastEnd()) {
            listener.onAudioUnavailable(ISwitchAudioListener.ERROR_IS_END, null);
        } else {
            if (isNext) {
                PlayListPresent.getInstance().switchMoreAudios(operation, audio, listener);
            } else {
                PlayListPresent.getInstance().getLasterAudios(operation, audio, listener);
            }

        }
    }

    /**
     * 通过回调的方式来切歌
     *
     * @param operation 操作来源
     * @param isNext    上一首还是下一首
     * @param listener  回调
     */
    private void switchAudio(final EnumState.Operation operation, final boolean isNext,
                             @NonNull final ISwitchAudioListener listener) {
        final PlayInfoManager playInfoManager = PlayInfoManager.getInstance();
        if (playInfoManager.isPlayListEmpty()) {
            return;
        }

        Audio currentAudio = playInfoManager.getCurrentAudio();
        if (null == currentAudio) {
            listener.onAudioReady(playInfoManager.getPlayListAudio(0));
            return;
        }

        int pos = 0;
        int index = playInfoManager.playListIndexOf(currentAudio);
        int offset = isNext ? 1 : -1;
        final boolean force = operation != EnumState.Operation.auto;

        switch (playInfoManager.getCurrentPlayMode()) {
            case PlayerInfo.PLAYER_MODE_SEQUENCE:
                pos = index + offset;
                break;
            case PlayerInfo.PLAYER_MODE_SINGLE_CIRCLE:
                if (force) {
                    pos = index + offset;
                } else {
                    pos = index;
                }

                break;
            case PlayerInfo.PLAYER_MODE_RANDOM:
                int randomInt = (int) (Math.random() * playInfoManager.getPlayListSize());
                if (index == randomInt) {
                    pos = index + 1;
                } else {
                    pos = randomInt;
                }
                break;
        }
        if (PlayInfoManager.getInstance().getNextCarFmTime() != null) {
            if (PlayInfoManager.getInstance().getCurrentScene() == PlayInfoManager.DATA_CHEZHU_FM) {
                //只有车主FM才有这样的逻辑
                listener.onAudioUnavailable(ISwitchAudioListener.ERROR_SWITCH_OTHER_CAR, PlayInfoManager.getInstance().getNextCarFmTime());
                return;
            } else {
                PlayInfoManager.getInstance().setNextCarFmTime(null);
            }
        }
        switchAudio(operation, isNext, listener, playInfoManager, pos, force);
    }

    private void switchAudio(Operation operation, boolean isNext, @NonNull ISwitchAudioListener listener, PlayInfoManager playInfoManager, int pos, boolean force) {
        if (pos >= playInfoManager.getPlayListSize()) {
            switchMoreAudio(operation, true, listener);
        } else if (pos < 0) {
            switchMoreAudio(operation, false, listener);
        } else {
            Audio playListAudio = playInfoManager.getPlayListAudio(pos);
            if (!force && isNext && PlayInfoManager.getInstance().getCurrentScene() == PlayInfoManager.DATA_CHEZHU_FM) {//只有下一首的逻辑才会走，这里的逻辑
                //因为上一首，触发的逻辑，不是声控就是手动，都属于用户主动操作行为。
                playListAudio = PlayInfoManager.getInstance().getOnceLastPlayAudio(pos);
            }

            if (null == playListAudio) {
                listener.onAudioUnavailable(ISwitchAudioListener.ERROR_IS_END, null);
            } else {
                listener.onAudioReady(playListAudio);
            }
        }
    }

    @Override
    public void next(final EnumState.Operation operation) {
        if (PlayInfoManager.getInstance().isPlayListEmpty()) {
            LogUtil.e(TAG + " next: play list is empty");
            return;
        }


        switchAudio(operation, true, new ISwitchAudioListener() {
            @Override
            public void onAudioReady(final Audio audio) {
                playAudio(operation, audio);
                if (PlayInfoManager.getInstance().getCurrentScene() == PlayInfoManager.DATA_CHEZHU_FM) {
                    final Album album = PlayInfoManager.getInstance().getCurrentAlbum();
                    if (PlayInfoManager.getInstance().playListIndexOf(audio) == PlayInfoManager.getInstance().getPlayListSize() - 1) {
                        requestLoadMoreAudio(album, audio);
                    }
                }
            }

            @Override
            public void onAudioUnavailable(int errorCode, Object object) {
                Logger.d(TAG, "switch:audio:error:" + errorCode);
                Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();

                if (errorCode == ISwitchAudioListener.ERROR_EMPTY_DATA || errorCode == ISwitchAudioListener.ERROR_IS_END) {
                    //语音唤醒听订阅
                    if (PlayInfoManager.getInstance().needInterceptPlayListLogic(currentAudio, currentAlbum)) {
                        //交给响应的处理逻辑去处理
                        return;
                    }
                }
                if (errorCode == ISwitchAudioListener.ERROR_SWITCH_OTHER_CAR) {
                    if (object != null && object instanceof RespCarFmCurTops) {
                        if (PlayInfoManager.getInstance().needInterceptPlayListCarFmLogic(((RespCarFmCurTops) object))) {
                            PlayInfoManager.getInstance().setNextCarFmTime(null);
                            return;
                        }
                    }
                }


                if (errorCode == ISwitchAudioListener.ERROR_IS_END
                        || errorCode == ISwitchAudioListener.ERROR_EMPTY_DATA
                        || errorCode == ISwitchAudioListener.ERROR_NULL_ALBUM) {
                    if (currentAudio != null && Utils.isSong(currentAudio.getSid())) {
                        playAudio(operation, PlayInfoManager.getInstance().getPlayListAudio(0));
                        return;
                    }
                    ToastUtils.showShortStopTTs("已经是最后一个了");
                }
                PlayEngineFactory.getEngine().pause(operation);
                PlayEngineFactory.getEngine().release(operation);
            }
        });
    }

    public void requestLoadMoreAudio(final Album album, final Audio audio) {
        AppLogic.runOnBackGround(
                new Runnable() {
                    @Override
                    public void run() {
                        NetManager.getInstance().requestAudio(Operation.auto, album, audio, true, album.getCategoryId(), null, new RequestAudioCallBack(album) {
                            @Override
                            public void onResponse(List<Audio> audios, Album album, ResponseAlbumAudio responseAlbumAudio) {
                                if (audios.isEmpty()) {
                                    PlayInfoManager.getInstance().setLastEnd(true);
                                } else {
                                    PlayInfoManager.getInstance().addAudios(true, audios);
                                    PlayInfoManager.getInstance().setRequestMoreAudio(audios.get(audios.size() - 1));
                                }
                            }

                            @Override
                            public void onError(String cmd, Error error) {
                                LogUtil.e(TAG + "switch more audios failed " + error.getErrorCode());
                                //TODO 当没联网的时候直接关闭请求，防止无限请求
                                if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                                    return;
                                }
                                if (PlayInfoManager.getInstance().getCurrentPlayerUIStatus() == PlayerInfo.PLAYER_UI_STATUS_PLAYING ||
                                        PlayInfoManager.getInstance().getCurrentPlayerUIStatus() == PlayerInfo.PLAYER_UI_STATUS_BUFFER) {
                                    requestLoadMoreAudio(album, audio);
                                } else {
                                    ToastUtils.showShortOnUI("请求超时");
                                }
                            }
                        });
                    }
                });
    }

    @Override
    public void last(final Operation operation) {
        if (PlayInfoManager.getInstance().isPlayListEmpty()) {
            LogUtil.e(TAG + " last: play list is empty");
            return;
        }

        switchAudio(operation, false, new ISwitchAudioListener() {
            @Override
            public void onAudioReady(Audio audio) {
                playAudio(operation, audio);
            }

            @Override
            public void onAudioUnavailable(int errorCode, Object object) {
                if (errorCode == ISwitchAudioListener.ERROR_IS_FIRST
                        || errorCode == ISwitchAudioListener.ERROR_EMPTY_DATA
                        || errorCode == ISwitchAudioListener.ERROR_NULL_ALBUM) {
//                    Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
//                    if (Utils.isSong(currentAudio.getSid())) {
//                        playAudio(operation, PlayInfoManager.getInstance().getPlayListAudio(
//                                PlayInfoManager.getInstance().getPlayListSize() - 1));
//                        return;
//                    }
                    if (operation.equals(Operation.extra)) {
                        //3S内没播放会导致TTS播放加载超时逻辑
                        TtsUtil.speakResource(Constant.RS_VOICE_ALREADY_FIRST, Constant.RS_VOICE_ALREADY_FIRST);
                    } else if (operation.equals(Operation.sound)) {
                        PlayerAboutAsrManager.getInstance().stopBroadcastTTs();
                        TtsUtil.speakResource(Constant.RS_VOICE_ALREADY_FIRST, Constant.RS_VOICE_ALREADY_FIRST);
                    } else {
                        ToastUtils.showShortStopTTs(Constant.RS_VOICE_ALREADY_FIRST);
                    }
                }
            }
        });
    }

    @Override
    public void playAudio(Operation operation, Audio willPlayAudio) {
        LogUtil.logd(TAG + "mode:" + SharedPreferencesUtils.getPlayMode() + ",operState=" + operation + ",willPlay=" + willPlayAudio);
        PlayEngineFactory.getEngine().release(operation);

        PlayInfoManager.getInstance().setCurrentAudio(willPlayAudio);
        PlayEngineFactory.getEngine().play(operation);
    }

    /**
     * 释放资源
     */
    @Override
    public void release(EnumState.Operation operation) {
        if (mPlayer == null) {//不应该在release的时候进行创建
            return;
        }
        if (EnumState.Operation.temp != operation) {
            MyFocusListener.getInstance().setManualPlay(false);
        }
//        getState().release(operState);
        releaseByState(operation);
        isRelease = true;
        if (mPlayer != null) {
            PlayerControlManager.getInstance().remoteAllCallback(mPlayer.hashCode());
        }
        mPlayer = null;
//        PlayInfoManager.getInstance().setCurrentAudio(null);
//        mState = getReleaseState();
//        setState(getReleaseState());
        setState(PlayerInfo.PLAYER_STATUS_RELEASE);
        PlayInfoManager.getInstance().setProgress(0, 0);
        LogUtil.logd(Constant.OOM_TAG + "createNewPlayer:release:(" + (mPlayer == null) + "/" + (isRelease) + ")");
    }

    @Override
    public void changeMode(EnumState.Operation operation) {
        PlayEngineFactory.getEngine().changeMode(operation, PlayInfoManager.getInstance().getNextPlayerMode());
    }

    @Override
    public void changeMode(EnumState.Operation operation, @PlayerInfo.PlayerMode int mode) {
        SharedPreferencesUtils.setPlayMode(mode);
        PlayInfoManager.getInstance().setCurrentPlayMode(mode);
    }

    @Override
    public int getState() {//多线程考虑
        return mState;
    }


    @Override
    public void setState(int state) {
        mState = state;
        switch (state) {
            case PlayerInfo.PLAYER_STATUS_BUFFER:
                PlayInfoManager.getInstance().setCurrentPlayerUIStatus(PlayerInfo.PLAYER_UI_STATUS_BUFFER);
                break;
            case PlayerInfo.PLAYER_STATUS_PAUSE:
                PlayInfoManager.getInstance().setCurrentPlayerUIStatus(PlayerInfo.PLAYER_UI_STATUS_PAUSE);
                break;
            case PlayerInfo.PLAYER_STATUS_PLAYING:
                PlayInfoManager.getInstance().setCurrentPlayerUIStatus(PlayerInfo.PLAYER_UI_STATUS_PLAYING);
                break;
            case PlayerInfo.PLAYER_STATUS_RELEASE:
                PlayInfoManager.getInstance().setCurrentPlayerUIStatus(PlayerInfo.PLAYER_UI_STATUS_RELEASE);
                break;
        }
    }

    @Override
    public synchronized TXZAudioPlayer getAudioPlayer() {
        if (mPlayer == null || isRelease) {
            LogUtil.logd(Constant.OOM_TAG + "createNewPlayer:begin:(" + (mPlayer == null) + "/" + (isRelease) + ")");
            mPlayer = createInnerPlayer();
        }
        return mPlayer;
    }

    @Override
    public void seekTo(EnumState.Operation operation, long position) {
//		if (PlayEngineFactory.getEngine().getAudioPlayer() != null) {
//			PlayEngineFactory.getEngine().getAudioPlayer().seekTo(percent);
//		}
        if (EnumState.Operation.temp != operation) {
            MyFocusListener.getInstance().setManualPlay(true);
        }
//        getState().seekTo(operation, position);
        seekByState(operation, position);
    }

    @Override
    public void searchListData(EnumState.Operation operation, boolean isDown) {
        mOperation.searchListData(operation, isDown);
    }

    @Override
    public void playOrPause(EnumState.Operation operation) {
//        getState().playOrPause(operation);
        playOrPauseByState(operation);
//        if (getState() == getReleaseState()) {
//            PlayEngineFactory.getEngine().prepareAsync(operation);
//        } else if (getState() == getPlayState()) {
//            PlayEngineFactory.getEngine().pause(operation);
//        } else if (getState() == getPauseState()) {
//            PlayEngineFactory.getEngine().play(operation);
//        }
    }

    @Override
    public void setVolume(EnumState.Operation operation, float volume) {
        if (PlayEngineFactory.getEngine().getAudioPlayer() != null) {
            PlayEngineFactory.getEngine().getAudioPlayer().setVolume(volume);
        }
    }

    @Override
    public Album getCurrentAlbum() {
        return PlayInfoManager.getInstance().getCurrentAlbum();
    }

    @Override
    public Audio getCurrentAudio() {
        return PlayInfoManager.getInstance().getCurrentAudio();
    }

    @Override
    public boolean isPlaying() {
//        return PlayEngineFactory.getEngine().getState() == PlayEngineFactory.getPlayState();
//        return PlayEngineFactory.getEngine().getState().getValue().equals(IPlayerState.PLAY_STATE);
        return PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_PLAYING;
    }

    @Override
    public void prepareAsync(final EnumState.Operation operation) {
        if (Operation.temp != operation) {
            MyFocusListener.getInstance().setManualPlay(true);
        }
        final Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (currentAudio != null) {
            TimeUtils.startTime(Constant.SPEND_TAG + "quick:prepare");
            LogUtil.logd(TAG + "startPlay:" + "name=" + currentAudio.getName() + ",operation="
                    + operation.name() + ",showUI=" + WinListener.isShowSoundUI
                    + ",needBroadcast=" + AlbumEngine.getInstance().isNeedBroadcast() + ",flag=" + currentAudio.getFlag());
//            if (Utils.getDataWithPosition(currentAudio.getFlag(), Audio.POS_NEED_REPORT) == Audio.FLAG_SUPPORT) {
//                String name = currentAudio.getReport();
//                if (StringUtils.isEmpty(currentAudio.getReport())) {
//                    name = currentAudio.getName();
//                }
//                String tips = Constant.RS_VOICE_MUSIC_PLAY_AUDIO + name;
//
//                if (mTtsId != -1) {
//                    TtsUtil.cancelSpeak(mTtsId);
//                }
//                if (!WinListener.isShowSoundUI
//                        && AlbumEngine.getInstance().isNeedBroadcast()
//                        && operation != EnumState.Operation.error
//                        && operation != EnumState.Operation.manual
//                        && mPlayedId != currentAudio.getId()) {
//                    mTtsId = TtsUtil.speakText(tips, new TtsUtil.ITtsCallback() {
//                        @Override
//                        public void onEnd() {
////                            getState().prepareAsync(operation);
//                            mPlayedId = currentAudio.getId();
//                            prepareAsyncByState(operation);
//                        }
//                    });
//                } else {
////                    getState().prepareAsync(operation);
//                    prepareAsyncByState(operation);
//                }
//                ComponentName topActivity = Utils.getTopActivity();
//                if (topActivity == null || (!TextUtils.equals(topActivity.getPackageName(), GlobalContext.get().getPackageName())))
//                    ToastUtils.showShortOnUI(tips);
//            } else {
//                getState().prepareAsync(operation);
            prepareAsyncByState(operation);
//            }
        } else {
            LogUtil.logd(TAG + "startPlay:" + "prepare  can't  finished");
        }
    }

    private static class PlayListChangedListener implements IPlayListChangedListener {

        @Override
        public void onPlayListChanged(List<Audio> audios) {
            if (CollectionUtils.isEmpty(audios)) {
                PlayInfoManager.getInstance().setCurrentAlbum(null);
                PlayInfoManager.getInstance().setCurrentAudio(null);
                PlayEngineFactory.getEngine().release(EnumState.Operation.manual);
                ObserverManage.getObserver().send(InfoMessage.PLAYER_INIT);
                return;
            }
            if (!audios.contains(PlayInfoManager.getInstance().getCurrentAudio())) {
                PlayEngineFactory.getEngine().next(EnumState.Operation.manual);
            }
        }
    }


    private class PrepareRunnable implements Runnable {
        EnumState.Operation operation = EnumState.Operation.error;

        @Override
        public void run() {
            prepareAsync(operation);
        }

        public void setOperation(EnumState.Operation operation) {
            this.operation = operation;
        }
    }

}
