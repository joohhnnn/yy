package com.txznet.sdk;

import java.util.HashSet;
import java.util.LinkedList;

import android.os.SystemClock;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;

/**
 * Created by cain on 2016/12/27.
 * 蓝牙方控管理器
 */

public class TXZWheelControlManager {

    private static final TXZWheelControlManager INSTANCE = new TXZWheelControlManager();

    /** 方控功能是否可用 */
    private Boolean enableWheelControl = null;
    /** 方控连接状态 */
    private boolean mIsWheelControlConnected = false;
    /** 方控普通事件回调监听队列 */
    private LinkedList<OnTXZWheelControlListener>  mWheelControlListeners;
    /** 注册方控事件时间戳 */
    private long mLastTimestamp;
    /** 方控连接状态监听回调监听 */
    private OnConnectionStatusLinstener mConnectionStatusLinstener;
    
    private OnTXZGlobalWheelControlListener mGlobalWheelControlListener;
    private HashSet<Integer> mGlobalEvents;

    private TXZWheelControlManager() {
    }

    public static TXZWheelControlManager getInstance() {
        return INSTANCE;
    }
    
    /** 重连时需要重新通知同行者的操作放这里 */
	void onReconnectTXZ() {
		if (enableWheelControl != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.enable", enableWheelControl.toString().getBytes(), null);
		}
		
		if (mConnectionStatusLinstener != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.connectionstatus", null, null);
		}
		
		if (mWheelControlListeners != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.setlistener", ("" + mLastTimestamp).getBytes(), null);
		}
		
		if (mGlobalWheelControlListener != null && !mGlobalEvents.isEmpty()) {
			JSONBuilder jsonBuilder = new JSONBuilder();
	        jsonBuilder.put("globalevent", mGlobalEvents.toArray());
	        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.setgloballistener", jsonBuilder.toBytes(), null);
		}
	}

	/** 处理core 发送的消息回调 */
    public byte[] notifyCallback(String command, byte[] data) {
    	LogUtil.logd("WheelControl : notify callback: " + command);
    	if (data == null) {
    		LogUtil.loge("WheelControl : data == null");
    		return null;
		}
    	// 方控连接状态回调
    	if ("txz.wheelcontrol.notify.connected".equals(command)) {
    		boolean connected = Boolean.parseBoolean(new String(data));
    		LogUtil.logd("WheelControl : connection state: " + connected);
			mIsWheelControlConnected = connected;
			
    		if (mConnectionStatusLinstener != null) {
    			mConnectionStatusLinstener.isConnected(connected);
    		}
			return null;
		}
    	// 方控普通事件的回调
        if ("txz.wheelcontrol.notify.event".equals(command)) {
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            if (jsonBuilder != null) {
                int eventId = jsonBuilder.getVal("evnetid", Integer.class, 0);
                LogUtil.logd("WheelControl : onKeyEvent: " + eventId);
                if (eventId != 0) {
                	if (mWheelControlListeners != null && !mWheelControlListeners.isEmpty()) {
            			mWheelControlListeners.getLast().onKeyEvent(eventId);
            		}
                }
            }
            return null;
        }
        // 方控全局事件
        if ("txz.wheelcontrol.notify.globalevent".equals(command)) {
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            if (jsonBuilder != null) {
                int eventId = jsonBuilder.getVal("evnetid", Integer.class, 0);
                if (eventId != 0) {
                    handlerGlobalKeyEvent(eventId);
                }
            }
            return null;
        }
    	return null;
    }

    private boolean handlerGlobalKeyEvent(int eventId) {
	    if (mGlobalWheelControlListener == null || mGlobalEvents.isEmpty()) {
	        // 全局事件回调为空
	        return false;
	    }
	    
	    if (mGlobalEvents.contains(eventId)) {
	    	mGlobalWheelControlListener.onKeyEvent(eventId);
		}
	    return true;
	}

    /** 当前方控连接状态需要先注册状态监听 */
	public boolean isWheelControlConnected() {
    	return mIsWheelControlConnected;
    }
    
	/** 启用或禁用方控功能 */
    public void enableWheelControl(boolean enable) {
    	enableWheelControl = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.enable", enableWheelControl.toString().getBytes(), null);
    }
    
    /** 开始或停止扫描方控设备，不需要主动调用，启动方控自动连接方控 */
    public void scanLEDevice(boolean enable) {
	    if (enable) {
	        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.startlescan", null, null);
	    } else {
	        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.stoplescan", null, null);
	    }
	}

	public void setConnectionStatusLinstener(OnConnectionStatusLinstener listener) {
		mConnectionStatusLinstener = listener;
		if (listener == null) {
			// 清除掉监听命令
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.connectionstatus", null, null);
		}
	}

