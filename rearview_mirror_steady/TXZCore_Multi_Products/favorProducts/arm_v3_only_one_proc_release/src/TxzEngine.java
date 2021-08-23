package com.txznet.txz.component.asr.txzasr;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

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
import com.txznet.txz.component.asr.txzasr.IFlyPlugin;
import com.txznet.txz.component.asr.txzasr.IFlyPlugin.INetAsrCallBack;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrAndWakeupIIintCallback;
import com.txznet.txz.component.asr.yunzhisheng_3_0.AsrWakeupEngine.AsrState;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.component.text.IText.ITextCallBack;
import com.txznet.txz.component.tts.yunzhisheng_3_0.AudioSourceDistributer;
import com.txznet.txz.component.wakeup.IWakeup.IWakeupCallback;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.record.Recorder;
import com.txznet.txz.module.text.TextManager;
import com.txznet.txz.module.text.TextResultHandle;
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
public class TxzEngine implements IEngine{
	private SpeechUnderstander mMixSpeechUnderstander = null;
	private AsrState mAsrState = AsrState.ASR_IDLE;
	
	private void setAsrState(AsrState asrState) {
//		synchronized (TxzEngine.class) {
			mAsrState = asrState;
//		}
	}

	public AsrState getAsrState() {
//		synchronized (TxzEngine.class) {
			return mAsrState;
//		}
	}

	private boolean mAutoRun = false;

	public void enableAutoRun(boolean enable) {
		mAutoRun = enable;
		JNIHelper.logd("mAutoRun = " + mAutoRun);
	}

	private String mFixResult = null;
	private float mLocalScore = -100.0f;
	
	private boolean bAsrHasSpeech = false;

	private void clear() {
		mFixResult = null;
		bAsrHasSpeech = false;
		mIFlyPluginError = 0;
		mLocalScore = -100.0f;
		bRecordEnd = false;
	}

