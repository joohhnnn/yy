package com.txznet.txz.component.wheelcontrol.tencent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.os.RemoteException;

import com.tencent.libwecarwheelcontrolsdk.IBleInterface;
import com.tencent.libwecarwheelcontrolsdk.WheelControlManager;
import com.tencent.libwecarwheelcontrolsdk.core.ble.BleDefine;
import com.tencent.libwecarwheelcontrolsdk.event.IBackKeyEventListener;
import com.tencent.libwecarwheelcontrolsdk.event.IHomeKeyEventListener;
import com.tencent.libwecarwheelcontrolsdk.event.IVoiceKeyEventListener;
import com.tencent.libwecarwheelcontrolsdk.event.IVolKeyEventListener;
import com.tencent.libwecarwheelcontrolsdk.event.WheelControlEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.txz.component.wheelcontrol.IWheelControl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.PackageUtil;

/**
 * Created by cain on 2016/12/28.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class WheelControlTencentImpl implements IWheelControl {

    private static final String APPKEY = "WheelControl";
    private static final String SERVICE_UUID = "0000ff00-0000-1000-8000-00805f9b34fb";
    private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    private IBleInterface mBleInterface;
    private BluetoothAdapter mBluetoothAdapter;

    private OnGlobalWheelControlListener mGlobalWheelControlListener;

//    private HashMap<OnWheelControlListener,TXWheelControlListener> mListenerMap;
    // 只用注册一个监听，不用  map 保存
    private TXWheelControlListener mTXWheelControlListener;
    
//    private IInitCallback mInitCallback;
    private OnConnectionStatusLinstener mConnectionStatusLinstener;
    private boolean isServiceExist;

    public WheelControlTencentImpl() {
    	isServiceExist = PackageUtil.isServiceExist(GlobalContext.get(), "com.tencent.libwecarwheelcontrolsdk.core.service.WheelControlService");
    	if (isServiceExist) {
			JNIHelper.loge("TXWheelControl : service exist");
		} else {
			JNIHelper.loge("TXWheelControl : service not exist");
		}
    }

    @Override
    public void initialize(final IInitCallback oRun) {
    	if (!isServiceExist) {
    		AppLogicBase.runOnBackGround(new Runnable() {
    			@Override
    			public void run() {
    				if (oRun != null) {
    					oRun.onInit(false);
    				}
    			}
    		}, 0);
    		return;
		}
    	// Initializes Bluetooth adapter.
    	final BluetoothManager bluetoothManager = (BluetoothManager) GlobalContext.get().getSystemService(Context.BLUETOOTH_SERVICE);
    	mBluetoothAdapter = bluetoothManager.getAdapter();
    	
        WheelControlManager manager = WheelControlManager.getInstance();
        manager.init(GlobalContext.get(), APPKEY);
//        mInitCallback = oRun;

        // 设置本APP所有界面的事件处理方法名以及对应的按钮事件ID
        manager.appendMethodEventId("onLevorotation", WheelControlEvent.LEVOROTATION_EVENTID);
        manager.appendMethodEventId("onDextrorotation", WheelControlEvent.DEXTROROTATION_EVENTID);
        manager.appendMethodEventId("onOkClicked", WheelControlEvent.OK_KEY_CLICKED_EVENTID);
        manager.appendMethodEventId("onUpClicked", WheelControlEvent.UP_KEY_CLICKED_EVENTID);
        manager.appendMethodEventId("onDownClicked", WheelControlEvent.DOWN_KEY_CLICKED_EVENTID);
        manager.appendMethodEventId("onLeftClicked", WheelControlEvent.LEFT_KEY_CLICKED_EVENTID);
        manager.appendMethodEventId("onRightClicked", WheelControlEvent.RIGHT_KEY_CLICKED_EVENTID);
        manager.appendMethodEventId("onOkDoubleClicked", WheelControlEvent.OK_KEY_DOUBLE_CLICKED_EVENTID);
        manager.appendMethodEventId("onUpDoubleClicked", WheelControlEvent.UP_KEY_DOUBLE_CLICKED_EVENTID);
        manager.appendMethodEventId("onDownDoubleClicked", WheelControlEvent.DOWN_KEY_DOUBLE_CLICKED_EVENTID);
        manager.appendMethodEventId("onLeftDoubleClicked", WheelControlEvent.LEFT_KEY_DOUBLE_CLICKED_EVENTID);
        manager.appendMethodEventId("onRightDoubleClicked", WheelControlEvent.RIGHT_KEY_DOUBLE_CLICKED_EVENTID);
        manager.appendMethodEventId("onOkLongClicked", WheelControlEvent.OK_KEY_LONG_CLICKED_EVENTID);
        manager.appendMethodEventId("onUpLongClicked", WheelControlEvent.UP_KEY_LONG_CLICKED_EVENTID);
        manager.appendMethodEventId("onDownLongClicked", WheelControlEvent.DOWN_KEY_LONG_CLICKED_EVENTID);
        manager.appendMethodEventId("onLeftLongClicked", WheelControlEvent.LEFT_KEY_LONG_CLICKED_EVENTID);
        manager.appendMethodEventId("onRightLongClicked", WheelControlEvent.RIGHT_KEY_LONG_CLICKED_EVENTID);
        manager.appendMethodEventId("onOkDown", WheelControlEvent.OK_KEY_DOWN_EVENTID);
        manager.appendMethodEventId("onUpDown", WheelControlEvent.UP_KEY_DOWN_EVENTID);
        manager.appendMethodEventId("onDownDown", WheelControlEvent.DOWN_KEY_DOWN_EVENTID);
        manager.appendMethodEventId("onLeftDown", WheelControlEvent.LEFT_KEY_DOWN_EVENTID);
        manager.appendMethodEventId("onRightDown", WheelControlEvent.RIGHT_KEY_DOWN_EVENTID);
        manager.appendMethodEventId("onOkUp", WheelControlEvent.OK_KEY_UP_EVENTID);
        manager.appendMethodEventId("onUpUp", WheelControlEvent.UP_KEY_UP_EVENTID);
        manager.appendMethodEventId("onDownUp", WheelControlEvent.DOWN_KEY_UP_EVENTID);
        manager.appendMethodEventId("onLeftUp", WheelControlEvent.LEFT_KEY_UP_EVENTID);
        manager.appendMethodEventId("onRightUp", WheelControlEvent.RIGHT_KEY_UP_EVENTID);
        // 最后需要保存操作
        manager.saveMethodEventIds();

        mBleInterface = new BleInterfaceImp();
        AppLogicBase.runOnBackGround(new Runnable() {
            @Override
            public void run() {
            	JNIHelper.logd("TXWheelControl : registerBleInterface =========================");
                WheelControlManager.getInstance().registerBleInterface(mBleInterface);
            }
        }, 2000);
        
        AppLogicBase.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				if (oRun != null) {
					oRun.onInit(true);
				}
			}
		}, 0);
    }

    @Override
    public void release() {
    	if (!isServiceExist) {
			return;
		}
    	try {
    		mBleInterface.unlinkLeDevice(null);
    	} catch (RemoteException e) {
    	}
    	
        WheelControlManager.getInstance().unInit();
        mBluetoothAdapter = null;
        mGlobalWheelControlListener = null;
//        mListenerMap = null;
        mTXWheelControlListener = null;
        mConnectionStatusLinstener = null;
    }

    @Override
    public void scanLEDevice(boolean enable) {
    	if (!isServiceExist) {
			return;
		}
        try {
            if (enable) {
                mBleInterface.startLeUpdate();
            } else {
                mBleInterface.stopLeUpdate();
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    @Override
    public void registerWheelControlListener(OnWheelControlListener listener) {
    	if (!isServiceExist) {
			return;
		}
        if (listener == null) {
            return;
        }
        
        if (mTXWheelControlListener == null) {
        	// 没有注册监听事件
			mTXWheelControlListener = new TXWheelControlListener(listener);
			WheelControlManager.getInstance().register(mTXWheelControlListener);
		} else {
			// 已注册监听事件
			mTXWheelControlListener.setOnWheelControlListener(listener);
		}
        JNIHelper.logd("TXWheelControl : registerWheelControlListener");
        
        /*checkListenerMap();
        if (mListenerMap.containsKey(listener)) {
            WheelControlManager.getInstance().register(mListenerMap.get(listener));
        } else {
            TXWheelControlListener subscriber = new TXWheelControlListener(listener);
            mListenerMap.put(listener,subscriber);
            WheelControlManager.getInstance().register(subscriber);
        }*/
    }

    @Override
    public void unregisterWheelControlListener(OnWheelControlListener listener) {
    	if (!isServiceExist) {
			return;
		}
        if (listener == null) {
            return;
        }
        
        if (mTXWheelControlListener == null) {
			return;
		}
        
        if (listener != mTXWheelControlListener.getOnWheelControlListener()) {
			return;
		}
        
        WheelControlManager.getInstance().unregister(mTXWheelControlListener);
        mTXWheelControlListener = null;
        JNIHelper.logd("TXWheelControl : unregisterWheelControlListener");

        /*checkListenerMap();
        if (mListenerMap.containsKey(listener)) {
            WheelControlManager.getInstance().unregister( mListenerMap.remove(listener));
        }*/
    }

    @Override
    public void setGlobalWheelControlListener(OnGlobalWheelControlListener listener) {
    	if (!isServiceExist) {
			return;
		}
        if (mGlobalWheelControlListener != null) {
        	JNIHelper.logd("TXWheelControl : OnGlobalWheelControlListener is not null");
            return;
        }

        mGlobalWheelControlListener = listener;
        registerGlobalWheelControlListener();
    }

    @Override
    public void removeGlobalWheelControlListener() {
    	if (!isServiceExist) {
			return;
		}
        mGlobalWheelControlListener = null;
    }

    @Override
	public boolean isWheelControlConnected() {
		return WheelControlManager.getInstance().isWheelControlConnected();
	}

	@Override
	public void setConnectionStatusLinstener(OnConnectionStatusLinstener listener) {
		if (!isServiceExist) {
			return;
		}
		mConnectionStatusLinstener = listener;
	}

