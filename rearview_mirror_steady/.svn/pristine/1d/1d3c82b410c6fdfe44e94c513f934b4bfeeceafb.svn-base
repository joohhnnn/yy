package com.txznet.comm.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * 
 * 方控配置的获取及更新
 * 
 * @author Terry
 *
 */
public class NavControlConfiger extends BaseConfiger{

	private static NavControlConfiger sInstance = new NavControlConfiger();

	/**
	 * 方控操控模式
	 */
	private int mNavControlMode = 0;
	/**
	 * 厂商配置的默认操控模式
	 */
	private int mConfigNavControlMode = 0;
	/**
	 * 不支持方控
	 */
	public static final int MODE_NAV_NONE = 0;
	/**
	 * 双键操控模式
	 */
	public static final int MODE_NAV_TWO_DIRECTION = 2;
	/**
	 * 四键操控模式
	 */
	public static final int MODE_NAV_FOUR_DIRECTION = 4;

	private List<NavControlStateListener> mStateListeners;
	private Object mLock = new Object();

	private NavControlConfiger() {
	}

	public static NavControlConfiger getInstance() {
		return sInstance;
	}

	public void init() {
		HashMap<String, String> congfigs = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_NAV_CONTROL_MODE);
		if (congfigs != null && congfigs.get(TXZFileConfigUtil.KEY_NAV_CONTROL_MODE) != null) {
			try {
				mConfigNavControlMode = Integer.parseInt(congfigs.get(TXZFileConfigUtil.KEY_NAV_CONTROL_MODE));
				mNavControlMode = mConfigNavControlMode;
			} catch (NumberFormatException e) {
				LogUtil.loge(
						"keyEvnetMode set but format error:" + congfigs.get(TXZFileConfigUtil.KEY_NAV_CONTROL_MODE));
			}
		}
		if (!GlobalContext.isTXZ()) {
			// 非Core进程初始化时同步一次
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					INVOKE_COMM_PREFIX + INVOKE_NAV_CONTROL + "sync", null, null);
		} else {
			// Core进行直接同步一次当前配置
			sendCommInvoke(INVOKE_COMM_PREFIX + INVOKE_NAV_CONTROL + "navMode", ("" + mNavControlMode).getBytes());
		}
	}
	
	/**
	 * 获取方控操控模式
	 * 
	 * @return
	 * 		@see #MODE_NAV_FOUR_DIRECTION
	 * 		@see #MODE_NAV_TWO_DIRECTION
	 */
	public int getNavControlMode(){
		return mNavControlMode;
	}
	
	/**
	 * 设置方控操控模式
	 * 
	 * @param mode
	 */
	public void setNavControlMode(int mode) {
		if (mode != mNavControlMode) {
			mNavControlMode = mode;
			notifyModeChange();
		}
	}
	
	public interface NavControlStateListener {
		/**
		 * 方控操控模式发生变化
		 * 
		 * @param mode
		 *            当前操控模式
		 */
		void onControlModeChange(int mode);
	}
	
	/**
	 * 蓝牙方控连接状态发生变化
	 * @param connected
	 */
	public void onConnectStateChange(boolean connected) {
		if (connected) {
			// 蓝牙方控目前只有四键模式，因此默认认为是4键模式
			if (mNavControlMode != MODE_NAV_FOUR_DIRECTION) {
				mNavControlMode = MODE_NAV_FOUR_DIRECTION;
				notifyModeChange();
			}
		} else {
			// 恢复成默认配置模式
			if (mNavControlMode != mConfigNavControlMode) {
				mNavControlMode = mConfigNavControlMode;
				notifyModeChange();
			}
		}
	}
	
	
	/**
	 * @param listener
	 * @see #removeNavControlStateListener(NavControlStateListener)
	 */
	public void addNavControlStateListener(NavControlStateListener listener) {
		synchronized (mLock) {
			if (mStateListeners == null) {
				mStateListeners = new ArrayList<NavControlConfiger.NavControlStateListener>();
			}
			if (!mStateListeners.contains(listener)) {
				mStateListeners.add(listener);
			}
		}
	}

	/**
	 * @param listener
	 * @see #addNavControlStateListener(NavControlStateListener)
	 */
	public void removeNavControlStateListener(NavControlStateListener listener) {
		synchronized (mLock) {
			if (listener == null || mStateListeners == null) {
				return;
			}
			mStateListeners.remove(listener);
		}
	}

	private void notifyModeChange() {
		if (GlobalContext.isTXZ()) {
			sendCommInvoke(INVOKE_COMM_PREFIX + INVOKE_NAV_CONTROL + "navMode", ("" + mNavControlMode).getBytes());
		}
		synchronized (mLock) {
			if (mStateListeners != null) {
				for (NavControlStateListener listener : mStateListeners) {
					if (listener != null) {
						listener.onControlModeChange(mNavControlMode);
					}
				}
			}
		}
	}

	private void syncConfig(String packageName) {
		ServiceManager.getInstance().sendInvoke(packageName, INVOKE_COMM_PREFIX + INVOKE_NAV_CONTROL + "navMode",
				("" + mNavControlMode).getBytes(), null);
	}
	
	
	@Override
	public byte[] onEvent(String packageName, String command, byte[] data) {
		LogUtil.logd("receive cmd:" + command + " from:" + packageName);
		if (GlobalContext.isTXZ()) {
			if ("sync".equals(command)) {
				syncConfig(packageName);
			}
			return null;
		}
		if ("navMode".equals(command)) {
			Integer mode = Integer.parseInt(new String(data));
			if (mode != mNavControlMode) {
				mNavControlMode = mode;
				notifyModeChange();
			}
			return null;
		}
		return null;
	}
}
