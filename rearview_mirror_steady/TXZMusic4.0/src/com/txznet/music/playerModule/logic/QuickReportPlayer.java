package com.txznet.music.playerModule.logic;

import android.media.AudioManager;
import android.text.TextUtils;
import android.view.View;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.listener.WinListener;
import com.txznet.music.localModule.logic.StorageUtil;
import com.txznet.music.message.Message;
import com.txznet.music.net.HttpUtils;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.logic.factory.TxzAudioPlayerFactory;
import com.txznet.music.playerModule.logic.focus.MyFocusListener;
import com.txznet.music.playerModule.logic.focus.MyQuickPlayerFocusListener;
import com.txznet.music.power.PowerChangedListener;
import com.txznet.music.power.PowerManager;
import com.txznet.music.push.PushIntercepter;
import com.txznet.music.push.PushLogicHelper;
import com.txznet.music.push.PushManager;
import com.txznet.music.push.PushNotification;
import com.txznet.music.push.bean.PullData;
import com.txznet.music.push.bean.PushResponse;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.ReportManager;
import com.txznet.music.report.bean.PushEvent;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.PlayerCommunicationManager;
import com.txznet.music.utils.ToastUtils;
import com.txznet.sdk.TXZAsrManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ASUS User on 2016/12/21.
 */

public class QuickReportPlayer implements PowerChangedListener {

    public static final String ASR_TASK_ID = "ASR_TASK_ID_QUICK_REPORT";
    private static final String ASR_CMD_LISTEN = "CMD_LISTEN";
    private static final String ASR_CMD_CLOSE = "CMD_CLOSE";
    private static final String TAG = "QuickReportPlayer:";
    private static final int ASR_INTERVAL = 10000;
    MyQuickPlayerFocusListener focusListener;//焦点
    private PushNotification mPushNotification;
    private TXZAudioPlayer mPlayer;
    private boolean isReversing = false; //判断是否正在倒车
    private boolean needStart = false; //判断倒车结束后是否需要开始播放
    private boolean isPrepared = false;  //判断是否已经开始播放
    private int mCount; //倒计时
    private PushResponse mPushResponse;
    private PullData mPullData;
    private boolean mDefUserChoose; // 是否自动播放
    boolean mUserCancelBeforePlay; // 用户是否取消了

    private int mTtsId = -1;
    private Runnable mCountDownRunnable = new Runnable() {
        @Override
        public void run() {
            mPushNotification.setCountDown(mCount);

            if (mCount > 0) {
                mCount--;
                AppLogic.runOnUiGround(this, 1000);
            } else {
                if (Constant.isPennyTest) {
                    clickContinue();
                } else {
                    release();
                    saveMessage(mPushResponse, false);
                    ReportEvent.reportPushEvent(PushEvent.ACTION_TIMEOUT, PushEvent.getType(mPushResponse), mPushResponse.getMid());
                }
            }
        }
    };


    public QuickReportPlayer(PullData pullData, PushResponse pushResponse) {
        this.mPushResponse = pushResponse;
        this.mPullData = pullData;
        this.mDefUserChoose = pushResponse.getDefUserChoose() == 1;
    }

