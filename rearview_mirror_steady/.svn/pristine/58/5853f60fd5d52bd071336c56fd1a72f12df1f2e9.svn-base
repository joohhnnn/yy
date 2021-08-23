package com.txznet.music.model.logic;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.music.Constant;
import com.txznet.music.data.entity.Album;
import com.txznet.music.model.logic.album.AbstractPlayAlbum;
import com.txznet.music.model.logic.album.PlayAiPushAlbum;
import com.txznet.music.model.logic.album.PlayMusicAlbum;
import com.txznet.music.model.logic.album.PlayNewsAlbum;
import com.txznet.music.model.logic.album.PlayNovelAlbum;
import com.txznet.music.model.logic.album.PlayOtherAlbum;
import com.txznet.music.model.logic.queue.AiPushQueueItemPicker;
import com.txznet.music.model.logic.queue.MusicQueueItemPicker;
import com.txznet.music.model.logic.queue.NewsQueueItemPicker;
import com.txznet.music.model.logic.queue.NovelQueueItemPicker;
import com.txznet.music.model.logic.queue.OtherQueueItemPicker;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.Logger;

/**
 * 播放专辑的逻辑工厂
 *
 * @author zackzhou
 * @date 2018/12/17,10:29
 */

public class PlayLogicFactory {

    private static PlayLogicFactory sInstance = new PlayLogicFactory();

    public static PlayLogicFactory get() {
        return sInstance;
    }

    /**
     * 根据播放的专辑获取对应的处理逻辑
     */
    public AbstractPlayAlbum getPlayAlbumLogic(Album album) {
        /*
         * 根据产品定义的逻辑，分类音乐、小说和非小说三类，其中非小说还包括新闻和其他
         */
        Logger.d(Constant.LOG_TAG_LOGIC, "playAlbum album=" + (album == null ? "" : album));
        if (album.id == 1000001 && album.sid == 100) {
            return new PlayAiPushAlbum(album);
        } else if (AlbumUtils.isMusic(album)) {
            return new PlayMusicAlbum(album);
        } else if (AlbumUtils.isNovel(album)) {
            return new PlayNovelAlbum(album);
        } else if (AlbumUtils.isNews(album)) {
            return new PlayNewsAlbum(album);
        } else {
            return new PlayOtherAlbum(album);
        }
    }

    /**
     * 根据播放的专辑获取对应的音频上下首处理逻辑
     */
    public AudioPlayer.AudioPlayerQueueInterceptor getQueueItemPicker(Album album) {
        if (album != null && album.id == 1000001 && album.sid == 100) {
            return new AiPushQueueItemPicker(album);
        } else if (album == null || AlbumUtils.isMusic(album)) {
            return new MusicQueueItemPicker(album);
        } else if (AlbumUtils.isNovel(album)) {
            return new NovelQueueItemPicker(album);
        } else if (AlbumUtils.isNews(album)) {
            return new NewsQueueItemPicker(album);
        } else {
            return new OtherQueueItemPicker(album);
        }
    }
}
