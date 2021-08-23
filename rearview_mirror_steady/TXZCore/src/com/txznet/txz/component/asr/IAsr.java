package com.txznet.txz.component.asr;

import com.txz.ui.voice.VoiceData;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.choice.ChoiceManager;

public interface IAsr {
	/**
	 * 成功
	 */
	public static final int ERROR_SUCCESS = 0;
	/**
	 * 取消操作
	 */
	public static final int ERROR_CANCLE = 1;
	/**
	 * 启动异常
	 */
	public static final int ERROR_ABORT = 2;
	/**
	 * 未知错误
	 */
	public static final int ERROR_CODE = -1;

	public static final int ERROR_NO_MATCH = -2; // 不匹配
	public static final int ERROR_NO_SPEECH = -3; // 没有说话
	public static final int ERROR_ASR_ISBUSY = -4;// 语音识别正在进行中
	public static final int ERROR_ASR_NET_REQUEST = -5;// 网络不通
	public static final int ERROR_ASR_NET_NLU_EMTPY = -6;//语义为空
	public static final int ERROR_ASR_NO_USE = -1000;//该在线或者离线识别引擎未启用
	
	public static final String MONITOR_ALL = "all";
	public static final String MONITOR_NO_MATCH = "match";
	public static final String MONITOR_NO_SPEECH = "speech";
	public static final String MONITOR_ASR_BUSY = "busy";
	public static final String MONITOR_NO_REQUEST = "request";
	public static final String MONITOR_NO_NLP = "nlp";
	public static final String MONITOR_UPLOAD = "upload";
	public static final String MONITOR_UPLOAD_TOO_FAST = "upFast";
	public static final String MONITOR_UPLOAD_NET = "upNet";
	
