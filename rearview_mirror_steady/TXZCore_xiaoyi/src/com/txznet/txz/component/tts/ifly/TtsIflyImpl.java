package com.txznet.txz.component.tts.ifly;

import java.util.Locale;

import android.os.Bundle;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;
import com.iflytek.cloud.util.ResourceUtil.RESOURCE_TYPE;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.ModuleManager;

public class TtsIflyImpl implements ITts {

	// ////////////////////////////////////////////////////////////

	public static final String VOICER_NAME = "xiaoyan";
	public static final boolean USE_LOCAL_TTS = true;
	private  int VOICE_SPEED = 60;
	public static final int VOICE_PITCH = 50;
	public static final int VOICE_VOLUME = 100;
	public static final int SAMPLE_RATE = 8000;

	// ////////////////////////////////////////////////////////////

	SpeechSynthesizer mTts;
	InitListener mTtsInitListener;
	ITtsCallback mTtsCallback;

	// ////////////////////////////////////////////////////////////

	@Override
	public int initialize(final IInitCallback oRun) {
		ModuleManager.getInstance().initSdk_Ifly();
		
		mTtsInitListener = new InitListener() {
			@Override
			public void onInit(int code) {
				if (code == ErrorCode.SUCCESS) {
					oRun.onInit(true);
				} else {
					oRun.onInit(false);
				}
			}
		};
		mTts = SpeechSynthesizer.createSynthesizer(GlobalContext.get(),
				mTtsInitListener);
		return ERROR_SUCCESS;
	}

	@Override
	public void release() {
		mTts.destroy();
		mTts = null;
		mTtsInitListener = null;
	}

	// 获取发音人资源路径
	private String getResourcePath() {
		StringBuffer tempBuffer = new StringBuffer();
		// 合成通用资源
		tempBuffer.append(ResourceUtil.generateResourcePath(GlobalContext.get(),
				RESOURCE_TYPE.assets, "tts/common.jet"));
		tempBuffer.append(";");
		// 发音人资源
		tempBuffer.append(ResourceUtil.generateResourcePath(GlobalContext.get(),
				RESOURCE_TYPE.assets, "tts/" + VOICER_NAME + ".jet"));
		return tempBuffer.toString();
	}

	void setCommonParam() {
		mTts.setParameter(SpeechConstant.PARAMS, null);

		if (USE_LOCAL_TTS) {
			// 设置合成
			mTts.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_LOCAL);
			// 设置资源路径
			mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
		} else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE,
					SpeechConstant.TYPE_CLOUD);
		}

		// 设置发音人
		mTts.setParameter(SpeechConstant.VOICE_NAME, VOICER_NAME);
		// 设置语速
		mTts.setParameter(SpeechConstant.SPEED, "" + VOICE_SPEED);
		// 设置音调
		mTts.setParameter(SpeechConstant.PITCH, "" + VOICE_PITCH);
		// 设置音量
		mTts.setParameter(SpeechConstant.VOLUME, "" + VOICE_VOLUME);
		// 设置采样率
		mTts.setParameter(SpeechConstant.SAMPLE_RATE, "" + SAMPLE_RATE);
	}

	SynthesizerListener mTtsListener = new SynthesizerListener() {

		@Override
		public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (mTtsCallback != null) {
				ITtsCallback callback = mTtsCallback;
				mTtsCallback = null;
				if (error == null) {
					callback.onSuccess();
				} else if (error != null) {
					callback.onError(error.getErrorCode());
				}
			}
		}

		@Override
		public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeakBegin() {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeakPaused() {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeakProgress(int arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeakResumed() {
			// TODO Auto-generated method stub
		}
	};

	@Override
	public int start(int iStream, String sText, ITtsCallback oRun) {
		stop();
		
		setCommonParam();

		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "" + iStream); // 语音通话

		// 记录回调对象
		mTtsCallback = oRun;

		if (mTts.startSpeaking(sText, mTtsListener) != ErrorCode.SUCCESS) {
			return ERROR_UNKNOW;
		}
		return ERROR_SUCCESS;
	}

	@Override
	public int pause() {
		mTts.pauseSpeaking();
		return ERROR_SUCCESS;
	}

	@Override
	public int resume() {
		mTts.resumeSpeaking();
		return ERROR_SUCCESS;
	}

	@Override
	public void stop() {
		ITtsCallback callback = mTtsCallback;
		mTtsCallback = null;
		mTts.stopSpeaking(); // 先设置为防止这里会触发onDone
		if (callback != null)
			callback.onCancel();
	}

	@Override
	public boolean isBusy() {
		return mTts.isSpeaking();
	}

	@Override
	public int setLanguage(Locale loc) {
		// TODO 讯飞tts语种设置转换
		return 0;
	}

	@Override
	public void setTtsModel(String ttsModel) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVoiceSpeed(int speed){
		if (speed < 0){
			VOICE_SPEED = 0;
		}else if (speed > 100){
			VOICE_SPEED = 100;
		}else{
			if (speed == 50){
				VOICE_SPEED = 51;
			}else{
			    VOICE_SPEED = speed;
			}
		}
		JNIHelper.logd("speed = " + speed + ", VOICE_SPEED = " + VOICE_SPEED);
	}

	@Override
	public int getVoiceSpeed() {
		// TODO Auto-generated method stub
		return VOICE_SPEED;
	}

}
