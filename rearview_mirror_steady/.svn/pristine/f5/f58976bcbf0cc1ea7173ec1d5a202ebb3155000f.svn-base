package com.txznet.music;

import java.io.File;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.pm.ApplicationInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.widget.Toast;

public class Utils {

	private final static double NEEDSPACE = 1000 * 1000 * 10;// 10G
	
	public static void showToast(String message) {
		AppLogic.runOnUiGround(new Runnable1<String>(message) {
			@Override
			public void run() {
				Toast.makeText(AppLogic.getApp(), mP1, 0).show();
			}
		}, 0);
	}

	/**
	 * 检查空间是否充足 +
	 * 
	 * 
	 * @return
	 */
	public static boolean checkSpaceEnough() {
		File sdcard = Environment.getExternalStorageDirectory();
		StatFs statfs = new StatFs(sdcard.getPath());
		long blockSize = statfs.getBlockSize();
		long availaBlock = statfs.getAvailableBlocks();
		return availaBlock * blockSize > NEEDSPACE;
	}
	
	public static boolean checkAppExist(String packageName) {
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = GlobalContext
					.get()
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
		return checkAppExist("com.tencent.qqmusic") || checkAppExist("com.tencent.qqmusicpad");
	}
}
