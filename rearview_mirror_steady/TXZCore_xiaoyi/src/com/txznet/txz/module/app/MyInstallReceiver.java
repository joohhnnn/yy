package com.txznet.txz.module.app;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.txz.component.music.txz.AudioTxzImpl;
import com.txznet.txz.component.nav.qihoo.NavQihooImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyInstallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// 接收安装广播
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
			String packageName = intent.getDataString();
			JNIHelper.logd("installed:" + packageName);
			PackageManager.getInstance().onAppAdded(packageName);
			if ("package:com.txznet.android_call".equals(packageName)) {
				ServiceManager.getInstance().sendInvoke("com.txznet.android_call", "", null, null);
			}
			if (packageName.startsWith("package:")) {
				packageName=packageName.substring(packageName.indexOf("package:")+8);
			}
			if (ServiceManager.MUSIC.equals(packageName)) {// 获取最新版本
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.get.version", null, new GetDataCallback() {

					@Override
					public int getTimeout() {
						return 60 * 1000;
					}

					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (null != data && null != data.getBoolean()) {
							AudioTxzImpl.newVersion = data.getBoolean();
						} else {
							AudioTxzImpl.newVersion = false;
						}
					}
				});
			}
			PackageManager.getInstance().refreshApkInfo(packageName);
		}
		// 接受更新事件
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
			String packageName = intent.getDataString();
			if (packageName.startsWith("package:")) {
				packageName=packageName.substring(packageName.indexOf("package:")+8);
			}
			PackageManager.getInstance().refreshApkInfo(packageName);
			if (ServiceManager.MUSIC.equals(packageName)) {// 获取最新版本
				AudioTxzImpl.newVersion = false;
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.get.version", null, new GetDataCallback() {

					@Override
					public int getTimeout() {
						return 60 * 1000;
					}

					@Override
					public void onGetInvokeResponse(ServiceData data) {
						if (null != data && null != data.getBoolean()) {
							AudioTxzImpl.newVersion = data.getBoolean();
						} else {
							AudioTxzImpl.newVersion = false;
						}
					}
				});
			}
			if (NavQihooImpl.PACKAGE_NAME.equals(packageName)) {
				NavManager.getInstance().checkPoiResApp();
				LocationManager.getInstance().checkLocResApp();
			}
		}
		// 接收卸载广播
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
			String packageName = intent.getDataString();
			if (packageName.startsWith("package:")) {
				packageName=packageName.substring(packageName.indexOf("package:")+8);
			}
			JNIHelper.logd("removed:" + packageName);
			PackageManager.getInstance().onAppRemoved(packageName);
			if (ServiceManager.MUSIC.equals(packageName)) {// 获取最新版本
				AudioTxzImpl.newVersion = false;
			}
			if (NavQihooImpl.PACKAGE_NAME.equals(packageName)) {
				NavManager.getInstance().checkPoiResApp();
			}
		}
		PackageManager.getInstance().sendAppList();
	}

}
