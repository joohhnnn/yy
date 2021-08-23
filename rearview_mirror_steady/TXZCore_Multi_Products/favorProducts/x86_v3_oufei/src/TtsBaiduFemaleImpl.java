package com.txznet.txz.component.tts.proxy;

import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.component.tts.ITts;

public class TtsBaiduFemaleImpl implements ITts {

	private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
	private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
	private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
	private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";

	// 授权文件
	private static final String LICENSE_FILE_NAME = "bd_license_txz.dat";

	// private static final String APP_ID = "8568732";
	// private static final String API_KEY = "lSKfvFi6vMEApBhCj8b9Aeam";
	// private static final String SECRET_KEY =
	// "0a5c3b93d5d23e358845932c14f306d8";

	private static final String mAppId = "8568732";
	private static final String mAssetsPath = GlobalContext.get().getApplicationInfo().dataDir + "/data/";
	private int mVoiceSpeed = 60;

	private SpeechSynthesizer mSpeechSynthesizer = null;
	private ITtsCallback mTtsCallback = null;
	private boolean isBusy = false;

	public TtsBaiduFemaleImpl() {
		super();
	}

	SpeechSynthesizerListener mTtsListener = new SpeechSynthesizerListener() {

		@Override
		public void onSynthesizeStart(String utteranceId) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSynthesizeFinish(String utteranceId) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSynthesizeDataArrived(String utteranceId, byte[] data, int progress) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeechStart(String utteranceId) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeechProgressChanged(String utteranceId, int progress) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSpeechFinish(String utteranceId) {
			isBusy = false;
			if (mTtsCallback != null) {
				ITtsCallback callback = mTtsCallback;
				mTtsCallback = null;
				callback.onSuccess();
			}
		}

		@Override
		public void onError(String utteranceId, SpeechError error) {
			isBusy = false;
			if (mTtsCallback != null) {
				ITtsCallback callback = mTtsCallback;
				mTtsCallback = null;
				callback.onError(error.code);
			}
		}
	};

