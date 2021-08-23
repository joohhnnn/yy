package com.txznet.marketing;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.ActiveListener;
import com.txznet.sdk.TXZConfigManager.InitListener;
import com.txznet.sdk.TXZConfigManager.InitParam;

public class SDKDemoApp extends Application implements InitListener,
		ActiveListener {

	public final static String TAG = "SDKDemoApp";

	private static SDKDemoApp instance;
	protected static Handler uiHandler = new Handler(Looper.getMainLooper());

	public static SDKDemoApp getApp() {
		return instance;
	}

	public static void runOnUiGround(Runnable r, long delay) {
		if (delay > 0) {
			uiHandler.postDelayed(r, delay);
		} else {
			uiHandler.post(r);
		}
	}

	public static void removeUiGroundCallback(Runnable r) {
		uiHandler.removeCallbacks(r);
	}

	public InitParam mInitParam;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		// 同行者
		TXZConfigManager.getInstance().initialize(this, this);

	}

	@Override
	public void onFirstActived() {

		Log.e("Prisoner", "onFirstActived");
	}

	@Override
	public void onError(int errCode, String errDesc) {

		Log.e("Prisoner", "onError errCore " + errCode + " errDesc " + errDesc);
	}

	public void onSuccess() {

		Log.e("Prisoner", "com.txznet.sdkdemo  onSuccess");

	}

}
