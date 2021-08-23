package com.txznet.reserve.service;

import android.content.Intent;
import android.os.IBinder;

import com.txznet.comm.base.BaseForegroundService;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.ttsplayer.proxy.TtsPlayerServer;

public class ReserveService5 extends BaseForegroundService {
	private TtsPlayerServer mServer = null;

	@Override
	public IBinder onBind(Intent intent) {
		if (mServer == null) {
			mServer = new TtsPlayerServer();
		}
		return mServer.getMessenger().getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		AppLogic.exit();
		return super.onUnbind(intent);
	}
}
		