    private void initView() {
        List<PushResponse.Key> arrKeys = mPushResponse.getArrKeys();
        if (null == arrKeys || arrKeys.size() < 2) {
            Logger.e(TAG, "Key size error:" + (null == arrKeys ? "null" : arrKeys.size()));
            return;
        }

        mPushNotification = new PushNotification.Build()
                .setTitle(mPushResponse.getTitle())
                .setSubTitle(mPushResponse.getSubTitle())
                .setConfirmText(arrKeys.get(0).getText())
                .setCancelText(arrKeys.get(1).getText())
                .setIconUrl(mPushResponse.getIconUrl())
                .create();

        // 默认情况下，第一个按钮显示继续收听，第二个按钮显示取消
        // 自动播放情况下，第一个按钮显示不再收听，第二个按钮显示取消
        mPushNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickContinue();
            }
        });
        mPushNotification.setCloseListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCancel();
            }
        });

        isReversing = PowerManager.getInstance().isReversing();
        PowerManager.getInstance().addPowerChangedListener(this);
    }

    // 收听详情
    public void clickContinue() {
        if (mDefUserChoose && mPushResponse != null && PushResponse.POST_ACTION_NO_PROMPT == mPushResponse.getPostAction()) {
            mUserCancelBeforePlay = true;
            PlayEngineFactory.getEngine().pause(EnumState.Operation.manual);
        }
        PushManager.getInstance().playDetail(EnumState.Operation.manual, mPushResponse);
        clickRelease();
        ReportEvent.reportPushEvent(PushEvent.ACTION_CONTINUE_MANUAL, PushEvent.getType(mPushResponse), mPushResponse.getMid());
    }

    // 取消
    public void clickCancel() {
        if (mDefUserChoose) {
            mUserCancelBeforePlay = true;
            PlayEngineFactory.getEngine().pause(EnumState.Operation.manual);
        }
        clickRelease();
        ReportEvent.reportPushEvent(PushEvent.ACTION_CANCEL_MANUAL, PushEvent.getType(mPushResponse), mPushResponse.getMid());
    }

    private void clickRelease() {
        release();

        saveMessage(mPushResponse, true);

        TtsUtil.cancelSpeak(mTtsId);
    }

    public boolean isUserCancelBeforePlay() {
        return mUserCancelBeforePlay;
    }

    private void registerAsrCmd() {
        List<PushResponse.Key> arrKeys = mPushResponse.getArrKeys();
        if (arrKeys.size() < 2) {
            Logger.e(TAG, "Key cmd size error, size=" + arrKeys.size());
            return;
        }
        for (int i = 0; i < arrKeys.size(); i++) {
            PushResponse.Key key = arrKeys.get(i);
            if (null == key || null == key.getArrCms() || key.getArrCms().size() == 0) {
                Logger.e(TAG, "Key cmd size error, index=" + i);
                return;
            }
        }

        TXZAsrManager.AsrComplexSelectCallback asrCallBack = new TXZAsrManager.AsrComplexSelectCallback() {

            @Override
            public void onCommandSelected(String type, String command) {
                super.onCommandSelected(type, command);

                Logger.d(TAG, "type:" + type + " command:" + command + " isWakeup:" + isWakeupResult());
                switch (type) {
                    case ASR_CMD_LISTEN:
                        TimeUtils.startTime(Constant.SPEND_TAG + "quick:sound");
                        release();
                        if (mDefUserChoose && mPushResponse != null && PushResponse.POST_ACTION_NO_PROMPT == mPushResponse.getPostAction()) {
                            mUserCancelBeforePlay = true;
                            PlayEngineFactory.getEngine().pause(EnumState.Operation.sound);
                        }
                        if (!isWakeupResult()) {
                            String resId;
                            String defTts;
                            if (PushResponse.POST_ACTION_NO_PROMPT == mPushResponse.getPostAction()) {
                                resId = "RS_VOICE_MUSIC_WILL_CLOSE_SHORT_PLAY_ALWAYS";
                                defTts = Constant.RS_VOICE_MUSIC_WILL_CLOSE_SHORT_PLAY_ALWAYS;
                            } else {
                                resId = "RS_VOICE_MUSIC_WILL_PLAY_SHORT_PLAY";
                                defTts = Constant.RS_VOICE_MUSIC_WILL_PLAY_SHORT_PLAY;
                            }
                            TtsUtil.speakTextOnRecordWin(resId, defTts, true, new Runnable() {
                                @Override
                                public void run() {
                                    PushManager.getInstance().playDetail(EnumState.Operation.sound, mPushResponse);
                                }
                            });
                        } else {
                            PushManager.getInstance().playDetail(EnumState.Operation.sound, mPushResponse);
                        }
                        TimeUtils.endTime(Constant.SPEND_TAG + "quick:sound");

                        ReportEvent.reportPushEvent(PushEvent.ACTION_CONTINUE_SOUND, PushEvent.getType(mPushResponse), mPushResponse.getMid());
                        saveMessage(mPushResponse, true);
                        break;
                    case ASR_CMD_CLOSE:
                        release();
                        if (mDefUserChoose) {
                            mUserCancelBeforePlay = true;
                            PlayEngineFactory.getEngine().pause(EnumState.Operation.manual);
                        }
                        if (!isWakeupResult()) {
                            String resId;
                            String defTts;
                            if (PushResponse.POST_ACTION_NO_PROMPT == mPushResponse.getPostAction()) {
                                resId = "RS_VOICE_MUSIC_SHORT_PLAY_CANCELED";
                                defTts = Constant.RS_VOICE_MUSIC_SHORT_PLAY_CANCELED;
                            } else {
                                resId = "RS_VOICE_MUSIC_WILL_CLOSE_SHORT_PLAY";
                                defTts = Constant.RS_VOICE_MUSIC_WILL_CLOSE_SHORT_PLAY;
                            }
                            TtsUtil.speakTextOnRecordWin(resId, defTts, true, null);
                        }
                        ReportEvent.reportPushEvent(PushEvent.ACTION_CANCEL_SOUND, PushEvent.getType(mPushResponse), mPushResponse.getMid());
                        saveMessage(mPushResponse, true);
                        break;
                }
            }

            @Override
            public String getTaskId() {
                return ASR_TASK_ID;
            }

            @Override
            public boolean needAsrState() {
                return false;
            }
        };
        asrCallBack.addCommand(ASR_CMD_LISTEN, arrKeys.get(0).getArrCms().toArray(new String[0]));
        asrCallBack.addCommand(ASR_CMD_CLOSE, arrKeys.get(1).getArrCms().toArray(new String[0]));

        TXZAsrManager.getInstance().useWakeupAsAsr(asrCallBack);
    }


    private void playAudio(final Audio audio) {
        Logger.d(TAG, "start");
        mPlayer = TxzAudioPlayerFactory.createPlayer(audio);

        if (mPlayer == null) {
            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                    playAudio(audio);
                }
            }, 200);

            return;
        }

        focusListener = new MyQuickPlayerFocusListener(mPlayer) {
            @Override
            public void onAudioFocusChange(int focusChange) {
                super.onAudioFocusChange(focusChange);
                if (!WinListener.isShowSoundUI && AudioManager.AUDIOFOCUS_LOSS == focusChange) {
                    releaseDelay();
                }
            }
        };
        PlayerControlManager.getInstance().addStatusListener(mPlayer.hashCode(), new IPlayerStateListener() {
            @Override
            public void onIdle(Audio var1) {
                //release();
                PushIntercepter.getInstance().onStatusChange(PlayerCommunicationManager.STATE_ON_IDLE);
            }

            @Override
            public void onPlayerPreparing(Audio var1) {
                Logger.d(TAG, "onPlayerPreparing");
                if (!checkSafe2NextStep()) {
                    return;
                }
                if (mPlayer != null) {
                    mPlayer.start();
                }
                if (mPushNotification != null) {
                    mPushNotification.show();
                    if (mPushResponse.getReportData() != null) {
                        ReportManager.getInstance().reportImmediate(mPushResponse.getReportData());
                    }
                    ReportEvent.reportPushEvent(PushEvent.ACTION_SHOW, PushEvent.getType(mPushResponse), mPushResponse.getMid());
                }
                if (focusListener != null) {
                    focusListener.requestAudioFocus(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                }
                PushIntercepter.getInstance().onInfoChange(var1);
            }

            @Override
            public void onPlayerPrepareStart(Audio audio) {

            }

            @Override
            public void onPlayerPlaying(Audio var1) {
                Logger.d(TAG, "onPlayerPlaying");
                PushIntercepter.getInstance().onStatusChange(PlayerCommunicationManager.STATE_ON_PLAYING);
            }

            @Override
            public void onPlayerPaused(Audio var1) {
                Logger.d(TAG, "onPlayerPaused");
                //分别处理（此时是否由主播放器导致的）
                PushIntercepter.getInstance().onStatusChange(PlayerCommunicationManager.STATE_ON_PAUSED);
            }

            @Override
            public void onProgress(Audio audio, long position, long duration) {
                PushIntercepter.getInstance().onProgressChange(position / Constant.TIME_UNIT, duration / Constant.TIME_UNIT);
            }

            @Override
            public void onBufferProgress(Audio audio, List<LocalBuffer> buffers) {
                PushIntercepter.getInstance().onStatusChange(PlayerCommunicationManager.STATE_ON_BUFFERING);
            }

            @Override
            public void onPlayerFailed(Audio var1, Error error) {
                Logger.d(TAG, "onPlayerFailed " + error.toString());
                // FIXME: 2017/7/13 有一些错误发生时应该release掉
//                release();
                PushIntercepter.getInstance().onStatusChange(PlayerCommunicationManager.STATE_ON_FAILED);
            }


            @Override
            public void onPlayerEnd(Audio var1) {
                Logger.d(TAG, "onPlayerEnd");
                mPlayer.pause();
                releasePlayer();

                if (!checkSafe2NextStep()) {
                    return;
                }

                if (!TextUtils.isEmpty(mPushResponse.getEndTip()) && checkTip(mPushResponse.getEndTip())) {
                    mTtsId = TtsUtil.speakVoice(getCacheFile(mPushResponse.getEndTip()).getAbsolutePath(), new TtsUtil.ITtsCallback() {
                        @Override
                        public void onEnd() {
                            if (!checkSafe2NextStep()) {
                                return;
                            }
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    if (mPushNotification != null && mPushNotification.isShown()) {
                                        registerAsrCmd();
                                        checkNeedAutoPlay();
                                        startCountDown(ASR_INTERVAL);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    if (mPushNotification != null && mPushNotification.isShown()) {
                        registerAsrCmd();
                        checkNeedAutoPlay();
                        startCountDown(ASR_INTERVAL);
                    }
                }
            }

            @Override
            public void onSeekStart(Audio audio) {

            }

            @Override
            public void onSeekComplete(Audio audio, long seekTime) {

            }

            @Override
            public void onBufferingStart(Audio var1) {
            }

            @Override
            public void onBufferingEnd(Audio var1) {
            }
        });

        if (isReversing) {
            needStart = true;
        } else {
            mPlayer.prepareAsyncSub();
            isPrepared = true;
        }
    }

    private void playTTS(String tts) {
        mTtsId = TtsUtil.speakText(tts, new TtsUtil.ITtsCallback() {
            @Override
            public void onBegin() {
                super.onBegin();
                Logger.d(TAG, "playTTS:onBegin()");
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        if (!checkSafe2NextStep()) {
                            return;
                        }
                        if (mPushNotification != null) {
                            mPushNotification.show();
                            if (mPushResponse.getReportData() != null) {
                                ReportManager.getInstance().reportImmediate(mPushResponse.getReportData());
                            }
                            ReportEvent.reportPushEvent(PushEvent.ACTION_SHOW, PushEvent.getType(mPushResponse), mPushResponse.getMid());
                        }
                    }
                });
            }

            @Override
            public void onError(int iError) {
                super.onError(iError);
                Logger.d(TAG, "playTTS:onError()" + iError);
            }

            @Override
            public void onEnd() {
                Logger.d(TAG, "playTTS:onEnd()");
                if (!checkSafe2NextStep()) {
                    return;
                }
                if (!TextUtils.isEmpty(mPushResponse.getEndTip()) && checkTip(mPushResponse.getEndTip())) {
                    mTtsId = TtsUtil.speakVoice(getCacheFile(mPushResponse.getEndTip()).getAbsolutePath(), new TtsUtil.ITtsCallback() {
                        @Override
                        public void onEnd() {
                            if (!checkSafe2NextStep()) {
                                return;
                            }
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    if (mPushNotification != null && mPushNotification.isShown()) {
                                        registerAsrCmd();
                                        checkNeedAutoPlay();
                                        startCountDown(ASR_INTERVAL);
                                    }
                                }
                            });
                        }
                    });
                } else {
                    if (mPushNotification != null && mPushNotification.isShown()) {
                        registerAsrCmd();
                        checkNeedAutoPlay();
                        startCountDown(ASR_INTERVAL);
                    }
                }
            }
        });
    }

    private File getCacheFile(String tip) {
        return new File(StorageUtil.getOtherCacheDir(), String.valueOf(tip.hashCode()));
    }

    private void downloadTip(String tip) {
        Observable.just(tip)
                .map(new Function<String, Boolean>() {
                    @Override
                    public Boolean apply(String s) throws Exception {
                        int i = HttpUtils.downloadFile(s, getCacheFile(s).getAbsolutePath());
                        return i >= 0;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        Logger.d(TAG, "download tip:" + aBoolean);
                        if (aBoolean) {
                            start();
                        }
                    }
                });
    }


    private boolean checkTip(String tip) {
        return getCacheFile(tip).exists();
    }


    public void start() {
        mTtsId = TtsUtil.speakText("", new TtsUtil.ITtsCallback() {

            @Override
            public void onSuccess() {
                super.onSuccess();

                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        if (!checkSafe2NextStep()) {
                            return;
                        }

                        initView();

                        Logger.d(TAG, mPushResponse.toString());

                        if (!TextUtils.isEmpty(mPushResponse.getTip())) {
                            if (!checkTip(mPushResponse.getTip())) {
                                downloadTip(mPushResponse.getTip());
                                return;
                            }
                        }

                        if (!TextUtils.isEmpty(mPushResponse.getEndTip())) {
                            if (!checkTip(mPushResponse.getEndTip())) {
                                downloadTip(mPushResponse.getEndTip());
                                return;
                            }
                        }

                        int preAction = mPushResponse.getPreAction();
                        if (preAction == PushResponse.PRE_ACTION_FORCE_PLAY) {
                            List<Audio> audioList = mPushResponse.getArrAudio();
                            if (audioList == null || audioList.size() == 0) {
                                Logger.e(TAG, " !!!!!!!!!! push audio list is null or empty !!!!!!!!!! ");
                                return;
                            }
                            Audio audio = audioList.get(0);
                            playAudio(audio);
                        } else if (preAction == PushResponse.PRE_ACTION_PLAY_URL_OR_TTS) {
                            String tts = mPushResponse.getTts();
                            String mp3 = mPushResponse.getMp3();
                            Logger.d(TAG, "TTS:" + tts + " mp3:" + mp3);

                            // 先播放tip，然后按mp3>tts的优先级播报正文，最后再播放endTip
                            if (!TextUtils.isEmpty(mPushResponse.getTip()) && checkTip(mPushResponse.getTip())) {
                                mTtsId = TtsUtil.speakVoice(getCacheFile(mPushResponse.getTip()).getAbsolutePath(), new TtsUtil.ITtsCallback() {
                                    @Override
                                    public void onEnd() {
                                        super.onEnd();
                                        AppLogic.runOnUiGround(new Runnable() {
                                            @Override
                                            public void run() {
                                                startPlay(mPushResponse);
                                            }
                                        });
                                    }
                                });
                            } else {
                                startPlay(mPushResponse);
                            }
                        }
                    }
                });
            }
        });

    }


    private void startPlay(PushResponse pushResponse) {
        if (!checkSafe2NextStep()) {
            return;
        }

        String tts = mPushResponse.getTts();
        String mp3 = mPushResponse.getMp3();
        if (!TextUtils.isEmpty(mp3)) {
            Audio audio = new Audio();
            audio.setSid(9);
            audio.setId(9);
            audio.setDownloadType("0");
            audio.setStrDownloadUrl(mp3);
            playAudio(audio);
        } else if (!TextUtils.isEmpty(tts)) {
            playTTS(tts);
        }
    }


    public void release() {
        PowerManager.getInstance().removePowerChangedListener(this);
        releasePlayer();
        releaseView();
        TtsUtil.cancelSpeak(mTtsId);
    }

    public void releaseDelay() {
        TtsUtil.cancelSpeak(mTtsId);
        AppLogic.removeUiGroundCallback(mReleaseRunnable);
        AppLogic.runOnUiGround(mReleaseRunnable, 5000);
    }

    private Runnable mReleaseRunnable = new Runnable() {
        @Override
        public void run() {
            release();
        }
    };

    private void releaseView() {
        Logger.d(TAG, " release view");
        TXZAsrManager.getInstance().recoverWakeupFromAsr(ASR_TASK_ID);
        if (mPushNotification != null) {
            mPushNotification.dismiss();
        }
        if (null != focusListener) {
            focusListener.abandonAudioFocus();
            focusListener = null;
        }
        //有打开过界面,打开界面的时候,这里不能注册焦点
        if (ActivityStack.getInstance().has() && !MyFocusListener.playerInFocus) {
            MyFocusListener.getInstance().requestAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
        }

        AppLogic.removeUiGroundCallback(mCountDownRunnable);
    }

    private void releasePlayer() {
        Logger.d(TAG, " release player");
        if (mPlayer != null) {
            mPlayer.pause();
            mPlayer.release();
            PlayerControlManager.getInstance().remoteAllCallback(mPlayer.hashCode());
            mPlayer = null;
        }

    }

    @Override
    public void onSleep() {
    }

    @Override
    public void onWakeUp() {
    }

    @Override
    public void onExit() {
    }

    @Override
    public void onReverseStart() {
        isReversing = true;
        //如果开始倒车的时候已经播放快报，那就关闭
        if (isPrepared) {
            ToastUtils.showLong("倒车中，同听播报暂停使用");
            release();
        }
    }

    @Override
    public void onReverseEnd() {
        isReversing = false;
        //倒车过程中收到了快报推送就在倒车结束的时候再播放
        if (needStart) {
            mPlayer.prepareAsyncSub();
        }
    }

    public void checkNeedAutoPlay() {
        if (mDefUserChoose && !mUserCancelBeforePlay) {
            Logger.d(TAG, "checkNeedAutoPlay: true");
            PushManager.getInstance().playAlbumWrappers(this, EnumState.Operation.auto, mPushResponse);
        }
    }

    public void startCountDown(int count) {
        this.mCount = count / 1000;
        Logger.i(TAG, "start count down:" + count);
        ReportEvent.reportPushEvent(PushEvent.ACTION_COUNTDOWN, PushEvent.getType(mPushResponse), mPushResponse.getMid());
        AppLogic.removeUiGroundCallback(mCountDownRunnable);
        AppLogic.runOnUiGround(mCountDownRunnable, 0);
    }


    private void saveMessage(final PushResponse pushResponse, final boolean read) {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                Logger.i(TAG, "save message:" + read);
                if (TextUtils.equals(pushResponse.getService(), PushResponse.PUSH_SERVICE_UPDATE)) {

                    List<PushResponse.AlbumWrapper> arrAlbumWrappers = pushResponse.getArrAlbumWrappers();
                    if (null == arrAlbumWrappers || arrAlbumWrappers.size() == 0) {
                        Logger.e(TAG, "save update message error:album size is empty");
                        return;
                    }

                    List<Message> messages = new ArrayList<>();
                    for (PushResponse.AlbumWrapper albumWrapper : arrAlbumWrappers) {
                        Message message = new Message();

                        Album album = albumWrapper.getAlbum();
                        message.setAlbum(album);
                        message.setId(album.getId());
                        message.setSid(album.getSid());
                        message.setTime(pushResponse.getTime());
                        message.setType(Message.TYPE_ALBUM);
                        message.setTitle(albumWrapper.getTitle());
                        if (read) {
                            message.setStatus(Message.STATUS_READ);
                        } else {
                            message.setStatus(Message.STATUS_UNREAD);
                        }

                        messages.add(message);
                    }
                    DBManager.getInstance().saveMessages(messages);

                } else if (TextUtils.equals(pushResponse.getService(), PushResponse.PUSH_SERVICE_AUDIOS)) {
                    List<Audio> arrAudio = pushResponse.getArrAudio();

                    if (null == arrAudio || arrAudio.size() == 0) {
                        Logger.e(TAG, "save audios message error:audios size is empty");
                        return;
                    }

                    Message message = new Message();
                    message.setTime(pushResponse.getTime());
                    message.setType(Message.TYPE_AUDIO);
                    message.setTitle(pushResponse.getSubTitle());
                    message.setAudios(arrAudio);
                    if (read) {
                        message.setStatus(Message.STATUS_READ);
                    } else {
                        message.setStatus(Message.STATUS_UNREAD);
                    }

                    DBManager.getInstance().saveMessage(message);

                }

                if (!read) {
                    ObserverManage.getObserver().send(InfoMessage.MESSAGE_NEW_UNREAD);
                } else {
                    ObserverManage.getObserver().send(InfoMessage.MESSAGE_NEW_READ);

                }
            }
        });

    }

    // 检测是否可以执行下一步
    private boolean checkSafe2NextStep() {
        if (WinListener.isShowSoundUI) {
            LogUtil.d(TAG + "record win is showing, skip shortPlay");
            PushManager.getInstance().clickCancel();
            return false;
        }
        if (PushLogicHelper.getInstance().isAppOpened() && mPullData != null && PullData.TYPE_NEWS == mPullData.getType()) {
            LogUtil.d(TAG + "main view has been shown, skip shortPlay");
            PushManager.getInstance().clickCancel();
            return false;
        }
        return true;
    }
}
