package com.txznet.txz.component.asr.txzasr;

import android.os.Bundle;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.ModuleManager;

public class IFlyPlugin{
	private static final String RESULT_TYPE_JSON = "json";
	private static final int NET_TIMEOUT = 8000;//在线识别超时时间
	SpeechRecognizer mRecognizer = null;
    private boolean bInitOk = false;
    private boolean bReady = false;
	
	public int initialize() {
		JNIHelper.logd("initialize");
		ModuleManager.getInstance().initSdk_Ifly();
		Setting.setLocationEnable(true);
		mRecognizer = SpeechRecognizer.createRecognizer(GlobalContext.get(), null);;
		if (mRecognizer != null){
			bInitOk = true;
		}
		JNIHelper.logd("bInitOK = " + bInitOk);
		return 0;
	}

	
	public void release() {
		stop();
		mRecognizer.destroy();
		mRecognizer = null;
	}

	public static interface INetAsrCallBack{
		public void onResult(String text);
		public void onError(int errCode);
		public void onSpeechBegin();
		public void onSpeechEnd();
	}
	
	private INetAsrCallBack mNetAsrCallBack = null;
	private AsrOption mAsrOption = null;
	public int start(INetAsrCallBack callBack, AsrOption oOption) {
		if (bReady){
			return IAsr.ERROR_ASR_ISBUSY;
		}
		mAsrOption = oOption;
		mNetAsrCallBack = callBack;
		if (!start_startRecorder()){
			bReady = false;
			return IAsr.ERROR_ABORT;
		}
        bReady = true;
		return IAsr.ERROR_SUCCESS;
	}

	int mResultType = 0;

	boolean start_startRecorder() {
		mRecognizer.setParameter(SpeechConstant.ENGINE_TYPE,  SpeechConstant.TYPE_CLOUD);
		// 设置增加在线语义识别
		mRecognizer.setParameter("sch", "1");
		mRecognizer.setParameter("nlp_version", "2.0");
		mRecognizer.setParameter(SpeechConstant.DOMAIN, "iat"); // 设置在线语义识别领域

		// 根据手动模式设置前端静音超时
		mRecognizer.setParameter(SpeechConstant.VAD_BOS, "" + mAsrOption.mBOS);
		mRecognizer.setParameter(SpeechConstant.VAD_EOS, "" + mAsrOption.mEOS);
		
		//设置录音最长时间
		mRecognizer.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "" + mAsrOption.mKeySpeechTimeout);

