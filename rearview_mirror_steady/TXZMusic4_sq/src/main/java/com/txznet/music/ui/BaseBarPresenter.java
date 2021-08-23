package com.txznet.music.ui;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.playerModule.logic.IPlayListChangedListener;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.PlayerInfoUpdateListener;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2017/12/22.
 */

public class BaseBarPresenter implements BaseBarContract.Presenter, IPlayListChangedListener, PlayerInfoUpdateListener, Observer {
    private static final String TAG = "BaseBarPresenter:";

    private BaseBarContract.View mView;
    private CompositeDisposable mCompositeDisposable;

    public BaseBarPresenter(BaseBarContract.View view) {
        mView = view;
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.clear();
    }

    @Override
    public void register() {
        PlayInfoManager.getInstance().addPlayListChangedListener(this);
        PlayInfoManager.getInstance().addPlayerInfoUpdateListener(this);

        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        ObserverManage.getObserver().addObserver(this);

        onPlayerModeUpdated(PlayInfoManager.getInstance().getCurrentPlayMode());
        onPlayerStatusUpdated(PlayInfoManager.getInstance().getCurrentPlayerUIStatus());

        long duration = PlayInfoManager.getInstance().getDuration();
        long progress = PlayInfoManager.getInstance().getProgress();

        onProgressUpdated(progress, duration);

        if (null != currentAudio) {
            onPlayInfoUpdated(currentAudio, currentAlbum);
        }
    }

    @Override
    public void unregister() {
        PlayInfoManager.getInstance().removePlayListChangedListener(this);
        PlayInfoManager.getInstance().removePlayerInfoUpdateListener(this);

        ObserverManage.getObserver().deleteObserver(this);


        mCompositeDisposable.clear();
        mView = null;
    }

    @Override
    public void playNext() {
        PlayEngineFactory.getEngine().next(EnumState.Operation.manual);
    }

    @Override
    public void playPrev() {
        PlayEngineFactory.getEngine().last(EnumState.Operation.manual);
    }

    @Override
    public void playOrPause() {
        PlayEngineFactory.getEngine().playOrPause(EnumState.Operation.manual);
    }

    @Override
    public void switchMode() {
        PlayEngineFactory.getEngine().changeMode(EnumState.Operation.manual);
    }

    @Override
    public void seek(long position) {
        PlayEngineFactory.getEngine().seekTo(EnumState.Operation.manual, position);
    }

    @Override
    public void favor(boolean isCancel) {
        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (null == currentAudio) {
            return;
        }

        if (isCancel) {
            FavorHelper.unfavor(currentAudio,EnumState.Operation.manual);
        } else {
            FavorHelper.favor(currentAudio,EnumState.Operation.manual);
        }
    }

    @Override
    public void subscribe(boolean isCancel) {
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        if (null == currentAlbum) {
            return;
        }

        if (isCancel) {
            FavorHelper.unSubscribeRadio(currentAlbum,EnumState.Operation.manual);
        } else {
            FavorHelper.subscribeRadio(currentAlbum,EnumState.Operation.manual);
        }
    }

    @Override
    public void onPlayListChanged(List<Audio> audios) {
        if (null == mView) {
            return;
        }
        if (null == audios || audios.isEmpty()) {
            mView.setPlayListEnable(false);
        } else {
            mView.setPlayListEnable(true);
        }
    }

    @Override
    public void onPlayInfoUpdated(Audio audio, Album album) {
        if (null == audio) {
            return;
        }
        mView.onPlayInfoUpdated(audio, album);
        mView.onFavorVisibilityChanged(false);

        Disposable audioDisposable = io.reactivex.Observable.just(audio)
                .subscribeOn(Schedulers.io())
                .filter(new Predicate<Audio>() {
                    @Override
                    public boolean test(Audio audio) throws Exception {
                        return FavorHelper.isSupportFavour(audio);
                    }
                })
                .map(new Function<Audio, Boolean>() {
                    @Override
                    public Boolean apply(Audio audio) throws Exception {
                        boolean favour = FavorHelper.isFavour(audio);
                        Logger.i(TAG, "%s favour %s", audio.getName(), favour);
                        return favour;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        mView.onFavorVisibilityChanged(true);
                        mView.onFavorStatusChanged(aBoolean, true);
                    }
                });

        mCompositeDisposable.add(audioDisposable);


        if (album != null) {
            Disposable albumDisposable = io.reactivex.Observable.just(album)
                    .subscribeOn(Schedulers.io())
                    .filter(new Predicate<Album>() {
                        @Override
                        public boolean test(Album album) throws Exception {
                            return FavorHelper.isSupportSubscribe(album);
                        }
                    })
                    .map(new Function<Album, Boolean>() {
                        @Override
                        public Boolean apply(Album album) throws Exception {
                            boolean subscribe = FavorHelper.isSubscribe(album);
                            Logger.i(TAG, "%s subscribe %s", album.getName(), subscribe);
                            return subscribe;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean aBoolean) throws Exception {
                            mView.onFavorVisibilityChanged(true);
                            mView.onSubscribeStatusChanged(aBoolean, true);
                        }
                    });
            mCompositeDisposable.add(albumDisposable);
        }

    }

    @Override
    public void onProgressUpdated(long position, long duration) {
        mView.onProgressUpdated(position, duration);
    }

    @Override
    public void onPlayerModeUpdated(int mode) {
        mView.onPlayerModeUpdated(mode);
    }

    @Override
    public void onPlayerStatusUpdated(int status) {
        mView.onPlayerStatusUpdated(status);
    }

    @Override
    public void onBufferProgressUpdated(List<LocalBuffer> buffers) {
        mView.onBufferProgressUpdated(buffers);
    }

    @Override
    public void showPlayList() {
        mView.showPlayList(true);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            switch (info.getType()) {
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_NET:
                    mView.showTips(Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS);
                    break;

                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_UNKNOWN:
                    mView.showTips(Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN);
                    break;

                case InfoMessage.FAVOUR_MUSIC:
                    if (info.getObj() instanceof Audio) {
                        Audio bean = (Audio) info.getObj();
                        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                        if (null != currentAudio && bean.getId() == currentAudio.getId()
                                && bean.getSid() == currentAudio.getSid()) {
                            mView.onFavorStatusChanged(true, true);
                        }
                    }

                    break;
                case InfoMessage.UNFAVOUR_MUSIC:
                    if (info.getObj() instanceof Audio) {
                        Audio bean = (Audio) info.getObj();
                        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                        if (null != currentAudio && bean.getId() == currentAudio.getId()
                                && bean.getSid() == currentAudio.getSid()) {
                            mView.onFavorStatusChanged(false, true);
                        }
                    }
                    break;
                case InfoMessage.SUBSCRIBE_RADIO:
                    if (info.getObj() instanceof Album) {
                        Album bean = (Album) info.getObj();
                        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                        if (null != currentAlbum && bean.getId() == currentAlbum.getId()
                                && bean.getSid() == currentAlbum.getSid()) {
                            mView.onSubscribeStatusChanged(true, true);
                        }
                    }
                    break;
                case InfoMessage.UNSUBSCRIBE_RADIO:
                    if (info.getObj() instanceof Album) {
                        Album bean = (Album) info.getObj();
                        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                        if (null != currentAlbum && bean.getId() == currentAlbum.getId()
                                && bean.getSid() == currentAlbum.getSid()) {
                            mView.onSubscribeStatusChanged(false, true);
                        }
                    }
                    break;

                default:

                    break;
            }
        }
    }
}
