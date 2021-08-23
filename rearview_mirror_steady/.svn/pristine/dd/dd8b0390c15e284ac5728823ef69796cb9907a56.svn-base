package com.txznet.comm.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;

public class StartReciver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (TextUtils.isEmpty(action))
			return;
		if (action.equals(GlobalContext.get().getApplicationInfo().packageName
				+ ".startTXZService")) {
			
		}
	}
}
