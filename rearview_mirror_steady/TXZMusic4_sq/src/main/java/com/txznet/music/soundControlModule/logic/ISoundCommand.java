package com.txznet.music.soundControlModule.logic;


import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;

/**
 * Created by telenewbie on 2016/12/23.
 */

public interface ISoundCommand extends ISound {


    /**
     * 播放电台
     */
    public byte[] playAudio();

    /**
     * 播放音乐
     */
    public byte[] playMusic();

    /**
     * 随便听听
     */
    public byte[] playRecommandMusic();

    /**
     * 暂停
     */
    public byte[] pause();

    /**
     * 播放
     */
    public byte[] play();

    public byte[] next();

    public byte[] prev();

    /**
     * 退出播放器
     */
    public  byte[]  exit();

    /**
     * 切换响应的播放模式
     */
    public byte[] changeSingleMode(@PlayerInfo.PlayerMode int mode);

    /**
     * 打开播放器
     */
    public byte[] open();


    public byte[] favour(byte[] objects);

    public byte[] playfavour();

    /**
     * 不喜欢该歌曲
     * @return
     */
    public  byte[] hateAudio();


}
