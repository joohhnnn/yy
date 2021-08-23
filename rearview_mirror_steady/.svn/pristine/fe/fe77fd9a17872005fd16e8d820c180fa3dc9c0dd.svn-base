package com.txznet.music.util;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.helper.PlayHelper;

/**
 * 播放场景工具
 *
 * @author zackzhou
 * @date 2019/1/7,18:57
 */

public class PlaySceneUtils {

    private PlaySceneUtils() {

    }

    /**
     * 是否音乐播放场景
     */
    public static boolean isMusicScene() {
        PlayScene scene = PlayHelper.get().getCurrPlayScene();
        if (scene == null
                || PlayScene.IDLE == scene
                || PlayScene.LOCAL_MUSIC == scene
                || PlayScene.HISTORY_MUSIC == scene
                || PlayScene.FAVOUR_MUSIC == scene
                || PlayScene.WECHAT_PUSH == scene) {
            return true;
        }
        if (AudioPlayer.getDefault().getQueue().getFirstItem() != null && AudioUtils.isSong(AudioPlayer.getDefault().getQueue().getFirstItem().sid)) {
            return true;
        }
        if (PlayHelper.get().getCurrAudio() != null) {
            return AudioUtils.isSong(PlayHelper.get().getCurrAudio().sid);
        }
        Album album = PlayHelper.get().getCurrAlbum();
        return album != null && AlbumUtils.isMusic(album);
    }

    /**
     * 是否电台播放场景
     */
    public static boolean isRadioScene() {
        return isRadioScene(PlayHelper.get().getCurrPlayScene(), PlayHelper.get().getCurrAlbum());
    }

    /**
     * 是否电台播放场景
     */
    public static boolean isRadioScene(PlayScene scene, Album album) {
        if (PlayScene.HISTORY_ALBUM == scene
                || PlayScene.FAVOUR_ALBUM == scene) {
            return true;
        }
        return album != null && !AlbumUtils.isMusic(album);
    }

    /**
     * 当前是否AI电台场景
     */
    public static boolean isAiScene() {
        Album album = PlayHelper.get().getCurrAlbum();
        if (album != null && album.sid == 100 && album.id == 1000001) {
            return true;
        }
        return PlayScene.AI_RADIO == PlayHelper.get().getCurrPlayScene();
    }
}
