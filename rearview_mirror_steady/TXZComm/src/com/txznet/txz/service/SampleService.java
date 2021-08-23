package com.txznet.txz.service;

import com.txznet.comm.remote.ServiceHandler;
import com.txznet.txz.service.IService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

/*
 *
         <service android:name="com.txznet.bluetooth.service.BluetoothService">
            <intent-filter>
                <action android:name="com.txznet.bluetooth.service.IService"/>
                <category  android:name="android.intent.category.DEFAULT"/>
            </intent-filter>
        </service>
 *
 */

public class SampleService extends Service{
	private static class TXZBinder extends IService.Stub{
		@Override
		public byte[] sendInvoke(String packageName, String command, byte[] data)
				throws RemoteException {
			if (TextUtils.isEmpty(command))
				return null;
			ServiceHandler.preInvoke(packageName, command, data);
			return null;
		}
	} 

	@Override
	public IBinder onBind(Intent intent) {
		return new TXZBinder();
	}
	
}
