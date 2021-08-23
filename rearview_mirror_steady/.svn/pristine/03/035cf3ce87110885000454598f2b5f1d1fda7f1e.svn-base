package com.txznet.comm.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.zip.CRC32;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.update.UpdateCenter;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.version.ApkVersion;
import com.txznet.loader.AppLogicBase;
import com.txznet.loader.TXZDexClassLoader;
import com.txznet.txz.util.NativeHelper;
import com.txznet.txz.util.TXZCommUtil;
import com.txznet.txz.util.TXZFileConfigUtil;


public class BaseApplication extends Application {
	public final static String VERSION = "1.0"; // 版本号，替换端根据版本号进行选择
	public final static String TAG = "TXZAppLoader" + VERSION;

	public final static String ASSETS_BIN = "txz.jet"; // 内置包名
	public final static String SP_SUFFIX = ".ApkLoader"; // sp存储后缀
	public final static String SP_KEY_APK = "apk";
	public final static String SP_KEY_TIME = "time";
	public final static String SP_KEY_SIZE = "size";
	public final static String SP_KEY_LAUNCH_TIMES = "launchTimes";

	public final static String SP_KEY_LOADER_APK_VER = "loader_ver";
	public final static String SP_KEY_LOADER_APK_LEN = "loader_len";
	public final static String SP_KEY_LOADER_APK_CRC = "loader_crc";

	// 运行异常判断常量，最近5次启动都发生在5分钟内，则认为异常，回滚版本
	public static long MIN_RECORD_CLOCK = 180*1000; //开机后多少时间才记录重启时间
	public static long MIN_RUN_TIME = 5 * 60 * 1000;
	public static int MIN_RESET_COUNT = 5;
	public static int MIN_ROLLBACK_COUNT = 10;
	public static String WORK_SPACE = Environment.getExternalStorageDirectory()
			.getPath() + "/.txz/loader";


	//渠道号字段
	public static final String KEY_CHANNEL = "com.txznet.channel";

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
	static String mDexUnzipPath;

	// 重置
	private void reset() {
		mApkPath = null;
		mDexPath = null;
		mDexUnzipPath = null;
		mLibPath = null;
		mClassLoader = null;
		mAppLogic = null;
		mAssetManager = null;
		mResources = null;
		mTheme = null;
	}

