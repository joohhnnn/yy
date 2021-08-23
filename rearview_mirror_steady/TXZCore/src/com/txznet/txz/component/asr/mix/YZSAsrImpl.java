package com.txznet.txz.component.asr.mix;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.service.TXZContext;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource.Config;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.util.KeyWordFilter;
import com.txznet.txz.util.StringUtils;
import com.txznet.txz.util.VoiceGainHelper;
import com.txznet.txz.util.runnables.Runnable2;
import com.unisound.client.ErrorCode;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;

public class YZSAsrImpl extends SuperEngineBase {
	public static final String MONITOR_INFO = "asr.yzs.I.";
	public static final String MONITOR_ERROR = "asr.yzs.E.";
	public static final String MONITOR_WARNING = "asr.yzs.W.";
	
	private static class Result{
		public int mDataType = 0;
		public int mErrorCode = IAsr.ERROR_SUCCESS;
		public float mScore = -100;
		public String mStrResult;
	}

	//打字效果
	private StringBuffer mAsrResultBuffer = null;
	private StringBuffer mAsrChangeResultBuffer = null;
	private boolean mLastSplitedResult = false;
	
	private Config mConfig = null;
	private TXZAudioSource mAudioSource = null;
	private IAsrCallBackProxy mAsrCallBackProxy = null;
	private Result[] mResults = null;
	private SpeechUnderstander mSpeechUnderstander = null;
	private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {
		@Override
		public void onResult(int type, String jsonResult) {
			switch (type) {
			case SpeechConstants.ASR_RESULT_LOCAL:
				JNIHelper.logd("onFixResult =" + jsonResult);
				parseLocalResult(jsonResult);
				break;
			case SpeechConstants.ASR_RESULT_NET:
				LogUtil.logd("onNetResult =" + jsonResult);
				parseNetResult(jsonResult);
				break;
			case SpeechConstants.ASR_RESULT_RECOGNITION:
				JNIHelper.logd("onRecognition =" + jsonResult);
				parseRecoginitionResult(jsonResult);
				break;
			}
		}

		@Override
		public void onEvent(int type, int timeMs) {
			switch (type) {
			case SpeechConstants.ASR_EVENT_NET_END:
				JNIHelper.logd("ASR_EVENT_NET_END");
				break;
			case SpeechConstants.ASR_EVENT_LOCAL_END:
				JNIHelper.logd("ASR_EVENT_LOCAL_END");
				break;
			case SpeechConstants.ASR_EVENT_RECOGNITION_END:
				JNIHelper.logd("ASR_EVENT_RECOGNITION_END");
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_DETECTED:
				JNIHelper.logd("ASR_EVENT_SPEECH_DETECTED");
				onSpeech(true);
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_END:
				JNIHelper.logd("ASR_EVENT_SPEECH_END");
				onSpeech(false);
				break;
			case SpeechConstants.ASR_EVENT_COMPILE_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILE_DONE");
				break;
			case SpeechConstants.ASR_EVENT_LOADGRAMMAR_DONE:
				JNIHelper.logd("ASR_EVENT_LOADGRAMMAR_DONE");
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_START:
				JNIHelper.logd("ASR_EVENT_RECORDING_START");
				onStart();
				break;
			case SpeechConstants.ASR_EVENT_VAD_TIMEOUT:
				JNIHelper.logd("ASR_EVENT_VAD_TIMEOUT");
				onEnd();
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_STOP:
				JNIHelper.logd("ASR_EVENT_RECORDING_STOP");
				break;
			case SpeechConstants.ASR_EVENT_VOLUMECHANGE:
				Integer vol = (Integer) mSpeechUnderstander
						.getOption(SpeechConstants.GENERAL_UPDATE_VOLUME);
				if (null == vol) {
					return;
				}
				onVolume(vol);
				break;
			case SpeechConstants.ASR_EVENT_ENGINE_INIT_DONE:
				JNIHelper.logd("ASR_EVENT_ENGINE_INIT_DONE");
				onInit(true);
				break;
			case SpeechConstants.ASR_EVENT_USERDATA_UPLOADED:
				onImportKeywords(true, 0);
				break;
			}
		}

		@Override
		public void onError(int type, String errorMSG) {
			JNIHelper.loge("onError " + type + " " + errorMSG);
			int errorCode = parseErrorCode(errorMSG);
			parseError(errorCode);
		}
	};

