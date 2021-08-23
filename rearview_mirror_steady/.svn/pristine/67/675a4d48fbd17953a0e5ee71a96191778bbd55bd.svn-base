package com.txznet.comm.util;

import java.lang.reflect.Field;
import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

public class ProcessUtil {

	public static boolean isForeground(String packageName) {
		try {
			String topProcess = getTopPackageName(GlobalContext.get());
			LogUtil.logd("isForeground topProcess:" + topProcess);
			if (packageName.equals(topProcess)) {
				return true;
			}
		} catch (Exception e) {
			LogUtil.loge("[Core] isForeground", e);
		}
		return false;
	}
	
	private static String getTopPackageName(Context context) {
		if (android.os.Build.VERSION.SDK_INT >= 21) {
			try {
				Field field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
				ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
				List<RunningAppProcessInfo> infos = am.getRunningAppProcesses();
				for (RunningAppProcessInfo info : infos) {
					if (info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
						Integer state = field.getInt(info);
						if (state != null && state == 2) {
							return info.processName;
						}
					}
				}
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		} else {
			ActivityManager mActManager = (ActivityManager) GlobalContext.get()
					.getSystemService(Context.ACTIVITY_SERVICE);
			return mActManager.getRunningTasks(1).get(0).topActivity.getPackageName();
		}
		return "";
	}

	public static boolean isProcessRunning(String packageName) {
		try {
			ActivityManager mActManager = (ActivityManager) GlobalContext.get()
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningAppProcessInfo> mRais = mActManager.getRunningAppProcesses();
			if (mRais == null) {
				return false;
			}
			for (RunningAppProcessInfo info : mRais) {
				if (packageName.equalsIgnoreCase(info.processName)) {
					return true;
				}
			}
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
		return false;
	}

    public static int getProcessIdByPkgName(String pkgName) {
        ActivityManager activityManager = (ActivityManager) GlobalContext.get()
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> infos = activityManager
                .getRunningAppProcesses();
        for (RunningAppProcessInfo info : infos) {
            if (info.processName.equals(pkgName)) {
                return info.pid;
            }
        }
        return -1;
    }
}
