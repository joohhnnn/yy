package com.txznet.sdk;

import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.txznet.comm.remote.ServiceCallerCheck;
import com.txznet.comm.remote.ServiceHandlerBase;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.sdkinner.TXZServiceCommandDispatcher;
import com.txznet.txz.service.IService;

public class TXZService extends Service {

	static boolean mTXZHasExited = false;

	static class TXZServiceBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(final String packageName,
				final String command, final byte[] data) throws RemoteException {
			if (ServiceCallerCheck.checkBinderCaller(packageName, command, data) == false) {
				return null;
			}
			if (TextUtils.isEmpty(command))
				return null;
			if (ServiceManager.TXZ.equals(packageName)) {
				if ("comm.exitTXZ.exited".equals(command)) {
					LogUtil.logd(packageName + " comm.exitTXZ.exited");
					mTXZHasExited = true;
					synchronized (TXZPowerManager.class) {
						if (TXZPowerManager.mReleased) {
							ServiceManager.getInstance().mDisableSendInvoke = true;
						}
					}
					return null;
				}
				if ("comm.exitTXZ.inited".equals(command)) {
					LogUtil.logd(packageName + " comm.exitTXZ.inited");
					if (TXZPowerManager.mReleased != null
							&& TXZPowerManager.mReleased == true) {
						LogUtil.logw("release already, but txz inited again, release it");
						// 释放状态时，被别人误初始化，重新发送释放通知
						TXZPowerManager.getInstance().releaseTXZ();
						return null;
					}
					mTXZHasExited = false;
					TXZConfigManager.getInstance().onReconnectTXZ();
					return null;
				}
			}
			byte[] ret = ServiceHandlerBase.preInvoke(packageName, command,
					data);
			for (Map.Entry<String, TXZServiceCommandDispatcher.CommandProcessor> entry : TXZServiceCommandDispatcher.mProcessors
					.entrySet()) {
				if (command.startsWith(entry.getKey())) {
					if (entry.getValue() == null)
						break;
					return entry.getValue().process(packageName,
							command.substring(entry.getKey().length()), data);
				}
			}
			return ret;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new TXZServiceBinder();
	}

	static interface CommandProcessor extends
			TXZServiceCommandDispatcher.CommandProcessor {
	}

	static void setCommandProcessor(String prefix, CommandProcessor processor) {
		TXZServiceCommandDispatcher.setCommandProcessor(prefix, processor);
	}
}