	private void onAbort(final int abort) {
		// 启动录音发生异常
		if (mAsrState == AsrState.ASR_WAKEUP) {
			mAsrState = AsrState.ASR_IDLE;
			mMixSpeechUnderstander.stop();
			mMixSpeechUnderstander.cancel();
			mIFlyplugin.cancel();
			JNIHelper.logw("唤醒启动录音失败,即将延时3s重启唤醒");
			WakeupManager.getInstance().startDelay(3000);
			return;
		} else if (mAsrState == AsrState.ASR_RECOGNIZE) {
			//录音结束了, 抛异常不用理会了
			if (bRecordEnd) {
				JNIHelper.logd("onAbort bRecordEnd = " + bRecordEnd);
				return;
			}
			onEnd();
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
					synchronized (TxzEngine.class) {
						if (mAsrOption != null && mAsrOption.mCallback != null) {
							mAsrOption.mCallback.onAbort(mAsrOption, abort);
						}
					}
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
						 * 如果离线结果分数很高或者网络识别出现过错误， 则直接使用离线识别结果,否则先记录下离线识别结果
						 */
						if (score >= -5.0 || mIFlyPluginError != 0) {
							fixLocalResult(score, result);
						} else {
							mLocalScore =score;
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
		onEnd();
		do {
			if (mAsrOption != null && mAsrOption.mCallback != null) {
				if (score + DOUBLE > -20.0 && score - DOUBLE < -20.0) {
					int error2 = !(mIFlyPluginError == IAsr.ERROR_NO_SPEECH ) && bAsrHasSpeech ? IAsr.ERROR_NO_MATCH
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

	// {"local_asr":[{"result_type":"full","score":-2.39,"recognition_result":"  你好小踢   ","engine_mode":"wakeup"}]}
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

	// {"errorCode":-91002,"errorMsg":"请求初始化错误"}
	private void parseErrorCode(String errorMsg) {
		JSONObject json = null;
		int errorCode = 0;
		try {
			json = new JSONObject(errorMsg);
			errorCode = json.getInt("errorCode");
			switch (errorCode) {
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
				//onRecordEnd();
				doLocalResult(jsonResult);
				break;
			}
		}

		@Override
		public void onEvent(int type, int timeMs) {
			switch (type) {
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
			case SpeechConstants.ASR_EVENT_RECORDING_START:
				JNIHelper.logd("ASR_EVENT_RECORDING_START");
				onRecordBegin();
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
                //onRecordEnd();
				break;
			case SpeechConstants.ASR_EVENT_LOCAL_END:
				JNIHelper.logd("onFixEnd");
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
				Log.d("", "pbh test wakeup wakeup file data");
				onWakeup();
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
			parseErrorCode(errorMSG);
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
		//识别中也不允许更新唤醒词
		if (mAsrState == AsrState.ASR_RECORDING  || mAsrState == AsrState.ASR_WAKEUP || mAsrState == AsrState.ASR_RECOGNIZE) {
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
		// 等待设置完成的回调事件，超时时间为100ms
		int nCount = 0;
		while (bInsertWakeupwordsing && nCount < 2000) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			nCount++;
		}
		// 记录插入成功的唤醒词
		if (!bInsertWakeupwordsing) {
			mLastWakeupKeywords = keyWordList;
		}
		bInsertWakeupwordsing = false;
	}
	
	public void startWakeup(IWakeupCallback oCallback) {
		synchronized (TxzEngine.class) {
			if (mAsrState == AsrState.ASR_RECOGNIZE
					|| mAsrState == AsrState.ASR_WAKEUP
					|| mAsrState == AsrState.ASR_RECORDING) {
				JNIHelper.logw("mAsrState = " + mAsrState);
				return;
			}
			JNIHelper.logd("startWakeup");
			mWakeupCallback = oCallback;
			mAsrState = AsrState.ASR_WAKEUP;
			//如果默认使用大模型唤醒，初始化成功后已经设置了,所以这里不用再根据使用场景来设置模型的大小。
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
			}else{
				mMixSpeechUnderstander.setOption(
						SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID, SpeechConstants.AUTO_320_MODEL);
			}
			// 设置唤醒结果不以json的格式返回
			mMixSpeechUnderstander.setOption(
					SpeechConstants.ASR_OPT_RESULT_JSON, false);
			mMixSpeechUnderstander.setOption(
					SpeechConstants.ASR_VAD_TIMEOUT_BACKSIL, 10); //设置后端静音超时10ms，加快唤醒响应
			mMixSpeechUnderstander.setOption(
					SpeechConstants.WAKEUP_OPT_THRESHOLD_VALUE,
					AsrWakeupEngine.WAKEUP_OPT_THRESHOLD);// 阈值设置需要为float类型
			// -3.0f
			mMixSpeechUnderstander.setOption(
					SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA, AsrWakeupEngine.NULL_FILE);//传入黑洞(/dev/null)降低云知声唤醒时的CPU占用，解决骏泰达唤醒响应慢的问题

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
			mMixSpeechUnderstander.start("wakeup");

		}
	}

	public void stopWakeup() {
		synchronized (TxzEngine.class) {
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
		synchronized (TxzEngine.class) {
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
					AsrWakeupEngine.WAKEUP_OPT_THRESHOLD);// 阈值设置需要为float类型
			mMixSpeechUnderstander.setOption(
					SpeechConstants.ASR_OPT_SAVE_RECORDING_DATA, "");
			mMixSpeechUnderstander.start("wakeup");
		}
		return 0;
	}

	public void stopWithRecord() {
		synchronized (TxzEngine.class) {
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

		// AppID为空时
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
				SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		mMixSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		// 关闭引擎log打印
		mMixSpeechUnderstander.setOption(
				SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, bDebuged);
		mMixSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG,
				bDebuged);
         
		Runnable oPluginRun = new Runnable() {
			@Override
			public void run() {
				mIFlyplugin.initialize();
			}
		};
		AppLogic.runOnBackGround(oPluginRun, 0);
		
		// 识别引擎初始化
		JNIHelper.logd("initAsrBegin");
		int nRet = 0;
		mMixSpeechUnderstander.init("");
		JNIHelper.logd("nRet = " + nRet);
		return 0;
	}

	/*
	 * 响应引擎初始化完成事件
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
	 * 设置语音资源
	 * 
	 * @param src
	 */
	public void setAudioSource(IAudioSource src) {
		mMixSpeechUnderstander.setAudioSource(src);
	}

	/*
	 * 识别引擎初始化完成后的预处理
	 */
	private IAudioSource mAudioSource = null;
	private synchronized void doInit() {
		if (ProjectCfg.mUseHQualityWakeupModel) {
			mMixSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_WAKEUP_MODEL_ID, 13);
		}
		mAudioSource = AudioSourceFactory.createAudioSource();
		mMixSpeechUnderstander.setAudioSource(mAudioSource);
		mMixSpeechUnderstander.loadCompiledJsgf("txzTag", GlobalContext.get()
				.getApplicationInfo().dataDir + "/data/txz.dat");// 加载编译语法
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
		bInitDone = true;// 更新完场景识别的词表,才认为初始化完成。否则有可能插唤醒词时互相影响。
	}

	private synchronized void notifyInitDone() {
		// 通知上层引擎初始化完成
		for (int i = 0; i < initCallBacks.size(); ++i) {
			final AsrAndWakeupIIintCallback callback = initCallBacks.get(i);
			if (callback != null) {
				callback.onInit(true);
			}
		}
	}

	/*
	 * load上次grammar
	 */
	private boolean loadLastGrammar() {
		String grammarPath = GlobalContext.get().getApplicationContext()
				.getFilesDir().getAbsolutePath()
				+ "/YunZhiSheng/asrfix/jsgf_model/txzTag.dat";// 加载上一次生成的模型
		File file = new File(grammarPath);
		if (file.exists()) {
			JNIHelper.logd("load grammarPath = " + grammarPath);
			mMixSpeechUnderstander.loadGrammar("txzTag", grammarPath);
			return true;
		}
		return false;
	}


	public void release() {
		mMixSpeechUnderstander = null;
	}

	AsrOption mAsrOption = null;
	
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
	private int mAsrSrvMode = SpeechConstants.ASR_SERVICE_MODE_MIX;
	private static final String USELESS_SERVER_ADDR = "127.0.0.1:8080";
	public synchronized int startAsr(AsrOption oOption) {
		if (mMixSpeechUnderstander == null) {
			JNIHelper.logw("mMixSpeechUnderstander == null");
			return -1;
		}

		AppLogic.removeUiGroundCallback(oRecordTimeOutRun);// 清除定时器

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
		//mMixSpeechUnderstander.setOption(SpeechConstants.ASR_SERVER_ADDR, USELESS_SERVER_ADDR);

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

		mMixSpeechUnderstander.setOption(SpeechConstants.ASR_DOMAIN, "poi");
		mMixSpeechUnderstander.setOption(SpeechConstants.NLU_SCENARIO, "incar");

		String grammar = "txzTag";
		JNIHelper.logd("grammar = " + grammar);
        
		mAsrSrvMode = SpeechConstants.ASR_SERVICE_MODE_MIX;
		if (AsrManager.getInstance().getAsrSvrMode() == AsrServiceMode.ASR_SVR_MODE_AUTO || ProjectCfg.RecognOnline()) {
			if (NetworkManager.getInstance().hasNet() || ProjectCfg.RecognOnline()) {
				mAsrSrvMode = SpeechConstants.ASR_SERVICE_MODE_NET;
			} 
		}
		JNIHelper.logd("mAsrSrvMode = " + mAsrSrvMode);
		startIFly();
		mRecorder = new NetAsrRecorder();
		AudioSourceDistributer.getIntance().addRecorder(mRecorder);
		if (mAsrSrvMode == SpeechConstants.ASR_SERVICE_MODE_MIX ){
			mMixSpeechUnderstander.start(grammar);
		}else{
			int nRet = mAudioSource.openAudioIn();
			JNIHelper.logd("nRet = " + nRet);
			if (nRet != 0) {
				//处理启动录音失败的情况
				SelfRecordHelper.setNeedWait(false);
				onAbort(ErrorCode.RECORDING_EXCEPTION);
			} else {
				onRecordBegin();
				mAudioSource.closeAudioIn();
			}
		}
		return 0;
	}
    
	private void startIFly() {
		int nRet = mIFlyplugin.start(mNetAsrCallBack, mAsrOption);
		if (nRet != IAsr.ERROR_SUCCESS) {
			mIFlyPluginError = IAsr.ERROR_CODE;
			// 如果是纯在线识别启动失败则直接return
			if (mAsrSrvMode == SpeechConstants.ASR_SERVICE_MODE_NET) {
				// 处理启动录音失败的情况
				JNIHelper.logw("start pure net asr fail!!!");
				SelfRecordHelper.setNeedWait(false);
				onAbort(ErrorCode.RECORDING_EXCEPTION);
			}
		} 
		JNIHelper.logd("nRet = " + nRet);
	}
	
	public void stopAsr() {
		if (mMixSpeechUnderstander == null) {
			JNIHelper.logw("mMixSpeechUnderstander == null");
			return;
		}
		JNIHelper.logd("stop");
		SelfRecordHelper.setNeedWait(false);
		SelfRecordHelper.stop();
		mMixSpeechUnderstander.stop();
		mIFlyplugin.stop();
		onRecordEnd();
		AppLogic.removeUiGroundCallback(oRecordTimeOutRun);// 清除录音超时定时器
	}

	/*
	 * 取消识别
	 * 
	 * @see com.txznet.txz.component.asr.IAsr#cancel()
	 */
	public void cancelAsr() {
		if (mMixSpeechUnderstander == null) {
			JNIHelper.logw("mMixSpeechUnderstander == null");
			return;
		}

		synchronized (TxzEngine.class) {
			JNIHelper.logd("cancel");
			if (mAsrState != AsrState.ASR_RECOGNIZE) {
				JNIHelper.logd("current state is not recogine, no need cancel");
				return;
			}

			mAsrState = AsrState.ASR_IDLE;
			SelfRecordHelper.setNeedWait(false);
			SelfRecordHelper.stop();
			mMixSpeechUnderstander.cancel();
			mIFlyplugin.cancel();
			AudioSourceDistributer.getIntance().delRecorder(mRecorder);
			mRecorder = null;
			AppLogic.removeUiGroundCallback(oRecordTimeOutRun);// 清除定时器

			if (mAsrOption != null && null != mAsrOption.mCallback){
				mAsrOption.mCallback.onCancel(mAsrOption);
			}
		}
	}

	public boolean isBusy() {
		return AsrState.ASR_RECOGNIZE == mAsrState; 
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
							mSetNetDataSdkKeywords = oKeywords;
							mSetNetDataCallback = mP2;
							boolean bRet = mIFlyplugin.importKeywords(oKeywords, mP2);
							if (!bRet){
								IImportKeywordsCallback oSetNetDataCallback = mSetNetDataCallback;
								mSetNetDataCallback = null;
								if (oSetNetDataCallback != null){
									oSetNetDataCallback.onError(-1, mSetNetDataSdkKeywords);
								}
							}
						} else {
							//如果强制使用在线识别, 不能插离线命令
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
									// 过滤引擎不支持的tag
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
	/********************************************onEvent*********************************************************/
	private void onSpeech(final boolean bSpeaking) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				//使用try捕获有可能出现的空指针异常
				//不使用同步锁的原因是:防止引擎连续回调onSpeech,
				//导致stopWakeup进不去或者等待过长时间才执行
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
						JNIHelper.logd("onSpeech bSpeaking = " + bSpeaking);
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

	
	private void onRecordBegin() {
		if (mAsrState != AsrState.ASR_RECOGNIZE){
			return;
		}
		AppLogic.runOnUiGround(oRecordTimeOutRun, mAsrOption.mKeySpeechTimeout);//启动录音超时定时器
		if (mIFlyPluginError == 0){
		    SelfRecordHelper.setNeedWait(true);
		}
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				synchronized (TxzEngine.class) {
					if (mAsrOption != null && mAsrOption.mCallback != null) {
						mAsrOption.mCallback.onStart(mAsrOption);
					}
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	private boolean bRecordEnd = false;
	private void onRecordEnd(){
		synchronized (TxzEngine.class) {
			if (mAsrState != AsrState.ASR_RECOGNIZE) {
				return;
			}
			if (bRecordEnd) {
				JNIHelper.logd("bRecordEnd = " + bRecordEnd);
				return;
			}
			bRecordEnd = true;
			mIFlyplugin.stop();
			mMixSpeechUnderstander.stop();
		}
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				synchronized (TxzEngine.class) {
					if (mAsrOption != null && mAsrOption.mCallback != null) {
						mAsrOption.mCallback.onEnd(mAsrOption);
					}
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
				synchronized (TxzEngine.class) {
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
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onWakeup() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				synchronized (TxzEngine.class) {
					if (mWakeupCallback != null) {
						mWakeupCallback.onWakeUp(mWakeupText, mWakeupTime);
					}
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onEngineError(final int errCode) {
		onEnd();
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				synchronized (TxzEngine.class) {
					if (mAsrOption != null && mAsrOption.mCallback != null) {
						mAsrOption.mCallback.onError(mAsrOption, 0, null, null,
								errCode);
					}
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	private void onEnd() {
		synchronized (TxzEngine.class) {
			JNIHelper.logd("onEnd");
			mAsrState = AsrState.ASR_IDLE;
			mMixSpeechUnderstander.cancel();
			mIFlyplugin.cancel();
			SelfRecordHelper.setNeedWait(false);
			SelfRecordHelper.stop();
			AudioSourceDistributer.getIntance().delRecorder(mRecorder);
			mRecorder = null;
			AppLogic.removeUiGroundCallback(oRecordTimeOutRun);// 清除录音超时定时器
		}
	}
	
	/********************************************IFlyPlugin*********************************************************/
    private boolean bOnlyByPlugin = true;
	private int mIFlyPluginError = 0;
	IFlyPlugin mIFlyplugin = new IFlyPlugin();
	NetAsrRecorder mRecorder = null;
	INetAsrCallBack mNetAsrCallBack = new INetAsrCallBack() {
		@Override
		public void onResult(String text) {
			try {
				if (bOnlyByPlugin) {
					fixNetResult(text, VoiceData.VOICE_DATA_TYPE_SENCE_JSON);
				} else {
					JSONObject json = new JSONObject(text);
					String strText = json.getString("text");
					startWithText(strText, mSysTextCallBack);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onError(int errCode) {
			mIFlyPluginError = errCode;
			if (mAsrSrvMode == SpeechConstants.ASR_SERVICE_MODE_MIX) {
				//混合识别时，需要等待离线识别结果
				if (mFixResult != null) {
					if (mLocalScore > -20.0f || errCode == IAsr.ERROR_ASR_NET_REQUEST) {
						fixLocalResult(mLocalScore, mFixResult);
					} else {
						onEngineError(errCode);
					}
				} else {
					// 云知声离线识别结果还没出来时
					// 如果讯飞在线识别返回的errorCode是"用户没有说话", 也可以直接返回错误回调
					if (errCode == IAsr.ERROR_NO_SPEECH) {
						onEngineError(errCode);
					}
				}
			} else {
				//纯在线识别时，直接报错
				onEngineError(errCode);
			}
		
		}

		@Override
		public void onSpeechBegin() {
			
		}

		@Override
		public void onSpeechEnd() {
			SelfRecordHelper.setNeedWait(false);
			onRecordEnd();
			SelfRecordHelper.stop();
		}
	};
	public class NetAsrRecorder extends Recorder {
		@Override
		public void write(byte[] data, int len) {
			mIFlyplugin.write(data, len);
		}
	}
	
	private String mRawText = "";
	private ITextCallBack mSysTextCallBack = new ITextCallBack() {
		@Override
		public void onResult(String jsonResult) {
			if (mAsrState != AsrState.ASR_RECOGNIZE){
				return;
			}
			
			if (TextUtils.isEmpty(jsonResult)){
				onError(IText.UnkownError);
				return;
			}
			fixNetResult(jsonResult, VoiceData.VOICE_DATA_TYPE_YUNZHISHENG_SENCE_JSON);
		}
		@Override
		public void onError(int errorCode) {
			if (mAsrState != AsrState.ASR_RECOGNIZE){
				return;
			}
			
			JNIHelper.logd("errorCode = " + errorCode);
			fixNetResult(mRawText, VoiceData.VOICE_DATA_TYPE_RAW);
		}
	};
	
	private void startWithText(String text, ITextCallBack callBack){
		mRawText = text;
		VoiceParseData parseData = new VoiceParseData();
		parseData.strText = text;
		parseData.floatTextScore = TextResultHandle.TEXT_SCORE_INVALID;
		parseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_RAW;
		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_VOICE,
				VoiceData.SUBEVENT_VOICE_PARSE_NEW, parseData);
//		TextManager.getInstance().parseText(text, mSysTextCallBack);
	}
	
	private void fixNetResult(String jsonResult, int dataType){
		JNIHelper.logd("jsonResult =" + jsonResult);
		onEnd();
		if (mAsrOption != null && mAsrOption.mCallback != null) {
			VoiceData.VoiceParseData voice = new VoiceData.VoiceParseData();
			voice.uint32DataType = dataType;
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

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
	}
}
