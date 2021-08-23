package com.txznet.wakeup.component.wakeup.yunzhisheng;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.wakeup.R;
import com.txznet.wakeup.component.wakeup.IWakeup;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;

public class WakeupImpl implements IWakeup{
	private static final String WAKEUPWORD = "WAKUP";
	private static final String WAKEUP_TAG = "wakeup";
	private SpeechUnderstander mWakeUpRecognizer = null;
	IInitCallback mInitCallback = null;
	IWakeupCallback mWakeupCallback = null;
	
	private boolean mInitSuccessed = false;
	private boolean mIniting = false;
	
	@Override
	public int initialize(String[] cmds, final IInitCallback oRun) {
		mInitCallback = oRun;
		if (mInitSuccessed){
			onInit();
			return 0;
		}
		Runnable oInitRun = new Runnable(){
			@Override
			public void run() {
				initWakeUp();
			}
			
		};
		AppLogic.runOnBackGround(oInitRun, 0);
		return 0;
	}
    
	/**
	 * 初始化本地离线唤醒
	 */
	private void initWakeUp() {
		LogUtil.logd("mIniting = " + mIniting);
		if (mIniting){
			LogUtil.logd("initWakeup...");
			return;
		}
		mIniting = true;
		mWakeUpRecognizer = new SpeechUnderstander(AppLogic.getApp(), GlobalContext.get().getString(R.string.yunzhisheng_appKey), null);
		mWakeUpRecognizer.setOption(SpeechConstants.ASR_SERVICE_MODE, SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		
		mWakeUpRecognizer.setListener(new SpeechUnderstanderListener() {
			@Override
			public void onResult(int type, String jsonResult) {
				LogUtil.logd("jsonResult = " + jsonResult);
			}
			
			@Override
			public void onEvent(int type, int timeMs) {
				switch (type) {
				case SpeechConstants.WAKEUP_EVENT_RECOGNITION_SUCCESS:
					onWakeup();
					break;
				case SpeechConstants.ASR_EVENT_RECORDING_START:
					LogUtil.logd("startRecord");
					break;
				case SpeechConstants.ASR_EVENT_RECORDING_STOP:
					LogUtil.logd("stopRecord");
					break;
				case SpeechConstants.ASR_EVENT_ENGINE_INIT_DONE:
					mInitSuccessed = true;
					onInit();
					break;
				case SpeechConstants.WAKEUP_EVENT_SET_WAKEUPWORD_DONE:
					break;
				default:
					break;
				}
			}
			
			@Override
			public void onError(int type, String errorMSG) {
			}
		});
		
		mWakeUpRecognizer.init("");
	}
	
	@Override
	public synchronized int start(IWakeupCallback oCallback) {
		LogUtil.logd("start");
		mWakeupCallback = oCallback;
		if (!mInitSuccessed){
			LogUtil.logd("mInitSuccessed = " + mInitSuccessed);
			Runnable oInitRun = new Runnable(){
				@Override
				public void run() {
					initWakeUp();
				}
			};
			AppLogic.runOnBackGround(oInitRun, 0);
			return -1;
		}
        mWakeUpRecognizer.setOption(SpeechConstants.WAKEUP_WORK_ENGINE, 0);
		mWakeUpRecognizer.start(WAKEUP_TAG);
		return 1;
	}

	@Override
	public synchronized void stop() {
		LogUtil.logd("stop");
		mWakeupCallback = null;
		if (!mInitSuccessed){
			LogUtil.logd("mInitSuccessed = " + mInitSuccessed);
			return;
		}
		mWakeUpRecognizer.cancel();
	}

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
    
	private void onInit(){
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
                doInit();
			}
		}, 0);
	}
	
	private synchronized void doInit(){
		LogUtil.logd("mInitSuccessed = " + mInitSuccessed);
		IInitCallback callBack = mInitCallback;
		mInitCallback = null;
		mIniting = false;
		if (callBack != null){
			callBack.onInit(mInitSuccessed);
		}
		 mWakeUpRecognizer.setAudioSource(new AudioSourceImpl());
		//if mWakeupCallback is not null, because wakeup has started by other
		//main purpose is deal the case that service killed by system
		if (mWakeupCallback != null){
	        mWakeUpRecognizer.setOption(SpeechConstants.WAKEUP_WORK_ENGINE, 0);
			mWakeUpRecognizer.start(WAKEUP_TAG);
		}
	}
	
	private synchronized void doWakeup(){
		if (mWakeupCallback != null){
			mWakeupCallback.onWakeUp(WAKEUPWORD);
		}
	}
}
