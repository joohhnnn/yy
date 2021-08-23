package com.txznet.music.model.logic.album;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.util.DisposableManager;
import com.txznet.rxflux.Operation;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 播放音乐专辑的逻辑
 *
 * @author zackzhou
 * @date 2018/12/17,10:34
 */

public class PlayMusicAlbum extends AbstractPlayAlbum {

    public PlayMusicAlbum(Album album) {
        super(album);
    }

    @Override
    public void onPlayAlbumInner(Operation operation) {
        /*
         * 声控操作从随即一首开始播放
         * 其余操作从第一首开始播放
         */
        Disposable disposable = TXZMusicDataSource.get().listAudios(album, null, null, Configuration.DefVal.PAGE_COUNT_MUSIC)
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.single())
                .subscribe(audioV5List -> {
                    if (audioV5List == null || audioV5List.isEmpty()) {
                        notifyAlbumEmpty(operation);
                    } else {
                        List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
                        AudioPlayer.getDefault().setQueue(audioList);
                        PlayHelper.get().resetLastMusicPlayMode();
//                        if (Operation.SOUND == operation) {
//                            PlayHelper.get().play(audioList.get((int) (Math.random() * audioList.size())));
//                        } else {
                        PlayHelper.get().play(audioList.get(0));
//                        }
                    }
                }, throwable -> {
                    notifyRequestError(operation);
                    check2DoRetry(operation, throwable);
                });
        DisposableManager.get().add("onPlayAlbum", disposable);
    }

}
