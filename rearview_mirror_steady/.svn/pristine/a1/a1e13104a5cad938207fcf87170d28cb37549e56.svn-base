package com.txznet.webchat.plugin;

import android.text.TextUtils;

import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.util.FileUtil;
import com.txznet.webchat.BuildConfig;
import com.txznet.webchat.Constant;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.comm.plugin.base.WxPlugin;
import com.txznet.webchat.comm.plugin.utils.PluginTaskRunner;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.WxPluginInfo;
import com.txznet.webchat.plugin.preset.WxLogicPlugin;
import com.txznet.webchat.util.WxMonitorUtil;
import com.txznet.webchat.util.WxPluginUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dalvik.system.DexClassLoader;

/**
 * 微信插件管理器
 * Created by J on 2016/8/15.
 * <p>
 * 管理微信插件的装载, 处理插件发起的事件通知和针对插件的方法调用
 * <p>
 * <p> 插件更新逻辑:
 * 1.定时从服务器检查插件版本
 * 2.有插件更新时下载最新版本插件, 进行校验后等待加载
 * 3.符合加载条件时进行新版本插件装载, 对旧版本插件进行备份/删除
 * <p>
 * 检查更新时机:
 * 1.微信进程启动时
 * 2.登录\退出登录\更新二维码时
 * <p>
 * 插件装载时机:
 * 微信切换到后台状态&&未登录&&存在新版本插件待加载, 尝试重启进程进行插件装载
 */
public class WxPluginManager {
    private static final String LOG_TAG = "WxPluginManager";
    private static final String WECHAT_INVOKE_PREFIX = "wx.cmd.";

    private static String mDexPath = AppLogicBase.getApp().getApplicationInfo().dataDir
            + "/dex";
    private static String mLibPath = AppLogicBase.getApp().getApplicationInfo().dataDir
            + "/solibs";

    /**
     * 预置插件列表
     * 微信客户端内预置的插件需要添加到此列表中, 保证预置插件释放和加载逻辑正常运作
     */
    private Map<WxPluginInfo, Class<? extends WxPlugin>> mPresetPluginMap = new HashMap<>();

    private static final WxPluginManager sInstance = new WxPluginManager();

    /**
     * 已装载的插件列表
     * 插件装载成功后将引用保存到此list中, 用于插件invoke功能和保证不同版本插件不会重复装载
     */
    private Map<String, WxLoadedPluginInfo> mPluginMap = new ConcurrentHashMap<>();

    private WxPluginManager() {
        PluginManager.addCommandProcessor(WECHAT_INVOKE_PREFIX, mCmdProcessor);

        // 初始化预置插件列表
        initPresetPluginMap();

        // 加载插件
        loadPlugin();
    }

    /**
     * 初始化预置插件列表
     *
     * 因历史遗留问题, 微信插件的初始化是发生在实例化阶段的, 初始化预置插件列表不能对插件进行实例化操作,
     * 所有的插件实例化都应该发生在确实要进行插件装载时
     */
    private void initPresetPluginMap() {
        WxPluginInfo wxLogicPluginInfo = new WxPluginInfo(WxLogicPlugin.PLUGIN_TOKEN,
                WxLogicPlugin.PLUGIN_VERSION_NAME);
        mPresetPluginMap.put(wxLogicPluginInfo, WxLogicPlugin.class);
    }

    /**
     * 插件装载逻辑
     */
    private void loadPlugin() {
        // 插件调试模式下优先装载测试目录下的微信插件
        if (BuildConfig.PLUGIN_DEBUG) {
            File pluginDebugDir = new File(Constant.PLUGIN_DEBUG_PATH);
            File[] debugPluginList = pluginDebugDir.listFiles();

            // 这里要做判空处理, 因为可能因为对应路径不存在导致debugPluginList为null, 引起WxPluginManager
            // 初始化异常, 最终所有引用WxPluginManager的调用都会抛出NoClassDefFoundError
            if (null != debugPluginList) {
                for (File plugin : debugPluginList) {
                    loadPluginFile(plugin, WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.TEST);
                }
            }
        }

        // 优先装载pluginNew目录下的插件
        File pluginNewDir = new File(Constant.PLUGIN_NEW_PATH);
        File[] newPluginList = pluginNewDir.listFiles();

        for (File pluginFile : newPluginList) {
            loadNewPluginFile(pluginFile);
        }

        // 装载本地路径下的插件
        File pluginDir = new File(Constant.PLUGIN_PATH);
        File[] pluginList = pluginDir.listFiles();

        for (File pluginFile : pluginList) {
            loadPluginFile(pluginFile, WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.LOAD);
        }

        // 装载内置插件
        loadPresetPlugin();
    }

