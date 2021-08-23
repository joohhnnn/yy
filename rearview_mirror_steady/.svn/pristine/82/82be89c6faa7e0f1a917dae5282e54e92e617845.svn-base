package com.txznet.music.action;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.entity.PushItem;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

import java.util.List;

/**
 * 播放器操作类
 *
 * @author zackzhou
 * @date 2018/12/3,14:44
 */
public class PlayerActionCreator {
    public static final String TAG = Constant.LOG_TAG_FLUX;
    private static PlayerActionCreator sInstance = new PlayerActionCreator();

    private PlayerActionCreator() {
    }

    public static PlayerActionCreator get() {
        return sInstance;
    }

    /**
     * 获取播放状态
     */
    public void getPlayInfo(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_GET_PLAY_INFO).operation(operation).build());
    }

    /**
     * 播放本地音乐
     */
    public void playLocal(Operation operation, List<LocalAudio> audioList, int position) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_LOCAL).operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO_LIST, audioList)
                .bundle(Constant.PlayConstant.KEY_POSITION, position)
                .build());
    }

    /**
     * 播放本地音乐，从指定音频开始
     */
    public void playLocal(Operation operation, AudioV5 audio) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_LOCAL).operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO, audio)
                .build());
    }

    /**
     * 随机播放一首本地音乐
     */
    public void playLocal(Operation operation) {
        playLocal(operation, null, -1);
    }

    /**
     * 播放最后一次播放的本地音乐，若无则随机播放
     */
    public void playLocalWithLastPlay(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_LOCAL).operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO_LIST, null)
                .bundle(Constant.PlayConstant.KEY_POSITION, -1)
                .bundle(Constant.PlayConstant.KEY_RESUME_LAST_PLAY, true)
                .build());
    }

    /**
     * 播放微信推送
     */
    public void playWxPush(Operation operation, List<PushItem> audioList, int position) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_WX_PUSH).operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO_LIST, audioList)
                .bundle(Constant.PlayConstant.KEY_POSITION, position)
                .build());
    }

    /**
     * 播放微信推送，从指定音频开始
     */
    public void playWxPush(Operation operation, AudioV5 audio) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_WX_PUSH).operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO, audio)
                .build());
    }

    /**
     * 播放收藏音乐
     */
    public void playFavour(Operation operation, List<FavourAudio> audioList, int position) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_FAVOUR).operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO_LIST, audioList)
                .bundle(Constant.PlayConstant.KEY_POSITION, position)
                .build());
    }

    /**
     * 播放收藏音乐
     */
    public void playFavour(Operation operation) {
        playFavour(operation, null, -1);
    }

    /**
     * 播放收藏音乐，从指定音频开始
     */
    public void playFavour(Operation operation, AudioV5 favourAudio) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_FAVOUR).operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO, favourAudio)
                .build());
    }

    /**
     * 播放订阅专辑
     */
    public void playSubscribe(Operation operation, Album album) {
        playAlbum(operation, album, PlayScene.FAVOUR_ALBUM);
    }

    /**
     * 播放订阅专辑
     */
    public void playSubscribe(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_SUBSCRIBE).operation(operation).build());
    }

    /**
     * 播放历史音乐
     */
    public void playHistoryMusic(Operation operation) {
        playHistoryMusic(operation, null, -1);
    }

    /**
     * 播放历史音乐
     */
    public void playHistoryMusic(Operation operation, List<HistoryAudio> audioList, int position) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_HISTORY_MUSIC).operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO_LIST, audioList)
                .bundle(Constant.PlayConstant.KEY_POSITION, position)
                .build());
    }

    /**
     * 播放历史也音乐，从指定音频开始
     */
    public void playHistoryMusic(Operation operation, AudioV5 historyAudio) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_HISTORY_MUSIC).operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO, historyAudio)
                .build());
    }

    /**
     * 播放历史专辑
     */
    public void playHistoryAlbum(Operation operation, Album album) {
        playAlbum(operation, album, PlayScene.HISTORY_ALBUM);
    }

    /**
     * 播放历史专辑
     */
    @Deprecated
    public void playHistoryAlbum(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_HISTORY_ALBUM).operation(operation).build());
    }

    /**
     * 播放AI电台
     */
    public void playAi(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_AI).operation(operation).build());
    }

    /**
     * 播放专辑
     */
    public void playAlbum(Operation operation, @NonNull Album album) {
        playAlbum(operation, album, PlayScene.ALBUM);
    }

    /**
     * 播放专辑
     */
    private void playAlbum(Operation operation, @NonNull Album album, @Nullable PlayScene scene) {
        RxAction.Builder builder = RxAction.type(ActionType.ACTION_PLAYER_PLAY_ALBUM).operation(operation)
                .bundle(Constant.PlayConstant.KEY_SCENE, scene)
                .bundle(Constant.PlayConstant.KEY_ALBUM, album);
        Dispatcher.get().postAction(builder.build());
    }

    /**
     * 播放指定音频
     */
    public void play(Operation operation, AudioV5 audioV5) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_ITEM)
                .operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO, audioV5).build());
    }

    /**
     * 插入到正在播放的音频的下一首，采用{@See PreemptType.PREEMPT_TYPE_IMMEDIATELY}到播放指定音频
     */
    public void playImmediately(Operation operation, AudioV5 audioV5) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_ITEM)
                .operation(operation)
                .bundle(Constant.PlayConstant.KEY_AUDIO, audioV5).bundle(Constant.PlayConstant.KEY_MODE_IMMEDIATELY, true).build());
    }

    /**
     * 播放
     */
    public void play(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY)
                .operation(operation).build());
    }

    /**
     * 播放音乐
     */
    public void playMusic(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_MUSIC)
                .operation(operation).build());
    }

    /**
     * 播放电台
     */
    public void playRadio(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_RADIO).operation(operation).build());
    }

    /**
     * 开始播放
     */
    public void start(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_START).operation(operation).build());
    }

    /**
     * 暂停
     */
    public void pause(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PAUSE)
                .operation(operation).build());
    }

    /**
     * 停止
     */
    public void stop(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_STOP)
                .operation(operation).build());
    }

    /**
     * 跳转
     */
    public void seekTo(Operation operation, long position) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_SEEK_TO)
                .operation(operation)
                .bundle(Constant.PlayConstant.KEY_POSITION, position).build());
    }

    /**
     * 跳转百分比处
     */
    public void seekTo(Operation operation, float percent) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_SEEK_TO)
                .operation(operation)
                .bundle(Constant.PlayConstant.KEY_PERCENT, percent).build());
    }

    /**
     * 播放/暂停
     */
    public void playOrPause(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_OR_PAUSE)
                .operation(operation).build());
    }

    /**
     * 上一首
     */
    public void prev(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_PREV)
                .operation(operation).build());
    }

    /**
     * 下一首
     */
    public void next(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_PLAY_NEXT)
                .operation(operation).build());
    }

    /**
     * 设置音量
     */
    public void setVol(Operation operation, float leftVol, float rightVol) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PLAYER_SET_VOL)
                .operation(operation)
                .bundle(Constant.PlayConstant.KEY_LEFT_VOLUME, leftVol)
                .bundle(Constant.PlayConstant.KEY_RIGHT_VOLUME, rightVol)
                .build());
    }
}