	public byte[] getSignInfoFromApk(String archiveFilePath) {
		// try {
		// Class<?> clsApkParser = Class
		// .forName("android.content.pm.PackageParser");
		// Constructor<?> conApkParser = clsApkParser
		// .getConstructor(String.class);
		// Object objApkParser = conApkParser.newInstance(archiveFilePath);
		// DisplayMetrics metrics = new DisplayMetrics();
		// metrics.setToDefaults();
		// File sourceFile = new File(archiveFilePath);
		// Method parsePackage = clsApkParser.getDeclaredMethod(
		// "parsePackage", File.class, String.class,
		// DisplayMetrics.class, int.class);
		// Object objPackage = parsePackage.invoke(objApkParser, sourceFile,
		// archiveFilePath, metrics, 0);
		// Class<?> clsPackage = Class
		// .forName("android.content.pm.PackageParser$Package");
		// Field packageName = clsPackage.getDeclaredField("packageName");
		// packageName.setAccessible(true);
		// String name = (String) packageName.get(objPackage);
		// if (!this.getPackageName().equals(name)) {
		// Log.w(TAG, "load outter failed: check sign name failed");
		// return null;
		// }
		// Method collectCertificates = clsApkParser.getDeclaredMethod(
		// "collectCertificates", clsPackage, int.class);
		// collectCertificates.invoke(objApkParser, objPackage, 0);
		// Field fldSigns = clsPackage.getDeclaredField("mSignatures");
		// fldSigns.setAccessible(true);
		// Signature[] sign = (Signature[]) fldSigns.get(objPackage);
		// return sign[0].toByteArray();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		try {
			PackageInfo packageInfo = super.getPackageManager()
					.getPackageArchiveInfo(archiveFilePath,
							PackageManager.GET_SIGNATURES);
			return packageInfo.signatures[0].toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] getSignInfo(String packName) {
		try {
			PackageInfo packageInfo = super.getPackageManager().getPackageInfo(
					packName, PackageManager.GET_SIGNATURES);
			return packageInfo.signatures[0].toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getProcessName() {
		String currentProcName = "";
		int pid = android.os.Process.myPid();
		ActivityManager manager = (ActivityManager) this
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningAppProcessInfo processInfo : manager
				.getRunningAppProcesses()) {
			if (processInfo.pid == pid) {
				currentProcName = processInfo.processName;
				break;
			}
		}
		return currentProcName;
	}

	static Boolean mIsMainProcess = null;

	public boolean isMainProcess() {
		if (mIsMainProcess == null) {
			mIsMainProcess = getProcessName().equals(
					this.getApplicationInfo().packageName);
		}
		return mIsMainProcess;
	}

	private void readFileFullBytes(FileInputStream f, byte[] bs)
			throws IOException {
		int t = 0;
		while (t < bs.length) {
			int r = f.read(bs, t, bs.length - t);
			t += r;
		}
	}
	
	private class RollbackException extends RuntimeException {
		private static final long serialVersionUID = 3533455453098834184L;

		private Throwable mEx;
		public RollbackException(String info, Throwable ex) {
			super("Rollback excepiton: " + info);
			mEx = ex;
		}
		
		@Override
		public String getLocalizedMessage() {
			if (mEx != null) {
				return super.getLocalizedMessage() + mEx.getLocalizedMessage();
			}
			return super.getLocalizedMessage();
		}
		
		@Override
		public Throwable getCause() {
			if (mEx != null) {
				return mEx.getCause();
			}
			return super.getCause();
		}
		
		@Override
		public String getMessage() {
			if (mEx != null) {
				return super.getMessage() + mEx.getMessage();
			}
			return super.getMessage();
		}
		
		@Override
		public StackTraceElement[] getStackTrace() {
			if (mEx != null) {
				return mEx.getStackTrace();
			}
			return super.getStackTrace();
		}
	}
	
	private void dumpRollbackCrashInfo(String info, Throwable ex) {
		Log.w(TAG, info);
		CrashCommonHandler.dumpExceptionToSDCard(this, Environment
				.getExternalStorageDirectory().getPath()
				+ "/txz/report/", null, new RollbackException(info, ex));
	}

	private boolean loadOuter() {
		reset();

		if (isMainProcess()) {
			String mLoaderApkVer = mSharedPreferences.getString(
					SP_KEY_LOADER_APK_VER, "");
			long mLoaderApkLen = mSharedPreferences.getLong(
					SP_KEY_LOADER_APK_LEN, 0);
			long mLoaderApkCrc = mSharedPreferences.getLong(
					SP_KEY_LOADER_APK_CRC, 0);
			try {
				String mLoaderApkVer2 = AppLogicBase.getVersionString();
				File loaderApkFile = new File(
						this.getApplicationInfo().sourceDir);
				FileInputStream loaderApkFileStream = new FileInputStream(
						loaderApkFile);
				long mLoaderApkLen2 = loaderApkFile.length();
				CRC32 crc32 = new CRC32();
				byte[] bs = new byte[100];
				readFileFullBytes(loaderApkFileStream, bs);
				crc32.update(bs);
				loaderApkFileStream.skip(loaderApkFile.length() / 4);
				readFileFullBytes(loaderApkFileStream, bs);
				crc32.update(bs);
				loaderApkFileStream.skip(loaderApkFile.length() / 4);
				readFileFullBytes(loaderApkFileStream, bs);
				crc32.update(bs);
				loaderApkFileStream.skip(loaderApkFile.length() / 4);
				readFileFullBytes(loaderApkFileStream, bs);
				crc32.update(bs);
				loaderApkFileStream.close();
				long mLoaderApkCrc2 = crc32.getValue();
				if (mLoaderApkLen != mLoaderApkLen2
						|| mLoaderApkCrc != mLoaderApkCrc2
						|| !mLoaderApkVer.equals(mLoaderApkVer2)) {
					Editor e = mSharedPreferences.edit();
					e.putString(SP_KEY_LOADER_APK_VER, mLoaderApkVer2);
					e.putLong(SP_KEY_LOADER_APK_LEN, mLoaderApkLen2);
					e.putLong(SP_KEY_LOADER_APK_CRC, mLoaderApkCrc2);
					e.commit();
					Log.w(TAG, "load outter failed: not match loader config: len["
							+ mLoaderApkLen + "/" + mLoaderApkLen2
							+ "], crc[" + mLoaderApkCrc + "/"
							+ mLoaderApkCrc2 + "], ver[" 
							+ mLoaderApkVer + "/" + mLoaderApkVer2
							+ "]");

					resetUpzipData();
					
					return false;
				}
			} catch (Exception e) {
			}
		}

		// 创建动态类装载优化目录
		mDexPath = this.getApplicationInfo().dataDir + "/dex";
		new File(mDexPath).mkdirs();

		// 从配置里读取外部apk路径
		mApkPath = mSharedPreferences.getString(SP_KEY_APK, null);
		
		if (this.getApplicationInfo().sourceDir.equals(mApkPath)) {
			Log.w(TAG, "load outter failed: same source apk");
			return false;
		}

		// 没有设置路径
		if (TextUtils.isEmpty(mApkPath)) {
			Log.w(TAG, "load outter failed: no outter data setting");
			return false;
		}

		// 判断路径的有效性
		File fApk = new File(mApkPath);
		if (!fApk.exists()) {
			File fApkDir = fApk.getParentFile();
			fApkDir.mkdirs();
			if (fApkDir.getTotalSpace() <= 0) {
				//分区尚未准备好，3秒后尝试重启
				Log.w(TAG, "load outter failed: partition is not ready");
				Editor editor = mSharedPreferences.edit();
				editor.remove(SP_KEY_LAUNCH_TIMES);
				editor.commit();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
				}
				AppLogicBase.exit();
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
				}
				return true;
			}
			dumpRollbackCrashInfo("load outter failed: check data failed - not exist " + fApk.getAbsolutePath(), null);
			return false;
		}
		
