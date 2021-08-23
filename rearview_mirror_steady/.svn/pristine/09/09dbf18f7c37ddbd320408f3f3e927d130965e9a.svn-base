package com.txznet.txz.module.media.plugin;

import java.util.List;

/**
 * 外部装载的媒体工具
 *
 * NOTE: 此接口不允许任何形式的修改, Core里会通过此接口进行外部媒体工具适配的装载. 修改接口内容会引起兼容
 * 问题. 确实需要修改外部适配的相关方法时通过提升外部接口版本(chk文件内配置)并实现新版本接口的方式进行变更.
 *
 * Created by J on 2019/03/01.
 */

public interface IPluginMediaTool {
    /**
     * 循环模式
     */
    enum LOOP_MODE {
        /**
         * 顺序播放
         */
        SEQUENTIAL,
        /**
         * 随机
         */
        SHUFFLE,
        /**
         * 单曲循环
         */
        SINGLE_LOOP,
        /**
         * 列表循环
         */
        LIST_LOOP
    }

    /**
     * 播放器状态
     */
    enum PLAYER_STATUS {
        /**
         * 空闲中
         */
        IDLE,
        /**
         * 缓冲中
         */
        BUFFERING,
        /**
         * 播放中
         */
        PLAYING,
        /**
         * 暂停中
         */
        PAUSED,
        /**
         * 已停止(退出)
         */
        STOPPED
    }

    /**
     * 媒体工具对应操作
     */
    enum MEDIA_TOOL_OP {
        /**
         * 打开播放器
         */
        OPEN,
        /**
         * 退出播放器
         */
        EXIT,
        /**
         * 播放指定节目(搜索歌曲/电台)
         */
        PLAY,
        /**
         * 停止播放
         */
        STOP,
        /**
         * 暂停播放
         */
        PAUSE,
        /**
         * 继续播放
         */
        CONTINUE_PLAY,
        /**
         * 下一首
         */
        NEXT,
        /**
         * 上一首
         */
        PREV,
        /**
         * 顺序播放模式
         */
        SWITCH_MODE_SEQUENTIAL,
        /**
         * 单曲循环模式
         */
        SWITCH_MODE_SINGLE_LOOP,
        /**
         * 列表循环模式
         */
        SWITCH_MODE_LIST_LOOP,
        /**
         * 随机播放模式
         */
        SWITCH_MODE_SHUFFLE,
        /**
         * 收藏
         */
        COLLECT,
        /**
         * 取消收藏
         */
        UNCOLLECT,
        /**
         * 订阅
         */
        SUBSCRIBE,
        /**
         * 取消订阅
         */
        UNSUBSCRIBE,
        /**
         * 播放收藏
         */
        PLAY_COLLECTION,
        /**
         * 播放订阅
         */
        PLAY_SUBSCRIBE,
        /**
         * 查询当前正在播放的曲目
         * "现在正在播放什么歌"
         */
        GET_PLAYING_MODEL,
    }

    /**
     * 媒体搜索回调
     */
    interface MediaSearchCallback {
        void onSuccess(List<PluginMediaModel> result);
        void onError(String cause);
    }


    /**
     * 获取当前工具包名
     *
     * @return 包名
     */
    String getPackageName();

    /**
     * 获取优先级
     * @return
     */
    int getPriority();

    // ------------ 搜索接口 ---------------

    /**
     * 搜索
     * @param model
     * @param callback
     */
    void search(PluginMediaModel model, MediaSearchCallback callback);

    /**
     * 取消搜索
     * 兼容旧版本交互逻辑
     */
    void cancelRequest();

    /**
     * 播放指定列表
     * @param list 播放列表
     * @param index 开始播放节目
     */
    void play(List<PluginMediaModel> list, int index);

    // ------------ 控制接口 ---------------

    /**
     * 打开界面
     * @param play 是否自动播放
     */
    void open(boolean play);

    /**
     * 退出
     */
    void exit();

    /**
     * 开始播放
     * @param model 搜索的节目
     */
    void play(PluginMediaModel model);

    /**
     * 停止播放
     */
    void stop();

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
     * @see LOOP_MODE#SEQUENTIAL
     * @see LOOP_MODE#SHUFFLE
     * @see LOOP_MODE#SINGLE_LOOP
     * @see LOOP_MODE#LIST_LOOP
     *
     * @param mode 循环模式
     */
    void switchLoopMode(LOOP_MODE mode);

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
     * @see PLAYER_STATUS#IDLE
     * @see PLAYER_STATUS#BUFFERING
     * @see PLAYER_STATUS#PLAYING
     * @see PLAYER_STATUS#PAUSED
     * @see PLAYER_STATUS#STOPPED
     *
     * @return 当前播放状态
     */
    PLAYER_STATUS getStatus();

    /**
     * 获取当前正在播放的节目
     * @return
     */
    PluginMediaModel getPlayingModel();

    /**
     * 是否支持指定的循环模式
     *
     * @see LOOP_MODE#SEQUENTIAL
     * @see LOOP_MODE#SHUFFLE
     * @see LOOP_MODE#SINGLE_LOOP
     * @see LOOP_MODE#LIST_LOOP
     *
     * @param mode 循环模式
     * @return 支持状态
     */
    boolean supportLoopMode(LOOP_MODE mode);

    /**
     * 是否支持收藏
     * @return
     */
    boolean supportCollect();

    /**
     * 是否支持取消收藏
     * @return
     */
    boolean supportUnCollect();

    /**
     * 是否支持播放收藏列表
     * @return
     */
    boolean supportPlayCollection();

    /**
     * 是否支持订阅
     * @return
     */
    boolean supportSubscribe();

    /**
     * 是否支持取消订阅
     * @return
     */
    boolean supportUnSubscribe();

    /**
     * 是否支持播放订阅列表
     * @return
     */
    boolean supportPlaySubscribe();

    /**
     * 是否支持搜索
     * @return
     */
    boolean supportSearch();

    /**
     * 是否有下一首
     * @return
     */
    boolean hasNext();

    /**
     * 是否有上一首
     * @return
     */
    boolean hasPrev();

    // ------------ 其他接口 ---------------

    /**
     * 是否自己控制声控界面
     *
     * 若自己控制声控界面, 需要在各调用时中自己处理声控界面相关状态(显示文本/列表/打开关闭等)
     *
     * @param op 对应的操作
     * @return 返回true后, 指定操作中声控相关的tts和界面关闭逻辑都不会被调用, 需要媒体工具自己处理
     */
    boolean interceptRecordWinControl(MEDIA_TOOL_OP op);

    // 调用接口
    byte[] invoke(String cmd, byte[] data);
}
