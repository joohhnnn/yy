package com.txznet.reserve.service;

import com.txznet.comm.base.BaseForegroundService;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.wakeup.mix.WakeupServer;
import com.txznet.txz.component.wakeup.mix.WkMsgConstants;
import com.txznet.txz.component.wakeup.sence.WakeupSenceServer;
import com.txznet.txz.jni.JNIHelper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public  class ReserveService2 extends BaseForegroundService{
    private WakeupSenceServer mServer = null;
    
	@Override
	public IBinder onBind(Intent intent) {
		mServer = new WakeupSenceServer(WkMsgConstants.ENGINE_TYPE_YZS_SENCE_IMPL);
		JNIHelper.logd("WakeupSenceServer onBind");
        return mServer.getMessenger().getBinder();
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		AppLogic.exit();
		return super.onUnbind(intent);
	}	
}
		
