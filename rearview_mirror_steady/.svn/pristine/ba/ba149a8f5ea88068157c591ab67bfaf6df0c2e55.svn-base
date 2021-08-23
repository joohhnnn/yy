package com.txznet.tts.module.tts;

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
    private static TtsManager sIntance = new TtsManager();
    public static TtsManager getInstance(){
    	return sIntance;
    }
    
    private ITts mTts = null;
	private TtsManager() {
		try {
			mTts = (ITts) Class.forName("com.txznet.txz.component.tts.baidu.BaiduTts").newInstance();
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
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

	private void initEngine() {
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
