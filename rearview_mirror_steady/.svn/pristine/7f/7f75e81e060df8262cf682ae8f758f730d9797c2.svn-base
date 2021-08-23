package com.txznet.txz.component.asr.mix.local;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.pachira.common.Constant;
import com.pachira.sr.ISrListener;
import com.pachira.sr.SrSession;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;
import com.txznet.txz.util.runnables.Runnable2;

public class LocalAsrPachiraImpl implements IAsr {
	private SrSession srSession;
	private IInitCallback mInitCallback = null;
	private AsrOption mAsrOption = null;
	private TXZAudioRecorder mAudioRecorder = null;
	private Handler mWorkHandler = null;
	private HandlerThread mWorkThread = null;// 录音工作线程
	private boolean mRecording = false;// 录音状态
	private boolean mReady = false;
	private boolean bInitOk = false;
//	FileOutputStream fos = null;
	
	private ISrListener isrListener = new ISrListener() {

		@Override
		public void onSrMsgProc(long msgId, long msgId2, String sMsg) {
			int type = (int) msgId;
			switch (type) {
			case Constant.STATE_SR_MSG_Result:// 识别结果
				parseResult(sMsg);
				break;
//			case Constant.STATE_RESOURCE_NOT_FOUND:// 资源路径不正确
//				JNIHelper.loge("pachira STATE_RESOURCE_NOT_FOUND");
//				break;
			case Constant.STATE_SR_MSG_SpeechStart:// 检测到语音开始点
				onStart();
				break;
			case Constant.STATE_SR_MSG_SpeechEnd:// 检测到语音结束点，正在进行识别处理，不需要再写入数据
				onEnd();
				break;
			case Constant.STATE_SR_MSG_VolumeLevel:// 当前说话的音量大小
//				JNIHelper.logd("msgId = "+msgId+" ,msgId2 = "+msgId2+" ,sMsg = "+sMsg);
				Integer vol = (int) msgId2;
				if (null == vol) {
					return;
				}
				onVolume(vol);
				break;
			case Constant.STATE_SR_NET_FAILED:
				JNIHelper.logd("pachira STATE_SR_NET_FAILED");
//				onInit(false);
				break;
			case Constant.STATE_SR_INIT_SUCCESS://初始化成功
				JNIHelper.logd("LocalAsrPachiraImpl onSrInited state:" + true);
				onInit(true);
				break;
			case Constant.STATE_SR_INIT_FAILED://初始化失败
				JNIHelper.logd("LocalAsrPachiraImpl onSrInited state:" + false);
				onInit(false);
				break;
			case Constant.STATE_SR_MSG_SilenceTimeout://静音超时
				JNIHelper.loge("onError STATE_SR_MSG_SilenceTimeout");
				onError(ERROR_NO_SPEECH);
				break;
			case Constant.STATE_SR_MSG_SpeechInputTooLong://
				JNIHelper.logd("onError STATE_SR_MSG_SpeechInputTooLong");
				break;
			case Constant.STATE_SR_RESULT_FAILED:// 表示识别过程出错或者识别结果为空
				JNIHelper.loge("onError " + msgId + " " + sMsg);
				onError(ERROR_NO_MATCH);
				break;
			case Constant.STATE_ERROR:// 出现未捕获错误 ，识别引擎已经停止，不需要再写入数据
				JNIHelper.loge("onError " + msgId + " " + sMsg);
				onError(ERROR_CODE);
				break;

			default:
				JNIHelper.logd("msgId = "+msgId+",msgId2 = "+msgId2+" ,sMsg = "+sMsg);
				break;
			}

		}

		
	};
	
	public LocalAsrPachiraImpl() {
	}
	

	@Override
	public int initialize(IInitCallback oRun) {
		mInitCallback = oRun;
//		clearGrm();
		mWorkThread = new HandlerThread("pachira_record_thread");
		mWorkThread.start();
		mWorkHandler = new Handler(mWorkThread.getLooper());
		mAudioRecorder = new TXZAudioRecorder(ProjectCfg.mEnableAEC);
		doInit();
		return 0;
	}
	
	

