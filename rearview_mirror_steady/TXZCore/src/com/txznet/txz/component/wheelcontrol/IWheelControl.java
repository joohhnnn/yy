package com.txznet.txz.component.wheelcontrol;

/**
 * Created by cain on 2016/12/28.
 */

public interface IWheelControl {

    /**
     * 初始化方控
     */
    void initialize(final IInitCallback oRun);

    void release();
    
    void scanLEDevice(boolean enable);

    /**
     * 注册方控按键事件回调监听
     * @param listener
     */
    void registerWheelControlListener(OnWheelControlListener listener);

    /**
     * 取消注册方控按键事件回调监听
     * @param listener
     */
    void unregisterWheelControlListener(OnWheelControlListener listener);

    void setGlobalWheelControlListener(OnGlobalWheelControlListener listener);

    void removeGlobalWheelControlListener();
    
    /**
	 * 方控是否已连接
	 */
	boolean isWheelControlConnected();

	/**
	 * 方控连接状态监听回调
	 * @param listener
	 */
	void setConnectionStatusLinstener(OnConnectionStatusLinstener listener);

    /**
     * 方控初始化回调
     */
    interface IInitCallback {
        void onInit(boolean bSuccess);
    }

    /**
     * 普通方控事件的回调
     */
    interface OnWheelControlListener {
        void onKeyEvent(int eventId);
    }

    /**
     * 方控全局事件的回调，全局只能注册一个。
     */
     interface OnGlobalWheelControlListener {
        void onKeyEvent(int eventId);
    }
     
     interface OnConnectionStatusLinstener {
    	 void isConnected(boolean isConnected);
     }

}
