package com.txznet.comm.util;

import java.util.HashMap;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

/**
 * Created by ASUS User on 2015/9/2.
 */
public class ScreenLock {
	private Context mContext;
	private PowerManager mPowerManager;
	private KeyguardManager mKeyguardManager;
	private PowerManager.WakeLock mWakeLock;
	private KeyguardManager.KeyguardLock mKeyguardLock;
	private static Boolean mEnableScreenLock = null;
	
	public ScreenLock(Context context) {
		mContext = context;
		mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
		HashMap<String, String> config = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_SCREEN_LOCK);
		if (config != null && config.get(TXZFileConfigUtil.KEY_SCREEN_LOCK) != null) {
			try {
				mEnableScreenLock = Boolean.parseBoolean(config.get(TXZFileConfigUtil.KEY_SCREEN_LOCK));
			} catch (Exception e) {
				LogUtil.loge("parse screen lock error", e);
			}
		}
	}

	public void lock() {
		if (mEnableScreenLock != null && !mEnableScreenLock) {
			LogUtil.logd("disable screen lock,return");
			return;
		}
		if (mWakeLock != null || mKeyguardLock != null) {
			release();
		}
		mWakeLock = mPowerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "TXZ_WakeLock");
		mWakeLock.acquire();
		mKeyguardLock = mKeyguardManager.newKeyguardLock("KeyguardLock");
		mKeyguardLock.disableKeyguard();
	}

	public void release() {
		if (mWakeLock != null) {
			if(mWakeLock.isHeld()){
				try {
					mWakeLock.release();
				} catch (Exception e) {
				}
			}
			mWakeLock = null;
		}
		if (mKeyguardLock != null) {
			mKeyguardLock.reenableKeyguard();
			mKeyguardLock = null;
		}
	}
}
