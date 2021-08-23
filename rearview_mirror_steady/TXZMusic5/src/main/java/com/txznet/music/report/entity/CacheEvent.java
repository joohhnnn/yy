package com.txznet.music.report.entity;

/**
 * 缓存事件
 *
 * @author zackzhou
 * @date 2019/1/22,17:26
 */

public class CacheEvent extends BaseEvent {

    public long cacheSize;

    public CacheEvent(int eventId, long cacheSize) {
        super(eventId);
        this.cacheSize = cacheSize;
    }
}
