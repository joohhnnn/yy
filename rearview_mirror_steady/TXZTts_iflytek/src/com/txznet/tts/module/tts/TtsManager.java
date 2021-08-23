package com.txznet.tts.module.tts;

import android.content.Context;
import android.content.SharedPreferences;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.sdk.TXZTtsManager.TtsCallback;
import com.txznet.sdk.TXZTtsManager.TtsOption;
import com.txznet.sdk.TXZTtsManager.TtsTool;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.tts.ITts.IInitCallBack;
import com.txznet.txz.component.tts.ITts.ITtsCallBack;

public class TtsManager {
	public final static String TTS_IFLYTEK = "com.txznet.txz.component.tts.iflytek.IflytekTts";
	public final static String TTS_LELE = "com.txznet.txz.component.tts.lele.LeleTts";
    private static TtsManager sIntance = new TtsManager();
	private String strEngine = TTS_IFLYTEK;
    public static TtsManager getInstance(){
    	return sIntance;
    }
    
    private ITts mTts = null;
	private TtsManager() {
	}
    
	public void initComponent() {
		Runnable oRun = new Runnable() {
			@Override
			public void run() {
				initEngine();
			}
		};
		AppLogic.runOnBackGround(oRun, 0);
	}
    
	private ITts createTts(){
		SharedPreferences pref = AppLogic.getApp().getSharedPreferences(TTSConfig.CONFIG, Context.MODE_PRIVATE);
		int engine = pref.getInt(TTSConfig.ENGINE_TYPE, 0);
		switch (engine) {
		case TTSConfig.ENGINE_LELE:
			strEngine = TTS_LELE;
			break;
		case TTSConfig.ENGINE_IFLYTECK:
			strEngine = TTS_IFLYTEK;
			break;
		default:
			strEngine = TTS_IFLYTEK;
		}

		ITts tts = null;
		try {
			tts = (ITts) Class.forName(strEngine).newInstance();
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
		return tts;
	}
	
	private void initEngine() {
		mTts = createTts();
		if (mTts != null) {
			mTts.init(new IInitCallBack() {
				@Override
				public void onInit(boolean successed) {
					LogUtil.logd("onInit : " + successed);
					if (successed) {
						setTtsTool2Core();
					}
				}
			});
		}
	}
	
	private ITtsCallBack mSysCallBack = null;
	private com.txznet.sdk.TXZTtsManager.TtsCallback mUserCallBack = null;
    private void setTtsTool2Core(){
    	mSysCallBack = new ITtsCallBack() {
			
			@Override
			public void onError() {
				com.txznet.sdk.TXZTtsManager.TtsCallback cb = mUserCallBack;
				 mUserCallBack = null;
				 if (cb != null){
				   cb.onError();
				 }
			}
			
			@Override
			public void onSuccess() {
				com.txznet.sdk.TXZTtsManager.TtsCallback cb = mUserCallBack;
				 mUserCallBack = null;
				 if (cb != null){
				   cb.onSuccess();
				 }
			}
			
			@Override
			public void onCancel() {
				com.txznet.sdk.TXZTtsManager.TtsCallback cb = mUserCallBack;
				 mUserCallBack = null;
				 if (cb != null){
				   cb.onCancel();
				 }
			}
		};
    	TXZTtsManager.getInstance().setTtsTool(new TtsTool() {
			
			@Override
			public void start(int stream, String text, TtsCallback cb) {
				mUserCallBack = cb;
				mTts.start(stream, text, mSysCallBack);
			}
			
			@Override
			public void setOption(TtsOption arg0) {
				
			}
			
			@Override
			public void cancel() {
				mTts.stop();
			}
		});
    }
    
    public void speak(int stream, String text){
    	if (mTts != null){
    		mTts.start(stream, text, mSysCallBack);
    	}
    }
}
