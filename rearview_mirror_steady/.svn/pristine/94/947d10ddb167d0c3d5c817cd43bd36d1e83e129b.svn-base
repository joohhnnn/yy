package com.txznet.music.albumModule.ui;

import android.app.Activity;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.data.entity.Category;
import com.txznet.music.albumModule.ui.adapter.BaseSwipeAdapter;
import com.txznet.music.baseModule.BasePresenter;
import com.txznet.music.baseModule.BaseView;

import java.util.List;

/**
 * Created by brainBear on 2018/2/24.
 */

public interface AlbumListContract {


    public interface View extends BaseView<Presenter> {

        void showLoadMore();

        void hideLoadMore();

        void showLoading();

//        void hideLoading();

        void showAlbums(List<Album> albums, boolean isLoadMore);

        void refreshItem(int pos);

        void showEmpty();

        void showError(String hint);

        /**
         * 跳转到车主超级电台的播放界面
         */
        void jumpToSuperRadioView(Album album, long categoryID);

        /**
         * 跳转到播放详情界面
         */
        void jumpToPlayerDetailView(int screen, Album album, long categoryID);

        /**
         * 跳转到分类电台的播放界面
         */
        void jumpToTypeRadioView(Album album, long categoryID);

        void updateItemPlayStatus();
    }

    public interface Presenter extends BasePresenter {

        void requestAlbum(boolean isLoadMore);

        void showAlbum(Album album);

        void playAlbum(Album album);

        void jumpToDetail(int screen, Album album, Category category, int position);

        BaseSwipeAdapter getAlbumAdapter(Category mCategory, Activity activity);
    }

}
