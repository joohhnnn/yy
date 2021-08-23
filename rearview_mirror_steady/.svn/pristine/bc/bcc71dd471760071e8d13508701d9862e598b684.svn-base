package com.txznet.music.data.http;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.net.request.ReqAlbumAudio;
import com.txznet.music.soundControlModule.logic.net.request.ReqChapter;

import java.util.List;

import io.reactivex.Observable;

public interface IAudioDataSource {
    /**
     * 获取音频列表
     *
     * @param album
     * @param isCache 是否从缓存中获取数据
     * @return
     */
    Observable<List<Audio>> getAudios(final Album album, Audio audio, boolean isNext, boolean isCache);

    /**
     * @param album
     * @param audio   音频 可为null
     * @param isNext  是否往下获取数据
     * @param chapter 章节回
     * @param isCache
     * @return
     */
    Observable<List<Audio>> getAudios(final Album album, Audio audio, boolean isNext, List<ReqChapter> chapter, boolean isCache);

    Observable<List<Audio>> getAudios(ReqAlbumAudio reqAlbumAudio);
}
