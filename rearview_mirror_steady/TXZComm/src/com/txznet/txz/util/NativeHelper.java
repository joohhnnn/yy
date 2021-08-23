package com.txznet.txz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.version.ApkVersion;
import com.txznet.loader.AppLogicBase;

/**
 * 
 * addLibraryPath
 * 
 * @测试方法 在system/lib下push对应so，安装运行后cat /proc/xxxx/maps|grep
 *       查找对应so，看看装载的是哪个目录下的，如果是solibs下的则通过
 * 
 * @已覆盖验证平台
 * @4.4.2 全志达讯 install+/sys/app/*.apk
 * @6.0.1 高通诺威达 install+/sys/app/*.apk
 * @5.1.1 高通诺威达 install+/sys/app/*.apk
 * @4.4.4 马威尔威仕特 install+/sys/app/*.apk
 * @5.0 普方达 install+无法/sys/app/和/system/app/TXZCore/安装验证
 * @5.0 锐承小蚁yunos install+/system/app/TXZCore/
 * @6.0 迪恩杰 install+/sys/vendor
 * 
 * @author pppi
 *
 */

public class NativeHelper {
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	static void addLibraryPath_DexClassLoader(Context context, String libpath) {
		addLibraryPath_DexClassLoader(context.getClassLoader(), libpath);
		try {
			Field LoadedApk = Application.class.getDeclaredField("mLoadedApk");
			LoadedApk.setAccessible(true);
			Object mLoadedApk = LoadedApk.get(context);
			Class<?> clsLoadedApk = Class.forName("android.app.LoadedApk");
			Field f = clsLoadedApk.getDeclaredField("mClassLoader");
			f.setAccessible(true);
			ClassLoader loader = (ClassLoader) f.get(mLoadedApk);
			addLibraryPath_DexClassLoader(loader, libpath);
		} catch (Exception e) {
		}
	}

