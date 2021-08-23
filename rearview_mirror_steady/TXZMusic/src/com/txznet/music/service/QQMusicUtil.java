package com.txznet.music.service;

import android.content.pm.ApplicationInfo;

import com.txznet.comm.remote.GlobalContext;

public class QQMusicUtil {
	private static boolean checkAppExist(String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = GlobalContext.get()
					.getPackageManager()
					.getApplicationInfo(
							packageName,
							android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES);
			if (info != null)
				return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static boolean checkQQMusicInstalled() {
		return checkAppExist("com.tencent.qqmusic")
				|| checkAppExist("com.tencent.qqmusicpad");
	}
}
