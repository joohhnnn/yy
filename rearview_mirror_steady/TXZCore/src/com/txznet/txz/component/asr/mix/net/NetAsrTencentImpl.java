package com.txznet.txz.component.asr.mix.net;

import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.tencent.ai.sdk.control.SpeechManager;
import com.tencent.ai.sdk.jni.CommonInterface;
import com.tencent.ai.sdk.jni.LoadingCallback;
import com.tencent.ai.sdk.tr.ITrListener;
import com.tencent.ai.sdk.tr.TrParameters;
import com.tencent.ai.sdk.tr.TrSession;
import com.tencent.ai.sdk.utils.ISSErrors;
import com.txz.ui.record.UiRecord;
import com.txz.ui.record.UiRecord.RecordData;
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
import com.txznet.txz.component.asr.mix.Arguments;
import com.txznet.txz.component.asr.mix.AsrCallbackFactory;
import com.txznet.txz.component.asr.mix.IAsrCallBackProxy;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;

public class NetAsrTencentImpl implements IAsr {
	
	private boolean isInitAISDK = false;
	private Boolean loadSoSUccess = null;
	public void init_AISDK() {
		/*if (Build.VERSION.SDK_INT >= 24) {
			// 规避 Android7.0（24） 系统上加载 so 问题
			LogUtil.loge("tencent: loadLibrary...");
			try {
				System.loadLibrary("tvad");
				System.loadLibrary("dingdang_api");
			} catch (Exception e) {
				LogUtil.loge("tencent: loadLibrary false: ", e);
			}
		}*/
		if (isInitAISDK) {
			if (null != loadSoSUccess) {
				doInit();
			}
			return;
		}
		isInitAISDK = true;
		// 初始化AISDK，必须在使用AISDK服务前进行调用，建议在Application onCreate的时候调用
		String result = "";
		try {
			final JSONObject info = new JSONObject();
			String appkey = ProjectCfg.getTencentAppkey();
			String token = ProjectCfg.getTencentToken();
			// 只要一个配置项不正确使用默认参数
			if (TextUtils.isEmpty(token) || TextUtils.isEmpty(appkey)) {
				appkey = "2c336a52-0379-490e-88c3-de556d3426b5";
				token = "ed93bf06d2e64b84ac20ce91830d3d3e";
			}
			info.put("appkey", appkey); // "填入应用的appkey"
			info.put("token", token); // "填入应用的access token"
			info.put("deviceName", "CAR"); // "固定，填入CAR或者TV或者SPEAKER或者PHONE"
			info.put("productName", "txz"); // "产品名称，不要有特殊字符和空格"
			info.put("vendor", "txzing"); // "厂商英文名,不要有特殊字符和空格"
			final JSONObject json = new JSONObject();
			json.put("info", info);
			result = json.toString();
		} catch (Exception e) {
		}
		SpeechManager.getInstance().startUp(GlobalContext.get(), result, new LoadingCallback() {
			@Override
			public void onLoadFinished(boolean success) {
				LogUtil.logw("TX startUP result:" +  success);
				loadSoSUccess = success;
				// 设置请求Ai相关服务的环境, true:测试环境；false:正式环境
				SpeechManager.getInstance().setTestEnvironment(false);
				// 设置显示Ai相关日志, true:打印日志；false:关闭日志
				SpeechManager.getInstance().setDisplayLog(DebugCfg.TENCENT_LOG_AI_DEBUG);
				doInit();
			}
		});
	}
	
	public static final String MONITOR_INFO = "asr.tencent.I.";
	public static final String MONITOR_ERROR = "asr.tencent.E.";
	public static final String MONITOR_WARNING = "asr.tencent.W.";

	private TrSession mTrSession = null;
	private IInitCallback mInitCallback = null;
	private IAsrCallBackProxy mAsrCallBackProxy = AsrCallbackFactory.proxy();
	private AsrOption mAsrOption = null;
	private TXZAudioRecorder mAudioRecorder = null;
	private Handler mWorkHandler = null;
	private HandlerThread mWorkThread = null;// 录音工作线程
	private boolean mRecording = false;// 录音状态
	private boolean mReady = false;
	private boolean bInitOk = false;
	
	private final int BUFFER_SIZE = 4096;// 腾讯推荐
	private byte[] data_buffer = new byte[BUFFER_SIZE];
	private int mSkipEdByte = 6400;
	private String mLastGpsInfo = null;
	
