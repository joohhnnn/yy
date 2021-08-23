package com.txznet.webchat.actions;

import com.txznet.loader.AppLogic;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.webchat.Constant;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.helper.FileDownloadHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxPluginInfo;
import com.txznet.webchat.plugin.WxPluginManager;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.util.Md5Util;
import com.txznet.webchat.util.WxMonitorUtil;
import com.txznet.webchat.util.WxPluginUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 微信插件相关ActionCreator
 * <p>
 * 微信插件加载逻辑:
 * /localDir/plugin/ 插件目录
 * /localDir/pluginNew/ 插件临时目录， 新版本插件下载时会先下载到临时目录
 * 当新的后台配置下发中包含了插件的更新时, 客户端会启动下载, 将新插件下载到插件临时目录中并进行校验,
 * 校验通过后通知客户端有新的插件待加载, 客户端寻找合适时机重启进程进行插件加载.
 * 客户端每次启动时, 会优先从插件临时目录中装载插件, 临时目录中的插件装载完毕后将插件文件复制到插件
 * 目录覆盖旧版本
 * <p>
 * Created by J on 2017/7/14.
 */

public class WxPluginActionCreator {
    private static final String LOG_TAG = "WxPlugin";
    private static final long RETRY_DOWNLOAD_PLUGIN_INTERVAL = 10 * 60 * 1000;
    private static final int RETRY_DOWNLOAD_PLUGIN_COUNT_MAX = 2;

    // single instance
    private static WxPluginActionCreator sInstance;

    public static WxPluginActionCreator getInstance() {
        if (null == sInstance) {
            synchronized (WxPluginActionCreator.class) {
                if (null == sInstance) {
                    sInstance = new WxPluginActionCreator();
                }
            }

        }

        return sInstance;
    }

    private WxPluginActionCreator() {
    }
    // eof single instance

    /**
     * 已下载的插件列表, 记录本次运行过程中下载完成的插件
     */
    private ArrayList<WxPluginInfo> mDownloadedPlugin = new ArrayList<>();

    /**
     * 记录重试插件下载的次数
     */
    private HashMap<String, Integer> mRetryCountMap = new HashMap<>();

    private void checkDownloadPlugin(final WxPluginInfo plugin) {
        final String path = WxPluginUtil.getPluginDownloadPath(plugin);

        // 如果已存在指定文件, 检查md5是否匹配, 匹配不进行重复下载
        final File chkFile = new File(path);
        if (chkFile.exists()) {
            Md5Util.checkFileMd5(path, plugin.md5, new Md5Util.CheckCallback() {
                @Override
                public void onSuccess(boolean isCorrect) {
                    if (isCorrect) {
                        L.i(LOG_TAG, String.format("plugin: %s(%s) already exists, skip downloading", plugin.name, plugin.version));
                        return;
                    }

                    chkFile.delete();
                    downloadPlugin(plugin, path);
                }

                @Override
                public void onError(String reason) {
                    L.i(LOG_TAG, String.format("checking md5 for plugin: %s(%s) encountered error: %s, force re-download", plugin.name, plugin.version, reason));
                    chkFile.delete();
                    downloadPlugin(plugin, path);
                }
            });

            return;
        }

        // 不存在指定文件, 直接启动插件下载
        downloadPlugin(plugin, path);
    }

