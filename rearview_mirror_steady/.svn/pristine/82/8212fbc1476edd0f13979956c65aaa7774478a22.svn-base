package com.txznet.txz.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.service.MusicInteractionWithCore.OnAppIDListener;

import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;

/**
 * 预处理工具类
 * 
 * @author telenewbie
 *
 */
public class PreUrlUtil {

	private static final String TAG = "music::interaction::";

	/**
	 * 
	 * 
	 * @param downloadUrl
	 *            这里没有任何作用，只是起到误会的作用。嘿嘿
	 * @return 预处理路径
	 */
	public static String genPreUrl(String downloadUrl) {
		SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance();
		dateFormat.applyPattern("MM dd yyyy");

		return "http://base.music.qq.com/fcgi-bin/fcg_musicexpress.fcg?json=3&guid="
				+ MD5Util.generateMD5(Base64.encode((uid + dateFormat.format(new Date())).getBytes(), Base64.DEFAULT));
	}

	public static void initAppid(final OnAppIDListener listener) {
		// 从文件中读取
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						LogUtil.logd(TAG + "begin init appid");
						String appid = getAppid();
						long uid = getUid();
						if (uid != 0 && !TextUtils.isEmpty(appid)) {
							LogUtil.logd(TAG + "onSuccess[" + "uid=" + uid + ",appid=" + subAppid(appid) + "]");
							listener.onSuccess(com.txznet.txz.util.UIDUtils.genarateUid(uid),
									com.txznet.txz.util.UIDUtils.genarateAppid(appid));
							LogUtil.logd(TAG + "end init appid");
							return;
						}
					} catch (Exception e) {
						listener.onError("get data info occur error:" + e.toString());
					}
					SystemClock.sleep(10);
				}

			}

		}, "t_initAPPID").start();
	}

	static String appid;

	public static String getAppid() {
		appid = "";

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.PackageInfo", null, new GetDataCallback() {

			@Override
			public void onGetInvokeResponse(ServiceData sendInvokeSync) {
				do {
					if (sendInvokeSync != null) {
						JSONBuilder jsonBuilder = new JSONBuilder(sendInvokeSync.getBytes());
						int versionCode = jsonBuilder.getVal("versionCode", int.class, 0);
						LogUtil.logd(TAG + "versionCode:" + versionCode);
						if (versionCode > 246) {// 版本号大于246才支持
							ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.getAppid", null,
									new GetDataCallback() {

										@Override
										public void onGetInvokeResponse(ServiceData data) {
											// 区分版本号
											if (data != null) {
												String appid_t = data.getString();
												if (StringUtils.isNotEmpty(appid_t)) {
													appid = appid_t;
												} else {
													SystemClock.sleep(1000);
													ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
															"txz.music.getAppid", null, this);
												}

											}
										}

									});
							break;
						}
						appid = getDefaultAppid();
					}else{
						LogUtil.logd(TAG + "can't bind txz service");
						SystemClock.sleep(1000);
						ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.PackageInfo", null,this);
					}
				} while (false);
			}

		});

		while (TextUtils.isEmpty(appid)) {
			SystemClock.sleep(100);
		}
		LogUtil.logd(TAG + "appid:" + subAppid(appid));
		return appid;
	}

	private static String subAppid(String appid) {
		return appid.substring(0, 3) + "......" + appid.substring(appid.length() - 3, appid.length());
	}

	public static boolean isSupportGetAppid(String versionName) {
		String[] split = versionName.split("\\.");
		int[] supportVersion = { 2, 4, 5 };
		try {
			int parm1 = split.length > 0 ? Integer.parseInt(split[0]) : 0;
			if (supportVersion[0] > parm1) {
				return false;
			} else if (supportVersion[0] == parm1) {
				int parm2 = split.length > 1 ? Integer.parseInt(split[1]) : 0;
				if (supportVersion[1] > parm2) {
					return false;
				} else if (parm2 == supportVersion[1]) {
					int parm3 = split.length > 2 ? Integer.parseInt(split[2]) : 0;
					if (supportVersion[2] > parm3) {
						return false;
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private static String getDefaultAppid() {
		return "2cd7ffca4cb8795ff7a4616c3f362256";
	}

	private static String getTempAppid() {
		return "---";
	}

	private static long getDefaultUid() {
		long uid = 0;
		File file = new File(Environment.getExternalStorageDirectory() + "/txz/");
		// 便利文件夹
		LogUtil.logd(TAG + "uid:path::" + file.getAbsolutePath());
		File[] listFiles = file.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith("uid_");
			}
		});

		long maxTime = 0;
		int maxIndex = 0;
		if (listFiles == null || listFiles.length == 0) {
			LogUtil.loge(TAG + "UID 为空，请注意初始化声控引擎");
		} else {
			for (int i = 0; i < listFiles.length; i++) {
				if (maxTime < listFiles[i].lastModified()) {
					maxTime = listFiles[i].lastModified();
					maxIndex = i;
				}
			}

			String fileName = listFiles[maxIndex].getName();
			uid = Long.parseLong(fileName.substring(fileName.indexOf("uid_") + 4, fileName.indexOf(".")));
		}
		return uid;
	}

	private static String getTempUid() {
		return "0";
	}

	static long uid = 0;

	public static long getUid() {
		uid = 0;
		boolean isReal=false;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.getUid", null, new GetDataCallback() {

			@Override
			public void onGetInvokeResponse(ServiceData sendInvokeSync) {
				// 返回MD5
				if (sendInvokeSync != null) {// 连接上
					Long uid_t = sendInvokeSync.getLong();
					if (uid_t != null) {
						uid = uid_t;
						if (uid == 0) {// 默认值
							SystemClock.sleep(1000);
							ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.getUid", null, this);
						}
						return;
					}
				}
				uid = getDefaultUid();
				if (uid == 0) {// 此时获取的uid和appid都是有问题的.
					SystemClock.sleep(1000);
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.getUid", null, this);
				}

			}
		});

		while (uid == 0) {
			SystemClock.sleep(100);
		}

		LogUtil.logd(TAG + "uid:" + uid);

		return uid;
	}

}
