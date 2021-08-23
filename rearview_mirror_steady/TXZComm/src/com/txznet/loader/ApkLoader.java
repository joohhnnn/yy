package com.txznet.loader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.util.Log;

import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.version.ApkVersion;
import com.txznet.comm.version.FactoryVersion;
import com.txznet.comm.version.TXZVersion;
import com.txznet.txz.util.NativeHelper;
import com.txznet.txz.util.TXZCommUtil;

public class ApkLoader {
	public static final String TAG = "TXZAppLoader";

	public static void replaceObjectMember(String className, Object obj,
			String memName, Object memObj) {
		try {
			Class<?> cls = Class.forName(className);
			replaceObjectMember(cls, obj, memName, memObj);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException("Load app error: replace " + memName
					+ " for " + obj.toString() + " error");
		}
	}

	public static void replaceObjectMember(Class<?> cls, Object obj,
			String memName, Object memObj) {
		try {
			Field f = cls.getDeclaredField(memName);
			f.setAccessible(true);
			f.set(obj, memObj);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Load app error: replace " + memName
					+ " for " + obj.toString() + " error");
		}
	}

	public static Object getStaticMember(ClassLoader loader, String className, String memName) {
		try {
			Class<?> clazz = loader.loadClass(className);
			Field f = clazz.getDeclaredField(memName);
			f.setAccessible(true);
			return f.get(null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static Object getObjectMember(Class<?> clazz, Object obj, String memName) {
		try {
			Field f = clazz.getDeclaredField(memName);
			f.setAccessible(true);
			return f.get(clazz);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
	}
	}
	
	public static Object getAppMember(Application app, String memName) {
		try {
			Field f = app.getClass().getDeclaredField(memName);
			f.setAccessible(true);
			return f.get(app);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Load app error: get " + memName
					+ " from " + app.toString() + " error");
		}
	}

	// 获取目标app加载程序的版本号
	public static String getAppLoaderVersion(Application obj) {
		try {
			Class<?> app = obj.getClass();
			Field f = app.getDeclaredField("VERSION");
			return (String) f.get(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void readVersionFromApk(Context context,
			String archiveFilePath) {
		try {
			PackageManager pm = context.getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(archiveFilePath,
					0);
			ApkVersion.versionName = packageInfo.versionName;
			ApkVersion.versionCode = packageInfo.versionCode;
			ApkVersion.channelName = TXZCommUtil.getAppMetaData(context, BaseApplication.KEY_CHANNEL);
			Log.d(TAG, archiveFilePath + " read version result "
					+ packageInfo.versionCode + "-" + packageInfo.versionName +(ApkVersion.channelName == null ? "" :("-" + StringUtils.getHideString(ApkVersion.channelName,3,3))));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 预处理app
	public static void process(Application app, ClassLoader loader) {
		Log.d(TAG, "begin process Application: " + app.toString());
        GlobalContext.set(app);
		String version = getAppLoaderVersion(app);
		Log.d(TAG, "Application Loader Version: " + version);
		if ("1.0".equals(version)) {
			process_1_0(app, loader);
			Log.d(TAG, "end process Application: " + app.toString());
			return;
		}
		throw new RuntimeException(
				"Load app error: unsupport app loader version");
	}

	// 1.0版本处理器
	public static void process_1_0(Application app, ClassLoader loader) {
		// 从配置里读取外部apk路径
		String apkPath = (String) getAppMember(app, "mApkPath");
		String libPath = (String) getAppMember(app, "mLibPath");
		String appPath = app.getApplicationInfo().dataDir;
		String dexPath = (String) getAppMember(app, "mDexPath");

		// 获取application包对象
		Object mLoadedApk = null;
		Field LoadedApk;
		try {
			LoadedApk = Application.class.getDeclaredField("mLoadedApk");
			LoadedApk.setAccessible(true);
			mLoadedApk = LoadedApk.get(app);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mLoadedApk == null) {
			throw new RuntimeException(
					"Load app error: can not find apk object");
		}

		// 获取资源管理器
		AssetManager mAssetManager = null;
		try {
			mAssetManager = AssetManager.class.newInstance();
			Method addAssetPath = AssetManager.class.getMethod("addAssetPath",
					String.class);
			addAssetPath.invoke(mAssetManager, apkPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mAssetManager == null) {
			throw new RuntimeException(
					"Load app error: create asset manager error");
		}

		// 获取类装载器
		ClassLoader mClassLoader = loader;

		// 如果是老版本的装载器，则使用新的方法重新装载
		if (!mClassLoader.getClass().getName()
				.equals("com.txznet.loader.TXZDexClassLoader")) {
//			mClassLoader = new TXZDexClassLoader(apkPath,
//					appPath + "/dexfiles", dexPath, libPath, loader.getParent());
			NativeHelper.replaceDexClassLoader(
					mClassLoader,
					TXZDexClassLoader.installInnerDexFiles(apkPath, appPath
							+ "/dexfiles", dexPath, libPath,
							mClassLoader.getParent()), libPath);
		}

		// 获取出厂版本信息
		FactoryVersion.ApkPath = app.getApplicationInfo().sourceDir;
		try {
			// 基础包版本号
			PackageInfo info = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
			FactoryVersion.versionName = info.versionName;
			FactoryVersion.versionCode = info.versionCode;
			// comm编译版本号
			Class<?> clsTXZVersion = app.getClassLoader().loadClass("com.txznet.comm.version.TXZVersion");
			FactoryVersion.COMPUTER = (String)getObjectMember(clsTXZVersion, null, "COMPUTER");
			FactoryVersion.PACKTIME = (String)getObjectMember(clsTXZVersion, null, "PACKTIME");
			FactoryVersion.BRANCH = (String)getObjectMember(clsTXZVersion, null, "BRANCH");
			FactoryVersion.SVNVERSION = (Integer)getObjectMember(clsTXZVersion, null, "SVNVERSION");
			try {
				clsTXZVersion = app.getClassLoader().loadClass("com.txznet.comm.version.TXZVersion");
			} catch (Exception e) {
				// 附加TXZ的特定版本号
				clsTXZVersion = app.getClassLoader().loadClass("com.txznet.txz.module.version.TXZVersion");
				FactoryVersion.COMPUTER_TXZ = (String)getObjectMember(clsTXZVersion, null, "COMPUTER");
				FactoryVersion.PACKTIME_TXZ = (String)getObjectMember(clsTXZVersion, null, "PACKTIME");
				FactoryVersion.SVNVERSION_TXZ = (Integer)getObjectMember(clsTXZVersion, null, "SVNVERSION");
			}
		} catch (Exception e) {
		}
		
		// 获取资源
		Resources superRes = app.getResources();
		Resources mResources = new Resources(mAssetManager,
				superRes.getDisplayMetrics(), superRes.getConfiguration());

		Theme mTheme = mResources.newTheme();
		mTheme.setTo(app.getTheme());

		// 替换操作
		replaceObjectMember("android.app.LoadedApk", mLoadedApk, "mResources",
				mResources);
		replaceObjectMember("android.app.LoadedApk", mLoadedApk,
				"mClassLoader", mClassLoader);
		replaceObjectMember(app.getClass(), app, "mClassLoader", mClassLoader);
		replaceObjectMember(app.getClass(), app, "mAssetManager", mAssetManager);
		replaceObjectMember(app.getClass(), app, "mResources", mResources);
		replaceObjectMember(app.getClass(), app, "mTheme", mTheme);
		app.getApplicationInfo().sourceDir = apkPath;
		readVersionFromApk(app, apkPath);

		// 创建App逻辑对象，这个要准备好环境后最后执行
		Object mAppLogic = null;
		try {
			Class<?> clsAppLogic;
			try {
				clsAppLogic = mClassLoader
						.loadClass("com.txznet.loader.AppLogic");
			} catch (ClassNotFoundException e1) {
				clsAppLogic = mClassLoader
						.loadClass("com.txznet.loader.AppLogicDefault");
			}
			mAppLogic = clsAppLogic.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mAppLogic == null) {
			throw new RuntimeException(
					"Load app error: create app logic object error");
		}
		replaceObjectMember(app.getClass(), app, "mAppLogic", mAppLogic);
	}
}
