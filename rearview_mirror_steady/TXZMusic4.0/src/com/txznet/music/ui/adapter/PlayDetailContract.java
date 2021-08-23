package com.txznet.music.ui.adapter;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.ui.PlayInfoContract;

/**
 * Created by 58295 on 2018/5/5.
 */

public class PlayDetailContract {

    public interface View extends PlayInfoContract.View<PlayDetailContract.Presenter> {

        void showLoadContent();

        void showLoadTimeOut();

        void showLoadNotData();

        void showLoadNotNet();

        void showLoading();

        void showPlayList(boolean show);
    }

    public interface Presenter extends PlayInfoContract.Presenter {

        void refreshContent(int screen, Album album, long categoryID);

        void playAlbum(int screen, Album album, long categoryID, boolean needPlay);

        void showPlayList();

    }
}
