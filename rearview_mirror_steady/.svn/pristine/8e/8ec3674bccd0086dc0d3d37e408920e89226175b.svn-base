package com.txznet.wakeup.service;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.wakeup.component.wakeup.mix.WakeupServer;
import com.txznet.wakeup.component.wakeup.mix.WkMsgConstants;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public  class WakeupService extends Service{
    private WakeupServer mServer = null;
    
	@Override
	public IBinder onBind(Intent intent) {
		 LogUtil.logd("bind");
         mServer = new WakeupServer(WkMsgConstants.ENGINE_TYPE_YZS_IMPL);
         return mServer.getMessenger().getBinder();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		AppLogic.exit();
		return super.onUnbind(intent);
	}	
}
		