		long l1 = fApk.length();
		long l2 = mSharedPreferences.getLong(SP_KEY_SIZE, -1);
		if (l1 != l2) {
			dumpRollbackCrashInfo("load outter failed: check data failed - length not match " + l1 + "/" + l2, null);
			return false;
		}
		
		long t1 = fApk.lastModified();
		long t2 = mSharedPreferences.getLong(SP_KEY_TIME, -1);
		if (t1 != t2) {
			// dumpRollbackCrashInfo("load outter warning: check data failed - time not match " + t1 + "/" + t2, null);
			// return false;
		}

		mLibPath = this.getApplicationInfo().dataDir + "/solibs";
		new File(mLibPath).mkdirs();
		
		mDexUnzipPath = this.getApplicationInfo().dataDir + "/dexfiles";
		new File(mDexUnzipPath).mkdirs();

		// 替换类装载器，谨慎，这里从系统类装载生成，从super生成会导致类优先还是读取的当前包里的
		ClassLoader loader = new TXZDexClassLoader(mApkPath, mDexUnzipPath, mDexPath, mLibPath,
				ClassLoader.getSystemClassLoader());
		
		// NativeHelper.replaceDexClassLoader(this.getClassLoader(), loader, mLibPath);
		
		try {
			Class<?> ApkLoader = loader
					.loadClass("com.txznet.loader.ApkLoader");
			Method m = ApkLoader.getDeclaredMethod("process",
					Application.class, ClassLoader.class);
			m.invoke(null, this, loader);
		} catch (Exception e) {
			e.printStackTrace();
			dumpRollbackCrashInfo("load outter failed: process failed " + e.getClass().getName() + "#" + e.getMessage(), e);
			return false;
		}

		final Application app = this;
		final String apkPath = mApkPath;

		
		// 延迟20s后校验签名文件，校验签名非常费时
		if (isMainProcess()) {
			new Thread() {
				boolean checkSign() {
					try {
						byte[] signApp = getSignInfo(app.getPackageName());
						byte[] signApk = getSignInfoFromApk(apkPath);
						if (signApp == null || signApk == null) {
							// 获取签名失败，重启进程重新获取一次
							Log.d(TAG, "load outter warning: get sign failed " + signApk);
							AppLogicBase.restartProcess();
							return true;
						}
						if (signApp == null || signApk == null
								|| signApp.length != signApk.length) {
							dumpRollbackCrashInfo("load outter failed: check sign length failed", null);
							return false;
						}
						for (int i = 0; i < signApk.length; ++i) {
							if (signApp[i] != signApk[i]) {
								dumpRollbackCrashInfo("load outter failed: check sign data failed", null);
								return false;
							}
						}
					} catch (Exception e) {
						dumpRollbackCrashInfo("load outter failed: check sign exception failed" + e.getClass().getName() + "#" + e.getMessage(), e);
						return false;
					}
					Log.d(TAG, "load outter success: check sign success");
					return true;
				}

				public void run() {
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {
					}
					if (checkSign() == false) {
						UpdateCenter.rollback(app);
						AppLogicBase.restartProcess();
					}
				};
			}.start();
		}

