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
			case SpeechConstants.ASR_EVENT_ACTIVATE_SUCCESS:
				JNIHelper.logd("ASR_EVENT_ACTIVATE_SUCCESS");
				onActivate(ActivateStatus.ACTIVATE_OK);
				break;
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
			case SpeechConstants.ASR_EVENT_COMPILER_INIT_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILER_INIT_DONE");
				bCompilerInited = true;
				break;
			case SpeechConstants.ASR_EVENT_COMPILE_VOCAB_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILE_DONE");
				onImportFixKeywords(true, 0);
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
				mSpeechUnderstander.stop();//72??????, ????????????stop??????????????????
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
			case SpeechConstants.ASR_EVENT_INIT_DONE:
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

	// {"errorCode":-91002,"errorMsg":"?????????????????????"}
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
			case ErrorCode.ASR_SDK_FIX_COMPILE_ERROR:
				JNIHelper.loge("ASR_SDK_FIX_COMPILE_ERROR");
				onImportFixKeywords(false, errorCode);
				break;
			case ErrorCode.UPLOAD_USER_DATA_TOO_FAST:
				JNIHelper.logd("upload_user_data_too_fast");
				onImportKeywords(false, IImportKeywordsCallback.ERROR_UPLOAD_TOO_FAST);
				AppLogic.removeBackGroundCallback(mRetryImportOnlineKeywordsTask);
				AppLogic.runOnBackGround(mRetryImportOnlineKeywordsTask, 10*60*1000);//?????????????????????,??????????????????
				break;
			case ErrorCode.UPLOAD_USER_DATA_EMPTY:
			case ErrorCode.UPLOAD_USER_DATA_NETWORK_ERROR:
			case ErrorCode.UPLOAD_USER_DATA_SERVER_REFUSED:
			case ErrorCode.UPLOAD_USER_TOO_LARGE:
			case ErrorCode.UPLOAD_USER_ENCODE_ERROR:
				onImportKeywords(false, -1);
				break;
			case -91155://{"errorCode":-91155,"errorMsg":"??????????????????"}
			case -91007://{"errorCode":-91007,"errorMsg":"????????????"}
			case -90005://{"errorCode":-90005,"errorMsg":"????????????"}
			case -62002://{"errorCode":-62002,"errorMsg":"?????????????????????"}
			case -90002://{"errorCode":-90002,"errorMsg":"?????????????????????"}
			case -91883:
			case ErrorCode.SEND_REQUEST_ERROR:
			case ErrorCode.REQ_INIT_ERROR:
				errorCode = IAsr.ERROR_ASR_NET_REQUEST;
				monitor(MONITOR_ERROR + MONITOR_NO_REQUEST);
				break;
			case ErrorCode.ASR_SDK_ACTIVATE_ERROR:
				onActivate(ActivateStatus.ACTIVATE_FAIL);
				monitor(MONITOR_ERROR + errorCode);
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
	
	private static enum ActivateStatus{
		ACTIVATE_IDLE,
		ACTIVATE_FAIL,
		ACTIVATE_OK
	}
	
	private ActivateStatus mActivationStatus = ActivateStatus.ACTIVATE_IDLE;
	private final static String LOCAL_ACTIVATE_DEVSN = "000000";
	private boolean bReActivateDone = false;
	private String mInitDevSn = "";
	private final static int ACTIVATE_DELAY_TIME = 5000;
	private final static int ACTIVATE_MAX_RETRY_CNT = 20;
	
	private synchronized void onActivate(ActivateStatus status){
		LogUtil.logd("onActivate:" + status);
		mActivationStatus = status;
		
		if (status == ActivateStatus.ACTIVATE_FAIL){
			reDoActivate();	
		}
	}
	
	private void reDoActivate(){
		if (!TextUtils.equals(LOCAL_ACTIVATE_DEVSN, mInitDevSn)){
			return;
		}
		
		if (mActivationStatus != ActivateStatus.ACTIVATE_FAIL) {
			return;
		}
		
		if (bReActivateDone){
			return;
		}
		
		LogUtil.loge("reDoActivate");
		bReActivateDone = true;
		
		AppLogic.runOnBackGround(new Runnable() {
			int cnt = 0;
			@Override
			public void run() {
				AppLogic.removeBackGroundCallback(this);
				
				cnt++;
				do {
					String strDevSn = getDeviceSn();
					if (TextUtils.equals(LOCAL_ACTIVATE_DEVSN, strDevSn)) {// ????????????v3??????????????????????????????:000000
						LogUtil.loge("invalid activator:" + strDevSn);
						break;
					}
					if (TextUtils.isEmpty(strDevSn)) {
						LogUtil.loge("empty activator:" + strDevSn);
						break;
					}

					JSONObject json = new JSONObject();
					try {
						json.put("activate", "" + true);
						json.put("deviceSn", "" + strDevSn);
					} catch (Exception e) {
						LogUtil.loge("activator exception : " + e.toString());
						break;
					}
					
					LogUtil.loge("reDoActivate:" + json.toString());
					LogUtil.logw("reinit for activation");
					monitor(MONITOR_INFO + "re_activate");
					mSpeechUnderstander.init(json.toString());
					return;
				} while (false);
				
				if (cnt < ACTIVATE_MAX_RETRY_CNT + 1){
					AppLogic.runOnBackGround(this, ACTIVATE_DELAY_TIME);
				}
			}
		}, ACTIVATE_DELAY_TIME);
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
	
	private void onImportFixKeywords(boolean bSuccessed, int code){
		LogUtil.logd("onImportFixKeywords bSuccessed : " + bSuccessed + ", code : " + code);
		bCompiling = false;//????????????????????????
		
		/**********??????????????????DestroyCompiler??????????????????????????????********************/
		destroyCompiler();
		/******** **?????????????????????initCompiler???remove????????????DestroyCompiler??????****/
		
		final IImportKeywordsCallback oCallback = mSetDataCallback;
		mSetDataCallback = null;
		if (oCallback != null){
			if (bSuccessed){
				oCallback.onSuccess(mSetDataSdkKeywords);
			}else{
				LogUtil.loge("onImportFixKeywords_error : " + mSpeechUnderstander.getOption(SpeechConstants.ASR_OPT_GET_COMPILE_ERRORINFO));
				oCallback.onError(code, mSetDataSdkKeywords);
			}
		}else{
			LogUtil.loge("onImportFixKeywords oCallback = null");
		}
	}
	

	private IInitCallback mInitCallback = null;
	private Context mContext = null;
	@Override
	public int initialize(IInitCallback oRun) {
		mAsrCallBackProxy = AsrCallbackFactory.proxy();
		mInitCallback = oRun;
		mContext = new TXZContext(GlobalContext.get(), ProjectCfg.getYzsFileDir());
		final String strAppId = ProjectCfg.getYunzhishengAppId();
		final String strSecret = ProjectCfg.getYunzhishengSecret();
		com.unisound.common.aa.setAppKey(strAppId);
		mSpeechUnderstander = new SpeechUnderstander(mContext,strAppId, strSecret);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_INIT_MODE,
				SpeechConstants.ASR_INIT_MODE_MIX);//???????????????SDK,????????????ASR_INIT_MODE??????
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		//??????????????????LOG
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_ADVANCE_INIT_COMPILER, false);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_DEBUG_SAVELOG, false);
		JNIHelper.logd("init asr vesioninfo ->SDK=" + mSpeechUnderstander.getVersion());
		JNIHelper.logd("init asr vesioninfo ->ENGINE =" + mSpeechUnderstander.getFixEngineVersion());
		JNIHelper.logd("init asr begin");
		JSONObject json = new JSONObject();
		try {
			json.put("activate", "" + true);
			String strDevSn = getDeviceSn();
			json.put("deviceSn", "" + strDevSn);
			mInitDevSn = strDevSn;//??????????????????
		} catch (Exception e) {
			LogUtil.loge("activator exception : " + e.toString());
		}
		//LogUtil.logd("activator : " + json.toString());//?????????????????????????????????, ?????????????????????
		int nRet = 0;
		mSpeechUnderstander.init(json.toString());
		JNIHelper.logd("init asr end = " + nRet);
		if (nRet < 0) {
			onInit(false);
		}
		return 0;
	}
	
	private String getDeviceSn(){
		byte[] data = ProjectCfg.getYzsActivator();
		if (data == null){
			return "";
		}
		
		String strDeviceSn = "";
		try{
			strDeviceSn = new String(data);
		}catch(Exception e){
			
		}
		return strDeviceSn;
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
		mAudioSource = new TXZAudioSource(mConfig, ProjectCfg.mEnableAEC);
		mSpeechUnderstander.setAudioSource(mAudioSource);
//		mSpeechUnderstander.loadCompiledJsgf("txzTag", GlobalContext.get()
//				.getApplicationInfo().dataDir + "/data/txz.dat");// ??????????????????
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
		AppLogic.runOnBackGround(oRun, delayTime);
	}

	/*
	 * load??????grammar
	 */
	private boolean loadLastGrammar() {
		if (mContext == null){
			mContext = new TXZContext(GlobalContext.get(), ProjectCfg.getYzsFileDir());
			JNIHelper.logw("TXZContext create");
		}
		
		String grammarPath = mContext.getFilesDir().getPath() + "/YunZhiSheng/asrfix/jsgf_model/txzTag.dat";// ??????????????????????????????
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
			if (mInitSuccessed){//??????????????????release
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
		mResults[0] = new Result();//????????????????????????
		mResults[1] = new Result();//????????????????????????
		mAsrOption = oOption;
		mAsrCallBackProxy.setAsrOption(mAsrOption);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_VAD_TIMEOUT_FRONTSIL, mAsrOption.mBOS);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, mAsrOption.mEOS);
		String grammar = "txzTag";

		// ??????????????????????????????
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
		
		//OneShot??????????????????
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, true); //????????????????????????json?????? 
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_IGNORE_RESULT_TAG, "unk");
		
		//?????????beep?????????????????????6400??????
		if (mAsrOption.mPlayBeepSound) {
			mConfig.setSkipBytes(6400);
		}else {
			mConfig.setSkipBytes(0);
		}
		
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
		//mSpeechUnderstander.init("");//???????????????SDK,????????????ASR_INIT_MODE??????,????????????init
		//????????????????????????????????????
		if (DebugCfg.SAVE_RAW_PCM_CACHE && VoiceGainHelper.enable()){
			mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA, 
					"/sdcard/txz/voice/" +  "yzs_" + System.currentTimeMillis() + "_" + VoiceGainHelper.getRate() + ".pcm");
		}
		mSpeechUnderstander.start(grammar);
		monitor(MONITOR_INFO + MONITOR_ALL);
		return 0;
	}
	
	private static final int NET_TIMEOUT = 10000;//??????????????????????????????,????????????
	private void setParam() {
		mSpeechUnderstander.setOption(SpeechConstants.ASR_NET_TIMEOUT, NET_TIMEOUT);
		String strCity = getCurrentCity();
		String strGpsInfo = getGpsInfo();
		JNIHelper.logd("gpsinfo : (" + strGpsInfo + "), currCity : " + strCity);
		mSpeechUnderstander.setOption(SpeechConstants.GENERAL_CITY, strCity);
		mSpeechUnderstander.setOption(SpeechConstants.GENERAL_GPS,  strGpsInfo);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_DOMAIN, "poi,song");
		mSpeechUnderstander.setOption(SpeechConstants.NLU_SCENARIO, "incar");
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_LOCAL_CONTINUE_RECOGNIZE, false);

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
			//????????????????????????????????????
			if (mResults[1].mDataType == 0){//????????????????????????????????????????????????,????????????????????????????????????????????????????????????????????????
				mResults[1].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON;
				mResults[1].mErrorCode = IAsr.ERROR_ASR_NET_REQUEST;
			}
			
			if (mResults[0].mDataType == 0){//????????????????????????????????????????????????,????????????????????????????????????????????????????????????????????????
				mResults[0].mDataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON;
				mResults[0].mErrorCode = IAsr.ERROR_CODE;
			}
		}
		onResults();
	}
	
	private void onResults(){
		Result result = null;
		do {
			//?????????????????????
			if (mResults[0].mErrorCode == IAsr.ERROR_ASR_NO_USE){
				result = mResults[1];
				break;
			}
			
			//?????????????????????
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
		// ??????????????????
		if (result.mScore > -20 - DOUBLE && result.mScore < -20 + DOUBLE) {
			int error2 = mHasSpeaking ? IAsr.ERROR_NO_MATCH : IAsr.ERROR_NO_SPEECH;
			result.mErrorCode = error2;
			//?????????????????????,??????????????????????????????, ?????????????????????, ?????????????????????????????????
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
						onResult(100, jsonObject.toString(), VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON);
						return;
					} while (false);
					JNIHelper.loge("asr nlu result data is incomplete : " + jsonResult);
					mAsrCallBackProxy.onError(IAsr.ERROR_ASR_NET_NLU_EMTPY);
				} catch (Exception e) {
					JNIHelper.loge("asr nlu result data is incomplete : " + jsonResult);
					mAsrCallBackProxy.onError(IAsr.ERROR_ASR_NET_NLU_EMTPY);
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
						String result = jsonObject.getString("recognition_result");
						if (score > -20 - DOUBLE && score < -20 + DOUBLE) {
							//-20.0f?????????????????????????????????????????????
						}else{
							try {
								score = score + 5.0f;//74???????????????????????????-10.0f????????????????????????,??????????????????
								JSONObject jsonObj = new JSONObject(result);
								JSONObject c = jsonObj.getJSONArray("c").getJSONObject(0);
								c.put("score", score);
								result = jsonObj.toString();
								JNIHelper.logd("new_result : " + result);
							} catch (Exception e) {

							}
						}
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
							// ??????????????????????????????, ?????????????????????
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
									// ????????????????????????tag
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
									initCompiler();
									bCompiling = true;//????????????????????????
									JNIHelper.logd("insertVocab " + key);
									int nRet = 0;
									mSetDataSdkKeywords = oKeywords;
									mSetDataCallback = mP2;
									nRet = mSpeechUnderstander.insertVocab(
											KeyNamesList, "txzTag#" + key);
									JNIHelper.logd("insertVocab[" + key
											+ "] : nRet = " + nRet);
									error = nRet;
									return;//?????????SDk62+??????,insertVocab???????????????????????????
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
								 * ???????????????
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
	
	/********initCompiler???oDestroyCompilerRun????????????????????????????????????,?????????????????????********/
	private final static int  COMPILE_MAX_PRONUNCIATION = 6*6*4*4*3*3*2*2;//??????????????????????????????,?????????????????????????????????????????????SDK???????????????20???????????????????????????
	private boolean bCompilerInited = false;
	private boolean bCompiling = false;//????????????????????????????????????
	private void initCompiler(){
		//???????????????DestroyCompiler??????
		AsrWakeupEngine.getEngine().delOnCompileBackGround(oDestroyCompilerRun);
		if (bCompilerInited){
			return;
		}
		
		JNIHelper.logd("initCompiler");
		mSpeechUnderstander.initCompiler();
		long nCount = 0;
		while(!bCompilerInited && nCount < 100){
			nCount++;
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		/*****??????????????????????????????????????????????????????????????????*******/
		if (bCompilerInited){
			mSpeechUnderstander.loadCompiledJsgf("txzTag", GlobalContext.get().getApplicationInfo().dataDir + "/data/txz.dat");// ??????????????????
		}
		
		/**********????????????????????????,??????????????????????????????**********/
		if (bCompilerInited){
			mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_SET_COMPILE_MAX_PRONUNCIATION, COMPILE_MAX_PRONUNCIATION);//????????????????????????
			//mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_SET_OVER_MAX_PRONUNCIATION_INSERT,  false);//???????????????????????????????????????
		}
		JNIHelper.logd("initCompiler bCompilerInited = " + bCompilerInited);
	}
	
	private Runnable oDestroyCompilerRun = new Runnable() {
		@Override
		public void run() {
			//??????????????????destroyCompiler???????????????????????????????????????????????????????????????????????????????????????
			if (bCompilerInited && !bCompiling){
				JNIHelper.logd("destroyCompiler begin");
				mSpeechUnderstander.destoryCompiler();
				bCompilerInited = false;
				JNIHelper.logd("destroyCompiler");
			}
		}
	};
	
	private boolean bNeedDestroyCompiler = true;
	//??????5?????????Compiler,????????????????????????????????????
	private void destroyCompiler(){
		//72??????destroyCompiler????????????????????????Compiler??????
		if (!bNeedDestroyCompiler){
			return;
		}
		AsrWakeupEngine.getEngine().delOnCompileBackGround(oDestroyCompilerRun);
		AsrWakeupEngine.getEngine().runOnCompileBackGround(oDestroyCompilerRun, 5*1000);
	}
	/*****************initCompiler???oDestroyCompilerRun????????????????????????????????????,?????????????????????****************/
}
