package com.txznet.txz.component.call.dxwy;

import com.txznet.txz.jni.JNIHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DXWYPhoneStatReciever extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		JNIHelper.logd("onRecieve"+intent.getAction()+" bundle:"+intent.getExtras());
		CallDxwyImpl.onSysStateUpdate(intent);	
	}

}
