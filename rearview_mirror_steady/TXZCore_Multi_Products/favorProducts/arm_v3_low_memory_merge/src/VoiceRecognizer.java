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

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData;
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
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.AsrType;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource.Config;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.component.wakeup.IWakeup.WakeupOption;
import com.txznet.txz.component.wakeup.mix.AsrCmdCompileTask;
import com.txznet.txz.component.wakeup.mix.CmdCompileTask;
import com.txznet.txz.component.wakeup.mix.PreBuildWKCmdUtils;
import com.txznet.txz.component.wakeup.mix.CmdCompileTask.TaskType;
import com.txznet.txz.component.wakeup.mix.CmdCompiler;
import com.txznet.txz.component.wakeup.mix.WakeupCmdCompileTask;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.wakeup.WakeupCmdTask;
import com.txznet.txz.util.KeyWordFilter;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.ThreshHoldAdapter;
import com.txznet.txz.util.VoiceGainHelper;
import com.txznet.txz.util.runnables.Runnable2;
import com.unisound.client.ErrorCode;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.StringUtils;


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
	private CmdCompiler mCmdCompiler = null;

	//打字效果
	private StringBuffer mAsrResultBuffer = null;
	private StringBuffer mAsrChangeResultBuffer = null;
	private boolean mLastSplitedResult = false;
	
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
				onCompilerInitDone();
				break;
			case SpeechConstants.ASR_EVENT_COMPILE_VOCAB_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILE_VOCAB_DONE");
				onCmdCompileSuccess(TaskType.TYPE_ASR);
				break;
			case SpeechConstants.ASR_EVENT_COMPILE_WAKEUP_WORD_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILE_WAKEUP_WORD_DONE");
				onCmdCompileSuccess(TaskType.TYPE_WAKEUP);
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
				JNIHelper.logd("WAKEUP_EVENT_SET_WAKEUPWORD_DONE");
				onCmdSetDone();
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
	 * {"errorCode":-91002,"errorMsg":"请求初始化错误"}
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
				onCmdCompileFail(errorCode);
				errorCode = IAsr.ERROR_SUCCESS;//不处理回调
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
	private Context mContext = null;
	public void init(InitListener oListener){
		mAsrCallBackProxy = AsrCallbackFactory.proxy();
		mInitListener = oListener;
		com.unisound.common.aa.setAppKey(ProjectCfg.getYunzhishengAppId());
		mContext = new TXZContext(GlobalContext.get(), ProjectCfg.getYzsFileDir());
		mRecognizer = new SpeechUnderstander(mContext, ProjectCfg.getYunzhishengAppId(), ProjectCfg.getYunzhishengSecret());
		mRecognizer.setOption(SpeechConstants.ASR_INIT_MODE, SpeechConstants.ASR_INIT_MODE_MIX);//云知声新的SDK,需要使用ASR_INIT_MODE参数
		mRecognizer.setListener(mRecognitionListener);
		
		//默认关闭引擎LOG
		mRecognizer.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mRecognizer.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mRecognizer.setOption(SpeechConstants.ASR_OPT_ADVANCE_INIT_COMPILER, false);
		mRecognizer.setOption(SpeechConstants.ASR_OPT_DEBUG_SAVELOG, false);
		mRecognizer.setOption(SpeechConstants.ASR_OPT_TEMP_RESULT_ENABLE, true);//设置可变结果
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
		
		PreBuildWKCmdUtils.checkCacheLimit();
		mCmdCompiler = new CmdCompiler(mRecognizer);
		
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
		boolean bRet = loadLastAsrGrammar();
		int delayTime = 0;
		if (bRet) {
			delayTime = 200;//load模型是异步的操作
		}

		mAsrResultBuffer = new StringBuffer();
		mAsrChangeResultBuffer = new StringBuffer();

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

	private boolean loadWakeupGrammar(){
		boolean bRet = false;
		String grammarPath = PreBuildWKCmdUtils.getWakeupGrammarPath(mLastWakeupCompileTask);
		if (grammarPath != null) {
			LogUtil.logd("load wakeupPath = " + grammarPath);
			mRecognizer.loadGrammar("wakeup", grammarPath);
			bRet = true;
		}
		return bRet;
	}
	
	private boolean loadLastAsrGrammar() {
		if (mContext == null){
			mContext = new TXZContext(GlobalContext.get(), ProjectCfg.getYzsFileDir());
			JNIHelper.logw("TXZContext create");
		}
		
		String grammarPath = mContext.getFilesDir().getPath() + "/YunZhiSheng/asrfix/jsgf_model/txzTag.dat";// 加载上一次生成的模型
		File file = new File(grammarPath);
		if (file.exists()) {
			JNIHelper.logd("load grammarPath = " + grammarPath);
			mRecognizer.loadGrammar("txzTag", grammarPath);
			return true;
		}
		return false;
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
		//设置前后端超时时间
		mRecognizer.setOption(SpeechConstants.ASR_VAD_TIMEOUT_FRONTSIL, mAsrOption.mBOS);
		mRecognizer.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, mAsrOption.mEOS);
		//OneShot离线识别使用
		mRecognizer.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, true); //先打开离线结果转json开关 
		mRecognizer.setOption(SpeechConstants.ASR_OPT_IGNORE_RESULT_TAG, "unk");
		String grammar = "txzTag";
		//记录用户是否讲话
		mHasSpeaking = false;
		
		// 判断是否是免唤醒识别
		mConfig.setBeginSpeechTime(oOption.mBeginSpeechTime);
		
		String strSaveDataPath = null;
		/*if (oOption.mVoiceID != 0){
			strSaveDataPath = oOption.mVoiceID+"";
		}*/ // 不用保存录音
		mConfig.enable(true);//防止其他地方置为false，没有切换回原始值
		mConfig.setmUID(oOption.mUID);
		mConfig.setmServerTime(oOption.mServerTime);
		mConfig.setbServerTimeConfidence(oOption.bServerTimeConfidence);
		mConfig.setSaveDataPath(strSaveDataPath);
		
		//不播放beep音的时候补跳过6400字节
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
		//调整塞给引擎的声音的增益
		if (DebugCfg.SAVE_RAW_PCM_CACHE && VoiceGainHelper.enable()){
			mRecognizer.setOption(SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA, 
					"/sdcard/txz/voice/" +  "yzs_" + System.currentTimeMillis() + "_" + VoiceGainHelper.getRate() + ".pcm");
		}

		mAsrResultBuffer.delete(0,mAsrResultBuffer.length());
		mAsrChangeResultBuffer.delete(0,mAsrChangeResultBuffer.length());
		mLastSplitedResult = false;

		JNIHelper.logd("start_asr");
		mRecognizer.start(grammar);
		mWorkStatus = WorkStatus.STATUS_ASR;
	}
	
	private static final int NET_TIMEOUT = 10000;//在线识别网络超时时间,单位毫秒
	private void setParam() {
		mRecognizer.setOption(SpeechConstants.ASR_NET_TIMEOUT, NET_TIMEOUT);
		String strCity = getCurrentCity();
		String strGpsInfo = getGpsInfo();
		JNIHelper.logd("gpsinfo : (" + strGpsInfo + "), currCity : " + strCity);
		mRecognizer.setOption(SpeechConstants.GENERAL_CITY, strCity);
		mRecognizer.setOption(SpeechConstants.GENERAL_GPS,  strGpsInfo);
		mRecognizer.setOption(SpeechConstants.ASR_DOMAIN, "poi,song,incar");
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

		mAsrResultBuffer.delete(0,mAsrResultBuffer.length());
		mAsrChangeResultBuffer.delete(0,mAsrChangeResultBuffer.length());
	}
	
	private WakeupOption mWakeupOption = null;
	private List<String> mLastWkWordList = new ArrayList<String>();
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
		
		mCmdCompiler.lock();
		loadWakeupGrammar();
		// load grammar不能立马启动唤醒,不然grammar load不了
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
		
		
		mRecognizer.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, false);
		mRecognizer.setOption(SpeechConstants.WAKEUP_OPT_THRESHOLD_VALUE,  -5.8f);
		mRecognizer.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, 100);
		
		/********需要VAD事件但是又不影响识别******/
		mRecognizer.setOption(SpeechConstants.ASR_OPT_WAKEUP_VAD_ENABLED, true);
		mRecognizer.setOption(SpeechConstants.ASR_OPT_VAD_AFFECT_ASR, false);
		
		//唤醒词前后有其他语音也可以唤醒
		mRecognizer.setOption(SpeechConstants.ASR_OPT_INHIBIT_BACK_WAKEUP, false);
		mRecognizer.setOption(SpeechConstants.ASR_OPT_INHIBIT_FRONT_WAKEUP, false);
		JNIHelper.logd("finally start_wakeup");
		mRecognizer.start("wakeup");
		//检测音量回调
		AppLogic.removeBackGroundCallback(oCheckTask);
		AppLogic.runOnBackGround(oCheckTask, 6*1000);
		mCmdCompiler.unlock();
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
	
	private CmdCompileTask mLastWakeupCompileTask = null;
	public void setWakeupKeywords(String[] keywords) {
		if (!mInitSuccessed){
			LogUtil.logw("bInitSuccessed = " + mInitSuccessed);
			return;
		}
		if (mWorkStatus != WorkStatus.STATUS_IDLE){
			JNIHelper.logw("start_wakeup work status : " + mWorkStatus);
			return;
		}
		//防止调用方设置了空唤醒词
		if (keywords == null || keywords.length == 0){
			LogUtil.logw("setWkKeywords keywords is empty!!!");
			return;
		}
		LogUtil.logd("setWakeupKws:" + Arrays.toString(keywords));
		// { "type" : "WAKEUP_TYPE_COMMON" }
		// 将需要添加的扩展信息放置到数组第一个位置
		int offset = 0;
		int kwsType = WakeupCmdTask.TYPE_NONE_MASK;
		
		try {
			JSONObject json = new JSONObject(keywords[0]);
			kwsType = json.getInt("type");
			offset = 1;
			LogUtil.logd("type:" + kwsType);
		} catch (Exception e) {
			LogUtil.logw("exception:" + e.toString());
		}
		
		List<String> WkWordList = new ArrayList<String>(keywords.length - offset);
		for (int i = offset; i < keywords.length; i++) {
			//空串SDK不会回调事件
			if (TextUtils.isEmpty(keywords[i])){
				continue;
			}
			WkWordList.add(keywords[i]);
		}
		
		//空SDK不会回调事件
		if (WkWordList.isEmpty()){
			LogUtil.logw("wakeup_words is empty");
			return;
		}
		
		mLastWkWordList = WkWordList;
		Set<String> set = new HashSet<String>();
		for (String string : WkWordList) {
			if (!TextUtils.isEmpty(string)) {
				set.add(string);
			}
		}
		// 判断唤醒词是否改变
		String sTaskId = MD5Util.generateMD5(set.toString());
		CmdCompileTask oTask = new WakeupCmdCompileTask(sTaskId, WkWordList, kwsType);
		//预编译类型的唤醒词，只参与编译，不影响下一次唤醒使用的唤醒词
		if (!WakeupCmdTask.isPreBuildType(kwsType)){
			mLastWakeupCompileTask = oTask;//下一次启动唤醒时,load这一次编译任务的唤醒词
		}
		mCmdCompiler.addCompileTask(oTask);
	}
	
	public void setWakeupThreshold(float val) {
		
	}

	public void enableVoiceChannel(boolean enable) {	
		if (mWorkStatus == WorkStatus.STATUS_ASR ){
			JNIHelper.logw("enableVoiceChannel_wakeup work status : " + mWorkStatus);
			return;
		}
		JNIHelper.logd("enableVoiceChannel_wakeup enable : " + enable);
		//enable录音通道时, 可以认为当即有音量回调, 避免两个问题：
		//1、TTS较长, 大于3秒, 且长度约为3的整数倍的时候，容易出现， enable为true的瞬间， 尚未拿到音量回调，但是检测线程正在执行检测。
		//2、enable为true后, 音量回调有延时,检测线程稍后立马执行检测。
		if (enable){
			mLastVolTime = SystemClock.elapsedRealtime();
		}
		bVoiceEnable = enable;
		mConfig.enable(enable);
	}
	
	
	public boolean checkWakeupWords(List<String> lastKws, List<String> kws) {
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
	// {"local_asr":[{"result_type":"full","score":-2.39,"recognition_result":"  你好小踢   ","engine_mode":"wakeup"}]}
	private boolean parseWakeupResult(String jsonResult) {
		JNIHelper.logd("jsonResult : " + jsonResult);
		String rawText = "";
		float score = 0.0f;
		try {
			JSONObject json = new JSONObject(jsonResult);
			JSONArray jsonArray = json.getJSONArray("local_asr");
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			rawText = jsonObject.getString("recognition_result");
			mWakeupText = rawText.replace(" ", "");//去掉空格
			score = (float) jsonObject.getDouble("score");
			mWakeupTime = jsonObject.getInt("utteranceTime");
			//转换与V2引擎一致的分数标准
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
			mRecognizer.stop();//72版本需要主动stop
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
	
	private void onCompilerInitDone(){	    
		mCmdCompiler.onInitDone();
	}
	
	public void onCmdCompileFail(int code){
		TaskType taskType = mCmdCompiler.getCurrTaskType();
		if (taskType == TaskType.TYPE_WAKEUP){
			
		}else if (taskType == TaskType.TYPE_ASR){
			onImportKeywords_fix(false, code);
		}
		mCmdCompiler.onCompileDone();
	}
	
	public void onCmdCompileSuccess(TaskType type){
		if (type == TaskType.TYPE_WAKEUP){
			if (mCmdCompiler.isCurrCompileTask(mLastWakeupCompileTask)){
				LogUtil.logd("wakeup_restart");
				onError(IWakeup.ERROR_CODE_RECORD_FAIL);
			}
		}else if (type == TaskType.TYPE_ASR){
			onImportKeywords_fix(true, 0);
		}
		mCmdCompiler.onCompileDone();
	}
	
	public void onCmdSetDone(){
		
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
		mRecognizer.cancel(); // 先取消后执行回调操作
		
		// 没有说话判断
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
					mAsrResultBuffer.delete(0,mAsrResultBuffer.length());
					mAsrChangeResultBuffer.delete(0,mAsrChangeResultBuffer.length());
					JNIHelper.loge("asr nlu result data is incomplete : " + jsonResult);
					mAsrCallBackProxy.onError(IAsr.ERROR_ASR_NET_NLU_EMTPY);
				} catch (Exception e) {
					JNIHelper.loge("asr nlu result data is incomplete : " + jsonResult);
					mAsrCallBackProxy.onError(IAsr.ERROR_ASR_NET_NLU_EMTPY);
				}
			}else if (jsonResult.contains("net_asr")) {
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
								boolean bSplitedResult = StringUtils.startWithSpecialChar(strResult);

								String strSimpleResult = StringUtils.filterSpecialChar(strResult);

								if (mLastSplitedResult != bSplitedResult) {
									mAsrResultBuffer.append(mAsrChangeResultBuffer.toString());
								}

								mAsrChangeResultBuffer.delete(0, mAsrChangeResultBuffer.length());
								mAsrChangeResultBuffer.append(strSimpleResult);

								mLastSplitedResult = bSplitedResult;

								mAsrCallBackProxy.onPartialResult(mAsrResultBuffer.toString() + mAsrChangeResultBuffer.toString());
								return;
							}
						} else if (TextUtils.equals(resultType, "partial")) {
							String strResult = jsonObject.getString("recognition_result");
							if (!TextUtils.isEmpty(strResult)) {
								String strSimpleResult = StringUtils.filterSpecialChar(strResult);
								mAsrChangeResultBuffer.delete(0, mAsrChangeResultBuffer.length());
								mAsrChangeResultBuffer.append(strSimpleResult);
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
						String result = jsonObject.getString("recognition_result");
						if (score > -20 - DOUBLE && score < -20 + DOUBLE) {
							//-20.0f表示没有结果，不再另外调整分数
						}else{
							try {
								score = score + 5.0f;//74版本离线识别阈值为-10.0f。兼容之前的版本,重新映射得分
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
	private void onImportKeywords_fix(boolean bSuccessed, int code){
		JNIHelper.logd("onImportFixKeywords bSuccessed : " + bSuccessed + ", code : " + code);
		final IImportKeywordsCallback oCallback = mSetDataCallback;
		mSetDataCallback = null;
		final SdkKeywords setDataSdkKeywords = mSetDataSdkKeywords;
		mSetDataSdkKeywords = null;//主动解引用,因为sdkKeywords数据量可能比较大
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
							// 如果强制使用在线识别, 不能插离线命令
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
									
									
									// 过滤引擎不支持的tag
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
									JNIHelper.logd("insertVocab " + key);
									int nRet = 0;
									mSetDataSdkKeywords = oKeywords;
									mSetDataCallback = mP2;
									//nRet = mRecognizer.insertVocab(KeyNamesList, "txzTag#" + key);
									String strTaskId = "txzTag#" + key;
									CmdCompileTask task = new AsrCmdCompileTask(strTaskId, KeyNamesList, 0);
									mCmdCompiler.addCompileTask(task);
									JNIHelper.logd("insertVocab[" + key + "] : nRet = " + nRet);
									error = nRet;
									return;// 云知声SDk62+版本,insertVocab接口变成了异步接口
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
								 * 讯飞的词库
								 */
								if (mP2 != null) {
									mP2.onSuccess(mP1);
								}
							}
						}
					}
				}, 0);
	}
	
	
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
			//已经停止唤醒, 结束检测
			if (mWorkStatus != WorkStatus.STATUS_WAKEUP){
				JNIHelper.logw("checkVol bWkStarted  : " + bWkStarted);
				return;
			}
			
			// 唤醒录音被拦截时，延时检测
			if (!bVoiceEnable) {
				//JNIHelper.logw("checkVol bVoiceEnable : " + bVoiceEnable);
				AppLogic.removeBackGroundCallback(oCheckTask);
				AppLogic.runOnBackGround(oCheckTask, 3 * 1000);
				return;
			}

			//2秒内有音量回调
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
