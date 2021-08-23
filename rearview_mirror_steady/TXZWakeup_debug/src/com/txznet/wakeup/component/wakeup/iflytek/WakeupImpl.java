package com.txznet.wakeup.component.wakeup.iflytek;

import android.os.Bundle;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.wakeup.R;
import com.txznet.wakeup.component.wakeup.IWakeup;

public class WakeupImpl implements IWakeup{
	private static final String WAKEUPWORD = "WAKUP";
	IInitCallback mInitCallback = null;
	IWakeupCallback mWakeupCallback = null;
	
	// 语音唤醒对象
	private VoiceWakeuper mIvw = null;
	
	private boolean mInitSuccessed = false;
	
	private boolean loadResource() {
		// 加载识唤醒地资源，resPath为本地识别资源路径
		StringBuffer param = new StringBuffer();
		String resPath = ResourceUtil.generateResourcePath(AppLogic.getApp(),
				RESOURCE_TYPE.assets,
				"ivw/" + AppLogic.getApp().getString(R.string.iflytek_appKey)
						+ ".jet");
		param.append(ResourceUtil.IVW_RES_PATH + "=" + resPath);
		param.append("," + ResourceUtil.ENGINE_START + "="
				+ SpeechConstant.ENG_IVW);
		boolean bRet = SpeechUtility.getUtility().setParameter(
				ResourceUtil.ENGINE_START, param.toString());
		return bRet;
	}
	
	@Override
	public int initialize(String[] cmds, final IInitCallback oRun) {
		mInitCallback = oRun;
		boolean bRet = false;
		do {
			if (mInitSuccessed){
				break;
			}
			bRet = loadResource();
			if (!bRet) {
				break;
			}
			// 初始化唤醒对象
			mIvw = VoiceWakeuper.createWakeuper(AppLogic.getApp(), null);
		} while (false);

		if (mInitSuccessed || (bRet && mIvw != null)) {
			mInitSuccessed = true;
		} else {
			mInitSuccessed = false;
		}

		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				LogUtil.logd("mInitSuccessed = " + mInitSuccessed);
				mInitCallback.onInit(mInitSuccessed);
			}
		}, 0);

		return 1;
	}

	@Override
	public synchronized int start(IWakeupCallback oCallback) {
		LogUtil.logd("start");
		if (!mInitSuccessed){
			LogUtil.logd("mInitSuccessed = " + mInitSuccessed);
			return -1;
		}
		mWakeupCallback = oCallback;
		// 清空参数
		mIvw.setParameter(SpeechConstant.PARAMS, null);
		// 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
		mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:-20");
		// 设置唤醒模式
		mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
		// 设置持续进行唤醒
		mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
		if (!mIvw.isListening()) {
			mIvw.startListening(mWakeuperListener);
		}
		return 1;
	}

	@Override
	public synchronized void stop() {
		LogUtil.logd("stop");
		if (!mInitSuccessed){
			LogUtil.logd("mInitSuccessed = " + mInitSuccessed);
			return;
		}
		mIvw.stopListening();
		mWakeupCallback = null;
	}
	
	private WakeuperListener mWakeuperListener = new WakeuperListener() {

		@Override
		public void onResult(WakeuperResult result) {
			LogUtil.logd("onResult = " + result.getResultString());
			onWakeup();
		}

		@Override
		public void onError(SpeechError error) {
			LogUtil.logd("Error = " + error.getErrorDescription());
		}

		@Override
		public void onBeginOfSpeech() {
		}

		@Override
		public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
		}
	};

	public void setWakeupKeywords(String[] mWakeupKeywords) {
		
	}

	@Override
	public int startWithRecord(IWakeupCallback oCallback,
			String savePathPrefix, String[] overTag) {
		return 0;
	}

	@Override
	public void stopWithRecord() {	
	}

	@Override
	public void setWakeupThreshold(float val) {
		
	}
	
	private void onWakeup(){
		Runnable oRun = new Runnable(){
			@Override
			public void run() {
				doWakeup();
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
    
	private synchronized void doWakeup(){
		if (mWakeupCallback != null){
			mWakeupCallback.onWakeUp(WAKEUPWORD);
		}
	}
}
