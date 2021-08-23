package com.txznet.txz.component.asr.mix.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;






import android.os.SystemClock;

import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.service.TXZContext;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.mix.Arguments;
import com.txznet.txz.component.asr.mix.AsrCallbackFactory;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource.Config;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.runnables.Runnable2;
import com.unisound.client.ErrorCode;
import com.unisound.client.IAudioSource;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;

public class NetAsrYunzhishengImpl implements IAsr {
	public static final String MONITOR_INFO = "asr.yzs.I.";
	public static final String MONITOR_ERROR = "asr.yzs.E.";
	public static final String MONITOR_WARNING = "asr.yzs.W.";
	private Config mConfig = null;
	private IAudioSource mAudioSource = null;
	private SpeechUnderstander mSpeechUnderstander = null;
	private IAsrCallBackProxy mAsrCallBackProxy = null;
	private boolean bInitOk = false;
	private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {
	    private long sReady = 0;
	    private long sEnd = 0;
	    private long sBegin = 0;
		@Override
		public void onResult(int type, String jsonResult) {
			switch (type) {
			case SpeechConstants.ASR_RESULT_NET:
				LogUtil.logd("onFixResult =" + jsonResult);
				if (jsonResult.contains("net_asr")
						&& jsonResult.contains("net_nlu")) {
					LogUtil.logd("Asr:finish,timeCast:"+(SystemClock.elapsedRealtime() - sEnd));
				}
				parseResult(jsonResult);
				break;
			}
		}

		@Override
		public void onEvent(int type, int timeMs) {
			switch (type) {
			case SpeechConstants.ASR_EVENT_RECORDING_START:
				LogUtil.logd("Asr:ready");
				sReady = SystemClock.elapsedRealtime();
				mAsrCallBackProxy.onStart();
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_DETECTED:
				sBegin = SystemClock.elapsedRealtime();
				LogUtil.logd("Asr:speachBegin,timeCast:"+(sBegin - sReady));
				mAsrCallBackProxy.onBeginOfSpeech();
				break;
			case SpeechConstants.ASR_EVENT_VAD_TIMEOUT:
				LogUtil.logd("NetAsr ASR_EVENT_VAD_TIMEOUT");
				mSpeechUnderstander.stop();//72版本需要主动stop
				mAsrCallBackProxy.onEnd();
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_END:
				sEnd = SystemClock.elapsedRealtime();
				LogUtil.logd("Asr:speachEnd,timeCast:"+(sEnd - sBegin));
				mAsrCallBackProxy.onEndOfSpeech();
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_STOP:
				LogUtil.logd("NetAsr ASR_EVENT_RECORDING_STOP");
				break;
			case SpeechConstants.ASR_EVENT_USERDATA_UPLOADED:
				onImportKeywords(true, 0);
				break;
			case SpeechConstants.ASR_EVENT_VOLUMECHANGE:
				Integer vol = (Integer) mSpeechUnderstander
						.getOption(SpeechConstants.GENERAL_UPDATE_VOLUME);
				if (null == vol) {
					return;
				}
				mAsrCallBackProxy.onVolume(vol);
				break;
			case SpeechConstants.ASR_EVENT_INIT_DONE:
				LogUtil.logd("ASR_EVENT_ENGINE_INIT_DONE");// 纯在线识别不会有此事件回调
				break;
			}
		}

		@Override
		public void onError(int type, String errorMSG) {
			LogUtil.logd("Asr:finish,timeCast:"+(SystemClock.elapsedRealtime() - sEnd));
			LogUtil.loge("onError " + type + " " + errorMSG);
			parseErrorCode(errorMSG);
		}
	};

