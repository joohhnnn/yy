package com.txznet.music.net;

import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.net.response.ResponseAlbumAudio;
import com.txznet.music.baseModule.bean.Error;

import java.util.List;

/**
 * Created by brainBear on 2017/7/27.
 */

public abstract class RequestAudioCallBack extends RequestCallBack<ResponseAlbumAudio> {

    private Album mReqAlbum;

    public RequestAudioCallBack(Album reqAlbum) {
        super(ResponseAlbumAudio.class);
        this.mReqAlbum = reqAlbum;
    }


    public abstract void onResponse(List<Audio> audios, Album album, ResponseAlbumAudio responseAlbumAudio);


    @Override
    public void onResponse(ResponseAlbumAudio data) {
        AlbumEngine.getInstance().handleResponseAlbumAudios(data, this, mReqAlbum);
    }

    @Override
    public void onError(String cmd, Error error) {
        AlbumEngine.getInstance().handleAlbumAudioError(error.getErrorCode());
    }
}
