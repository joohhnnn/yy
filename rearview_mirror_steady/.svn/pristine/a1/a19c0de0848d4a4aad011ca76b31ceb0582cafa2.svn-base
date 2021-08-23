package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;

/**
 * Created by telenewbie on 2017/6/7.
 * 单独用于控制同听的接口
 */

public class TXZOwnerMusicManager {
    private static final String INTENT_PLAY_ACTION = "com.txznet.music.play";
    private static final String INTENT_PAUSE_ACTION = "com.txznet.music.pause";
    private static final String INTENT_EXIT_ACTION = "com.txznet.music.action.exit";
    private static final String INTENT_NEXT_ACTION = "com.txznet.music.next";
    private static final String INTENT_PREV_ACTION = "com.txznet.music.prev";
    private static final String INTENT_SWITCHMODELOOPALL_ACTION = "com.txznet.music.action.switchModeLoopAll";
    private static final String INTENT_SWITCHMODELOOPONE_ACTION = "com.txznet.music.action.switchModeLoopOne";
    private static final String INTENT_SWITCHMODERANDOM_ACTION = "com.txznet.music.action.switchModeRandom";
    private static final String INTENT_PLAYFAVOURMUSIC_ACTION = "com.txznet.music.action.playFavourMusic";
    private static final String INTENT_FAVOURMUSIC_ACTION = "com.txznet.music.action.favourMusic";
    private static final String INTENT_UNFAVOURMUSIC_ACTION = "com.txznet.music.action.unfavourMusic";
    private static final String INTENT_SEARCHMUSIC_ACTION = "com.txznet.music.search.audio";

    //##创建一个单例类##
    private volatile static TXZOwnerMusicManager singleton;

    private TXZOwnerMusicManager() {
    }

    public static TXZOwnerMusicManager getInstance() {
        if (singleton == null) {
            synchronized (TXZOwnerMusicManager.class) {
                if (singleton == null) {
                    singleton = new TXZOwnerMusicManager();
                }
            }
        }
        return singleton;
    }


    /**
     * 播放状态获取接口
     *
     * @return 是否正在播放
     */
    public boolean isPlaying() {
        byte[] data = ServiceManager.getInstance().sendTXZInvokeSync("txz.music.tongting.isPlaying", null);
        if (data == null)
            return false;
        return Boolean.parseBoolean(new String(data));
    }

    /**
     * 播放状态获取接口（仅限电台之家）
     *
     * @return 是否正在播放
     */
    public boolean isBuffering() {
        byte[] data = ServiceManager.getInstance().sendTXZInvokeSync("txz.music.tongting.isBuffering", null);
        if (data == null)
            return false;
        return Boolean.parseBoolean(new String(data));
    }

    /**
     * 是否显示在界面
     * @return
     */
    public  boolean isShowUI(){
        byte[] data = ServiceManager.getInstance().sendTXZInvokeSync("txz.music.tongting.isShowUI", null);
        if (data == null)
            return false;
        return Boolean.parseBoolean(new String(data));
    }


    /**
     * 开始播放
     *
     */
    public void play() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.play", null, null);
    }

    /**
     * 继续播放
     *
     */
    public void continuePlay() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.play.extra", null, null);
    }


    /**
     * 暂停播放
     */
    public void pause() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.pause", null, null);
    }

    /**
     * 关闭音乐
     */
    public void exit() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.exit", null, null);
    }


    /**
     * 下一首
     */
    public void next() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.next", null, null);
    }

    /**
     * 上一首
     */
    public void prev() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.prev", null, null);
    }

    /**
     * 全部循环模式
     */
    public void switchModeLoopAll() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.switchModeLoopAll", null, null);
    }

    /**
     * 单曲循环模式
     */
    public void switchModeLoopOne() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.switchModeLoopOne", null, null);
    }

    /**
     * 随机播放模式
     */
    public void switchModeRandom() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.switchModeRandom", null, null);
    }

    /**
     * 切歌
     */
    public void switchSong() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.switchSong", null, null);
    }

    /**
     * 随便听听
     */
    public void playRandom() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.playRandom", null, null);
    }

    /**
     * 获取当前正在播放的音乐模型，没有播放返回null
     */
    public TXZMusicManager.MusicModel getCurrentMusicModel() {
        try {
            byte[] data = ServiceManager.getInstance().sendTXZInvokeSync("txz.music.tongting.getCurrentMusicModel", null);
            if (data == null)
                return null;
            return TXZMusicManager.MusicModel.fromString(new String(data));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 播放收藏歌曲
     */
    public void playFavourMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.playFavourMusic", null, null);
    }

    /**
     * 收藏当前音乐
     */
    public void favourMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.favourMusic", null, null);
    }

    /**
     * 取消收藏当前音乐
     */
    public void unfavourMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.tongting.unfavourMusic", null, null);
    }

}
