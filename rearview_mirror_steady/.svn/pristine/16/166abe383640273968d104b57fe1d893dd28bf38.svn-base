package com.txznet.txz.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.PluginInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.PluginUtil;
import com.txznet.loader.AppLogicBase;

import android.util.Log;
import dalvik.system.DexClassLoader;

/**
 * 插件下载保存策略
 * 
 * @保存路径 /sdcard/txz/plugin/apk包名/入口类名.jar //txz music webchat comm
 * @插件更新 TXZCore提交每个本地软件版本后通过push的方式，通知客户端对应的apk的版本需要下载的插件包信息和MD5
 * @校验方式 MD5校验不通过的插件包删除，插件下载完成校验通过，保存校验文件到jar对应的chk文件，通知对应包装载插件
 * @装载插件 默认直接装载插件，需要额外处理插件装载逻辑的应用在AppLogic中重载onLoadPlugin进行一些处理
 * @升级处理 升级时插件要直接删除
 * @启动装载 校验jar对应的chk文件，校验通过的插件启动时直接装载
 * @插件校验 Hash(目录名+文件数据+入口类名)=校验码，保存校验码到chk文件
 * 
 * @author bihongpi
 *
 */
public class PluginLoader {
	private static String mDexPath = AppLogicBase.getApp().getApplicationInfo().dataDir
			+ "/dex";
	private static String mLibPath = AppLogicBase.getApp().getApplicationInfo().dataDir
			+ "/solibs";
	public static List<UiEquipment.PluginInfo> pluginList = new ArrayList<UiEquipment.PluginInfo>();

	public static String getPackageByPath(String path) {
		int end = path.lastIndexOf('/');
		int start = path.lastIndexOf('/', end - 1);
		return path.substring(start + 1, end);
	}

	public static Object loadPlugin(String path, String className, byte[] data) {
		LogUtil.logd("load plugin " + className + ": " + path);
		new File(mDexPath).mkdirs();
		new File(mLibPath).mkdirs();
		ClassLoader loader = new DexClassLoader(path, mDexPath, mLibPath,
				PluginLoader.class.getClassLoader());
		try {
			Class<?> clsPlugin = loader.loadClass(className);
			IExecPluginVersion objPluginVersion = (IExecPluginVersion) clsPlugin.newInstance();
			switch (objPluginVersion.getPluginInterfaceVersion()) {
			case 1:
			{
				IExecPluginV1 objPlugin = (IExecPluginV1) clsPlugin.newInstance();
				if (objPlugin.getMinSupportCommVersion() > PluginManager.PLUGIN_MGR_VERSION) {
					LogUtil.logd("plugin:" + className + ":" + path
							+ " load err:comm version is too low["
							+ PluginManager.PLUGIN_MGR_VERSION + "]-["
							+ objPlugin.getMinSupportCommVersion() + "]");
					return null;
				}
				PluginInfo info = new PluginInfo();
				info.strName = className;
				info.strPackage = getPackageByPath(path);
				info.strVersion = objPlugin.getVersion();
				pluginList.add(info);
				return objPlugin.execute(loader, path, data);
				// Object objPlugin = clsPlugin.newInstance();
				// Method exec = clsPlugin.getDeclaredMethod("execute",
				// ClassLoader.class, String.class, byte[].class);
				// return exec.invoke(objPlugin, loader, path, data);
			}
			}
			
		} catch (Exception e) {
			LogUtil.loge("plugin:load plugin error", e);
			e.printStackTrace();
		} finally {
			// System.gc();
		}
		return null;
	}

	public static byte[] invokePluginCommand(String packageName,
			String command, byte[] data) {
		try {
			if (command.startsWith("load.")) {
				String param = command.substring("load.".length());
				int n = param.indexOf('|');
				AppLogicBase.getInstance().onLoadPlugin(param.substring(n + 1),
						param.substring(0, n), data);
				return null;
			} else if (command.startsWith("download")) {
				PluginUtil.handleDownLoadedPlugin(data);
				return null;
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 
	 * @param packageName
	 *            需要装载插件的包名
	 * @param path
	 *            插件包路径
	 * @param className
	 *            插件入口类名
	 * @param data
	 *            插件调用数据
	 */
	public static void loadServicePlugin(String packageName, String path,
			String className, byte[] data) {
		// if (GlobalContext.get().getPackageName().equals(packageName)) {
		// loadPlugin(path, className, data);
		// return;
		// }
		ServiceManager.getInstance().sendInvoke(packageName,
				"comm.plugin.load." + className + "|" + path, data, null);
	}
}