    private void downloadPlugin(final WxPluginInfo plugin, final String path) {
        // 首先删除同名插件的其他版本
        clearRelatedFilesForPlugin(plugin);

        // 启动下载
        FileDownloadHelper.getInstance().startDownload(plugin.url, path, new FileDownloadHelper.DownloadCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onProgress(float progress) {

            }

            @Override
            public void onFinished() {
                L.i(LOG_TAG, String.format("plugin download success: %s(%s), start checking md5", plugin.name, plugin.version));
                Md5Util.checkFileMd5(path, plugin.md5, new Md5Util.CheckCallback() {
                    @Override
                    public void onSuccess(boolean isCorrect) {
                        if (!isCorrect) {
                            L.i(LOG_TAG, String.format("md5 check failed: %s(%s)", plugin.name, plugin.version));
                            WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_DOWNLOAD_FAILED_MD5, plugin);
                            retryDownloadPlugin(plugin);
                        }

                        L.i(LOG_TAG, String.format("md5 check passed: %s(%s)", plugin.name, plugin.version));
                        WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_DOWNLOAD_SUCCESS, plugin);
                        mDownloadedPlugin.add(plugin);
                        Dispatcher.get().dispatch(new Action<>(ActionType.WX_PLUGIN_DOWNLOAD_SUCCESS, plugin));
                        notifyLoadPlugin();
                    }

                    @Override
                    public void onError(String reason) {
                        L.e(LOG_TAG, "md5 check for " + plugin.name + " encountered error: " + reason);
                        WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_DOWNLOAD_FAILED_MD5_ERROR, plugin);
                    }
                });
            }

            @Override
            public void onError(String err) {
                L.e(LOG_TAG, "download plugin " + plugin.name + " failed: " + err);
                WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_DOWNLOAD_FAILED, plugin);
                retryDownloadPlugin(plugin);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void clearRelatedFilesForPlugin(WxPluginInfo plugin) {
        File downloadDir = new File(Constant.PLUGIN_NEW_PATH);
        File[] otherVersions = downloadDir.listFiles();

        for (File file : otherVersions) {
            if (file.getName().contains(plugin.name)) {
                file.delete();
            }
        }
    }

    public void notifyLoadPlugin() {
        if (0 == mDownloadedPlugin.size()) {
            L.d(LOG_TAG, "no plugin downloaded, skip loadPlugin");
            return;
        }

        if (AppLogic.isForeground()) {
            L.d(LOG_TAG, "loadPlugin::app is in foreground, skip");
            return;
        }

        if (WxLoginStore.get().isLogin()) {
            L.d(LOG_TAG, "loadPlugin::wechat logged in, skip");
            return;
        }

        // 如果当前微信没有在前台运行, 且处于未登录状态, 直接重启进程进行插件装载
        L.i(LOG_TAG, "app not in foreground & not logged in, restart to execute plugin");
        AppLogic.exit();
    }

    public void checkPluginUpdate(WxPluginInfo[] pluginList) {
        if (null == pluginList) {
            return;
        }

        for (WxPluginInfo info : pluginList) {
            String versionCurrent = getCurrentVersionForPlugin(info.name);

            if (WxPluginUtil.isNewerVersion(versionCurrent, info.version)) {
                L.i(LOG_TAG, String.format("plugin update: %s(%s), raw version: %s", info.name, info.version, versionCurrent));
                WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_UPDATE_DETECTED, info);
                // 插件有更新, 启动下载
                checkDownloadPlugin(info);
            }
        }
    }

    private String getCurrentVersionForPlugin(String pluginToken) {
        for (WxPluginInfo info : mDownloadedPlugin) {
            if (info.name.equals(pluginToken)) {
                return info.version;
            }
        }

        return WxPluginManager.getInstance().getPluginVersionName(pluginToken);
    }

    private void retryDownloadPlugin(final WxPluginInfo info) {
        Integer currRetryCount = mRetryCountMap.get(info.name);

        if (null == currRetryCount) {
            currRetryCount = 0;
        }

        if (++currRetryCount <= RETRY_DOWNLOAD_PLUGIN_COUNT_MAX) {
            L.i(LOG_TAG, String.format("retry download plugin: %s(%s), times: %s", info.name, info.version, currRetryCount));

            AppLogic.runOnBackGround(new Runnable1<WxPluginInfo>(info) {
                @Override
                public void run() {
                    checkDownloadPlugin(info);
                }
            }, RETRY_DOWNLOAD_PLUGIN_INTERVAL);

            mRetryCountMap.put(info.name, currRetryCount);
            return;
        }

        mRetryCountMap.put(info.name, 0);
    }
}
