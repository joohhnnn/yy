package com.txznet.txz.component.asr.ifly;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.txz.ui.data.UiData;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.SdkGrammar;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.ModuleManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.Pcm2Wav;
import com.txznet.txz.util.SDCardUtil;
import com.txznet.txz.util.SystemInfo;

public class AsrIflyImpl implements IAsr {
	static final String RESULT_TYPE_XML = "xml";
	static final String RESULT_TYPE_JSON = "json";
	static final String RESULT_TYPE_RAW = "rst";

	static final boolean CFG_ENABLE_LOCAL = true;

	static final String AUDIO_FILE_PREFIX = Environment
			.getExternalStorageDirectory().getPath() + "/txz/txz";
	static final String PCM_AUDIO_SAVE_PATH = AUDIO_FILE_PREFIX + ".pcm";
	static final String AMR_AUDIO_SAVE_PATH = AUDIO_FILE_PREFIX + ".amr";
	static final String WAV_AUDIO_SAVE_PATH = AUDIO_FILE_PREFIX + ".wav";
	static final String WAV_AUDIO_BACKUP_PATH = AUDIO_FILE_PREFIX
			+ "_backup.wav";

	SpeechRecognizer mIat;
	AsrOption mAsrOption;

	@Override
	public int initialize(final IInitCallback oRun) {
		ModuleManager.getInstance().initSdk_Ifly();

		mIat = SpeechRecognizer.createRecognizer(GlobalContext.get(),
				new InitListener() {
					@Override
					public void onInit(int code) {
						if (code == ErrorCode.SUCCESS) {
							oRun.onInit(true);
						} else {
							oRun.onInit(false);
						}
					}
				});

		return 0;
	}

	@Override
	public void release() {
		stop();
		mIat.destroy();
		mIat = null;
	}

	@Override
	public int start(AsrOption oOption) {
		mAsrOption = oOption;

		start_startRecorder();

		return ERROR_SUCCESS;
	}

	int mResultType = 0;