	// {"errorCode":-91002,"errorMsg":"请求初始化错误"}
	private int parseErrorCode(String errorMsg) {
		JSONObject json = null;
		int errorCode = IAsr.ERROR_SUCCESS;
		try {
			json = new JSONObject(errorMsg);
			errorCode = json.getInt("errorCode");
			switch (errorCode) {
			case ErrorCode.ASR_SDK_FIX_RECOGNIZER_NO_INIT:
			case ErrorCode.GENERAL_INIT_ERROR:
			case ErrorCode.FAILED_START_RECORDING:
			case ErrorCode.RECORDING_EXCEPTION:
				errorCode = IAsr.ERROR_CODE;
				break;
			case ErrorCode.UPLOAD_USER_DATA_TOO_FAST:
				JNIHelper.logd("upload_user_data_too_fast");
				onImportKeywords(false, IImportKeywordsCallback.ERROR_UPLOAD_TOO_FAST);
				AppLogic.removeBackGroundCallback(mRetryImportOnlineKeywordsTask);
				AppLogic.runOnBackGround(mRetryImportOnlineKeywordsTask, 10*60*1000);//上传过于频繁时,十分钟后重试
				break;
			case ErrorCode.UPLOAD_USER_DATA_EMPTY:
			case ErrorCode.UPLOAD_USER_DATA_NETWORK_ERROR:
			case ErrorCode.UPLOAD_USER_DATA_SERVER_REFUSED:
			case ErrorCode.UPLOAD_USER_TOO_LARGE:
			case ErrorCode.UPLOAD_USER_ENCODE_ERROR:
				onImportKeywords(false, -1);
				break;
			case -91155://{"errorCode":-91155,"errorMsg":"请求时间过长"}
			case -91007://{"errorCode":-91007,"errorMsg":"其他异常"}
			case -90005://{"errorCode":-90005,"errorMsg":"其他异常"}
			case -62002://{"errorCode":-62002,"errorMsg":"服务器通讯错误"}
			case -90002://{"errorCode":-90002,"errorMsg":"模型加载失败！"}
			case -91883:
			case ErrorCode.SEND_REQUEST_ERROR:
			case ErrorCode.REQ_INIT_ERROR:
				errorCode = IAsr.ERROR_ASR_NET_REQUEST;
				monitor(MONITOR_ERROR + MONITOR_NO_REQUEST);
				break;
			default:
				JNIHelper.logd("other errorCode : " + errorCode);
				monitor(MONITOR_ERROR + errorCode);
				break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return errorCode;
	}
	
	private Runnable mRetryImportOnlineKeywordsTask = new Runnable() {
		@Override
		public void run() {
			JNIHelper.logd("retryImportOnlineKeywords while too fast timeout");
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RETRY_ONLINE_KEYWORDS);
		}
	};
	
	private void onImportKeywords(boolean bSuccessed, int code){
		LogUtil.logd("onImportKeywords bSuccessed : " + bSuccessed + ", code : " + code);
		final IImportKeywordsCallback oCallback = mSetNetDataCallback;
		mSetNetDataCallback = null;
		if (oCallback != null){
			if (bSuccessed){
				oCallback.onSuccess(mSetNetDataSdkKeywords);
			}else{
				oCallback.onError(code, mSetNetDataSdkKeywords);
			}
		}else{
			LogUtil.loge("onImportKeywords oCallback = null");
		}
	}
	

	private IInitCallback mInitCallback = null;
	private Context mContext = null;
	@Override
	public int initialize(IInitCallback oRun) {
		mAsrCallBackProxy = AsrCallbackFactory.proxy();
		mInitCallback = oRun;
		mContext = new TXZContext(GlobalContext.get(), ProjectCfg.getYzsFileDir());
		mSpeechUnderstander = new SpeechUnderstander(mContext,
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_MIX);
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		//默认关闭引擎LOG
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_DEBUG_SAVELOG, false);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_TEMP_RESULT_ENABLE, true);//设置可变结果
		JNIHelper.logd("init asr vesioninfo ->SDK=" + mSpeechUnderstander.getVersion());
		JNIHelper.logd("init asr vesioninfo ->ENGINE =" + mSpeechUnderstander.getFixEngineVersion());
		JNIHelper.logd("init asr begin");
		int nRet = mSpeechUnderstander.init("");
		JNIHelper.logd("init asr end = " + nRet);
		if (nRet < 0) {
			onInit(false);
		}
		return 0;
	}
	
	private boolean mInitSuccessed = false; 
	private void onInit(final boolean bSuccessed) {
		mInitSuccessed = bSuccessed;
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (bSuccessed) {
					doInit();
				} else {
					if (mInitCallback != null) {
						mInitCallback.onInit(bSuccessed);
					}
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void doInit() {
		mConfig = new Config();
		mConfig.setSkipBytes(6400);
		boolean bUsePreProcessedData = ProjectCfg.mEnableAEC && ProjectCfg.isUseSePreprocessedData();
		LogUtil.logd("asr:bUsePreProcessedData:" + bUsePreProcessedData);
		mAudioSource = new TXZAudioSource(mConfig, bUsePreProcessedData);
		mSpeechUnderstander.setAudioSource(mAudioSource);
		mSpeechUnderstander.loadCompiledJsgf("txzTag", GlobalContext.get()
				.getApplicationInfo().dataDir + "/data/txz.dat");// 加载编译语法
		boolean bRet = loadLastGrammar();
		int delayTime = 0;
		if (bRet) {
			delayTime = 200;
		}
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mInitCallback != null) {
					mInitCallback.onInit(true);
				}
			}
		};

		mAsrResultBuffer = new StringBuffer();
		mAsrChangeResultBuffer = new StringBuffer();
		AppLogic.runOnBackGround(oRun, delayTime);
	}

	/*
	 * load上次grammar
	 */
	private boolean loadLastGrammar() {
		if (mContext == null){
			mContext = new TXZContext(GlobalContext.get(), ProjectCfg.getYzsFileDir());
			JNIHelper.logw("TXZContext create");
		}
		
		String grammarPath = mContext.getFilesDir().getPath() + "/YunZhiSheng/asrfix/jsgf_model/txzTag.dat";// 加载上一次生成的模型
		File file = new File(grammarPath);
		if (file.exists()) {
			JNIHelper.logd("load grammarPath = " + grammarPath);
			mSpeechUnderstander.loadGrammar("txzTag", grammarPath);
			return true;
		}
		return false;
	}

	@Override
	public void release() {
		if(mSpeechUnderstander != null){
			int ret = 0;
			if (mInitSuccessed){//初始化成功才release
				ret = mSpeechUnderstander.release(SpeechConstants.ASR_RELEASE_ENGINE, "");
			}
			mSpeechUnderstander = null;
			JNIHelper.logd("LocalYunzhisheng release ret = "+ret + " mSuccessed = " + mInitSuccessed);
		}
	}
	
	private AsrOption mAsrOption = null;

	@Override
	public synchronized int start(AsrOption oOption) {
		mResults = new Result[2];
		mResults[0] = new Result();//缓存离线识别结果
		mResults[1] = new Result();//缓存在线识别结果
		mAsrOption = oOption;
		mAsrCallBackProxy.setAsrOption(mAsrOption);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_VAD_TIMEOUT_FRONTSIL, mAsrOption.mBOS);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, mAsrOption.mEOS);
		String grammar = "txzTag";

		// 判断是否是免唤醒识别
		mConfig.setBeginSpeechTime(oOption.mBeginSpeechTime);
		mHasSpeaking = false;
		
		String strSaveDataPath = null;
		if (oOption.mVoiceID != 0){
			strSaveDataPath = oOption.mVoiceID+"";
		}
		mConfig.setmUID(oOption.mUID);
		mConfig.setmServerTime(oOption.mServerTime);
		mConfig.setbServerTimeConfidence(oOption.bServerTimeConfidence);
		mConfig.setSaveDataPath(strSaveDataPath);
		
		//OneShot离线识别使用
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, true); //先打开离线结果转json开关 
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_IGNORE_RESULT_TAG, "unk");
		
		//不播放beep音的时候补跳过6400字节
		if (mAsrOption.mPlayBeepSound) {
			mConfig.setSkipBytes(6400);
		}else {
			mConfig.setSkipBytes(0);
		}

		//打字效果使用的字符串
		mAsrResultBuffer.delete(0,mAsrResultBuffer.length());
		mAsrChangeResultBuffer.delete(0,mAsrChangeResultBuffer.length());
		mLastSplitedResult = false;
		
		int nAsrSvrMode = SpeechConstants.ASR_SERVICE_MODE_MIX;
		if (mAsrOption.mAsrType == AsrType.ASR_ONLINE){
			nAsrSvrMode = SpeechConstants.ASR_SERVICE_MODE_NET;
			mResults[0].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON;
			mResults[0].mErrorCode = IAsr.ERROR_ASR_NO_USE;
		}else if (mAsrOption.mAsrType == AsrType.ASR_LOCAL){
			nAsrSvrMode = SpeechConstants.ASR_SERVICE_MODE_LOCAL;
			mResults[1].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON;
			mResults[1].mErrorCode = IAsr.ERROR_ASR_NO_USE;
		}
		setParam();
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE, nAsrSvrMode);
		mSpeechUnderstander.init("");
		
		//调整塞给引擎的声音的增益
		if (DebugCfg.SAVE_RAW_PCM_CACHE && VoiceGainHelper.enable()){
			mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA, 
					"/sdcard/txz/voice/" +  "yzs_" + System.currentTimeMillis() + "_" + VoiceGainHelper.getRate() + ".pcm");
		}
		mSpeechUnderstander.start(grammar);
		monitor(MONITOR_INFO + MONITOR_ALL);
		return 0;
	}
	
	private static final int NET_TIMEOUT = 10000;//在线识别网络超时时间,单位毫秒
	private void setParam() {
		mSpeechUnderstander.setOption(SpeechConstants.ASR_NET_TIMEOUT, NET_TIMEOUT);
		String strCity = getCurrentCity();
		String strGpsInfo = getGpsInfo();
		JNIHelper.logd("gpsinfo : (" + strGpsInfo + "), currCity : " + strCity);
		mSpeechUnderstander.setOption(SpeechConstants.GENERAL_CITY, strCity);
		mSpeechUnderstander.setOption(SpeechConstants.GENERAL_GPS,  strGpsInfo);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_DOMAIN, "poi,song,incar");
		mSpeechUnderstander.setOption(SpeechConstants.NLU_SCENARIO, "incar");

	}
	
	@Override
	public synchronized void stop() {
		mSpeechUnderstander.stop();
	}

	@Override
	public synchronized void cancel() {
		mSpeechUnderstander.cancel();
		end();
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		return false;
	}

	@Override
	public void releaseBuildGrammarData() {

	}

	@Override
	public void retryImportOnlineKeywords() {

	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {

	}
	
	public static final double DOUBLE = 0.00000001;
	private boolean mHasSpeaking = false;

	private void onSpeech(final boolean bSpeaking) {
		JNIHelper.logd("bSpeaking : " + bSpeaking);
		if (bSpeaking) {
			mHasSpeaking = true;
			mAsrCallBackProxy.onBeginOfSpeech();
		} else {
			mAsrCallBackProxy.onEndOfSpeech();
		}
	}
	
	private void onVolume(final int vol) {
		mAsrCallBackProxy.onVolume(vol);
	}

	private void onStart() {
		mAsrCallBackProxy.onStart();
	}

	private void onEnd() {
		mAsrCallBackProxy.onEnd();
	}
	
	private synchronized void onResult(int errorCode, int dataType) {
		if (mResults == null){
			return;
		}
		if (dataType == VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON){
			mResults[0].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON;
			mResults[0].mErrorCode = errorCode;
		}else if (dataType == VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON){
			mResults[1].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON;
			mResults[1].mErrorCode = errorCode;
		}
		onResults();
	}
	
	private synchronized void onResult(final float score, final String jsonResult, int dataType) {
		if (mResults == null){
			return;
		}
		if (dataType == VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON){
			mResults[0].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON;
			mResults[0].mStrResult = jsonResult;
			mResults[0].mScore = score;
		}else if (dataType == VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON){
			mResults[1].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON;
			mResults[1].mStrResult = jsonResult;
			mResults[1].mScore = score;
		}else if (dataType == -1){
			//离、在线识别都结束后执行
			if (mResults[1].mDataType == 0){//说明之前既没有收到在线识别错误码,又没有收到过在线识别结果，所以可以直接认为失败了
				mResults[1].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON;
				mResults[1].mErrorCode = IAsr.ERROR_ASR_NET_REQUEST;
			}
			
			if (mResults[0].mDataType == 0){//说明之前既没有收到离线识别错误码,又没有收到过离线识别结果，所以可以直接认为失败了
				mResults[0].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON;
				mResults[0].mErrorCode = IAsr.ERROR_CODE;
			}
		}
		onResults();
	}
	
	private void onResults(){
		Result result = null;
		do {
			//未开启离线识别
			if (mResults[0].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				result = mResults[1];
				break;
			}
			
			//未开启在线识别
			if (mResults[1].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				result = mResults[0];
				break;
			}
			
			if (mResults[1].mScore > ASR_THRESH) {
				result = mResults[1];
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
		
		if (result != null && result.mDataType != 0){
			onResult(result);
		}
	}
	
	private void onResult(Result result){
		// 没有说话判断
		if (result.mScore > -20 - DOUBLE && result.mScore < -20 + DOUBLE) {
			int error2 = mHasSpeaking ? IAsr.ERROR_NO_MATCH : IAsr.ERROR_NO_SPEECH;
			result.mErrorCode = error2;
			//离线结果为空时,并且用户有说话的时候, 并且网络有连接, 需要使用在线识别错误码
			if (result.mErrorCode == IAsr.ERROR_NO_MATCH  && NetworkManager.getInstance().hasNet()){
				int netError = mResults[1].mErrorCode;
				switch(netError){
				case IAsr.ERROR_SUCCESS:
				case IAsr.ERROR_ASR_NO_USE:
					break;
				default:
					result.mErrorCode = netError;
					JNIHelper.logw("onResult use net error if local result is no match");
				}
			}
			
		}
		
		do {
			
			if (result.mErrorCode != IAsr.ERROR_SUCCESS){
				mAsrCallBackProxy.onError(result.mErrorCode);
				break;
			}

			VoiceParseData oVoiceParseData = new VoiceParseData();
			oVoiceParseData.floatResultScore = result.mScore;
			oVoiceParseData.strVoiceData = result.mStrResult;
			oVoiceParseData.uint32DataType = result.mDataType;
			AsrOption oOption = mAsrOption;
			if (oOption != null) {
				if (oOption.mManual) {
					oVoiceParseData.boolManual = 1;
				} else {
					oVoiceParseData.boolManual = 0;
				}
				oVoiceParseData.uint32Sence = oOption.mGrammar;
			}
			
			String strExtraData = genExtraData(oVoiceParseData);
			if (!TextUtils.isEmpty(strExtraData)){
				oVoiceParseData.strExtraData = strExtraData.getBytes();
			}
			
			mAsrCallBackProxy.onSuccess(oVoiceParseData);
		} while (false);
		
		mSpeechUnderstander.cancel();
		end();
	}
	
	private String genExtraData(VoiceParseData oVoiceData){
		String strExtraData = "";
		JSONObject jsonObj = new JSONObject();
		try {
			Result result = mResults[1];
			if (result != null && result.mDataType != 0
					&& result.mErrorCode != IAsr.ERROR_SUCCESS) {
				jsonObj.put("netErrorCode", result.mErrorCode);
			}
		} catch (Exception e) {
		}
		
		try {
			strExtraData = jsonObj.toString();
		} catch (Exception e) {

		}
		
		return strExtraData;
	}
	
	private void end(){
		mResults = null;
		if (mAsrResultBuffer != null)
			mAsrResultBuffer.delete(0,mAsrResultBuffer.length());
		if (mAsrChangeResultBuffer != null)
			mAsrChangeResultBuffer.delete(0,mAsrChangeResultBuffer.length());
	}
	
	private void parseRecoginitionResult(String jsonResult){
		onResult(0, jsonResult, -1);
	}
	
	private void parseError(int errorCode){
		if (errorCode == IAsr.ERROR_ASR_NET_REQUEST){
			onResult(errorCode, VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON);
		}else if (errorCode == IAsr.ERROR_CODE){
			onResult(errorCode, VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON);
		}
	}
	
	private void parseNetResult(String jsonResult) {
		if (jsonResult != null) {
			if (jsonResult.contains("net_asr")
					&& jsonResult.contains("net_nlu")) {
				try {

					JSONObject json = null;
					JSONArray jsonArray = null;
					JSONObject jsonObject = null;
					do {
						json = new JSONObject(jsonResult);
						jsonArray = json.getJSONArray("net_nlu");
						if (null == jsonArray) {
							break;
						}
						jsonObject = jsonArray.getJSONObject(0);
						if (null == jsonObject) {
							break;
						}
						if (ProjectCfg.enableTypingEffect()) {
							//在识别结束后发一次识别结果通知过去，解决最后部分结果错误的问题，eg:今天的天气是多云
							String result = jsonObject.getString("text");
							if (!TextUtils.isEmpty(result)) {
								mAsrCallBackProxy.onPartialResult(result);
							}
						}

						onResult(100, jsonObject.toString(), VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON);
						return;
					} while (false);
					JNIHelper.loge("asr nlu result data is incomplete : " + jsonResult);
					mAsrCallBackProxy.onError(IAsr.ERROR_ASR_NET_NLU_EMTPY);
				} catch (Exception e) {
					JNIHelper.loge("asr nlu result data is incomplete : " + jsonResult);
					mAsrCallBackProxy.onError(IAsr.ERROR_ASR_NET_NLU_EMTPY);
				}
			} else if (jsonResult.contains("net_asr")) {
				if (ProjectCfg.enableTypingEffect()) {
					try {

						JSONObject json = null;
						JSONArray jsonArray = null;
						JSONObject jsonObject = null;
						json = new JSONObject(jsonResult);
						jsonArray = json.getJSONArray("net_asr");
						if (jsonArray == null || jsonArray.length() < 1) {
							return;
						}
						jsonObject = jsonArray.getJSONObject(0);
						if (jsonObject == null) {
							return;
						}
						String resultType = jsonObject.getString("result_type");
						if (TextUtils.equals(resultType, "change")) {
							String strResult = jsonObject.getString("recognition_result");
							if (!TextUtils.isEmpty(strResult)) {
								String strSimpleResult = StringUtils.filterSpecialChar(strResult);
								mAsrChangeResultBuffer.delete(0, mAsrChangeResultBuffer.length());
								mAsrChangeResultBuffer.append(mAsrResultBuffer.toString());
								mAsrChangeResultBuffer.append(strSimpleResult);
								mAsrCallBackProxy.onPartialResult(mAsrChangeResultBuffer.toString());
								return;
							}
						} else if (TextUtils.equals(resultType, "partial")) {
							String strResult = jsonObject.getString("recognition_result");
							if (!TextUtils.isEmpty(strResult)) {
								String strSimpleResult = StringUtils.filterSpecialChar(strResult);
								mAsrResultBuffer.append(strSimpleResult);
							}
						}


					} catch (Exception e) {
					}
				}
			}
		}
	}
	
	private void parseLocalResult(String jsonResult) {
		if (jsonResult != null) {
			if (jsonResult.contains("local_asr")) {
				try {

					JSONObject json = null;
					JSONArray jsonArray = null;
					JSONObject jsonObject = null;
					do {
						json = new JSONObject(jsonResult);
						jsonArray = json.getJSONArray("local_asr");
						if (null == jsonArray) {
							break;
						}
						jsonObject = jsonArray.getJSONObject(0);
						if (null == jsonObject) {
							break;
						}
						String status = jsonObject.getString("result_type");
						if (!status.equals("full")) {
							JNIHelper.loge("result_type != full");
							break;
						}

						float score = (float) getDouble(jsonObject, "score");
						String result = jsonObject
								.getString("recognition_result");
						onResult(score, result, VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON);
					} while (false);

				} catch (JSONException e) {

				}
			}
		}
	}

	private double getDouble(JSONObject json, String name) {
		double value = -100.0;
		if (json == null || name == null) {
			JNIHelper.loge("null");
			return value;
		}
		do {
			try {
				value = json.getDouble(name);
				break;
			} catch (JSONException e) {

			}

			try {
				value = json.getLong(name);
				break;
			} catch (JSONException e) {

			}

			try {
				value = json.getInt(name);
				break;
			} catch (JSONException e) {

			}
			JNIHelper.loge("no value named [name] in json");
		} while (false);
		return value;
	}

	SdkKeywords mSetNetDataSdkKeywords;
	IImportKeywordsCallback mSetNetDataCallback;
	SdkKeywords mSetDataSdkKeywords;
	IImportKeywordsCallback mSetDataCallback;

	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		AsrWakeupEngine.getEngine().runOnCompileBackGround(
				new Runnable2<SdkKeywords, IImportKeywordsCallback>(oKeywords,
						oCallback) {
					@Override
					public void run() {
						if (mSpeechUnderstander == null) {
							JNIHelper.logw("mMixSpeechUnderstander == null");
							if (mP2 != null) {
								mP2.onSuccess(mP1);
							}
							return;
						}

						SdkKeywords oKeywords = mP1;
						if (oKeywords.strType.equals("contact")) {
							List<String> lstData = new ArrayList<String>();
							String[] lst = oKeywords.strContent.split("\n");
							for (int i = 0; i < lst.length; ++i) {
								if (lst[i].isEmpty()) {
									continue;
								}
								lstData.add(lst[i]);
							}
							JNIHelper.logd("importKeywordsContact: session_id="
									+ oKeywords.uint32SessionId);
							Map<Integer, List<String>> mapData = new HashMap<Integer, List<String>>();
							mapData.put(SpeechConstants.UPLOAD_DATA_NAME,
									lstData);
							mSetNetDataSdkKeywords = oKeywords;
							mSetNetDataCallback = mP2;
							mSpeechUnderstander.uploadUserData(mapData);
						} else {
							// 如果强制使用在线识别, 不能插离线命令
							if (ProjectCfg.RecognOnline()) {
								JNIHelper.logw("ProjectCfg.RecognOnline() = "
										+ ProjectCfg.RecognOnline());
								if (mP2 != null) {
									mP2.onSuccess(mP1);
								}
								return;
							}
							int error = 0;
							if (oKeywords.strType.startsWith("<")/*
																 * && oKeywords.
																 * msgGrammarInfo
																 * .strId
																 * .endsWith
																 * ("0")
																 */) {
								String key = oKeywords.strType.substring(1,
										oKeywords.strType.length() - 1);
								JNIHelper.logd("importKeywordsKey = " + key
										+ ", session_id="
										+ oKeywords.uint32SessionId);
								do {
									// 过滤引擎不支持的tag
									if (key.contains("callPrefix")
											|| key.contains("callSuffix")) {
										JNIHelper.logd("filter unsupport slot");
										break;
									}
									
									String[] aContents = null;
									if (oKeywords.rptStrKw != null){
										aContents = oKeywords.rptStrKw;
									}else{
										String strContents = oKeywords.strContent;
										if (strContents == null
												|| strContents.isEmpty()) {
											break;
										}
										aContents = strContents
											.split("\n");
									}
									
									if (aContents == null
											|| aContents.length == 0) {
										break;
									}

									List<String> KeyNamesList = new ArrayList<String>();
									KeyNamesList.clear();

									for (int i = 0; i < aContents.length; i++) {
										String content = aContents[i];
										if (content == null
												|| content.trim().isEmpty()
												|| KeyWordFilter.hasIgnoredChar(content.trim())) {
											JNIHelper.logd("importKeyword ignore the special keyword : " + content);
											continue;
										}
										KeyNamesList.add(content);
									}

									if (KeyNamesList.isEmpty()) {
										break;
									}
									JNIHelper.logd("insertVocab " + key);
									int nRet = 0;
									mSetDataSdkKeywords = oKeywords;
									mSetDataCallback = mP2;

									nRet = mSpeechUnderstander.insertVocab(
											KeyNamesList, "txzTag#" + key);
									JNIHelper.logd("insertVocab[" + key
											+ "] : nRet = " + nRet);
									error = nRet;
								} while (false);

								if (error != 0) {
									if (mP2 != null) {
										mP2.onError(error, mP1);
									}
								} else {
									if (mP2 != null) {
										mP2.onSuccess(mP1);
									}
								}
							} else if (oKeywords.strType.startsWith("~")) {
								String key = oKeywords.strType.substring(1,
										oKeywords.strType.length());
								List<String> lstData = new ArrayList<String>();
								String[] lst = oKeywords.strContent.split("\n");
								for (int i = 0; i < lst.length; ++i) {
									if (lst[i].isEmpty()) {
										continue;
									}
									lstData.add(lst[i]);
								}
								JNIHelper.logd("importKeywordsOnlineUserData ="
										+ key + ", session_id="
										+ oKeywords.uint32SessionId);
								Map<Integer, List<String>> mapData = new HashMap<Integer, List<String>>();
								mapData.put(SpeechConstants.UPLOAD_DATA_APP,
										lstData);
								mSetNetDataSdkKeywords = oKeywords;
								mSetNetDataCallback = mP2;
								mSpeechUnderstander.uploadUserData(mapData);
							} else {
								/*
								 * 讯飞的词库
								 */
								if (mP2 != null) {
									mP2.onSuccess(mP1);
								}
							}
						}
					}
				}, 0);

		return true;
	}
	
	private void monitor(String strAttr){
		if (NetworkManager.getInstance().hasNet()){
			MonitorUtil.monitorCumulant(strAttr);
		}
	}
}