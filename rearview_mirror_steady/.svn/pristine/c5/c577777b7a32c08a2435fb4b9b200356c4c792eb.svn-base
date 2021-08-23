package com.txznet.sample.service;

import com.txznet.comm.remote.ServiceHandler;
import com.txznet.txz.service.IService;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class MyService extends Service {
	public class SampleBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(String packageName, String command, byte[] data)
				throws RemoteException {
			ServiceHandler.preInvoke(packageName, command, data);
			return null;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new SampleBinder();
	}

}
