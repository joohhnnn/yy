package com.txznet.sdk.media;

/**
 * 媒体工具搜索配置
 * Created by J on 2018/10/19.
 */

public final class MediaToolSearchConfig {
    public String toolName;
    public boolean showResult;
    public int timeout;

    public MediaToolSearchConfig(String toolName, boolean showResult, int timeout) {
        this.toolName = toolName;
        this.showResult = showResult;
        this.timeout = timeout;
    }
}
