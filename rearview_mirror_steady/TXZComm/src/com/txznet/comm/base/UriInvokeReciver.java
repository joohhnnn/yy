package com.txznet.comm.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;

public class UriInvokeReciver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// URI API
		Uri uri = intent.getData();
		if (uri == null)
			return;

		LogUtil.logd("recive uri invoke: " + uri.toString());

		if (!"txznet".equals(uri.getScheme()))
			return;

		if (!GlobalContext.get().getPackageName().equals(uri.getHost()))
			return;

		String command = uri.getPath();
		while (command.startsWith("/")) {
			command = command.substring(1);
		}

		if (TextUtils.isEmpty(command))
			return;

		String param = uri.getQuery();
		LogUtil.logd("recive uri invoke " + command + ": " + param);
		byte[] bs = null;
		if (param != null)
			bs = param.getBytes();

		ServiceManager.getInstance().sendInvoke(
				GlobalContext.get().getPackageName(), command, bs, null);
	}
}
