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
 * 播放器辅助工具
 *
 * @author zackzhou
 * @date 2018/12/3,14:27
 * <p>
 * fixme 优化，应该把播放逻辑都改成同步的
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
     * 当前播放场景
     */
    PlayScene mCurrScene = PlayScene.IDLE;

    /**
     * 当前播放的音频
     */
    AudioV5 mCurrAudio;

    /**
     * 当前播放的专辑
     */
    Album mCurrAlbum;

    /**
     * 当前播放状态
     */
    @IMediaPlayer.PlayState
    int mCurrPlayState;

    /**
     * 最后一次切换音频的操作来源
     */
    private Operation mLastSwitchAudioOperation;

    /**
     * 设置最后一次切换音频的操作来源
     */
    public void setLastSwitchAudioOperation(Operation operation) {
        this.mLastSwitchAudioOperation = operation;
    }

    /**
     * 获取最后一次切换音频的操作来源（不涵盖音频播完后自动切换的逻辑，但可以通过监听的fromUser进行判断）
     */
    public Operation getLastSwitchAudioOperation() {
        return mLastSwitchAudioOperation;
    }

    /**
     * 设置播放场景
     */
    public void setPlayScene(PlayScene scene) {
        Logger.d(TAG, "setPlayScene " + scene);
        mCurrScene = scene == null ? PlayScene.IDLE : scene;
    }

    /**
     * 获取当前播放场景
     */
    public PlayScene getCurrPlayScene() {
        return mCurrScene;
    }

    /**
     * 获取当前播放的音频
     */
    public AudioV5 getCurrAudio() {
        return mCurrAudio == null ? mCurrAudio = AudioConverts.convert2Audio(AudioPlayer.getDefault().getCurrentAudio()) : mCurrAudio;
    }

    /**
     * 获取当前播放的专辑
     */
    public Album getCurrAlbum() {
        return mCurrAlbum;
    }

    /**
     * 给当前播放状态
     */
    public int getCurrPlayState() {
        return mCurrPlayState;
    }

    /**
     * 设置当前播放的专辑
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
     * 当前是否处于播放中
     */
    public boolean isPlaying() {
        return mCurrPlayState == IMediaPlayer.STATE_ON_PLAYING;
    }

    /**
     * 当前是否处于暂停中
     */
    public boolean isPause() {
        return mCurrPlayState == IMediaPlayer.STATE_ON_PAUSED;
    }

    /**
     * 当前是否处于播放中，非严谨模式，即将会播放都算
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
     * 当前的播放模式
     */
    private PlayMode mCurrPlayMode;

    /**
     * 获取当前播放模式，音乐专辑适用
     */
    public PlayMode getCurrPlayMode() {
        if (mCurrPlayMode == null) {
            mCurrPlayMode = SharedPreferencesUtils.getMusicPlayModel();
        }
        return mCurrPlayMode;
    }

    /**
     * 设置当前播放模式，音乐专辑适用
     */
    public void setCurrPlayMode(PlayMode playMode) {
        mCurrPlayMode = playMode;
        AppLogic.runOnBackGround(() -> {
            SharedPreferencesUtils.setMusicPlayMode(playMode);
        });
    }

    /**
     * 切换播放模式
     */
    public void setPlayMode(Operation operation, PlayMode mode) {
        Logger.d(TAG, "setPlayMode mode=" + mode);
        // 检测
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
     * 切换到电台专辑播放模式
     */
    public void useRadioPlayMode() {
        Logger.d(TAG, "useRadioPlayMode");
        AudioPlayer.getDefault().useQueuePlay(); // 顺序播放
    }

    /**
     * 重置之前的音乐播放模式
     */
    public void resetLastMusicPlayMode() {
        Logger.d(TAG, "resetLastMusicPlayMode");
        PlayMode mode = getCurrPlayMode();
        setPlayMode(Operation.AUTO, mode);
    }

    /**
     * 设置播放队列
     */
    public void setQueue(PlayScene scene, List<Audio> audioList) {
        if (PlayHelper.get().getCurrPlayScene() == null || PlayHelper.get().getCurrPlayScene() != scene) {
            PlayHelper.get().setPlayScene(scene);

            // 非专辑场景清空当前专辑
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
     * 标记最后点击的音频
     */
    public void markLastClickAudio(Audio audio) {
        mLastClickAudioSid = audio.sid;
        mLastClickAudioId = audio.id;
    }

    /**
     * 获取最后点击的音频sid
     */
    public int lastClickAudioSid() {
        return mLastClickAudioSid;
    }

    /**
     * 获取最后点击的音频id
     */
    public long lastClickAudioId() {
        return mLastClickAudioId;
    }

    /**
     * 获取专辑下音频的总数
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
     * 播放时长
     */
    long mDuration;

    /**
     * 播放进度
     */
    long mPosition;

    /**
     * 获取上次播放时长
     */
    public long getLastDuration() {
        return mDuration;
    }

    /**
     * 获取上次播放进度
     */
    public long getLastPosition() {
        return mPosition;
    }

    /**
     * 播放音频
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
     * 播放音频列表
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
     * 播放音频
     */
    public void playImmediately(Audio audio) {
        AudioPlayer.getDefault().play(audio, AudioManager.AUDIOFOCUS_GAIN, AudioPlayer.PreemptType.PREEMPT_TYPE_IMMEDIATELY);
    }

    /**
     * 播放/打开同听的逻辑
     */
    public void play(Operation operation) {
        Logger.d(TAG, "play, " + operation);
        if (PlayHelper.get().getCurrAlbum() != null) {
            if (PlayHelper.get().getCurrAlbum().isPlayEnd || PlayHelper.get().getCurrAudio() == null) {
                // 当前专辑播放完毕，重头开始播放
                Logger.d(TAG, "album is playEnd or unload, replay now");
                PlayHelper.get().playAlbum(operation, PlayHelper.get().getCurrPlayScene(), PlayHelper.get().getCurrAlbum());
                PlayHelper.get().getCurrAlbum().isPlayEnd = false;
                return;
            }
        }
        if (mCurrAlbum != null || mCurrAudio != null) {
            // 继续播放
            if (mCurrAudio != null && IMediaPlayer.STATE_ON_PAUSED == mCurrPlayState) {
                AudioPlayer.getDefault().start();
            }
            return;
        }
        AppLogic.runOnBackGround(() -> {
            PlayListData playListData = PlayListDataHelper.getPlayListData();
            Logger.d(TAG, "play, last play info=" + playListData);
            if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                // 有网
                if (playListData == null) {
                    // 没有上次播放内容，播放AI电台
                    Logger.d(TAG, "play, will play ai radio");
                    playAi(operation);
                } else {
                    // 尝试恢复上次播放内容
                    if (playListData.audio != null) { // 音频没丢失
                        Logger.d(TAG, "play, will play last play");
                        setCurrAlbum(playListData.album);
                        if (playListData.audioList == null) {
                            playListData.audioList = new ArrayList<>(1);
                            playListData.audioList.add(playListData.audio);
                        }
                        setQueue(playListData.scene, playListData.audioList);
                        play(playListData.audio);
                    } else if (playListData.audioList != null && !playListData.audioList.isEmpty() && checkVerify(playListData.audioList.get(0))) { // 列表没丢失
                        Logger.d(TAG, "play, will play last play");
                        setCurrAlbum(playListData.album);
                        setQueue(playListData.scene, playListData.audioList);
                        play(playListData.audioList.get(0));
                    } else if (playListData.album != null && playListData.scene != null) { // 专辑没丢失
                        playAlbum(operation, playListData.scene, playListData.album);
                    } else {
                        // 根据上次播放类型，播放历史音乐/电台(存在加载专辑，但音频没刷新出来关闭进程，或
                        // 上次播放的是否音乐
                        if (PlayScene.AI_RADIO == playListData.scene || playListData.scene == null) {
                            Logger.w(TAG, "play, last play is empty");
                            // 没有上次播放内容，播放AI电台
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
                // 无网，如果上次播放的是内容是本地，则恢复
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

    // 校验音频完整性
    private boolean checkVerify(Audio audio) {
        if (audio.sourceUrl == null) {
            return false;
        }
        return audio.name != null;
    }

    /**
     * 播放本地
     */
    private void playLocalUnDirect(Operation operation) {
        playLocal(operation, false, false);
    }

    /**
     * 播放本地
     *
     * @param direct 间接触发播放本地，还是直接声控/手动触发播放本地
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
     * 播放本地音乐
     *
     * @param audio 从该音频开始播放
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
     * 打开音乐/播放音乐
     */
    public void playMusic(Operation operation) {
        Logger.d(TAG, "playMusic");
        // 当前有播放内容，且是音乐类型
        AudioV5 currAudio = PlayHelper.get().getCurrAudio();
        if (currAudio != null && AudioUtils.isSong(currAudio.sid)) {
            Logger.d(TAG, "playMusic continue play");
            AudioPlayer.getDefault().start();
            return;
        }
        AppLogic.runOnBackGround(() -> {
            PlayListData playListData = PlayListDataHelper.getPlayListData();
            // 检测网络状态
            if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                // 有网
                if (playListData == null || playListData.audioList == null || playListData.audioList.isEmpty() || !AudioUtils.isSong(playListData.audioList.get(0).sid)
                        || (PlayScene.ALBUM == playListData.scene && playListData.album == null)) {
                    // 拉取不到上次播单或者拉取到的歌单不是音乐
                    int historyCount = DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao().getCount();
                    Logger.d(TAG, "playMusic checkHistory count=" + historyCount);
                    if (historyCount > 0) {
                        // 本地有历史音乐
                        Logger.d(TAG, "playMusic playHistoryMusic");
                        PlayHelper.get().playHistoryMusic();
                    } else {
                        // 播放推荐
                        Logger.d(TAG, "playMusic playRecommend");
                        PlayHelper.get().playRecommend(operation);
                    }
                } else {
                    // 存在上次播放的歌单
                    Logger.d(TAG, "playMusic playLast");
                    PlayHelper.get().playListData(playListData, true);
                }
            } else {
                // 没网
                if (playListData == null
                        || PlayScene.LOCAL_MUSIC != playListData.scene
                        || playListData.audioList == null || playListData.audio == null) {
                    LocalAudioDao localAudioDao = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao();
                    int count = localAudioDao.getCount();
                    if (count == 0) {
                        Logger.d(TAG, "playMusic playLocal, local data is empty");
                        // 本地没有音乐，提示网络异常
                        if (Operation.SOUND == operation) {
                            TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_NET_OFFLINE", RS_VOICE_SPEAK_ASR_NET_OFFLINE, null);
                        } else {
                            ToastUtils.showShortOnUI(RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                        }
                    } else {
                        // 播放本地音乐
                        Logger.d(TAG, "playMusic playLocal");
                        PlayHelper.get().playLocalUnDirect(operation);
                    }
                } else {
                    // 从上次播放恢复
                    Logger.d(TAG, "playMusic, resume local scene from last play");
                    playListData(playListData, true);
                }
            }
        });
    }

    /**
     * 打开电台/播放电台
     */
    public void playRadio(Operation operation) {
        Logger.d(TAG, "playRadio");
        // 当前有播放内容，且不是音乐类型
        AudioV5 currAudio = PlayHelper.get().getCurrAudio();
        if (currAudio != null && !AudioUtils.isSong(currAudio.sid)) {
            Logger.d(TAG, "playRadio continue play");
            AudioPlayer.getDefault().start();
            return;
        }
        // 检测网络状态
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            // 有网
            AppLogic.runOnBackGround(() -> {
                Album album = null;
                // 从最近播放记录里面找
//                PlayListData playListData = PlayListDataHelper.getPlayListData();
//                if (playListData != null) {
//                    album = playListData.album;
//                }
                // 从历史电台里面找
                if (album == null) {
                    HistoryAlbumDao historyAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao();
                    List<HistoryAlbum> historyAlbumList = historyAlbumDao.listAllNotHidden();
                    if (historyAlbumList != null && !historyAlbumList.isEmpty()) {
                        album = historyAlbumList.get(historyAlbumList.size() - 1);
                    }
                }
                if (album == null) {
                    // 不存在历史播放电台，播放车主节目榜
                    playCarFm(operation);
                } else {
                    Logger.d(TAG, "playRadio play history album");
                    playAlbum(operation, PlayScene.HISTORY_ALBUM, album);
                }
            });
        } else {
            // 没网
            TtsHelper.speakResource("RS_VOICE_SPEAK_PLAY_NO_CONTENT", Constant.RS_VOICE_SPEAK_PLAY_NO_CONTENT);
            ToastUtils.showLongOnUI(Constant.RS_VOICE_SPEAK_PLAY_NO_CONTENT);
        }
    }


    /**
     * 播放榜单第一个音频
     */
    private void playCarFm(Operation operation) {
        Logger.d(TAG, "playRadio play carFm");

        Category category = new Category();
        category.categoryId = 8589934592L; // FIXME: 2019/3/14 固定ID
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
     * 播放AI电台
     */
    public void playAi(Operation operation) {
        playAi(operation, null);
    }

    /**
     * 播放AI电台，并把指定音频插入到队列头部，从该音频开始播放
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
        album.name = "AI电台";
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
     * 播放每日推荐
     */
    public void playRecommend(Operation operation) {
        Album album = new Album();
        album.id = 1000002;
        album.albumType = Album.ALBUM_TYPE_MUSIC;
        album.sid = 100;
        album.name = "每日推荐歌曲";
        album.logo = SharedPreferencesUtils.getRecommendLogoUrl();
        playAlbum(operation, PlayScene.ALBUM, album);
    }

    /**
     * 播放播放记录
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
     * 播放选择音频，搜索选择用
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

            // 虚构的播放列表
            setCurrAlbum(null);
            setQueue(PlayScene.ALBUM, AudioConverts.convert2List(queue, AudioConverts::convert2MediaAudio));
            PlayHelper.get().play(AudioConverts.convert2MediaAudio(audioV5));
            Logger.d(TAG, "playChoiceAudio with resp queue");
            return;
        }
        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            // 当前有音乐播放列表
            Audio audio = AudioConverts.convert2MediaAudio(audioV5);
            // 有网
            if (mCurrAudio != null && PlayScene.AI_RADIO != mCurrScene && (AudioUtils.isSong(mCurrAudio.sid) || PlayScene.WECHAT_PUSH == mCurrScene)) {
                AudioV5 currAudio = PlayHelper.get().getCurrAudio();
                if (currAudio != null) {
                    ProxyHelper.releaseProxyRequest(currAudio.sid, currAudio.id);
                    ReportEvent.reportAudioPlayEnd(currAudio,
                            PlayInfoEvent.MANUAL_TYPE_MANUAL,
                            AudioUtils.isLocalSong(currAudio.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE, PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(),
                            PlayInfoEvent.EXIT_TYPE_SOUND, false);
                }

                // 当前播放的是音乐列表，AI电台、电台专辑不算，微信推送算
                resetLastMusicPlayMode();
                audio.setExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE, true);
                AudioPlayer.getDefault().getQueue().remove(audio); // 去重
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
                // 当前播放的不是音乐
                // FIXME: 2019/4/13 已经处于AI电台下声控插入，不能清除当前内容
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

            // 无网
            AppLogic.runOnBackGround(() -> {
                if (AudioUtils.existsLocal(audioV5)) {
                    // 本身就是本地音频
                    Logger.d(TAG, "playChoiceAudio begin play local audio");
                    playLocal(audioV5);
                } else {
                    // FIXME: 2018/12/28 模糊匹配
                    LocalAudioDao localAudioDao = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao();
                    List<LocalAudio> audioList = localAudioDao.findBySql(LocalAudio.getSearchQuery(audioV5.name, null, audioV5.albumName));
                    if (audioList != null && !audioList.isEmpty()) {
                        Logger.d(TAG, "playChoiceAudio begin play local audio");
                        playLocal(audioList.get(0));
                    } else {
                        // 提示网络故障
                        TtsHelper.speakNetworkError();
                    }
                }
            });
        }
    }

    /**
     * 播放专辑
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
     * 播放某个专辑，搜索选择用
     */
    public void playChoiceAlbum(Album album) {
        Logger.d(TAG, "playChoiceAlbum album=" + album);
        playAlbum(Operation.SOUND, PlayScene.ALBUM, album);
    }

    /**
     * 播放订阅
     */
    public void playSubscribe() {
        Logger.d(TAG, "playSubscribe");
        AppLogic.runOnBackGround(() -> {
            SubscribeAlbumDao subscribeAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getSubscribeAlbumDao();
            SubscribeAlbum subscribeAlbum = subscribeAlbumDao.getFirst();
            if (subscribeAlbum == null) {
                // 不存在订阅节目
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_SUBSCRIBE_EMPTY", Constant.RS_VOICE_SPEAK_ASR_SUBSCRIBE_EMPTY);
            } else {
                playAlbum(Operation.SOUND, PlayScene.FAVOUR_ALBUM, subscribeAlbum);
            }
        });
    }

    /**
     * 播放收藏
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
                // 不存在收藏歌曲
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_FAVOUR_EMPTY", Constant.RS_VOICE_SPEAK_ASR_FAVOUR_EMPTY);
            } else {
                List<Audio> audioList = AudioConverts.convert2List(favourAudioList, AudioConverts::convert2MediaAudio);

                setQueue(PlayScene.FAVOUR_MUSIC, audioList);
                if (audioV5 == null) {
                    if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                        play(audioList.get((int) (Math.random() * audioList.size())));
                    } else {
                        // 优先播放已经缓存好的
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
     * 播放收藏
     */
    public void playFavour() {
        playFavour(null);
    }

    /**
     * 播放历史电台
     */
    public void playHistoryAlbum() {
        Logger.d(TAG, "playHistoryAlbum");
        AppLogic.runOnBackGround(() -> {
            HistoryAlbumDao historyAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao();
            List<HistoryAlbum> historyAlbumList = historyAlbumDao.listAllNotHidden();
            if (historyAlbumList.isEmpty()) {
                // 不存在订阅节目
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_SUBSCRIBE_EMPTY", Constant.RS_VOICE_SPEAK_ASR_SUBSCRIBE_EMPTY);
            } else {
                Collections.reverse(historyAlbumList);
                playAlbum(Operation.AUTO, PlayScene.HISTORY_ALBUM, historyAlbumList.get(0));
            }
        });
    }

    /**
     * 播放历史音乐
     */
    public void playHistoryMusic(AudioV5 audioV5) {
        Logger.d(TAG, "playHistoryMusic");
        AppLogic.runOnBackGround(() -> {
            HistoryAudioDao historyAudioDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao();
            List<HistoryAudio> historyAudioList = historyAudioDao.listAll();
            AudioUtils.removeLocalNotExists(historyAudioList);
            if (historyAudioList.isEmpty()) {
                // 不存在收藏歌曲
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_HISTORY_EMPTY", Constant.RS_VOICE_SPEAK_ASR_HISTORY_EMPTY);
            } else {
                List<Audio> audioList = AudioConverts.convert2List(historyAudioList, AudioConverts::convert2MediaAudio);
                Collections.reverse(audioList);
                setQueue(PlayScene.HISTORY_MUSIC, audioList);
                if (audioV5 == null) {
                    if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                        play(audioList.get(0));
                    } else {
                        // 优先播放已经缓存好的
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
     * 播放历史音乐
     */
    public void playHistoryMusic() {
        playHistoryMusic(null);
    }

    /**
     * 播放微信推送
     */
    public void playWxPush(AudioV5 audioV5) {
        Logger.d(TAG, "playWxPush");
        AppLogic.runOnBackGround(() -> {
            PushItemDao pushItemDao = DBUtils.getDatabase(GlobalContext.get()).getPushItemDao();
            List<PushItem> pushItemList = pushItemDao.listAll();
            if (pushItemList.isEmpty()) {
                // 不存在推送
                TtsHelper.speakResource("RS_VOICE_SPEAK_ASR_WX_EMPTY", Constant.RS_VOICE_SPEAK_ASR_WX_EMPTY);
            } else {
                List<Audio> audioList = AudioConverts.convert2List(pushItemList, AudioConverts::convert2MediaAudio);
                setQueue(PlayScene.WECHAT_PUSH, audioList);
                if (audioV5 == null) {
                    if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                        play(audioList.get((int) (Math.random() * audioList.size())));
                    } else {
                        // 优先播放已经缓存好的
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
     * 播放微信推送
     */
    public void playWxPush() {
        playWxPush(null);
    }

    /**
     * 移除指定本地音频，若删除音频为本地音乐(sid=0)，则级联删除历史记录
     * 若正在播放的音频被移除，则会触发下一首
     */
    public void removeLocalAudioCascade(List<? extends AudioV5> audioList, boolean containLocal) {
        // 收藏音乐、历史音乐、本地音乐场景会受到影响
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
                // 如果当前播放的歌曲被删除，下一首
                if (mCurrAudio != null && audioList.contains(mCurrAudio)) {
                    // 被删的是本地音频
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

            // 同步数据库，删除音乐(sid=0的播放记录)
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
     * 移除指定历史音频，如果当前是历史播放场景，则级联删除播放列表中相同音频
     * 若正在播放的音频被移除，则会触发下一首
     *
     * @deprecated 历史音乐删除时，播放列表不需要同步更新，该方法无调用方。
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
                // 如果当前播放的歌曲被删除，下一首
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
     * 移除不存在的本地音频
     * 若移除的音频在播放列表内，则触发下一首
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
            // 非文件丢失导致的异常
            ToastUtils.showShortOnUI(Constant.TIP_MEDIA_CAN_NOT_PLAY);
            if (AudioPlayer.getDefault().getQueue().getSize() > 1
                    || PlayScene.AI_RADIO == getCurrPlayScene()) { // AI电台场景，下一首不由播放列表决定，当前播放列表没有下一首也照样执行切换
                skipOp = operation;
                AppLogic.removeUiGroundCallback(mSkip2NextTask);
                AppLogic.runOnUiGround(mSkip2NextTask, 100);
            }
        } else {
            // 文件丢失导致的异常
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
     * 根据本地音乐列表同步当前歌单，只针对当前播放场景为本地音乐有效
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
     * 根据历史音乐同步当前歌单，只针对当前播放场景为历史音乐有效
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
     * 根据收藏音乐同步当前歌单，只针对当前播放场景为收藏音乐有效
     *
     * @deprecated 收藏列表不需要更新。
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
     * 刷新播放列表
     * 历史、收藏播放场景下，热插拔TF卡需要刷新播放列表，剔除本地不存在的音频
     */
    public void refreshQueueIfNotExists() {
        AppLogic.runOnBackGround(() -> {
            List<Audio> audioList = null;
            PlayScene scene = getCurrPlayScene();
            boolean bNext = false;
            if (PlayScene.FAVOUR_MUSIC == scene || PlayScene.HISTORY_MUSIC == scene || PlayScene.ALBUM == scene) {
                // 收藏音乐场景，重刷播放列表
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
     * 初始化当前播放内容和队列
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
     * 当前是否处于短暂焦点丢失状态
     */
    public boolean isPausedByTransientLossOfFocus() {
        return mPausedByTransientLossOfFocus;
    }

    List<LocalBuffer> lastBuffs;

    /**
     * 获取最后同步的缓冲状态
     */
    public List<LocalBuffer> getLastLocalBuffer() {
        return lastBuffs;
    }


    /**
     * 重播当前
     */
    public void replay() {
        Logger.d(TAG, "replay");
        AudioPlayer.getDefault().replay();
    }

    /**
     * 重播指定音频
     */
    public void replay(AudioV5 audio) {
        Logger.d(TAG, "replay, audio=" + audio);
        if (audio != null) {
            AudioPlayer.getDefault().replay(AudioConverts.convert2MediaAudio(audio));
        }
    }

    /**
     * 移除缓存文件并切换到下一首
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
