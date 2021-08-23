package com.txznet.txz.module.wheelcontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;

import com.txznet.comm.config.NavControlConfiger;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.txz.component.wheelcontrol.IWheelControl;
import com.txznet.txz.component.wheelcontrol.IWheelControl.OnConnectionStatusLinstener;
import com.txznet.txz.component.wheelcontrol.IWheelControl.OnWheelControlListener;
import com.txznet.txz.component.wheelcontrol.mix.WheelControlMixImpl;
import com.txznet.txz.component.wheelcontrol.mix.WheelControlMixImpl.WheelControlType;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.ui.WinManager;

/**
 * 蓝牙方控管理模块
 * Created by cain on 2017/1/3.
 */

public class WheelControlManager extends IModule {
	
	private static final String DEFAULT_PACKAGE_NAME = "default";

    private static WheelControlManager sInstance = new WheelControlManager();
    
    private boolean mIsWheelControlConnected;
    /** 方控是否可用 */
    private Boolean enableWheelControl = null;
    
    private IWheelControl mWheelControl;

    /**
     * core 应用内部的方控事件的维护列表
     */
    private LinkedList<OnTXZWheelControlListener>  mWheelControlListeners;
    
    /**
     * 最新注册普通事件监听的时间戳
     */
    private long mLastTimestamp;
    
    /**
     * 方控连接状态监听列表
     */
    private ArrayList<String> mWheelControlConnectedList = new ArrayList<String>();
    
    /**
     * 注册方控监听列表
     */
    private LinkedList<String> mPackageNames = new LinkedList<String>();
    
    /**
     * 全局事件监听注册
     */
    private HashMap<Integer, String> mGlobalEventMap = new HashMap<Integer, String>();
    
    private OnStateChangeListener mStateChangeListener = null;

	private WheelControlManager() {
		mInited = false;
		mInitSuccessed = false;
		mIsWheelControlConnected = false;
    }

	public static WheelControlManager getInstance() {
	    return sInstance;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public boolean checkSupportBLE() {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
    		JNIHelper.loge("WheelControl: BLE requires API level 18");
    		return false;
		}
    	
        Context context = GlobalContext.get();
        // Use this check to determine whether BLE is supported on the device. 
        // Then you can selectively disable BLE-related features.
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            JNIHelper.logw("WheelControl: don't support BLE");
            return false;
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        // Checks if Bluetooth is supported on the device.
        // Ensures Bluetooth is available on the device and it is enabled. If not, displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            JNIHelper.logw("WheelControl : Bluetooth is null or not enable");
            return false;
        }
        
