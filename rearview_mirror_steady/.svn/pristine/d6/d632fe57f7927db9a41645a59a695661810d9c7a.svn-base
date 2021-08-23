package com.txznet.music.ui;

import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.data.http.AudioRepository;
import com.txznet.music.data.http.CarFmRepository;
import com.txznet.music.data.http.resp.RespCarFmCurTops;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.net.rx.TongtingException;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.PlayerInfoUpdateListener;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.logic.listener.IPlayerListCarfmOtherListener;
import com.txznet.music.playerModule.logic.listener.IPlayerListEndListener;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.helper.CarFmLogicHelper;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.Utils;

import java.util.ArrayList;
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
 * @author kevin
 */

public class PlayRadioRecPresenter implements PlayInfoRadioRecContract.Presenter, PlayerInfoUpdateListener, Observer, IPlayerListEndListener, IPlayerListCarfmOtherListener {

    private static final String TAG = "PlayRadioRecPresenter:";
    private PlayInfoRadioRecContract.View mView;
    private CompositeDisposable mCompositeDisposable;
    private CompositeDisposable mClockDisposable;

    private Album mAlbum;
    private Album willPlayAlbum;
    private long mCategoryID;
    int type = -1;
    Disposable fullReq = null;//整点请求
    Disposable interval;//轮询的请求

    private long click_time;//点击时间

    private static class EmptyView implements PlayInfoRadioRecContract.View {

        //##创建一个单例类##
        private volatile static EmptyView singleton;

        private EmptyView() {
        }

        public static EmptyView getInstance() {
            if (singleton == null) {
                synchronized (EmptyView.class) {
                    if (singleton == null) {
                        singleton = new EmptyView();
                    }
                }
            }
            return singleton;
        }

        @Override
        public void showAlbums(List<Album> albums) {

        }

        @Override
        public void showLoadContent() {

        }

        @Override
        public void showLoadTimeOut() {

        }

        @Override
        public void showLoadNotData() {

        }

        @Override
        public void showLoadNotNet() {

        }

        @Override
        public void showLoading() {

        }

        @Override
        public void setTitle(String name) {

        }

        @Override
        public void changeAlbum(Album album) {

        }

        @Override
        public void onPlayInfoUpdated(Audio audio, Album album) {

        }

        @Override
        public void onProgressUpdated(long position, long duration) {

        }

        @Override
        public void onPlayerModeUpdated(int mode) {

        }

        @Override
        public void onPlayerStatusUpdated(int status) {

        }

        @Override
        public void onBufferProgressUpdated(List<LocalBuffer> buffers) {

        }

        @Override
        public void showTips(String tips) {

        }

        @Override
        public void onFavorVisibilityChanged(boolean visibility) {

        }

        @Override
        public void onFavorStatusChanged(boolean isFavor, boolean available) {

        }

        @Override
        public void onSubscribeStatusChanged(boolean isSubscribe, boolean available) {

        }

        @Override
        public void exitView() {

        }

        @Override
        public void setPresenter(PlayInfoRadioRecContract.Presenter presenter) {

        }
    }

    private PlayInfoRadioRecContract.View getView() {
        if (mView == null) {
            return EmptyView.getInstance();
        }
        return mView;
    }

    private void setView(PlayInfoRadioRecContract.View view) {
        mView = view;
    }

    public PlayRadioRecPresenter(PlayInfoRadioRecContract.View view, Intent intent) {
        setView(view);
        initIntentData(intent);
        getView().setTitle(getTitleName());
        mCompositeDisposable = new CompositeDisposable();
        mCompositeDisposable.clear();
        mClockDisposable = new CompositeDisposable();
        mClockDisposable.clear();

    }

    private void initIntentData(Intent intent) {
        if (intent != null) {
            if (intent.hasExtra(PlayInfoManager.INTENT_FIELD_PARENT_ALBUM)) {
                mAlbum = JsonHelper.toObject(intent.getStringExtra(PlayInfoManager.INTENT_FIELD_PARENT_ALBUM), Album.class);
                willPlayAlbum = JsonHelper.toObject(intent.getStringExtra(PlayInfoManager.INTENT_FIELD_ALBUM), Album.class);
            } else {
                mAlbum = JsonHelper.toObject(intent.getStringExtra(PlayInfoManager.INTENT_FIELD_ALBUM), Album.class);
            }


            mCategoryID = intent.getLongExtra(PlayInfoManager.INTENT_FIELD_CATEGORY_ID, -1);
            type = intent.getIntExtra(PlayInfoManager.INTENT_FIELD_TYPE, -1);
        }
    }

