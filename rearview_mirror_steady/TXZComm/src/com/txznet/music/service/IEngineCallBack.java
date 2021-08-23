package com.txznet.music.service;

import com.txznet.sdk.bean.LocationData;

/**
 * 声控交互回调类
 *
 * @author telenewbie
 */
public interface IEngineCallBack {

    /**
     * 设备休眠
     */
    public byte[] deviceSleep();

    /**
     * 设备唤醒
     */
    public byte[] deviceWakeUp();

    /**
     * 客户端退出
     */
    public byte[] clientExit();

    /**
     * 开始倒车时回调
     */
    public byte[] clientBackCarOn();

    /**
     * 结束倒车时回调
     */
    public byte[] clientBackCarOff();

    /**
     * 声控引擎初始化成功
     */
    public byte[] soundInitSuccess();

    /**
     * 操作音乐相关
     *
     * @param command 关键字
     * @param data    参数
     */
    public byte[] invokeMusic(String pkgName, String command, byte[] data);

    /**
     * 操作电台相关
     *
     * @param command 关键字
     * @param data    参数
     */
    public byte[] invokeAudio(String pkgName, String command, byte[] data);

    /**
     * GPS数据更新回调
     *
     * @param data
     */
    public void onLocationUpdate(LocationData data);

    /**
     * 其他的命令字
     *
     * @param command
     * @param data
     */
    public byte[] onOtherCmd(String pkgName, String command, byte[] data);
}
