package com.txznet.txz.component.tts.lele;

import com.lele.sdk.ErrorCode;
import com.lele.sdk.LeleAccout;
import com.lele.sdk.LeleUtility;
import com.lele.sdk.LeleUtility.DeviceType;
import com.lele.sdk.player.Player;
import com.lele.sdk.player.PlayerProcess;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.tts.ITts;

public class LeleTts  implements ITts{
	
	private static class Account {
		public final static String AppId = "3";
		public final static String AppKey = "3926C32D61AD47DBB0F9719E5D319844";
		public final static String AppSecret = "974adb794541f516779c3d86e6e661d9";
		public final static String ServiceUrl = "http://voice.cp21.ott.cibntv.net";
	};
	
    private boolean bInited = false;

	private synchronized int initTts() {
		if (bInited) {
			LogUtil.logw("bInited = " + bInited);
			return 0;
		}
		int nRet = 0;
		
		LeleUtility.showLogcat(true);
		LeleUtility.DebugMode();
		LeleUtility.setDeviceType(DeviceType.PHONE);
		LeleAccout account = new LeleAccout();
		account.setAppId(Account.AppId);
		account.setAppKey(Account.AppKey);
		account.setAppSecret(Account.AppSecret);
		account.setServiceUrl(Account.ServiceUrl);
		com.lele.sdk.ErrorCode errCode = LeleUtility.init(AppLogic.getApp(), account);
		if (errCode != ErrorCode.NONE) {
			LogUtil.loge("error:" + errCode);
			doInit_async(false);
		}
		Player.getInstance().init();
		doInit_async(true);
		return nRet;
	}
    
    private IInitCallBack mInitCallBack = null;
	@Override
	public void init(final IInitCallBack cb) {
		mInitCallBack = cb;
		initTts();
	}
    
	private ITtsCallBack mCallBack = null;
	@Override
	public void start(int stream, String text, ITtsCallBack cb) {
		if (!bInited){
			initTts();
		}
		mCallBack = cb;
		LogUtil.logd("speak " + text);
		final String strText = text;
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				playText(strText);
			}
		};
		AppLogic.runOnUiGround(oRun, 0);
	}
    
	private void playText(String text){
		Player.getInstance().playText(text.toString(), new PlayerProcess() {
            @Override
            public void onStart() {
            	LogUtil.logd("speak  start");
            }

            @Override
            public void onEnd() {
            	LogUtil.logd("speak  end");
            	doSuccess_async();
            }

			@Override
			public void onError(com.lele.sdk.ErrorCode errCode) {
				LogUtil.logd("speak  error : " + errCode.name());
				doError_async();
			}
        });
	}
	
	private void doInit(boolean bOK){
		bInited = bOK;
		IInitCallBack cb = mInitCallBack;
		mInitCallBack = null;
		if (cb != null){
			cb.onInit(bOK);
		}
	}
	
	@Override
	public void stop() {
		if (!bInited){
			return;
		}
		Player.getInstance().stop();
	}
	
	private void doError(){
		if (mCallBack != null){
			mCallBack.onError();
		}
	}
	
	private void doSuccess(){
		if (mCallBack != null){
			mCallBack.onSuccess();
		}
	}
	
	private void doSuccess_async(){
		Runnable oRun = new Runnable(){
			@Override
			public void run() {
				doSuccess();
			}
			
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void doError_async(){
		Runnable oRun = new Runnable(){
			@Override
			public void run() {
				doError();
			}
			
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void doInit_async(final boolean bOk){
		Runnable oRun = new Runnable(){
			@Override
			public void run() {
				doInit(bOk);
			}
			
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
}
