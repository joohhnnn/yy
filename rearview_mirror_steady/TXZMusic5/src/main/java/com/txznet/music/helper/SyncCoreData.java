package com.txznet.music.helper;

import android.content.Intent;

import com.txznet.audio.player.IMediaPlayer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.FileUtils;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 向服务器上报当前的数据
 *
 * @author ASUS User
 */
public class SyncCoreData {
    // 同步所有状态
//    public static void syncCurStatusFullStyle() {
//        // 请求端口
//        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
//                "txz.music.inner.syncMusicList", null, null);
//        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
//                "txz.music.isNewVersion", new String("true").getBytes(), null);
//        syncCurPlayerStatus();
//        syncCurMusicModel();
//    }
    private static MusicModel AudioToMusicModel(AudioV5 audio) {
        if (audio == null) {
            return null;
        }
        MusicModel data = new MusicModel();
        try {
            data.setTitle(audio.name);
            data.setAlbum(audio.albumName);
            data.setArtist(audio.artist);
        } catch (Exception e) {
        }
        return data;
    }

    // 同步当前音乐模型
    public static void syncCurMusicModel(AudioV5 audio) {
        AppLogic.runOnSlowGround(() -> {
            Logger.d(Constant.LOG_TAG_LOGIC, "syncCoreData current Audio is :" + audio);
            MusicModel data = AudioToMusicModel(audio);
            if (audio == null || AudioUtils.isSong(audio.sid)) {
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                        "txz.music.inner.musicModel",
                        (data != null ? JsonHelper.toJson(data) : "").getBytes(), null);
            } else {
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                        "txz.music.inner.audioModel",
                        (data != null ? JsonHelper.toJson(data) : "").getBytes(), null);
            }
        });
    }


    private static Boolean mLastPlayingStatus;

    // 同步当前播放状态
    public static void syncCurPlayerStatus(boolean isPlaying) {
        if (mLastPlayingStatus != null && mLastPlayingStatus == isPlaying) {
            return;
        }
        mLastPlayingStatus = isPlaying;
        AppLogic.runOnSlowGround(() -> {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    "txz.music.inner.isPlaying", ("" + isPlaying).getBytes(), null);
            Logger.d(Constant.LOG_TAG_LOGIC, "syncCoreData isPlaying is :" + isPlaying);
        });
    }

    public static final int STATE_ON_IDLE = 0;
    public static final int STATE_ON_BUFFER = 1;
    public static final int STATE_ON_PLAYING = 2;
    public static final int STATE_ON_PAUSED = 3;
    public static final int STATE_ON_END = 4;
    public static final int STATE_ON_FAILED = 5;

    /**
     * 根据现在的状态发送广播
     */
    public static void sendStatusByCurrent(int state) {
        // 直接发送广播断点
        switch (state) {
            case IMediaPlayer.STATE_ON_BUFFERING:
                sendStatusBroadcast(STATE_ON_BUFFER);
                break;
            case IMediaPlayer.STATE_ON_PAUSED:
                sendStatusBroadcast(STATE_ON_PAUSED);
                break;
            case IMediaPlayer.STATE_ON_STOPPED:
                sendStatusBroadcast(STATE_ON_END);
                break;
            case IMediaPlayer.STATE_ON_PLAYING:
                sendStatusBroadcast(STATE_ON_PLAYING);
                break;
            default:
                break;
        }
    }

    private static void sendStatusBroadcast(int status) {
        // 桌面组件调用该广播
        Intent intent = new Intent("com.txznet.music.action.PLAY_STATUS_CHANGE");
        intent.putExtra("status", status);
        GlobalContext.get().sendBroadcast(intent);
    }


    /**
     * 上报本地歌曲到Core
     */
    public static void updateMusicModel(List<? extends AudioV5> audios) {
        AppLogic.runOnSlowGround(() -> {
            List<? extends AudioV5> audioList = new ArrayList<>(audios);
            Logger.d(Constant.LOG_TAG_LOGIC, "updateMusicModel audios=" + audioList.size());
            if (CollectionUtils.isNotEmpty(audioList)) {
                List<MusicModel> musics = new ArrayList<>();
                for (AudioV5 audio : audioList) {
                    MusicModel model = new MusicModel();
                    model.setTitle(audio.name);
                    model.setArtist(audio.artist);
                    model.setAlbum(audio.albumName);
                    model.setPath(audio.sourceUrl);
                    // FIXME: 2019/1/2 播放路径必须本地存在的路径，不能用内部协议，如txz://
                    if (audio.sourceUrl != null && (audio.sourceUrl.startsWith(TXZUri.PREFIX_V1) || audio.sourceUrl.startsWith(TXZUri.PREFIX_V2))) {
                        File file = AudioUtils.getAudioTMDFile(audio);
                        if (file != null) {
                            model.setPath(file.getAbsolutePath());
                        }
                    }
                    musics.add(model);
                }
                TXZMusicManager.getInstance().syncExMuicListToCore(musics);
                //存文件
                FileUtils.getFile(MusicModel.collecionToString(musics).getBytes(), com.txznet.txz.util.StorageUtil.getInnerSDCardPath() + "/txz/audio", "local_audio.cfg");
            }
        });
    }

    /**
     * 上报本地音乐
     */
    public static void updateLocalMusic() {
        AppLogic.runOnSlowGround(() -> {
            List<LocalAudio> localAudioList = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao().listAll();
            SyncCoreData.updateMusicModel(AudioConverts.convert2List(localAudioList, AudioConverts::convertLocalAudio2Audio));
        });
    }

    /**
     * 上报本地音乐
     */
    public static void updateLocalMusic(AudioV5 audioV5) {
        AppLogic.runOnSlowGround(() -> {
            SyncCoreData.updateMusicModel(Collections.singletonList(audioV5));
        });
    }
}
