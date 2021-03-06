package com.txznet.music.helper;

import android.media.AudioManager;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.IMediaPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.action.FavourActionCreator;
import com.txznet.music.action.HistoryActionCreator;
import com.txznet.music.action.LocalActionCreator;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.FavourAudioDao;
import com.txznet.music.data.db.dao.HistoryAlbumDao;
import com.txznet.music.data.db.dao.HistoryAudioDao;
import com.txznet.music.data.db.dao.LocalAudioDao;
import com.txznet.music.data.db.dao.PushItemDao;
import com.txznet.music.data.db.dao.SubscribeAlbumDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Category;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.Nothing;
import com.txznet.music.data.entity.PlayListData;
import com.txznet.music.data.entity.PlayMode;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.entity.SubscribeAlbum;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.model.logic.PlayLogicFactory;
import com.txznet.music.model.logic.album.AbstractPlayAlbum;
import com.txznet.music.model.logic.album.PlayAiPushAlbum;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.PlayInfoEvent;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.FileUtils;
import com.txznet.music.util.Logger;
import com.txznet.music.util.PlaySceneUtils;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.TtsHelper;
import com.txznet.proxy.cache.LocalBuffer;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.music.Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE;

/**
 * ?????????????????????
 *
 * @author zackzhou
 * @date 2018/12/3,14:27
 * <p>
 * fixme ????????????????????????????????????????????????
 */

public class PlayHelper {
    public static final String TAG = Constant.LOG_TAG_LOGIC + ":PlayHelper";

    private static final class Holder {
        private static final PlayHelper INSTANCE = new PlayHelper();
    }

    public static PlayHelper get() {
        return Holder.INSTANCE;
    }

    /**
     * ??????????????????
     */
    PlayScene mCurrScene = PlayScene.IDLE;

    /**
     * ?????????????????????
     */
    AudioV5 mCurrAudio;

    /**
     * ?????????????????????
     */
    Album mCurrAlbum;

    /**
     * ??????????????????
     */
    @IMediaPlayer.PlayState
    int mCurrPlayState;

    /**
     * ???????????????????????????????????????
     */
    private Operation mLastSwitchAudioOperation;

