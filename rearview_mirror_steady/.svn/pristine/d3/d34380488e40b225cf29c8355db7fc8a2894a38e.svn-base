package com.txznet.music.playerModule.ui;

import android.support.v7.util.DiffUtil;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.BasePresenter;
import com.txznet.music.baseModule.BaseView;
import com.txznet.music.playerModule.PlayListItem;

import java.util.List;

/**
 * Created by brainBear on 2017/12/19.
 */

public interface PlayListContract {

    public interface View extends BaseView<Presenter> {

        void showRefresh();

        void showLoadMore();

        void hideRefreshOrLoadMore();

        void refreshData(DiffUtil.DiffResult diffResult, List<PlayListItem> playListItems);

        void refreshItem(int pos);

        void setSubscribeVisibility(boolean visible);

        void setSubscribeStatus(boolean isHighLight, boolean enable);

        void showLoadContent();

        void showLoadTimeOut();

        void showLoadNotData();

        void showLoadNotNet();

        void showLoading();
    }


    public interface Presenter extends BasePresenter {

        void favor(Audio audio, boolean isCancel);

        void subscribe(Album album, boolean isCancel);

        void play(Audio audio);

        void refresh();

        void loadMore();
    }


}
