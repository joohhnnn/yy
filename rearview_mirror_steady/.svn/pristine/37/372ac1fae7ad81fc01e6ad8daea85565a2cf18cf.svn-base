package com.txznet.txz.component.asr.mix.local;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.reserve.service.TXZContext;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
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
			onErrorofEngine(parseErrorCode(errorMSG));
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
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return errorCode;
	}

	private IInitCallback mInitCallback = null;
	private Context mContext = null;
	@Override
	public int initialize(IInitCallback oRun) {
		mInitCallback = oRun;
		mContext = new TXZContext(GlobalContext.get(), ProjectCfg.getYzsFileDir());
		mSpeechUnderstander = new SpeechUnderstander(mContext,
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		//默认关闭引擎LOG
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_DEBUG_SAVELOG, false);
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
		if (oOption.mBeginSpeechTime > 0 && eos < 700){//OneShot方式用户可能会习惯性停顿
			eos = 700;
		}
		JNIHelper.logd("eos : " + eos);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL,
				eos);
		mSpeechUnderstander
				.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, true);
		String grammar = "txzTag";

		// 判断是否是免唤醒识别
		mConfig.setBeginSpeechTime(oOption.mBeginSpeechTime);
		if(ProjectCfg.getOfflineAsrSaveData()){
			mConfig.setmUID(oOption.mUID);
			mConfig.setmServerTime(oOption.mServerTime);
			mConfig.setbServerTimeConfidence(oOption.bServerTimeConfidence);
			if(oOption.mVoiceID != 0){
				mConfig.setSaveDataPath("#"+oOption.mVoiceID);
			}
		}
		mHasSpeaking = false;
		
		//OneShot离线识别使用
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_RESULT_JSON, true); //先打开离线结果转json开关 
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_IGNORE_RESULT_TAG, "unk");
		
		//不播放beep音的时候补跳过6400字节
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
					//没有说话判断
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

}
