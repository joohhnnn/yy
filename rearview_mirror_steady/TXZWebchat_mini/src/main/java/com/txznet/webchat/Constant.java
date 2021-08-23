package com.txznet.webchat;

import android.content.Context;
import android.os.Environment;

import com.txznet.loader.AppLogic;

public final class Constant {
    public static final long TTS_SAME_SPEAKER_DELAY = 6000; // 同一个联系人连续说话的间隔
    public static final long MAX_TTS_VOICE_LENGTH = Integer.MAX_VALUE; // 允许播报的最大TTS长度
    public static final int MAX_TTS_TEXT_LENGTH = 100; // 允许播报的最大文本字数

    public static final String URL_WX_SERVER_CONFIG_UPDATE = "http://f.txzing.com/service/wechat/plugin.php";

    // 插件路径

    /**
     * 插件调试路径, 装载插件时优先级最高
     */
    public static final String PLUGIN_DEBUG_PATH = Environment.getExternalStorageDirectory() + "/txz/webchat/plugin/";
    /**
     * 插件下载路径, 装载插件第二优先级目录
     */
    public static final String PLUGIN_NEW_PATH = AppLogic.getApp().getDir("pluginNew", Context.MODE_PRIVATE).getPath();
    /**
     * 本地插件路径, 装载插件时优先级最低
     */
    public static final String PLUGIN_PATH = AppLogic.getApp().getDir("plugin", Context.MODE_PRIVATE).getPath();

    /**
     * 预置插件路径, 微信启动时会自动将assets目录中的预置插件释放到此目录
     */
    public static final String PLUGIN_PRESET_PATH = AppLogic.getApp().getDir("plugin_preset", Context.MODE_PRIVATE).getPath();

    public static final String PLUGIN_PRESET_ASSETS_PATH = "wxp";

    public static final String PATH_WECHAT_DIR_BASE = Environment.getExternalStorageDirectory().getPath() + "/txz/webchat/";

    // 语音消息本地缓存路径
    public static final String PATH_MSG_VOICE_CACHE = PATH_WECHAT_DIR_BASE + "cache/Voice/";
    public static final String PATH_MSG_VOICE_CACHE_SELF = PATH_WECHAT_DIR_BASE + "cache/Voice/Self/";

    // 头像本地缓存路径
    public static final String PATH_HEAD_CACHE = PATH_WECHAT_DIR_BASE + "cache/Head/";

    // 录音缓存路径
    public static final String PATH_RECORD_CACHE_PREFIX = PATH_MSG_VOICE_CACHE + "record";
    public static final String PATH_RECORD_CACHE = PATH_RECORD_CACHE_PREFIX + ".wav";
    public static final String PATH_RECORD_CACHE_OLD = PATH_RECORD_CACHE_PREFIX + ".mp3";


    // 插件名称版本分隔符
    public static final String PLUGIN_NAME_VERSION_SEPERATOR = "#";
}
