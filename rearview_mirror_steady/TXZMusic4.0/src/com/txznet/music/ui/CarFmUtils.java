package com.txznet.music.ui;

import com.bumptech.glide.util.Util;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.ui.TXZObserver;
import com.txznet.music.data.http.CarFmRepository;
import com.txznet.music.data.http.resp.RespCarFmCurTops;
import com.txznet.music.playerModule.logic.IPlayListChangedListener;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.Utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CarFmUtils {

    private final static String TAG = "music:carfm:";

    private CompositeDisposable mChangeAlbumDisposable = null;
    private CompositeDisposable mClockDisposable = null;
    //##创建一个单例类##
    private volatile static CarFmUtils singleton;

    private CarFmUtils() {
        mChangeAlbumDisposable = new CompositeDisposable();
        mClockDisposable = new CompositeDisposable();
    }

    public static CarFmUtils getInstance() {
        if (singleton == null) {
            synchronized (CarFmUtils.class) {
                if (singleton == null) {
                    singleton = new CarFmUtils();
                }
            }
        }
        return singleton;
    }

    private Album mAlbum;

    public Album getNeedPlayAlbum(List<Album> albums) {
        if (mAlbum != null) {
            if (!isCurrentTime(mAlbum)) {
                return mAlbum;
            }
        }
        return mAlbum = getCurrTimeAlbum(albums);
    }

    public void setIsPlayingAlbum(Album album) {
        mAlbum = album;
    }

    public Album getIsPlayingAlbum() {
        return mAlbum;
    }

    /**
     * 判断是否事当前时段
     *
     * @param album
     * @return
     */
    public boolean isCurrentTime(@NonNull Album album) {
        return Utils.getDataWithPosition(album.getFlag(), Album.FLAG_CURRENT_TIME_ZONE) == Album.FLAG_SUPPORT;
    }


    private Album getCurrTimeAlbum(List<Album> albums) {
        int i = 0;
        Album tempAlbum = null;
        if (CollectionUtils.isNotEmpty(albums)) {
            tempAlbum = albums.get(0);
            for (Album album : albums) {
                if (Utils.getDataWithPosition(album.getFlag(), Album.FLAG_CURRENT_TIME_ZONE) == Album.FLAG_SUPPORT) {
                    tempAlbum = album;
                    break;
                }
                i++;
            }

        }
        return tempAlbum;
    }

    IPlayListChangedListener clearCheckNextAlbumRunnable = new IPlayListChangedListener() {
        @Override
        public void onPlayListChanged(List<Audio> audios) {
            //如果切换了车主FM则这里就需要反注册掉主题
                if (!Utils.isCarFm(PlayInfoManager.getInstance().getCurrentAlbum())) {
                    CarFmUtils.getInstance().clearCheckNextAlbum();
            }

        }
    };

    /**
     * 请求整点请求的数据
     *
     * @param isAlbum    分时段
     * @param click_time 点击的时间
     */
    public void reqFullTimeReq(final Album isAlbum, final long click_time) {

        PlayInfoManager.getInstance().addPlayListChangedListener(clearCheckNextAlbumRunnable);

        intervalAndRequest(1, mClockDisposable, isAlbum.getId(), click_time, new ICallback<RespCarFmCurTops>() {
            @Override
            public void onSuccess(RespCarFmCurTops data) {
                PlayInfoManager.getInstance().setNextCarFmTime(data);
            }

            @Override
            public void onError(int code) {

            }
        });

    }

    /**
     * @param isAlbum    车主Fm
     * @param click_time
     */
    public void changeAlbumName(final Album isAlbum, final long click_time) {
        // album id  为0
        long albumId = 0;
        if (null != CarFmUtils.getInstance().getIsPlayingAlbum()) {
            albumId = CarFmUtils.getInstance().getIsPlayingAlbum().getId();
        }
        intervalAndRequest(2, mChangeAlbumDisposable, albumId, click_time, new ICallback<RespCarFmCurTops>() {
            @Override
            public void onSuccess(RespCarFmCurTops data) {
                PlayInfoManager.getInstance().setNextCarFmTime(data);
                Album album = new Album();
                album.setPid(isAlbum.getId());
                album.setpSid(isAlbum.getSid());
                album.setId(data.getNext_album_id());
                album.setName(data.getNext_album_name());
                //发送一个事件出去

                //必须要等到，当前播放的非车主超级电台即可否则，自己会跳转的
                if (PlayInfoManager.getInstance().getCurrentAlbum() == null || !Utils.isCarFm(PlayInfoManager.getInstance().getCurrentAlbum())) {
                    ObserverManage.getObserver().send(InfoMessage.UPDATE_CAR_FM_CURRENT_TIME, album);
                }
            }

            @Override
            public void onError(int code) {

            }
        });


    }

    private void intervalAndRequest(final int type, final CompositeDisposable disposable, final long albumId, final long click_time, final ICallback callback) {
        disposable.clear();

        Observable<RespCarFmCurTops> carFmCurTopsObservable = null;
        if (type == 1) {
            carFmCurTopsObservable = CarFmRepository.getInstance().getCurTimeTops(albumId, click_time);
        } else if (type == 2) {
            carFmCurTopsObservable = CarFmRepository.getInstance().checkNextAlbumName(albumId, click_time);
        }

        carFmCurTopsObservable.observeOn(Schedulers.newThread()).subscribe(new TXZObserver<RespCarFmCurTops>() {

            @Override
            public void onSubscribe(Disposable d) {
                super.onSubscribe(d);
                disposable.add(d);
            }

            @Override
            public void onResponse(final RespCarFmCurTops data) {
                Logger.d(TAG + "response:", data);
                //开启定时器
                if (data.getNext_album_id() == 0) {

                } else {
//                        开启一个定时器
                    Disposable fullReq = Single.timer(data.getRemain_time(), TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long integer) throws Exception {
                            Logger.d(TAG, data.getRemain_time() + "setting next car fmTime");

                            callback.onSuccess(data);
                        }
                    });
                    disposable.add(fullReq);
                }

                //开启轮询
                if (data.getTimer() == 0) {
                    //表示当前时段非直播时段（直播定义：当前播放的专辑，在播放的时间段内）
                } else {
                    //开启轮询
                    long timeer = Math.max(data.getTimer(), 20);
                    Disposable interval = Single.timer(timeer, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            LogUtil.logd(TAG + "request full time :车主fm整点请求");
                            intervalAndRequest(type, disposable, albumId, click_time, callback);
                        }
                    });
                    disposable.add(interval);
                }
            }

            @Override
            public boolean showOtherException(final int code) {
                Disposable interval = Single.timer(20, TimeUnit.SECONDS).observeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        LogUtil.logd(TAG + "request full time :车主fm整点请求,error :" + code);
                        intervalAndRequest(type, disposable, albumId, click_time, callback);
                    }
                });
                disposable.add(interval);

                return true;
            }
        });
    }

    public void clearCheckAlbumNameTimer() {
        Logger.d(TAG, "clearCheckAlbumNameTimer");
        mChangeAlbumDisposable.clear();
    }

    public void clearCheckNextAlbum() {
        Logger.d(TAG, "clearCheckNextAlbum");
        mClockDisposable.clear();
    }


    private interface ICallback<T> {

        void onSuccess(T data);

        void onError(int code);
    }

}
