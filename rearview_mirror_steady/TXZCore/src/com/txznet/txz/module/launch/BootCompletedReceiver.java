package com.txznet.txz.module.launch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txz.ui.event.UiEvent;
import com.txz.ui.platform.UiPlatform;
import com.txznet.loader.AppLogic;
import com.txznet.txz.CoreService;
import com.txznet.txz.jni.JNIHelper;

public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// JNIHelper.logd("BootCompletedReceiver");
		// Intent intentService = new Intent(context, CoreService.class);
		// context.startService(intentService);
		// if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
		// AppLogic.initWhenStart();
		// AppLogic.runOnUiGround(new Runnable() {
		// @Override
		// public void run() {
		// JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM,
		// UiPlatform.SUBEVENT_POWER_ACTION_POWER_ON);
		// }
		// }, 100);
		// }
	}
}