	private Runnable recordingTask = new Runnable() {
		@Override
		public void run() {
			int nSkipedByte = 0;
			mRecording = true;
			mReady = true;
			long beginSpeechTime = mAsrOption.mBeginSpeechTime;
			LogUtil.logd("ASR: mRecording start beginSpeechTime = " + beginSpeechTime);
			if (beginSpeechTime > 0){
				nSkipedByte = mSkipEdByte;
				LogUtil.logd("beginSpeechTime > 0, so forbidden skiped beep");
			}
			mAudioRecorder.startRecording(beginSpeechTime);
			
			long nVoiceId = 0;
			if (mAsrOption != null){
				nVoiceId = mAsrOption.mVoiceID;
			}
			if (nVoiceId != 0){
				mAudioRecorder.beginSaveCache(20*16000*2);//保存最多20s的录音数据
			}
			
			while (mRecording && mReady) {
				if (mAudioRecorder != null) {
					int read = mAudioRecorder.read(data_buffer, 0, data_buffer.length);
					if (read > 0) {
						if (nSkipedByte < mSkipEdByte){
							int nNeedSkipByte = mSkipEdByte - nSkipedByte;
							if (nNeedSkipByte >= read){
								nSkipedByte += read;
								continue;
							}else{
								nSkipedByte += nNeedSkipByte;
								System.arraycopy(data_buffer, nNeedSkipByte, data_buffer, 0, read - nNeedSkipByte);
								read -= nNeedSkipByte;
							}
						}
						int result =mTrSession.appendAudioData(data_buffer, read);
						if (result != ISSErrors.ISS_SUCCESS) {
							LogUtil.logw("ASR: appendAudioData error: " + result);
						}
					}
				} else {
					break;
				}
			}
			LogUtil.logd("ASR: mRecording end");
			mRecording = false;
			mAudioRecorder.stop();
			if (nVoiceId != 0 && Arguments.sIsSaveVoice){
				RecordData mRecordData = new RecordData();
				mRecordData.uint32SampleRate = RecordFile.SAMPLE_RATE_16K;
				mRecordData.uint32RecordType = UiRecord.RECORD_TYPE_ASR;
				mRecordData.uint64RecordTime = mAsrOption.mServerTime;
				mRecordData.boolRecordTime = mAsrOption.bServerTimeConfidence;
				mRecordData.uint32Uid = mAsrOption.mUID;
				mRecordData.boolRecordTime = true;
				mAudioRecorder.endSaveCache(nVoiceId+"", mRecordData, Arguments.sIsSaveRawPCM);
			}
		}
	};
	
