package com.txznet.music.store;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import com.txznet.audio.player.IMediaPlayer;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.PlayMode;
import com.txznet.music.data.entity.SubscribeAlbum;
import com.txznet.music.util.AlbumUtils;
import com.txznet.proxy.cache.LocalBuffer;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;

import java.util.List;
import java.util.Locale;

/**
 * 当前播放信息Store
 *
 * @author zackzhou
 * @date 2018/12/3,11:23
 */
public class PlayInfoStore extends Store {

    private MutableLiveData<AudioV5> mCurrPlaying = new MutableLiveData<>();
    private MutableLiveData<Long> mPosition = new MutableLiveData<>();
    private MutableLiveData<Long> mDuration = new MutableLiveData<>();
    private MutableLiveData<Integer> mPlayerState = new MutableLiveData<>();
    private Function<Long, String> mDateTran = val -> String.format(Locale.getDefault(), "%02d:%02d", (int) (val / 1000f / 60), (int) (val / 1000f % 60));
    private MutableLiveData<List<LocalBuffer>> mBuffer = new MutableLiveData<>();
    private MutableLiveData<Album> mAlbum = new MutableLiveData<>();
    private MutableLiveData<PlayMode> mPlayMode = new MutableLiveData<>();

    private LiveData<Boolean> isPlaying = Transformations.map(mPlayerState, val -> {
        if (val == null) {
            return false;
        } else {
            return IMediaPlayer.STATE_ON_PLAYING == val
                    || IMediaPlayer.STATE_ON_PREPARING == val
                    || IMediaPlayer.STATE_ON_PREPARED == val
                    || IMediaPlayer.STATE_ON_BUFFERING == val;
        }
    });

    private LiveData<Boolean> isBuffering = Transformations.map(mPlayerState, val -> {
        if (val == null) {
            return false;
        } else {
            return IMediaPlayer.STATE_ON_PREPARING == val
                    || IMediaPlayer.STATE_ON_BUFFERING == val;
        }
    });

    private LiveData<Boolean> isPlayingStrict = Transformations.map(mPlayerState, val -> {
        if (val == null) {
            return false;
        } else {
            return IMediaPlayer.STATE_ON_PLAYING == val;
        }
    });

    public LiveData<AudioV5> getCurrPlaying() {
        return mCurrPlaying;
    }

    public LiveData<Long> getPosition() {
        return mPosition;
    }

    public LiveData<Long> getDuration() {
        return mDuration;
    }

    private LiveData<String> mPositionFormatted = Transformations.map(mPosition, mDateTran);

    public LiveData<String> getPositionFormatted() {
        return mPositionFormatted;
    }

    private LiveData<String> mDurationFormatted = Transformations.map(mDuration, mDateTran);

    public LiveData<String> getDurationFormatted() {
        return mDurationFormatted;
    }

    public LiveData<Integer> getPlayerState() {
        return mPlayerState;
    }

    public LiveData<Boolean> isPlaying() {
        return isPlaying;
    }

    public LiveData<Boolean> isBuffering() {
        return isBuffering;
    }

    public LiveData<Boolean> isPlayingStrict() {
        return isPlayingStrict;
    }

    public LiveData<List<LocalBuffer>> getBuffer() {
        return mBuffer;
    }

    public LiveData<Album> getAlbum() {
        return mAlbum;
    }

    public LiveData<PlayMode> getPlayMode() {
        return mPlayMode;
    }

