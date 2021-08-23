package com.txznet.music.playerModule;

import android.support.v7.util.DiffUtil;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.playerModule.logic.IPlayListChangedListener;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.ui.PlayListContract;
import com.txznet.music.report.ReportEvent;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observables.GroupedObservable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2017/12/19.
 */

public class PlayListPresenter implements PlayListContract.Presenter, Observer, IPlayListChangedListener, PlayListItemDataSource.PlayListItemChangedListener {

    private static final String TAG = "PlayListPresenter:";
    private PlayListContract.View mView;
    private List<PlayListItem> mPlayListItems;

    private CompositeDisposable mCompositeDisposable;

    public PlayListPresenter(PlayListContract.View view) {
        LogUtil.logd("music:playlist:set:view," + view.hashCode());
        this.mView = view;
        mCompositeDisposable = new CompositeDisposable();
    }


    @Override
    public void register() {
        ObserverManage.getObserver().addObserver(this);
        PlayInfoManager.getInstance().addPlayListChangedListener(this);
        PlayListItemDataSource.getInstance().addOnPlayListItemChangedListener(this);
    }

    @Override
    public void unregister() {
        LogUtil.logd("music:playlist:clear:view," + mView.hashCode());
        ObserverManage.getObserver().deleteObserver(this);
        PlayInfoManager.getInstance().removePlayListChangedListener(this);
        PlayListItemDataSource.getInstance().removeOnPlayListItemChangedListener(this);

        mView = null;
        mCompositeDisposable.clear();
    }

    @Override
    public void favor(Audio audio, boolean isCancel) {
        if (isCancel) {
            FavorHelper.unfavor(audio, EnumState.Operation.manual);
            ReportEvent.clickPlayerPageMusicFavour(PlayInfoManager.getInstance().getCurrentAudio());
        } else {
            FavorHelper.favor(audio, EnumState.Operation.manual);
            ReportEvent.clickPlayerPageMusicUnFavour(PlayInfoManager.getInstance().getCurrentAudio());
        }
    }

    @Override
    public void subscribe(Album album, boolean isCancel) {
        if (isCancel) {
            FavorHelper.unSubscribeRadio(album, EnumState.Operation.manual);
        } else {
            FavorHelper.subscribeRadio(album, EnumState.Operation.manual);
        }
    }

    @Override
    public void play(Audio audio) {
        ReportEvent.reportClickThemePlayList();
        PlayEngineFactory.getEngine().playAudio(EnumState.Operation.manual, audio);
    }

    @Override
    public void refresh() {
        PlayEngineFactory.getEngine().searchListData(EnumState.Operation.manual, false);
    }

    @Override
    public void loadMore() {
        PlayEngineFactory.getEngine().searchListData(EnumState.Operation.manual, true);
    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            switch (info.getType()) {
                case InfoMessage.REQUEST_AUDIO_RESPONSE:
                    mView.hideRefreshOrLoadMore();
                    break;
                case InfoMessage.SUBSCRIBE_RADIO:
                    if (info.getObj() instanceof Album) {
                        Album bean = (Album) info.getObj();
                        Album currentAlbum = PlayInfoManager.getInstance().getSubscribeAlbum();
                        if (bean.getId() == currentAlbum.getId()
                                && bean.getSid() == currentAlbum.getSid()) {
                            mView.setSubscribeStatus(true, true);
                        }
                    }
                    break;
                case InfoMessage.UNSUBSCRIBE_RADIO:
                    if (info.getObj() instanceof Album) {
                        Album bean = (Album) info.getObj();
                        Album currentAlbum = PlayInfoManager.getInstance().getSubscribeAlbum();
                        if (bean.getId() == currentAlbum.getId()
                                && bean.getSid() == currentAlbum.getSid()) {
                            mView.setSubscribeStatus(false, true);
                        }
                    }
                    break;
                case InfoMessage.PLAY_LIST_NORMAL:
                    mView.showLoadContent();
                    break;
                case InfoMessage.PLAY_LIST_NET_TIMEOUT_ERROR:
                    mView.showLoadTimeOut();
                    break;
                case InfoMessage.PLAY_LIST_RESP_ALBUM_AUDIO_ERROR_NO_DATA:
                    mView.showLoadNotData();
                    break;
                case InfoMessage.PLAY_LIST_NET_ERROR:
                    mView.showLoadNotNet();
                    break;
                case InfoMessage.PLAY_LIST_LOADING:
                    mView.showLoading();
                    break;
            }
        }
    }

    @Override
    public void onPlayListChanged(List<Audio> audios) {
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        if (null == currentAlbum) {
            mView.setSubscribeVisibility(false);
            return;
        }

        Disposable disposable = io.reactivex.Observable.just(currentAlbum)
                .groupBy(new Function<Album, Boolean>() {
                    @Override
                    public Boolean apply(Album album) throws Exception {
                        return FavorHelper.isSupportSubscribe(album);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<GroupedObservable<Boolean, Album>>() {
                    @Override
                    public void accept(GroupedObservable<Boolean, Album> booleanAlbumGroupedObservable) throws Exception {
                        Boolean key = booleanAlbumGroupedObservable.getKey();
                        if (!key) {
                            mView.setSubscribeVisibility(false);
                        } else {
                            Disposable disposable = booleanAlbumGroupedObservable.map(new Function<Album, Boolean>() {
                                @Override
                                public Boolean apply(Album album) throws Exception {
                                    return FavorHelper.isSubscribe(album);
                                }
                            })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<Boolean>() {
                                        @Override
                                        public void accept(Boolean aBoolean) throws Exception {
                                            mView.setSubscribeVisibility(true);
                                            mView.setSubscribeStatus(aBoolean, true);
                                        }
                                    });
                            mCompositeDisposable.add(disposable);
                        }
                    }
                });

        mCompositeDisposable.add(disposable);

    }

    @Override
    public void onPlayListItemChanged(List<PlayListItem> playListItems) {
        if (null != mView) {
            mView.refreshData(null, playListItems);
        }
    }

    @Override
    public void onPlayItemChanged(int pos) {
        if (null != mView) {
            mView.refreshItem(pos);
        }
    }

    public static class PlayListItemDiffCallback extends DiffUtil.Callback {

        private List<PlayListItem> mOldData;
        private List<PlayListItem> mNewData;

        public PlayListItemDiffCallback(List<PlayListItem> oldData, List<PlayListItem> newData) {
            this.mOldData = oldData;
            this.mNewData = newData;
        }

        @Override
        public int getOldListSize() {
            return mOldData == null ? 0 : mOldData.size();
        }

        @Override
        public int getNewListSize() {
            return mNewData == null ? 0 : mNewData.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            Audio oldAudio = mOldData.get(oldItemPosition).getAudio();
            Audio newAudio = mNewData.get(newItemPosition).getAudio();

            return oldAudio.getId() == newAudio.getId() && oldAudio.getSid() == newAudio.getSid();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            PlayListItem oldPlayListItem = mOldData.get(oldItemPosition);
            PlayListItem newPlayListItem = mNewData.get(oldItemPosition);

            return oldPlayListItem.areContentsTheSame(newPlayListItem);
        }
    }
}
