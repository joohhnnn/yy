package com.txznet.wakeup.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.service.IService;

public class MyService extends Service {
	public class Binder extends IService.Stub {
		@Override
		public byte[] sendInvoke(final String packageName, final String command, final byte[] data) throws RemoteException {
			return null;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new Binder();
	}
}
