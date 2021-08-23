package com.txznet.music.historyModule.ui;

import com.txznet.comm.remote.util.Logger;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2018/1/17.
 */

public class HistoryPresenter implements HistoryContract.Presenter, Observer {

    private static final String TAG = "HistoryPresenter";
    private HistoryContract.View mView;
    private HistoryContract.DataSource mDataSource;
    private CompositeDisposable mCompositeDisposable;
    private int mType;

    public HistoryPresenter(HistoryContract.View view, int type) {
        mView = view;
        mDataSource = HistoryDataSource.getInstance();
        mCompositeDisposable = new CompositeDisposable();
        mType = type;
    }

    @Override
    public void register() {
        ObserverManage.getObserver().addObserver(this);
        requestHistory();
    }

    @Override
    public void unregister() {
        mCompositeDisposable.clear();
        ObserverManage.getObserver().deleteObserver(this);
        mView = null;
    }

    @Override
    public void play(List<HistoryData> historyData, int index) {
        HistoryData data = historyData.get(index);
        if (data.getType() == HistoryData.TYPE_AUDIO) {
            ReportEvent.clickHistoryMusicPlay(data.getAudio().getSid(), data.getAudio().getId(), data.getAudio().getName());
            List<Audio> audios = new ArrayList<>();
            for (HistoryData d : historyData) {
                audios.add(d.getAudio());
            }
            PlayEngineFactory.getEngine().setAudios(EnumState.Operation.manual, audios, null, index, PlayInfoManager.DATA_HISTORY);
            PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
        } else {
            Album album = data.getAlbum();
            ReportEvent.clickHistoryRadioPlay(album.getSid(), album.getId(), album.getName());
            AlbumEngine.getInstance().playAlbum(EnumState.Operation.manual, album, album.getCategoryId(), null);
        }
    }

    @Override
    public void favor(HistoryData historyData) {
        if (historyData.getType() == HistoryData.TYPE_AUDIO) {
            Audio audio = historyData.getAudio();
            if (FavorHelper.isFavour(audio)) {
                FavorHelper.unfavor(audio,EnumState.Operation.manual);
                ReportEvent.clickHistoryMusicUnfavour(audio.getSid(), audio.getId(), audio.getName());
            } else {
                FavorHelper.favor(audio,EnumState.Operation.manual);
                ReportEvent.clickHistoryMusicFavour(audio.getSid(), audio.getId(), audio.getName());
            }
        } else {
            Album album = historyData.getAlbum();
            if (FavorHelper.isSubscribe(album)) {
                FavorHelper.unSubscribeRadio(album,EnumState.Operation.manual);
                ReportEvent.clickHistoryRadioUnSubscribe(album.getSid(), album.getId(), album.getName());
            } else {
                FavorHelper.subscribeRadio(album,EnumState.Operation.manual);
                ReportEvent.clickHistoryRadioSubscribe(album.getSid(), album.getId(), album.getName());
            }
        }
    }

    @Override
    public void delete(HistoryData historyData) {
        if (historyData == null) {
            return;
        }

        if(historyData.getType() == HistoryData.TYPE_AUDIO){
            ReportEvent.clickHistoryMusicDelete(historyData.getAudio().getSid(), historyData.getAudio().getId(), historyData.getAudio().getName());
        }else{
            ReportEvent.clickHistoryRadioDelete(historyData.getAlbum().getSid(), historyData.getAlbum().getId(), historyData.getAlbum().getName());
        }

        List<HistoryData> data;
        if (mType == HistoryContract.TYPE_MUSIC) {
            data = mDataSource.getMusicHistory();
        } else {
            data = mDataSource.getAlbumHistory();
        }

        int index = data.indexOf(historyData);
        if (index >= 0) {
            mDataSource.deleteHistory(historyData);

            if (data.isEmpty()) {
                mView.showEmpty();
            } else {
                mView.refreshItem(-1);
            }

        } else {
            Logger.e(TAG, "delete history error:not contain:" + historyData.getAudio().getName());
        }
    }


    @Override
    public void requestHistory() {
        Logger.d(TAG, "request history");
        Observable<List<HistoryData>> listObservable;
        if (mType == HistoryContract.TYPE_MUSIC) {
            listObservable = mDataSource.requestMusicHistory();
        } else {
            listObservable = mDataSource.requestAlbumHistory();
        }
        Disposable disposable = listObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<HistoryData>>() {
                    @Override
                    public void accept(List<HistoryData> historyData) throws Exception {
                        Logger.i(TAG, "request history:" + historyData.size());
                        if (historyData.isEmpty()) {
                            mView.showEmpty();
                        } else {
                            mView.hideLoading();
                            mView.showHistory(historyData);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Logger.e(TAG, "request history:" + throwable.toString());
                    }
                });
        mCompositeDisposable.add(disposable);
    }


    @Override
    public void update(java.util.Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            switch (info.getType()) {

                case InfoMessage.FAVOUR_MUSIC:
                    if (info.getObj() instanceof Audio) {
                        Audio bean = (Audio) info.getObj();
                        refreshItem(bean.getId(), bean.getSid());

                    }

                    break;
                case InfoMessage.UNFAVOUR_MUSIC:
                    if (info.getObj() instanceof Audio) {
                        Audio bean = (Audio) info.getObj();
                        refreshItem(bean.getId(), bean.getSid());
                    }
                    break;
                case InfoMessage.SUBSCRIBE_RADIO:
                    if (info.getObj() instanceof Album) {
                        Album bean = (Album) info.getObj();
                        refreshItem(bean.getId(), bean.getSid());
                    }
                    break;
                case InfoMessage.UNSUBSCRIBE_RADIO:
                    if (info.getObj() instanceof Album) {
                        Album bean = (Album) info.getObj();
                        refreshItem(bean.getId(), bean.getSid());
                    }
                    break;

                case InfoMessage.PLAYER_CURRENT_AUDIO:
                    if (mType == HistoryContract.TYPE_MUSIC) {
                        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                        List<HistoryData> musicHistory = mDataSource.getMusicHistory();

                        for (int i = 0; i < musicHistory.size(); i++) {
                            HistoryData historyData = musicHistory.get(i);
                            if (historyData.getId() == currentAudio.getId() && historyData.getSid() == currentAudio.getSid()) {
                                mView.refreshItem(-1);
                                return;
                            }
                        }
                    } else {
                        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                        if (currentAlbum != null) {
                            List<HistoryData> albumHistory = mDataSource.getAlbumHistory();
                            for (int i = 0; i < albumHistory.size(); i++) {
                                HistoryData historyData = albumHistory.get(i);
                                if (historyData.getId() == currentAlbum.getId() && historyData.getSid() == currentAlbum.getSid()) {
                                    mView.refreshItem(-1);
                                    return;
                                }
                            }
                        }
                    }


                    break;
                case InfoMessage.REQUERY_HISTORY_MUSIC_LIST:
                    requestHistory();
                    break;

                default:

                    break;
            }
        }
    }


    private void refreshItem(long id, int sid) {
        List<HistoryData> historyData;
        if (mType == HistoryContract.TYPE_MUSIC) {
            historyData = mDataSource.getMusicHistory();
        } else {
            historyData = mDataSource.getAlbumHistory();
        }

        for (int i = 0; i < historyData.size(); i++) {
            HistoryData data = historyData.get(i);
            if (data.getId() == id && data.getSid() == sid) {
                mView.refreshItem(i);
                return;
            }
        }
    }

}
