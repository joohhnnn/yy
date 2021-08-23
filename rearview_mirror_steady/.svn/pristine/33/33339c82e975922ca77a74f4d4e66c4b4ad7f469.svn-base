package com.txznet.txz.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

import com.txznet.apkinstaller.IApkInstallService;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.reserve.activity.ReserveStandardActivity0;
import com.txznet.txz.jni.JNIHelper;

public class PackageInstaller {

	private static IApkInstallService service;

	private static Set<String> mInstallUrls = new HashSet<String>();

	private static void procTasks() {
		synchronized (mInstallUrls) {
			if (service == null)
				return;
			for (String url : mInstallUrls) {
				try {
					service.install(url, GlobalContext.get().getPackageName());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mInstallUrls.clear();
		}
	}

	private static ServiceConnection con = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			service = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder binder) {
			service = IApkInstallService.Stub.asInterface(binder);
			procTasks();
		}
	};

	private static void BindService() {
		Intent intent = new Intent("com.txznet.apkinstaller.IApkInstallService");
		// for android 5.0
		intent.setPackage("com.txznet.apkinstaller");
		GlobalContext.get().bindService(intent, con, Context.BIND_AUTO_CREATE|Context.BIND_IMPORTANT);
	}

	public static void rebindService() {
		BindService();
	}

	public static boolean installApkByPackageManager(String url) {
		try {
			synchronized (mInstallUrls) {
				mInstallUrls.add(url);
			}

			if (service == null) {
				rebindService();
			}

			procTasks();

			return true;
		} catch (Exception e) {
			JNIHelper.loge("install package error");
			e.printStackTrace();
			return false;
		}
	}

	public static boolean installApkByIntent(String url) {
		// if (url.startsWith("http://")) {
		// Uri uri = Uri.parse(url);
		// Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		// GlobalContext.get().startActivity(intent);
		// } else {
		// File fNewApk = new File(url);
		// Intent intent = new Intent();
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setAction(android.content.Intent.ACTION_VIEW);
		// intent.setDataAndType(Uri.fromFile(fNewApk),
		// "application/vnd.android.package-archive");
		// GlobalContext.get().startActivity(intent);
		// }
		Intent intent = new Intent(GlobalContext.get(), ReserveStandardActivity0.class);
		intent.putExtra("url", url);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
		return true;
	}
}