    private void loadNewPluginFile(File file) {
        if (loadPluginFile(file, WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.LOAD_NEW)) {
            // 插件装载成功, 复制到插件目录替换原插件
            replaceOldPlugin(file);
        }
    }

    private void replaceOldPlugin(File file) {
        // 同版本插件不会重复下载, 所以不需要考虑插件目录中已包含同名同版本插件的情况
        // 先复制当前加载完成的插件到插件目录
        String dstDir = Constant.PLUGIN_PATH + File.separator + file.getName();
        if (FileUtil.copyFile(file.getPath(), dstDir)) {
            L.i(LOG_TAG, String.format("copied %s to pluginDir, new path: %s", file.getName(),
                    dstDir));

            WxPluginInfo pluginInfo = WxPluginUtil.getPluginInfoFromFile(file);
            L.i(LOG_TAG, "deleting old version for plugin: " + pluginInfo.name);
            // 删除旧版本插件
            File pluginDir = new File(Constant.PLUGIN_PATH);
            File[] files = pluginDir.listFiles();
            for (File pluginFile : files) {
                if (pluginFile.getName().contains(WxPluginUtil.getPluginInfoFromFile(file).name) &&
                        !pluginFile.getName().equals(file.getName())) {
                    pluginFile.delete();
                    L.i(LOG_TAG, "1 old version deleted: " + pluginFile.getPath());
                }
            }

            WxMonitorUtil.doMonitor(WxMonitorUtil.WX_PLUGIN_REPLACE_SUCCESS);
        } else {
            WxMonitorUtil.doMonitor(WxMonitorUtil.WX_PLUGIN_REPLACE_FAILED);
        }
    }

