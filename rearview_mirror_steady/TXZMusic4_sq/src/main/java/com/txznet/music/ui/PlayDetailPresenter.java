package com.txznet.music.ui;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.favor.FavorHelper;
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
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2017/12/8.
 */

public class PlayDetailPresenter implements PlayInfoContract.Presenter, PlayerInfoUpdateListener, Observer {

    private static final String TAG = "PlayDetailPresenter:";
    private PlayInfoContract.View mView;
    private CompositeDisposable mCompositeDisposable;

    public PlayDetailPresenter(PlayInfoContract.View view) {
        this.mView = view;
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.clear();
    }


    @Override
    public void register() {
        PlayInfoManager.getInstance().addPlayerInfoUpdateListener(this);

        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();


        onPlayerModeUpdated(PlayInfoManager.getInstance().getCurrentPlayMode());
        onPlayerStatusUpdated(PlayInfoManager.getInstance().getCurrentPlayerUIStatus());

        long duration = PlayInfoManager.getInstance().getDuration();
        long progress = PlayInfoManager.getInstance().getProgress();

        onProgressUpdated(progress, duration);

        if (null != currentAudio) {
            onPlayInfoUpdated(currentAudio, currentAlbum);
        }

        ObserverManage.getObserver().addObserver(this);
    }

    @Override
    public void unregister() {
        PlayInfoManager.getInstance().removePlayerInfoUpdateListener(this);
        ObserverManage.getObserver().deleteObserver(this);

        mCompositeDisposable.clear();
        this.mView = null;
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
    public void onPlayInfoUpdated(Audio audio, Album album) {
        if (null == audio) {
            return;
        }

        mView.onPlayInfoUpdated(audio, album);

        Disposable disposable = io.reactivex.Observable.just(audio)
                .groupBy(new Function<Audio, Boolean>() {
                    @Override
                    public Boolean apply(Audio audio) throws Exception {
                        return FavorHelper.isSupportFavour(audio);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<GroupedObservable<Boolean, Audio>>() {
                    @Override
                    public void accept(GroupedObservable<Boolean, Audio> booleanAudioGroupedObservable) throws Exception {
                        Boolean key = booleanAudioGroupedObservable.getKey();
                        if (!key) {
                            mView.onFavorVisibilityChanged(false);
                        } else {
                            Disposable disposable = booleanAudioGroupedObservable.map(new Function<Audio, Boolean>() {
                                @Override
                                public Boolean apply(Audio audio) throws Exception {
                                    return FavorHelper.isFavour(audio);
                                }
                            })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<Boolean>() {
                                        @Override
                                        public void accept(Boolean aBoolean) throws Exception {
                                            mView.onFavorVisibilityChanged(true);
                                            mView.onFavorStatusChanged(aBoolean, true);
                                        }
                                    });
                            mCompositeDisposable.add(disposable);
                        }
                    }
                });

        mCompositeDisposable.add(disposable);

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
    public void update(Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            switch (info.getType()) {
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
                default:

                    break;
            }
        }
    }
}