	protected void onError(final int errCode) {
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

	private synchronized void doInit() {
		JNIHelper.logd("pachira start init");
		AppLogic.runOnBackGround(initTimeoutRunnable, 1000*30);
		srSession = SrSession.getInstance(GlobalContext.get(), isrListener);
		srSession.setParam(Constant.PARAM_RESULT_TYPE,
				Constant.VALUE_RESULT_TYPE_JSON);
		srSession.setParam(Constant.PARAM_AUDIO_SOURCE,
				Constant.VALUE_AUDIO_SOURCE_STREAM);
		srSession.setConfidenceLevel(0.7f);
//		try {
//			fos = new FileOutputStream(new File(Environment
//					.getExternalStorageDirectory().getPath() + "/txz/a.pcm"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}

	}

	Runnable initTimeoutRunnable = new Runnable() {
		
		@Override
		public void run() {
			if(mInitCallback != null){
				JNIHelper.loge("pachira init failed");
				mInitCallback.onInit(false);
			}
		}
	};
	
	protected void onVolume(final Integer vol) {
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

	protected void onEnd() {
		JNIHelper.logd("onEnd");
		mRecording = false;
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

	protected void onStart() {
		JNIHelper.logd("onStart");
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

	protected void parseResult(String jsonResult) {
		JNIHelper.logd("LocalAsrPachiraImpl = " + jsonResult);
		if(TextUtils.isEmpty(jsonResult)){
			return;
		}
		VoiceData.VoiceParseData oVoiceParseData = new VoiceData.VoiceParseData();
		oVoiceParseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_PACHIRA_LOCAL_JSON;
		oVoiceParseData.strVoiceData = jsonResult;
		oVoiceParseData.uint32Sence = mAsrOption.mGrammar;
		AsrOption oOption = mAsrOption;
		if (oOption != null) {
			if (oOption.mManual) {
				oVoiceParseData.boolManual = 1;
			} else {
				oVoiceParseData.boolManual = 0;
			}
			oVoiceParseData.uint32Sence = oOption.mGrammar;
			if (!oOption.mNeedStopWakeup) {
				oVoiceParseData.uint32AsrWakeupType = VoiceData.VOICE_ASR_WAKEUP_TYPE_MIX;
			}
		}
		mAsrOption.mCallback.onSuccess(mAsrOption, oVoiceParseData);
	}

	/**
	 * 初始化
	 * 
	 * @param state
	 * @param errID
	 */
	protected void onInit(final boolean state) {
		JNIHelper.logd("onInit");
		AppLogic.removeBackGroundCallback(initTimeoutRunnable);
		if (state) {
			bInitOk = true;
		} else {
			bInitOk = false;
			srSession = null;
		}
		Runnable oRun = new Runnable() {
			public void run() {
				if (mInitCallback != null) {
					mInitCallback.onInit(state);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);

	}

	@Override
	public void release() {
		if (mReady && srSession != null) {
			mRecording = false;
			mReady = false;
			bInitOk = false;
			JNIHelper.logd("destroy");
//			srSession.destroy();
			srSession.stop();
		}
	}

	@Override
	public int start(AsrOption oOption) {
		if (srSession == null || !bInitOk) {
			doInit();
		}
		mAsrOption = oOption;
//		srSession.setParam(Constant.PARAM_SR_SPEECH_TAIL,
//				mAsrOption.mKeySpeechTimeout + "");
//		JNIHelper.logd("mKeySpeechTimeout = "+mAsrOption.mKeySpeechTimeout);
//		LocationInfo locationInfo = LocationManager.getInstance()
//				.getLastLocation();
//		if (locationInfo != null) {
//			if (locationInfo.msgGpsInfo != null) {
//				srSession.setParam(Constant.PARAM_SR_LATITUDE,
//						locationInfo.msgGpsInfo.dblLat + "");
//				srSession.setParam(Constant.PARAM_SR_LONGTITUDE,
//						locationInfo.msgGpsInfo.dblLng + "");
//				srSession.setParam(Constant.PARAM_SR_CITY,
//						locationInfo.msgGeoInfo.strCity);
//			} else {
//				JNIHelper
//						.logw("LocalAsrPachiraImpl locationInfo.msgGpsInfo == null");
//			}
//		} else {
//			JNIHelper.logw("LocalAsrPachiraImpl Location == null");
//		}
		if (mAsrOption.mPlayBeepSound) {
			mSkipEdByte = 6400;
		}else {
			mSkipEdByte = 0;
		}
		JNIHelper.logd("start");
		srSession.start(Constant.PARAM_SR_SCENE_ALL,
				Constant.PARAM_SR_MODE_LOCAL_NLP);// 纯本地端识别
		mWorkHandler.postDelayed(recordingTask, 0);
		return 0;
	}

	private final int BUFFER_SIZE = 6400;
	private byte[] data_buffer = new byte[BUFFER_SIZE];
	private int mSkipEdByte = 6400;

	private Runnable recordingTask = new Runnable() {
		@Override
		public void run() {
			mRecording = true;
			mReady = true;
			long beginSpeechTime = mAsrOption.mBeginSpeechTime;
			JNIHelper.logd("mRecording start beginSpeechTime = " + beginSpeechTime);
			if (0 == beginSpeechTime) {
				mAudioRecorder.startRecording();
			} else {
				mAudioRecorder.startRecording(beginSpeechTime);
			}
//			FileOutputStream fos = null;
//			try {
//				fos = new FileOutputStream(
//						new File(Environment.getExternalStorageDirectory()
//								.getPath() + "/txz/a.pcm"));
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
			int skip = mSkipEdByte;
			
			while (mRecording && mReady) {
				if (mAudioRecorder != null) {
					int read = mAudioRecorder.read(data_buffer, 0,
							data_buffer.length);
					if (read > 0) {
						if (read <= skip) {
							skip -= read;
							continue;
						}
						srSession.writeAudio(data_buffer, read);
//						try {
//							fos.write(data_buffer, 0, read);
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
					}
				} else {
					break;
				}
			}
			JNIHelper.logd("mRecording end");
			mRecording = false;
			mAudioRecorder.stop();
		}
	};

	@Override
	public void stop() {
		if (mReady) {
			mRecording = false;
			JNIHelper.logd("stop");
			srSession.stop();
		}
	}

	@Override
	public void cancel() {
		if (mReady) {
			mRecording = false;
			mReady = false;
			JNIHelper.logd("cancel");
			srSession.cancel();
//			JNIHelper.logd("stop");
//			srSession.stop();
		}
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
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		AppLogic.runOnBackGround(
				new Runnable2<SdkKeywords, IImportKeywordsCallback>(oKeywords,
						oCallback) {
					@Override
					public void run() {
						int error = 0;
						try {
							if (srSession == null) {
								JNIHelper.logw("srSession == null");
								return;
							}
							// 如果强制使用在线识别, 不能插离线命令
							if (ProjectCfg.RecognOnline()) {
								JNIHelper.logw("ProjectCfg.RecognOnline() = "
										+ ProjectCfg.RecognOnline());
								return;
							}
							String type = "";
							SdkKeywords oKeywords = mP1;

							if (oKeywords.strType.startsWith("<")) {
								String key = oKeywords.strType.substring(1,
										oKeywords.strType.length() - 1);
								if (key.contains("mscSong")) {
									type = "songs";
								} else if (key.contains("mscList")) {
									type = "songs";
								} else if (key.contains("mscSinger")) {
									type = "singers";
								} else if (key.contains("cmdAppNames")) {
									type = "apps";
								} else if (key.contains("cmdKeywords")) {
									type = "localcmds";
								} else if (key.contains("callCon1")) {
									type = "contact";
								} else if (key.contains("callCon2")) {
									type = "contact";
								} else {
									return;
								}
							} else {
								return;// type不是正确的类型
							}
							JSONArray dictJa = new JSONArray();
							JSONObject dictJson = new JSONObject();
							dictJson.put("dictname", type);
							JSONArray contentJson = new JSONArray();
							String strContents = oKeywords.strContent;
							if (strContents == null || strContents.isEmpty()) {
								return;
							}
							String[] aContents = strContents.split("\n");
							if (aContents == null || aContents.length == 0) {
								return;
							}
							for (int i = 0; i < aContents.length; i++) {
								if (aContents[i] == null
										|| aContents[i].trim().isEmpty()) {
									continue;
								}
								JNIHelper.logd(aContents[i]);
								JSONObject itemJson = new JSONObject();
								itemJson.put("name", aContents[i]);
								contentJson.add(itemJson);
							}
							dictJson.put("dictcontent", contentJson);
							dictJa.add(dictJson);
							JSONObject json = new JSONObject();
							json.put("grm", dictJa);
							if (srSession != null) {
								error = srSession.uploadDict(json.toString());
								JNIHelper.logd("srSession.uploadDict:"
										+ json.toString());
							}
						} catch (Exception e) {
							JNIHelper.logw(e.getMessage());
						} finally {
							if (error != 0) {
								if (mP2 != null) {
									mP2.onError(error, mP1);
								}
							} else {
								if (mP2 != null) {
									mP2.onSuccess(mP1);
								}
							}
						}
					}
				}, 0);

		return true;
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

	public static VoiceParseData pachiraDataToTxzScene(VoiceParseData parseData) {
		if(parseData == null){
			return parseData;
		}
		if(TextUtils.isEmpty(parseData.strText)){
			parseData.strText = "";
		}
		VoiceParseData newData = new VoiceParseData();
		try {
			newData = VoiceParseData.parseFrom(VoiceParseData.toByteArray(parseData));
		} catch (InvalidProtocolBufferNanoException e) {
			JNIHelper.loge("pachira VoiceParseData parse error = "+e.getMessage());
		}
		PachiraLocalJsonConver conver = new PachiraLocalJsonConver(parseData);
		newData.strVoiceData = conver.getJson();
		newData.floatTextScore = conver.getScore();
		newData.strText = conver.getRawText();
		JNIHelper.logd("newData strVoiceData = "+newData.strVoiceData+" floatTextScore = "+newData.floatTextScore
				+" strText = "+newData.strText);
		return newData;
	}


}