    @Override
    protected void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_PLAYER_ON_PROGRESS_CHANGE:
                long duration = (long) action.data.get(Constant.PlayConstant.KEY_DURATION);
                long position = (long) action.data.get(Constant.PlayConstant.KEY_POSITION);
                if (position > duration) {
                    position = duration;
                }
                mDuration.setValue(duration);
                mPosition.setValue(position);
                break;
            case ActionType.ACTION_PLAYER_ON_INFO_CHANGE:
                AudioV5 audio = (AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO);
                mCurrPlaying.setValue(audio);
                break;
            case ActionType.ACTION_PLAYER_ON_STATE_CHANGE:
                mPlayerState.setValue((Integer) action.data.get(Constant.PlayConstant.KEY_PLAY_STATE));
                break;
            case ActionType.ACTION_PROXY_BUFFERING_UPDATE:
                if (BuildConfig.DEBUG) {
                    Log.d(Constant.LOG_TAG_PROXY, "buff=" + action.data.get(Constant.PlayConstant.KEY_PLAY_BUFFER));
                }
                mBuffer.setValue((List<LocalBuffer>) action.data.get(Constant.PlayConstant.KEY_PLAY_BUFFER));
                break;
            case ActionType.ACTION_PLAYER_ON_ALBUM_CHANGE:
                mAlbum.setValue((Album) action.data.get(Constant.PlayConstant.KEY_ALBUM));
                break;
            case ActionType.ACTION_PLAY_MODE_CHANGED:
                mPlayMode.setValue((PlayMode) action.data.get(Constant.PlayConstant.KEY_PLAY_MODE));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onData(RxAction action) {
        AudioV5 mCurrPlay;
        Album album;
        switch (action.type) {
            case ActionType.ACTION_FAVOUR_EVENT_FAVOUR:
                mCurrPlay = mCurrPlaying.getValue();
                FavourAudio favour = (FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO);
                if (mCurrPlay != null && mCurrPlay.sid == favour.sid && mCurrPlay.id == favour.id) {
                    mCurrPlay.isFavour = true;
                    mCurrPlaying.setValue(mCurrPlay);
                }
                break;
            case ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR:
                mCurrPlay = mCurrPlaying.getValue();
                FavourAudio unFavour = (FavourAudio) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO);
                if (mCurrPlay != null && mCurrPlay.sid == unFavour.sid && mCurrPlay.id == unFavour.id) {
                    mCurrPlay.isFavour = false;
                    mCurrPlaying.setValue(mCurrPlay);
                }
                break;
            case ActionType.ACTION_SUBSCRIBE_EVENT_SUBSCRIBE:
                album = mAlbum.getValue();
                SubscribeAlbum subscribeAlbum = (SubscribeAlbum) action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM);
                if (album != null && album.sid == subscribeAlbum.sid && album.id == subscribeAlbum.id) {
                    album.isSubscribe = true;
                    mAlbum.setValue(album);
                }
                if (AlbumUtils.isAiRadio(album)) {
                    mCurrPlay = mCurrPlaying.getValue();
                    if (mCurrPlay != null && mCurrPlay.albumSid == subscribeAlbum.sid && mCurrPlay.albumId == subscribeAlbum.id) {
                        album.isSubscribe = true;
                    }
                    mAlbum.setValue(album);
                }
                break;
            case ActionType.ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE:
                album = mAlbum.getValue();
                SubscribeAlbum unsubscribeAlbum = (SubscribeAlbum) action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM);
                if (album != null && album.sid == unsubscribeAlbum.sid && album.id == unsubscribeAlbum.id) {
                    album.isSubscribe = false;
                    mAlbum.setValue(album);
                }
                if (AlbumUtils.isAiRadio(album)) {
                    mCurrPlay = mCurrPlaying.getValue();
                    if (mCurrPlay != null && mCurrPlay.albumSid == unsubscribeAlbum.sid && mCurrPlay.albumId == unsubscribeAlbum.id) {
                        album.isSubscribe = false;
                    }
                    mAlbum.setValue(album);
                }
                break;
            case ActionType.ACTION_PLAYER_GET_PLAY_INFO:
                mAlbum.setValue((Album) action.data.get(Constant.PlayConstant.KEY_ALBUM));
                mCurrPlaying.setValue((AudioV5) action.data.get(Constant.PlayConstant.KEY_AUDIO));
                mDuration.setValue((Long) action.data.get(Constant.PlayConstant.KEY_DURATION));
                mPosition.setValue((Long) action.data.get(Constant.PlayConstant.KEY_POSITION));
                mPlayerState.setValue((Integer) action.data.get(Constant.PlayConstant.KEY_PLAY_STATE));
                mBuffer.setValue((List<LocalBuffer>) action.data.get(Constant.PlayConstant.KEY_PLAY_BUFFER));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {

    }

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_PLAYER_ON_PROGRESS_CHANGE,
                ActionType.ACTION_PLAYER_ON_INFO_CHANGE,
                ActionType.ACTION_PLAYER_ON_STATE_CHANGE,
                ActionType.ACTION_FAVOUR_EVENT_FAVOUR,
                ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR,
                ActionType.ACTION_PROXY_BUFFERING_UPDATE,
                ActionType.ACTION_PLAYER_ON_ALBUM_CHANGE,
                ActionType.ACTION_PLAYER_GET_PLAY_INFO,
                ActionType.ACTION_SUBSCRIBE_EVENT_SUBSCRIBE,
                ActionType.ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE,
                ActionType.ACTION_PLAY_MODE_CHANGED
        };
    }
}
