package com.txznet.txz.component.audio.txz;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.media.base.AbsAudioTool;
import com.txznet.txz.component.media.base.MediaToolConstants;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.component.music.txz.MusicTongTing;
import com.txznet.txz.ui.win.record.RecorderWin;

/**
 * 同听的电台版本适配工具
 *
 * Created by J on 2018/5/4.
 */

public class AudioTongTing extends AbsAudioTool {

    private MediaModel mPlayingModel;
    private PLAYER_STATUS mStatus = PLAYER_STATUS.IDLE;

    public boolean updateStatus(PLAYER_STATUS status) {
        boolean statusChanged = (status == mStatus);
        mStatus = status;
        notifyPlayerStatusChange(status);

        return statusChanged;
    }

    @Override
    public void cancelRequest() {
        MusicTongTing.getInstance().cancelRequest();
    }

    @Override
    public String getPackageName() {
        return ServiceManager.MUSIC;
    }

    @Override
    public int getPriority() {
        return MediaToolConstants.PRIORITY_TONGTING;
    }

    @Override
    public void open(final boolean play) {
        if (play) {
            //新版本走同听，旧版本
            if (supportAudioPlay()) {
                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "audio.open.play", null, null);
            } else {
                //兼容，缓冲版本，其实已经支持打开电台指令的版本
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("target","audio");//目标地址

                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.open.play",
                        jsonBuilder.toBytes(), null);
            }
        } else {
            if (supportAudioPlay()) {
                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "audio.open", null, null);
            } else {
                //兼容，缓冲版本，其实已经支持打开电台指令的版本
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("target","audio");
                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.open",
                        jsonBuilder.toBytes(), null);
            }
        }
    }

    public void updatePlayingModel(MediaModel model) {
        this.mPlayingModel = model;
    }

    /**
     * 440支持播放打开的操作
     *
     * @return
     */
    public boolean supportAudioPlay() {
        return MusicTongTing.getInstance().getMusicVersionCode() >= 440;
    }

    @Override
    public void setSearchTimeout(final long timeout) {

    }

    @Override
    public void setShowSearchResult(final boolean show) {

    }

    @Override
    public void exit() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.exit", null, null);
        RecorderWin.close();
    }

    @Override
    public void play(final MediaModel model) {
        MusicTongTing.getInstance().play(model);
    }

    @Override
    public void stop() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.pause", null, null);
        RecorderWin.close();
    }

    @Override
    public void pause() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.pause", null, null);
        RecorderWin.close();
    }

    @Override
    public void continuePlay() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "audio.play", null, null);
        RecorderWin.close();
    }

    @Override
    public void next() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.next", null, null);
        RecorderWin.close();
    }

    @Override
    public void prev() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.prev", null, null);
        RecorderWin.close();
    }

    @Override
    public void switchLoopMode(final LOOP_MODE mode) {
        switch (mode) {
            case LIST_LOOP:
            case SEQUENTIAL:
                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                        "music.switchModeLoopAll", null, null);
                break;

            case SINGLE_LOOP:
                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                        "music.switchModeLoopOne", null, null);
                break;

            case SHUFFLE:
                ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                        "music.switchModeRandom", null, null);
                break;
        }
        RecorderWin.close();
    }

    @Override
    public void collect() {
        MusicTongTing.getInstance().collect();
    }

    @Override
    public void unCollect() {
        MusicTongTing.getInstance().unCollect();
    }

    @Override
    public void playCollection() {
        MusicTongTing.getInstance().playCollection();
    }

    @Override
    public void subscribe() {
        MusicTongTing.getInstance().subscribe();
    }

    @Override
    public void unSubscribe() {
        MusicTongTing.getInstance().unSubscribe();
    }

    @Override
    public void playSubscribe() {
        MusicTongTing.getInstance().playSubscribe();
    }

    @Override
    public PLAYER_STATUS getStatus() {
        if (MusicTongTing.getInstance().getMusicVersionCode() < 440) {
            return MusicTongTing.getInstance().getStatus();
        }

        return mStatus;
    }

    @Override
    public MediaModel getPlayingModel() {
        return mPlayingModel;
    }

    @Override
    public boolean supportLoopMode(final LOOP_MODE mode) {
        return true;
    }

    @Override
    public boolean supportCollect() {
        return MusicTongTing.getInstance().supportCollect();
    }

    @Override
    public boolean supportUnCollect() {
        return MusicTongTing.getInstance().supportUnCollect();
    }

    @Override
    public boolean supportPlayCollection() {
        return MusicTongTing.getInstance().supportPlayCollection();
    }

    @Override
    public boolean supportSubscribe() {
        return MusicTongTing.getInstance().supportSubscribe();
    }

    @Override
    public boolean supportUnSubscribe() {
        return MusicTongTing.getInstance().supportUnSubscribe();
    }

    @Override
    public boolean supportPlaySubscribe() {
        return MusicTongTing.getInstance().supportPlaySubscribe();
    }

    @Override
    public boolean supportSearch() {
        return true;
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public boolean hasPrev() {
        return true;
    }

    @Override
    public boolean interceptRecordWinControl(MEDIA_TOOL_OP op) {
        return MusicTongTing.getInstance().interceptRecordWinControl(op);
    }

    @Override
    public boolean equals(final Object obj) {
        if (MusicTongTing.getInstance().getMusicVersionCode() < 440) {
            return (obj instanceof MusicTongTing) || super.equals(obj);
        }

        return super.equals(obj);
    }

    //----------- single instance -----------
    private static volatile AudioTongTing sInstance;

    public static AudioTongTing getInstance() {
        if (null == sInstance) {
            synchronized (AudioTongTing.class) {
                if (null == sInstance) {
                    sInstance = new AudioTongTing();
                }
            }
        }

        return sInstance;
    }

    private AudioTongTing() {

    }
    //----------- single instance -----------
}
