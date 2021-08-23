package com.txznet.music.localModule;

import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.BasePresenter;
import com.txznet.music.baseModule.BaseView;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by brainBear on 2018/1/9.
 */

public interface LocalContract {


    public interface View extends BaseView<Presenter> {

        void showScanning();

        void dismissScanning();

        void showLocalData(List<Audio> localAudios);

        void showEmpty();

        void showContent();

        void showScanCount(int count);

        /**
         * 刷新列表指定位置，如果position < 0则会全部刷新
         *
         * @param position 指定刷新的位置
         */
        void refreshItem(int position);

        void deleteAudios(List<Audio> audios, boolean isSuccess);

        void hideCheckAllBtn(boolean hideBtn);

        /**
         * 加载视图
         *
         * @param showLoading
         */
        void showLoadingView(boolean showLoading);

        void showAllCheckState(boolean isCheckState);

    }


    public interface Presenter extends BasePresenter {

        boolean isScanning();

        void startScan();

        void stopScan();

        void favor(Audio audio);

        void unFavor(Audio audio);

        void deleteLocalAudio(List<Audio> audios, boolean isNeedDeleteFinalFile);

        void play(List<Audio> audios, int position);

        /**
         * 选中Audio
         *
         * @param audio
         * @param isCheck 是否选中
         */
        void checkItem(Audio audio, boolean isCheck);

        void checkItems(List<Audio> audios, boolean isCheck);

        /**
         * 默认加载的列表为已扫描到的音乐&最新缓存的音乐
         */
        void getLocalAudio();
    }


    public interface DataSource {

        /**
         * 获取缓存的本地歌曲
         *
         * @return 缓存的歌曲列表
         */
        List<Audio> getCache();

        List<Audio> getCheckIndexs();

        /**
         * 扫描本地歌曲，该方法必须在主线程调用
         *
         * @param listener 回调接口
         */
        void scanLocal(ILocalScanListener listener);

        /**
         * 从本地数据库获取歌曲列表
         */
        Observable<Audio> findLocalDBDate();

        /**
         * 当前是否正在扫描
         *
         * @return true 正在扫描
         */
        boolean isScanning();

        /**
         * 判断本地歌曲是否存在
         *
         * @param audio 本地歌曲
         * @return true 可用 false 不可用
         */
        boolean isAvailable(Audio audio);

        /**
         * 删除本地列表中不可用的歌曲
         *
         * @param listener 回调接口
         */
        void deleteNotAvailableAudios(ILocalScanListener listener);

        /**
         * 删除本地列表中不可用的歌曲
         *
         * @param listener 回调接口
         */
        void deleteAudios(List<Audio> audios, boolean isNeedDeleteFinalFile, IDeleteListener listener);

        Observable<List<Audio>> addAudio(Audio audio);
    }

    public interface ILocalScanListener {

        /**
         * 扫描到歌曲数改变时回调
         *
         * @param count 扫描到的歌曲数
         */
        void onScanCount(int count);

        /**
         * 扫描完成后回调
         *
         * @param audios 扫描完成的歌曲列表
         */
        void onScanFinish(List<Audio> audios);
    }

    public interface IDeleteListener {

        /**
         * 删除成功
         */
        void onSuccess(List<Audio> audios);

        /**
         * 删除失败
         */
        void onError(List<Audio> audios);
    }
}
