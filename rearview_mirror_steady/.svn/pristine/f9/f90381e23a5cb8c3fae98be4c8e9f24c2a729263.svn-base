package com.txznet.webchat.util;

import android.text.TextUtils;

import com.txznet.webchat.Constant;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxPluginInfo;

import java.io.File;

/**
 * 提供插件下载/装载等相关逻辑的工具方法
 * Created by J on 2017/7/26.
 */

public class WxPluginUtil {
    private static final String LOG_TAG = "WxPluginUtil";

    public static final String WX_PLUGIN_SUFFIX = ".jar";
    public static final String WX_PLUGIN_NAME_SEPARATOR = "_";
    private static final String WX_PLUGIN_PACKAGE_NAME_PREFIX = "com.txznet.webchat.plugin.";

    /**
     * 从指定文件名中解析微信插件的相关信息
     * 后台下发的插件存储的文件名格式为token#version.jar, token以wx_开头
     * 如wx_logic_plugin#2.0.2.jar
     *
     * @param fileName 插件文件名
     * @return 插件信息(token version)
     */
    public static WxPluginInfo getPluginInfoFromFileName(String fileName) {
        WxPluginInfo info = new WxPluginInfo();

        if (!fileName.endsWith(WX_PLUGIN_SUFFIX)) {
            L.e(LOG_TAG, "illegal plugin name: " + fileName);
            return info;
        }

        String[] split = fileName.split(Constant.PLUGIN_NAME_VERSION_SEPERATOR);
        info.name = split[0];
        info.version = split[1].substring(0, split[1].length() - WX_PLUGIN_SUFFIX.length());

        return info;
    }

    /**
     * 根据插件文件解析插件信息
     *
     * @param file
     * @return
     */
    public static WxPluginInfo getPluginInfoFromFile(File file) {
        return getPluginInfoFromFileName(file.getName());
    }

    /**
     * 获取插件下载路径
     * @param info
     * @return
     */
    public static String getPluginDownloadPath(WxPluginInfo info) {
        return Constant.PLUGIN_NEW_PATH + File.separator + info.name + Constant.PLUGIN_NAME_VERSION_SEPERATOR + info.version + WX_PLUGIN_SUFFIX;
    }

    /**
     * 获取预置插件释放路径
     *
     * @param info
     * @return
     */
    public static String getPluginReleasePath(WxPluginInfo info) {
        return Constant.PLUGIN_PRESET_PATH + File.separator + info.name + Constant.PLUGIN_NAME_VERSION_SEPERATOR + info.version + WX_PLUGIN_SUFFIX;
    }

    /**
     * 获取预置插件在assets目录中的路径
     *
     * @param info
     * @return
     */
    public static String getAssetsNameForPresetPlugin(WxPluginInfo info) {
        return Constant.PLUGIN_PRESET_ASSETS_PATH + File.separator + info.name + Constant.PLUGIN_NAME_VERSION_SEPERATOR + info.version + WX_PLUGIN_SUFFIX;
    }

    /**
     * 根据插件信息获取对应的WxPlugin包名(用于ClassLoader进行装载)
     *
     * @param info
     * @return
     */
    public static String getPluginPackageName(WxPluginInfo info) {
        return getPluginPackageName(info.name);
    }

    /**
     * 根据插件名获取对应的WxPlugin包名(用于ClassLoader进行装载)
     *
     * @param pluginName
     * @return
     */
    public static String getPluginPackageName(String pluginName) {
        return WX_PLUGIN_PACKAGE_NAME_PREFIX + toCamelCase(pluginName, WX_PLUGIN_NAME_SEPARATOR);
    }

    /**
     * 将以指定分隔符分隔的String转为驼峰命名格式
     *
     * @param rawStr
     * @param separator
     * @return
     */
    private static String toCamelCase(String rawStr, String separator) {
        String[] split = rawStr.split(separator);
        StringBuilder sb = new StringBuilder();

        for (String str : split) {
            if (!TextUtils.isEmpty(str)) {
                char[] arrChar = str.toCharArray();
                arrChar[0] -= 32;
                sb.append(String.valueOf(arrChar));
            }
        }

        return sb.toString();
    }

    /**
     * 返回newVersion是否比rawVersion标识的版本号要新
     *
     * @param rawVersion
     * @param newVersion
     * @return 仅当newVersion比rawVersion版本号要高时返回true
     */
    public static boolean isNewerVersion(String rawVersion, String newVersion) {
        String[] split1 = rawVersion.split("\\.");
        String[] split2 = newVersion.split("\\.");

        // 对于长度不相等的情况, 统一返回false
        if (split1.length != split2.length) {
            return false;
        }

        // 按.分隔版本号后逐位比对, 避免类似3.0.9 > 3.0.10的情况
        for (int i = 0; i < split1.length; i++) {
            int versionCode1 = Integer.valueOf(split1[i]);
            int versionCode2 = Integer.valueOf(split2[i]);

            if (versionCode1 == versionCode2) {
                continue;
            }

            return versionCode1 < versionCode2;
        }

        return false;
    }
}