    private void loadPresetPlugin() {
        for (WxPluginInfo plugin : mPresetPluginMap.keySet()) {
            if (mPluginMap.containsKey(plugin.name)) {
                L.d(LOG_TAG, String.format("a newer verison of preset plugin %s(%s) has already " +
                                "been loaded, loaded version: %s", plugin.name, plugin.version,
                        getPluginVersionName(plugin.name)));
                return;
            }

            Class clazz = mPresetPluginMap.get(plugin);
            try {
                addPlugin(plugin, (WxPlugin) clazz.newInstance(), WxLoadedPluginInfo
                        .PLUGIN_LOAD_TYPE.PRESET);
                WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_LOAD_SUCCESS_PRESET,
                        plugin);
            } catch (IllegalAccessException e) {
                L.e(LOG_TAG, String.format("load preset plugin %s(%s) encountered error: %s",
                        plugin.name, plugin.version, e.toString()));
                WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_LOAD_FAILED_PRESET,
                        plugin);

            } catch (InstantiationException e) {
                L.e(LOG_TAG, String.format("load preset plugin %s(%s) encountered error: %s",
                        plugin.name, plugin.version, e.toString()));
                WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_LOAD_FAILED_PRESET,
                        plugin);
            }
        }
    }

    /**
     * 装载插件文件
     *
     * @param file     需要装载的文件
     * @param loadType 装载类型, 用于区分监控
     * @return
     */
    private boolean loadPluginFile(File file, WxLoadedPluginInfo.PLUGIN_LOAD_TYPE loadType) {
        // 解析插件信息
        WxPluginInfo info = WxPluginUtil.getPluginInfoFromFile(file);
        if (TextUtils.isEmpty(info.name)) {
            file.delete();
            L.e(LOG_TAG, "cannot resolve plugin info from file: " + file.getPath() + ", delete");
            monitorLoadPluginFileFailed(info, loadType);
            return false;
        }

        // 如果已经装载过同token的插件, 跳过装载
        if (null != mPluginMap.get(info.name)) {
            return false;
        }

        // 对于非插件debug或预置插件装载, 进行插件版本检查
        if (WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.TEST != loadType
                && WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.PRESET != loadType) {
            if (isNewerPresetPluginVersionExists(info)) {
                // 如果有更高版本的预置插件存在, 删除当前的插件文件
                L.i(LOG_TAG, "a newer preset plugin exists, deleting current plugin: "
                        + info.toString());
                file.delete();
                return false;
            }
        }

        ClassLoader loader = new DexClassLoader(file.getPath(), mDexPath, mLibPath,
                WxPluginManager.class.getClassLoader());

        try {
            String strPkgName = WxPluginUtil.getPluginPackageName(info);
            L.i(LOG_TAG, "load plugin class: " + strPkgName + ", loadType = " + loadType);
            Class<?> clsPlugin = loader.loadClass(strPkgName);
            WxPlugin plugin = (WxPlugin) clsPlugin.newInstance();
            addPlugin(info, plugin, loadType);
            monitorLoadPluginFileSuccess(info, loadType);
            return true;
        } catch (Exception e) {
            L.e(LOG_TAG, "load class failed: " + e.toString());
            monitorLoadPluginFileFailed(info, loadType);
        }

        return false;
    }

    /**
     * 检查预置插件列表中是否存在更高版本的指定插件
     * <p>
     * 装载本地插件和预置插件之前有必要针对指定插件做版本检查, 确认预置插件列表中不包含指定插件的更高版本后再
     * 进行装载. 因为车载微信客户端升级时不会清空插件缓存目录, 可能版本升级后预置插件版本比已下载的对应插件版本
     * 要高, 但此时按默认的装载顺序优先装载了已下载插件, 从而引发最新版本插件没有被装载的问题.
     *
     * @param info
     * @return
     */
    private boolean isNewerPresetPluginVersionExists(WxPluginInfo info) {
        for (WxPluginInfo presetInfo : mPresetPluginMap.keySet()) {
            if (presetInfo.name.equals(info.name)) {
                return WxPluginUtil.isNewerVersion(info.version, presetInfo.version);
            }
        }

        return false;
    }

    private void monitorLoadPluginFileSuccess(WxPluginInfo info,
                                              WxLoadedPluginInfo.PLUGIN_LOAD_TYPE loadType) {
        if (WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.LOAD_NEW == loadType) {
            WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_LOAD_SUCCESS_NEW, info);
        } else if (WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.LOAD == loadType) {
            WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_LOAD_SUCCESS, info);
        } else if (WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.PRESET == loadType) {
            WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_LOAD_SUCCESS_PRESET, info);
        }
    }

    private void monitorLoadPluginFileFailed(WxPluginInfo info,
                                             WxLoadedPluginInfo.PLUGIN_LOAD_TYPE loadType) {
        if (WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.LOAD_NEW == loadType) {
            WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_LOAD_FAILED_NEW, info);
        } else if (WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.LOAD == loadType) {
            WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_LOAD_FAILED, info);
        } else if (WxLoadedPluginInfo.PLUGIN_LOAD_TYPE.PRESET == loadType) {
            WxMonitorUtil.monitorPluginAction(WxMonitorUtil.WX_PLUGIN_LOAD_FAILED_PRESET, info);
        }
    }

    public static WxPluginManager getInstance() {
        return sInstance;
    }

    /**
     * 添加插件
     *
     * @param plugin
     */
    public void addPlugin(WxPluginInfo pluginInfo, WxPlugin plugin,
                          WxLoadedPluginInfo.PLUGIN_LOAD_TYPE loadType) {
        mPluginMap.put(plugin.getToken(), new WxLoadedPluginInfo(pluginInfo, plugin, loadType));
    }

    /**
     * 获取当前加载的插件列表
     *
     * @return
     */
    public WxLoadedPluginInfo[] getPluginList() {
        return mPluginMap.values().toArray(new WxLoadedPluginInfo[]{});
    }

    public String getPluginVersionName(String token) {
        // 首先检查已装载插件列表
        WxLoadedPluginInfo plugin = mPluginMap.get(token);
        if (null != plugin) {
            return plugin.getPluginInfo().version;
        }

        // 检查预置插件列表
        for (WxPluginInfo info : mPresetPluginMap.keySet()) {
            if (info.name.equals(token)) {
                return info.version;
            }
        }

        return "0";
    }

    /**
     * 对插件发起调用
     *
     * @param token 插件token, 传空对所有plugin发起
     * @param cmd   操作码
     * @param args  操作参数
     */
    public void invokePlugin(String token, String cmd, Object... args) {
        L.d(LOG_TAG, "invokePlugin::token = " + token + ", cmd = " + cmd);
        if (TextUtils.isEmpty(token)) {
            for (WxLoadedPluginInfo plugin : mPluginMap.values()) {
                plugin.getPlugin().invoke(cmd, args);
            }

        } else if (!mPluginMap.containsKey(token)) {
            L.e(LOG_TAG, "invokePlugin::cannot find plugin for token: " + token);
        } else {
            mPluginMap.get(token).getPlugin().invoke(cmd, args);
        }
    }

    private PluginManager.CommandProcessor mCmdProcessor = new PluginManager.CommandProcessor() {
        @Override
        public Object invoke(String command, Object[] args) {
            if ("dispatch_event".equals(command)) {
                String action = (String) args[0];
                Object data = args.length > 1 ? args[1] : null;
                Dispatcher.get().dispatch(new Action<>(action, data));
            } else if ("logd".equals(command)) {
                L.d((String) args[0], (String) args[1]);
            } else if ("loge".equals(command)) {
                L.e((String) args[0], (String) args[1]);
            } else if ("logi".equals(command)) {
                L.i((String) args[0], (String) args[1]);
            } else if ("logw".equals(command)) {
                L.w((String) args[0], (String) args[1]);
            } else if ("logf".equals(command)) {
                L.f((String) args[0], (String) args[1]);
            } else if ("monitor".equals(command)) {
                WxMonitorUtil.doMonitor((String) args[0]);
            } else if ("run_task".equals(command)) {
                runTask((int) args[0], (Runnable) args[1], (int) args[2]);
            } else if ("remove_task".equals(command)) {
                removeTask((int) args[0], (Runnable) args[1]);
            } else if ("invoke_plugin".equals(command)) {
                String token = (String) args[0];
                String cmd = (String) args[1];
                Object[] invokeArgs = new Object[args.length - 2];
                System.arraycopy(args, 2, invokeArgs, 0, args.length - 2);
                invokePlugin(token, cmd, invokeArgs);
            } else {
                L.e(LOG_TAG, "invokePluginManager::unsupported invoke command: " + command);
            }

            return null;
        }
    };

    private void runTask(int threadType, Runnable runnable, int delay) {
        switch (threadType) {
            case PluginTaskRunner.TASK_TYPE_UI_GROUND:
                AppLogic.runOnUiGround(runnable, delay);
                break;

            case PluginTaskRunner.TASK_TYPE_BACK_GROUND:
                AppLogic.runOnBackGround(runnable, delay);
                break;

            case PluginTaskRunner.TASK_TYPE_SLOW_GROUND:
                AppLogic.runOnSlowGround(runnable, delay);
                break;

            default:
                L.e(LOG_TAG, "runTask failed: cannot determine thread for type " + threadType);
                break;
        }
    }

    private void removeTask(int threadType, Runnable runnable) {
        switch (threadType) {
            case PluginTaskRunner.TASK_TYPE_UI_GROUND:
                AppLogic.removeUiGroundCallback(runnable);
                break;

            case PluginTaskRunner.TASK_TYPE_BACK_GROUND:
                AppLogic.removeBackGroundCallback(runnable);
                break;

            case PluginTaskRunner.TASK_TYPE_SLOW_GROUND:
                AppLogic.removeSlowGroundCallback(runnable);
                break;

            default:
                L.e(LOG_TAG, "removeTask failed: cannot determine thread for type " + threadType);
                break;
        }
    }
}
