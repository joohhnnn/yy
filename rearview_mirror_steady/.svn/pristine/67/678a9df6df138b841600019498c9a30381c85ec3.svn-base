package com.txznet.music.data.http;

import com.txznet.music.albumModule.bean.Album;

import java.util.List;

import io.reactivex.Observable;

public interface IAlbumDataSource {

    /**
     * 获取专辑列表
     *
     * @param categoryId 分类ID
     * @param pageOff    页数
     * @param isCache    是否从缓存中获取
     * @return
     */
    Observable<List<Album>> getAlbums(long categoryId, int pageOff, boolean isCache);
}
