package com.txznet.txz.component.asr;

import com.txz.ui.voice.VoiceData;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.module.asr.AsrManager;

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
		ASR_LOCAL
	}

	public static class AsrOption {
		public Integer mId; // 回调标志，需要的话由使用方自己填写
		public Boolean mManual = true; // 手动触发标志
		public String mLanguage = "zh-cn";
		public String mAccent = "mandarin"; // 方言
		public Integer mBOS = null;
		public Integer mEOS = 700;
		public Integer mKeySpeechTimeout = 10000; // 录音总长度
		public Integer mGrammar = null; // VoiceData.GRAMMAR_SENCE_DEFAULT; //
										// 使用的语法场景
		public IAsrCallback mCallback = null;
		public Object mRecorder = null; // 需要自己录音，则传入录音对象
		public AsrType mAsrType = AsrType.ASR_AUTO;
		public Boolean mNeedStopWakeup = true;
		
		public long mVoiceID = 0;
		public AsrOption setNeedStopWakeup(boolean b) {
			mNeedStopWakeup = b || (ProjectCfg.mCoexistAsrAndWakeup == false);
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
		public void onSuccess(VoiceData.SdkKeywords mSdkKeywords);

		public void onError(int error, VoiceData.SdkKeywords mSdkKeywords);
	}

	public boolean importKeywords(VoiceData.SdkKeywords oKeywords,
			IImportKeywordsCallback oCallback);

	public void releaseBuildGrammarData();
	
	public void retryImportOnlineKeywords();

	public void insertVocab_ext(int nGrammar, StringBuffer vocab);
}
