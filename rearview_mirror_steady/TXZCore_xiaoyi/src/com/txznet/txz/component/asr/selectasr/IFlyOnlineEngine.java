package com.txznet.txz.component.asr.selectasr;

import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.VoiceParseCommResult;
import com.txz.ui.voice.VoiceData.VoiceParseData;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.txzasr.IFlyPlugin;
import com.txznet.txz.component.asr.txzasr.IFlyPlugin.INetAsrCallBack;
import com.txznet.txz.component.tts.yunzhisheng_3_0.AudioSourceDistributer;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.record.Recorder;
import com.txznet.txz.ui.win.record.RecorderWin;


public class IFlyOnlineEngine {
	static IFlyOnlineEngine sModuleInstance = new IFlyOnlineEngine();
	private IFlyOnlineEngine() {
		Runnable oPluginRun = new Runnable() {
			@Override
			public void run() {
				mIFlyplugin.initialize();
			}
		};
		AppLogic.runOnBackGround(oPluginRun, 0);
	};
	public static IFlyOnlineEngine getInstance() {
		return sModuleInstance;
	}
	private IFlyPlugin mIFlyplugin = new IFlyPlugin();
	private NetAsrRecorder mRecorder = null;
	private AsrOption mAsrOption;
	private INetAsrCallBack mNetAsrCallBack = new INetAsrCallBack() {
		@Override
		public void onResult(String text) {
			fixNetResult(text, VoiceData.VOICE_DATA_TYPE_SENCE_JSON);
		}
		
		@Override
		public void onError(int errCode) {
			onEngineError(errCode);
		}

		@Override
		public void onSpeechBegin() {
		}

		@Override
		public void onSpeechEnd() {
		}
	};
	private void onEnd() {
			mIFlyplugin.cancel();
			delRecorder();
	}
	private void delRecorder() {
		if (mRecorder != null) {
			AudioSourceDistributer.getIntance().delRecorder(mRecorder);
			mRecorder = null;
		}
	}
	private void onEngineError(final int errCode) {
		onEnd();
		JNIHelper.logd("err="+errCode);

		switch (errCode) {
			case IAsr.ERROR_NO_MATCH: {
				RecorderWin.setLastUserText(NativeData.getResString("RS_USER_UNKNOW_TEXT"));
				VoiceParseCommResult result = new VoiceParseCommResult();
				result.boolLocal = true;
				result.boolManual = mAsrOption.mManual;
				result.strUserText = "";
				result.uint32ResultType = VoiceData.COMMON_RESULT_TYPE_UNKNOW;
				result.uint32GrammarId = mAsrOption.mGrammar;
				result.uint32GrammarCompileStatus = 1;
				result.uint64VoiceFileId = mAsrOption.mVoiceID;
				JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_COMMON_RESULT, result);
				break;
			}
			case IAsr.ERROR_NO_SPEECH:
				RecorderWin.setLastUserText(NativeData.getResString("RS_USER_EMPTY_TEXT"));
				VoiceParseCommResult result = new VoiceParseCommResult();
				result.boolLocal = true;
				result.boolManual = mAsrOption.mManual;
				result.strUserText = "";
				result.uint32ResultType = VoiceData.COMMON_RESULT_TYPE_EMPTY;
				result.uint32GrammarId = mAsrOption.mGrammar;
				result.uint32GrammarCompileStatus = 1;
				result.uint64VoiceFileId = mAsrOption.mVoiceID;
				JNIHelper.sendEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_COMMON_RESULT, result);
				break;
		}
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
			voice.strVoiceEngineId = "IFYONLINE";
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
	public class NetAsrRecorder extends Recorder {
		@Override
		public void write(byte[] data, int len) {
			mIFlyplugin.write(data, len);
		}
	}
	public synchronized boolean start(AsrOption asrOption) {
		if (asrOption == null)
			return false;
		mAsrOption = asrOption;
		Runnable oPluginRun = new Runnable() {
			@Override
			public void run() {
				int nRet = mIFlyplugin.start(mNetAsrCallBack, mAsrOption);
				JNIHelper.logd("ifyOnline:nRet = " + nRet);
			}
		};
		mRecorder = new NetAsrRecorder();
		AudioSourceDistributer.getIntance().addRecorder(mRecorder);
		AppLogic.runOnBackGround(oPluginRun, 0);
		return true;
	}
	public synchronized void stop() {
		delRecorder();
		mIFlyplugin.stop();
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
	
	public synchronized void cancel(){
		delRecorder();
		mIFlyplugin.cancel();

		if (mAsrOption != null && null != mAsrOption.mCallback){
			mAsrOption.mCallback.onCancel(mAsrOption);
		}
	}
}
