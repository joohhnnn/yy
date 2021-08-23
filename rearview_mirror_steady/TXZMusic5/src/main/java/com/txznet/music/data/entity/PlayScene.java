package com.txznet.music.data.entity;

/**
 * 播放场景，描述当前播放内容归属什么分类
 */
public enum PlayScene {
    IDLE, // 空闲，当前没有播放内容时，默认值

    LOCAL_MUSIC, // 本地音乐
    HISTORY_MUSIC, // 历史音乐
    FAVOUR_MUSIC, // 收藏音乐
    WECHAT_PUSH, // 微信推送

    HISTORY_ALBUM, // 历史专辑
    FAVOUR_ALBUM, // 收藏专辑
    ALBUM, // 专辑

    AI_RADIO // AI电台
}