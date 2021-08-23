package com.txznet.txz.module.launch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.txz.jni.JNIHelper;

public class UpdatedReciver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		JNIHelper.logd("onReceive " + intent.getAction());
		LaunchManager.getInstance().launchWithUpdated();
	}
}