	public static void addLibraryPath_PathList(ClassLoader loader, String name,
			String libpath) {
		try {
			Field f = dalvik.system.BaseDexClassLoader.class
					.getDeclaredField("pathList");
			f.setAccessible(true);
			Object pathList = f.get(loader);
			f = pathList.getClass().getDeclaredField(name);
			f.setAccessible(true);
			Object fsobj = f.get(pathList);
			if (fsobj instanceof File[]) {
				File[] fs = (File[]) fsobj;
				File[] nfs = new File[fs.length + 1];
				nfs[0] = new File(libpath);
				for (int i = 0; i < fs.length; ++i) {
					nfs[i + 1] = fs[i];
				}
				f.set(pathList, nfs);
			} else if (fsobj instanceof String[]) {
				String[] fs = (String[]) fsobj;
				String[] nfs = new String[fs.length + 1];
				nfs[0] = libpath + "/";
				for (int i = 0; i < fs.length; ++i) {
					nfs[i + 1] = fs[i];
				}
				f.set(pathList, nfs);
			} else if (fsobj instanceof List) {
				List fs = (List) fsobj;
				if (fs.get(0) instanceof File) {
					fs.add(0, new File(libpath));
				} else if (fs.get(0) instanceof String) {
					fs.add(0, libpath);
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static void addLibraryPath_DexClassLoader(ClassLoader loader,
			String libpath) {
		if (loader == null)
			return;

		addLibraryPath_PathList(loader, "nativeLibraryDirectories", libpath);
		addLibraryPath_PathList(loader, "systemNativeLibraryDirectories",
				libpath);

		try {
			Field f = dalvik.system.BaseDexClassLoader.class
					.getDeclaredField("pathList");
			f.setAccessible(true);
			Object pathList = f.get(loader);
			f = pathList.getClass().getDeclaredField(
					"nativeLibraryPathElements");
			f.setAccessible(true);
			Object elems = f.get(pathList);
			Object elems_new = Array.newInstance(elems.getClass()
					.getComponentType(), Array.getLength(elems) + 1);
			Constructor<?> con = elems.getClass().getComponentType()
					.getDeclaredConstructors()[0];
			con.setAccessible(true);
			Object elem = con.newInstance(new File(libpath), true, null, null);
			Array.set(elems_new, 0, elem);
			for (int i = 0; i < Array.getLength(elems); ++i) {
				Array.set(elems_new, i + 1, Array.get(elems, i));
			}
			f.set(pathList, elems_new);
		} catch (Exception e) {
			// e.printStackTrace();
		}

		try {
			Field f = dalvik.system.DexClassLoader.class
					.getDeclaredField("mLibPaths");
			f.setAccessible(true);
			String[] mLibPaths = (String[]) f.get(loader);
			String[] mLibPaths_new = new String[mLibPaths.length + 1];
			if (libpath.endsWith("/")) {
				mLibPaths_new[0] = libpath;
			} else {
				mLibPaths_new[0] = libpath + "/";
			}
			for (int i = 0; i < mLibPaths.length; ++i) {
				mLibPaths_new[i + 1] = mLibPaths[i];
			}
			f.set(loader, mLibPaths_new);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		addLibraryPath_DexClassLoader(loader.getParent(), libpath);
	}

	public static Object combineArray(Object arrayLhs, Object arrayRhs) {
		if (arrayLhs.getClass().isArray()) {
			Class<?> localClass = arrayLhs.getClass().getComponentType();
			int i = Array.getLength(arrayLhs);
			int j = i + Array.getLength(arrayRhs);
			Object result = Array.newInstance(localClass, j);
			for (int k = 0; k < j; ++k) {
				if (k < i) {
					Array.set(result, k, Array.get(arrayLhs, k));
				} else {
					Array.set(result, k, Array.get(arrayRhs, k - i));
				}
			}
			return result;
		}
		if (arrayLhs instanceof List) {
			try {
				Object result = arrayLhs.getClass().newInstance();
				List lstResult = (List) result;
				List lstLhs = (List) arrayLhs;
				List lstRhs = (List) arrayRhs;
				for (Object o : lstLhs) {
					lstResult.add(o);
				}
				for (Object o : lstRhs) {
					lstResult.add(o);
				}
				return result;
			} catch (Exception e) {
			}

		}
		return arrayRhs;
	}

	public static void insertArrayMember(Object src, Object dst, String mem) {
		try {
			Field f = src.getClass().getDeclaredField(mem);
			f.setAccessible(true);
			f.set(src, combineArray(f.get(dst), f.get(src)));
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	public static void replaceDexClassLoader(ClassLoader loader,
			ClassLoader dest, String libPath) {
		try {
			if (dest == loader) {
				return;
			}
			Field f = dalvik.system.BaseDexClassLoader.class
					.getDeclaredField("pathList");
			f.setAccessible(true);
			Object pathList_loader = f.get(loader);
			
			
			ArrayList<ClassLoader> loaders = new ArrayList<ClassLoader>();
			while (dest != null && dest != loader) {
				loaders.add(dest);
				dest = dest.getParent();
			}
			for (int i =  loaders.size(); i >0 ; --i) {
				Object pathList_dest = f.get(loaders.get(i - 1));
				insertArrayMember(pathList_loader, pathList_dest, "dexElements");
				insertArrayMember(pathList_loader, pathList_dest,
						"nativeLibraryPathElements");
				insertArrayMember(pathList_loader, pathList_dest,
						"nativeLibraryDirectories");
				insertArrayMember(pathList_loader, pathList_dest,
						"systemNativeLibraryDirectories");
			}
			addLibraryPath_DexClassLoader(loader, libPath);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	static void addLibraryPath_SystemEnv(Context context, String libpath) {
		try {
			System.setProperty("java.library.path",
					libpath + ":" + System.getProperty("java.library.path"));
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	static void addLibraryPath_Runtime(Context context, String libpath) {
		try {
			Field f = Runtime.class.getDeclaredField("mLibPaths");
			f.setAccessible(true);
			String[] usr_paths = (String[]) f.get(Runtime.getRuntime());
			String[] my_paths = new String[usr_paths.length + 1];
			my_paths[0] = libpath;
			for (int i = 0; i < usr_paths.length; ++i) {
				my_paths[i + 1] = usr_paths[i];
			}
			f.set(Runtime.getRuntime(), my_paths);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	static void addLibraryPath_ClassLoader(Context context, String libpath) {
		try {
			Field f = ClassLoader.class.getDeclaredField("usr_paths");
			f.setAccessible(true);
			String[] usr_paths = (String[]) f.get(null);
			String[] my_paths = new String[usr_paths.length + 1];
			my_paths[0] = libpath;
			for (int i = 0; i < usr_paths.length; ++i) {
				my_paths[i + 1] = usr_paths[i];
			}
			f.set(null, my_paths);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * 添加本地库查找路径
	 * 
	 * @param context
	 * @param libpath
	 */
	public static void addLibraryPath(Context context, String libpath) {
		addLibraryPath_SystemEnv(context, libpath);
		addLibraryPath_Runtime(context, libpath);
		addLibraryPath_ClassLoader(context, libpath);
		addLibraryPath_DexClassLoader(context, libpath);
	}

	public static boolean checkZipFileExist(String apkPath, String path) {
		try {
			ZipFile zipFile = new ZipFile(apkPath);
			ZipEntry entry = zipFile.getEntry(path);
			boolean ret = (entry != null);
			zipFile.close();
			return ret;
		} catch (Exception e) {
			return false;
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////

	static final int READ_BUFFER_SIZE = 1024 * 1024;
	static final int CHECK_BUFFER_SIZE = 1024 * 1024;

	/**
	 * @param checkHandler
	 * @param zipFile 原始apk文件/升级包/压缩包
	 * @param entry 原始文件路径
	 * @param unzipPath	目的路径
	 * @param delayCheck
	 * @return
	 */
	static boolean unzipFile(CheckHandler checkHandler, ZipFile zipFile,
			ZipEntry entry, String unzipPath, long delayCheck) {
		try {
			File f = new File(unzipPath);
			if (f.exists()) {
				File chk = new File(unzipPath+".chk");
				if (chk.exists()) {
					Properties properties = new Properties();
					FileInputStream fis = new FileInputStream(chk);
					properties.load(fis);
					Set<Object> keySet = properties.keySet();
					if (keySet.contains("size")) {
						int size = Integer.valueOf(properties.getProperty("size"));
						if (size == f.length()) {
							LogUtil.logw("no need unzip new file: " + entry.getName());
							checkUnzipNewFile(checkHandler, zipFile, entry, unzipPath,
									delayCheck);
							return true;
						}
					}
					chk.delete();
					fis.close();
				}
				else if (f.length() == entry.getSize()
				/* && f.lastModified() == entry.getTime() *//* 动态库修改时间会在加载后被修改 */) {
					LogUtil.logw("no need unzip file: " + entry.getName());
					checkUnzipFile(checkHandler, zipFile, entry, unzipPath,
							delayCheck);
					return true;
				}
			}
			LogUtil.logd("begin unzip " + entry.getName() + ": size="
					+ entry.getCompressedSize() + "/" + entry.getSize()
					+ ",time=" + entry.getTime() + ",crc=" + entry.getCrc());
			f.getParentFile().mkdirs();
			FileOutputStream out = new FileOutputStream(f);
			InputStream in = zipFile.getInputStream(entry);
			do {
				byte[] buf = new byte[READ_BUFFER_SIZE];
				int l = 0;
				while ((l = in.read(buf)) >= 0) {
					out.write(buf, 0, l);
				}
			} while (false);
			out.close();
			in.close();
			// f = new File(dst);
			// f.setLastModified(entry.getTime());
			// f.setReadOnly();
			return true;
		} catch (IOException e) {
			// 出现IO错误时需要清理启动时间记录，下次启动时可能io错误就不存在了
			AppLogicBase.clearStartTimeRecord();
			// e.printStackTrace();
			throw new RuntimeException("Load app error: unzip file "
					+ entry.getName() + " error: " + e.getMessage());
		}
	}
	
	private static void checkUnzipNewFile(final CheckHandler checkHandler,
			final ZipFile zipFile, final ZipEntry entry,
			final String unzipPath, long delayCheck) {
		checkHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// 校验crc，不一致的话要求重启重新解压
				FileInputStream chkStream = null;
				File f = new File(unzipPath);
				File chk = new File(unzipPath+".chk");
				try {
					boolean bSame = true;
					// LogUtil.loge("check unzip file begin: " + unzipPath);
					if (!chk.exists()) {
						LogUtil.loge("chk unzip file not exist: " + unzipPath);
						f.delete();
						checkHandler.checkFailed = true;
						return;
					}
					Properties properties = new Properties();
					chkStream = new FileInputStream(chk);
					properties.load(chkStream);
					Set<Object> keySet = properties.keySet();
					if (!keySet.contains("size") || !keySet.contains("key") || !keySet.contains("begin") || !keySet.contains("end")) {
						LogUtil.loge("chk file err: " + unzipPath);
						f.delete();
						chk.delete();
						checkHandler.checkFailed = true;
						return;
					}
					
					int size = Integer.valueOf(properties.getProperty("size"));
					if (size != f.length()) {
						LogUtil.loge("check unzip file size not same: " + unzipPath);
						f.delete();
						chk.delete();
						checkHandler.checkFailed = true;
						return;
					}
					String begin = properties.getProperty("begin");
					String end = properties.getProperty("end");
					String key = properties.getProperty("key");
					if (compareVersion(ApkVersion.versionName,begin) < 0 || 
							compareVersion(end,ApkVersion.versionName) < 0) {
						LogUtil.loge("check unzip file version not right: "
								+ unzipPath);
						f.delete();
						chk.delete();
						checkHandler.checkFailed = true;
						return;
					}
					String md5 = MD5Util.generateMD5(f);
					if (!key.toLowerCase().equals(
							MD5Util.generateMD5(
									"file" + md5 + begin + end + f.length()
											+ f.getName()).toLowerCase())) {
						LogUtil.loge("check unzip file key not right: "
								+ unzipPath);
						f.delete();
						chk.delete();
						checkHandler.checkFailed = true;
						return;
					}
					LogUtil.loge("check unzip file success: " + unzipPath);
				} catch (Exception e) {
					LogUtil.loge("check unzip file exception="
							+ unzipPath);
					f.delete();
					chk.delete();
					checkHandler.checkFailed = true;
					e.printStackTrace();
				} finally {
					if (chkStream != null) {
						try {
							chkStream.close();
						} catch (IOException e) {
						}
					}
				}
			}
		}, delayCheck);
	}
	/**
	 * version1大返回正数 version2大返回负数 相等返回0
	 * @param version1
	 * @param version2
	 * @return
	 */
	private static int compareVersion(String version1,String version2) {
		String[] v1 = version1.split("\\.");
		String[] v2 = version2.split("\\.");
		int compare;
		for (int i=0;i<v1.length;i++) {
			compare = Integer.valueOf(v1[i])-Integer.valueOf(v2[i]);
			if (compare != 0)
				return compare;
		}
		return 0;
	}
	public static class UnzipOption {
		String dataPath;
		String unzipPath;
		boolean unzipSubDir = false;
		boolean unzipTree = false;
		boolean isFile = false;

		void checkPath() {
			if (this.isFile == false) {
				if (!this.dataPath.endsWith("/")) {
					this.dataPath += "/";
				}
				if (!this.unzipPath.endsWith("/")) {
					this.unzipPath += "/";
				}
			}
		}

		// 只解压当前目录
		public static UnzipOption createUnzipDirOption(String dataPath,
				String unzipPath) {
			UnzipOption opt = new UnzipOption();
			opt.dataPath = dataPath;
			opt.unzipPath = unzipPath;
			opt.unzipTree = false;
			opt.unzipSubDir = false;
			opt.isFile = false;
			opt.checkPath();
			return opt;
		}

		// 也解压子目录，但是子目录的会解压到目标目录，不会在目标目录创建子目录
		public static UnzipOption createUnzipSubDirOption(String dataPath,
				String unzipPath) {
			UnzipOption opt = new UnzipOption();
			opt.dataPath = dataPath;
			opt.unzipPath = unzipPath;
			opt.unzipTree = false;
			opt.unzipSubDir = true;
			opt.isFile = false;
			opt.checkPath();
			return opt;
		}

		// 按树形解压
		public static UnzipOption createUnzipTreeOption(String dataPath,
				String unzipPath) {
			UnzipOption opt = new UnzipOption();
			opt.dataPath = dataPath;
			opt.unzipPath = unzipPath;
			opt.unzipTree = true;
			opt.unzipSubDir = true;
			opt.isFile = false;
			opt.checkPath();
			return opt;
		}

		// 解压文件
		public static UnzipOption createUnzipFileOption(String dataPath,
				String unzipPath) {
			UnzipOption opt = new UnzipOption();
			opt.dataPath = dataPath;
			opt.unzipPath = unzipPath;
			opt.unzipSubDir = false;
			opt.unzipTree = false;
			opt.isFile = true;
			opt.checkPath();
			return opt;
		}

		/**
		 * 获取解压路径
		 * 
		 * @param entryName
		 * @return 返回空表示不需要解压
		 */
		String getUnzipPath(String entryName) {
			if (this.isFile) {
				if (entryName.equals(this.dataPath)) {
					return this.unzipPath;
				}
				return null;
			}

			if (this.unzipSubDir) {
				if (entryName.startsWith(this.dataPath)) {
					if (this.unzipTree) {
						return this.unzipPath
								+ entryName.substring(this.dataPath.length());
					}
					String fileName = entryName.substring(entryName
							.lastIndexOf('/') + 1);
					return this.unzipPath + fileName;
				}
				return null;
			}

			String fileName = entryName
					.substring(entryName.lastIndexOf('/') + 1);
			if (entryName.equals(this.dataPath + fileName)) {
				return this.unzipPath + fileName;
			}
			return null;
		}
	};

	public static List<String> unzipFiles(String apkPath, UnzipOption opt,
			long delayCheck) {
		return unzipFiles(apkPath, new UnzipOption[] { opt }, delayCheck);
	}

	static class CheckHandler extends Handler {
		public boolean checkFailed;
		byte[] bufFile = new byte[CHECK_BUFFER_SIZE];
		byte[] bufZip = new byte[CHECK_BUFFER_SIZE];

		public CheckHandler(Looper looper) {
			super(looper);
			checkFailed = false;
		}
	};

	public static List<String> unzipFiles(String apkPath,
			UnzipOption[] options, long delayCheck) {
		try {
			List<String> ret = new ArrayList<String>();

			final HandlerThread checkThread = new HandlerThread("checkThread");
			checkThread.start();
			final CheckHandler checkHandler = new CheckHandler(
					checkThread.getLooper());
			final ZipFile zipFile = new ZipFile(apkPath);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				for (UnzipOption opt : options) {
					String unzipFile = opt.getUnzipPath(entry.getName());
					if (unzipFile == null)
						continue;
					unzipFile(checkHandler, zipFile, entry, unzipFile,
							delayCheck);
					ret.add(unzipFile);
				}
			}
			checkHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						zipFile.close();
					} catch (IOException e) {
					}
					if (checkHandler.checkFailed) {
						LogUtil.loge("check unzip file failed, restarting...");
						AppLogicBase.restartProcess();
					} else {
						LogUtil.logi("check unzip file end");
					}
					checkThread.quit();
				}
			}, delayCheck);

			return ret;
		} catch (IOException e) {
			// 出现IO错误时需要清理启动时间记录，下次启动时可能io错误就不存在了
			AppLogicBase.clearStartTimeRecord();
			// e.printStackTrace();
			throw new RuntimeException("Load app error: unzip [" + apkPath
					+ "] files error: " + e.getMessage());
		}
	}

	public static void unzipLibFiles(String apkPath, String libPath,
			long delayCheck) {
		try {
			final HandlerThread checkThread = new HandlerThread("checkThread");
			checkThread.start();
			final CheckHandler checkHandler = new CheckHandler(
					checkThread.getLooper());
			final ZipFile zipFile = new ZipFile(apkPath);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			Map<String, ZipEntry> mapLibs = new HashMap<String, ZipEntry>();
			String abiType = Build.CPU_ABI.split("\\-")[0];
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				String entryName = entry.getName();
				if (entry.isDirectory()
						|| (entryName.startsWith("lib/") == false && entryName
								.startsWith("assets/solibs/") == false)
						|| entryName.endsWith(".so") == false)
					continue;
				String[] tags = entryName.split("/");
				if (tags == null || tags.length < 3)
					continue;
				String libName = tags[tags.length - 1];
				String abiName = tags[tags.length - 2];
				if (mapLibs.containsKey(libName)) {
					if (abiName.startsWith(abiType)
							&& abiName.compareTo(Build.CPU_ABI) <= 0) {
						mapLibs.put(libName, entry);
					}
				} else {
					mapLibs.put(libName, entry);
				}
			}
			for (Entry<String, ZipEntry> lib : mapLibs.entrySet()) {
				unzipFile(checkHandler, zipFile, lib.getValue(),
						libPath + lib.getKey(), delayCheck);
				File fLib = new File(libPath + lib.getKey());
				if (!fLib.setExecutable(true, false)) {
					LogUtil.logw("set library execute attribute failed: " + fLib.getAbsolutePath());
				}
				if (!fLib.setReadable(true, false)) {
					LogUtil.logw("set library read attribute failed: " + fLib.getAbsolutePath());
				}
			}
			checkHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					try {
						zipFile.close();
					} catch (IOException e) {
					}
					if (checkHandler.checkFailed) {
						LogUtil.loge("check unzip libs failed, restarting...");
						AppLogicBase.restartProcess();
					} else {
						LogUtil.logi("check unzip libs end");
					}
					checkThread.quit();
				}
			}, delayCheck);
		} catch (IOException e) {
			// 出现IO错误时需要清理启动时间记录，下次启动时可能io错误就不存在了
			AppLogicBase.clearStartTimeRecord();
			// e.printStackTrace();
			throw new RuntimeException("Load app error: unzip [" + apkPath
					+ "] files error: " + e.getMessage());
		}
	}

	public static void checkUnzipFile(final CheckHandler checkHandler,
			final ZipFile zipFile, final ZipEntry entry,
			final String unzipPath, long delayCheck) {
		checkHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// 校验crc，不一致的话要求重启重新解压
				boolean bSame = true;
				// LogUtil.loge("check unzip file begin: " + unzipPath);
				File f = new File(unzipPath);
				FileInputStream inFile = null;
				InputStream inZip = null;
				try {
					do {
						if (f.length() != entry.getSize()) {
							bSame = false;
							LogUtil.loge("check unzip file not same size["
									+ f.length() + "/" + entry.getSize() + ": "
									+ unzipPath);
							break;
						}
						inFile = new FileInputStream(unzipPath);
						inZip = zipFile.getInputStream(entry);
						byte[] bufFile = checkHandler.bufFile;
						byte[] bufZip = checkHandler.bufZip;
						int nRemainFile = 0;
						int nRemainZip = 0;
						while (true) {
							// 尽可能读取文件数据
							int nReadFile = 0;
							while (nReadFile == 0) {
								nReadFile = inFile.read(bufFile, nRemainFile,
										bufFile.length - nRemainFile);
							}
							// 读取完了
							if (nRemainFile == 0 && nReadFile < 0) {
								break;
							}
							nRemainFile += nReadFile;
							// 读取zip相同数据量
							while (nRemainZip < nRemainFile) {
								int nReadZip = inZip.read(bufZip, nRemainZip,
										nRemainFile - nRemainZip);
								if (nReadZip < 0) {
									LogUtil.loge("check unzip read zip file error: "
											+ entry.getName());
									bSame = false;
									break;
								}
								nRemainZip += nReadZip;
							}
							if (bSame == false)
								break;
							for (int i = 0; i < nRemainFile; ++i) {
								if (bufFile[i] != bufZip[i]) {
									bSame = false;
									LogUtil.loge("check unzip file not same byte at "
											+ i + ": " + unzipPath);
									break;
								}
							}
							if (bSame == false)
								break;
							nRemainFile = nRemainZip = 0;
							// sleep 20ms
							try {
								Thread.sleep(20);
							} catch (Exception e) {
								LogUtil.loge("sleep exception");
							}
						}
					} while (false);
				} catch (Exception e) {
					bSame = false;
					LogUtil.loge("check unzip file exception: " + unzipPath);
					e.printStackTrace();
				} finally {
					if (inFile != null) {
						try {
							inFile.close();
						} catch (IOException e) {
						}
					}
					if (inZip != null)
						try {
							inZip.close();
						} catch (IOException e) {
						}
				}
				if (bSame == false) {
					LogUtil.loge("check unzip file not same: " + unzipPath);
					f.delete();
					checkHandler.checkFailed = true;
				}
			}
		}, delayCheck);
	}
}