	boolean start_startRecorder() {
		boolean bUseLocal = false; // 强制使用本地引擎
		// TODO 网络不好时强制离线

		switch (NetworkManager.getInstance().getNetType()) {
		case UiData.NETWORK_STATUS_3G:
		case UiData.NETWORK_STATUS_4G:
		case UiData.NETWORK_STATUS_WIFI:
			break;
		case UiData.NETWORK_STATUS_2G:
		case UiData.NETWORK_STATUS_NONE:
		case UiData.NETWORK_STATUS_FLY:
		case UiData.NETWORK_STATUS_UNKNOW:
			bUseLocal = true;
			break;
		}

		if (!bUseLocal && mAsrOption.mManual) {
			switch (mAsrOption.mGrammar) {
			case VoiceData.GRAMMAR_SENCE_DEFAULT:
			case VoiceData.GRAMMAR_SENCE_MAKE_CALL:
			case VoiceData.GRAMMAR_SENCE_NAVIGATE:
			case VoiceData.GRAMMAR_SENCE_SET_HOME:
			case VoiceData.GRAMMAR_SENCE_SET_COMPANY: {
				break;
			}
			case VoiceData.GRAMMAR_SENCE_SELECT_WITH_CANCEL:
			case VoiceData.GRAMMAR_SENCE_CALL_MAKE_SURE:
			case VoiceData.GRAMMAR_SENCE_CALL_SELECT:
			case VoiceData.GRAMMAR_SENCE_SMS_MAKE_SURE:
			case VoiceData.GRAMMAR_SENCE_INCOMING_MAKE_SURE:
				bUseLocal = true;
				break;
			}
		}
		// 此时 bUseLocal表示了网络状态，true表示网络状态差，只能使用离线，false表示能够使用同时使用在线, mix
		boolean bUseMix = !bUseLocal; // 是否使用混合识别
		boolean bUseUnderstander = !bUseLocal; // 是否使用语音识别

		// 选择识别模式
		switch (mAsrOption.mAsrType) {
		case ASR_AUTO:
			int numCore = 1;
			numCore = SystemInfo.getNumCores();
			JNIHelper.logd("numCore = " + numCore);
			if (numCore < 4) {
				bUseMix = false;
			}
			break;
		case ASR_MIX:
			bUseMix = true;
		case ASR_LOCAL:
			bUseMix = false;
			bUseUnderstander = false;
			bUseLocal = true;
		case ASR_ONLINE:
			bUseLocal = false;
			bUseMix = false;
			bUseUnderstander = true;
		default:
			break;
		}

		// 设置本地语法
		if (bUseMix || bUseLocal) {
			VoiceData.GrammarInfo grammar = getSeneceGrammarInfo(mAsrOption.mGrammar);
			setGrammarParams(grammar);
			mIat.setParameter(SpeechConstant.GRAMMAR_LIST, null);
			mIat.setParameter(SpeechConstant.LOCAL_GRAMMAR, grammar.strId);
			mIat.setParameter(SpeechConstant.MIXED_THRESHOLD, "30"); // 识别门限值,用在本地识别中
		}

		// 离线在线设置
		if (bUseLocal) {
			mIat.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_LOCAL);
			// 检测是否只读
			if (SDCardUtil.checkSDCardError() == -1) {
				onAbort(0);
				return false;
			}
		} else {
			mIat.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_CLOUD);
		}

		// 设置混合模式
		if (bUseMix) {
			mIat.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_MIX);
			mIat.setParameter("mixed_type", "delay");
			mIat.setParameter("mixed_timeout", "15000");
		}

		// 设置增加在线语义识别
		if (bUseUnderstander) {
			mIat.setParameter("sch", "1");
			mIat.setParameter("nlp_version", "2.0");
			mIat.setParameter(SpeechConstant.DOMAIN, "iat"); // 设置在线语义识别领域
		}

		// 根据手动模式设置前端静音超时
		mIat.setParameter(SpeechConstant.VAD_BOS, "" + mAsrOption.mBOS);
		mIat.setParameter(SpeechConstant.VAD_EOS, "" + mAsrOption.mEOS);

		// 通用参数设置
		mIat.setParameter(SpeechConstant.LANGUAGE, mAsrOption.mLanguage);// 设置语言
		mIat.setParameter(SpeechConstant.ACCENT, mAsrOption.mAccent);// 设置语言区域
		mIat.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, ""
				+ mAsrOption.mKeySpeechTimeout); // 设置语音超时
		mIat.setParameter(SpeechConstant.ASR_PTT, "0");// 设置标点符号
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, PCM_AUDIO_SAVE_PATH);// 设置音频保存路径
		mIat.setParameter(SpeechConstant.RESULT_TYPE, RESULT_TYPE_JSON); // 设置结果类型
		// mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1"
		// /*MediaRecorder.AudioSource.MIC*/); // 设置音频源
		// mIat.setParameter(SpeechConstant.SAMPLE_RATE, "8000"); //设置采样率
		JNIHelper.logd("params: " + mIat.getParameter(SpeechConstant.PARAMS));

		int ret = mIat.startListening(mRecognizerListener);
		if (ret != ErrorCode.SUCCESS) {
			onAbort(ret);
			return false;
		}

		if (mIat.getParameter(SpeechConstant.ENGINE_TYPE) == SpeechConstant.TYPE_MIX)
			mResultType = VoiceData.VOICE_DATA_TYPE_MIX_JSON;
		else if (mIat.getParameter(SpeechConstant.RESULT_TYPE) == RESULT_TYPE_XML)
			mResultType = VoiceData.VOICE_DATA_TYPE_XML;
		else if (mIat.getParameter(SpeechConstant.RESULT_TYPE) == RESULT_TYPE_JSON)
			mResultType = VoiceData.VOICE_DATA_TYPE_LOCAL_JSON;
		else if (mIat.getParameter(SpeechConstant.RESULT_TYPE) == RESULT_TYPE_RAW)
			mResultType = VoiceData.VOICE_DATA_TYPE_RAW;

		if (mAsrOption.mCallback != null) {
			mAsrOption.mCallback.onStart(mAsrOption);
		}

		return true;
	}

	RecognizerListener mRecognizerListener = new RecognizerListener() {
		@Override
		public void onBeginOfSpeech() {
			if (mAsrOption.mCallback != null) {
				mAsrOption.mCallback.onSpeechBegin(mAsrOption);
			}
		}

		@Override
		public void onEndOfSpeech() {
			if (mAsrOption.mCallback != null) {
				mAsrOption.mCallback.onSpeechEnd(mAsrOption);
				mAsrOption.mCallback.onEnd(mAsrOption);
			}
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			saveWavFile();

			if (null != mAsrOption.mCallback) {
				VoiceData.VoiceParseData voice = new VoiceData.VoiceParseData();
				voice.uint32DataType = mResultType;
				voice.strVoiceData = results.getResultString();
				if (mAsrOption.mManual)
					voice.boolManual = 1;
				else
					voice.boolManual = 0;
				voice.uint32Sence = mAsrOption.mGrammar;
				mAsrOption.mCallback.onSuccess(mAsrOption, voice);
			}
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			if (null != mAsrOption.mCallback) {
				mAsrOption.mCallback.onVolume(mAsrOption, volume);
			}
		}

		@Override
		public void onError(SpeechError error) {
			if (error.getErrorCode() == ErrorCode.ERROR_AUDIO_RECORD) {
				onAbort(error.getErrorCode());
				return;
			}

			stopInner();
			String strHint = NativeData.getResString("RS_ASR_RECOGNIZE_ERROR");
			String strHintTts = null;
			int error2 = ERROR_CODE;
			if (error.getErrorCode() == ErrorCode.ERROR_AITALK_GRM_NOT_UPDATE) {
				strHint = NativeData.getResString("RS_ASR_GRAMMAR_UPDATE");
			} else if (error.getErrorCode() == ErrorCode.ERROR_NO_MATCH) {
				// strHint = "当前网络环境较差，不支持该说法";
				strHint = null;
				error2 = ERROR_NO_MATCH;
			} else if (/*error.getErrorCode() == ErrorCode.ERROR_NO_SPPECH
					|| */error.getErrorCode() == ErrorCode.MSP_ERROR_NO_DATA) {
				// strHint = "您好像没有说话";
				strHint = null;
				error2 = ERROR_NO_SPEECH;
			} else {
				strHint = "错误[" + error.getErrorCode() + "]-"
						+ error.getErrorDescription();
				strHintTts = error.getErrorDescription();
			}
			if (mAsrOption.mCallback != null) {
				mAsrOption.mCallback.onError(mAsrOption, error.getErrorCode(),
						strHint, strHintTts, error2);
			}
		}
	};

	void saveWavFile() {
		String sampleRate = mIat.getParameter(SpeechConstant.SAMPLE_RATE);
		if (null == sampleRate || sampleRate.length() == 0) {
			sampleRate = "16000";
		}
		File fWav = new File(WAV_AUDIO_SAVE_PATH);
		if (fWav.exists()) {
			FileUtil.copyFile(WAV_AUDIO_SAVE_PATH, WAV_AUDIO_BACKUP_PATH);
			fWav.delete();
		}
		Pcm2Wav.encode(PCM_AUDIO_SAVE_PATH, WAV_AUDIO_SAVE_PATH,
				Integer.parseInt(sampleRate));
	}

	static VoiceData.GrammarInfo getSeneceGrammarInfo(int sence) {
		VoiceData.GrammarInfo grammarInfo = null;
		try {
			grammarInfo = VoiceData.GrammarInfo.parseFrom(NativeData
					.getNativeData(UiData.DATA_ID_VOICE_GRAMMAR_INFO, ""
							+ sence));

		} catch (InvalidProtocolBufferNanoException e) {
			e.printStackTrace();
			return null;
		}
		if (grammarInfo == null || grammarInfo.strId == null
				|| grammarInfo.strId.length() <= 0)
			return null;
		return grammarInfo;
	}

	void setGrammarParams(VoiceData.GrammarInfo grammarInfo) {
		mIat.setParameter(SpeechConstant.PARAMS, null);
		if (grammarInfo != null && grammarInfo.strId != null
				&& grammarInfo.strId.length() > 0) {
			mIat.setParameter(ResourceUtil.GRM_BUILD_PATH,
					grammarInfo.strBuildPath);
			// 设置资源路径
			mIat.setParameter(ResourceUtil.ASR_RES_PATH, ResourceUtil
					.generateResourcePath(GlobalContext.get(),
							RESOURCE_TYPE.assets, "asr/common.jet"));
			mIat.setParameter(SpeechConstant.GRAMMAR_LIST, grammarInfo.strId);
			mIat.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_LOCAL);
		} else {
			mIat.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_CLOUD);
		}
		mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
	}

	void stopInner() {
		mIat.cancel();
	}

	void onAbort(int abort) {
		// 启动录音发生异常
		stopInner();

		if (mAsrOption.mCallback != null) {
			mAsrOption.mCallback.onAbort(mAsrOption, abort);
		}
	}

	@Override
	public void cancel() {
		boolean bBusy = isBusy();
		IAsrCallback cb = null;
		if (null != mAsrOption) {
			cb = mAsrOption.mCallback;
			mAsrOption.mCallback = null; // 先置空
		}
		stopInner();
		if (cb != null && bBusy) {
			cb.onCancel(mAsrOption);
		}
	}

	@Override
	public void stop() {
		mIat.stopListening();
	}

	@Override
	public boolean isBusy() {
		return mIat.isListening();
	}

	class TXZGrammarListener implements GrammarListener {
		VoiceData.SdkGrammar mParam;

		public TXZGrammarListener(VoiceData.SdkGrammar param) {
			mParam = param;
		}

		@Override
		public void onBuildFinish(String grammarId, SpeechError error) {
			if (error != null) {
				if (null != mBuildGrammarCallback) {
					mBuildGrammarCallback.onError(error.getErrorCode(), mParam);
				}
			} else {
				if (null != mBuildGrammarCallback) {
					mBuildGrammarCallback.onSuccess(mParam);
				}
			}
		}
	}

	BuildGrammarClient mBuildGrammarClient = new BuildGrammarClient();

	IBuildGrammarCallback mBuildGrammarCallback;

	@Override
	public boolean buildGrammar(SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback) {
		if (!CFG_ENABLE_LOCAL)
			return false;
		mBuildGrammarCallback = oCallback;
		TXZGrammarListener grammarListener = new TXZGrammarListener(
				oGrammarData);
		setGrammarParams(oGrammarData.msgGrammarInfo);
		int ret = mIat.buildGrammar("bnf", oGrammarData.strContent,
				grammarListener);
		if (ret != ErrorCode.SUCCESS) {
			SpeechError error = new SpeechError(0, "异常码:(" + ret + ")");
			grammarListener.onBuildFinish(oGrammarData.msgGrammarInfo.strId,
					error);
		}
		return false;
	}

	@Override
	public boolean importKeywords(SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback) {
		if (!CFG_ENABLE_LOCAL && oKeywords.msgGrammarInfo.strId != null
				&& oKeywords.msgGrammarInfo.strId.length() > 0)
			return false;
		mBuildGrammarClient.regKeywords(oKeywords, oCallback);
		return true;
	}

	@Override
	public void releaseBuildGrammarData() {
		mBuildGrammarClient.unbind();
	}

	@Override
	public void retryImportOnlineKeywords() {

	}

	@Override
	public void insertVocab_ext(int nGrammar, StringBuffer vocab) {

	}

}
