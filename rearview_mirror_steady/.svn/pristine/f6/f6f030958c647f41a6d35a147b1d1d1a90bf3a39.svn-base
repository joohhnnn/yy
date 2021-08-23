package com.txznet.txz.component.media.loader;

import android.support.annotation.NonNull;

import com.txznet.txz.component.media.base.AbsMusicTool;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.component.media.util.MediaModelConverter;
import com.txznet.txz.component.media.util.MediaSearchPresenter;
import com.txznet.txz.module.media.plugin.IPluginMediaTool;
import com.txznet.txz.module.media.plugin.PluginMediaModel;
import com.txznet.txz.module.music.bean.AudioShowData;

import java.util.List;

/**
 * 装载的音乐工具
 *
 * 封装装载的音乐工具适配, 转发相关方法调用
 *
 * Created by J on 2019/3/15.
 */
public class LoadedMusicTool extends AbsMusicTool implements ILoadedMediaTool {
    private IPluginMediaTool mLoadedTool;
    private boolean bShowSearchResult = false;

    private MediaSearchPresenter<PluginMediaModel> mSearchPresenter =
            new MediaSearchPresenter<PluginMediaModel>(this) {

                @Override
                public void getSearchResult(final MediaModel model,
                                            final SearchCallback<PluginMediaModel> callback) {
                    mLoadedTool.search(model.toPluginMediaModel(),
                            new IPluginMediaTool.MediaSearchCallback() {
                                @Override
                                public void onSuccess(final List<PluginMediaModel> result) {
                                    callback.onSuccess(result);
                                }

                                @Override
                                public void onError(final String cause) {
                                    callback.onFailure(cause);
                                }
                            });
                }

                @Override
                public void play(final List<PluginMediaModel> list, final int index) {
                    mLoadedTool.play(list, index);
                }

                @NonNull
                @Override
                public AudioShowData getShowModel(final PluginMediaModel resultModel) {
                    AudioShowData asd = new AudioShowData();
                    asd.setTitle(resultModel.getTitle());

                    if (null != resultModel.getArtists() && resultModel.getArtists().length > 0) {
                        asd.setName(resultModel.getArtists()[0]);
                    }

                    asd.setAlbumName(resultModel.getAlbum());

                    return asd;
                }
            };

    public LoadedMusicTool(IPluginMediaTool tool) {
        mLoadedTool = tool;
    }

    @Override
    public byte[] sendInvoke(final String cmd, final byte[] data) {
        return mLoadedTool.invoke(cmd, data);
    }

    @Override
    public String getPackageName() {
        return mLoadedTool.getPackageName();
    }

    @Override
    public int getPriority() {
        return mLoadedTool.getPriority();
    }

    @Override
    public void cancelRequest() {
        if (bShowSearchResult) {
            mSearchPresenter.cancelPlayMusic();
        }
    }

    @Override
    public void open(final boolean play) {
        mLoadedTool.open(play);
    }

    @Override
    public void exit() {
        mLoadedTool.exit();
    }

    @Override
    public void play(final MediaModel model) {
        mSearchPresenter.playMusic(model, bShowSearchResult);
    }

    @Override
    public void stop() {
        mLoadedTool.stop();
    }

    @Override
    public void pause() {
        mLoadedTool.pause();
    }

    @Override
    public void continuePlay() {
        mLoadedTool.continuePlay();
    }

    @Override
    public void next() {
        mLoadedTool.next();
    }

    @Override
    public void prev() {
        mLoadedTool.prev();
    }

    @Override
    public void switchLoopMode(final LOOP_MODE mode) {
        mLoadedTool.switchLoopMode(MediaModelConverter.toPluginLoopMode(mode));
    }

    @Override
    public void collect() {
        mLoadedTool.collect();
    }

    @Override
    public void unCollect() {
        mLoadedTool.unCollect();
    }

    @Override
    public void playCollection() {
        mLoadedTool.playCollection();
    }

    @Override
    public void subscribe() {
        mLoadedTool.subscribe();
    }

    @Override
    public void unSubscribe() {
        mLoadedTool.unSubscribe();
    }

    @Override
    public void playSubscribe() {
        mLoadedTool.playSubscribe();
    }

    @Override
    public PLAYER_STATUS getStatus() {
        return MediaModelConverter.fromPluginPlayerStatus(mLoadedTool.getStatus());
    }

    @Override
    public MediaModel getPlayingModel() {
        return MediaModel.fromPluginMediaModel(mLoadedTool.getPlayingModel());
    }

    @Override
    public boolean supportLoopMode(final LOOP_MODE mode) {
        return mLoadedTool.supportLoopMode(MediaModelConverter.toPluginLoopMode(mode));
    }

    @Override
    public boolean supportCollect() {
        return mLoadedTool.supportCollect();
    }

    @Override
    public boolean supportUnCollect() {
        return mLoadedTool.supportUnCollect();
    }

    @Override
    public boolean supportPlayCollection() {
        return mLoadedTool.supportPlayCollection();
    }

    @Override
    public boolean supportSubscribe() {
        return mLoadedTool.supportSubscribe();
    }

    @Override
    public boolean supportUnSubscribe() {
        return mLoadedTool.supportUnSubscribe();
    }

    @Override
    public boolean supportPlaySubscribe() {
        return mLoadedTool.supportPlaySubscribe();
    }

    @Override
    public boolean supportSearch() {
        return mLoadedTool.supportSearch();
    }

    @Override
    public boolean hasNext() {
        return mLoadedTool.hasNext();
    }

    @Override
    public boolean hasPrev() {
        return mLoadedTool.hasPrev();
    }

    @Override
    public void setSearchTimeout(final long timeout) {
        mSearchPresenter.setSearchTimeout(timeout);
    }

    @Override
    public void setShowSearchResult(final boolean show) {
        bShowSearchResult = show;
    }

    @Override
    public boolean interceptRecordWinControl(final MEDIA_TOOL_OP op) {
        if (MEDIA_TOOL_OP.PLAY == op && bShowSearchResult) {
            return true;
        }
        return false;
    }
}