    public String getTitleName() {
        if (type == PlayInfoManager.TYPE_CAR_FM) {
            if (mAlbum != null) {
                return GlobalContext.get().getResources().getText(R.string.str_car_fm).toString();
            }
        } else if (type == PlayInfoManager.TYPE_NORMAL_FM) {
            if (mAlbum != null) {
                return mAlbum.getName();
            }
        }
        return "";
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
        PlayInfoManager.getInstance().setPlayerListEndListener(this);
        PlayInfoManager.getInstance().setsIPlayerListCarFmOtherListener(this);

        ObserverManage.getObserver().addObserver(this);
    }

    @Override
    public void unregister() {
        PlayInfoManager.getInstance().removePlayerInfoUpdateListener(this);
        ObserverManage.getObserver().deleteObserver(this);
        PlayInfoManager.getInstance().clearPlayerListEndListener();

        mCompositeDisposable.clear();
        mClockDisposable.clear();
        setView(null);
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
            FavorHelper.unfavor(currentAudio, EnumState.Operation.manual);
        } else {
            FavorHelper.favor(currentAudio, EnumState.Operation.manual);
        }
    }

    @Override
    public void subscribe(boolean isCancel) {
        Album currentAlbum = PlayInfoManager.getInstance().getSubscribeAlbum();
        if (null == currentAlbum) {
            return;
        }

        if (isCancel) {
            FavorHelper.unSubscribeRadio(currentAlbum, EnumState.Operation.manual);
        } else {
            FavorHelper.subscribeRadio(currentAlbum, EnumState.Operation.manual);
        }
    }

    //标志位10000下的Album(从分时段主题中选出推荐的主题)
    static Album isAlbum;

    /**
     * 获取当前时段的分时段主题
     *
     * @param albums
     * @return
     */
