package com.txznet.music.data.http;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.net.request.ReqAlbumAudio;
import com.txznet.music.albumModule.logic.net.response.ResponseAlbumAudio;
import com.txznet.music.albumModule.ui.ResponseErrorException;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.rx.NetErrorException;
import com.txznet.music.net.rx.RxNet;
import com.txznet.music.soundControlModule.logic.net.request.ReqChapter;
import com.txznet.music.utils.CollectionUtils;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class AudioRepository implements IAudioDataSource {

    //##创建一个单例类##
    private volatile static AudioRepository singleton;

    private AudioRepository() {
    }

    public static AudioRepository getInstance() {
        if (singleton == null) {
            synchronized (AudioRepository.class) {
                if (singleton == null) {
                    singleton = new AudioRepository();
                }
            }
        }
        return singleton;
    }


    @Override
    public Observable<List<Audio>> getAudios(final Album album, Audio audio, boolean isNext, boolean isCache) {
        return getAudios(album, audio, isNext, null, isCache);
    }

    @Override
    public Observable<List<Audio>> getAudios(final Album album, Audio audio, boolean isNext, List<ReqChapter> chapter, boolean isCache) {

        ReqAlbumAudio reqAlbumAudio = new ReqAlbumAudio();
        reqAlbumAudio.setSid(album.getSid());
        reqAlbumAudio.setId(album.getId());
        reqAlbumAudio.setSvrData(album.getSvrData());
        if (audio != null) {
            reqAlbumAudio.setAudioId(audio.getId());
            reqAlbumAudio.setAudioSid(audio.getSid());
        }

        int order = 1;
        if (isNext) {
            order = 0;
        }
        reqAlbumAudio.setUp(order);
        reqAlbumAudio.setType(1);
        reqAlbumAudio.setCategoryId(album.getCategoryId());
        reqAlbumAudio.setOffset(Constant.PAGECOUNT);
        if (CollectionUtils.isNotEmpty(chapter)) {
            reqAlbumAudio.setArrMeasure(chapter);
        }

        return getAudios(reqAlbumAudio);

    }

    @Override
    public Observable<List<Audio>> getAudios(final ReqAlbumAudio reqAlbumAudio) {
        return RxNet.request(Constant.GET_ALBUM_AUDIO, reqAlbumAudio, ResponseAlbumAudio.class).flatMap(new TXZFunction<ResponseAlbumAudio, ObservableSource<List<Audio>>>() {
            @Override
            public ObservableSource<List<Audio>> handleData(ResponseAlbumAudio responseAlbumAudio) {
                if (responseAlbumAudio.getErrMeasure() != 0) {
                    return Observable.error(new ResponseErrorException(ResponseErrorException.ERROR_SERVER_MEASURE));
                } else {
                    return Observable.fromIterable(responseAlbumAudio.getArrAudio()).toList().toObservable();
                }
            }
        }).map(new Function<List<Audio>, List<Audio>>() {
            @Override
            public List<Audio> apply(List<Audio> audios) throws Exception {
                changeAudiosSourceAlbumId(audios, reqAlbumAudio.getId());
                return audios;
            }
        });
    }


    /**
     * @param audios
     * @param albumId 0 则不用添加
     */
    private void changeAudiosSourceAlbumId(List<Audio> audios, long albumId) {
        if (albumId != 0) {
            for (Audio audio : audios) {
                audio.setSrcAlbumId(albumId);
            }
        }
    }
}
