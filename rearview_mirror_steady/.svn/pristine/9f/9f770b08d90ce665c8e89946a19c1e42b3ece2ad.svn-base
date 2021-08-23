package com.txznet.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import dalvik.system.DexClassLoader;

public class MyApplication extends Application {
	public final static String VERSION = "1.0"; // 版本号，替换端根据版本号进行选择
	public final static String TAG = "TXZAppLoader" + VERSION;

	public final static String ASSETS_BIN = "txz.jet"; // 内置包名
	public final static String SP_SUFFIX = ".ApkLoader"; // sp存储后缀
	public final static String SP_KEY_APK = "apk";
	public final static String SP_KEY_TIME = "time";
	public final static String SP_KEY_SIZE = "size";
	public final static String SP_KEY_LAUNCH_TIMES = "launchTimes";

	// 运行异常判断常量，最近5次启动都发生在5分钟内，则认为异常，回滚版本
	public static long MIN_RUN_TIME = 1 * 60 * 1000;
	public static int MIN_RUN_COUNT = 5;
	public static String WORK_SPACE = Environment.getExternalStorageDirectory()
			.getPath() + "/txz/loader";

	public void showFieds(String name) {
		try {
			Class<?> cls = Class.forName(name);
			showFieds(cls);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void showFieds(Class<?> cls) {
		for (Field f : cls.getDeclaredFields()) {
			Log.d(TAG, cls.getName() + " Member: " + f.getName() + "="
					+ f.getType().toString());
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////
	static String mApkPath;
	static String mDexPath;
	static String mLibPath;

	// 重置
	private void reset() {
		mApkPath = null;
		mDexPath = null;
		mLibPath = null;
		mClassLoader = null;
		mAppLogic = null;
		mAssetManager = null;
		mResources = null;
		mTheme = null;
	}

	private boolean loadOuter() {
		reset();

		// 创建动态类装载优化目录
		mDexPath = this.getApplicationInfo().dataDir + "/dex";
		new File(mDexPath).mkdirs();

		// 从配置里读取外部apk路径
		mApkPath = mSharedPreferences.getString(SP_KEY_APK, null);

		// 没有设置路径
		if (TextUtils.isEmpty(mApkPath)) {
			Log.w(TAG, "load outter failed: no outter data setting");
			return false;
		}

		// 判断路径的有效性
		File fApk = new File(mApkPath);
		if (!fApk.exists()
				|| fApk.length() != mSharedPreferences.getLong(SP_KEY_SIZE, -1)
				|| fApk.lastModified() != mSharedPreferences.getLong(
						SP_KEY_TIME, -1)) {
			Log.w(TAG, "load outter failed: check data failed");
			return false;
		}

		mLibPath = WORK_SPACE + this.getApplicationInfo().packageName + "/lib/";
		new File(mLibPath).mkdirs();

		// 替换类装载器
		ClassLoader loader = new DexClassLoader(mApkPath, mDexPath, mLibPath,
				super.getClassLoader());
		try {
			Class<?> ApkLoader = loader
					.loadClass("com.txznet.loader.ApkLoader");
			Method m = ApkLoader
					.getDeclaredMethod("process", Application.class);
			m.invoke(null, this);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "load outter failed: process failed");
			return false;
		}
		return true;
	}

	private boolean loadInner() {
		reset();

		Class<?> clsAppLogic = null;
		try {
			clsAppLogic = Class.forName("com.txznet.loader.AppLogic");
		} catch (ClassNotFoundException e1) {
			try {
				clsAppLogic = Class
						.forName("com.txznet.loader.AppLogicDefault");
			} catch (ClassNotFoundException e2) {
				Log.w(TAG, "load inner failed: load logic class failed");
				return false;
			}
		}
		try {
			mAppLogic = clsAppLogic.newInstance();
		} catch (Exception e) {
			Log.w(TAG, "load inner failed: create logic instance failed");
			return false;
		}
		return true;
	}

	private boolean loadAssets() {
		reset();

		String packageName = this.getApplicationInfo().packageName;
		// 创建动态类装载优化目录
		mDexPath = this.getApplicationInfo().dataDir + "/dex";
		new File(mDexPath).mkdirs();

		// 释放内部的assets数据文件
		String tmpDir = WORK_SPACE + "tmp/";
		mApkPath = tmpDir + "/" + packageName + ".apk";
		File f = new File(mApkPath);
		AssetFileDescriptor fd = null;
		InputStream in = null;
		OutputStream out = null;
		try {
			fd = this.getAssets().openFd(ASSETS_BIN);
			boolean needUnzip = true;
			if (f.exists()) {
				if (f.length() == fd.getLength()) {
					needUnzip = false;
				}
			}
			if (needUnzip) {
				new File(tmpDir).mkdirs();
				in = fd.createInputStream();
				out = new FileOutputStream(f);
				int l = 0;
				byte[] buf = new byte[1024 * 1024];
				while ((l = in.read(buf)) > 0) {
					out.write(buf, 0, l);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "load assets failed: unzip assets data failed");
			return false;
		} finally {
			try {
				if (out != null)
					out.close();
				if (in != null)
					in.close();
				if (fd != null)
					fd.close();
			} catch (Exception e) {
			}
		}

		mLibPath = WORK_SPACE + this.getApplicationInfo().packageName + "/lib/";
		new File(mLibPath).mkdirs();

		// 替换类装载器
		ClassLoader loader = new DexClassLoader(mApkPath, mDexPath, mLibPath,
				super.getClassLoader());
		try {
			Class<?> ApkLoader = loader
					.loadClass("com.txznet.loader.ApkLoader");
			Method m = ApkLoader
					.getDeclaredMethod("process", Application.class);
			m.invoke(null, this);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "load assets failed: proccess assets data failed");
			return false;
		}

		return true;
	}

	SharedPreferences mSharedPreferences = null;

	private void process() {
		String packageName = this.getApplicationInfo().packageName;
		// 从配置里读取外部apk路径
		mSharedPreferences = this.getSharedPreferences(packageName + SP_SUFFIX,
				Context.MODE_PRIVATE);
		WORK_SPACE = mSharedPreferences.getString("WORK_SPACE", WORK_SPACE);
		MIN_RUN_TIME = mSharedPreferences.getLong("MIN_RUN_TIME", MIN_RUN_TIME);
		MIN_RUN_COUNT = mSharedPreferences.getInt("MIN_RUN_COUNT",
				MIN_RUN_COUNT);

		// 判断启动时间，是否存在异常重启
		String launchTimeStr = mSharedPreferences.getString(
				SP_KEY_LAUNCH_TIMES, "");
		String[] launchTimes = launchTimeStr.split(";");
		boolean bNeedRollback = false;
		if (launchTimes != null && launchTimes.length >= MIN_RUN_COUNT) {
			bNeedRollback = true;
			for (String t : launchTimes) {
				long tm = Long.parseLong(t);
				if (tm < System.currentTimeMillis() - MIN_RUN_TIME) {
					bNeedRollback = false;
					break;
				}
			}
		}

		// 记录启动时间
		StringBuilder sb = new StringBuilder();
		for (int i = (launchTimes.length >= MIN_RUN_COUNT) ? MIN_RUN_COUNT
				: (launchTimes.length); i > 0; --i) {
			sb.append(launchTimes[launchTimes.length - i]);
			sb.append(';');
		}
		sb.append(System.currentTimeMillis());
		Editor editor = mSharedPreferences.edit();
		editor.putString(SP_KEY_LAUNCH_TIMES, sb.toString());
		editor.commit();

		do {
			if (bNeedRollback) {
				Log.w(TAG, "application need rollback");
			} else {
				try {
					if (loadOuter())
						break;
				} catch (Exception e) {
				}
			}

			// 加载外部数据异常时，强制切换到内部执行
			editor.putString(SP_KEY_APK, "");
			editor.commit();

			try {
				if (loadInner())
					break;
			} catch (Exception e) {
			}

			try {
				if (loadAssets())
					break;
			} catch (Exception e) {
			}

			// 全部入口加载失败
			throw new RuntimeException("load application failed");
		} while (false);

		// //////////////////////////////////////////////////////////////////////////////////////////////////

		// 环境准备就绪开始执行真实逻辑
		callAppLogicMethod("onCreate", Application.class, this);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		process();
	}

	@Override
	public void onLowMemory() {
		callAppLogicMethod("onLowMemory");

		super.onLowMemory();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		callAppLogicMethod("onConfigurationChanged", Configuration.class,
				newConfig);
	}

	@Override
	public void onTerminate() {
		callAppLogicMethod("onTerminate");

		super.onTerminate();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);

		callAppLogicMethod("onTrimMemory", int.class, level);
	}

	public void caughtException() {
		callAppLogicMethod("caughtException");
	}

	// ////////////////////////////////////////////////////////////////////////////

	private static Object mAppLogic = null;

	public static void callAppLogicMethod(String name) {
		try {
			Class<?> clsAppLogic = mClassLoader
					.loadClass("com.txznet.loader.AppLogicBase");
			Method m = clsAppLogic.getDeclaredMethod(name);
			m.invoke(mAppLogic);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static <T> void callAppLogicMethod(String name, Class<T> clazz,
			T param) {
		try {
			Class<?> clsAppLogic = mClassLoader
					.loadClass("com.txznet.loader.AppLogicBase");
			Method m = clsAppLogic.getDeclaredMethod(name, clazz);
			m.invoke(mAppLogic, param);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ///////////////////////////////////////////////////////////////

	static ClassLoader mClassLoader;

	@Override
	public ClassLoader getClassLoader() {
		if (mClassLoader != null) {
			return mClassLoader;
		}
		return super.getClassLoader();
	}

	static AssetManager mAssetManager;

	@Override
	public AssetManager getAssets() {
		if (mAssetManager != null) {
			return mAssetManager;
		}
		return super.getAssets();
	}

	static Resources mResources;

	@Override
	public Resources getResources() {
		if (mResources != null) {
			return mResources;
		}
		return super.getResources();
	}

	static ApplicationInfo mApplicationInfo;

	@Override
	public ApplicationInfo getApplicationInfo() {
		if (mApplicationInfo != null) {
			return mApplicationInfo;
		}
		return super.getApplicationInfo();
	}

	static Theme mTheme;

	@Override
	public Theme getTheme() {
		if (mTheme != null) {
			return mTheme;
		}
		return super.getTheme();
	}
}
