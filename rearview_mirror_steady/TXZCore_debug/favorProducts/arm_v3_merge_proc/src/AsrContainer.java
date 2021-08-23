package com.txznet.txz.component.asr.mix;

import android.os.SystemClock;
import android.text.TextUtils;

import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.AsrCallbackFactory.ICallBackNotify;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy.CallBackOption;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy.CallBackOption.EngineType;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.util.runnables.Runnable2;

public class AsrContainer implements IAsr{
	private static class Result{
		public EngineType mEngineType = EngineType.ENGINE_NONE;
		public int mErrorCode = IAsr.ERROR_SUCCESS;
		public float mScore = -100;
		public VoiceParseData mVoiceData = null;
		public long mRefeshTime = SystemClock.elapsedRealtime();
	}
	
     private SuperEngineBase mNetAsr = null;
     private SuperEngineBase mLocalAsr = null;
	 private IInitCallback mInitCallback = null;
	 private Result[] mResults = null;
	 private int mLocalNetEngineType = AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET;
     public AsrContainer(){
	 }
     
	@Override
	public int initialize(IInitCallback oRun) {
		if (!TextUtils.isEmpty(ProjectCfg.getIflyAppId())) {
			mLocalNetEngineType = AsrMsgConstants.ENGINE_TYPE_IFLYTEK_NET;
		}else{
			mLocalNetEngineType = AsrMsgConstants.ENGINE_TYPE_YZS_NET;
		}
		mInitCallback = oRun;
		
		//初始化回调处理器
		mAsrCallBackProxy = AsrCallbackFactory.proxy(mCallBackHandler, mCallBackNotify);
		mLocalCallBackOption =new CallBackOption(EngineType.ENGINE_LOCAL);
		mNetCallBackOption =new CallBackOption(EngineType.ENGINE_NET);
		mMixCallBackOption =new CallBackOption(EngineType.ENGINE_MIX);
		mLocalCallback = SuperEngineBase.createAsrCallBack(mAsrCallBackProxy, mLocalCallBackOption);
		mNetCallback = SuperEngineBase.createAsrCallBack(mAsrCallBackProxy, mNetCallBackOption);
		mMixCallback = SuperEngineBase.createAsrCallBack(mAsrCallBackProxy, mMixCallBackOption);
		
		//mLocalNetEngineType = AsrMsgConstants.ENGINE_TYPE_YZS_NET;
		boolean bLocal = true;
		if (mLocalNetEngineType == AsrMsgConstants.ENGINE_TYPE_YZS_NET){
			bLocal = false;
		}
		mLocalAsr = new YZSSuperEngine();
		if (bLocal){
			mNetAsr = SuperEngineProxy.getProxy(bLocal);
		}else{
			mNetAsr = SuperEngineProxy.getProxy();
		}
		mNetAsr.setEngineType(mLocalNetEngineType);
		mNetAsr.initialize(null);
   	    mLocalAsr.initialize(mLocalAsrInitCallback);
		return 0;
	}
	
	IInitCallback mLocalAsrInitCallback = new IInitCallback() {
		
		@Override
		public void onInit(boolean bSuccess) {
			if(mInitCallback == null){
				return;
			}
			mInitCallback.onInit(bSuccess);
		}
	};
	
	@Override
	public void release() {
		if (mNetAsr != null){
			mNetAsr.release();
		}
		mLocalAsr.release();
	}
    
	private AsrOption mNetAsrOption = null;
	private AsrOption mLocalAsrOption = null;
	private AsrOption mAsrOption = null;
	private AsrType mAsrType = AsrType.ASR_MIX;
	private CallBackOption mLocalCallBackOption = null;
	private CallBackOption mNetCallBackOption = null;
	private CallBackOption mMixCallBackOption = null;
	private boolean isStartRecording = false;
	private boolean isAsrWorking = false;
	
	private IAsrCallback mLocalCallback = null;
	private IAsrCallback mNetCallback = null;
	private IAsrCallback mMixCallback = null;
	
	private ICallBackNotify mCallBackNotify = new ICallBackNotify() {
		@Override
		public boolean enable() {
			return isAsrWorking;
		}
	};
	
