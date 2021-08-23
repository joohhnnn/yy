package com.txznet.txz.util;

import java.io.File;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.tts.ITts.IInitCallback;
import com.txznet.txz.component.tts.ITtsRef;
import com.txznet.txz.component.tts.ITtsRefVersion;
import com.txznet.txz.component.tts.proxy.ITtsRef1;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.NativeHelper.UnzipOption;

import dalvik.system.DexClassLoader;

public class DynamicLoaderUtil {
	
	private static Long checkHandlerThreadDelay = null;

	/**
	 * 通过动态加载外部jar包获取TTS引擎
	 * 
	 * @param context
	 *        上下文
	 * @param jarDir
	 *        jar包路径
	 * @param jarFileName
	 *        jar包包名
	 * @param className
	 *        TTS引擎的类名
	 * @param assetsPath
	 *        保存资源文件的位置
	 * @param json
	 *        设置参数是需要的数据
	 * @return ITts没有初始化,只是设置一些参数，需要调用
	 *         {@link com.txznet.txz.component.tts.ITts#initialize(IInitCallback oRun)}
	 */
	public static ITts getTtsTool(Context context, String jarDir, String jarFileName, String className,
			String assetsPath, String json) {
		if (assetsPath == null) {
			assetsPath = jarDir + "/assets/";
		} else {
			assetsPath = assetsPath + File.separator;
		}

		LogUtil.logd("unzip tts engine file");
		makeDir(assetsPath);
		String jarPath = jarDir + File.separator + jarFileName;
		String classPath = assetsPath + MD5Util.generateMD5(jarFileName) + ".dex";
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
			JNIHelper.logd("checkHandlerThreadDelay::" + checkHandlerThreadDelay);
		}
		// 解压dex文件
		NativeHelper.unzipFiles(jarPath, UnzipOption.createUnzipFileOption("classes.dex", classPath), checkHandlerThreadDelay > 0 ? checkHandlerThreadDelay : 3000);
		// 解压assets下的资源文件
		NativeHelper.unzipFiles(jarPath,
				new UnzipOption[] { UnzipOption.createUnzipTreeOption("assets/", assetsPath) }, checkHandlerThreadDelay > 0 ? checkHandlerThreadDelay : 3000);
		// dex 缓存目录
		final String dexPath = context.getApplicationInfo().dataDir + "/dex/";
		makeDir(dexPath);
		// 解压lib下的so动态库
		final String libPath = context.getApplicationInfo().dataDir + "/solibs/";
		makeDir(libPath);
		// NativeHelper.addLibraryPath(context, libPath);
		NativeHelper.unzipLibFiles(jarPath, libPath, checkHandlerThreadDelay > 0 ? checkHandlerThreadDelay : 3000);

		byte[] data = null;
		try {
			JSONObject object;
			if (json == null || json.length() == 0) {
				object = new JSONObject();
			} else {
				object = new JSONObject(json);
			}
			data = object.put(ITtsRef1.ASSETS_PATH, assetsPath).toString().getBytes();
		} catch (JSONException e1) {
			LogUtil.loge("json data error" + e1.toString());
			return null;
		}
		
		ClassLoader loader = new DexClassLoader(classPath, dexPath, libPath, DynamicLoaderUtil.class.getClassLoader());
		Class<?> clazz = null;
		try {
			clazz = (Class<?>) loader.loadClass(className);
		} catch (ClassNotFoundException e1) {
			LogUtil.loge(className + " class not found " + e1.toString());
			return null;
		}

		ITts ttsTool = null;
		try {
			ITtsRefVersion ttsVersion = (ITtsRefVersion) clazz.newInstance();
			// 获取tts接口的版本号
			switch (ttsVersion.getVersion()) {
			case ITtsRefVersion.VERSION_NORMAL:
				ITtsRef ttsRef = (ITtsRef) ttsVersion;
				ttsRef.setArguments(context, data); // 传递参数过去
				ttsTool = ttsRef;
			case ITtsRefVersion.VERSION_1:
				ITtsRef1 ttsRef1 = (ITtsRef1) ttsVersion;
				ttsRef1.setArguments(context, data); // 传递参数过去
				ttsTool = ttsRef1;
				break;
			default:
				LogUtil.loge(className + " version code: " + ttsVersion.getVersion());
				// TODO 每次增加ITts接口时，可以使用版本号来区别
			}
		} catch (Exception e1) {
			LogUtil.loge(className + " not implements ITtsRefVersion" + e1.toString());
			try {
				ITtsRef ttsRef = (ITtsRef) clazz.newInstance();
				ttsRef.setArguments(context, data); // 传递参数过去
				ttsTool = ttsRef;
			} catch (Exception e) {
				LogUtil.loge(className + " not implements ITtsRef" + e.toString());
				return null;
			}
		}
		return ttsTool;
	}

	private static void makeDir(String dirPath) {
		File file = new File(dirPath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

}
