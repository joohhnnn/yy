package com.txznet.music.playerModule.logic;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Path;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.net.request.ReqAlbumAudio;
import com.txznet.music.albumModule.logic.net.response.ResponseAlbumAudio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.EnumState.AudioType;
import com.txznet.music.baseModule.bean.EnumState.Operation;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.listener.WinListener;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestAudioCallBack;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.bean.IOperation;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.logic.factory.TxzAudioPlayerFactory;
import com.txznet.music.playerModule.logic.focus.HeadSetHelper;
import com.txznet.music.playerModule.logic.focus.MyFocusListener;
import com.txznet.music.power.PowerChangedListener;
import com.txznet.music.power.PowerManager;
import com.txznet.music.search.SearchEngine;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.music.utils.NetworkUtil;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.SyncCoreData;
import com.txznet.music.utils.SyncOtherAppBroadcastListener;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.TtsHelper;
import com.txznet.music.utils.TtsUtilWrapper;
import com.txznet.music.utils.Utils;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.List;
import java.util.Objects;


public class PlayEngineCoreDecorator implements IPlayerEngine, PowerChangedListener {
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
    private SyncOtherAppBroadcastListener syncOtherAppBroadcastListener = new SyncOtherAppBroadcastListener();
    private int mRetryCount = 0;
    /**
     * 倒车导致的暂停
     */
    private boolean isReversingPause = false;
    private PrepareRunnable prepareRunnable = new PrepareRunnable();

    public PlayEngineCoreDecorator() {
        mPlayerType = PlayEngineFactory.TYPE_ALL;
        init();
        PowerManager.getInstance().addPowerChangedListener(this);
    }

    public PlayEngineCoreDecorator(int type) {
        mPlayerType = type;
        init();
    }


    @Override
    public void playAudioList(Operation operation, List<Audio> audios, int index, Album album) {
        throw new RuntimeException("这个方法不能调用");
//        PlayEngineFactory.getEngine().setAudios(operState, audios, album, index);
//        PlayEngineFactory.getEngine().play(operState);
    }

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
                Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                if (currentAlbum != null) {
                    LogUtil.logd(TAG + "search:more:audios:" + currentAlbum.getName());
                    Audio reqAudio = null;
                    if (isDown) {
                        if (!PlayInfoManager.getInstance().isPlayListEmpty()) {
                            reqAudio = PlayInfoManager.getInstance().getPlayListAudio(PlayInfoManager.getInstance().getPlayListSize() - 1);
                        }
                    } else {
                        if (!PlayInfoManager.getInstance().isPlayListEmpty()) {
                            reqAudio = PlayInfoManager.getInstance().getPlayListAudio(0);
                        }
                    }

                    AlbumEngine.getInstance().requestMoreAudio(operation, currentAlbum, reqAudio, currentAlbum.getCategoryId(), isDown, null);
//                    NetManager.getInstance().requestAudio(operState, currentAlbum, reqAudio, isUp, currentAlbum.getCategoryID(), null);
//                    NetManager.getInstance().requestAudioByAudioId(currentAlbum, PlayInfoManager.getInstance().getCurrentAudio(), currentAlbum.getCategoryID());
                } else {
                    LogUtil.logd(TAG + "search:more:audios:null");
                    ObserverManage.getObserver().send(InfoMessage.REQUEST_AUDIO_RESPONSE);
                    if (Operation.manual == operation) {
                        ToastUtils.showShortOnUI(Constant.RS_VOICE_MUSIC_NO_MORE_DATA);
                    }
                }
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
            SyncOtherAppBroadcastListener syncOtherAppBroadcastListener = new SyncOtherAppBroadcastListener();
            PlayerControlManager.getInstance().addStatusListener(player.hashCode(), syncOtherAppBroadcastListener);
            PlayInfoManager.getInstance().addPlayerInfoUpdateListener(this.syncOtherAppBroadcastListener);
            PlayInfoManager.getInstance().addPlayListChangedListener(listChangedListener);
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
        if (operation != EnumState.Operation.sound) {
            SearchEngine.getInstance().setRawText("");//除声控切换导致的播单更新则需要将rawText（用于上报）进行清除
        }
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
        if (index < 0 || index > audios.size()) {
            if (audios.size() > 0) {
                currentAudio = audios.get(0);
            } else {
                LogUtil.d(TAG, "audios is empty");
                return;
            }
        } else {
            currentAudio = audios.get(index);
        }

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
        TimeUtils.endTime(Constant.SPEND_TAG + "quick:setAudios");
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
        Logger.d(TAG, "play:" + operation + " , " + Objects.toString(PlayInfoManager.getInstance().getCurrentAudio()));
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
                    ReqAlbumAudio reqAlbumAudio = new ReqAlbumAudio();
                    reqAlbumAudio.setSid(9);
                    reqAlbumAudio.setId(50100);
                    NetManager.getInstance().sendRequestToCore(Constant.GET_ALBUM_AUDIO, reqAlbumAudio, new RequestCallBack<ResponseAlbumAudio>(ResponseAlbumAudio.class) {
                        @Override
                        public void onResponse(ResponseAlbumAudio responseAlbumAudio) {
                            PlayEngineFactory.getEngine().setAudios(operation, responseAlbumAudio.getArrAudio(), null, 0, PlayInfoManager.DATA_NET);
                            PlayEngineFactory.getEngine().play(operation);
                        }

                        @Override
                        public void onError(String cmd, Error error) {
                            TtsUtilWrapper.speakText(Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS);
                        }
                    });
                }
                return;
            } else if (PlayInfoManager.getInstance().getCurrentAudio() == null) {
                //播放本地
                if (!playLocalMusic(operation)) {
                    TtsUtilWrapper.speakText(Constant.RS_VOICE_SPEAK_NONE_NET);
                }
                return;
            }
        }