//    private Album getCurrTimeAlbum(List<Album> albums) {
//        int i = 0;
//        if (CollectionUtils.isNotEmpty(albums)) {
//            Album tempAlbum = albums.get(0);
//            for (Album album : albums) {
//                if (Utils.getDataWithPosition(album.getFlag(), Album.FLAG_CURRENT_TIME_ZONE) == Album.FLAG_SUPPORT) {
//                    tempAlbum = album;
//                    break;
//                }
//                i++;
//            }
//            setCurrentTimeAlbum(tempAlbum);
//        }
//        if (isAlbum == null) {
//            LogUtil.e(TAG, "carFM req alum/audio is error," + "due to null");
//            ToastUtils.showShortOnUI("当前没有分时段主题");
//            return null;
//        }
//        return isAlbum;
//    }


    /**
     * 切换不同的分时段主题
     *
     * @param album
     */
    @Override
    public void changeTheme(final Album album, long audioId) {
        getView().changeAlbum(album);
        setCurrentTimeAlbum(album);
        ReportEvent.reportClickTimeOfTheme(String.valueOf(ReportEvent.TYPE_MANUAL), String.valueOf(album.getId()));
        PlayEngineFactory.getEngine().release(EnumState.Operation.manual);
        reqAlbumAudios(album, audioId);
    }

    private void reqAlbumAudios(final Album album, long audioId) {
        getView().showLoading();
        Disposable subscribe = AudioRepository.getInstance().getAudios(album, null, true, false).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<Audio>>() {
            @Override
            public void accept(List<Audio> audios) throws Exception {
                if (CollectionUtils.isNotEmpty(audios)) {
                    getView().showLoadContent();
                    PlayEngineFactory.getEngine().setAudios(EnumState.Operation.manual, audios, album, 0, PlayInfoManager.DATA_CHEZHU_FM);
                    PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
                } else {
                    getView().showLoadNotData();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                getView().showLoadTimeOut();
            }
        });
        mCompositeDisposable.add(subscribe);
    }


    /**
     * 刷新当前的分时段主题的内容
     */
    @Override
    public void refreshContent() {
        if (type == PlayInfoManager.TYPE_CAR_FM) {
            if (isAlbum != null) {
                reqAlbumAudios(isAlbum, 0);
            } else {
                queryAlbum(mAlbum);
            }
        } else if (type == PlayInfoManager.TYPE_NORMAL_FM) {
            reqAlbumAudios(mAlbum, 0);
        }
    }


    public void reqData() {
        if (type == PlayInfoManager.TYPE_CAR_FM) {
            Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
            if (currentAlbum != null && (mAlbum.equals(currentAlbum) || mAlbum.equals(currentAlbum.getParentAlbum()))) {
                Log.d(TAG, "reqData: " + "相同");
                List<Album> carFmAlbums = PlayInfoManager.getCarFmAlbums();
                getView().showAlbums(carFmAlbums);
                // TODO: 2018/8/22 需要判断如果点击进来的是另外一个分时段的，则需要特别注意 @see willplayAlbum
                if (carFmAlbums != null && willPlayAlbum != null && carFmAlbums.contains(willPlayAlbum)) {
                    //如果当前的album就是播放的album则不用管
                    if (!currentAlbum.equals(willPlayAlbum)) {
                        changeTheme(willPlayAlbum, 0);
                        return;
                    }
                }
                if (isAlbum == null) {
                    setCurrentTimeAlbum(currentAlbum);
                }
                PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
            } else {
                queryAlbum(mAlbum);
            }
        } else if (type == PlayInfoManager.TYPE_NORMAL_FM) {
            PlayInfoManager.setCarFmAlbums(null);
            if (needRequestAudios(mAlbum)) {
                reqAlbumAudios(mAlbum, 0);
            } else if (!isPlaying()) {
                toPlay();
            }
        }
    }

    /**
     * 查询当前分时段主题专辑
     */
    @Override
    public void queryAlbum(Album album) {
        getView().showLoading();

        Disposable subscribe = CarFmRepository.getInstance().getTypeFmObservable(album.getId()).flatMap(v -> {
            if (CollectionUtils.isNotEmpty(v)) {
                CarFmLogicHelper.getInstance().addAlbumParent(v, album);
            } else {
                return io.reactivex.Observable.error(new TongtingException("car fm no type albums", TongtingException.ERROR_CODE_EMPTY));
            }
            if (willPlayAlbum != null && v.contains(willPlayAlbum)) {
                setCurrentTimeAlbum(willPlayAlbum);
            } else {
                setCurrentTimeAlbum(CarFmUtils.getInstance().getNeedPlayAlbum(v));
            }

            if (null != isAlbum) {
                return AudioRepository.getInstance().getAudios(isAlbum, null, true, false);
            } else {
                return io.reactivex.Observable.error(new TongtingException("car fm no type albums", TongtingException.ERROR_CODE_DATA));
            }
        }).subscribe(new Consumer<List<Audio>>() {
            @Override
            public void accept(List<Audio> audios) throws Exception {
                if (CollectionUtils.isNotEmpty(audios)) {
                    getView().showLoadContent();
                    getView().showAlbums(PlayInfoManager.getCarFmAlbums());
                    PlayEngineFactory.getEngine().setAudios(EnumState.Operation.manual, audios, isAlbum, 0, PlayInfoManager.DATA_CHEZHU_FM);
                    PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
                } else {
                    getView().showLoadNotData();
                }
            }
        }, e -> {
            if (e instanceof TongtingException) {
                if (((TongtingException) e).getErrorCode() == TongtingException.ERROR_CODE_EMPTY) {
                    getView().showLoadNotData();
                } else if (((TongtingException) e).getErrorCode() == TongtingException.ERROR_CODE_DATA) {
                    getView().showLoadContent();
                    getView().showTips("获取不到当前时段的主题区域");
                }
            } else {
                getView().showLoadTimeOut();
            }
        });
        mCompositeDisposable.add(subscribe);
    }

    /**
     * DESC 除了FM标志的，其它播放都用这个
     *
     * @param album
     * @param categoryID
     */
    @Override
    public void playAlbum(final Album album, final long categoryID) {
        Album playingAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();
        if (playingAlbum != null && playingAlbum.equals(album)) {
            if (PlayEngineFactory.getEngine().isPlaying()) {
                return;
            }
            if (PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_PAUSE) {
                PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
                return;
            }
            return;
        }
//        type = TYPE_NORMAL_FM;
//        mAlbum = album;
        getView().showLoading();
        PlayEngineFactory.getEngine().release(EnumState.Operation.manual);
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                if (album == null) {
                    Logger.e(TAG, "playAlbum album is null");
                    return;
                }
                if (Utils.isSong(album.getSid())) {
                    AlbumEngine.getInstance().playAlbum(PlayInfoManager.DATA_CHEZHU_FM, EnumState.Operation.manual, album, categoryID, true, null);
                } else {
                    AlbumEngine.getInstance().playAlbumFMWithBreakpoint(EnumState.Operation.manual, album, categoryID, true);
                }
            }
        }, 0);
    }


    @Override
    public void onPlayInfoUpdated(final Audio audio, Album album) {
        Album currentAlbum = PlayInfoManager.getInstance().getSubscribeAlbum();
        if (audio != null) {
            if (audio.getAlbum() != null) {
                currentAlbum = audio.getAlbum();
            }
        }
        if (null == currentAlbum) {
            //如果当前的歌曲为空,则需要退出界面
//            getView().exitView();
//            getView().showLoadNotData();
            return;
        }
        getView().onPlayInfoUpdated(audio, album);
        getView().showLoadContent();
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
                            getView().onFavorVisibilityChanged(false);
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
                                            getView().onFavorVisibilityChanged(true);
                                            getView().onSubscribeStatusChanged(aBoolean, true);
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
        getView().onProgressUpdated(position, duration);
    }

    @Override
    public void onPlayerModeUpdated(int mode) {
        getView().onPlayerModeUpdated(mode);
    }

    @Override
    public void onPlayerStatusUpdated(int status) {
        getView().onPlayerStatusUpdated(status);
    }

    @Override
    public void onBufferProgressUpdated(List<LocalBuffer> buffers) {
        getView().onBufferProgressUpdated(buffers);
    }

    @Override
    public void onFavourStatusUpdated(int favour) {

    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            Logger.d(TAG, info.getType() + ":" + (info.getObj() != null ? info.getObj() : "null"));
            switch (info.getType()) {
                case InfoMessage.SUBSCRIBE_RADIO:
                    if (info.getObj() instanceof Album) {
                        Album bean = (Album) info.getObj();
                        Album currentAlbum = PlayInfoManager.getInstance().getSubscribeAlbum();
                        if (bean.getId() == currentAlbum.getId()
                                && bean.getSid() == currentAlbum.getSid()) {
//                            getView().setSubscribeStatus(true, true);
                            getView().onSubscribeStatusChanged(true, true);
                        }
                    }
                    break;
                case InfoMessage.UNSUBSCRIBE_RADIO:
                    if (info.getObj() instanceof Album) {
                        Album bean = (Album) info.getObj();
                        Album currentAlbum = PlayInfoManager.getInstance().getSubscribeAlbum();
                        if (bean.getId() == currentAlbum.getId()
                                && bean.getSid() == currentAlbum.getSid()) {
                            getView().onSubscribeStatusChanged(false, true);
                        }
                    }
                    break;
//                case InfoMessage.RESP_ALBUM:
//                    if (type == TYPE_CAR_FM) {
//                        ResponseSearchAlbum responseAlbum = (ResponseSearchAlbum) info.getObj();
//                        if (String.valueOf(mAlbum.getId()).equals(responseAlbum.getCategoryId())) {
//                            List<Album> albums = new ArrayList<Album>();
//                            if (CollectionUtils.isNotEmpty(responseAlbum.getArrAlbum())) {
//                                for (Album album : responseAlbum.getArrAlbum()) {
//                                    album.setParentAlbum(mAlbum);
//                                    albums.add(album);
//                                }
//                            }
//                            PlayInfoManager.setCarFmAlbums(albums);
//                            //展示Album
//                            getView().handleAlbum(albums);
//                            //获取当前专辑的曲目列表内容
//                            getView().showLoading();
//                            isPlayAlbum(albums);
//                        } else {
//                            LogUtil.d(TAG, "mAlbum：" + mAlbum.toString() + " , " + responseAlbum.toString());
//                        }
//                    } else {
//                        LogUtil.d(TAG, "type=" + type);
//                    }
//                    break;
                case InfoMessage.NET_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_NET:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_NO_NET:
                    getView().showLoadNotNet();
                    break;
                case InfoMessage.NET_TIMEOUT_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_TIMEOUT:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_TIMEOUT:
                    getView().showLoadTimeOut();
                    break;
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_UNKNOWN:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_UNKNOWN:
                    getView().showLoadTimeOut();
                    break;
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_DATA:
                    getView().showLoadNotData();
                default:

                    break;
            }
        }
    }


    /**
     * 点击播放
     */
    private boolean isPlaying() {
        return PlayEngineFactory.getEngine().isPlaying();
    }

    private void toPlay() {
        if (PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_PAUSE) {
            PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
        }
    }

    /**
     * 判断是否需要请求网络，如果遇到现在播放的就是即将要请求的则不用再次请求了。
     *
     * @param album
     * @return
     */
    private boolean needRequestAudios(Album album) {
        Album playingAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();
        return !(playingAlbum != null && playingAlbum.equals(album));
    }

    @Override
    public boolean onListEnd(Audio audio, Album album) {
        //切换到下一个专辑
        int index = -1;
        if (album != null) {
            List<Album> carFmAlbums = PlayInfoManager.getCarFmAlbums();
            if (com.txznet.comm.util.CollectionUtils.isNotEmpty(carFmAlbums) && (index = carFmAlbums.indexOf(album)) >= 0) {
                if (album.getParentAlbum() != null) {
                    if (index < carFmAlbums.size() - 1) {
                        index = index + 1;
                    } else {
                        index = 0;
                    }
                    TtsUtil.speakResource("RS_VOICE_MUSIC_NEXT_BEGIN_OTHER_CAR_FM", Constant.RS_VOICE_MUSIC_NEXT_BEGIN_OTHER_CAR_FM);
                    changeTheme(carFmAlbums.get(index), 0);
                    return true;
                }
            }
            Logger.d(TAG + ":" + index, carFmAlbums);
        }
        return false;
    }

    @Override
    public boolean otherCarFm(RespCarFmCurTops data) {

        Album album = new Album();
        album.setId(data.getNext_album_id());
        ArrayList<Long> categorys = new ArrayList<Long>();
        categorys.add(data.getNext_category_id());
        album.setArrCategoryIds(categorys);
        List<Album> carFmAlbums = PlayInfoManager.getCarFmAlbums();
        if (CollectionUtils.isNotEmpty(carFmAlbums)) {
            int i = carFmAlbums.indexOf(album);
            if (i >= 0) {
//                carFmAlbums.get(i).getFlag()
                Album album1 = carFmAlbums.get(i);
                album1.setFlag(Utils.setDataWithPosition(album1.getFlag(), Album.FLAG_SUPPORT, Album.FLAG_CURRENT_TIME_ZONE));

                //将当前分时段主题的标志位进行置换

                isAlbum.setFlag(Utils.setDataWithPosition(isAlbum.getFlag(), Album.FLAG_UNSUPPORT, Album.FLAG_CURRENT_TIME_ZONE));


                changeTheme(album1, data.getNext_audio_id());
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /**
     * 设置当前分时段主题
     *
     * @param album
     */
    public void setCurrentTimeAlbum(Album album) {
        isAlbum = album;
        CarFmUtils.getInstance().setIsPlayingAlbum(isAlbum);
        //如果当前播放的和即将要播放的是一个专辑，则清空即将播放的专辑
        RespCarFmCurTops nextCarFmTime = PlayInfoManager.getInstance().getNextCarFmTime();
        if (nextCarFmTime != null) {
            Album tempAlbum = new Album();
            tempAlbum.setId(nextCarFmTime.getNext_album_id());
            tempAlbum.setSid(nextCarFmTime.getNext_album_sid());
        }


        click_time = SystemClock.elapsedRealtime();
//        if (CarFmUtils.getInstance().isCurrentTime(album)) {
        ObserverManage.getObserver().send(InfoMessage.UPDATE_CAR_FM_CURRENT_TIME, album);
//        }

        CarFmUtils.getInstance().reqFullTimeReq(isAlbum, click_time);
    }
}
