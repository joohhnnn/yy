package com.txznet.loader;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import android.util.Log;

import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.NativeHelper;
import com.txznet.txz.util.NativeHelper.UnzipOption;
import com.txznet.txz.util.TXZFileConfigUtil;

import dalvik.system.DexClassLoader;

public class TXZDexClassLoader extends DexClassLoader {
	public final static String TAG = BaseApplication.TAG;

	String mlibrarySearchPath;
	
	private static Long checkHandlerThreadDelay = null;

	public static ClassLoader installInnerDexFiles(String dexPath,
			String dexUnzipDir, String optimizedDirectory,
			String librarySearchPath, ClassLoader parent) {
		if (dexPath.endsWith(".apk")) {
			// 如果是apk就解压apk里的dex文件进行装载
			if (checkHandlerThreadDelay == null) {
				HashMap<String, String> config = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_CHECK_HANDLER_THREAD_DELAY);
				checkHandlerThreadDelay = 0L;
				if (config != null && config.get(TXZFileConfigUtil.KEY_CHECK_HANDLER_THREAD_DELAY) != null) {
					try {
						checkHandlerThreadDelay = Long.parseLong(config.get(TXZFileConfigUtil.KEY_CHECK_HANDLER_THREAD_DELAY));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				LogUtil.logd("TXZDexClassLoader::" + checkHandlerThreadDelay);
			}
			List<String> dexFiles = NativeHelper.unzipFiles(dexPath,
					new UnzipOption[] { UnzipOption.createUnzipDirOption(
							"assets/dexs/", dexUnzipDir) }, checkHandlerThreadDelay > 0 ? checkHandlerThreadDelay : 5000);

			ClassLoader ret = parent;

			for (String f : dexFiles) {
				Log.d(TAG, "add dex file: " + f);
				// Log.d(TAG, "before add dex: " + mClassLoader.toString());
				ret = new TXZDexClassLoader(f, dexUnzipDir, optimizedDirectory,
						librarySearchPath, ret);
				// Log.d(TAG, "after add dex: " + mClassLoader.toString());
			}
			return ret;
		}
		return parent;
	}

	public static String getODexPath(String dexPath) {
		return dexPath;
	}

	public TXZDexClassLoader(String dexPath, String dexUnzipDir,
			String optimizedDirectory, String librarySearchPath,
			ClassLoader parent) {
		super(getODexPath(dexPath), optimizedDirectory, librarySearchPath,
				installInnerDexFiles(dexPath, dexUnzipDir, optimizedDirectory,
						librarySearchPath, parent));
		mlibrarySearchPath = librarySearchPath;
	}

	@Override
	public String findLibrary(String name) {
		File fLibPath = new File(mlibrarySearchPath, "lib" + name + ".so");
		if (fLibPath.exists()) {
			String ret = fLibPath.getAbsolutePath();
			fLibPath.setExecutable(true, false);
			Log.d(TAG, "find solibs[" + name + "] library: " + ret);
			return ret;
		}
		String def = super.findLibrary(name);
		Log.d(TAG, "find default[" + name + "] library: " + def);
		return def;
	}
}
