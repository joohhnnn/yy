package com.txznet.txz.module.bt;

import com.txznet.txz.module.IModule;

/**
 * 蓝牙管理模块，负责蓝牙状态监听获取，蓝牙事件分发处理
 * 
 * @author bihongpi
 *
 */
public class BluetoothManager extends IModule {
	static BluetoothManager sModuleInstance = new BluetoothManager();

	private BluetoothManager() {
	}

	public static BluetoothManager getInstance() {
		return sModuleInstance;
	}

	/**
	 * 
	 * @return SCO的连接是否打开
	 */
	public boolean isScoStateOn() {
		return false;
	}

	/**
	 * spp是否连接上
	 * 
	 * @return
	 */
	public boolean isSppConnected() {
		return false;
	}

	/**
	 * 
	 * @return 是否有蓝牙设备连接上
	 */
	public boolean isBluetoothDeviceConnected() {
		return false;
	}

	public boolean startSco() {
		return true;
	}

	public boolean stopSco() {
		return true;
	}
	

}
