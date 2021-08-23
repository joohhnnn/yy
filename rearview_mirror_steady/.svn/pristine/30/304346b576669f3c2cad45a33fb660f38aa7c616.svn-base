package com.txznet.music.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExitAppReceiver extends BroadcastReceiver {

	public final String receiver = "com.txznet.music.close.app";

	@Override
	public void onReceive(Context arg0, Intent intent) {
		if (receiver.equals(intent.getAction())) {
			UIHelper.exit();
		}
	}

}
