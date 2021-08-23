package com.txznet.comm.remote.util;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.util.ScreenLock;

import android.content.Context;

public class WakeLockUtil {
	private  ScreenLock mScreenLock;

	public WakeLockUtil(Context context) {
		mScreenLock = new ScreenLock(context);
	}

	public  void acquire() {
		ServiceData resp = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.sys.wakelock.acquire",
				null);
		if (resp == null || resp.getBoolean() == false) {
			mScreenLock.lock();
		}
	}

	public  void release() {
		ServiceData resp = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, "txz.sys.wakelock.release",
				null);
		if (resp == null || resp.getBoolean() == false) {
			mScreenLock.release();
		}
	}
}