/*	private void checkListenerMap() {
        if (mListenerMap == null) {
            mListenerMap  = new HashMap<OnWheelControlListener,TXWheelControlListener>();
        }
    }*/

    private void registerGlobalWheelControlListener() {
        WheelControlManager.getInstance().setHomeKeyEventListener(new IHomeKeyEventListener() {
            @Override
            public void onHomeKeyEvent(byte eventId) {
                handlerGlobalKeyEvent(eventId);
            }
        });

        WheelControlManager.getInstance().setVoiceKeyEventListener(new IVoiceKeyEventListener() {
            @Override
            public void onVoiceKeyEvent(byte eventId) {
                handlerGlobalKeyEvent(eventId);
            }
        });

        WheelControlManager.getInstance().setBackKeyEventListener(new IBackKeyEventListener() {
            @Override
            public void onBackKeyEvent(byte eventId) {
                handlerGlobalKeyEvent(eventId);
            }
        });

        WheelControlManager.getInstance().setVolKeyEventListener(new IVolKeyEventListener() {
            @Override
            public void onVolKeyEvent(byte eventId) {
                handlerGlobalKeyEvent(eventId);
            }
        });
    }

    private void handlerGlobalKeyEvent(int eventId) {
        if (mGlobalWheelControlListener != null) {
            int txzId = 0;
            switch (eventId) {
                case WheelControlEvent.VOL_KEY_CLICKED_EVENTID:
                    txzId = TXZWheelControlEvent.VOL_KEY_CLICKED_EVENTID;
                    break;
                case WheelControlEvent.HOME_KEY_CLICKED_EVENTID:
                    txzId = TXZWheelControlEvent.HOME_KEY_CLICKED_EVENTID;
                    break;
                case WheelControlEvent.BACK_KEY_CLICKED_EVENTID:
                    txzId = TXZWheelControlEvent.BACK_KEY_CLICKED_EVENTID;
                    break;
                case WheelControlEvent.VOICE_KEY_CLICKED_EVENTID:
                    txzId = TXZWheelControlEvent.VOICE_KEY_CLICKED_EVENTID;
                    break;
                case WheelControlEvent.VOL_KEY_LONG_CLICKED_EVENTID:
                    txzId = TXZWheelControlEvent.VOL_KEY_LONG_CLICKED_EVENTID;
                    break;
                case WheelControlEvent.HOME_KEY_LONG_CLICKED_EVENTID:
                    txzId = TXZWheelControlEvent.HOME_KEY_LONG_CLICKED_EVENTID;
                    break;
                case WheelControlEvent.BACK_KEY_LONG_CLICKED_EVENTID:
                    txzId = TXZWheelControlEvent.BACK_KEY_LONG_CLICKED_EVENTID;
                    break;
                case WheelControlEvent.VOICE_KEY_LONG_CLICKED_EVENTID:
                    txzId = TXZWheelControlEvent.VOICE_KEY_LONG_CLICKED_EVENTID;
                    break;
                case WheelControlEvent.VOL_KEY_UP_EVENTID:
                    txzId = TXZWheelControlEvent.VOL_KEY_UP_EVENTID;
                    break;
                case WheelControlEvent.HOME_KEY_UP_EVENTID:
                    txzId = TXZWheelControlEvent.HOME_KEY_UP_EVENTID;
                    break;
                case WheelControlEvent.BACK_KEY_UP_EVENTID:
                    txzId = TXZWheelControlEvent.BACK_KEY_UP_EVENTID;
                    break;
                case WheelControlEvent.VOICE_KEY_UP_EVENTID:
                    txzId = TXZWheelControlEvent.VOICE_KEY_UP_EVENTID;
                    break;
                case WheelControlEvent.VOL_KEY_DOWN_EVENTID:
                    txzId = TXZWheelControlEvent.VOL_KEY_DOWN_EVENTID;
                    break;
                case WheelControlEvent.HOME_KEY_DOWN_EVENTID:
                    txzId = TXZWheelControlEvent.HOME_KEY_DOWN_EVENTID;
                    break;
                case WheelControlEvent.BACK_KEY_DOWN_EVENTID:
                    txzId = TXZWheelControlEvent.BACK_KEY_DOWN_EVENTID;
                    break;
                case WheelControlEvent.VOICE_KEY_DOWN_EVENTID:
                    txzId = TXZWheelControlEvent.VOICE_KEY_DOWN_EVENTID;
                    break;
                default:
            }
            if (txzId != 0) {
                mGlobalWheelControlListener.onKeyEvent(eventId);
            }
        }
    }

   class TXWheelControlListener {
        private OnWheelControlListener mWheelControlListener;

        public TXWheelControlListener(OnWheelControlListener wheelControlListener) {
            mWheelControlListener = wheelControlListener;
        }
        
        public void setOnWheelControlListener(OnWheelControlListener wheelControlListener) {
        	mWheelControlListener = wheelControlListener;
        }
        
        public OnWheelControlListener getOnWheelControlListener() {
        	return mWheelControlListener;
        }
        
        private void sendKeyEvent(int eventId) {
        	if (mWheelControlListener != null) {
        		mWheelControlListener.onKeyEvent(eventId);
			}
        }

        public void onLevorotation() {
        	sendKeyEvent(TXZWheelControlEvent.LEVOROTATION_EVENTID);
        }

        public void onDextrorotation() {
        	sendKeyEvent(TXZWheelControlEvent.DEXTROROTATION_EVENTID);
        }

        public void onOkClicked() {
        	sendKeyEvent(TXZWheelControlEvent.OK_KEY_CLICKED_EVENTID);
        }

        public void onUpClicked() {
        	sendKeyEvent(TXZWheelControlEvent.UP_KEY_CLICKED_EVENTID);
        }

        public void onDownClicked() {
        	sendKeyEvent(TXZWheelControlEvent.DOWN_KEY_CLICKED_EVENTID);
        }

        public void onLeftClicked() {
        	sendKeyEvent(TXZWheelControlEvent.LEFT_KEY_CLICKED_EVENTID);
        }

        public void onRightClicked() {
        	sendKeyEvent(TXZWheelControlEvent.RIGHT_KEY_CLICKED_EVENTID);
        }

        public void onOkDoubleClicked() {
        	sendKeyEvent(TXZWheelControlEvent.OK_KEY_DOUBLE_CLICKED_EVENTID);
        }

        public void onUpDoubleClicked() {
        	sendKeyEvent(TXZWheelControlEvent.UP_KEY_DOUBLE_CLICKED_EVENTID);
        }

        public void onDownDoubleClicked() {
        	sendKeyEvent(TXZWheelControlEvent.DOWN_KEY_DOUBLE_CLICKED_EVENTID);
        }

        public void onLeftDoubleClicked() {
        	sendKeyEvent(TXZWheelControlEvent.LEFT_KEY_DOUBLE_CLICKED_EVENTID);
        }

        public void onRightDoubleClicked() {
        	sendKeyEvent(TXZWheelControlEvent.RIGHT_KEY_DOUBLE_CLICKED_EVENTID);
        }

        public void onOkLongClicked() {
        	sendKeyEvent(TXZWheelControlEvent.OK_KEY_LONG_CLICKED_EVENTID);
        }

        public void onUpLongClicked() {
        	sendKeyEvent(TXZWheelControlEvent.UP_KEY_LONG_CLICKED_EVENTID);
        }

        public void onDownLongClicked() {
        	sendKeyEvent(TXZWheelControlEvent.DOWN_KEY_LONG_CLICKED_EVENTID);
        }

        public void onLeftLongClicked() {
        	sendKeyEvent(TXZWheelControlEvent.LEFT_KEY_LONG_CLICKED_EVENTID);
        }

        public void onRightLongClicked() {
        	sendKeyEvent(TXZWheelControlEvent.RIGHT_KEY_LONG_CLICKED_EVENTID);
        }

        public void onOkDown() {
        	sendKeyEvent(TXZWheelControlEvent.OK_KEY_DOWN_EVENTID);
        }

        public void onUpDown() {
        	sendKeyEvent(TXZWheelControlEvent.UP_KEY_DOWN_EVENTID);
        }

        public void onDownDown() {
        	sendKeyEvent(TXZWheelControlEvent.DOWN_KEY_DOWN_EVENTID);
        }

        public void onLeftDown() {
        	sendKeyEvent(TXZWheelControlEvent.LEFT_KEY_DOWN_EVENTID);
        }

        public void onRightDown() {
        	sendKeyEvent(TXZWheelControlEvent.RIGHT_KEY_DOWN_EVENTID);
        }

        public void onOkUp() {
        	sendKeyEvent(TXZWheelControlEvent.OK_KEY_UP_EVENTID);
        }

        public void onUpUp() {
        	sendKeyEvent(TXZWheelControlEvent.UP_KEY_UP_EVENTID);
        }

        public void onDownUp() {
        	sendKeyEvent(TXZWheelControlEvent.DOWN_KEY_UP_EVENTID);
        }

        public void onLeftUp() {
        	sendKeyEvent(TXZWheelControlEvent.LEFT_KEY_UP_EVENTID);
        }

        public void onRightUp() {
        	sendKeyEvent(TXZWheelControlEvent.RIGHT_KEY_UP_EVENTID);
        }

    }

    class BleInterfaceImp extends IBleInterface.Stub {
        private HashSet<BluetoothDevice> mDeviceSet = new HashSet<BluetoothDevice>();
        private BluetoothGatt mBluetoothGatt;

        /**
         * 开始扫描
         * @throws RemoteException
         */
        @Override
        public void startLeUpdate() throws RemoteException {
        	JNIHelper.logd("TXWheelControl : startLeUpdate");
            mDeviceSet.clear();
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }

        /**
         * 结束扫描
         * @throws RemoteException
         */
        @Override
        public void stopLeUpdate() throws RemoteException {
            JNIHelper.logd("TXWheelControl : stopLeUpdate");
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

        /**
         * 连接 BLE 设备
         * @param addr 蓝牙地址（00:00:00:00:00:00）
         * @throws RemoteException
         */
        @Override
        public void linkLeDevice(String addr) throws RemoteException {
            JNIHelper.logd("TXWheelControl : linkLeDevice addr = " + addr);
            if (mBluetoothGatt != null && addr.equalsIgnoreCase(mBluetoothGatt.getDevice().getAddress())) {
            	JNIHelper.logd("TXWheelControl : reconnect " + addr);
				mBluetoothGatt.connect();
				return;
			}
            for (BluetoothDevice device : mDeviceSet) {
                JNIHelper.logd("TXWheelControl : linkLeDevice BluetoothDevice = " + device.getAddress());
                if (addr.equalsIgnoreCase(device.getAddress())) {
                    JNIHelper.logd("TXWheelControl : linkLeDevice 2 addr = " + addr);
                    mBluetoothGatt = device.connectGatt(GlobalContext.get(), true, mGattCallback);
                }
            }
        }

        /**
         * 断开 BLE 设备
         * @param addr 蓝牙地址（00:00:00:00:00:00）
         * @throws RemoteException
         */
        @Override
        public void unlinkLeDevice(String addr) throws RemoteException {
            if (mBluetoothGatt != null) {
                JNIHelper.logd("TXWheelControl : unlinkDevice addr = " + addr);
                mBluetoothGatt.disconnect();
                // 释放资源
                mBluetoothGatt.close();
                mBluetoothGatt = null;
            } else {
                JNIHelper.logd("TXWheelControl : unlinkDevice mBluetoothGatt = null");
            }
        }

        /**
         * 写特征值
         * @param addr 蓝牙地址（00:00:00:00:00:00）
         * @param serviceUUID  服务的 UUID 字符串 （"0000ff00-0000-1000-8000-00805f9b34fb"）
         * @param characteristicUUID 特征值的 UUID 字符串
         * @param value 写入值
         * @throws RemoteException
         */
        @Override
        public void writeCharacteristic(String addr, String serviceUUID, String characteristicUUID, byte[] value) throws RemoteException {
            //check mBluetoothGatt is available
            JNIHelper.logd("TXWheelControl : writeCharacteristic addr = " + addr + "; service:" + serviceUUID + "; characteristic:" + characteristicUUID);
            if (mBluetoothGatt == null) {
                JNIHelper.loge("TXWheelControl : lost connection");
            }
            BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (Service == null) {
                JNIHelper.loge("TXWheelControl : service not found!");
            }
            BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID.fromString(characteristicUUID));
            if (charac == null) {
                JNIHelper.loge("TXWheelControl : char not found!");
            } else {
                charac.setValue(value);
                mBluetoothGatt.writeCharacteristic(charac);
            }
        }

        /**
         * 读特征值
         * @param addr 蓝牙地址（00:00:00:00:00:00）
         * @param serviceUUID  服务的 UUID 字符串 （"0000ff00-0000-1000-8000-00805f9b34fb"）
         * @param characteristicUUID 特征值的 UUID 字符串
         * @return
         * @throws RemoteException
         */
        @Override
        public boolean readCharacteristic(String addr, String serviceUUID, String characteristicUUID) throws RemoteException {
            //check mBluetoothGatt is available
            JNIHelper.logd("TXWheelControl : readCharacteristic addr = " + addr + "; service:" + serviceUUID + "; characteristic:" + characteristicUUID);
            if (mBluetoothGatt == null) {
                JNIHelper.loge("TXWheelControl : lost connection");
                return false;
            }
            BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (Service == null) {
                JNIHelper.loge("TXWheelControl : service not found!");
                return false;
            }
            BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID.fromString(characteristicUUID));
            if (charac == null) {
                JNIHelper.loge("TXWheelControl : char not found!");
                return false;
            }
            return mBluetoothGatt.readCharacteristic(charac);
        }

        /**
         * 读特征值
         * @param addr 蓝牙地址（00:00:00:00:00:00）
         * @param serviceUUID  服务的 UUID 字符串 （"0000ff00-0000-1000-8000-00805f9b34fb"）
         * @param characteristicUUID 特征值的 UUID 字符串
         * @param enable  是否打开通知
         * @throws RemoteException
         */
        @Override
        public void setNotification(String addr, String serviceUUID, String characteristicUUID, boolean enable) throws RemoteException {
            JNIHelper.logd("TXWheelControl : setNotification addr = " + addr + "; service:" + serviceUUID + "; characteristic:" + characteristicUUID);
            if (mBluetoothGatt == null) {
                JNIHelper.loge("TXWheelControl : lost connection");
            }
            BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString(serviceUUID));
            if (Service == null) {
                JNIHelper.loge("TXWheelControl : service not found!");
            }
            BluetoothGattCharacteristic charac = Service.getCharacteristic(UUID.fromString(characteristicUUID));
            if (charac == null) {
                JNIHelper.loge("TXWheelControl : char not found!");
            }
            mBluetoothGatt.setCharacteristicNotification(charac, enable);

            BluetoothGattDescriptor descriptor = charac.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
            if (descriptor != null) {
                JNIHelper.logd("TXWheelControl : notification set : " + enable);
                if (enable) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        }

        /**
         * 获取 BLE 设备的连接状态，结果通过 onBleConnectStateChanged 回调
         * @throws RemoteException
         */
        @Override
        public void getBleConnectState() throws RemoteException {
            //check mBluetoothGatt is available
            if (mBluetoothGatt == null) {
                JNIHelper.loge("TXWheelControl : lost connection");
            }
            int connectState = mBluetoothGatt.getConnectionState(mBluetoothGatt.getDevice());
            // 方控连接状态变化时回调
            WheelControlManager.getInstance().onBleConnectStateChanged(mBluetoothGatt.getDevice().getAddress(), connectState);
        }

        // Device scan callback.
        // You can only scan for Bluetooth LE devices or scan for Classic Bluetooth devices
        BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
				JNIHelper.logd("TXWheelControl:onLeScan device: "
								+ bluetoothDevice.getName() + " / " + bluetoothDevice.getAddress());
				String btName = bluetoothDevice.getName();
                if (btName == null) {
                    return;
                }
                mDeviceSet.add(bluetoothDevice);
                ParcelUuid[] uuids = bluetoothDevice.getUuids();
                if (bluetoothDevice.getName().equals("TAS_CONTROLLER")) {
                    if (uuids != null) {
                        String[] supportUuids = new String[uuids.length];
                        for (int i = 0; i < uuids.length; i++) {
                            supportUuids[i] = uuids[i].toString();
                        }
                        JNIHelper.logd("TXWheelControl:onLeScan UUID=" + Arrays.toString(supportUuids));
                        // 扫描结果回调
                        WheelControlManager.getInstance().onBleDeviceFound(bluetoothDevice.getAddress(), bluetoothDevice.getName(), supportUuids);
                    } else {
                        String[] supportUuids = {SERVICE_UUID};
                        JNIHelper.logd("TXWheelControl:onLeScan UUID=null");
                        WheelControlManager.getInstance().onBleDeviceFound(bluetoothDevice.getAddress(), bluetoothDevice.getName(), supportUuids);
                    }
                } else if (uuids == null) {
                    return;
                }
            }
        };

        BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                JNIHelper.logd("TXWheelControl: onConnectionStateChange: " + newState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    WheelControlManager.getInstance().onBleConnectStateChanged(gatt.getDevice().getAddress(), BleDefine.BLE_CONNECTED);
                    gatt.discoverServices();
                    
                    // 根据设备连接状态的改变，切换初始化回调
                    if (mConnectionStatusLinstener != null) {
						mConnectionStatusLinstener.isConnected(true);
					}
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    WheelControlManager.getInstance().onBleConnectStateChanged(gatt.getDevice().getAddress(), BleDefine.BLE_DISCONNECTED);
                    
                    if (mConnectionStatusLinstener != null) {
						mConnectionStatusLinstener.isConnected(false);
					}
                }
            }

            @Override
            // New services discovered
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                JNIHelper.logd("TXWheelControl: onServicesDiscovered: " + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService Service = mBluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
                    if (Service == null) {
                        JNIHelper.loge("TXWheelControl: service not found!");
                    } else {
                        // 连接上蓝牙设备是回调
                        WheelControlManager.getInstance().onServiceDiscoverFinished(gatt.getDevice().getAddress());
                    }
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                JNIHelper.logd("TXWheelControl : onDescriptorWrite: " + gatt.getDevice().getAddress());
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                JNIHelper.logd("TXWheelControl : onCharacteristicChanged: " + gatt.getDevice().getAddress() + "value = " + characteristic.getValue().length);
                WheelControlManager.getInstance().onBleDeviceNotified(
                        gatt.getDevice().getAddress(),
                        characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        characteristic.getValue());
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                JNIHelper.logd("TXWheelControl: onCharacteristicRead: " + gatt.getDevice().getAddress());
                WheelControlManager.getInstance().onBleDeviceNotified(
                        gatt.getDevice().getAddress(),
                        characteristic.getService().getUuid().toString(),
                        characteristic.getUuid().toString(),
                        characteristic.getValue());
            }
        };

    }

}
