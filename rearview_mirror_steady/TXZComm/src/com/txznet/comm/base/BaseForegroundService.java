package com.txznet.comm.base;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.update.UpdateCenter;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.util.TXZFileConfigUtil;

public abstract class BaseForegroundService extends Service {
	public final static int NOTIFICATION_ID = Process.myPid();

	@Override
	public void onCreate() {
		super.onCreate();

		LogUtil.logd("onCreate: Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT);
		LogUtil.logd("onCreate: " + this.toString());

		boolean enableForegroundService = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_ENABLE_FOREGROUND_SERVICE, false);

		if (Build.VERSION.SDK_INT >= 19) {
			if (enableForegroundService) {
				startForeground(NOTIFICATION_ID, new Notification());
			}
		}
		
//		if (Build.VERSION.SDK_INT < 18) {
//			startForeground(NOTIFICATION_ID, new Notification());
//		} else {
//			Intent i = new Intent(GlobalContext.get(), ReserveService9.class);
//			i.putExtra("NOTIFICATION_ID", NOTIFICATION_ID);
//			GlobalContext.get().startService(i);
//		}
		
		//system app升级问题，主动查询主进程版本路径
		if (AppLogicBase.isMainProcess() == false) {
			ServiceManager.getInstance().sendInvoke(getPackageName(),
					"comm.PackageInfo", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							try {
								String mainApk = null;
								if (data != null) {
									mainApk = data.getJSONObject().optString(
											"sourceDir");
									if (getApplicationInfo().sourceDir
											.equals(mainApk) == false) {
										LogUtil.logw("dismatch source apk, need restart device: "
												+ data + "#" + mainApk);

										UpdateCenter
												.showRestartDeviceNotification(-1);

										// BaseForegroundService.this.stopSelf();
									}
								}
							} catch (Exception e) {
								LogUtil.logw("check source apk exception: "
										+ e.getClass() + "#"
										+ e.getLocalizedMessage());
							}
						}
					});
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// return super.onStartCommand(intent, flags, startId);
		return Service.START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		this.stopForeground(true);
		LogUtil.logd("onDestroy: " + this.toString());
		super.onDestroy();
	}
}
