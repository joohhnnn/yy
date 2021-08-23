package com.txznet.txz.component.asr.mix;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.txz.ui.voice.VoiceData.SdkKeywords;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.component.asr.IAsr.AsrOption;
import com.txznet.txz.component.asr.IAsr.IImportKeywordsCallback;
import com.txznet.txz.component.wakeup.IWakeup.WakeupOption;
import com.txznet.txz.util.TXZHandler;

public interface IVoiceRecogition {
	public static interface InitListener{
		public void onInit(boolean bSuccessed);
	}
	
	public void init(InitListener oListener);
	
	public void start(AsrOption oOption);
	
	public void stop(AsrOption oOption);
	
	public void cancel(AsrOption oOption);

	public void start(WakeupOption oOption);
	
	public void stop(WakeupOption oOption);

	public void setWakeupKeywords(String[] keywords);
	
	public void importKeywords(SdkKeywords oKeywords, IImportKeywordsCallback oCallback);
	
	public void setWakeupThreshold(float val);

	public void enableVoiceChannel(boolean enable);
	
	public static class VoiceRecogitionHandler implements InvocationHandler{
		private IVoiceRecogition mProxy = null;
		private TXZHandler mWorkHandler = null;
        public VoiceRecogitionHandler(IVoiceRecogition proxy, TXZHandler workHandler){
        	mProxy = proxy;
        	mWorkHandler = workHandler;
        }
        
		@Override
		public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
			Runnable oRun = new Runnable() {
				@Override
				public void run() {
						try {
							method.invoke(mProxy, args);
						} catch (IllegalAccessException e) {
							LogUtil.loge("VoiceRecogitionHandler : " + e.toString());
						} catch (IllegalArgumentException e) {
							LogUtil.loge("VoiceRecogitionHandler : " + e.toString());
						} catch (InvocationTargetException e) {
							LogUtil.loge("VoiceRecogitionHandler : " + e.toString());
						}
				}
			};
			if (mWorkHandler != null){
				mWorkHandler.postDelayed(oRun, 0);
			}else{
				oRun.run();
			}
			return null;
		}
		
	}
}
