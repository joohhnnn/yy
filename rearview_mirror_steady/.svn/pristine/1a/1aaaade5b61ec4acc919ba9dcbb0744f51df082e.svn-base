package com.txznet.txz.component.wakeup.txz;

import com.txznet.comm.remote.util.RecorderUtil.RecordOption;
import com.txznet.txz.component.wakeup.IWakeup;
import com.txznet.txz.component.wakeup.mix.WakeupProxy;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.util.runnables.Runnable2;

public class WakeupMixImpl implements IWakeup {
	public final static String REMOTE_SVR_ACTION = "com.txznet.wakeup.intent.action.service.wakeupservice";
	private IWakeup mAsrWakeup = null;
	private IWakeup mWakeup = null;
	
	@Override
	public int initialize(String[] cmds, IInitCallback oRun) {
		mWakeup = WakeupProxy.getProxy(REMOTE_SVR_ACTION);
		mAsrWakeup = WakeupProxy.getProxy();
		
		mWakeup.initialize(cmds, oRun);
		mAsrWakeup.initialize(null, null);
		return 0;
	}
	
    /************************拦截固定唤醒词*****************************/
	private WakeupOption mNormalWakeupOption = null;
	private IWakeupCallback mNormalCallback = new IWakeupCallback() {
		public void onSetWordsDone() {
			WakeupOption wakeupOption = mNormalWakeupOption;
			if (wakeupOption != null){
				IWakeupCallback callback = wakeupOption.wakeupCallback;
				if (callback != null){
					callback.onSetWordsDone();
				}
			}
		};
		public void onSpeechBegin() {
			WakeupOption wakeupOption = mNormalWakeupOption;
			if (wakeupOption != null){
				IWakeupCallback callback = wakeupOption.wakeupCallback;
				if (callback != null){
					callback.onSpeechBegin();
				}
			}
		};
		public void onSpeechEnd() {
			WakeupOption wakeupOption = mNormalWakeupOption;
			if (wakeupOption != null){
				IWakeupCallback callback = wakeupOption.wakeupCallback;
				if (callback != null){
					callback.onSpeechEnd();
				}
			}
		};
		public void onVolume(int vol) {
			WakeupOption wakeupOption = mNormalWakeupOption;
			if (wakeupOption != null){
				IWakeupCallback callback = wakeupOption.wakeupCallback;
				if (callback != null){
					callback.onVolume(vol);
				}
			}
		};
		public void onWakeUp(String text, float score) {
			JNIHelper.logd("only one wakeup word : " + text);
			if (emptyWakeupWords()){
				JNIHelper.logw("empty sdk wakeup words : " + text);
				return;
			}
			WakeupOption wakeupOption = mNormalWakeupOption;
			if (wakeupOption != null){
				IWakeupCallback callback = wakeupOption.wakeupCallback;
				if (callback != null){
					callback.onWakeUp(text, score);
				}
			}
		};
		public void onWakeUp(String text, int time, float score) {
			JNIHelper.logd("only one wakeup word : " + text);
			if (emptyWakeupWords()){
				JNIHelper.logw("empty sdk wakeup words : " + text);
				return;
			}
			WakeupOption wakeupOption = mNormalWakeupOption;
			if (wakeupOption != null){
				IWakeupCallback callback = wakeupOption.wakeupCallback;
				if (callback != null){
					callback.onWakeUp(text, time, score);
				}
			}
		};
	};
	
	private WakeupOption cloneOption(WakeupOption oOption){
		WakeupOption oNewOption = new WakeupOption();
		oNewOption.mBeginSpeechTime = oOption.mBeginSpeechTime;
		oNewOption.wakeupCallback = oOption.wakeupCallback;
		return oNewOption;
	}
	
	private boolean emptyWakeupWords(){
		boolean bRet = false;
		String[] words = WakeupManager.getInstance().getWakeupKeywords_Sdk();
		if (words == null ||  words.length  == 0){
			bRet = true;
		}
		return bRet;
	}
	/************************拦截固定唤醒词*****************************/
	
	@Override
	public int start(WakeupOption oOption) {
		if (mWakeup == null || mAsrWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup + ", mAsrWakeup = " + mAsrWakeup);
			return 0;
		}
		WakeupManager.getInstance().checkUsingAsr(new Runnable2<IWakeup, WakeupOption>(mAsrWakeup, oOption) {
			@Override
			public void run() {
				mP1.start(mP2);
			}
		}, new Runnable2<IWakeup, WakeupOption>(mWakeup, oOption) {
			@Override
			public void run() {
				mNormalWakeupOption = mP2;
				WakeupOption oNewOption = cloneOption(mP2);
				oNewOption.setCallback(mNormalCallback);
				mP1.start(oNewOption);
			}
		});
		return 0;
	}

	@Override
	public int startWithRecord(IWakeupCallback oCallback, RecordOption options,
			String[] overTag) {
		return 0;
	}

	@Override
	public void stop() {
		if (mWakeup == null || mAsrWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup + ", mAsrWakeup = " + mAsrWakeup);
			return;
		}
		mWakeup.stop();
		mAsrWakeup.stop();
	}

	@Override
	public void stopWithRecord() {
		
	}

	@Override
	public void setWakeupKeywords(String[] keywords) {
		if (mWakeup == null || mAsrWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup + ", mAsrWakeup = " + mAsrWakeup);
			return;
		}
		//mWakeup.setWakeupKeywords(keywords);
		mAsrWakeup.setWakeupKeywords(keywords);
	}

	@Override
	public void setWakeupThreshold(float val) {
		if (mWakeup == null || mAsrWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup + ", mAsrWakeup = " + mAsrWakeup);
			return;
		}
		mWakeup.setWakeupThreshold(val);
		mAsrWakeup.setWakeupThreshold(val);
		
	}

	@Override
	public void enableVoiceChannel(boolean enable) {
		if (mWakeup == null || mAsrWakeup == null){
			JNIHelper.logd("mWakeup = " + mWakeup + ", mAsrWakeup = " + mAsrWakeup);
			return;
		}
		JNIHelper.logd("enable voice channel = " + enable);
		mWakeup.enableVoiceChannel(enable);
		mAsrWakeup.enableVoiceChannel(enable);
	}

}
