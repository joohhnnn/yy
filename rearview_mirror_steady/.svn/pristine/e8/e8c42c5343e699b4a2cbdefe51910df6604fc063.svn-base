package com.txznet.launcher.module.music;

import com.txznet.launcher.component.BasePresenter;
import com.txznet.launcher.component.BaseView;
import com.txznet.launcher.domain.music.bean.PlayInfo;

/**
 * Created by brainBear on 2018/2/23.
 * 基于mvp模式，定义musci的v会有的变化和p会处理数据的情况
 */

public interface MusicContract {



    public interface View extends BaseView {

        void updatePlayInfo(PlayInfo playInfo);

        void updatePlayProgress(long progress, long duration);

        void updatePlayStatus(@PlayInfo.PlayState int playState);
    }


    public interface Presenter extends BasePresenter {

        void playOrPause();

        void play();

        void pause();

        void next();

        void prev();

        boolean isPlaying();

        void refreshData();
    }

}
