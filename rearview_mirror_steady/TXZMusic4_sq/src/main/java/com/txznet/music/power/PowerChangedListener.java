package com.txznet.music.power;

/**
 * Created by brainBear on 2017/10/31.
 */

public interface PowerChangedListener {

    /**
     * 休眠时回调
     */
    void onSleep();


    /**
     * 唤醒时回调
     */
    void onWakeUp();

    /**
     * 退出时回调
     */
    void onExit();


    /**
     * 开始倒车时回调
     */
    void onReverseStart();

    /**
     * 结束倒车时回调
     */
    void onReverseEnd();

}