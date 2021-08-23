/**
 *
 */
package com.txznet.fm.bean;

import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.utils.StringUtils;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * 参数配置
 */
public class Configuration {

    public static final String TXZ_VERSION = "version";
    public static final String TXZ_Search_VERSION = "search_version";
    public static final String TXZ_Category_VERSION = "category_version";
    public static final String TXZ_Audio_VERSION = "audio_version";
    public static final String TXZ_ALBUM_LIST_VERSION = "album_list_version";
    public static final String TXZ_PLUGIN_PATH = "audio_plugin_path";
    public static final String TXZ_PLUGIN_CLASS_NAME = "audio_plugin_class_name";
    public static final String TXZ_SKIN = "skin";// 皮肤
    public static final String TXZ_TEST = "isTest";// 皮肤
    public static final String TXZ_WIDGET = "widget";

    private static ResourceBundle resourceBundle;

    private static final Configuration INSTANCE = new Configuration();
    private static final String TAG = "[MUSIC][Config]";

    private Configuration() {
        try {
            resourceBundle = ResourceBundle.getBundle("TXZAudio", Locale.getDefault());
        } catch (Exception e) {
            resourceBundle = null;
            LogUtil.loge(TAG + "[error]config", e);
        }
    }

    public static Configuration getInstance() {
        return INSTANCE;
    }

    public String getString(String key) {
        String result = "";
        if (TXZFileConfigUtil.getConfig(key) != null) {
            result = TXZFileConfigUtil.getConfig(key).get(key);
            LogUtil.logd("music:config:outer:" + key + "=" + result);
        }
        if (TextUtils.isEmpty(result) && resourceBundle != null) {
            result = resourceBundle.getString(key);
            LogUtil.logd("music:config:inner:" + key + "=" + result);
        }
        return result;
    }

    public int getInteger(String key) {
//        Object number = getObject(key);
//        if (number instanceof Integer) {
//            return (Integer) number;
//        }
//        if (number instanceof String) {
        String s = getString(key);
        if (StringUtils.isNumeric(s)) {
            return Integer.parseInt(s);
//            }
        }
        return 0;
    }

    public boolean getBoolean(String key) {
        String isTest = getString(key);
        try {
            return Boolean.parseBoolean(isTest);
        } catch (Exception e) {
            return false;
        }
    }

    public Object getObject(String key) {
        String result = "";
        if (TXZFileConfigUtil.getConfig() != null) {
            result = TXZFileConfigUtil.getConfig().get(key);
        }

        if (TextUtils.isEmpty(result) && resourceBundle != null) {
            result = (String) resourceBundle.getObject(key);
        }
        return result;
    }
}