	// {"errorCode":-91002,"errorMsg":"请求初始化错误"}
	private void parseErrorCode(String errorMsg) {
		JSONObject json = null;
		int errorCode = 0;
		try {
			json = new JSONObject(errorMsg);
			errorCode = json.getInt("errorCode");
			switch (errorCode) {
			case -91155://{"errorCode":-91155,"errorMsg":"请求时间过长"}
			case -91007://{"errorCode":-91007,"errorMsg":"其他异常"}
			case -90005://{"errorCode":-90005,"errorMsg":"其他异常"}
			case -62002://{"errorCode":-62002,"errorMsg":"服务器通讯错误"}
			case -90002://{"errorCode":-90002,"errorMsg":"模型加载失败！"}
			case -91883:
			case ErrorCode.SEND_REQUEST_ERROR:
			case ErrorCode.REQ_INIT_ERROR:
			case ErrorCode.GENERAL_INIT_ERROR:
				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + MONITOR_NO_REQUEST);
				mAsrCallBackProxy.onError(IAsr.ERROR_ASR_NET_REQUEST);
				break;
			case ErrorCode.UPLOAD_USER_DATA_TOO_FAST:
				mAsrCallBackProxy.onMonitor(MONITOR_WARNING
						+ MONITOR_UPLOAD_TOO_FAST);
				onImportKeywords(false, IImportKeywordsCallback.ERROR_UPLOAD_TOO_FAST);
				break;
			case ErrorCode.UPLOAD_USER_DATA_EMPTY:
			case ErrorCode.UPLOAD_USER_DATA_NETWORK_ERROR:
			case ErrorCode.UPLOAD_USER_DATA_SERVER_REFUSED:
				mAsrCallBackProxy.onMonitor(MONITOR_WARNING
						+ MONITOR_UPLOAD_NET);
				onImportKeywords(false, -1);
				break;
			default:
				LogUtil.loge("unkown error :  " + errorMsg);
				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + errorCode);
				mAsrCallBackProxy.onError(IAsr.ERROR_CODE);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private IInitCallback mInitCallback = null;
	@Override
	public int initialize(IInitCallback oRun) {
		mInitCallback = oRun;
		init_async();
		return 0;
	}

	private void init_async() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				init_sync();
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void init_sync() {
		mAsrCallBackProxy = AsrCallbackFactory.proxy();
		com.unisound.common.aa.setAppKey(ProjectCfg.getYunzhishengAppId());
		mSpeechUnderstander = new SpeechUnderstander(GlobalContext.get(),
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_NET);
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		//默认关闭引擎LOG
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_DEBUG_SAVELOG, false);
		JSONObject json = new JSONObject();
		try {
			json.put("activate", "" + true);
			String strDevSn = getDeviceSn();
			json.put("deviceSn", "" + strDevSn);
		} catch (Exception e) {
			
		}
		//LogUtil.logd("activator : " + json.toString());
		
		LogUtil.logd("init asr begin");
		int nRet = 0;
		mSpeechUnderstander.init(json.toString());
		LogUtil.logd("init asr end = " + nRet);
		bInitOk = nRet == 0;
		onInit(bInitOk);
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
	
	private void onInit(final boolean bSuccessed) {
		if (bSuccessed) {
			doInit();
			return;
		}
		// 失败直接返回
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				IInitCallback cb = mInitCallback;
				mInitCallback = null;
				if (cb != null) {
					cb.onInit(bSuccessed);
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
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				IInitCallback cb = mInitCallback;
				mInitCallback = null;
				if (cb != null) {
					cb.onInit(true);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	@Override
	public void release() {

	}

	private AsrOption mAsrOption = null;
	@Override
	public int start(AsrOption oOption) {
		mAsrOption = oOption;

		if (!bInitOk) {
			LogUtil.logd("bInitOk = " + bInitOk);
			init_sync();
		}
		mConfig.setBeginSpeechTime(oOption.mBeginSpeechTime);
		
		String strSaveDataPath = null;
		if (oOption.mVoiceID != 0){
			strSaveDataPath = oOption.mVoiceID+"";
		}
		mConfig.setmUID(oOption.mUID);
		mConfig.setmServerTime(oOption.mServerTime);
		mConfig.setbServerTimeConfidence(oOption.bServerTimeConfidence);
		mConfig.setSaveDataPath(strSaveDataPath);
		mAsrCallBackProxy.setAsrOption(mAsrOption);
		mAsrCallBackProxy.onMonitor(MONITOR_INFO + MONITOR_ALL);
		if (mAsrOption.mPlayBeepSound) {
			mConfig.setSkipBytes(6400);
		}else {
			mConfig.setSkipBytes(0);
		}
		setParam();

		mSpeechUnderstander.start();

		return 0;
	}

	private void parseResult(String jsonResult) {
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

						VoiceData.VoiceParseData voice = new VoiceData.VoiceParseData();
						voice.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON;
						voice.strVoiceData = jsonObject.toString();
						voice.uint32Sence = mAsrOption.mGrammar;
						AsrOption oOption = mAsrOption;
						if (oOption != null) {
							if (oOption.mManual) {
								voice.boolManual = 1;
							} else {
								voice.boolManual = 0;
							}
							voice.uint32Sence = oOption.mGrammar;
							if (!oOption.mNeedStopWakeup)
								voice.uint32AsrWakeupType = VoiceData.VOICE_ASR_WAKEUP_TYPE_MIX;
						}
						mAsrCallBackProxy.onSuccess(voice);
					} while (false);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
    
	private static final int NET_TIMEOUT = 10000;//在线识别网络超时时间,单位毫秒
	private void setParam() {
		mSpeechUnderstander.setOption(SpeechConstants.ASR_NET_TIMEOUT, NET_TIMEOUT);//设置网络超时时间,单位毫秒,取值范围[10000, 60000],默认值30000。
																																							 //NET_TIMEOUT<10000, SDK置为10000, >60000,SDK置为60000。(修改原因:云知声接口经常变动) 
		mSpeechUnderstander.setOption(SpeechConstants.ASR_VAD_TIMEOUT_FRONTSIL,
				mAsrOption.mBOS);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL,
				mAsrOption.mEOS);
		mSpeechUnderstander
				.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, true);
		LogUtil.logd("gpsinfo : (" + Arguments.sGpsInfo + "), currCity : "
				+ Arguments.sCurrCity);
		mSpeechUnderstander.setOption(SpeechConstants.GENERAL_CITY,
				Arguments.sCurrCity);
		mSpeechUnderstander.setOption(SpeechConstants.GENERAL_GPS,
				Arguments.sGpsInfo);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_DOMAIN, "poi,song");
		mSpeechUnderstander.setOption(SpeechConstants.NLU_SCENARIO, "incar");

	}

	@Override
	public void stop() {
		mSpeechUnderstander.stop();
	}

	@Override
	public void cancel() {
		mSpeechUnderstander.cancel();
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

	SdkKeywords mSetNetDataSdkKeywords;
	IImportKeywordsCallback mSetNetDataCallback;

	@Override
	public void releaseBuildGrammarData() {

	}

	@Override
	public void retryImportOnlineKeywords() {

	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {

	}
	
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
	
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		if (mSpeechUnderstander == null || !bInitOk){
			LogUtil.loge("engine has not been inited ok !!!");
			oCallback.onError(IImportKeywordsCallback.ERROR_ENGINE_NOT_READY, oKeywords);
			return false;
		}
		mAsrCallBackProxy.onMonitor(MONITOR_INFO + MONITOR_UPLOAD);

		Runnable2<SdkKeywords, IImportKeywordsCallback> oRun = new Runnable2<SdkKeywords, IImportKeywordsCallback>(
				oKeywords, oCallback) {
			@Override
			public void run() {
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
					LogUtil.logd("importKeywordsContact: session_id="
							+ oKeywords.uint32SessionId);
					Map<Integer, List<String>> mapData = new HashMap<Integer, List<String>>();
					mapData.put(SpeechConstants.UPLOAD_DATA_NAME, lstData);
					mSetNetDataSdkKeywords = oKeywords;
					mSetNetDataCallback = mP2;
					mSpeechUnderstander.uploadUserData(mapData);
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
					LogUtil.logd("importKeywordsOnlineUserData =" + key
							+ ", session_id=" + oKeywords.uint32SessionId);
					Map<Integer, List<String>> mapData = new HashMap<Integer, List<String>>();
					mapData.put(SpeechConstants.UPLOAD_DATA_APP, lstData);
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
		};
		AsrWakeupEngine.getEngine().runOnCompileBackGround(oRun, 0);

		return true;
	}

}
