package com.txznet.nav.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.nav.MyApplication;
import com.txznet.nav.NavManager;
import com.txznet.nav.ui.InitActivity;

public class MyBootBroadcast extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			if (!NavManager.getInstance().isInit()) {
				Intent i = new Intent(MyApplication.getInstance(),
						InitActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				MyApplication.getInstance().startActivity(i);
			}
		}
	}
}
