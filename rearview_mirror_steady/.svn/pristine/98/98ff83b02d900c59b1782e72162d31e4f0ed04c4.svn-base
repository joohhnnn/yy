package com.txznet.music.ui.base;

import android.os.Bundle;

import com.txznet.music.store.PlayInfoStore;

/**
 * 用来统一刷新adapter,统一注册当前播放的事件的
 *
 * @author telen
 * @date 2018/12/12,16:39
 */
public abstract class BasePlayerFragment<T extends BasePlayerAdapter> extends BaseFragment {

    protected T mAdapter;

    @Override
    protected void initData(Bundle savedInstanceState) {
        PlayInfoStore mPlayInfoStore = com.txznet.rxflux.extensions.aac.ViewModelProviders.of(getActivity()).get(PlayInfoStore.class);

        mPlayInfoStore.getCurrPlaying().observe(this, audioV5 -> {
            getAdapter().setPlayingAudio(audioV5);
        });

        mPlayInfoStore.isPlayingStrict().observe(this, aBoolean -> {
            getAdapter().setPlayState(aBoolean);
        });

        mPlayInfoStore.getAlbum().observe(this, album -> {
            getAdapter().setPlayingAlbum(album);
        });
        initAdapter(getAdapter());
    }

    protected void initAdapter(T adapter) {
    }

    public T getAdapter() {
        if (mAdapter == null) {
            mAdapter = setAdapter();
        }
        return mAdapter;
    }

    protected abstract T setAdapter();
}
