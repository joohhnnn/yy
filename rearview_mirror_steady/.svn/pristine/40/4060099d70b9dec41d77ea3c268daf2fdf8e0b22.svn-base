package com.txznet.music.model.logic.album;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.BreakpointDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.util.DisposableManager;
import com.txznet.rxflux.Operation;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 播放新闻专辑的处理逻辑
 *
 * @author zackzhou
 * @date 2018/12/17,10:43
 */

public class PlayNewsAlbum extends AbstractPlayAlbum {

    public PlayNewsAlbum(Album album) {
        super(album);
    }

    @Override
    public void onPlayAlbumInner(Operation operation) {
        /*
         * 后台每次返回最新资源，
         * 针对乐听来源的新闻资源，后台特殊处理，一次返回100条目数据
         * 听过的会自动排到最底部。
         */
        Disposable disposable = TXZMusicDataSource.get().listAudios(album, null, null, Configuration.DefVal.PAGE_COUNT)
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.single())
                .subscribe(audioV5List -> {
                    if (audioV5List == null || audioV5List.isEmpty()) {
                        notifyAlbumEmpty(operation);
                    } else {
                        // 遍历本地记录，筛选出播放时长超过10s的音频数据，排到队尾
                        List<AudioV5> hasPlayList = new ArrayList<>();
                        BreakpointDao breakpointDao = DBUtils.getDatabase(GlobalContext.get()).getBreakpointDao();
                        for (AudioV5 audio : audioV5List) {
                            Breakpoint breakpoint = breakpointDao.findByAudio(audio.id, audio.sid);
                            if (breakpoint != null) {
                                hasPlayList.add(audio);
                            }
                        }
                        audioV5List.removeAll(hasPlayList);
                        audioV5List.addAll(hasPlayList);

                        List<Audio> audioList = AudioConverts.convert2List(audioV5List, AudioConverts::convert2MediaAudio);
                        AudioPlayer.getDefault().setQueue(audioList);
                        PlayHelper.get().useRadioPlayMode();
                        PlayHelper.get().play(audioList.get(0));
                    }
                }, throwable -> {
                    notifyRequestError(operation);
                    check2DoRetry(operation, throwable);
                });
        DisposableManager.get().add("onPlayAlbum", disposable);
    }
}
