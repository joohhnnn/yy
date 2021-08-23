package com.txznet.music.historyModule.ui;


import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.historyModule.bean.HistoryData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by brainBear on 2018/1/17.
 */

public class HistoryDataSource implements HistoryContract.DataSource {

    private static final String TAG = "HistoryDataSource";
    private static HistoryDataSource sInstance;
    private List<HistoryData> mMusicHistory = new ArrayList<>();
    private List<HistoryData> mAlbumHistory = new ArrayList<>();


    private HistoryDataSource() {
    }

    public static HistoryDataSource getInstance() {
        if (null == sInstance) {
            synchronized (HistoryDataSource.class) {
                if (null == sInstance) {
                    sInstance = new HistoryDataSource();
                }
            }
        }
        return sInstance;
    }


    @Override
    public List<HistoryData> getMusicHistory() {
        return mMusicHistory;
    }

    @Override
    public List<HistoryData> getAlbumHistory() {
        return mAlbumHistory;
    }

    @Override
    public Observable<List<HistoryData>> requestMusicHistory() {
        return Observable.fromCallable(new Callable<List<HistoryData>>() {
            @Override
            public List<HistoryData> call() throws Exception {
                return DBManager.getInstance().findHistory();
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<List<HistoryData>, ObservableSource<HistoryData>>() {
                    @Override
                    public ObservableSource<HistoryData> apply(List<HistoryData> historyData) throws Exception {
                        return Observable.fromIterable(historyData);
                    }
                })
                .filter(new Predicate<HistoryData>() {
                    @Override
                    public boolean test(HistoryData historyData) throws Exception {
                        return historyData.getType() == HistoryData.TYPE_AUDIO;
                    }
                })
                .toList()
                .flatMapObservable(new Function<List<HistoryData>, ObservableSource<List<HistoryData>>>() {
                    @Override
                    public ObservableSource<List<HistoryData>> apply(List<HistoryData> historyData) throws Exception {
                        return Observable.just(historyData);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<HistoryData>>() {
                    @Override
                    public void accept(List<HistoryData> historyData) throws Exception {
                        mMusicHistory = historyData;
                    }
                });
    }

    @Override
    public Observable<List<HistoryData>> requestAlbumHistory() {
        return Observable.fromCallable(new Callable<List<HistoryData>>() {
            @Override
            public List<HistoryData> call() throws Exception {
                return DBManager.getInstance().findHistory();
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<List<HistoryData>, ObservableSource<HistoryData>>() {
                    @Override
                    public ObservableSource<HistoryData> apply(List<HistoryData> historyData) throws Exception {
                        return Observable.fromIterable(historyData);
                    }
                })
                .filter(new Predicate<HistoryData>() {
                    @Override
                    public boolean test(HistoryData historyData) throws Exception {
                        return historyData.getType() == HistoryData.TYPE_ALBUM;
                    }
                })
                .toList()
                .flatMapObservable(new Function<List<HistoryData>, ObservableSource<List<HistoryData>>>() {
                    @Override
                    public ObservableSource<List<HistoryData>> apply(List<HistoryData> historyData) throws Exception {
                        return Observable.just(historyData);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<HistoryData>>() {
                    @Override
                    public void accept(List<HistoryData> historyData) throws Exception {
                        mAlbumHistory = historyData;
                    }
                });
    }

    @Override
    public void deleteHistory(HistoryData historyData) {
        if (mMusicHistory.contains(historyData)) {
            mMusicHistory.remove(historyData);
        } else if (mAlbumHistory.contains(historyData)) {
            mAlbumHistory.remove(historyData);
        }
        DBManager.getInstance().deleteHistory(historyData);
    }

    @Override
    public void release() {
    }


}