	private ComplexAsrCallBackHandler mCallBackHandler = new ComplexAsrCallBackHandler() {
		
		@Override
		public void onVolume(CallBackOption oOption, int vol) {
			//在线出错, 才使用离线引擎的音量,或者使用非离线引擎的音量
			if (oOption.engineType == EngineType.ENGINE_LOCAL){
				if (mResults[1].mErrorCode != IAsr.ERROR_SUCCESS){
					mAsrOption.mCallback.onVolume(mAsrOption, vol);
					return;
				}
				return;
			}
			mAsrOption.mCallback.onVolume(mAsrOption, vol);
		}
		
		@Override
		public void onSuccess(CallBackOption oOption, VoiceParseData oVoiceParseData) {
			if (oOption.engineType == EngineType.ENGINE_LOCAL){
				mResults[0].mEngineType = EngineType.ENGINE_LOCAL;
				mResults[0].mScore = oVoiceParseData.floatResultScore;
				mResults[0].mVoiceData = oVoiceParseData;
				mResults[0].mRefeshTime = SystemClock.elapsedRealtime();
			}else if (oOption.engineType == EngineType.ENGINE_NET){
				mResults[1].mEngineType = EngineType.ENGINE_NET;
				mResults[1].mScore = 100;
				mResults[1].mVoiceData = oVoiceParseData;
				mResults[1].mRefeshTime = SystemClock.elapsedRealtime();
			}else{
				mResults[2].mEngineType = EngineType.ENGINE_MIX;
				mResults[2].mScore = 100;
				mResults[2].mVoiceData = oVoiceParseData;
				mResults[2].mRefeshTime = SystemClock.elapsedRealtime();
			}
			onResults();
		}
		
		@Override
		public void onStart(CallBackOption oOption) {
			if (!isStartRecording){
				mAsrOption.mCallback.onStart(mAsrOption);
				isStartRecording = true;
			}
		}
		
		@Override
		public void onError(CallBackOption oOption, int error2) {
			if (oOption.engineType == EngineType.ENGINE_LOCAL){
				mResults[0].mEngineType = EngineType.ENGINE_LOCAL;
				mResults[0].mErrorCode = error2;
				mResults[0].mRefeshTime = SystemClock.elapsedRealtime();
			}else if (oOption.engineType == EngineType.ENGINE_NET){
				mResults[1].mEngineType = EngineType.ENGINE_NET;
				mResults[1].mErrorCode = error2;
				mResults[1].mRefeshTime = SystemClock.elapsedRealtime();
			}else{
				mResults[2].mEngineType = EngineType.ENGINE_MIX;
				mResults[2].mErrorCode = error2;
				mResults[2].mRefeshTime = SystemClock.elapsedRealtime();
			}
			onResults();
		}
		
		@Override
		public void onEndOfSpeech(CallBackOption oOption) {
			mAsrOption.mCallback.onSpeechEnd(mAsrOption);
		}
		
		@Override
		public void onEnd(CallBackOption oOption) {
			if (oOption.engineType != EngineType.ENGINE_LOCAL){
				mAsrOption.mCallback.onEnd(mAsrOption);
			}
		}
		
		@Override
		public void onBeginOfSpeech(CallBackOption oOption) {
			mAsrOption.mCallback.onSpeechBegin(mAsrOption);
		}
	};
	
	private void onResults(){
		Result result = null;
		do {
			//MIX引擎方式
			if (mResults[2].mScore > ASR_THRESH){
				result = mResults[2];
				break;
			}
			
			if (mResults[2].mErrorCode != IAsr.ERROR_SUCCESS){
				result = mResults[2];
				break;
			}
			
			//未启用在线识别,直接使用离线识别的返回结果
			if (mResults[1].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				result = mResults[0];
				break;
			}
			
			//未启用离线线识别,直接使用在线识别的返回结果
			if (mResults[0].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				result = mResults[1];
				break;
			}
			
			if (mResults[1].mScore > ASR_THRESH) {
				result = mResults[1];
				break;
			}
			
			//在线识别提示没说话,信任度较高,可以直接采纳
			if (mResults[1].mErrorCode == IAsr.ERROR_NO_SPEECH) {
				result = mResults[1];
				break;
			}
			
			//离线识别提示没说话,信任度不够，需要结合在线结果
			if (mResults[0].mErrorCode == IAsr.ERROR_NO_SPEECH) {
				//在线比离线更早返回错误,直接使用离线识别结果
				if (mResults[1].mErrorCode != IAsr.ERROR_SUCCESS  
						&& mResults[0].mRefeshTime > mResults[1].mRefeshTime){
					result = mResults[0];
				}else{
					result = mResults[1];
				}
				break;
			}
			
			if (mResults[0].mErrorCode != IAsr.ERROR_SUCCESS){
				result = mResults[1];
				break;
			}
			
			if (mResults[0].mScore > ASR_THRESH) {
				result = mResults[0];
				break;
			}
			
			if (mResults[1].mErrorCode != IAsr.ERROR_SUCCESS){
				result = mResults[0];
				break;
			}
			
		} while (false);
		
		if (result != null && result.mEngineType != EngineType.ENGINE_NONE){
			onResult(result);
		}
	}
	
	private void onResult(Result result){
		JNIHelper.logd("engineType : " + result.mEngineType);
		do {
			
			if (result.mErrorCode != IAsr.ERROR_SUCCESS){
				mAsrOption.mCallback.onError(mAsrOption, 0, null, null, result.mErrorCode);
				break;
			}
			
			mAsrOption.mCallback.onSuccess(mAsrOption, result.mVoiceData);
		} while (false);
		end();
	}
	
