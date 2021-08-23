package com.txznet.nav.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.nav.manager.DataSourceManager;

public class SDCardReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DataSourceManager.getInstance().resetSourcePath(true);
	}
}