package com.txznet.music.baseModule.bean;

public class EnumState {

    //操作的类型
    public static enum Operation {
        auto,//自动
        manual,//手动
        sound,//声控
        error,//出错
        temp,//临时暂停播放,被抢焦点
        wakeUP,//启动
        extra;//外部调用，通过sdk

    }

    public static enum AudioType {
        music,//音乐
        radio,//电台
        live,//直播
    }

}
