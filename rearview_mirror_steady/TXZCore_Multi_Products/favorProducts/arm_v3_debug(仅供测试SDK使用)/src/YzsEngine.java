package com.txznet.txz.component.asr.txzasr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Environment;

import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZConfigManager.AsrServiceMode;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrAndWakeupIIintCallback;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrState;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.util.KeyWordFilter;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;
import com.unisound.client.ErrorCode;
import com.unisound.client.IAudioSource;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
import com.unisound.client.SpeechUnderstanderListener;

@SuppressLint("UseSparseArrays")
public class YzsEngine implements IEngine{
	SpeechUnderstander mMixSpeechUnderstander;
	private AsrState mAsrState = AsrState.ASR_IDLE;

	private void setAsrState(AsrState asrState) {
//		synchronized (YzsEngine.class) {
			mAsrState = asrState;
//		}
	}

	public AsrState getAsrState() {
//		synchronized (YzsEngine.class) {
			return mAsrState;
//		}
	}

	private boolean mAutoRun = false;

	public void enableAutoRun(boolean enable) {
		mAutoRun = enable;
		JNIHelper.logd("mAutoRun = " + mAutoRun);
	}

	private int mFixError = 0;
	private int mNetError = 0;
	// private StringBuilder mNetResponseStr;
	private String mFixResult = null;
	private String mNetResult = null;
	private boolean bAsrHasSpeech = false;

	private void clear() {
		mFixError = 0;
		mNetError = 0;
		// mNetResponseStr = new StringBuilder();
		mFixResult = null;
		mNetResult = null;
		bAsrHasSpeech = false;
	}

	private void onAbort(final int abort) {
		// ????????????????????????
		if (mAsrState == AsrState.ASR_WAKEUP) {
			mAsrState = AsrState.ASR_IDLE;
			mMixSpeechUnderstander.stop();
			mMixSpeechUnderstander.cancel();
			JNIHelper.logw("????????????????????????,????????????3s????????????");
			WakeupManager.getInstance().startDelay(3000);
			return;
		}
		if (mAsrOption != null && mAsrOption.mCallback != null) {
			asrEnd();
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					mAsrOption.mCallback.onAbort(mAsrOption, abort);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}
	}

	private void doLocalResult(String jsonResult) {
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

						/*
						 * ?????????????????????????????????????????????????????????????????? ?????????????????????????????????,????????????????????????????????????
						 */
						if (mAsrMode == SpeechConstants.ASR_SERVICE_MODE_LOCAL
								|| score >= -5.0 || mNetError != 0) {
							fixLocalResult(score, result);
						} else {
							/*
							 * ??????????????????????????? ??????????????????SpeechConstants.
							 * ASR_RESULT_RECOGNITION????????????
							 */
							mFixResult = result;
						}

					} while (false);

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void fixLocalResult(float score, String jsonResult) {
		JNIHelper.logd("score = " + score + ":" + "jsonResult =" + jsonResult);
		asrEnd();
		do {

			if (mAsrOption != null && null != mAsrOption.mCallback) {
				if (score + DOUBLE > -20.0 && score - DOUBLE < -20.0) {
					int error2 = bAsrHasSpeech ? IAsr.ERROR_NO_MATCH
							: IAsr.ERROR_NO_SPEECH;
					mAsrOption.mCallback.onError(mAsrOption, 0, null, null,
							error2);
					break;
				}

				VoiceData.VoiceParseData voice = new VoiceData.VoiceParseData();
				voice.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_LOCAL_JSON;
				voice.strVoiceData = jsonResult;
				if (mAsrOption.mManual) {
					voice.boolManual = 1;
				} else {
					voice.boolManual = 0;
				}
				voice.uint32Sence = mAsrOption.mGrammar;

				final VoiceParseData voiceData = voice;
				Runnable oRun = new Runnable() {
					@Override
					public void run() {
						if (mAsrOption != null && mAsrOption.mCallback != null) {
							mAsrOption.mCallback.onSuccess(mAsrOption,
									voiceData);
						}
					}
				};
				AppLogic.runOnBackGround(oRun, 0);
			}
		} while (false);
	}

