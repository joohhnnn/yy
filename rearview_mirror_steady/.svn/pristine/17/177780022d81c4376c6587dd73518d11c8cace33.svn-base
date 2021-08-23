package com.txznet.loader;

import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.wakeup.R;

public class AppLogic extends AppLogicBase {
	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.logd("onCreate");
		ServiceManager.getInstance().keepConnection(ServiceManager.TXZ,
				new Runnable() {
					@Override
					public void run() {
						
					}
				});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wakeup.event.process.start", null, null);
	}
	
	public void initSDK_IFly(){
		// 应用程序入口处调用,避免手机内存过小，杀死后台进程,造成SpeechUtility对象为null
		// 设置你申请的应用appid
		StringBuffer param = new StringBuffer();
		param.append("appid="+GlobalContext.get().getString(R.string.iflytek_appKey));
		param.append(",");
		param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
		Setting.showLogcat(false);
		SpeechUtility.createUtility(GlobalContext.get(), param.toString());
		LogUtil.logd(param.toString());
	}
}
