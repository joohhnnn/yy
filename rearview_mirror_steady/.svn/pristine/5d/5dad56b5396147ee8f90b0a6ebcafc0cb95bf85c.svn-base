package com.txznet.music.utils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author telenewbie
 * @version 创建时间：2016年4月21日 下午5:16:37
 */
public class UpdateToCoreUtil {

    public static void updateMusicModel() {
        // 同步
        List<Audio> resultAudios = DBManager.getInstance().findAllLocalAudios();
        updateMusicModel(resultAudios);
    }

    /**
     * 上报本地歌曲到Core
     */
    public static void updateMusicModel(List<Audio> audios) {
        List<MusicModel> musics = new ArrayList<TXZMusicManager.MusicModel>();
        for (Audio audio : audios) {
            MusicModel model = new MusicModel();
            model.setTitle(audio.getName());
            model.setArtist(CollectionUtils.toStrings(audio.getArrArtistName()));
            model.setAlbum(audio.getAlbumName());
            model.setPath(audio.getStrDownloadUrl());
            musics.add(model);
        }
        TXZMusicManager.getInstance().syncExMuicListToCore(musics);

        //存文件
        FileUtils.getFile(MusicModel.collecionToString(musics).getBytes(), com.txznet.txz.util.StorageUtil.getInnerSDCardPath()+"/txz/audio", "local_audio.cfg");
    }
}
