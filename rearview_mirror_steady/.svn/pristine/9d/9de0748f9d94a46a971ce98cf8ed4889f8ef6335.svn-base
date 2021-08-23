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
import android.database.Observable;

public class MyInstallReceiver extends BroadcastReceiver {
	
	public static final InstallObservable SINSTALL_OBSERVABLE = new InstallObservable();
	
	public static class InstallObservable extends Observable<InstallObservable.InstallObserver> {
		
		public static interface InstallObserver {
			public void onApkInstall(String packageName);
			
			public void onApkUnInstall(String packageName);
		}
		
		public void notifyApkInstall(String packageName){
			synchronized (mObservers) {
				for (int i = mObservers.size() - 1; i >= 0; i--) {
					mObservers.get(i).onApkInstall(packageName);
				}
			}
		}
		
		public void notifyApkUnInstall(String packageName) {
			synchronized (mObservers) {
				for (int i = mObservers.size() - 1; i >= 0; i--) {
					mObservers.get(i).onApkUnInstall(packageName);
				}
			}
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// 接收安装广播
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
			String packageName = intent.getDataString();
			JNIHelper.logd("Core:package:installed:" + packageName);
			PackageManager.getInstance().onAppAdded(packageName);
			if ("package:com.txznet.android_call".equals(packageName)) {
				ServiceManager.getInstance().sendInvoke("com.txznet.android_call", "", null, null);
			}
			if (packageName.startsWith("package:")) {
				packageName=packageName.substring(packageName.indexOf("package:")+8);
			}
			
			SINSTALL_OBSERVABLE.notifyApkInstall(packageName);
			
			if (ServiceManager.MUSIC.equals(packageName)) {// 获取最新版本
				AudioTxzImpl.getAppVersion();
			}
			PackageManager.getInstance().refreshApkInfo(packageName);
		}
		// 接受更新事件
		if (intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {
			String packageName = intent.getDataString();
			if (packageName.startsWith("package:")) {
				packageName=packageName.substring(packageName.indexOf("package:")+8);
			}
			JNIHelper.logd("Core:package:REPLACED:" + packageName);
			PackageManager.getInstance().refreshApkInfo(packageName);
			if (ServiceManager.MUSIC.equals(packageName)) {// 获取最新版本
//				AudioTxzImpl.setVersion(false);
				AudioTxzImpl.getAppVersion();
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
			JNIHelper.logd("Core:package:REMOVED:" + packageName);
			
			SINSTALL_OBSERVABLE.notifyApkUnInstall(packageName);
			
			JNIHelper.logd("removed:" + packageName);
			PackageManager.getInstance().onAppRemoved(packageName);
			if (ServiceManager.MUSIC.equals(packageName)) {// 获取最新版本
				//TODO:这里可能会导致卸载的时候安装了电台之家，version还没有及时改过来
//				AudioTxzImpl.setVersion(false);
			}
			if (NavQihooImpl.PACKAGE_NAME.equals(packageName)) {
				NavManager.getInstance().checkPoiResApp();
			}
		}
		PackageManager.getInstance().sendAppList();
	}
}
