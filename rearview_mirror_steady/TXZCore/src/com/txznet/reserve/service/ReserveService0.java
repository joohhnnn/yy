package com.txznet.reserve.service;

import android.content.Intent;
import android.os.IBinder;

import com.txznet.comm.base.BaseForegroundService;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.mix.AsrServer;

public class ReserveService0 extends BaseForegroundService {
	private AsrServer mServer = null;

	@Override
	public IBinder onBind(Intent intent) {
		mServer = new AsrServer(null);
		return mServer.getMessenger().getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		AppLogic.exit();
		return super.onUnbind(intent);
	}
}
		
