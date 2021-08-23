package com.txznet.music.ui;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.BasePresenter;
import com.txznet.music.baseModule.BaseView;
import com.txznet.music.baseModule.bean.PlayerInfo;

import java.util.List;

/**
 * Created by brainBear on 2017/12/6.
 */

public interface PlayInfoContract {

    public interface View<T extends Presenter> extends BaseView<T> {


        /**
         * 当前播放音频改变时候回调
         *
         * @param audio 新播放的音频
         * @param album 所在Album
         */
        void onPlayInfoUpdated(Audio audio, Album album);

        /**
         * 当播放进度更新时回调
         *
         * @param position 当前时长
         * @param duration 总时长
         */
        void onProgressUpdated(long position, long duration);

        /**
         * 播放模式更新时回调
         *
         * @param mode 播放模式
         */
        void onPlayerModeUpdated(@PlayerInfo.PlayerMode int mode);

        /**
         * 播放状态更新时回调
         *
         * @param status 播放状态
         */
        void onPlayerStatusUpdated(@PlayerInfo.PlayerUIStatus int status);

        /**
         * 缓冲数据更新时回调
         *
         * @param buffers 缓冲数据
         */
        void onBufferProgressUpdated(List<LocalBuffer> buffers);

        void showTips(String tips);

        void onFavorVisibilityChanged(boolean visibility);

        void onFavorStatusChanged(boolean isFavor, boolean available);

        void onSubscribeStatusChanged(boolean isSubscribe, boolean available);

    }


    public interface Presenter extends BasePresenter {

        void playNext();

        void playPrev();

        void playOrPause();

        void switchMode();

        void seek(long position);

        void favor(boolean isCancel);

        void subscribe(boolean isCancel);
    }


}
