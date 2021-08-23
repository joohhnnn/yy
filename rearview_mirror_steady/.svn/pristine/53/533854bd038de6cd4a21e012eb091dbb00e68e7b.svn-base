package com.txznet.music.playerModule.logic;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.PlayListData;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.utils.Utils;

import java.util.List;

public class SavePlaylistHelper {

    public static class SaveInfoListener implements PlayerInfoUpdateListener {

        @Override
        public void onPlayInfoUpdated(Audio audio, Album album) {
            if (album != null && !Utils.isSong(album.getSid())) {
                return;
            }
            AppLogic.runOnSlowGround(() -> {
                DBManager.getInstance().updatePlayListData(audio);
            });
        }

        @Override
        public void onProgressUpdated(long position, long duration) {

        }

        @Override
        public void onPlayerModeUpdated(int mode) {

        }

        @Override
        public void onPlayerStatusUpdated(int status) {

        }

        @Override
        public void onBufferProgressUpdated(List<LocalBuffer> buffers) {

        }

        @Override
        public void onFavourStatusUpdated(int favourState) {

        }
    }


    public static class PlayListSaveListener implements IPlayListChangedListener {

        @Override
        public void onPlayListChanged(List<Audio> audios) {
            AppLogic.runOnSlowGround(new Runnable() {
                @Override
                public void run() {
                    PlayListData data = new PlayListData();
                    data.setAudioStr(audios);
                    Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                    if (currentAudio != null) {
                        data.setAudio(currentAudio);
                    }
                    Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                    if (currentAlbum != null) {
                        data.setAlbum(currentAlbum);
                        //如果是电台则不保存
                        if (!Utils.isSong(currentAlbum.getSid())) {
                            return;
                        }
                    }
                    data.setDataOri(PlayInfoManager.getInstance().getCurrentScene());// 播放数据的来源
                    DBManager.getInstance().updatePlayListData(data);
                }
            });
        }
    }

}
