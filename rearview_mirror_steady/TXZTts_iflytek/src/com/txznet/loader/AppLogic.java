package com.txznet.loader;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.InitListener;
import com.txznet.tts.module.tts.TtsManager;

public class AppLogic extends AppLogicBase {
	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.logd("onCreate");
		initCore();
	}
	
	private void initCore(){
		TXZConfigManager.getInstance().initialize(getApp(), new InitListener() {
			@Override
			public void onSuccess() {
				LogUtil.logd("onSuccess");
				TtsManager.getInstance().initComponent();
			}
			@Override
			public void onError(int arg0, String arg1) {
				
			}
		});
	}
	
}