	private void doNetResult(String jsonResult) {
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
						if (null == jsonArray || 0 == jsonArray.length()) {
							break;
						}
						jsonObject = jsonArray.getJSONObject(0);
						if (null == jsonObject || 0 == jsonObject.length()) {
							break;
						}
						fixNetResult(jsonObject);
						return;
					} while (false);
					JNIHelper.loge("asr nlu result data is incomplete : " + jsonResult);
					fixError(IAsr.ERROR_ASR_NET_NLU_EMTPY);
				} catch (JSONException e) {
					JNIHelper.loge("asr result parse exception : " + e.toString());
					fixError(IAsr.ERROR_ASR_NET_NLU_EMTPY);
				}
			}
		}
	}

	private void fixNetResult(JSONObject json) {
		if (json == null) {
			return;
		}
		String jsonResult = json.toString();
		JNIHelper.logd("jsonResult =" + jsonResult);

		asrEnd();
		mNetResult = jsonResult;
		if (mAsrOption != null && null != mAsrOption.mCallback) {
			if (IsEmptySenceRecogine(json)) {
				JNIHelper.logd("nospeech");

				Runnable oRun = new Runnable() {
					@Override
					public void run() {
						if (mAsrOption != null && mAsrOption.mCallback != null) {
							mAsrOption.mCallback.onError(mAsrOption, 0, null,
									null, IAsr.ERROR_NO_SPEECH);
						}
					}
				};
				AppLogic.runOnBackGround(oRun, 0);
				return;
			}

			VoiceData.VoiceParseData voice = new VoiceData.VoiceParseData();
			voice.uint32DataType = VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON;
			voice.strVoiceData = jsonResult;
			if (mAsrOption.mManual) {
				voice.boolManual = 1;
			} else {
				voice.boolManual = 0;
			}
			voice.uint32Sence = mAsrOption.mGrammar;
			final VoiceParseData voiceData = voice;
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					if (mAsrOption != null && mAsrOption.mCallback != null) {
						mAsrOption.mCallback.onSuccess(mAsrOption, voiceData);
					}
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}
	}
    
	private void fixError(final int errCode){
		JNIHelper.logd("errCode : " + errCode);
		asrEnd();
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					mAsrOption.mCallback.onError(mAsrOption, 0, null,
							null, errCode);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	/*
	 * ??????????????????????????????????????????
	 */
	private boolean IsEmptySenceRecogine(JSONObject json) {
		boolean bRet = false;
		if (json == null) {
			return true;
		}
		int rc = 0;
		try {
			rc = json.getInt("rc");
			if (rc == 2) {
				bRet = true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			bRet = false;
		}
		return bRet;
	}

	public static final double DOUBLE = 0.00000001;

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

	IWakeupCallback mWakeupCallback;
	private String mWakeupText = null;
	private int mWakeupTime = 0;

	// {"local_asr":[{"result_type":"full","score":-2.39,"recognition_result":"  ????????????   ","engine_mode":"wakeup"}]}
	public boolean parseWakeupRawText(String jsonResult) {
		JNIHelper.logd("WakeupResult : " + jsonResult);
		String rawText = "";
		try {
			JSONObject json = new JSONObject(jsonResult);
			JSONArray jsonArray = json.getJSONArray("local_asr");
			JSONObject jsonObject = jsonArray.getJSONObject(0);
			rawText = jsonObject.getString("recognition_result");
			mWakeupText = rawText.trim();
			mWakeupTime = jsonObject.getInt("utteranceTime");
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			mWakeupTime = 0;
		}
		return false;
	}

	private void onSpeech(final boolean bSpeaking) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					if (mAsrState == AsrState.ASR_WAKEUP
							|| mAsrState == AsrState.ASR_RECORDING) {
						if (mWakeupCallback == null) {
							return;
						}
						if (bSpeaking) {
							mWakeupCallback.onSpeechBegin();
						} else {
							mWakeupCallback.onSpeechEnd();
						}
					}
					if (mAsrState == AsrState.ASR_RECOGNIZE) {
						if (mAsrOption != null && null != mAsrOption.mCallback) {
							if (bSpeaking) {
								bAsrHasSpeech = true;
								mAsrOption.mCallback.onSpeechBegin(mAsrOption);
							} else {
								mAsrOption.mCallback.onSpeechEnd(mAsrOption);
							}
						}
					}
				} catch (Exception e) {
					JNIHelper.loge("onSpeech : " + e.toString());
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}

	private void onVolume(int vol) {
		final int volume = vol;

		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				try {
					if (mAsrState == AsrState.ASR_WAKEUP
							|| mAsrState == AsrState.ASR_RECORDING) {
						if (mWakeupCallback == null) {
							return;
						}
						mWakeupCallback.onVolume(volume);

					}
					if (mAsrState == AsrState.ASR_RECOGNIZE) {
						if (mAsrOption != null && null != mAsrOption.mCallback) {
							mAsrOption.mCallback.onVolume(mAsrOption, volume);
						}
					}
				} catch (Exception e) {
					JNIHelper.loge("onVolume : " + e.toString());
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
    
	private void doOtherError(){
		asrEnd();
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && null != mAsrOption.mCallback) {
					mAsrOption.mCallback.onError(mAsrOption, 0, null, null,
							IAsr.ERROR_CODE);
				}

			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void doNetRequstError() {
		boolean bEnd = false;
		if (mAsrSrvMode == SpeechConstants.ASR_SERVICE_MODE_NET) {
			bEnd = true;
		} else if (mAsrSrvMode == SpeechConstants.ASR_SERVICE_MODE_MIX) {
			if (mFixResult != null) {
				float score = 0.0F;
				try {
					JSONObject json = new JSONObject(mFixResult);
					JSONArray jsonArray = json.getJSONArray("c");
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					score = (float) getDouble(jsonObject, "score");
					if (score < -10.0) {
						bEnd = true;
					}
				} catch (Exception e) {
				}
			}
		}
		if (bEnd) {
			asrEnd();
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					if (mAsrOption != null && null != mAsrOption.mCallback) {
						mAsrOption.mCallback.onError(mAsrOption, mNetError, null, null,
								IAsr.ERROR_ASR_NET_REQUEST);
					}

				}
			};
			AppLogic.runOnBackGround(oRun, 0);
		}
	}
	
	// {"errorCode":-91002,"errorMsg":"?????????????????????"}
	private void parseErrorCode(String errorMsg) {
		JSONObject json = null;
		int errorCode = 0;
		try {
			json = new JSONObject(errorMsg);
			errorCode = json.getInt("errorCode");
			switch (errorCode) {
			//case ErrorCode.HTTP_LOGIN_ERROR:
			case -90002://{"errorCode":-90002,"errorMsg":"?????????????????????"}
			case -91883:
			case ErrorCode.SEND_REQUEST_ERROR:
			case ErrorCode.REQ_INIT_ERROR:
				mNetError = errorCode;
				break;
			case ErrorCode.ASR_SDK_FIX_RECOGNIZER_NO_INIT:
				mFixError = errorCode;
				break;
			case ErrorCode.GENERAL_INIT_ERROR:
				mNetError = mFixError = errorCode;
				break;
			case ErrorCode.FAILED_START_RECORDING:
			case ErrorCode.RECORDING_EXCEPTION:
				onAbort(ErrorCode.RECORDING_EXCEPTION);
				break;
			case ErrorCode.UPLOAD_USER_DATA_TOO_FAST:
				mUploadUserDataTooFast = true;
			case ErrorCode.UPLOAD_USER_DATA_EMPTY:
			case ErrorCode.UPLOAD_USER_DATA_NETWORK_ERROR:
			case ErrorCode.UPLOAD_USER_DATA_SERVER_REFUSED:
				JNIHelper.loge("ASR_EVENT_USERDATA_UPLOADED_ERROR");
				AppLogic.removeBackGroundCallback(mRunnableImportOnlineKeywords);
				AppLogic.runOnBackGround(mRunnableImportOnlineKeywords,
						5 * 60 * 1000);
				IImportKeywordsCallback setNetDataCallback = mSetNetDataCallback;
				mSetNetDataCallback = null;
				if (setNetDataCallback != null) {
					setNetDataCallback.onError(errorCode,
							mSetNetDataSdkKeywords);
				}
				break;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	public void asrEnd() {
		synchronized (YzsEngine.class) {
			JNIHelper.logd("AsrEnd");
			mAsrState = AsrState.ASR_IDLE;
			mMixSpeechUnderstander.cancel();
			// saveWav(mPcmFileCount);
			AppLogic.removeUiGroundCallback(oRecordTimeOutRun);// ???????????????????????????
		}
	}

	private boolean mUploadUserDataTooFast = false;
	private Runnable mRunnableImportOnlineKeywords = new Runnable() {
		@Override
		public void run() {
			mUploadUserDataTooFast = false;
			JNIHelper.logd("retryImportOnlineKeywords while too fast timeout");
			JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
					VoiceData.SUBEVENT_VOICE_RETRY_ONLINE_KEYWORDS);
		}
	};

	public void retryImportOnlineKeywords() {
		if (mUploadUserDataTooFast)
			return;
		AppLogic.removeBackGroundCallback(mRunnableImportOnlineKeywords);
		JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_RETRY_ONLINE_KEYWORDS);
	}

	private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {
		@Override
		public void onResult(int type, String jsonResult) {
			switch (type) {
			case SpeechConstants.WAKEUP_RESULT:
				parseWakeupRawText(jsonResult);
				break;
			case SpeechConstants.ASR_RESULT_LOCAL:
				JNIHelper.logd("onFixResult =" + jsonResult);
				doLocalResult(jsonResult);
				break;
			case SpeechConstants.ASR_RESULT_NET:
				JNIHelper.logd("onNetResult = " + jsonResult);
				doNetResult(jsonResult);
				break;
			case SpeechConstants.ASR_RESULT_RECOGNITION:
				JNIHelper.logd("RecognitionResult = " + jsonResult);
				/*
				 * ???????????????????????????????????????????????????????????????????????? ???????????????????????? 1????????????????????????????????????mFixResult?????????
				 * 2??????????????????????????????mNetResult???null
				 */
				if (mFixResult != null && mNetResult == null) {
					float score = 0.0F;
					try {
						JSONObject json = new JSONObject(mFixResult);
						JSONArray jsonArray = json.getJSONArray("c");
						JSONObject jsonObject = jsonArray.getJSONObject(0);
						score = (float) getDouble(jsonObject, "score");
					} catch (Exception e) {
					}
					fixLocalResult(score, mFixResult);
				}
				else if(mNetError == ErrorCode.SEND_REQUEST_ERROR || mNetError == -91883){
					doNetRequstError();
				}else{
					doOtherError();
				}
				break;
			}
		}

		@Override
		public void onEvent(int type, int timeMs) {
			switch (type) {
			case SpeechConstants.ASR_EVENT_CANCEL:
				JNIHelper.logd("ASR_EVENT_CANCEL");
				break;
			case SpeechConstants.ASR_EVENT_USERDATA_UPLOADED:
				JNIHelper.logd("ASR_EVENT_USERDATA_UPLOADED");
				mUploadUserDataTooFast = false;
				AppLogic.removeBackGroundCallback(mRunnableImportOnlineKeywords);
				IImportKeywordsCallback setNetDataCallback = mSetNetDataCallback;
				mSetNetDataCallback = null;
				if (setNetDataCallback != null) {
					setNetDataCallback.onSuccess(mSetNetDataSdkKeywords);
				}
				break;
//			case SpeechConstants.ASR_EVENT_COMPILE_DONE:
//				JNIHelper.logd("ASR_EVENT_COMPILE_DONE");
//				break;
			case SpeechConstants.ASR_EVENT_LOADGRAMMAR_DONE:
				JNIHelper.logd("ASR_EVENT_LOADGRAMMAR_DONE");
				break;
			case SpeechConstants.ASR_EVENT_VOCAB_INSERTED:
				JNIHelper.logd("ASR_EVENT_VOCAB_INSERTED");
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_PREPARED:
				JNIHelper.logd("ASR_EVENT_RECORDING_PREPARED");
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_START:
				JNIHelper.logd("ASR_EVENT_RECORDING_START");
				if (mAsrState == AsrState.ASR_RECOGNIZE) {
					// AsrManager.getInstance().stopWav();
					if (mAsrOption != null && null != mAsrOption.mCallback) {
						AppLogic.runOnUiGround(oRecordTimeOutRun,
								mAsrOption.mKeySpeechTimeout);// ???????????????????????????
						Runnable oRun = new Runnable() {
							@Override
							public void run() {
								//??????????????????????????????????????????????????????
								try {
									mAsrOption.mCallback.onStart(mAsrOption);
								} catch (Exception e) {

								}
							}
						};
						AppLogic.runOnBackGround(oRun, 0);
					}
				}
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_DETECTED:
				onSpeech(true);
				break;
			case SpeechConstants.ASR_EVENT_VAD_TIMEOUT:
				JNIHelper.logd("ASR_EVENT_VAD_TIMEOUT");
				break;
			case SpeechConstants.ASR_EVENT_SPEECH_END:
				onSpeech(false);
				break;
			case SpeechConstants.ASR_EVENT_RECORDING_STOP:
				JNIHelper.logd("ASR_EVENT_RECORDING_STOP");
				if (mAsrState != AsrState.ASR_RECOGNIZE) {
					break;
				}
				if (mAsrOption != null && null != mAsrOption.mCallback) {
					Runnable oRun = new Runnable() {
						@Override
						public void run() {
							mAsrOption.mCallback.onEnd(mAsrOption);
						}
					};
					AppLogic.runOnBackGround(oRun, 0);
				}
				break;
			case SpeechConstants.ASR_EVENT_LOCAL_END:
				JNIHelper.logd("onFixEnd");
				break;
			case SpeechConstants.ASR_EVENT_NET_END:
				// ??????????????????????????????????????????
				JNIHelper.logd("onNetEnd");
				break;
			case SpeechConstants.ASR_EVENT_RECOGNITION_END:
				JNIHelper.logd("ASR_EVENT_RECOGNITION_END");
				break;
			case SpeechConstants.ASR_EVENT_VOLUMECHANGE:
				Integer vol = (Integer) mMixSpeechUnderstander
						.getOption(SpeechConstants.GENERAL_UPDATE_VOLUME);
				if (null == vol) {
					return;
				}
				onVolume(vol);
				break;
			case SpeechConstants.ASR_EVENT_INIT_DONE:
				JNIHelper.logd("ASR_EVENT_ENGINE_INIT_DONE");
				onInit();
				break;
			case SpeechConstants.WAKEUP_EVENT_RECOGNITION_SUCCESS:
				JNIHelper.logd("WAKEUP_EVENT_RECOGNITION_SUCCESS");
				if (mWakeupCallback != null) {
					Runnable oRun = new Runnable() {
						@Override
						public void run() {
							synchronized (YzsEngine.class) {
								if (mWakeupCallback != null) {
									mWakeupCallback.onWakeUp(mWakeupText,mWakeupTime);
								}
							}
						}
					};
					AppLogic.runOnBackGround(oRun, 0);
				}
				break;
			case SpeechConstants.WAKEUP_EVENT_SET_WAKEUPWORD_DONE:
				JNIHelper.logd("WAKEUP_EVENT_SET_WAKEUPWORD_DONE");
				bInsertWakeupwordsing = false;
				break;
			default:
				break;
			}
		}

		@Override
		public void onError(int type, String errorMSG) {
			JNIHelper.loge("onError " + type + " " + errorMSG);
			switch (type) {
			case SpeechConstants.ASR_ERROR:
				parseErrorCode(errorMSG);	
				// ?????????????????????????????????????????????error
				if (mFixError != 0 && mNetError != 0) {
					if (mAsrOption != null && mAsrOption.mCallback != null) {
						asrEnd();
						Runnable oRun = new Runnable() {
							@Override
							public void run() {
								mAsrOption.mCallback.onAbort(mAsrOption, 0);
							}
						};
						AppLogic.runOnBackGround(oRun, 0);
					}
				}
				break;
			default:
				break;
			}
		}
	};

	private List<String> mLastWakeupKeywords = new ArrayList<String>();
	private boolean bInsertWakeupwordsing = false;

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

	public synchronized void setWakeupWords(List<String> keyWordList) {
		if (mMixSpeechUnderstander == null) {
			JNIHelper.logw("mMixSpeechUnderstander == null");
			return;
		}

		if (!bInitDone) {
			JNIHelper.loge("engine is initing, wait...");
			return;
		}
		//????????????????????????????????????
		if (mAsrState == AsrState.ASR_RECORDING  || mAsrState == AsrState.ASR_WAKEUP  || mAsrState == AsrState.ASR_RECOGNIZE) {
			JNIHelper.logw("mAsrState = " + mAsrState);
			return;
		}

		if (checkWakeupWords(mLastWakeupKeywords, keyWordList)) {
			JNIHelper.logw("wakeup keywords not change");
			return;
		}
		bInsertWakeupwordsing = true;
		JNIHelper.logd("setWakupWordIn");
		mMixSpeechUnderstander.setWakeupWord(keyWordList);
		JNIHelper.logd("setWakupWordOut");
		// ???????????????????????????????????????????????????100ms
		int nCount = 0;
		while (bInsertWakeupwordsing && nCount < 2000) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			nCount++;
		}
		// ??????????????????????????????
		if (!bInsertWakeupwordsing) {
			mLastWakeupKeywords = keyWordList;
		}
		bInsertWakeupwordsing = false;
	}

	//public static float WAKEUP_OPT_THRESHOLD = -3.1f;

	public void startWakeup(IWakeupCallback oCallback) {
		synchronized (YzsEngine.class) {
			if (mAsrState == AsrState.ASR_RECOGNIZE
					|| mAsrState == AsrState.ASR_WAKEUP
					|| mAsrState == AsrState.ASR_RECORDING) {
				JNIHelper.logw("mAsrState = " + mAsrState);
				return;
			}
			JNIHelper.logd("startWakeup");
			mWakeupCallback = oCallback;
			mAsrState = AsrState.ASR_WAKEUP;
			//?????????????????????????????????????????????????????????????????????,??????????????????????????????????????????????????????????????????
			if (!ProjectCfg.mUseHQualityWakeupModel){
				WakeupManager.getInstance().checkUsingAsr(new Runnable1<SpeechUnderstander>(mMixSpeechUnderstander) {
					@Override
					public void run() {
						mP1.setOption(
								SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID, SpeechConstants.AUTO_320_MODEL);
					}
				}, new Runnable1<SpeechUnderstander>(mMixSpeechUnderstander) {
					@Override
					public void run() {
						mP1.setOption(
								SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID, SpeechConstants.AUTO_128_MODEL);
					}
				});
			}else {
				mMixSpeechUnderstander.setOption(
						SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID, SpeechConstants.AUTO_320_MODEL);
			}
			// ????????????????????????json???????????????
			mMixSpeechUnderstander.setOption(
					SpeechConstants.ASR_OPT_RESULT_JSON, false);
			mMixSpeechUnderstander.setOption(
					SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, 10); //????????????????????????10ms?????????????????????
			mMixSpeechUnderstander.setOption(
					SpeechConstants.WAKEUP_OPT_THRESHOLD_VALUE,
					AsrWakeupEngine.WAKEUP_OPT_THRESHOLD);// ?????????????????????float??????
			// -3.0f
			mMixSpeechUnderstander.setOption(
					SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA, AsrWakeupEngine.NULL_FILE);//????????????(/dev/null)???????????????????????????CPU????????????????????????????????????????????????

//			mMixSpeechUnderstander.setOption(
//					SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID,
//					SpeechConstants.AUTO_128_MODEL);
//			mMixSpeechUnderstander.setOption(
//					SpeechConstants.ASR_OPT_RECOGNIZE_MODEL_ID,
//					SpeechConstants.AUTO_320_MODEL);

			// if (mAutoRun){
			// JNIHelper.logw("auto run asr!!!");
			// if (getAutoRunAudioSource() != null){
			// mMixSpeechUnderstander.setAudioSource(getAutoRunAudioSource());
			// }
			// }
			
			if (!ProjectCfg.mUseHQualityWakeupModel) {
				mMixSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID,
						CallManager.getInstance().isRinging() ? 13 : 11);
			}
			mMixSpeechUnderstander.start("wakeup");

		}
	}

	public void stopWakeup() {
		synchronized (YzsEngine.class) {
			if (mAsrState != AsrState.ASR_WAKEUP) {
				return;
			}

			JNIHelper.logd("stopWakeup");
			mAsrState = AsrState.ASR_IDLE;
			mWakeupCallback = null;
			JNIHelper.logd("stopWakeupIn");
			mMixSpeechUnderstander.cancel();
			JNIHelper.logd("stopWakeupOut");
		}
	}

	public int startWithRecord(IWakeupCallback oCallback) {
		synchronized (YzsEngine.class) {
			if (mAsrState != AsrState.ASR_IDLE) {
				JNIHelper.logw("mAsrState = " + mAsrState);
				return 0;
			}
			JNIHelper.logd("startWithRecord");
			mWakeupCallback = oCallback;
			mAsrState = AsrState.ASR_RECORDING;
			mMixSpeechUnderstander.setOption(
					SpeechConstants.ASR_OPT_RESULT_JSON, false);
			mMixSpeechUnderstander.setOption(
					SpeechConstants.WAKEUP_OPT_THRESHOLD_VALUE,
					AsrWakeupEngine.WAKEUP_OPT_THRESHOLD);// ?????????????????????float??????
			mMixSpeechUnderstander.setOption(
					SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA, "");
			mMixSpeechUnderstander.start("wakeup");
		}
		return 0;
	}

	public void stopWithRecord() {
		synchronized (YzsEngine.class) {
			if (mAsrState != AsrState.ASR_RECORDING) {
				JNIHelper.logw("mAsrState = " + mAsrState);
				return;
			}
			JNIHelper.logd("stopWithRecord");
			mAsrState = AsrState.ASR_IDLE;
			mWakeupCallback = null;
			mMixSpeechUnderstander.cancel();
		}
	}

	private boolean bDebuged = false;

	private boolean checkDebugFlag() {
		String debug = Environment.getExternalStorageDirectory().getPath()
				+ "/txz/txz_abc1234321.debug";
		boolean bRet = false;
		File f = new File(debug);
		if (f.exists()) {
			bRet = true;
		}
		return bRet;
	}

	private boolean bIniting = false;
	private boolean bInitDone = false;
	List<AsrAndWakeupIIintCallback> initCallBacks = new ArrayList<AsrAndWakeupIIintCallback>();

	public synchronized int initialize(final AsrAndWakeupIIintCallback oRun) {
		if (bInitDone) {
			AppLogic.runOnBackGround(new Runnable() {
				@Override
				public void run() {
					oRun.onInit(true);
				}
			}, 0);

			return 0;
		}

		initCallBacks.add(oRun);

		if (bIniting) {
			return 0;
		}
		bIniting = true;

		// AppID?????????
		if (ProjectCfg.getYunzhishengAppId() == null
				|| ProjectCfg.getYunzhishengSecret() == null
				|| ProjectCfg.getYunzhishengAppId().isEmpty()
				|| ProjectCfg.getYunzhishengSecret().isEmpty()) {
			mMixSpeechUnderstander = null;
			bInitDone = true;
			JNIHelper.logw("AppId or Secret is Empty!!!");
			Runnable run = new Runnable() {
				@Override
				public void run() {
					notifyInitDone();
				}
			};
			AppLogic.runOnBackGround(run, 0);
			return 0;
		}
		bDebuged = checkDebugFlag();

		mMixSpeechUnderstander = new SpeechUnderstander(GlobalContext.get(),
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mMixSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_MIX);
		mMixSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		// ????????????log??????
		mMixSpeechUnderstander.setOption(
				SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, bDebuged);
		mMixSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG,
				bDebuged);

		// ?????????????????????
		JNIHelper.logd("initAsrBegin");
		int nRet = 0;
		mMixSpeechUnderstander.init("");
		JNIHelper.logd("nRet = " + nRet);
		return 0;
	}

	/*
	 * ?????????????????????????????????
	 */
	private void onInit() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				doInit();
			}
		};
		AppLogic.runOnBackGround(oRun, 0);

	}

	/**
	 * ??????????????????
	 * 
	 * @param src
	 */
	public void setAudioSource(IAudioSource src) {
		mMixSpeechUnderstander.setAudioSource(src);
	}

	/*
	 * ??????????????????????????????????????????
	 */
	private IAudioSource mAudioSource = null;
	private synchronized void doInit() {
		if (ProjectCfg.mUseHQualityWakeupModel) {
			mMixSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID, 13);
		}
		mAudioSource = AudioSourceFactory.createAudioSource();
		mMixSpeechUnderstander.setAudioSource(mAudioSource);
		mMixSpeechUnderstander.loadCompiledJsgf("txzTag", GlobalContext.get()
				.getApplicationInfo().dataDir + "/data/txz.dat");// ??????????????????
		boolean bRet = loadLastGrammar();
		int delayTime = 0;
		if (bRet) {
			delayTime = 200;
		}
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				onInitEnd();
				notifyInitDone();
			}
		};
		AppLogic.runOnBackGround(oRun, delayTime);
	}

	private synchronized void onInitEnd() {
		insertVocab_ext_Inner(VoiceData.GRAMMAR_SENCE_INCOMING_MAKE_SURE,
				getAssetsFile("txzIncm.vocab"));
		bInitDone = true;// ??????????????????????????????,????????????????????????????????????????????????????????????????????????
	}

	private synchronized void notifyInitDone() {
		// ?????????????????????????????????
		for (int i = 0; i < initCallBacks.size(); ++i) {
			final AsrAndWakeupIIintCallback callback = initCallBacks.get(i);
			if (callback != null) {
				callback.onInit(true);
			}
		}
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
			mMixSpeechUnderstander.loadGrammar("txzTag", grammarPath);
			return true;
		}
		return false;
	}

	private Map<String, Long> mExtVocabMap = new HashMap<String, Long>();

	public void insertVocab_ext(final int nGrammar, final StringBuffer vocab) {
		AppLogic.runOnBackGround(new Runnable() {
			// AsrWakeupEngine.getEngine().runOnBackGround(new Runnable() {
			@Override
			public void run() {
				insertVocab_ext_Inner(nGrammar, vocab);
				// try {
				// Thread.sleep(500);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
			}
		}, 0);
	}

	public void insertVocab_ext_Inner(int nGrammar, StringBuffer vocab) {
		synchronized (mExtVocabMap) {
			String strModelFile;
			String grammar = "txzSel";
			int nRet = -1;
			switch (nGrammar) {
			case VoiceData.GRAMMAR_SENCE_INCOMING_MAKE_SURE:
				grammar = "txzIncm";
				break;
			case VoiceData.GRAMMAR_SENCE_CALL_SELECT:
				grammar = "txzSel";
				break;
			case VoiceData.GRAMMAR_SENCE_CALL_MAKE_SURE:
				grammar = "txzSel";
				break;
			case VoiceData.GRAMMAR_SENCE_DEFAULT:
				return;
			default:
				grammar = "txzSel";
			}

			CRC32 crc32 = new CRC32();
			long oldCRC32Value = mExtVocabMap.get(grammar) == null ? 0
					: mExtVocabMap.get(grammar);
			crc32.update(vocab.toString().getBytes());
			long newCRC32Value = crc32.getValue();
			JNIHelper.logd("oldCRC32 = " + oldCRC32Value + " " + "newCRC32 = "
					+ newCRC32Value);
			if (oldCRC32Value == newCRC32Value) {
				return;
			}

			strModelFile = GlobalContext.get().getApplicationInfo().dataDir
					+ "/data/" + grammar + ".dat";
			JNIHelper.logd("compile model[" + strModelFile + "] ");
			nRet = mMixSpeechUnderstander.insertVocab_ext(grammar,
					strModelFile, vocab.toString());
			JNIHelper
					.logd("compile model[" + strModelFile + "] nRet = " + nRet);
			mExtVocabMap.put(grammar, newCRC32Value);
		}
	}

	public StringBuffer getAssetsFile(String fileName) {
		StringBuffer sb = new StringBuffer(1024 * 10);
		try {
			InputStream in = GlobalContext.get().getAssets().open(fileName);
			InputStreamReader reader = new InputStreamReader(in);
			BufferedReader bufferReader = new BufferedReader(reader);
			String line;
			while ((line = bufferReader.readLine()) != null) {
				sb.append(line).append("\n");
			}
			reader.close();
			in.close();
			return sb;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return sb;
	}

	public void release() {
		mMixSpeechUnderstander = null;
	}

	AsrOption mAsrOption;
	private int mAsrMode = 0;

	//
	private Runnable oRecordTimeOutRun = new Runnable() {
		@Override
		public void run() {
			if (mAsrState == AsrState.ASR_RECOGNIZE) {
				stopAsr();
			}
		}

	};

	private String getSavedAudioPath(long nVoiceID) {
		String recordPath = AsrWakeupEngine.AUDIO_SAVE_FILE_PREFIX + "/" + "txz_" + nVoiceID
				+ ".pcm";
		return recordPath;
	}

	/*
	 * private int mPcmFileCount = 0; private void saveWav(int n){ final int
	 * count = n; Runnable oRun = new Runnable(){
	 * 
	 * @Override public void run() { saveWavInner(count); } };
	 * //???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? runOnCompileBackGround(oRun,
	 * 0); }
	 * 
	 * 
	 * private void saveWavInner(int n){ //???PCM??????WAV
	 * JNIHelper.logd("saveWavIn"); String recordPath = AUDIO_SAVE_FILE_PREFIX +
	 * "/" + "txz_" + mPcmFileCount + ".pcm"; String wavPath =
	 * AUDIO_SAVE_FILE_PREFIX + "/" + "txz_" + mPcmFileCount + ".wav"; boolean
	 * bRet = Pcm2Wav.encode(recordPath, wavPath, 16*1000);
	 * JNIHelper.logd("saveWavOut bRet = " + bRet);
	 * 
	 * //?????????????????????????????????PCM??????????????????????????????????????????????????? mPcmFileCount++; if (mPcmFileCount >=
	 * MAX_RECORD_FILE_COUNT){ mPcmFileCount = 0; } String nextRecordPath =
	 * AUDIO_SAVE_FILE_PREFIX + "/" + "txz_" + mPcmFileCount + ".pcm"; File f =
	 * new File( nextRecordPath); if (f.exists()) { bRet = f.delete(); if
	 * (!bRet) { JNIHelper.logw("delete pcm fail!!!"); } }
	 * 
	 * String nextWavPath = AUDIO_SAVE_FILE_PREFIX + "/" + "txz_" +
	 * mPcmFileCount + ".wav"; File wav = new File(nextWavPath); if
	 * (wav.exists()) { bRet = wav.delete(); if (!bRet) {
	 * JNIHelper.logw("delete wav fail!!!"); } } }
	 */
    private int mAsrSrvMode = SpeechConstants.ASR_SERVICE_MODE_MIX;
	public synchronized int startAsr(AsrOption oOption) {
		if (mMixSpeechUnderstander == null) {
			JNIHelper.logw("mMixSpeechUnderstander == null");
			return -1;
		}

		AppLogic.removeUiGroundCallback(oRecordTimeOutRun);// ???????????????

		setAsrState(AsrState.ASR_RECOGNIZE);
		clear();
		mAsrOption = oOption;
		mMixSpeechUnderstander.setOption(SpeechConstants.ASR_NET_TIMEOUT, 5);
		mMixSpeechUnderstander.setOption(
				SpeechConstants.ASR_VAD_TIMEOUT_FRONTSIL, oOption.mBOS);
		mMixSpeechUnderstander.setOption(
				SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, oOption.mEOS);
		mMixSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_RESULT_JSON,
				true);

		oOption.mVoiceID = System.currentTimeMillis();
		mMixSpeechUnderstander.setOption(
				SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA,
				getSavedAudioPath(oOption.mVoiceID));
		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			JNIHelper.logd("strCity: " + location.msgGeoInfo.strCity);
			mMixSpeechUnderstander.setOption(SpeechConstants.GENERAL_CITY,
					location.msgGeoInfo.strCity);
		} catch (Exception e) {
		}

		try {
			LocationInfo location = LocationManager.getInstance()
					.getLastLocation();
			double lat = location.msgGpsInfo.dblLat;
			double lng = location.msgGpsInfo.dblLng;
			String strGpsInfo = lat + "," + lng;
			JNIHelper.logd("strGpsInfo: " + strGpsInfo);
			mMixSpeechUnderstander.setOption(SpeechConstants.GENERAL_GPS,
					strGpsInfo);
		} catch (Exception e) {
		}

		mMixSpeechUnderstander.setOption(SpeechConstants.ASR_DOMAIN, "poi,song,incar");
		mMixSpeechUnderstander.setOption(SpeechConstants.NLU_SCENARIO, "incar");

		String grammar = "txzTag";
		switch (oOption.mGrammar) {
		case VoiceData.GRAMMAR_SENCE_DEFAULT:
			grammar = "txzTag";
			break;
		case VoiceData.GRAMMAR_SENCE_INCOMING_MAKE_SURE:
			grammar = "txzIncm";
			break;
		case VoiceData.GRAMMAR_SENCE_CALL_SELECT:
			grammar = "txzTag";
			break;
		case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL:
			grammar = "txzTag";
			break;
		case VoiceData.GRAMMAR_SENCE_CALL_MAKE_SURE:
			grammar = "txzTag";
			break;
		default:
			grammar = "txzTag";
			break;
		}
		JNIHelper.logd("grammar = " + grammar);
		mAsrMode = SpeechConstants.ASR_SERVICE_MODE_MIX;
		if (grammar.equals("txzIncm")) {
			mAsrMode = SpeechConstants.ASR_SERVICE_MODE_LOCAL;
		}
		// if (mAutoRun){
		// JNIHelper.logw("auto run asr!!!");
		// if (getAutoRunAudioSource() != null){
		// mMixSpeechUnderstander.setAudioSource(getAutoRunAudioSource());
		// }
		// }
		if (AsrManager.getInstance().getAsrSvrMode() == AsrServiceMode.ASR_SVR_MODE_AUTO || ProjectCfg.RecognOnline()) {
			if (NetworkManager.getInstance().hasNet() || ProjectCfg.RecognOnline()) {
				mAsrSrvMode = SpeechConstants.ASR_SERVICE_MODE_NET;
				mMixSpeechUnderstander.setOption(
						SpeechConstants.ASR_SERVICE_MODE,
						 mAsrSrvMode);
				mMixSpeechUnderstander.init("");
			} else {
				mAsrSrvMode = SpeechConstants.ASR_SERVICE_MODE_MIX;
				mMixSpeechUnderstander.setOption(
						SpeechConstants.ASR_SERVICE_MODE,
						mAsrSrvMode);
				mMixSpeechUnderstander.init("");
			}
		}
		mMixSpeechUnderstander.start(grammar);
		return 0;
	}

	public void stopAsr() {
		if (mMixSpeechUnderstander == null) {
			JNIHelper.logw("mMixSpeechUnderstander == null");
			return;
		}
		JNIHelper.logd("stop");
		mMixSpeechUnderstander.stop();
		AppLogic.removeUiGroundCallback(oRecordTimeOutRun);// ???????????????????????????
	}

	/*
	 * ????????????
	 * 
	 * @see com.txznet.txz.component.asr.IAsr#cancel()
	 */
	public void cancelAsr() {
		if (mMixSpeechUnderstander == null) {
			JNIHelper.logw("mMixSpeechUnderstander == null");
			return;
		}

		synchronized (YzsEngine.class) {
			JNIHelper.logd("cancel");
			if (mAsrState != AsrState.ASR_RECOGNIZE) {
				JNIHelper.logd("current state is not recogine, no need cancel");
				return;
			}

			mAsrState = AsrState.ASR_IDLE;
			mMixSpeechUnderstander.cancel();
			AppLogic.removeUiGroundCallback(oRecordTimeOutRun);// ???????????????

			if (mAsrOption != null && null != mAsrOption.mCallback) {
				mAsrOption.mCallback.onCancel(mAsrOption);
			}
		}
	}

	public boolean isBusy() {
		return AsrState.ASR_RECOGNIZE == mAsrState; // ||
													// mMixSpeechUnderstander.isRunning()
	}

	SdkKeywords mSetNetDataSdkKeywords;
	IImportKeywordsCallback mSetNetDataCallback;
	SdkKeywords mSetDataSdkKeywords;
	IImportKeywordsCallback mSetDataCallback;

	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		AsrWakeupEngine.getEngine().runOnCompileBackGround(
				new Runnable2<SdkKeywords, IImportKeywordsCallback>(oKeywords,
						oCallback) {
					@Override
					public void run() {
						if (mMixSpeechUnderstander == null) {
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
							mapData.put(SpeechConstants.UPLOAD_DATA_NAME, lstData);
							mSetNetDataSdkKeywords = oKeywords;
							mSetNetDataCallback = mP2;
							mMixSpeechUnderstander.uploadUserData(mapData);
						} else {
							//??????????????????????????????, ?????????????????????
							if (ProjectCfg.RecognOnline()){
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
								String key = oKeywords.strType.substring(1,
										oKeywords.strType.length() - 1);
								JNIHelper.logd("importKeywordsKey = " + key
										+ ", session_id="
										+ oKeywords.uint32SessionId);
								do {
									// ????????????????????????tag
									if (key.contains("callPrefix")
											|| key.contains("callSuffix")) {
										if (mP2 != null) {
											mP2.onSuccess(mP1);
										}
										break;
									}
									String strContents = oKeywords.strContent;
									if (strContents == null
											|| strContents.isEmpty()) {
										break;
									}

									String[] aContents = strContents
											.split("\n");
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

									nRet = mMixSpeechUnderstander.insertVocab(
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
								mapData.put(SpeechConstants.UPLOAD_DATA_APP, lstData);
								mSetNetDataSdkKeywords = oKeywords;
								mSetNetDataCallback = mP2;
								mMixSpeechUnderstander.uploadUserData(mapData);
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
}
