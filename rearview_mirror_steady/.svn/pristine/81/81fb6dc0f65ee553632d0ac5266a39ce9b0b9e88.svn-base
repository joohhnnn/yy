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
import com.txznet.music.data.entity.PlayScene;
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
 * 播放其他电台的处理逻辑
 *
 * @author zackzhou
 * @date 2018/12/17,10:42
 */

public class PlayOtherAlbum extends AbstractPlayAlbum {
    private int index = 0;

    public PlayOtherAlbum(Album album) {
        super(album);
    }

    @Override
    public void onPlayAlbumInner(Operation operation) {
        /*
            距离上次收听有更新，则从最新一期开始播放
            否则从上次播放处开始播放
         */
        Disposable disposable = Observable.create((ObservableOnSubscribe<List<AudioV5>>) emitter -> {
            HistoryAlbumDao historyAlbumDao = DBUtils.getDatabase(GlobalContext.get()).getHistoryAlbumDao();
            HistoryAlbum historyAlbum = historyAlbumDao.findByAlbum(album.sid, album.id);
            boolean hasAlbumUpdate = false;
            // 电台历史场景下，从上次播放处开始播放
            if (PlayScene.HISTORY_ALBUM != PlayHelper.get().getCurrPlayScene()) {
                // 尝试请求专辑下的第一条数据
                List<AudioV5> topList = TXZMusicDataSource.get().listAudios(album, null, null, 1).blockingFirst();
                if (topList != null && !topList.isEmpty()) {
                    AudioV5 firstItem = topList.get(0);
                    Logger.d(TAG, "album= " + album.name + ", newest audio=" + firstItem + ", local newest audio=" + (historyAlbum == null ? "null" : historyAlbum.newestAudio));
                    // 保存数据库
                    if (historyAlbum != null && !firstItem.equals(historyAlbum.newestAudio)) {
                        if (historyAlbum.newestAudio != null) {
                            hasAlbumUpdate = true;
                        }
                        historyAlbum.newestAudio = firstItem;
                        historyAlbumDao.saveOrUpdate(historyAlbum);
                    }
                }
            }
            List<AudioV5> audioV5List;
            if (hasAlbumUpdate || historyAlbum == null || historyAlbum.audio == null) {
                // 距离上次收听的时候，专辑有更新
                audioV5List = TXZMusicDataSource.get().listAudios(album, null, null, Configuration.DefVal.PAGE_COUNT).blockingFirst();
            } else {
                // 距离上次收听的时候，专辑没有更新
                String audioId = String.format("0-%s-%s", historyAlbum.audio.sid, historyAlbum.audio.id);
                audioV5List = TXZMusicDataSource.get().listAudios(album, historyAlbum.audio, audioId, Configuration.DefVal.PAGE_COUNT - 1).blockingFirst();
                if (audioV5List.isEmpty()) {
                    Breakpoint breakpoint = BreakpointHelper.findBreakpointByAudio(historyAlbum.audio.sid, historyAlbum.audio.id);
                    if (breakpoint != null && breakpoint.position == 0) {
                        // 播放完毕
                        Logger.w(TAG, "PlayOtherAlbum request album first audio");
                        audioV5List = TXZMusicDataSource.get().listAudios(album, historyAlbum.audio, null, Configuration.DefVal.PAGE_COUNT, true).blockingFirst();
                        index = 0;
                        emitter.onNext(audioV5List);
                        emitter.onComplete();
                        return;
                    }
                }
                //由于用audio去请求返回的列表中不包含这个audio，所以需要手动加进去
                if (!audioV5List.contains(historyAlbum.audio)) {
                    audioV5List.add(0, historyAlbum.audio);
                }
                // 如果返回条目数少于一页，尝试向前请求
                if (audioV5List.size() < Configuration.DefVal.PAGE_COUNT) {
                    Logger.w(TAG, "PlayOtherAlbum request album  prev page data");
                    audioId = String.format("%s-%s-%s-%s", "1", historyAlbum.audio.sid, historyAlbum.audio.id, historyAlbum.audio.albumId);
                    List<AudioV5> prevPage = TXZMusicDataSource.get().listAudios(album, historyAlbum.audio, audioId, Configuration.DefVal.PAGE_COUNT).blockingFirst();
                    audioV5List.addAll(0, prevPage);
                }
                index = audioV5List.indexOf(historyAlbum.audio);
            }
            emitter.onNext(audioV5List);
            emitter.onComplete();
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
