package com.txznet.music.soundControlModule.logic;


import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;

/**
 * 声控需要获取基本信息
 * Created by telenewbie on 2016/12/23.
 */

public class SoundCommon implements ISoundCommon {

    //##创建一个单例类##
    private volatile static SoundCommon singleton;
    private SoundCommon(){}
    public static SoundCommon getInstance() {
        if (singleton == null) {
            synchronized (SoundCommon.class) {
                if (singleton == null) {
                    singleton = new SoundCommon();
                }
            }
        }
        return singleton;
    }
    @Override
    public byte[] isPlaying() {
        return String.valueOf(PlayEngineFactory.getEngine().isPlaying()).getBytes();
    }

    @Override
    public byte[] getVersion() {
        //在3.1.1之前这里返回true,2017年5月11日18:03:43
//        return String.valueOf(BuildConfig.VERSION_CODE).getBytes();
        //这里不能修改为版本号,再别的地方进行设置,@See Myservice的"app.version"
        return String.valueOf(true).getBytes();
    }
}
