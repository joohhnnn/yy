package com.txznet.txz.module.media.plugin;

/**
 * 外部媒体工具数据接口, 用于外部工具向Core发起调用
 *
 * Created by J on 2019/3/15.
 */
public interface PluginMediaToolDataInterface {
    byte[] onMediaToolInvoke(String token, String cmd, byte[] data);
}
