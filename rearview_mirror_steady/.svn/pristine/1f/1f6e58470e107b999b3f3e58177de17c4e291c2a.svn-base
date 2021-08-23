package com.txznet.music.utils;

import android.content.Intent;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.playerModule.logic.IPlayerStateListener;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.sdk.TXZMusicManager.MusicModel;

import java.util.List;

/**
 * 向服务器上报当前的数据
 *
 * @author ASUS User
 */
public class SyncCoreData implements IPlayerStateListener {
    // 同步所有状态
    public static void syncCurStatusFullStyle() {
        // 请求端口
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music.inner.syncMusicList", null, null);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music.isNewVersion", new String("true").getBytes(), null);
        // TXZMusicManager.getInstance()
        // .setMusicTool(MusicToolType.MUSIC_TOOL_TXZ);
        syncCurPlayerStatus();
        syncCurMusicModel();
    }

    // 同步当前音乐模型
    public static void syncCurMusicModel() {
        Audio audio = PlayEngineFactory.getEngine().getCurrentAudio();
        LogUtil.logd("syncCoreData current Audio is :" + audio);
        MusicModel data = AudioToMusicModel(audio);
        if (audio == null || Utils.isSong(audio.getSid())) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    "txz.music.inner.musicModel",
                    (data != null ? JsonHelper.toJson(data) : "").getBytes(), null);
        } else {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    "txz.music.inner.audioModel",
                    (data != null ? JsonHelper.toJson(data) : "").getBytes(), null);
        }
        // 发送广播
        Intent intent = new Intent("com.txznet.music.action.MUSIC_MODEL_CHANGE");
        intent.putExtra("audio", audio);
        AppLogic.getApp().sendBroadcast(intent);
    }

    public static void syncNextMusicModel(Audio nextAudio) {
        if (null == nextAudio)
            return;
        LogUtil.logd("syncCoreData next audio is :" + nextAudio.getName());

        MusicModel data = AudioToMusicModel(nextAudio);

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music.inner.audioModel.next",
                (data != null ? JsonHelper.toJson(data) : "").getBytes(), null);

    }

    // 同步当前播放状态
    public static void syncCurPlayerStatus() {
        boolean isPlaying = PlayEngineFactory.getEngine().isPlaying();
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music.inner.isPlaying", ("" + isPlaying).getBytes(), null);
        LogUtil.logd("syncCoreData isPlaying is :" + isPlaying);
    }


    // 同步当前播放状态
    public static void syncCurPlayerStatus(boolean isPlaying) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music.inner.isPlaying", ("" + isPlaying).getBytes(), null);
        LogUtil.logd("syncCoreData isPlaying is :" + isPlaying);
    }


    public static final int STATE_ON_IDLE = 0;
    public static final int STATE_ON_BUFFER = 1;
    public static final int STATE_ON_PLAYING = 2;
    public static final int STATE_ON_PAUSED = 3;
    public static final int STATE_ON_END = 4;
    public static final int STATE_ON_FAILED = 5;

    /**
     * 根据现在的广播发送广播
     */
    public static void sendStatusByCurrent() {
        // 直接发送广播断点
        int state = PlayEngineFactory.getEngine().getState();
        switch (state){
            case PlayerInfo.PLAYER_STATUS_BUFFER:
                sendStatusBroadcast(STATE_ON_BUFFER);
                break;
            case PlayerInfo.PLAYER_STATUS_PAUSE:
                sendStatusBroadcast(STATE_ON_PAUSED);
                break;
            case PlayerInfo.PLAYER_STATUS_RELEASE:
                sendStatusBroadcast(STATE_ON_END);
                break;
            case PlayerInfo.PLAYER_STATUS_PLAYING:
                sendStatusBroadcast(STATE_ON_PLAYING);
                break;
        }
    }

    private static void sendStatusBroadcast(int status) {
        // 桌面组件调用该广播
        Intent intent = new Intent("com.txznet.music.action.PLAY_STATUS_CHANGE");
        intent.putExtra("status", status);
        GlobalContext.get().sendBroadcast(intent);
    }

    // 同步当前播放状态
    public static void syncCurPlayerBufferingStatus() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.inner.isBuffering",
                ("" + (PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_BUFFER)).getBytes(), null);
    }

    private static MusicModel AudioToMusicModel(Audio audio) {
        if (audio == null) {
            return null;
        }
        MusicModel data = new MusicModel();
        try {
            data.setTitle(audio.getName());
            data.setAlbum(audio.getAlbumName());
            data.setArtist(audio.getArrArtistName().toArray(
                    new String[audio.getArrArtistName().size()]));
        } catch (Exception e) {
        }
        return data;
    }

    @Override
    public void onIdle(Audio audio) {
        sendStatusBroadcast(STATE_ON_IDLE);
    }

    @Override
    public void onPlayerPreparing(Audio audio) {
        syncCurMusicModel();
    }

    @Override
    public void onPlayerPrepareStart(Audio audio) {

    }

    @Override
    public void onPlayerPlaying(Audio audio) {
        syncCurPlayerStatus(true);
        sendStatusBroadcast(STATE_ON_PLAYING);
    }

    @Override
    public void onPlayerPaused(Audio audio) {
//        syncCurPlayerStatus(false);
        sendStatusBroadcast(STATE_ON_PAUSED);
    }

    @Override
    public void onProgress(Audio audio, long position, long duration) {

    }

    @Override
    public void onBufferProgress(Audio audio, List<LocalBuffer> buffers) {

    }

    @Override
    public void onPlayerFailed(Audio audio, Error error) {
        sendStatusBroadcast(STATE_ON_FAILED);
    }

    @Override
    public void onPlayerEnd(Audio audio) {
        sendStatusBroadcast(STATE_ON_END);
    }

    @Override
    public void onSeekStart(Audio audio) {

    }

    @Override
    public void onSeekComplete(Audio audio,long seekTime) {

    }

    @Override
    public void onBufferingStart(Audio audio) {
        sendStatusBroadcast(STATE_ON_BUFFER);
    }

    @Override
    public void onBufferingEnd(Audio audio) {

    }
}
