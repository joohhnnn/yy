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
     * //["打开音乐", "播放音乐", "听音乐", "播音乐", "播放歌曲", "听歌曲", "播歌曲", "播歌", "听歌", "我要听音乐", "随便听听", "随意听听", "随便来首歌", "随便来首音乐", "随便来点歌", "你随便唱吧", "好听的歌有哪些", "放首歌听", "放首歌听听", "放首歌"],
     */
    public byte[] playMusic(byte[] bytes);

//    /**
//     * 随便听听
//     */
//    public byte[] playRecommandMusic();

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
    public byte[] exit();

    /**
     * 切换响应的播放模式
     */
    public byte[] changeSingleMode(@PlayerInfo.PlayerMode int mode);

    /**
     * 切换播放模式
     */
    public byte[] changeMode(EnumState.Operation operation);

    /**
     * 打开播放器
     */
    public byte[] open();


    public byte[] favour(byte[] objects);

    public byte[] playfavour();

    /**
     * 打开本地音乐
     *
     * @return
     */
    public byte[] openLocal();

    /**
     * 播放本地音乐
     */
    public void playLocalAudios();

    /**
     * 不喜欢该歌曲
     *
     * @return
     */
    public byte[] hateAudio();


}
