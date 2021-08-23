package com.txznet.comm.remote.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import android.os.Environment;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.innernet.UiInnerNet;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.version.ApkVersion;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.plugin.PluginLoader;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.runnables.Runnable2;

public class PluginUtil {
	private static final String BASIC_PATH = Environment
			.getExternalStorageDirectory().getPath() + "/txz/plugin/";
	private static final String SUFFIX_CHK = ".chk";
	private static final String SUFFIX_PLUGIN = ".jar";
	private static final String TAG = "Plugin:";

	public static void handleDownLoadedPlugin(byte[] data) {
		try {
			UiInnerNet.PluginFile pluginFile = UiInnerNet.PluginFile
					.parseFrom(data);
			creatChkFile(pluginFile);
			if (pluginFile.uint32Type != UiInnerNet.PLUGIN_CURRENT) {
				HashSet<String> files = new HashSet<String>();
				files.add(getPluginPath(pluginFile, SUFFIX_PLUGIN));
				AppLogicBase.putFileIntoResetList(0, files);
			}
			switch (pluginFile.uint32Type) {
			case UiInnerNet.PLUGIN_START:
				if (pluginFile.bReboot) {
					ServiceManager.getInstance().sendInvoke(
							pluginFile.strPackage, "comm.update.restart", null,
							null);
				}
				break;
			case UiInnerNet.PLUGIN_LOAD:
			case UiInnerNet.PLUGIN_CURRENT:
				PluginLoader.loadPlugin(
						getPluginPath(pluginFile, SUFFIX_PLUGIN),
						pluginFile.strName, pluginFile.strData);
			}
		} catch (InvalidProtocolBufferNanoException e) {
			e.printStackTrace();
			LogUtil.loge(TAG + "download plugin error", e);
		}

	}

	private static void creatChkFile(UiInnerNet.PluginFile pluginFile) {
		String chkString = "string" + new String(pluginFile.strMd5)
				+ pluginFile.strPackage + pluginFile.uint32Size
				+ ApkVersion.versionName;
		String chk = MD5Util.generateMD5(chkString);
		String chkFilePath = getPluginPath(pluginFile, SUFFIX_CHK);
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(new File(chkFilePath));
			outputStream.write(chk.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			LogUtil.loge(TAG + "create check file failed", e);
		} finally {
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (IOException e) {
			}
		}
	}

	public static boolean checkPlugin(String path, String strPackage) {
		File file = new File(path);
		String md5 = MD5Util.generateMD5(file);

		char[] charChkMd5 = new char[32];
		File chkFile = new File(getChkPathByFilePath(path));
		FileInputStream inStream = null;
		InputStreamReader reader = null;
		try {
			inStream = new FileInputStream(chkFile);
			reader = new InputStreamReader(inStream);
			reader.read(charChkMd5);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
				if (inStream != null)
					inStream.close();
			} catch (IOException e) {
			}
		}
		if (String.valueOf(charChkMd5).equals(
				MD5Util.generateMD5("string" + md5 + strPackage + file.length()
						+ ApkVersion.versionName)))
			return true;
		if (chkFile.exists())
			chkFile.delete();
		return false;
	}

	private static String getPluginPath(UiInnerNet.PluginFile pluginFile,
			String suffix) {
		if (pluginFile.uint32Type == UiInnerNet.PLUGIN_CURRENT)
			return BASIC_PATH + pluginFile.strPackage + "/current/"
					+ pluginFile.strName + suffix;
		else
			return BASIC_PATH + pluginFile.strPackage + "/"
					+ pluginFile.strName + suffix;
	}

	private static String getChkPathByFilePath(String path) {
		int place = path.lastIndexOf('.');
		return path.substring(0, place) + ".chk";
	}

	public static void loadDirectoryPlugin(String directory, String packageName) {
		File file = new File(directory);
		if (!file.exists() || !file.isDirectory()) {
			LogUtil.logd("plugin:no directory");
			return;
		}
		File[] jarFiles = file.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".jar"))
					return true;
				return false;
			}
		});
		if (jarFiles == null || jarFiles.length == 0) {
			LogUtil.logd("plugin:no file");
			return;
		}
		for (int i = 0; i < jarFiles.length; i++) {
			LogUtil.logd("plugin:fileName=" + jarFiles[i].getPath());
			if (checkPlugin(jarFiles[i].getPath(), packageName)) {
				PluginLoader.loadPlugin(jarFiles[i].getPath(),
						jarFiles[i].getName().substring(0, jarFiles[i].getName().lastIndexOf('.')), null);
			} else {
				LogUtil.logd("plugin:check plugin failed, delete");
				jarFiles[i].delete();
			}

		}
	}

	/**
	 * 启动时检验实时包，如果校验不通过就删除该实时包，防止实时包一直存在占用存储空间
	 * @param currentPluginPath
	 * @param packageName
	 */
	public static void checkCurrentPlugin(String currentPluginPath, String packageName){
		File file = new File(currentPluginPath);
		if(!file.exists() || !file.isDirectory()){
			LogUtil.logd("plugin:no current plugin");
			return;
		}
		File[] jarFiles = file.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				if (filename.endsWith(".jar"))
					return true;
				return false;
			}
		});
		if (jarFiles == null || jarFiles.length == 0) {
			LogUtil.logd("plugin:no current plugin");
			return;
		}
		for (int i = 0; i < jarFiles.length; i++) {
			LogUtil.logd("plugin:current plugin fileName=" + jarFiles[i].getPath());
			if (!checkPlugin(jarFiles[i].getPath(), packageName)) {
				LogUtil.logd("plugin:check plugin failed, delete");
				jarFiles[i].delete();
			}

		}
	}

	//启动自动加载插件
	private static boolean bLoadOnStart = false;

    public static void loadPluginOnStart(String strPackage, Runnable overCallback) {
        if (bLoadOnStart) {
            return;
        }
        bLoadOnStart = true;

        AppLogicBase.runOnSlowGround(new Runnable2<String, Runnable>(strPackage, overCallback) {
            @Override
            public void run() {
                checkCurrentPlugin(BASIC_PATH + "/" + mP1 + "/current", mP1);
                LogUtil.logd("plugin:load");
                loadDirectoryPlugin(BASIC_PATH + "/" + mP1, mP1);
                loadDirectoryPlugin(BASIC_PATH + "/comm", "comm");
                if (null != mP2) {
                    mP2.run();
                }
            }
        }, 0);
    }
}
