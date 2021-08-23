package com.txznet.reserve.service;

import android.content.Intent;
import android.os.IBinder;

import com.txznet.comm.base.BaseForegroundService;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.tts.mix.TtsServer;

public class ReserveService3 extends BaseForegroundService {
	private TtsServer mServer = null;

	@Override
	public IBinder onBind(Intent intent) {
		if (mServer == null) {
			mServer = new TtsServer(null);
		}
		return mServer.getMessenger().getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		AppLogic.exit();
		return super.onUnbind(intent);
	}
}
		
