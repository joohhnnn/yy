package com.txznet.music.ui;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.BasePresenter;
import com.txznet.music.baseModule.BaseView;
import com.txznet.music.baseModule.bean.PlayerInfo;

import java.util.List;

/**
 * @author kevin
 */

public interface PlayInfoRadioRecContract {

    public interface View extends PlayInfoContract.View<PlayInfoRadioRecContract.Presenter> {

        void showAlbums(List<Album> albums);

//        long getAlbumId();

        void showLoadContent();

        void showLoadTimeOut();

        void showLoadNotData();

        void showLoadNotNet();

        void showLoading();

        void setTitle(String name);

        void changeAlbum(Album album);
    }


    public interface Presenter extends PlayInfoContract.Presenter {

//        void isPlayAlbum(List<Album> albums);

        void changeTheme(Album album, long audioId);

        void refreshContent();

        void queryAlbum(Album album);

        void playAlbum(Album album, long categoryId);
    }


}