//        getState().play(operation);
        playByState(operation);
    }

    private void playByState(EnumState.Operation operation) {
        int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
        Logger.d(TAG, "playByState:" + operation + " , " + currentPlayerStatus);
        switch (currentPlayerStatus) {
            case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                getAudioPlayer().start();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PAUSE:
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.inner.isPlaying", ("" + true).getBytes(), null);
                getAudioPlayer().start();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PLAYING:
                getAudioPlayer().start();
                break;
            case PlayerInfo.PLAYER_UI_STATUS_RELEASE:
                PlayEngineFactory.getEngine().prepareAsync(operation);
                break;
        }
    }

    private void pauseByState(EnumState.Operation operation) {
        int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
        Logger.d(TAG, "pauseByState:" + operation + " , " + currentPlayerStatus);
        switch (currentPlayerStatus) {
            case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                getAudioPlayer().pause();
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
                    DBManager.getInstance().saveMusicHistory(currentAudio);
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
        List<Audio> audios = DBManager.getInstance().findAllLocalAudios();
        if (audios != null && !audios.isEmpty()) {
            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.auto, audios, null, 0, PlayInfoManager.DATA_LOCAL);
//            getState().play(operation);
            playByState(operation);
            return true;
        }
        return false;
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
    }

    private void switchMoreAudio(final EnumState.Operation operation, final boolean isNext,
                                 @NonNull final ISwitchAudioListener listener) {
        final Audio audio;

        if (isNext) {
            audio = PlayInfoManager.getInstance().getPlayListAudio(PlayInfoManager.getInstance().getPlayListSize() - 1);
        } else {
            audio = PlayInfoManager.getInstance().getPlayListAudio(0);
        }


        if (!isNext && PlayInfoManager.getInstance().isFirstEnd()) {
            listener.onAudioUnavailable(ISwitchAudioListener.ERROR_IS_FIRST);
        } else if (isNext && PlayInfoManager.getInstance().isLastEnd()) {
            listener.onAudioUnavailable(ISwitchAudioListener.ERROR_IS_END);
        } else {
            Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
            if (null == currentAlbum) {
                listener.onAudioUnavailable(ISwitchAudioListener.ERROR_NULL_ALBUM);
                return;
            }




            AlbumEngine.getInstance().requestMoreAudio(operation, currentAlbum,
                    audio, currentAlbum.getCategoryId(), isNext,
                    new RequestAudioCallBack(currentAlbum) {
                        @Override
                        public void onResponse(List<Audio> audios, Album album, ResponseAlbumAudio responseAlbumAudio) {
                            mRetryCount = 0;
                            if (null == audios || audios.isEmpty()) {
                                listener.onAudioUnavailable(ISwitchAudioListener.ERROR_EMPTY_DATA);
                                return;
                            }
                            LogUtil.d(TAG + "switch more audios success " + audios.size());
                            PlayEngineFactory.getEngine().addAudios(operation, audios, isNext);
                            if (isNext) {
                                listener.onAudioReady(audios.get(0));
                            } else {
                                listener.onAudioReady(audios.get(audios.size() - 1));
                            }
                        }

                        @Override
                        public void onError(String cmd, Error error) {
                            super.onError(cmd, error);
                            LogUtil.e(TAG + "switch more audios failed " + error.getErrorCode());
                            if (error.getErrorCode() == Error.ERROR_CLIENT_NET_TIMEOUT) {
                                mRetryCount++;
                                if (mRetryCount > 2) {
                                    LogUtil.d(TAG, "request timeout, retry " + mRetryCount);
                                    switchMoreAudio(operation, isNext, listener);
                                    return;
                                }
                            }
                            listener.onAudioUnavailable(error.getErrorCode());
                        }
                    });
        }
    }

    /**
     * 通过回调的方式来切歌
     *
     * @param operation 操作来源
     * @param isNext    上一首还是下一首
     * @param listener  回调
     */
    private void switchAudio(EnumState.Operation operation, final boolean isNext,
                             @NonNull final ISwitchAudioListener listener) {
        PlayInfoManager playInfoManager = PlayInfoManager.getInstance();
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
        boolean force = operation != EnumState.Operation.auto;

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

        if (pos >= playInfoManager.getPlayListSize()) {
            switchMoreAudio(operation, true, listener);
        } else if (pos < 0) {
            switchMoreAudio(operation, false, listener);
        } else {
            listener.onAudioReady(playInfoManager.getPlayListAudio(pos));
        }
    }

    @Override
    public void next(final EnumState.Operation operation) {
        mNextOperationLastTime = true;
        if (PlayInfoManager.getInstance().isPlayListEmpty()) {
            LogUtil.e(TAG + " next: play list is empty");
            return;
        }

        switchAudio(operation, true, new ISwitchAudioListener() {
            @Override
            public void onAudioReady(Audio audio) {
                playAudio(operation, audio);
            }

            @Override
            public void onAudioUnavailable(int errorCode) {
                if (errorCode == ISwitchAudioListener.ERROR_IS_END
                        || errorCode == ISwitchAudioListener.ERROR_EMPTY_DATA
                        || errorCode == ISwitchAudioListener.ERROR_NULL_ALBUM) {
                    Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                    if (Utils.isSong(currentAudio.getSid())) {
                        playAudio(operation, PlayInfoManager.getInstance().getPlayListAudio(0));
                        return;
                    }
//                    ToastUtils.showShort("已经是最后一个了");

                    if (Operation.auto == operation) { // 自动切换
                        String albumName = null;
                        if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
                            albumName = PlayInfoManager.getInstance().getCurrentAlbum().getName();
                        }
                        if (TextUtils.isEmpty(albumName)) {
                            albumName = "当前专辑";
                        }
                        TtsUtil.speakText(albumName + "已全部播放完毕");
                    } else if (Operation.sound == operation || Operation.extra == operation) {
                        TtsUtil.speakText("已经是最后一个了");
                        return;
                    }
                }
                PlayEngineFactory.getEngine().pause(operation);
                PlayEngineFactory.getEngine().release(operation);
            }
        });
    }

    @Override
    public void last(final Operation operation) {
        mNextOperationLastTime = false;
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
            public void onAudioUnavailable(int errorCode) {
                if (errorCode == ISwitchAudioListener.ERROR_IS_FIRST
                        || errorCode == ISwitchAudioListener.ERROR_EMPTY_DATA
                        || errorCode == ISwitchAudioListener.ERROR_NULL_ALBUM) {
                    Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                    if (Utils.isSong(currentAudio.getSid())) {
                        playAudio(operation, PlayInfoManager.getInstance().getPlayListAudio(
                                PlayInfoManager.getInstance().getPlayListSize() - 1));
                        return;
                    }
                    TtsUtil.speakText("已经是第一个了");
//                    ToastUtils.showShort("已经是第一个了");
                }
            }
        });
    }

    private boolean mNextOperationLastTime;

    @Override
    public boolean isNextOperationLastTime() {
        return mNextOperationLastTime;
    }

    @Override
    public void playAudio(Operation operation, Audio willPlayAudio) {
        LogUtil.logd(TAG + "mode:" + SharedPreferencesUtils.getPlayMode() + ",operState=" + operation + ",willPlay=" + willPlayAudio);
        PlayEngineFactory.getEngine().release(operation);

        PlayInfoManager.getInstance().setCurrentAudio(willPlayAudio);
        PlayEngineFactory.getEngine().prepareAsync(operation);
    }

    /**
     * 释放资源
     */
    @Override
    public void release(EnumState.Operation operation) {
        Logger.d(TAG, "release:" + operation + "," + getState() + " , " + Objects.toString(mPlayer));
        setState(PlayerInfo.PLAYER_STATUS_RELEASE);
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
//        mState = getReleaseState();
//        setState(getReleaseState());
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
            /*
             * 这里的Player是RemoteAudioPlayer，RemoteAudioPlayer里面没有做实际播放的处理，只会做一些
             * 逻辑上的处理然后通过Message发送出来，在对应的Handler中生成AudioPlayer去播放。
             */
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
        MyFocusListener.getInstance().requestAudioFocusImmediately(AudioManager.AUDIOFOCUS_GAIN);
        if (Operation.temp != operation) {
            MyFocusListener.getInstance().setManualPlay(true);
        }
        final Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (currentAudio != null) {
            TimeUtils.startTime(Constant.SPEND_TAG + "quick:prepare");
            LogUtil.logd(TAG + "startPlay:" + "name=" + currentAudio.getName() + ",operation="
                    + operation.name() + ",showUI=" + WinListener.isShowSoundUI
                    + ",needBroadcast=" + AlbumEngine.getInstance().isNeedBroadcast() + ",flag=" + currentAudio.getFlag());
            if (Utils.getDataWithPosition(currentAudio.getFlag(), Audio.POS_NEED_REPORT) == Audio.FLAG_SUPPORT) {
                String name = currentAudio.getReport();
                if (StringUtils.isEmpty(currentAudio.getReport())) {
                    name = currentAudio.getName();
                }
                String tips = Constant.RS_VOICE_MUSIC_PLAY_AUDIO + name;

                if (mTtsId != -1) {
                    TtsUtil.cancelSpeak(mTtsId);
                }
                if (!WinListener.isShowSoundUI
                        && AlbumEngine.getInstance().isNeedBroadcast()
                        && operation != EnumState.Operation.error
                        && operation != EnumState.Operation.manual
                        && mPlayedId != currentAudio.getId()) {
                    mTtsId = TtsUtilWrapper.speakText(tips, new TtsUtil.ITtsCallback() {
                        @Override
                        public void onEnd() {
//                            getState().prepareAsync(operation);
                            mPlayedId = currentAudio.getId();
                            prepareAsyncByState(operation);
                        }
                    });
                } else {
//                    getState().prepareAsync(operation);
                    prepareAsyncByState(operation);
                }
                ComponentName topActivity = Utils.getTopActivity();
                if (topActivity == null || (!TextUtils.equals(topActivity.getPackageName(), GlobalContext.get().getPackageName())))
                    ToastUtils.showShortOnUI(tips);
            } else {
//                getState().prepareAsync(operation);
                prepareAsyncByState(operation);
            }
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
