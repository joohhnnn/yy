package com.txznet.dvr;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.reserve.service.ReserveService4;
import com.txznet.txz.jni.JNIHelper;

public class DVRProxy {
	
	private static DVRProxy sInstance = null;
	
	public static DVRProxy getInstance() {
		if (sInstance == null) {
			synchronized (DVRProxy.class) {
				if (sInstance ==null) {
					sInstance = new DVRProxy();
				}
			}
		}
		return sInstance;
	}
	
	public static void release() {
		JNIHelper.logd("DVRProxy: release");
		if (sInstance != null) {
			sInstance.onDestroy();
			sInstance = null;
		}
	}
	
	private class DVRServiceConnection implements ServiceConnection {
	
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = IDVRInterface.Stub.asInterface(service);
			JNIHelper.logi("DVRProxy: DVRServer Connected");
			DVRScaner.getInstance().procTasks();
		}
	
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			JNIHelper.logi("DVRProxy: DVRServer Disconnected");
		}
		
	}

	private IDVRInterface mService = null;
	private DVRServiceConnection mConnection = new DVRServiceConnection();
	private DVRProxy() {
		super();
	}
	
	/**
     * 销毁客户端代理，不能再该对象，会报异常
     */
    private void onDestroy() {
    	unbindService();
		mConnection = null;
	}

	public boolean enableInvoke() {
		return mService != null;
	}

	public boolean bindService() {
		try {
			Intent intent = new Intent(GlobalContext.get(), ReserveService4.class);
			// for android 5.0  ServiceManager.TXZ
			intent.setPackage(ServiceManager.TXZ);
			return GlobalContext.get().bindService(intent, mConnection,
					Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
		} catch (Exception e) {
			JNIHelper.loge("DVRProxy: bindService error:" + e.getMessage());
			return false;
		}
	}
	
	private void unbindService() {
		try {
			GlobalContext.get().unbindService(mConnection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mService = null;
	}
	
	public boolean getFrameAtTime(String inFile , String outFile,  long time){
		if (mService != null) {
			try {
				return mService.getFrameAtTime(inFile, outFile, time);
			} catch (Exception e) {
				e.printStackTrace();
				JNIHelper.loge("DVRProxy: remote invoke error:" + e.getMessage());
			}
		}
		bindService();
		SystemClock.sleep(200);
		return false;
	}
	
}
