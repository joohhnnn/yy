package com.txznet.sdkdemo;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.txznet.comm.Tx.Tx;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.FloatToolType;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicToolType;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.sdk.TXZConfigManager.ActiveListener;
import com.txznet.sdk.TXZConfigManager.AsrEngineType;
import com.txznet.sdk.TXZConfigManager.InitListener;
import com.txznet.sdk.TXZConfigManager.InitParam;
import com.txznet.sdk.TXZConfigManager.TtsEngineType;
import com.txznet.sdk.TXZTtsPlayerManager;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.service.RemoteRecord;

import static com.txznet.sdk.TXZConfigManager.EXT_AUDIOSOURCE_TYPE_TXZ;

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

	InitParam mInitParam;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;


		String appId = getResources().getString(R.string.txz_sdk_init_app_id);
		String appToken = getResources().getString(R.string.txz_sdk_init_app_token);
		mInitParam = new InitParam(appId, appToken);
		mInitParam.setDialogTimeOut(1000*10);
		mInitParam.setFloatToolType(FloatToolType.FLOAT_TOP);
 
        mInitParam.setFilterNoiseType(1);
		mInitParam.setWakeupKeywordsNew("你好小踢");
		mInitParam.setExtAudioSourcePkg("com.txznet.sdkdemo");
		mInitParam.setExtAudioSourceType(EXT_AUDIOSOURCE_TYPE_TXZ);
		mInitParam.useExternalAudioSource(true);
		TXZConfigManager.getInstance().initialize(this, mInitParam, this, this);


		Intent intent = new Intent(this,RemoteRecord.class);
		startService(intent);

	}

	@Override
	public void onFirstActived() {
		// TODO 首次联网激活，如果需要出厂激活提示，可以在这里完成
	}

	@Override
	public void onError(int errCode, String errDesc) {
		// TODO 初始化出错
	}

	@Override
	public void onSuccess() {

	}
}