        JNIHelper.logd("WheelControl : The device suppurts BLE");
        return true;
	}

	public void initializeComponent() {
		if (enableWheelControl == null || enableWheelControl.booleanValue() == false) {
			JNIHelper.loge("WheelControl : Wheel control is disabled");
			return;
		}
		if (mWheelControl != null) {
			JNIHelper.logw("WheelControl : already inited");
			return;
		}
    	if (!checkSupportBLE()) {
    		 JNIHelper.logw("WheelControl : The module is not available");
    		 return;
		}
    	
		mWheelControl = new WheelControlMixImpl(WheelControlType.BLE_TENCENT);
        mWheelControl.initialize(new IWheelControl.IInitCallback() {
            @Override
            public void onInit(boolean bSuccess) {
            	JNIHelper.logd("WheelControl : init result: " + bSuccess);
            	mInited = true;
            	mInitSuccessed = bSuccess;
            }
        });
        
        mWheelControl.setConnectionStatusLinstener(new OnConnectionStatusLinstener() {
			
			@Override
			public void isConnected(boolean isConnected) {
				changeBLEConnectedState(isConnected);
			}
		});
        
        // 注册事件
        mWheelControl.registerWheelControlListener(new OnWheelControlListener() {
			
			@Override
			public void onKeyEvent(int eventId) {
				sendWheelControlKeyEvent(eventId);
			}
		});

        mWheelControl.setGlobalWheelControlListener(new IWheelControl.OnGlobalWheelControlListener() {
            @Override
            public void onKeyEvent(int eventId) {
            	sendGlobalKeyEvent(eventId);
            }
        });
    }
    
    private void changeBLEConnectedState(boolean isConnected) {
    	// 将蓝牙连接状态发送所有观察者
		if (mIsWheelControlConnected != isConnected) {
			JNIHelper.logd("WheelControl : BLE connection state changes: " + isConnected);
			mIsWheelControlConnected = isConnected;
			AppLogic.runOnBackGround(new Runnable() {
				
				@Override
				public void run() {
					for (String packageName : mWheelControlConnectedList) {
						ServiceManager.getInstance().sendInvoke(packageName, "txz.wheelcontrol.notify.connected", ("" + mIsWheelControlConnected).getBytes(), null);
					}
					
					if (mStateChangeListener != null) {
						mStateChangeListener.isConnected(mIsWheelControlConnected);
					}
					
					NavControlConfiger.getInstance().onConnectStateChange(mIsWheelControlConnected);
				}
			}, 0);
		}
	}

	private void sendWheelControlKeyEvent(int eventId) {
		JNIHelper.logd("WheelControl : send onKeyEvent: " + eventId);
		if (mPackageNames.isEmpty()) {
			return;
		}
		
		String packageName = null;
		synchronized (mPackageNames) {
			packageName = mPackageNames.getLast();
		}
		JNIHelper.logd("WheelControl : send onKeyEvent to " + packageName);
		if (DEFAULT_PACKAGE_NAME.equals(packageName)) {
			// 内部消耗方控事件
			mWheelControlListeners.getLast().onKeyEvent(eventId);
			return;
		}
		JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("evnetid", eventId);
		ServiceManager.getInstance().sendInvoke(packageName, "txz.wheelcontrol.notify.event", jsonBuilder.toBytes(), null);
	}

	private void sendGlobalKeyEvent(int eventId) {
		switch (eventId) {
		case TXZWheelControlEvent.VOICE_KEY_CLICKED_EVENTID:
			WinManager.getInstance().clickWheelVoiceBtn();
			JNIHelper.logd("WheelControl : VOICE_KEY_CLICKED_EVENTID");
			break;
		case TXZWheelControlEvent.VOICE_KEY_DOWN_EVENTID:
			break;
		case TXZWheelControlEvent.VOICE_KEY_LONG_CLICKED_EVENTID:
			break;
		case TXZWheelControlEvent.VOICE_KEY_UP_EVENTID:
			break;
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
			if (!mGlobalEventMap.isEmpty()) {
				JNIHelper.logd("WheelControl : send GlobalEvent: " + eventId);
				String packageName = null;
				synchronized (mGlobalEventMap) {
					packageName = mGlobalEventMap.get(eventId);
				}
				if (packageName != null) {
					JSONBuilder jsonBuilder = new JSONBuilder();
					jsonBuilder.put("evnetid", eventId);
					ServiceManager.getInstance().sendInvoke(packageName, "txz.wheelcontrol.notify.globalevent", jsonBuilder.toBytes(), null);
					return;
				}
			}
			JNIHelper.logd("WheelControl : No register GlobalEvent Listener");
			sendWheelControlKeyEvent(eventId);
			break;
		default:
		}
	}

	/**
	 * 设置方控状态监听事件
	 */
	public void setOnStateChangeListener(final OnStateChangeListener listener) {
		mStateChangeListener = listener;
		if (listener != null) {
			AppLogic.runOnBackGround(new Runnable() {
				
				@Override
				public void run() {
					listener.isConnected(mIsWheelControlConnected);
				}
			}, 0);
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
			mWheelControlListeners = new LinkedList<OnTXZWheelControlListener>();
		}
		if (mWheelControlListeners.contains(listener)) {
			mWheelControlListeners.remove(listener);
		}
    	mWheelControlListeners.add(listener);
    	setWheelControlListener(DEFAULT_PACKAGE_NAME, SystemClock.elapsedRealtime());
    }

	/**
     * @param listener
     * @see #registerWheelControlListener(OnTXZWheelControlListener)
     */
    public void unregisterWheelControlListener(OnTXZWheelControlListener listener) {
    	if (listener == null) {
			return;
		}
    	if (mWheelControlListeners == null || mWheelControlListeners.isEmpty() || !mWheelControlListeners.contains(listener)) {
			return;
		}
    	
    	if (mWheelControlListeners.getLast() == listener) {
			// 当前最新的监听事件
    		removeWheelControlListener(DEFAULT_PACKAGE_NAME);
		}
    	mWheelControlListeners.remove(listener);
    }

    public void releaseWheelControlTool() {
        if (mWheelControl != null) {
            mWheelControl.release();
            mWheelControl = null;
            changeBLEConnectedState(false);
        }
    }

	// comm.wheelcontrol.**
    public byte[] invokeCommWheelControl(final String packageName, String command, byte[] data) {
        JNIHelper.logd("WheelControl : command: " + command + " , packageName: " + packageName);
        if ("comm.wheelcontrol.connectionstatus".equals(command)) {
        	setConnectionStatusLinstener(packageName);
        } else if ("comm.wheelcontrol.setlistener".equals(command)) {
        	try {
        		long timestamp = Long.parseLong(new String(data));
        		setWheelControlListener(packageName, timestamp);
			} catch (Exception e) {
				JNIHelper.logd("WheelControl : comm.wheelcontrol.setlistener Data error");
			}
        } else if ("comm.wheelcontrol.removelistener".equals(command)) {
        	removeWheelControlListener(packageName);
        } else if ("comm.wheelcontrol.setgloballistener".equals(command)) {
        	try {
				JSONBuilder jsonBuilder = new JSONBuilder(data);
				Integer[] arr = jsonBuilder.getVal("globalevent", Integer[].class);
				setGlobalListener(packageName, arr);
			} catch (Exception e) {
				JNIHelper.logd("WheelControl : ccomm.wheelcontrol.setgloballistener Data error");
			}
            return null;
        } else if ("comm.wheelcontrol.removegloballistener".equals(command)) {
            removeGlobalListener(packageName);
            return null;
        } else if ("comm.wheelcontrol.startlescan".equals(command)) {
            if (mWheelControl != null) {
                mWheelControl.scanLEDevice(true);
            }
        } else if ("comm.wheelcontrol.stoplescan".equals(command)) {
            if (mWheelControl != null) {
                mWheelControl.scanLEDevice(false);
            }
        } else if ("comm.wheelcontrol.release".equals(command)) {
            releaseWheelControlTool();
        } else if ("comm.wheelcontrol.enable".equals(command)) {
        	 enableWheelControl(Boolean.parseBoolean(new String(data)));
        }
        return null;
    }
    
    public void enableWheelControl(boolean enable) {
    	enableWheelControl = enable;
    	if (enableWheelControl) {
			// 启动方控
    		AppLogic.runOnUiGround(new Runnable() {
				
				@Override
				public void run() {
					initializeComponent();
				}
			}, 0);
		} else {
			// 禁用方控
			releaseWheelControlTool();
		}
    }
    
    private void setConnectionStatusLinstener(String packageName) {
    	JNIHelper.logd("WheelControl : set ConnectionStatusLinstener " + packageName);
    	if (mWheelControlConnectedList.contains(packageName)) {
    		JNIHelper.logw("WheelControl : Connection Status Linstener is registered");
		} else {
			mWheelControlConnectedList.add(packageName);
		}
    	ServiceManager.getInstance().sendInvoke(packageName, "txz.wheelcontrol.notify.connected", ("" + mIsWheelControlConnected).getBytes(), null);
    }

    private void setWheelControlListener(String packageName, long timestamp) {
    	if (mLastTimestamp > timestamp) {
    		// 时间戳过时
			return;
		} else {
			mLastTimestamp = timestamp;
		}
    	synchronized (mPackageNames) {
			if (mPackageNames.contains(packageName)) {
				mPackageNames.remove(packageName);
			}
			mPackageNames.add(packageName);
    	}
		JNIHelper.logd("WheelControl : add WheelControlListener " + packageName);
	}

	private void removeWheelControlListener(String packageName) {
		synchronized (mPackageNames) {
			mPackageNames.remove(packageName);
		}
		JNIHelper.logd("WheelControl : removeWheelControlListener " + packageName);
	}
	
	private void setGlobalListener(String packageName, Integer[] arr) {
		if (arr == null || arr.length == 0) {
			return;
		}
		
		synchronized (mGlobalEventMap) {
			for (int i = 0; i < arr.length; i++) {
				// 只有没有注册过的 globaleventid 才能注册
				if (!mGlobalEventMap.containsKey(arr[i])) {
					mGlobalEventMap.put(arr[i], packageName);
				}
			}
		}
		JNIHelper.logd("WheelControl : setGlobalListener " + packageName);
	}

	private void removeGlobalListener(String packageName) {
		synchronized (mGlobalEventMap) {
			Iterator<Integer> iterator = mGlobalEventMap.keySet().iterator();
			while (iterator.hasNext()) {
				Integer integer = (Integer) iterator.next();
				if (mGlobalEventMap.get(integer).equals(packageName)) {
					mGlobalEventMap.remove(integer);
				}
			}
		}
		JNIHelper.logd("WheelControl : removeGlobalListener " + packageName);
	}

	public interface OnTXZWheelControlListener extends IWheelControl.OnWheelControlListener{
	}
	
	/**
	 * 方控蓝牙连接状态
	 */
	public interface OnStateChangeListener {
		
		void isConnected(boolean isConnected);
		
	}
}
