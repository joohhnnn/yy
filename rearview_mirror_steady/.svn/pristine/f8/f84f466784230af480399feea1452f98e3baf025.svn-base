package com.txznet.txz.util;

import java.io.FileOutputStream;
import java.nio.channels.FileLock;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;

class ProcessSyncLocker {
	private FileOutputStream mFos = null;
	private FileLock mLock = null;
	public final static String sLockFileName = AppLogic.getApp().getApplicationInfo().dataDir + "/model_lock";

	private ProcessSyncLocker(String file) {
		try {
			mFos = new FileOutputStream(file, true);//true可以可以避免，文件存在的时候，触发截断操作
		} catch (Exception e) {

		}

	}

	public void lock() {
		LogUtil.logd("lock begin mFos : " + mFos);
		if (mFos != null) {
			try {
				mLock = mFos.getChannel().lock();
			} catch (Exception e) {
			}
		}
		LogUtil.logd("lock end mFos : " + mFos);
	}

	public void release() {
		if (mLock != null) {
			try {
				mLock.release();
			} catch (Exception e) {

			}
			mLock = null;
		}

		if (mFos != null) {
			try {
				mFos.close();
			} catch (Exception e) {
			}
			mFos = null;
		}
	}
}
