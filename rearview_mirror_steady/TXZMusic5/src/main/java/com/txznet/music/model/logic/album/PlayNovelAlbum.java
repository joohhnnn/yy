package com.txznet.music.model.logic.album;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.HistoryAlbumDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.BreakpointHelper;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.Operation;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 播放小说专辑的逻辑
 *
 * @author zackzhou
 * @date 2018/12/17,10:42
 */

public class PlayNovelAlbum extends AbstractPlayAlbum {
    private int index = 0;

    public PlayNovelAlbum(Album album) {
        super(album);
    }


    @Override
    public void onPlayAlbumInner(Operation operation) {
        /*
           从上次收听的音频开始往后请求
         */
        Disposable disposable = Observable.create((ObservableOnSubscribe<HistoryAlbum>) emitter -> {
            HistoryAlbumDao historyAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao();
            HistoryAlbum historyAlbum = historyAlbumDao.findByAlbum(album.sid, album.id);
            if (historyAlbum == null) {
                historyAlbum = HistoryAlbum.NONE;
            }
            emitter.onNext(historyAlbum);
            emitter.onComplete();
        }).map(historyAlbum -> {
            List<AudioV5> audioV5List;
            if (historyAlbum == null || historyAlbum.audio == null) {
                // 不存在收听历史
                audioV5List = TXZMusicDataSource.get().listAudios(album, null, null, Configuration.DefVal.PAGE_COUNT).blockingFirst();
                if (audioV5List.isEmpty()) {

                    Logger.w(TAG, "PlayNovelAlbum request album first audio");
                    audioV5List = TXZMusicDataSource.get().listAudios(album, null, null, Configuration.DefVal.PAGE_COUNT, true).blockingFirst();
                }
            } else {
                // 存在收听历史
                String audioId = String.format("%s-%s-%s-%s", "0", historyAlbum.audio.sid, historyAlbum.audio.id, historyAlbum.audio.albumId);
                audioV5List = TXZMusicDataSource.get().listAudios(album, historyAlbum.audio, audioId, Configuration.DefVal.PAGE_COUNT - 1).blockingFirst();
                if (audioV5List.isEmpty()) {
                    // 往下拉不到数据
                    Breakpoint breakpoint = BreakpointHelper.findBreakpointByAudio(historyAlbum.audio.sid, historyAlbum.audio.id);
                    if (breakpoint != null && breakpoint.position == 0) {
                        // 播放完毕
                        Logger.w(TAG, "PlayNovelAlbum request album first audio");
                        audioV5List = TXZMusicDataSource.get().listAudios(album, null, null, Configuration.DefVal.PAGE_COUNT, true).blockingFirst();
                        index = 0;
                        return audioV5List;
                    }
                }
                // 由于用audio去请求返回的列表中不包含这个audio，所以需要手动加进去
                if (!audioV5List.contains(historyAlbum.audio)) {
                    audioV5List.add(0, historyAlbum.audio);
                }
                // 如果返回条目数少于一页，尝试向前请求
                if (audioV5List.size() < Configuration.DefVal.PAGE_COUNT) {
                    Logger.w(TAG, "PlayNovelAlbum request album  prev page data");
                    audioId = String.format("%s-%s-%s-%s", "1", historyAlbum.audio.sid, historyAlbum.audio.id, historyAlbum.audio.albumId);
                    List<AudioV5> prevPage = TXZMusicDataSource.get().listAudios(album, historyAlbum.audio, audioId, Configuration.DefVal.PAGE_COUNT).blockingFirst();
                    audioV5List.addAll(0, prevPage);
                }
                index = audioV5List.indexOf(historyAlbum.audio);
            }
            return audioV5List;
        })
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.single())
                .subscribe(audioV5List -> {
                    if (audioV5List == null || audioV5List.isEmpty()) {
                        notifyAlbumEmpty(operation);
                    } else {
                        List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
                        AudioPlayer.getDefault().setQueue(audioList);
                        PlayHelper.get().useRadioPlayMode();
                        PlayHelper.get().play(audioList.get(index));
                    }
                }, throwable -> {
                    notifyRequestError(operation);
                    check2DoRetry(operation, throwable);
                });
        DisposableManager.get().add("onPlayAlbum", disposable);
    }
}
