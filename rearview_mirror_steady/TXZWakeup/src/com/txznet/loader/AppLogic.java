package com.txznet.loader;

import android.util.Log;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.DebugCfg;

public class AppLogic extends AppLogicBase {
	@Override
	public void onCreate() {
		super.onCreate();
		if (!DebugCfg.debug_yzs()){
			LogUtil.setConsoleLogLevel(Log.ERROR);
		}
		LogUtil.logd("onCreate");
	}
}
