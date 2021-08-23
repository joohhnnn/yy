package com.txznet.music.config;

import android.os.Environment;

import com.txznet.txz.util.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用内部配置项
 */
public class Configuration {
    // 第三方资源路径
    public static class ThirdPath {
        //        public static final String PATH_QQ_MUSIC = Environment.getExternalStorageDirectory().getPath() + File.separator + "qqmusic";
        public static final String PATH_QQ_MUSIC_CAR = Environment.getExternalStorageDirectory().getPath() + File.separator + "qqmusiccar";
        public static final String PATH_KW_CAR = Environment.getExternalStorageDirectory().getPath() + File.separator + "kwmusiccar";

        public static List<String> PATHS = new ArrayList<>();

        static {
//            PATHS.add(PATH_QQ_MUSIC);
            PATHS.add(PATH_QQ_MUSIC_CAR);
            PATHS.add(PATH_KW_CAR);
        }
    }

    public static class Key {
        // 后台请求参数配置
        public static final String TXZ_SEARCH_VERSION = "search_version"; // 搜索资源数据 3表示支持乐听 10 表示后台支持的同听最低版本（<10 表示非4.0）
        public static final String TXZ_CATEGORY_VERSION = "category_version"; // 请求分类数据 4 表示支持乐听
        public static final String TXZ_AUDIO_VERSION = "audio_version"; // 请求专辑底下的音频数据 1 表示之前的字段albumId等字段服务器由返回变为不返回（冗余）
        public static final String TXZ_ALBUM_LIST_VERSION = "album_list_version"; // 10 表示后台支持的同听最低版本（<10 表示非4.0）
        public static final String TXZ_PLUGIN_PATH = "audio_plugin_path"; // 插件预埋的路径
        public static final String TXZ_PLUGIN_CLASS_NAME = "audio_plugin_class_name"; // 插件处理的类
        public static final String ENABLE_MEDIA_PROXY = "enable_media_proxy";// 是否启用代理服务，true/false
        public static final String HIDE_THIRD_SOURCE = "hide_third_source"; // 隐藏第三方资源，true/false
        public static final String HARDWARE_ACCELERATED = "hardware_accelerated"; // 是否开启硬件加速

        public static final String LOCAL_CACHE_DIR = "local_cache_dir"; // 本地音频缓存存放路径()
        public static final String LOCAL_CACHE_TEMP_DIR = "local_cache_temp_dir"; // 本地音频缓存临时目录（.tmp文件），默认/sdcard/txz/cache/
    }

    public static class DefVal {
        public static final int SEARCH_VERSION = 10;
        public static final int CATEGORY_VERSION = 10;
        public static final int AUDIO_VERSION = 10;
        public static final int ALBUM_LIST_VERSION = 10;
        public static final String AUDIO_PLUGIN_PATH = "/txz/audio/output.dex";
        public static final String AUDIO_PLUGIN_CLASS_NAME = "com.txznet.audio.player.TestAudioPlugin";
        public static final boolean ENABLE_MEDIA_PROXY = true; // 是否启用代理服务
        public static final boolean HIDE_THIRD_SOURCE = false; // 隐藏第三方资源

        public static final String LOCAL_CACHE_TEMP_DIR = StorageUtil.getInnerSDCardPath() + "/txz/cache";
        public static final String LOCAL_CACHE_DIR = StorageUtil.getInnerSDCardPath() + "/txz/audio/song";

        public static final int PAGE_COUNT = 10; // 去服务端请求十条数据
        public static final int PAGE_COUNT_MUSIC = 100; // 音乐专辑默认请求数量
        public static final int PAGE_COUNT_AI = 5; // Ai电台默认请求数量

        public static final int SPLASH_PAGE_SHOW_TIME = 5000; // 默认启动页展示时长

        public static final int AI_RADIO_NEED_TO_REQ_COUNT = 3; // AI推送补充缓存的临界值

        public static final int REQ_FAVOUR_SIZE = 100; //请求收藏的数量,为了加快请求,改为100条数据

        public static final int DEFAULT_TIME_OUT = 5000; // 默认请求超时时间
    }
}
