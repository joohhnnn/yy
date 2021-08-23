package com.txznet.txzsetting.util;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.txznet.txzsetting.data.SettingData;

import java.io.File;

/**
 * json格式内容转成对象
 *
 * @param { "wakeup.threshhold.value" : 0.0f,
 *          "wakeup.words":["你好小贱", "你好菜菜"],
 *          "wakeup.enable":false,
 *          "device.welcome.msg":"主人你好",
 *          "poi.map.mode":1,
 *          }
 */

public class JsonIntentUtil {
    public static final String TAG = JsonIntentUtil.class.getSimpleName();

    public static final String LOG_ENABLE_FILE = "log_enable_file";
    public static final String PCM_ENABLE_DEBUG = "pcm_enable.debug";

    public static final String BROADCAST_TXZ_RECEIVED = "com.txznet.adapter.recv"; // 同行者收到的广播
    public static final String BROADCAST_TXZ_SEND = "com.txznet.adapter.send"; // 同行者发送的广播

    public static final String USERCONF_UPDATE_ACTION = "com.txznet.userconf.update";//用户设置完成广播
    public static final String USERCONF_CORE_UPDATE_ACTION = "com.txznet.userconf.core.update";//Core更新用户配置广播
    public static final String FACTORY_CONF_CORE_UPDATE_ACTION = "com.txznet.factoryconf.core.update";//Core更新出厂配置广播(adapter有改动时发出)
    public static final String USERCONF_JSON_CONTENT_NAME = "json_conf";//用户设置完成广播，携带json内容的字段
    public static final String USERCONF_SAVE_DIR = getTXZRootDir();//用户配置存放目录
    public static final String USERCONF_NAME = "userconf.json";//用户配置文件名称
    public static final String FACTORYCONF_NAME = "factoryconf.json";//出厂配置名称

    public static final String TXZ_SETTING_CFG_SAVE_DIR_SDCARD =getTXZRootDir();//com.txznet.txzsetting.cfg配置文件目录1（优先级较高）
    public static final String TXZ_SETTING_CFG_SAVE_DIR_SYSTEM ="system/txz";//com.txznet.txzsetting.cfg配置文件目录2
    public static final String TXZ_SETTING_CFG_NAME ="com.txznet.txzsetting.cfg";//TXZSetting的配置文件
    public static final String TXZ_SETTING_CFG_SHOW_WAKEUP_COMMAND = "SHOW_WAKEUP_COMMAND";


    public static final String JSON_THRESHHOLD = "wakeup.threshhold.value";
    public static final String JSON_WAKEUP_WORDS = "wakeup.words";
    public static final String JSON_WAKEUP_ENABLE = "wakeup.enable";
    public static final String JSON_WELCOME_TEXT = "device.welcome.msg";
    public static final String JSON_POI_MAP_MODE = "poi.map.mode";//1是列表模式，2是混合模式
    public static final String JSON_ENGINE_TENCENT_ISUSE = "engine.tencent.isuse";//布尔值：是否需要显示腾讯相关logo


    public static final double JSON_HIGH_VERY = -3.5f;
    public static final double JSON_HIGH = -3.2f;
    public static final double JSON_NORMAL = -3.1f;
    public static final double JSON_LOW = -2.7f;
    public static final double JSON_LOW_VERY = -2.5f;

    public static final int JSON_POI_MODE_LIST = 1;
    public static final int JSON_POI_MODE_BLEND = 2;
    public static final int JSON_POI_MODE_NOTSET = -1;

    private static Intent intent;
    private static JsonIntentUtil instance;

    public JsonIntentUtil() {
        intent = new Intent();
    }

    public static JsonIntentUtil getInstance() {
        instance = new JsonIntentUtil();
        return instance;
    }

    public static String getTXZRootDir() {
        String rootDir = "/sdcard/txz";
        File sdcard = Environment.getExternalStorageDirectory();
        if (sdcard != null) {
            rootDir = sdcard.getPath() + File.separator + "txz";
        }
        Log.d(TAG, "txz root dir = " + rootDir);
        return rootDir;
    }

    public void sendTXZSettingBroadcast(Context contexts, SettingData json) {
        Log.d(TAG, "发送广播给Core = " + json.toString());
        intent.setAction(USERCONF_UPDATE_ACTION);
        intent.putExtra(USERCONF_JSON_CONTENT_NAME, json.toString());
        contexts.sendBroadcast(intent);
    }
}
