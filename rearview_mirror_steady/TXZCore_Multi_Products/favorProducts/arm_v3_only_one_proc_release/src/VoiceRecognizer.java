package com.txznet.txz.component.asr.mix;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemClock;

import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.AsrType;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource.Config;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.component.wakeup.IWakeup.WakeupOption;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.util.ExchangeHelper;
import com.txznet.txz.util.KeyWordFilter;
import com.txznet.txz.util.ThreshHoldAdapter;
import com.txznet.txz.util.VoiceGainHelper;
import com.txznet.txz.util.runnables.Runnable2;
import com.unisound.client.ErrorCode;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;

import static com.txznet.txz.component.asr.IAsr.ASR_THRESH;
import static com.txznet.txz.component.asr.IAsr.MONITOR_NO_REQUEST;

public class VoiceRecognizer implements IVoiceRecogition{
	public static final String MONITOR_INFO = "asr.yzs.I.";
	public static final String MONITOR_ERROR = "asr.yzs.E.";
	public static final String MONITOR_WARNING = "asr.yzs.W.";
	
	private enum WorkStatus{ 
		STATUS_ASR,
		STATUS_WAKEUP,
		STATUS_IDLE
	};
	private static class Result{
		public int mDataType = 0;
		public int mErrorCode = 0;
		public float mScore = -100;
		public String mStrResult;
	}
	
	private WorkStatus mWorkStatus = WorkStatus.STATUS_IDLE;
	private Result[] mResults = null;
	private SpeechUnderstander mRecognizer = null;
	private SpeechUnderstanderListener mRecognitionListener = new SpeechUnderstanderListener() {
		@Override
		public void onResult(int type, String jsonResult) {
			switch (type) {
			case SpeechConstants.ASR_RESULT_LOCAL:
				JNIHelper.logd("onFixResult =" + jsonResult);
				parseLocalResult(jsonResult);
				break;
			case SpeechConstants.ASR_RESULT_NET:
				JNIHelper.logd("onNetResult =" + jsonResult);
				parseNetResult(jsonResult);
				break;
			case SpeechConstants.ASR_RESULT_RECOGNITION:
				JNIHelper.logd("onRecognition =" + jsonResult);
				parseRecoginitionResult(jsonResult);
				break;
			case SpeechConstants.WAKEUP_RESULT:
				parseWakeupResult(jsonResult);
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
			case SpeechConstants.ASR_EVENT_COMPILER_INIT_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILER_INIT_DONE");
				bCompilerInited = true;
				break;
			case SpeechConstants.ASR_EVENT_COMPILE_VOCAB_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILE_VOCAB_DONE");
				onImportFixKeywords(true, 0);
				break;
			case SpeechConstants.ASR_EVENT_COMPILE_WAKEUP_WORD_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILE_WAKEUP_WORD_DONE");
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
				onTimeout();
				onEnd();
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_STOP:
				JNIHelper.logd("ASR_EVENT_RECORDING_STOP");
				onEnd();
				break;
			case SpeechConstants.ASR_EVENT_VOLUMECHANGE:
				Integer vol = (Integer) mRecognizer.getOption(SpeechConstants.GENERAL_UPDATE_VOLUME);
				if (null != vol) {
					onVolume(vol);
				}
				break;
			case SpeechConstants.ASR_EVENT_INIT_DONE:
				JNIHelper.logd("ASR_EVENT_ENGINE_INIT_DONE");
				onInit(true);
				break;
			case SpeechConstants.WAKEUP_EVENT_SET_WAKEUPWORD_DONE:
				JNIHelper.logd("WAKEUP_EVENT_RECOGNITION_SUCCESS");
				bSetWkWordDone = true;
				break;
			case SpeechConstants.WAKEUP_EVENT_RECOGNITION_SUCCESS:
				JNIHelper.logd("WAKEUP_EVENT_RECOGNITION_SUCCESS");
				onWakeup();
				break;
			case SpeechConstants.ASR_EVENT_USERDATA_UPLOADED:
				onImportKeywords(true, 0);
				break;
			}
		}

		@Override
		public void onError(int type, String errorMSG) {
			JNIHelper.loge("onError " + type + " " + errorMSG);
			int errCode = parseErrorCode(errorMSG);
			parseError(errCode);
		}
	};