	private ITrListener mTrListener = new ITrListener() {
		
		// MsgNum: ISS_SR_MSG_UpLoadDictToLocalStatus Description: 本地个性化数据上传是否成功
		// wParam: ISS_SUCCESS ISS_ERROR_INVALID_JSON_FMT:输入的Json格式有问题
		// ISS_ERROR_INVALID_JSON_INFO:没有从Json输入中提取到必要的个性化数据
		// MsgNum: ISS_SR_MSG_UpLoadDictToCloudStatus Description: 云端个性化数据上传是否成功
		// wParam: ISS_SUCCESS ISS_ERROR_INVALID_JSON_FMT
		// ISS_ERROR_INVALID_JSON_INFO
		@Override
		public void onTrInited(boolean state, int errId) {
			// SR会话初始化结果
			onInit(state);
		}

		@Override
		public void onTrVoiceMsgProc(long uMsg, long wParam, String lParam, Object extraData) {
			switch ((int) uMsg) {
			case TrSession.ISS_TR_MSG_SpeechStart:
				// 检测到说话开始
				onStart();
				break;
			case TrSession.ISS_TR_MSG_SpeechEnd:
				// 检测到说话结束";
				onEnd();
				break;
			case TrSession.ISS_TR_MSG_VolumeLevel:
				// 音量变化，每计算25ms的录音返回一次(40Hz)。最小为0，最大为20
				onVolume(Integer.parseInt(lParam));
				break;
			case TrSession.ISS_TR_MSG_VoiceResult:
				// 识别文本
				LogUtil.logd("ASR: VoiceResult=" + lParam);
				break;
			case TrSession.ISS_TR_MSG_ProcessResult:
				// 流式文本
				LogUtil.logd("ASR: ProcessResult=" + lParam);
				if (!TextUtils.isEmpty(lParam)) {
					mAsrCallBackProxy.onPartialResult(lParam);
				}
				break;
			case TrSession.ISS_TR_MSG_VoiceStart:
			case TrSession.ISS_TR_MSG_VoiceStart_Stage_1:
			case TrSession.ISS_TR_MSG_VoiceStart_Stage_2:
			case TrSession.ISS_TR_MSG_VoiceStart_Stage_3:
			case TrSession.ISS_TR_MSG_VoiceStart_Stage_4:
				break;
			default:
				LogUtil.logd("ASR: onTrVoiceMsgProc - uMsg : " + uMsg + ", lParam : " + lParam);
			}
		}

		@Override
		public void onTrSemanticMsgProc(long uMsg, long wParam, int cmd, String lParam, Object extraMsg) {
			// 返回带语义的识别结果，识别结果的解析
			LogUtil.logd("ASR: onTrSemanticMsgProc - uMsg : " + uMsg + ", wParam : " + wParam);
			parseResult(lParam);
		}

		@Override
		public void onTrVoiceErrMsgProc(long uMsg, long errCode, String lParam, Object extraData) {
			switch ((int) uMsg) {
			case TrSession.ISS_TR_MSG_SpeechTimeout:
				// 语音超时
				LogUtil.logd("ASR: ISS_TR_MSG_SpeechTimeout=" + errCode);
				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + "speechTimeout");
				onError(ERROR_NO_SPEECH);
				break;
             case TrSession.ISS_TR_MSG_Error:
            	// 出现其他错误 
     			LogUtil.logd("ASR: ISS_SR_MSG_Error=" + errCode + " , " + lParam);
     			if (ISSErrors.ISS_ERROR_FAIL == errCode) {
     				// 不用解析文本，字符串，没有识别结果
     				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + MONITOR_NO_NLP);
     				onError(ERROR_ASR_NET_NLU_EMTPY);
     			} else if (ISSErrors.ISS_ERROR_FILE_NOT_FOUND == errCode) {
     				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + uMsg + "_" + errCode);
     				onError(ERROR_CODE);
     			} else if (errCode == 5) {
     				mAsrCallBackProxy.onMonitor(MONITOR_ERROR + MONITOR_NO_REQUEST);
     				onError(ERROR_ASR_NET_REQUEST);
				} else if (errCode == 6) {
					mAsrCallBackProxy.onMonitor(MONITOR_ERROR + MONITOR_NO_REQUEST);
					onError(ERROR_ASR_NET_REQUEST);
				} else if (errCode == 10) {
					mAsrCallBackProxy.onMonitor(MONITOR_ERROR + MONITOR_NO_SPEECH);
					onError(ERROR_NO_SPEECH);
				} else {
					mAsrCallBackProxy.onMonitor(MONITOR_ERROR + uMsg + "_" + errCode);
					onError(ERROR_CODE);
     			}
				 // ISS_ERROR_UNKNOWN_ERROR = 1 未知错误
				 // ISS_ERROR_NOT_INITIALIZED = 2 SDK未初始化
				 // ISS_ERROR_INTERNAL_ERROR = 3 内部错误
				 // ISS_ERROR_VOICE_TIMEOUT = 4 语音超时
				 // ISS_ERROR_NETWORK_FAIL = 5 网络请求发送失败
				 // ISS_ERROR_NETWORK_RESPONSE_FAIL = 6 网络请求回包失败
				 // ISS_ERROR_NETWORK_TIMEOUT = 7 网络请求超时
				 // 10 无有效输入
                 break;
             default:
            	 mAsrCallBackProxy.onMonitor(MONITOR_ERROR + uMsg + "_" + errCode);
            	 onError(ERROR_CODE);
            	 LogUtil.logd("ASR: onTrVoiceErrMsgProc - uMsg : " + uMsg + ", errCode : " + errCode + ", lParam : " + lParam);
			}
		}

		@Override
		public void onTrSemanticErrMsgProc(long uMsg, long errCode, int cmd, String lParam, Object extraMsg) {
			// TODO Auto-generated method stub
			mAsrCallBackProxy.onMonitor(MONITOR_ERROR + uMsg + "_" + errCode);
			onError(ERROR_ASR_NET_REQUEST);
		}
	};

	private Runnable timeoutRunnable = new Runnable() {
		@Override
		public void run() {
			onError(ERROR_CODE);
			LogUtil.logw("ASR: timeout");
		}
	};
	
	private void onStart() {
		LogUtil.logd("ASR: onStart");
		mAsrCallBackProxy.onStart();
	}
	
	private void onEnd() {
		LogUtil.logd("ASR: onEnd");
		mRecording = false;
		mAsrCallBackProxy.onEndOfSpeech();
		if (mReady) {
			AppLogic.removeBackGroundCallback(timeoutRunnable);
			AppLogic.runOnBackGround(timeoutRunnable, 5000);
		}
	}
	
	private void onVolume(final Integer vol) {
		mAsrCallBackProxy.onVolume(vol);
	}
	
	protected void onError(final int errCode) {
		LogUtil.logd("ASR: onError=" + errCode);
		AppLogic.removeBackGroundCallback(timeoutRunnable);
		mRecording = false;
		mAsrCallBackProxy.onError(errCode);
	}
	
	private void parseResult(String jsonResult) {
		// LogUtil.logd("ASR: NetAsrTencentImpl = " + jsonResult);
		mRecording = false;
		if(TextUtils.isEmpty(jsonResult)){
			return;
		}
		AppLogic.removeBackGroundCallback(timeoutRunnable);
		VoiceData.VoiceParseData oVoiceParseData = new VoiceData.VoiceParseData();
		oVoiceParseData.uint32DataType = VoiceData.VOICE_DATA_TYPE_TENCENT_SCENE_JSON;
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
		mAsrCallBackProxy.onSuccess(oVoiceParseData);
	}
	
	private void onInit(final boolean state) {
		bInitOk = state;
		LogUtil.logd("ASR: onInit "+state);
		if (state) {
			int ret = mTrSession.setSlientTimeout(7000); // 设置前端超时时间ms, 初始化成功后在设置
			LogUtil.logd("ASR: setSlientTimeout result：" + ret);
			// 选择识别类型
            mTrSession.setParam(TrSession.ISS_TR_PARAM_VOICE_TYPE, TrSession.ISS_TR_PARAM_VOICE_TYPE_RSP_ALL);
		}
		Runnable oRun = new Runnable() {
			public void run() {
				IInitCallback cb  =mInitCallback;
				mInitCallback = null;
				if (cb != null) {
					cb.onInit(state);
				}
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
	
	public NetAsrTencentImpl() {
		mWorkThread = new HandlerThread("tencent_record_thread");
		mWorkThread.start();
		mWorkHandler = new Handler(mWorkThread.getLooper());
	}

	@Override
	public int initialize(IInitCallback oRun) {
		LogUtil.logd("ASR: initialize");
		mInitCallback = oRun;

		this.init_AISDK();
//		doInit();
		return 0;
	}

	private synchronized void doInit() {
		if (null == loadSoSUccess) {
			// so 库加载未完成 
			return;
		}
		if (!loadSoSUccess) {
			// 初始化失败没有必要往下执行了
			onInit(false);
			return;
		}
		if (mTrSession != null) {
			mTrSession.release();
			mTrSession = null;
		}
		SpeechManager.getInstance().setAsrDomain(90); // 把识别引擎设置为车机的
		SpeechManager.getInstance().setManualMode(false); // 自动结束录音
		SpeechManager.getInstance().setFullMode(false); // 选择流程模式
		SpeechManager.getInstance().setSilenceTime(1000); // 静音结束时间，默认 500
		boolean bUsePreProcessedData = ProjectCfg.mEnableAEC && ProjectCfg.isUseSePreprocessedData();
		LogUtil.logd("asr:bUsePreProcessedData:" + bUsePreProcessedData);
		mAudioRecorder = new TXZAudioRecorder(bUsePreProcessedData); // 腾讯需要使用降噪模块

		String res = GlobalContext.get().getApplicationInfo().dataDir + "/dingdang/";
		String mRes;
		boolean mWithModelDir;
		if (CommonInterface.isCDefined(CommonInterface.C_DEFINE_USE_FAKE_SO_MDL_NAME)) {
			mRes = GlobalContext.get().getApplicationInfo().nativeLibraryDir;
			mWithModelDir = true;
		} else {
			mRes = res;
			mWithModelDir = false;
		}
		TrParameters params = new TrParameters();
		params.setOnlineVoiceResDir(mRes, mWithModelDir);
		params.setOfflineSemanticResDir(mRes, mWithModelDir);
		params.setOfflineVoiceResDir(res, false);
		params.setMixModeConfigDir(res);
		// 初始化Session
		mTrSession = TrSession.getInstance(GlobalContext.get(), params);
		mTrSession.init(mTrListener);
	}

	@Override
	public void release() {
		LogUtil.logd("ASR: release");
		if (mReady && mTrSession != null) {
			mRecording = false;
			mReady = false;
			bInitOk = false;
			mTrSession.stop();
		}
	}
	
	private void updateLocation() {
		if (Arguments.sGpsInfo == null) {
			return;
		}
		LogUtil.logd("gpsinfo : (" + Arguments.sGpsInfo + "), currCity : " + Arguments.sCurrCity);
		if (!Arguments.sGpsInfo.equals(mLastGpsInfo)) {
			LogUtil.logd("update location info");
			mLastGpsInfo = Arguments.sGpsInfo;
			String[] gps = mLastGpsInfo.split(",");
			if (gps == null || gps.length < 2) {
				return;
			}
			
			try {
				Location location = new Location("gps");
				location.setLatitude(Double.parseDouble(gps[0]));
				location.setLongitude(Double.parseDouble(gps[1]));
				SpeechManager.getInstance().setCurrentLocation(location);
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.loge("update location:", e);
			}
		}
	}
	
	@Override
	public int start(AsrOption oOption) {
		LogUtil.logd("ASR: start");
		mAsrOption = oOption;
		if (mAsrOption.mPlayBeepSound) {
			mSkipEdByte = 6400;
		}else {
			mSkipEdByte = 0;
		}
		mAsrCallBackProxy.setAsrOption(mAsrOption);
		mAsrCallBackProxy.onMonitor(MONITOR_INFO + MONITOR_ALL);
		if (null == loadSoSUccess || !loadSoSUccess || !bInitOk) {
			if (!isInitAISDK) {
				this.init_AISDK();
			}
			Runnable oRun = new Runnable() {
				public void run() {
					mAsrCallBackProxy.onMonitor(MONITOR_ERROR + "no_init");
					onError(ERROR_CODE);
				}
			};
			AppLogic.runOnBackGround(oRun, 0);
			return ERROR_ABORT;
		}
		updateLocation();
//		SpeechManager.getInstance().setSilenceTime(mAsrOption.mEOS); // 后端静音超时
//		mTrSession.setSlientTimeout(mAsrOption.mBOS); // 设置前端超时时间ms
		int errStartid = 0;
		errStartid = mTrSession.start(TrSession.ISS_TR_MODE_CLOUD_REC,false);
		if (errStartid != ISSErrors.ISS_SUCCESS) {
			LogUtil.loge("ASR: start sr failed, ERR_ID=" + errStartid);
		}
		mWorkHandler.removeCallbacks(recordingTask);
		mWorkHandler.post(recordingTask);
		AppLogic.runOnBackGround(timeoutRunnable, 30000);
		return 0;
	}

	@Override
	public void stop() {
		LogUtil.logd("ASR: stop");
		if (mReady) {
			mRecording = false;
			mTrSession.endAudioData();
//			LogUtil.logd("ASR: endAudioData");
		}
	}

	@Override
	public void cancel() {
		LogUtil.logd("ASR: cancel");
		if (mReady) {
			mRecording = false;
			mReady = false;
			mTrSession.stop();
//			LogUtil.logd("ASR: cancel end");
		}
		AppLogic.removeBackGroundCallback(timeoutRunnable);
	}

	@Override
	public boolean isBusy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		// 没有这个接口也需要实现回调方法
		if (null != oCallback) {
			oCallback.onSuccess(oKeywords);
		}
		return false;
	}

	@Override
	public void releaseBuildGrammarData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void retryImportOnlineKeywords() {
		// TODO Auto-generated method stub
	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {
		// TODO Auto-generated method stub
	}
	
	public static VoiceParseData tencentDataToTxzScene(VoiceParseData parseData) {
		if (null == parseData) {
			LogUtil.logd("ASR: tencentAI VoiceParseData == null");
			return null;
		}
		
		if(null == parseData.strText){
			parseData.strText = "";
		}
		
		// 解析语义数据场景结果
		boolean result = false;
		
		VoiceParseData newData = new VoiceParseData();
		JSONObject newJson = new JSONObject();
		JSONObject json = new JSONObject();
		do {
			try {
				newData = VoiceParseData.parseFrom(VoiceParseData.toByteArray(parseData));
			} catch (Exception e) {
				LogUtil.loge("ASR: tencentAI VoiceParseData parse error = "+e.getMessage());
				break;
			}
			
			if (TextUtils.isEmpty(parseData.strVoiceData)) {
				break;
			}
			
			try {
				json = JSONObject.parseObject(parseData.strVoiceData);
			} catch (Exception e) {
				LogUtil.loge("ASR: tencentAI parse voice data error = "+e.getMessage());
				break;
			}
			
			String text = json.getString(JSON_KEY_TEXT);
			if (null == text) {
				break;
			} else {
				newJson.put("text", text);
				newData.strText = text;
			}
			
			int responseCode = json.getIntValue(JSON_KEY_RC);
			// 回复码， 0表示正常
			if (responseCode != 0) {
				LogUtil.logd("ASR: tencentAI responseCode=" + responseCode);
				break;
			}
			
			String service = json.getString(JSON_KEY_SERVICE);
			if (TextUtils.isEmpty(service)) {
				break;
			}
			
			if (SERVICE_TYPE_TELEPHONE.equals(service)) { // 电话场景
//				result = parseTelephone(newData, newJson, json); 电话场景技能有问题，
			} else if (SERVICE_TYPE_NAVIGATION.equals(service)) { // 导航场景
				result = parseNavigation(newData, newJson, json);
			} else if (SERVICE_TYPE_MUSIC.equals(service)) { // 音乐场景
				result = parseMusic(newData, newJson, json);
			} else if (SERVICE_TYPE_WEATHER.equals(service)) { // 天气场景
				result = parseWeather(newData, newJson, json);
			} else if (SERVICE_TYPE_AUDIO.equals(service)) { // 电台场景
				result = parseAudio(newData, newJson, json);
			} else if (SERVICE_TYPE_BAIKE.equals(service)
					|| SERVICE_TYPE_CHAT.equals(service)
					) {
//				newData.floatTextScore = TextResultHandle.TEXT_SCORE_NOCONFIDENCE;
//				result = true;
			} else {
//				SERVICE_TYPE_JOKE.equals(service)
			}
			
		} while (false);
		if (!result) {
			newData.floatTextScore = TextResultHandle.TEXT_SCORE_MIN;
			newJson.put("scene", "unknown");
			newJson.put("action", "unknown");
			String answer = json.getString(JSON_KEY_ANSWER);
			if (!TextUtils.isEmpty(answer)) {
				newJson.put("answer", answer);
			} else {
				answer = json.getString(JSON_KEY_TIPS);
				if (!TextUtils.isEmpty(answer)) {
					newJson.put("answer", answer);
				} else {
					newJson.put("answer", NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"));
				}
			}
		}
		newData.strVoiceData = newJson.toJSONString();
		LogUtil.logd("ASR: tencentAI strVoiceData="+newData.strVoiceData+" Score="+newData.floatTextScore);
		return newData;
	}
	
	// 腾讯语义字段
	private static final String JSON_KEY_RC = "rc"; // int, 返回码， 0为正常， 其他为错误
	private static final String JSON_KEY_SERVICE = "service"; // 领域
	private static final String JSON_KEY_OPERATION = "operation"; // 意图
	private static final String JSON_KEY_TEXT = "text"; //请求文本或者语音识别到的
	private static final String JSON_KEY_ANSWER = "answer"; // 有屏幕设备播报文本
	private static final String JSON_KEY_TIPS = "tips"; // 播报提示
//	private static final String JSON_KEY_NOSREEN_ANSWER = "nosreen_answer"; // 无屏幕设备播报文本
	private static final String JSON_KEY_SEMANTIC = "semantic"; // 语义返回
//	private static final String JSON_KEY_TIPS = "tips"; // 播报提示
	private static final String JSON_KEY_SEMANTIC_SLOTS = "slots"; // 语义信息
	private static final String JSON_KEY_SEMANTIC_SLOT_STRUCT = "slot_struct"; // 语义信息
	private static final String JSON_KEY_SEMANTIC_NAME = "name"; // 语义名称
	private static final String JSON_KEY_SEMANTIC_VALUES = "values"; // 语义信息
	
	private static final int SLOT_STRUCT_DATE = 0; // 时间
	private static final int SLOT_STRUCT_ENTITY = 1; // 实体
	private static final int SLOT_STRUCT_LOCATION = 2; // 位置
	private static final int SLOT_STRUCT_NUMBER = 3; // 数字
	
	/** 百科场景 */
	private static final String SERVICE_TYPE_BAIKE = "baike"; 
	/** 聊天场景 */
	private static final String SERVICE_TYPE_CHAT = "chat";
	/** 笑话场景 */
	private static final String SERVICE_TYPE_JOKE = "joke";
	/** 导航场景 */
	private static final String SERVICE_TYPE_NAVIGATION = "navigation";
	/** 电话场景 */
	private static final String SERVICE_TYPE_TELEPHONE = "phone_call";
	/** 音乐场景 */
	private static final String SERVICE_TYPE_MUSIC = "music";
	/** 天气场景 */
	private static final String SERVICE_TYPE_WEATHER = "weather";
	/** 电台场景 */
	private static final String SERVICE_TYPE_AUDIO = "fm";
	
	private static boolean parseAudio(VoiceParseData newData, JSONObject newJson, JSONObject json) {
		String operation = json.getString(JSON_KEY_OPERATION);
		if (TextUtils.isEmpty(operation)) {
			// 没有找到意图
			return false;
		}
		do {
			if ("prev".equals(operation)) {
				newJson.put("action", "prev");
				break;
			} else if ("next".equals(operation)) {
				newJson.put("action", "next");
				break;
			} else if ("resume".equals(operation)) {
				newJson.put("action", "continue");
				break;
			} else if ("pause".equals(operation)) {
				newJson.put("action", "pause");
				break;
			}
			
			if ("query_album_update".equals(operation)
					|| "search_anchor".equals(operation)
					|| "query_cur_play".equals(operation)
					|| "search_show_num".equals(operation)) {
				// TODO
				return false;
			}
			
			/*if ((!strVoiceText
					.matches("^(?:.*?)(?:调频到|调频|调幅到|调幅|(?i)FM(?-i)|(?i)AM(?-i))(?:..+)(?:兆赫|千赫|赫兹)?$")
					&& (AudioManager.getInstance().isAudioToolSet() || AudioManager
					.getInstance().hasRemoteTool())) && ProjectCfg.isUseRadioAsAudio()) {
				
			}*/
			JSONObject jsonModel = new JSONObject();
			if ("play".equals(operation) || "search_album".equals(operation)
					|| "play_radio".equals(operation)
					|| "search_radio".equals(operation)) {
				JSONObject semantic = json.getJSONObject(JSON_KEY_SEMANTIC);
				if (null == semantic) {
					return false;
				}
				JSONArray slots = semantic.getJSONArray(JSON_KEY_SEMANTIC_SLOTS);
				if (null == slots || slots.isEmpty()) {
					return false;
				}
				JSONArray arrArtists = new JSONArray();
				JSONArray arrKeywords = new JSONArray();
				for (int i = 0; i < slots.size(); i++) {
					JSONObject slot = slots.getJSONObject(i);
					int slot_struct = slot.getIntValue(JSON_KEY_SEMANTIC_SLOT_STRUCT);
					// 不同语义场景对应不同数据结构
					if (SLOT_STRUCT_ENTITY != slot_struct) {
						continue;
					}
					String name = slot.getString(JSON_KEY_SEMANTIC_NAME);
					if (TextUtils.isEmpty(name)) {
						continue;
					}
					JSONArray values = slot.getJSONArray(JSON_KEY_SEMANTIC_VALUES);
					if (null == values || values.isEmpty()) {
						continue;
					}
					JSONObject value = values.getJSONObject(0);
					if (null == value) {
						continue;
					}
					String text = value.getString("text");
					if (TextUtils.isEmpty(text)) {
						continue;
					}
					if ("show".equals(name)) {
						jsonModel.put("title", text);
					} else if ("anchor".equals(name)) {
						arrArtists.add(text);
					} else if ("album".equals(name)) {
						jsonModel.put("album", text);
					} else if ("fm_category".equals(name)) {
						jsonModel.put("category", text);
					} else if ("fm_theme".equals(name)) {
						jsonModel.put("subCategory", text);
					} else if ("show_index".equals(name)) {
						jsonModel.put("episode", text);
					} else {
						// 不用判断直接放入keywords中
						arrKeywords.add(text);
					}
				}
				if (!arrKeywords.isEmpty()) {
					jsonModel.put("keywords", arrKeywords);
				}
				if (!arrArtists.isEmpty()) {
					jsonModel.put("artist", arrArtists);
				}
				newJson.put("model", jsonModel);
				newJson.put("action", "play");
			} else {
				// TODO 目前使用不到的指令集
				return false;
			}
		} while (false);
		newJson.put("scene", "audio");
		if ("play_radio".equals(operation)
				|| "search_radio".equals(operation)) {
			newData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
		} else {
			newData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
		}
		return false;
	}

	private static boolean parseMusic(VoiceParseData newData, JSONObject newJson, JSONObject json) {
		String operation = json.getString(JSON_KEY_OPERATION);
		if (TextUtils.isEmpty(operation)) {
			// 没有找到意图
			return false;
		}
		do {
			if ("prev".equals(operation)) {
				newJson.put("action", "prev");
				break;
			} else if ("next".equals(operation)) {
				newJson.put("action", "next");
				break;
			} else if ("resume".equals(operation)) {
				newJson.put("action", "continue");
				break;
			} else if ("pause".equals(operation)) {
				newJson.put("action", "pause");
				break;
			} else if ("ctrl_single_cycle".equals(operation)) {
				newJson.put("action", "switchModeLoopOne");
				break;
			} else if ("ctrl_break_cycle".equals(operation)) {
				newJson.put("action", "switchModeLoopAll");
				break;
			} else if ("ctrl_favorite".equals(operation)) {
				newJson.put("action", "favourMusic");
				break;
			}
			
			JSONObject jsonModel = new JSONObject();
			if ("play".equals(operation) || "play_album".equals(operation)
					|| "search_song".equals(operation)
					|| "search_singer".equals(operation)
					|| "query_song_editions".equals(operation)
					|| "search_album".equals(operation)
					|| "search_tvfilm".equals(operation)
					) {
				JSONObject semantic = json.getJSONObject(JSON_KEY_SEMANTIC);
				if (null == semantic) {
					return false;
				}
				JSONArray slots = semantic.getJSONArray(JSON_KEY_SEMANTIC_SLOTS);
				if (null == slots || slots.isEmpty()) {
					return false;
				}
				JSONArray arrArtists = new JSONArray();
				JSONArray arrKeywords = new JSONArray();
				for (int i = 0; i < slots.size(); i++) {
					JSONObject slot = slots.getJSONObject(i);
					int slot_struct = slot.getIntValue(JSON_KEY_SEMANTIC_SLOT_STRUCT);
					// 不同语义场景对应不同数据结构
					if (SLOT_STRUCT_ENTITY != slot_struct) {
						continue;
					}
					String name = slot.getString(JSON_KEY_SEMANTIC_NAME);
					if (TextUtils.isEmpty(name)) {
						continue;
					}
					JSONArray values = slot.getJSONArray(JSON_KEY_SEMANTIC_VALUES);
					if (null == values || values.isEmpty()) {
						continue;
					}
					JSONObject value = values.getJSONObject(0);
					if (null == value) {
						continue;
					}
					String text = value.getString("text");
					if (TextUtils.isEmpty(text)) {
						continue;
					}
					if ("song".equals(name)) {
						jsonModel.put("title", text);
					} else if ("singer".equals(name)) {
						arrArtists.add(text);
					} else if ("album".equals(name)) {
						jsonModel.put("album", text);
					} else {
						// tvfilm toplist language theme instrument age scene style emotion opera lyrics
						// 不用判断直接放入keywords中
						arrKeywords.add(text);
					}
				}
				if (arrKeywords != null && !arrKeywords.isEmpty()) {
					jsonModel.put("keywords", arrKeywords);
				}
				if (arrArtists != null && !arrArtists.isEmpty()) {
					jsonModel.put("artist", arrArtists);
				}
				newJson.put("model", jsonModel);
				newJson.put("action", "play");
			} else {
				// TODO 目前使用不到的指令集
				return false;
			}
		} while (false);
		newJson.put("scene", "music");
		newData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
		return true;
	}

	private static boolean parseWeather(VoiceParseData newData, JSONObject newJson, JSONObject json) {
		String operation = json.getString(JSON_KEY_OPERATION);
		if (TextUtils.isEmpty(operation)) {
			// 没有找到意图
			return false;
		}
		
		/*newData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
		newJson.put("scene", "weather");
		
		JSONObject jsonData = new JSONObject();
		jsonData.put("header", json.getString(JSON_KEY_ANSWER));
		
		JSONObject semantic = json.getJSONObject(JSON_KEY_SEMANTIC);
		if (null == semantic) {
			return;
		}
		JSONObject slots = semantic.getJSONObject("slots");
		if (null == slots) {
			return;
		}
		JSONObject datetime = slots.getJSONObject("datetime");
		if (null == datetime) {
			return;
		}
		JSONObject datedata= datetime.getJSONObject("datetime");
		if (null == datedata) {
			return;
		}
		newJson.put("date", datedata.getString("date"));
		int year = datedata.getIntValue("year");
		
		JSONObject jsonResult = new JSONObject();
		JSONArray weatherDays = new JSONArray();
		
//		JSONArray result = 
		
		jsonResult.put("weatherDays", weatherDays);
//		jsonResult.put("focusDateIndex", 0); // 那天的天气
//		jsonResult.put("cityName", city);
		jsonData.put("result", jsonResult);
//		newJson.put("city", city);
		newJson.put("data", jsonData);
		JSONObject jsonModel = new JSONObject();
		do {
			if ("QUERY".equals(operation)
			|| "CONDITIONAL_SEARCH_HUMIDITY".equals(operation)
			|| "CONDITIONAL_SEARCH_FEEL".equals(operation)
			|| "CONDITIONAL_SEARCH_ACTIVITY".equals(operation)) {
				newJson.put("action", "query");
				String strSong = slots.getString("song");
				if (!TextUtils.isEmpty(strSong)) {
					jsonModel.put("title", strSong);
				}
				
				newJson.put("model", jsonModel);
			} else {
				// TODO 目前使用不到的指令集
				return;
			}
		} while (false);*/
		return false;
	}
	
	private static boolean parseTelephone(VoiceParseData newData, JSONObject newJson, JSONObject json) {
		/****************************新版本语义标准数据结构*********************************************/
		String operation = json.getString(JSON_KEY_OPERATION);
		if (TextUtils.isEmpty(operation)) {
			// 没有找到意图
			return false;
		}
		do {
			// 打电话
			if ("make_a_phone_call".equals(operation)) {
				newJson.put("action", "make");
				JSONObject semantic = json.getJSONObject(JSON_KEY_SEMANTIC);
				if (null == semantic) {
					// 没有号码和人名时为空
					break;
				}
				
				JSONArray slots = semantic.getJSONArray(JSON_KEY_SEMANTIC_SLOTS);
				if (null == slots || slots.isEmpty()) {
					break;
				}
				
				for (int i = 0; i < slots.size(); i++) {
					JSONObject slot = slots.getJSONObject(i);
					int slot_struct = slot.getIntValue(JSON_KEY_SEMANTIC_SLOT_STRUCT);
					// 不同语义场景对应不同数据结构
					if (SLOT_STRUCT_ENTITY != slot_struct) {
						continue;
					}
					String name = slot.getString(JSON_KEY_SEMANTIC_NAME);
					if (TextUtils.isEmpty(name)) {
						continue;
					}
					JSONArray values = slot.getJSONArray(JSON_KEY_SEMANTIC_VALUES);
					if (null == values || values.isEmpty()) {
						continue;
					}
					JSONObject value = values.getJSONObject(0);
					if (null == value) {
						continue;
					}
					String text = value.getString("text");
					if (TextUtils.isEmpty(text)) {
						continue;
					}
					if ("contact_name".equals(name)) {
						newJson.put("name", text);
					} else if ("phone_num".equals(name)) {
						newJson.put("number", text);
					} else {
						
					}
				}
			} else {
				// TODO 目前使用不到的指令集
				return false;
			}
			
		} while (false);
		newJson.put("scene", "call");
		newData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE;
		return true;
	}
	
	private static boolean parseNavigation(VoiceParseData newData, JSONObject newJson, JSONObject json) {
		/****************************新版本语义标准数据结构*********************************************/
		String operation = json.getString(JSON_KEY_OPERATION);
		if (TextUtils.isEmpty(operation)) {
			// 没有找到意图
			return false;
		}
		do {
			if ("navigation".equals(operation)) {
				JSONObject semantic = json.getJSONObject(JSON_KEY_SEMANTIC);
				if (null == semantic) {
					return false;
				}
				JSONArray slots = semantic.getJSONArray(JSON_KEY_SEMANTIC_SLOTS);
				if (null == slots || slots.isEmpty()) {
					return false;
				}
				JSONObject slot = slots.getJSONObject(0);
				int slot_struct = slot.getIntValue(JSON_KEY_SEMANTIC_SLOT_STRUCT);
				// 不同语义场景对应不同数据结构
				if (SLOT_STRUCT_LOCATION != slot_struct) {
					return false;
				}
				JSONArray values = slot.getJSONArray(JSON_KEY_SEMANTIC_VALUES);
				if (null == values || values.isEmpty()) {
					return false;
				}
				JSONObject value = values.getJSONObject(0);
				if (null == value) {
					return false;
				}
				String city = value.getString("city");
				// 关键字
				String name = value.getString("name");
				String district = value.getString("district");
				newJson.put("city", city);
				newJson.put("keywords", name);
				newJson.put("region", district);
				newJson.put("action", "search");
			} else {
				// TODO 目前使用不到的指令集
				return false;
			}
		} while (false);
		newData.floatTextScore = TextResultHandle.TEXT_SCORE_CONFIDENCE_LITTLE;
		newJson.put("scene", "nav");
		return true;
	}

}