    /**
     * ?????????????????????????????????????????????
     */
    public void setLastSwitchAudioOperation(Operation operation) {
        this.mLastSwitchAudioOperation = operation;
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????fromUser???????????????
     */
    public Operation getLastSwitchAudioOperation() {
        return mLastSwitchAudioOperation;
    }

    /**
     * ??????????????????
     */
    public void setPlayScene(PlayScene scene) {
        Logger.d(TAG, "setPlayScene " + scene);
        mCurrScene = scene == null ? PlayScene.IDLE : scene;
    }

    /**
     * ????????????????????????
     */
    public PlayScene getCurrPlayScene() {
        return mCurrScene;
    }

    /**
     * ???????????????????????????
     */
    public AudioV5 getCurrAudio() {
        return mCurrAudio == null ? mCurrAudio = AudioConverts.convert2Audio(AudioPlayer.getDefault().getCurrentAudio()) : mCurrAudio;
    }

    /**
     * ???????????????????????????
     */
    public Album getCurrAlbum() {
        return mCurrAlbum;
    }

    /**
     * ?????????????????????
     */
    public int getCurrPlayState() {
        return mCurrPlayState;
    }

    /**
     * ???????????????????????????
     */
    public void setCurrAlbum(Album album) {
        Logger.d(TAG, "setCurrAlbum album=" + album);
        AppLogic.removeUiGroundCallback(mSkip2NextTask);
        this.mCurrAlbum = album;
        Disposable disposable = Observable.create(emitter -> {
            if (PlayHelper.get().mCurrAlbum != null) {
                PlayHelper.get().mCurrAlbum.isSubscribe = FavourHelper.isSubscribe(PlayHelper.get().mCurrAlbum.id, PlayHelper.get().mCurrAlbum.sid);
            }
            emitter.onNext(Nothing.NONE);
            emitter.onComplete();
        }).subscribeOn(Schedulers.single()).subscribe(result -> {
            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_ON_ALBUM_CHANGE).bundle(Constant.PlayConstant.KEY_ALBUM, album).build());
        });
        DisposableManager.get().add("onAlbumChanged", disposable);
    }

    /**
     * ???????????????????????????
     */
    public boolean isPlaying() {
        return mCurrPlayState == IMediaPlayer.STATE_ON_PLAYING;
    }

    /**
     * ???????????????????????????
     */
    public boolean isPause() {
        return mCurrPlayState == IMediaPlayer.STATE_ON_PAUSED;
    }

    /**
     * ?????????????????????????????????????????????????????????????????????
     */
    public boolean isPlayingUnStrict() {
        Logger.d(TAG, "mCurrPlayState=" + mCurrPlayState);
        return
                (mCurrPlayState == IMediaPlayer.STATE_ON_IDLE && mCurrAudio != null)
                        || mCurrPlayState == IMediaPlayer.STATE_ON_PREPARING
                        || mCurrPlayState == IMediaPlayer.STATE_ON_PREPARED
                        || mCurrPlayState == IMediaPlayer.STATE_ON_PLAYING
                        || mCurrPlayState == IMediaPlayer.STATE_ON_BUFFERING;
    }

    /**
     * ?????????????????????
     */
    private PlayMode mCurrPlayMode;

    /**
     * ?????????????????????????????????????????????
     */
    public PlayMode getCurrPlayMode() {
        if (mCurrPlayMode == null) {
            mCurrPlayMode = SharedPreferencesUtils.getMusicPlayModel();
        }
        return mCurrPlayMode;
    }

    /**
     * ?????????????????????????????????????????????
     */
    public void setCurrPlayMode(PlayMode playMode) {
        mCurrPlayMode = playMode;
        AppLogic.runOnBackGround(() -> {
            SharedPreferencesUtils.setMusicPlayMode(playMode);
        });
    }

    /**
     * ??????????????????
     */
    public void setPlayMode(Operation operation, PlayMode mode) {
        Logger.d(TAG, "setPlayMode mode=" + mode);
        // ??????
        boolean isSupport = true;
        if (PlaySceneUtils.isAiScene() || !PlaySceneUtils.isMusicScene()) {
            if (PlayMode.QUEUE_LOOP != mode) {
                isSupport = false;
            }
        }
        if (!isSupport) {
            Logger.d(TAG, "setPlayMode un support, mode=" + mode + ", scene=" + mCurrScene);
            if (Operation.SOUND == operation) {
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_PLAY_MODE_UN_SUPPORT", Constant.RS_VOICE_SPEAK_ASR_PLAY_MODE_UN_SUPPORT);
            } else if (Operation.AUTO != operation) {
                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_PLAY_MODE_UN_SUPPORT);
            }
            return;
        }
        setCurrPlayMode(mode);
        switch (mode) {
            case QUEUE_LOOP:
                if (PlaySceneUtils.isAiScene() || !PlaySceneUtils.isMusicScene()) {
                    AudioPlayer.getDefault().useQueuePlay();
                } else {
                    AudioPlayer.getDefault().useQueueLoop();
                }
                break;
            case RANDOM_PLAY:
                AudioPlayer.getDefault().useRandomPlay();
                break;
            case SINGLE_LOOP:
                AudioPlayer.getDefault().useSingleLoop();
                break;
            default:
                break;
        }
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAY_MODE_CHANGED).bundle(Constant.PlayConstant.KEY_PLAY_MODE, mCurrPlayMode).build());
    }

    /**
     * ?????????????????????????????????
     */
    public void useRadioPlayMode() {
        Logger.d(TAG, "useRadioPlayMode");
        AudioPlayer.getDefault().useQueuePlay(); // ????????????
    }

    /**
     * ?????????????????????????????????
     */
    public void resetLastMusicPlayMode() {
        Logger.d(TAG, "resetLastMusicPlayMode");
        PlayMode mode = getCurrPlayMode();
        setPlayMode(Operation.AUTO, mode);
    }

    /**
     * ??????????????????
     */
    public void setQueue(PlayScene scene, List<Audio> audioList) {
        if (PlayHelper.get().getCurrPlayScene() == null || PlayHelper.get().getCurrPlayScene() != scene) {
            PlayHelper.get().setPlayScene(scene);

            // ?????????????????????????????????
            if (PlayScene.AI_RADIO != scene
                    && PlayScene.ALBUM != scene
                    && PlayScene.HISTORY_ALBUM != scene
                    && PlayScene.FAVOUR_ALBUM != scene) {
                setCurrAlbum(null);
            }
        }
        AudioPlayer.getDefault().setQueue(audioList);
        if (PlaySceneUtils.isMusicScene() && !PlaySceneUtils.isAiScene()) {
            resetLastMusicPlayMode();
        } else {
            useRadioPlayMode();
        }
    }

    private int mLastClickAudioSid;
    private long mLastClickAudioId;

    /**
     * ???????????????????????????
     */
    public void markLastClickAudio(Audio audio) {
        mLastClickAudioSid = audio.sid;
        mLastClickAudioId = audio.id;
    }

    /**
     * ???????????????????????????sid
     */
    public int lastClickAudioSid() {
        return mLastClickAudioSid;
    }

    /**
     * ???????????????????????????id
     */
    public long lastClickAudioId() {
        return mLastClickAudioId;
    }

    /**
     * ??????????????????????????????
     */
    public int getAlbumAudioCount() {
        int queueSize = AudioPlayer.getDefault().getQueue().getSize();
        if (mCurrAlbum == null) {
            return queueSize;
        }
        try {
            int totalNum = mCurrAlbum.getExtraKey(Constant.AlbumExtra.TOTAL_NUM);
            if (totalNum > 0 && totalNum > queueSize) {
                queueSize = totalNum;
                for (Audio audio : AudioPlayer.getDefault().getQueue().getQueue()) {
                    Boolean isFromSoundChoice = audio.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE);
                    if (isFromSoundChoice != null && isFromSoundChoice) {
                        queueSize++;
                    }
                }
            }

        } catch (Exception e) {
        }

        return queueSize;
    }


    /**
     * ????????????
     */
    long mDuration;

    /**
     * ????????????
     */
    long mPosition;

    /**
     * ????????????????????????
     */
    public long getLastDuration() {
        return mDuration;
    }

    /**
     * ????????????????????????
     */
    public long getLastPosition() {
        return mPosition;
    }

    /**
     * ????????????
     */
    public void play(Audio audio) {
        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            if (AudioUtils.isSong(audio.sid)) {
                if (!AudioUtils.isLocalSong(audio.sid)) {
                    File file = AudioUtils.getAudioTMDFile(audio);
                    if (file == null || !file.exists()) {
                        ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                    }
                }
            } else {
                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            }
        }
        AudioPlayer.getDefault().play(audio);
    }

    /**
     * ??????????????????
     */
    public void play(PlayScene scene, List<? extends AudioV5> audioList, int index) {
        if (audioList == null || index < 0 || index > audioList.size() - 1) {
            return;
        }
        List<Audio> list = AudioConverts.convert2List(audioList, AudioConverts::convert2MediaAudio);
        PlayHelper.get().play(list.get(index));
        PlayHelper.get().setQueue(scene, list);
    }

    /**
     * ????????????
     */
    public void playImmediately(Audio audio) {
        AudioPlayer.getDefault().play(audio, AudioManager.AUDIOFOCUS_GAIN, AudioPlayer.PreemptType.PREEMPT_TYPE_IMMEDIATELY);
    }

    /**
     * ??????/?????????????????????
     */
    public void play(Operation operation) {
        Logger.d(TAG, "play, " + operation);
        if (PlayHelper.get().getCurrAlbum() != null) {
            if (PlayHelper.get().getCurrAlbum().isPlayEnd || PlayHelper.get().getCurrAudio() == null) {
                // ?????????????????????????????????????????????
                Logger.d(TAG, "album is playEnd or unload, replay now");
                PlayHelper.get().playAlbum(operation, PlayHelper.get().getCurrPlayScene(), PlayHelper.get().getCurrAlbum());
                PlayHelper.get().getCurrAlbum().isPlayEnd = false;
                return;
            }
        }
        if (mCurrAlbum != null || mCurrAudio != null) {
            // ????????????
            if (mCurrAudio != null && IMediaPlayer.STATE_ON_PAUSED == mCurrPlayState) {
                AudioPlayer.getDefault().start();
            }
            return;
        }
        AppLogic.runOnBackGround(() -> {
            PlayListData playListData = PlayListDataHelper.getPlayListData();
            Logger.d(TAG, "play, last play info=" + playListData);
            if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                // ??????
                if (playListData == null) {
                    // ?????????????????????????????????AI??????
                    Logger.d(TAG, "play, will play ai radio");
                    playAi(operation);
                } else {
                    // ??????????????????????????????
                    if (playListData.audio != null) { // ???????????????
                        Logger.d(TAG, "play, will play last play");
                        setCurrAlbum(playListData.album);
                        if (playListData.audioList == null) {
                            playListData.audioList = new ArrayList<>(1);
                            playListData.audioList.add(playListData.audio);
                        }
                        setQueue(playListData.scene, playListData.audioList);
                        play(playListData.audio);
                    } else if (playListData.audioList != null && !playListData.audioList.isEmpty() && checkVerify(playListData.audioList.get(0))) { // ???????????????
                        Logger.d(TAG, "play, will play last play");
                        setCurrAlbum(playListData.album);
                        setQueue(playListData.scene, playListData.audioList);
                        play(playListData.audioList.get(0));
                    } else if (playListData.album != null && playListData.scene != null) { // ???????????????
                        playAlbum(operation, playListData.scene, playListData.album);
                    } else {
                        // ?????????????????????????????????????????????/??????(???????????????????????????????????????????????????????????????
                        // ???????????????????????????
                        if (PlayScene.AI_RADIO == playListData.scene || playListData.scene == null) {
                            Logger.w(TAG, "play, last play is empty");
                            // ?????????????????????????????????AI??????
                            Logger.d(TAG, "play, will play ai radio");
                            playAi(operation);
                        }
                        boolean isLastPlayMusic = true;
                        if (PlayScene.HISTORY_ALBUM == playListData.scene
                                || PlayScene.FAVOUR_ALBUM == playListData.scene) {
                            isLastPlayMusic = false;
                        }
                        if (PlayScene.ALBUM == playListData.scene) {
                            if (playListData.album != null && !AlbumUtils.isMusic(playListData.album)) {
                                isLastPlayMusic = false;
                            }
                        }
                        int count = 0;
                        if (isLastPlayMusic) {
                            count = DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao().getCount();
                            Logger.d(TAG, "play,  checkHistory count=" + count);
                            if (count > 0) {
                                Logger.d(TAG, "play, will play history music");
                                playHistoryMusic();
                            }
                        } else {
                            count = DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao().getNotHiddenCount();
                            Logger.d(TAG, "play,  checkHistory count=" + count);
                            if (count > 0) {
                                Logger.d(TAG, "play, will play history album");
                                playHistoryAlbum();
                            }
                        }
                        if (count == 0) {
                            Logger.d(TAG, "play, will play ai radio");
                            playAi(operation);
                        }
                    }
                }
            } else {
                // ????????????????????????????????????????????????????????????
                Logger.d(TAG, "play, will play local");
                if (playListData == null
                        || PlayScene.LOCAL_MUSIC != playListData.scene
                        || playListData.audioList == null || playListData.audio == null) {
                    playLocalUnDirect(operation);
                } else {
                    Logger.d(TAG, "play, resume local scene from last play");
                    playListData(playListData, true);
                }
            }
        });
    }

    // ?????????????????????
    private boolean checkVerify(Audio audio) {
        if (audio.sourceUrl == null) {
            return false;
        }
        return audio.name != null;
    }

    /**
     * ????????????
     */
    private void playLocalUnDirect(Operation operation) {
        playLocal(operation, false, false);
    }

    /**
     * ????????????
     *
     * @param direct ?????????????????????????????????????????????/????????????????????????
     */
    public void playLocal(Operation operation, boolean needResume, boolean direct) {
        Logger.d(TAG, "playLocal");
        if (PlayScene.LOCAL_MUSIC == mCurrScene && mCurrAudio != null) {
            if (IMediaPlayer.STATE_ON_PAUSED == mCurrPlayState) {
                AudioPlayer.getDefault().start();
            }
            return;
        }
        Disposable disposable = Observable.create((ObservableOnSubscribe<List<LocalAudio>>) emitter -> {
            List<LocalAudio> localAudioList = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao().listAll();
            if (SharedPreferencesUtils.isLocalSortByTime()) {
                LocalAudioSortUtil.sortAudiosByTime(localAudioList);
            } else {
                LocalAudioSortUtil.sortAudiosByName(localAudioList);
            }
            emitter.onNext(localAudioList);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.isEmpty()) {
                        if (Operation.MANUAL == operation) {
                            ToastUtils.showShortOnUI(Constant.TIP_LOCAL_AUDIO_EMPTY);
                        } else {
                            if (direct) {
                                TtsHelper.speakResource("RS_VOICE_SPEAK_LOCAL_AUDIO_EMPTY", Constant.RS_VOICE_SPEAK_LOCAL_AUDIO_EMPTY);
                            } else {
                                TtsHelper.speakResource("RS_VOICE_SPEAK_PLAY_NO_CONTENT", Constant.RS_VOICE_SPEAK_PLAY_NO_CONTENT);
                                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                            }
                        }
                        LocalActionCreator.get().scan(Operation.AUTO);
                    } else {
                        AppLogic.runOnBackGround(() -> {
                            PlayListData playListData = PlayListDataHelper.getPlayListData();
                            List<Audio> audioList = AudioConverts.convert2List(list, AudioConverts::convert2MediaAudio);
                            setQueue(PlayScene.LOCAL_MUSIC, audioList);
                            if (needResume && playListData != null && PlayScene.LOCAL_MUSIC == playListData.scene
                                    && playListData.audio != null && audioList.contains(playListData.audio)) {
                                PlayHelper.get().play(playListData.audio);
                            } else {
                                if (Operation.SOUND == operation || Operation.MANUAL == operation) {
                                    PlayHelper.get().play(audioList.get((int) (Math.random() * audioList.size())));
                                } else {
                                    PlayHelper.get().play(audioList.get(0));
                                }
                            }
                        });
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    Logger.e(TAG, "playLocal error " + throwable);
                });
        DisposableManager.get().add("playLocal", disposable);
    }

    /**
     * ??????????????????
     *
     * @param audio ????????????????????????
     */
    public void playLocal(AudioV5 audio) {
        Logger.d(TAG, "playLocal, bPlay=" + audio);
        Disposable disposable = Observable.create((ObservableOnSubscribe<List<LocalAudio>>) emitter -> {
            List<LocalAudio> localAudioList = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao().listAll();
            if (SharedPreferencesUtils.isLocalSortByTime()) {
                LocalAudioSortUtil.sortAudiosByTime(localAudioList);
            } else {
                LocalAudioSortUtil.sortAudiosByName(localAudioList);
            }
            emitter.onNext(localAudioList);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    if (list.isEmpty()) {
                        TtsHelper.speakNetworkError();
                    } else {
                        List<Audio> audioList = AudioConverts.convert2List(list, AudioConverts::convert2MediaAudio);
                        setQueue(PlayScene.LOCAL_MUSIC, audioList);
                        PlayHelper.get().play(AudioConverts.convert2MediaAudio(audio));
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    Logger.e(TAG, "playLocal error " + throwable);
                });
        DisposableManager.get().add("playLocal", disposable);
    }

    /**
     * ????????????/????????????
     */
    public void playMusic(Operation operation) {
        Logger.d(TAG, "playMusic");
        // ??????????????????????????????????????????
        AudioV5 currAudio = PlayHelper.get().getCurrAudio();
        if (currAudio != null && AudioUtils.isSong(currAudio.sid)) {
            Logger.d(TAG, "playMusic continue play");
            AudioPlayer.getDefault().start();
            return;
        }
        AppLogic.runOnBackGround(() -> {
            PlayListData playListData = PlayListDataHelper.getPlayListData();
            // ??????????????????
            if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                // ??????
                if (playListData == null || playListData.audioList == null || playListData.audioList.isEmpty() || !AudioUtils.isSong(playListData.audioList.get(0).sid)
                        || (PlayScene.ALBUM == playListData.scene && playListData.album == null)) {
                    // ????????????????????????????????????????????????????????????
                    int historyCount = DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao().getCount();
                    Logger.d(TAG, "playMusic checkHistory count=" + historyCount);
                    if (historyCount > 0) {
                        // ?????????????????????
                        Logger.d(TAG, "playMusic playHistoryMusic");
                        PlayHelper.get().playHistoryMusic();
                    } else {
                        // ????????????
                        Logger.d(TAG, "playMusic playRecommend");
                        PlayHelper.get().playRecommend(operation);
                    }
                } else {
                    // ???????????????????????????
                    Logger.d(TAG, "playMusic playLast");
                    PlayHelper.get().playListData(playListData, true);
                }
            } else {
                // ??????
                if (playListData == null
                        || PlayScene.LOCAL_MUSIC != playListData.scene
                        || playListData.audioList == null || playListData.audio == null) {
                    LocalAudioDao localAudioDao = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao();
                    int count = localAudioDao.getCount();
                    if (count == 0) {
                        Logger.d(TAG, "playMusic playLocal, local data is empty");
                        // ???????????????????????????????????????
                        if (Operation.SOUND == operation) {
                            TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_NET_OFFLINE", RS_VOICE_SPEAK_ASR_NET_OFFLINE, null);
                        } else {
                            ToastUtils.showShortOnUI(RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                        }
                    } else {
                        // ??????????????????
                        Logger.d(TAG, "playMusic playLocal");
                        PlayHelper.get().playLocalUnDirect(operation);
                    }
                } else {
                    // ?????????????????????
                    Logger.d(TAG, "playMusic, resume local scene from last play");
                    playListData(playListData, true);
                }
            }
        });
    }

    /**
     * ????????????/????????????
     */
    public void playRadio(Operation operation) {
        Logger.d(TAG, "playRadio");
        // ?????????????????????????????????????????????
        AudioV5 currAudio = PlayHelper.get().getCurrAudio();
        if (currAudio != null && !AudioUtils.isSong(currAudio.sid)) {
            Logger.d(TAG, "playRadio continue play");
            AudioPlayer.getDefault().start();
            return;
        }
        // ??????????????????
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            // ??????
            AppLogic.runOnBackGround(() -> {
                Album album = null;
                // ??????????????????????????????
//                PlayListData playListData = PlayListDataHelper.getPlayListData();
//                if (playListData != null) {
//                    album = playListData.album;
//                }
                // ????????????????????????
                if (album == null) {
                    HistoryAlbumDao historyAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao();
                    List<HistoryAlbum> historyAlbumList = historyAlbumDao.listAllNotHidden();
                    if (historyAlbumList != null && !historyAlbumList.isEmpty()) {
                        album = historyAlbumList.get(historyAlbumList.size() - 1);
                    }
                }
                if (album == null) {
                    // ???????????????????????????????????????????????????
                    playCarFm(operation);
                } else {
                    Logger.d(TAG, "playRadio play history album");
                    playAlbum(operation, PlayScene.HISTORY_ALBUM, album);
                }
            });
        } else {
            // ??????
            TtsHelper.speakResource("RS_VOICE_SPEAK_PLAY_NO_CONTENT", Constant.RS_VOICE_SPEAK_PLAY_NO_CONTENT);
            ToastUtils.showLongOnUI(Constant.RS_VOICE_SPEAK_PLAY_NO_CONTENT);
        }
    }


    /**
     * ???????????????????????????
     */
    private void playCarFm(Operation operation) {
        Logger.d(TAG, "playRadio play carFm");

        Category category = new Category();
        category.categoryId = 8589934592L; // FIXME: 2019/3/14 ??????ID
        Disposable disposable = TXZMusicDataSource.get().listAlbum(0, category, 1, 1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(albums -> {
                    if (albums != null && albums.size() > 0) {
                        playAlbum(operation, PlayScene.ALBUM, albums.get(0));
                    }
                }, throwable -> {
                    Logger.e(TAG, "playRadio play carFm failed, album list is empty");
                });
        DisposableManager.get().add("playCarFm", disposable);
    }

    /**
     * ??????AI??????
     */
    public void playAi(Operation operation) {
        playAi(operation, null);
    }

    /**
     * ??????AI???????????????????????????????????????????????????????????????????????????
     */
    public void playAi(Operation operation, Audio audio) {
        if (PlayScene.AI_RADIO == mCurrScene && mCurrAudio != null && audio == null) {
            if (IMediaPlayer.STATE_ON_PAUSED == mCurrPlayState) {
                AudioPlayer.getDefault().start();
            }
            return;
        }
        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            if (Operation.SOUND == operation) {
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_NET_OFFLINE", Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            } else {
                ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            }
            return;
        }
        Album album = new Album();
        album.id = 1000001;
        album.sid = 100;
        album.name = "AI??????";
        album.logo = SharedPreferencesUtils.getAiRadioLogoUrl();
        if (audio == null) {
            playAlbum(operation, PlayScene.AI_RADIO, album);
        } else {
            if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                if (Operation.SOUND == operation) {
                    TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_NET_OFFLINE", Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                } else {
                    ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                }
                return;
            }
            new PlayAiPushAlbum(album).onPlayAlbumWithAudio(operation, audio);
        }
    }

    /**
     * ??????????????????
     */
    public void playRecommend(Operation operation) {
        Album album = new Album();
        album.id = 1000002;
        album.albumType = Album.ALBUM_TYPE_MUSIC;
        album.sid = 100;
        album.name = "??????????????????";
        album.logo = SharedPreferencesUtils.getRecommendLogoUrl();
        playAlbum(operation, PlayScene.ALBUM, album);
    }

    /**
     * ??????????????????
     */
    public void playListData(PlayListData listData, boolean isMusic) {
        Logger.d(TAG, "playListData, album=" + listData.album + ", audio=" + listData.audio);
        setCurrAlbum(listData.album);
        setQueue(listData.scene, listData.audioList);
        if (isMusic) {
            PlayHelper.get().resetLastMusicPlayMode();
        } else {
            PlayHelper.get().useRadioPlayMode();
        }
        PlayHelper.get().play(listData.audio == null ? listData.audioList.get(0) : listData.audio);
    }

    /**
     * ????????????????????????????????????
     */
    public void playChoiceAudio(AudioV5 audioV5, List<? extends AudioV5> queue) {
        Logger.d(TAG, "playChoiceAudio audio=" + audioV5);
        if (queue != null && queue.size() > 1) {
            AudioV5 currAudio = PlayHelper.get().getCurrAudio();
            if (currAudio != null) {
                ProxyHelper.releaseProxyRequest(currAudio.sid, currAudio.id);
                ReportEvent.reportAudioPlayEnd(currAudio,
                        PlayInfoEvent.MANUAL_TYPE_MANUAL,
                        AudioUtils.isLocalSong(currAudio.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE, PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(),
                        PlayInfoEvent.EXIT_TYPE_SOUND, false);
            }

            // ?????????????????????
            setCurrAlbum(null);
            setQueue(PlayScene.ALBUM, AudioConverts.convert2List(queue, AudioConverts::convert2MediaAudio));
            PlayHelper.get().play(AudioConverts.convert2MediaAudio(audioV5));
            Logger.d(TAG, "playChoiceAudio with resp queue");
            return;
        }
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            // ???????????????????????????
            Audio audio = AudioConverts.convert2MediaAudio(audioV5);
            // ??????
            if (mCurrAudio != null && PlayScene.AI_RADIO != mCurrScene && (AudioUtils.isSong(mCurrAudio.sid) || PlayScene.WECHAT_PUSH == mCurrScene)) {
                AudioV5 currAudio = PlayHelper.get().getCurrAudio();
                if (currAudio != null) {
                    ProxyHelper.releaseProxyRequest(currAudio.sid, currAudio.id);
                    ReportEvent.reportAudioPlayEnd(currAudio,
                            PlayInfoEvent.MANUAL_TYPE_MANUAL,
                            AudioUtils.isLocalSong(currAudio.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE, PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(),
                            PlayInfoEvent.EXIT_TYPE_SOUND, false);
                }

                // ?????????????????????????????????AI?????????????????????????????????????????????
                resetLastMusicPlayMode();
                audio.setExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE, true);
                AudioPlayer.getDefault().getQueue().remove(audio); // ??????
                if (mCurrAlbum != null) {
                    try {
                        int totalNum = mCurrAlbum.getExtraKey(Constant.AlbumExtra.TOTAL_NUM);
                        if (totalNum > 1) {
                            mCurrAlbum.setExtraKey(Constant.AlbumExtra.TOTAL_NUM, --totalNum);
                        }
                    } catch (Exception e) {
                    }
                }
                AudioPlayer.getDefault().play(audio, AudioManager.AUDIOFOCUS_GAIN, AudioPlayer.PreemptType.PREEMPT_TYPE_NEXT);
                AudioPlayer.getDefault().play(audio);
                Logger.d(TAG, "playChoiceAudio insert into musicList");
            } else {
                // ???????????????????????????
                // FIXME: 2019/4/13 ????????????AI????????????????????????????????????????????????
                if (PlayScene.AI_RADIO != getCurrPlayScene()) {
                    clear();
                }
                Logger.d(TAG, "playChoiceAudio switch to ai play mode");
                playAi(Operation.SOUND, audio);
            }
        } else {
            AudioV5 currAudio = PlayHelper.get().getCurrAudio();
            if (currAudio != null) {
                ProxyHelper.releaseProxyRequest(currAudio.sid, currAudio.id);
                ReportEvent.reportAudioPlayEnd(currAudio,
                        PlayInfoEvent.MANUAL_TYPE_MANUAL,
                        AudioUtils.isLocalSong(currAudio.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE, PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(),
                        PlayInfoEvent.EXIT_TYPE_SOUND, false);
            }

            // ??????
            AppLogic.runOnBackGround(() -> {
                if (AudioUtils.existsLocal(audioV5)) {
                    // ????????????????????????
                    Logger.d(TAG, "playChoiceAudio begin play local audio");
                    playLocal(audioV5);
                } else {
                    // FIXME: 2018/12/28 ????????????
                    LocalAudioDao localAudioDao = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao();
                    List<LocalAudio> audioList = localAudioDao.findBySql(LocalAudio.getSearchQuery(audioV5.name, null, audioV5.albumName));
                    if (audioList != null && !audioList.isEmpty()) {
                        Logger.d(TAG, "playChoiceAudio begin play local audio");
                        playLocal(audioList.get(0));
                    } else {
                        // ??????????????????
                        TtsHelper.speakNetworkError();
                    }
                }
            });
        }
    }

    /**
     * ????????????
     */
    public void playAlbum(Operation operation, PlayScene scene, Album album) {
        AbstractPlayAlbum playAlbumLogic = PlayLogicFactory.get().getPlayAlbumLogic(album);
        if (AlbumUtils.isAiRadio(album)) {
            scene = PlayScene.AI_RADIO;
        } else if (scene == null) {
            scene = PlayScene.ALBUM;
        }
        playAlbumLogic.onPlayAlbum(operation, scene);
    }

    /**
     * ????????????????????????????????????
     */
    public void playChoiceAlbum(Album album) {
        Logger.d(TAG, "playChoiceAlbum album=" + album);
        playAlbum(Operation.SOUND, PlayScene.ALBUM, album);
    }

    /**
     * ????????????
     */
    public void playSubscribe() {
        Logger.d(TAG, "playSubscribe");
        AppLogic.runOnBackGround(() -> {
            SubscribeAlbumDao subscribeAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao();
            SubscribeAlbum subscribeAlbum = subscribeAlbumDao.getFirst();
            if (subscribeAlbum == null) {
                // ?????????????????????
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_SUBSCRIBE_EMPTY", Constant.RS_VOICE_SPEAK_ASR_SUBSCRIBE_EMPTY);
            } else {
                playAlbum(Operation.SOUND, PlayScene.FAVOUR_ALBUM, subscribeAlbum);
            }
        });
    }

    /**
     * ????????????
     */
    public void playFavour(AudioV5 audioV5) {
        if (PlayScene.FAVOUR_MUSIC == mCurrScene && mCurrAudio != null) {
            if (IMediaPlayer.STATE_ON_PAUSED == mCurrPlayState) {
                AudioPlayer.getDefault().start();
            }
            return;
        }
        Logger.d(TAG, "playFavour");
        AppLogic.runOnBackGround(() -> {
            FavourAudioDao favourAudioDao = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao();
            List<FavourAudio> favourAudioList = favourAudioDao.listAll();
            AudioUtils.removeLocalNotExists(favourAudioList);
            if (favourAudioList.isEmpty()) {
                // ?????????????????????
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_FAVOUR_EMPTY", Constant.RS_VOICE_SPEAK_ASR_FAVOUR_EMPTY);
            } else {
                List<Audio> audioList = AudioConverts.convert2List(favourAudioList, AudioConverts::convert2MediaAudio);

                setQueue(PlayScene.FAVOUR_MUSIC, audioList);
                if (audioV5 == null) {
                    if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                        play(audioList.get((int) (Math.random() * audioList.size())));
                    } else {
                        // ??????????????????????????????
                        int index = (int) (Math.random() * audioList.size());
                        for (int i = 0; i < audioList.size(); i++) {
                            Audio audio = audioList.get(i);
                            if (AudioUtils.isLocalSong(audio.sid)) {
                                index = i;
                                break;
                            } else {
                                File tmdFile = AudioUtils.getAudioTMDFile(audio);
                                if (tmdFile != null && tmdFile.exists()) {
                                    index = i;
                                    break;
                                }
                            }
                        }
                        play(audioList.get(index));
                    }
                } else {
                    play(AudioConverts.convert2MediaAudio(audioV5));
                }
            }
        });
    }

    /**
     * ????????????
     */
    public void playFavour() {
        playFavour(null);
    }

    /**
     * ??????????????????
     */
    public void playHistoryAlbum() {
        Logger.d(TAG, "playHistoryAlbum");
        AppLogic.runOnBackGround(() -> {
            HistoryAlbumDao historyAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao();
            List<HistoryAlbum> historyAlbumList = historyAlbumDao.listAllNotHidden();
            if (historyAlbumList.isEmpty()) {
                // ?????????????????????
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_SUBSCRIBE_EMPTY", Constant.RS_VOICE_SPEAK_ASR_SUBSCRIBE_EMPTY);
            } else {
                Collections.reverse(historyAlbumList);
                playAlbum(Operation.AUTO, PlayScene.HISTORY_ALBUM, historyAlbumList.get(0));
            }
        });
    }

    /**
     * ??????????????????
     */
    public void playHistoryMusic(AudioV5 audioV5) {
        Logger.d(TAG, "playHistoryMusic");
        AppLogic.runOnBackGround(() -> {
            HistoryAudioDao historyAudioDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao();
            List<HistoryAudio> historyAudioList = historyAudioDao.listAll();
            AudioUtils.removeLocalNotExists(historyAudioList);
            if (historyAudioList.isEmpty()) {
                // ?????????????????????
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_HISTORY_EMPTY", Constant.RS_VOICE_SPEAK_ASR_HISTORY_EMPTY);
            } else {
                List<Audio> audioList = AudioConverts.convert2List(historyAudioList, AudioConverts::convert2MediaAudio);
                Collections.reverse(audioList);
                setQueue(PlayScene.HISTORY_MUSIC, audioList);
                if (audioV5 == null) {
                    if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                        play(audioList.get(0));
                    } else {
                        // ??????????????????????????????
                        int index = 0;
                        for (int i = 0; i < audioList.size(); i++) {
                            Audio audio = audioList.get(i);
                            if (AudioUtils.isLocalSong(audio.sid)) {
                                index = i;
                                break;
                            } else {
                                File tmdFile = AudioUtils.getAudioTMDFile(audio);
                                if (tmdFile != null && tmdFile.exists()) {
                                    index = i;
                                    break;
                                }
                            }
                        }
                        play(audioList.get(index));
                    }
                } else {
                    play(AudioConverts.convert2MediaAudio(audioV5));
                }
            }
        });
    }

    /**
     * ??????????????????
     */
    public void playHistoryMusic() {
        playHistoryMusic(null);
    }

    /**
     * ??????????????????
     */
    public void playWxPush(AudioV5 audioV5) {
        Logger.d(TAG, "playWxPush");
        AppLogic.runOnBackGround(() -> {
            PushItemDao pushItemDao = DBUtils.getDatabase(GlobalContext.get()).getPushItemDao();
            List<PushItem> pushItemList = pushItemDao.listAll();
            if (pushItemList.isEmpty()) {
                // ???????????????
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_WX_EMPTY", Constant.RS_VOICE_SPEAK_ASR_WX_EMPTY);
            } else {
                List<Audio> audioList = AudioConverts.convert2List(pushItemList, AudioConverts::convert2MediaAudio);
                setQueue(PlayScene.WECHAT_PUSH, audioList);
                if (audioV5 == null) {
                    if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                        play(audioList.get((int) (Math.random() * audioList.size())));
                    } else {
                        // ??????????????????????????????
                        int index = (int) (Math.random() * audioList.size());
                        for (int i = 0; i < audioList.size(); i++) {
                            Audio audio = audioList.get(i);
                            if (AudioUtils.isLocalSong(audio.sid)) {
                                index = i;
                                break;
                            } else {
                                File tmdFile = AudioUtils.getAudioTMDFile(audio);
                                if (tmdFile != null && tmdFile.exists()) {
                                    index = i;
                                    break;
                                }
                            }
                        }
                        play(audioList.get(index));
                    }
                } else {
                    play(AudioConverts.convert2MediaAudio(audioV5));
                }
            }
        });
    }

    /**
     * ??????????????????
     */
    public void playWxPush() {
        playWxPush(null);
    }

    /**
     * ?????????????????????????????????????????????????????????(sid=0)??????????????????????????????
     * ?????????????????????????????????????????????????????????
     */
    public void removeLocalAudioCascade(List<? extends AudioV5> audioList, boolean containLocal) {
        // ???????????????????????????????????????????????????????????????
        if (PlayScene.FAVOUR_MUSIC == PlayHelper.get().getCurrPlayScene()
                || PlayScene.HISTORY_MUSIC == PlayHelper.get().getCurrPlayScene()
                || PlayScene.LOCAL_MUSIC == PlayHelper.get().getCurrPlayScene()
                || PlayScene.ALBUM == PlayHelper.get().getCurrPlayScene()) {
            Logger.d(TAG, "removeLocalAudioCascade");
            List<Audio> bRemove = new ArrayList<>();
            List<Audio> queue = AudioPlayer.getDefault().getQueue().getQueue();
            for (AudioV5 audioV5 : audioList) {
                if (PlayScene.LOCAL_MUSIC == PlayHelper.get().getCurrPlayScene()
                        || AudioUtils.isLocalSong(audioV5.sid)) {
                    for (Audio item : queue) {
                        if (item.sid == audioV5.sid && item.id == audioV5.id) {
                            bRemove.add(item);
                            break;
                        }
                    }
                }
            }
            AudioPlayer.getDefault().getQueue().removeItem(bRemove);
            if (queue.isEmpty()) {
                clear();
            } else {
                // ????????????????????????????????????????????????
                if (mCurrAudio != null && audioList.contains(mCurrAudio)) {
                    // ????????????????????????
                    if (PlayScene.LOCAL_MUSIC == PlayHelper.get().getCurrPlayScene()
                            || AudioUtils.isLocalSong(mCurrAudio.sid)) {
                        Audio audio = AudioPlayer.getDefault().getCurrentAudio();
                        if (audio == null) {
                            AudioPlayer.getDefault().next(true);
                        } else {
                            AudioPlayer.getDefault().play(audio);
                        }
                    }
                }
            }

            // ??????????????????????????????(sid=0???????????????)
            AppLogic.runOnBackGround(() -> {
                if (containLocal) {
                    LocalActionCreator.get().deleteLocal(Operation.AUTO, AudioConverts.convert2List(audioList, AudioConverts::convert2LocalAudio));
                }
                List<HistoryAudio> historyAudioList = new ArrayList<>();
                for (AudioV5 audioV5 : audioList) {
                    if (AudioUtils.isLocalSong(audioV5.sid)) {
                        HistoryAudio audio = new HistoryAudio();
                        audio.id = audioV5.id;
                        audio.sid = audioV5.sid;
                        historyAudioList.add(audio);
                    }
                }
                HistoryAudioDao historyAudioDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao();
                historyAudioDao.delete(historyAudioList);
                HistoryActionCreator.getInstance().getHistoryMusicData(Operation.AUTO);

                List<FavourAudio> favourAudioList = new ArrayList<>();
                for (AudioV5 audioV5 : audioList) {
                    if (AudioUtils.isLocalSong(audioV5.sid)) {
                        FavourAudio audio = new FavourAudio();
                        audio.id = audioV5.id;
                        audio.sid = audioV5.sid;
                        favourAudioList.add(audio);
                    }
                }
                FavourAudioDao favourAudioDao = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao();
                favourAudioDao.delete(favourAudioList);
                FavourActionCreator.getInstance().getData(Operation.AUTO, null);
            });
        }
    }


    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????
     * ?????????????????????????????????????????????????????????
     *
     * @deprecated ????????????????????????????????????????????????????????????????????????????????????
     */
    @Deprecated
    public void removeHistoryAudioCascade(List<? extends AudioV5> audioList) {
        if (PlayScene.HISTORY_MUSIC == PlayHelper.get().getCurrPlayScene()) {
            Logger.d(TAG, "removeHistoryAudioCascade");
            List<Audio> bRemove = new ArrayList<>();
            List<Audio> queue = AudioPlayer.getDefault().getQueue().getQueue();
            for (AudioV5 audioV5 : audioList) {
                for (Audio item : queue) {
                    if (item.sid == audioV5.sid && item.id == audioV5.id) {
                        bRemove.add(item);
                        break;
                    }
                }
            }
            AudioPlayer.getDefault().getQueue().removeItem(bRemove);
            if (queue.isEmpty()) {
                clear();
            } else {
                // ????????????????????????????????????????????????
                if (mCurrAudio != null && audioList.contains(mCurrAudio)) {
                    Audio audio = AudioPlayer.getDefault().getCurrentAudio();
                    if (audio == null) {
                        AudioPlayer.getDefault().next(true);
                    } else {
                        AudioPlayer.getDefault().play(audio);
                    }
                }
            }
        }
    }

    /**
     * ??????????????????????????????
     * ?????????????????????????????????????????????????????????
     */
    public void cleanNotExistMediaAndNext(Operation operation) {
        Logger.d(TAG, "cleanNotExistMediaAndNext");
        List<Audio> audioList = AudioPlayer.getDefault().getQueue().getQueue();
        List<Audio> bRemove = new ArrayList<>();
        if (!audioList.isEmpty()) {
            for (int i = 0; i < audioList.size(); i++) {
                Audio audio = audioList.get(i);
                if (audio != null && AudioUtils.isLocalSong(audio.sid) && audio.sourceUrl != null && !FileUtils.isExist(audio.sourceUrl)) {
                    bRemove.add(audio);
                }
            }
        }
        if (bRemove.isEmpty()) {
            // ??????????????????????????????
            ToastUtils.showShortOnUI(Constant.TIP_MEDIA_CAN_NOT_PLAY);
            if (AudioPlayer.getDefault().getQueue().getSize() > 1
                    || PlayScene.AI_RADIO == getCurrPlayScene()) { // AI?????????????????????????????????????????????????????????????????????????????????????????????????????????
                skipOp = operation;
                AppLogic.removeUiGroundCallback(mSkip2NextTask);
                AppLogic.runOnUiGround(mSkip2NextTask, 100);
            }
        } else {
            // ???????????????????????????
            Logger.d(TAG, "cleanNotExistMediaAndNext, be remove=" + bRemove);
            ToastUtils.showShortOnUI(Constant.TIP_MEDIA_NOT_EXISTS);
            removeLocalAudioCascade(AudioConverts.convert2List(bRemove, AudioConverts::convert2LocalAudio), true);
        }
    }

    private Operation skipOp;
    private final Runnable mSkip2NextTask = () -> {
        PlayerActionCreator.get().next(skipOp);
    };

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????
     */
    public void mergeQueueWithLocalAudio(List<LocalAudio> localAudioList) {
        if (PlayScene.LOCAL_MUSIC == PlayHelper.get().getCurrPlayScene()) {
            Logger.d(TAG, "mergeQueueWithLocalAudio");
            boolean hasFind = false;
            if (SharedPreferencesUtils.isLocalSortByTime()) {
                LocalAudioSortUtil.sortAudiosByTime(localAudioList);
            } else {
                LocalAudioSortUtil.sortAudiosByName(localAudioList);
            }
            List<Audio> audioLists = AudioConverts.convert2List(localAudioList, AudioConverts::convert2MediaAudio);
            if (mCurrAudio != null) {
                Audio currAudio = AudioConverts.convert2MediaAudio(mCurrAudio);
                Boolean fromChoice = mCurrAudio.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE, null);
                if (fromChoice != null && fromChoice && !AudioUtils.isLocalSong(mCurrAudio.sid)) {
                    audioLists.remove(currAudio);
                    audioLists.add(0, currAudio);
                    hasFind = true;
                } else {
                    for (LocalAudio localAudio : localAudioList) {
                        if (localAudio.sid == mCurrAudio.sid && localAudio.id == mCurrAudio.id) {
                            hasFind = true;
                            break;
                        }
                    }
                }
            }
            if (audioLists.isEmpty()) {
                clear();
            } else {
                setQueue(PlayScene.LOCAL_MUSIC, audioLists);
                if (!hasFind && localAudioList.size() > 0) {
                    play(AudioConverts.convert2MediaAudio(localAudioList.get((int) (Math.random() * localAudioList.size()))));
                }
            }
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     */
    @Deprecated
    public void mergeQueueWithHistoryAudio(List<HistoryAudio> historyAudioList) {
        if (PlayScene.HISTORY_MUSIC == PlayHelper.get().getCurrPlayScene()) {
            Logger.d(TAG, "mergeQueueWithHistoryAudio");
            List<Audio> audioLists = AudioConverts.convert2List(historyAudioList, AudioConverts::convert2MediaAudio);
            if (audioLists.isEmpty()) {
                clear();
            } else {
                setQueue(PlayScene.HISTORY_MUSIC, audioLists);
            }
        }
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????????????????
     *
     * @deprecated ??????????????????????????????
     */
    @Deprecated
    public void mergeQueueWithFavourAudio(List<FavourAudio> favourAudioList) {
        if (PlayScene.FAVOUR_MUSIC == PlayHelper.get().getCurrPlayScene()) {
            Logger.d(TAG, "mergeQueueWithFavourAudio");
            List<Audio> audioLists = AudioConverts.convert2List(favourAudioList, AudioConverts::convert2MediaAudio);
            if (audioLists.isEmpty()) {
                clear();
            } else {
                setQueue(PlayScene.FAVOUR_MUSIC, audioLists);
            }
        }
    }

    /**
     * ??????????????????
     * ??????????????????????????????????????????TF????????????????????????????????????????????????????????????
     */
    public void refreshQueueIfNotExists() {
        AppLogic.runOnBackGround(() -> {
            List<Audio> audioList = null;
            PlayScene scene = getCurrPlayScene();
            boolean bNext = false;
            if (PlayScene.FAVOUR_MUSIC == scene || PlayScene.HISTORY_MUSIC == scene || PlayScene.ALBUM == scene) {
                // ???????????????????????????????????????
                List<Audio> playQueue = AudioPlayer.getDefault().getQueue().getQueue();
                List<Audio> bRemove = new ArrayList<>();
                for (Audio audio : playQueue) {
                    if (AudioUtils.isLocalSong(audio.sid) && !FileUtils.isExist(audio.sourceUrl)) {
                        bRemove.add(audio);
                    }
                }
                AudioPlayer.getDefault().getQueue().removeItem(bRemove);
                if (mCurrAudio != null && !playQueue.contains(AudioConverts.convert2MediaAudio(mCurrAudio))) {
                    bNext = true;
                }
                audioList = playQueue;
            }
            if (audioList != null) {
                if (audioList.isEmpty()) {
                    clear();
                } else {
                    Audio audio = AudioPlayer.getDefault().getCurrentAudio();
//                if (audio != null) {
//                    Boolean fromChoice = (Boolean) audio.extra.get(Constant.AudioExtra.FROM_SOUND_CHOICE);
//                    if (fromChoice != null && fromChoice) {
//                        AudioPlayer.getDefault().getQueue().remove(audio);
//                        AudioPlayer.getDefault().getQueue().addToQueue(audio, 0);
//                    }
//                }
                    if (bNext) {
                        if (audio == null) {
                            AudioPlayer.getDefault().next(true);
                        } else {
                            AudioPlayer.getDefault().play(audio);
                        }
                    }
                }
            }
        });
    }

    /**
     * ????????????????????????????????????
     */
    public void clear() {
        Logger.d(TAG, "clear");
        AudioPlayer.getDefault().clear();
        setCurrAlbum(null);
        setPlayScene(PlayScene.IDLE);
        lastBuffs = null;
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PROXY_BUFFERING_UPDATE).bundle(Constant.PlayConstant.KEY_PLAY_BUFFER, new ArrayList<>()).build());
    }

    public void clearNotNotify() {
        Logger.d(TAG, "clearNotNotify");
        mCurrAudio = null;
        lastBuffs = null;
        mDuration = 0;
        mPosition = 0;
    }

    public void clearNotAlbum() {
        Logger.d(TAG, "clearNotAlbum");
        AudioPlayer.getDefault().clear();
        setPlayScene(PlayScene.IDLE);
        lastBuffs = null;
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PROXY_BUFFERING_UPDATE).bundle(Constant.PlayConstant.KEY_PLAY_BUFFER, new ArrayList<>()).build());
    }


    boolean mPausedByTransientLossOfFocus = false;

    /**
     * ??????????????????????????????????????????
     */
    public boolean isPausedByTransientLossOfFocus() {
        return mPausedByTransientLossOfFocus;
    }

    List<LocalBuffer> lastBuffs;

    /**
     * ?????????????????????????????????
     */
    public List<LocalBuffer> getLastLocalBuffer() {
        return lastBuffs;
    }


    /**
     * ????????????
     */
    public void replay() {
        Logger.d(TAG, "replay");
        AudioPlayer.getDefault().replay();
    }

    /**
     * ??????????????????
     */
    public void replay(AudioV5 audio) {
        Logger.d(TAG, "replay, audio=" + audio);
        if (audio != null) {
            AudioPlayer.getDefault().replay(AudioConverts.convert2MediaAudio(audio));
        }
    }

    /**
     * ???????????????????????????????????????
     */
    public void deleteCacheAndNext(Operation operation) {
        Audio audio = AudioPlayer.getDefault().getCurrentAudio();
        if (audio != null) {
            File file = AudioUtils.getAudioTMDFile(audio);
            if (file != null && file.exists()) {
                file.delete();
            }
            skipOp = operation;
            AppLogic.removeUiGroundCallback(mSkip2NextTask);
            AppLogic.runOnUiGround(mSkip2NextTask, 100);
        }
    }
}