	private void initialTts(Context context) {
		// 获取tts实例
		mSpeechSynthesizer = SpeechSynthesizer.getInstance();
		// 设置app上下文（必需参数）
		mSpeechSynthesizer.setContext(context);
		// 设置tts监听器
		mSpeechSynthesizer.setSpeechSynthesizerListener(mTtsListener);

		// 文本模型文件路径径，文件的绝对路径 (离线引擎使用)
		mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mAssetsPath + TEXT_MODEL_NAME);
		// 声学模型文件路径 (离线引擎使用)
		mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mAssetsPath
				+ SPEECH_FEMALE_MODEL_NAME);
		// 本地授权文件路径,如未设置将使用默认路径.设置临时授权文件路径，LICENCE_FILE_NAME请替换成临时授权文件的实际路径，仅在使用临时license文件时需要进行设置，如果在[应用管理]中开通了正式离线授权，不需要设置该参数，建议将该行代码删除（离线引擎）
		mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_LICENCE_FILE, mAssetsPath + LICENSE_FILE_NAME);
		// 请替换为语音开发者平台上注册应用得到的App ID (离线授权)
		mSpeechSynthesizer.setAppId(mAppId);
		// 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
		// mSpeechSynthesizer.setApiKey(API_KEY, SECRET_KEY);

		// 授权检测接口(只是通过AuthInfo进行检验授权是否成功。)
		// AuthInfo接口用于测试开发者是否成功申请了在线或者离线授权，如果测试授权成功了，可以删除AuthInfo部分的代码（该接口首次验证时比较耗时），不会影响正常使用（合成使用时SDK内部会自动验证授权）
		/*AuthInfo authInfo = mSpeechSynthesizer.auth(TtsMode.MIX);
		if (authInfo.isSuccess()) {
			toPrint("auth success");
		} else {
			String errorMsg = authInfo.getTtsError().getDetailMessage();
			toPrint("auth failed errorMsg=" + errorMsg);
		}*/

		// 初始化tts
		mSpeechSynthesizer.initTts(TtsMode.MIX);

		// 参数配置
		// 设置Mix模式的合成策略
		mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE,
				SpeechSynthesizer.MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI);
		// 调节播放音量 衰减
		mSpeechSynthesizer.setStereoVolume(1.0f, 1.0f);
		// 合成音量
		mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
		mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");
		mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, getSpeedValue(mVoiceSpeed));
	}

	private int initSpeaker() {
		int result = 0;
		
		// 发音人（在线引擎），可用参数为0,1,2,3。。。（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
		// mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER,"0");
		// 加载离线英文资源（提供离线英文合成功能）
		result += mSpeechSynthesizer.loadEnglishModel(mAssetsPath + ENGLISH_TEXT_MODEL_NAME, mAssetsPath
				+ ENGLISH_SPEECH_FEMALE_MODEL_NAME);
		// 切换离线发音人
//		result += mSpeechSynthesizer.loadModel(mAssetsPath + TEXT_MODEL_NAME, mAssetsPath + SPEECH_FEMALE_MODEL_NAME);
		return result;
	}

	@Override
	public int initialize(final IInitCallback oRun) {
		initialTts(GlobalContext.get());
		final int result = initSpeaker();
		Runnable run = new Runnable() {
			@Override
			public void run() {
				if (oRun != null) {
					if (result == 0) {
						Log.d("TTS", "initialize true");
						oRun.onInit(true);
					} else {
						Log.d("TTS", "initialize false");
						oRun.onInit(false);
					}
				}
			}
		};
		if (oRun != null) {
			AppLogicBase.runOnBackGround(run, 0);
		}
		Log.d("TTS", "mSpeechSynthesizer initialize");
		return ERROR_SUCCESS;
	}

	@Override
	public void release() {
		mSpeechSynthesizer.release();
		mSpeechSynthesizer = null;
	}

	@Override
	public int start(int iStream, String sText, ITtsCallback oRun) {
		if (mSpeechSynthesizer == null) {
			// JNIHelper.logw("mSpeechSynthesizer == null");
			mTtsCallback = oRun;
			Runnable run = new Runnable() {
				@Override
				public void run() {
					if (mTtsCallback != null) {
						mTtsCallback.onSuccess();
						isBusy = false;
					}
				}
			};
			AppLogicBase.runOnBackGround(run, 0);
			return 0;
		}
		mSpeechSynthesizer.setAudioStreamType(iStream);
		mTtsCallback = oRun;
		isBusy = true;
		mSpeechSynthesizer.speak(sText);
		return ERROR_SUCCESS;
	}

	@Override
	public int pause() {
		isBusy = false;
		mSpeechSynthesizer.pause();
		return ERROR_SUCCESS;
	}

	@Override
	public int resume() {
		isBusy = true;
		mSpeechSynthesizer.resume();
		return ERROR_SUCCESS;
	}

	@Override
	public void stop() {
		if (mSpeechSynthesizer == null) {
			// JNIHelper.logw("mSpeechSynthesizer == null");
			Runnable run = new Runnable() {
				@Override
				public void run() {
					if (mTtsCallback != null) {
						mTtsCallback.onCancel();
						isBusy = false;
					}
				}
			};
			AppLogicBase.runOnBackGround(run, 0);
			return;
		}
		isBusy = false;
		mSpeechSynthesizer.stop();
		if (mTtsCallback != null) {
			ITtsCallback callback = mTtsCallback;
			mTtsCallback = null;
			callback.onCancel();
		}
	}

	@Override
	public boolean isBusy() {
		return isBusy;
	}

	@Override
	public void setTtsModel(String ttsModel) {
		// TODO Auto-generated method stub
		return ;
	}

	@Override
	public int setLanguage(Locale loc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setVoiceSpeed(int speed) {
		if (speed < 0) {
			mVoiceSpeed = 0;
		} else if (speed >= 100) {
			mVoiceSpeed = 99;
		} else {
			mVoiceSpeed = speed;
		}
		if (mSpeechSynthesizer != null) {
			mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, getSpeedValue(speed));
		}
	}

	@Override
	public int getVoiceSpeed() {
		return mVoiceSpeed;
	}

	private String getSpeedValue(int voiceSpeed) {
		return "" + (voiceSpeed / 10);
	}

	@Override
	public void setOption(TTSOption oOption) {
		// TODO Auto-generated method stub
		
	}

}
