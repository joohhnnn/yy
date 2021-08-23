package com.txznet.txz.component.asr.mix.local;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Looper;
import android.text.TextUtils;
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
			case SpeechConstants.ASR_EVENT_COMPILER_INIT_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILER_INIT_DONE");
				bCompilerInited = true;
				break;
			case SpeechConstants.ASR_EVENT_COMPILE_VOCAB_DONE:
				JNIHelper.logd("ASR_EVENT_COMPILE_DONE");
				isbCompilerVocabDone = true;
				break;
			case SpeechConstants.ASR_EVENT_LOADGRAMMAR_DONE:
				JNIHelper.logd("ASR_EVENT_LOADGRAMMAR_DONE");
				if (isbCompilerVocabDone) {
					onImportFixKeywords(true, 0);
					isbCompilerVocabDone = false;
				}
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
			case SpeechConstants.ASR_EVENT_INIT_DONE:
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

	// {"errorCode":-91002,"errorMsg":"请求初始化错误"}
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

	private IInitCallback mInitCallback = null;
	private Context mContext = null;
	@Override
	public int initialize(IInitCallback oRun) {
		mInitCallback = oRun;
		mContext = new TXZContext(GlobalContext.get(), ProjectCfg.getYzsFileDir());
		if (Looper.myLooper() == null) {
			Looper.prepare();
		}
		mSpeechUnderstander = new SpeechUnderstander(mContext,
				ProjectCfg.getYunzhishengAppId()//
				, ProjectCfg.getYunzhishengSecret());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_SERVICE_MODE,
				SpeechConstants.ASR_SERVICE_MODE_LOCAL);
		mSpeechUnderstander.setListener(mSpeechUnderstanderListener);
		//默认关闭引擎LOG
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_ENGINE_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_PRINT_LOG, DebugCfg.debug_yzs());
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_ADVANCE_INIT_COMPILER, false);
		mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_DEBUG_SAVELOG, false);
		JNIHelper.logd("init asr vesioninfo ->SDK=" + mSpeechUnderstander.getVersion());
		JNIHelper.logd("init asr vesioninfo ->ENGINE =" + mSpeechUnderstander.getFixEngineVersion());
		JNIHelper.logd("init asr begin");
		int nRet = 0;
		mSpeechUnderstander.init("");
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
				mConfig.setSaveDataPath("offline"+oOption.mVoiceID+"");
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
	
	// 判断离线识别是否有说话
	private boolean isSilenceOnLocalAsr(float score) {
		boolean bRet = false;
		do {

			//识别分数大于20.0f
			if (score > -15.0f) {
				break;
			}
			
			bRet = !mHasSpeaking;
		} while (false);
		
		return bRet;
	}
	
	private void onSuccess(final float score, final String jsonResult) {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				if (mAsrOption != null && mAsrOption.mCallback != null) {
					//没有说话判断
					if ((score > -20 - DOUBLE && score < -20 + DOUBLE ) || isSilenceOnLocalAsr(score)){
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
						onSuccess(score, result);
						mSpeechUnderstander.cancel();//云知声72版本离线会连续识别, 所以需要主动停掉
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
		bCompiling = false;//结束离线编译状态
		
		/**********确保回调之前DestroyCompiler任务已经添加到队列中********************/
		destroyCompiler();
		/******** **这样子才能保证initCompiler中remove掉添加的DestroyCompiler任务****/
		
		final IImportKeywordsCallback oCallback = mSetDataCallback;
		mSetDataCallback = null;
		final SdkKeywords setDataSdkKeywords = mSetDataSdkKeywords;
		mSetDataSdkKeywords = null;//主动解引用,因为sdkKeywords数据量可能比较大
		if (oCallback != null){
			if (bSuccessed){
				oCallback.onSuccess(setDataSdkKeywords);
			}else{
				LogUtil.loge("onImportFixKeywords_error : " + mSpeechUnderstander.getOption(SpeechConstants.ASR_OPT_GET_COMPILE_ERRORINFO));
				oCallback.onError(code, setDataSdkKeywords);
			}
		}else{
			LogUtil.loge("onImportFixKeywords oCallback = null");
		}
	}
	
	public boolean importKeywords(final SdkKeywords oKeywords, final IImportKeywordsCallback oCallback){
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				importKeywordsSync(oKeywords, oCallback);
			}
		};
		AsrWakeupEngine.getEngine().runOnCompileBackGround(oRun, 0);
		return true;
	}
	
	private boolean importKeywordsSync(SdkKeywords oKeywords, IImportKeywordsCallback oCallback) {
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
				initCompiler();
				bCompiling = true;//进入离线编译状态
				JNIHelper.logd("insertVocab " + key);
				int nRet = 0;
				nRet = mSpeechUnderstander.insertVocab(KeyNamesList, "txzTag#" + key);
				JNIHelper.logd("insertVocab[" + key + "] : nRet = " + nRet);
				error = nRet;
				//insertVocab现在是异步接口
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
			// 非云知声离线识别词表
			if (oCallback != null) {
				oCallback.onSuccess(oKeywords);
			}
		}
		
		return true;
	}

	//是否是词表编译完成
	private boolean isbCompilerVocabDone = false;
	
	/********initCompiler和oDestroyCompilerRun均实际运行在同一个线程内,避免多线程问题********/
	private final static int  COMPILE_MAX_PRONUNCIATION = 6*6*4*4*3*3*2*2;//离线词条最大发音长度,超过该长度，编译过程中会报错。SDK默认长度为20，这个限制太短了。
	private boolean bCompilerInited = false;
	private boolean bCompiling = false;//当前是否正在编译离线词条
	private void initCompiler(){
		//清掉之前的DestroyCompiler任务
		AsrWakeupEngine.getEngine().delOnCompileBackGround(oDestroyCompilerRun);
		if (bCompilerInited){
			return;
		}
		
		JNIHelper.logd("initCompiler");
		mSpeechUnderstander.initCompiler();
		long nCount = 0;
		while(!bCompilerInited && nCount < 100){
			nCount++;
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		/*****编译模块成功初始化后，才能操作与其相关的操作*******/
		if (bCompilerInited){
			mSpeechUnderstander.loadCompiledJsgf("txzTag", GlobalContext.get().getApplicationInfo().dataDir + "/data/txz.dat");// 加载编译语法
		}
		/**********设置最大发音长度,避免特殊词条撑爆内存**********/
		if (bCompilerInited){
			mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_SET_COMPILE_MAX_PRONUNCIATION, COMPILE_MAX_PRONUNCIATION);//设置最大发音长度
			//mSpeechUnderstander.setOption(SpeechConstants.ASR_OPT_SET_OVER_MAX_PRONUNCIATION_INSERT,  false);//忽略超过最长发音长度的词条
		}
		JNIHelper.logd("initCompiler bCompilerInited = " + bCompilerInited);
	}
	
	private Runnable oDestroyCompilerRun = new Runnable() {
		@Override
		public void run() {
			//编译期间调用destroyCompiler接口引擎会崩溃，所以添加一个状态进行保护，防止极端情况发生
			if (bCompilerInited && !bCompiling){
				JNIHelper.logd("destroyCompiler begin");
				mSpeechUnderstander.destoryCompiler();
				bCompilerInited = false;
				JNIHelper.logd("destroyCompiler");
			}
		}
	};
	
	private boolean bNeedDestroyCompiler = true;
	//延时5秒释放Compiler,避免连续分配和释放大内存
	private void destroyCompiler(){
		//72版本destroyCompiler后没法重新初始化Compiler使用
		if (!bNeedDestroyCompiler){
			return;
		}
		AsrWakeupEngine.getEngine().delOnCompileBackGround(oDestroyCompilerRun);
		AsrWakeupEngine.getEngine().runOnCompileBackGround(oDestroyCompilerRun, 5*1000);
	}
	/*****************initCompiler和oDestroyCompilerRun均实际运行在同一个线程内,避免多线程问题****************/
}
