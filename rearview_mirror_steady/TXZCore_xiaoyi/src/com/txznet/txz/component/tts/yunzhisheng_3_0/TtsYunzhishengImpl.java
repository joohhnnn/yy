package com.txznet.txz.component.tts.yunzhisheng_3_0;

import java.io.File;
import java.util.Locale;

import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.jni.JNIHelper;
import com.unisound.client.IAudioSource;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechSynthesizer;
import com.unisound.client.SpeechSynthesizerListener;

public class TtsYunzhishengImpl implements ITts {
	ITtsCallback mTtsCallback;

	private SpeechSynthesizer mTTSPlayer;
	private boolean mIsBusy = false;
    
	public static final String VOICER_NAME = "xiaoli";
	public static final boolean USE_LOCAL_TTS = true;
	private  int VOICE_SPEED = 70;
	public static final int VOICE_PITCH = 50;
	public static final int VOICE_VOLUME = 100;
	public static final int SAMPLE_RATE = 8000;
	public static final int START_BUFFER_TIME = 0;
	public static final String DEFAULT_BACKEND_MODEL = GlobalContext.get().getApplicationInfo().dataDir + "/data/backend_female";
	public static final String DEFAULT_FRONTEND_MODEL = GlobalContext.get().getApplicationInfo().dataDir +"/data/frontend_model";
	
	private IAudioSource mAudioSource = null;
	IInitCallback mInitCallback = null;
	
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
	public int getVoiceSpeed(){
		return VOICE_SPEED;
	}
	
	@Override
	public int initialize(IInitCallback oRun) {
		mInitCallback = oRun;
		
		//AppID为空时
		if (ProjectCfg.getYunzhishengAppId() ==null || ProjectCfg.getYunzhishengSecret() == null ||
				ProjectCfg.getYunzhishengAppId().isEmpty() || ProjectCfg.getYunzhishengSecret().isEmpty()){
			mTTSPlayer = null;
			JNIHelper.logw("AppId or Secret is Empty!!!");
			Runnable run = new Runnable() {
				@Override
				public void run() {
					if  (mInitCallback != null){
						mInitCallback.onInit(true);
					}
				}
			};
			AppLogic.runOnBackGround(run, 0);
			return 0;
		}
	
		mTTSPlayer = new SpeechSynthesizer(GlobalContext.get(), 
				                      ProjectCfg.getYunzhishengAppId(),
				                      ProjectCfg.getYunzhishengSecret());
		mTTSPlayer.setOption(SpeechConstants.TTS_SERVICE_MODE,
				                           SpeechConstants.TTS_SERVICE_MODE_LOCAL);
		mTTSPlayer.setOption(SpeechConstants.TTS_KEY_FRONTEND_MODEL_PATH, DEFAULT_FRONTEND_MODEL);
		mTTSPlayer.setOption(SpeechConstants.TTS_KEY_BACKEND_MODEL_PATH,  DEFAULT_BACKEND_MODEL);
		mTTSPlayer.setOption(SpeechConstants.TTS_KEY_IS_DEBUG, true);
		mTTSPlayer.setTTSListener(new SpeechSynthesizerListener() {

			@Override
			public void onEvent(int type) {
				switch (type) {
				case SpeechConstants.TTS_EVENT_INIT:
					JNIHelper.logd("TTS_EVENT_INIT");
					mAudioSource = new TxzAudioSourceImpl();
					mTTSPlayer.setAudioSource(mAudioSource);
					if (mInitCallback != null){
						 Runnable oRun = new Runnable() {
								@Override
								public void run() {
									mInitCallback.onInit(true);
									mInitCallback = null;
								}
							};
							AppLogic.runOnBackGround(oRun, 0);
					}
					break;
				case SpeechConstants.TTS_EVENT_SYNTHESIZER_START:
					// 开始合成回调
					JNIHelper.logd("beginSynthesizer");
					break;
				case SpeechConstants.TTS_EVENT_SYNTHESIZER_END:
					// 合成结束回调
					JNIHelper.logd("endSynthesizer");
					break;
				case SpeechConstants.TTS_EVENT_BUFFER_BEGIN:
					// 开始缓存回调
					JNIHelper.logd("beginBuffer");
					break;
				case SpeechConstants.TTS_EVENT_BUFFER_READY:
					// 缓存完毕回调
					JNIHelper.logd("bufferReady");
					break;
				case SpeechConstants.TTS_EVENT_PLAYING_START:
					// 开始播放回调
					JNIHelper.logd("onPlayBegin");
//					AppLogic.removeBackGroundCallback(closeTtsWhenOverTimeRunnable);
					break;
				case SpeechConstants.TTS_EVENT_PLAYING_END:
					// 播放完成回调
					JNIHelper.logd("onPlayEnd");
					if (mTtsCallback != null){
						 Runnable oRun = new Runnable() {
								@Override
								public void run() {
									mTtsCallback.onSuccess();
									mIsBusy = false;
								}
							};
							AppLogic.runOnBackGround(oRun, 0);
					}
					break;
				case SpeechConstants.TTS_EVENT_PAUSE:
					// 暂停回调
					JNIHelper.logd("pause");
					break;
				case SpeechConstants.TTS_EVENT_RESUME:
					// 恢复回调
					JNIHelper.logd("resume");
					break;
				case SpeechConstants.TTS_EVENT_STOP:
					// 停止回调
					JNIHelper.logd("stop");
					mIsBusy = false;
					break;
				case SpeechConstants.TTS_EVENT_RELEASE:
					// 释放资源回调
					JNIHelper.logd("release");
					break;
				case SpeechConstants.TTS_EVENT_SWITCH_FRONTEND_MODEL_SUCCESS:
					// 切换TTS模型成功
					JNIHelper.logd("TTS_EVENT_SWITCH_FRONTEND_MODEL_SUCCESS");
					break;
				default:
					JNIHelper.logd("type =" + type);
					break;
				}

			}

			@Override
			public void onError(int type, String errorMSG) {
				// 语音合成错误回调
				if (mInitCallback != null){
					JNIHelper.loge("onInit onError " + type + ": " + errorMSG);
					mInitCallback.onInit(false);
					mInitCallback = null;
					return;
				}
				
				final int error = type;
				JNIHelper.loge("onError " + type + ": " + errorMSG);
				if (mTtsCallback != null){
					 Runnable oRun = new Runnable() {
							@Override
							public void run() {
								mTtsCallback.onError(error);
								mIsBusy = false;
							}
						};
						AppLogic.runOnBackGround(oRun, 0);
				}
			}
		});
		JNIHelper.logd("TTS init");
		int nRet = -1;
		// 初始化合成引擎
		nRet = mTTSPlayer.init(null);
		JNIHelper.logd("nRet = " + nRet);
		return 0;
	}

