package com.txznet.wakeup.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.service.IService;
import com.txznet.wakeup.component.wakeup.IWakeup.IInitCallback;
import com.txznet.wakeup.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.wakeup.module.WakeupManager;

/*
 * 与TXZCore通信的协议格式为：
 * wakeup.XXX.XXX
 * wakeup.event.XXX
 */
public class MyService extends Service {
	public class Binder extends IService.Stub {
		@Override
		public byte[] sendInvoke(final String packageName, final String command, final byte[] data) throws RemoteException {
			byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
			if (command.startsWith("wakeup.")) {
				LogUtil.logd(packageName + " invokeWakeup " + command);
				ret = invokeWakeup(packageName, command.substring("wakeup.".length()), data);
			}else if ("comm.exitTXZ.exited".equals(command)){
				WakeupManager.getInstance().stop();
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						AppLogic.exit();
					}
				}, 5000);//delay 5000ms to exit after txz exit, then txz would not start wakeup because wakeup is not inited at that time
			}
			return ret;
		}
	}

	private byte[] invokeWakeup(final String packageName, String command, byte[] data) {
		LogUtil.logd(packageName + ":" + command);
		if ("init".equals(command)){
			String[] cmds = null;
			IInitCallback oRun = new IInitCallback() {
				@Override
				public void onInit(boolean bSuccess) {
					ServiceManager.getInstance().sendInvoke(packageName, "wakeup.event.init.result", ("" + bSuccess).getBytes(), null);
				}
			};
			WakeupManager.getInstance().initialize(cmds, oRun);
			
		}else if ("wakeup.start".equals(command)){
			IWakeupCallback oCallBack = new IWakeupCallback() {
				@Override
				public void onWakeUp(String text) {
					ServiceManager.getInstance().sendInvoke(packageName, "wakeup.event.wakeup.result", text.getBytes(), null);
				}
			};
			WakeupManager.getInstance().start(oCallBack);
		}else if ("wakeup.stop".equals(command)){
			WakeupManager.getInstance().stop();
		}
		return null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new Binder();
	}
}
