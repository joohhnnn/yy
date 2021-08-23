package com.txznet.reserve.service;

import android.content.Intent;
import android.os.IBinder;

import com.txznet.comm.base.BaseForegroundService;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.dvr.DVRServer;
import com.txznet.loader.AppLogic;

public class ReserveService4 extends BaseForegroundService {
	private DVRServer mServer = new DVRServer();

	@Override
	public IBinder onBind(Intent intent) {
		return mServer.getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		AppLogic.exit();
		return super.onUnbind(intent);
	}
}
		