		return true;
	}

	private boolean loadInnerFromApk(String apkPath) {
		reset();
		
		Log.w(TAG, "begin load  inner from apk: " + apkPath);
		
		// 创建动态类装载优化目录
		mDexPath = this.getApplicationInfo().dataDir + "/dex";
		new File(mDexPath).mkdirs();

		// 从配置里读取外部apk路径
		mApkPath = this.getApplicationInfo().sourceDir = apkPath;

		// 没有设置路径
		if (TextUtils.isEmpty(mApkPath)) {
			Log.w(TAG, "load inner from apk failed");
			return false;
		}

		// 判断路径的有效性
		File fApk = new File(mApkPath);
		if (!fApk.exists()) {
			Log.w(TAG, "load inner from apk failed: check data failed - not exist " + fApk.getAbsolutePath());
			return false;
		}
		
		if (NativeHelper.checkZipFileExist(mApkPath, "classes.dex") == false) {
			Log.w(TAG, "load inner from apk failed: classes.dex not found - " + fApk.getAbsolutePath());
			return false;
		}          
		
		mLibPath = this.getApplicationInfo().dataDir + "/solibs";
		new File(mLibPath).mkdirs();
		
		mDexUnzipPath = this.getApplicationInfo().dataDir + "/dexfiles";
		new File(mDexUnzipPath).mkdirs();

		// 替换类装载器，谨慎，这里从系统类装载生成，从super生成会导致类优先还是读取的当前包里的
		ClassLoader loader = new TXZDexClassLoader(mApkPath, mDexUnzipPath, mDexPath, mLibPath,
				ClassLoader.getSystemClassLoader());
		
		// NativeHelper.replaceDexClassLoader(this.getClassLoader(), loader, mLibPath);
		
		try {
			Class<?> ApkLoader = loader
					.loadClass("com.txznet.loader.ApkLoader");
			Method m = ApkLoader.getDeclaredMethod("process",
					Application.class, ClassLoader.class);
			m.invoke(null, this, loader);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "load  inner from apk failed: process failed");
			return false;
		}
		return true;
	}
	
	private boolean loadInner() {
		reset();
		
		mDexPath = this.getApplicationInfo().dataDir + "/dex";
		new File(mDexPath).mkdirs();
		mLibPath = this.getApplicationInfo().dataDir + "/solibs";
		new File(mLibPath).mkdirs();
		mDexUnzipPath = this.getApplicationInfo().dataDir + "/dexfiles";
		new File(mDexUnzipPath).mkdirs();
		mApkPath = this.getApplicationInfo().sourceDir;
		
		try {
			ApplicationInfo appInfo = super.getPackageManager()
					.getApplicationInfo(super.getApplicationInfo().packageName,
							0);
			if (super.getApplicationInfo().sourceDir.equals(appInfo.sourceDir) == false) {
				Log.w(TAG,
						"load inner failed: source dir not match "
								+ super.getApplicationInfo().sourceDir
								+ "|"
								+ appInfo.sourceDir
								+ ", UPDATED="
								+ ((super.getApplicationInfo().flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0));

				UpdateCenter.showRestartDeviceNotification(30000);

				return loadInnerFromApk(appInfo.sourceDir);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		NativeHelper.replaceDexClassLoader(this.getClassLoader(),
				TXZDexClassLoader.installInnerDexFiles(mApkPath, mDexUnzipPath,
						mDexPath, mLibPath, this.getClassLoader()), mLibPath);
		
		Class<?> clsAppLogic = null;
		
		try {
			clsAppLogic = this.getClassLoader().loadClass(
					"com.txznet.loader.AppLogic");
		} catch (ClassNotFoundException e1) {
			try {
				clsAppLogic = this.getClassLoader().loadClass(
						"com.txznet.loader.AppLogicDefault");
			} catch (ClassNotFoundException e2) {
				Log.w(TAG, "load inner failed: load logic class failed");
				return false;
			}
		}
		try {
			mAppLogic = clsAppLogic.newInstance();
			mClassLoader = clsAppLogic.getClassLoader();
		} catch (Exception e) {
			Log.w(TAG, "load inner failed: create logic instance failed");
			return false;
		}

		// 填充当前版本号
		try {
			PackageInfo info = super.getPackageManager().getPackageInfo(
					this.getPackageName(), 0);
			ApkVersion.versionCode = info.versionCode;
			ApkVersion.versionName = info.versionName;
			ApkVersion.channelName = TXZCommUtil.getAppMetaData(this, KEY_CHANNEL);
			Log.d(TAG, "read version result "
					+ ApkVersion.versionCode + "-" + ApkVersion.versionName + ( ApkVersion.channelName == null ? "" :("-" + StringUtils.getHideString(ApkVersion.channelName,3,3))));
		} catch (Exception e) {
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
		String tmpDir = WORK_SPACE + "/tmp";
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

		mLibPath = this.getApplicationInfo().dataDir + "/solibs";
		new File(mLibPath).mkdirs();

		mDexUnzipPath = this.getApplicationInfo().dataDir + "/dexfiles";
		new File(mDexUnzipPath).mkdirs();
		
		// 替换类装载器
		ClassLoader loader = new TXZDexClassLoader(mApkPath, mDexUnzipPath, mDexPath, mLibPath,
				super.getClassLoader());
		try {
			Class<?> ApkLoader = loader
					.loadClass("com.txznet.loader.ApkLoader");
			Method m = ApkLoader
					.getDeclaredMethod("process", Application.class);
			m.invoke(null, this, loader);
		} catch (Exception e) {
			e.printStackTrace();
			Log.w(TAG, "load assets failed: proccess assets data failed");
			return false;
		}

		return true;
	}

	private void clearDir(String path) {
		try {
			File d = new File(path);
			for (File f : d.listFiles()) {
				if (f.isDirectory()) {
					clearDir(f.getPath());
				} else {
					f.delete();
				}
			}
			d.delete();
		} catch (Exception e) {
		}
	}

	private void resetUpzipData() {
		Log.w(TAG, "application need reset upzip data");
		clearDir(this.getApplicationInfo().dataDir + "/solibs");
		clearDir(this.getApplicationInfo().dataDir + "/data");
		clearDir(this.getApplicationInfo().dataDir + "/dexfiles");
	}

	SharedPreferences mSharedPreferences = null;

	private boolean mNeedReset = false;

	public boolean needReset() {
		return mNeedReset;
	}

	private void process() {
		GlobalContext.set(this);
		int level = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.CONSOLE_LOG_LEVEL, -1);
		LogUtil.setConsoleLogLevel(level);
		String packageName = this.getApplicationInfo().packageName;

		// 从配置里读取外部apk路径
		mSharedPreferences = this.getSharedPreferences(packageName + SP_SUFFIX,
				Context.MODE_PRIVATE);
		WORK_SPACE = mSharedPreferences.getString("WORK_SPACE", WORK_SPACE);
		MIN_RUN_TIME = mSharedPreferences.getLong("MIN_RUN_TIME", MIN_RUN_TIME);
		MIN_RESET_COUNT = mSharedPreferences.getInt("MIN_RESET_COUNT",
				MIN_RESET_COUNT);
		MIN_ROLLBACK_COUNT = mSharedPreferences.getInt("MIN_ROLLBACK_COUNT",
				MIN_ROLLBACK_COUNT);

		mNeedReset = false;
		boolean bNeedRollback = false;

		if (isMainProcess()) {
			// 判断启动时间，是否存在异常重启
			String launchTimeStr = mSharedPreferences.getString(
					SP_KEY_LAUNCH_TIMES, "");
			String[] launchTimes = launchTimeStr.split(";");

			if (launchTimes != null && launchTimes.length >= MIN_RESET_COUNT) {
				mNeedReset = true;
				for (int i = launchTimes.length - 1; i >= launchTimes.length
						- MIN_RESET_COUNT; --i) {
					long tm = 0;
					try {
						tm = Long.parseLong(launchTimes[i]);
					} catch (Exception e) {
					}

					if (System.currentTimeMillis() - tm > MIN_RUN_TIME) {
						mNeedReset = false;
						break;
					}
				}
			}

			long cur = System.currentTimeMillis();
			if (launchTimes != null && launchTimes.length >= MIN_ROLLBACK_COUNT) {
				bNeedRollback = true;
				for (int i = launchTimes.length - 1; i >= launchTimes.length
						- MIN_ROLLBACK_COUNT; --i) {
					long tm = 0;
					try {
						tm = Long.parseLong(launchTimes[i]);
					} catch (Exception e) {
					}
					if (cur < tm) {
						// 出现时间反序，则重置时间记录序列，防止设备时间归0，导致判断出错
						launchTimes = null;
						bNeedRollback = false;
						break;
					}
					if (cur - tm > MIN_RUN_TIME) {
						bNeedRollback = false;
						break;
					}
				}
			}

			// 记录启动时间
			StringBuilder sb = new StringBuilder();
			int start = MIN_ROLLBACK_COUNT;
			if (launchTimes == null) {
				start = 0;
			} else if (launchTimes.length < MIN_ROLLBACK_COUNT) {
				start = launchTimes.length;
			}
			for (int i = start; i > 0; --i) {
				sb.append(launchTimes[launchTimes.length - i]);
				sb.append(';');
			}
			// 开机超过3分钟后出现的重启时间才记录，否则可能出现开机时间被还原，统计出错
			if (SystemClock.elapsedRealtime() > MIN_RECORD_CLOCK) {
				sb.append(cur);
				Editor editor = mSharedPreferences.edit();
				editor.putString(SP_KEY_LAUNCH_TIMES, sb.toString());
				editor.commit();
			} else {
				AppLogicBase.clearStartTimeRecord();
			}
		}

		// 重置环境
		if (mNeedReset) {
			resetUpzipData();
		}

		do {
			if (bNeedRollback) {
				Log.w(TAG, "application need rollback");
				Editor editor = mSharedPreferences.edit();
				editor.remove(BaseApplication.SP_KEY_LAUNCH_TIMES); // 升级或回滚时需要把之前启动时间清除
				editor.commit();
				dumpRollbackCrashInfo("restart too many times at few minutes", null);
			} else {
				try {
					if (loadOuter())
						break;
				} catch (Exception e) {
					dumpRollbackCrashInfo("load outer exception", e);
				}
			}

			// 加载外部数据异常时，版本回滚
			UpdateCenter.rollback(this);

			try {
//				if (loadInnerFromApk())
//					break;
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

	static boolean sAlreadyCreateApp = false;
	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "begin create application: main=" + isMainProcess());
		
		if (sAlreadyCreateApp) {
			Log.w(TAG, "already created application");
			return;
		}
		
		sAlreadyCreateApp = true;
		
		process();
	}

	@Override
	public void onLowMemory() {
		Log.w(TAG, "application onLowMemory");
		
		callAppLogicMethod("onLowMemory");

		super.onLowMemory();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		/*
        * 更新下适配分辨率配置
        * 设备Configuration发生改变时, 会回调到Application#onConfigurationChanged方法,
        * 传入的newConfig中的屏幕分辨率配置是设备真实分辨率, 从而导致设置的适配分辨率被还原,
        * 所以在此处重新进行设置.
        * 注: 不能直接修改传入的newConfig, 其他逻辑中都认为传入的config是不可变的,
        * 修改会导致其他问题(如Activity闪屏)
        * */
		int screenWidthDp = ScreenUtils.getScreenWidthDp();
		int screenHeightDp = ScreenUtils.getScreenHeightDp();
		Configuration finalConfig = newConfig;
		// 没有指定适配分辨率的情况下按默认逻辑处理
		if (0 != screenWidthDp && 0 != screenHeightDp) {
			Log.e("!!!!!", String.format("BaseApplication config change: %s x %s", screenWidthDp, screenHeightDp));
			finalConfig = new Configuration(newConfig);
			finalConfig.screenWidthDp = screenWidthDp;
			finalConfig.screenHeightDp = screenHeightDp;
		}

		super.onConfigurationChanged(finalConfig);
		callAppLogicMethod("onConfigurationChanged", Configuration.class,
				finalConfig);
	}

	@Override
	public void onTerminate() {
		Log.w(TAG, "application onTerminate");
		
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

	static Object mAppLogic = null;

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
	
	@Override
	public Intent registerReceiver(BroadcastReceiver receiver,
			IntentFilter filter) {
		AppLogicBase.addReceiver(receiver);
		return super.registerReceiver(receiver, filter);
	}
	
	@Override
	public Intent registerReceiver(BroadcastReceiver receiver,
			IntentFilter filter, String broadcastPermission, Handler scheduler) {
		AppLogicBase.addReceiver(receiver);
		return super.registerReceiver(receiver, filter, broadcastPermission, scheduler);
	}
	
	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {
		if (!AppLogicBase.removeReceiver(receiver)) {
			Log.w(TAG, "Receiver: " + receiver + "not register");
			return;
		}
		super.unregisterReceiver(receiver);
	}
}
