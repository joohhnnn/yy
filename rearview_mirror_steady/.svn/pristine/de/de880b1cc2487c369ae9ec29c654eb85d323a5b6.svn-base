package com.txznet.txz.component.wheelcontrol.mix;

import com.txznet.txz.component.wheelcontrol.IWheelControl;
import com.txznet.txz.component.wheelcontrol.tencent.WheelControlTencentImpl;

public class WheelControlMixImpl implements IWheelControl {
	
	private IWheelControl mWheelControlTool;
	
	public enum WheelControlType {
		/**
		 * 不使用蓝牙方控
		 */
		BLE_NONE,
		/**
		 * 腾讯方控
		 */
		BLE_TENCENT
	}

	public WheelControlMixImpl(WheelControlType type) {
		super();
        mWheelControlTool = new WheelControlTencentImpl();
	}

	@Override
	public void initialize(IInitCallback oRun) {
		mWheelControlTool.initialize(oRun);
	}

	@Override
	public void release() {
		mWheelControlTool.release();
	}

	@Override
	public void scanLEDevice(boolean enable) {
		mWheelControlTool.scanLEDevice(enable);
	}

	@Override
	public void registerWheelControlListener(OnWheelControlListener listener) {
		mWheelControlTool.registerWheelControlListener(listener);
	}

	@Override
	public void unregisterWheelControlListener(OnWheelControlListener listener) {
		mWheelControlTool.unregisterWheelControlListener(listener);
	}

	@Override
	public void setGlobalWheelControlListener(OnGlobalWheelControlListener listener) {
		mWheelControlTool.setGlobalWheelControlListener(listener);
	}

	@Override
	public void removeGlobalWheelControlListener() {
		mWheelControlTool.removeGlobalWheelControlListener();
	}

	@Override
	public void setConnectionStatusLinstener(OnConnectionStatusLinstener listener) {
		mWheelControlTool.setConnectionStatusLinstener(listener);
	}

	@Override
	public boolean isWheelControlConnected() {
		return mWheelControlTool.isWheelControlConnected();
	}
}
