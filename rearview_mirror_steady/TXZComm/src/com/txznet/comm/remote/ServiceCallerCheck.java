package com.txznet.comm.remote;

import java.util.HashSet;

import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Process;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

public class ServiceCallerCheck {
	public final static int MAX_CALLER_COUNT = 20;

	private static int[] mAllowUids = new int[MAX_CALLER_COUNT];

	public static boolean checkBinderCallerUid(int uid, String packageName) {
		if (Process.myUid() == uid) {
			return true;
		}

//		if (mWhiteListUids != null && mWhiteListUids.contains(uid)) {
//			return true;
//		}

		boolean max = true;
		for (int i = 0; i < mAllowUids.length; ++i) {
			if (mAllowUids[i] == uid) {
				return true;
			}
			if (mAllowUids[i] == 0) {
				max = false;
				break;
			}
		}
		try {
			if (mWhiteListPackages != null
					&& mWhiteListPackages.contains(packageName) == false) {
				return false;
			}
			// 判断是否为伪造的包名
			ApplicationInfo info = GlobalContext.get().getPackageManager()
					.getApplicationInfo(packageName, 0);
			if (uid != info.uid) {
				return false;
			}
		} catch (Exception e) {
			return false;
		}

		synchronized (mAllowUids) {
			// 满了的话先全部清理掉，可能发生过泄漏
			if (max) {
				for (int i = mAllowUids.length - 1; i >= 0; --i) {
					mAllowUids[i] = 0;
				}
			}
			for (int i = 0; i < mAllowUids.length; ++i) {
				if (mAllowUids[i] == uid) { // 可能被另一个线程设置了
					break;
				} else if (mAllowUids[i] == 0) {
					mAllowUids[i] = uid;
					break;
				}
			}
		}

		return true;
	}

	private static boolean mLoadedWhiteList = false;
//	private static HashSet<Integer> mWhiteListUids;
	private static HashSet<String> mWhiteListPackages;

	private static void loadWhiteList() {
		if (mLoadedWhiteList == false) {
			synchronized (ServiceCallerCheck.class) {
				if (mLoadedWhiteList == false) {
					String whiteList = TXZFileConfigUtil
							.getSingleConfig(TXZFileConfigUtil.KEY_INVOKE_SECURITY_WHITE_LIST);
					if (!TextUtils.isEmpty(whiteList)) {
						mWhiteListPackages = new HashSet<String>();
						String[] names = whiteList.split(",");
						for (String name : names) {
							mWhiteListPackages.add(name);
						}
					}
					mLoadedWhiteList = true;
				}
			}
		}
	}

	public static boolean checkBinderCaller(String packageName, String command,
			byte[] data) {
		loadWhiteList();

		boolean ret = checkBinderCallerUid(Binder.getCallingUid(), packageName);

		// LogUtil.logd("checkBinderCaller: pkg="+packageName + ", cmd=" +
		// command + ", ret=" + ret + ", uids=" + Arrays.toString(mAllowUids));
		if (ret == false) {
			LogUtil.logw("not allow to invoke [" + command + "] from "
					+ packageName + "/" + Binder.getCallingPid() + "/"
					+ Binder.getCallingUid());
		}
		return ret;
	}
}