	@Override
	public void release() {
         //mTTSPlayer.release(arg0, arg1);
		mTTSPlayer = null;
	}
	Runnable closeTtsWhenOverTimeRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mTtsCallback != null)
				mTtsCallback.onError(3);
		}
	};
	@Override
	public int start(int iStream, String sText, ITtsCallback oRun) {
		if (mTTSPlayer == null) {
			JNIHelper.logw("mTTSPlayer == null");
			mTtsCallback = oRun;
			Runnable run = new Runnable() {
				@Override
				public void run() {
					if (mTtsCallback != null) {
						mTtsCallback.onSuccess();
						mIsBusy = false;
					}
				}
			};
			AppLogic.runOnBackGround(run, 0);
			return 0;
		}
		
		JNIHelper.logd("streamtype = " + iStream);
		TxzAudioSourceImpl.setStreamType(iStream);
		// 参数设置
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_STREAM_TYPE,   iStream);
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_VOICE_NAME,  VOICER_NAME);
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_VOICE_SPEED, VOICE_SPEED);

        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_FRONT_SILENCE,  0);
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_BACK_SILENCE,  100);
        
        mTTSPlayer.setOption(SpeechConstants.TTS_KEY_VOICE_VOLUME,  VOICE_VOLUME);
        //mTTSPlayer.setOption(SpeechConstants.TTS_KEY_PLAY_START_BUFFER_TIME,  "" + START_BUFFER_TIME);
        
        //mTTSPlayer.setAudioSource(new AudioSourceImpl());
		// 记录回调对象
		mTtsCallback = oRun;
		if (!sText.endsWith("。")){
			sText = sText + "。";
		}
		JNIHelper.logd("sText = " + sText);
		mTTSPlayer.playText(sText);

//		AppLogic.runOnBackGround(closeTtsWhenOverTimeRunnable, 2000);
		mIsBusy = true;
		return ERROR_SUCCESS;

	}

	@Override
	public int pause() {
		return ERROR_UNKNOW;
	}

	@Override
	public int resume() {
		return ERROR_UNKNOW;
	}

	@Override
	public void stop() {
		if (mTTSPlayer == null) {
			JNIHelper.logw("mTTSPlayer == null");
			Runnable run = new Runnable() {
				@Override
				public void run() {
					if (mTtsCallback != null) {
						mTtsCallback.onCancel();
						mIsBusy = false;
					}
				}
			};
			AppLogic.runOnBackGround(run, 0);
			return;
		}
		
		ITtsCallback callback = mTtsCallback;
		mTtsCallback = null;
		mTTSPlayer.cancel();// to do ...
		mIsBusy = false;

		if (callback != null){
			callback.onCancel();
		}
	}
  
	@Override
	public boolean isBusy() {
		return mIsBusy;
	}

	@Override
	public int setLanguage(Locale loc) {
		return ERROR_UNKNOW;
	}

	@Override
	public void setTtsModel(String ttsModel) {
		if (mTTSPlayer == null){
			return;
		}
		String backModel = "";
		if (TextUtils.isEmpty(ttsModel)){
			backModel = DEFAULT_BACKEND_MODEL;
		}else{
			backModel = ttsModel;
		}
		File f = new File(backModel);
		if (!f.exists()){
			JNIHelper.loge("model : " + ttsModel + " is not exist");
			return;
		}
		mTTSPlayer.setOption(SpeechConstants.TTS_KEY_SWITCH_BACKEND_MODEL_PATH,  backModel);
	}

}
