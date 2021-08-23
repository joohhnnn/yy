package com.txznet.txz.component.tts.iflytek;

import android.media.AudioManager;
import android.os.Bundle;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.tts.R;
import com.txznet.txz.component.tts.ITts;

public class IflytekTts  implements ITts{
    
    private boolean bInited = false;
	// 语音合成对象
	private SpeechSynthesizer mTts;

	// 默认发音人
	private String voicer = "";
	
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_LOCAL;

	/**
	 * 初始化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			LogUtil.logd("code = " + code);
			bInited = code == ErrorCode.SUCCESS;
			doInit_async(code ==ErrorCode.SUCCESS);
		}
			
	};

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		
		@Override
		public void onSpeakBegin() {
			
		}

		@Override
		public void onSpeakPaused() {
			
		}

		@Override
		public void onSpeakResumed() {
			
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// 合成进度
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null){
				doSuccess_async();
			}else{
				doError_async();
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
		}
	};

	/**
	 * 参数设置
	 * @param param
	 * @return 
	 */
	private void setParam() {
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// 根据合成引擎设置相应参数
		mTts.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置在线合成发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
		 //设置合成语速
		 mTts.setParameter(SpeechConstant.SPEED, "50");
		 //设置合成音调
		 mTts.setParameter(SpeechConstant.PITCH, "50");
		// 设置合成音量
		mTts.setParameter(SpeechConstant.VOLUME, "100");

		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "" + mStream);
		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "false");
	}
	
    private synchronized int initTts() {
    	if (bInited){
    		LogUtil.logw("bInited = " + bInited);
    		return 0;
    	}
    	init_iflytek_sdk();
    	int nRet = 0;
		// 初始化合成对象
		mTts = SpeechSynthesizer.createSynthesizer(AppLogic.getApp(), mTtsInitListener);
        return nRet;
    }
    
    private IInitCallBack mInitCallBack = null;
	@Override
	public void init(final IInitCallBack cb) {
		mInitCallBack = cb;
		initTts();
	}
    
	private ITtsCallBack mCallBack = null;
	private int mStream = AudioManager.STREAM_MUSIC;
	@Override
	public void start(int stream, String text, ITtsCallBack cb) {
		if (!bInited){
			initTts();
		}
		mCallBack = cb;
		mStream = stream;
		int nRet = -1;
		LogUtil.logd("speak " + text);
		setParam();
		nRet = mTts.startSpeaking(text, mTtsListener);
		if (nRet < 0){
			LogUtil.loge("nRet = " + nRet);
			doError_async();
		}
	}
    
	private void doInit(boolean bOK){
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
		mTts.stopSpeaking();
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
	
    private boolean bSdkInited = false;
	public void init_iflytek_sdk(){
		if (bSdkInited){
			return;
		}
		bSdkInited = true;
		SpeechUtility.createUtility(AppLogic.getApp(), "appid =" + AppLogic.getApp().getString(R.string.app_id_iflytek));
		// 以下语句用于设置日志开关（默认开启），设置成false时关闭语音云SDK日志打印
	    Setting.setShowLog(false);
	}
}
