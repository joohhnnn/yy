package com.txznet.txz.module.launch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.txz.jni.JNIHelper;

public class AsrButtonReciver extends BroadcastReceiver {
	private final String KEY_EVENT = "txz.intent.action.KEY_EVENT";
	private final int ACIVATE_VOICE = 0xff00;

	@Override
	public void onReceive(Context context, Intent intent) {
		int keyEvent = intent.getIntExtra(KEY_EVENT, -1);
		JNIHelper.logd("onReceive "+keyEvent);
		if (ACIVATE_VOICE == keyEvent) {
			JNIHelper.logd("DXWYButtonReciver doLaunch");
			LaunchManager.getInstance().launchWithRecord();
		}

	}
}