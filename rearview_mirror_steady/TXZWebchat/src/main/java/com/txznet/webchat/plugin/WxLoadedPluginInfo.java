package com.txznet.webchat.plugin;

import com.txznet.webchat.comm.plugin.base.WxPlugin;
import com.txznet.webchat.model.WxPluginInfo;

/**
 * 微信插件加载信息
 * Created by J on 2018/10/9.
 */

public final class WxLoadedPluginInfo {
    private final WxPluginInfo mPluginInfo;
    private final WxPlugin mPlugin;
    private final PLUGIN_LOAD_TYPE mLoadType;

    public WxLoadedPluginInfo(WxPluginInfo info, WxPlugin plugin, PLUGIN_LOAD_TYPE loadType) {
        this.mPluginInfo = info;
        this.mPlugin = plugin;
        this.mLoadType = loadType;
    }

    public WxPluginInfo getPluginInfo() {
        return mPluginInfo;
    }

    public WxPlugin getPlugin() {
        return mPlugin;
    }

    public PLUGIN_LOAD_TYPE getLoadType() {
        return mLoadType;
    }

    /**
     * 插件加载模式
     */
    public enum PLUGIN_LOAD_TYPE {
        // 预置模式, 打包在微信apk内
        PRESET,
        // 测试模式, 由测试路径加载
        TEST,
        // 正常模式, 由插件存储路径加载(只发生在下发了比预置插件版本更高的插件版本情况下)
        LOAD,
        /*
        * 下发模式, 由插件下发目录加载(新插件)
        * 新下发的插件会由下发目录装载, 首次装载成功后才会复制到插件存储路径下, 以后会在插件存储路径进行
        * 加载
        * */
        LOAD_NEW,
        // wtf??
        UNKNOWN;

        public String getTypeName() {
            switch (this) {
                case PRESET:
                    return "preset mode";
                case TEST:
                    return "test mode";
                case LOAD:
                    return "default";
                case LOAD_NEW:
                    return "new";

                default:
                    return "unknown";
            }
        }
    }
}
