package com.txznet.loader;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.InitListener;
import com.txznet.tts.module.tts.TtsManager;
import com.txznet.txz.util.NativeHelper;
import com.txznet.txz.util.NativeHelper.UnzipOption;

public class AppLogic extends AppLogicBase {
	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.logd("onCreate");
		initSource();
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
	
	private void initSource(){
		// 初始化资源
		String appDir = GlobalContext.get().getApplicationInfo().dataDir;
		NativeHelper.unzipFiles(
				GlobalContext.get().getApplicationInfo().sourceDir,
				new UnzipOption[] {
						UnzipOption.createUnzipDirOption(
								"assets/data/", appDir + "/data/"),
								UnzipOption.createUnzipDirOption(
										"assets/extend/", appDir + "/files/") }, 5000);//将extend下的百度TTS离线授权文件(事先从已经授权成功的Apk中取得),
		                                                                                                         //复制到百度TTS离线授权文件的默认位置,没联网之前一直不能使用的问题
	}
}