	/*
	 * {"errorCode":-91002,"errorMsg":"?????????????????????"}
	 */
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
				errorCode = IAsr.ERROR_SUCCESS;//???????????????
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
			default:
				JNIHelper.logd("other errorCode : " + errorCode);
				monitor(MONITOR_ERROR + errorCode);
				break;
			}
		} catch (Exception e) {
			JNIHelper.loge("parseErrorCode exception : " + e.toString());
		}
		return errorCode;
	}
	
	private void monitor(String strAttr){
		if (NetworkManager.getInstance().hasNet()){
			MonitorUtil.monitorCumulant(strAttr);
		}
	}
	
	private InitListener mInitListener = null;
	public void init(InitListener oListener){
		mAsrCallBackProxy = AsrCallbackFactory.proxy();
		mInitListener = oListener;
		com.unisound.common.aa.setAppKey(ProjectCfg.getYunzhishengAppId());
		mRecognizer = new SpeechUnderstander(AppLogic.getApp(), ProjectCfg.getYunzhishengAppId(), ProjectCfg.getYunzhishengSecret());
		mRecognizer.setOption(SpeechConstants.ASR_INIT_MODE, SpeechConstants.ASR_INIT_MODE_MIX);//???????????????SDK,????????????ASR_INIT_MODE??????
		mRecognizer.setListener(mRecognitionListener);
		
		//??????????????????LOG
		mRecognizer.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mRecognizer.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mRecognizer.setOption(SpeechConstants.ASR_OPT_ADVANCE_INIT_COMPILER, false);
		mRecognizer.setOption(SpeechConstants.ASR_OPT_DEBUG_SAVELOG, false);
		JNIHelper.logd("init_asr_version SDK=" + mRecognizer.getVersion());
		JNIHelper.logd("init_asr_vesion ENGINE=" + mRecognizer.getFixEngineVersion());
		JNIHelper.logd("init_asr_begin");
		JSONObject json = new JSONObject();
		try {
			json.put("activate", "" + true);
			String strDevSn = getDeviceSn();
			json.put("deviceSn", "" + strDevSn);
		} catch (Exception e) {
			JNIHelper.loge("activator exception : " + e.toString());
		}
		int nRet = 0;
		mRecognizer.init(json.toString());
		if (nRet != 0){
			onInit(false);
		}
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
		JNIHelper.logd("mInitSuccessed = " + mInitSuccessed);
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (bSuccessed) {
					doInit();
				} else {
					notifyInitListener(0);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void doInit() {
		mConfig = new Config();
		mConfig.setSkipBytes(6400);
		mAudioSource = new TXZAudioSource(mConfig, ProjectCfg.mEnableAEC);
		mRecognizer.setAudioSource(mAudioSource);
//		mRecognizer.loadCompiledJsgf("txzTag", GlobalContext.get().getApplicationInfo().dataDir + "/data/txz.dat");// ??????????????????
		boolean bRet = loadLastGrammar();
		int delayTime = 0;
		if (bRet) {
			delayTime = 200;//load????????????????????????
		}
		notifyInitListener(delayTime);
	}
	
	private void notifyInitListener(long delayTime){
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				InitListener oListener = mInitListener;
				mInitListener = null;
				if(oListener != null){
					oListener.onInit(mInitSuccessed);
				}
			}
		}, delayTime);
	}

	/*
	 * load??????grammar
	 */
	private boolean loadLastGrammar() {
		loadWakeupGrammar();
		loadAsrGrammar();
		return true;
	}
	
	private void loadWakeupGrammar(){
		String wakeupPath = GlobalContext.get().getApplicationInfo().dataDir + "/data/wakeup.dat";
		String factoryWakeupPath = "/system/etc/txz/wakeup.dat";
		{
			File f = new File(factoryWakeupPath);
			if (f.exists()){
				wakeupPath = factoryWakeupPath;
			}
		}
		File wakeupFile = new File(wakeupPath);
		if (wakeupFile.exists()) {
			JNIHelper.logd("load wakeupPath = " + wakeupPath);
			mRecognizer.loadGrammar("wakeup", wakeupPath);
		}
	}
	
	private void loadAsrGrammar(){
		String grammarPath = GlobalContext.get().getApplicationInfo().dataDir + "/data/txzTag.dat";// ??????????????????????????????
		String fatoryGrammarPath = "/system/etc/txz/txzTag.dat";
		{
			File f = new File(fatoryGrammarPath);
			if (f.exists()){
				grammarPath = fatoryGrammarPath;
			}
		}
		File file = new File(grammarPath);
		if (file.exists()) {
			JNIHelper.logd("load grammarPath = " + grammarPath);
			mRecognizer.loadGrammar("txzTag", grammarPath);
		}
	}
	
	
	private AsrOption mAsrOption = null;
	private IAsrCallBackProxy mAsrCallBackProxy = null;
	private Config mConfig = null;
	private TXZAudioSource mAudioSource = null;
	private boolean mHasSpeaking = false;
	
	public void start(AsrOption oOption){
		mResults = new Result[2];
		mResults[0] = new Result();
		mResults[1] = new Result();
		mAsrOption = oOption;
		mAsrCallBackProxy.setAsrOption(mAsrOption);
		//???????????????????????????
		mRecognizer.setOption(SpeechConstants.ASR_VAD_TIMEOUT_FRONTSIL, mAsrOption.mBOS);
		mRecognizer.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, mAsrOption.mEOS);
		//OneShot??????????????????
		mRecognizer.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, true); //????????????????????????json?????? 
		mRecognizer.setOption(SpeechConstants.ASR_OPT_IGNORE_RESULT_TAG, "unk");
		String grammar = "txzTag";
		//????????????????????????
		mHasSpeaking = false;
		
		// ??????????????????????????????
		mConfig.setBeginSpeechTime(oOption.mBeginSpeechTime);
		
		String strSaveDataPath = null;
		/*if (oOption.mVoiceID != 0){
			strSaveDataPath = oOption.mVoiceID+"";
		}*/ // ??????????????????
		mConfig.enable(true);//????????????????????????false???????????????????????????
		mConfig.setmUID(oOption.mUID);
		mConfig.setmServerTime(oOption.mServerTime);
		mConfig.setbServerTimeConfidence(oOption.bServerTimeConfidence);
		mConfig.setSaveDataPath(strSaveDataPath);
		
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
		mRecognizer.setOption(SpeechConstants.ASR_SERVICE_MODE, nAsrSvrMode);
		//????????????????????????????????????
		if (DebugCfg.SAVE_RAW_PCM_CACHE && VoiceGainHelper.enable()){
			mRecognizer.setOption(SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA, 
					"/sdcard/txz/voice/" +  "yzs_" + System.currentTimeMillis() + "_" + VoiceGainHelper.getRate() + ".pcm");
		}
		JNIHelper.logd("start_asr");
		mRecognizer.start(grammar);
		mWorkStatus = WorkStatus.STATUS_ASR;
	}
	
	private static final int NET_TIMEOUT = 10000;//??????????????????????????????,????????????
	private void setParam() {
		mRecognizer.setOption(SpeechConstants.ASR_NET_TIMEOUT, NET_TIMEOUT);
		String strCity = getCurrentCity();
		String strGpsInfo = getGpsInfo();
		JNIHelper.logd("gpsinfo : (" + strGpsInfo + "), currCity : " + strCity);
		mRecognizer.setOption(SpeechConstants.GENERAL_CITY, strCity);
		mRecognizer.setOption(SpeechConstants.GENERAL_GPS,  strGpsInfo);
		mRecognizer.setOption(SpeechConstants.ASR_DOMAIN, "poi,song");
		mRecognizer.setOption(SpeechConstants.NLU_SCENARIO, "incar");
		mRecognizer.setOption(SpeechConstants.ASR_OPT_LOCAL_CONTINUE_RECOGNIZE, false);
	}
	
	public static  String getCurrentCity(){
		String strCity = null;
		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			strCity = location.msgGeoInfo.strCity;
		} catch (Exception e) {
		}
        return strCity;
	}
	
	public static String getGpsInfo(){
		String strGpsInfo = null;
		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			double lat = location.msgGpsInfo.dblLat;
			double lng = location.msgGpsInfo.dblLng;
			strGpsInfo = lat + "," + lng;
		} catch (Exception e) {
		}
		return strGpsInfo;
	}
	
	public void stop(AsrOption oOption){
		if (mWorkStatus != WorkStatus.STATUS_ASR){
			JNIHelper.logw("stop_asr work status : " + mWorkStatus);
			return;
		}
		JNIHelper.logd("stop_asr");
		mRecognizer.stop();
	}
	
	public void cancel(AsrOption oOption){
		if (mWorkStatus != WorkStatus.STATUS_ASR){
			JNIHelper.logw("cancel_asr work status : " + mWorkStatus);
			return;
		}
		JNIHelper.logd("cancel_asr");
		mRecognizer.cancel();
		mWorkStatus = WorkStatus.STATUS_IDLE;
	}
	
	private WakeupOption mWakeupOption = null;
	private List<String> mLastWkWordList = new ArrayList<String>();
	private Set<String> mDigitWords = new HashSet<String>();
	private boolean bSetWkWordDone = false;
	public void start(WakeupOption oOption){
		if (!mInitSuccessed){
			JNIHelper.logw("start_wakeup mInitSuccessed  : " + mInitSuccessed);
			return;
		}
		if (mWorkStatus != WorkStatus.STATUS_IDLE){
			JNIHelper.logw("start_wakeup work status : " + mWorkStatus);
			return;
		}
		mWakeupOption = oOption;
		JNIHelper.logd("start_wakeup");
		mWorkStatus = WorkStatus.STATUS_WAKEUP;
		
		loadWakeupGrammar();
		
		mRecognizer.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, false);
		mRecognizer.setOption(SpeechConstants.WAKEUP_OPT_THRESHOLD_VALUE,  -5.8f);
		mRecognizer.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, 100);
		
		/********??????VAD??????????????????????????????******/
		mRecognizer.setOption(SpeechConstants.ASR_OPT_WAKEUP_VAD_ENABLED, true);
		mRecognizer.setOption(SpeechConstants.ASR_OPT_VAD_AFFECT_ASR, false);
		
		//?????????????????????????????????????????????
		mRecognizer.setOption(SpeechConstants.ASR_OPT_INHIBIT_BACK_WAKEUP, false);
		mRecognizer.setOption(SpeechConstants.ASR_OPT_INHIBIT_FRONT_WAKEUP, false);
		mRecognizer.start("wakeup");
		//??????????????????
		AppLogic.removeBackGroundCallback(oCheckTask);
		AppLogic.runOnBackGround(oCheckTask, 6*1000);
	}
	
	public void stop(WakeupOption oOption){
		if (mWorkStatus != WorkStatus.STATUS_WAKEUP){
			JNIHelper.logw("stop_wakeup work status : " + mWorkStatus);
			return;
		}
		mWakeupOption = null;
		JNIHelper.logd("stop_wakeup");
		AppLogic.removeBackGroundCallback(oCheckTask);
		mRecognizer.cancel();
		mWorkStatus = WorkStatus.STATUS_IDLE;
	}

	public void setWakeupKeywords(String[] keywords) {
		if (!mInitSuccessed){
			JNIHelper.logw("VoiceRecogizer mInitSuccessed  : " + mInitSuccessed);
			return;
		}
		if (mWorkStatus != WorkStatus.STATUS_IDLE){
			JNIHelper.logw("setWakeupKeywords work status : " + mWorkStatus);
			return;
		}
		
		JNIHelper.logd("setWakeupKeywords");
		
		if (!ProjectCfg.enableUpdateKeywords()) {
			JNIHelper.logd("disableUpdateKeywords");
			return;
		}
		
		keywords = new String[] { "???????????????", "???????????????", "???????????????", "???????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", "????????????", 
				"??????", "??????", "?????????", "?????????", "?????????", "?????????", "?????????", "?????????" };
		
		//????????????????????????????????????
		if (keywords == null || keywords.length == 0){
			JNIHelper.logw("setWkKeywords keywords is empty!!!");
			return;
		}
		
		List<String>WkWordList = Arrays.asList(keywords);
		if (checkWakeupWords(mLastWkWordList, WkWordList)) {
			JNIHelper.logw("wakeup keywords not change");
			return;
		}
		bSetWkWordDone = false;
		mRecognizer.setWakeupWord(WkWordList);
		int nCount = 0;
		while(!bSetWkWordDone && nCount < 1000){
			try {
				Thread.sleep(10);
				nCount++;
			} catch (InterruptedException e) {
			}
		}
		if (bSetWkWordDone){
			mLastWkWordList = WkWordList;
			mDigitWords.clear();
			for (int i = 0; i < mLastWkWordList.size(); ++i){
				String strWord = mLastWkWordList.get(i);
				if (ExchangeHelper.hasDigit(strWord)){
					mDigitWords.add(strWord);
				}
			}
			JNIHelper.logd("mDigitWords size  : " + mDigitWords.size());
		}
		JNIHelper.logd("bSetWkWordDone : " + bSetWkWordDone);
	}
	
	public void setWakeupThreshold(float val) {
		
	}

	public void enableVoiceChannel(boolean enable) {	
		if (mWorkStatus == WorkStatus.STATUS_ASR ){
			JNIHelper.logw("enableVoiceChannel_wakeup work status : " + mWorkStatus);
			return;
		}
		JNIHelper.logd("enableVoiceChannel_wakeup enable : " + enable);
		//enable???????????????, ?????????????????????????????????, ?????????????????????
		//1???TTS??????, ??????3???, ???????????????3??????????????????????????????????????? enable???true???????????? ??????????????????????????????????????????????????????????????????
		//2???enable???true???, ?????????????????????,???????????????????????????????????????
		if (enable){
			mLastVolTime = SystemClock.elapsedRealtime();
		}
		bVoiceEnable = enable;
		mConfig.enable(enable);
	}
	
	
	private boolean checkWakeupWords(List<String> lastKws, List<String> kws) {
		if (lastKws == null || kws == null || lastKws.size() != kws.size()) {
			return false;
		}
		Set<String> setKw = new HashSet<String>();
		Set<String> setRemove  = new HashSet<String>();
		setKw.addAll(lastKws);
		for (String s : kws) {
			if (setKw.remove(s) == false && setRemove.contains(s) == false) {
				return false;
			}
			setRemove.add(s);
		}
		return setKw.isEmpty();
	}
	
	private String mWakeupText = "";
	private int mWakeupTime = 0;
	private float mWakeupScore = 0.0f;
	// {"local_asr":[{"result_type":"full","score":-2.39,"recognition_result":"  ????????????   ","engine_mode":"wakeup"}]}
	private boolean parseWakeupResult(String jsonResult) {
		JNIHelper.logd("jsonResult : " + jsonResult);
		String rawText = "";
		float score = 0.0f;
		try {
			JSONObject json = new JSONObject(jsonResult);
			JSONArray jsonArray = json.getJSONArray("local_asr");
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			rawText = jsonObject.getString("recognition_result");
			mWakeupText = rawText.replace(" ", "");//????????????
			score = (float) jsonObject.getDouble("score");
			mWakeupTime = jsonObject.getInt("utteranceTime");
			//?????????V2???????????????????????????
			mWakeupScore = ThreshHoldAdapter.getThreshValueFromV3(score);
			return true;
		} catch (Exception e) {
			JNIHelper.loge("parseWakeupRawText : " + e.toString());
			mWakeupTime = 0;
		}
		return false;
	}
	
	public static final double DOUBLE = 0.00000001;
	private void onSpeech(final boolean bSpeaking) {
		if (mWorkStatus == WorkStatus.STATUS_ASR) {
			JNIHelper.logd("bSpeaking : " + bSpeaking);
			if (bSpeaking) {
				mHasSpeaking = true;
				mAsrCallBackProxy.onBeginOfSpeech();
			} else {
				mAsrCallBackProxy.onEndOfSpeech();
			}
		}else if (mWorkStatus == WorkStatus.STATUS_WAKEUP){
			AppLogic.runOnBackGround(new Runnable() {
				
				@Override
				public void run() {
					WakeupOption oOption = mWakeupOption;
					if (oOption == null){
						return;
					}
					IWakeupCallback callBack = oOption.wakeupCallback;
					if (callBack == null){
						return;
					}
					if (bSpeaking) {
						callBack.onSpeechBegin();
					} else {
						callBack.onSpeechEnd();
					}
				}
			});
		}
	}
	
	private void onVolume(final int vol) {
		if (mWorkStatus == WorkStatus.STATUS_ASR) {
			mAsrCallBackProxy.onVolume(vol);
		} else if (mWorkStatus == WorkStatus.STATUS_WAKEUP) {
			mLastVolTime = SystemClock.elapsedRealtime();
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					WakeupOption oOption = mWakeupOption;
					if (oOption == null) {
						return;
					}
					IWakeupCallback callBack = oOption.wakeupCallback;
					if (callBack == null) {
						return;
					}
					callBack.onVolume(vol);
				}
			});
		}
	}

	private void onStart() {
		if (mWorkStatus == WorkStatus.STATUS_ASR) {
			mAsrCallBackProxy.onStart();
		}
	}
	
	private void onTimeout() {
		if (mWorkStatus == WorkStatus.STATUS_ASR) {
			mRecognizer.stop();//72??????????????????stop
		}
	}

	private void onEnd() {
		if (mWorkStatus == WorkStatus.STATUS_ASR) {
			mAsrCallBackProxy.onEnd();
		}
	}
	
	private void onError(final int errCode){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				WakeupOption oOption = mWakeupOption;
				if (oOption == null) {
					return;
				}
				IWakeupCallback callBack = oOption.wakeupCallback;
				if (callBack == null) {
					return;
				}
				callBack.onError(errCode);
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onWakeup(){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					WakeupOption oOption = mWakeupOption;
					if (oOption == null) {
						return;
					}
					IWakeupCallback callBack = oOption.wakeupCallback;
					if (callBack == null) {
						return;
					}
					callBack.onWakeUp(mWakeupText, mWakeupTime, mWakeupScore);
				} catch (Exception e) {
					
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void parseError(final int errCode) {
		if (errCode == IAsr.ERROR_ASR_NET_REQUEST){
			onResult(errCode, VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON);
		}else if (errCode == IAsr.ERROR_CODE){
			onResult(errCode, VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON);
		}
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
		mRecognizer.cancel(); // ??????????????????????????????
		
		// ??????????????????
		if (result.mScore > -20 - DOUBLE && result.mScore < -20 + DOUBLE) {
			int error2 = mHasSpeaking ? IAsr.ERROR_NO_MATCH : IAsr.ERROR_NO_SPEECH;
			result.mErrorCode = error2;
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
			mAsrCallBackProxy.onSuccess(oVoiceParseData);
		} while (false);
		end();
	}
	
	private void end(){
		mResults = null;
	}
	
	private void parseRecoginitionResult(String jsonResult){
		onResult(0, jsonResult, -1);
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
				} catch (Exception e) {
					JNIHelper.loge("parseLocalResult : " + e.toString());
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
	
	private void onImportKeywords(boolean bSuccessed, int code){
		JNIHelper.logd("onImportKeywords bSuccessed : " + bSuccessed + ", code : " + code);
		final IImportKeywordsCallback oCallback = mSetNetDataCallback;
		mSetNetDataCallback = null;
		if (oCallback != null){
			if (bSuccessed){
				oCallback.onSuccess(mSetNetDataSdkKeywords);
			}else{
				oCallback.onError(code, mSetNetDataSdkKeywords);
			}
		}else{
			JNIHelper.loge("onImportKeywords oCallback = null");
		}
	}
	
	private SdkKeywords mSetNetDataSdkKeywords;
	private IImportKeywordsCallback mSetNetDataCallback;
	private SdkKeywords mSetDataSdkKeywords = null;
	private IImportKeywordsCallback mSetDataCallback = null;
	private void onImportFixKeywords(boolean bSuccessed, int code){
		JNIHelper.logd("onImportFixKeywords bSuccessed : " + bSuccessed + ", code : " + code);
		final IImportKeywordsCallback oCallback = mSetDataCallback;
		mSetDataCallback = null;
		final SdkKeywords setDataSdkKeywords = mSetDataSdkKeywords;
		mSetDataSdkKeywords = null;//???????????????,??????sdkKeywords????????????????????????
		if (oCallback != null){
			if (bSuccessed){
				oCallback.onSuccess(setDataSdkKeywords);
			}else{
				oCallback.onError(code, setDataSdkKeywords);
			}
		}else{
			JNIHelper.loge("onImportFixKeywords oCallback = null");
		}
	}
	
	public void importKeywords(SdkKeywords oKeywords, IImportKeywordsCallback oCallback) {
		AsrWakeupEngine.getEngine().runOnCompileBackGround(
				new Runnable2<SdkKeywords, IImportKeywordsCallback>(oKeywords, oCallback) {
					@Override
					public void run() {
						if (mRecognizer == null) {
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
							JNIHelper.logd("importKeywordsContact: session_id=" + oKeywords.uint32SessionId);
							Map<Integer, List<String>> mapData = new HashMap<Integer, List<String>>();
							mapData.put(SpeechConstants.UPLOAD_DATA_NAME, lstData);
							mSetNetDataSdkKeywords = oKeywords;
							mSetNetDataCallback = mP2;
							mRecognizer.uploadUserData(mapData);
						} else {
							// ??????????????????????????????, ?????????????????????
							if (ProjectCfg.RecognOnline()) {
								JNIHelper.logw("ProjectCfg.RecognOnline() = " + ProjectCfg.RecognOnline());
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
								String key = oKeywords.strType.substring(1, oKeywords.strType.length() - 1);
								JNIHelper.logd("importKeywordsKey = " + key + ", session_id="
										+ oKeywords.uint32SessionId);
								do {
									if (!ProjectCfg.enableUpdateKeywords()) {
										JNIHelper.logd("disableUpdateKeywords");
										break;
									}
									
									// ????????????????????????tag
									if (key.contains("callPrefix") || key.contains("callSuffix")) {
										JNIHelper.logd("filter unsupport slot");
										break;
									}

									String[] aContents = null;
									if (oKeywords.rptStrKw != null) {
										aContents = oKeywords.rptStrKw;
									} else {
										String strContents = oKeywords.strContent;
										if (strContents == null || strContents.isEmpty()) {
											break;
										}
										aContents = strContents.split("\n");
									}

									if (aContents == null || aContents.length == 0) {
										break;
									}

									List<String> KeyNamesList = new ArrayList<String>();
									KeyNamesList.clear();

									for (int i = 0; i < aContents.length; i++) {
										String content = aContents[i];
										if (content == null || content.trim().isEmpty()
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
									bCompiling = true;// ????????????????????????
									JNIHelper.logd("insertVocab " + key);
									int nRet = 0;
									mSetDataSdkKeywords = oKeywords;
									mSetDataCallback = mP2;
									nRet = mRecognizer.insertVocab(KeyNamesList, "txzTag#" + key);
									JNIHelper.logd("insertVocab[" + key + "] : nRet = " + nRet);
									error = nRet;
									return;// ?????????SDk62+??????,insertVocab???????????????????????????
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
								String key = oKeywords.strType.substring(1, oKeywords.strType.length());
								List<String> lstData = new ArrayList<String>();
								String[] lst = oKeywords.strContent.split("\n");
								for (int i = 0; i < lst.length; ++i) {
									if (lst[i].isEmpty()) {
										continue;
									}
									lstData.add(lst[i]);
								}
								JNIHelper.logd("importKeywordsOnlineUserData =" + key + ", session_id="
										+ oKeywords.uint32SessionId);
								Map<Integer, List<String>> mapData = new HashMap<Integer, List<String>>();
								mapData.put(SpeechConstants.UPLOAD_DATA_APP, lstData);
								mSetNetDataSdkKeywords = oKeywords;
								mSetNetDataCallback = mP2;
								mRecognizer.uploadUserData(mapData);
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
	}
	
	private void initCompiler() {
		// ???????????????DestroyCompiler??????
		AsrWakeupEngine.getEngine().delOnCompileBackGround(oDestroyCompilerRun);
		if (bCompilerInited) {
			return;
		}

		JNIHelper.logd("initCompiler");
		mRecognizer.initCompiler();
		long nCount = 0;
		while (!bCompilerInited && nCount < 100) {
			nCount++;
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		/***** ?????????????????????????????????????????????????????????????????? *******/
		if (bCompilerInited && !btxzTag_jsgf_loaded) {
			mRecognizer.loadCompiledJsgf("txzTag", GlobalContext.get().getApplicationInfo().dataDir
					+ "/data/txz.dat");// ??????????????????
			btxzTag_jsgf_loaded = true;
		}
		/********** ????????????????????????,?????????????????????????????? **********/
		if (bCompilerInited) {
			mRecognizer.setOption(SpeechConstants.ASR_OPT_SET_COMPILE_MAX_PRONUNCIATION,
					COMPILE_MAX_PRONUNCIATION);// ????????????????????????
			// mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_SET_OVER_MAX_PRONUNCIATION_INSERT,
			// false);//???????????????????????????????????????
		}
		JNIHelper.logd("initCompiler bCompilerInited = " + bCompilerInited);
	}
	/********initCompiler???oDestroyCompilerRun????????????????????????????????????,?????????????????????********/
	private final static int  COMPILE_MAX_PRONUNCIATION = 6*4*4*2*2;//??????????????????????????????,?????????????????????????????????????????????SDK???????????????20???????????????????????????
	private boolean bCompilerInited = false;
	private boolean btxzTag_jsgf_loaded = false;
	private boolean bCompiling = false;//????????????????????????????????????
	
	private Runnable oDestroyCompilerRun = new Runnable() {
		@Override
		public void run() {
			//??????????????????destroyCompiler???????????????????????????????????????????????????????????????????????????????????????
			if (bCompilerInited && !bCompiling){
				JNIHelper.logd("destroyCompiler begin");
				mRecognizer.destoryCompiler();
				bCompilerInited = false;
				JNIHelper.logd("destroyCompiler");
			}
		}
	};
	
	private Runnable mRetryImportOnlineKeywordsTask = new Runnable() {
		@Override
		public void run() {
			JNIHelper.logd("retryImportOnlineKeywords while too fast timeout");
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_RETRY_ONLINE_KEYWORDS);
		}
	};
	
	private boolean bWkStarted = false;
	private boolean bVoiceEnable = true;
	private long mLastVolTime = SystemClock.elapsedRealtime();
	private Runnable oCheckTask = new Runnable() {
		@Override
		public void run() {
			//??????????????????, ????????????
			if (mWorkStatus != WorkStatus.STATUS_WAKEUP){
				JNIHelper.logw("checkVol bWkStarted  : " + bWkStarted);
				return;
			}
			
			// ???????????????????????????????????????
			if (!bVoiceEnable) {
				//JNIHelper.logw("checkVol bVoiceEnable : " + bVoiceEnable);
				AppLogic.removeBackGroundCallback(oCheckTask);
				AppLogic.runOnBackGround(oCheckTask, 3 * 1000);
				return;
			}

			//2?????????????????????
			if (mLastVolTime + 2000 > SystemClock.elapsedRealtime()) {
				//JNIHelper.logw("checkVol mLastVolTime : " + mLastVolTime + ", now : " + SystemClock.elapsedRealtime());
				AppLogic.removeBackGroundCallback(oCheckTask);
				AppLogic.runOnBackGround(oCheckTask, 3 * 1000);
				return;
			}
			
			JNIHelper.loge("checkVol have no volume data for long time");
			onError(IWakeup.ERROR_CODE_NO_VOL);
		}
	};
	
}