	public static final float ASR_THRESH = -5.0f;

	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}

	public abstract class IAsrCallback {
		public void onSuccess(AsrOption option,
				VoiceData.VoiceParseData oVoiceParseData) {
		}

		/**
		 * 
		 * @param option
		 * @param error
		 * @param desc
		 *            界面提示的字符串内容
		 * @param speech
		 *            语音提示的字符串内容
		 * @param error2
		 *            ERROR_NOMATCH、ERROR_NOSPEECH等
		 */
		public void onError(AsrOption option, int error, String desc,
				String speech, int error2) {
		}

		public void onAbort(AsrOption option, int error) {
		}

		public void onCancel(AsrOption option) {
		}

		/**
		 * 开始录音
		 * 
		 * @param option
		 */
		public void onStart(AsrOption option) {
		}

		/**
		 * 录音结束，开始使用在线或离线识别
		 * 
		 * @param option
		 */
		public void onEnd(AsrOption option) {
		}

		/**
		 * 有人开始说话
		 * 
		 * @param option
		 */
		public void onSpeechBegin(AsrOption option) {
		}
		public void onVolume(AsrOption option, int volume) {
		}
		public void onSpeechEnd(AsrOption option){
		}
		public void onMonitor(String attr){
		}
		public void onPartialResult(AsrOption option, String partialResult) {}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////
	public enum AsrEngineType {
		/**
		 * 讯飞
		 */
		ASR_IFLY,
		/**
		 * 云知声
		 */
		ASR_YUNZHISHENG
	}

	/**
	 * 识别方式
	 * 
	 * @author txz
	 *
	 */
	public static enum AsrType {
		/**
		 * 自动识别模式，默认使用MIX，当出现高cpu占用并且网络可用时，自动切换为在线识别
		 */
		ASR_AUTO,
		/**
		 * 混合识别模式
		 */
		ASR_MIX,
		/**
		 * 在线
		 */
		ASR_ONLINE,
		/**
		 * 本地
		 */
		ASR_LOCAL,
		/**
		 * 默认模式，不更改原始模式
		 */
		ASR_DEFAULT
	}
	public static final int DEFAULT_SPEECH_TIMEOUT = 20000;
	public static class AsrOption {
		public Integer mId; // 回调标志，需要的话由使用方自己填写
		public Boolean mManual = true; // 手动触发标志
		public String mLanguage = "zh-cn";
		public String mAccent = "mandarin"; // 方言
		public Integer mBOS = null;
		public Integer mEOS = 700;
		public Integer mKeySpeechTimeout = IAsr.DEFAULT_SPEECH_TIMEOUT; // 录音总长度
		public Integer mGrammar = null; // VoiceData.GRAMMAR_SENCE_DEFAULT; //
										// 使用的语法场景
		public IAsrCallback mCallback = null;
		public Object mRecorder = null; // 需要自己录音，则传入录音对象
		public AsrType mAsrType = AsrType.ASR_MIX;
		public Boolean mNeedStopWakeup = true;
		public Boolean mPlayBeepSound = true; // 启动识别前是否播放“滴”声
		public Boolean mRecoverAsr = false;	//是否恢复识别的标志位
		public Integer mTtsId;//标记启动识别的tts
		public Integer mUID;//设备UID
		public Long mServerTime;//服务器时间
		public Boolean bServerTimeConfidence = false;//服务器时间是否可信
		public Boolean mEnableSemanticHint = false;//识别录音结束，等待语义结果返回，等待时间过长时进行提示
		
		public Boolean getPlayBeepSound() {
			return mPlayBeepSound;
		}

		public AsrOption setPlayBeepSound(Boolean mPlayBeepSound) {
			this.mPlayBeepSound = mPlayBeepSound;
			return this;
		}

		public long mBeginSpeechTime = 0; // 本次唤醒开始说话的时间点
		public String mDirectAsrKw = null;//本次直接识别交互的唤醒词
		
		public long mVoiceID = 0;
		public AsrOption setNeedStopWakeup(boolean b) {
			if (InterruptTts.getInstance().isInterruptTTS()) {
				mNeedStopWakeup = b;
			}else {
				mNeedStopWakeup = b || (ChoiceManager.getInstance().isCoexistAsrAndWakeup() == false);
			}
			return this;
		}

		public AsrOption setManual(boolean Manual) {
			this.mManual = Manual;
			return this;
		}

		public AsrOption setLanguage(String Language) {
			this.mLanguage = Language;
			return this;
		}

		public AsrOption setAccent(String Accent) {
			this.mAccent = Accent;
			return this;
		}

		public AsrOption setBOS(int BOS) {
			this.mBOS = BOS;
			return this;
		}

		public AsrOption setEOS(int EOS) {
			this.mEOS = EOS;
			return this;
		}

		public AsrOption setKeySpeechTimeout(int KeySpeechTimeout) {
			this.mKeySpeechTimeout = KeySpeechTimeout;
			return this;
		}

		public AsrOption setCallback(IAsrCallback Callback) {
			mCallback = Callback;
			return this;
		}

		public AsrOption setGrammar(int Grammar) {
			this.mGrammar = Grammar;
			return this;
		}
		
		public AsrOption setEnableSemanticHint(boolean enableSemanticHit) {
			this.mEnableSemanticHint = enableSemanticHit;
			return this;
		}

		public AsrOption check() {
			if (this.mBOS == null) {
				if (this.mManual)
					this.mBOS = 3000;
				else
					this.mBOS = 5000;
			}
			if (this.mGrammar == null) {
				this.mGrammar = AsrManager.getInstance().getCurrentGrammarId();
			}
			return this;
		}
	}

	public int initialize(final IInitCallback oRun);

	public void release();

	public int start(AsrOption oOption);

	public void stop();

	public void cancel();

	public boolean isBusy();

	// ///////////////////////////////////////////////////////////////

	public interface IBuildGrammarCallback {
		public void onSuccess(VoiceData.SdkGrammar oGrammarData);

		public void onError(int error, VoiceData.SdkGrammar oGrammarData);
	}

	public boolean buildGrammar(VoiceData.SdkGrammar oGrammarData,
			IBuildGrammarCallback oCallback);

	// ///////////////////////////////////////////////////////////////

	public interface IImportKeywordsCallback {
		public final static int ERROR_ENGINE_NOT_READY = -1000;//插词时,引擎尚未准备好,主要是在线识别引擎, 同行者2.0版本
		public final static int ERROR_UPLOAD_TOO_FAST = -1001;//插词时,引擎尚未准备好,主要是在线识别引擎, 同行者2.0版本
		public void onSuccess(VoiceData.SdkKeywords mSdkKeywords);

		public void onError(int error, VoiceData.SdkKeywords mSdkKeywords);
	}

	public boolean importKeywords(VoiceData.SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback);

	public void releaseBuildGrammarData();
	
	public void retryImportOnlineKeywords();

	public void insertVocab_ext(int nGrammar, StringBuffer vocab);
}
