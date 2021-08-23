package com.txznet.txz.util;

import com.txznet.txz.jni.JNIHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

public class PackageUtil {
	
	public static boolean isServiceExist(Context context, String cls) {
		if (null == cls) {
			return false;
		}
		PackageManager pm = context.getPackageManager();
		try {
			ServiceInfo serviceInfo = pm.getServiceInfo(new ComponentName(context, cls), PackageManager.GET_RECEIVERS);
			if (serviceInfo != null && cls.equals(serviceInfo.name)) {
				return true;
			}
		} catch (Exception e) {
			JNIHelper.logw("This service does not exist in AndroidManifest");
			e.printStackTrace();
		}
		return false;
	}

}
