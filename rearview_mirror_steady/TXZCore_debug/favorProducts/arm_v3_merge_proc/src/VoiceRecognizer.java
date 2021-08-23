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

import android.text.TextUtils;

import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IAsrCallback;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource.Config;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.component.wakeup.IWakeup.WakeupOption;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.ExchangeHelper;
import com.txznet.txz.util.KeyWordFilter;
import com.txznet.txz.util.runnables.Runnable2;
import com.unisound.client.ErrorCode;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;

public class VoiceRecognizer implements IVoiceRecogition{
	
	private enum WorkStatus{ 
		STATUS_ASR,
		STATUS_WAKEUP,
		STATUS_IDLE
	};
	
	private WorkStatus mWorkStatus = WorkStatus.STATUS_IDLE;
	private SpeechUnderstander mRecognizer = null;
	private SpeechUnderstanderListener mRecognitionListener = new SpeechUnderstanderListener() {
		@Override
		public void onResult(int type, String jsonResult) {
			switch (type) {
			case SpeechConstants.ASR_RESULT_LOCAL:
				JNIHelper.logd("onFixResult =" + jsonResult);
				parseLocalResult(jsonResult);
				break;
			case SpeechConstants.ASR_RESULT_RECOGNITION:
				JNIHelper.logd("onRecognition =" + jsonResult);
				break;
			case SpeechConstants.WAKEUP_RESULT:
				JNIHelper.logd("onWakeupResult =" + jsonResult);
				parseWakeupResult(jsonResult);
				break;
			}
		}

		@Override
		public void onEvent(int type, int timeMs) {
			switch (type) {
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
			case SpeechConstants.ASR_EVENT_ENGINE_INIT_DONE:
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
			}
		}

		@Override
		public void onError(int type, String errorMSG) {
			JNIHelper.loge("onError " + type + " " + errorMSG);
			int errCode = parseErrorCode(errorMSG);
			if (errCode != IAsr.ERROR_SUCCESS){
				onErrorofEngine(errCode);
			}
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
				errorCode = IAsr.ERROR_CODE;
				break;
			case ErrorCode.ASR_SDK_FIX_COMPILE_ERROR:
				JNIHelper.loge("ASR_SDK_FIX_COMPILE_ERROR");
				onImportFixKeywords(false, errorCode);
				errorCode = IAsr.ERROR_SUCCESS;//不处理回调
				break;
			default:
				JNIHelper.logd("other errorCode : " + errorCode);
				errorCode = IAsr.ERROR_CODE;
				break;
			}
		} catch (Exception e) {
			JNIHelper.loge("parseErrorCode exception : " + e.toString());
			errorCode = IAsr.ERROR_CODE;
		}
		return errorCode;
	}
	
	private InitListener mInitListener = null;
	public void init(InitListener oListener){
		mInitListener = oListener;
		mRecognizer = new SpeechUnderstander(AppLogic.getApp(), ProjectCfg.getYunzhishengAppId(), ProjectCfg.getYunzhishengSecret());
		mRecognizer.setOption(SpeechConstants.ASR_SERVICE_MODE, SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		mRecognizer.setListener(mRecognitionListener);
		
		//默认关闭引擎LOG
		mRecognizer.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mRecognizer.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		
		JNIHelper.logd("init_asr_version SDK=" + mRecognizer.getVersion());
		JNIHelper.logd("init_asr_vesion ENGINE=" + mRecognizer.getFixEngineVersion());
		JNIHelper.logd("init_asr_begin");
		int nRet = mRecognizer.init("");
		if (nRet != 0){
			onInit(false);
		}
		JNIHelper.logd("init_asr_end nRet = " + nRet);
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
		mAudioSource = new TXZAudioSource(mConfig, ProjectCfg.mEnableAEC);
		mRecognizer.setAudioSource(mAudioSource);
		mRecognizer.loadCompiledJsgf("txzTag", GlobalContext.get().getApplicationInfo().dataDir + "/data/txz.dat");// 加载编译语法
		boolean bRet = loadLastGrammar();
		int delayTime = 0;
		if (bRet) {
			delayTime = 200;//load模型是异步的操作
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
	 * load上次grammar
	 */
	private boolean loadLastGrammar() {
		String grammarPath = AppLogic.getApp().getFilesDir().getPath() + "/YunZhiSheng/asrfix/jsgf_model/txzTag.dat";// 加载上一次生成的模型
		File file = new File(grammarPath);
		if (file.exists()) {
			JNIHelper.logd("load grammarPath = " + grammarPath);
			mRecognizer.loadGrammar("txzTag", grammarPath);
			return true;
		}
		return false;
	}
	
	private AsrOption mAsrOption = null;
	private Config mConfig = null;
	private TXZAudioSource mAudioSource = null;
	private boolean mHasSpeaking = false;
	
	public void start(AsrOption oOption){
		mAsrOption = oOption;
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
		
		//不播放beep音的时候补跳过6400字节
		if (mAsrOption.mPlayBeepSound) {
			mConfig.setSkipBytes(6400);
		}else {
			mConfig.setSkipBytes(0);
		}
		JNIHelper.logd("start_asr");
		mRecognizer.start(grammar);
		mWorkStatus = WorkStatus.STATUS_ASR;
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
	
	private void clean(){
		JNIHelper.logd("clean_asr");
		mRecognizer.cancel();
		mWorkStatus = WorkStatus.STATUS_IDLE;
	}
	
	private WakeupOption mWakeupOption = null;
	private List<String> mLastWkWordList = new ArrayList<String>();
	private Set<String> mDigitWords = new HashSet<String>();
	private boolean bSetWkWordDone = false;
	public void start(WakeupOption oOption){
		if (mWorkStatus != WorkStatus.STATUS_IDLE){
			JNIHelper.logw("start_wakeup work status : " + mWorkStatus);
			return;
		}
		mWakeupOption = oOption;
		JNIHelper.logd("start_wakeup");
		mRecognizer.setOption(SpeechConstants.ASR_OPT_WAKEUP_VAD_ENABLED, true);
		mRecognizer.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, false);
		mRecognizer.setOption(SpeechConstants.WAKEUP_OPT_THRESHOLD_VALUE,  -1.2f);
		mRecognizer.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, 100);
		mRecognizer.start("wakeup");
		mWorkStatus = WorkStatus.STATUS_WAKEUP;
	}
	
	public void stop(WakeupOption oOption){
		if (mWorkStatus != WorkStatus.STATUS_WAKEUP){
			JNIHelper.logw("stop_wakeup work status : " + mWorkStatus);
			return;
		}
		mWakeupOption = null;
		JNIHelper.logd("stop_wakeup");
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
		LogUtil.logd("setWakeupKeywords");
		List<String>WkWordList = Arrays.asList(keywords);
		if (checkWakeupWords(mLastWkWordList, WkWordList)) {
			LogUtil.logw("wakeup keywords not change");
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
			LogUtil.logd("mDigitWords size  : " + mDigitWords.size());
		}
		LogUtil.logd("bSetWkWordDone : " + bSetWkWordDone);
	}
	
	public void setWakeupThreshold(float val) {
		
	}

	public void enableVoiceChannel(boolean enable) {	
		if (mWorkStatus == WorkStatus.STATUS_ASR ){
			JNIHelper.logw("enableVoiceChannel_wakeup work status : " + mWorkStatus);
			return;
		}
		JNIHelper.logd("enableVoiceChannel_wakeup enable : " + enable);
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
	// {"local_asr":[{"result_type":"full","score":-2.39,"recognition_result":"  你好小踢   ","engine_mode":"wakeup"}]}
	public boolean parseWakeupResult(String jsonResult) {
		LogUtil.logd("jsonResult : " + jsonResult);
		String rawText = "";
		float score = 0.0f;
		try {
			JSONObject json = new JSONObject(jsonResult);
			JSONArray jsonArray = json.getJSONArray("local_asr");
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			rawText = jsonObject.getString("recognition_result");
			mWakeupText = rawText.replace(" ", "");//去掉空格
			//替换成包含数字的原串
			do {
				if (mDigitWords.size() > 0) {
					//不包含中文数字,不执行比较
					if (!ExchangeHelper.hasChineseDigit(mWakeupText)){
						break;
					}
					String digitWord = ExchangeHelper.toDigit(mWakeupText, true);
					//转换失败,或者空串不执行比较
					if (TextUtils.isEmpty(digitWord)) {
						break;
					}
					for (String word : mDigitWords) {
						String strDigit = ExchangeHelper.toDigit(word, true);
						//转换失败,或者空串不执行比较
						if (TextUtils.isEmpty(strDigit)){
							continue;
						}
						if (TextUtils.equals(digitWord, strDigit)) {
							mWakeupText = word;
							break;
						}
					}
				}
			} while (false);
			
			score = (float) jsonObject.getDouble("score");
			mWakeupScore = score;
			mWakeupTime = jsonObject.getInt("utteranceTime");
			return true;
		} catch (Exception e) {
			JNIHelper.loge("parseWakeupRawText : " + e.toString());
			mWakeupTime = 0;
		}
		return false;
	}
	
	public static final double DOUBLE = 0.00000001;
	private void onSpeech(final boolean bSpeaking) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					if (mWorkStatus == WorkStatus.STATUS_ASR) {
						AsrOption oOption = mAsrOption;
						IAsrCallback callBack = null;
						if (oOption == null) {
							return;
						}
						callBack = oOption.mCallback;
						if (callBack == null) {
							return;
						}
						JNIHelper.logd("bSpeaking : " + bSpeaking);
						if (bSpeaking) {
							mHasSpeaking = true;
							callBack.onSpeechBegin(oOption);
						} else {
							callBack.onSpeechEnd(oOption);
						}
					}else if (mWorkStatus == WorkStatus.STATUS_WAKEUP){
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
				} catch (Exception e) {

				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onVolume(final int vol) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					if (mWorkStatus == WorkStatus.STATUS_ASR) {
						AsrOption oOption = mAsrOption;
						IAsrCallback callBack = null;
						if (oOption == null) {
							return;
						}
						callBack = oOption.mCallback;
						if (callBack == null) {
							return;
						}
						callBack.onVolume(oOption, vol);
					} else if (mWorkStatus == WorkStatus.STATUS_WAKEUP) {
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
				} catch (Exception e) {

				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void onStart() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					AsrOption oOption = mAsrOption;
					IAsrCallback callBack = null;
					if (oOption == null) {
						return;
					}
					callBack = oOption.mCallback;
					if (callBack == null){
						return;
					}
					callBack.onStart(oOption);
				} catch (Exception e) {

				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void onEnd() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					AsrOption oOption = mAsrOption;
					IAsrCallback callBack = null;
					if (oOption == null) {
						return;
					}
					callBack = oOption.mCallback;
					if (callBack == null){
						return;
					}
					callBack.onEnd(oOption);
				} catch (Exception e) {

				}
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
	
	private void onSuccess(final float score, final String jsonResult) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					AsrOption oOption = mAsrOption;
					IAsrCallback callBack = null;
					if (oOption == null) {
						return;
					}
					callBack = oOption.mCallback;
					if (callBack == null){
						return;
					}
					//没有说话判断
					if (score > -20 - DOUBLE && score < -20 + DOUBLE){
						int error2 = mHasSpeaking ? IAsr.ERROR_NO_MATCH : IAsr.ERROR_NO_SPEECH;
						callBack.onError(oOption, 0, null, null, error2);
						return;
					}
					//有识别结果
					VoiceParseData oVoiceParseData = new VoiceParseData();
					oVoiceParseData.floatResultScore = score;
					oVoiceParseData.strVoiceData = jsonResult;
					oVoiceParseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON;
					if (oOption != null) {
						if (oOption.mManual) {
							oVoiceParseData.boolManual = 1;
						} else {
							oVoiceParseData.boolManual = 0;
						}
						oVoiceParseData.uint32Sence = oOption.mGrammar;
					}
					clean();
					callBack.onSuccess(oOption, oVoiceParseData);
					
				} catch (Exception e) {

				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void onErrorofEngine(final int errCode) {
		JNIHelper.loge("onErrorOfEngine : " + errCode);
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					AsrOption oOption = mAsrOption;
					IAsrCallback callBack = null;
					if (oOption == null) {
						return;
					}
					callBack = oOption.mCallback;
					if (callBack == null){
						return;
					}
					callBack.onError(oOption, 0, null, null, errCode);
				} catch (Exception e) {

				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
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
						onSuccess(score, result);
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
	
	private SdkKeywords mSetDataSdkKeywords = null;
	private IImportKeywordsCallback mSetDataCallback = null;
	private void onImportFixKeywords(boolean bSuccessed, int code){
		LogUtil.logd("onImportFixKeywords bSuccessed : " + bSuccessed + ", code : " + code);
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
			LogUtil.loge("onImportFixKeywords oCallback = null");
		}
	}
	
	public void importKeywords(SdkKeywords oKeywords, IImportKeywordsCallback oCallback) {
		int error = 0;
		if (oKeywords.strType.startsWith("<")) {
			String key = oKeywords.strType.substring(1, oKeywords.strType.length() - 1);
			JNIHelper.logd("importKeywords sKey = " + key + ", session_id=" + oKeywords.uint32SessionId);
			do {
				// 过滤引擎不支持的tag
				if (key.contains("callPrefix") || key.contains("callSuffix")) {
					JNIHelper.logd("importKeywords filter unsupported slot");
					break;
				}

				String[] aContents = null;
				if (oKeywords.rptStrKw != null) {
					aContents = oKeywords.rptStrKw;
				} else {
					String strContents = oKeywords.strContent;
					if (TextUtils.isEmpty(strContents)) {
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
						JNIHelper.logd("ignore special keyword : " + content);
						continue;
					}
					KeyNamesList.add(content);
				}

				if (KeyNamesList.isEmpty()) {
					break;
				}
				JNIHelper.logd("insertVocab " + key);
				int nRet = 0;
				nRet = mRecognizer.insertVocab(KeyNamesList, "txzTag#" + key);
				JNIHelper.logd("insertVocab[" + key + "] : nRet = " + nRet);
				error = nRet;
				//insertVocab现在是异步接口
				mSetDataCallback = oCallback;
				mSetDataSdkKeywords = oKeywords;
				return;
			} while (false);

			if (error != 0) {
				if (oCallback != null) {
					oCallback.onError(error, oKeywords);
				}
			} else {
				if (oCallback != null) {
					oCallback.onSuccess(oKeywords);
				}
			}
		} else {
			// 非云知声离线识别词表
			if (oCallback != null) {
				oCallback.onSuccess(oKeywords);
			}
		}
	}
	
}
