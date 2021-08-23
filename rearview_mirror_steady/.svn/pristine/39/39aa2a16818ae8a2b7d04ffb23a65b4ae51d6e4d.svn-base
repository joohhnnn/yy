package com.txznet.txz.component.tts.sys;

import java.util.HashMap;
import java.util.Locale;

import android.annotation.TargetApi;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.jni.JNIHelper;

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
public class TtsSysImpl implements ITts {

	TextToSpeech mTextToSpeech;
	int mCurUtteranceId = 0;
	ITtsCallback mTtsCallback;
	private int mSpeechRate = 60;

	@Override
	public int initialize(final IInitCallback oRun) {
		mTextToSpeech = new TextToSpeech(GlobalContext.get(),
				new OnInitListener() {
					@Override
					public void onInit(int status) {
						if (status == TextToSpeech.SUCCESS) {
							mTextToSpeech.setLanguage(Locale.CHINESE);
							if (oRun != null) {
								oRun.onInit(true);
							}
						} else if (status == TextToSpeech.ERROR) {
							if (oRun != null) {
								oRun.onInit(false);
							}
						}
					}
				});
		if (mTextToSpeech
				.setOnUtteranceProgressListener(new UtteranceProgressListener() {
					@Override
					public void onStart(String utteranceId) {
						if (!utteranceId.equals("" + mCurUtteranceId))
							return;
					}

					@Override
					public void onError(String utteranceId) {
						initialize(null);
						if (!utteranceId.equals("" + mCurUtteranceId))
							return;
						if (mTtsCallback != null) {
							mTtsCallback.onError(ERROR_UNKNOW);
						}
					}

					@Override
					public void onDone(String utteranceId) {
						if (!utteranceId.equals("" + mCurUtteranceId))
							return;

						if (mTtsCallback != null) {
							mTtsCallback.onSuccess();
						}
					}
				}) != TextToSpeech.SUCCESS) {
			return ERROR_UNKNOW;
		}

		return ERROR_SUCCESS;
	}

	@Override
	public void release() {
		stop();
		mTextToSpeech.shutdown();
		mTextToSpeech = null;
	}

	@Override
	public int start(int iStream, String sText, ITtsCallback oRun) {
		if (mTextToSpeech == null) {
			initialize(null);
			return ERROR_UNKNOW;
		}
		
//		if (isBusy())
//			stop();

		mTtsCallback = oRun;

		HashMap<String, String> params = new HashMap<String, String>();
		params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, ""
				+ (++mCurUtteranceId));
		params.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
			String.valueOf(iStream));
		float rate = switchRate(mSpeechRate);
		mTextToSpeech.setSpeechRate(rate);

		if (mTextToSpeech.speak(sText, TextToSpeech.QUEUE_FLUSH, params) != TextToSpeech.SUCCESS) {
			initialize(null);
			return ERROR_UNKNOW;
		}

		return ERROR_SUCCESS;
	}

	@Override
	public int pause() {
		// TODO 系统tts不支持暂停
		return ERROR_UNKNOW;
	}

	@Override
	public int resume() {
		// TODO 系统tts不支持暂停，则没有恢复
		return ERROR_UNKNOW;
	}

	@Override
	public void stop() {
		ITtsCallback callback = mTtsCallback;
		mTtsCallback = null;
		mTextToSpeech.stop(); // 先设置为防止这里会触发onDone
		if (callback != null)
			callback.onCancel();
	}

	@Override
	public boolean isBusy() {
		return mTextToSpeech.isSpeaking();
	}

	@Override
	public int setLanguage(Locale loc) {
		return mTextToSpeech.setLanguage(loc);
	}

	@Override
	public void setTtsModel(String ttsModel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVoiceSpeed(int speed) {
		if (speed < 0) {
			mSpeechRate = 0;
		} else if (speed > 100) {
			mSpeechRate = 100;
		} else {
			if (speed == 50) {
				mSpeechRate = 51;
			} else {
				mSpeechRate = speed;
			}
		}
		JNIHelper.logd("speed = " + speed + ", mSpeechRate = " + mSpeechRate);
	}

	@Override
	public int getVoiceSpeed() {
		return mSpeechRate;
	}

	private float switchRate(int nRate) {
		float rate = 1.0f;
		if (nRate < 96) {
			rate = (nRate / 6) * 0.1f;
		} else if (nRate <= 100) {
			rate = 2.0f;
		}
		JNIHelper.logd("rate = " + rate);
		return rate;
	}

}
