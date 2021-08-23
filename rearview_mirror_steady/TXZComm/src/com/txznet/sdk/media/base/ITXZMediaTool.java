package com.txznet.sdk.media.base;

import com.txznet.sdk.media.constant.PlayerLoopMode;
import com.txznet.sdk.media.constant.PlayerStatus;
import com.txznet.sdk.media.TXZMediaModel;

/**
 * 远程音乐工具
 * Created by J on 2018/5/7.
 */

public interface ITXZMediaTool {
    /**
     * 是否屏蔽声控tts提示
     * @return
     */
    boolean interceptTts();

    // ------------ 控制接口 ---------------

    /**
     * 打开界面
     *
     * @param play 是否自动播放
     */
    void open(boolean play);

    /**
     * 退出
     */
    void exit();

    /**
     * 开始播放
     *
     * @param model 搜索的节目
     */
    void play(TXZMediaModel model);

    /**
     * 暂停播放
     */
    void pause();

    /**
     * 继续播放
     */
    void continuePlay();

    /**
     * 下一首
     */
    void next();

    /**
     * 上一首
     */
    void prev();

    /**
     * 切换循环模式
     *
     * @param mode 循环模式
     * @see PlayerLoopMode#SEQUENTIAL
     * @see PlayerLoopMode#SHUFFLE
     * @see PlayerLoopMode#SINGLE_LOOP
     * @see PlayerLoopMode#LIST_LOOP
     */
    void switchLoopMode(PlayerLoopMode mode);

    /**
     * 收藏
     */
    void collect();

    /**
     * 取消收藏
     */
    void unCollect();

    /**
     * 播放收藏
     */
    void playCollection();

    /**
     * 订阅
     */
    void subscribe();

    /**
     * 取消订阅
     */
    void unSubscribe();

    /**
     * 播放订阅
     */
    void playSubscribe();

    // ------------ 状态查询 ---------------

    /**
     * 获取当前播放状态
     *
     * @return 当前播放状态
     * @see PlayerStatus#IDLE
     * @see PlayerStatus#BUFFERING
     * @see PlayerStatus#PLAYING
     * @see PlayerStatus#PAUSED
     * @see PlayerStatus#STOPPED
     */
    PlayerStatus getStatus();

    /**
     * 是否支持指定的循环模式
     *
     * @param mode 循环模式
     * @return 支持状态
     * @see PlayerLoopMode#SEQUENTIAL
     * @see PlayerLoopMode#SHUFFLE
     * @see PlayerLoopMode#SINGLE_LOOP
     * @see PlayerLoopMode#LIST_LOOP
     */
    boolean supportLoopMode(PlayerLoopMode mode);

    /**
     * 是否支持收藏
     *
     * @return
     */
    boolean supportCollect();

    /**
     * 是否支持取消收藏
     *
     * @return
     */
    boolean supportUnCollect();

    /**
     * 是否支持播放收藏列表
     *
     * @return
     */
    boolean supportPlayCollection();

    /**
     * 是否支持订阅
     *
     * @return
     */
    boolean supportSubscribe();

    /**
     * 是否支持取消订阅
     *
     * @return
     */
    boolean supportUnSubscribe();

    /**
     * 是否支持播放订阅列表
     *
     * @return
     */
    boolean supportPlaySubscribe();

    /**
     * 是否支持搜索
     *
     * @return
     */
    boolean supportSearch();

    /**
     * 是否有下一首
     *
     * @return
     */
    boolean hasNext();

    /**
     * 是否有上一首
     *
     * @return
     */
    boolean hasPrev();
}


