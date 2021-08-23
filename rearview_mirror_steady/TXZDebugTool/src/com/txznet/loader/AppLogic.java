package com.txznet.loader;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.InitListener;

public class AppLogic extends AppLogicBase {
	@Override
	public void onCreate() {
		super.onCreate();

		TXZConfigManager.getInstance().initialize(GlobalContext.get(),
				new InitListener() {
					@Override
					public void onSuccess() {
					}

					@Override
					public void onError(int arg0, String arg1) {
					}
				});
	}
}