	private IAsrCallBackProxy mAsrCallBackProxy = null;
	
	private void end(){
		isAsrWorking = false;
		if (mNetAsr != null){
			mNetAsr.cancel();
		}
		mLocalAsr.cancel();
		mResults = null;
	}
	
	private AsrOption cloneOption(AsrOption oOption, IAsrCallback callback){
		AsrOption option = new AsrOption();
		option.setAccent(oOption.mAccent);
		option.setBOS(oOption.mBOS);
		option.setEOS(oOption.mEOS);
		option.setGrammar(oOption.mGrammar);
		option.setKeySpeechTimeout(oOption.mKeySpeechTimeout);
		option.setLanguage(oOption.mLanguage);
		option.setManual(oOption.mManual);
		option.setNeedStopWakeup(oOption.mNeedStopWakeup);
		option.mAsrType = oOption.mAsrType;
		option.setCallback(callback != null ? callback : oOption.mCallback);
		option.mBeginSpeechTime = oOption.mBeginSpeechTime;
		option.mNeedStopWakeup = oOption.mNeedStopWakeup;
		option.mVoiceID = oOption.mVoiceID;
		option.mTtsId = oOption.mTtsId;
		return option;
	}

	@Override
	public int start(AsrOption oOption) {
		mResults = new Result[3];
		mResults[0] = new Result();
		mResults[1] = new Result();
		mResults[2] = new Result();
		
		mAsrType = oOption.mAsrType;
		mAsrOption = oOption;
		//生成识别ID
		if (oOption.mVoiceID == 0){
			mAsrOption.mVoiceID = System.currentTimeMillis();
		}
		isStartRecording = false;
		isAsrWorking = true;
		mAsrCallBackProxy.setAsrOption(mAsrOption);
		if (mAsrType == AsrType.ASR_AUTO) {
			if (NetworkManager.getInstance().hasNet()) {
				mAsrType = AsrType.ASR_ONLINE;
			}else{
				mAsrType = AsrType.ASR_LOCAL;
			}
		}
		JNIHelper.logd("AsrType origin : " + oOption.mAsrType.name() + " now : " + mAsrType.name());
		if (mLocalAsr.capacity() == 0x0011){
			mLocalAsrOption = cloneOption(oOption, mMixCallback);
			mLocalAsrOption.mAsrType = mAsrType;
			mLocalAsr.start(mLocalAsrOption);
			return 0;
		}
		
		if (mAsrType == AsrType.ASR_ONLINE){
			mResults[0].mEngineType = EngineType.ENGINE_LOCAL;
			mResults[0].mErrorCode = IAsr.ERROR_ASR_NO_USE;
			mResults[0].mRefeshTime = SystemClock.elapsedRealtime();
		}else if (mAsrType == AsrType.ASR_LOCAL){
			mResults[1].mEngineType = EngineType.ENGINE_NET;
			mResults[1].mErrorCode = IAsr.ERROR_ASR_NO_USE;
			mResults[1].mRefeshTime = SystemClock.elapsedRealtime();
		}
	
		if (mAsrType == AsrType.ASR_MIX
				|| mAsrType == AsrType.ASR_ONLINE) {
				mNetAsrOption = cloneOption(oOption, mNetCallback);
				mNetAsr.start(mNetAsrOption);
		}

		if (mAsrType == AsrType.ASR_MIX
				|| mAsrType == AsrType.ASR_LOCAL) {
			mLocalAsrOption = cloneOption(oOption, mLocalCallback);
			mLocalAsr.start(mLocalAsrOption);
		}
		return 0;
	}

	@Override
	public void stop() {
		mLocalAsr.stop();
		if (mNetAsr != null){
			mNetAsr.stop();
		}
	}

	@Override
	public void cancel() {
		end();
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		AppLogic.runOnBackGround(
				new Runnable2<SdkGrammar, IBuildGrammarCallback>(oGrammarData,
						oCallback) {
					@Override
					public void run() {
						if (mP2 != null) {
							mP2.onSuccess(mP1);
						}
					}
				}, 0);
		return true;
	}

	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		boolean bRet = false;
		if (mLocalAsr.capacity() == 0x0011){
			return mLocalAsr.importKeywords(oKeywords, oCallback);
		}
		
		if (oKeywords.strType.equals("contact") || oKeywords.strType.startsWith("~")){
			//在线词表
			bRet = mNetAsr.importKeywords(oKeywords, oCallback);
		}else{
			//本地词表
			bRet = mLocalAsr.importKeywords(oKeywords, oCallback);
		}
		return bRet;
	}

	@Override
	public void releaseBuildGrammarData() {
	}

	@Override
	public void retryImportOnlineKeywords() {
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RETRY_ONLINE_KEYWORDS);
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		
	}
}
