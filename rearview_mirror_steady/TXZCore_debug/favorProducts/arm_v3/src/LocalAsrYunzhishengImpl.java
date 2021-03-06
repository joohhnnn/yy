package com.txznet.txz.component.asr.mix.local;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource;
import com.txznet.txz.component.asr.mix.audiosource.TXZAudioSource.Config;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.KeyWordFilter;
import com.txznet.txz.util.runnables.Runnable2;
import com.unisound.client.ErrorCode;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;

public class LocalAsrYunzhishengImpl implements IAsr {
	private Config mConfig = null;
	private TXZAudioSource mAudioSource = null;
	private SpeechUnderstander mSpeechUnderstander = null;
	private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {
		@Override
		public void onResult(int type, String jsonResult) {
			switch (type) {
			case SpeechConstants.ASR_RESULT_LOCAL:
				JNIHelper.logd("onFixResult =" + jsonResult);
				parseLocalResult(jsonResult);
				break;
			}
		}

		@Override
		public void onEvent(int type, int timeMs) {
			switch (type) {
			case SpeechConstants.ASR_EVENT_SPEECH_DETECTED:
				JNIHelper.logd("ASR_EVENT_SPEECH_DETECTED");
				onSpeech(true);
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_END:
				JNIHelper.logd("ASR_EVENT_SPEECH_END");
				onSpeech(false);
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
			case SpeechConstants.ASR_EVENT_RECORDING_STOP:
				JNIHelper.logd("ASR_EVENT_RECORDING_STOP");
				onEnd();
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

	// {"errorCode":-91002,"errorMsg":"?????????????????????"}
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
				errorCode = IAsr.ERROR_SUCCESS;//???????????????
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

	private IInitCallback mInitCallback = null;

	@Override
	public int initialize(IInitCallback oRun) {
		mInitCallback = oRun;
		mSpeechUnderstander = new SpeechUnderstander(GlobalContext.get(),
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		//??????????????????LOG
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
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
		mAudioSource = new TXZAudioSource(mConfig, ProjectCfg.mEnableAEC);
		mSpeechUnderstander.setAudioSource(mAudioSource);
		mSpeechUnderstander.loadCompiledJsgf("txzTag", GlobalContext.get()
				.getApplicationInfo().dataDir + "/data/txz.dat");// ??????????????????
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
		String grammarPath = GlobalContext.get().getApplicationContext()
				.getFilesDir().getAbsolutePath()
				+ "/YunZhiSheng/asrfix/jsgf_model/txzTag.dat";// ??????????????????????????????
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

//	private final static String sFixSaveData = Environment
//			.getExternalStorageDirectory().getPath() + "/txz/fix.pcm";
//
//	private void delectLastData() {
//		File f = new File(sFixSaveData);
//		if (f.exists()) {
//			f.delete();
//		}
//	}

	private AsrOption mAsrOption = null;

	@Override
	public int start(AsrOption oOption) {
		mAsrOption = oOption;
		mSpeechUnderstander.setOption(SpeechConstants.ASR_VAD_TIMEOUT_FRONTSIL,
				mAsrOption.mBOS);
		
		int eos = 1000;
		try{
			eos = mAsrOption.mEOS;
		}catch(Exception e){
			
		}
		if (oOption.mBeginSpeechTime > 0 && eos < 700){//OneShot????????????????????????????????????
			eos = 700;
		}
		JNIHelper.logd("eos : " + eos);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL,
				eos);
		mSpeechUnderstander
				.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, true);
		String grammar = "txzTag";

		// ??????????????????????????????
		mConfig.setBeginSpeechTime(oOption.mBeginSpeechTime);
		mHasSpeaking = false;
		
		//OneShot??????????????????
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, true); //????????????????????????json?????? 
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_IGNORE_RESULT_TAG, "unk");
		
		//?????????beep?????????????????????6400??????
		if (mAsrOption.mPlayBeepSound) {
			mConfig.setSkipBytes(6400);
		}else {
			mConfig.setSkipBytes(0);
		}
		
		mSpeechUnderstander.start(grammar);
		
		return 0;
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
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					if (mAsrOption == null || null == mAsrOption.mCallback) {
						return;
					}
					JNIHelper.logd("bSpeaking : " + bSpeaking);
					if (bSpeaking) {
						mHasSpeaking = true;
						mAsrOption.mCallback.onSpeechBegin(mAsrOption);
					} else {
						mAsrOption.mCallback.onSpeechEnd(mAsrOption);
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
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onVolume(mAsrOption, vol);
				}
			}

		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void onStart() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onStart(mAsrOption);
				}
			}

		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void onEnd() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onEnd(mAsrOption);
				}
			}

		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void onSuccess(final float score, final String jsonResult) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					//??????????????????
					if (score > -20 - DOUBLE && score < -20 + DOUBLE){
						int error2 = mHasSpeaking ? IAsr.ERROR_NO_MATCH : IAsr.ERROR_NO_SPEECH;
						mAsrOption.mCallback.onError(mAsrOption, 0, null, null, error2);
						return;
					}
					
					//
					VoiceParseData oVoiceParseData = new VoiceParseData();
					oVoiceParseData.floatResultScore = score;
					oVoiceParseData.strVoiceData = jsonResult;
					oVoiceParseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON;
					AsrOption oOption = mAsrOption;
					if (oOption != null) {
						if (oOption.mManual) {
							oVoiceParseData.boolManual = 1;
						} else {
							oVoiceParseData.boolManual = 0;
						}
						oVoiceParseData.uint32Sence = oOption.mGrammar;
					}
					mAsrOption.mCallback.onSuccess(mAsrOption, oVoiceParseData);
				}
			}

		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void onErrorofEngine(final int errCode) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onError(mAsrOption, 0, null, null,
							errCode);
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
						String result = jsonObject
								.getString("recognition_result");
						onSuccess(score, result);
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

	private SdkKeywords mSetDataSdkKeywords = null;
	private IImportKeywordsCallback mSetDataCallback = null;
	private void onImportFixKeywords(boolean bSuccessed, int code){
		LogUtil.logd("onImportFixKeywords bSuccessed : " + bSuccessed + ", code : " + code);
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
			LogUtil.loge("onImportFixKeywords oCallback = null");
		}
	}
	
	public boolean importKeywords(SdkKeywords oKeywords, IImportKeywordsCallback oCallback) {
		int error = 0;
		if (oKeywords.strType.startsWith("<")) {
			String key = oKeywords.strType.substring(1, oKeywords.strType.length() - 1);
			JNIHelper.logd("importKeywords sKey = " + key + ", session_id=" + oKeywords.uint32SessionId);
			do {
				// ????????????????????????tag
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
				nRet = mSpeechUnderstander.insertVocab(KeyNamesList, "txzTag#" + key);
				JNIHelper.logd("insertVocab[" + key + "] : nRet = " + nRet);
				error = nRet;
				//insertVocab?????????????????????
				mSetDataCallback = oCallback;
				mSetDataSdkKeywords = oKeywords;
				return true;
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
			// ??????????????????????????????
			if (oCallback != null) {
				oCallback.onSuccess(oKeywords);
			}
		}
		
		return true;
	}
}
