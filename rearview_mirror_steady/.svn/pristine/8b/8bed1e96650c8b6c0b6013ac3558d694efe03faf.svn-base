package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;

/**
 * Created by telenewbie on 2017/8/17.
 */

public class TXZTongTingManager {
    //##创建一个单例类##
    private volatile static TXZTongTingManager singleton;

    private TXZTongTingManager() {
    }

    public static TXZTongTingManager getInstance() {
        if (singleton == null) {
            synchronized (TXZTongTingManager.class) {
                if (singleton == null) {
                    singleton = new TXZTongTingManager();
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
        ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.MUSIC, "music.tongting.isPlaying", null);
        byte[] data = null;
        if (serviceData != null) {
            data = serviceData.getBytes();
        }
        if (data == null) {
            return false;
        }
        return Boolean.parseBoolean(new String(data));
    }

    /**
     * 播放状态获取接口（仅限电台之家）
     *
     * @return 是否正在播放
     */
    public boolean isBuffering() {
        ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.MUSIC, "music.tongting.isBuffering", null);
        byte[] data = null;
        if (serviceData != null) {
            data = serviceData.getBytes();
        }
        if (data == null) {
            return false;
        }
        return Boolean.parseBoolean(new String(data));
    }

    /**
     * 是否显示在界面
     *
     * @return
     */
    public boolean isShowUI() {
        ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.MUSIC, "music.tongting.isShowUI", null);
        byte[] data = null;
        if (serviceData != null) {
            data = serviceData.getBytes();
        }
        if (data == null) {
            return false;
        }
        return Boolean.parseBoolean(new String(data));
    }


    /**
     * 开始播放
     */
    @Deprecated
    public void play() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.start", null, null);
    }

    /**
     * 继续播放
     */
    public void continuePlay() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.continuePlay", null, null);
    }

    /**
     * 播放在线音乐
     */
    public void playOnlineMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.playOnlineMusic", null, null);
    }

    /**
     * 查询音乐榜下的歌曲
     */
    public void queryOnlineMusic(){
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.queryOnlineMusic", null, null);
    }

    /**
     * 暂停播放
     */
    public void pause() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.pause", null, null);
    }

    /**
     * 关闭音乐
     */
    public void exit() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.exit", null, null);
    }


    /**
     * 下一首
     */
    public void next() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.next", null, null);
    }

    /**
     * 上一首
     */
    public void prev() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.prev", null, null);
    }

    /**
     * 全部循环模式
     */
    public void switchModeLoopAll() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.switchModeLoopAll", null, null);
    }

    /**
     * 单曲循环模式
     */
    public void switchModeLoopOne() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.switchModeLoopOne", null, null);
    }

    /**
     * 随机播放模式
     */
    public void switchModeRandom() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.switchModeRandom", null, null);
    }

    /**
     * 切歌
     */
    public void switchSong() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.switchSong", null, null);
    }

    /**
     * 随便听听
     */
    public void playRandom() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.playRandom", null, null);
    }

    /**
     * 获取当前正在播放的音乐模型，没有播放返回null
     */
    public TXZMusicManager.MusicModel getCurrentMusicModel() {
        try {
            ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.MUSIC, "music.tongting.getCurrentMusicModel", null);
            byte[] data = null;
            if (serviceData != null) {
                data = serviceData.getBytes();
            }
            if (data == null) {
                return null;
            }

            return TXZMusicManager.MusicModel.fromString(new String(data));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 播放收藏歌曲
     */
    public void playFavourMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.playFavourMusic", null, null);
    }

    /**
     * 收藏当前音乐
     */
    public void favourMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.favourMusic", null, null);
    }

    /**
     * 取消收藏当前音乐
     */
    public void unfavourMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.unfavourMusic", null, null);
    }

    /**
     * 播放音乐
     */
    public void playMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tongting.play.inner", null, null);
    }

    /**
     * 播放电台
     */
    public void playRadio() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "audio.play", null, null);
    }
}
