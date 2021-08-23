package com.txznet.music.historyModule.ui;


import android.content.Context;

import com.txznet.music.baseModule.BasePresenter;
import com.txznet.music.baseModule.BaseView;
import com.txznet.music.historyModule.bean.HistoryData;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by brainBear on 2018/1/16.
 */

public interface HistoryContract {

    int TYPE_MUSIC = 1;
    int TYPE_RADIO = 2;


    public interface View extends BaseView<Presenter> {

        void showHistory(List<HistoryData> historyData);

        void removeHistory(HistoryData historyData);

        void showLoading();

        void hideLoading();

        void showEmpty();

        void refreshItem(int position);

        /**
         * @return
         */
        Context getJumpContext();
    }


    public interface Presenter extends BasePresenter {

        void play(List<HistoryData> historyData, int index);

        void favor(HistoryData historyData);

        void delete(HistoryData historyData);

        void requestHistory();
    }


    public interface DataSource {

        List<HistoryData> getMusicHistory();

        List<HistoryData> getAlbumHistory();

        Observable<List<HistoryData>> requestMusicHistory();

        Observable<List<HistoryData>> requestAlbumHistory();

        void deleteHistory(HistoryData historyData);

        void release();
    }

}