		// 通用参数设置
		mRecognizer.setParameter(SpeechConstant.ASR_PTT, "0");// 设置标点符号
		mRecognizer.setParameter(SpeechConstant.RESULT_TYPE, RESULT_TYPE_JSON); // 设置结果类型
		mRecognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");// /*MediaRecorder.AudioSource.MIC*/);
		mRecognizer.setParameter(SpeechConstant.NET_TIMEOUT, "" + NET_TIMEOUT);//设置在线识别超时时间
		mRecognizer.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "" + false);
		
		JNIHelper.logd("params: " + mRecognizer.getParameter(SpeechConstant.PARAMS));

		int ret = mRecognizer.startListening(mRecognizerListener);
		if (ret != ErrorCode.SUCCESS) {
			return false;
		}
		return true;
	}

	RecognizerListener mRecognizerListener = new RecognizerListener() {
		
		public void onBeginOfSpeech() {
			JNIHelper.logd("onBeginOfSpeech");
		}
		
		public void onEndOfSpeech(){
			JNIHelper.logd("onEndOfSpeech");
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					doSpeechEvent(true);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
			
		}
		
		public void onResult(RecognizerResult results, boolean isLast) {
			final RecognizerResult oResults = results;
			final boolean bLast = isLast;
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					doResult(oResults, bLast);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}

		public void onVolumeChanged(int volume, byte[] data) {
		}

		public void onError(SpeechError error) {
			final SpeechError err = error;
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					doError(err);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}

		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			
		}
	};
    
	private void doSpeechEvent(boolean end) {
		if (mNetAsrCallBack != null) {
			if (end) {
				mNetAsrCallBack.onSpeechEnd();
			} else {
				mNetAsrCallBack.onSpeechBegin();
			}
		}
	}
	
	private void doResult(RecognizerResult results, boolean isLast){
		if (!bReady){
			return;
		}
		JNIHelper.logd(results.getResultString() + ": isLast = " + isLast);
		INetAsrCallBack callBcak = mNetAsrCallBack;
		mNetAsrCallBack = null;
		bReady = false;
		if (callBcak != null){
		    callBcak.onResult(results.getResultString());
		}
	}
	
	private void doError(SpeechError error) {
		JNIHelper.logd(error.getErrorDescription());
		JNIHelper.logd("errorCode = " + error.getErrorCode());
		INetAsrCallBack callBcak = mNetAsrCallBack;
		mNetAsrCallBack = null;
		bReady = false;
		if (callBcak != null) {
			int errorCode = IAsr.ERROR_CODE;
			switch(error.getErrorCode()){
			case ErrorCode.ERROR_NO_SPEECH:
			case ErrorCode.MSP_ERROR_NO_DATA:
				errorCode = IAsr.ERROR_NO_SPEECH;
				break;
			case ErrorCode.ERROR_NO_MATCH:
				errorCode = IAsr.ERROR_NO_MATCH;
				break;
			case 12400://未知网络错误,shit!!!
			case ErrorCode.MSP_ERROR_MSG_PARSE_ERROR:
			case ErrorCode.ERROR_NO_NETWORK:
			case ErrorCode.ERROR_NET_EXCEPTION:
			case ErrorCode.ERROR_NETWORK_TIMEOUT:
			    errorCode = IAsr.ERROR_ASR_NET_REQUEST;
				break;
			}
			callBcak.onError(errorCode);
		}
	}
	
	private void stopInner() {
		if (mRecognizer == null || !bInitOk){
			return;
		}
		bReady = false;
		mRecognizer.cancel();
	}
	
	public void cancel() {
		if (!bReady){
			return;
		}
		
		if (mRecognizer == null || !bInitOk){
			return;
		}
		stopInner();
	}

	
	public void stop() {
		if (!bReady){
			return;
		}
		
		if (mRecognizer == null || !bInitOk){
			return;
		}
		mRecognizer.stopListening();
		JNIHelper.logd("stop");
	}

	
	public boolean isBusy() {
		if (mRecognizer == null || !bInitOk){
			return false;
		}
		return mRecognizer.isListening();
	}
    
	public void write(byte[] data, int size){
		if (!bReady){
			return;
		}
		
		if (mRecognizer == null || !bInitOk){
			return;
		}
		mRecognizer.writeAudio(data, 0, size);
	}
	
	SdkKeywords mSetNetDataSdkKeywords = null;
	IImportKeywordsCallback mSetNetDataCallback = null;
	
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		if (mRecognizer == null || !bInitOk){
			return false;
		}
		mRecognizer.updateLexicon(oKeywords.strType, oKeywords.strContent, mLexiconListener);
		mSetNetDataSdkKeywords = oKeywords;
		mSetNetDataCallback= oCallback;
		return true;
	}
	
	/**
     * 更新词典监听器。
     */
	private LexiconListener mLexiconListener = new LexiconListener() {
		@Override
		public void onLexiconUpdated(String lexiconId, SpeechError error) {
			if(error == null){
				JNIHelper.logd("update success");
				IImportKeywordsCallback oSetNetDataCallback = mSetNetDataCallback;
				mSetNetDataCallback = null;
				if (oSetNetDataCallback != null){
					oSetNetDataCallback.onSuccess(mSetNetDataSdkKeywords);
				}
			}else{
				JNIHelper.logd("update fail : " + error.getErrorDescription());
				IImportKeywordsCallback oSetNetDataCallback = mSetNetDataCallback;
				mSetNetDataCallback = null;
				if (oSetNetDataCallback != null){
					oSetNetDataCallback.onError(-1, mSetNetDataSdkKeywords);
				}
			}
		}
	};
}