    /**
     * 注册普通方控事件监听，不使用时需要反注册。<br>
     * 一般在界面显示时注册监听回调，隐藏时反注册。
     * @param listener
     * @see #unregisterWheelControlListener(OnTXZWheelControlListener)
     */
    public void registerWheelControlListener(OnTXZWheelControlListener listener) {
    	if (listener == null) {
			return;
		}
    	
    	if (mWheelControlListeners == null) {
			mWheelControlListeners = new LinkedList<TXZWheelControlManager.OnTXZWheelControlListener>();
		}
    	
    	if (mWheelControlListeners.contains(listener)) {
			mWheelControlListeners.remove(listener);
		}
    	mWheelControlListeners.add(listener);

    	mLastTimestamp = SystemClock.elapsedRealtime();
    	ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.setlistener", ("" + mLastTimestamp).getBytes(), null);
    }

	/**
	 * 反注册普通方控事件监听
     * @param listener
     * @see #registerWheelControlListener(OnTXZWheelControlListener)
     */
    public void unregisterWheelControlListener(OnTXZWheelControlListener listener) {
    	if (listener == null) {
			return;
		}
    	
    	if (mWheelControlListeners == null || mWheelControlListeners.isEmpty()) {
			return;
		}
    	
    	mWheelControlListeners.remove(listener);
    	
    	if (mWheelControlListeners.isEmpty()) {
    		// 当前最新的监听事件
    		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.removelistener", null, null);
		}
    }
    
    /**
	 * @param eventId
	 * @return 全局事件ID添加成功或失败，已设置 listener 或 ID已添加返回失败。
	 * @see com.txznet.sdk.TXZWheelControlEvent#VOL_KEY_CLICKED_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#VOL_KEY_LONG_CLICKED_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#VOL_KEY_UP_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#VOL_KEY_DOWN_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#HOME_KEY_CLICKED_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#HOME_KEY_LONG_CLICKED_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#HOME_KEY_UP_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#HOME_KEY_DOWN_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#BACK_KEY_CLICKED_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#BACK_KEY_LONG_CLICKED_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#BACK_KEY_UP_EVENTID
	 * @see com.txznet.sdk.TXZWheelControlEvent#BACK_KEY_DOWN_EVENTID
	 */
    public boolean regGlobalEvent(int eventId) {
    	if (mGlobalWheelControlListener != null) {
    		// 已注册
    		return false;
    	}
    	
    	if (mGlobalEvents == null) {
    		mGlobalEvents = new HashSet<Integer>();
		}
		switch (eventId) {
		case TXZWheelControlEvent.VOL_KEY_CLICKED_EVENTID:
		case TXZWheelControlEvent.VOL_KEY_LONG_CLICKED_EVENTID:
		case TXZWheelControlEvent.VOL_KEY_UP_EVENTID:
		case TXZWheelControlEvent.VOL_KEY_DOWN_EVENTID:
		case TXZWheelControlEvent.HOME_KEY_CLICKED_EVENTID:
		case TXZWheelControlEvent.HOME_KEY_LONG_CLICKED_EVENTID:
		case TXZWheelControlEvent.HOME_KEY_UP_EVENTID:
		case TXZWheelControlEvent.HOME_KEY_DOWN_EVENTID:
		case TXZWheelControlEvent.BACK_KEY_CLICKED_EVENTID:
		case TXZWheelControlEvent.BACK_KEY_LONG_CLICKED_EVENTID:
		case TXZWheelControlEvent.BACK_KEY_UP_EVENTID:
		case TXZWheelControlEvent.BACK_KEY_DOWN_EVENTID:
			return mGlobalEvents.add(eventId);
		default:
			return false;
		}
    }

    /**
     * 全局事件只能注册一次，多次注册无效，在调用之前需要先调用{@link #regGlobalEvent(int)}
     * @param listener
     */
    public boolean setGlobalWheelControlListener(OnTXZGlobalWheelControlListener listener) {
    	if (listener == null) {
			return false;
		}
    	if (mGlobalEvents == null || mGlobalEvents.isEmpty()) {
			return false;
		}
    	
    	if (mGlobalWheelControlListener != null) {
    		return false;
    	}
        mGlobalWheelControlListener = listener;

        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("globalevent", mGlobalEvents.toArray());
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.setgloballistener", jsonBuilder.toBytes(), null);
        return true;
    }

    /** 反注册全局事件监听 */
    public void removeGlobalWheelControlListener() {
        mGlobalWheelControlListener = null;
        mGlobalEvents = null;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.wheelcontrol.removegloballistener", null, null);
    }

    /**
     * 普通方控事件的回调
     */
    public interface OnTXZWheelControlListener {
    	
    	/**
    	 * 滚轮左右滚动事件和上下左右点击事件的回调，事件ID见{@link TXZWheelControlEvent}
    	 * @param eventId
    	 */
    	void onKeyEvent(int eventId);
    }

    /**
     * 方控全局事件的回调，全局事件每个事件只能注册一次。
     */
    public interface OnTXZGlobalWheelControlListener {

    	/**
    	 * 全局事件回调，事件ID见{@link TXZWheelControlEvent}
    	 * @param eventId
    	 */
        void onKeyEvent(int eventId);

    }
    
    /**
     * 方控蓝牙连接状态
     */
    public interface OnConnectionStatusLinstener {
		
    	void isConnected(boolean isConnected);
		
	}

}
