package com.txznet.webchat.util;

import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.webchat.Config;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxPluginInfo;

/**
 * 微信监控
 * Created by J on 2016/6/28.
 */

public class WxMonitorUtil {
    /**
     * @定义规则：业务类型.模块.错误等级.接口
     * @错误等级：F/E/W/I/N
     */

    // 语音发送失败
    public static final String WX_VOICE_UPLOAD_FAILED = "wx3.voice.E.upload_err";
    // 位置分享失败
    public static final String WX_LOC_GET_FAILED = "wx3.loc.E.get_err";

    // 获取后台配置成功
    public static final String WX_SERVER_CONFIG_UPDATE_SUCCESS = "wx3.server.I.update";
    // 解析获取的后台配置json失败
    public static final String WX_SERVER_CONFIG_UPDATE_FAILED_JSON = "wx3.server.E.update_res_json";
    // 获取后台配置网络请求失败
    public static final String WX_SERVER_CONFIG_UPDATE_FAILED_NETWORK = "wx3.server.E.update_net";

    // 下发的后台配置中包含插件更新
    public static final String WX_PLUGIN_UPDATE_DETECTED = "wx3.plugin.I.update_find";
    // 微信插件下载成功
    public static final String WX_PLUGIN_DOWNLOAD_SUCCESS = "wx3.plugin.I.download";
    // 微信插件下载失败
    public static final String WX_PLUGIN_DOWNLOAD_FAILED = "wx3.plugin.E.download";
    // 微信插件下载完毕后md5校验不通过
    public static final String WX_PLUGIN_DOWNLOAD_FAILED_MD5 = "wx3.plugin.E.download_md5";
    // 微信插件下载完毕后md5校验失败
    public static final String WX_PLUGIN_DOWNLOAD_FAILED_MD5_ERROR = "wx3.plugin.E.download_md5_err";
    // 加载新插件目录下的插件成功
    public static final String WX_PLUGIN_LOAD_SUCCESS_NEW = "wx3.plugin.I.load_new";
    // 加载新插件目录下的插件失败
    public static final String WX_PLUGIN_LOAD_FAILED_NEW = "wx3.plugin.E.load_new";
    // 加载插件目录下的插件成功
    public static final String WX_PLUGIN_LOAD_SUCCESS = "wx3.plugin.I.load";
    // 加载插件目录下的插件失败
    public static final String WX_PLUGIN_LOAD_FAILED = "wx3.plugin.E.load";
    // 加载预置插件成功
    public static final String WX_PLUGIN_LOAD_SUCCESS_PRESET = "wx3.plugin.I.load_pre";
    // 加载预置插件失败
    public static final String WX_PLUGIN_LOAD_FAILED_PRESET = "wx3.plugin.E.load_pre";
    // 替换旧插件成功(发生在新插件下发后装载成功时, 复制到插件目录替换旧版插件)
    public static final String WX_PLUGIN_REPLACE_SUCCESS = "wx3.plugin.I.replace";
    // 替换旧插件失败
    public static final String WX_PLUGIN_REPLACE_FAILED = "wx3.plugin.E.replace";
    // 释放预置插件成功
    //public static final String WX_PLUGIN_RELEASE_SUCCESS = "wx3.plugin.I.release";
    // 释放预置插件失败
    //public static final String WX_PLUGIN_RELEASE_FAILED = "wx3.plugin.E.release";

    public static final void monitorPluginAction(String action, final WxPluginInfo plugin) {
        doMonitor(action + "_" + plugin.version);
    }


    public static void doMonitor(String tag) {
        L.d("WxMonitorUtil", tag);

        if (!Config.MONITOR) {
            return;
        }

        MonitorUtil.monitorCumulant(tag);
    }

}
