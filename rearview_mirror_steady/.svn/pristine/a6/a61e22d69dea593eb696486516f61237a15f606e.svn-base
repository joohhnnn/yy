package com.txznet.reserve.service;

import android.content.Intent;
import android.os.IBinder;

import com.txznet.comm.base.BaseForegroundService;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.wakeup.mix.WakeupServer;
import com.txznet.txz.component.wakeup.mix.WkMsgConstants;

public  class ReserveService1 extends BaseForegroundService{
    private WakeupServer mServer = null;
    
	@Override
	public IBinder onBind(Intent intent) {
         mServer = new WakeupServer(WkMsgConstants.ENGINE_TYPE_YZS_IMPL);
         return mServer.getMessenger().getBinder();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		AppLogic.exit();
		return super.onUnbind(intent);
	}	
}
		
